package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.hp.jipp.util.Hook;
import com.hp.jipp.util.Pretty;
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
public abstract class Attribute<T> implements Pretty.Printable {

    /** Set to false in {@link Hook} to disable builders that accept invalid tags. */
    public static final String HOOK_ALLOW_BUILD_INVALID_TAGS = Encoder.class.getName() +
            ".HOOK_ALLOW_BUILD_INVALID_TAGS";

    /** Create and return a new Attribute builder */
    static <T> Builder<T> builder(BaseEncoder<T> encoder, Tag valueTag) {
        return new AutoValue_Attribute.Builder<T>().setEncoder(encoder).setValueTag(valueTag);
    }

    interface EncoderFinder {
        BaseEncoder<?> find(Tag valueTag, String name) throws IOException;
    }

    /**
     * Read an attribute from an input stream, based on its tag
     */
    static Attribute<?> read(DataInputStream in, EncoderFinder finder, Tag valueTag) throws IOException {
        String name = new String(readValueBytes(in), Util.UTF8);
        return finder.find(valueTag, name).read(in, finder, valueTag, name);
    }

    /** A generic attribute builder to be subclassed for specific types of T. */
    @AutoValue.Builder
    abstract static class Builder<T> {
        abstract Builder<T> setEncoder(BaseEncoder<T> encoder);

        abstract Builder<T> setValueTag(Tag valueTag);

        abstract Builder<T> setName(String name);

        abstract Builder<T> setValues(List<T> values);

        @SuppressWarnings("unchecked")
        public Builder<T> setValues(T... values) {
            setValues(Arrays.asList(values));
            return this;
        }

        public abstract Attribute<T> build();
    }

    public abstract static class Encoder<T> extends BaseEncoder<T> {
        private final String mTypeName;

        public Encoder(String typeName) {
            mTypeName = typeName;
        }

        public String getType() {
            return mTypeName;
        }

        /** Read a single value from the input stream, making use of the set of encoders */
        public abstract T readValue(DataInputStream in, Tag valueTag) throws IOException;

        public final T readValue(DataInputStream in, Attribute.EncoderFinder finder, Tag valueTag) throws IOException {
            return readValue(in, valueTag);
        }
    }

    /**
     * Reads/writes attributes to the attribute's type.
     */
    public abstract static class BaseEncoder<T> {

        /** Return a human-readable name describing this type */
        public abstract String getType();

        /** Read a single value from the input stream, making use of the set of encoders */
        public abstract T readValue(DataInputStream in, Attribute.EncoderFinder finder, Tag valueTag)
                throws IOException;

        /** Write a single value to the output stream */
        public abstract void writeValue(DataOutputStream out, T value) throws IOException;


        /** Return true if this tag can be handled by this encoder */
        public abstract boolean valid(Tag valueTag);

        /**
         * Return a new Attribute builder for the specified valueTag (assumes a valid valueTag).
         * @param valueTag value-tag for attributes that can be built for the returned builder.
         *                 Throws if not a known tag for this encoder.
         */
        Builder<T> builder(Tag valueTag) {
            if (!(valid(valueTag) || Hook.is(HOOK_ALLOW_BUILD_INVALID_TAGS))) {
                throw new BuildError("Invalid " + valueTag.toString() + " for " + getType());
            }
            return Attribute.builder(this, valueTag);
        }

        /** Read an attribute and its values from the data stream */
        Attribute<T> read(DataInputStream in, Attribute.EncoderFinder finder, Tag valueTag, String name)
                throws IOException {
            Builder<T> builder = builder(valueTag).setName(name);

            // Read first value...there always has to be one, right?
            ImmutableList.Builder<T> valueBuilder = new ImmutableList.Builder<>();
            valueBuilder.add(readValue(in, finder, valueTag));

            Optional<T> value;
            while ((value = readAdditionalValue(in, valueTag, finder)).isPresent()) {
                valueBuilder.add(value.get());
            }
            builder.setValues(valueBuilder.build());
            return builder.build();
        }

        /** Read a single additional value if possible */
        private Optional<T> readAdditionalValue(DataInputStream in, Tag valueTag, Attribute.EncoderFinder finder)
                throws IOException {
            if (in.available() < 3) return Optional.absent();
            in.mark(3);
            if (Tag.read(in) == valueTag) {
                int nameLength = in.readShort();
                if (nameLength == 0) {
                    return Optional.of(readValue(in, finder, valueTag));
                }
            }
            // Failed to parse an additional value so back up and quit.
            in.reset();
            return Optional.absent();
        }

        /** Write a length-value tuple */
        void writeValueBytes(DataOutputStream out, byte[] bytes) throws IOException {
            out.writeShort(bytes.length);
            out.write(bytes);
        }

        /** Skip (discard) a length-value pair */
        void skipValueBytes(DataInputStream in) throws IOException {
            int valueLength = in.readShort();
            if (valueLength != in.skip(valueLength)) throw new ParseError("Value too short");
        }
    }

    /** Read and return value bytes from a length-value pair */
    static byte[] readValueBytes(DataInputStream in) throws IOException {
        int valueLength = in.readShort();
        byte[] valueBytes = new byte[valueLength];
        int actual = in.read(valueBytes);
        if (valueLength > actual) {
            throw new ParseError("Value too short: expected " + valueBytes.length + ", got only " + actual);
        }
        return valueBytes;
    }

    /** Reads a two-byte length field, asserting that it is of a specific length */
    static void expectLength(DataInputStream in, int length) throws IOException {
        int readLength = in.readShort();
        if (readLength != length) {
            throw new ParseError("Bad attribute length: expected " + length + ", got " + readLength);
        }
    }

    public abstract Tag getValueTag();

    public abstract String getName();

    public abstract List<T> getValues();

    abstract BaseEncoder<T> getEncoder();

    abstract Attribute.Builder<T> toBuilder();

    /** Return the n'th value in this attribute, assuming it is present */
    public T getValue(int n) {
        return getValues().get(n);
    }

    /** Return a copy of this attribute with a different name */
    Attribute<T> withName(String newName) {
        return toBuilder().setName(newName).setValues(getValues()).build();
    }

    /** Write this attribute (including all of its values) to the output stream */
    void write(DataOutputStream out) throws IOException {
        writeHeader(out, getValueTag(), getName());
        if (getValues().isEmpty()) {
            out.writeShort(0);
            return;
        }

        getEncoder().writeValue(out, getValue(0));
        for (int i = 1; i < getValues().size(); i++) {
            writeHeader(out, getValueTag(), "");
            getEncoder().writeValue(out, getValues().get(i));
        }
    }

    /** Write value tag and name components of an attribute */
    private void writeHeader(DataOutputStream out, Tag valueTag, String name) throws IOException {
        valueTag.write(out);
        out.writeShort(name.length());
        out.write(name.getBytes(Util.UTF8));
    }

    @Override
    public void print(Pretty.Printer printer) {
        String prefix = (getName().equals("") ? "" : getName()) + "(" + getValueTag() + "):";
        if (getValues().size() == 1) {
            printer.open(Pretty.KEY_VALUE, prefix);
        } else {
            printer.open(Pretty.ARRAY, prefix);
        }

        for (Object value: getValues()) {
            if (value instanceof String) {
                printer.add("\"" + value + "\"");
            } else if (value instanceof byte[]) {
                printer.add("x" + BaseEncoding.base16().encode((byte[]) value));
            } else if (value instanceof Pretty.Printable) {
                ((Pretty.Printable) value).print(printer);
            } else {
                printer.add(value.toString());
            }
        }
        printer.close();
    }

    @Override
    public String toString() {
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
