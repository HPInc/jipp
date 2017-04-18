package com.hp.jipp.encoding;

/**
 * Associates a specific tag and name such that an attribute can be safely created or retrieved from a group
 */
public class AttributeType<T> {

    private final AttributeEncoder<T> encoder;
    private final Tag tag;
    private final String name;

    AttributeType(AttributeEncoder<T> encoder, Tag tag, String name) {
        this.encoder = encoder;
        this.tag = tag;
        this.name = name;
    }

    @SafeVarargs
    public final Attribute<T> create(T... values) {
        return getEncoder().builder(getTag()).setValues(values).setName(getName()).build();
    }

    public AttributeEncoder<T> getEncoder() {
        return encoder;
    }

    public Tag getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public boolean isValid(Attribute<?> attribute) {
        return attribute.getEncoder() == getEncoder();
    }

}
