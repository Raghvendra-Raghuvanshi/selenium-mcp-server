# Selenium MCP Server - Team Setup Guide

## Overview

This guide explains how to set up the Selenium MCP server for your team to use with Cursor for browser automation.

## Deployment Options

### Option 1: Shared Server (Recommended for Teams)

Deploy the server on a shared machine that all team members can access.

#### 1. Deploy on a Server

```bash
# On your server machine
git clone <your-repo>
cd SeleniumMCP3
mvn clean package

# Start the server (accessible to team)
./start-server.sh 8931 0.0.0.0
```

#### 2. Team Members Configure Cursor

Each team member adds this to their `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "selenium": {
      "url": "http://YOUR_SERVER_IP:8931/sse"
    }
  }
}
```

Replace `YOUR_SERVER_IP` with your actual server IP address.

### Option 2: Docker Deployment

#### 1. Build and Run with Docker

```bash
# Build the Docker image
docker build -t selenium-mcp .

# Run the container
docker run -d -p 8931:8931 --name selenium-mcp selenium-mcp
```

#### 2. Or use Docker Compose

```bash
docker-compose up -d
```

#### 3. Team Configuration

Same as Option 1 - team members use the server URL in their Cursor config.

### Option 3: Individual Installation

Each team member installs their own copy.

#### 1. Create Distribution Package

```bash
./create-distribution.sh
```

This creates `selenium-mcp-v0.0.1.zip`.

#### 2. Team Members Install

1. Download the zip file
2. Extract it
3. Run `./install.sh`
4. Restart Cursor

## Usage Examples

Once configured, team members can ask Cursor:

- **"Navigate to our staging environment and take a screenshot"**
- **"Fill out the login form on our app"**
- **"Extract all the links from the homepage"**
- **"Test if the contact form works"**
- **"Take screenshots of our app in different viewport sizes"**

## Available Tools

| Tool | Description |
|------|-------------|
| `browser_navigate` | Navigate to URLs |
| `browser_snapshot` | Capture page accessibility tree |
| `browser_take_screenshot` | Take screenshots |
| `browser_click` | Click elements |
| `browser_type` | Type text into inputs |
| `browser_hover` | Hover over elements |
| `browser_wait_for` | Wait for elements/text |
| `browser_tab_list` | List open tabs |
| `browser_tab_new` | Open new tabs |
| `browser_tab_select` | Switch tabs |
| `browser_tab_close` | Close tabs |
| `browser_drag` | Drag and drop |
| `browser_select_option` | Select dropdown options |
| `browser_press_key` | Press keyboard keys |
| `browser_file_upload` | Upload files |
| `browser_handle_dialog` | Handle alerts/dialogs |
| `browser_resize` | Resize browser window |
| `browser_pdf_save` | Save page as PDF |

## Security Considerations

### For Shared Server Deployment:

1. **Network Security**: Only allow access from trusted networks
2. **Authentication**: Consider adding authentication if needed
3. **Resource Limits**: Monitor server resources and set limits
4. **Firewall**: Configure firewall rules appropriately

### Configuration Example with Security:

```bash
# Start server with specific host binding
./start-server.sh 8931 192.168.1.100  # Only accessible from local network
```

## Monitoring and Maintenance

### Health Check

```bash
curl http://your-server:8931/
```

### Logs

Check server logs for issues:
```bash
# If running with Docker
docker logs selenium-mcp

# If running directly
# Check the terminal where you started the server
```

### Updates

To update the server:
1. Build new version: `mvn clean package`
2. Restart the server
3. Team members don't need to change anything

## Troubleshooting

### Common Issues:

1. **"Connection refused"**
   - Check if server is running
   - Verify firewall settings
   - Check the correct IP/port

2. **"Browser not found"**
   - Install Chrome on the server
   - Use `--browser chrome --headless` options

3. **"Permission denied"**
   - Check file permissions
   - Ensure Java is installed

### Getting Help

1. Check server logs
2. Test with curl: `curl http://server:8931/`
3. Verify Cursor MCP configuration
4. Contact your development team

## Best Practices

1. **Use headless mode** for server deployments
2. **Set resource limits** to prevent server overload
3. **Regular updates** to keep the server current
4. **Monitor usage** to understand team needs
5. **Backup configurations** for easy recovery

## Example Team Workflow

1. **Developer asks Cursor**: "Test our login flow"
2. **Cursor uses MCP tools** to:
   - Navigate to login page
   - Fill username/password
   - Click login button
   - Take screenshot of result
3. **Developer gets** automated test results and screenshots

This enables teams to use natural language for browser automation tasks!
