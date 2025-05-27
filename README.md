# Selenium MCP Server

A Model Context Protocol (MCP) server that provides browser automation capabilities using Selenium WebDriver. This server enables LLMs to interact with web pages through structured accessibility snapshots and browser automation tools.

## Features

- 🌐 **Multi-browser support**: Chrome, Firefox, Edge, Safari
- 🤖 **LLM-friendly**: Provides structured accessibility snapshots for LLMs
- 🔧 **Comprehensive tools**: Navigate, click, type, screenshot, and more
- 📱 **Tab management**: Open, close, and switch between browser tabs
- 🎯 **Element interaction**: Click, hover, drag, type into elements
- 📸 **Screenshots and PDFs**: Capture page content
- ⚡ **Fast and reliable**: Built on Selenium WebDriver

## Installation

### 🚀 **Zero-Installation Setup (Recommended - Just Like Playwright MCP)**

No installation needed! Just add configuration to Cursor:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome"
      ]
    }
  }
}
```

### Alternative Installation Methods

<details>
<summary>Click to see other installation options</summary>

#### Method 1: Local Installation
```bash
npm install selenium-mcp-server
```

#### Method 2: Global Installation
```bash
npm install -g selenium-mcp-server
```

</details>

## Quick Start

### 🎯 **Zero-Installation (Recommended)**

#### 1. Add Configuration
Create `.cursor/mcp.json` in your project root:
```json
{
  "mcpServers": {
    "selenium": {
      "command": "npx",
      "args": [
        "selenium-mcp-server@latest",
        "--browser", "chrome"
      ]
    }
  }
}
```

#### 2. Restart Cursor
- Quit Cursor completely
- Restart Cursor
- npx will automatically download and run the server

<details>
<summary>Alternative Setup Methods</summary>

### Option A: Local Installation

#### 1. Install in your project
```bash
npm install selenium-mcp-server
```

#### 2. Configure Cursor
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

### Option B: Global Installation

#### 1. Install globally
```bash
npm install -g selenium-mcp-server
```

#### 2. Configure Cursor
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

</details>

### 3. Use in Cursor

Ask Cursor to perform browser automation:

- "Navigate to google.com and take a screenshot"
- "Fill out the contact form on example.com"
- "Extract all links from the current page"

## Configuration Options

```bash
selenium-mcp [options]

Options:
  --browser <browser>        Browser to use (chrome, firefox, edge, safari) (default: "chrome")
  --headless                 Run browser in headless mode
  --executable-path <path>   Path to browser executable
  --user-data-dir <path>     Path to user data directory
  --isolated                 Keep browser profile in memory
  --viewport-size <size>     Browser viewport size (e.g., 1280,720)
  --output-dir <path>        Path to directory for output files
  -h, --help                 Display help for command
```

## Available Tools

### Navigation
- `browser_navigate` - Navigate to URLs
- `browser_navigate_back` - Go back in history
- `browser_navigate_forward` - Go forward in history

### Page Interaction
- `browser_click` - Click on elements
- `browser_type` - Type text into inputs
- `browser_hover` - Hover over elements
- `browser_drag` - Drag and drop elements
- `browser_select_option` - Select dropdown options
- `browser_press_key` - Press keyboard keys

### Information Gathering
- `browser_snapshot` - Capture accessibility tree
- `browser_take_screenshot` - Take screenshots
- `browser_network_requests` - List network requests
- `browser_console_messages` - Get console messages

### Tab Management
- `browser_tab_list` - List open tabs
- `browser_tab_new` - Open new tabs
- `browser_tab_select` - Switch tabs
- `browser_tab_close` - Close tabs

### Utilities
- `browser_wait_for` - Wait for elements/text
- `browser_file_upload` - Upload files
- `browser_handle_dialog` - Handle alerts/dialogs
- `browser_resize` - Resize browser window
- `browser_pdf_save` - Save page as PDF

## Usage Examples

### Basic Navigation and Screenshot

```javascript
// In Cursor, ask:
"Navigate to https://example.com and take a screenshot"
```

### Form Interaction

```javascript
// In Cursor, ask:
"Go to the contact page and fill out the form with name 'John Doe' and email 'john@example.com'"
```

### Data Extraction

```javascript
// In Cursor, ask:
"Visit the product page and extract all product names and prices"
```

## Requirements

- Node.js 18+
- Java 11+ (for Selenium WebDriver)
- Browser drivers (automatically managed by WebDriverManager)

## Configuration Examples

### Local Development

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--browser", "chrome"]
    }
  }
}
```

### Headless Production

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--headless",
        "--viewport-size", "1920,1080"
      ]
    }
  }
}
```

### Custom Browser Path

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": [
        "--browser", "chrome",
        "--executable-path", "/path/to/chrome"
      ]
    }
  }
}
```

## Troubleshooting

### Browser Not Found

If you get browser not found errors:

1. Install the browser (Chrome, Firefox, etc.)
2. Use `--executable-path` to specify browser location
3. Check that Java is installed and accessible

### Permission Issues

On macOS, you might need to grant accessibility permissions:

1. Go to System Preferences > Security & Privacy > Privacy
2. Select "Accessibility"
3. Add your terminal application

### Memory Issues

For large pages or long-running sessions:

```json
{
  "mcpServers": {
    "selenium": {
      "command": "selenium-mcp",
      "args": ["--isolated", "--headless"]
    }
  }
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License - see LICENSE file for details.

## Related Projects

- [Playwright MCP](https://github.com/microsoft/playwright-mcp) - Similar server using Playwright
- [Model Context Protocol](https://modelcontextprotocol.io/) - The protocol specification
- [Selenium WebDriver](https://selenium.dev/) - The underlying browser automation framework
