package sh.gepetto.app.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/**
 * Command for displaying version information
 */
@Component
@Command(
    name = "version", 
    description = "Display version information"
)
public class VersionCommand implements Runnable {

    @Value("${spring.application.version:0.0.1-SNAPSHOT}")
    private String version;
    
    @Override
    public void run() {
        System.out.println("Gepetto version " + version);
        System.out.println("AI-powered natural language testing framework");
        System.out.println("Copyright © 2025");
    }
}