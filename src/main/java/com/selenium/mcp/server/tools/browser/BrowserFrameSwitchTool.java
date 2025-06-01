package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to switch between frames and iframes.
 */
public class BrowserFrameSwitchTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_frame_switch";
    }

    @Override
    public String getTitle() {
        return "Switch frame";
    }

    @Override
    public String getDescription() {
        return "Switch to a frame or iframe, or back to the main content";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to switch to the frame", false);
        addStringParameter(schema, "ref", "Exact target frame element reference from the page snapshot", false);
        addStringParameter(schema, "name", "Name or ID of the frame to switch to", false);
        addIntegerParameter(schema, "index", "Index of the frame to switch to (0-based)", false);
        addBooleanParameter(schema, "parent", "Whether to switch to the parent frame", false);
        addBooleanParameter(schema, "default", "Whether to switch back to the main content", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        // Count how many frame selection methods are specified
        int selectionMethods = 0;
        if (params.has("element") || params.has("ref")) selectionMethods++;
        if (params.has("name")) selectionMethods++;
        if (params.has("index")) selectionMethods++;
        if (params.has("parent")) selectionMethods++;
        if (params.has("default")) selectionMethods++;

        if (selectionMethods > 1) {
            throw new IllegalArgumentException("Only one frame selection method can be specified");
        }

        // Validate element parameters
        if ((params.has("element") && !params.has("ref")) || (!params.has("element") && params.has("ref"))) {
            throw new IllegalArgumentException("Both element description and reference must be provided together");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        // Handle different frame switching methods
        if (params.has("default") && params.get("default").asBoolean()) {
            // Switch to main content
            driver.switchTo().defaultContent();
            result.put("message", "Switched to main content");
        } else if (params.has("parent") && params.get("parent").asBoolean()) {
            // Switch to parent frame
            driver.switchTo().parentFrame();
            result.put("message", "Switched to parent frame");
        } else if (params.has("element") && params.has("ref")) {
            // Switch to frame by element
            String elementRef = params.get("ref").asText();
            WebElement frameElement = findElementByRef(driver, elementRef);
            driver.switchTo().frame(frameElement);
            result.put("message", "Switched to frame element: " + params.get("element").asText());
        } else if (params.has("name")) {
            // Switch to frame by name
            String name = params.get("name").asText();
            driver.switchTo().frame(name);
            result.put("message", "Switched to frame: " + name);
        } else if (params.has("index")) {
            // Switch to frame by index
            int index = params.get("index").asInt();
            driver.switchTo().frame(index);
            result.put("message", "Switched to frame at index: " + index);
        } else {
            throw new IllegalArgumentException("No frame selection method specified");
        }

        return result;
    }

    private WebElement findElementByRef(WebDriver driver, String elementRef) {
        if (elementRef.startsWith("element-")) {
            // Extract the element index
            String indexStr = elementRef.substring("element-".length());
            try {
                int index = Integer.parseInt(indexStr);
                
                // Find all elements
                java.util.List<WebElement> allElements = driver.findElements(By.xpath("//*"));
                
                // Return the element at the specified index
                if (index >= 0 && index < allElements.size()) {
                    return allElements.get(index);
                }
            } catch (NumberFormatException e) {
                // Ignore and fall back to XPath
            }
        }
        
        // Fallback: try to find by ID, CSS selector, or XPath
        try {
            return driver.findElement(By.id(elementRef));
        } catch (Exception e1) {
            try {
                return driver.findElement(By.cssSelector("[data-ref='" + elementRef + "']"));
            } catch (Exception e2) {
                try {
                    return driver.findElement(By.xpath("//*[@data-ref='" + elementRef + "']"));
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Could not find element with reference: " + elementRef);
                }
            }
        }
    }
} 