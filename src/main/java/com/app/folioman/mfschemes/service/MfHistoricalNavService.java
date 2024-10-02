package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeDtoToEntityMapper;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.CommonConstants;
import com.app.folioman.shared.MFSchemeDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
class MfHistoricalNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfHistoricalNavService.class);

    private final MfSchemesService mfSchemeService;
    private final RestClient restClient;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;

    MfHistoricalNavService(
            MfSchemesService mfSchemeService,
            RestClient restClient,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper) {
        this.mfSchemeService = mfSchemeService;
        this.restClient = restClient;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
    }

    public String getHistoricalNav(Long schemeCode, LocalDate navDate) {
        String toDate = navDate.format(CommonConstants.FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(CommonConstants.FORMATTER_DD_MMM_YYYY);
        URI historicalNavUri = buildHistoricalNavUri(toDate, fromDate);
        Optional<MfFundScheme> bySchemeCode = this.mfSchemeService.findBySchemeCode(schemeCode);
        if (bySchemeCode.isPresent()) {
            return fetchAndProcessNavData(historicalNavUri, bySchemeCode.get().getIsin(), false, schemeCode, navDate);
        } else {
            return handleDiscontinuedScheme(schemeCode, historicalNavUri, navDate);
        }
    }

    private String handleDiscontinuedScheme(Long schemeCode, URI historicalNavUri, LocalDate navDate) {
        return fetchAndProcessNavData(historicalNavUri, null, true, schemeCode, navDate);
    }

    private String fetchAndProcessNavData(
            URI historicalNavUri, String payOut, boolean persistSchemeInfo, Long schemeCode, LocalDate navDate) {
        try {
            String allNAVsByDate = fetchHistoricalNavData(historicalNavUri);
            Reader inputString = new StringReader(Objects.requireNonNull(allNAVsByDate));
            return parseNavData(inputString, payOut, persistSchemeInfo, schemeCode, navDate);
        } catch (ResourceAccessException exception) {
            // eating as we can't do much, and it should be set when available
            LOGGER.error("Unable to load Historical Data, downstream service is down ", exception);
            return null;
        }
    }

    private String parseNavData(
            Reader inputString, String isin, boolean persistSchemeInfo, Long schemeCode, LocalDate navDate) {
        String oldSchemeId = null;
        try (BufferedReader br = new BufferedReader(inputString)) {
            String lineValue = br.readLine();
            for (int i = 0; i < 2; ++i) {
                lineValue = br.readLine();
            }
            String schemeType = lineValue;
            String amc = lineValue;
            while (lineValue != null && !StringUtils.hasText(oldSchemeId)) {
                String[] tokenize = lineValue.split(SchemeConstants.NAV_SEPARATOR);
                if (tokenize.length == 1) {
                    String tempVal = lineValue;
                    lineValue = readNextNonEmptyLine(br);
                    tokenize = lineValue.split(SchemeConstants.NAV_SEPARATOR);
                    if (tokenize.length == 1) {
                        schemeType = tempVal;
                        amc = lineValue;
                    } else {
                        amc = tempVal;
                        oldSchemeId = handleMultipleTokenLine(
                                isin, persistSchemeInfo, tokenize, oldSchemeId, amc, schemeType, schemeCode);
                    }

                } else {
                    oldSchemeId = handleMultipleTokenLine(
                            isin, persistSchemeInfo, tokenize, oldSchemeId, amc, schemeType, schemeCode);
                }
                lineValue = readNextNonEmptyLine(br);
            }
        } catch (IOException e) {
            LOGGER.error("Exception Occurred while reading response", e);
            throw new NavNotFoundException("Unable to parse for %s".formatted(schemeCode), navDate);
        }
        return oldSchemeId;
    }

    private String handleMultipleTokenLine(
            String isin,
            boolean persistSchemeInfo,
            String[] tokenize,
            String oldSchemeId,
            String amc,
            String schemeType,
            Long inputSchemeCode) {
        final Long schemeCode = Long.valueOf(tokenize[0]);
        final String payout = tokenize[2];
        if (payout.equalsIgnoreCase(isin) || schemeCode.equals(inputSchemeCode)) {
            oldSchemeId = String.valueOf(schemeCode);
            if (persistSchemeInfo) {
                String nav = tokenize[4];
                String date = tokenize[7];
                String schemeName = tokenize[1];
                MFSchemeDTO mfSchemeDTO = new MFSchemeDTO(amc, schemeCode, payout, schemeName, nav, date, schemeType);
                MfFundScheme mfScheme = mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMfFundScheme(mfSchemeDTO);
                mfSchemeService.saveEntity(mfScheme);
            }
        }
        return oldSchemeId;
    }

    private String readNextNonEmptyLine(BufferedReader br) throws IOException {
        String lineValue = br.readLine();
        while (lineValue != null && !StringUtils.hasText(lineValue)) {
            lineValue = br.readLine();
        }
        return lineValue;
    }

    private String fetchHistoricalNavData(URI historicalNavUri) {
        return restClient.get().uri(historicalNavUri).retrieve().body(String.class);
    }

    private URI buildHistoricalNavUri(String toDate, String fromDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        // tp=3 Interval Fund Schemes ( Income )
        // tp=2 Close Ended Schemes ( Income )
        // tp=1 Open Ended Schemes
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        return UriComponentsBuilder.fromUriString(historicalUrl).build().toUri();
    }
}
