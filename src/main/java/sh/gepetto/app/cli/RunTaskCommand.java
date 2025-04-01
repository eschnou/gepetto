package sh.gepetto.app.cli;

import java.io.IOException;
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
import sh.gepetto.app.model.TaskDetails;
import sh.gepetto.app.model.TaskResult;
import sh.gepetto.app.service.JUnitReportService;
import sh.gepetto.app.service.TaskExecutionService;
import sh.gepetto.app.service.TaskParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command for running tasks
 */
@Component
@Command(
    name = "run", 
    description = "Run a task file"
)
public class RunTaskCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RunTaskCommand.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final TaskParser taskParser;
    private final TaskExecutionService taskExecutionService;
    private final ApplicationConfig appConfig;
    private final JUnitReportService reportService;
    
    @Option(names = {"--var", "-v"}, description = "Define a variable in format NAME=VALUE (overrides configured variables)", split = ",")
    private java.util.Map<String, String> variables;
    
    @Option(names = {"--debug", "-d"}, description = "Enable debug mode for this run")
    private boolean debug;
    
    @Option(names = {"--no-report"}, description = "Disable saving test reports")
    private boolean noReport;
    
    @picocli.CommandLine.Parameters(index = "0", description = "Path to the task file to run (.test or .gpt)")
    private String taskFilePath;
    
    public RunTaskCommand(
            TaskParser taskParser,
            TaskExecutionService taskExecutionService,
            ApplicationConfig appConfig,
            JUnitReportService reportService) {
        this.taskParser = taskParser;
        this.taskExecutionService = taskExecutionService;
        this.appConfig = appConfig;
        this.reportService = reportService;
    }
    
    @Override
    public void run() {
        try {
            // Set debug mode for this run if specified
            if (debug) {
                System.setProperty("AIGENQA_LOG_LEVEL", "info");
                logger.info("Debug mode enabled for this run");
            }
            
            // Create configuration with variables
            Configuration config = Configuration.builder()
                .variables(new java.util.HashMap<>())
                .build();
                
            // Add configured variables if available
            if (appConfig.getVariables() != null) {
                config.getVariables().putAll(appConfig.getVariables());
            }
                
            // Add command line variables (override configured ones)
            if (variables != null && !variables.isEmpty()) {
                config.getVariables().putAll(variables);
                logger.info("Added command line variables: {}", variables);
            }
            
            // Log the variables that will be used
            logger.info("Using variables: {}", config.getVariables());

            // Parse task file
            Path path = Paths.get(taskFilePath);
            if (!Files.exists(path)) {
                System.out.println("Error: Task file not found: " + taskFilePath);
                return;
            }
            
            // Validate file extension
            if (!taskParser.isValidTaskFile(path)) {
                System.out.println("Error: Invalid file type. File must have .test or .gpt extension: " + taskFilePath);
                return;
            }

            TaskDetails task = taskParser.parseTaskFile(path);
            logger.info("Parsed task: {}", task);

            // Execute task (detailed progress will be shown during execution)
            TaskResult result = taskExecutionService.executeTask(config, task);

            // Print final summary result
            System.out.println(formatTaskResult(result));
            
            // Save reports unless disabled
            if (!noReport) {
                try {
                    Path reportPath = reportService.saveReport(result);
                    System.out.println("Test report saved to: " + reportPath);
                } catch (IOException e) {
                    // Log without stack trace
                    logger.error("Failed to save test report: {}", e.getMessage());
                    System.out.println("Warning: Failed to save test report: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Log without stack trace
            logger.error("Error running task: {}", e.getMessage());
            System.out.println("\n❌ Error: " + e.getMessage());
        }
    }
    
    private String formatTaskResult(TaskResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== TASK RESULT =====\n");
        sb.append("Task: ").append(result.getTask().getName()).append("\n");
        sb.append("Description: ").append(result.getTask().getDescription()).append("\n");
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
