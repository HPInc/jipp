package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Generic IPP Attribute. Every attribute has a one-byte "value tag" suggesting its type,
 * a string name, and one or more values.
 */
abstract class Attribute<T> {
    // TODO: Convert to immutable

    private final String name;
    private final Tag valueTag;
    protected final List<T> values;

    public Attribute(Tag valueTag, String name, List<T> values) {
        this.values = values;
        this.name = name;
        this.valueTag = valueTag;
    }

    public Tag getValueTag() {
        return valueTag;
    }

    public String getName() {
        return name;
    }

    public T getValue(int i) {
        return values.get(i);
    }

    public List<T> getValues() {
        return values;
    }

    /**
     * Write this entire
     * @param out
     * @throws IOException
     */
    public void write(DataOutputStream out) throws IOException {
        writeHeader(out, valueTag.getValue(), name);
        writeValue(out, values.get(0));
        for (int i = 1; i < values.size(); i++) {
            writeHeader(out, valueTag.getValue());
            writeValue(out, values.get(i));
        }
    }

    /**
     * Write the specified value to the output stream, including a two-byte length
     */
    abstract void writeValue(DataOutputStream out, T value) throws IOException;

    /**
     * Read and return a single value from the input stream
     */
    abstract T readValue(DataInputStream in) throws IOException;

    /**
     * Return a string representation of a value for debugging purposes. Override to replace
     * the default implementation which uses toString on the value.
     */
    String valueToString(T value) {
        return value.toString();
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<>();
        for (T value: values) {
            strings.add(valueToString(value));
        }
        return "<tag=" + valueTag +
                " name=" + name +
                " values=" + strings + ">";
    }

    /** Read the value in the current attribute, and check for additional following attributes as well */
    static <T> void readValues(DataInputStream in, Attribute<T> attribute) throws IOException {
        attribute.values.add(attribute.readValue(in));
        while(readAdditionalValue(in, attribute));
    }

    static private <T> boolean readAdditionalValue(DataInputStream in, Attribute<T> attribute) throws IOException {
        if (in.available() < 3) return false;
        in.mark(3);
        Tag tag = Tag.toTag(in.readByte());
        if (tag == attribute.getValueTag()) {
            int nameLength = in.readShort();
            if (nameLength == 0) {
                attribute.values.add(attribute.readValue(in));
                return true;
            }
        }
        // Actually this is *not* an additional value, so back up and quit.
        in.reset();
        return false;
    }

    /** Write value tag and name components of an attribute */
    static void writeHeader(DataOutputStream out, byte valueTag, String name) throws IOException {
        out.writeByte(valueTag);
        out.writeShort(name.length());
        out.write(name.getBytes());
    }

    /** Write value tag and empty name components of an attribute */
    static void writeHeader(DataOutputStream out, byte valueTag) throws IOException {
        out.writeByte(valueTag);
        out.writeShort(0);
    }

    static byte[] readValueBytes(DataInputStream in) throws IOException {
        // Read value
        int valueLength = in.readShort();
        byte valueBytes[] = new byte[valueLength];
        in.read(valueBytes);
        return valueBytes;
    }

    static String readName(DataInputStream in) throws IOException {
        return new String(readValueBytes(in));
    }
}
