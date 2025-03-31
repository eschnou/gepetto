# Gepetto

AI-powered natural language testing framework that allows anyone to write and run software tests using plain English.

## Project Status

This is a minimal implementation of Gepetto that supports:
- Basic configuration (target hostname)
- Running a test described in natural language
- Capturing test results

## Installation

```bash
# Clone repository
git clone https://github.com/your-username/gepetto.git
cd gepetto

# Build executable JAR
./mvnw clean package

# Install to system (requires sudo)
sudo ./install.sh

# Or run locally without installing
./gepetto help
```

## Usage

```bash
# Initialize a new project
gepetto init

# Configure settings
gepetto configure --hostname example.com
gepetto configure --debug

# Run a test file
gepetto run path/to/test.test
```

## Test File Format

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

## Clearing the cache

Playwright MCP will launch Chrome browser with the new profile, located at
- `%USERPROFILE%\AppData\Local\ms-playwright\mcp-chrome-profile` on Windows
- `~/Library/Caches/ms-playwright/mcp-chrome-profile` on macOS
- `~/.cache/ms-playwright/mcp-chrome-profile` on Linux

All the logged in information will be stored in that profile, you can delete it between 
sessions if you'd like to clear the offline state.

## Development

### Running from Source

```bash
# Run directly with Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments="help"

# Build and run JAR
./mvnw clean package
java -jar target/gepetto-0.0.1-SNAPSHOT.jar help
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