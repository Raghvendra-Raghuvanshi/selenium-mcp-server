#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema, Tool as McpTool } from "@modelcontextprotocol/sdk/types.js";
import { zodToJsonSchema } from "zod-to-json-schema";
import { BrowserAutomation, tools } from "./mcp-server.js";

class SeleniumMCPServer {
  private server: Server;
  private browserAutomation: BrowserAutomation;

  constructor(options: { headless?: boolean } = {}) {
    this.browserAutomation = new BrowserAutomation(options);

    this.server = new Server(
      {
        name: "selenium-mcp-server",
        version: "0.1.0",
      }
    );

    this.setupToolHandlers();
  }

  private setupToolHandlers() {
    // List available tools
    this.server.setRequestHandler(ListToolsRequestSchema, async () => {
      return {
        tools: tools.map(tool => ({
          name: tool.name,
          description: tool.description,
          inputSchema: zodToJsonSchema(tool.inputSchema),
        })) as McpTool[],
      };
    });

    // Handle tool calls
    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;

      try {
        switch (name) {
          case "browser_navigate":
            const navigateResult = await this.browserAutomation.navigate(args.url as string);
            return {
              content: [{ type: "text", text: navigateResult }],
            };

          case "browser_snapshot":
            const snapshotResult = await this.browserAutomation.getSnapshot();
            return {
              content: [{ type: "text", text: snapshotResult }],
            };

          case "browser_click":
            const clickResult = await this.browserAutomation.click(args.element as string, args.ref as string);
            return {
              content: [{ type: "text", text: clickResult }],
            };

          case "browser_type":
            const typeResult = await this.browserAutomation.type(
              args.element as string,
              args.ref as string,
              args.text as string,
              args.submit as boolean | undefined
            );
            return {
              content: [{ type: "text", text: typeResult }],
            };

          case "browser_wait_for":
            const waitResult = await this.browserAutomation.waitFor({
              time: args.time as number | undefined,
              text: args.text as string | undefined,
              textGone: args.textGone as string | undefined,
              selector: args.selector as string | undefined,
            });
            return {
              content: [{ type: "text", text: waitResult }],
            };

          case "browser_take_screenshot":
            const screenshotResult = await this.browserAutomation.takeScreenshot(args.filename as string | undefined);
            return {
              content: [{ type: "text", text: screenshotResult }],
            };

          case "browser_get_frames":
            const framesResult = await this.browserAutomation.getFrames();
            return {
              content: [{ type: "text", text: framesResult }],
            };

          case "browser_switch_frame":
            const switchResult = await this.browserAutomation.switchFrame({
              frameSelector: args.frameSelector as string | undefined,
              frameIndex: args.frameIndex as number | undefined,
              switchToMain: args.switchToMain as boolean | undefined,
            });
            return {
              content: [{ type: "text", text: switchResult }],
            };

          default:
            throw new Error(`Unknown tool: ${name}`);
        }
      } catch (error) {
        return {
          content: [{ type: "text", text: `Error: ${error instanceof Error ? error.message : String(error)}` }],
          isError: true,
        };
      }
    });
  }

  async run() {
    const transport = new StdioServerTransport();
    await this.server.connect(transport);

    // Wait for initialization
    await new Promise<void>((resolve) => {
      this.server.oninitialized = () => {
        console.error("Selenium MCP server running on stdio");
        resolve();
      };
    });
  }

  async close() {
    await this.browserAutomation.cleanup();
  }
}

async function main() {
  const server = new SeleniumMCPServer({ headless: false });

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
