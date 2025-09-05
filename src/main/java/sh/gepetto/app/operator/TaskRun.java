package sh.gepetto.app.operator;

import lombok.Builder;
import lombok.Data;
import sh.gepetto.app.model.TaskDetails;
import sh.gepetto.app.model.TaskResult;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class TaskRun {

  private String id;

  private TaskDetails task;

  private TaskResult result;

}

