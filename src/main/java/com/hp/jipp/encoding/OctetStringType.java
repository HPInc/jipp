package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OctetStringType extends AttributeType<byte[]> {

    static final Attribute.Encoder<byte[]> ENCODER = new Attribute.Encoder<byte[]>() {

        @Override
        public void writeValue(DataOutputStream out, byte[] value) throws IOException {
            writeValueBytes(out, value);
        }

        @Override
        public byte[] readValue(DataInputStream in, Tag valueTag) throws IOException {
            return readValueBytes(in);
        }

        @Override
        boolean valid(Tag valueTag) {
            // OctetString is a fallback for all types we don't otherwise understand
            return true;
        }
    };

    public OctetStringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
