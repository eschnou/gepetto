package sh.gepetto.app.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskResult;

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
        Prompt prompt = preparePrompt(taskRun, input);

        // We create the stepId now so we can inject in the context
        UUID stepId = UUID.randomUUID();

        // Collect the entire response for later processing
        StringBuilder fullResponse = new StringBuilder();
        List<Usage> usages = new ArrayList<>();

        // Prepare tool context data
        Map<String, Object> toolContextData = new HashMap<>();
        toolContextData.put("input", input);

        // Stream the response
        ChatResponse response = chatClient
                .prompt(prompt)
                .toolContext(toolContextData)
                .call()
                .chatResponse();

        return StepResult.builder()
                .step(input)
                .status(TaskResult.Status.SUCCESS)
                .build();
    }

    private Prompt preparePrompt(TaskRun taskRun, String input) {
        // Prepare the system prompt. This one contains non user/taskRun specific
        // information such as the list of possible actions.
        PromptTemplate systemPrompt = new SystemPromptTemplate(stepSystemPromptResource);
        Message systemMessage = systemPrompt.createMessage();

        // Prepare the message history
        List<Message> messageList = new ArrayList<>();
        messageList.add(systemMessage);

        // Add historical steps
        for (StepResult step : taskRun.getResult().getStepResults()) {
            messageList.add(UserMessage.builder().text(step.getStep()).build());
            //messageList.add(new AssistantMessage(step.getOutput().toString()));
        }

        // Add the user request
        messageList.add(new UserMessage(input));

        // Prepare the final prompt
        return new Prompt(messageList);
    }
}