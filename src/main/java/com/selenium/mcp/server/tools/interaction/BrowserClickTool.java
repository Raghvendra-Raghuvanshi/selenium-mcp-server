package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to click on an element.
 */
public class BrowserClickTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_click";
    }

    @Override
    public String getTitle() {
        return "Click";
    }

    @Override
    public String getDescription() {
        return "Perform click on a web page";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "element", "ref");
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to interact with the element", true);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot", true);
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
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String elementDesc = params.get("element").asText();
        String elementRef = params.get("ref").asText();
        
        logger.info("Clicking on element: {} (ref: {})", elementDesc, elementRef);
        
        // Find the element by its reference ID
        // The reference is in the format "element-X" where X is a number
        WebElement element = findElementByRef(driver, elementRef);
        
        // Scroll the element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for the element to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        // Click the element
        element.click();
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Clicked on element: " + elementDesc);
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
