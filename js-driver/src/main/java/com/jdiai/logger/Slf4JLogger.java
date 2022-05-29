package com.jdiai.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import static org.slf4j.MarkerFactory.getMarker;

public class Slf4JLogger implements JLogger {
    private Logger slf4j;
    private static Marker stepMarker = getMarker("STEP");
    boolean logEnabled = true;

    public Slf4JLogger(String name) {
        slf4j = LoggerFactory.getLogger(name);
    }

    public void loggerOff() {
        logEnabled = false;
    }
    public void loggerOn() {
        logEnabled = true;
    }

    public void trace(String msg, Object... args) {
        if (logEnabled) {
            slf4j.trace(msg);
        }
    }

    public void debug(String msg, Object... args) {
        if (logEnabled) {
            slf4j.debug(msg, args);
        }
    }

    public void info(String msg, Object... args) {
        if (logEnabled) {
            slf4j.info(msg, args);
        }
    }

    public void step(String msg, Object... args) {
        if (logEnabled) {
            slf4j.info(stepMarker, msg, args);
        }
    }

    public void error(String msg, Object... args) {
        if (logEnabled) {
            slf4j.error(msg, args);
        }
    }
}
