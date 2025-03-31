package sh.gepetto.app.operator.opengpa;

import sh.gepetto.app.model.TaskResult;
import lombok.Data;

@Data
public class CompleteTaskActionInput {

    private String result;
    private TaskResult.Status status;

}
