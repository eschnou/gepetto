package sh.gepetto.app.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main command for Gepetto CLI
 */
@Component
@Command(
    name = "gepetto", 
    description = "AI-powered natural language task execution framework",
    mixinStandardHelpOptions = true,
    subcommands = {RunTaskCommand.class, InitCommand.class, HelpCommand.class, VersionCommand.class}
)
public class GepettoCommand implements Runnable {
    
    private final CommandLine.IFactory factory;
    
    public GepettoCommand(CommandLine.IFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public void run() {
        // Show help when no command is specified
        new CommandLine(this, factory).usage(System.out);
    }
}
