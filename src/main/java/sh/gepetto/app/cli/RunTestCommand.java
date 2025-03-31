package sh.gepetto.app.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import sh.gepetto.app.config.ApplicationConfig;
import sh.gepetto.app.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sh.gepetto.app.model.Configuration;
import sh.gepetto.app.model.QATest;
import sh.gepetto.app.model.TestResult;
import sh.gepetto.app.service.TestExecutionService;
import sh.gepetto.app.service.TestParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command for running tests
 */
@Component
@Command(
    name = "run", 
    description = "Run a test file"
)
public class RunTestCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RunTestCommand.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final TestParser testParser;
    private final TestExecutionService testExecutionService;
    private final ApplicationConfig appConfig;
    
    @Option(names = {"--hostname", "-h"}, required = false, description = "Target hostname to test (overrides configured hostname)")
    private String hostname;
    
    @Option(names = {"--debug", "-d"}, description = "Enable debug mode for this run")
    private boolean debug;
    
    @picocli.CommandLine.Parameters(index = "0", description = "Path to the test file to run")
    private String testFilePath;
    
    public RunTestCommand(TestParser testParser, TestExecutionService testExecutionService, ApplicationConfig appConfig) {
        this.testParser = testParser;
        this.testExecutionService = testExecutionService;
        this.appConfig = appConfig;
    }
    
    @Override
    public void run() {
        try {
            // Set debug mode for this run if specified
            if (debug) {
                System.setProperty("AIGENQA_LOG_LEVEL", "info");
                logger.info("Debug mode enabled for this run");
            }
            
            // Determine hostname to use
            String targetHostname = hostname;
            if (targetHostname == null) {
                targetHostname = appConfig.getTargetHostname();
                if (targetHostname == null) {
                    System.out.println("Error: No target hostname provided and none configured.");
                    System.out.println("Please either:");
                    System.out.println("1. Specify a hostname with --hostname or -h");
                    System.out.println("2. Configure a default hostname with 'gepetto configure --hostname <hostname>'");
                    return;
                }
                logger.info("Using configured hostname: {}", targetHostname);
            }
            
            // Create configuration
            Configuration config = Configuration.builder().targetHostname(targetHostname).build();

            // Parse test file
            Path path = Paths.get(testFilePath);
            if (!Files.exists(path)) {
                System.out.println("Error: Test file not found: " + testFilePath);
                return;
            }

            QATest test = testParser.parseTestFile(path);
            logger.info("Parsed test: {}", test);

            // Execute test
            TestResult result = testExecutionService.executeTest(config, test);

            // Print result
            System.out.println(formatTestResult(result));
        } catch (Exception e) {
            logger.error("Error running test: {}", e.getMessage(), e);
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private String formatTestResult(TestResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== TEST RESULT =====\n");
        sb.append("Test: ").append(result.getTest().getName()).append("\n");
        sb.append("Description: ").append(result.getTest().getDescription()).append("\n");
        sb.append("Status: ").append(result.getStatus()).append("\n");
        sb.append("Execution Time: ").append(result.getExecutionTime().format(DATE_FORMATTER)).append("\n");
        sb.append("Duration: ").append(result.getExecutionDurationMs()).append("ms\n");

        if (result.getErrorMessage() != null) {
            sb.append("Error: ").append(result.getErrorMessage()).append("\n");
        }

        sb.append("\n----- Step Results -----\n");
        for (int i = 0; i < result.getStepResults().size(); i++) {
            StepResult stepResult = result.getStepResults().get(i);
            sb.append(i + 1).append(". ").append(stepResult.getStep()).append("\n");
            sb.append("   Status: ").append(stepResult.getStatus()).append("\n");
            if (stepResult.getDetails() != null) {
                sb.append("   Details: ").append(stepResult.getDetails()).append("\n");
            }
        }

        sb.append("\n=======================\n");
        return sb.toString();
    }
}
