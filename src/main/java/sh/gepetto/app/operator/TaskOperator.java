package sh.gepetto.app.operator;

import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.TaskDetails;

public interface TaskOperator {
    TaskRun plan(TaskDetails task);

    StepResult nextStep(TaskRun taskRun, String input);
}
