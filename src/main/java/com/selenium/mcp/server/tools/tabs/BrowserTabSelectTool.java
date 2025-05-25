package com.selenium.mcp.server.tools.tabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.WebDriver;

/**
 * Tool to select a tab by index.
 */
public class BrowserTabSelectTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_tab_select";
    }

    @Override
    public String getTitle() {
        return "Select a tab";
    }

    @Override
    public String getDescription() {
        return "Select a tab by index";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "index");
        addNumberParameter(schema, "index", "The index of the tab to select", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("index") || !params.get("index").isNumber()) {
            throw new IllegalArgumentException("Index parameter is required and must be a number");
        }
        
        int index = params.get("index").asInt();
        if (index < 0) {
            throw new IllegalArgumentException("Index must be a non-negative number");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        int index = params.get("index").asInt();
        
        logger.info("Selecting tab at index: {}", index);
        
        // Check if the index is valid
        if (index >= browserManager.getOpenTabs().size()) {
            throw new IllegalArgumentException("Invalid tab index: " + index + ". Only " + browserManager.getOpenTabs().size() + " tabs are open.");
        }
        
        // Select the tab
        browserManager.switchToTab(index);
        
        // Get the current URL
        WebDriver driver = browserManager.getDriver();
        String url = driver.getCurrentUrl();
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Selected tab at index " + index + " with URL: " + url);
    }
}
