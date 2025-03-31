package sh.gepetto.app.cli;

import sh.gepetto.app.config.ApplicationConfig;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command for configuring Gepetto
 */
@Component
@Command(
    name = "configure", 
    description = "Configure Gepetto settings"
)
public class ConfigureCommand implements Runnable {
    
    private final ApplicationConfig appConfig;
    
    @Option(names = {"--hostname", "-h"}, description = "Target hostname to test")
    private String hostname;
    
    @Option(names = {"--debug", "-d"}, negatable = true, description = "Enable debug mode")
    private boolean debug;
    
    public ConfigureCommand(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Override
    public void run() {
        boolean changed = false;
        
        // Update hostname if specified
        if (hostname != null) {
            appConfig.setTargetHostname(hostname);
            System.out.println("Target hostname configured: " + hostname);
            changed = true;
        }
        
        // The picocli.CommandLine.Option negatable=true property automatically 
        // handles both --debug and --no-debug
        appConfig.setDebug(debug);
        System.out.println("Debug mode: " + (debug ? "enabled" : "disabled"));
        changed = true;
        
        // Show current configuration if no changes were made
        if (!changed) {
            System.out.println("Current configuration:");
            System.out.println("  Target hostname: " + (appConfig.getTargetHostname() != null ? appConfig.getTargetHostname() : "Not configured"));
            System.out.println("  Debug mode: " + (appConfig.isDebug() ? "enabled" : "disabled"));
            System.out.println("\nUse --hostname or --debug/--no-debug options to configure");
        } else {
            System.out.println("Configuration saved to .gepetto/config.yaml");
        }
    }
}
