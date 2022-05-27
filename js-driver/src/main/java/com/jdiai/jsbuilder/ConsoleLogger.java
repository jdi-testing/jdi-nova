package com.jdiai.jsbuilder;

import com.jdiai.tools.ILogger;

import static com.jdiai.jsbuilder.LogLevels.*;
import static com.jdiai.tools.StringUtils.format;
import static com.jdiai.tools.Timer.nowTimeShort;

public class ConsoleLogger implements ILogger {
    private final String name;
    LogLevels LOG_LEVEL = LogLevels.INFO;

    public ConsoleLogger(String name) {
        this.name = name;
    }

    public void setLogLevel(LogLevels newLevel) {
        this.LOG_LEVEL = newLevel;
    }

    public void trace(String msg, Object... args) {
        if (LOG_LEVEL.isLower(TRACE)) {
            return;
        }
        printMessage(TRACE, msg, args);
    }

    public void debug(String msg, Object... args) {
        if (LOG_LEVEL.isLower(DEBUG)) {
            return;
        }
        printMessage(DEBUG, msg, args);
    }

    public void info(String msg, Object... args) {
        if (LOG_LEVEL.isLower(INFO)) {
            return;
        }
        printMessage(INFO, msg, args);
    }

    public void error(String msg, Object... args) {
        if (LOG_LEVEL.isLower(ERROR)) {
            return;
        }
        printMessage(ERROR, msg, args);
    }

    private void printMessage(LogLevels logLevel, String msg, Object... args) {
        long threadId = Thread.currentThread().getId();
        String logInfo = threadId == 1
            ? logLevel.toString()
            : logLevel.toString() + ":" + threadId;
        System.out.printf("[%s] %s %s %n", logInfo, nowTimeShort(), format(msg, args));
    }
}
