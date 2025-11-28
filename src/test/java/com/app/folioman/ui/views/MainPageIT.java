package com.app.folioman.ui.views;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.testcontainers.Testcontainers;
import org.testcontainers.selenium.BrowserWebDriverContainer;
import org.testcontainers.utility.DockerImageName;

class MainPageIT extends AbstractIntegrationTest {

    private WebDriver driver;

    static BrowserWebDriverContainer container =
            new BrowserWebDriverContainer(DockerImageName.parse("selenium/standalone-edge"));

    @BeforeAll
    static void beforeAll(@Autowired Environment environment) {
        Testcontainers.exposeHostPorts(environment.getProperty("local.server.port", Integer.class));
        container.start();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void mainPageLoads() {
        driver = new RemoteWebDriver(container.getSeleniumAddress(), new ChromeOptions());
        driver.get("http://host.testcontainers.internal:" + port);
        assertTrue(driver.getTitle() != null && !driver.getTitle().isEmpty(), "Main page should load and have a title");

        // Click navigation links and verify page changes
        driver.findElement(By.linkText("Import Mutual Funds")).click();
        assertTrue(
                driver.getPageSource().contains("Import Mutual Funds"), "Should navigate to Import Mutual Funds page");
        driver.navigate().back();
        driver.findElement(By.linkText("UserPortfolio")).click();
        assertTrue(driver.getPageSource().contains("UserPortfolio"), "Should navigate to UserPortfolio page");
        driver.navigate().back();
        driver.findElement(By.linkText("ReBalance Calculator")).click();
        assertTrue(
                driver.getPageSource().contains("ReBalance Calculator"),
                "Should navigate to ReBalance Calculator page");
        driver.navigate().back();

        // Interact with search field
        WebElement searchField =
                driver.findElement(By.cssSelector("input[placeholder='Search mutual fund schemes...']"));
        searchField.sendKeys("Fund");

        // Wait for either search results or the "No schemes found" message
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> {
            // Check for either search results containing "Fund" or the "No schemes found" message
            return ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Fund')]")),
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[contains(text(), 'No schemes found')]")))
                    .apply(driver);
        });

        String pageSource = driver.getPageSource();
        boolean foundResults = pageSource.contains("Fund") && !pageSource.contains("No schemes found");
        assertTrue(
                foundResults || pageSource.contains("No schemes found"),
                "Search should return results or show 'No schemes found' message");

        // Open scheme details dialog if results exist
        List<WebElement> schemeButtons = driver.findElements(By.cssSelector("button[theme='tertiary']"));
        if (!schemeButtons.isEmpty()) {
            schemeButtons.getFirst().click();
            var dialogWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            dialogWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'NAV Value')]")));
            // Close dialog
            WebElement closeButton = dialogWait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Close dialog']")));
            closeButton.click();
        }
    }
}
