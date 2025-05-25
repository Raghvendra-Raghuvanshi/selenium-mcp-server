package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tool to handle browser dialogs (alert, confirm, prompt).
 */
public class BrowserHandleDialogTool extends AbstractTool {
    @Override
    public String getName() {
        return "browser_handle_dialog";
    }

    @Override
    public String getTitle() {
        return "Handle a dialog";
    }

    @Override
    public String getDescription() {
        return "Handle a dialog";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "accept");
        addBooleanParameter(schema, "accept", "Whether to accept the dialog.", true);
        addStringParameter(schema, "promptText", "The text of the prompt in case of a prompt dialog.", false);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("accept")) {
            throw new IllegalArgumentException("Accept parameter is required");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        boolean accept = params.get("accept").asBoolean();
        String promptText = params.has("promptText") ? params.get("promptText").asText() : null;
        
        logger.info("Handling dialog: accept={}, promptText={}", accept, promptText);
        
        // Wait for alert to be present
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        Alert alert;
        
        try {
            alert = wait.until(ExpectedConditions.alertIsPresent());
        } catch (Exception e) {
            throw new IllegalStateException("No dialog is present");
        }
        
        // Get dialog text
        String dialogText = alert.getText();
        
        // Handle dialog
        if (promptText != null) {
            // This is a prompt dialog
            alert.sendKeys(promptText);
        }
        
        if (accept) {
            alert.accept();
        } else {
            alert.dismiss();
        }
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Handled dialog: " + (accept ? "accepted" : "dismissed") + ", text: " + dialogText);
    }
}
