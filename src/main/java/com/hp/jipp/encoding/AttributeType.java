package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.Hook;

import java.util.Arrays;
import java.util.List;

/**
 * Associates a specific tag and name such that an attribute can be safely created or retrieved from a group
 */
public class AttributeType<T> {

    private final Attribute.BaseEncoder<T> encoder;
    private final Tag tag;
    private final String name;

    public AttributeType(Attribute.BaseEncoder<T> encoder, Tag tag, String name) {
        if (!(encoder.valid(tag) || Hook.is(Attribute.Companion.getHOOK_ALLOW_BUILD_INVALID_TAGS()))) {
            throw new BuildError("Invalid tag " + tag + " for encoder " + encoder);
        }
        this.encoder = encoder;
        this.tag = tag;
        this.name = name;
    }

    /** Create an attribute of this attribute type with supplied values */
    @SafeVarargs
    public final Attribute<T> of(T... values) {
        return new Attribute<>(getTag(), name, Arrays.asList(values), getEncoder());
    }

    /** Create an attribute of this attribute type with supplied values */
    public final Attribute<T> of(List<T> values) {
        return new Attribute<>(getTag(), name, values, getEncoder());
    }

    public Attribute.BaseEncoder<T> getEncoder() {
        return encoder;
    }

    public Tag getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    /** Return true if the attribute has a matching encoder */
    boolean isValid(Attribute<?> attribute) {
        return attribute.getEncoder().equals(getEncoder());
    }

    /** If possible, convert the supplied attribute into an attribute of this type. */
    @SuppressWarnings("unchecked")
    public Optional<Attribute<T>> of(Attribute<?> attribute) {
        if (attribute.getEncoder() == encoder) {
            return Optional.of(of((List<T>) attribute.getValues()));
        } else {
            return Optional.absent();
        }
    }
}
