package sh.gepetto.app.utils;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;
import java.util.List;

public final class ToolCallbackUtils {
    private ToolCallbackUtils() {}

    public static ToolCallback[] sanitizedFrom(List<Object> toolObjects) {
        return toolObjects.stream()
                .flatMap(obj -> Arrays.stream(ToolCallbacks.from(obj))) // make MethodToolCallback(s)
                .map(SanitizingToolCallback::new)                       // wrap with sanitizer
                .toArray(ToolCallback[]::new);
    }
}
