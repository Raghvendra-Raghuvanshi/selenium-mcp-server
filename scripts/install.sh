#!/bin/bash

# Selenium MCP Server Installation Script

set -e

echo "🚀 Installing Selenium MCP Server..."

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
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11+ first."
    echo "Visit: https://adoptium.net/"
    exit 1
fi

# Install the package globally
echo "📦 Installing selenium-mcp-server..."
npm install -g selenium-mcp-server

# Create Cursor configuration directory
CURSOR_CONFIG_DIR="$HOME/.cursor"
mkdir -p "$CURSOR_CONFIG_DIR"

# Create or update Cursor MCP configuration
CURSOR_CONFIG_FILE="$CURSOR_CONFIG_DIR/mcp.json"

if [ -f "$CURSOR_CONFIG_FILE" ]; then
    echo "📝 Updating existing Cursor MCP configuration..."
    # Backup existing config
    cp "$CURSOR_CONFIG_FILE" "$CURSOR_CONFIG_FILE.backup"
    
    # Add selenium server to existing config (basic merge)
    # Note: This is a simple approach - for complex configs, use a JSON tool
    if ! grep -q "selenium" "$CURSOR_CONFIG_FILE"; then
        # Remove the last } and add selenium config
        sed -i '' '$d' "$CURSOR_CONFIG_FILE"
        cat >> "$CURSOR_CONFIG_FILE" << 'EOF'
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--headless"]
    }
  }
}
EOF
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
fi

echo "✅ Installation complete!"
echo ""
echo "🎯 Next steps:"
echo "1. Restart Cursor"
echo "2. Try asking Cursor: 'Navigate to google.com and take a screenshot'"
echo ""
echo "🔧 Configuration file created at: $CURSOR_CONFIG_FILE"
echo "📚 Documentation: https://github.com/yourusername/selenium-mcp-server"
echo ""
echo "🧪 Test the installation:"
echo "  selenium-mcp --help"
