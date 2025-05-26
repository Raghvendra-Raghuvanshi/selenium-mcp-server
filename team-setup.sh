#!/bin/bash

# Selenium MCP Server - Team Setup Script
# This script automatically installs and configures the Selenium MCP server for Cursor

set -e

echo "🚀 Setting up Selenium MCP Server for your team..."
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ first."
    echo "Visit: https://nodejs.org/"
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js version 18+ is required. Current version: $(node -v)"
    echo "Please update Node.js: https://nodejs.org/"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11+ first."
    echo "Visit: https://adoptium.net/"
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Install the package
echo "📦 Installing selenium-mcp-server..."
if command -v npm &> /dev/null; then
    npm install -g selenium-mcp-server
elif command -v yarn &> /dev/null; then
    yarn global add selenium-mcp-server
else
    echo "❌ Neither npm nor yarn found. Please install Node.js first."
    exit 1
fi

echo "✅ Package installed successfully"
echo ""

# Test installation
echo "🧪 Testing installation..."
if selenium-mcp --help > /dev/null 2>&1; then
    echo "✅ CLI is working correctly"
else
    echo "❌ CLI test failed. Please check the installation."
    exit 1
fi

# Create Cursor configuration
echo "⚙️  Configuring Cursor..."

# Create global Cursor config directory
CURSOR_CONFIG_DIR="$HOME/.cursor"
mkdir -p "$CURSOR_CONFIG_DIR"

CURSOR_CONFIG_FILE="$CURSOR_CONFIG_DIR/mcp.json"

# Check if config file exists
if [ -f "$CURSOR_CONFIG_FILE" ]; then
    echo "📝 Updating existing Cursor MCP configuration..."
    # Backup existing config
    cp "$CURSOR_CONFIG_FILE" "$CURSOR_CONFIG_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    echo "   (Backup created: $CURSOR_CONFIG_FILE.backup.$(date +%Y%m%d_%H%M%S))"
    
    # Check if selenium server already exists
    if grep -q '"selenium"' "$CURSOR_CONFIG_FILE"; then
        echo "   Selenium MCP server already configured in Cursor"
    else
        echo "   Adding selenium server to existing configuration..."
        # This is a simple merge - for complex configs, teams should merge manually
        echo "   Please manually add the selenium configuration to your existing mcp.json"
        echo "   See the example configuration below."
    fi
else
    echo "📝 Creating new Cursor MCP configuration..."
    cat > "$CURSOR_CONFIG_FILE" << 'EOF'
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--headless"]
    }
  }
}
EOF
    echo "✅ Configuration created at: $CURSOR_CONFIG_FILE"
fi

echo ""
echo "🎉 Setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Restart Cursor completely"
echo "2. Open your project in Cursor"
echo "3. Try asking Cursor: 'Navigate to google.com and take a screenshot'"
echo ""
echo "📖 Configuration file: $CURSOR_CONFIG_FILE"
echo "🔧 To customize, edit the configuration file with different options:"
echo ""
echo "Example configurations:"
echo ""
echo "Basic:"
echo '{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp"
    }
  }
}'
echo ""
echo "Advanced:"
echo '{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080",
        "--output-dir", "./screenshots"
      ]
    }
  }
}'
echo ""
echo "🆘 Troubleshooting:"
echo "- If Cursor doesn't detect the server, restart Cursor completely"
echo "- Check that Java 11+ is installed: java --version"
echo "- Test CLI directly: selenium-mcp --help"
echo "- Check Cursor developer console (Cmd+Shift+I) for errors"
echo ""
echo "📚 Documentation: https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server"
echo "🐛 Issues: https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server/issues"
