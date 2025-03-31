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
    private static final String TEST_DIR = "qa-tests";
    private static final String SAMPLE_TEST_FILENAME = "navigation-test.test";
    
    private final ApplicationConfig appConfig;
    private final ConfigurationService configService;
    
    @Option(names = {"--hostname", "-h"}, description = "Target hostname to test", interactive = true)
    private String hostname;
    
    public InitCommand(ApplicationConfig appConfig, ConfigurationService configService) {
        this.appConfig = appConfig;
        this.configService = configService;
    }
    
    @Override
    public void run() {
        // Check if already initialized
        if (configService.configurationExists()) {
            System.out.println("Project already initialized. Configuration exists at .gepetto/config.yaml");
            System.out.println("Use 'configure' command to update existing configuration.");
            return;
        }
        
        // If hostname not provided, prompt for it manually
        if (hostname == null || hostname.isEmpty()) {
            System.out.print("Enter target hostname (e.g., example.com): ");
            try {
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                if (scanner.hasNextLine()) {
                    hostname = scanner.nextLine().trim();
                }
            } catch (Exception e) {
                logger.error("Error reading user input: {}", e.getMessage(), e);
            }
        }
        
        // Validate hostname and proceed
        if (hostname != null && !hostname.isEmpty()) {
            appConfig.setTargetHostname(hostname);
            configService.saveConfiguration(appConfig.getConfiguration());
            System.out.println("Target hostname configured: " + hostname);
        } else {
            System.out.println("Error: No hostname provided. Initialization aborted.");
            return;
        }
        
        // Create test directory
        try {
            createTestDirectory();
            createNavigationTest();
            System.out.println("Gepetto project initialized successfully!");
            System.out.println("- Configuration saved to .gepetto/config.yaml");
            System.out.println("- Sample test created at " + TEST_DIR + "/" + SAMPLE_TEST_FILENAME);
            System.out.println("\nTo run your test: gepetto run " + TEST_DIR + "/" + SAMPLE_TEST_FILENAME);
        } catch (IOException e) {
            logger.error("Error initializing project: {}", e.getMessage(), e);
            System.err.println("Error initializing project: " + e.getMessage());
        }
    }
    
    private void createTestDirectory() throws IOException {
        Path testDir = Paths.get(TEST_DIR);
        if (!Files.exists(testDir)) {
            logger.info("Creating test directory: {}", testDir);
            Files.createDirectories(testDir);
        }
    }
    
    private void createNavigationTest() throws IOException {
        Path testPath = Paths.get(TEST_DIR, SAMPLE_TEST_FILENAME);
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        StringBuilder testContent = new StringBuilder();
        testContent.append("# Basic Navigation Test\n");
        testContent.append("description: \"Navigate to the application homepage\"\n");
        testContent.append("tags: [navigation, smoke]\n");
        testContent.append("author: \"Gepetto\"\n");
        testContent.append("created: \"").append(currentDate).append("\"\n\n");
        testContent.append("Test:\n");
        testContent.append("  Navigate to the homepage.\n");
        
        Files.writeString(testPath, testContent.toString());
    }
}