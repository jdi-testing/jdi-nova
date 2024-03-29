package com.jdiai.jswraper.driver;

import com.jdiai.jswraper.driver.download.DownloadDriverSettings;
import com.jdiai.jswraper.driver.download.GetDriverByPath;
import com.jdiai.jswraper.driver.remote.BrowserStackOptions;
import com.jdiai.jswraper.driver.remote.RemoteRunOptions;
import com.jdiai.jswraper.driver.remote.RemoteSettings;
import com.jdiai.jswraper.driver.remote.SelenoidOptions;

import java.util.function.Consumer;

import static com.jdiai.tools.PathUtils.mergePath;
import static com.jdiai.tools.PathUtils.path;

/**
 * Created by Roman Iovlev on 25.08.2021
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

public class RunModes {
    public RunModes(String runMode, RemoteSettings settings) {
        type = runMode;
        this.remote = true;
        remoteSettings = settings;
    }
    public RunModes(String runMode, DownloadDriverSettings settings) {
        type = runMode;
        this.remote = false;
        downloadSettings = settings;
    }
    public RunModes(String runMode, String driverPath) {
        type = runMode;
        this.remote = false;
        this.driverPath = new GetDriverByPath(driverPath);
    }
    public String type;
    public boolean remote = false;
    public DownloadDriverSettings downloadSettings;
    public RemoteSettings remoteSettings;
    public GetDriverByPath driverPath;

    public static RunModes LOCAL_DOWNLOAD = new RunModes("localdownload", new DownloadDriverSettings());

    public static RunModes LOCAL_DOWNLOAD(Consumer<DownloadDriverSettings> settings) {
        DownloadDriverSettings options = new DownloadDriverSettings();
        settings.accept(options);
        return new RunModes("localdownload", options);
    }

    public static RunModes LOCAL_BY_PATH = new RunModes("localbypath", mergePath(path(""), "src", "test", "resources", "drivers"));

    public static RunModes LOCAL_BY_PATH(String driverPath) {
        return new RunModes("localbypath", driverPath);
    }

    public static RunModes REMOTE = new RunModes("remote", new RemoteRunOptions());

    public static RunModes REMOTE(String url) {
        RemoteRunOptions options = new RemoteRunOptions(url);
        return new RunModes("remote", options);
    }

    public static RunModes REMOTE(Consumer<RemoteRunOptions> settings) {
        RemoteRunOptions options = new RemoteRunOptions();
        settings.accept(options);
        return new RunModes("remote", options);
    }

    public static RunModes SELENIUM_GRID = new RunModes("remote", new RemoteRunOptions());

    public static RunModes BROWSER_STACK = new RunModes("browserstack", new RemoteRunOptions());

    public static RunModes BROWSER_STACK(String userName, String accessKey) {
        BrowserStackOptions options = new BrowserStackOptions(userName, accessKey);
        return new RunModes("browserstack", options);
    }

    public static RunModes BROWSER_STACK(String userName, String accessKey, Consumer<BrowserStackOptions> settings) {
        BrowserStackOptions options = new BrowserStackOptions(userName, accessKey);
        settings.accept(options);
        return new RunModes("browserstack", options);
    }

    public static RunModes SELENOID = new RunModes("selenoid", new SelenoidOptions());

    public static RunModes SELENOID(String url) {
        SelenoidOptions options = new SelenoidOptions(url);
        return new RunModes("selenoid", options);
    }

    public static RunModes SELENOID(Consumer<SelenoidOptions> settings) {
        SelenoidOptions options = new SelenoidOptions();
        settings.accept(options);
        return new RunModes("selenoid", options);
    }

    public static RunModes getByName(String name) {
        switch (name.toLowerCase().replaceAll("[^a-z\\s]", "")) {
            case "localdownload":
                return LOCAL_DOWNLOAD;
            case "localbypath":
                return LOCAL_BY_PATH;
            case "remote":
                return REMOTE;
            case "selenium":
                return SELENIUM_GRID;
            case "browserstack":
                return BROWSER_STACK;
            case "selenoid":
                return SELENOID;
            default:
                return LOCAL_DOWNLOAD;
        }
    }
}