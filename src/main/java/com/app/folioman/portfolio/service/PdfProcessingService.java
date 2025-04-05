package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.models.request.CasDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PdfProcessingService.class);
    private static final String CASPARSER_COMMAND = "casparser";
    private static boolean casparserChecked = false;

    private final PortfolioServiceHelper portfolioServiceHelper;

    public PdfProcessingService(PortfolioServiceHelper portfolioServiceHelper) {
        this.portfolioServiceHelper = portfolioServiceHelper;
    }
    
    /**
     * Checks if casparser CLI is installed and attempts to install it if not found.
     * 
     * @return true if casparser is available (either already installed or successfully installed), false otherwise
     */
    private boolean ensureCasparserInstalled() {
        if (casparserChecked) {
            return isCasparserAvailable();
        }
        
        log.info("Checking if casparser CLI is installed...");
        
        if (isCasparserAvailable()) {
            log.info("casparser CLI is already installed");
            casparserChecked = true;
            return true;
        }
        
        log.info("casparser CLI is not installed. Attempting to install it...");
        
        try {
            // Attempt to install casparser using pip
            ProcessBuilder installProcess = new ProcessBuilder("pip", "install", "casparser");
            installProcess.redirectErrorStream(true);
            Process process = installProcess.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("pip install output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("Failed to install casparser. Exit code: {}", exitCode);
                return false;
            }
            
            // Check again if installation was successful
            boolean installed = isCasparserAvailable();
            casparserChecked = true;
            
            if (installed) {
                log.info("Successfully installed casparser CLI");
            } else {
                log.error("casparser installation seemed to succeed but command is still not available");
            }
            
            return installed;
            
        } catch (IOException | InterruptedException e) {
            log.error("Error while trying to install casparser: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
    
    /**
     * Checks if casparser command is available in the system path.
     * 
     * @return true if casparser is available, false otherwise
     */
    private boolean isCasparserAvailable() {
        try {
            ProcessBuilder checkProcess = new ProcessBuilder(CASPARSER_COMMAND, "--version");
            checkProcess.redirectErrorStream(true);
            Process process = checkProcess.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                log.debug("casparser version check output: {}", output);
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
        log.info(
                "Converting password-protected PDF CAS file to Object using python casparser cli: {}",
                pdfFile.getOriginalFilename());

        // Check if casparser is installed and try to install it if not
        if (!ensureCasparserInstalled()) {
            throw new IOException("casparser CLI is not installed and automatic installation failed. Please install manually using 'pip install casparser'");
        }

        // Create temporary files for the PDF and JSON output
        Path tempPdfPath = Files.createTempFile("cas_", ".pdf");
        Path tempJsonPath = Files.createTempFile("pdf_parsed_", ".json");

        try {
            // Save the uploaded file to a temporary location
            Files.copy(pdfFile.getInputStream(), tempPdfPath, StandardCopyOption.REPLACE_EXISTING);

            // Build the command to run casparser
            ProcessBuilder processBuilder = new ProcessBuilder(
                    CASPARSER_COMMAND, tempPdfPath.toString(), "-p", password, "-o", tempJsonPath.toString());

            processBuilder.redirectErrorStream(true);

            // Execute the command
            Process process = processBuilder.start();

            // Read and log the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("casparser output: {}", line);
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("casparser failed with exit code: " + exitCode);
            }

            // Read the JSON file created by casparser
            byte[] jsonContent = Files.readAllBytes(tempJsonPath);

            // Parse the JSON Byte Array and convert to CasDTO
            return portfolioServiceHelper.readValue(jsonContent, CasDTO.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("casparser process interrupted", e);
        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(tempPdfPath);
                Files.deleteIfExists(tempJsonPath);
            } catch (IOException e) {
                log.warn("Could not delete temporary files: {}", e.getMessage());
            }
        }
    }
}
