package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NameCodeType<T extends NameCode> extends AttributeType<T> {

    /** Create a new enumeration encoder */
    public static <T extends NameCode> Encoder<T> encoder(String name, Collection<T> enums,
            NameCode.Factory<T> factory) {
        return new AutoValue_NameCodeType_Encoder<>(name,
                new ImmutableMap.Builder<Integer, T>().putAll(NameCode.toMap(enums)).build(),
                factory);
    }

    public static <T extends NameCode> NameCodeType<T> type(Encoder<T> encoder, String name) {
        return new NameCodeType<>(encoder, name);
    }

    /**
     * An encoder for NameCode enumerations.
     * <p>
     * Use {@link #encoder(String, Collection, NameCode.Factory)} to construct an encoder instance for new EnumTypes.
     */
    @AutoValue
    public abstract static class Encoder<T extends NameCode> extends Attribute.Encoder<T> {

        /** Return the user-visible name of the enum (for debugging purposes) */
        public abstract String getName();

        /** Return the map all known enums */
        public abstract Map<Integer, T> getMap();

        /** Return a factory for constructing new enum instances */
        abstract NameCode.Factory<T> getFactory();

        /** Returns a known enum, or creates a new instance if not found */
        public T get(int code) {
            Optional<T> e = Optional.fromNullable(getMap().get(code));
            if (e.isPresent()) return e.get();
            return getFactory().of(getName() + "(x" + Integer.toHexString(code) + ")", code);
        }

        @Override
        T readValue(DataInputStream in, Tag valueTag) throws IOException {
            expectLength(in, 4);
            return get(in.readInt());
        }

        @Override
        void writeValue(DataOutputStream out, T value) throws IOException {
            IntegerType.ENCODER.writeValue(out, value.getCode());
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.EnumValue;
        }
    }

    private final Function<Integer, T> toEnum = new Function<Integer, T>() {
        @Override
        public T apply(Integer input) {
            return ((Encoder<T>) getEncoder()).get(input);
        }
    };

    public NameCodeType(Encoder<T> encoder, String name) {
        super(encoder, Tag.EnumValue, name);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Optional<Attribute<T>> from(Attribute<?> attribute) {
        if (attribute.getValueTag() != Tag.EnumValue) {
            return Optional.absent();
        }
        return Optional.of(of(Lists.transform((List<Integer>) attribute.getValues(), toEnum)));
    }
}
