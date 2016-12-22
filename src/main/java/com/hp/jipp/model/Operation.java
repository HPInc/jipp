package com.hp.jipp.model;

import com.google.auto.value.AutoValue;

/**
 * An operation identifier
 */
@AutoValue
public abstract class Operation {

    /**
     * Returns a new instance
     * @param name human-readable name of the the operation
     * @param value machine-readable identifier for the operation
     */
    public static Operation create(String name, int value) {
        return new AutoValue_Operation(name, value);
    }

    abstract public String getName();
    abstract public int getValue();
}