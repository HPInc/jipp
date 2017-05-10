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

    @Override
    public String toString() {
        return getName();
    }
}
