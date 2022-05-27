package com.jdiai;

import com.google.gson.JsonObject;
import com.jdiai.interfaces.HasCore;
import com.jdiai.jsdriver.JDINovaException;
import com.jdiai.jsdriver.RuleType;
import com.jdiai.jsproducer.Json;
import com.jdiai.jswraper.JSEngine;
import com.jdiai.scripts.Whammy;
import com.jdiai.tools.*;
import com.jdiai.tools.map.MapArray;
import com.jdiai.tools.pairs.Pair;
import com.jdiai.visual.Direction;
import com.jdiai.visual.ImageTypes;
import com.jdiai.visual.OfElement;
import com.jdiai.visual.StreamToImageVideo;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.jdiai.JDI.*;
import static com.jdiai.jsbuilder.GetTypes.dataType;
import static com.jdiai.jsdriver.JDINovaException.THROW_ASSERT;
import static com.jdiai.jsdriver.JSDriverUtils.*;
import static com.jdiai.jswraper.JSWrappersUtils.NAME_TO_LOCATOR;
import static com.jdiai.jswraper.JSWrappersUtils.setStringAction;
import static com.jdiai.tools.EnumUtils.getEnumValue;
import static com.jdiai.tools.GetTextTypes.INNER_TEXT;
import static com.jdiai.tools.JSUtils.getLocators;
import static com.jdiai.tools.Keyboard.pasteText;
import static com.jdiai.tools.LinqUtils.*;
import static com.jdiai.tools.PrintUtils.print;
import static com.jdiai.tools.ReflectionUtils.*;
import static com.jdiai.tools.StringUtils.format;
import static com.jdiai.tools.VisualSettings.*;
import static com.jdiai.visual.Direction.VECTOR_SIMILARITY;
import static com.jdiai.visual.ImageTypes.VIDEO_WEBM;
import static com.jdiai.visual.RelationsManager.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.openqa.selenium.Keys.BACK_SPACE;
import static org.openqa.selenium.OutputType.*;

public class JSLight implements JS {
    protected JSEngine engine;
    protected Supplier<WebDriver> driver;
    protected Safe<Actions> actions;
    public String name = "";
    public String varName = "";
    public Object parent = null;
    protected JSImages imagesData;
    public int renderTimeout = 5000;
    protected String objectMap;
    protected String filter;
    protected String defaultFilter = findFilters.isDisplayed;

    public JSLight() {
        this(JDI::driver, new ArrayList<>());
    }

    public JSLight(Supplier<WebDriver> driver, List<By> locators) {
        this.driver = driver;
        this.engine = initEngine.apply(driver, locators);
        this.actions = new Safe<>(() -> new Actions(driver()));
        init();
    }
    public JSLight(WebDriver driver, List<By> locators) {
        this(() -> driver, locators);
    }

    public JSLight(Supplier<WebDriver> driver, By... locators) {
        this(driver, newList(locators));
    }

    public JSLight(WebDriver driver, By... locators) {
        this(() -> driver, locators);
    }

    public JSLight(Object parent, By locator) {
        this(JDI::driver, locator);
        setParent(parent);
    }

    public JSLight(WebDriver driver, By locator, Object parent) {
        this(() -> driver, locator);
        setParent(parent);
    }

    public JSLight(Supplier<WebDriver> driver, By locator, Object parent) {
        this(driver, getLocators(locator, parent));
        this.parent = parent;
        if (parent != null && isInterface(parent.getClass(), HasCore.class)) {
            this.engine().updateFrom(((HasCore) parent).core().engine());
        }
    }

    public void click(int timeoutWait) {
        setTimeout(timeoutWait);
        click();
    }
    public void click() {
        engine().doAction("click();");
    }
    public JS clickJS() {
        engine().doAction("click();");
        return this;
    }

    public JS clickCenter() {
        engine().doAction("rect = element.getBoundingClientRect();" +
            "x = rect.x + rect.width / 2;" +
            "y = rect.y + rect.height / 2;" +
            "document.elementFromPoint(x, y).click();");
        return this;
    }

    public JS click(int x, int y) {
        engine().jsExecute("document.elementFromPoint(" + x + ", " + y + ").click();");
        return this;
    }

    public JS select() { click(); return this; }

    public void select(String value) {
        if (value == null) {
            throw new JDINovaException("get(null) failed. Value can't be null");
        }
        if (isEmpty(locators())) {
            throw new JDINovaException("get(" + value + ") failed. Element should have locator");
        }
        By lastLocator = last(locators());
        JS item;
        if (lastLocator.toString().contains("%s")) {
            item = findTemplate(value);
        } else {
            item = findFirst(valueCondition(value));
        }
        item.setName(getName() + "[" + value + "]");
        item.click();
    }

    public void select(String... values) {
        for (String value : values) {
            select(value);
        }
    }

    public <TEnum extends Enum<?>> void select(TEnum name) {
        select(getEnumValue(name));
    }

    public JS check(boolean value) {
        engine().doAction("checked=" + value + ";");
        return this;
    }

    public JS check() {
        return check(true);
    }

    public JS uncheck() {
        return check(false);
    }

    public JS rightClick() {
        return weElementActions(Actions::contextClick);
    }

    public JS doubleClick() {
        return weElementActions(Actions::doubleClick);
    }

    public JS hover() {
        return weActions(Actions::moveToElement);
    }

    public JS dragAndDropTo(WebElement to) {
        return dragAndDropTo(to.getLocation().x, to.getLocation().y);
    }

    public JS dragAndDropTo(int x, int y) {
        return weActions((a, e) -> a.dragAndDropBy(e, x, y));
    }

    public void submit() {
        we().submit();
    }

    protected String charToString(CharSequence... value) {
        return value.length == 1 ? value[0].toString() : "";
    }

    public void sendKeys(CharSequence... value) {
        if (value == null) {
            return;
        }
        if (value.length == 1 && value[0].equals("\n")) {
            we().sendKeys("\n " + BACK_SPACE);
        } else {
            we().sendKeys(value);
        }
    }

    public JS input(CharSequence... value) {
        if (value == null) {
            return this;
        }
        engine().doAction("setAttribute('value', '');\nelement.value='" + charToString(value) + "';\nelement.dispatchEvent(new Event('input'));");
        return this;
    }

    public JS slide(String value) {
        throw new NotImplementedException();
        // TODO
        //Actions a = new Actions(DRIVER.get());
        //a.dragAndDropBy(DRIVER.get().findElement(By.xpath("[aria-labelledby='range-slider'][data-index='0']")),20, 0)
        //  .build().perform();
        //js.jsDriver().builder().oneToOne("document", locators.get(0))
        //  .addJSCode("element.value='" + value + "';\n")
        //  .trigger("mousedown")
        //  .trigger("mousemove", "which: 1, pageX: 460");
        //.trigger("mousedown")
        //  .trigger("mousemove", { which: 1, pageX: 460 })
    }

    public void clear() {
        setProperty("value", "");
    }

    public String getTagName() {
        return getJSResult("tagName").toLowerCase();
    }

    public String tag() {
        return getTagName();
    }

    public String getAttribute(String attrName) {
        return attributesInSeleniumWay
            ? engine().getProperty(attrName)
            : engine().getAttribute(attrName);
    }

    public String getProperty(String property) {
        return attributesInSeleniumWay
            ? engine().getAttribute(property)
            : engine().getProperty(property);
    }

    public void setAttribute(String attrName, String value) {
        engine().doAction("setAttribute('" + attrName + "', '" + value + "');");
    }

    public void setProperty(String property, String value) {
        engine().doAction("element." + property + " = '" + value + "';");
    }

    public Json getJson(String valueFunc) {
        return engine().getAsMap(valueFunc);
    }

    public String attr(String attrName) {
        return getAttribute(attrName);
    }

    public String prop(String attrName) {
        return getProperty(attrName);
    }

    public List<String> getAttributesAsList(String attr) {
        return attributesInSeleniumWay
            ? engine().getPropertyList(attr)
            : engine().getAttributeList(attr);
    }

    public List<String> getPropertiesAsList(String property) {
        return attributesInSeleniumWay
            ? engine().getAttributeList(property)
            : engine().getPropertyList(property);
    }

    public List<String> attrList(String attr) {
        return getAttributesAsList(attr);
    }

    public List<String> propsList(String property) {
        return getPropertiesAsList(property);
    }

    public List<Json> getAttributesAsList(String... attributes) {
        return engine().getMultiAttributes(attributes);
    }

    public List<Json> attrList(String... attributes) {
        return getAttributesAsList(attributes);
    }

    public List<String> allClasses() {
        String cl = attr("class");
        return cl.length() > 0
            ? newList(cl.split(" "))
            : new ArrayList<>();
    }

    public boolean hasClass(String className) {
        return allClasses().contains(className);
    }

    public boolean hasAttribute(String attrName) {
        return JDI.attributesInSeleniumWay
            ? engine().hasProperty(attrName)
            : engine().hasAttribute(attrName);
    }

    public boolean hasProperty(String property) {
        return JDI.attributesInSeleniumWay
            ? engine().hasAttribute(property)
            : engine().hasProperty(property);
    }

    public Json allAttributes() {
        return engine().getAsMap("return '{'+[...element.attributes].map((attr)=> `'${attr.name}'='${attr.value}'`).join()+'}'");
    }

    public String printHtml() {
        return MessageFormat.format("<{0} {1}>{2}</{0}>", getTagName().toLowerCase(),
            print(allAttributes(), el -> format("%s='%s'", el.key, el.value), " "),
            getJSResult("innerHTML"));
    }

    public JS showIfNotInView() {
        if (isDisplayed() && !isInView()) {
            show();
        }
        return this;
    }
    public JS show() {
        engine().doAction("scrollIntoView({behavior:'auto',block:'center',inline:'center'})");
        return this;
    }

    public JS highlight(String color) {
        show();
        engine().doAction("styles.border='3px dashed "+color+"'");
        return this;
    }

    public void highlight() {
        highlight("red");
    }

    public String style(String style) {
        return engine().getStyle(style);
    }

    public Json styles(String... styles) {
        return engine().getStyles(styles);
    }

    public Json allStyles() {
        return engine().getAllStyles();
    }

    public boolean isSelected() {
        return getAttribute("checked").equals("true")
            || getAttribute("selected").equals("true");
    }

    public boolean isDeselected() {
        return !isSelected();
    }

    public boolean isDisabled() {
        return hasAttribute("disabled");
    }

    public JS setTextType(GetTextTypes textType) {
        this.textType = textType; return this;
    }

    protected GetTextTypes textType = null;

    public String textType() {
        return textType == null ? INNER_TEXT.value : textType.value;
    }

    public String getText() {
        return getJSResult(textType());
    }

    public String getText(String textType) {
        return getJSResult(textType);
    }

    public List<WebElement> findElements(By by) {
        return we().findElements(by);
    }

    public WebElement findElement(By by) {
        return we().findElement(by);
    }

    public boolean isDisplayed() {
        try {
            return getElement(findFilters.isDisplayed).equalsIgnoreCase("true");
        } catch (Throwable ignore) {
            return false;
        }
    }

    public boolean isVisible() {
        if (isHidden()) {
            return false;
        }
        show();
        return getElement(findFilters.isVisible).equalsIgnoreCase("true");
    }

    public boolean isInView() {
        if (isHidden()) {
            return false;
        }
        Dimension visibleRect = getSize();
        return visibleRect.height > 0 && visibleRect.width > 0;
    }

    public boolean isExist() {
        return jsDriver().getSize() > 0;
    }

    public Point getLocation() {
        ClientRect rect = getClientRect();
        int x, y;
        if (inVision(rect)) {
            return new Point(-1, -1);
        }
        int left = max(rect.left, 0);
        int top = max(rect.top, 0);
        x = left + getWidth(rect) / 2;
        y = top + getHeight(rect) / 2;
        return new Point(x, y);
    }

    protected boolean inVision(ClientRect rect) {
        return rect.x >= rect.windowWidth || rect.y >= rect.windowHeight || rect.bottom < 0 || rect.right < 0;
    }

    @Override
    public Dimension getSize() {
        ClientRect rect = getClientRect();
        int width, height;
        if (inVision(rect))
            return new Dimension(0, 0);
        width = getWidth(rect);
        height = getHeight(rect);
        return new Dimension(width, height);
    }

    protected int getWidth(ClientRect rect) {
        int left = max(rect.left, 0);
        int right = min(rect.right, rect.windowWidth);
        return right - left;
    }

    protected int getHeight(ClientRect rect) {
        int top = max(rect.top, 0);
        int bottom = min(rect.bottom, rect.windowHeight);
        return bottom - top;
    }

    public Rectangle getRect() {
        ClientRect rect = getClientRect();
        return new Rectangle(rect.x, rect.y, getHeight(rect), getWidth(rect));
    }

    public ClientRect getClientRect() {
        return new ClientRect(engine().getJson("rect = element.getBoundingClientRect();\n" +
            "return { x: rect.x, y: rect.y, top: rect.top, bottom: rect.bottom, left: rect.left, right: rect.right, " +
            "wWidth: window.innerWidth, wHeight: window.innerHeight };"));
    }

    public String getCssValue(String style) {
        return engine().getStyle(style);
    }

    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        StreamToImageVideo screen = makeScreenshot(DEFAULT_IMAGE_TYPE);
        if (outputType == BASE64) {
            return (X) screen.asBase64();
        }
        if (outputType == BYTES) {
            return (X) screen.asByteStream();
        }
        if (outputType == FILE) {
            return (X) screen.asFile(IMAGE_TEMPLATE.apply("", this));
        }
        throw new JDINovaException("Failed to get screenshot - unknown type: " + outputType);
    }

    protected String canvas2Image(ImageTypes imageType) {
        return "toDataURL('" + imageType.value + "')";
    }

    protected String element2Image(ImageTypes imageType) {
        return "html2canvas(element).then((canvas) => canvas."+canvas2Image(imageType)+")";
    }

    public StreamToImageVideo makeScreenshot() {
        return makeScreenshot(DEFAULT_IMAGE_TYPE);
    }

    public File makeScreenshot(String tag) {
        show();
        File imageFile = makeScreenshot().asFile(getScreenshotName(tag));
        imagesData().images.update(tag, imageFile.getPath());
        imagesData().imageFile = imageFile;
        return imageFile;
    }

    protected String getScreenshotName(String tag) {
        return IMAGE_TEMPLATE.apply(tag, this);
    }

    public StreamToImageVideo makeScreenshot(ImageTypes imageType) {
        String stream = getElement("if (element.toDataURL) { return element."+canvas2Image(imageType)+"; }\n"
            + "try { return " + element2Image(imageType) + "; } catch {\n"
            + "return await import(`https://html2canvas.hertzen.com/dist/html2canvas.min.js`).then("
            + "() => " + element2Image(imageType) + ") }"
        );
        return new StreamToImageVideo(stream, imageType);
    }

    public JS startRecording() {
        return startRecording(VIDEO_WEBM);
    }

    public JS startRecording(ImageTypes imageType) {
        String value = getElement("blobs = [];\n" +
            "recorder = new MediaRecorder(element.captureStream(), { mimeType: '" + imageType.value + "' });\n" +
            "recorder.ondataavailable = (e) => {\n" +
            "  if (e.data && e.data.size > 0) { blobs.push(e.data); }\n}\n" +
            "recorder.onstop = () => {\n" +
            "  blob = new Blob(blobs, { type: '" + imageType.value + "' });\n" +
            "  reader = new FileReader();\n" +
            "  reader.readAsDataURL(blob);\n" +
            "  reader.onloadend = () => window.jdiVideoBase64 = reader.result;\n" +
            "}\n" +
            "recorder.start();\n" +
            "window.jdiRecorder = recorder;\n" +
            "return 'start recording'");
        if (!value.equals("start recording")) {
            throw new JDINovaException(value);
        }
        return this;
    }

    public StreamToImageVideo stopRecordingAndSave(ImageTypes imageType) {
        engine().jsExecute("window.jdiRecorder.stop();");
        String stream = "";
        Timer timer = new Timer(renderTimeout);
        while (stream.length() < 10 && timer.isRunning()) {
            stream = engine().jsExecute("return window.jdiVideoBase64;");
        }
        return new StreamToImageVideo(stream, imageType);
    }

    public StreamToImageVideo stopRecordingAndSave() {
        return stopRecordingAndSave(VIDEO_WEBM);
    }

    public StreamToImageVideo recordCanvasVideo(int sec) {
        return recordCanvasVideo(VIDEO_WEBM, sec);
    }

    public StreamToImageVideo recordCanvasVideo(ImageTypes imageType, int sec) {
        startRecording(imageType);
        Timer.sleep((sec+1) * 1000L);
        return stopRecordingAndSave(imageType);
    }

    // Experimental record video for any element
    public StreamToImageVideo recordVideo(int sec) {
        engine().jsExecute("await import(`https://html2canvas.hertzen.com/dist/html2canvas.min.js`)");
        getElement(Whammy.script);
        Timer.sleep((sec+5) * 1000L);
        engine().jsExecute("jdi.recording = false; jdi.compile();");
        String stream = "";
        Timer timer = new Timer(renderTimeout);
        while (stream.length() < 10 && timer.isRunning()) {
            stream = engine().jsExecute("return jdi.videoBase64");
        }
        return new StreamToImageVideo(stream, VIDEO_WEBM);
    }

    public JS setObjectMapping(String objectMap, Class<?> cl) {
        this.objectMap = objectMap;
        this.engine().setupEntity(cl);
        return this;
    }

    public JsonObject getJSObject(String json) {
        return engine().getJson(json);
    }

    public <T> T getEntity(Class<T> cl) {
        engine().setupEntity(cl);
        return engine().getEntity(GET_OBJECT_MAP.apply(cl));
    }

    public <T> T getEntity() {
        return engine().getEntity(objectMap);
    }

    public <T> T getEntity(String objectMap, Class<T> cl) {
        engine().setupEntity(cl);
        return engine().getEntity(objectMap);
    }

    public void setEntity(String objectMap) {
        engine().setMultipleValues(objectMap);
    }

    public JS children() {
        return find("*");
    }

    public JS ancestor() {
        return find("/..");
    }

    public List<String> allValues(String getTextType) {
        return getAttributesAsList(getTextType);
    }

    public List<String> allValues() {
        return allValues(textType());
    }

    public int size() {
        By lastLocator = last(locators());
        return lastLocator.toString().contains("%s")
            ? GET_SIZE_FROM_TEMPLATE.apply(lastLocator)
            : useFilter(() -> engine().getSize());
    }

    public Function<By, Integer> GET_SIZE_FROM_TEMPLATE = this::getTemplateSize;

    private int getTemplateSize(By lastLocator) {
        JS result = copy();
        String[] split = getByLocator(lastLocator).split("\\[");
        String baseLocator = split[0];
        if (split.length == 1) {
            THROW_ASSERT.accept(format("Failed to get size for template locator(%s). Please remove %s from your locator", lastLocator));
        }
        if (split.length > 2) {
            baseLocator = "";
            for (int i = 0; i < split.length - 1; i++) {
                baseLocator += split[i];
            }
        }
        int length = baseLocator.length();
        char lastChar = baseLocator.charAt(length - 1);
        String newLocator = lastChar == ' ' || lastChar == '>'
            ? baseLocator.substring(0, length - 1)
            : baseLocator;
        result.jsDriver().replaceLocator(By.cssSelector(newLocator));
        return result.size();
    }

    public List<JsonObject> getObjectList(String json) {
        return useFilter(() -> engine().getJsonList(json));
    }

    public <T> List<T> getEntityList() {
        return engine().getEntityList(objectMap);
    }

    public void setEntity() {
        engine().setMultipleValues(objectMap);
    }

    public <T> List<T> getEntityList(Class<T> cl) {
        if (objectMap == null) {
            objectMap = GET_OBJECT_MAP.apply(cl);
        }
        return useFilter(() -> getEntityList(objectMap, cl));
    }

    public JS fill(Object obj) {
        return setEntity(obj);
    }

    public JS submit(Object obj, String locator) {
        setEntity(obj);
        find(locator).click();
        return this;
    }

    public JS submit(Object obj) {
        return submit(obj, SUBMIT_LOCATOR);
    }

    public JS loginAs(Object obj, String locator) {
        return submit(obj, locator);
    }

    public JS loginAs(Object obj) {
        return submit(obj);
    }

    public JS setEntity(Object obj) {
        engine().setMultipleValues(print(getMapList(obj), "\n"));
        return this;
    }

    public JS findTemplate(String value) {
        JS result = copy();
        result.jsDriver().fillLocatorTemplate(value);
        result.setName(getName() + "(" + value + ")");
        return result;
    }

    public JS find(String by) {
        return find(NAME_TO_LOCATOR.apply(by));
    }

    public JS find(String by, RuleType previous) {
        return find(NAME_TO_LOCATOR.apply(by), previous);
    }

    public JS find(By by) {
        return find(by, null);
    }

    public JS find(By by, RuleType previous) {
        JS result = copy();
        if (previous == null) {
            result.jsDriver().addLocator(by);
        } else {
            result.jsDriver().addLocator(by, previous);
        }
        result.setName(getName() + "('" + getByLocator(by) + "')");
        return result;
    }

    public JS addJSCode(String script, String name) {
        return addJSCode(script, RuleType.List, name);
    }

    public JS addJSCode(String script, RuleType type, String name) {
        JS result = copy();
        result.jsDriver().addScript(script, type);
        result.setName(getName() + name);
        return result;
    }

    protected List<String> getMapList(Object obj) {
        List<Field> allFields = getFieldsDeep(obj);
        List<String> mapList = new ArrayList<>();
        for (Field field : allFields) {
            Object fieldValue = getValueField(field, obj);
            if (fieldValue == null) {
                continue;
            }
            String value = SET_COMPLEX_VALUE.apply(field, fieldValue);
            if (value != null) {
                mapList.add(value);
            }
        }
        return mapList;
    }

    public <T> List<T> getEntityList(String objectMap, Class<?> cl) {
        engine().setupEntity(cl);
        return engine().getEntityList(objectMap);
    }

    public JS setEntityList(String objectMap) {
        engine().setMap(objectMap);
        return this;
    }

    public JS get(int index) {
        return addJSCode("element = elements[" + index + "];\n", "[" + index + "]");
    }

    public JS get(String by, int index) {
        return get(NAME_TO_LOCATOR.apply(by), index);
    }

    public JS get(By by, int index) {
        String script = "element = elements.filter(e =>e && "+
                MessageFormat.format(dataType(by).get, "e", selector(by, jsDriver().builder()))+
                ")[" + index + "];\n";
        return addJSCode(script, "[" + index + "]");
    }

    public JS get(String value) {
        if (value == null) {
            throw new JDINovaException("get(null) failed. Value can't be null");
        }
        if (isEmpty(locators())) {
            throw new JDINovaException("get(" + value + ") failed. Element should have locator");
        }
        By lastLocator = last(locators());
        if (lastLocator.toString().contains("%s")) {
            return findTemplate(value);
        } else {
            JS js = findFirst(valueCondition(value));
            js.setName(format("%s[%s]",getName(), value));
            return js;
        }
    }

    protected String valueCondition(String value) {
        return format("%s === '%s'", setStringAction("#element#", valueFunc()), value);
    }
    protected String valueFunc;

    protected String valueFunc() {
        return valueFunc == null ? GET_TEXT_DEFAULT : valueFunc;
    }

    public JS setGetValueFunc(String getValueFunc) {
        this.valueFunc = getValueFunc;

        return this;
    }

    public long indexOf(Function<JS, String> condition) {
        return jsDriver().indexOf(condition.apply(this));
    }

    public JS findFirst(String by, Function<JS, String> condition) {
        return findFirst(NAME_TO_LOCATOR.apply(by), condition.apply(this));
    }

    public JS findFirst(By by, Function<JS, String> condition) {
        return findFirst(by, condition.apply(this));
    }

    public JS findFirst(Function<JS, String> condition) {
        return findFirst(condition.apply(this));
    }

    public JS findFirst(String by, String condition) {
        return findFirst(NAME_TO_LOCATOR.apply(by), condition);
    }

    public JS findFirst(By by, String condition) {
        String script = "element = elements.find(e => { fel = " +
            MessageFormat.format(dataType(by).get, "e", selector(by, builder())) + "; " +
                "return fel && " + handleCondition(condition, "fel") + "; });\n" +
                "if (!element) { throw 'Failed to find element' };\n";
        return addJSCode(script, ".first");
    }

    public JS findFirst(String condition) {
        return addJSCode("element = elements.find(e => e && " + handleCondition(condition, "e") + ");\n" +
            "if (!element) { throw 'Failed to find element' };\n",
            ".first");
    }

    protected String handleCondition(String condition, String elementName) {
        return condition.contains("#element#")
            ? condition.replace("#element#", elementName)
            : elementName + "." + condition;
    }

    public boolean isNotCovered() {
        Dimension dimension = getSize();
        if (dimension.getWidth() == 0) {
            return false;
        }
        return isNotCovered(dimension.getWidth() / 2, dimension.getHeight() / 2 - 1);
    }

    public JS uploadFile(String filePath) {
        we().click();
        String pathToPaste = new File(filePath).getAbsolutePath();
        pasteText(pathToPaste);
        return this;
    }

    public JS press(Keys key) {
        Keyboard.press(key);
        return this;
    }

    public JS commands(String... commands) {
        Keyboard.commands(commands);
        return this;
    }

    public boolean isNotCovered(int xOffset, int yOffset) {
        return getElement("rect = element.getBoundingClientRect();\n" +
            "cx = rect.left + " + xOffset + ";\n" +
            "cy = rect.top + " + yOffset + ";\n" +
            "e = document.elementFromPoint(cx, cy);\n" +
            "for (; e; e = e.parentElement) {\n" +
            "  if (e === element)\n" +
            "    return true;\n" +
            "}\n" +
            "return false;").equals("true");
    }

    public String fontColor() {
        return engine().color();
    }

    public String bgColor() {
        return engine().bgColor();
    }

    public String pseudo(String name, String value) {
        return engine().pseudo(name, value);
    }

    public JS focus() {
        engine().doAction("dispatchEvent(new Event('focus', { 'bubbles': true }));");
        return this;
    }

    public JS blur() {
        engine().doAction("dispatchEvent(new Event('blur', { 'bubbles': true }));");
        return this;
    }

    public boolean focused() {
        return getElement("element === document.activeElement").equalsIgnoreCase("true");
    }

    public List<By> locators() {
        List<By> locators = jsDriver().locators();
        if (locators == null) {
            return new ArrayList<>();
        }
        return filter(locators, Objects::nonNull);
    }

    public JSImages imagesData() {
        if (imagesData == null) {
            imagesData = new JSImages();
        }
        return imagesData;
    }

    public File getImageFile() {
        return getImageFile("");
    }

    public File getImageFile(String tag) {
        return imagesData().images.has(tag) ? new File(imagesData().images.get(tag)) : null;
    }

    public JS visualValidation() {
        visualValidation("");
        return this;
    }

    public JS visualValidation(String tag) {
        VISUAL_VALIDATION.accept(tag, this);
        return this;
    }

    public JS visualCompareWith(JS element) {
        COMPARE_IMAGES.apply(imagesData().imageFile, element.imagesData().imageFile);
        return this;
    }

    public Direction getDirectionTo(WebElement element) {
        Rectangle elementCoordinates = getRect();
        Rectangle destinationCoordinates = element.getRect();
        Direction direction = new Direction(getCenter(elementCoordinates), getCenter(destinationCoordinates));
        if (isInterface(element.getClass(), HasCore.class)) {
            JS core = ((HasCore)element).core();
            if (relations == null) {
                relations = new MapArray<>(core.toString(), direction);
            } else {
                relations.update(core.toString(), direction);
            }
        }
        return direction;
    }

    public boolean relativePosition(JS element, Direction expected) {
        return COMPARE_POSITIONS.apply(getDirectionTo(element), expected);
    }

    public OfElement isOn(Function<Direction, Boolean> expected) {
        return new OfElement(expected, this);
    }

    public boolean relativePosition(JS element, Function<Direction, Boolean> expected) {
        return expected.apply(getDirectionTo(element));
    }

    public MapArray<String, Direction> relations;

    public JS clearRelations() {
        relations = null;
        return this;
    }

    public MapArray<String, Direction> getRelativePositions(JS... elements) {
        relations = new MapArray<>();
        for (JS element : elements) {
            relations.update(element.getName(), getDirectionTo(element));
        }
        storeRelations(this, relations);
        return relations;
    }

    protected boolean similar(Pair<String, Direction> relation, Direction expectedRelation) {
        return VECTOR_SIMILARITY.apply(relation.value, expectedRelation);
    }

    public List<String> validateRelations() {
        MapArray<String, Direction> storedRelations = readRelations(this);
        if (isEmpty(storedRelations)) {
            return newList("No relations found in: " + RELATIONS_STORAGE);
        }
        List<String> failures = new ArrayList<>();
        if (isEmpty(relations)) {
            return newList("No element relations found: use getRelativePosition(...) first and save element relations");
        }
        MapArray<String, Direction> newRelations = getRelations(storedRelations, failures);
        if (isNotEmpty(newRelations)) {
            storeRelations(this, newRelations);
        }
        return failures;
    }

    protected MapArray<String, Direction> getRelations(MapArray<String, Direction> storedRelations, List<String> failures) {
        MapArray<String, Direction> newRelations = new MapArray<>();
        for (Pair<String, Direction> relation : relations) {
            if (storedRelations.has(relation.key)) {
                checkRelations(storedRelations.get(relation.key), relation, failures);
            } else {
                newRelations.add(relation);
            }
        }
        return newRelations;
    }

    protected void checkRelations(Direction expectedRelation,  Pair<String, Direction> relation, List<String> failures) {
        if (similar(relation, expectedRelation)) {
            return;
        }
        failures.add(format("Elements '%s' and '%s' are misplaced: angle: %s => %s; length: %s => %s",
            toString(), relation.key, relation.value.angle(), expectedRelation.angle(),
            relation.value.length(), expectedRelation.length()));
    }

    public Point getCenter() {
        return getCenter(getRect());
    }

    protected Point getCenter(Rectangle rect) {
        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height / 2;
        return new Point(x, y);
    }

    public JSEngine engine() {
        return engine;
    }

    @Override
    public String toString() {
        if (isBlank(name)) {
            return printLocators();
        }
        return format("%s(%s%s)", getName(), getClassVarName(), printLocators());
    }

    public String getClassVarName() {
        if (parent() != null && isNotBlank(varName)) {
            String parentName = parent().getClass().getSimpleName();
            String prefix = isNotBlank(parentName) ? parentName + "." : "";
            return prefix + varName + ": ";
        }
        return "";
    }

    protected void init() { }

    public WebDriver driver() {
        return this.driver.get();
    }

    public JavascriptExecutor js() {
        return (JavascriptExecutor) driver();
    }

    public JS core() { return this; }

    public JS setCore(JS core) {
        if (!isClass(core.getClass(), JSLight.class)) {
            return this;
        }
        JSLight jsLight = (JSLight) core;
        updateFrom(jsLight);
        return this;
    }

    public JSLight copy() {
        return new JSLight(driver).updateFrom(this);
    }

    public <T> T updateFrom(JS js) {
        if (!isClass(js.getClass(), JSLight.class)) {
            return (T) this;
        }
        JSLight jsLight = (JSLight) js;
        this.engine.updateFrom(jsLight.engine);
        this.actions = jsLight.actions;
        this.name = jsLight.name;
        this.varName = jsLight.varName;
        this.parent = jsLight.parent;
        this.imagesData = jsLight.imagesData;
        this.renderTimeout = jsLight.renderTimeout;
        this.objectMap = jsLight.objectMap;
        return (T) this;
    }

    /**
     * @param valueFunc = element !== null
     *        valueFunc = element !== null && styles.visibility === 'visible'
     *        ...
     * @return value
     */
    public String getElement(String valueFunc) {
        return engine().getValue(valueFunc);
    }

    public List<String> getList(String valueFunc) {
        return useFilter(() -> engine().getValues(valueFunc));
    }

    protected  <T> T useFilter(Supplier<T> func) {
        T result;
        if (jsDriver().hasFilter() || defaultFilter == null) {
            result = func.get();
        } else {
            jsDriver().setFilter(defaultFilter);
            try {
                result = func.get();
            } finally {
                jsDriver().setFilter(null);
            }
        }
        return result;
    }

    public String filterElements(String valueFunc) {
        return useFilter(() -> engine().firstValue(valueFunc));
    }

    /**
     * @param action = getAttribute('value')
     *        action = innerText;
     *        ...
     * @return value
     */
    public String getJSResult(String action) {
        return engine().getProperty(action);
    }

    public JS setOption(String option) {
        if (option == null) {
            return this;
        }
        engine().doAction("option.value = " + option + ";\nelement.dispatchEvent(new Event('change'));");
        return this;
    }

    public JS selectByName(String name) {
        if (name == null) {
            return this;
        }
        engine().doAction("dispatchEvent(new Event('change'));\n" +
                "element.selectedIndex = [...element.options]" +
                ".findIndex(option => option.text === '" + name + "');\n" +
                "element.dispatchEvent(new Event('change'));");
        return this;
    }

    public String selectedValueOption() {
        return core().getJSResult("selectedOptions[0].value").trim();
    }

    public String selectedOption() {
        return core().getJSResult("selectedOptions[0]?.innerText ?? ''").trim();
    }

    public JS doAction(String action) {
        engine().doAction(action);
        return this;
    }

    public WebElement rawWe() {
        if (isEmpty(locators())) {
            throw new JDINovaException("Failed to use we() because element has no locators");
        }
        SearchContext ctx = driver();
        for (By locator : locators()) {
            List<WebElement> elements = ctx.findElements(locator);
            ctx = getContext(elements);
        }
        return (WebElement) ctx;
    }

    private SearchContext getContext(List<WebElement> elements) {
        switch (elements.size()) {
            case 0:
                THROW_ASSERT.accept("Failed to find element (" + this + ")");
                return driver();
            case 1:
                return elements.get(0);
            default:
                WebElement visible = LinqUtils.first(elements, WebElement::isDisplayed);
                return visible != null ? visible : elements.get(0);
        }
    }

    protected int elementTimeout = -1;

    public int elementTimeout() {
        return elementTimeout < 0 ? timeout : elementTimeout;
    }

    public int setTimeout(int timeoutSec) {
        return elementTimeout = timeoutSec;
    }

    public WebElement we() {
        Timer timer = new Timer(elementTimeout());
        while (timer.isRunning()) {
            try {
                WebElement element = rawWe();
                element.getTagName();
                return element;
            } catch (Exception ignore) { }
        }
        return rawWe();
    }

    public JS weElementActions(BiFunction<Actions, WebElement, Actions> action) {
        action.apply(actions.get().moveToElement(we()), we()).build().perform();
        return this;
    }

    public JS weActions(BiFunction<Actions, WebElement, Actions> action) {
        action.apply(actions.get(), we()).build().perform();
        return this;
    }

    public String getName() {
        return NAME_FUNC.apply(this);
    }

    protected String printLocators() {
        String result = "";
        boolean first = false;
        for (By by : locators()) {
            if (by != null) {
                if (first) {
                    result += " > ";
                }
                result += getByType(by) + ":'" + getByLocator(by) + "'";
                first = true;
            }
        }
        return result;
    }

    public JS setName(String name) {
        this.name = name;
        return this;
    }

    public void setVarName(Field field) {
        this.varName = field.getName();
    }

    public Object parent() {
        return this.parent;
    }

    public JS setParent(Object parent) {
        this.parent = parent;
        if (parent != null && isInterface(parent.getClass(), HasCore.class)) {
            this.engine().updateFrom(((HasCore) parent).engine());
        }
        return this;
    }

    public JS setFilter(String filter) {
        this.filter = filter;
        this.defaultFilter = filter;
        jsDriver().setFilter(filter);
        return this;
    }

    protected void withFilter(String filter) {
        jsDriver().setFilter(this.filter != null ? this.filter : filter);
    }
}
