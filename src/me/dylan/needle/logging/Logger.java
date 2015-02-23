package me.dylan.needle.logging;

import java.util.Calendar;

public class Logger {

    public static void log(String info, LogLevel level) {
        System.out.println("[" + Calendar.getInstance().getTime().toString() + "] [" + level.name() + "] " + info);
    }

    public static void info(String info) {
        log(info, LogLevel.INFO);
    }

}
