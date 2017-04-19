package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.Hook;
import com.hp.jipp.model.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

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
    public static Attribute<?> read(DataInputStream in, Tag valueTag) throws IOException {
        for (Attribute.ClassEncoder classEncoder: Attribute.ENCODERS) {
            if (classEncoder.getEncoder().valid(valueTag)) {
                return classEncoder.getEncoder().read(in, valueTag);
            }
        }
        throw new ParseError("Unreadable attribute " + valueTag);
    }

    @AutoValue
    abstract static class ClassEncoder {
        public static ClassEncoder create(Class<?> cls, Encoder<?> encoder) {
            return new AutoValue_Attribute_ClassEncoder(cls, encoder);
        }
        public abstract Class<?> getEncodedClass();
        public abstract Encoder<?> getEncoder();
    }

    /** Encoders available to parse incoming data */
    private static ImmutableList<ClassEncoder> ENCODERS = ImmutableList.of(
            ClassEncoder.create(Operation.class, Operation.Encoder),
            ClassEncoder.create(Integer.class, IntegerType.ENCODER),
            ClassEncoder.create(String.class, StringType.ENCODER),
            ClassEncoder.create(URI.class, UriType.ENCODER),
            ClassEncoder.create(Boolean.class, BooleanType.ENCODER),
            ClassEncoder.create(Map.class, CollectionType.ENCODER),
            ClassEncoder.create(LangString.class, LangStringType.ENCODER),
//            // TODO: RangeOfInteger attribute
//            // TODO: 1setofX
//            // TODO: resolution
//            // TODO: dateTime
//            // TODO: LanguageStringAttribute
            ClassEncoder.create(byte[].class, OctetStringType.ENCODER));



    /** A generic attribute builder. Must be subclassed for specific types of T */
    @AutoValue.Builder
    abstract public static class Builder<T> {
        abstract Builder<T> setEncoder(Encoder<T> encoder);
        abstract Builder<T> setValueTag(Tag valueTag);
        abstract Builder<T> setName(String name);
        abstract Builder<T> setValues(T... values);
        abstract Builder<T> setValues(Collection<T> values);
        abstract ImmutableList.Builder<T> valuesBuilder();
        abstract public Attribute<T> build();

        @SafeVarargs
        public final Builder<T> addValue(T... value) {
            valuesBuilder().add(value);
            return this;
        }
    }

    abstract public Tag getValueTag();
    abstract public String getName();
    abstract public ImmutableList<T> getValues();
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
}
