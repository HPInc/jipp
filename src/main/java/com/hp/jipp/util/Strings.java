package com.hp.jipp.util;

public class Strings {
    public static String join(String separator, Iterable<Object> items) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object item : items) {
            if (first) first = false;
            else builder.append(separator);
            builder.append(item);
        }
        return builder.toString();
    }
}
