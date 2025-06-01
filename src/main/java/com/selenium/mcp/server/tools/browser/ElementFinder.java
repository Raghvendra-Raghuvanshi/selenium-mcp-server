package com.selenium.mcp.server.tools.browser;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class for enhanced element detection with multiple fallback strategies.
 */
public class ElementFinder {
    private static final Logger logger = LoggerFactory.getLogger(ElementFinder.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern VISIBLE_TEXT_PATTERN = Pattern.compile("^text=(.+)$");
    private static final Pattern PARTIAL_TEXT_PATTERN = Pattern.compile("^partial-text=(.+)$");
    private static final Pattern LABEL_PATTERN = Pattern.compile("^label=(.+)$");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^placeholder=(.+)$");
    private static final Pattern ROLE_PATTERN = Pattern.compile("^role=(.+)$");
    private static final Pattern TEST_ID_PATTERN = Pattern.compile("^test-id=(.+)$");

    /**
     * Find an element using multiple strategies.
     * @param driver The WebDriver instance
     * @param elementRef The element reference or description
     * @return The found WebElement
     * @throws NoSuchElementException if the element cannot be found
     */
    public static WebElement findElement(WebDriver driver, String elementRef) {
        logger.debug("Finding element with reference: {}", elementRef);
        
        // Try different strategies in order
        List<ElementFindingStrategy> strategies = Arrays.asList(
            ElementFinder::findByElementRef,
            ElementFinder::findByVisibleText,
            ElementFinder::findByPartialText,
            ElementFinder::findByLabel,
            ElementFinder::findByPlaceholder,
            ElementFinder::findByRole,
            ElementFinder::findByTestId,
            ElementFinder::findByCommonSelectors
        );

        for (ElementFindingStrategy strategy : strategies) {
            try {
                WebElement element = strategy.find(driver, elementRef);
                if (element != null && isElementVisible(element)) {
                    logger.debug("Found element using strategy: {}", strategy.getClass().getSimpleName());
                    return element;
                }
            } catch (Exception e) {
                logger.debug("Strategy {} failed: {}", strategy.getClass().getSimpleName(), e.getMessage());
            }
        }

        throw new NoSuchElementException("Could not find element with reference: " + elementRef);
    }

    /**
     * Find elements using multiple strategies.
     * @param driver The WebDriver instance
     * @param elementRef The element reference or description
     * @return List of found WebElements
     */
    public static List<WebElement> findElements(WebDriver driver, String elementRef) {
        logger.debug("Finding elements with reference: {}", elementRef);
        List<WebElement> elements = new ArrayList<>();

        // Try different strategies
        try {
            elements.addAll(findByElementRefMultiple(driver, elementRef));
        } catch (Exception e) {
            logger.debug("Element ref strategy failed: {}", e.getMessage());
        }

        try {
            elements.addAll(findByVisibleTextMultiple(driver, elementRef));
        } catch (Exception e) {
            logger.debug("Visible text strategy failed: {}", e.getMessage());
        }

        // Add more strategies as needed...

        return elements;
    }

    private static WebElement findByElementRef(WebDriver driver, String elementRef) {
        if (elementRef.startsWith("element-")) {
            String indexStr = elementRef.substring("element-".length());
            try {
                int index = Integer.parseInt(indexStr);
                List<WebElement> allElements = driver.findElements(By.xpath("//*"));
                if (index >= 0 && index < allElements.size()) {
                    return allElements.get(index);
                }
            } catch (NumberFormatException e) {
                // Fall through to other strategies
            }
        }

        // Try common selectors
        try {
            return driver.findElement(By.id(elementRef));
        } catch (Exception e1) {
            try {
                return driver.findElement(By.cssSelector("[data-ref='" + elementRef + "']"));
            } catch (Exception e2) {
                try {
                    return driver.findElement(By.xpath("//*[@data-ref='" + elementRef + "']"));
                } catch (Exception e3) {
                    // Fall through to other strategies
                }
            }
        }
        return null;
    }

    private static List<WebElement> findByElementRefMultiple(WebDriver driver, String elementRef) {
        List<WebElement> elements = new ArrayList<>();
        if (elementRef.startsWith("element-")) {
            String indexStr = elementRef.substring("element-".length());
            try {
                int index = Integer.parseInt(indexStr);
                List<WebElement> allElements = driver.findElements(By.xpath("//*"));
                if (index >= 0 && index < allElements.size()) {
                    elements.add(allElements.get(index));
                }
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        try {
            elements.addAll(driver.findElements(By.cssSelector("[data-ref='" + elementRef + "']")));
        } catch (Exception e) {
            // Fall through
        }

        try {
            elements.addAll(driver.findElements(By.xpath("//*[@data-ref='" + elementRef + "']")));
        } catch (Exception e) {
            // Fall through
        }

        return elements;
    }

    private static WebElement findByVisibleText(WebDriver driver, String elementRef) {
        var matcher = VISIBLE_TEXT_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String text = matcher.group(1);
            return driver.findElement(By.xpath("//*[text()='" + text + "']"));
        }
        return null;
    }

    private static List<WebElement> findByVisibleTextMultiple(WebDriver driver, String elementRef) {
        List<WebElement> elements = new ArrayList<>();
        var matcher = VISIBLE_TEXT_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String text = matcher.group(1);
            elements.addAll(driver.findElements(By.xpath("//*[text()='" + text + "']")));
        }
        return elements;
    }

    private static WebElement findByPartialText(WebDriver driver, String elementRef) {
        var matcher = PARTIAL_TEXT_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String text = matcher.group(1);
            return driver.findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
        }
        return null;
    }

    private static WebElement findByLabel(WebDriver driver, String elementRef) {
        var matcher = LABEL_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String label = matcher.group(1);
            return driver.findElement(By.xpath("//label[contains(text(),'" + label + "')]/following::input[1]"));
        }
        return null;
    }

    private static WebElement findByPlaceholder(WebDriver driver, String elementRef) {
        var matcher = PLACEHOLDER_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String placeholder = matcher.group(1);
            return driver.findElement(By.cssSelector("[placeholder='" + placeholder + "']"));
        }
        return null;
    }

    private static WebElement findByRole(WebDriver driver, String elementRef) {
        var matcher = ROLE_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String role = matcher.group(1);
            return driver.findElement(By.cssSelector("[role='" + role + "']"));
        }
        return null;
    }

    private static WebElement findByTestId(WebDriver driver, String elementRef) {
        var matcher = TEST_ID_PATTERN.matcher(elementRef);
        if (matcher.matches()) {
            String testId = matcher.group(1);
            return driver.findElement(By.cssSelector("[data-testid='" + testId + "']"));
        }
        return null;
    }

    private static WebElement findByCommonSelectors(WebDriver driver, String elementRef) {
        // Try common selectors in order of specificity
        String[] selectors = {
            "//button[contains(text(),'" + elementRef + "')]",
            "//a[contains(text(),'" + elementRef + "')]",
            "//input[@placeholder='" + elementRef + "']",
            "//label[contains(text(),'" + elementRef + "')]/following::input[1]",
            "//*[@aria-label='" + elementRef + "']",
            "//*[@title='" + elementRef + "']"
        };

        for (String selector : selectors) {
            try {
                WebElement element = driver.findElement(By.xpath(selector));
                if (element != null && isElementVisible(element)) {
                    return element;
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        return null;
    }

    private static boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    @FunctionalInterface
    private interface ElementFindingStrategy {
        WebElement find(WebDriver driver, String elementRef) throws Exception;
    }
} 