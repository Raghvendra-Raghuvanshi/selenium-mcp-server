package com.selenium.mcp.server.tools.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.ServerConfig;
import com.selenium.mcp.server.tools.AbstractTool;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Tool to install the browser driver.
 */
public class BrowserInstallTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_install";
    }

    @Override
    public String getTitle() {
        return "Install the browser specified in the config";
    }

    @Override
    public String getDescription() {
        return "Install the browser specified in the config. Call this if you get an error about the browser not being installed.";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        return createParameterSchema(objectMapper);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        // Get the browser name from the browser manager
        ServerConfig config = new ServerConfig(); // This is a hack, we should get the config from the browser manager
        String browserName = config.getBrowserName().toLowerCase();
        
        logger.info("Installing driver for browser: {}", browserName);
        
        // Install the appropriate driver
        switch (browserName) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                break;
            case "safari":
                // Safari driver is built-in on macOS
                if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
                    throw new UnsupportedOperationException("Safari is only supported on macOS");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Installed driver for " + browserName);
    }
}
