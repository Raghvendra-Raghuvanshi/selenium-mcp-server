package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

/**
 * Tool to capture an accessibility snapshot of the current page.
 */
public class BrowserSnapshotTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_snapshot";
    }

    @Override
    public String getTitle() {
        return "Page snapshot";
    }

    @Override
    public String getDescription() {
        return "Capture accessibility snapshot of the current page, this is better than screenshot";
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
        
        // Wait for page to load
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete"));
        } catch (TimeoutException e) {
            logger.warn("Page load timeout, proceeding with snapshot anyway");
        }
        
        // Create snapshot
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        
        // Add page info
        result.put("url", driver.getCurrentUrl());
        result.put("title", driver.getTitle());
        
        // Add accessibility tree
        result.set("snapshot", createAccessibilityTree(driver, objectMapper));
        
        return result;
    }

    private JsonNode createAccessibilityTree(WebDriver driver, ObjectMapper objectMapper) {
        // Get all elements
        List<WebElement> allElements = driver.findElements(By.xpath("//*"));
        
        // Create a map of elements to their IDs
        Map<WebElement, String> elementIds = new HashMap<>();
        for (int i = 0; i < allElements.size(); i++) {
            elementIds.put(allElements.get(i), "element-" + i);
        }
        
        // Create the root node
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "root");
        root.put("name", "Document");
        root.put("ref", "root");
        
        // Add the body as a child of the root
        ArrayNode rootChildren = root.putArray("children");
        
        // Find the body element
        WebElement body = driver.findElement(By.tagName("body"));
        
        // Process the body and its children
        rootChildren.add(processElement(body, elementIds, objectMapper));
        
        return root;
    }

    private ObjectNode processElement(WebElement element, Map<WebElement, String> elementIds, ObjectMapper objectMapper) {
        ObjectNode node = objectMapper.createObjectNode();
        
        // Get element properties
        String tagName = element.getTagName();
        String elementId = elementIds.get(element);
        
        // Set basic properties
        node.put("type", tagName);
        node.put("ref", elementId);
        
        // Try to get text content
        try {
            String text = element.getText().trim();
            if (!text.isEmpty()) {
                node.put("name", text);
            }
        } catch (Exception e) {
            // Ignore errors when getting text
        }
        
        // Try to get attributes
        ObjectNode attributes = node.putObject("attributes");
        try {
            // Common attributes
            addAttributeIfPresent(element, attributes, "id");
            addAttributeIfPresent(element, attributes, "class");
            addAttributeIfPresent(element, attributes, "href");
            addAttributeIfPresent(element, attributes, "src");
            addAttributeIfPresent(element, attributes, "alt");
            addAttributeIfPresent(element, attributes, "title");
            addAttributeIfPresent(element, attributes, "value");
            addAttributeIfPresent(element, attributes, "placeholder");
            addAttributeIfPresent(element, attributes, "type");
            addAttributeIfPresent(element, attributes, "name");
            addAttributeIfPresent(element, attributes, "role");
            addAttributeIfPresent(element, attributes, "aria-label");
            
            // Add more attributes as needed
        } catch (Exception e) {
            // Ignore errors when getting attributes
        }
        
        // Try to get position and size
        try {
            Rectangle rect = element.getRect();
            ObjectNode position = node.putObject("position");
            position.put("x", rect.getX());
            position.put("y", rect.getY());
            position.put("width", rect.getWidth());
            position.put("height", rect.getHeight());
        } catch (Exception e) {
            // Ignore errors when getting position
        }
        
        // Process children
        try {
            List<WebElement> children = element.findElements(By.xpath("./*"));
            if (!children.isEmpty()) {
                ArrayNode childNodes = node.putArray("children");
                for (WebElement child : children) {
                    if (elementIds.containsKey(child)) {
                        childNodes.add(processElement(child, elementIds, objectMapper));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors when processing children
        }
        
        return node;
    }

    private void addAttributeIfPresent(WebElement element, ObjectNode attributes, String attributeName) {
        try {
            String value = element.getAttribute(attributeName);
            if (value != null && !value.isEmpty()) {
                attributes.put(attributeName, value);
            }
        } catch (Exception e) {
            // Ignore errors when getting attribute
        }
    }
}
