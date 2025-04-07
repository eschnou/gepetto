#!/bin/bash
# Java CLI Application Installer
# Usage: curl -s https://example.com/install.sh | bash
# or: wget -qO- https://example.com/install.sh | bash

set -e

# Print messages
print_info() { echo "[INFO] $1"; }
print_success() { echo "[SUCCESS] $1"; }
print_error() { echo "[ERROR] $1"; }

# Configuration
APP_NAME="gepetto"
JAR_URL="https://dist.gepetto.sh/releases/gepetto-latest.jar"
MIN_JAVA_VERSION="21"
INSTALL_DIR="${HOME}/.local/share/${APP_NAME}"
BIN_DIR="${HOME}/.local/bin"
JAR_PATH="${INSTALL_DIR}/${APP_NAME}.jar"

# Check if running from source directory with target jar
if [ -f ".version" ] && [ -d "target" ]; then
  VERSION=$(cat .version)
  LOCAL_JAR="target/gepetto-${VERSION}.jar"
  if [ -f "$LOCAL_JAR" ]; then
    print_info "Local build found at $LOCAL_JAR"
    SKIP_DOWNLOAD=true
    LOCAL_SOURCE=true
  fi
fi

# Function to check if a command exists
command_exists() {
  command -v "$1" >/dev/null 2>&1
}

# Check for Java installation
if ! command_exists java; then
  print_error "Java is not installed. Please install Java ${MIN_JAVA_VERSION} or higher."
  exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [[ -z "$JAVA_VERSION" || "$JAVA_VERSION" -lt "$MIN_JAVA_VERSION" ]]; then
  print_error "Java ${MIN_JAVA_VERSION} or higher is required, but version ${JAVA_VERSION} was found."
  exit 1
fi

print_info "Java version ${JAVA_VERSION} found. Continuing installation..."

# Create directories if they don't exist
mkdir -p "${INSTALL_DIR}" "${BIN_DIR}"

# Copy local JAR or download it
if [ "$SKIP_DOWNLOAD" = "true" ] && [ "$LOCAL_SOURCE" = "true" ]; then
  print_info "Copying local build to ${JAR_PATH}..."
  cp "$LOCAL_JAR" "${JAR_PATH}"
elif [ "$SKIP_DOWNLOAD" != "true" ]; then
  print_info "Downloading application..."
  if command_exists curl; then
    curl -s -L "${JAR_URL}" -o "${JAR_PATH}"
  elif command_exists wget; then
    wget -q "${JAR_URL}" -O "${JAR_PATH}"
  else
    print_error "Neither curl nor wget found. Cannot download the application."
    exit 1
  fi
fi

# Make the JAR executable
chmod +x "${JAR_PATH}"

# Create wrapper script
cat > "${BIN_DIR}/${APP_NAME}" << EOF
#!/bin/bash

# Run Java with additional Spring config
SPRING_CONFIG_LOCATION="file:\${HOME}/.gepetto/application.properties"
export SPRING_CONFIG_ADDITIONAL_LOCATION="\${SPRING_CONFIG_LOCATION}"

java -jar "${JAR_PATH}" "\$@"
EOF

# Make wrapper script executable
chmod +x "${BIN_DIR}/${APP_NAME}"

# Check if BIN_DIR is in PATH, if not suggest adding it
if [[ ":${PATH}:" != *":${BIN_DIR}:"* ]]; then
  print_info "Adding ${BIN_DIR} to your PATH..."
  SHELL_CONFIG=""

  if [[ -f "${HOME}/.bashrc" ]]; then
    SHELL_CONFIG="${HOME}/.bashrc"
  elif [[ -f "${HOME}/.zshrc" ]]; then
    SHELL_CONFIG="${HOME}/.zshrc"
  fi

  if [[ -n "${SHELL_CONFIG}" ]]; then
    echo "export PATH=\"\${PATH}:${BIN_DIR}\"" >> "${SHELL_CONFIG}"
    print_info "Added ${BIN_DIR} to ${SHELL_CONFIG}. Please restart your terminal or run 'source ${SHELL_CONFIG}'."
  else
    print_info "Please add ${BIN_DIR} to your PATH manually."
  fi
fi

# Create default config directory and properties file
CONFIG_DIR="${HOME}/.gepetto"
PROPERTIES_FILE="${CONFIG_DIR}/application.properties"

mkdir -p "${CONFIG_DIR}"

# Create default properties file if it doesn't exist
if [ ! -f "${PROPERTIES_FILE}" ]; then
    print_info "Creating default properties file..."
    cat > "${PROPERTIES_FILE}" << EOF
# Gepetto application properties
# This file overrides the default application settings

# API Keys
spring.ai.openai.api-key=sk-***

# Logging
logging.level.root=off
logging.level.org.springframework=off
logging.level.sh.gepetto=error

# Other settings
# spring.ai.openai.chat.options.model=gpt-4o
# spring.main.banner-mode=off
# spring.main.log-startup-info=false
EOF
    chmod 600 "${PROPERTIES_FILE}"
    print_info "Default properties file created at ${PROPERTIES_FILE}"
    print_info "Please edit ${PROPERTIES_FILE} and set your OpenAI API key"
fi

print_success "Gepetto has been installed successfully!"
print_info "You can run it by typing '${APP_NAME}' in your terminal."