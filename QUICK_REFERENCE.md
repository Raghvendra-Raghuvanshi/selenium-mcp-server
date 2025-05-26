# Selenium MCP Server - Quick Reference

## 🚀 Installation (30 seconds)

```bash
npm install -g selenium-mcp-server
```

## ⚙️ Cursor Configuration

Create `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome", "--headless"]
    }
  }
}
```

**Restart Cursor** after configuration.

## 💬 Example Commands

Ask Cursor these questions:

| What to Ask Cursor | What It Does |
|-------------------|--------------|
| "Navigate to google.com and take a screenshot" | Opens Google and captures the page |
| "Fill out the contact form with test data" | Automatically fills form fields |
| "Extract all links from this page" | Gets all hyperlinks from current page |
| "Test the login flow with username 'test' and password 'demo'" | Automates login process |
| "Take screenshots in mobile and desktop sizes" | Captures responsive design views |
| "Check if the search functionality works" | Tests search features |
| "Save the current page as PDF" | Exports page to PDF format |
| "Open 3 tabs with different competitor websites" | Multi-tab browsing automation |

## 🛠️ Configuration Options

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",           // chrome, firefox, edge, safari
        "--headless",                    // run without GUI
        "--viewport-size", "1920,1080",  // screen resolution
        "--output-dir", "./screenshots"  // save location
      ]
    }
  }
}
```

## 🔧 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Command not found" | `npm install -g selenium-mcp-server` |
| "Java not found" | Install Java 11+ from [adoptium.net](https://adoptium.net/) |
| "Browser not found" | Install Chrome or specify `--executable-path` |
| "Cursor not detecting" | Restart Cursor completely, check `~/.cursor/mcp.json` |

## 📚 More Info

- **GitHub**: https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server
- **npm**: https://www.npmjs.com/package/selenium-mcp-server
- **Full Guide**: See TEAM_USAGE_GUIDE.md
