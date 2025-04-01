# Gepetto - A minimalistic cli-based Operator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


**Gepetto** is a minimalistic cli-based Operator: an AI Agent that uses its own browser to perform tasks for you.
The primary use case is running browser test cases, but it could be used for any other task automation needs. It
supports a wide variety of AI models and can leverage any tools exposed through MCP.

## 🛠️️ Key Features

- Execute a task described in natural language
- Sequential step processing, only proceed to next step if previous succeeded
- Reporting task results in JUnit XML
- Support a wide variety of LLM (gpt, claude, ollama, etc..)
- Supports MCP to connect to third party actions providers

## 🤖Example of a Test Execution

```
===== TASK RESULT =====
Task: login
Description: Log in to the application with valid credentials
Status: SUCCESS
Execution Time: 2025-04-01 07:29:29
Duration: 68753ms

----- Step Results -----
1. Navigate to https://wonderpod.ai.
   Status: SUCCESS
   Details: The step to navigate to the URL 'https://wonderpod.ai' was already completed successfully.
2. Navigate to the login page.
   Status: SUCCESS
   Details: The step to navigate to the login page was already completed successfully.
3. Log in with username testuser2 and password trustno1.
   Status: SUCCESS
   Details: The login with username johndoe and password secret was successful, and the user was redirected to the dashboard.
4. Verify that the dashboard page is displayed.
   Status: SUCCESS
   Details: The dashboard page is successfully displayed, as verified by the accessibility snapshot.
5. Logout from the application with the last icon on the right.
   Status: SUCCESS
   Details: The logout was successful, and the user has been redirected to the login page.
6. Verify that you are back to the login screen.
   Status: SUCCESS
   Details: Verification complete: The user is back on the login screen as confirmed by the accessibility snapshot.

=======================

Test report saved to: gepetto/results/login/20250401_072929/junit-report.xml

```

## 🏗️ Installation

```bash
# Clone repository
git clone https://github.com/eschnou/gepetto.git
cd gepetto

# Build executable JAR
./mvnw clean package

# Install Playwright MCP
npm install @playwright/mcp
npx playwright install
npx playwright install chrome

# Run locally without installing
./gepetto help

# Or install to system (requires sudo)
sudo ./install.sh
```

## 🕹️Usage

By default, Gepetto uses OpenAI gpt-4o as LLM. You therefore need an API key defined
either as en environment variable or in a .env file.

```bash
# Export your Openai key (check documentation to use other providers)
export OPENAI_API_KEY=sk-***

# Alternate is to store it in a .env file
echo OPENAI_API_KEY=sk-*** > .env

# Initialize a new project
gepetto init

# Run a task file
gepetto run gepetto/tasks/login.gpt
```

### Task File Format

```
# Task Name
description: "Description of the task"
tags: [tag1, tag2, tag3]
author: "Author Name"
created: "2025-03-15"

Test:
  Visit ${HOSTNAME}.
  Navigate to the login page.
  Login with username ${USERNAME} and password ${PASSWORD}.
  Verify user is logged in and on the main dashboard.
  Click the logout button.
  Verify you are back to the login page.
```

### Variables

Gepetto supports variables in task files using the `${VARIABLE}` syntax. Variables allow your 
tasks to be reusable across different environments and scenarios.

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

## 🔧Configuration

### LLM Configuration

Gepetto is built with [OpenGPA](https://github.com/eschnou/OpenGPA), an open source agentic orchestration
framework built in Java. It is built on top of [Spring AI](https://docs.spring.io/spring-ai/reference/index.html) and thus
supports many different Chat Models. Do refer to the OpenGPA and Spring documentation to configure Gepetto to your needs.

### Playwright Configuration

Gepetto is controlling a browser using Playwright over the MCP protocol. For details
on the Playwright configuration and caching; visit their [plugin documentation](https://github.com/microsoft/playwright-mcp.

Playwright MCP will launch Chrome browser with the new profile, located at
- `%USERPROFILE%\AppData\Local\ms-playwright\mcp-chrome-profile` on Windows
- `~/Library/Caches/ms-playwright/mcp-chrome-profile` on macOS
- `~/.cache/ms-playwright/mcp-chrome-profile` on Linux

All the logged in information will be stored in that profile, you can delete it between 
sessions if you'd like to clear the offline state.

### Running headless on a server

If you want to run Gepetto on a server (without UI) in headless mode, you need to modify the MCP
configuration at `src/main/resources/mcp-config.json` and then rebuild the application.

```
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--headless"
      ]
    }
  }
}
```

## 👷‍♂️Development

### Running from Source

```bash
# Run directly with Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments="help"

# Build and run JAR
./mvnw clean package
java -jar target/gepetto-0.0.1-SNAPSHOT.jar help
```

# Support
- Join us on [Discord](https://discord.gg/3XPsmCRNE2)
- Reach out on [Bluesky](https://bsky.app/profile/eschnou.com)
- File an [issue](https://github.com/eschnou/gepetto/issues)

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
