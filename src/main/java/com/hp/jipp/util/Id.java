package com.hp.jipp.util;

import com.google.auto.value.AutoValue;

/**
 * An identifier having a name and value
 */
@AutoValue
public abstract class Id {

    /**
     * Returns a new instance
     * @param name human-readable name corresponding to the identifier
     * @param value machine-readable value for the identifier
     */
    public static Id create(String name, int value) {
        return new AutoValue_Id(name, value);
    }

    abstract public String getName();
    abstract public int getValue();
}