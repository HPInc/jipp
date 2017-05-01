package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;

/**
 * A machine-readable integer code paired with a human-readable name.
 */
public abstract class NameCode {
    public abstract String getName();
    public abstract int getCode();

    /** A factory for objects of a NameCode subclass */
    public interface Factory<T extends NameCode> {
        T of(String name, int code);
    }

    /** Convert a List of T into a Map of integers to T, where T is a NameCode subclass. */
    public static <T extends NameCode> Map<Integer, T> toMap(Collection<T> nameCodes) {
        ImmutableMap.Builder<Integer, T> builder = new ImmutableMap.Builder<>();
        for (T nameCode : nameCodes) {
            //noinspection ResultOfMethodCallIgnored
            builder.put(nameCode.getCode(), nameCode);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return getName();
    }
}
