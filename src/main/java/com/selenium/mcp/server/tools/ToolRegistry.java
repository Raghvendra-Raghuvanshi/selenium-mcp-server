package com.selenium.mcp.server.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.browser.*;
import com.selenium.mcp.server.tools.interaction.*;
import com.selenium.mcp.server.tools.navigation.*;
import com.selenium.mcp.server.tools.tabs.*;
import com.selenium.mcp.server.tools.utility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all MCP tools.
 */
public class ToolRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ToolRegistry.class);
    private final Map<String, Tool> tools = new HashMap<>();

    /**
     * Register a tool.
     */
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        logger.debug("Registered tool: {}", tool.getName());
    }

    /**
     * Get a tool by name.
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * Execute a tool.
     */
    public JsonNode executeTool(String name, JsonNode params, BrowserManager browserManager) throws Exception {
        Tool tool = getTool(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        
        return tool.execute(params, browserManager);
    }

    /**
     * Get all tools as JSON.
     */
    public ArrayNode getToolsAsJson(ObjectMapper objectMapper) {
        ArrayNode toolsArray = objectMapper.createArrayNode();
        
        for (Tool tool : tools.values()) {
            toolsArray.add(tool.toJson(objectMapper));
        }
        
        return toolsArray;
    }

    /**
     * Register browser tools.
     */
    public void registerBrowserTools() {
        registerTool(new BrowserSnapshotTool());
        registerTool(new BrowserScreenshotTool());
        registerTool(new BrowserCloseTool());
        registerTool(new BrowserResizeTool());
        registerTool(new BrowserNetworkRequestsTool());
        registerTool(new BrowserConsoleMessagesTool());
    }

    /**
     * Register navigation tools.
     */
    public void registerNavigationTools() {
        registerTool(new BrowserNavigateTool());
        registerTool(new BrowserNavigateBackTool());
        registerTool(new BrowserNavigateForwardTool());
    }

    /**
     * Register interaction tools.
     */
    public void registerInteractionTools() {
        registerTool(new BrowserClickTool());
        registerTool(new BrowserHoverTool());
        registerTool(new BrowserTypeTool());
        registerTool(new BrowserDragTool());
        registerTool(new BrowserSelectOptionTool());
        registerTool(new BrowserPressKeyTool());
        registerTool(new BrowserWaitForTool());
        registerTool(new BrowserFileUploadTool());
        registerTool(new BrowserHandleDialogTool());
    }

    /**
     * Register utility tools.
     */
    public void registerUtilityTools() {
        registerTool(new BrowserPdfSaveTool());
    }

    /**
     * Register tab tools.
     */
    public void registerTabTools() {
        registerTool(new BrowserTabListTool());
        registerTool(new BrowserTabNewTool());
        registerTool(new BrowserTabSelectTool());
        registerTool(new BrowserTabCloseTool());
    }

    /**
     * Register file tools.
     */
    public void registerFileTools() {
        // No file tools yet
    }

    /**
     * Register install tools.
     */
    public void registerInstallTools() {
        registerTool(new BrowserInstallTool());
    }
}
