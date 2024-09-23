package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeDtoToEntityMapperHelper;
import com.app.folioman.shared.CommonConstants;
import com.app.folioman.shared.LocalDateUtility;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BSEStarMasterDataService {

    private static final Logger log = LoggerFactory.getLogger(BSEStarMasterDataService.class);

    private final Pattern delimiterPattern = Pattern.compile("\\|");

    private final RestClient restClient;
    private final MfAmcService mfAmcService;
    private final MfSchemeDtoToEntityMapperHelper mfSchemeDtoToEntityMapperHelper;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public BSEStarMasterDataService(
            RestClient restClient,
            MfAmcService mfAmcService,
            MfSchemeDtoToEntityMapperHelper mfSchemeDtoToEntityMapperHelper) {
        this.restClient = restClient;
        this.mfAmcService = mfAmcService;
        this.mfSchemeDtoToEntityMapperHelper = mfSchemeDtoToEntityMapperHelper;
    }

    public Map<String, MfFundScheme> fetchBseStarMasterData(
            Map<String, Map<String, String>> amfiDataMap, Map<String, String> amfiCodeIsinMapping)
            throws IOException, CsvException {
        log.info("BSE Master data Downloading...");

        // Step 1: Initial GET request to download the page
        String response = restClient
                .get()
                .uri("https://bsestarmf.in/RptSchemeMaster.aspx")
                .header(HttpHeaders.USER_AGENT, "folioman-java-httpclient/0.0.1")
                .retrieve()
                .body(String.class);

        // Step 2: Parse the HTML response to extract hidden form fields
        Map<String, String> formData = getExtractedFormData(response);

        // Step 4: POST request to submit the form and download the master data
        String bseMasterData = restClient
                .post()
                .uri("https://bsestarmf.in/RptSchemeMaster.aspx")
                .header(HttpHeaders.USER_AGENT, "folioman-java-httpclient/0.0.1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.ALL)
                .body(ofFormData(formData)) // Form data encoded and passed in the request bseMasterData
                .retrieve()
                .body(String.class);

        log.info("BSE Master data downloaded successfully.");

        return parseResponseText(bseMasterData, amfiDataMap, amfiCodeIsinMapping);
    }

    private Map<String, MfFundScheme> parseResponseText(
            String bseMasterData, Map<String, Map<String, String>> amfiDataMap, Map<String, String> amfiCodeIsinMapping)
            throws IOException, CsvException {
        Map<String, MfFundScheme> masterData = new HashMap<>();
        Map<String, MfFundScheme> isinMasterData = new HashMap<>();

        // Process BSE Master Data
        if (bseMasterData != null && !bseMasterData.isEmpty()) {
            try (StringReader stringReader = new StringReader(bseMasterData);
                    CSVReader csvReader = new CSVReader(stringReader)) {

                String[] headerRow = csvReader.readNext(); // Read first row as headers
                if (headerRow == null) return masterData; // If empty, return immediately

                Map<String, Integer> headerIndexKeyMap = mapHeaders(headerRow); // Map headers once

                // Read and process rows one by one, avoiding readAll()
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                String[] rows;
                while ((rows = csvReader.readNext()) != null) {
                    String[] row = getDataAsArray(rows);
                    // Submit each task as a CompletableFuture for parallel execution
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        String isin = row[headerIndexKeyMap.get("ISIN")].strip();
                        String amfiCode = amfiCodeIsinMapping.get(isin);

                        if (amfiCode != null) {
                            // Ensure processSchemeData is thread-safe or synchronized if required
                            processSchemeData(
                                    row, headerIndexKeyMap, masterData, isinMasterData, amfiDataMap, amfiCode);
                        }
                    });

                    // Add the future to the list
                    futures.add(future);
                }

                // Wait for all tasks to complete by combining all CompletableFutures
                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

                // This ensures that the code waits for all parallel tasks to finish
                allOf.join();
            }
        }

        // Fill remaining data from amfiCodeIsinMapping if not present in BSE Master Data
        for (Map.Entry<String, String> amfiEntry : amfiCodeIsinMapping.entrySet()) {
            String amfiCode = amfiEntry.getValue();
            if (!masterData.containsKey(amfiCode)) {
                processAmfiFallback(amfiCode, amfiEntry.getKey(), masterData, amfiDataMap);
            }
        }
        return masterData;
    }

    private void processAmfiFallback(
            String amfiCode,
            String isin,
            Map<String, MfFundScheme> masterData,
            Map<String, Map<String, String>> amfiDataMap) {
        MfFundScheme fallbackScheme = new MfFundScheme();
        fallbackScheme.setAmfiCode(Long.valueOf(amfiCode));
        fallbackScheme.setIsin(isin);
        Map<String, String> amfiSchemeData = amfiDataMap.get(amfiCode);
        if (amfiSchemeData != null) {
            fallbackScheme.setName(amfiSchemeData.get("Scheme Name"));
            // Process AMC
            String amcName = amfiSchemeData.get("AMC").strip();
            MfAmc amc = mfAmcService.findByName(amcName);
            if (amc == null) {
                amc = new MfAmc();
                amc.setName(amcName);
                amc.setCode(amcName);
                amc = mfAmcService.saveMfAmc(amc);
            }
            fallbackScheme.setAmc(amc);
            setMfSchemeCategory(amfiSchemeData, fallbackScheme);
        } else {
            log.error("amfiSchemeData is null");
        }
        masterData.put(amfiCode, fallbackScheme);
    }

    private void processSchemeData(
            String[] row,
            Map<String, Integer> headerIndexKeyMap,
            Map<String, MfFundScheme> masterData,
            Map<String, MfFundScheme> isinMasterData,
            Map<String, Map<String, String>> amfiDataMap,
            String amfiCode) {

        String isin = row[headerIndexKeyMap.get("ISIN")].strip();
        String schemeCode = row[headerIndexKeyMap.get("Scheme Code")].strip();

        // Skip if better data exists
        if (isinMasterData.containsKey(isin)
                && isinMasterData.get(isin).getAmcCode().length() <= schemeCode.length()) {
            return;
        }

        MfFundScheme scheme = createMfFundScheme(row, headerIndexKeyMap, amfiDataMap.get(amfiCode));

        // Process AMC
        String amcCode = row[headerIndexKeyMap.get("AMC Code")].strip();
        MfAmc amc = mfAmcService.findByCode(amcCode);
        if (amc == null) {
            reentrantLock.lock(); // Acquiring the lock
            try {
                // Double-check within the locked section
                amc = mfAmcService.findByCode(amcCode);
                if (amc == null) {
                    amc = new MfAmc();
                    amc.setName(amfiDataMap.get(amfiCode).get("AMC"));
                    amc.setCode(amcCode);
                    amc = mfAmcService.saveMfAmc(amc);
                }
            } finally {
                reentrantLock.unlock(); // Ensure the lock is released in the finally block
            }
        }
        scheme.setAmc(amc);

        // Add to masterData and isinMasterData
        masterData.put(amfiCode, scheme);
        isinMasterData.put(isin, scheme);
    }

    private MfFundScheme createMfFundScheme(
            String[] row, Map<String, Integer> headerIndexKeyMap, Map<String, String> amfiSchemeData) {
        MfFundScheme scheme = new MfFundScheme();
        scheme.setSid(Integer.parseInt(row[headerIndexKeyMap.get("Unique No")]));
        scheme.setName(row[headerIndexKeyMap.get("Scheme Name")].strip());
        scheme.setRta(row[headerIndexKeyMap.get("RTA Agent Code")].strip());
        scheme.setPlan(row[headerIndexKeyMap.get("Scheme Plan")].contains("DIRECT") ? "DIRECT" : "REGULAR");
        scheme.setRtaCode(row[headerIndexKeyMap.get("Channel Partner Code")].strip());
        scheme.setAmcCode(row[headerIndexKeyMap.get("AMC Scheme Code")].strip());
        scheme.setIsin(row[headerIndexKeyMap.get("ISIN")].strip());
        scheme.setStartDate(LocalDateUtility.parse(
                row[headerIndexKeyMap.get("Start Date")].strip(), CommonConstants.FORMATTER_MMM_D_YYYY));
        scheme.setEndDate(LocalDateUtility.parse(
                row[headerIndexKeyMap.get("End Date")].strip(), CommonConstants.FORMATTER_MMM_D_YYYY));

        if (amfiSchemeData != null) {
            setMfSchemeCategory(amfiSchemeData, scheme);
            String endDate = amfiSchemeData.get("Closure Date");
            if (endDate != null && !endDate.isBlank()) {
                LocalDate closureDate = LocalDateUtility.parse(endDate);
                if (closureDate.isBefore(LocalDate.now().minusWeeks(1))) {
                    scheme.setEndDate(closureDate);
                }
            }
        }
        return scheme;
    }

    private void setMfSchemeCategory(Map<String, String> amfiSchemeData, MfFundScheme mfFundScheme) {
        String catStr = amfiSchemeData.get("Scheme Category");
        String catSchemeType = amfiSchemeData.get("Scheme Type");
        String category, subcategory;

        if (catStr != null && catStr.contains("-")) {
            category = catStr.substring(0, catStr.indexOf("-")).strip();
            subcategory = catStr.substring(catStr.indexOf("-") + 1).strip();
        } else {
            category = catStr;
            subcategory = null;
        }

        // Check if category already exists in cache
        MFSchemeType mfSchemeTypeEntity =
                mfSchemeDtoToEntityMapperHelper.findOrCreateMFSchemeTypeEntity(catSchemeType, category, subcategory);
        mfFundScheme.setMfSchemeType(mfSchemeTypeEntity);
    }

    public Map<String, Integer> mapHeaders(String[] rows) {
        String[] headers = getDataAsArray(rows);

        // Create a map of index and header values
        return IntStream.range(0, headers.length)
                .boxed()
                .collect(Collectors.toMap(
                        index -> headers[index], // Value is the header at that index
                        index -> index // Key is the index
                        ));
    }

    private String[] getDataAsArray(String[] rows) {
        // Assume the first row contains the data
        return delimiterPattern.split(rows[0]); // Split using the precompiled pattern
    }

    private Map<String, String> getExtractedFormData(String response) throws IOException {
        Document doc = Jsoup.parse(response);
        Element formElement = doc.getElementById("frmOrdConfirm");

        if (formElement == null) {
            throw new IOException("Unable to find the form with ID 'frmOrdConfirm'.");
        }

        // Step 3: Extract hidden form fields and their values
        Map<String, String> formData = new HashMap<>();
        formData.put("ddlTypeOption", "SCHEMEMASTERPHYSICAL"); // Form dropdown value

        // Include any other hidden input fields
        formElement.select("input[type=hidden]").forEach(input -> {
            String name = input.attr("name");
            String value = input.attr("value");
            formData.put(name, value);
        });

        // Additional fields for form submission
        formData.put("btnText", "Export to Text");
        return formData;
    }

    /**
     * Helper method to encode form data for URL submission.
     */
    private String ofFormData(Map<String, String> data) {
        return data.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * Helper method to safely encode form parameters using UTF-8 encoding.
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
