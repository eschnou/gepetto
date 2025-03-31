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

# Run a task file
gepetto run gepetto/tasks/login.gpt
```

## Task File Format

```
# Task Name
description: "Description of the task"
tags: [tag1, tag2, tag3]
author: "Author Name"
created: "2025-03-15"

Test:
  Visit the login page at ${HOSTNAME}.
  Login with username ${USERNAME} and password ${PASSWORD}.
  Verify user is logged in and on the main dashboard.
  Click the logout button.
  Verify you are back to the login page.
```

## Variables

Gepetto supports variables in task files using the `${VARIABLE}` syntax. Variables allow your 
tasks to be reusable across different environments and scenarios.

### Variable Sources

Variables can be defined from two sources:

1. **Configuration file** - Stored in `gepetto/config.yaml`:
   ```yaml
   variables:
     HOSTNAME: "example.com"
     USERNAME: "testuser"
   ```

2. **Command line arguments** - Using the `--var` or `-v` flag:
   ```bash
   gepetto run task.gpt --var PASSWORD=secret123
   ```

### Precedence

When the same variable is defined in multiple places, command line arguments take precedence 
over the configuration file.

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

# License

MIT License

Copyright (c) 2024-2025 Laurent Eschenauer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.