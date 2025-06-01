# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2024-03-19

### Added
- Enhanced element detection with multiple fallback strategies
  - Support for finding elements by visible text, partial text, labels, placeholders, ARIA roles, and test IDs
  - Improved element reference handling with better selectors
  - Common selector fallbacks for buttons, links, and inputs
- Improved interaction tools
  - Enhanced click tool with support for double clicks, right clicks, and force clicks
  - Improved type tool with better element handling and force typing
  - Better error handling and recovery mechanisms
  - JavaScript fallbacks for difficult interactions
- Better logging and debugging support
  - Detailed logging of element finding strategies
  - Improved error messages and stack traces
  - Debug-level logging for element detection

### Changed
- Updated element finding logic to be more robust and reliable
- Improved error handling across all tools
- Enhanced documentation and code comments

## [0.1.0] - 2024-05-25

### Added
- Initial release of Selenium MCP Server
- Model Context Protocol (MCP) server implementation for Selenium WebDriver
- Support for multiple browsers (Chrome, Firefox, Edge, Safari)
- Comprehensive browser automation tools:
  - Navigation tools (navigate, back, forward)
  - Interaction tools (click, type, hover, drag, select)
  - Information gathering tools (snapshot, screenshot, network requests)
  - Tab management tools (list, new, select, close)
  - Utility tools (wait, file upload, dialog handling, PDF save)
- Command-line interface with configurable options
- Node.js wrapper around Java Selenium server
- TypeScript support with full type definitions
- Headless browser support
- Configurable viewport sizes
- Browser profile management (isolated and persistent modes)
- Automatic WebDriver management
- CI/CD pipeline with GitHub Actions
- Comprehensive documentation and examples

### Features
- **20+ Browser Automation Tools**: Complete set of tools for web automation
- **Multi-browser Support**: Works with Chrome, Firefox, Edge, and Safari
- **Headless Mode**: Perfect for CI/CD and server environments
- **Easy Installation**: One-command npm install
- **Cursor Integration**: Seamless integration with Cursor IDE
- **Professional Packaging**: Follows npm and open-source best practices
- **Cross-platform**: Works on Windows, macOS, and Linux
- **Enterprise Ready**: Robust error handling and logging

### Technical Details
- Built with TypeScript and compiled to ES2022 modules
- Java 11+ backend using Selenium WebDriver 4.16.1
- Node.js 18+ frontend wrapper
- Automatic browser driver management with WebDriverManager
- Support for both stdio and SSE transports
- Comprehensive error handling and logging
- Professional CI/CD pipeline with automated testing and publishing

### Documentation
- Complete README with installation and usage instructions
- API documentation for all tools
- Configuration examples for different use cases
- Troubleshooting guide
- Team setup guide for enterprise deployment
- Publishing guide for contributors
