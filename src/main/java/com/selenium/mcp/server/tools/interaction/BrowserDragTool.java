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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to perform drag and drop between two elements.
 */
public class BrowserDragTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_drag";
    }

    @Override
    public String getTitle() {
        return "Drag mouse";
    }

    @Override
    public String getDescription() {
        return "Perform drag and drop between two elements";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "startElement", "startRef", "endElement", "endRef");
        addStringParameter(schema, "startElement", "Human-readable source element description used to obtain the permission to interact with the element", true);
        addStringParameter(schema, "startRef", "Exact source element reference from the page snapshot", true);
        addStringParameter(schema, "endElement", "Human-readable target element description used to obtain the permission to interact with the element", true);
        addStringParameter(schema, "endRef", "Exact target element reference from the page snapshot", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("startElement") || params.get("startElement").asText().isEmpty()) {
            throw new IllegalArgumentException("Start element description parameter is required");
        }
        
        if (!params.has("startRef") || params.get("startRef").asText().isEmpty()) {
            throw new IllegalArgumentException("Start element reference parameter is required");
        }
        
        if (!params.has("endElement") || params.get("endElement").asText().isEmpty()) {
            throw new IllegalArgumentException("End element description parameter is required");
        }
        
        if (!params.has("endRef") || params.get("endRef").asText().isEmpty()) {
            throw new IllegalArgumentException("End element reference parameter is required");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String startElementDesc = params.get("startElement").asText();
        String startElementRef = params.get("startRef").asText();
        String endElementDesc = params.get("endElement").asText();
        String endElementRef = params.get("endRef").asText();
        
        logger.info("Dragging from element: {} (ref: {}) to element: {} (ref: {})", 
                startElementDesc, startElementRef, endElementDesc, endElementRef);
        
        // Find the elements by their reference IDs
        WebElement sourceElement = findElementByRef(driver, startElementRef);
        WebElement targetElement = findElementByRef(driver, endElementRef);
        
        // Scroll the source element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", sourceElement);
        
        // Wait for the source element to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(sourceElement));
        
        // Perform drag and drop
        Actions actions = new Actions(driver);
        actions.dragAndDrop(sourceElement, targetElement).perform();
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Dragged from element: " + startElementDesc + " to element: " + endElementDesc);
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
