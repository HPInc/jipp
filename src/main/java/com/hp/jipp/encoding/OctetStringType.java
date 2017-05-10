package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OctetStringType extends AttributeType<byte[]> {
    private static final String TYPE_NAME = "OctetString";

    static final Attribute.SimpleEncoder<byte[]> ENCODER = new Attribute.SimpleEncoder<byte[]>(TYPE_NAME) {

        @Override
        public void writeValue(DataOutputStream out, byte[] value) throws IOException {
            writeValueBytes(out, value);
        }

        @Override
        public byte[] readValue(DataInputStream in, Tag valueTag) throws IOException {
            return Attribute.readValueBytes(in);
        }

        @Override
        public boolean valid(Tag valueTag) {
            // OctetString is a fallback for all types we don't otherwise understand
            return true;
        }
    };

    public OctetStringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
