package sh.gepetto.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Configuration model for test execution
 * This class is used for both in-memory configuration and serialized to YAML
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    private String targetHostname;
    private String logPath;

    @Builder.Default
    private int maxSteps = 10;

    @Builder.Default
    private boolean debug = false;

    @Override
    public String toString() {
        return "Configuration{" +
                "targetHostname='" + targetHostname + '\'' +
                ", debug=" + debug +
                '}';
    }
}