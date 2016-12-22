package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegerAttribute extends Attribute<Integer> {

    public IntegerAttribute(byte valueTag, String name, List<Integer> values) {
        super(valueTag, name, values);
    }

    public IntegerAttribute(byte valueTag, String name, Integer... value) {
        super(valueTag, name, new ArrayList<Integer>());
        values.addAll(Arrays.asList(value));
    }

    @Override
    void writeValue(DataOutputStream out, Integer value) throws IOException {
        out.writeShort(4);
        out.writeInt(value);
    }

    @Override
    Integer readValue(DataInputStream in) throws IOException {
        int length = in.readShort(); // Must be 4, throw away
        if (length != 4) throw new IOException("tag " + getValueTag() +
                " value length=" + length + " (4 expected)");
        return in.readInt();
    }

    public static IntegerAttribute read(DataInputStream in, int valueTag) throws IOException {
        IntegerAttribute attribute = new IntegerAttribute(
                (byte) valueTag,
                readName(in),
                new ArrayList<Integer>());
        readValues(in, attribute);
        return attribute;
    }

    public static boolean hasTag(byte valueTag) {
        return valueTag == Tags.Integer || valueTag == Tags.Enum;
    }

}
