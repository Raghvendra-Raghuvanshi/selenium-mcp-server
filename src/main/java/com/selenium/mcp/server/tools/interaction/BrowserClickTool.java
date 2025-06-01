package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import com.selenium.mcp.server.tools.browser.ElementFinder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Tool to click on elements with enhanced element detection.
 */
public class BrowserClickTool extends AbstractTool {
    private static final Logger logger = LoggerFactory.getLogger(BrowserClickTool.class);
    private static final Duration CLICK_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public String getName() {
        return "browser_click";
    }

    @Override
    public String getTitle() {
        return "Click element";
    }

    @Override
    public String getDescription() {
        return "Click on an element using enhanced element detection";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "element", "Human-readable element description", true);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot", true);
        addBooleanParameter(schema, "force", "Whether to force click even if element is not clickable", false);
        addBooleanParameter(schema, "double", "Whether to perform a double click", false);
        addBooleanParameter(schema, "right", "Whether to perform a right click", false);
        return schema;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("element") || !params.has("ref")) {
            throw new IllegalArgumentException("Both element description and reference must be provided");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        String elementRef = params.get("ref").asText();
        boolean force = params.has("force") && params.get("force").asBoolean();
        boolean doubleClick = params.has("double") && params.get("double").asBoolean();
        boolean rightClick = params.has("right") && params.get("right").asBoolean();

        logger.debug("Finding element to click: {}", elementRef);
        WebElement element = ElementFinder.findElement(driver, elementRef);

        if (!force) {
            // Wait for element to be clickable
            WebDriverWait wait = new WebDriverWait(driver, CLICK_TIMEOUT);
            wait.until(ExpectedConditions.elementToBeClickable(element));
        }

        Actions actions = new Actions(driver);

        try {
            if (doubleClick) {
                actions.doubleClick(element).perform();
                result.put("message", "Double clicked element: " + params.get("element").asText());
            } else if (rightClick) {
                actions.contextClick(element).perform();
                result.put("message", "Right clicked element: " + params.get("element").asText());
            } else {
                if (force) {
                    // Use JavaScript click as a fallback
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                } else {
                    element.click();
                }
                result.put("message", "Clicked element: " + params.get("element").asText());
            }
        } catch (Exception e) {
            logger.error("Failed to click element: {}", e.getMessage());
            if (force) {
                // Try JavaScript click as a last resort
                try {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    result.put("message", "Force clicked element using JavaScript: " + params.get("element").asText());
                } catch (Exception jsError) {
                    throw new RuntimeException("Failed to click element even with force option: " + jsError.getMessage());
                }
            } else {
                throw e;
            }
        }

        return result;
    }
}
