package sh.gepetto.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskResult;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating JUnit XML reports from Gepetto test results
 */
@Service
public class JUnitReportService {
    private static final Logger logger = LoggerFactory.getLogger(JUnitReportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Save a test result as a JUnit XML report
     *
     * @param result The test result to save
     * @return The path to the saved report file
     * @throws IOException If there is an error writing the file
     */
    public Path saveReport(TaskResult result) throws IOException {
        // Create results directory structure
        String timestamp = result.getExecutionTime().format(TIMESTAMP_FORMATTER);
        String taskName = sanitizeFileName(result.getTask().getName());
        
        Path resultsDir = Path.of("gepetto", "results", taskName, timestamp);
        Files.createDirectories(resultsDir);
        
        // Create the XML report
        String xmlContent = generateJUnitXml(result);
        
        // Write the XML file
        Path reportFile = resultsDir.resolve("junit-report.xml");
        try (FileWriter writer = new FileWriter(reportFile.toFile())) {
            writer.write(xmlContent);
        }
        
        // Create JSON report for the full result data
        Path jsonFile = resultsDir.resolve("result.json");
        saveJsonReport(result, jsonFile);
        
        logger.info("Saved JUnit report to {}", reportFile);
        return reportFile;
    }
    
    /**
     * Save the test result as a JSON file
     */
    private void saveJsonReport(TaskResult result, Path jsonFile) throws IOException {
        // Simple JSON serialization
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"testName\": \"").append(escapeJson(result.getTask().getName())).append("\",\n");
        json.append("  \"testDescription\": \"").append(escapeJson(result.getTask().getDescription())).append("\",\n");
        json.append("  \"status\": \"").append(result.getStatus()).append("\",\n");
        json.append("  \"executionTime\": \"").append(result.getExecutionTime().format(ISO_FORMATTER)).append("\",\n");
        json.append("  \"executionDurationMs\": ").append(result.getExecutionDurationMs()).append(",\n");
        
        if (result.getErrorMessage() != null) {
            json.append("  \"errorMessage\": \"").append(escapeJson(result.getErrorMessage())).append("\",\n");
        }
        
        json.append("  \"stepResults\": [\n");
        for (int i = 0; i < result.getStepResults().size(); i++) {
            StepResult step = result.getStepResults().get(i);
            json.append("    {\n");
            json.append("      \"step\": \"").append(escapeJson(step.getStep())).append("\",\n");
            json.append("      \"status\": \"").append(step.getStatus()).append("\"");
            
            if (step.getDetails() != null) {
                json.append(",\n      \"details\": \"").append(escapeJson(step.getDetails())).append("\"");
            }
            
            if (step.getScreenshot() != null) {
                json.append(",\n      \"screenshot\": \"").append(escapeJson(step.getScreenshot())).append("\"");
            }
            
            json.append("\n    }");
            if (i < result.getStepResults().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        
        Files.writeString(jsonFile, json.toString());
    }
    
    /**
     * Generate a JUnit XML report from a test result
     */
    private String generateJUnitXml(TaskResult result) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        // Calculate test statistics
        int failures = 0;
        int errors = 0;
        int tasks = result.getStepResults().size();
        
        if (result.getStatus() == TaskResult.Status.FAILED) {
            failures = 1;
        } else if (result.getStatus() == TaskResult.Status.ERROR) {
            errors = 1;
        }
        
        // Build the testsuite element
        xml.append("<testsuite");
        xml.append(" name=\"").append(escapeXml(result.getTask().getName())).append("\"");
        xml.append(" tasks=\"").append(tasks).append("\"");
        xml.append(" failures=\"").append(failures).append("\"");
        xml.append(" errors=\"").append(errors).append("\"");
        xml.append(" skipped=\"0\"");
        xml.append(" hostname=\"gepetto\"");
        xml.append(" time=\"").append(result.getExecutionDurationMs() / 1000.0).append("\"");
        xml.append(" timestamp=\"").append(result.getExecutionTime().format(ISO_FORMATTER)).append("\"");
        xml.append(">\n");
        
        // Add test properties
        xml.append("  <properties>\n");
        xml.append("    <property name=\"testName\" value=\"").append(escapeXml(result.getTask().getName())).append("\"/>\n");
        xml.append("    <property name=\"testDescription\" value=\"").append(escapeXml(result.getTask().getDescription())).append("\"/>\n");
        xml.append("  </properties>\n");
        
        // Add test cases (steps)
        for (StepResult step : result.getStepResults()) {
            xml.append("  <testcase");
            xml.append(" name=\"").append(escapeXml(step.getStep())).append("\"");
            xml.append(" classname=\"sh.gepetto.task\"");
            
            // If we have timing info for individual steps, we could add it here
            xml.append(">\n");
            
            // Add failure or error information if any
            if (step.getStatus() == TaskResult.Status.FAILED) {
                xml.append("    <failure");
                xml.append(" message=\"Step failed\"");
                xml.append(" type=\"sh.gepetto.StepFailure\"");
                xml.append(">");
                if (step.getDetails() != null) {
                    xml.append(escapeXml(step.getDetails()));
                }
                xml.append("</failure>\n");
            } else if (step.getStatus() == TaskResult.Status.ERROR) {
                xml.append("    <error");
                xml.append(" message=\"Step error\"");
                xml.append(" type=\"sh.gepetto.StepError\"");
                xml.append(">");
                if (step.getDetails() != null) {
                    xml.append(escapeXml(step.getDetails()));
                }
                xml.append("</error>\n");
            }
            
            // Add system-out with details if any
            if (step.getDetails() != null) {
                xml.append("    <system-out>");
                xml.append(escapeXml(step.getDetails()));
                xml.append("</system-out>\n");
            }
            
            xml.append("  </testcase>\n");
        }
        
        // Add main error if any
        if (result.getErrorMessage() != null) {
            xml.append("  <system-err>");
            xml.append(escapeXml(result.getErrorMessage()));
            xml.append("</system-err>\n");
        }
        
        xml.append("</testsuite>\n");
        return xml.toString();
    }
    
    /**
     * Sanitize a file name by replacing invalid characters
     */
    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_.]", "_");
    }
    
    /**
     * Escape special characters in XML
     */
    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
    
    /**
     * Escape special characters in JSON
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}