package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.hp.jipp.Hook;

import java.util.Collection;

/**
 * Associates a specific tag and name such that an attribute can be safely created or retrieved from a group
 */
public class AttributeType<T> {

    private final Encoder<T> encoder;
    private final Tag tag;
    private final String name;

    public AttributeType(Encoder<T> encoder, Tag tag, String name) {
        if (!(encoder.valid(tag) || Hook.is(Attribute.HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw new BuildError("Invalid tag " + tag + " for encoder " + encoder);
        }
        this.encoder = encoder;
        this.tag = tag;
        this.name = name;
    }

    /** Create an attribute of this attribute type with supplied values */
    @SafeVarargs
    public final Attribute<T> of(T... values) {
        return getEncoder().builder(getTag()).setValues(values).setName(getName()).build();
    }

    /** Create an attribute of this attribute type with supplied values */
    public final Attribute<T> of(Collection<T> values) {
        return getEncoder().builder(getTag()).setValues(values).setName(getName()).build();
    }

    public Encoder<T> getEncoder() {
        return encoder;
    }

    public Tag getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    /** Return true if the attribute has a matching encoder */
    public boolean isValid(Attribute<?> attribute) {
        return attribute.getEncoder().equals(getEncoder());
    }

    /** Convert the other attribute into this one if reasonable */
    public Optional<Attribute<T>> adopt(Attribute<?> attribute) {
        return Optional.absent();
    }
}
