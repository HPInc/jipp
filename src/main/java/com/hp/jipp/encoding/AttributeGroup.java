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
                    ImmutableMap.Builder<String, Attribute<?>> builder =
                            new ImmutableMap.Builder<>();
                    for (Attribute<?> attribute : getAttributes()) {
                        builder.put(attribute.getName(), attribute);
                    }
                    return builder.build();
                }
            });

    /** Return a lazily-created, read-only map of attribute name to a list of matching attributes */
    public Map<String, Attribute<?>> getAttributesMap() {
        return mAttributeMap.get();
    }

    /** Finds an attribute in the group matching the name */
    public Optional<Attribute<?>> getAttribute(String name) {
        // TODO: need AttributeType so that type assumptions can be made
        return Optional.<Attribute<?>>fromNullable(getAttributesMap().get(name));
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

    void write(DataOutputStream out) throws IOException {
        out.writeByte(getStartTag().getValue());
        for(Attribute attribute : getAttributes()) {
            attribute.write(out);
        }
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
                builder.addAttribute(readAttribute(in, valueTag));
            }
        }
        return builder.build();
    }

    static Attribute<?> readAttribute(DataInputStream in, Tag valueTag) throws IOException {
        for (Attribute.ClassEncoder classEncoder: Attribute.ENCODERS) {
            if (classEncoder.getEncoder().valid(valueTag)) {
                return classEncoder.getEncoder().read(in, valueTag);
            }
        }
        throw new RuntimeException("Unreadable attribute " + valueTag);
    }
}
