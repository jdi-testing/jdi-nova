package com.jdiai.jswraper.driver;

import com.jdiai.jsdriver.JDINovaException;
import com.jdiai.jswraper.driver.download.DownloadDriverSettings;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariOptions;

import java.util.function.Supplier;

import static com.jdiai.jswraper.driver.DriverTypes.*;
import static com.jdiai.jswraper.driver.GetDriverUtilities.getLocalDriver;
import static com.jdiai.jswraper.driver.JDIDriver.*;
import static java.lang.Runtime.getRuntime;

public class DriverManager {
    static DriverTypes BROWSER = CHROME;
    static Supplier<WebDriver> CUSTOM_DRIVER = null;

    public static void useDriver(DriverTypes driver) {
        BROWSER = driver;
    }

    public static void useDriver(Supplier<WebDriver> driver) {
        CUSTOM_DRIVER = driver;
    }

    public static WebDriver getDriver() {
        return CUSTOM_DRIVER != null
            ? CUSTOM_DRIVER.get()
            : getDriver(BROWSER);
    }

    public static WebDriver getDriver(DriverTypes driverType) {
        if (driverType == null) {
            throw new JDINovaException("Driver type can't be null");
        }
        switch (driverType) {
            case CHROME:
                return chromeDriver();
            case FIREFOX:
                return firefoxDriver();
            case IE:
                return ieDriver();
            case IE_EDGE:
                return ieEdgeDriver();
            case SAFARI:
                return safariDriver();
            case OPERA:
                return operaDriver();
            default:
                throw new JDINovaException("Unknown driver type %s", driverType);
        }
    }

    public static WebDriver getDriver(DriverTypes browser, MutableCapabilities options) {
        if (RUN_MODE == null) {
            new DownloadDriverSettings().downloadDriver(browser);
            return getLocalDriver(browser, options);
        }
        if (RUN_MODE.remote) {
            return RUN_MODE.remoteSettings.getRemoteDriver(browser, options);
        }
        switch (RUN_MODE.type) {
            case "localdownload":
                RUN_MODE.downloadSettings.downloadDriver(browser);
                break;
            case "localbypath":
                RUN_MODE.driverPath.setSystemProperty(browser);
                break;
        }
        return getLocalDriver(browser, options);
    }

    public static WebDriver chromeDriver() {
        ChromeOptions options = new ChromeOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.chrome.accept(options);
        WebDriver driver = getDriver(CHROME, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static WebDriver firefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.firefox.accept(options);
        WebDriver driver = getDriver(FIREFOX, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static WebDriver ieDriver() {
        InternetExplorerOptions options = new InternetExplorerOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.ie.accept(options);
        WebDriver driver = getDriver(IE, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static WebDriver ieEdgeDriver() {
        EdgeOptions options = new EdgeOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.ieEdge.accept(options);
        WebDriver driver = getDriver(IE_EDGE, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static WebDriver safariDriver() {
        SafariOptions options = new SafariOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.safari.accept(options);
        WebDriver driver = getDriver(SAFARI, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static WebDriver operaDriver() {
        OperaOptions options = new OperaOptions();
        DRIVER_OPTIONS.common.accept(options);
        DRIVER_OPTIONS.opera.accept(options);
        WebDriver driver = getDriver(OPERA, options);
        DRIVER_SETUP.accept(driver);
        return driver;
    }

    public static void killDrivers() {
        try {
            getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
        } catch (Exception ignore) { }
    }

}
