package com.hp.jipp.encoding;

public abstract class NameCode {
    public abstract String getName();
    public abstract int getCode();

    @Override
    public String toString() {
        return getName();
    }
}
