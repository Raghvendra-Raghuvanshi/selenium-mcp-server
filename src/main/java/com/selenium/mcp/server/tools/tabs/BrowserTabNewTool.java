package com.selenium.mcp.server.tools.tabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;

/**
 * Tool to open a new tab.
 */
public class BrowserTabNewTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_tab_new";
    }

    @Override
    public String getTitle() {
        return "Open a new tab";
    }

    @Override
    public String getDescription() {
        return "Open a new tab";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "url", "The URL to navigate to in the new tab. If not provided, the new tab will be blank.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        String url = params.has("url") ? params.get("url").asText() : null;
        
        logger.info("Opening new tab with URL: {}", url != null ? url : "about:blank");
        
        // Open new tab
        browserManager.openNewTab(url);
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Opened new tab" + (url != null ? " with URL: " + url : ""));
    }
}
