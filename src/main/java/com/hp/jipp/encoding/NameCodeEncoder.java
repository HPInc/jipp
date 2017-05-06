package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/** An encoder for NameCode enumerations. */
@AutoValue
public abstract class NameCodeEncoder<T extends NameCode> extends Attribute.Encoder<T> {

    /**
     * Return a new NameCode.Encoder including values from all static members defined in the class (from reflection)
     */
    @SuppressWarnings("unchecked")
    public static <T extends NameCode> NameCodeEncoder<T> of(Class<T> cls,
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
    public static <T extends NameCode> NameCodeEncoder<T> of(String name, Collection<T> enums,
            NameCode.Factory<T> factory) {
        return new AutoValue_NameCodeEncoder<>(name,
                new ImmutableMap.Builder<Integer, T>().putAll(NameCode.toMap(enums)).build(),
                factory);
    }

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
    String getType() {
        return getName();
    }

    @Override
    T readValue(DataInputStream in, Tag valueTag) throws IOException {
        // TODO: We need this code but we never use it because we actually parse an IntegerType
        // and convert it, sometimes much later. With Packet in model can we do this a lot earlier and
        // not waste time on the conversion? And actually call this code?
        return get(IntegerType.ENCODER.readValue(in, valueTag));
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

