package com.example.application.mfschemes.bootstrap;

import com.example.application.mfschemes.MFSchemeDTO;
import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.mapper.MfSchemeDtoToEntityMapper;
import com.example.application.mfschemes.service.MfSchemeService;
import com.example.application.mfschemes.util.SchemeConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    private final RestClient restClient;
    private final MfSchemeService mfSchemeService;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;

    public Initializer(
            RestClient restClient,
            MfSchemeService mfSchemeService,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper) {
        this.restClient = restClient;
        this.mfSchemeService = mfSchemeService;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void handleApplicationStartedEvent() {
        LOGGER.info("Loading all Mutual Funds");
        long start = System.currentTimeMillis();
        try {
            String allNAVs = restClient
                    .get()
                    .uri(SchemeConstants.AMFI_WEBSITE_LINK)
                    .headers(HttpHeaders::clearContentHeaders)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .retrieve()
                    .body(String.class);
            Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
            List<MFSchemeDTO> chopArrayList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(inputString)) {
                String lineValue = br.readLine();
                for (int i = 0; i < 2; ++i) {
                    lineValue = br.readLine();
                }
                String schemeType = lineValue;
                String amc = lineValue;
                while (lineValue != null) {
                    boolean nonAmcRow = true;
                    String[] tokenize = lineValue.split(SchemeConstants.NAV_SEPARATOR);
                    boolean processRowByForce = false;
                    if (tokenize.length == 1) {
                        nonAmcRow = false;
                        String tempVal = lineValue;
                        lineValue = br.readLine();
                        if (!StringUtils.hasText(lineValue)) {
                            lineValue = br.readLine();
                            tokenize = lineValue.split(SchemeConstants.NAV_SEPARATOR);
                            if (tokenize.length == 1) {
                                schemeType = tempVal;
                                amc = lineValue;
                            } else {
                                amc = tempVal;
                                processRowByForce = true;
                            }
                        }
                    }
                    if (nonAmcRow || processRowByForce) {
                        final String schemecode = tokenize[0];
                        final String payout = tokenize[1];
                        final String schemename = tokenize[3];
                        final String nav = tokenize[4];
                        final String date = tokenize[5];
                        final MFSchemeDTO tempObj = new MFSchemeDTO(
                                amc, Long.valueOf(schemecode), payout, schemename, nav, date, schemeType);
                        chopArrayList.add(tempObj);
                    }
                    lineValue = br.readLine();
                    if (!StringUtils.hasText(lineValue)) {
                        lineValue = br.readLine();
                    }
                }
            }
            LOGGER.info(
                    "All Funds loaded in {} milliseconds, total funds loaded :{}",
                    (System.currentTimeMillis() - start),
                    chopArrayList.size());
            if (mfSchemeService.count() != chopArrayList.size()) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("saving fundNames");
                List<Long> schemeCodesList = mfSchemeService.findAllSchemeIds();
                List<CompletableFuture<MFScheme>> completableFutureList = chopArrayList.stream()
                        .filter(scheme -> !schemeCodesList.contains(scheme.schemeCode())) // Filter out existing schemes
                        .map(mfSchemeDTO -> CompletableFuture.supplyAsync(
                                () -> mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMFSchemeEntity(mfSchemeDTO)))
                        .toList();
                List<MFScheme> newMfSchemeList = completableFutureList.stream()
                        .map(CompletableFuture::join)
                        .toList();
                if (!newMfSchemeList.isEmpty()) {
                    LOGGER.info("found {} new funds, hence inserting", newMfSchemeList.size());
                    mfSchemeService.saveAllEntities(newMfSchemeList);
                }
                stopWatch.stop();
                LOGGER.info("saved in db in : {} sec", stopWatch.getTotalTimeSeconds());
            }
        } catch (HttpClientErrorException | ResourceAccessException | IOException httpClientErrorException) {
            LOGGER.error("Failed to load all Funds", httpClientErrorException);
        }
    }
}
