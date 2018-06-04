package com.yeraygarcia.recipes.util;

import android.util.Log;

public class Debug {

    private static final String PREFIX = "YGQ: ";
    private static final boolean DEBUG = true;

    private Debug() {
        // avoid instantiation
    }

    public static void d(Object obj, String message) {
        if (DEBUG) {
            Log.d(PREFIX + obj.getClass().getSimpleName(), message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(PREFIX + tag, message);
        }
    }
}
