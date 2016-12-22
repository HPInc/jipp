package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringAttribute extends Attribute<String> {

    public StringAttribute(Tag valueTag, String name, List<String> values) {
        super(valueTag, name, values);
    }

    public StringAttribute(Tag valueTag, String name, String... value) {
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

    /** Read value into an Attribute or null if not handled here */
    public static StringAttribute read(DataInputStream in, Tag valueTag) throws IOException {
        if ((valueTag.getValue() & 0x40) != 0x40) return null;

        StringAttribute attribute = new StringAttribute(
                valueTag, readName(in), new ArrayList<String>());
        readValues(in, attribute);
        return attribute;
    }
}
