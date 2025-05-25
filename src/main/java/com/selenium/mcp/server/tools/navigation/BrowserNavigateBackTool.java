package com.selenium.mcp.server.tools.navigation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.WebDriver;

/**
 * Tool to navigate back in browser history.
 */
public class BrowserNavigateBackTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_navigate_back";
    }

    @Override
    public String getTitle() {
        return "Go back";
    }

    @Override
    public String getDescription() {
        return "Go back to the previous page";
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
        WebDriver driver = browserManager.getDriver();
        
        logger.info("Navigating back");
        driver.navigate().back();
        
        // Wait for page to load
        try {
            Thread.sleep(1000); // Simple wait for page to start loading
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Navigated back to " + driver.getCurrentUrl());
    }
}
