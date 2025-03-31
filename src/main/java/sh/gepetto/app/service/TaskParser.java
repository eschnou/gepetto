package sh.gepetto.app.service;

import sh.gepetto.app.model.TaskDetails;
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
 * Service for parsing task files into TaskDetails objects
 */
@Service
public class TaskParser {
    private static final Logger logger = LoggerFactory.getLogger(TaskParser.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parse a task file into a TaskDetails object
     *
     * @param filePath the path to the task file
     * @return the parsed TaskDetails
     * @throws IOException if an error occurs reading the file
     */
    public TaskDetails parseTaskFile(Path filePath) throws IOException {
        logger.info("Parsing task file: {}", filePath);

        TaskDetails task = new TaskDetails();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean inTaskSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse task metadata
                if (line.startsWith("description:")) {
                    task.setDescription(extractQuotedValue(line));
                } else if (line.startsWith("tags:")) {
                    String tagsStr = line.substring(5).trim();
                    if (tagsStr.startsWith("[") && tagsStr.endsWith("]")) {
                        tagsStr = tagsStr.substring(1, tagsStr.length() - 1);
                    }
                    List<String> tags = Arrays.asList(tagsStr.split(","));
                    task.setTags(tags.stream().map(String::trim).toList());
                } else if (line.startsWith("author:")) {
                    task.setAuthor(extractQuotedValue(line));
                } else if (line.startsWith("created:")) {
                    String dateStr = extractQuotedValue(line);
                    task.setCreated(LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay());
                }

                // Parse task steps
                if (line.equals("Task:")) {
                    inTaskSection = true;
                    continue;
                }

                if (inTaskSection) {
                    task.addStep(line);
                }
            }
        }

        // Use filename as task name if not specified
        if (task.getName() == null) {
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".task")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            } else if (fileName.endsWith(".gpt")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            task.setName(fileName.replace("-", " "));
        }

        return task;
    }

    /**
     * Check if a file is a valid test or task file
     * 
     * @param filePath the path to the file
     * @return true if the file has a valid extension (.test or .gpt)
     */
    public boolean isValidTaskFile(Path filePath) {
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