package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringAttribute extends Attribute<String> {

    public StringAttribute(byte valueTag, String name, List<String> values) {
        super(valueTag, name, values);
    }

    public StringAttribute(byte valueTag, String name, String... value) {
        super(valueTag, name, new ArrayList<String>());
        values.addAll(Arrays.asList(value));
    }

    @Override
    void writeValue(DataOutputStream out, String value) throws IOException {
        byte bytes[] = value.getBytes();
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    @Override
    String readValue(DataInputStream in) throws IOException {
        byte bytes[] = new byte[in.readShort()];
        in.read(bytes);
        return new String(bytes);
    }

    public static StringAttribute read(DataInputStream in, int valueTag) throws IOException {
        StringAttribute attribute = new StringAttribute(
                (byte) valueTag,
                readName(in),
                new ArrayList<String>());
        readValues(in, attribute);
        return attribute;
    }

    public static boolean hasTag(byte valueTag) {
        return (valueTag & 0x40) == 0x40;
    }
}
