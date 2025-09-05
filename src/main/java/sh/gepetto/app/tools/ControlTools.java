package sh.gepetto.app.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import sh.gepetto.app.model.TaskResult;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ControlTools {

    public record CompleteActionResult(String message, TaskResult.Status status) {
    }

    @Tool(description = "Complete the test with either success, failure or error and a message describing what happened", returnDirect = true)
    public CompleteActionResult completeAction(
            @ToolParam(description = "A descriptive message for the test result.") String message,
            @ToolParam(description = "The result of the test") TaskResult.Status status, ToolContext toolContext) throws IOException
    {
        log.debug("Complete action for task: {}", toolContext.getContext().get("taskId"));
        return new CompleteActionResult(message, status);
    }
}
