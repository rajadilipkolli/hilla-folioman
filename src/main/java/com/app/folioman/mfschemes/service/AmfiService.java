package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.config.MfSchemesProperties;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
    private final MfSchemesProperties mfSchemesProperties;

    AmfiService(
            RestClient restClient,
            ApplicationProperties applicationProperties,
            MfSchemesProperties mfSchemesProperties) {
        this.restClient = restClient;
        this.applicationProperties = applicationProperties;
        this.mfSchemesProperties = mfSchemesProperties;
    }

    public void fetchAmfiSchemeData(Consumer<Map<String, Map<String, String>>> batchProcessor)
            throws IOException, CsvException {
        LOGGER.info("Downloading AMFI scheme data...");

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
            return;
        }

        if (csvContent == null || csvContent.isBlank()) {
            throw new IllegalStateException("Invalid response! No data received.");
        }

        // Read the CSV data using OpenCSV's CSVReader
        try (StringReader stringReader = new StringReader(csvContent);
                CSVReader csvReader = new CSVReader(stringReader)) {

            String[] headers = csvReader.readNext();
            if (headers == null) {
                return;
            }

            int batchSize = mfSchemesProperties.getCsvProcessingBatchSize();
            Map<String, Map<String, String>> currentBatch = new HashMap<>(batchSize);

            // Process each row incrementally
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                // Skip malformed rows with insufficient columns
                if (row.length < 2 || row.length < headers.length) {
                    LOGGER.warn("Skipping malformed row with {} columns (expected {})", row.length, headers.length);
                    continue;
                }
                // Get the 'Code' column value
                String code = row[1].strip();

                // Create a map for each row with header-value pairs
                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    rowData.put(headers[j].strip(), row[j].strip());
                }

                // Store the row data in the batch map using the code as key
                currentBatch.put(code, rowData);

                if (currentBatch.size() >= batchSize) {
                    batchProcessor.accept(currentBatch);
                    currentBatch = new HashMap<>(batchSize);
                }
            }

            // Flush final batch
            if (!currentBatch.isEmpty()) {
                batchProcessor.accept(currentBatch);
            }
        }
    }
}
