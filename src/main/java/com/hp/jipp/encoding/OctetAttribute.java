package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An attribute containing a series of Octet buffers */
public class OctetAttribute {

    /** Return a new integer attribute builder */
    public static Attribute.Builder<byte[]> builder(Tag valueTag) {
        return Attribute.builder(ENCODER, valueTag);
    }

    /** Return a new integer attribute */
    public static Attribute<byte[]> create(Tag valueTag, String name, byte[]... values) {
        return builder(valueTag).setValues(values).setName(name).build();
    }

    static Attribute.Encoder<byte []> ENCODER = new Attribute.Encoder<byte[]>() {
        @Override
        public void writeValue(DataOutputStream out, byte[] value) throws IOException {
            writeValueBytes(out, value);
        }

        @Override
        public byte[] readValue(DataInputStream in, Tag valueTag) throws IOException {
            return readValueBytes(in);
        }

        @Override
        public Attribute.Builder<byte[]> builder(Tag valueTag) {
            return OctetAttribute.builder(valueTag);
        }

        @Override
        boolean valid(Tag valueTag) {
            return true;
        }
    };
//    String valueToString(byte bytes[]) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
}