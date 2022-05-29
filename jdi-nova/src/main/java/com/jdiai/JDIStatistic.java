package com.jdiai;

import com.jdiai.tools.Safe;
import com.jdiai.tools.Timer;
import com.jdiai.tools.func.JAction;
import com.jdiai.tools.map.MapArray;
import com.jdiai.tools.pairs.Pair;

import static com.jdiai.JDI.timeout;
import static com.jdiai.listeners.JDIEvents.*;
import static java.lang.Long.valueOf;

public class JDIStatistic {
    public static long shouldValidations = 0;
    public static Safe<MapArray<String, Pair<Long, Long>>> actionsTime = new Safe<>(new MapArray<>());

    public static void trackAsserts(JAction action, String actionName, String assertionToLog, String failAssertMessage, JS core) {
        shouldValidations ++;
        fireEvent(BEFORE_ACTION_EVENT, actionName, assertionToLog, core);
        Timer timer = new Timer();
        boolean success = false;
        try {
            action.invoke();
            success = true;
            fireEvent(AFTER_SUCCESS_ACTION_EVENT, actionName, assertionToLog, core, actionName + " Success", valueOf(timeout), timer.timePassedInMSec());
        } catch (Throwable ex) {
            fireEvent(AFTER_ACTION_FAIL_EVENT, actionName, assertionToLog, core, actionName + " Failed", valueOf(timeout), timer.timePassedInMSec(), ex, failAssertMessage);
        } finally {
            fireEvent(AFTER_ACTION_EVENT, actionName, assertionToLog, core, success ? actionName + " Success" : actionName + " Failed", valueOf(timeout), timer.timePassedInMSec());
        }
    }
}
