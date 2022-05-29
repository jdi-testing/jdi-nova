package com.jdiai;

import com.jdiai.annotations.UI;
import com.jdiai.asserts.Condition;
import com.jdiai.asserts.ConditionTypes;
import com.jdiai.interfaces.HasCore;
import com.jdiai.logger.ThreadConsoleLogger;
import com.jdiai.jsbuilder.IJSBuilder;
import com.jdiai.logger.JLogger;
import com.jdiai.jsbuilder.JSBuilder;
import com.jdiai.jsbuilder.jsfunctions.*;
import com.jdiai.jsdriver.JDINovaException;
import com.jdiai.jswraper.JSBaseEngine;
import com.jdiai.jswraper.JSEngine;
import com.jdiai.jswraper.driver.DriverManager;
import com.jdiai.jswraper.driver.DriverTypes;
import com.jdiai.jswraper.driver.JDIDriver;
import com.jdiai.listeners.JDIEventsListener;
import com.jdiai.listeners.JDILogListener;
import com.jdiai.logger.Slf4JLogger;
import com.jdiai.tools.Safe;
import com.jdiai.tools.StringUtils;
import com.jdiai.tools.Timer;
import com.jdiai.tools.func.JAction2;
import com.jdiai.tools.func.JAction3;
import com.jdiai.tools.pairs.Pair;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.jdiai.JDIStatistic.actionsTime;
import static com.jdiai.JDIStatistic.trackAsserts;
import static com.jdiai.asserts.Conditions.above;
import static com.jdiai.asserts.Conditions.onLeftOf;
import static com.jdiai.asserts.ShouldUtils.SOFT_ASSERTION_MODE;
import static com.jdiai.asserts.ShouldUtils.waitForResult;
import static com.jdiai.jsbuilder.GetTypes.dataType;
import static com.jdiai.logger.QueryLogger.*;
import static com.jdiai.jsdriver.JDINovaException.THROW_ASSERT;
import static com.jdiai.jsdriver.JDINovaException.assertContains;
import static com.jdiai.jsdriver.JSDriverUtils.getByLocator;
import static com.jdiai.jswraper.JSWrappersUtils.*;
import static com.jdiai.jswraper.driver.DriverManager.useDriver;
import static com.jdiai.jswraper.driver.JDIDriver.BROWSER_SIZE;
import static com.jdiai.jswraper.driver.JDIDriver.DRIVER_OPTIONS;
import static com.jdiai.listeners.JDIEvents.*;
import static com.jdiai.logger.LoggerTypes.CONSOLE;
import static com.jdiai.logger.LoggerTypes.SLF4J;
import static com.jdiai.page.objects.PageFactory.initSite;
import static com.jdiai.page.objects.PageFactoryUtils.getLocatorFromField;
import static com.jdiai.tools.Alerts.acceptAlert;
import static com.jdiai.tools.BrowserTabs.getWindowHandles;
import static com.jdiai.tools.BrowserTabs.setTabName;
import static com.jdiai.tools.JsonUtils.getDouble;
import static com.jdiai.tools.LinqUtils.any;
import static com.jdiai.tools.LinqUtils.newList;
import static com.jdiai.tools.PrintUtils.print;
import static com.jdiai.tools.ReflectionUtils.getFieldsDeep;
import static com.jdiai.tools.ReflectionUtils.isInterface;
import static com.jdiai.tools.StringUtils.format;
import static java.lang.Integer.parseInt;
import static java.lang.Long.valueOf;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JDI {
    public static Safe<WebDriver> DRIVER = new Safe<>(DriverManager::getDriver);

    public static String JDI_STORAGE = "src/test/resources/jdi";

    public static String domain;

    public static JLogger logger() {
        return logger;
    }

    public static void setLogger(JLogger newLogger) {
        logger = newLogger;
    }

    public static int timeout = 10;

    public static ConditionTypes findFilters = new ConditionTypes();

    public static String browserSize = null;

    public static String selectFindTextLocator = ".//*[text()='%s']";

    public static Function<List<By>, JS> initJSFunc = locators -> new JSStable(JDI::driver, locators);

    public static Function<List<By>, JS> initCoreFunc = initJSFunc;

    public static JAction2<String, JS> beforeJSActions = (actionName, element) -> { };

    public static JAction3<String, JS, Object> afterJSActions = (actionName, element, result) -> { };

    public static void alwaysCloseNativeAlerts() {
        registerJDIListener(BEFORE_ACTION_EVENT, args -> {
            acceptAlert();
        });
    }

    public static void screenshotAfterFail() {
        registerJDIListener(AFTER_ACTION_FAIL_EVENT, args -> {
            if (args.length > 3) {
                JS element = (JS) args[2];
                try {
                    element.highlight();
                } catch (Throwable ignore) { }
                try {
                    logger().info("Screenshot: " + makeScreenshot().getAbsolutePath());
                } catch (Throwable ignore) { }
            }
        });
    }

    public static void trackStatistic() {
        registerJDIListener(AFTER_ACTION_EVENT, args -> {
            try {
                String fullName = ((String) args[0]);
                String actionName = fullName.indexOf('(') > 0
                    ? fullName.substring(0, fullName.indexOf('('))
                    : fullName;
                long actionTime = (long) args[5];
                if (actionsTime.get().has(actionName)) {
                    Pair<Long, Long> pair = actionsTime.get().get(actionName);
                    long averageTime = pair.key;
                    long count = pair.value;
                    long newCount = count + 1;
                    actionsTime.update(statistic ->
                        statistic.update(actionName, Pair.$((averageTime * count + actionTime) / newCount, newCount)));
                } else {
                    actionsTime.update(statistic ->
                        statistic.add(actionName, Pair.$(actionTime, (long) 1)));
                }
            } catch (Exception ignore){ }
        });
    }

    public static void maximizeBrowser() {
        JDIDriver.maximizeBrowser(driver());
    }

    public static void setBrowserSize(int width, int height) {
        JDIDriver.setBrowserSize(driver(), width, height);
    }

    public static void alwaysShowElement() {
        registerJDIListener(BEFORE_ACTION_EVENT, args -> {
            if (args.length > 3) {
                JS element = (JS) args[2];
                String step = args[1].toString();
                if (isInterface(element.getClass(), JS.class) &&  !step.contains("Show")) {
                    element.showIfNotInView();
                }
            }
        });
    }

    public static void logJDIActions() {
        addListener(new JDILogListener());
    }

    public static Function<Supplier<WebDriver>, IJSBuilder> initBuilder = driver -> {
        BuilderFunctions bf = new BuilderFunctions().set(f -> {
            f.oneToOne = JSOneToOne.ONE_TO_ONE;
            f.oneToOneFilter = JSOneToOne.STRICT_ONE_TO_ONE;

            f.oneToList = JSOneToList.ONE_TO_LIST;
            f.oneToListFilter = JSOneToList.FILTER_ONE_TO_LIST;

            f.listToOne = JSListToOne.LIST_TO_ONE;
            f.listToOneFilter = JSListToOne.FILTER_LIST_TO_ONE;

            f.listToList = JSListToList.ONE_LIST_TO_LIST;
            f.listToListFilter = JSListToList.FILTER_ONE_LIST_TO_LIST;

            f.result = JSResults.ONE_TO_RESULT;
            f.listResult = JSResults.LIST_TO_RESULT;
            f.action = JSResults.ONE_TO_ACTION;
            f.listAction = JSResults.LIST_TO_ACTION;
        });
        return new JSBuilder(driver, bf);
    };

    public static BiFunction<Supplier<WebDriver>, List<By>, JSEngine> initEngine =
        (driver, locators) -> new JSBaseEngine(driver, locators, initBuilder.apply(driver));

    public static BiFunction<Object, Exception, Boolean> IGNORE_FAILURE = (js, e) -> true;

    public static String LOGGER_TYPE = "console";

    public static boolean LOG_ACTIONS = true;

    private static boolean initialized = false;

    public static void setNewListener(JDIEventsListener listener) {
        clearAllListeners();
        addListener(listener);
    }

    public static void addListener(JDIEventsListener listener) {
        registerJDIListener(BEFORE_ACTION_EVENT, args -> {
            try {
                String actionName = (String) args[0];
                String step = (String) args[1];
                HasCore element = (HasCore) args[2];
                listener.beforeAction(actionName, step, element);
            } catch (Exception ex) {
                throw new JDINovaException(ex, "Failed to parse "+ BEFORE_ACTION_EVENT + " args");
            }
        });
        registerJDIListener(AFTER_ACTION_EVENT, args -> {
            try {
                String actionName = (String) args[0];
                String step = (String) args[1];
                HasCore element = (HasCore) args[2];
                Object result = args[3];
                long timeout = (Long) args[4];
                long timePassed = (Long) args[5];
                listener.afterAction(actionName, step, element, result, timeout, timePassed);
            } catch (Exception ex) {
                throw new JDINovaException(ex, "Failed to parse "+ AFTER_ACTION_EVENT + " args");
            }
        });
        registerJDIListener(AFTER_SUCCESS_ACTION_EVENT, args -> {
            try {
                String actionName = (String) args[0];
                String step = (String) args[1];
                HasCore element = (HasCore) args[2];
                Object result = args[3];
                long timeout = (Long) args[4];
                long timePassed = (Long) args[5];
                listener.afterSuccessAction(actionName, step, element, result, timeout, timePassed);
            } catch (Exception ex) {
                throw new JDINovaException(ex, "Failed to parse "+ AFTER_SUCCESS_ACTION_EVENT + " args");
            }
        });
        registerJDIListener(AFTER_ACTION_FAIL_EVENT, args -> {
            String actionName = (String) args[0];
            String step = (String) args[1];
            HasCore element = (HasCore) args[2];
            Object result = args[3];
            long timeout = (Long) args[4];
            long timePassed = (Long) args[5];
            Throwable failException = (Throwable) args[6];
            String failAssertMessage = (String) args[7];
            listener.afterFailAction(actionName, step, element, result, timeout, timePassed, failException, failAssertMessage);
        });
    }

    public static Function<Field, String> GET_COMPLEX_VALUE = field -> {
        if (!field.isAnnotationPresent(FindBy.class) && !field.isAnnotationPresent(UI.class)) {
            return null;
        }
        By locator = getLocatorFromField(field);
        if (locator != null) {
            String element = MessageFormat.format(dataType(locator).get, "element", getByLocator(locator));
            return StringUtils.format("'%s': %s", field.getName(), getValueType(field, element));
        }
        return null;
    };

    public static BiFunction<Field, Object, String> SET_COMPLEX_VALUE = (field, value)-> {
        if (!field.isAnnotationPresent(FindBy.class) && !field.isAnnotationPresent(UI.class))
            return null;
        By locator = getLocatorFromField(field);
        if (locator == null) {
            return null;
        }
        String element = MessageFormat.format(dataType(locator).get, "element", getByLocator(locator));
        return setValueType(field, element, value);
    };

    public static Function<Class<?>, String> GET_OBJECT_MAP = cl -> {
        List<Field> allFields = getFieldsDeep(cl);
        List<String> mapList = new ArrayList<>();
        for (Field field : allFields) {
            String value = GET_COMPLEX_VALUE.apply(field);
            if (value != null) {
                mapList.add(value);
            }
        }
        return "{ " + print(mapList, ", ") + " }";
    };
    public static String SUBMIT_LOCATOR = "[type=submit]";

    public static String GET_TEXT_DEFAULT = "innerText";

    private static Safe<Integer> savedLogLevel = new Safe<>(() -> null);

    public static void loggerOff() {
        if (savedLogLevel.get() == null) {
            logger().loggerOff();
            savedLogLevel.set(LOG_QUERY.get());
            logJSRequests(OFF);
        }
    }

    public static void logNone() {
        logJSRequests(OFF);
    }

    public static void logAll() {
        logJSRequests(ALL);
    }

    public static void logJSRequests(int logQueryLevel) {
        LOG_QUERY.set(logQueryLevel);
    }

    public static void loggerOn() {
        Integer logLevel = savedLogLevel.get();
        if (logLevel != null) {
            logger().loggerOn();
            logJSRequests(logLevel);
            savedLogLevel.set(null);
        }
    }

    public static Function<JSLight, String> NAME_FUNC = js -> isNotBlank(js.name) ? js.name : js.printLocators();

    public static WebDriver driver() {
        return DRIVER.get();
    }

    public static Object jsExecute(String script, Object... params) {
        return ((JavascriptExecutor) driver()).executeScript(script, params);
    }

    public static Object jsEvaluate(String script, Object... params) {
        return jsExecute("return " + script, params);
    }

    public static void refreshPage() {
        driver().navigate().refresh();
    }

    public static void navigateBack() {
        driver().navigate().back();
    }

    public static void navigateForward() {
        driver().navigate().forward();
    }

    public static String getUrl() {
        return (String) jsEvaluate("document.URL;");
    }

    public static void urlShouldBe(String url) {
        trackAsserts(
            () -> waitForResult(() -> getUrl().contains(url)),
            "validate page url",
            "Validate that page url contains '" + url + "'",
            String.format("Page has url '%s' but expected '%s'", getUrl(), url),
            null
        );
    }

    public static String getTitle() {
        return (String) jsEvaluate("document.title;");
    }

    public static String getPageHeader() {
        return $("h1").getText();
    }

    public static void pageShouldHaveHeader(String pageHeader) {
        String h1 = getPageHeader();
        trackAsserts(
            () -> assertContains("Page Header", h1, pageHeader),
            "validate page header",
            "Validate that page has header(<h1> tag) '" + pageHeader + "'",
            String.format("Page has header(<h1> tag) '%s' but expected '%s'", h1, pageHeader),
            null
        );
    }

    public static void titleShouldContains(String title) {
        String actualTitle = getTitle();
        trackAsserts(
            () -> assertContains("Page Title", actualTitle, title),
            "validate page title",
            "Validate that page has title '" + title + "'",
            String.format("Page has title '%s' but expected '%s'", actualTitle, title),
            null
        );
    }

    public static String getDomain() { return (String) jsEvaluate("document.domain;"); }

    public static double zoomLevel() {
        return getDouble(jsEvaluate("window.devicePixelRatio;"));
    }

    public static File makeScreenshot(String name) {
        return new WebPage().setName(name).makeScreenshot();
    }

    public static File makeScreenshot() {
        return new WebPage().setName(getTitle()).makeScreenshot();
    }

    public static void strictAssertions() {
        SOFT_ASSERTION_MODE = false;
    }

    public static void softAssertions() {
        SOFT_ASSERTION_MODE = true;
    }

    public static void useSlf4JLogger() {
        setLogger(new Slf4JLogger(LOGGER_NAME));
    }

    public static void useConsoleLogger() {
        setLogger(new ThreadConsoleLogger(getLoggerName(CONSOLE)));
    }

    private static void init() {
        if (initialized) {
            return;
        }
        switch (LOGGER_TYPE) {
            case SLF4J:
                useSlf4JLogger();
                break;
            case CONSOLE:
            default:
                useConsoleLogger();
                break;
        }
        logJDIActions();
        try {
            String[] split = browserSize.toLowerCase().split("x");
            BROWSER_SIZE = new Dimension(parseInt(split[0]), parseInt(split[1]));
        } catch (Exception ignore) {
            logger().info("Failed to setup browser size: " + browserSize);
        }
        initialized = true;
    }

    public static boolean attributesInSeleniumWay = true;

    private static String getLoggerName(String name) {
        return format("%s(%s)", LOGGER_NAME, name);
    }

    public static void openIn(DriverTypes driver) {
        useDriver(driver);
        openSite();
    }
    public static void openIn(DriverTypes driver, String url) {
        useDriver(driver);
        openSite(url);
    }

    public static void openSite(String url) {
        domain = url;
        openSite();
    }

    public static void openSite() {
        init();
        openPage(domain);
    }

    public static void openSite(int width, int height) {
        openSite();
        setBrowserSize(width, height);
    }

    public static void openSiteHeadless(String url) {
        domain = url;
        openSiteHeadless();
    }

    public static void headless() {
        DRIVER_OPTIONS.chrome = cap -> cap.addArguments("--headless");
    }

    public static void openSiteHeadless() {
        headless();
        openSite();
    }
    public static void openSiteHeadless(int width, int height) {
        openSiteHeadless();
        setBrowserSize(width, height);
    }
    public static void openSiteHeadless(String url, int width, int height) {
        domain = url;
        openSiteHeadless();
        setBrowserSize(width, height);
    }

    public static void reopenSite() {
        init();
        if (DRIVER.hasValue()) {
            driver().quit();
        }
        DRIVER.reset();
        openPage(domain);
    }

    public static void reopenSite(Class<?> cl) {
        initSite(cl);
        reopenSite();
    }

    public static void reopenSite(int width, int height) {
        reopenSite();
        setBrowserSize(width, height);
    }

    public static void openSite(Class<?> cl) {
        init();
        initSite(cl);
        if (domain != null) {
            JDI.openSite();
            setTabName(cl.getSimpleName());
        }
    }

    public static void lineLayout(HasCore... elements) {
        if (isEmpty(elements) || elements.length == 1) {
            return;
        }
        for (int i = 1; i < elements.length; i++) {
            elements[i-1].shouldBe(onLeftOf(elements[i]));
        }
    }

    public static void complexLayout(HasCore[][] elements) {
        if (isEmpty(elements)) {
            return;
        }
        for (int i = 0; i < elements.length; i++) {
            HasCore[] line = elements[i];
            lineLayout(line);
            if (i > 0) {
                for (HasCore above : elements[i-1]) {
                    for (HasCore below : elements[i]) {
                        above.shouldBe(above(below));
                    }
                }
            }
        }
    }

    public static void gridLayout(HasCore[][] elements) {
        if (isEmpty(elements)) {
            return;
        }
        if (isNotGrid(elements)) {
            THROW_ASSERT.accept("Layout is not grid (grid should have at least one line and all lines have same length)");
        }
        long amount = elements[0].length;
        for (int i = 1; i < elements.length; i++) {
            for (int j = 1; j < amount; j++) {
                for (int k = 0; k < amount; k++) {
                    HasCore above = elements[i-1][j];
                    HasCore below = elements[i][k];
                    if (above == null || below == null) {
                        continue;
                    }
                    above.shouldBe(above(below));
                }
            }
        }
        for (int j = 1; j < amount; j++) {
            for (int i = 1; i < elements.length; i++) {
                for (HasCore[] element : elements) {
                    HasCore left = element[j-1];
                    HasCore right = elements[i][j];
                    if (left == null || right == null) {
                        continue;
                    }
                    left.shouldBe(onLeftOf(right));
                }
            }
        }
    }

    private static boolean isNotGrid(HasCore[][] elements) {
        int amount = elements[0].length;
        return amount < 1 || any(elements, line -> line.length != amount);
    }

    public static void openPageWith(String url, Credentials user) {
        ((HasAuthentication) driver()).register(() -> user);
        openPage(url);
    }
    public static void openPage(String url) {
        init();
        String fullUrl = isNotEmpty(domain) && !url.contains("//")
            ? domain + url
            : url;
        if (isEmpty(domain) && url.contains("//")) {
            domain = url;
        }
        String openPageAction = "openPage()";
        String openPageStep = "Open page '" + fullUrl + "'";
        fireEvent(BEFORE_ACTION_EVENT, openPageAction, openPageStep, null);
        Timer timer = new Timer();
        try {
            driver().get(fullUrl);
            getWindowHandles();
            fireEvent(AFTER_SUCCESS_ACTION_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec());
        } catch (Exception ex){
            fireEvent(AFTER_ACTION_FAIL_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec(), ex, null);
        } finally {
            fireEvent(AFTER_ACTION_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec());
        }
    }

    public static JS $() {
        return initJSFunc.apply(newList());
    }

    public static JS $(By locator) {
        return initJSFunc.apply(newList(locator));
    }

    public static JS $(By... locators) {
        return initJSFunc.apply(newList(locators));
    }

    public static JS $(String locator) {
        return $(NAME_TO_LOCATOR.apply(locator));
    }

    public static JS $(String... locators) {
        return $(locatorsToBy(locators));
    }

    public static void loginAs(String formLocator, Object user) {
        $(formLocator).loginAs(user);
    }

    public static void loginAs(Object user) {
        initJSFunc.apply(newList()).loginAs(user);
    }

    public static void submitForm(String formLocator, Object user) {
        $(formLocator).submit(user);
    }

    public static void submitForm(Object user) {
        initJSFunc.apply(null).submit(user);
    }

    public static void fillFormWith(String formLocator, Object user) {
        $(formLocator).fill(user);
    }

    public static void fillFormWith(Object user) {
        initJSFunc.apply(null).fill(user);
    }

    public static DragAndDrop drag(JS dragElement) { return new DragAndDrop(dragElement);}

    public static void waitFor(JS element, Condition... conditions) {
        element.waitFor(conditions);
    }

    public static JSEngine jsDriver() { return initEngine.apply(JDI::driver, new ArrayList<>()); }
}
