package com.selenium.mcp.server.tools.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpMethod;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool to save the current page as PDF.
 * Note: This currently only works with Chrome/Chromium browsers.
 */
public class BrowserPdfSaveTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_pdf_save";
    }

    @Override
    public String getTitle() {
        return "Save as PDF";
    }

    @Override
    public String getDescription() {
        return "Save the current page as PDF";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "filename", "File name to save the PDF to", false);
        addBooleanParameter(schema, "landscape", "Whether to use landscape orientation", false);
        addBooleanParameter(schema, "printBackground", "Whether to print background graphics", false);
        addNumberParameter(schema, "scale", "Scale of the webpage rendering (0.1 to 2)", false);
        addBooleanParameter(schema, "preferCSSPageSize", "Whether to prefer page size as defined by CSS", false);
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

        // Check if we're using Chrome/Chromium
        if (!(driver instanceof ChromeDriver || driver instanceof RemoteWebDriver)) {
            throw new UnsupportedOperationException("PDF generation is only supported in Chrome/Chromium browsers");
        }

        // Get parameters
        String filename = params.has("filename") ? params.get("filename").asText() : null;
        boolean landscape = params.has("landscape") && params.get("landscape").asBoolean();
        boolean printBackground = params.has("printBackground") && params.get("printBackground").asBoolean();
        double scale = params.has("scale") ? params.get("scale").asDouble() : 1.0;
        boolean preferCSSPageSize = params.has("preferCSSPageSize") && params.get("preferCSSPageSize").asBoolean();

        // Create print parameters
        Map<String, Object> printParams = new HashMap<>();
        printParams.put("landscape", landscape);
        printParams.put("printBackground", printBackground);
        printParams.put("scale", scale);
        printParams.put("preferCSSPageSize", preferCSSPageSize);

        // Execute print command using DevTools Protocol
        String pdfData;
        if (driver instanceof ChromeDriver) {
            pdfData = ((ChromeDriver) driver).executeCdpCommand("Page.printToPDF", printParams).get("data").toString();
        } else {
            // For RemoteWebDriver, we need to use a different approach
            // This is a simplified version - in production, you'd want to use proper CDP support
            throw new UnsupportedOperationException("PDF generation is not supported for remote Chrome instances");
        }

        // Handle output
        if (filename != null) {
            // Save to file
            File outputFile = new File(filename);
            byte[] pdfBytes = Base64.getDecoder().decode(pdfData);
            java.nio.file.Files.write(outputFile.toPath(), pdfBytes);
            result.put("filename", outputFile.getAbsolutePath());
        } else {
            // Return as base64
            result.put("data", pdfData);
            result.put("format", "pdf");
        }

        return result;
    }

    /**
     * Add a number parameter to the schema.
     */
    protected void addNumberParameter(ObjectNode schema, String name, String description, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        ObjectNode param = properties.putObject(name);
        param.put("type", "number");
        param.put("description", description);
        
        if (required) {
            // Make sure the required array exists
            if (!schema.has("required")) {
                schema.putArray("required");
            }
            
            // Add the parameter to the required array
            schema.withArray("required").add(name);
        }
    }
} 