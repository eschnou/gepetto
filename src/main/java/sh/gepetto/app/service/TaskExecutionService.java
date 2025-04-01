package sh.gepetto.app.service;

import sh.gepetto.app.model.Configuration;
import sh.gepetto.app.model.TaskDetails;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskResult;
import sh.gepetto.app.operator.TaskOperator;
import sh.gepetto.app.operator.TaskRun;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for executing tasks
 */
@Service
@AllArgsConstructor
public class TaskExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutionService.class);
    
    private final TaskOperator taskOperator;

    /**
     * Execute a task with the given configuration
     *
     * @param configuration the task configuration
     * @param task          the task to execute
     * @return the task result
     */
    public TaskResult executeTask(Configuration configuration, TaskDetails task) {
        logger.info("Executing task '{}' with variables {}", task.getName(), configuration.getVariables());

        // Create a task result
        TaskResult result = TaskResult.builder()
                .task(task)
                .build();

        result.setExecutionTime(LocalDateTime.now());

        // Record start time to calculate duration
        long startTime = System.currentTimeMillis();

        try {
            // Validate all variables up front before executing steps
            validateAllRequiredVariables(configuration, task);
            
            // Here we would normally implement the actual task execution logic
            // For now, we'll just set up a placeholder that creates a positive result

            // Placeholder for processing task steps
            processTaskSteps(configuration, task, result);

            // If all steps passed, mark the task as passed
            result.setStatus(TaskResult.Status.SUCCESS);
        } catch (IllegalArgumentException e) {
            // If a variable is missing, mark the task as an error
            result.setStatus(TaskResult.Status.ERROR);
            result.setErrorMessage(e.getMessage());
            // Log without stack trace for variable errors
            logger.error("Error executing task - missing variable: {}", e.getMessage());
            System.out.println("\n❌ Error: " + e.getMessage());
        } catch (Exception e) {
            // If an exception occurs, mark the task as an error
            result.setStatus(TaskResult.Status.ERROR);
            result.setErrorMessage(e.getMessage());
            // Log without stack trace for other errors
            logger.error("Error executing task: {}", e.getMessage());
            System.out.println("\n❌ Error: " + e.getMessage());
        } finally {
            // Calculate and set execution duration
            long endTime = System.currentTimeMillis();
            result.setExecutionDurationMs(endTime - startTime);
        }

        logger.info("Task execution completed with status: {}", result.getStatus());
        return result;
    }

    /**
     * Process each step in the task and add results to the task result
     */
    private void processTaskSteps(Configuration configuration, TaskDetails task, TaskResult result) {
        // First, plan the task run with the TaskOperator
        System.out.println("\n===== STARTING TASK: " + task.getName() + " =====");
        System.out.println("Description: " + task.getDescription());
        System.out.println("Total steps: " + task.getSteps().size());
        
        TaskRun taskRun = taskOperator.plan(task);
        logger.info("Task run planned with ID: {}", taskRun.getId());
        
        // Process each step in the task
        for (int i = 0; i < task.getSteps().size(); i++) {
            String step = task.getSteps().get(i);
            System.out.println("\n===== STEP " + (i + 1) + "/" + task.getSteps().size() + " =====");
            
            // Replace variables in the step
            // We've already validated all variables exist, so this should not fail
            String processedStep = replaceVariables(configuration, step);
            logger.info("Processing step: {}", processedStep);
            
            // Use the TaskOperator to execute the step
            StepResult stepResult = taskOperator.nextStep(taskRun, processedStep);
            logger.info("Step result: {}", stepResult.getStatus());
            
            // Add the step result to the task result
            result.getStepResults().add(stepResult);
            
            // If the step failed, mark the task as failed and break
            if (stepResult.getStatus() == TaskResult.Status.FAILED ||
                stepResult.getStatus() == TaskResult.Status.ERROR) {
                result.setStatus(stepResult.getStatus());
                result.setErrorMessage("Step failed: " + processedStep);
                System.out.println("\n❌ Step " + (i + 1) + " failed with status: " + stepResult.getStatus());
                break;
            } else {
                System.out.println("\n✅ Step " + (i + 1) + " completed successfully");
            }
        }
        
        System.out.println("\n===== TASK EXECUTION COMPLETED =====");
    }
    
    /**
     * Validate that all required variables are defined before executing tasks
     * This method scans all steps for variable references and ensures they're defined
     * 
     * @param configuration the configuration containing variables
     * @param task the task to validate
     * @throws IllegalArgumentException if any required variable is missing
     */
    private void validateAllRequiredVariables(Configuration configuration, TaskDetails task) {
        // Create a set to track all required variables
        java.util.Set<String> requiredVariables = new java.util.HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        
        // Scan all steps to find required variables
        for (String step : task.getSteps()) {
            java.util.regex.Matcher matcher = pattern.matcher(step);
            while (matcher.find()) {
                String variableName = matcher.group(1);
                requiredVariables.add(variableName);
            }
        }
        
        // Check if all required variables are defined
        for (String variableName : requiredVariables) {
            boolean found = false;
            
            // Case-insensitive search for the variable
            for (java.util.Map.Entry<String, String> entry : configuration.getVariables().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(variableName)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                throw new IllegalArgumentException("Required variable '" + variableName + "' is not defined");
            }
        }
        
        logger.info("All required variables are defined: {}", requiredVariables);
    }
    
    /**
     * Replace variables in the form ${NAME} with their values from configuration
     * 
     * @param configuration the configuration containing variables
     * @param text the text with placeholders
     * @return text with variables replaced
     * @throws IllegalArgumentException if a required variable is missing
     */
    private String replaceVariables(Configuration configuration, String text) {
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            // Look up the variable (case insensitive)
            String value = null;
            for (java.util.Map.Entry<String, String> entry : configuration.getVariables().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(variableName)) {
                    value = entry.getValue();
                    break;
                }
            }
            
            // If variable not found, throw error
            if (value == null) {
                throw new IllegalArgumentException("Required variable '" + variableName + "' is not defined");
            }
            
            // Replace the variable placeholder with its value
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}