package com.yourcompany;

/**
 * @author Neil Manvar
 */

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;

import com.yourcompany.utils.RetryRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;


/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 * @author Neil Manvar
 */
@Listeners({SauceOnDemandTestListener.class})
public class SampleSauceTest implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

    public String username = System.getenv("SAUCE_USER_NAME") != null ? System.getenv("SAUCE_USER_NAME") : System.getenv("SAUCE_USERNAME");
    public String accesskey = System.getenv("SAUCE_API_KEY") != null ? System.getenv("SAUCE_API_KEY") : System.getenv("SAUCE_ACCESS_KEY");

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(username, accesskey);

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return
     */
    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{
                new Object[]{"internet explorer", "11", "Windows 8.1"},
                new Object[]{"chrome", "41", "Windows XP"},
                new Object[]{"safari", "7", "OS X 10.9"},
                new Object[]{"firefox", "36", "Windows 7"},
                new Object[]{"firefox", "35", "Windows 7"}

        };
    }


    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the browser,
     * version and os parameters, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @return
     * @throws MalformedURLException if an error occurs parsing the url
     */
    private WebDriver createDriver(String browser, String version, String os, String methodName) throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        if (version != null) {
            capabilities.setCapability(CapabilityType.VERSION, version);
        }
        capabilities.setCapability(CapabilityType.PLATFORM, os);

        String jobName = methodName + '_' + os + '_' + browser + '_' + version;
        capabilities.setCapability("name", jobName);
        webDriver.set(new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities));
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);

        String message = String.format("SauceOnDemandSessionID=%1$s job-name=%2$s", id, jobName);
        System.out.println(message);

        return webDriver.get();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        webDriver.get().quit();
    }

    /**
     * Runs a simple test verifying the title of the wikipedia.org home page.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param Method Represents the method, used for getting the name of the test/method
     * @throws Exception if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers", retryAnalyzer = RetryRule.class)
    public void pandoraTitleTest(String browser, String version, String os, Method method) throws Exception {
        WebDriver driver = createDriver(browser, version, os, method.getName());
        driver.get("http://www.pandora.com/");

        assertEquals(driver.getTitle(), "Pandora Internet Radio - Listen to Free Music You'll Love");
    }

    /**
     * Runs a simple test verifying the login form and login button of the pandora.com home page.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param Method Represents the method, used for getting the name of the test/method
     * @throws Exception if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers", retryAnalyzer = RetryRule.class)
    public void welcomeScreenLaunchTest(String browser, String version, String os, Method method) throws Exception {
        WebDriver driver = createDriver(browser, version, os, method.getName());
        driver.get("http://www.pandora.com/");

        WebDriverWait wait = new WebDriverWait(driver, 10);

        // click signin button
        WebElement signInButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".message.signin a")));
        Thread.sleep(5000);
        signInButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".loginForm [name=email]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".loginForm [name=password]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".message.register a")));
    }

    /**
     * Types in coldplay in the pandora search box, clicks Coldplay, and verifies Coldplay playlist is playing
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param Method Represents the method, used for getting the name of the test/method
     * @throws Exception if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers", retryAnalyzer = RetryRule.class)
    public void coldplayTest(String browser, String version, String os, Method method) throws Exception {
        WebDriver driver = createDriver(browser, version, os, method.getName());
        driver.get("http://www.pandora.com/");

        WebDriverWait wait = new WebDriverWait(driver, 10);

        // click signin button
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#welcomeSearch .searchInput")));
        Thread.sleep(3000);
        searchBox.sendKeys("coldplay");

        WebElement coldplaySuggestion = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchPopupWelcomePosition']//span[contains(text(), 'Coldplay')]")));
        coldplaySuggestion.click();

        WebElement topMenu = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stationChangeSelectorNoMenu")));
        Assert.assertTrue(topMenu.getText().contains("Coldplay"), "Text not found!");
    }

    /**
     * @return the {@link WebDriver} for the current thread
     */
    public WebDriver getWebDriver() {
        System.out.println("WebDriver" + webDriver.get());
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /**
     *
     * @return the {@link SauceOnDemandAuthentication} instance containing the Sauce username/access key
     */
    @Override
    public SauceOnDemandAuthentication getAuthentication() {
        return authentication;
    }
}

