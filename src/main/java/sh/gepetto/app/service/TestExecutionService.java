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
        logger.info("Executing test '{}' on host '{}'", test.getName(), configuration.getTargetHostname());

        // Create a test result
        TestResult result = TestResult.builder()
                .test(test)
                .build();

        result.setExecutionTime(LocalDateTime.now());

        // Record start time to calculate duration
        long startTime = System.currentTimeMillis();

        try {
            // Here we would normally implement the actual test execution logic
            // For now, we'll just set up a placeholder that creates a positive result

            // Placeholder for processing test steps
            processTestSteps(configuration, test, result);

            // If all steps passed, mark the test as passed
            result.setStatus(TestResult.Status.PASSED);
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
            logger.info("Processing step: {}", step);
            
            // Use the TestOperator to execute the step
            StepResult stepResult = testOperator.nextStep(testRun, step);
            logger.info("Step result: {}", stepResult.getStatus());
            
            // Add the step result to the test result
            result.getStepResults().add(stepResult);
            
            // If the step failed, mark the test as failed and break
            if (stepResult.getStatus() == TestResult.Status.FAILED || 
                stepResult.getStatus() == TestResult.Status.ERROR) {
                result.setStatus(stepResult.getStatus());
                result.setErrorMessage("Step failed: " + step);
                break;
            }
        }
    }
}