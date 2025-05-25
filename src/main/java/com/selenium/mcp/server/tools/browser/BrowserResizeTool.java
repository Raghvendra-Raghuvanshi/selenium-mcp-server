package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

/**
 * Tool to resize the browser window.
 */
public class BrowserResizeTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_resize";
    }

    @Override
    public String getTitle() {
        return "Resize browser window";
    }

    @Override
    public String getDescription() {
        return "Resize the browser window";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "width", "height");
        addNumberParameter(schema, "width", "Width of the browser window", true);
        addNumberParameter(schema, "height", "Height of the browser window", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("width") || !params.get("width").isNumber()) {
            throw new IllegalArgumentException("Width parameter is required and must be a number");
        }
        
        if (!params.has("height") || !params.get("height").isNumber()) {
            throw new IllegalArgumentException("Height parameter is required and must be a number");
        }
        
        int width = params.get("width").asInt();
        int height = params.get("height").asInt();
        
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive numbers");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        int width = params.get("width").asInt();
        int height = params.get("height").asInt();
        
        logger.info("Resizing browser window to {}x{}", width, height);
        
        driver.manage().window().setSize(new Dimension(width, height));
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Resized browser window to " + width + "x" + height);
    }
}
