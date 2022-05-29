package com.jdiai.jsbuilder;

import com.jdiai.jsbuilder.jsfunctions.BuilderFunctions;
import com.jdiai.jsdriver.JDINovaBuilderException;
import com.jdiai.tools.Safe;
import com.jdiai.tools.map.MapArray;
import com.jdiai.tools.pairs.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.jdiai.logger.QueryLogger.*;
import static com.jdiai.jsbuilder.RetryFunctions.DEFAULT_LIST_SCRIPT_EXECUTE;
import static com.jdiai.jsbuilder.RetryFunctions.DEFAULT_SCRIPT_EXECUTE;
import static com.jdiai.tools.PrintUtils.print;
import static com.jdiai.tools.ReflectionUtils.isClass;
import static com.jdiai.tools.StringUtils.LINE_BREAK;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class JSBuilder implements IJSBuilder {
    public Integer logQuery = null;
    public Function<String, String> processResultFunc =
        result -> result.length() > 200
            ? result.substring(0, 195) + "..."
            : result;
    protected String query = "";

    private final Supplier<WebDriver> driver;
    private Class<?> site;

    protected JavascriptExecutor js() {
        return (JavascriptExecutor) driver.get();
    }
    protected MapArray<String, String> useFunctions = new MapArray<>();
    public IBuilderActions builderActions;

    public JSBuilder(Supplier<WebDriver> driver) {
        this(driver, new BuilderActions());
    }

    public JSBuilder(Supplier<WebDriver> driver, IBuilderActions builderActions) {
        this.driver = driver;
        this.builderActions = builderActions != null
            ? builderActions
            : new BuilderActions();
        this.builderActions.setBuilder(() -> this);
    }

    public JSBuilder(Supplier<WebDriver> driver, BuilderFunctions functions) {
        this.driver = driver;
        this.builderActions = new BuilderActions(functions);
        this.builderActions.setBuilder(() -> this);
    }

    public WebDriver driver() {
        return driver.get();
    }

    public JSBuilder setProcessResultFunc(Function<String, String> processResultFunc) {
        this.processResultFunc = processResultFunc;
        return this;
    }

    protected String elementName = "element";

    public IJSBuilder setElementName(String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    public IJSBuilder updateActions(IBuilderActions builderActions) {
        this.builderActions = builderActions;
        this.builderActions.setBuilder(() -> this);
        return this;
    }
    public IJSBuilder registerFunction(String name, String function) {
        useFunctions.update(name, function);
        return this;
    }

    public IJSBuilder logQuery(int logLevel) {
        this.logQuery = logLevel;
        return this;
    }

    private int shouldLogQuery() {
        return logQuery != null ? logQuery : LOG_QUERY.get();
    }

    public boolean logScript() {
        return shouldLogQuery() == ALL;
    }

    public boolean logResult() {
        return shouldLogQuery() > OFF;
    }

    public static BiFunction<Object, String, Object> EXECUTE_SCRIPT = DEFAULT_SCRIPT_EXECUTE;

    public static Safe<ScriptResult> lastScriptExecution = new Safe<>(() -> new ScriptResult("No JS executions", ""));

    public Object executeQuery() {
        lastScriptExecution.reset();
        String jsScript = getQuery();
        if (logScript()) {
            logger.info("Execute query:" + LINE_BREAK + jsScript);
        }
        Object result;
        result = getScriptResult(jsScript);
        lastScriptExecution.set(new ScriptResult(jsScript, result));
        if (result != null && logResult()) {
            logger.info(">>> " + processResultFunc.apply(result.toString()));
        }
        return result;
    }

    private Object getScriptResult(String jsScript) {
        return EXECUTE_SCRIPT.apply(js(), jsScript);
    }

    public static BiFunction<Object, String, List<Object>> EXECUTE_LIST_SCRIPT = DEFAULT_LIST_SCRIPT_EXECUTE;

    private static boolean smartStringify = true;

    public static void switchOffStringify() { smartStringify = false; }

    public List<Object> executeAsList() {
        String jsScript = getQuery();
        if (logScript()) {
            logger.info("Execute query:" + LINE_BREAK + jsScript);
        }
        List<Object> result;
        result = EXECUTE_LIST_SCRIPT.apply(js(), jsScript);
        lastScriptExecution.set(new ScriptResult(jsScript, result));
        if (result != null && logResult()) {
            logger.info(">>> " + processResultFunc.apply(result.toString()));
        }
        return result;
    }

    public IJSBuilder addJSCode(String code) {
        query += code;
        return this;
    }

    public IJSBuilder oneToOne(String ctx, By locator) {
        return addJSCode(builderActions.oneToOne(ctx, locator));
    }
    public IJSBuilder oneToOneFilter(String ctx, By locator, String filter) {
        return addJSCode(builderActions.oneToOneFilter(ctx, locator, filter));
    }

    public IJSBuilder listToOne(By locator) {
        return addJSCode(builderActions.listToOne(locator));
    }
    public IJSBuilder listToOneFilter(By locator, String filter) {
        return addJSCode(builderActions.listToOneFilter(locator, filter));
    }

    public IJSBuilder oneToList(String ctx, By locator) {
        return addJSCode(builderActions.oneToList(ctx, locator));
    }
    public IJSBuilder oneToListFilter(String ctx, By locator, String filter) {
        return addJSCode(builderActions.oneToListFilter(ctx, locator, filter));
    }

    public IJSBuilder listToList(By locator) {
        return addJSCode(builderActions.listToList(locator));
    }
    public IJSBuilder listToListFilter(By locator, String filter) {
        return addJSCode(builderActions.listToListFilter(locator, filter));
    }

    public IJSBuilder doAction(String collectResult) {
        return addJSCode(builderActions.doAction(collectResult));
    }

    public IJSBuilder doListAction(String collectResult) {
        return addJSCode(builderActions.doListAction(collectResult));
    }

    public IJSBuilder getResult(String collectResult) {
        return addJSCode(builderActions.getResult(getCollector(collectResult)));
    }

    public IJSBuilder getResultList(String collectResult) {
        return addJSCode(builderActions.getResultList(getCollector(collectResult)));
    }

    public IJSBuilder trigger(String event) {
        return trigger(event,"'bubbles': true");
    }

    public IJSBuilder trigger(String event, String options) {
        return addJSCode("element.dispatchEvent(new Event('" + event + "', { " + options + " }));\n");
    }

    protected String getCollector(String collectResult) {
        if (collectResult == null) {
            return "";
        }
        if (smartStringify) {
            if (collectResult.trim().contains("return {") && collectResult.trim().contains("}")) {
                return collectResult.replace("return {", "return JSON.stringify({")
                    .replace("}", "})");
            }
            if (collectResult.trim().contains("return [") && collectResult.trim().contains("]")) {
                return collectResult.replace("return [", "return JSON.stringify([")
                    .replace("}", "})");
            }
            if (collectResult.trim().startsWith("{") && collectResult.trim().contains("}")) {
                return collectResult.replace("{", "JSON.stringify({")
                    .replace("}", "})");
            }
            if (collectResult.trim().startsWith("[") && collectResult.trim().contains("]")) {
                return collectResult.replace("[", "JSON.stringify([")
                    .replace("]", "])");
            }
        }
        return collectResult;
    }

    public String getQuery() {
        if (isEmpty(useFunctions)) {
            return getScript();
        }
        String script = isNotEmpty(useFunctions) ? print(useFunctions.values(), "") : "";
        script += getScript();
        if (!script.contains("%s")) {
            return script;
        }
        throw new JDINovaBuilderException("Failed to execute js script for template locator. Please replace %s before usage");
    }

    protected String getScript() {
        String result = query;
        cleanup();
        return result;
    }

    public void cleanup() {
        useFunctions.clear();
        query = "";
    }

    public String preResult(String collector) {
        return builderActions.preResult(collector);
    }

    public void updateFromBuilder(IJSBuilder builder) {
        if (!isClass(builder.getClass(), JSBuilder.class)) {
            return;
        }
        JSBuilder jsBuilder = (JSBuilder) builder;
        for (Pair<String, String> pair : jsBuilder.useFunctions) {
            if (!useFunctions.has(pair.key)) {
                useFunctions.add(pair);
            }
        }
    }

    public JSBuilder copy() {
        JSBuilder result = new JSBuilder(driver, builderActions);
        result.useFunctions = useFunctions;
        result.logQuery = logQuery;
        return result;
    }
    // endregion
}
