package com.selenium.mcp.server.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP tools.
 */
public abstract class AbstractTool implements Tool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public JsonNode execute(JsonNode params, BrowserManager browserManager) throws Exception {
        logger.info("Executing tool: {} with params: {}", getName(), params);
        
        // Validate parameters
        validateParameters(params);
        
        // Execute the tool
        JsonNode result = executeImpl(params, browserManager);
        
        logger.info("Tool execution completed: {}", getName());
        return result;
    }
    
    /**
     * Validate the parameters for the tool.
     */
    protected void validateParameters(JsonNode params) throws Exception {
        // Default implementation does nothing
    }
    
    /**
     * Implement the tool execution.
     */
    protected abstract JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception;
    
    /**
     * Create a simple parameter schema with required parameters.
     */
    protected ObjectNode createParameterSchema(ObjectMapper objectMapper, String... requiredParams) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        
        ObjectNode properties = schema.putObject("properties");
        
        if (requiredParams.length > 0) {
            schema.set("required", objectMapper.valueToTree(requiredParams));
        }
        
        return schema;
    }
    
    /**
     * Add a string parameter to the schema.
     */
    protected void addStringParameter(ObjectNode schema, String name, String description, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        ObjectNode param = properties.putObject(name);
        param.put("type", "string");
        param.put("description", description);
        
        if (required) {
            // Make sure the required array exists
            if (!schema.has("required")) {
                schema.putArray("required");
            }
            
            // Add the parameter to the required array
            schema.withArray("required").add(name);
        }
    }
    
    /**
     * Add a number parameter to the schema.
     */
    protected void addNumberParameter(ObjectNode schema, String name, String description, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        ObjectNode param = properties.putObject(name);
        param.put("type", "number");
        param.put("description", description);
        
        if (required) {
            // Make sure the required array exists
            if (!schema.has("required")) {
                schema.putArray("required");
            }
            
            // Add the parameter to the required array
            schema.withArray("required").add(name);
        }
    }
    
    /**
     * Add a boolean parameter to the schema.
     */
    protected void addBooleanParameter(ObjectNode schema, String name, String description, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        ObjectNode param = properties.putObject(name);
        param.put("type", "boolean");
        param.put("description", description);
        
        if (required) {
            // Make sure the required array exists
            if (!schema.has("required")) {
                schema.putArray("required");
            }
            
            // Add the parameter to the required array
            schema.withArray("required").add(name);
        }
    }
    
    /**
     * Add an array parameter to the schema.
     */
    protected void addArrayParameter(ObjectNode schema, String name, String description, String itemType, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        ObjectNode param = properties.putObject(name);
        param.put("type", "array");
        param.put("description", description);
        
        ObjectNode items = param.putObject("items");
        items.put("type", itemType);
        
        if (required) {
            // Make sure the required array exists
            if (!schema.has("required")) {
                schema.putArray("required");
            }
            
            // Add the parameter to the required array
            schema.withArray("required").add(name);
        }
    }
    
    /**
     * Create a simple result with a message.
     */
    protected JsonNode createSimpleResult(ObjectMapper objectMapper, String message) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("message", message);
        return result;
    }
    
    /**
     * Create a result with content.
     */
    protected JsonNode createContentResult(ObjectMapper objectMapper, String content) {
        ObjectNode result = objectMapper.createObjectNode();
        result.putArray("content").add(content);
        return result;
    }
}
