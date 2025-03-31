package sh.gepetto.app.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TestResult {

    public enum Status {
        PASSED, FAILED, ERROR
    }

    private QATest test;
    private Status status;
    private LocalDateTime executionTime;
    private long executionDurationMs;
    private String errorMessage;

    @Builder.Default
    private List<StepResult> stepResults = new ArrayList<>();

}