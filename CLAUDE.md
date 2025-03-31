# Gepetto Development Guide

## Build Commands
```bash
# Build the project
./mvnw clean install

# Quick build (skip tests)
./mvnw clean package -P dev

# Show main help
./mvnw spring-boot:run -Dspring-boot.run.arguments="help"

# Show help for a specific command
./mvnw spring-boot:run -Dspring-boot.run.arguments="help run"

# Show version information
./mvnw spring-boot:run -Dspring-boot.run.arguments="version"

# Initialize a new project
./mvnw spring-boot:run -Dspring-boot.run.arguments="init --hostname example.com"

# Configure the default hostname
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --hostname example.com"

# Enable debug mode
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --debug"

# Disable debug mode
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --no-debug"

# Run a task file (using configured hostname)
./mvnw spring-boot:run -Dspring-boot.run.arguments="run gepetto/tasks/login.gpt"

# Run a task file (with explicit hostname)
./mvnw spring-boot:run -Dspring-boot.run.arguments="run gepetto/tasks/login.gpt --hostname example.com"

# Run a task file with debug output
./mvnw spring-boot:run -Dspring-boot.run.arguments="run gepetto/tasks/login.gpt --debug"

# Run from JAR (after building)
java -jar target/gepetto-0.0.1-SNAPSHOT.jar run gepetto/tasks/login.gpt

# Run all Java tests
./mvnw test

# Run a single Java test
./mvnw test -Dtest=TestClassName#testMethodName
```

## Code Style Guidelines
- **Package structure**: Maintain `sh.gepetto.app` with functional areas (`config`, `model`, `service`, `cli`)
- **Imports**: No wildcards, standard Java first, then framework-specific imports
- **Naming**: PascalCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants
- **Error handling**: Use try-catch with specific exceptions, log with SLF4J, return appropriate status codes
- **Dependency injection**: Use Spring's constructor injection
- **Documentation**: Maintain Javadoc for classes and public methods

## Project Structure
After initialization, a Gepetto project will have the following structure:
```
gepetto/
  ├── config.yaml      # Configuration
  ├── tasks/           # Generic automation tasks
  │   ├── login.gpt    # Example task file 
  └── results/         # Execution results
```

## Task File Format
```
# Task Name
description: "Brief description of task purpose"
tags: [tag1, tag2]
author: "Author Name"
created: "YYYY-MM-DD"

Task:
  Step 1 in natural language.
  Step 2 in natural language.
  ...
  Verification step in natural language.
```

## Product Overview
Gepetto enables natural language task automation without programming knowledge. The app parses plain English task instructions, executes them automatically, and generates user-friendly reports. Focus on maintaining the core components: task parsing, execution service, and CLI commands.