package com.jdiai.logger;

import com.jdiai.tools.ILogger;
import com.jdiai.tools.Timer;

import static java.lang.String.format;

public class ConsoleLogger implements ILogger {
    public ConsoleLogger() { }

    public void trace(String msg, Object... args) {
        System.out.printf("[TRACE %s]: %s%n", nowTime(), format(msg, args));
    }

    public void debug(String msg, Object... args) {
        System.out.printf("[DEBUG %s]: %s%n", nowTime(), format(msg, args));
    }

    public void info(String msg, Object... args) {
        System.out.printf("[INFO %s]: %s%n", nowTime(), format(msg, args));
    }

    public void step(String msg, Object... args) {
        System.out.printf("[STEP %s]: %s%n", nowTime(), format(msg, args));
    }

    public void error(String msg, Object... args) {
        System.out.printf("[ERROR %s]: %s%n", nowTime(), format(msg, args));
    }

    private String nowTime() {
        return Timer.nowTime("mm:ss.SSS");
    }
}
