# Gepetto - A cli-based Operator for QA/DevOps tasks

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


**Gepetto** is a minimalistic cli-based Operator: an AI Agent that uses its own browser (and any other tools) 
to perform tasks for you. The primary use case is running browser test cases, but it could be used for any 
other task automation needs. It supports a wide variety of AI models and can leverage any tools exposed through MCP.

## 🛠️️ Key Features

- Execute a task described in natural language
- Sequential step processing, only proceed to next step if previous succeeded
- Reporting task results in JUnit XML
- Support a wide variety of LLM (gpt, claude, ollama, etc..)
- Supports MCP to connect to third party actions providers
- Can run headless with cli-only on a server (Ubuntu)

## 🤖 Use Cases

Gepetto excels at automating tasks that require interacting with systems lacking API. This could be 
browser based interaction, data collection from unstructured log files, processing emails, etc... Below
is a list of use cases that I'm exploring in a development/operation context.

- **QA Automation**: Execute 'manual' QA on web properties to ensure end-to-end flow are functional.
- **Liveness check**: Periodically simulate real user behavior to ensure systems are live and working as expected.
- **Data scraping**: Connect to web-only dashboard/tools to collect data and export it into machine-readable formats.
- **Logs monitoring**: Process system logs to detect unusual behaviors.
- **Report generation**: Collect, analyze, and summarize content from various dashboards into daily reports.

## 📝 Example

### Sample task

Here is a simple task: performing a liveness/security check of the login/logout on a web property.

```
# Task Name
description: "Website login/logout test."
tags: [smoketest, qa]
tools: [web, test]
author: "Laurent"
created: "2025-03-15"

Test:
  Visit ${HOSTNAME}.
  Navigate to the login page.
  Login with username ${USERNAME} and password ${PASSWORD}.
  Verify user is logged in and on the main dashboard.
  Click the logout button.
  Verify you are back to the login page.
```

### Sample task result

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
3. Log in with username johndoe and password secret.
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

## 🏗️ Install

### Pre-requisite

- You need a Java 21 or above
- You need to install Playwright MCP if you want to control a browser

```bash
# Install Playwright MCP
npm install @playwright/mcp
npx playwright install
npx playwright install chrome
```

### Quick oneliner install

```bash
curl -s https://dist.gepetto.sh/releases/install.sh | bash
```

Then you should edit `~/.gepetto/application.properties` and set your Open AI key. 

## 🕹️Usage

By default, Gepetto uses OpenAI gpt-4o as LLM. You therefore need an API key defined
either as en environment variable or in a .env file.

```bash

# Create a new gepetto project or add gepetto to an existing one
mkdir sample-project
cd sample-project

# Initialize the project
gepetto init

# Run your first task
gepetto run gepetto/tasks/hello.gpt
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

If you want to run Gepetto on a server (without UI) in headless mode, you need to create a MCP
configuration at `~/.gepetto/config/mcp-config.json` and then set the configuration path in application.properties.

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

### Build from sources

```bash
# Clone repository
git clone https://github.com/eschnou/gepetto.git
cd gepetto

# Build executable JAR
./mvnw clean package

# Install to local user
# ./install.sh
```

### Running from Source

```bash
# Run directly with Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments="help"

# Build and run JAR
./mvnw clean package
java -jar target/gepetto-0.0.1-SNAPSHOT.jar help
```

## 💡 Roadmap

- Ensure everything can be configured without requiring a rebuild
- Make it a single executable that be easily deployed
- Add non-mcp tooling to support server side DevOps automation
- You got ideas? File an issue!

## Support
- Join us on [Discord](https://discord.gg/3XPsmCRNE2)
- Reach out on [Bluesky](https://bsky.app/profile/eschnou.com)
- File an [issue](https://github.com/eschnou/gepetto/issues)

## License

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
