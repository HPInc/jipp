package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An attribute containing a series of Octet buffers */
public class OctetAttribute extends Attribute<byte[]> {
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public OctetAttribute(Tag valueTag, String name, List<byte[]> values) {
        super(valueTag, name, values);
    }

    public OctetAttribute(Tag valueTag, String name, byte[]... value) {
        super(valueTag, name, new ArrayList<byte[]>());
        values.addAll(Arrays.asList(value));
    }

    @Override
    void writeValue(DataOutputStream out, byte[] value) throws IOException {
        out.writeShort(value.length);
        out.write(value);
    }

    /**
     * Read an attribute from the input stream, assuming an initial valueTag. Note: this
     * is a "catch-all" attribute and will always succeed for good input, regardless of the actual
     * valueTag type.
     */
    public static OctetAttribute read(DataInputStream in, Tag valueTag) throws IOException {
        OctetAttribute attribute = new OctetAttribute(
                valueTag, readName(in), new ArrayList<byte[]>());
        readValues(in, attribute);
        return attribute;
    }

    @Override
    byte[] readValue(DataInputStream in) throws IOException {
        return readValueBytes(in);
    }

    @Override
    String valueToString(byte bytes[]) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}