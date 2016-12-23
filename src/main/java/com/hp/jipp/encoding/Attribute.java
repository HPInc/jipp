package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * A Generic IPP Attribute. Every attribute has a one-byte "value tag" suggesting its type,
 * a string name, and one or more values.
 */
@AutoValue
abstract public class Attribute<T> {

    /** Create and Return a new Attribute builder */
    static <T> Builder<T> builder(Encoder<T> encoder, Tag valueTag) {
        return new AutoValue_Attribute.Builder<T>().setEncoder(encoder).setValueTag(valueTag);
    }

    /** Return a new String attribute */
    public static Attribute<String> create(Tag valueTag, String name, String... values) {
        return StringAttributes.create(valueTag, name, values);
    }

    /** Return a new Boolean attribute */
    public static Attribute<Boolean> create(Tag valueTag, String name, Boolean... values) {
        return BooleanAttributes.create(valueTag, name, values);
    }

    // TODO: continue with additional attribute creators/builders here.

    abstract public Tag getValueTag();
    abstract public String getName();
    abstract public ImmutableList<T> getValues();
    abstract Encoder<T> getEncoder();

    public T getValue(int i) {
        return getValues().get(i);
    }

    public Attribute<Boolean> asBoolean() { return as(Boolean.class); }
    public Attribute<Integer> asInteger() { return as(Integer.class); }
    public Attribute<String> asString() { return as(String.class); }
    public Attribute<byte[]> asOctetString() { return as(byte[].class); }

    @SuppressWarnings("unchecked")
    public Attribute<Map<String, Attribute>> asCollection() {
        // Special because Map<>.class doesn't work
        as(Map.class);
        return (Attribute<Map<String, Attribute>>)this;
    }

    @SuppressWarnings("unchecked")
    private <T> Attribute<T> as(Class<T> cls) {
        for (ClassEncoder classEncoder : ENCODERS) {
            if (classEncoder.getEncoder().valid(getValueTag())) {
                if (!classEncoder.getEncodedClass().equals(cls)) {
                    throw new IllegalArgumentException("Attribute<" +
                            classEncoder.getEncodedClass() + "> does not enclose " + cls);
                }
                return (Attribute<T>) this;
            }
        }
        throw new IllegalArgumentException("Unknown type " + cls);
    }

    abstract static class Encoder<T> {
        /** Return a new builder for the specified valueTag or null if no possible */
        abstract Builder<T> builder(Tag valueTag);

        /** Read a single value from the input stream */
        abstract T readValue(DataInputStream in, Tag valueTag) throws IOException;

        /** Write a single value to the output stream */
        abstract void writeValue(DataOutputStream out, T value) throws IOException;

        /** Return true if this tag can be handled by this encoder */
        abstract boolean valid(Tag valueTag);

        /** Read an attribute and its values from the data stream or null for unrecognized tag */
        Attribute<T> read(DataInputStream in, Tag valueTag) throws IOException {
            Builder<T> builder = builder(valueTag);
            if (builder == null) return null;

            builder.setName(new String(readValueBytes(in)));

            T value = readValue(in, valueTag);
            builder.addValue(value);

            while((value = readAdditionalValue(in, valueTag)) != null) {
                builder.addValue(value);
            }
            return builder.build();
        }

        /** Read a single additional value into the builder, returning true if more */
        private T readAdditionalValue(DataInputStream in, Tag valueTag) throws IOException {
            if (in.available() < 3) return null;
            in.mark(3);
            if (Tag.read(in) == valueTag) {
                int nameLength = in.readShort();
                if (nameLength == 0) {
                    return readValue(in, valueTag);
                }
            }
            // Failed to read an additional value so back up and quit.
            in.reset();
            return null;
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
            if (valueLength != in.read(valueBytes)) throw new IOException("Value too short");
            return valueBytes;
        }

        /** Skip (discard) a length-value pair */
        void skipValueBytes(DataInputStream in) throws IOException {
            int valueLength = in.readShort();
            if (valueLength != in.skip(valueLength)) throw new IOException("Value too short");
        }
    }

    @AutoValue
    abstract static class ClassEncoder {
        public static ClassEncoder create(Class<?> cls, Attribute.Encoder<?> encoder) {
            return new AutoValue_Attribute_ClassEncoder(cls, encoder);
        }
        public abstract Class<?> getEncodedClass();
        public abstract Attribute.Encoder<?> getEncoder();
    }

    static ImmutableList<ClassEncoder> ENCODERS = ImmutableList.of(
            ClassEncoder.create(Integer.class, IntegerAttributes.ENCODER),
            ClassEncoder.create(String.class, StringAttributes.ENCODER),
            ClassEncoder.create(Boolean.class, BooleanAttributes.ENCODER),
            ClassEncoder.create(Map.class, CollectionAttributes.ENCODER),
//            // TODO: RangeOfInteger attribute
//            // TODO: 1setofX
//            // TODO: resolution
//            // TODO: dateTime
//            // TODO: LanguageStringAttribute
            ClassEncoder.create(byte[].class, OctetAttributes.ENCODER));



    /** A generic attribute builder. Must be subclassed for specific types of T */
    @AutoValue.Builder
    abstract public static class Builder<T> {
        abstract Builder<T> setEncoder(Encoder<T> encoder);
        abstract Builder<T> setValueTag(Tag valueTag);
        abstract Builder<T> setName(String name);
        abstract Builder<T> setValues(T... values);
        abstract ImmutableList.Builder<T> valuesBuilder();
        abstract public Attribute<T> build();

        @SafeVarargs
        public final Builder<T> addValue(T... value) {
            valuesBuilder().add(value);
            return this;
        }
    }

    /** Write this attribute (including all of its values) to the output stream */
    public void write(DataOutputStream out) throws IOException {
        writeHeader(out, getValueTag(), getName());
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
        out.write(name.getBytes());
    }

    @Override
    public final String toString() {
        // TODO: Fix byte[] output
        return "Attr{t=" + getValueTag() +
                (getName().equals("") ? "" : ", n=" + getName()) +
                ", v=" + getValues() + "}";
    }
}
