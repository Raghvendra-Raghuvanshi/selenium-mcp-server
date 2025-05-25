package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;

/**
 * Tool to close the browser.
 */
public class BrowserCloseTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_close";
    }

    @Override
    public String getTitle() {
        return "Close browser";
    }

    @Override
    public String getDescription() {
        return "Close the browser";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        return createParameterSchema(objectMapper);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        logger.info("Closing browser");
        
        browserManager.close();
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Browser closed");
    }
}
