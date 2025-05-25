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
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Tool to get console messages.
 */
public class BrowserConsoleMessagesTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_console_messages";
    }

    @Override
    public String getTitle() {
        return "Get console messages";
    }

    @Override
    public String getDescription() {
        return "Returns all console messages";
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
        
        // Get console logs
        List<LogEntry> logs = new ArrayList<>();
        try {
            logs = driver.manage().logs().get(LogType.BROWSER).getAll();
        } catch (Exception e) {
            logger.warn("Could not get browser logs: {}", e.getMessage());
            
            // Try to get console logs using JavaScript
            return getConsoleLogsUsingJavaScript(driver);
        }
        
        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode messages = result.putArray("messages");
        
        // Process each log entry
        for (LogEntry entry : logs) {
            ObjectNode message = messages.addObject();
            message.put("level", getLevelName(entry.getLevel()));
            message.put("message", entry.getMessage());
            message.put("timestamp", entry.getTimestamp());
        }
        
        // Add message
        result.put("message", "Retrieved " + messages.size() + " console messages");
        
        return result;
    }
    
    private String getLevelName(Level level) {
        if (level.equals(Level.SEVERE)) {
            return "error";
        } else if (level.equals(Level.WARNING)) {
            return "warning";
        } else if (level.equals(Level.INFO)) {
            return "info";
        } else {
            return "log";
        }
    }
    
    private JsonNode getConsoleLogsUsingJavaScript(WebDriver driver) {
        // Use JavaScript to get console logs
        // First, inject a script to capture console logs
        String injectScript = 
                "if (!window._seleniumConsoleLogs) {" +
                "  window._seleniumConsoleLogs = [];" +
                "  var originalConsole = {" +
                "    log: console.log," +
                "    info: console.info," +
                "    warn: console.warn," +
                "    error: console.error" +
                "  };" +
                "  console.log = function() {" +
                "    window._seleniumConsoleLogs.push({level: 'log', message: Array.from(arguments).join(' '), timestamp: Date.now()});" +
                "    originalConsole.log.apply(console, arguments);" +
                "  };" +
                "  console.info = function() {" +
                "    window._seleniumConsoleLogs.push({level: 'info', message: Array.from(arguments).join(' '), timestamp: Date.now()});" +
                "    originalConsole.info.apply(console, arguments);" +
                "  };" +
                "  console.warn = function() {" +
                "    window._seleniumConsoleLogs.push({level: 'warning', message: Array.from(arguments).join(' '), timestamp: Date.now()});" +
                "    originalConsole.warn.apply(console, arguments);" +
                "  };" +
                "  console.error = function() {" +
                "    window._seleniumConsoleLogs.push({level: 'error', message: Array.from(arguments).join(' '), timestamp: Date.now()});" +
                "    originalConsole.error.apply(console, arguments);" +
                "  };" +
                "}";
        
        ((JavascriptExecutor) driver).executeScript(injectScript);
        
        // Now get the captured logs
        String getLogsScript = "return window._seleniumConsoleLogs || [];";
        List<Object> logs = (List<Object>) ((JavascriptExecutor) driver).executeScript(getLogsScript);
        
        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode messages = result.putArray("messages");
        
        // Process each log entry
        for (Object log : logs) {
            try {
                // Convert log to JSON
                String logJson = objectMapper.writeValueAsString(log);
                JsonNode logNode = objectMapper.readTree(logJson);
                
                // Add to messages
                messages.add(logNode);
            } catch (Exception e) {
                logger.warn("Could not parse log entry: {}", e.getMessage());
            }
        }
        
        // Add message
        result.put("message", "Retrieved " + messages.size() + " console messages using JavaScript");
        
        return result;
    }
}
