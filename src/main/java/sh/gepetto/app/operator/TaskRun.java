package sh.gepetto.app.operator;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class TaskRun {

  public enum Status {
    SUCCESS,
    FAILURE
  }

  private ZonedDateTime created;

  private ZonedDateTime completed;

  private String id;

  private String title;

  private String description;

  private Map<String, String> context;

  private Status status;

}

