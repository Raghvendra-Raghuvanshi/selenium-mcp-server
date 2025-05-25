package com.selenium.mcp.server.tools.tabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Tool to list browser tabs.
 */
public class BrowserTabListTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_tab_list";
    }

    @Override
    public String getTitle() {
        return "List tabs";
    }

    @Override
    public String getDescription() {
        return "List browser tabs";
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
        List<String> tabs = browserManager.getOpenTabs();
        int currentTabIndex = browserManager.getCurrentTabIndex();
        
        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        
        // Add tabs
        ArrayNode tabsArray = result.putArray("tabs");
        
        // Save current window handle
        String currentHandle = driver.getWindowHandle();
        
        // Get information about each tab
        for (int i = 0; i < tabs.size(); i++) {
            String handle = tabs.get(i);
            
            // Switch to the tab
            driver.switchTo().window(handle);
            
            // Get tab information
            ObjectNode tab = tabsArray.addObject();
            tab.put("index", i);
            tab.put("url", driver.getCurrentUrl());
            tab.put("title", driver.getTitle());
            tab.put("current", i == currentTabIndex);
        }
        
        // Switch back to the original tab
        driver.switchTo().window(currentHandle);
        
        // Add message
        result.put("message", "Found " + tabs.size() + " tabs");
        
        return result;
    }
}
