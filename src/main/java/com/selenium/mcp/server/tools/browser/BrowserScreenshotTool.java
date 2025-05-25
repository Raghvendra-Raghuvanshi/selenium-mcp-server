package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Tool to take a screenshot of the current page.
 */
public class BrowserScreenshotTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_take_screenshot";
    }

    @Override
    public String getTitle() {
        return "Take a screenshot";
    }

    @Override
    public String getDescription() {
        return "Take a screenshot of the current page. You can't perform actions based on the screenshot, use browser_snapshot for actions.";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addBooleanParameter(schema, "raw", "Whether to return without compression (in PNG format). Default is false, which returns a JPEG image.", false);
        addStringParameter(schema, "filename", "File name to save the screenshot to. Defaults to `page-{timestamp}.{png|jpeg}` if not specified.", false);
        addStringParameter(schema, "element", "Human-readable element description used to obtain permission to screenshot the element. If not provided, the screenshot will be taken of viewport. If element is provided, ref must be provided too.", false);
        addStringParameter(schema, "ref", "Exact target element reference from the page snapshot. If not provided, the screenshot will be taken of viewport. If ref is provided, element must be provided too.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        boolean raw = params.has("raw") && params.get("raw").asBoolean();
        String filename = params.has("filename") ? params.get("filename").asText() : null;
        String elementDesc = params.has("element") ? params.get("element").asText() : null;
        String elementRef = params.has("ref") ? params.get("ref").asText() : null;
        
        // Take screenshot
        byte[] screenshotBytes;
        if (elementDesc != null && elementRef != null) {
            // Take screenshot of specific element
            WebElement element = findElementByRef(driver, elementRef);
            screenshotBytes = takeElementScreenshot(driver, element, raw);
        } else {
            // Take screenshot of entire page
            screenshotBytes = takeFullScreenshot(driver, raw);
        }
        
        // Save screenshot if filename is provided
        if (filename != null) {
            saveScreenshot(screenshotBytes, filename);
        } else {
            // Generate default filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String extension = raw ? "png" : "jpeg";
            filename = "page-" + timestamp + "." + extension;
            saveScreenshot(screenshotBytes, filename);
        }
        
        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        
        // Add screenshot as base64
        String base64Image = Base64.getEncoder().encodeToString(screenshotBytes);
        String mimeType = raw ? "image/png" : "image/jpeg";
        
        // Create content array with image
        result.putArray("content")
                .addObject()
                .put("type", "image")
                .put("data", base64Image)
                .put("mimeType", mimeType);
        
        // Add message
        result.put("message", "Screenshot saved as " + filename);
        
        return result;
    }
    
    private byte[] takeFullScreenshot(WebDriver driver, boolean raw) throws IOException {
        // Take screenshot
        byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        
        // Convert to requested format if needed
        if (!raw) {
            screenshotBytes = convertToJpeg(screenshotBytes);
        }
        
        return screenshotBytes;
    }
    
    private byte[] takeElementScreenshot(WebDriver driver, WebElement element, boolean raw) throws IOException {
        // Scroll element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        try {
            // Wait for scrolling to complete
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Take screenshot of element
        byte[] screenshotBytes = element.getScreenshotAs(OutputType.BYTES);
        
        // Convert to requested format if needed
        if (!raw) {
            screenshotBytes = convertToJpeg(screenshotBytes);
        }
        
        return screenshotBytes;
    }
    
    private byte[] convertToJpeg(byte[] pngBytes) throws IOException {
        // Read PNG image
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(pngBytes));
        
        // Convert to JPEG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", outputStream);
        
        return outputStream.toByteArray();
    }
    
    private void saveScreenshot(byte[] screenshotBytes, String filename) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get("screenshots");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        // Save screenshot
        File outputFile = outputDir.resolve(filename).toFile();
        FileUtils.writeByteArrayToFile(outputFile, screenshotBytes);
        
        logger.info("Screenshot saved to {}", outputFile.getAbsolutePath());
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
