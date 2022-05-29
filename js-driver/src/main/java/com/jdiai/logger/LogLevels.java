package com.jdiai.logger;

public enum LogLevels {
    OFF(0),
    FATAL(100),
    ERROR(200),
    WARN(300),
    INFO(400),
    DEBUG(500),
    TRACE(600),
    ALL(2147483647);

    public int value;
    public boolean isLower(LogLevels logLevel) {
        return this.value < logLevel.value;
    }

    LogLevels(final int val) {
        this.value = val;
    }

}
