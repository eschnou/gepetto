package sh.gepetto.app.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepResult {
    private String step;
    private TestResult.Status status;
    private String details;
    private String screenshot;
}