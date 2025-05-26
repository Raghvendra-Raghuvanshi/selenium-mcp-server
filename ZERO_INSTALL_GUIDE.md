# Zero-Installation Setup - Just Like Playwright MCP

Use Selenium MCP Server exactly like Microsoft's Playwright MCP - no manual installation required!

## 🚀 **One-Step Setup (Zero Installation)**

Teams just add this configuration to Cursor and it works immediately:

### **Basic Configuration**
```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": ["selenium-mcp-server@latest"]
    }
  }
}
```

### **Production Configuration**
```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080"
      ]
    }
  }
}
```

## 🔄 **Exact Same Pattern as Playwright MCP**

| Microsoft Playwright MCP | Your Selenium MCP |
|---------------------------|-------------------|
| **Configuration:** | **Configuration:** |
| ```json<br/>{<br/>  "mcpServers": {<br/>    "playwright": {<br/>      "command": "npx",<br/>      "args": ["@playwright/mcp@latest"]<br/>    }<br/>  }<br/>}``` | ```json<br/>{<br/>  "mcpServers": {<br/>    "selenium": {<br/>      "command": "npx",<br/>      "args": ["selenium-mcp-server@latest"]<br/>    }<br/>  }<br/>}``` |
| **What happens:** | **What happens:** |
| • npx downloads @playwright/mcp | • npx downloads selenium-mcp-server |
| • Runs automatically | • Runs automatically |
| • No manual installation | • No manual installation |
| • Always latest version | • Always latest version |

## 📋 **Complete Team Instructions**

### **Step 1: Add Configuration**
Create or edit `.cursor/mcp.json` in your project:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome",
        "--headless"
      ]
    }
  }
}
```

### **Step 2: Restart Cursor**
- Quit Cursor completely
- Restart Cursor
- Open your project

### **Step 3: Start Using**
Ask Cursor: *"Navigate to google.com and take a screenshot"*

**That's it!** No installation, no setup scripts, no global dependencies.

## 🎯 **Configuration Options**

### **Different Browsers**
```json
{
  "mcpServers": {
    "selenium-chrome": {
      "command": "npx",
      "args": ["selenium-mcp-server@latest", "--browser", "chrome", "--headless"]
    },
    "selenium-firefox": {
      "command": "npx",
      "args": ["selenium-mcp-server@latest", "--browser", "firefox", "--headless"]
    }
  }
}
```

### **Development vs Production**
```json
{
  "mcpServers": {
    "selenium-dev": {
      "command": "npx",
      "args": ["selenium-mcp-server@latest", "--browser", "chrome"]
    },
    "selenium-prod": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080"
      ]
    }
  }
}
```

### **Custom Output Directory**
```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome",
        "--headless",
        "--output-dir", "./automation-outputs"
      ]
    }
  }
}
```

## ✅ **Benefits of npx Approach**

1. **🚀 Zero Installation**: No `npm install` needed
2. **🔄 Always Latest**: Automatically uses latest version
3. **👥 Team Friendly**: Just commit `.cursor/mcp.json`
4. **🏗️ CI/CD Ready**: Works in any environment
5. **📦 No Dependencies**: No package.json changes needed
6. **🎯 Same as Playwright**: Familiar pattern for developers

## 🔧 **How It Works**

When Cursor starts the MCP server:

1. **npx checks** if `selenium-mcp-server@latest` is cached
2. **Downloads if needed** (first time only)
3. **Runs the server** with your specified arguments
4. **Caches for next time** (subsequent runs are fast)

## 📊 **Performance Notes**

- **First run**: ~30 seconds (downloads package)
- **Subsequent runs**: ~3 seconds (uses cache)
- **Cache location**: `~/.npm/_npx/`
- **Auto-updates**: npx checks for latest version periodically

## 🆚 **Comparison with Other Installation Methods**

| Method | Setup Time | Team Onboarding | Version Control | Maintenance |
|--------|------------|-----------------|-----------------|-------------|
| **npx (Recommended)** | 0 minutes | Just restart Cursor | Automatic | Zero |
| **Local Install** | 1 minute | `npm install` | package.json | Manual updates |
| **Global Install** | 2 minutes | Manual per person | None | Manual updates |

## 🎉 **Example Team Workflow**

1. **Developer adds configuration:**
   ```bash
   # Create .cursor/mcp.json with npx configuration
   git add .cursor/mcp.json
   git commit -m "Add Selenium MCP for browser automation"
   git push
   ```

2. **Team members pull and restart Cursor:**
   ```bash
   git pull
   # Restart Cursor
   # Ready to use immediately!
   ```

3. **Everyone can now ask Cursor:**
   - "Test our login flow"
   - "Take screenshots of our app"
   - "Extract data from competitor websites"

## 🔗 **Resources**

- **npm Package**: https://www.npmjs.com/package/selenium-mcp-server
- **GitHub**: https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server
- **Issues**: https://github.com/Raghvendra-Raghuvanshi/selenium-mcp-server/issues

**Your Selenium MCP Server now works exactly like Microsoft's Playwright MCP!**
