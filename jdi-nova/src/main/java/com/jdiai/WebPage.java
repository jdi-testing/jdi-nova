package com.jdiai;

import com.jdiai.interfaces.HasName;
import com.jdiai.interfaces.ISetup;
import com.jdiai.tools.Timer;

import java.io.File;
import java.lang.reflect.Field;

import static com.jdiai.JDI.*;
import static com.jdiai.JDIStatistic.trackAsserts;
import static com.jdiai.asserts.ShouldUtils.waitForResult;
import static com.jdiai.jsdriver.JDINovaException.assertContains;
import static com.jdiai.listeners.JDIEvents.*;
import static com.jdiai.page.objects.PageFactory.initPageElements;
import static com.jdiai.page.objects.PageFactoryUtils.getPageHeader;
import static com.jdiai.page.objects.PageFactoryUtils.*;
import static com.jdiai.tools.BrowserTabs.getWindowHandles;
import static com.jdiai.tools.BrowserTabs.openNewTabPage;
import static java.lang.Long.valueOf;
import static java.lang.String.format;

public class WebPage implements HasName, ISetup {
    private String url;
    private String title;
    private String pageHeader;
    private String name;

    public WebPage() {
        initPageElements(this);
    }

    public void open() {
        String openPageAction = "openPage()";
        String openPageStep = format("Open page '%s'(%s)", name, url);
        fireEvent(BEFORE_ACTION_EVENT, openPageAction, openPageStep, null);
        Timer timer = new Timer();
        try {
            openPage(url);
            getWindowHandles();
            fireEvent(AFTER_SUCCESS_ACTION_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec());
        } catch (Exception ex){
            fireEvent(AFTER_ACTION_FAIL_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec(), ex, null);
        } finally {
            fireEvent(AFTER_ACTION_EVENT, openPageAction, openPageStep, null, null, valueOf(timeout), timer.timePassedInMSec());
        }
    }

    public void shouldBeOpened() {
        trackAsserts(
            () -> waitForResult(() -> getUrl().contains(url)),
            "validate page url",
            "Validate that page url contains '" + url + "'",
            format("Page has url '%s' but expected '%s'", getUrl(), url),
            null
        );
        if (title != null) {
            String actualTitle = getTitle();
            trackAsserts(
                () -> assertContains("Page Title", actualTitle, title),
                "validate page title",
                "Validate that page has title '" + title + "'",
                format("Page has title '%s' but expected '%s'", actualTitle, title),
                null
            );
        }
        if (pageHeader != null) {
            String h1 = $("h1").getText();
            trackAsserts(
                () -> assertContains("Page Header", h1, pageHeader),
                "validate page header",
                "Validate that page has header(<h1> tag) '" + pageHeader + "'",
                format("Page has header(<h1> tag) '%s' but expected '%s'", h1, pageHeader),
                null
            );
        }
    }

    public void setup(Field field) {
        Class<?> fieldClass = field.getType();
        this.url = getPageUrl(fieldClass, field);
        this.title = getPageTitle(fieldClass, field);
        this.pageHeader = getPageHeader(fieldClass, field);
    }

    public String getName() {
        return name;
    }

    public WebPage setName(String name) {
        this.name = name;
        return this;
    }

    public void openAsNewTab() {
        openAsNewTab(getName());
    }

    public void openAsNewTab(String tabName) {
        openNewTabPage(url, tabName);
    }

    public File makeScreenshot() {
        return $("body")
            .setName(getName()).core()
            .makeScreenshot(getName());
    }
}
