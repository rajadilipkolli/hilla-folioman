package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional(readOnly = true)
class AmfiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmfiService.class);

    private final RestClient restClient;
    private final ApplicationProperties applicationProperties;

    AmfiService(RestClient restClient, ApplicationProperties applicationProperties) {
        this.restClient = restClient;
        this.applicationProperties = applicationProperties;
    }

    public Map<String, Map<String, String>> fetchAmfiSchemeData() throws IOException, CsvException {
        LOGGER.info("Downloading AMFI scheme data...");
        // Prepare a Map to store the scheme data
        Map<String, Map<String, String>> data = new HashMap<>();

        // Fetch the CSV content from the remote server
        String csvContent;
        try {
            csvContent = restClient
                    .get()
                    .uri(applicationProperties.getAmfi().getScheme().getDataUrl())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        LOGGER.error("Failed to retrieve data. Status: {} ", response.getStatusCode());
                    })
                    .body(String.class);
        } catch (Exception e) {
            // website down scenario
            LOGGER.error("Unable to download data", e);
            return data;
        }

        if (csvContent == null || csvContent.isBlank()) {
            throw new IllegalStateException("Invalid response! No data received.");
        }

        // Read the CSV data using OpenCSV's CSVReader
        try (StringReader stringReader = new StringReader(csvContent);
                CSVReader csvReader = new CSVReader(stringReader)) {

            String[] headers = csvReader.readNext();
            if (headers == null) {
                return data;
            }

            // Process each row incrementally
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                // Get the 'Code' column value
                String code = row[1].strip();

                // Create a map for each row with header-value pairs
                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    rowData.put(headers[j].strip(), row[j].strip());
                }

                // Store the row data in the main map using the code as key
                data.put(code, rowData);
            }
        }

        return data; // Return the map with scheme data
    }
}
