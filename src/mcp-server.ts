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
  textGone: z.string().optional().describe("The text to wait for to disappear"),
  selector: z.string().optional().describe("CSS selector to wait for element to appear")
});

const ScreenshotSchema = z.object({
  filename: z.string().optional().describe("File name to save the screenshot to")
});

const SwitchFrameSchema = z.object({
  frameSelector: z.string().optional().describe("CSS selector for the iframe to switch to (e.g., '#oauth-iframe')"),
  frameIndex: z.number().optional().describe("Index of the frame to switch to (0-based)"),
  switchToMain: z.boolean().optional().describe("Switch back to main page content")
});

const GetFramesSchema = z.object({});

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
  },
  {
    name: "browser_switch_frame",
    description: "Switch to an iframe or back to main content",
    inputSchema: SwitchFrameSchema,
    title: "Switch frame"
  },
  {
    name: "browser_get_frames",
    description: "List all available frames/iframes on the page",
    inputSchema: GetFramesSchema,
    title: "Get frames"
  }
];

class BrowserAutomation {
  private browser: Browser | null = null;
  private page: Page | null = null;
  private headless: boolean;
  private currentFrame: any = null; // Track current frame context

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

    // Determine which context to use (main page or current frame)
    const context = this.currentFrame || this.page!;
    const contextName = this.currentFrame ? 'frame' : 'main page';

    // Wait for page to be fully loaded and any dynamic content
    try {
      if (!this.currentFrame) {
        await this.page!.waitForFunction(() => document.readyState === 'complete', { timeout: 10000 });
      }
      await new Promise(resolve => setTimeout(resolve, 1000)); // Wait for any remaining async operations
    } catch (e) {
      console.warn('Page load timeout, proceeding with snapshot');
    }

    // Additional wait for dynamic content
    await new Promise(resolve => setTimeout(resolve, 2000));

    const title = await this.page!.title();
    const url = this.page!.url();

    const elements = await context.evaluate(() => {
      const doc = document;

      // Expanded selector to catch more interactive elements including dynamic ones
      const interactiveElements = doc.querySelectorAll(
        'a, button, input, select, textarea, [onclick], [role="button"], [tabindex], ' +
        '[contenteditable], [data-testid], [data-cy], [data-test], ' +
        '.btn, .button, .link, .clickable, .interactive, ' +
        'form, [type="submit"], [type="button"], [type="text"], [type="email"], [type="password"], ' +
        '[role="textbox"], [role="link"], [role="menuitem"], [role="tab"], ' +
        'div[onclick], span[onclick], li[onclick], td[onclick]'
      );

      return Array.from(interactiveElements).slice(0, 30).map((el: any, index: number) => {
        const tagName = el.tagName.toLowerCase();
        const text = el.textContent?.trim().slice(0, 100) || '';
        const type = el.getAttribute('type') || '';
        const placeholder = el.getAttribute('placeholder') || '';
        const href = el.getAttribute('href') || '';
        const name = el.getAttribute('name') || '';
        const id = el.getAttribute('id') || '';
        const className = el.getAttribute('class') || '';
        const role = el.getAttribute('role') || '';
        const testId = el.getAttribute('data-testid') || el.getAttribute('data-cy') || el.getAttribute('data-test') || '';
        const ariaLabel = el.getAttribute('aria-label') || '';
        const value = el.value || '';

        // Check if element is visible
        const rect = el.getBoundingClientRect();
        const isVisible = rect.width > 0 && rect.height > 0 &&
                         window.getComputedStyle(el).visibility !== 'hidden' &&
                         window.getComputedStyle(el).display !== 'none';

        return {
          ref: `element-${index}`,
          tag: tagName,
          text: text,
          type: type,
          placeholder: placeholder,
          href: href,
          name: name,
          id: id,
          className: className.split(' ').slice(0, 3).join(' '), // First 3 classes only
          role: role,
          testId: testId,
          ariaLabel: ariaLabel,
          value: value,
          isVisible: isVisible
        };
      });
    });

    // More inclusive filtering - show elements that have any identifying information
    const filteredElements = elements.filter(el =>
      el.isVisible && (
        el.text || el.placeholder || el.href || el.name ||
        el.id || el.testId || el.ariaLabel || el.value ||
        el.type || el.role || el.className
      )
    );

    // If still no elements, show all visible elements for debugging
    const elementsToShow = filteredElements.length > 0 ? filteredElements :
      elements.filter(el => el.isVisible).slice(0, 10);

    return `${contextName.charAt(0).toUpperCase() + contextName.slice(1)} snapshot:\nURL: ${url}\nTitle: ${title}\n\nInteractive elements (${elementsToShow.length} found):\n${
      elementsToShow.map(el => {
        const identifiers = [
          el.text && `text:"${el.text}"`,
          el.placeholder && `placeholder:"${el.placeholder}"`,
          el.name && `name:"${el.name}"`,
          el.id && `id:"${el.id}"`,
          el.testId && `testId:"${el.testId}"`,
          el.ariaLabel && `aria:"${el.ariaLabel}"`,
          el.value && `value:"${el.value}"`,
          el.href && `href:"${el.href}"`,
          el.className && `class:"${el.className}"`,
          el.role && `role:"${el.role}"`
        ].filter(Boolean).join(', ');

        return `- ${el.ref}: ${el.tag}${el.type ? `[${el.type}]` : ''} (${identifiers || 'no identifiers'})`;
      }).join('\n')
    }${elementsToShow.length === 0 ? `\n\nNo interactive elements detected in ${contextName}. Content may still be loading or use non-standard interactive patterns.` : ''}`;
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

  async waitFor(options: { time?: number; text?: string; textGone?: string; selector?: string }): Promise<string> {
    await this.ensureBrowser();

    if (options.time) {
      await new Promise(resolve => setTimeout(resolve, options.time! * 1000));
      return `Waited for ${options.time} seconds`;
    }

    if (options.selector) {
      try {
        await this.page!.waitForSelector(options.selector, { timeout: 30000, visible: true });
        return `Successfully waited for element with selector "${options.selector}" to appear`;
      } catch (e) {
        return `Timeout waiting for element with selector "${options.selector}" to appear`;
      }
    }

    if (options.text) {
      try {
        await this.page!.waitForFunction(
          (text) => document.body.textContent?.includes(text),
          { timeout: 30000 },
          options.text
        );
        return `Successfully waited for text "${options.text}" to appear`;
      } catch (e) {
        return `Timeout waiting for text "${options.text}" to appear`;
      }
    }

    if (options.textGone) {
      try {
        await this.page!.waitForFunction(
          (text) => !document.body.textContent?.includes(text),
          { timeout: 30000 },
          options.textGone
        );
        return `Successfully waited for text "${options.textGone}" to disappear`;
      } catch (e) {
        return `Timeout waiting for text "${options.textGone}" to disappear`;
      }
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

  async getFrames(): Promise<string> {
    await this.ensureBrowser();

    const frameInfo = await this.page!.evaluate(() => {
      const frames = [];

      // Get all iframes
      const iframes = document.querySelectorAll('iframe');
      Array.from(iframes).forEach((iframe, index) => {
        frames.push({
          index,
          type: 'iframe',
          src: iframe.src || '',
          id: iframe.id || '',
          name: iframe.name || '',
          className: iframe.className || '',
          title: iframe.title || '',
          width: iframe.width || '',
          height: iframe.height || ''
        });
      });

      // Get all frames
      const frameElements = document.querySelectorAll('frame');
      Array.from(frameElements).forEach((frame, index) => {
        frames.push({
          index: index + iframes.length,
          type: 'frame',
          src: (frame as any).src || '',
          id: (frame as any).id || '',
          name: (frame as any).name || '',
          className: (frame as any).className || '',
          title: (frame as any).title || ''
        });
      });

      return frames;
    });

    if (frameInfo.length === 0) {
      return "No frames or iframes found on the page";
    }

    return `Available frames (${frameInfo.length} found):\n${
      frameInfo.map(frame =>
        `- Frame ${frame.index} (${frame.type}): ${frame.id ? `id="${frame.id}"` : ''} ${frame.name ? `name="${frame.name}"` : ''} ${frame.src ? `src="${frame.src}"` : ''} ${frame.className ? `class="${frame.className}"` : ''}`
      ).join('\n')
    }`;
  }

  async switchFrame(options: { frameSelector?: string; frameIndex?: number; switchToMain?: boolean }): Promise<string> {
    await this.ensureBrowser();

    try {
      if (options.switchToMain) {
        // Switch back to main content
        this.currentFrame = null;
        return "Switched back to main page content";
      }

      if (options.frameSelector) {
        // Switch to frame by CSS selector
        const frameHandle = await this.page!.$(options.frameSelector);
        if (!frameHandle) {
          return `Frame with selector "${options.frameSelector}" not found`;
        }

        const frame = await frameHandle.contentFrame();
        if (!frame) {
          return `Could not access content of frame with selector "${options.frameSelector}" (may be cross-origin)`;
        }

        this.currentFrame = frame;
        return `Successfully switched to frame with selector "${options.frameSelector}"`;
      }

      if (options.frameIndex !== undefined) {
        // Switch to frame by index
        const frames = this.page!.frames();
        if (options.frameIndex >= frames.length || options.frameIndex < 0) {
          return `Frame index ${options.frameIndex} is out of range. Available frames: 0-${frames.length - 1}`;
        }

        this.currentFrame = frames[options.frameIndex];
        return `Successfully switched to frame at index ${options.frameIndex}`;
      }

      return "No frame selector or index specified";
    } catch (error) {
      return `Error switching frame: ${error instanceof Error ? error.message : String(error)}`;
    }
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
