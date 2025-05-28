#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema, Tool as McpTool } from "@modelcontextprotocol/sdk/types.js";
import { z } from "zod";
import { zodToJsonSchema } from "zod-to-json-schema";
import puppeteer, { Browser, Page } from "puppeteer";

// Tool schemas
const NavigateSchema = z.object({
  url: z.string().describe("The URL to navigate to")
});

const SnapshotSchema = z.object({});

const ClickSchema = z.object({
  element: z.string().describe("Human-readable element description"),
  ref: z.string().describe("Exact target element reference from the page snapshot")
});

const TypeSchema = z.object({
  element: z.string().describe("Human-readable element description"),
  ref: z.string().describe("Exact target element reference from the page snapshot"),
  text: z.string().describe("Text to type into the element"),
  submit: z.boolean().optional().describe("Whether to submit entered text (press Enter after)")
});

const WaitSchema = z.object({
  time: z.number().optional().describe("The time to wait in seconds"),
  text: z.string().optional().describe("The text to wait for"),
  textGone: z.string().optional().describe("The text to wait for to disappear")
});

const ScreenshotSchema = z.object({
  filename: z.string().optional().describe("File name to save the screenshot to")
});

// Tool definitions
const tools = [
  {
    name: "browser_navigate",
    description: "Navigate to a URL",
    inputSchema: NavigateSchema,
    title: "Navigate to a URL"
  },
  {
    name: "browser_snapshot",
    description: "Capture accessibility snapshot of the current page",
    inputSchema: SnapshotSchema,
    title: "Page snapshot"
  },
  {
    name: "browser_click",
    description: "Perform click on a web page",
    inputSchema: ClickSchema,
    title: "Click"
  },
  {
    name: "browser_type",
    description: "Type text into editable element",
    inputSchema: TypeSchema,
    title: "Type text"
  },
  {
    name: "browser_wait_for",
    description: "Wait for text to appear or disappear or a specified time to pass",
    inputSchema: WaitSchema,
    title: "Wait for"
  },
  {
    name: "browser_take_screenshot",
    description: "Take a screenshot of the current page",
    inputSchema: ScreenshotSchema,
    title: "Take a screenshot"
  }
];

class BrowserAutomation {
  private browser: Browser | null = null;
  private page: Page | null = null;
  private headless: boolean;

  constructor(options: { headless?: boolean } = {}) {
    this.headless = options.headless || false;
  }

  private async ensureBrowser(): Promise<void> {
    if (!this.browser || !this.browser.isConnected()) {
      this.browser = await puppeteer.launch({
        headless: this.headless,
        defaultViewport: null,
        args: [
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--disable-dev-shm-usage',
          '--disable-accelerated-2d-canvas',
          '--no-first-run',
          '--no-zygote',
          '--disable-gpu',
          '--disable-web-security',
          '--disable-features=VizDisplayCompositor'
        ]
      });
    }

    if (!this.page || this.page.isClosed()) {
      this.page = await this.browser.newPage();
    }
  }

  async navigate(url: string): Promise<string> {
    await this.ensureBrowser();
    await this.page!.goto(url, { waitUntil: 'networkidle2', timeout: 30000 });
    return `Successfully navigated to ${this.page!.url()}`;
  }

  async getSnapshot(): Promise<string> {
    await this.ensureBrowser();
    
    const title = await this.page!.title();
    const url = this.page!.url();
    
    const elements = await this.page!.evaluate(() => {
      const doc = document;
      const interactiveElements = doc.querySelectorAll(
        'a, button, input, select, textarea, [onclick], [role="button"], [tabindex]'
      );
      
      return Array.from(interactiveElements).slice(0, 20).map((el: any, index: number) => {
        const tagName = el.tagName.toLowerCase();
        const text = el.textContent?.trim().slice(0, 50) || '';
        const type = el.getAttribute('type') || '';
        const placeholder = el.getAttribute('placeholder') || '';
        const href = el.getAttribute('href') || '';
        const name = el.getAttribute('name') || '';
        
        return {
          ref: `element-${index}`,
          tag: tagName,
          text: text,
          type: type,
          placeholder: placeholder,
          href: href,
          name: name
        };
      });
    });
    
    const filteredElements = elements.filter(el => el.text || el.placeholder || el.href || el.name);
    
    return `Page snapshot:\nURL: ${url}\nTitle: ${title}\n\nInteractive elements:\n${
      filteredElements.map(el =>
        `- ${el.ref}: ${el.tag}${el.type ? `[${el.type}]` : ''} "${el.text || el.placeholder || el.name || el.href}"`
      ).join('\n')
    }`;
  }

  async click(element: string, ref: string): Promise<string> {
    await this.ensureBrowser();
    
    const elementIndex = parseInt(ref.replace('element-', ''));
    
    await this.page!.evaluate((index) => {
      const interactiveElements = document.querySelectorAll(
        'a, button, input, select, textarea, [onclick], [role="button"], [tabindex]'
      );
      const targetElement = interactiveElements[index] as HTMLElement;
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        targetElement.click();
      }
    }, elementIndex);
    
    return `Successfully clicked on element: ${element} (${ref})`;
  }

  async type(element: string, ref: string, text: string, submit?: boolean): Promise<string> {
    await this.ensureBrowser();
    
    const elementIndex = parseInt(ref.replace('element-', ''));
    
    await this.page!.evaluate((index, inputText) => {
      const interactiveElements = document.querySelectorAll(
        'a, button, input, select, textarea, [onclick], [role="button"], [tabindex]'
      );
      const targetElement = interactiveElements[index] as HTMLInputElement | HTMLTextAreaElement;
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        targetElement.focus();
        if (targetElement.tagName === 'INPUT' || targetElement.tagName === 'TEXTAREA') {
          targetElement.value = inputText;
          targetElement.dispatchEvent(new Event('input', { bubbles: true }));
        }
      }
    }, elementIndex, text);
    
    if (submit) {
      await this.page!.keyboard.press('Enter');
      return `Successfully typed "${text}" and submitted in element: ${element} (${ref})`;
    }
    
    return `Successfully typed "${text}" in element: ${element} (${ref})`;
  }

  async waitFor(options: { time?: number; text?: string; textGone?: string }): Promise<string> {
    await this.ensureBrowser();
    
    if (options.time) {
      await new Promise(resolve => setTimeout(resolve, options.time! * 1000));
      return `Waited for ${options.time} seconds`;
    }
    
    if (options.text) {
      await this.page!.waitForFunction(
        (text) => document.body.textContent?.includes(text),
        { timeout: 30000 },
        options.text
      );
      return `Waited for text "${options.text}" to appear`;
    }
    
    if (options.textGone) {
      await this.page!.waitForFunction(
        (text) => !document.body.textContent?.includes(text),
        { timeout: 30000 },
        options.textGone
      );
      return `Waited for text "${options.textGone}" to disappear`;
    }
    
    return "No wait condition specified";
  }

  async takeScreenshot(filename?: string): Promise<string> {
    await this.ensureBrowser();
    
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const screenshotPath = filename || `screenshot-${timestamp}.png`;
    
    await this.page!.screenshot({ path: screenshotPath, fullPage: true });
    return `Screenshot saved to ${screenshotPath}`;
  }

  async cleanup(): Promise<void> {
    if (this.page && !this.page.isClosed()) {
      await this.page.close();
    }
    if (this.browser && this.browser.isConnected()) {
      await this.browser.close();
    }
  }
}

export { BrowserAutomation, tools };
