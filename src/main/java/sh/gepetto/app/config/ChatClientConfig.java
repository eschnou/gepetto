package sh.gepetto.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sh.gepetto.app.tools.ControlTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient defaultChatClient(ChatModel chatModel,  ToolCallbackProvider tools) {
        List<ToolCallback> toolCallbacks = new ArrayList<>(Arrays.asList(tools.getToolCallbacks()));
        toolCallbacks.addAll(Arrays.asList(ToolCallbacks.from(new ControlTools())));

        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbacks)
                .build();
    }
}
