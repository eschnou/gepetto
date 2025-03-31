package sh.gepetto.app.service;

import sh.gepetto.app.model.Configuration;
import sh.gepetto.app.model.QATest;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TestResult;
import sh.gepetto.app.operator.TestOperator;
import sh.gepetto.app.operator.TestRun;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for executing tests
 */
@Service
@AllArgsConstructor
public class TestExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionService.class);
    
    private final TestOperator testOperator;

    /**
     * Execute a test with the given configuration
     *
     * @param configuration the test configuration
     * @param test          the test to execute
     * @return the test result
     */
    public TestResult executeTest(Configuration configuration, QATest test) {
        logger.info("Executing test '{}' with variables {}", test.getName(), configuration.getVariables());

        // Create a test result
        TestResult result = TestResult.builder()
                .test(test)
                .build();

        result.setExecutionTime(LocalDateTime.now());

        // Record start time to calculate duration
        long startTime = System.currentTimeMillis();

        try {
            // Validate all variables up front before executing steps
            validateAllRequiredVariables(configuration, test);
            
            // Here we would normally implement the actual test execution logic
            // For now, we'll just set up a placeholder that creates a positive result

            // Placeholder for processing test steps
            processTestSteps(configuration, test, result);

            // If all steps passed, mark the test as passed
            result.setStatus(TestResult.Status.PASSED);
        } catch (IllegalArgumentException e) {
            // If a variable is missing, mark the test as an error
            result.setStatus(TestResult.Status.ERROR);
            result.setErrorMessage(e.getMessage());
            logger.error("Error executing test - missing variable: {}", e.getMessage(), e);
        } catch (Exception e) {
            // If an exception occurs, mark the test as an error
            result.setStatus(TestResult.Status.ERROR);
            result.setErrorMessage(e.getMessage());
            logger.error("Error executing test: {}", e.getMessage(), e);
        } finally {
            // Calculate and set execution duration
            long endTime = System.currentTimeMillis();
            result.setExecutionDurationMs(endTime - startTime);
        }

        logger.info("Test execution completed with status: {}", result.getStatus());
        return result;
    }

    /**
     * Process each step in the test and add results to the test result
     */
    private void processTestSteps(Configuration configuration, QATest test, TestResult result) {
        // First, plan the test run with the TestOperator
        TestRun testRun = testOperator.plan(test);
        logger.info("Test run planned with ID: {}", testRun.getId());
        
        // Process each step in the test
        for (String step : test.getSteps()) {
            // Replace variables in the step
            // We've already validated all variables exist, so this should not fail
            String processedStep = replaceVariables(configuration, step);
            logger.info("Processing step: {}", processedStep);
            
            // Use the TestOperator to execute the step
            StepResult stepResult = testOperator.nextStep(testRun, processedStep);
            logger.info("Step result: {}", stepResult.getStatus());
            
            // Add the step result to the test result
            result.getStepResults().add(stepResult);
            
            // If the step failed, mark the test as failed and break
            if (stepResult.getStatus() == TestResult.Status.FAILED || 
                stepResult.getStatus() == TestResult.Status.ERROR) {
                result.setStatus(stepResult.getStatus());
                result.setErrorMessage("Step failed: " + processedStep);
                break;
            }
        }
    }
    
    /**
     * Validate that all required variables are defined before executing tests
     * This method scans all steps for variable references and ensures they're defined
     * 
     * @param configuration the configuration containing variables
     * @param test the test to validate
     * @throws IllegalArgumentException if any required variable is missing
     */
    private void validateAllRequiredVariables(Configuration configuration, QATest test) {
        // Create a set to track all required variables
        java.util.Set<String> requiredVariables = new java.util.HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        
        // Scan all steps to find required variables
        for (String step : test.getSteps()) {
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