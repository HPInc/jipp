package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.Hook;
import com.hp.jipp.model.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 */
@AutoValue
public abstract class Attribute<T> {
    /** Set to false in {@link Hook} to disable builders that accept invalid tags. */
    public static final String HOOK_ALLOW_BUILD_INVALID_TAGS = AttributeEncoder.class.getName() +
            ".HOOK_ALLOW_BUILD_INVALID_TAGS";

    /** Create and Return a new Attribute builder */
    static <T> Builder<T> builder(AttributeEncoder<T> encoder, Tag valueTag) {
        return new AutoValue_Attribute.Builder<T>().setEncoder(encoder).setValueTag(valueTag);
    }

    // TODO: Remove the create() methods below
    /** Return a new String attribute */
    public static Attribute<String> create(Tag valueTag, String name, String... values) {
        return StringEncoder.getInstance().builder(valueTag).setValues(values).setName(name)
                .build();
    }

    /** Return a new Boolean attribute */
    public static Attribute<Boolean> create(Tag valueTag, String name, Boolean... values) {
        return BooleanEncoder.getInstance().builder(valueTag).setName(name).setValues(values)
                .build();
    }

    /** Return a new Boolean attribute */
    public static Attribute<Integer> create(Tag valueTag, String name, Integer... values) {
        return IntegerEncoder.getInstance().builder(valueTag).setName(name).setValues(values)
                .build();
    }

    public static Attribute<byte[]> create(Tag valueTag, String name, byte[]... values) {
        return OctetEncoder.getInstance().builder(valueTag).setName(name).setValues(values)
                .build();
    }

    /**
     * Create a named collection attribute (only used at the top of the attribute tree)
     */
    public static Attribute<Map<String, Attribute<?>>> create(String name,
            Map<String, Attribute<?>>... values) {
        return CollectionEncoder.getInstance().builder(Tag.BeginCollection).setName(name)
                .setValues(values).build();
    }

    /**
     * Create a nameless collection attribute (only used when nesting inside a top-level named
     * attribute)
     */
    public static Attribute<Map<String, Attribute<?>>> create(Map<String, Attribute<?>>... values) {
        return CollectionEncoder.getInstance().builder(Tag.BeginCollection).setName("")
                .setValues(values).build();
    }

    /**
     * Read an attribute from an input stream, based on its tag
     */
    public static Attribute<?> read(DataInputStream in, Tag valueTag) throws IOException {
        for (Attribute.ClassEncoder classEncoder: Attribute.ENCODERS) {
            if (classEncoder.getEncoder().valid(valueTag)) {
                return classEncoder.getEncoder().read(in, valueTag);
            }
        }
        throw new RuntimeException("Unreadable attribute " + valueTag);
    }

    abstract public Tag getValueTag();
    abstract public String getName();
    abstract public ImmutableList<T> getValues();
    abstract AttributeEncoder<T> getEncoder();

    /** Return the n'th value in this attribute */
    public T getValue(int n) {
        return getValues().get(n);
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

    /**
     * Cast this attribute to one of the specified value type. Only do this if you are
     * sure of the expected attribute's type.
     */
    @SuppressWarnings("unchecked")
    public <U> Attribute<U> as(Class<U> cls) {
        for (ClassEncoder classEncoder : ENCODERS) {
            if (classEncoder.getEncoder().valid(getValueTag())) {
                if (!classEncoder.getEncodedClass().equals(cls)) {
                    throw new IllegalArgumentException("Attribute<" +
                            classEncoder.getEncodedClass() + "> does not enclose " + cls);
                }
                return (Attribute<U>) this;
            }
        }
        throw new IllegalArgumentException("Unknown type " + cls);
    }

    @AutoValue
    abstract static class ClassEncoder {
        public static ClassEncoder create(Class<?> cls, AttributeEncoder<?> encoder) {
            return new AutoValue_Attribute_ClassEncoder(cls, encoder);
        }
        public abstract Class<?> getEncodedClass();
        public abstract AttributeEncoder<?> getEncoder();
    }

    private static ImmutableList<ClassEncoder> ENCODERS = ImmutableList.of(
            ClassEncoder.create(Operation.class, Operation.Encoder),
            ClassEncoder.create(Integer.class, IntegerEncoder.getInstance()),
            ClassEncoder.create(String.class, StringEncoder.getInstance()),
            ClassEncoder.create(URI.class, UriEncoder.getInstance()),
            ClassEncoder.create(Boolean.class, BooleanEncoder.getInstance()),
            ClassEncoder.create(Map.class, CollectionEncoder.getInstance()),
//            // TODO: RangeOfInteger attribute
//            // TODO: 1setofX
//            // TODO: resolution
//            // TODO: dateTime
//            // TODO: LanguageStringAttribute
            ClassEncoder.create(byte[].class, OctetEncoder.getInstance()));



    /** A generic attribute builder. Must be subclassed for specific types of T */
    @AutoValue.Builder
    abstract public static class Builder<T> {
        abstract Builder<T> setEncoder(AttributeEncoder<T> encoder);
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
        out.write(name.getBytes());
    }

    @Override
    public final String toString() {
        // TODO: Fix byte[] output
        return "Attr{t=" + getValueTag() +
                (getName().equals("") ? "" : ", n=" + getName()) +
                ", v=" + getValues() + "}";
    }

    public static <T extends NameCode> AttributeType<T> enumType(AttributeEncoder<T> encoder, Tag tag, String name) {
        return new AttributeType<>(encoder, tag, name);
    }

    public static AttributeType<URI> uriType(Tag tag, String name) {
        return new AttributeType<>(UriEncoder.getInstance(), tag, name);
    }

    public static AttributeType<String> stringType(Tag tag, String name) {
        return new AttributeType<>(StringEncoder.getInstance(), tag, name);
    }

}
