package sh.gepetto.app.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskResult;
import sh.gepetto.app.tools.ControlTools;

import java.util.*;

@Slf4j
@Component
public class AgenticTaskOperator implements TaskOperator {

    private final Resource stepSystemPromptResource = new ClassPathResource("prompts/reactSystemPrompt.st");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatClient chatClient;

    public AgenticTaskOperator(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public StepResult nextStep(TaskRun taskRun, String input) {
        // Prepare a toolcallingmanager
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();

        // Prepare the prompt
        Prompt prompt;
        try {
            prompt = preparePrompt(taskRun, input, chatOptions);
        } catch (JsonProcessingException e) {
            return StepResult.builder()
                    .step(input)
                    .details(e.getMessage())
                    .status(TaskResult.Status.ERROR)
                    .build();
        }

        // We create the stepId now so we can inject in the context
        UUID stepId = UUID.randomUUID();

        // Collect the entire response for later processing
        StringBuilder fullResponse = new StringBuilder();
        List<Usage> usages = new ArrayList<>();

        // Prepare tool context data
        Map<String, Object> toolContextData = new HashMap<>();
        toolContextData.put("input", input);

        // Stream the response
        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .toolContext(toolContextData)
                .call()
                .chatResponse();

        while (chatResponse.hasToolCalls()) {
            for (AssistantMessage.ToolCall toolCall : chatResponse.getResult().getOutput().getToolCalls()) {
                if (!toolCall.name().equals("complete_test")) {
                    System.out.println("Executing action " + toolCall.name() + "(" + toolCall.arguments() + ")");
                } else {
                    try {
                        ControlTools.CompleteActionResult completeActionResult = objectMapper.readValue(toolCall.arguments(), ControlTools.CompleteActionResult.class);
                        return StepResult.builder()
                                .step(input)
                                .details(completeActionResult.message())
                                .status(completeActionResult.status())
                                .build();
                    } catch (JsonProcessingException e) {
                        return StepResult.builder()
                                .step(input)
                                .details(e.getMessage())
                                .status(TaskResult.Status.ERROR)
                                .build();
                    }
                }
            }

            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);
            chatResponse = chatClient.prompt(prompt).call().chatResponse();
        }

        return StepResult.builder()
                .step(input)
                .details(chatResponse.getResult().getOutput().getText())
                .status(TaskResult.Status.SUCCESS)
                .build();
    }

    private Prompt preparePrompt(TaskRun taskRun, String input, ChatOptions chatOptions) throws JsonProcessingException {
        // Prepare the system prompt. This one contains non user/taskRun specific
        // information such as the list of possible actions.
        PromptTemplate systemPrompt = new SystemPromptTemplate(stepSystemPromptResource);
        Message systemMessage = systemPrompt.createMessage(Map.of(
                "name", taskRun.getTask().getName(),
                "description", taskRun.getTask().getDescription()
        ));

        // Prepare the message history
        List<Message> messageList = new ArrayList<>();
        messageList.add(systemMessage);

        // Add historical steps
        for (StepResult step : taskRun.getResult().getStepResults()) {
            messageList.add(UserMessage.builder().text("Step: " + step.getStep()).build());
            messageList.add(new AssistantMessage(objectMapper.writeValueAsString(new ControlTools.CompleteActionResult(step.getDetails(), step.getStatus()))));
        }

        // Add the user request
        messageList.add(new UserMessage(input));

        // Prepare the final prompt
        return new Prompt(messageList, chatOptions);
    }
}