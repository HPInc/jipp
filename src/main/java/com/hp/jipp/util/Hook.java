package com.hp.jipp.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Allow global customization of behavior
 */
public class Hook {
    private static Map<String, Boolean> sBooleans = Collections.synchronizedMap(new HashMap<String, Boolean>());

    /** Retrieve a global Boolean value by name, or false if not set */
    public static boolean is(String name) {
        Boolean result = sBooleans.get(name);
        return result == null ? false : result;
    }

    /** Set a global Boolean value by name */
    public static void set(String name, Boolean value) {
        sBooleans.put(name, value);
    }

    public static void reset() {
        sBooleans.clear();
    }
}
