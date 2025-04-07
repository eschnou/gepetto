package sh.gepetto.app.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IVersionProvider;

/**
 * Provides version information for CLI commands
 */
@Component
public class VersionProvider implements IVersionProvider {

    @Value("${spring.application.version:0.1.0}")
    private String version;
    
    @Override
    public String[] getVersion() {
        return new String[] {
            "Gepetto version " + version,
            "AI-powered natural language task execution framework",
            "Copyright © 2025"
        };
    }
}