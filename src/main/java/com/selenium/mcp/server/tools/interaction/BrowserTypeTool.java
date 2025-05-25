package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to type text into an element.
 */
public class BrowserTypeTool extends AbstractTool {
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
        return "Type text into editable element";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "element", "ref", "text");
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to interact with the element", true);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot", true);
        addStringParameter(schema, "text", "Text to type into the element", true);
        addBooleanParameter(schema, "submit", "Whether to submit entered text (press Enter after)", false);
        addBooleanParameter(schema, "slowly", "Whether to type one character at a time. Useful for triggering key handlers in the page. By default entire text is filled in at once.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("element") || params.get("element").asText().isEmpty()) {
            throw new IllegalArgumentException("Element description parameter is required");
        }
        
        if (!params.has("ref") || params.get("ref").asText().isEmpty()) {
            throw new IllegalArgumentException("Element reference parameter is required");
        }
        
        if (!params.has("text")) {
            throw new IllegalArgumentException("Text parameter is required");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String elementDesc = params.get("element").asText();
        String elementRef = params.get("ref").asText();
        String text = params.get("text").asText();
        boolean submit = params.has("submit") && params.get("submit").asBoolean();
        boolean slowly = params.has("slowly") && params.get("slowly").asBoolean();
        
        logger.info("Typing text into element: {} (ref: {})", elementDesc, elementRef);
        
        // Find the element by its reference ID
        WebElement element = findElementByRef(driver, elementRef);
        
        // Scroll the element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for the element to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        // Clear the element
        element.clear();
        
        // Type the text
        if (slowly) {
            // Type character by character
            for (char c : text.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                try {
                    Thread.sleep(50); // Small delay between characters
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            // Type all at once
            element.sendKeys(text);
        }
        
        // Submit if requested
        if (submit) {
            element.sendKeys(Keys.ENTER);
        }
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Typed text into element: " + elementDesc);
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
