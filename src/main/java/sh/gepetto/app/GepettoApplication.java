package sh.gepetto.app;

import org.opengpa.core.config.EnableOpenGPA;
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
@EnableOpenGPA
public class GepettoApplication {

	public static void main(String[] args) {
		int exitCode = SpringApplication.exit(SpringApplication.run(GepettoApplication.class, args));
		System.exit(exitCode);
	}
	
	@Bean
	@Profile("!test")
	public CommandLineRunner commandLineRunner(CommandLine.IFactory factory, GepettoCommand command) {
		return args -> {
			int exitCode = new CommandLine(command, factory).execute(args);
			if (exitCode != 0) {
				throw new CommandExecutionException(exitCode);
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
		
		public CommandExecutionException(int exitCode) {
			this.exitCode = exitCode;
		}
		
		@Override
		public int getExitCode() {
			return exitCode;
		}
	}
}