package com.hp.jipp.encoding;

import java.io.IOException;

public class ParseError extends IOException {
    public ParseError(String s) {
        super(s);
    }
}
