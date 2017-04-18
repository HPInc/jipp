package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/** Represents a group of attributes */
@AutoValue
abstract public class AttributeGroup {

    /** Return a builder, allowing attributes to be added progressively */
    public static Builder builder(Tag startTag) {
        if (!startTag.isDelimiter()) throw new IllegalArgumentException("Not a delimiter: " + startTag);
        return new AutoValue_AttributeGroup.Builder().setStartTag(startTag);
    }

    /** Return a complete attribute group with all attributes */
    public static AttributeGroup create(Tag startTag, Attribute<?>... attributes) {
        return builder(startTag).setAttributes(attributes).build();
    }

    /** Return the tag that delimits this group */
    abstract public Tag getStartTag();

    /** Return all attributes in this group */
    abstract public ImmutableList<Attribute<?>> getAttributes();

    /** Lazy attribute map, generated only when needed */
    private Supplier<ImmutableMap<String, Attribute<?>>> mAttributeMap =
            Suppliers.memoize(new Supplier<ImmutableMap<String, Attribute<?>>>() {
                @Override
                public ImmutableMap<String, Attribute<?>> get() {
                    ImmutableMap.Builder<String, Attribute<?>> builder = new ImmutableMap.Builder<>();
                    for (Attribute<?> attribute : getAttributes()) {
                        builder.put(attribute.getName(), attribute);
                    }
                    return builder.build();
                }
            });

    /** Return a lazily-created, read-only map of attribute name to a list of matching attributes */
    public Map<String, Attribute<?>> getMap() {
        return mAttributeMap.get();
    }

    /** Finds an attribute in the group matching the name */
    public Optional<Attribute<?>> get(String name) {
        // TODO: need AttributeType so that type assumptions can be made
        return Optional.<Attribute<?>>fromNullable(getMap().get(name));
    }

    /** Return a attribute from this group. */
    public <T> Optional<Attribute<T>> get(AttributeType<T> attributeType) {
        Attribute<?> attribute = getMap().get(attributeType.getName());
        if (attribute != null && attributeType.isValid(attribute)) {
            return Optional.of((Attribute<T>) attribute);
        } else {
            return Optional.absent();
        }
    }

    /**
     * Return values for the specified attribute type in this group. If the attribute is missing, return an empty list.
     */
    public <T> ImmutableList<T> getValues(AttributeType<T> attributeType) {
        Optional<Attribute<T>> attribute = get(attributeType);
        if (!attribute.isPresent()) return ImmutableList.of();
        return attribute.get().getValues();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setStartTag(Tag startTag);
        public abstract Builder setAttributes(ImmutableList<Attribute<?>> attributes);
        public abstract Builder setAttributes(Attribute<?>... values);
        abstract ImmutableList.Builder<Attribute<?>> attributesBuilder();

        // TODO: Add some validation here to prevent multiple attributes of the same name
        public abstract AttributeGroup build();

        public final Builder addAttribute(Attribute<?>... attribute) {
            attributesBuilder().add(attribute);
            return this;
        }
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(getStartTag().getValue());
        for(Attribute attribute : getAttributes()) {
            attribute.write(out);
        }
    }

    public static AttributeGroup read(DataInputStream in) throws IOException {
        return read(Tag.read(in), in);
    }

    static AttributeGroup read(Tag startTag, DataInputStream in) throws IOException {
        Builder builder = builder(startTag);
        boolean attributes = true;
        while(attributes) {
            in.mark(1);
            Tag valueTag = Tag.toTag(in.readByte());
            if (valueTag.isDelimiter()) {
                in.reset();
                attributes = false;
            } else {
                builder.addAttribute(Attribute.read(in, valueTag));
            }
        }
        return builder.build();
    }
}
