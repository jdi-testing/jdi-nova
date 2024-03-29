package com.jdiai.jsdriver;

import com.jdiai.jsbuilder.IJSBuilder;
import com.jdiai.locators.ByFrame;
import com.jdiai.tools.StringUtils;
import com.jdiai.tools.map.MapArray;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jdiai.jsbuilder.jsfunctions.JSFunctions.XPATH_FUNC;
import static com.jdiai.jsbuilder.jsfunctions.JSFunctions.XPATH_LIST_FUNC;
import static com.jdiai.tools.LinqUtils.first;
import static com.jdiai.tools.LinqUtils.select;
import static com.jdiai.tools.PrintUtils.print;
import static com.jdiai.tools.ReflectionUtils.isClass;

/**
 * Created by Roman Iovlev on 25.08.2021
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

public final class JSDriverUtils {
    private JSDriverUtils() { }

    public static String selector(By by, IJSBuilder builder) {
        String selector = getByLocator(by);
        if (selector == null) {
            throw new JDINovaException("Failed to build selector. Locator is null");
        }
        if (getByType(by).equals("xpath")) {
            builder.registerFunction("xpath", XPATH_FUNC);
        }
        return selector;
    }
    public static String selectorAll(By by, IJSBuilder builder) {
        String selector = getByLocator(by);
        if (selector == null) {
            throw new JDINovaException("Failed to build selector. Locator is null");
        }
        if (getByType(by).equals("xpath")) {
            builder.registerFunction("xpathList", XPATH_LIST_FUNC);
        }
        return selector;
    }

    public static boolean isIFrame(By by) {
        if (by == null) {
            return false;
        }
        return isClass(by.getClass(), ByFrame.class);
    }

    public static String iFrame(By locator) {
        return isIFrame(locator) ? ".contentWindow.document" : "";
    }

    public static String getByLocator(By by) {
        if (by == null) {
            return null;
        }
        if (isIFrame(by)) {
            return ((ByFrame) by).locator;
        }
        String byAsString = by.toString();
        int index = byAsString.indexOf(": ") + 2;
        return byAsString.substring(index).replace("'", "\"");
    }
    private static final MapArray<String, String> byReplace = new MapArray<>(new Object[][] {
        {"cssSelector", "css"},
        {"tagName", "tag"},
        {"className", "class"}
    });
    public static String getByType(By by) {
        if (isIFrame(by)) {
            return ((ByFrame)by).type.equals("id") ? "id" : "css";
        }
        if (by == null) {
            return "";
        }
        Matcher m = Pattern.compile("By\\.(?<locator>[a-zA-Z]+):.*").matcher(by.toString());
        if (m.find()) {
            String result = m.group("locator");
            return byReplace.has(result) ? byReplace.get(result) : result;
        }
        throw new RuntimeException("Can't get By name for: " + by);
    }
    public static Function<String, By> getByFunc(By by) {
        return first(getMapByTypes(), key -> by.toString().contains(key));
    }
    private static String getBadLocatorMsg(String byLocator, Object... args) {
        return "Bad locator template '" + byLocator + "'. Args: " + print(select(args, Object::toString), ", ", "'%s'") + ".";
    }
    public static By fillByTemplate(By by, Object... args) {
        String byLocator = getByLocator(by);
        if (!byLocator.contains("%"))
            throw new RuntimeException(getBadLocatorMsg(byLocator, args));
        try {
            byLocator = StringUtils.format(byLocator, args);
        } catch (Exception ex) {
            throw new RuntimeException(getBadLocatorMsg(byLocator, args));
        }
        return getByFunc(by).apply(byLocator);
    }

    private static Map<String, Function<String, By>> getMapByTypes() {
        Map<String, Function<String, By>> map = new HashMap<>();
        map.put("By.cssSelector", By::cssSelector);
        map.put("By.className", By::className);
        map.put("By.id", By::id);
        map.put("By.linkText", By::linkText);
        map.put("By.name", By::name);
        map.put("By.partialLinkText", By::partialLinkText);
        map.put("By.tagName", By::tagName);
        map.put("By.xpath", By::xpath);
        return map;
    }
}
