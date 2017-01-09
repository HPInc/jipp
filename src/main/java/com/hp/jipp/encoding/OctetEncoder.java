package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tools for encoding Octet String (byte[]) attributes */
public class OctetEncoder extends AttributeEncoder<byte[]> {

    private static OctetEncoder INSTANCE = new OctetEncoder();

    /** Return the singleton encoder */
    static OctetEncoder getInstance() {
        return INSTANCE;
    }

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
        return true;
    }
}