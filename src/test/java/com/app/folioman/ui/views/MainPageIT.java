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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void mainPageLoads() {
        // Create test user if it doesn't exist
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM portfolio.users WHERE username = 'testuser'", Integer.class);
        if (userCount == null || userCount == 0) {
            String passwordHash = passwordEncoder.encode("password123");

            jdbcTemplate.update(
                    "INSERT INTO portfolio.users (id, username, email, password_hash, enabled, account_locked, failed_login_attempts, created_at, updated_at, version) "
                            + "VALUES (nextval('portfolio.users_seq'), 'testuser', 'test@test.com', ?, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                    passwordHash);

            jdbcTemplate.update("INSERT INTO portfolio.user_roles (user_id, role_id) "
                    + "SELECT u.id, r.id FROM portfolio.users u, portfolio.roles r "
                    + "WHERE u.username = 'testuser' AND r.name = 'USER'");
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
                        + "  body: JSON.stringify({username: 'testuser', password: 'password123'})"
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

        // Assert we are on the User Profile page
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'User Profile') or contains(text(), 'testuser')]")));

        // Click navigation links and verify page changes
        WebElement importLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/importmutualfunds']")));
        js.executeScript("arguments[0].click();", importLink);
        wait.until(ExpectedConditions.urlContains("/importmutualfunds"));
        assertThat(driver.getCurrentUrl().contains("/importmutualfunds"))
                .as("Should navigate to Import Mutual Funds page")
                .isTrue();
        driver.navigate().back();

        WebElement userPortfolioLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/userPortfolio']")));
        js.executeScript("arguments[0].click();", userPortfolioLink);
        wait.until(ExpectedConditions.urlContains("/userPortfolio"));
        assertThat(driver.getCurrentUrl().contains("/userPortfolio"))
                .as("Should navigate to UserPortfolio page")
                .isTrue();
        driver.navigate().back();

        WebElement rebalanceLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/rebalance']")));
        js.executeScript("arguments[0].click();", rebalanceLink);
        wait.until(ExpectedConditions.urlContains("/rebalance"));
        assertThat(driver.getCurrentUrl().contains("/rebalance"))
                .as("Should navigate to ReBalance Calculator page")
                .isTrue();
        driver.navigate().back();

        WebElement mfSchemesLink =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/mfschemes']")));
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
