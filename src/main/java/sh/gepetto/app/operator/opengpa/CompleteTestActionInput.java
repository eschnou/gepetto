package sh.gepetto.app.operator.opengpa;

import sh.gepetto.app.model.TestResult;
import lombok.Data;

@Data
public class CompleteTestActionInput {

    private String result;
    private TestResult.Status status;

}
