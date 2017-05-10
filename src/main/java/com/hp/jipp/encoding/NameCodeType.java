package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class NameCodeType<T extends NameCode> extends AttributeType<T> {

    /** Constructs a new NameCodeType based on the supplied encoder and name */
    public static <T extends NameCode> NameCodeType<T> typeOf(Encoder<T> encoder, String name) {
        return new NameCodeType<>(encoder, name);
    }

    /** An encoder for NameCode enumerations. */
    @AutoValue
    public abstract static class Encoder<T extends NameCode> extends Attribute.BaseEncoder<T> {

        /**
         * Return a new NameCode.Encoder including values from all static members defined in the class (from reflection)
         */
        @SuppressWarnings("unchecked")
        public static <T extends NameCode> Encoder<T> of(Class<T> cls,
                                                         NameCode.Factory<T> factory) {
            ImmutableList.Builder<T> nameCodes = new ImmutableList.Builder<>();
            for (Object object : Util.getStaticObjects(cls)) {
                if (cls.isAssignableFrom(object.getClass())) {
                    nameCodes.add((T) object);
                }
            }
            return of(cls.getSimpleName(), nameCodes.build(), factory);
        }

        /** Return a new enumeration encoder */
        public static <T extends NameCode> Encoder<T> of(String name, Collection<T> enums,
                                                         NameCode.Factory<T> factory) {
            return new AutoValue_NameCodeType_Encoder<>(name,
                    new ImmutableMap.Builder<Integer, T>().putAll(NameCode.toMap(enums)).build(),
                    factory);
        }

        /** Return the map all known enums */
        public abstract Map<Integer, T> getMap();

        /** Return a factory for constructing new enum instances */
        abstract NameCode.Factory<T> getFactory();

        /** Returns a known enum, or creates a new instance if not found */
        public T get(int code) {
            Optional<T> e = Optional.fromNullable(getMap().get(code));
            if (e.isPresent()) return e.get();
            return getFactory().of(getType() + "(x" + Integer.toHexString(code) + ")", code);
        }

        @Override
        public final T readValue(DataInputStream in, Attribute.EncoderFinder finder, Tag valueTag) throws IOException {
            return get(IntegerType.ENCODER.readValue(in, valueTag));
        }

        @Override
        public void writeValue(DataOutputStream out, T value) throws IOException {
            IntegerType.ENCODER.writeValue(out, value.getCode());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.EnumValue;
        }
    }

    private final Function<Integer, T> toNameCode = new Function<Integer, T>() {
        @Override
        public T apply(@Nonnull Integer input) {
            return ((Encoder<T>) getEncoder()).get(input);
        }
    };

    public NameCodeType(Encoder<T> encoder, String name) {
        super(encoder, Tag.EnumValue, name);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Optional<Attribute<T>> of(Attribute<?> attribute) {
        if (attribute.getValueTag() != Tag.EnumValue) return Optional.absent();
        return Optional.of(of(Lists.transform((List<Integer>) attribute.getValues(), toNameCode)));
    }
}
