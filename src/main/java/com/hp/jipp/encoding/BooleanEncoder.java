package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding Boolean attributes */
public class BooleanEncoder extends AttributeEncoder<Boolean> {
    private static final BooleanEncoder INSTANCE = new BooleanEncoder();
    public static BooleanEncoder getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeValue(DataOutputStream out, Boolean value) throws IOException {
        out.writeShort(1);
        out.writeByte(value ? 0x01 : 0x00);
    }

    @Override
    public Boolean readValue(DataInputStream in, Tag valueTag) throws IOException {
        expectLength(in, 1);
        return in.readByte() != 0;
    }

    @Override
    boolean valid(Tag valueTag) {
        return valueTag == Tag.BooleanValue;
    }
}
