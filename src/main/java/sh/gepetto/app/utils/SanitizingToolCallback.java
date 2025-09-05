package sh.gepetto.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/** Wraps a ToolCallback and repairs malformed JSON arguments (e.g., missing '}'). */
public final class SanitizingToolCallback implements ToolCallback {
    private final ToolCallback delegate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SanitizingToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public String call(String toolInput) {
        return delegate.call(repairJson(toolInput));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return delegate.call(repairJson(toolInput), toolContext);
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    /** Minimal, fast repair for the common case: missing closing '}' or extra tail after '}'. */
    static String repairJson(String s) {
        if (s == null || s.isBlank()) return "{}";
        // Try as-is
        if (isValidJsonObject(s)) return s;

        // Keep only from first '{'
        int start = s.indexOf('{');
        if (start > 0) s = s.substring(start);

        // Trim anything after the last balanced '}' and/or close missing braces.
        int depth = 0, lastBalancedEnd = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) lastBalancedEnd = i;
            }
        }
        // If we have a fully balanced prefix, trim to it.
        if (lastBalancedEnd >= 0) {
            String trimmed = s.substring(0, lastBalancedEnd + 1);
            if (isValidJsonObject(trimmed)) return trimmed;
        }
        // Otherwise, close missing braces.
        String fixed = s + "}".repeat(Math.max(0, depth));
        if (isValidJsonObject(fixed)) return fixed;

        // Last-resort: return empty object (avoids binder exceptions).
        return "{}";
    }

    private static boolean isValidJsonObject(String s) {
        try {
            return MAPPER.readTree(s).isObject();
        } catch (Exception ignore) {
            return false;
        }
    }
}
