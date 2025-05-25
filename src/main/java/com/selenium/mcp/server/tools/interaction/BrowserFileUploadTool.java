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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool to upload files.
 */
public class BrowserFileUploadTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_file_upload";
    }

    @Override
    public String getTitle() {
        return "Upload files";
    }

    @Override
    public String getDescription() {
        return "Upload one or multiple files";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "paths");
        addArrayParameter(schema, "paths", "The absolute paths to the files to upload. Can be a single file or multiple files.", "string", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("paths") || !params.get("paths").isArray() || params.get("paths").size() == 0) {
            throw new IllegalArgumentException("Paths parameter is required and must be a non-empty array");
        }
        
        // Validate that all files exist
        for (JsonNode pathNode : params.get("paths")) {
            String path = pathNode.asText();
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + path);
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("Path is not a file: " + path);
            }
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        // Get file paths
        List<String> paths = new ArrayList<>();
        for (JsonNode pathNode : params.get("paths")) {
            paths.add(pathNode.asText());
        }
        
        logger.info("Uploading files: {}", paths);
        
        // Find file input element
        WebElement fileInput = findFileInput(driver);
        
        if (fileInput == null) {
            throw new IllegalStateException("Could not find a file input element on the page");
        }
        
        // Make sure the file input is visible and enabled
        makeFileInputVisible(driver, fileInput);
        
        // Upload files
        fileInput.sendKeys(String.join("\n", paths));
        
        // Wait a moment for the upload to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Uploaded " + paths.size() + " file(s)");
    }
    
    private WebElement findFileInput(WebDriver driver) {
        // Try to find by type=file
        try {
            List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file']"));
            if (!fileInputs.isEmpty()) {
                return fileInputs.get(0);
            }
        } catch (Exception e) {
            logger.warn("Error finding file input by type: {}", e.getMessage());
        }
        
        // Try to find by accept attribute
        try {
            List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[accept]"));
            if (!fileInputs.isEmpty()) {
                return fileInputs.get(0);
            }
        } catch (Exception e) {
            logger.warn("Error finding file input by accept attribute: {}", e.getMessage());
        }
        
        return null;
    }
    
    private void makeFileInputVisible(WebDriver driver, WebElement fileInput) {
        try {
            // Check if the file input is visible and enabled
            if (!fileInput.isDisplayed() || !fileInput.isEnabled()) {
                // Make it visible using JavaScript
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].style.position = 'fixed';" +
                        "arguments[0].style.top = '0';" +
                        "arguments[0].style.left = '0';" +
                        "arguments[0].style.opacity = '1';" +
                        "arguments[0].style.display = 'block';" +
                        "arguments[0].style.visibility = 'visible';" +
                        "arguments[0].style.width = '100px';" +
                        "arguments[0].style.height = '100px';" +
                        "arguments[0].style.zIndex = '9999';",
                        fileInput);
            }
        } catch (Exception e) {
            logger.warn("Error making file input visible: {}", e.getMessage());
        }
    }
}
