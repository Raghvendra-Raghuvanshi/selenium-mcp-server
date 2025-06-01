package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import com.selenium.mcp.server.tools.browser.ElementFinder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Tool to type text into elements with enhanced element detection.
 */
public class BrowserTypeTool extends AbstractTool {
    private static final Logger logger = LoggerFactory.getLogger(BrowserTypeTool.class);
    private static final Duration TYPE_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public String getName() {
        return "browser_type";
    }

    @Override
    public String getTitle() {
        return "Type text";
    }

    @Override
    public String getDescription() {
        return "Type text into an element using enhanced element detection";
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
        addStringParameter(schema, "text", "Text to type into the element", true);
        addBooleanParameter(schema, "clear", "Whether to clear the element before typing", false);
        addBooleanParameter(schema, "submit", "Whether to submit the form after typing", false);
        addBooleanParameter(schema, "force", "Whether to force type even if element is not interactable", false);
        return schema;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("element") || !params.has("ref")) {
            throw new IllegalArgumentException("Both element description and reference must be provided");
        }
        if (!params.has("text")) {
            throw new IllegalArgumentException("Text parameter is required");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        String elementRef = params.get("ref").asText();
        String text = params.get("text").asText();
        boolean clear = params.has("clear") && params.get("clear").asBoolean();
        boolean submit = params.has("submit") && params.get("submit").asBoolean();
        boolean force = params.has("force") && params.get("force").asBoolean();

        logger.debug("Finding element to type into: {}", elementRef);
        WebElement element = ElementFinder.findElement(driver, elementRef);

        if (!force) {
            // Wait for element to be interactable
            WebDriverWait wait = new WebDriverWait(driver, TYPE_TIMEOUT);
            wait.until(ExpectedConditions.elementToBeClickable(element));
        }

        try {
            if (clear) {
                if (force) {
                    // Use JavaScript to clear the element
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = '';", element);
                } else {
                    element.clear();
                }
            }

            if (force) {
                // Use JavaScript to set the value
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];", element, text);
            } else {
                element.sendKeys(text);
            }

            if (submit) {
                element.sendKeys(Keys.RETURN);
            }

            result.put("message", "Typed text into element: " + params.get("element").asText());
        } catch (Exception e) {
            logger.error("Failed to type into element: {}", e.getMessage());
            if (force) {
                // Try JavaScript as a last resort
                try {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1];" + 
                        (submit ? "arguments[0].form.submit();" : ""), 
                        element, text);
                    result.put("message", "Force typed text into element using JavaScript: " + 
                        params.get("element").asText());
                } catch (Exception jsError) {
                    throw new RuntimeException("Failed to type into element even with force option: " + 
                        jsError.getMessage());
                }
            } else {
                throw e;
            }
        }

        return result;
    }
}
