package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.pythonbridge.PythonBridgeProperties;
import com.app.folioman.pythonbridge.PythonCommands;
import com.app.folioman.pythonbridge.PythonExecutor;
import com.app.folioman.pythonbridge.PythonResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfProcessingService.class);

    private static final AtomicBoolean CASPARSER_CHECKED = new AtomicBoolean(false);

    private final PortfolioServiceHelper portfolioServiceHelper;
    private final PythonExecutor pythonExecutor;
    private final PythonBridgeProperties pythonProperties;

    PdfProcessingService(
            PortfolioServiceHelper portfolioServiceHelper,
            PythonExecutor pythonExecutor,
            PythonBridgeProperties pythonProperties) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.pythonExecutor = pythonExecutor;
        this.pythonProperties = pythonProperties;
    }

    /**
     * Checks if casparser CLI is installed and attempts to install it if not found.
     *
     * @return true if casparser is available (either already installed or successfully installed), false otherwise
     */
    private boolean ensureCasparserInstalled() {
        if (CASPARSER_CHECKED.get()) {
            return true;
        }

        LOGGER.info("Checking if casparser CLI is installed...");

        if (isCasparserAvailable()) {
            LOGGER.info("casparser CLI is already installed");
            CASPARSER_CHECKED.set(true);
            return true;
        }

        LOGGER.error("casparser CLI is not installed. Please install it as part of the deployment process.");
        return false;
    }

    /**
     * Checks if casparser command is available in the system path.
     *
     * @return true if casparser is available, false otherwise
     */
    private boolean isCasparserAvailable() {
        try {
            PythonResult result = pythonExecutor.execute(
                    PythonCommands.cli(pythonProperties.casparser().executable(), "--version"));
            LOGGER.debug("casparser version check output: {}", result.asText());
            return result.isSuccess();
        } catch (Exception e) {
            LOGGER.debug("casparser availability check failed", e);
            return false;
        }
    }

    /**
     * Converts a password-protected CAS PDF file to a CasDTO object using the casparser Python CLI.
     *
     * @param pdfFile The PDF file to convert
     * @param password The password to unlock the PDF
     * @return A CasDTO object containing the parsed data
     * @throws IOException If there is an error reading or parsing the PDF
     */
    public CasDTO convertPdfCasToJson(MultipartFile pdfFile, String password) throws IOException {
        LOGGER.info(
                "Converting password-protected PDF CAS file to CasDTO Object using python casparser cli: {}",
                pdfFile.getOriginalFilename());

        // Check if casparser is installed and try to install it if not
        if (!ensureCasparserInstalled()) {
            throw new IOException("casparser CLI is not installed, this is prerequisite ");
        }

        // Create temporary files for the PDF and JSON output
        Path tempPdfPath = Files.createTempFile("cas_", ".pdf");
        Path tempJsonPath = Files.createTempFile("pdf_parsed_", ".json");

        try {
            // Save the uploaded file to a temporary location
            Files.copy(pdfFile.getInputStream(), tempPdfPath, StandardCopyOption.REPLACE_EXISTING);

            // Execute casparser via the centralized Python executor framework
            pythonExecutor
                    .execute(PythonCommands.cli(
                            pythonProperties.casparser().executable(),
                            tempPdfPath.toString(),
                            "-p",
                            password,
                            "-o",
                            tempJsonPath.toString()))
                    .orThrow();

            // Read the JSON file created by casparser
            byte[] jsonContent = Files.readAllBytes(tempJsonPath);

            // Parse the JSON Byte Array and convert to CasDTO
            return portfolioServiceHelper.readValue(jsonContent, CasDTO.class);
        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(tempPdfPath);
                Files.deleteIfExists(tempJsonPath);
            } catch (IOException e) {
                LOGGER.warn("Could not delete temporary files: {}", e.getMessage());
            }
        }
    }
}
