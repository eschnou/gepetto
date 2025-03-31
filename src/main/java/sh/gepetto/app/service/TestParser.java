package sh.gepetto.app.service;

import sh.gepetto.app.model.QATest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service for parsing test files into Test objects
 */
@Service
public class TestParser {
    private static final Logger logger = LoggerFactory.getLogger(TestParser.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parse a test file into a Test object
     *
     * @param filePath the path to the test file
     * @return the parsed Test
     * @throws IOException if an error occurs reading the file
     */
    public QATest parseTestFile(Path filePath) throws IOException {
        logger.info("Parsing test file: {}", filePath);

        QATest test = new QATest();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean inTestSection = false;
            boolean inTaskSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse test metadata
                if (line.startsWith("description:")) {
                    test.setDescription(extractQuotedValue(line));
                } else if (line.startsWith("tags:")) {
                    String tagsStr = line.substring(5).trim();
                    if (tagsStr.startsWith("[") && tagsStr.endsWith("]")) {
                        tagsStr = tagsStr.substring(1, tagsStr.length() - 1);
                    }
                    List<String> tags = Arrays.asList(tagsStr.split(","));
                    test.setTags(tags.stream().map(String::trim).toList());
                } else if (line.startsWith("author:")) {
                    test.setAuthor(extractQuotedValue(line));
                } else if (line.startsWith("created:")) {
                    String dateStr = extractQuotedValue(line);
                    test.setCreated(LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay());
                }

                // Parse test/task steps
                if (line.equals("Test:")) {
                    inTestSection = true;
                    continue;
                } else if (line.equals("Task:")) {
                    inTaskSection = true;
                    continue;
                }

                if (inTestSection || inTaskSection) {
                    test.addStep(line);
                }
            }
        }

        // Use filename as test name if not specified
        if (test.getName() == null) {
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".test")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            } else if (fileName.endsWith(".gpt")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            test.setName(fileName.replace("-", " "));
        }

        return test;
    }

    /**
     * Check if a file is a valid test or task file
     * 
     * @param filePath the path to the file
     * @return true if the file has a valid extension (.test or .gpt)
     */
    public boolean isValidTestFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.endsWith(".test") || fileName.endsWith(".gpt");
    }

    /**
     * Extract a quoted value from a line like 'key: "value"'
     */
    private String extractQuotedValue(String line) {
        int startIndex = line.indexOf("\"");
        int endIndex = line.lastIndexOf("\"");

        if (startIndex >= 0 && endIndex > startIndex) {
            return line.substring(startIndex + 1, endIndex);
        }

        // If no quotes, take everything after the colon
        int colonIndex = line.indexOf(":");
        if (colonIndex >= 0) {
            return line.substring(colonIndex + 1).trim();
        }

        return "";
    }
}