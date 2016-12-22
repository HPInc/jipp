package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooleanAttribute extends Attribute<Boolean> {

    public BooleanAttribute(byte valueTag, String name, List<Boolean> values) {
        super(valueTag, name, values);
    }

    public BooleanAttribute(byte valueTag, String name, Boolean... value) {
        super(valueTag, name, new ArrayList<Boolean>());
        values.addAll(Arrays.asList(value));
    }

    @Override
    void writeValue(DataOutputStream out, Boolean value) throws IOException {
        out.writeShort(1);
        out.writeByte(value ? 0x01 : 0x00);
    }

    @Override
    Boolean readValue(DataInputStream in) throws IOException {
        int length = in.readShort(); // Must be 4, throw away
        if (length != 1) throw new IOException("tag " + getValueTag() +
                " value length=" + length + " (1 expected for Boolean)");
        return in.readByte() != 0;
    }

    public static BooleanAttribute read(DataInputStream in, int valueTag) throws IOException {
        BooleanAttribute attribute = new BooleanAttribute(
                (byte) valueTag,
                readName(in),
                new ArrayList<Boolean>());
        readValues(in, attribute);
        return attribute;
    }

    public static boolean hasTag(byte valueTag) {
        return valueTag == Tags.Boolean;
    }

}
