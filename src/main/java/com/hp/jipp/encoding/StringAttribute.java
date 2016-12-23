package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringAttribute {

    /** Return a new integer attribute builder */
    public static Attribute.Builder<String> builder(Tag valueTag) {
        return Attribute.builder(ENCODER, valueTag);
    }

    /** Return a new integer attribute */
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
            return StringAttribute.builder(valueTag);
        }

        @Override
        boolean valid(Tag valueTag) {
            return (valueTag.getValue() & 0x40) == 0x40;
        }
    };
}
