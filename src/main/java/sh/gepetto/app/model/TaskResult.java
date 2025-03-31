package sh.gepetto.app.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TaskResult {

    public enum Status {
        SUCCESS, FAILED, ERROR
    }

    private TaskDetails task;
    private Status status;
    private LocalDateTime executionTime;
    private long executionDurationMs;
    private String errorMessage;

    @Builder.Default
    private List<StepResult> stepResults = new ArrayList<>();

}