#!/bin/bash
# Installation script for Gepetto

# Ensure the script is run with sufficient privileges
if [ "$(id -u)" -ne 0 ]; then
  echo "This script must be run as root (use sudo)" >&2
  exit 1
fi

# Set installation paths
INSTALL_DIR="/usr/local/share/gepetto"
BIN_PATH="/usr/local/bin/gepetto"

# Create installation directory if it doesn't exist
mkdir -p "$INSTALL_DIR"

# Get the version from the .version file
VERSION=$(cat .version | head -1)

# Build the JAR if not already built
if [ ! -f "target/gepetto-$VERSION.jar" ]; then
  echo "Building Gepetto JAR..."
  ./mvnw clean package
fi

# Copy the JAR to the installation directory
echo "Installing Gepetto to $INSTALL_DIR..."
cp "target/gepetto-$VERSION.jar" "$INSTALL_DIR/gepetto.jar"

# Create the wrapper script in /usr/local/bin
cat > "$BIN_PATH" << 'EOF'
#!/bin/bash
# Gepetto CLI wrapper

# Load environment variables from .env file if it exists in current directory
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Run the JAR with all arguments passed to this script
java -jar "/usr/local/share/gepetto/gepetto.jar" "$@"
EOF

# Make the wrapper script executable
chmod +x "$BIN_PATH"

echo "Gepetto has been installed successfully!"
echo "You can now run 'gepetto' from anywhere on your system."