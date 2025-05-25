# Cursor MCP Server Troubleshooting Guide

## Current Status
- ✅ Selenium MCP server is built and working
- ✅ JAR file is executable and responds correctly
- ✅ Server lists all available tools properly
- ❓ Cursor is not detecting the MCP server

## Configuration Files Created
We've created MCP configuration files in multiple locations:

1. **Project-specific**: `.cursor/mcp.json`
2. **User home**: `~/.cursor/mcp.json`
3. **Cursor app directory**: `~/Library/Application Support/Cursor/User/mcp.json`

## Troubleshooting Steps

### 1. Restart Cursor Completely
- Quit Cursor completely (Cmd+Q)
- Wait a few seconds
- Restart Cursor
- Open your project

### 2. Check Cursor Version
Make sure you're using a recent version of Cursor that supports MCP.

### 3. Check Cursor Settings
1. Open Cursor Settings (Cmd+,)
2. Look for "MCP" or "Model Context Protocol" settings
3. Make sure MCP is enabled

### 4. Check Cursor Developer Tools
1. Open Cursor
2. Press Cmd+Shift+I to open developer tools
3. Check the Console tab for any MCP-related errors

### 5. Try Manual Server Test
Run this command to test the server manually:
```bash
echo '{"type":"initialize","id":"test-1"}' | java -jar target/selenium-mcp-0.0.1.jar
```

### 6. Check Cursor Logs
Look for Cursor logs in:
- `~/Library/Logs/Cursor/`
- `~/Library/Application Support/Cursor/logs/`

### 7. Alternative Configuration Format
Some versions of Cursor might expect a different format. Try this in your mcp.json:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/raghvendraraghuvanshi/Downloads/SeleniumMCP3/target/selenium-mcp-0.0.1.jar"
      ],
      "env": {}
    }
  }
}
```

### 8. Check File Permissions
Make sure the JAR file is readable:
```bash
chmod +r target/selenium-mcp-0.0.1.jar
```

### 9. Try Different Configuration Location
Create the configuration in your project root:
```bash
cat > mcp.json << 'EOF'
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/raghvendraraghuvanshi/Downloads/SeleniumMCP3/target/selenium-mcp-0.0.1.jar"
      ]
    }
  }
}
