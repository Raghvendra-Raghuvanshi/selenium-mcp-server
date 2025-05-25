package com.selenium.mcp.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Base class for MCP servers.
 */
public abstract class MCPServer {
    private static final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final ServerConfig config;
    protected final ToolRegistry toolRegistry;
    protected final BrowserManager browserManager;

    public MCPServer(ServerConfig config) {
        this.config = config;
        this.toolRegistry = new ToolRegistry();
        this.browserManager = new BrowserManager(config);
        registerTools();
    }

    /**
     * Start the server.
     */
    public abstract void start() throws Exception;

    /**
     * Handle an incoming message.
     */
    protected void handleMessage(String message) {
        try {
            JsonNode messageNode = objectMapper.readTree(message);
            String type = messageNode.get("type").asText();

            switch (type) {
                case "initialize":
                    handleInitialize(messageNode);
                    break;
                case "toolCall":
                    handleToolCall(messageNode);
                    break;
                default:
                    logger.warn("Unknown message type: {}", type);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling message", e);
            sendError("Error handling message: " + e.getMessage());
        }
    }

    /**
     * Handle an initialize message.
     */
    private void handleInitialize(JsonNode messageNode) throws IOException {
        String id = messageNode.get("id").asText();
        
        // Create response
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "initialize");
        response.put("id", id);
        
        // Add server info
        ObjectNode serverInfo = response.putObject("serverInfo");
        serverInfo.put("name", "selenium-mcp");
        serverInfo.put("version", "0.0.1");
        
        // Add tools
        response.set("tools", toolRegistry.getToolsAsJson(objectMapper));
        
        sendMessage(response.toString());
    }

    /**
     * Handle a tool call message.
     */
    private void handleToolCall(JsonNode messageNode) throws Exception {
        String id = messageNode.get("id").asText();
        String toolName = messageNode.get("name").asText();
        JsonNode params = messageNode.get("params");
        
        logger.info("Tool call: {} with params: {}", toolName, params);
        
        try {
            // Execute the tool
            JsonNode result = toolRegistry.executeTool(toolName, params, browserManager);
            
            // Create response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "toolCallResult");
            response.put("id", id);
            response.set("result", result);
            
            sendMessage(response.toString());
        } catch (Exception e) {
            logger.error("Error executing tool: {}", toolName, e);
            
            // Create error response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "toolCallResult");
            response.put("id", id);
            
            ObjectNode error = response.putObject("error");
            error.put("message", e.getMessage());
            
            sendMessage(response.toString());
        }
    }

    /**
     * Send a message to the client.
     */
    protected abstract void sendMessage(String message);

    /**
     * Send an error message to the client.
     */
    protected void sendError(String errorMessage) {
        try {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("type", "error");
            error.put("id", UUID.randomUUID().toString());
            error.put("message", errorMessage);
            
            sendMessage(error.toString());
        } catch (Exception e) {
            logger.error("Error sending error message", e);
        }
    }

    /**
     * Register all tools.
     */
    private void registerTools() {
        // Register browser tools
        toolRegistry.registerBrowserTools();
        
        // Register navigation tools
        toolRegistry.registerNavigationTools();
        
        // Register interaction tools
        toolRegistry.registerInteractionTools();
        
        // Register utility tools
        toolRegistry.registerUtilityTools();
        
        // Register tab tools if enabled
        if (config.hasCapability("tabs")) {
            toolRegistry.registerTabTools();
        }
        
        // Register file tools if enabled
        if (config.hasCapability("files")) {
            toolRegistry.registerFileTools();
        }
        
        // Register install tools if enabled
        if (config.hasCapability("install")) {
            toolRegistry.registerInstallTools();
        }
    }
}
