package com.hp.jipp.util;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Allow global customization of behavior
 */
public final class Hook {
    private static Map<String, Boolean> sBooleans = Collections.synchronizedMap(new HashMap<String, Boolean>());

    /** Retrieve a global Boolean value by name, or false if not set */
    public static boolean is(String name) {
        Optional<Boolean> result = Optional.fromNullable(sBooleans.get(name));
        return result.isPresent() ? result.get() : false;
    }

    /** Set a global Boolean value by name */
    public static void set(String name, Boolean value) {
        sBooleans.put(name, value);
    }

    public static void reset() {
        sBooleans.clear();
    }

    /** Not constructable */
    private Hook() {
    }
}
