# Team Usage Guide - Selenium MCP Server

This guide explains how teams can install and use the Selenium MCP Server with Cursor for browser automation.

## 🚀 Quick Start (2 minutes)

### One-Line Setup
```bash
curl -fsSL https://raw.githubusercontent.com/Raghvendra-Raghuvanshi/selenium-mcp-server/main/team-setup.sh | bash
```

### Manual Setup
```bash
# 1. Install the package
npm install -g selenium-mcp-server

# 2. Configure Cursor (create ~/.cursor/mcp.json)
mkdir -p ~/.cursor
cat > ~/.cursor/mcp.json << 'EOF'
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--headless"]
    }
  }
}
EOF

# 3. Restart Cursor and you're ready!
```

## 📋 Prerequisites

- **Node.js 18+**: [Download here](https://nodejs.org/)
- **Java 11+**: [Download here](https://adoptium.net/)
- **Cursor IDE**: [Download here](https://cursor.sh/)
- **Chrome browser** (or Firefox/Edge/Safari)

## ⚙️ Configuration Options

### Basic Configuration
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp"
    }
  }
}
```

### Production Configuration
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080",
        "--output-dir", "./automation-outputs"
      ]
    }
  }
}
```

### Multi-Browser Setup
```json
{
  "mcpServers": {
    "selenium-chrome": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--headless"]
    },
    "selenium-firefox": {
      "command": "selenium-mcp",
      "args": ["--browser", "firefox", "--headless"]
    }
  }
}
```

### Development Configuration (with visible browser)
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--viewport-size", "1280,720"
      ]
    }
  }
}
```

## 🎯 Usage Examples

Once configured, team members can ask Cursor:

### Web Testing
- "Navigate to our staging site and take a screenshot"
- "Test the login flow on our application"
- "Check if all links on the homepage work"
- "Fill out the contact form with test data"

### Data Extraction
- "Extract all product names and prices from the e-commerce page"
- "Get all email addresses from the contact page"
- "Scrape the latest news headlines from the website"

### Quality Assurance
- "Take screenshots of our app in different viewport sizes"
- "Test the mobile responsiveness of our landing page"
- "Check if the search functionality works correctly"
- "Verify that all forms validate properly"

### Competitive Analysis
- "Compare our pricing page with competitor X"
- "Take screenshots of competitor websites for analysis"
- "Extract feature lists from competitor product pages"

## 🛠️ Available Tools

| Tool | Description | Example Usage |
|------|-------------|---------------|
| `browser_navigate` | Navigate to URLs | "Go to google.com" |
| `browser_snapshot` | Get page structure | "Get the page structure for analysis" |
| `browser_take_screenshot` | Capture screenshots | "Take a screenshot of the current page" |
| `browser_click` | Click elements | "Click the login button" |
| `browser_type` | Type text | "Type 'hello world' in the search box" |
| `browser_hover` | Hover over elements | "Hover over the menu item" |
| `browser_wait_for` | Wait for elements | "Wait for the page to load" |
| `browser_tab_*` | Tab management | "Open a new tab and go to example.com" |
| `browser_drag` | Drag and drop | "Drag the item to the shopping cart" |
| `browser_select_option` | Select dropdowns | "Select 'United States' from country dropdown" |
| `browser_file_upload` | Upload files | "Upload the test image file" |
| `browser_pdf_save` | Save as PDF | "Save the current page as PDF" |

## 🏢 Team Deployment Strategies

### Option 1: Individual Installation
Each team member installs on their machine:
```bash
npm install -g selenium-mcp-server
```

**Pros**: Simple, no infrastructure needed
**Cons**: Version management, individual setup

### Option 2: Shared Server
Deploy on a shared server for team access:
```bash
# On server
npm install -g selenium-mcp-server
selenium-mcp --port 8931 --host 0.0.0.0

# Team members configure:
{
  "mcpServers": {
    "selenium": {
      "url": "http://your-server:8931/sse"
    }
  }
}
```

**Pros**: Centralized, consistent environment
**Cons**: Single point of failure, requires server management

### Option 3: Docker Deployment
```bash
# Pull and run
docker run -d -p 8931:8931 selenium-mcp-server

# Or build from source
docker build -t selenium-mcp .
docker run -d -p 8931:8931 selenium-mcp
```

## 🔧 Troubleshooting

### Common Issues

#### "Command not found: selenium-mcp"
```bash
# Reinstall globally
npm install -g selenium-mcp-server

# Check installation
which selenium-mcp
selenium-mcp --help
```

#### "Java not found"
```bash
# Install Java 11+
# macOS: brew install openjdk@11
# Ubuntu: sudo apt install openjdk-11-jdk
# Windows: Download from https://adoptium.net/

# Verify installation
java --version
```

#### "Browser not found"
```bash
# Install Chrome
# macOS: brew install --cask google-chrome
# Ubuntu: sudo apt install google-chrome-stable
# Windows: Download from https://chrome.google.com/

# Or specify custom path
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--executable-path", "/path/to/chrome"]
    }
  }
}
```

#### "Cursor not detecting MCP server"
1. Restart Cursor completely (Cmd+Q, then reopen)
2. Check configuration file location: `~/.cursor/mcp.json`
3. Verify JSON syntax is valid
4. Check Cursor developer console (Cmd+Shift+I) for errors

### Debug Mode
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--output-dir", "./debug-logs"]
    }
  }
}
```

## 📊 Performance Tips

### For Better Performance
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--headless",
        "--isolated",
        "--viewport-size", "1280,720"
      ]
    }
  }
}
```

### For CI/CD Environments
```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--headless",
        "--isolated",
        "--output-dir", "./test-outputs"
      ]
    }
  }
}
```

## 🆘 Support

- **Documentation**: [GitHub Repository](https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server)
- **Issues**: [GitHub Issues](https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server/issues)
- **npm Package**: [npm Page](https://www.npmjs.com/package/selenium-mcp-server)

## 🎉 Success Stories

Teams are using Selenium MCP Server for:
- **QA Automation**: Automated testing workflows
- **Content Management**: Bulk content updates
- **Competitive Analysis**: Market research automation
- **Data Collection**: Web scraping for analytics
- **UI Testing**: Cross-browser compatibility testing

Start automating your browser tasks with natural language today!
