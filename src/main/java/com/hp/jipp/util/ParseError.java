package com.hp.jipp.util;

import java.io.IOException;

public class ParseError extends IOException {
    public ParseError(String s) {
        super(s);
    }
}
