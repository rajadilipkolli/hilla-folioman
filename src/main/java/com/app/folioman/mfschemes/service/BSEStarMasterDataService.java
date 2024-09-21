package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import com.app.folioman.shared.CommonConstants;
import com.app.folioman.shared.LocalDateUtility;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private final MfAmcRepository mfAmcRepository;

    public BSEStarMasterDataService(RestClient restClient, MfAmcRepository mfAmcRepository) {
        this.restClient = restClient;
        this.mfAmcRepository = mfAmcRepository;
    }

    public Map<String, MfFundScheme> fetchBseStarMasterData() throws IOException {
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

        return parseResponseText(bseMasterData);
    }

    private Map<String, MfFundScheme> parseResponseText(String bseMasterData) {
        Map<String, MfFundScheme> masterData = new HashMap<>();

        try (StringReader stringReader = new StringReader(bseMasterData);
                CSVReader csvReader = new CSVReader(stringReader)) {

            List<String[]> rows = csvReader.readAll();
            // First row is the header
            Map<String, Integer> headerIndexKeyMap = mapHeaders(rows.getFirst());

            // Process each row (starting from the second row)
            for (int i = 1; i < rows.size(); i++) {
                String[] row = getDataAsArray(rows.get(i));
                // CSV Parsing logic
                String isin = row[headerIndexKeyMap.get("ISIN")].strip();
                String code = row[headerIndexKeyMap.get("Scheme Code")].strip();
                String schemeName = row[headerIndexKeyMap.get("Scheme Name")].strip();
                String amcCode = row[headerIndexKeyMap.get("AMC Code")].strip();

                if (masterData.containsKey(isin)
                        && masterData.get(isin).getAmcCode().length() <= code.length()) {
                    continue;
                }

                MfFundScheme scheme = new MfFundScheme();
                scheme.setSid(Integer.parseInt(row[headerIndexKeyMap.get("Unique No")]));
                scheme.setName(schemeName);
                scheme.setRta(row[headerIndexKeyMap.get("RTA Agent Code")].strip());
                scheme.setPlan(row[headerIndexKeyMap.get("Scheme Plan")].contains("DIRECT") ? "DIRECT" : "REGULAR");
                scheme.setRtaCode(row[headerIndexKeyMap.get("Channel Partner Code")].strip());
                scheme.setAmcCode(row[headerIndexKeyMap.get("AMC Scheme Code")].strip());
                scheme.setIsin(isin);
                scheme.setStartDate(LocalDateUtility.parse(
                        row[headerIndexKeyMap.get("Start Date")].strip(), CommonConstants.FORMATTER_MMM_D_YYYY));
                scheme.setEndDate(LocalDateUtility.parse(
                        row[headerIndexKeyMap.get("End Date")].strip(), CommonConstants.FORMATTER_MMM_D_YYYY));
                // Find or create AMC
                MfAmc amc = mfAmcRepository.findByCode(amcCode);
                if (amc == null) {
                    amc = new MfAmc();
                    // TODO
                    // amc.setName(amcCode);
                    amc.setCode(amcCode);
                    mfAmcRepository.save(amc);
                }
                scheme.setAmc(amc);

                // Find or create AMC
                masterData.put(isin, scheme);
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
        return masterData;
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
        Map<String, String> formData = new LinkedHashMap<>();
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
