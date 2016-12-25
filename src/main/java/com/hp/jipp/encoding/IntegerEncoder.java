package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding Integer attributes */
public class IntegerEncoder extends Attribute.Encoder<Integer> {

    private static IntegerEncoder INSTANCE = new IntegerEncoder();

    /** Return the singleton encoder */
    static IntegerEncoder getInstance() {
        return INSTANCE;
    }

    /** Return a new integer attribute */
    public static Attribute<Integer> create(Tag valueTag, String name, Integer... values) {
        return INSTANCE.builder(valueTag).setValues(values).setName(name).build();
    }

    @Override
    public void writeValue(DataOutputStream out, Integer value) throws IOException {
        out.writeShort(4);
        out.writeInt(value);
    }

    @Override
    public Integer readValue(DataInputStream in, Tag valueTag) throws IOException {
        int length = in.readShort();
        if (length != 4) {
            throw new IOException("tag " + valueTag + " value expected 4 got " + length);
        }
        return in.readInt();
    }

    @Override
    public Attribute.Builder<Integer> builder(Tag valueTag) {
        return Attribute.builder(this, valueTag);
    }

    @Override
    boolean valid(Tag valueTag) {
        return valueTag == Tag.IntegerValue || valueTag == Tag.EnumValue;
    }
}
