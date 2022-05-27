package com.jdiai;

import com.google.gson.JsonObject;
import com.jdiai.interfaces.HasCore;
import com.jdiai.interfaces.HasLocators;
import com.jdiai.interfaces.HasParent;
import com.jdiai.jsdriver.RuleType;
import com.jdiai.jsproducer.Json;
import com.jdiai.tools.ClientRect;
import com.jdiai.tools.GetTextTypes;
import com.jdiai.tools.JSImages;
import com.jdiai.tools.map.MapArray;
import com.jdiai.visual.Direction;
import com.jdiai.visual.ImageTypes;
import com.jdiai.visual.OfElement;
import com.jdiai.visual.StreamToImageVideo;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface JS extends WebElement, HasLocators, HasParent, HasCore {
    String getElement(String valueFunc);
    List<String> getList(String valueFunc);
    String filterElements(String valueFunc);
    String getJSResult(String action);
    JS setOption(String option);
    JS selectByName(String name);
    String selectedValueOption();
    String selectedOption();
    String getText();
    boolean isDisplayed();
    default boolean isHidden() {
        return !isDisplayed();
    }
    default boolean isEnabled() {
        return !isDisabled();
    }
    boolean isDisabled();
    boolean isVisible();
    boolean isInView();
    default boolean isNotVisible() {
        return !isVisible();
    }
    default boolean isOutOfView() {
        return !isInView();
    }
    boolean isExist();
    boolean isNotCovered();
    default boolean isNotExist() {
        return !isExist();
    }
    JS weElementActions(BiFunction<Actions, WebElement, Actions> action);
    JS weActions(BiFunction<Actions, WebElement, Actions> action);
    JS clickCenter();
    JS click(int x, int y);
    JS select();
    void select(String value);
    void select(String... values);
    <TEnum extends Enum<?>> void select(TEnum name);
    JS check(boolean condition);
    JS check();
    JS uncheck();
    JS rightClick();
    JS doubleClick();
    JS hover();
    JS dragAndDropTo(WebElement to);
    JS dragAndDropTo(int x, int y);
    JS input(CharSequence... value);
    JS slide(String value);
    String tag();
    String getProperty(String property);
    Json getJson(String valueFunc);
    List<String> getAttributesAsList(String attr);
    Json allAttributes();
    JS highlight(String color);
    Json styles(String... style);
    Json allStyles();
    boolean isDeselected();
    JS setTextType(GetTextTypes textType);
    String getText(String textType);
    ClientRect getClientRect();
    File makeScreenshot(String tag);
    JS startRecording();
    JS startRecording(ImageTypes imageType);
    StreamToImageVideo stopRecordingAndSave(ImageTypes imageType);
    StreamToImageVideo stopRecordingAndSave();
    StreamToImageVideo recordCanvasVideo(int sec);
    StreamToImageVideo recordCanvasVideo(ImageTypes imageType, int sec);
    // Experimental record video for any element
    StreamToImageVideo recordVideo(int sec);
    JS setObjectMapping(String objectMap, Class<?> cl);
    JsonObject getJSObject(String json);
    <T> T getEntity();
    <T> T getEntity(String objectMap, Class<T> cl);
    void setEntity(String objectMap);
    JS find(String by);
    JS find(String by, RuleType previous);
    JS findTemplate(String value);
    JS find(By by);
    JS find(By by, RuleType previous);
    JS addJSCode(String script, String name);
    JS addJSCode(String script, RuleType type, String name);
    JS children();
    JS ancestor();
    List<String> allValues(String getTextType);
    List<JsonObject> getObjectList(String json);
    <T> List<T> getEntityList();
    void setEntity();
    JS fill(Object obj);
    JS submit(Object obj, String locator);
    JS submit(Object obj);
    JS loginAs(Object obj, String locator);
    JS loginAs(Object obj);
    JS setEntity(Object obj);
    <T> List<T> getEntityList(String objectMap, Class<?> cl);
    JS setEntityList(String objectMap);
    JS findFirst(String by, Function<JS, String> condition);
    JS findFirst(By by, Function<JS, String> condition);
    JS findFirst(String by, String condition);
    JS get(int index);
    JS get(String by, int index);
    JS get(By by, int index);
    JS get(String value);
    JS findFirst(Function<JS, String> condition);
    JS findFirst(String condition);
    JS findFirst(By by, String condition);
    long indexOf(Function<JS, String> condition);
    JS uploadFile(String filePath);
    JS press(Keys key);
    JS commands(String... commands);
    boolean isNotCovered(int xOffset, int yOffset);
    String fontColor();
    String bgColor();
    JS focus();
    JS blur();
    JSImages imagesData();
    File getImageFile();
    File getImageFile(String tag);
    JS visualValidation();
    JS visualValidation(String tag);
    JS visualCompareWith(JS element);
    boolean relativePosition(JS element, Direction expected);
    OfElement isOn(Function<Direction, Boolean> expected);
    boolean relativePosition(JS element, Function<Direction, Boolean> expected);
    JS clearRelations();
    MapArray<String, Direction> getRelativePositions(JS... elements);
    List<String> validateRelations();
    Point getCenter();
    String textType();

    JS setParent(Object parent);

    JS setFilter(String filter);
    JS setGetValueFunc(String getValueFunc);
    String getClassVarName();
    int elementTimeout();

    void setVarName(Field field);
    JS copy();
    default String scriptGetOne() {
        return jsDriver().buildOne().getQuery();
    }
    default String scriptGetList() {
        return jsDriver().buildList().getQuery();
    }
}
