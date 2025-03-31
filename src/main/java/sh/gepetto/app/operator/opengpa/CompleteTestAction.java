package sh.gepetto.app.operator.opengpa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.JsonSchemaUtils;
import org.opengpa.core.agent.Agent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class CompleteTestAction implements Action {

    public static final String NAME = "complete_test";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Invoke this action when the test is complete, either passed, failed or you encountered an error.";
    }

    @Override
    public JsonNode getJsonSchema() {
        return JsonSchemaUtils.generateSchemaFromClass(CompleteTestActionInput.class);
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, Object> input, Map<String, String> context) {
        CompleteTestActionInput actionInput;
        try {
            actionInput = objectMapper.convertValue(input, CompleteTestActionInput.class);
        } catch (Exception e) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .result("Error parsing input parameters: " + e.getMessage())
                    .build();
        }

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary(actionInput.getResult())
                .result(actionInput)
                .build();
    }
}
