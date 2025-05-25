package com.selenium.mcp.server;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages browser instances for the MCP server.
 */
public class BrowserManager {
    private static final Logger logger = LoggerFactory.getLogger(BrowserManager.class);
    private final ServerConfig config;
    private WebDriver driver;
    private List<String> openTabs = new ArrayList<>();
    private int currentTabIndex = 0;

    public BrowserManager(ServerConfig config) {
        this.config = config;
    }

    /**
     * Get the WebDriver instance, creating it if necessary.
     */
    public synchronized WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver();

            // Set viewport size
            driver.manage().window().setSize(
                    new Dimension(config.getViewportWidth(), config.getViewportHeight())
            );

            // Initialize tabs list
            openTabs.add(driver.getWindowHandle());
        }
        return driver;
    }

    /**
     * Create a new WebDriver instance based on the configuration.
     */
    private WebDriver createDriver() {
        String browserName = config.getBrowserName().toLowerCase();
        boolean headless = config.isHeadless();
        String userDataDir = config.getUserDataDir();
        String executablePath = config.getExecutablePath();

        logger.info("Creating {} WebDriver (headless: {})", browserName, headless);

        switch (browserName) {
            case "chrome":
                return createChromeDriver(headless, userDataDir, executablePath);
            case "firefox":
                return createFirefoxDriver(headless, userDataDir, executablePath);
            case "edge":
                return createEdgeDriver(headless, userDataDir, executablePath);
            case "safari":
                return createSafariDriver(executablePath);
            default:
                logger.warn("Unknown browser: {}. Using Chrome instead.", browserName);
                return createChromeDriver(headless, userDataDir, executablePath);
        }
    }

    private WebDriver createChromeDriver(boolean headless, String userDataDir, String executablePath) {
        if (executablePath == null) {
            WebDriverManager.chromedriver().setup();
        } else {
            System.setProperty("webdriver.chrome.driver", executablePath);
        }

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }

        if (userDataDir != null) {
            options.addArguments("--user-data-dir=" + userDataDir);
        } else if (!config.isIsolated()) {
            // Use default user data directory if not isolated
            File defaultUserDataDir = getDefaultUserDataDir("chrome");
            options.addArguments("--user-data-dir=" + defaultUserDataDir.getAbsolutePath());
        }

        // Add common options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return new ChromeDriver(options);
    }

    private WebDriver createFirefoxDriver(boolean headless, String userDataDir, String executablePath) {
        if (executablePath == null) {
            WebDriverManager.firefoxdriver().setup();
        } else {
            System.setProperty("webdriver.gecko.driver", executablePath);
        }

        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }

        if (userDataDir != null || !config.isIsolated()) {
            // Firefox uses a profile instead of user-data-dir
            if (userDataDir == null) {
                userDataDir = getDefaultUserDataDir("firefox").getAbsolutePath();
            }
            options.addArguments("-profile");
            options.addArguments(userDataDir);
        }

        return new FirefoxDriver(options);
    }

    private WebDriver createEdgeDriver(boolean headless, String userDataDir, String executablePath) {
        if (executablePath == null) {
            WebDriverManager.edgedriver().setup();
        } else {
            System.setProperty("webdriver.edge.driver", executablePath);
        }

        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }

        if (userDataDir != null) {
            options.addArguments("--user-data-dir=" + userDataDir);
        } else if (!config.isIsolated()) {
            // Use default user data directory if not isolated
            File defaultUserDataDir = getDefaultUserDataDir("edge");
            options.addArguments("--user-data-dir=" + defaultUserDataDir.getAbsolutePath());
        }

        // Add common options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return new EdgeDriver(options);
    }

    private WebDriver createSafariDriver(String executablePath) {
        if (executablePath != null) {
            System.setProperty("webdriver.safari.driver", executablePath);
        }

        SafariOptions options = new SafariOptions();
        // Safari doesn't support headless mode or user data directory

        return new SafariDriver(options);
    }

    private File getDefaultUserDataDir(String browser) {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String dirName = "selenium-mcp-" + browser + "-profile";

        if (os.contains("win")) {
            return new File(userHome + "\\AppData\\Local\\selenium-mcp\\" + dirName);
        } else if (os.contains("mac")) {
            return new File(userHome + "/Library/Caches/selenium-mcp/" + dirName);
        } else {
            return new File(userHome + "/.cache/selenium-mcp/" + dirName);
        }
    }

    /**
     * Get the current tab index.
     */
    public int getCurrentTabIndex() {
        return currentTabIndex;
    }

    /**
     * Get the list of open tabs.
     */
    public List<String> getOpenTabs() {
        return openTabs;
    }

    /**
     * Switch to a tab by index.
     */
    public void switchToTab(int index) {
        if (index < 0 || index >= openTabs.size()) {
            throw new IllegalArgumentException("Invalid tab index: " + index);
        }

        driver.switchTo().window(openTabs.get(index));
        currentTabIndex = index;
    }

    /**
     * Open a new tab.
     */
    public void openNewTab(String url) {
        // Selenium doesn't have a direct way to open a new tab
        // We'll use JavaScript to open a new window and then switch to it
        String currentHandle = driver.getWindowHandle();
        String script = "window.open(arguments[0], '_blank');";
        ((JavascriptExecutor) driver).executeScript(script, url != null ? url : "about:blank");

        // Find the new window handle
        for (String handle : driver.getWindowHandles()) {
            if (!openTabs.contains(handle)) {
                openTabs.add(handle);
                driver.switchTo().window(handle);
                currentTabIndex = openTabs.size() - 1;
                return;
            }
        }

        // If we didn't find a new handle, switch back to the original
        driver.switchTo().window(currentHandle);
    }

    /**
     * Close a tab by index.
     */
    public void closeTab(int index) {
        if (index < 0 || index >= openTabs.size()) {
            throw new IllegalArgumentException("Invalid tab index: " + index);
        }

        // If we're closing the current tab, switch to another tab first
        if (index == currentTabIndex) {
            // Switch to the tab we're closing
            driver.switchTo().window(openTabs.get(index));

            // Close it
            driver.close();

            // Remove it from our list
            openTabs.remove(index);

            // Switch to another tab if there are any left
            if (!openTabs.isEmpty()) {
                int newIndex = Math.min(index, openTabs.size() - 1);
                driver.switchTo().window(openTabs.get(newIndex));
                currentTabIndex = newIndex;
            } else {
                // No tabs left, create a new one
                driver = createDriver();
                openTabs.add(driver.getWindowHandle());
                currentTabIndex = 0;
            }
        } else {
            // Save current tab
            String currentHandle = openTabs.get(currentTabIndex);

            // Switch to the tab we're closing
            driver.switchTo().window(openTabs.get(index));

            // Close it
            driver.close();

            // Remove it from our list
            openTabs.remove(index);

            // Update current tab index if necessary
            if (index < currentTabIndex) {
                currentTabIndex--;
            }

            // Switch back to the original tab
            driver.switchTo().window(currentHandle);
        }
    }

    /**
     * Close all browser instances.
     */
    public synchronized void close() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Error closing WebDriver", e);
            } finally {
                driver = null;
                openTabs.clear();
                currentTabIndex = 0;
            }
        }
    }
}
