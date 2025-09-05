package sh.gepetto.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import sh.gepetto.app.cli.GepettoCommand;
import picocli.CommandLine;

/**
 * Gepetto Application Entry Point
 *
 * Gepetto is a natural language task execution framework that allows users
 * to write and execute tasks in plain English.
 */
@SpringBootApplication
public class GepettoApplication {

	public static void main(String[] args) {
		int exitCode = SpringApplication.exit(SpringApplication.run(GepettoApplication.class, args));
		System.exit(exitCode);
	}
	
	@Bean
	@Profile("!test")
	public CommandLineRunner commandLineRunner(CommandLine.IFactory factory, GepettoCommand command) {
		return args -> {
			try {
				CommandLine cmd = new CommandLine(command, factory);
				cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
					// Handle picocli exceptions without a stack trace
					System.err.println(ex.getMessage());
					return commandLine.getCommandSpec().exitCodeOnExecutionException();
				});
				
				// Silently handle parsing errors
				cmd.setParameterExceptionHandler((ex, args1) -> {
					System.err.println(ex.getMessage());
					if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
						ex.getCommandLine().usage(System.err, ex.getCommandLine().getColorScheme());
					}
					return ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
				});
				
				int exitCode = cmd.execute(args);
				if (exitCode != 0) {
					System.exit(exitCode);
				}
			} catch (Exception ex) {
				// This should never happen with our error handlers
				System.exit(1);
			}
		};
	}
	
	@Bean
	@Profile("!test")
	public ExitCodeGenerator exitCodeGenerator() {
		return () -> 0;
	}
	
	public static class CommandExecutionException extends RuntimeException implements ExitCodeGenerator {
		private final int exitCode;
		
		public CommandExecutionException(String message, int exitCode) {
			super(message);
			this.exitCode = exitCode;
		}
		
		public CommandExecutionException(String message, Throwable cause, int exitCode) {
			super(message, cause);
			this.exitCode = exitCode;
		}
		
		@Override
		public int getExitCode() {
			return exitCode;
		}
	}
}