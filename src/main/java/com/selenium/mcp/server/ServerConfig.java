package com.selenium.mcp.server;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the Selenium MCP server.
 */
public class ServerConfig {
    // Browser configuration
    private String browserName = "chrome";
    private boolean headless = false;
    private String executablePath;
    private String userDataDir;
    private boolean isolated = false;
    
    // Viewport configuration
    private int viewportWidth = 1280;
    private int viewportHeight = 720;
    
    // Capabilities
    private Set<String> capabilities = new HashSet<>();
    
    // Output configuration
    private String outputDir;

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getUserDataDir() {
        return userDataDir;
    }

    public void setUserDataDir(String userDataDir) {
        this.userDataDir = userDataDir;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(int viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(int viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    public void addCapability(String capability) {
        this.capabilities.add(capability);
    }

    public boolean hasCapability(String capability) {
        return this.capabilities.contains(capability);
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
