package sh.gepetto.app.cli;

import sh.gepetto.app.config.ApplicationConfig;
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
    private static final String PROJECT_DIR = "gepetto";
    private static final String TASKS_DIR = "tasks";
    private static final String RESULTS_DIR = "results";
    private static final String CONFIG_FILENAME = "config.yaml";
    private static final String SAMPLE_TASK_FILENAME = "login.gpt";
    
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
        Path projectDir = Paths.get(PROJECT_DIR);
        if (Files.exists(projectDir)) {
            System.out.println("Project already initialized. Directory exists at " + PROJECT_DIR);
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
            System.out.println("- Project structure created at " + PROJECT_DIR + "/");
            System.out.println("- Configuration saved to " + PROJECT_DIR + "/" + CONFIG_FILENAME);
            System.out.println("- Sample task created at " + PROJECT_DIR + "/" + TASKS_DIR + "/" + SAMPLE_TASK_FILENAME);
            System.out.println("\nTo run your task: gepetto run " + PROJECT_DIR + "/" + TASKS_DIR + "/" + SAMPLE_TASK_FILENAME);
        } catch (IOException e) {
            logger.error("Error initializing project: {}", e.getMessage(), e);
            System.err.println("Error initializing project: " + e.getMessage());
        }
    }
    
    private void createProjectStructure() throws IOException {
        // Create main project directory
        Path projectDir = Paths.get(PROJECT_DIR);
        if (!Files.exists(projectDir)) {
            logger.info("Creating project directory: {}", projectDir);
            Files.createDirectories(projectDir);
        }
        
        // Create tasks directory
        Path tasksDir = Paths.get(PROJECT_DIR, TASKS_DIR);
        if (!Files.exists(tasksDir)) {
            logger.info("Creating tasks directory: {}", tasksDir);
            Files.createDirectories(tasksDir);
        }
        
        // Create results directory
        Path resultsDir = Paths.get(PROJECT_DIR, RESULTS_DIR);
        if (!Files.exists(resultsDir)) {
            logger.info("Creating results directory: {}", resultsDir);
            Files.createDirectories(resultsDir);
        }
    }
    
    private void createConfigFile() throws IOException {
        Path configPath = Paths.get(PROJECT_DIR, CONFIG_FILENAME);
        
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
            configContent.append("  # HOSTNAME: \"example.com\"\n");
            configContent.append("  # USERNAME: \"user@example.com\"\n");
        }
        
        configContent.append("debug: false\n");
        configContent.append("timeout: 30000\n");
        
        Files.writeString(configPath, configContent.toString());
    }
    
    private void createSampleTask() throws IOException {
        Path taskPath = Paths.get(PROJECT_DIR, TASKS_DIR, SAMPLE_TASK_FILENAME);
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        StringBuilder taskContent = new StringBuilder();
        taskContent.append("# Sample Login Task\n");
        taskContent.append("description: \"Log in to the application with valid credentials\"\n");
        taskContent.append("tags: [login, authentication]\n");
        taskContent.append("author: \"Gepetto\"\n");
        taskContent.append("created: \"").append(currentDate).append("\"\n\n");
        taskContent.append("Task:\n");
        taskContent.append("  Navigate to ${HOSTNAME}/login.\n");
        taskContent.append("  Log in with username ${USERNAME} and password ${PASSWORD}.\n");
        taskContent.append("  Verify that the dashboard page is displayed.\n");
        taskContent.append("  Logout from the application.\n");
        taskContent.append("  Verify that you are back to the login screen.\n");

        Files.writeString(taskPath, taskContent.toString());
    }
}