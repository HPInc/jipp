package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerType extends AttributeType<Integer> {
    private static final String TYPE_NAME = "Integer";

    static final Attribute.SimpleEncoder<Integer> ENCODER = new Attribute.SimpleEncoder<Integer>(TYPE_NAME) {
        @Override
        public Integer readValue(DataInputStream in, Tag valueTag) throws IOException {
            Attribute.expectLength(in, 4);
            return in.readInt();
        }

        @Override
        public void writeValue(DataOutputStream out, Integer value) throws IOException {
            out.writeShort(4);
            out.writeInt(value);
        }

        @Override
        public boolean valid(Tag valueTag) {
            return Tag.IntegerValue.equals(valueTag) || Tag.EnumValue.equals(valueTag);
        }
    };

    public IntegerType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
