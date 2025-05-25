package com.selenium.mcp.server.tools.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.PrintsPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.print.PrintOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool to save the current page as PDF.
 * Note: This only works with headless Chrome.
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
        return "Save page as PDF";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper);
        addStringParameter(schema, "filename", "File name to save the pdf to. Defaults to `page-{timestamp}.pdf` if not specified.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();

        // Check if the driver supports PDF printing
        if (!(driver instanceof PrintsPage)) {
            // Try using JavaScript for non-Chrome browsers
            return savePdfUsingJavaScript(driver, params);
        }

        String filename = params.has("filename") ? params.get("filename").asText() : null;

        // Generate default filename if not provided
        if (filename == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            filename = "page-" + timestamp + ".pdf";
        }

        // Create output directory if it doesn't exist
        Path outputDir = Paths.get("pdfs");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Save PDF
        File outputFile = outputDir.resolve(filename).toFile();

        // Create print options
        PrintOptions printOptions = new PrintOptions();
        printOptions.setPageRanges("1-10"); // Limit to 10 pages by default

        // Print to PDF
        Pdf pdf = ((PrintsPage) driver).print(printOptions);

        // Save PDF to file
        byte[] pdfBytes = Base64.getDecoder().decode(pdf.getContent());
        FileUtils.writeByteArrayToFile(outputFile, pdfBytes);

        logger.info("PDF saved to {}", outputFile.getAbsolutePath());

        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        result.put("message", "PDF saved as " + filename);
        result.put("path", outputFile.getAbsolutePath());

        return result;
    }

    private JsonNode savePdfUsingJavaScript(WebDriver driver, JsonNode params) throws Exception {
        String filename = params.has("filename") ? params.get("filename").asText() : null;

        // Generate default filename if not provided
        if (filename == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            filename = "page-" + timestamp + ".pdf";
        }

        logger.warn("Direct PDF printing not supported by this browser. Using JavaScript to trigger browser's print dialog.");

        // Create result
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        result.put("message", "PDF printing is only supported in headless Chrome. Please use the browser's print dialog to save as PDF.");

        // Try to open print dialog
        try {
            ((JavascriptExecutor) driver).executeScript("window.print();");
            result.put("status", "print-dialog-opened");
        } catch (Exception e) {
            logger.error("Failed to open print dialog", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
