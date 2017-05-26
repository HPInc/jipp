package com.hp.jipp.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class Util {
    public static final String UTF8 = "UTF-8";

    /** Use reflection to return all static, accessible, initialized objects in the class */
    public static List<Object> getStaticObjects(Class cls) {
        List<Object> objects = new ArrayList<>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            Object object;
            try {
                object = field.get(null);
            } catch (IllegalAccessException ignored) {
                object = null;
            }
            if (object != null) {
                objects.add(object);
            }
        }
        return objects;
    }
}
