package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerType extends AttributeType<Integer> {
    static Attribute.Encoder<Integer> ENCODER = new Attribute.Encoder<Integer>() {

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
    };

    public IntegerType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
