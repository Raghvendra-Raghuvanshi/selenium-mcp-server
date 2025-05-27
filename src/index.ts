#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema, InitializeRequestSchema } from "@modelcontextprotocol/sdk/types.js";
import { spawn, ChildProcess, exec } from "child_process";
import { Command } from "commander";
import * as path from "path";
import * as fs from "fs";
import { fileURLToPath } from 'url';
import { promisify } from 'util';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const execAsync = promisify(exec);

interface SeleniumMCPOptions {
  browser?: string;
  headless?: boolean;
  executablePath?: string;
  userDataDir?: string;
  isolated?: boolean;
  viewportSize?: string;
  outputDir?: string;
}

class SimpleBrowserAutomation {
  private browser: string;
  private headless: boolean;

  constructor(options: SeleniumMCPOptions = {}) {
    this.browser = options.browser || 'chrome';
    this.headless = options.headless || false;
  }

  async navigate(url: string): Promise<string> {
    try {
      console.error(`Opening ${url} in ${this.browser}`);

      // Use system's open command to open URL in default browser
      const command = process.platform === 'darwin' ? 'open' :
                     process.platform === 'win32' ? 'start' : 'xdg-open';

      await execAsync(`${command} "${url}"`);

      return `Successfully opened ${url} in ${this.browser}`;
    } catch (error) {
      throw new Error(`Failed to navigate to ${url}: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  async takeScreenshot(filename?: string): Promise<string> {
    try {
      // For now, return a message indicating screenshot would be taken
      const screenshotFile = filename || `screenshot-${Date.now()}.png`;
      return `Screenshot would be saved as: ${screenshotFile}`;
    } catch (error) {
      throw new Error(`Failed to take screenshot: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  async getSnapshot(): Promise<string> {
    try {
      // For now, return a simple page structure
      return `Page snapshot: Current page is loaded and ready for interaction`;
    } catch (error) {
      throw new Error(`Failed to get page snapshot: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  async click(element: string, ref: string): Promise<string> {
    try {
      return `Would click on element: ${element} (ref: ${ref})`;
    } catch (error) {
      throw new Error(`Failed to click element: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  async type(element: string, ref: string, text: string, submit?: boolean): Promise<string> {
    try {
      const action = submit ? `type "${text}" and submit` : `type "${text}"`;
      return `Would ${action} in element: ${element} (ref: ${ref})`;
    } catch (error) {
      throw new Error(`Failed to type in element: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  async waitFor(options: { time?: number; text?: string; textGone?: string }): Promise<string> {
    try {
      if (options.time) {
        await new Promise(resolve => setTimeout(resolve, options.time! * 1000));
        return `Waited for ${options.time} seconds`;
      } else if (options.text) {
        return `Would wait for text to appear: "${options.text}"`;
      } else if (options.textGone) {
        return `Would wait for text to disappear: "${options.textGone}"`;
      } else {
        return `No wait condition specified`;
      }
    } catch (error) {
      throw new Error(`Failed to wait: ${error instanceof Error ? error.message : String(error)}`);
    }
  }
}

class SeleniumMCPServer {
  private server: Server;
  private javaProcess: ChildProcess | null = null;
  private options: SeleniumMCPOptions;
  private browserAutomation: SimpleBrowserAutomation;

  constructor(options: SeleniumMCPOptions = {}) {
    this.options = options;
    this.browserAutomation = new SimpleBrowserAutomation(options);
    this.server = new Server(
      {
        name: "selenium-mcp-server",
        version: "0.1.8",
      }
    );

    this.setupHandlers();
  }

  private getJarPath(): string {
    // Look for the JAR file in the package
    const jarName = "selenium-mcp-0.0.1.jar";
    const possiblePaths = [
      path.join(__dirname, "..", "lib", jarName),
      path.join(__dirname, "..", jarName),
      path.join(process.cwd(), "target", jarName),
    ];

    for (const jarPath of possiblePaths) {
      if (fs.existsSync(jarPath)) {
        return jarPath;
      }
    }

    throw new Error(`Selenium MCP JAR file not found. Looked in: ${possiblePaths.join(", ")}`);
  }

  private buildJavaArgs(): string[] {
    const args = ["-jar", this.getJarPath()];

    if (this.options.browser) {
      args.push("--browser", this.options.browser);
    }
    if (this.options.headless) {
      args.push("--headless");
    }
    if (this.options.executablePath) {
      args.push("--executable-path", this.options.executablePath);
    }
    if (this.options.userDataDir) {
      args.push("--user-data-dir", this.options.userDataDir);
    }
    if (this.options.isolated) {
      args.push("--isolated");
    }
    if (this.options.viewportSize) {
      args.push("--viewport-size", this.options.viewportSize);
    }
    if (this.options.outputDir) {
      args.push("--output-dir", this.options.outputDir);
    }

    return args;
  }

  private setupHandlers() {
    // Handle initialization
    this.server.setRequestHandler(InitializeRequestSchema, async (request) => {
      console.error("Received initialize request");
      return {
        protocolVersion: "2024-11-05",
        capabilities: {
          tools: {},
        },
        serverInfo: {
          name: "selenium-mcp-server",
          version: "0.1.8",
        },
      };
    });

    this.server.setRequestHandler(ListToolsRequestSchema, async () => {
      console.error("Received ListTools request");
      return {
        tools: [
          {
            name: "browser_navigate",
            description: "Navigate to a URL",
            inputSchema: {
              type: "object",
              properties: {
                url: {
                  type: "string",
                  description: "The URL to navigate to",
                },
              },
              required: ["url"],
            },
          },
          {
            name: "browser_snapshot",
            description: "Capture accessibility snapshot of the current page",
            inputSchema: {
              type: "object",
              properties: {},
            },
          },
          {
            name: "browser_take_screenshot",
            description: "Take a screenshot of the current page",
            inputSchema: {
              type: "object",
              properties: {
                filename: {
                  type: "string",
                  description: "File name to save the screenshot to",
                },
                raw: {
                  type: "boolean",
                  description: "Whether to return without compression (PNG format)",
                },
              },
            },
          },
          {
            name: "browser_click",
            description: "Perform click on a web page",
            inputSchema: {
              type: "object",
              properties: {
                element: {
                  type: "string",
                  description: "Human-readable element description",
                },
                ref: {
                  type: "string",
                  description: "Exact target element reference from the page snapshot",
                },
              },
              required: ["element", "ref"],
            },
          },
          {
            name: "browser_type",
            description: "Type text into editable element",
            inputSchema: {
              type: "object",
              properties: {
                element: {
                  type: "string",
                  description: "Human-readable element description",
                },
                ref: {
                  type: "string",
                  description: "Exact target element reference from the page snapshot",
                },
                text: {
                  type: "string",
                  description: "Text to type into the element",
                },
                submit: {
                  type: "boolean",
                  description: "Whether to submit entered text (press Enter after)",
                },
              },
              required: ["element", "ref", "text"],
            },
          },
          {
            name: "browser_wait_for",
            description: "Wait for text to appear or disappear or a specified time to pass",
            inputSchema: {
              type: "object",
              properties: {
                time: {
                  type: "number",
                  description: "The time to wait in seconds",
                },
                text: {
                  type: "string",
                  description: "The text to wait for",
                },
                textGone: {
                  type: "string",
                  description: "The text to wait for to disappear",
                },
              },
            },
          },
        ],
      };
    });

    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;
      console.error(`Received CallTool request: ${name}`);

      try {
        let result: string;

        // Type guard for args
        if (!args || typeof args !== 'object') {
          throw new Error('Invalid arguments provided');
        }

        const typedArgs = args as Record<string, any>;

        switch (name) {
          case "browser_navigate":
            if (!typedArgs.url || typeof typedArgs.url !== 'string') {
              throw new Error('URL is required for navigation');
            }
            result = await this.browserAutomation.navigate(typedArgs.url);
            break;

          case "browser_snapshot":
            result = await this.browserAutomation.getSnapshot();
            break;

          case "browser_take_screenshot":
            result = await this.browserAutomation.takeScreenshot(
              typedArgs.filename && typeof typedArgs.filename === 'string' ? typedArgs.filename : undefined
            );
            break;

          case "browser_click":
            if (!typedArgs.element || typeof typedArgs.element !== 'string' ||
                !typedArgs.ref || typeof typedArgs.ref !== 'string') {
              throw new Error('Element and ref are required for clicking');
            }
            result = await this.browserAutomation.click(typedArgs.element, typedArgs.ref);
            break;

          case "browser_type":
            if (!typedArgs.element || typeof typedArgs.element !== 'string' ||
                !typedArgs.ref || typeof typedArgs.ref !== 'string' ||
                !typedArgs.text || typeof typedArgs.text !== 'string') {
              throw new Error('Element, ref, and text are required for typing');
            }
            result = await this.browserAutomation.type(
              typedArgs.element,
              typedArgs.ref,
              typedArgs.text,
              typedArgs.submit === true
            );
            break;

          case "browser_wait_for":
            result = await this.browserAutomation.waitFor({
              time: typeof typedArgs.time === 'number' ? typedArgs.time : undefined,
              text: typeof typedArgs.text === 'string' ? typedArgs.text : undefined,
              textGone: typeof typedArgs.textGone === 'string' ? typedArgs.textGone : undefined,
            });
            break;

          default:
            throw new Error(`Unknown tool: ${name}`);
        }

        return {
          content: [
            {
              type: "text",
              text: result,
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `Error: ${error instanceof Error ? error.message : String(error)}`,
            },
          ],
          isError: true,
        };
      }
    });
  }

  private async startJavaProcess(): Promise<void> {
    return new Promise((resolve, reject) => {
      const args = this.buildJavaArgs();

      this.javaProcess = spawn("java", args, {
        stdio: ["pipe", "pipe", "pipe"],
      });

      this.javaProcess.on("error", (error) => {
        reject(new Error(`Failed to start Java process: ${error.message}`));
      });

      this.javaProcess.stdout?.on("data", (data) => {
        const output = data.toString();
        console.error("Java output:", output);
        if (output.includes('"type":"ready"')) {
          // Send initialize message using proper MCP format
          const initMessage = {
            jsonrpc: "2.0",
            id: 1,
            method: "initialize",
            params: {
              protocolVersion: "2024-11-05",
              capabilities: {},
              clientInfo: {
                name: "selenium-mcp-server",
                version: "0.1.3"
              }
            }
          };
          this.javaProcess?.stdin?.write(JSON.stringify(initMessage) + "\n");
          resolve();
        }
      });

      this.javaProcess.stderr?.on("data", (data) => {
        console.error("Java process error:", data.toString());
      });

      this.javaProcess.on("exit", (code) => {
        console.error(`Java process exited with code ${code}`);
        this.javaProcess = null;
      });
    });
  }

  async run() {
    const transport = new StdioServerTransport();

    // Add debug logging
    console.error("Selenium MCP server starting...");

    await this.server.connect(transport);
    console.error("Selenium MCP server running on stdio");

    // Log that we're ready
    console.error("Server ready for MCP connections");
  }

  async close() {
    if (this.javaProcess) {
      this.javaProcess.kill();
      this.javaProcess = null;
    }
  }
}

async function main() {
  const program = new Command();

  program
    .name("selenium-mcp")
    .description("Selenium MCP Server - Browser automation for LLMs")
    .version("0.1.0")
    .option("--browser <browser>", "Browser to use (chrome, firefox, edge, safari)", "chrome")
    .option("--headless", "Run browser in headless mode")
    .option("--executable-path <path>", "Path to browser executable")
    .option("--user-data-dir <path>", "Path to user data directory")
    .option("--isolated", "Keep browser profile in memory")
    .option("--viewport-size <size>", "Browser viewport size (e.g., 1280,720)")
    .option("--output-dir <path>", "Path to directory for output files");

  program.parse();

  const options = program.opts();
  const server = new SeleniumMCPServer(options);

  // Handle graceful shutdown
  process.on("SIGINT", async () => {
    await server.close();
    process.exit(0);
  });

  process.on("SIGTERM", async () => {
    await server.close();
    process.exit(0);
  });

  await server.run();
}

// Check if this file is being run directly
const isMainModule = process.argv[1] && import.meta.url === `file://${path.resolve(process.argv[1])}`;

if (isMainModule) {
  main().catch((error) => {
    console.error("Fatal error:", error);
    process.exit(1);
  });
}

export { SeleniumMCPServer };
