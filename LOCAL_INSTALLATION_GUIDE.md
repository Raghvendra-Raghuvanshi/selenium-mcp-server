# Local Installation Guide - Like Microsoft's Playwright MCP

This guide shows how to use Selenium MCP Server exactly like Microsoft's Playwright MCP - with local project installation instead of global installation.

## 🎯 **Method 1: Local Project Installation (Recommended)**

This is the same pattern as Microsoft's Playwright MCP.

### Step 1: Add to Your Project

```bash
# In your project directory
npm install selenium-mcp-server
```

### Step 2: Configure Cursor

Create `.cursor/mcp.json` in your project root:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "node",
      "args": ["./node_modules/selenium-mcp-server/dist/index.js"]
    }
  }
}
```

### Step 3: Alternative Configuration (using npx)

```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": ["selenium-mcp-server"]
    }
  }
}
```

## 🔄 **Comparison with Microsoft's Playwright MCP**

### Microsoft's Playwright MCP Setup:
```bash
npm install @microsoft/playwright-mcp
```

```json
{
  "mcpServers": {
    "playwright": {
      "command": "node",
      "args": ["./node_modules/@microsoft/playwright-mcp/dist/index.js"]
    }
  }
}
```

### Your Selenium MCP Setup:
```bash
npm install selenium-mcp-server
```

```json
{
  "mcpServers": {
    "selenium": {
      "command": "node",
      "args": ["./node_modules/selenium-mcp-server/dist/index.js"]
    }
  }
}
```

**Exactly the same pattern!**

## 📦 **Installation Options Comparison**

| Method | Microsoft Playwright MCP | Your Selenium MCP |
|--------|--------------------------|-------------------|
| **Local Install** | `npm install @microsoft/playwright-mcp` | `npm install selenium-mcp-server` |
| **Global Install** | Not recommended | `npm install -g selenium-mcp-server` |
| **Configuration** | Project-specific `.cursor/mcp.json` | Same pattern |
| **Usage** | Works immediately after install | Works immediately after install |

## 🚀 **Updated Team Instructions**

### For Teams Using Local Installation (Like Playwright):

#### Step 1: Add to package.json
```bash
npm install selenium-mcp-server
```

#### Step 2: Configure Cursor
Create `.cursor/mcp.json` in project root:
```json
{
  "mcpServers": {
    "selenium": {
      "command": "node",
      "args": ["./node_modules/selenium-mcp-server/dist/index.js", "--browser", "chrome", "--headless"]
    }
  }
}
```

#### Step 3: Commit Configuration
```bash
git add .cursor/mcp.json package.json package-lock.json
git commit -m "Add Selenium MCP server for browser automation"
```

Now **every team member** who clones the project gets the MCP server automatically with `npm install`!

## 🎯 **Benefits of Local Installation**

1. **Version Control**: Exact version locked in package.json
2. **Team Consistency**: Everyone uses the same version
3. **No Global Dependencies**: Cleaner development environment
4. **Project Isolation**: Different projects can use different versions
5. **CI/CD Friendly**: Works in automated environments
6. **Same as Playwright**: Familiar pattern for developers

## 📋 **Updated README Example**

For your project README, you can now show:

```markdown
## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Restart Cursor

3. Start automating:
   Ask Cursor: "Navigate to google.com and take a screenshot"
```

## 🔧 **Advanced Local Configuration**

### Development vs Production
```json
{
  "mcpServers": {
    "selenium-dev": {
      "command": "node",
      "args": ["./node_modules/selenium-mcp-server/dist/index.js", "--browser", "chrome"]
    },
    "selenium-prod": {
      "command": "node",
      "args": ["./node_modules/selenium-mcp-server/dist/index.js", "--browser", "chrome", "--headless"]
    }
  }
}
```

### With Custom Options
```json
{
  "mcpServers": {
    "selenium": {
      "command": "node",
      "args": [
        "./node_modules/selenium-mcp-server/dist/index.js",
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080",
        "--output-dir", "./test-outputs"
      ]
    }
  }
}
```

## 🎉 **Result**

Now your Selenium MCP server works **exactly like Microsoft's Playwright MCP**:
- Local project installation
- No global dependencies needed
- Version controlled
- Team-friendly
- CI/CD ready

Teams can use it the same way they use Playwright MCP!
