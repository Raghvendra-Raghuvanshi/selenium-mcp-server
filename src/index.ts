#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema } from "@modelcontextprotocol/sdk/types.js";
import { spawn, ChildProcess } from "child_process";
import { Command } from "commander";
import * as path from "path";
import * as fs from "fs";

interface SeleniumMCPOptions {
  browser?: string;
  headless?: boolean;
  executablePath?: string;
  userDataDir?: string;
  isolated?: boolean;
  viewportSize?: string;
  outputDir?: string;
}

class SeleniumMCPServer {
  private server: Server;
  private javaProcess: ChildProcess | null = null;
  private options: SeleniumMCPOptions;

  constructor(options: SeleniumMCPOptions = {}) {
    this.options = options;
    this.server = new Server(
      {
        name: "selenium-mcp-server",
        version: "0.1.0",
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
    this.server.setRequestHandler(ListToolsRequestSchema, async () => {
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

      try {
        // Start Java process if not already running
        if (!this.javaProcess) {
          await this.startJavaProcess();
        }

        // Send tool call to Java process
        const toolCall = {
          type: "toolCall",
          id: Date.now().toString(),
          name,
          params: args,
        };

        return new Promise((resolve, reject) => {
          if (!this.javaProcess) {
            reject(new Error("Java process not available"));
            return;
          }

          const timeout = setTimeout(() => {
            reject(new Error("Tool call timeout"));
          }, 30000);

          const handleData = (data: Buffer) => {
            const response = data.toString().trim();
            try {
              const parsed = JSON.parse(response);
              if (parsed.type === "toolCallResult" && parsed.id === toolCall.id) {
                clearTimeout(timeout);
                this.javaProcess?.stdout?.off("data", handleData);
                resolve({
                  content: [
                    {
                      type: "text",
                      text: JSON.stringify(parsed.result, null, 2),
                    },
                  ],
                });
              }
            } catch (e) {
              // Ignore parsing errors, might be partial data
            }
          };

          this.javaProcess.stdout?.on("data", handleData);
          this.javaProcess.stdin?.write(JSON.stringify(toolCall) + "\n");
        });
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
        if (output.includes('"type":"ready"')) {
          // Send initialize message
          const initMessage = {
            type: "initialize",
            id: "init-1",
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
    await this.server.connect(transport);
    console.error("Selenium MCP server running on stdio");
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

if (import.meta.url === `file://${process.argv[1]}`) {
  main().catch((error) => {
    console.error("Fatal error:", error);
    process.exit(1);
  });
}

export { SeleniumMCPServer };
