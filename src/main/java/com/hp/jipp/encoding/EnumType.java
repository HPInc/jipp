package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class EnumType<T extends NameCode> extends AttributeType<T> {

    /** Create and return a new encoder */
    public static <T extends NameCode> Encoder<T> encoder(String name, List<T> enums, NameCode.Factory<T> factory) {
        return new AutoValue_EnumType_Encoder<>(name,
                new ImmutableMap.Builder<Integer, T>().putAll(NameCode.toMap(enums)).build(),
                factory);
    }

    @AutoValue
    public abstract static class Encoder<T extends NameCode> extends com.hp.jipp.encoding.Encoder<T> {

        public abstract String getName();

        public abstract ImmutableMap<Integer, T> getEnumsMap();

        abstract NameCode.Factory<T> getFactory();

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

    public EnumType(Encoder<T> encoder, Tag tag, String name) {
        super(encoder, tag, name);
    }
}
