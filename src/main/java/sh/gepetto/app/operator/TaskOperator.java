package sh.gepetto.app.operator;

import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskDetails;

public interface TaskOperator {

    StepResult nextStep(TaskRun taskRun, String input);

}
