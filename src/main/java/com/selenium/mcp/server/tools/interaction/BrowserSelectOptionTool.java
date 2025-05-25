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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool to select an option in a dropdown.
 */
public class BrowserSelectOptionTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_select_option";
    }

    @Override
    public String getTitle() {
        return "Select option";
    }

    @Override
    public String getDescription() {
        return "Select an option in a dropdown";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "element", "ref", "values");
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to interact with the element", true);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot", true);
        addArrayParameter(schema, "values", "Array of values to select in the dropdown. This can be a single value or multiple values.", "string", true);
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
        
        if (!params.has("values") || !params.get("values").isArray() || params.get("values").size() == 0) {
            throw new IllegalArgumentException("Values parameter is required and must be a non-empty array");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String elementDesc = params.get("element").asText();
        String elementRef = params.get("ref").asText();
        JsonNode valuesNode = params.get("values");
        
        // Convert values to list
        List<String> values = new ArrayList<>();
        for (JsonNode valueNode : valuesNode) {
            values.add(valueNode.asText());
        }
        
        logger.info("Selecting options in element: {} (ref: {}), values: {}", elementDesc, elementRef, values);
        
        // Find the element by its reference ID
        WebElement element = findElementByRef(driver, elementRef);
        
        // Scroll the element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for the element to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        // Create a Select object
        Select select = new Select(element);
        
        // Check if multiple selection is allowed
        boolean isMultiple = select.isMultiple();
        
        // Clear existing selections if multiple
        if (isMultiple) {
            select.deselectAll();
        } else if (values.size() > 1) {
            logger.warn("Multiple values provided for a single-select dropdown. Only the first value will be used.");
        }
        
        // Select options
        for (String value : values) {
            try {
                // Try to select by value
                select.selectByValue(value);
            } catch (Exception e1) {
                try {
                    // Try to select by visible text
                    select.selectByVisibleText(value);
                } catch (Exception e2) {
                    try {
                        // Try to select by index if the value is a number
                        int index = Integer.parseInt(value);
                        select.selectByIndex(index);
                    } catch (Exception e3) {
                        logger.warn("Could not select option with value/text/index: {}", value);
                    }
                }
            }
            
            // Break after first selection if not multiple
            if (!isMultiple) {
                break;
            }
        }
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Selected options in element: " + elementDesc);
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
