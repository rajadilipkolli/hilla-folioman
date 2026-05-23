package com.app.folioman.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.auth.domain.RoleEntity;
import com.app.folioman.auth.domain.RoleRepository;
import com.app.folioman.auth.domain.UserRepository;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void mainPageLoads() {
        // Create test user if it doesn't exist
        if (userRepository.findByUsername("testuser").isEmpty()) {
            com.app.folioman.auth.domain.UserEntity user = new com.app.folioman.auth.domain.UserEntity();
            user.setUsername("testuser");
            user.setEmail("test@test.com");
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setEnabled(true);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            RoleEntity userRole = roleRepository
                    .findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Required role USER not found"));
            user.getRoles().add(userRole);
            userRepository.save(user);
        }

        driver = new RemoteWebDriver(container.getSeleniumAddress(), new EdgeOptions());

        // Go to login page first because / requires login
        driver.get("http://host.testcontainers.internal:" + port + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Wait until navigation to /login completes and let the React auth state settle
        // to prevent rapid re-rendering of the login form which causes StaleElementReferenceExceptions.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Wait until the login form components are present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-text-field")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-password-field")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-button")));

        // Use the original Javascript injection to interact with Vaadin Web Components.
        // A fluent wait robustly retries the entire block to handle any asynchronous
        // DOM re-renders that would otherwise cause a StaleElementReferenceException.
        wait.until(d -> {
            try {
                WebElement usernameField = d.findElement(By.tagName("vaadin-text-field"));
                js.executeScript(
                        "arguments[0].value = 'testuser'; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                        usernameField);

                WebElement passwordField = d.findElement(By.tagName("vaadin-password-field"));
                js.executeScript(
                        "arguments[0].value = 'password123'; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                        passwordField);

                WebElement loginButton = d.findElement(By.tagName("vaadin-button"));
                js.executeScript("arguments[0].click();", loginButton);
                return true;
            } catch (StaleElementReferenceException | ElementNotInteractableException e) {
                return false; // Retry until success or timeout
            }
        }); // Wait until navigation to userPortfolio
        wait.until(ExpectedConditions.urlContains("/userPortfolio"));

        // Now navigate to main page
        driver.get("http://host.testcontainers.internal:" + port);

        assertThat(driver.getTitle() != null && !driver.getTitle().isEmpty())
                .as("Main page should load and have a title")
                .isTrue();

        // Click navigation links and verify page changes
        WebElement importLink = driver.findElement(By.linkText("Import Mutual Funds"));
        js.executeScript("arguments[0].click();", importLink);
        assertThat(driver.getPageSource().contains("Import Mutual Funds"))
                .as("Should navigate to Import Mutual Funds page")
                .isTrue();
        driver.navigate().back();

        WebElement userPortfolioLink = driver.findElement(By.linkText("UserPortfolio"));
        js.executeScript("arguments[0].click();", userPortfolioLink);
        assertThat(driver.getPageSource().contains("UserPortfolio"))
                .as("Should navigate to UserPortfolio page")
                .isTrue();
        driver.navigate().back();

        WebElement rebalanceLink = driver.findElement(By.linkText("ReBalance Calculator"));
        js.executeScript("arguments[0].click();", rebalanceLink);
        assertThat(driver.getPageSource().contains("ReBalance Calculator"))
                .as("Should navigate to ReBalance Calculator page")
                .isTrue();
        driver.navigate().back();

        // Interact with search field
        WebElement searchField =
                driver.findElement(By.cssSelector("input[placeholder='Search mutual fund schemes...']"));
        searchField.sendKeys("Fund");

        // Wait for either search results or the "No schemes found" message
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
            WebElement firstSchemeButton = schemeButtons.getFirst();
            js.executeScript("arguments[0].click();", firstSchemeButton);

            // Wait for dialog to open and verify its content
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-dialog-overlay")));
            WebElement closeButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Close dialog']")));
            js.executeScript("arguments[0].click();", closeButton);
        }
    }
}
