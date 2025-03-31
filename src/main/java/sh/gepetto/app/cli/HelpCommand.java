package sh.gepetto.app.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command for providing help on commands and subcommands
 */
@Component
@Command(
    name = "help",
    description = "Display help information about gepetto"
)
public class HelpCommand implements Runnable {

    private final CommandLine.IFactory factory;
    private final GepettoCommand rootCommand;
    
    @Parameters(
        index = "0..*",
        description = "Command or subcommand to get help on",
        paramLabel = "<command>"
    )
    private String[] commandArray = new String[0];
    
    private List<String> getCommands() {
        return commandArray != null ? Arrays.asList(commandArray) : Collections.emptyList();
    }
    
    public HelpCommand(CommandLine.IFactory factory, GepettoCommand rootCommand) {
        this.factory = factory;
        this.rootCommand = rootCommand;
    }
    
    @Override
    public void run() {
        CommandLine commandLine = new CommandLine(rootCommand, factory);
        List<String> commands = getCommands();
        
        if (commands.isEmpty()) {
            // Display general help
            commandLine.usage(System.out);
            return;
        }
        
        // Navigate to the specified command/subcommand
        CommandLine subcommand = commandLine;
        for (String commandName : commands) {
            subcommand = findSubcommand(subcommand, commandName);
            if (subcommand == null) {
                System.out.println("Unknown command: " + commandName);
                System.out.println("Available commands: " + getAvailableCommands(commandLine));
                return;
            }
        }
        
        // Display help for the specified command/subcommand
        subcommand.usage(System.out);
    }
    
    private CommandLine findSubcommand(CommandLine command, String name) {
        for (CommandLine subcommand : command.getSubcommands().values()) {
            if (subcommand.getCommandName().equals(name)) {
                return subcommand;
            }
        }
        return null;
    }
    
    private String getAvailableCommands(CommandLine commandLine) {
        return String.join(", ", commandLine.getSubcommands().keySet());
    }
}