package com.hp.jipp.util;

/** An error in creation of anything intended to be sent */
public class BuildError extends RuntimeException {
    public BuildError(String s) {
        super(s);
    }
}
