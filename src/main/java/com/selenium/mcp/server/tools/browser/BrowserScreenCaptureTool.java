package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Base64;

/**
 * Tool to capture screen or element screenshots with vision mode support.
 */
public class BrowserScreenCaptureTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_screen_capture";
    }

    @Override
    public String getTitle() {
        return "Take a screenshot";
    }

    @Override
    public String getDescription() {
        return "Take a screenshot of the current page or a specific element";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to screenshot the element", false);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot", false);
        addBooleanParameter(schema, "raw", "Whether to return without compression (in PNG format)", false);
        addStringParameter(schema, "filename", "File name to save the screenshot to", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        // Get parameters
        boolean raw = params.has("raw") && params.get("raw").asBoolean();
        String filename = params.has("filename") ? params.get("filename").asText() : null;
        String elementDesc = params.has("element") ? params.get("element").asText() : null;
        String elementRef = params.has("ref") ? params.get("ref").asText() : null;

        // Validate element parameters
        if ((elementDesc != null && elementRef == null) || (elementDesc == null && elementRef != null)) {
            throw new IllegalArgumentException("Both element description and reference must be provided together");
        }

        // Take screenshot
        byte[] screenshot;
        if (elementRef != null) {
            // Find and screenshot specific element
            WebElement element = findElementByRef(driver, elementRef);
            screenshot = element.getScreenshotAs(OutputType.BYTES);
        } else {
            // Screenshot entire page
            screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }

        // Handle output
        if (filename != null) {
            // Save to file
            File outputFile = new File(filename);
            java.nio.file.Files.write(outputFile.toPath(), screenshot);
            result.put("filename", outputFile.getAbsolutePath());
        } else {
            // Return as base64
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            result.put("data", base64);
            result.put("format", raw ? "png" : "jpeg");
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