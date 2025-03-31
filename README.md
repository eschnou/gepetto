# Gepetto

Gepetto is an AI-powered natural language testing framework that allows anyone to write and run software tests using plain English.

## Project Status

This is a minimal implementation of Gepetto that supports:
- Basic configuration (target hostname)
- Running a test described in natural language
- Capturing test results

The actual test execution logic is not implemented in this version and will be added later.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
# Show main help
./mvnw spring-boot:run -Dspring-boot.run.arguments="help"

# Show help for a specific command
./mvnw spring-boot:run -Dspring-boot.run.arguments="help run"

# Show version information
./mvnw spring-boot:run -Dspring-boot.run.arguments="version"

# Configure the default hostname
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --hostname example.com"

# Enable debug mode
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --debug"

# Disable debug mode
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --no-debug"

# Run a test file (using configured hostname)
./mvnw spring-boot:run -Dspring-boot.run.arguments="run path/to/testfile.test"

# Run a test file (with explicit hostname)
./mvnw spring-boot:run -Dspring-boot.run.arguments="run path/to/testfile.test --hostname example.com"

# Run a test file with debug output
./mvnw spring-boot:run -Dspring-boot.run.arguments="run path/to/testfile.test --debug"
```

For development, you can create a JAR file:
```bash
./mvnw package -P dev
java -jar target/gepetto-0.0.1-SNAPSHOT.jar run path/to/testfile.test
```

## Command Line Interface

Gepetto provides a command-line interface for running tests.

### Configuration

Gepetto stores its configuration in `~/.gepetto/config.yaml`. This file is created automatically when you first configure the application.

Configure the target hostname:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="configure --hostname example.com"
```

### Running Tests

Run a test by providing a test file:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="run --hostname example.com --file path/to/testfile.test"
```

## Test File Format

Tests can be defined in text files with the following format:

```
# Test Name
description: "Description of the test"
tags: [tag1, tag2, tag3]
author: "Author Name"
created: "2025-03-15"

Test:
  Visit the login page.
  Enter username and password.
  Click login button.
  Verify user is logged in.
```

## Project Structure

- `config/` - Application configuration
- `model/` - Domain model classes
- `service/` - Business logic services
- `cli/` - Command-line interface components

## Next Steps

Future development will include:
- Implementing the actual test execution logic
- Adding test file loading and storage
- Creating a test management system
- Implementing reporting features