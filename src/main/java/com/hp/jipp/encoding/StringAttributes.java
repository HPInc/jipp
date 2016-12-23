package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding String attributes */
public class StringAttributes {

    /** Return a new String attribute builder */
    public static Attribute.Builder<String> builder(Tag valueTag) {
        return Attribute.builder(ENCODER, valueTag);
    }

    /** Return a new String attribute */
    public static Attribute<String> create(Tag valueTag, String name, String... values) {
        return builder(valueTag).setValues(values).setName(name).build();
    }

    static Attribute.Encoder<String> ENCODER = new Attribute.Encoder<String>() {
        @Override
        public void writeValue(DataOutputStream out, String value) throws IOException {
            writeValueBytes(out, value.getBytes());
        }

        @Override
        public String readValue(DataInputStream in, Tag valueTag) throws IOException {
            return new String(readValueBytes(in));
        }

        @Override
        public Attribute.Builder<String> builder(Tag valueTag) {
            return StringAttributes.builder(valueTag);
        }

        @Override
        boolean valid(Tag valueTag) {
            return (valueTag.getValue() & 0x40) == 0x40;
        }
    };
}
