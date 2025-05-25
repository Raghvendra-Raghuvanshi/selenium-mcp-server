#!/bin/bash

# Create distribution package for teams

VERSION="0.0.1"
DIST_DIR="selenium-mcp-dist"

echo "Creating Selenium MCP distribution package..."

# Create distribution directory
rm -rf $DIST_DIR
mkdir -p $DIST_DIR

# Copy JAR file
cp target/selenium-mcp-0.0.1.jar $DIST_DIR/

# Create installation script
cat > $DIST_DIR/install.sh << 'EOF'
#!/bin/bash

echo "Installing Selenium MCP Server..."

# Create installation directory
INSTALL_DIR="$HOME/.selenium-mcp"
mkdir -p "$INSTALL_DIR"

# Copy JAR file
cp selenium-mcp-0.0.1.jar "$INSTALL_DIR/"

# Create startup script
cat > "$INSTALL_DIR/start-server.sh" << 'SCRIPT_EOF'
#!/bin/bash
java -jar "$HOME/.selenium-mcp/selenium-mcp-0.0.1.jar" "$@"
SCRIPT_EOF

chmod +x "$INSTALL_DIR/start-server.sh"

# Create Cursor configuration
mkdir -p "$HOME/.cursor"
cat > "$HOME/.cursor/mcp.json" << 'CONFIG_EOF'
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": [
        "-jar",
        "$HOME/.selenium-mcp/selenium-mcp-0.0.1.jar"
      ]
    }
  }
}
CONFIG_EOF

echo "Installation complete!"
echo "Restart Cursor to use the Selenium MCP server."
echo ""
echo "You can also run the server standalone with:"
echo "  $HOME/.selenium-mcp/start-server.sh --port 8931"
EOF

chmod +x $DIST_DIR/install.sh

# Create README for teams
cat > $DIST_DIR/README.md << 'EOF'
# Selenium MCP Server

This package contains the Selenium MCP server for use with Cursor.

## Installation

1. Run the installation script:
   ```bash
   ./install.sh
   ```

2. Restart Cursor

3. You can now use browser automation in Cursor!

## Usage Examples

Ask Cursor to:
- "Navigate to google.com and take a screenshot"
- "Fill out a form on example.com"
- "Extract the title from a webpage"

## Standalone Server

You can also run the server as a standalone service:

```bash
# Start server on default port (8931)
~/.selenium-mcp/start-server.sh --port 8931

# Use in team configuration
{
  "mcpServers": {
    "selenium": {
      "url": "http://your-server:8931/sse"
    }
  }
}
```

## Available Tools

- browser_navigate: Navigate to URLs
- browser_snapshot: Capture page accessibility tree
- browser_take_screenshot: Take screenshots
- browser_click: Click elements
- browser_type: Type text
- browser_hover: Hover over elements
- browser_wait_for: Wait for elements/text
- browser_tab_*: Tab management
- And many more...

## Support

For issues or questions, contact your development team.
EOF

# Create team configuration examples
mkdir -p $DIST_DIR/examples

cat > $DIST_DIR/examples/local-cursor-config.json << 'EOF'
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": [
        "-jar",
        "$HOME/.selenium-mcp/selenium-mcp-0.0.1.jar"
      ]
    }
  }
}
EOF

cat > $DIST_DIR/examples/remote-cursor-config.json << 'EOF'
{
  "mcpServers": {
    "selenium": {
      "url": "http://your-selenium-mcp-server:8931/sse"
    }
  }
}
EOF

# Create zip package
zip -r selenium-mcp-v$VERSION.zip $DIST_DIR/

echo "Distribution package created: selenium-mcp-v$VERSION.zip"
echo "Teams can download and install this package."
