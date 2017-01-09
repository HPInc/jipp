package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding String attributes */
public class StringEncoder extends AttributeEncoder<String> {

    private static final StringEncoder INSTANCE = new StringEncoder();
    public static StringEncoder getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeValue(DataOutputStream out, String value) throws IOException {
        writeValueBytes(out, value.getBytes());
    }

    @Override
    public String readValue(DataInputStream in, Tag valueTag) throws IOException {
        return new String(readValueBytes(in));
    }

    @Override
    boolean valid(Tag valueTag) {
        return (valueTag.getValue() & 0x40) == 0x40;
    }
}
