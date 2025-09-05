package sh.gepetto.app.cli;

import sh.gepetto.app.config.ApplicationConfig;
import sh.gepetto.app.config.Constants;
import sh.gepetto.app.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Command for initializing a new Gepetto project
 */
@Component
@Command(
    name = "init", 
    description = "Initialize a new Gepetto project"
)
public class InitCommand implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);

    private final ApplicationConfig appConfig;
    private final ConfigurationService configService;
    
    @Option(names = {"--var", "-v"}, description = "Define a variable in format NAME=VALUE", split = ",")
    private java.util.Map<String, String> variables;
    
    public InitCommand(ApplicationConfig appConfig, ConfigurationService configService) {
        this.appConfig = appConfig;
        this.configService = configService;
    }
    
    @Override
    public void run() {
        // Check if already initialized
        Path projectDir = Paths.get(Constants.PROJECT_DIR);
        if (Files.exists(projectDir)) {
            System.out.println("Project already initialized. Directory exists at " + Constants.PROJECT_DIR);
            System.out.println("Use 'configure' command to update existing configuration.");
            return;
        }
        
        try {
            // Set variables in appConfig if provided
            if (variables != null && !variables.isEmpty()) {
                variables.forEach((name, value) -> appConfig.setVariable(name, value));
            }
            
            createProjectStructure();
            createConfigFile();
            createSampleTask();
            System.out.println("Gepetto project initialized successfully!");
            System.out.println("- Project structure created at " + Constants.PROJECT_DIR + "/");
            System.out.println("- Configuration saved to " + Constants.PROJECT_DIR + "/" + Constants.CONFIG_FILENAME);
            System.out.println("- Sample task created at " + Constants.PROJECT_DIR + "/" + Constants.TASKS_DIR + "/" + Constants.SAMPLE_TASK_FILENAME);
            System.out.println("\nTo run your task: gepetto run " + Constants.PROJECT_DIR + "/" + Constants.TASKS_DIR + "/" + Constants.SAMPLE_TASK_FILENAME);
        } catch (IOException e) {
            logger.error("Error initializing project: {}", e.getMessage(), e);
            System.err.println("Error initializing project: " + e.getMessage());
        }
    }
    
    private void createProjectStructure() throws IOException {
        // Create main project directory
        Path projectDir = Paths.get(Constants.PROJECT_DIR);
        if (!Files.exists(projectDir)) {
            logger.info("Creating project directory: {}", projectDir);
            Files.createDirectories(projectDir);
        }
        
        // Create tasks directory
        Path tasksDir = Paths.get(Constants.PROJECT_DIR, Constants.TASKS_DIR);
        if (!Files.exists(tasksDir)) {
            logger.info("Creating tasks directory: {}", tasksDir);
            Files.createDirectories(tasksDir);
        }
        
        // Create results directory
        Path resultsDir = Paths.get(Constants.PROJECT_DIR, Constants.RESULTS_DIR);
        if (!Files.exists(resultsDir)) {
            logger.info("Creating results directory: {}", resultsDir);
            Files.createDirectories(resultsDir);
        }
    }
    
    private void createConfigFile() throws IOException {
        Path configPath = Paths.get(Constants.PROJECT_DIR, Constants.CONFIG_FILENAME);
        
        StringBuilder configContent = new StringBuilder();
        configContent.append("# Gepetto Configuration\n\n");
        
        // Add variables section
        configContent.append("variables:\n");
        if (variables != null && !variables.isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                configContent.append("  ").append(entry.getKey()).append(": \"").append(entry.getValue()).append("\"\n");
            }
        } else {
            configContent.append("  # Example variables:\n");
            configContent.append("  HOSTNAME: \"https://weather.gov\"\n");
            configContent.append("  LOCATION: \"New York, NY, USA\"\n");
        }
        
        configContent.append("debug: false\n");
        configContent.append("timeout: 30000\n");
        
        Files.writeString(configPath, configContent.toString());
    }
    
    private void createSampleTask() throws IOException {
        Path taskPath = Paths.get(Constants.PROJECT_DIR, Constants.TASKS_DIR, Constants.SAMPLE_TASK_FILENAME);
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        StringBuilder taskContent = new StringBuilder();
        taskContent.append("# Sample Task\n");
        taskContent.append("description: \"Check weather for a US city\"\n");
        taskContent.append("tags: [smoketest]\n");
        taskContent.append("author: \"Gepetto\"\n");
        taskContent.append("created: \"").append(currentDate).append("\"\n\n");
        taskContent.append("Task:\n");
        taskContent.append("  Navigate to ${HOSTNAME} and verify you are at the weather service.\n");
        taskContent.append("  Search the weather for ${LOCATION}.\n");
        taskContent.append("  Verify that the weather matches the requested location.\n");

        Files.writeString(taskPath, taskContent.toString());
    }
}