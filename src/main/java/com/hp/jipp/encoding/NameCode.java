package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

/**
 * An object holding a human-readable name and a corresponding machine-readable integer code.
 */
public abstract class NameCode {
    public abstract String getName();
    public abstract int getCode();

    /** A factory for objects of a NameCode subclass */
    public interface Factory<T extends NameCode> {
        T create(String name, int code);
    }

    /** Convert a set of T into a Map of integers to T, where T is a NameCode subclass. */
    public static <T extends NameCode> Map<Integer, T> toMap(Set<T> enums) {
        ImmutableMap.Builder<Integer, T> builder = new ImmutableMap.Builder<>();
        for (T e : enums) {
            //noinspection ResultOfMethodCallIgnored
            builder.put(e.getCode(), e);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return getName();
    }
}
