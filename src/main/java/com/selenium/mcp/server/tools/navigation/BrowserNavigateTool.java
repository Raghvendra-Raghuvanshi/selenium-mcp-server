package com.selenium.mcp.server.tools.navigation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.WebDriver;

/**
 * Tool to navigate to a URL.
 */
public class BrowserNavigateTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_navigate";
    }

    @Override
    public String getTitle() {
        return "Navigate to a URL";
    }

    @Override
    public String getDescription() {
        return "Navigate to a URL";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "url");
        addStringParameter(schema, "url", "The URL to navigate to", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("url") || params.get("url").asText().isEmpty()) {
            throw new IllegalArgumentException("URL parameter is required");
        }
        
        String url = params.get("url").asText();
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
            // Add https:// prefix if missing
            url = "https://" + url;
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String url = params.get("url").asText();
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
            // Add https:// prefix if missing
            url = "https://" + url;
        }
        
        logger.info("Navigating to URL: {}", url);
        driver.get(url);
        
        // Wait for page to load
        try {
            Thread.sleep(1000); // Simple wait for page to start loading
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Navigated to " + driver.getCurrentUrl());
    }
}
