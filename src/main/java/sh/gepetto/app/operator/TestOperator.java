package sh.gepetto.app.operator;

import sh.gepetto.app.model.StepResult;
import sh.gepetto.app.model.QATest;

public interface TestOperator {
    TestRun plan(QATest test);

    StepResult nextStep(TestRun testRun, String input);
}
