package com.app.folioman.ui.views;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.folioman.shared.AbstractIntegrationTest;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

class MainPageIT extends AbstractIntegrationTest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = WebDriverManager.chromedriver()
                .capabilities(options)
                .browserInDocker()
                .create();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void mainPageLoads() {
        driver.get("http://localhost:" + port + "/");
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
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String pageSource = driver.getPageSource();
        boolean foundResults = pageSource.contains("Fund") && !pageSource.contains("No schemes found");
        assertTrue(
                foundResults || pageSource.contains("No schemes found"),
                "Search should return results or show 'No schemes found' message");

        // Open scheme details dialog if results exist
        List<WebElement> schemeButtons = driver.findElements(By.cssSelector("button[theme='tertiary']"));
        if (!schemeButtons.isEmpty()) {
            schemeButtons.getFirst().click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            assertTrue(
                    driver.getPageSource().contains("NAV Value"),
                    "Scheme details dialog should open and show NAV Value");
            // Close dialog
            WebElement closeButton = driver.findElement(By.cssSelector("button[aria-label='Close dialog']"));
            closeButton.click();
        }
    }
}
