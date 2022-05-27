package com.jdiai;

import com.jdiai.testng.JDIAllureListener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static com.jdiai.JDI.*;
import static com.jdiai.jswraper.driver.DriverManager.killDrivers;
import static com.jdiai.jswraper.driver.JDIDriver.DRIVER_OPTIONS;
import static com.jdiai.logger.LoggerTypes.SLF4J;

public interface TestInit {
    @BeforeSuite(alwaysRun = true)
    default void setUp() {
        killDrivers();
        JDI.openSiteHeadless("https://jdi-testing.github.io/jdi-light");
        // JDI.openSite("https://jdi-testing.github.io/jdi-light");
        // logAll();
        // logJDIActions();
        addListener(new JDIAllureListener());
    }

    @AfterSuite(alwaysRun = true)
    default void tearDown() {
        killDrivers();
    }
}
