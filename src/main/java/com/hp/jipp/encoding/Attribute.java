package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.hp.jipp.util.Hook;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 */
@AutoValue
public abstract class Attribute<T> {

    /** Set to false in {@link Hook} to disable builders that accept invalid tags. */
    public static final String HOOK_ALLOW_BUILD_INVALID_TAGS = Encoder.class.getName() +
            ".HOOK_ALLOW_BUILD_INVALID_TAGS";

    /** Create and return a new Attribute builder */
    static <T> Builder<T> builder(Encoder<T> encoder, Tag valueTag) {
        return new AutoValue_Attribute.Builder<T>().setEncoder(encoder).setValueTag(valueTag);
    }

    /**
     * Read an attribute from an input stream, based on its tag
     */
    static Attribute<?> read(DataInputStream in, List<Attribute.Encoder<?>> encoders, Tag valueTag) throws IOException {
        for (Attribute.Encoder<?> classEncoder: encoders) {
            if (classEncoder.valid(valueTag)) {
                return classEncoder.read(in, encoders, valueTag);
            }
        }
        throw new ParseError("Unreadable attribute " + valueTag);
    }

    /**
     * Reads/writes attributes to the attribute's type.
     */
    abstract static class Encoder<T> {
        /** Read a single value from the input stream */
        abstract T readValue(DataInputStream in, Tag valueTag) throws IOException;

        /** Write a single value to the output stream */
        abstract void writeValue(DataOutputStream out, T value) throws IOException;

        /** Read a single value from the input stream, making use of the set of encoders */
        T readValue(DataInputStream in, List<Attribute.Encoder<?>> encoders, Tag valueTag) throws IOException {
            // This method and writeValue are only present so that CollectionType can make use of the
            // encoder set. Other subclasses only need the two-argument style so we default to it.
            return readValue(in, valueTag);
        }

        /** Write a single value to the output stream, making use of the set of encoders */
        void writeValue(DataOutputStream out, List<Attribute.Encoder<?>> encoders, T value) throws IOException {
            writeValue(out, value);
        }

        /** Return true if this tag can be handled by this encoder */
        abstract boolean valid(Tag valueTag);

        /** Reads a two-byte length field, asserting that it is of a specific length */
        void expectLength(DataInputStream in, int length) throws IOException {
            int readLength = in.readShort();
            if (readLength != length) {
                throw new ParseError("expected " + length + " but got " + readLength);
            }
        }

        /**
         * Return a new Attribute builder for the specified valueTag (assumes a valid valueTag).
         * @param valueTag value-tag for attributes that can be built for the returned builder.
         *                 Throws if not a known tag for this encoder.
         */
        Builder<T> builder(Tag valueTag) {
            if (!(valid(valueTag) || Hook.is(HOOK_ALLOW_BUILD_INVALID_TAGS))) {
                throw new BuildError(valueTag.toString() + " is not a valid tag for " + this);
            }
            return Attribute.builder(this, valueTag);
        }

        /** Read an attribute and its values from the data stream */
        Attribute<T> read(DataInputStream in, List<Attribute.Encoder<?>> encoders, Tag valueTag) throws IOException {
            Builder<T> builder = builder(valueTag)
                    .setName(new String(readValueBytes(in), Util.UTF8));

            // Read first value...there always has to be one, right?
            ImmutableList.Builder<T> valueBuilder = new ImmutableList.Builder<>();
            valueBuilder.add(readValue(in, encoders, valueTag));

            Optional<T> value;
            while ((value = readAdditionalValue(in, valueTag, encoders)).isPresent()) {
                valueBuilder.add(value.get());
            }
            builder.setValues(valueBuilder.build());
            return builder.build();
        }

        /** Read a single additional value into the builder, returning true if more */
        private Optional<T> readAdditionalValue(DataInputStream in, Tag valueTag, List<Attribute.Encoder<?>> encoders)
                throws IOException {
            if (in.available() < 3) return Optional.absent();
            in.mark(3);
            if (Tag.read(in) == valueTag) {
                int nameLength = in.readShort();
                if (nameLength == 0) {
                    return Optional.of(readValue(in, encoders, valueTag));
                }
            }
            // Failed to read an additional value so back up and quit.
            in.reset();
            return Optional.absent();
        }

        /** Write a length-value tuple */
        void writeValueBytes(DataOutputStream out, byte[] bytes) throws IOException {
            out.writeShort(bytes.length);
            out.write(bytes);
        }

        /** Read and return value bytes from a length-value pair */
        byte[] readValueBytes(DataInputStream in) throws IOException {
            int valueLength = in.readShort();
            byte valueBytes[] = new byte[valueLength];
            if (valueLength != in.read(valueBytes)) throw new ParseError("Value too short");
            return valueBytes;
        }

        /** Skip (discard) a length-value pair */
        void skipValueBytes(DataInputStream in) throws IOException {
            int valueLength = in.readShort();
            if (valueLength != in.skip(valueLength)) throw new ParseError("Value too short");
        }
    }

    /** A generic attribute builder to be subclassed for specific types of T. */
    @AutoValue.Builder
    abstract static class Builder<T> {
        abstract Builder<T> setEncoder(Encoder<T> encoder);
        abstract Builder<T> setValueTag(Tag valueTag);
        abstract Builder<T> setName(String name);
        abstract Builder<T> setValues(List<T> values);
        @SuppressWarnings("unchecked")
        public Builder<T> setValues(T... values) {
            setValues(Arrays.asList(values));
            return this;
        }
        abstract public Attribute<T> build();
    }

    abstract public Tag getValueTag();
    abstract public String getName();
    abstract public List<T> getValues();
    abstract Encoder<T> getEncoder();

    /** Return the n'th value in this attribute, assuming it is present */
    public T getValue(int n) {
        return getValues().get(n);
    }

    /** Return a copy of this attribute with a different name */
    Attribute<T> withName(String newName) {
        return Attribute.builder(getEncoder(), getValueTag()).setName(newName).setValues(getValues()).build();
    }

    /** Write this attribute (including all of its values) to the output stream */
    void write(DataOutputStream out, List<Attribute.Encoder<?>> encoders) throws IOException {
        writeHeader(out, getValueTag(), getName());
        if (getValues().isEmpty()) {
            out.writeShort(0);
            return;
        }

        getEncoder().writeValue(out, encoders, getValue(0));
        for (int i = 1; i < getValues().size(); i++) {
            writeHeader(out, getValueTag(), "");
            getEncoder().writeValue(out, encoders, getValues().get(i));
        }
    }

    /** Write value tag and name components of an attribute */
    private void writeHeader(DataOutputStream out, Tag valueTag, String name) throws IOException {
        valueTag.write(out);
        out.writeShort(name.length());
        out.write(name.getBytes(Util.UTF8));
    }

    @Override
    public final String toString() {
        List<String> values = Lists.transform(getValues(), new Function<T, String>() {
            @Override
            public String apply(T input) {
                if (input instanceof String || input instanceof URI) {
                    return "\"" + input + "\"";
                } else if (input instanceof byte[]) {
                    return "x" + BaseEncoding.base16().encode((byte[]) input);
                }
                return input.toString();
            }
        });
        String valueString;
        if (values.size() == 1) {
            valueString = values.get(0);
        } else {
            valueString = values.toString();
        }
        return (getName().equals("") ? "" : getName()) +
                "(" + getValueTag() + ")" +
                ": " + valueString;
    }
}
