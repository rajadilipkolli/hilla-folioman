package com.app.folioman.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.Testcontainers;
import org.testcontainers.selenium.BrowserWebDriverContainer;
import org.testcontainers.utility.DockerImageName;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainPageIT extends AbstractIntegrationTest {

    private WebDriver driver;

    static BrowserWebDriverContainer container =
            new BrowserWebDriverContainer(DockerImageName.parse("selenium/standalone-edge"));

    @BeforeAll
    void beforeAll() {
        Testcontainers.exposeHostPorts(port);
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
        driver = new RemoteWebDriver(container.getSeleniumAddress(), new EdgeOptions());
        driver.get("http://host.testcontainers.internal:" + port);
        assertThat(driver.getTitle() != null && !driver.getTitle().isEmpty())
                .as("Main page should load and have a title")
                .isTrue();

        // Click navigation links and verify page changes
        driver.findElement(By.linkText("Import Mutual Funds")).click();
        assertThat(driver.getPageSource().contains("Import Mutual Funds"))
                .as("Should navigate to Import Mutual Funds page")
                .isTrue();
        driver.navigate().back();
        driver.findElement(By.linkText("UserPortfolio")).click();
        assertThat(driver.getPageSource().contains("UserPortfolio"))
                .as("Should navigate to UserPortfolio page")
                .isTrue();
        driver.navigate().back();
        driver.findElement(By.linkText("ReBalance Calculator")).click();
        assertThat(driver.getPageSource().contains("ReBalance Calculator"))
                .as("Should navigate to ReBalance Calculator page")
                .isTrue();
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
        assertThat(foundResults || pageSource.contains("No schemes found"))
                .as("Search should return results or show 'No schemes found' message")
                .isTrue();

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
