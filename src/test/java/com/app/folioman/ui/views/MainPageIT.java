package com.app.folioman.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.testcontainers.Testcontainers;
import org.testcontainers.selenium.BrowserWebDriverContainer;
import org.testcontainers.utility.DockerImageName;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class MainPageIT extends AbstractIntegrationTest {

    private WebDriver driver;

    static BrowserWebDriverContainer container =
            new BrowserWebDriverContainer(DockerImageName.parse("selenium/standalone-edge"));

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void mainPageLoads() {
        // Create test user if it doesn't exist
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update(
                    "DELETE FROM portfolio.user_roles WHERE user_id IN (SELECT id FROM portfolio.users WHERE username = 'mainpage_user')");
            jdbcTemplate.update(
                    "DELETE FROM portfolio.refresh_tokens WHERE user_id IN (SELECT id FROM portfolio.users WHERE username = 'mainpage_user')");
            jdbcTemplate.update("DELETE FROM portfolio.users WHERE username = 'mainpage_user'");

            Long roleId;
            try {
                roleId = jdbcTemplate.queryForObject("SELECT id FROM portfolio.roles WHERE name = 'USER'", Long.class);
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                roleId = jdbcTemplate.queryForObject("SELECT nextval('portfolio.roles_seq')", Long.class);
                jdbcTemplate.update(
                        "INSERT INTO portfolio.roles (id, name, created_at, version) VALUES (?, 'USER', CURRENT_TIMESTAMP, 0)",
                        roleId);
            }

            String passwordHash = passwordEncoder.encode("password123");
            jdbcTemplate.update(
                    "INSERT INTO portfolio.users (id, username, email, password_hash, enabled, account_locked, failed_login_attempts, created_at, updated_at, version) "
                            + "VALUES (nextval('portfolio.users_seq'), 'mainpage_user', 'mainpage_user@test.com', ?, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                    passwordHash);

            jdbcTemplate.update(
                    "INSERT INTO portfolio.user_roles (user_id, role_id) "
                            + "SELECT u.id, ? FROM portfolio.users u "
                            + "WHERE u.username = 'mainpage_user'",
                    roleId);
        });

        if (!container.isRunning()) {
            Testcontainers.exposeHostPorts(port);
            container.start();
        }

        driver = new RemoteWebDriver(container.getSeleniumAddress(), new EdgeOptions());
        String baseUrl = "http://host.testcontainers.internal:" + port;

        // Go to the site origin first
        driver.get(baseUrl + "/login");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Use fetch inside the browser to login and set state, bypassing flaky Vaadin Web Components
        Boolean loginSuccess = wait.until(d -> (Boolean)
                js.executeAsyncScript("const callback = arguments[arguments.length - 1];" + "fetch('/api/auth/login', {"
                        + "  method: 'POST',"
                        + "  headers: {'Content-Type': 'application/json'},"
                        + "  credentials: 'include',"
                        + "  body: JSON.stringify({username: 'mainpage_user', password: 'password123'})"
                        + "})"
                        + ".then(res => { if (!res.ok) throw new Error('not ok'); return res.json(); })"
                        + ".then(data => {"
                        + "  localStorage.setItem('accessToken', data.accessToken);"
                        + "  callback(true);"
                        + "})"
                        + ".catch(err => callback(false));"));

        assertThat(loginSuccess).isTrue();

        // Now navigate straight to the authenticated area
        driver.get(baseUrl + "/");

        wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

        assertThat(driver.getTitle() != null && !driver.getTitle().isEmpty())
                .as("Main page should load and have a title")
                .isTrue();

        // Assert we are on the User Profile page by looking for the actual heading or a specific element on the page
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            extendedWait.until((org.openqa.selenium.WebDriver webDriver) -> {
                try {
                    WebElement el = webDriver.findElement(
                            By.xpath("//h2[contains(., 'mainpage_user')] | //vaadin-login-overlay"));
                    if (el.getTagName().equalsIgnoreCase("vaadin-login-overlay")) {
                        String token = (String) js.executeScript("return localStorage.getItem('accessToken');");
                        throw new RuntimeException("Test redirected to login page! Token in localStorage: " + token);
                    }
                    return true;
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    try {
                        WebElement offlineIndicator =
                                webDriver.findElement(By.xpath("//vaadin-connection-indicator[@offline]"));
                        if (offlineIndicator != null) {
                            System.out.println("Connection lost detected by Vaadin. Refreshing page...");
                            webDriver.navigate().refresh();
                            // Give it a small pause to reload
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                            }
                        }
                    } catch (org.openqa.selenium.NoSuchElementException ignored) {
                    }
                    return false;
                }
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            String token = (String) js.executeScript("return localStorage.getItem('accessToken');");
            System.err.println("Access token in localStorage: " + token);
            org.openqa.selenium.logging.LogEntries logEntries =
                    driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
            for (org.openqa.selenium.logging.LogEntry entry : logEntries) {
                System.err.println(
                        new java.util.Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            }
            System.err.println("Page Source on Timeout: " + driver.getPageSource());
            throw e;
        }

        // Click navigation links and verify page changes
        WebElement importLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(., 'Import Mutual Funds')]")));
        js.executeScript("arguments[0].click();", importLink);
        wait.until(ExpectedConditions.urlContains("/importmutualfunds"));
        assertThat(driver.getCurrentUrl().contains("/importmutualfunds"))
                .as("Should navigate to Import Mutual Funds page")
                .isTrue();
        driver.navigate().back();

        WebElement userPortfolioLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(., 'UserPortfolio')]")));
        js.executeScript("arguments[0].click();", userPortfolioLink);
        wait.until(ExpectedConditions.urlContains("/userPortfolio"));
        assertThat(driver.getCurrentUrl().contains("/userPortfolio"))
                .as("Should navigate to UserPortfolio page")
                .isTrue();
        driver.navigate().back();

        WebElement rebalanceLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(., 'ReBalance')]")));
        js.executeScript("arguments[0].click();", rebalanceLink);
        wait.until(ExpectedConditions.urlContains("/rebalance"));
        assertThat(driver.getCurrentUrl().contains("/rebalance"))
                .as("Should navigate to ReBalance Calculator page")
                .isTrue();
        driver.navigate().back();

        WebElement mfSchemesLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(., 'Mutual Fund Schemes')]")));
        js.executeScript("arguments[0].click();", mfSchemesLink);
        wait.until(ExpectedConditions.urlContains("/mfschemes"));
        assertThat(driver.getCurrentUrl().contains("/mfschemes"))
                .as("Should navigate to Mutual Fund Schemes page")
                .isTrue();

        // Interact with search field
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder='Search mutual fund schemes...']")));
        searchField.sendKeys("Fund");

        // Wait for either search results or the "No schemes found" message
        wait.until(d -> {
            // Check for either search results containing "Fund" or the "No schemes found" message
            return ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Fund')]")),
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[contains(text(), 'No schemes found')]")))
                    .apply(d);
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
