package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to wait for text to appear or disappear or a specified time to pass.
 */
public class BrowserWaitForTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_wait_for";
    }

    @Override
    public String getTitle() {
        return "Wait for";
    }

    @Override
    public String getDescription() {
        return "Wait for text to appear or disappear or a specified time to pass";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addNumberParameter(schema, "time", "The time to wait in seconds", false);
        addStringParameter(schema, "text", "The text to wait for", false);
        addStringParameter(schema, "textGone", "The text to wait for to disappear", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        // At least one parameter must be provided
        if (!params.has("time") && !params.has("text") && !params.has("textGone")) {
            throw new IllegalArgumentException("At least one of 'time', 'text', or 'textGone' parameters must be provided");
        }
        
        // Validate time parameter
        if (params.has("time") && (!params.get("time").isNumber() || params.get("time").asDouble() <= 0)) {
            throw new IllegalArgumentException("Time parameter must be a positive number");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        // Get parameters
        double time = params.has("time") ? params.get("time").asDouble() : 10.0; // Default to 10 seconds
        String text = params.has("text") ? params.get("text").asText() : null;
        String textGone = params.has("textGone") ? params.get("textGone").asText() : null;
        
        // Create wait object
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis((long) (time * 1000)));
        
        // Wait for text to appear
        if (text != null) {
            logger.info("Waiting for text to appear: {}", text);
            try {
                wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//*"), text));
                return createSimpleResult(new ObjectMapper(), "Text appeared: " + text);
            } catch (TimeoutException e) {
                return createSimpleResult(new ObjectMapper(), "Timeout waiting for text to appear: " + text);
            }
        }
        
        // Wait for text to disappear
        if (textGone != null) {
            logger.info("Waiting for text to disappear: {}", textGone);
            try {
                wait.until(ExpectedConditions.not(
                        ExpectedConditions.textToBePresentInElementLocated(By.xpath("//*"), textGone)
                ));
                return createSimpleResult(new ObjectMapper(), "Text disappeared: " + textGone);
            } catch (TimeoutException e) {
                return createSimpleResult(new ObjectMapper(), "Timeout waiting for text to disappear: " + textGone);
            }
        }
        
        // Just wait for the specified time
        logger.info("Waiting for {} seconds", time);
        Thread.sleep((long) (time * 1000));
        
        return createSimpleResult(new ObjectMapper(), "Waited for " + time + " seconds");
    }
}
