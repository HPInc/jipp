package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding Boolean attributes */
public class BooleanAttributes {

    /** Return a new boolean attribute builder */
    public static Attribute.Builder<Boolean> builder(Tag valueTag) {
        return Attribute.builder(ENCODER, valueTag);
    }

    /** Return a new boolean attribute */
    public static Attribute<Boolean> create(Tag valueTag, String name, Boolean... values) {
        return builder(valueTag).setValues(values).setName(name).build();
    }

    static Attribute.Encoder<Boolean> ENCODER = new Attribute.Encoder<Boolean>() {
        @Override
        public void writeValue(DataOutputStream out, Boolean value) throws IOException {
            out.writeShort(1);
            out.writeByte(value ? 0x01 : 0x00);
        }

        @Override
        public Boolean readValue(DataInputStream in, Tag valueTag) throws IOException {
            int length = in.readShort();
            if (length != 1) {
                throw new IOException("tag " + valueTag + " value expected 1 got " + length);
            }
            return in.readByte() != 0;
        }

        @Override
        public Attribute.Builder<Boolean> builder(Tag valueTag) {
            return BooleanAttributes.builder(valueTag);
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.BooleanValue;
        }
    };
}
