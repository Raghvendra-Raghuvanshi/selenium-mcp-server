package com.selenium.mcp.server.tools.tabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;

/**
 * Tool to close a tab.
 */
public class BrowserTabCloseTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_tab_close";
    }

    @Override
    public String getTitle() {
        return "Close a tab";
    }

    @Override
    public String getDescription() {
        return "Close a tab";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addNumberParameter(schema, "index", "The index of the tab to close. Closes current tab if not provided.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        int index = params.has("index") ? params.get("index").asInt() : browserManager.getCurrentTabIndex();
        
        logger.info("Closing tab at index: {}", index);
        
        // Check if the index is valid
        if (index < 0 || index >= browserManager.getOpenTabs().size()) {
            throw new IllegalArgumentException("Invalid tab index: " + index + ". Only " + browserManager.getOpenTabs().size() + " tabs are open.");
        }
        
        // Close the tab
        browserManager.closeTab(index);
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Closed tab at index " + index);
    }
}
