package com.jdiai;

import com.jdiai.tools.Safe;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.jdiai.Pages.HOME_PAGE;
import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static java.lang.Runtime.getRuntime;

public class DriverManager {
    public static Safe<WebDriver> DRIVER = new Safe<>(ChromeDriver::new);
    public static WebDriver driver() {
        return DRIVER.get();
    }

    public static void downloadDriver() {
        WebDriverManager wdm = chromedriver();
        wdm.arch64();
        // wdm.gitHubToken("WDM_GITHUBTOKEN");
        wdm.setup();
    }

    public static void initDriver() {
        downloadDriver();
        DRIVER = new Safe<>(() -> {
            WebDriver driver = chromeDriver();
            driver.get(HOME_PAGE);
            driver.manage().window().maximize();
            return driver;
        });
    }

    public static WebDriver chromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    public static void killDrivers() {
        try {
            getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
        } catch (Exception ignore) { }
    }
}
