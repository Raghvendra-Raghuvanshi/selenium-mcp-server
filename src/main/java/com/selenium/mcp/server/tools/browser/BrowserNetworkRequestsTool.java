package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Tool to list network requests.
 * Note: This is a limited implementation as Selenium doesn't provide direct access to network requests.
 * It uses JavaScript to retrieve information from the browser's performance API.
 */
public class BrowserNetworkRequestsTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_network_requests";
    }

    @Override
    public String getTitle() {
        return "List network requests";
    }

    @Override
    public String getDescription() {
        return "Returns all network requests since loading the page";
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
        
        // Use JavaScript to get network requests from the Performance API
        String script = 
                "var performance = window.performance || window.mozPerformance || window.msPerformance || window.webkitPerformance || {}; " +
                "var network = performance.getEntries ? performance.getEntries() : []; " +
                "return JSON.stringify(network);";
        
        String networkData = (String) ((JavascriptExecutor) driver).executeScript(script);
        
        // Parse the network data
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode networkEntries = objectMapper.readTree(networkData);
        
        // Create result
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode requests = result.putArray("requests");
        
        // Process each network entry
        for (JsonNode entry : networkEntries) {
            if (entry.has("entryType") && entry.get("entryType").asText().equals("resource")) {
                ObjectNode request = requests.addObject();
                
                // Add basic information
                if (entry.has("name")) {
                    request.put("url", entry.get("name").asText());
                }
                
                if (entry.has("initiatorType")) {
                    request.put("type", entry.get("initiatorType").asText());
                }
                
                // Add timing information
                if (entry.has("startTime") && entry.has("responseEnd")) {
                    double duration = entry.get("responseEnd").asDouble() - entry.get("startTime").asDouble();
                    request.put("duration", duration);
                }
                
                // Add more details if available
                if (entry.has("transferSize")) {
                    request.put("size", entry.get("transferSize").asInt());
                }
            }
        }
        
        // Add message
        result.put("message", "Retrieved " + requests.size() + " network requests");
        
        return result;
    }
}
