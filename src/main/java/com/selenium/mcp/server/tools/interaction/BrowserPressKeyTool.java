package com.selenium.mcp.server.tools.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.mcp.server.BrowserManager;
import com.selenium.mcp.server.tools.AbstractTool;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool to press a key on the keyboard.
 */
public class BrowserPressKeyTool extends AbstractTool {
    private static final Map<String, Keys> KEY_MAP = new HashMap<>();
    
    static {
        // Initialize key map
        for (Field field : Keys.class.getDeclaredFields()) {
            if (field.getType() == Keys.class) {
                try {
                    String keyName = field.getName();
                    Keys keyValue = (Keys) field.get(null);
                    KEY_MAP.put(keyName, keyValue);
                } catch (IllegalAccessException e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "browser_press_key";
    }

    @Override
    public String getTitle() {
        return "Press a key";
    }

    @Override
    public String getDescription() {
        return "Press a key on the keyboard";
    }

    @Override
    public JsonNode getParameterSchema(ObjectMapper objectMapper) {
        ObjectNode schema = createParameterSchema(objectMapper, "key");
        addStringParameter(schema, "key", "Name of the key to press or a character to generate, such as `ArrowLeft` or `a`", true);
        return schema;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void validateParameters(JsonNode params) throws Exception {
        if (!params.has("key") || params.get("key").asText().isEmpty()) {
            throw new IllegalArgumentException("Key parameter is required");
        }
    }

    @Override
    protected JsonNode executeImpl(JsonNode params, BrowserManager browserManager) throws Exception {
        WebDriver driver = browserManager.getDriver();
        
        String keyName = params.get("key").asText();
        
        logger.info("Pressing key: {}", keyName);
        
        // Convert key name to Selenium Keys
        Keys key = getKeyFromName(keyName);
        
        // Press the key
        Actions actions = new Actions(driver);
        if (key != null) {
            actions.sendKeys(key).perform();
        } else {
            // If not a special key, just send the character
            actions.sendKeys(keyName).perform();
        }
        
        // Wait a moment for any page changes
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        return createSimpleResult(objectMapper, "Pressed key: " + keyName);
    }
    
    private Keys getKeyFromName(String keyName) {
        // Check for special key names
        switch (keyName.toLowerCase()) {
            case "enter":
            case "return":
                return Keys.ENTER;
            case "tab":
                return Keys.TAB;
            case "space":
                return Keys.SPACE;
            case "backspace":
                return Keys.BACK_SPACE;
            case "delete":
                return Keys.DELETE;
            case "escape":
            case "esc":
                return Keys.ESCAPE;
            case "arrowleft":
            case "left":
                return Keys.ARROW_LEFT;
            case "arrowright":
            case "right":
                return Keys.ARROW_RIGHT;
            case "arrowup":
            case "up":
                return Keys.ARROW_UP;
            case "arrowdown":
            case "down":
                return Keys.ARROW_DOWN;
            case "home":
                return Keys.HOME;
            case "end":
                return Keys.END;
            case "pageup":
                return Keys.PAGE_UP;
            case "pagedown":
                return Keys.PAGE_DOWN;
            case "f1":
                return Keys.F1;
            case "f2":
                return Keys.F2;
            case "f3":
                return Keys.F3;
            case "f4":
                return Keys.F4;
            case "f5":
                return Keys.F5;
            case "f6":
                return Keys.F6;
            case "f7":
                return Keys.F7;
            case "f8":
                return Keys.F8;
            case "f9":
                return Keys.F9;
            case "f10":
                return Keys.F10;
            case "f11":
                return Keys.F11;
            case "f12":
                return Keys.F12;
            default:
                // Check if it's a key in the KEY_MAP
                return KEY_MAP.get(keyName);
        }
    }
}
