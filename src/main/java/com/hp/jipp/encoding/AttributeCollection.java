package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;

/** A collection of attributes, used by {@link CollectionType} to model the contents of an RFC3382 collection. */
public class AttributeCollection {

    public static AttributeCollection of(Attribute<?>... attr) {
        return new AttributeCollection(ImmutableList.copyOf(attr));
    }

    private List<Attribute<?>> attributes;

    public AttributeCollection(List<Attribute<?>> attributes) {
        this.attributes = attributes;
    }

    /** Return the first attribute matching the type */
    public <T> Optional<Attribute<T>> get(AttributeType<T> type) {
        for (Attribute<?> attribute : this.attributes) {
            if (attribute.getValueTag().equals(type.getTag()) && attribute.getName().equals(type.getName())) {
                return Optional.of((Attribute<T>) attribute);
            }
        }
        return Optional.absent();
    }

    /** Return all values found from the first attribute matching the type, or an empty list if no match */
    public <T> List<T> values(AttributeType<T> type) {
        Optional<Attribute<T>> attribute = get(type);
        if (attribute.isPresent()) {
            return attribute.get().getValues();
        }
        return ImmutableList.of();
    }

    public List<Attribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
