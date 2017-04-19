package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Represents a boolean attribute type */
public class BooleanType extends AttributeType<Boolean> {
    static final Encoder<Boolean> ENCODER = new Encoder<Boolean>() {
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
    };

    BooleanType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
