package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringType extends AttributeType<String> {

    static Encoder<String> ENCODER = new Encoder<String>() {

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
            return (valueTag.getValue() & 0x40) == 0x40 && valueTag != Tag.Uri;
        }
    };

    public StringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
