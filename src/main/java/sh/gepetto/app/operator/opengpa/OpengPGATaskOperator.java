package sh.gepetto.app.operator.opengpa;

import sh.gepetto.app.config.ApplicationConfig;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskDetails;
import sh.gepetto.app.model.TaskResult;
import sh.gepetto.app.operator.TaskOperator;
import sh.gepetto.app.operator.TaskRun;
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
public class OpengPGATaskOperator implements TaskOperator {

    // lists to hold agents and their respective steps
    private final HashMap<String, Agent> agents = new HashMap<>();
    private final HashMap<String, List<AgentStep>> steps = new HashMap<>();

    // dependencies for the service
    private final ChatModel chatModel;
    private final Workspace workspace;
    private final ApplicationConfig applicationConfig;
    private final List<Action> actions;

    public OpengPGATaskOperator(ChatModel chatModel, Workspace workspace, McpActionProvider mcpActionProvider, ApplicationConfig applicationConfig, List<Action> actions) {
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
    public TaskRun plan(TaskDetails task) {
        Map<String, String> taskContext = new HashMap<>();
        taskContext.put("task", task.getName());
        
        // Add hostname if defined
        String hostname = applicationConfig.getVariable("HOSTNAME");
        if (hostname != null) {
            taskContext.put("hostname", hostname);
        }

        ReActAgent agent = new ReActAgent(chatModel, workspace, actions, task.getDescription(), taskContext);
        if (StringUtils.hasText(applicationConfig.getConfiguration().getLogPath())) {
            agent.enableLogging(new File(applicationConfig.getConfiguration().getLogPath()));
        }

        TaskRun taskRun = TaskRun.builder()
                .created(ZonedDateTime.now())
                .context(taskContext)
                .id(agent.getId())
                .title(task.getName())
                .build();

        agents.put(agent.getId(), agent);
        return taskRun;
    }

    @Override
    public StepResult nextStep(TaskRun taskRun, String input) {
        System.out.println("\n>> Executing step: " + input);

        List<AgentStep> taskSteps = steps.getOrDefault(taskRun.getId(), new ArrayList<>());
        steps.put(taskRun.getId(), taskSteps);

        int stepCount = 0;
        while (stepCount < applicationConfig.getConfiguration().getMaxSteps()) {
            System.out.println("  - Action " + (stepCount + 1) + " in progress...");
            
            AgentStep step = agents.get(taskRun.getId()).executeNextStep(input, new HashMap<>(), new HashMap<>());
            taskSteps.add(step);

            // Log the action being executed and its reasoning
            String actionName = step.getAction().getName();
            String reasoning = step.getReasoning() != null ? step.getReasoning() : "No reasoning provided";
            
            System.out.println("    * Executing action: " + actionName);
            System.out.println("    * Reasoning: " + reasoning.replaceAll("\\n", " ").trim());

            if (step.getAction().getName().equals(CompleteTaskAction.NAME)) {
                CompleteTaskActionInput stepResult = (CompleteTaskActionInput) step.getResult().getResult();
                System.out.println("  - Step completed with status: " + stepResult.getStatus());
                
                return StepResult.builder()
                        .step(input)
                        .status(stepResult.getStatus())
                        .details(stepResult.getResult())
                        .build();
            }

            if (step.isFinal()) {
                System.out.println("  - Step reached final state without completion action");
                
                return StepResult.builder()
                        .step(input)
                        .status(TaskResult.Status.ERROR)
                        .details("Step complete with an outcome.")
                        .build();
            }

            stepCount++;
        }

        System.out.println("  - Maximum action count reached without completion");
        
        return StepResult.builder()
                .step(input)
                .status(TaskResult.Status.ERROR)
                .details("Could not complete the step below the maximum number of actions.")
                .build();
    }
}
