package sh.gepetto.app.operator.opengpa;

import sh.gepetto.app.config.ApplicationConfig;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.QATest;
import sh.gepetto.app.model.TestResult;
import sh.gepetto.app.operator.TestOperator;
import sh.gepetto.app.operator.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.OutputMessageAction;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.agent.react.ReActAgent;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.mcp.McpActionProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@Slf4j
public class OpengPGATestOperator implements TestOperator {

    // lists to hold agents and their respective steps
    private final HashMap<String, Agent> agents = new HashMap<>();
    private final HashMap<String, List<AgentStep>> steps = new HashMap<>();

    // dependencies for the service
    private final ChatModel chatModel;
    private final Workspace workspace;
    private final ApplicationConfig applicationConfig;
    private final List<Action> actions;

    public OpengPGATestOperator(ChatModel chatModel, Workspace workspace, McpActionProvider mcpActionProvider, ApplicationConfig applicationConfig, List<Action> actions) {
        this.chatModel = chatModel;
        this.workspace = workspace;
        this.applicationConfig = applicationConfig;

        this.actions = new ArrayList<>(actions.stream()
                .filter(action -> !action.getName().equals(OutputMessageAction.NAME))
                .toList());

        // Add MCP actions
        List<Action> mcpActions = mcpActionProvider.getMCPActions();
        if (mcpActions != null) {
            this.actions.addAll(mcpActions);
        }
    }

    @Override
    public TestRun plan(QATest test) {
        Map<String, String> testContext = new HashMap<>();
        testContext.put("test", test.getName());
        
        // Add hostname if defined
        String hostname = applicationConfig.getVariable("HOSTNAME");
        if (hostname != null) {
            testContext.put("hostname", hostname);
        }

        ReActAgent agent = new ReActAgent(chatModel, workspace, actions, test.getDescription(), testContext);
        if (StringUtils.hasText(applicationConfig.getConfiguration().getLogPath())) {
            agent.enableLogging(new File(applicationConfig.getConfiguration().getLogPath()));
        }

        TestRun testRun = TestRun.builder()
                .created(ZonedDateTime.now())
                .context(testContext)
                .id(agent.getId())
                .title(test.getName())
                .build();

        agents.put(agent.getId(), agent);
        return testRun;
    }

    @Override
    public StepResult nextStep(TestRun testRun, String input) {

        List<AgentStep> testSteps = steps.getOrDefault(testRun.getId(), new ArrayList<>());
        steps.put(testRun.getId(),testSteps);

        int stepCount = 0;
        while (stepCount < applicationConfig.getConfiguration().getMaxSteps()) {
            AgentStep step = agents.get(testRun.getId()).executeNextStep(input, new HashMap<>(), new HashMap<>());
            testSteps.add(step);

            if (step.getAction().getName().equals(CompleteTestAction.NAME)) {
                CompleteTestActionInput stepResult = (CompleteTestActionInput) step.getResult().getResult();
                return StepResult.builder()
                        .step(input)
                        .status(stepResult.getStatus())
                        .details(stepResult.getResult())
                        .build();
            }

            if (step.isFinal()) {
                return StepResult.builder()
                        .step(input)
                        .status(TestResult.Status.ERROR)
                        .details("Step complete with an outcome.")
                        .build();
            }

            stepCount++;
        }

        return StepResult.builder()
                .step(input)
                .status(TestResult.Status.ERROR)
                .details("Could not complete the step below the maximum number of actions.")
                .build();
    }
}
