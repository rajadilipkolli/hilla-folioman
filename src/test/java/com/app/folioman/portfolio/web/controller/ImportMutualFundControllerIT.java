package com.app.folioman.portfolio.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.TestData;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImportMutualFundControllerIT extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void uploadFile() throws Exception {

        long countBeforeInsert = userPortfolioValueRepository.count();
        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(
                    """
                    {
                    	"statement_period": {
                    		"from": "01-Jan-1990",
                    		"to": "20-Jun-2023"
                    	},
                    	"file_type": "CAMS",
                    	"cas_type": "DETAILED",
                    	"investor_info": {
                    		"email": "junit@email.com",
                    		"name": "Junit",
                    		"mobile": "9848022338",
                    		"address": "address"
                    	},
                    	"folios": [
                    		{
                    			"folio": "15936342 / 43",
                    			"amc": "ICICI Prudential Mutual Fund",
                    			"schemes": [
                    				{
                    					"scheme": "ICICI Prudential Nifty Next 50 Index Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Y80",
                    					"isin": "INF109K01Y80",
                    					"amfi": 120684,
                    					"advisor": "INA200005166",
                    					"type": "EQUITY",
                    					"rta": "CAMS",
                    					"close": "3801.107",
                    					"rta_code": "P8107",
                    					"open": "0.0",
                    					"close_calculated": "3801.107",
                    					"valuation": {
                    						"date": "2024-04-12",
                    						"nav": 58.1998,
                    						"value": 0.0
                    					},
                    					"transactions": [
                    						{
                    							"date": "2021-07-19",
                    							"description": "Switch In - From Liquid Fund - DP Growth - INA000006651",
                    							"amount": 24383.78,
                    							"units": 153.371,
                    							"nav": 158.9851,
                    							"balance": 153.371,
                    							"type": "SWITCH_IN",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2021-07-19",
                    							"description": "*** Stamp Duty ***",
                    							"amount": 1.22,
                    							"units": null,
                    							"nav": null,
                    							"balance": null,
                    							"type": "STAMP_DUTY_TAX",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2022-08-01",
                    							"description": "*Switch Out - To Nifty 50 Index Fund-DP Growth-BSE - , less STT",
                    							"amount": -5000.0,
                    							"units": -28.261,
                    							"nav": 176.9251,
                    							"balance": 125.11,
                    							"type": "SWITCH_OUT",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2022-08-01",
                    							"description": "*** STT Paid ***",
                    							"amount": 0.05,
                    							"units": null,
                    							"nav": null,
                    							"balance": null,
                    							"type": "STT_TAX",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2021-01-14",
                    							"description": "SIP Purchase-BSE - - INA200005166",
                    							"amount": 499.98,
                    							"units": 15.965,
                    							"nav": 31.3182,
                    							"balance": 15.965,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						}
                    					]
                    				}
                    			],
                    			"PAN": "ABCDE1234F",
                    			"KYC": "OK",
                    			"PANKYC": "OK"
                    		}
                    	]
                    }
                    """);
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(1)))
                    .andExpect(jsonPath("$.newTransactions", is(5)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }

        await().atMost(Duration.ofSeconds(45)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            long countAfterInsert = userPortfolioValueRepository.count();
            assertThat(countAfterInsert).isGreaterThan(countBeforeInsert);
        });
    }

    @Test
    @Order(2)
    void uploadFileWithNoChanges() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(false, false, false)));
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(0)))
                    .andExpect(jsonPath("$.newSchemes", is(0)))
                    .andExpect(jsonPath("$.newTransactions", is(0)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(3)
    void uploadFileWithNewFolio() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, false, false)));
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(1)))
                    .andExpect(jsonPath("$.newTransactions", is(1)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(4)
    void uploadFileWithNewScheme() throws Exception {

        long countBeforeProcessing = mfSchemeNavRepository.count();

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, false)));
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(0)))
                    .andExpect(jsonPath("$.newSchemes", is(1)))
                    .andExpect(jsonPath("$.newTransactions", is(6)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }

        await().atMost(Duration.ofSeconds(45)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            long countAfterInsert = mfSchemeNavRepository.count();
            assertThat(countAfterInsert).isGreaterThan(countBeforeProcessing);
        });
    }

    @Test
    @Order(5)
    void uploadFileWithNewTransaction() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, true)));
        }
        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(0)))
                    .andExpect(jsonPath("$.newSchemes", is(0)))
                    .andExpect(jsonPath("$.newTransactions", is(1)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(6)
    void uploadFileWithNewFolioAndSchemeAndTransaction() throws Exception {

        long countBeforeProcessing = mfSchemeNavRepository.count();

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO()));
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(2)))
                    .andExpect(jsonPath("$.newTransactions", is(3)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }

        await().atMost(Duration.ofSeconds(45)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            long countAfterInsert = mfSchemeNavRepository.count();
            assertThat(countAfterInsert).isGreaterThan(countBeforeProcessing);
        });
    }

    @Test
    @Order(7)
    void addMultipleSchemesAndTransactions() throws Exception {
        long countBeforeProcessing = mfSchemeNavRepository.count();
        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(getUploadJson());
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(3)))
                    .andExpect(jsonPath("$.newTransactions", is(59)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }

        await().atMost(Duration.ofSeconds(45)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            long countAfterInsert = mfSchemeNavRepository.count();
            assertThat(countAfterInsert).isGreaterThan(countBeforeProcessing);
        });
    }

    @Test
    @Order(8)
    void shouldOnlyProcessNewTransactionsForExistingUser() throws Exception {
        // First, create a user with initial transactions
        File initialFile = File.createTempFile("initial-file", ".json");
        try (FileWriter fileWriter = new FileWriter(initialFile)) {
            // Create a CasDTO with an initial set of transactions
            fileWriter.write(
                    """
                    {
                    	"statement_period": {
                    		"from": "01-Jan-1990",
                    		"to": "20-Jun-2023"
                    	},
                    	"file_type": "CAMS",
                    	"cas_type": "DETAILED",
                    	"investor_info": {
                    		"email": "transaction.test@email.com",
                    		"name": "Transaction Test User",
                    		"mobile": "9848022338",
                    		"address": "Test Address"
                    	},
                    	"folios": [
                    		{
                    			"folio": "22222222 / 99",
                    			"amc": "ICICI Prudential Mutual Fund",
                    			"schemes": [
                    				{
                    					"scheme": "ICICI Prudential Technology Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Z48",
                    					"isin": "INF109K01Z48",
                    					"amfi": 120594,
                    					"advisor": "INA200005166",
                    					"type": "EQUITY",
                    					"rta": "CAMS",
                    					"close": "100.000",
                    					"rta_code": "P8019",
                    					"open": "0.0",
                    					"close_calculated": "100.000",
                    					"valuation": {
                    						"date": "2024-04-12",
                    						"nav": 190.11,
                    						"value": 0.0
                    					},
                    					"transactions": [
                    						{
                    							"date": "2023-06-15",
                    							"description": "SIP Purchase-BSE",
                    							"amount": 5000.0,
                    							"units": 33.445,
                    							"nav": 149.5,
                    							"balance": 33.445,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2023-07-15",
                    							"description": "SIP Purchase-BSE",
                    							"amount": 5000.0,
                    							"units": 32.154,
                    							"nav": 155.5,
                    							"balance": 65.599,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						}
                    					]
                    				}
                    			],
                    			"PAN": "XYZAB1234C",
                    			"KYC": "OK",
                    			"PANKYC": "OK"
                    		}
                    	]
                    }
                    """);
        }

        try (FileInputStream fileInputStream = new FileInputStream(initialFile)) {
            // Upload the initial file to create the user and initial transactions
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "initial-file.json", MediaType.APPLICATION_JSON_VALUE, fileInputStream);

            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(1)))
                    .andExpect(jsonPath("$.newTransactions", is(2)))
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            initialFile.deleteOnExit();
        }

        // Now create a second file with the same user but adding one new transaction
        File updatedFile = File.createTempFile("updated-file", ".json");
        try (FileWriter fileWriter = new FileWriter(updatedFile)) {
            fileWriter.write(
                    """
                    {
                    	"statement_period": {
                    		"from": "01-Jan-1990",
                    		"to": "20-Jun-2023"
                    	},
                    	"file_type": "CAMS",
                    	"cas_type": "DETAILED",
                    	"investor_info": {
                    		"email": "transaction.test@email.com",
                    		"name": "Transaction Test User",
                    		"mobile": "9848022338",
                    		"address": "Test Address"
                    	},
                    	"folios": [
                    		{
                    			"folio": "22222222 / 99",
                    			"amc": "ICICI Prudential Mutual Fund",
                    			"schemes": [
                    				{
                    					"scheme": "ICICI Prudential Technology Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Z48",
                    					"isin": "INF109K01Z48",
                    					"amfi": 120594,
                    					"advisor": "INA200005166",
                    					"type": "EQUITY",
                    					"rta": "CAMS",
                    					"close": "100.000",
                    					"rta_code": "P8019",
                    					"open": "0.0",
                    					"close_calculated": "100.000",
                    					"valuation": {
                    						"date": "2024-04-12",
                    						"nav": 190.11,
                    						"value": 0.0
                    					},
                    					"transactions": [
                    						{
                    							"date": "2023-06-15",
                    							"description": "SIP Purchase-BSE",
                    							"amount": 5000.0,
                    							"units": 33.445,
                    							"nav": 149.5,
                    							"balance": 33.445,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2023-07-15",
                    							"description": "SIP Purchase-BSE",
                    							"amount": 5000.0,
                    							"units": 32.154,
                    							"nav": 155.5,
                    							"balance": 65.599,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2023-08-15",
                    							"description": "SIP Purchase-BSE",
                    							"amount": 5000.0,
                    							"units": 31.055,
                    							"nav": 161.0,
                    							"balance": 96.654,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						}
                    					]
                    				}
                    			],
                    			"PAN": "XYZAB1234C",
                    			"KYC": "OK",
                    			"PANKYC": "OK"
                    		}
                    	]
                    }
                    """);
        }

        try (FileInputStream fileInputStream = new FileInputStream(updatedFile)) {
            // Upload the updated file and verify only the new transaction is processed
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "updated-file.json", MediaType.APPLICATION_JSON_VALUE, fileInputStream);

            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.newFolios", is(0)))
                    .andExpect(jsonPath("$.newSchemes", is(0)))
                    .andExpect(jsonPath("$.newTransactions", is(1))) // Only one new transaction should be processed
                    .andExpect(jsonPath("$.casId", notNullValue()));
        } finally {
            updatedFile.deleteOnExit();
        }
    }

    private String getUploadJson() {
        return """
                    {"cas_type": "DETAILED", "file_type": "CAMS", "folios": [{"amc": "Sundaram Mutual Fund", "folio": "501764629146 / 0", "KYC": "OK", "PAN": "ABCDE1234F", "PANKYC": "OK", "schemes": [{"advisor": "INZ000208032", "amfi": "148507", "close": 0.0, "close_calculated": 0.0, "isin": "INF903JA1JC0", "open": 0.0, "rta": "KFINTECH", "rta_code": "176BCDG", "scheme": "SUNDARAM LARGE CAP FUND - DIRECT GROWTH - ISIN: INF903JA1JC0", "transactions": [{"amount": 5181.46, "balance": 348.739, "date": "2021-12-24", "description": "Switch Over In", "dividend_rate": null, "nav": 14.8577, "type": "SWITCH_IN", "units": 348.739}, {"amount": -885.51, "balance": 289.967, "date": "2022-02-15", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.067, "type": "REDEMPTION", "units": -58.772}, {"amount": 0.01, "balance": null, "date": "2022-02-15", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -120.16, "balance": 282.087, "date": "2022-04-01", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.2498, "type": "REDEMPTION", "units": -7.88}, {"amount": 0.01, "balance": null, "date": "2022-04-01", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -4319.08, "balance": 0.0, "date": "2022-08-23", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.3113, "type": "REDEMPTION", "units": -282.087}, {"amount": 0.04, "balance": null, "date": "2022-08-23", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}], "type": "EQUITY", "valuation": {"date": "2024-04-12", "nav": 20.6131, "value": 0}}, {"advisor": "ARN-0845", "amfi": "141565", "close": 0.0, "close_calculated": 0.0, "isin": "INF903JA1AX5", "open": 0.0, "rta": "KFINTECH", "rta_code": "1769OGP", "scheme": "SUNDARAM LONG TERM MICRO CAP TAX ADVANTAGE FUND SERIES VI - 10 YEARS - REGULAR GROWTH - ISIN: INF903JA1AX5", "transactions": [{"amount": 3000.0, "balance": 300.0, "date": "2017-09-28", "description": "Initial Purchase", "dividend_rate": null, "nav": 10.0, "type": "PURCHASE", "units": 300.0}, {"amount": -4074.56, "balance": 0.0, "date": "2021-08-16", "description": "Switch Over Out less TDS, STT", "dividend_rate": null, "nav": 13.582, "type": "SWITCH_OUT", "units": -300.0}, {"amount": 0.04, "balance": null, "date": "2021-08-16", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}], "type": "EQUITY", "valuation": {"date": "2024-04-12", "nav": 23.6892, "value": 0}}, {"advisor": "INZ000208032", "amfi": "119578", "close": 0.0, "close_calculated": 0.0, "isin": "INF903J01MV8", "open": 0.0, "rta": "KFINTECH", "rta_code": "176SFDG", "scheme": "SUNDARAM SELECT FOCUS FUND - DIRECT GROWTH - ISIN: INF903J01MV8", "transactions": [{"amount": 100.0, "balance": 0.528, "date": "2019-10-14", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 189.5308, "type": "PURCHASE_SIP", "units": 0.528}, {"amount": 100.0, "balance": 1.026, "date": "2019-11-20", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 200.7917, "type": "PURCHASE_SIP", "units": 0.498}, {"amount": 100.0, "balance": 1.516, "date": "2019-12-20", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 204.1145, "type": "PURCHASE_SIP", "units": 0.49}, {"amount": 200.0, "balance": 2.491, "date": "2020-01-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 205.0402, "type": "PURCHASE_SIP", "units": 0.975}, {"amount": 200.0, "balance": 3.472, "date": "2020-02-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 203.889, "type": "PURCHASE_SIP", "units": 0.981}, {"amount": 200.0, "balance": 4.611, "date": "2020-03-13", "description": "Purchase", "dividend_rate": null, "nav": 175.5659, "type": "PURCHASE", "units": 1.139}, {"amount": 200.0, "balance": 6.012, "date": "2020-03-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 142.7603, "type": "PURCHASE_SIP", "units": 1.401}, {"amount": 200.0, "balance": 7.231, "date": "2020-04-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 164.0448, "type": "PURCHASE_SIP", "units": 1.219}, {"amount": 200.0, "balance": 8.483, "date": "2020-05-26", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 159.7268, "type": "PURCHASE_SIP", "units": 1.252}, {"amount": 200.0, "balance": 9.618, "date": "2020-06-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 176.2505, "type": "PURCHASE_SIP", "units": 1.135}, {"amount": 199.99, "balance": 10.691, "date": "2020-07-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 186.4209, "type": "PURCHASE_SIP", "units": 1.073}, {"amount": 0.01, "balance": null, "date": "2020-07-27", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 299.99, "balance": 12.239, "date": "2020-08-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 193.84, "type": "PURCHASE_SIP", "units": 1.548}, {"amount": 0.01, "balance": null, "date": "2020-08-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 16.005, "date": "2020-09-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 185.8557, "type": "PURCHASE_SIP", "units": 3.766}, {"amount": 0.03, "balance": null, "date": "2020-09-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 19.584, "date": "2020-10-26", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 195.5818, "type": "PURCHASE_SIP", "units": 3.579}, {"amount": 0.03, "balance": null, "date": "2020-10-26", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 22.927, "date": "2020-11-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 209.3876, "type": "PURCHASE_SIP", "units": 3.343}, {"amount": 0.03, "balance": null, "date": "2020-11-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 26.007, "date": "2020-12-28", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 227.2644, "type": "PURCHASE_SIP", "units": 3.08}, {"amount": 0.03, "balance": null, "date": "2020-12-28", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": -350.0, "balance": 24.554, "date": "2021-01-13", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 240.9493, "type": "REDEMPTION", "units": -1.453}, {"amount": 0.01, "balance": null, "date": "2021-01-13", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -262.38, "balance": 23.516, "date": "2021-02-17", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 252.7836, "type": "REDEMPTION", "units": -1.038}, {"amount": 0.01, "balance": null, "date": "2021-02-17", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": 100.0, "balance": 23.929, "date": "2021-03-30", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 242.2057, "type": "PURCHASE_SIP", "units": 0.413}, {"amount": -847.71, "balance": 20.429, "date": "2021-03-31", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 242.2057, "type": "REDEMPTION", "units": -3.5}, {"amount": 0.01, "balance": null, "date": "2021-03-31", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -302.83, "balance": 19.189, "date": "2021-04-29", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 244.2236, "type": "REDEMPTION", "units": -1.24}, {"amount": 0.01, "balance": null, "date": "2021-04-29", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -319.92, "balance": 17.937, "date": "2021-05-31", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 255.5346, "type": "REDEMPTION", "units": -1.252}, {"amount": 0.01, "balance": null, "date": "2021-05-31", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -297.78, "balance": 16.802, "date": "2021-06-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 262.3738, "type": "REDEMPTION", "units": -1.135}, {"amount": 0.01, "balance": null, "date": "2021-06-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -282.85, "balance": 15.729, "date": "2021-07-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 263.619, "type": "REDEMPTION", "units": -1.073}, {"amount": 0.01, "balance": null, "date": "2021-07-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -281.04, "balance": 14.656, "date": "2021-07-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 261.9292, "type": "REDEMPTION", "units": -1.073}, {"amount": 0.01, "balance": null, "date": "2021-07-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": 4074.36, "balance": 29.439, "date": "2021-08-20", "description": "Switch Over In", "dividend_rate": null, "nav": 275.6096, "type": "SWITCH_IN", "units": 14.783}, {"amount": 0.2, "balance": null, "date": "2021-08-20", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": -132.78, "balance": 28.964, "date": "2021-08-26", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 279.5657, "type": "REDEMPTION", "units": -0.475}, {"amount": 0.01, "balance": null, "date": "2021-08-26", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -938.66, "balance": 25.726, "date": "2021-10-01", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 289.8915, "type": "REDEMPTION", "units": -3.238}, {"amount": 0.01, "balance": null, "date": "2021-10-01", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -1047.79, "balance": 22.147, "date": "2021-10-29", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 292.7626, "type": "REDEMPTION", "units": -3.579}, {"amount": 0.01, "balance": null, "date": "2021-10-29", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -1095.9, "balance": 18.276, "date": "2021-11-29", "description": "Redemption", "dividend_rate": null, "nav": 283.1064, "type": "REDEMPTION", "units": -3.871}, {"amount": -5181.46, "balance": 0.0, "date": "2021-12-24", "description": "Switch Over Out", "dividend_rate": null, "nav": 283.5119, "type": "SWITCH_OUT", "units": -18.276}], "type": "EQUITY", "valuation": {"date": "2021-12-24", "nav": 283.5119, "value": 0}}]}], "investor_info": {"address": "address", "email": "junit@email.com", "mobile": "9848022338", "name": "Junit"}, "statement_period": {"from": "01-Jan-1990", "to": "20-Jun-2023"}}
                """;
    }

    @Test
    @Order(101)
    void getPortfolio() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("asOfDate", LocalDate.now().minusDays(2).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.portfolioDetailsDTOS.size()", is(5)));
    }

    @Test
    @Order(102)
    void getPortfolioWithOutDate() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.portfolioDetailsDTOS.size()", is(5)));
    }
}
