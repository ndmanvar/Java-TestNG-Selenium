package com.yourcompany;

/**
 * @author Neil Manvar
 */

// import Sauce TestNG helper libraries
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;

// import selenium libraries
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

// import testng libraries
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

// import page objects
import com.yourcompany.Pages.*;


/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 * @author Neil Manvar
 */
@Listeners({SauceOnDemandTestListener.class})
public class SampleSauceTest implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

    // Sauce username
    public String username = System.getenv("SAUCE_USERNAME");

    // Sauce access key
    public String accesskey = System.getenv("SAUCE_ACCESS_KEY");

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
                new Object[]{"firefox", "35", "Windows 7"}
        };
    }
    
    /**
     * @return the {@link WebDriver} for the current thread
     */
    public WebDriver getWebDriver() {
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

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the browser,
     * version and os parameters, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param methodName Represents the name of the test case that will be used to identify the test on Sauce.
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

        capabilities.setCapability("name", methodName);
        webDriver.set(new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities));
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);

        String message = String.format("SauceOnDemandSessionID=%1$s job-name=%2$s", id, methodName);
        System.out.println(message);

        return webDriver.get();
    }
    
    /**
     * Method that gets invoked after test.
     * Closes the browser
     *
     * @param testMethod
     * @return
     */
    @AfterMethod
    public void tearDown() throws Exception {
        webDriver.get().quit();
    }

    /**
     * Runs a simple test verifying inputField can typed into.
     *
     * @param browser
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param method Represents the name of the test.
     * @throws Exception if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers")
    public void verifyEmailInputTest(String browser, String version, String os, Method method) throws Exception {
        // all variable declarations should be at top of method
        WebDriver driver = createDriver(browser, version, os, method.getName());  // create the driver / browser instance
        String emailInputText = "abc@gmail.com";
        
        
        // actions and interaction with page should go here...
        driver.get("https://saucelabs.com/test/guinea-pig");
        
        // use page object to interact with elements
        //  will have public methods represent the services that the page offers
        //  internals of app (selectors / locators) should be in Page Object, and can be accessed via "service"
        GuineaPig page = new GuineaPig(driver);
        
        // expose "services" in page object that allow test to interact with page being tested
        //  in this example, service would be fillOutEmailInput that interacts with emailInputField by typing into it
        //  getEmail input is also a service, as we are using it to retrieve text in the emailInputField
        page.fillOutEmailInput(emailInputText);
        
        // assertions should be part of test and not part of Page object
        // each test should be verifying one piece of functionality (atomic testing)
        assertEquals(page.getEmailInput(), emailInputText);
    }

}
