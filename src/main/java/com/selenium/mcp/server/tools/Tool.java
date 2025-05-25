package com.selenium.mcp.server.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;

/**
 * Interface for MCP tools.
 */
public interface Tool {
    /**
     * Get the name of the tool.
     */
    String getName();
    
    /**
     * Get the title of the tool.
     */
    String getTitle();
    
    /**
     * Get the description of the tool.
     */
    String getDescription();
    
    /**
     * Get the parameter schema for the tool.
     */
    JsonNode getParameterSchema(ObjectMapper objectMapper);
    
    /**
     * Check if the tool is read-only.
     */
    boolean isReadOnly();
    
    /**
     * Execute the tool.
     */
    JsonNode execute(JsonNode params, BrowserManager browserManager) throws Exception;
    
    /**
     * Convert the tool to JSON.
     */
    default ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode toolNode = objectMapper.createObjectNode();
        toolNode.put("name", getName());
        toolNode.put("title", getTitle());
        toolNode.put("description", getDescription());
        toolNode.put("readOnly", isReadOnly());
        toolNode.set("parameterSchema", getParameterSchema(objectMapper));
        return toolNode;
    }
}
