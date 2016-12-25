package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@AutoValue
public abstract class EnumEncoder<T extends NameCode> extends Attribute.Encoder<T> {

    public abstract String getName();
    public abstract Set<String> getAttributeNames();
    public abstract Set<T> getEnums();
    public abstract Map<Integer, T> getEnumsMap();
    abstract Factory<T> getCreator();

    public interface Factory<T> {
        T create(String name, int code);
    }

    public static <T extends NameCode> EnumEncoder<T> create(String name,
            Set<String> attributeNames, Set<T> enums,Factory<T> factory) {
        return new AutoValue_EnumEncoder<>(name, attributeNames, enums, enumsToMap(enums), factory);
    }

    private static <T extends NameCode> Map<Integer, T> enumsToMap(Set<T> enums) {
        ImmutableMap.Builder<Integer, T> builder = new ImmutableMap.Builder<>();
        for (T e : enums) {
            builder.put(e.getCode(), e);
        }
        return builder.build();
    }

    /** Create a new enum attribute containing specified values */
    public Attribute<T> createAttribute(String name, T... values) {
        return builder(Tag.EnumValue).setName(name).setValues(values).build();
    }

    /** Returns a known enum, or creates a new T instance if not found */
    public T getEnum(int code) {
        T e = getEnumsMap().get(code);
        if (e != null) return e;
        return getCreator().create(getName() + "(x" + Integer.toHexString(code) + ")", code);
    }

    @Override
    Attribute.Builder<T> builder(Tag valueTag) {
        return Attribute.builder(this, valueTag);
    }

    @Override
    T readValue(DataInputStream in, Tag valueTag) throws IOException {
        int length = in.readShort();
        if (length != 4) {
            throw new IOException("tag " + valueTag + " value expected 4 got " + length);
        }
        return getEnum(in.readInt());
    }

    @Override
    void writeValue(DataOutputStream out, T value) throws IOException {
        out.writeShort(4);
        out.writeInt(value.getCode());
    }

    @Override
    boolean valid(Tag valueTag) {
        return (valueTag == Tag.EnumValue);
    }
}
