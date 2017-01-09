package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding Integer attributes */
public class IntegerEncoder extends AttributeEncoder<Integer> {

    private static IntegerEncoder INSTANCE = new IntegerEncoder();

    /** Return the singleton encoder */
    static IntegerEncoder getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeValue(DataOutputStream out, Integer value) throws IOException {
        out.writeShort(4);
        out.writeInt(value);
    }

    @Override
    public Integer readValue(DataInputStream in, Tag valueTag) throws IOException {
        expectLength(in, 4);
        return in.readInt();
    }

    @Override
    boolean valid(Tag valueTag) {
        return valueTag == Tag.IntegerValue || valueTag == Tag.EnumValue;
    }
}
