package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@AutoValue
public abstract class EnumEncoder<T extends NameCode> extends AttributeEncoder<T> {

    public abstract String getName();
    public abstract Set<String> getAttributeNames();
    public abstract Set<T> getEnums();
    public abstract Map<Integer, T> getEnumsMap();
    abstract NameCode.Factory<T> getFactory();

    public static <T extends NameCode> EnumEncoder<T> create(String name,
            Set<String> attributeNames, Set<T> enums, NameCode.Factory<T> factory) {
        return new AutoValue_EnumEncoder<>(name, attributeNames, enums, NameCode.toMap(enums),
                factory);
    }

    /** Create a new enum attribute containing specified values */
    @SafeVarargs
    public final Attribute<T> toAttribute(String name, T... values) {
        return builder(Tag.EnumValue).setName(name).setValues(values).build();
    }

    /** Returns a known enum, or creates a new T instance if not found */
    public T getEnum(int code) {
        Optional<T> e = Optional.fromNullable(getEnumsMap().get(code));
        if (e.isPresent()) return e.get();
        return getFactory().create(getName() + "(x" + Integer.toHexString(code) + ")", code);
    }

    @Override
    T readValue(DataInputStream in, Tag valueTag) throws IOException {
        expectLength(in, 4);
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
