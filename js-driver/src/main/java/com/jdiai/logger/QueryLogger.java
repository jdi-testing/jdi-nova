package com.jdiai.logger;

import com.jdiai.tools.Safe;

public class QueryLogger {
    public static final int OFF = 0;
    public static final int RESULT = 1;
    public static final int ALL = 2;
    public static Safe<Integer> LOG_QUERY = new Safe<>(() -> OFF);
    public static final String LOGGER_NAME = "JDI";
    public static JLogger logger = new ThreadConsoleLogger(LOGGER_NAME);
}
