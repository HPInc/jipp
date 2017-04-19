package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.jipp.Hook;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Represents a group of attributes */
@AutoValue
abstract public class AttributeGroup {
    public static final String HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP =
            AttributeGroup.class.getName() + ".HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP";

    /** Return a builder, allowing attributes to be added progressively */
    public static Builder builder(Tag startTag) {
        if (!startTag.isDelimiter()) throw new BuildError("Not a delimiter: " + startTag);
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
    Map<String, Attribute<?>> getMap() {
        return mAttributeMap.get();
    }

    /** Return a attribute from this group. */
    @SuppressWarnings("unchecked")
    public <T> Optional<Attribute<T>> get(AttributeType<T> attributeType) {

        if (!getMap().containsKey(attributeType.getName())) return Optional.absent();

        Attribute<?> attribute = getMap().get(attributeType.getName());
        if (attributeType.isValid(attribute)) {
            return Optional.of((Attribute<T>) attribute);
        } else {
            return attributeType.adopt(attribute);
        }
    }

    /**
     * Return values for the specified attribute type in this group. If the attribute is missing, return an empty list.
     */
    public <T> List<T> getValues(AttributeType<T> attributeType) {
        Optional<Attribute<T>> attribute = get(attributeType);
        if (!attribute.isPresent()) return ImmutableList.of();
        return attribute.get().getValues();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setStartTag(Tag startTag);

        public abstract Builder setAttributes(ImmutableList<Attribute<?>> attributes);

        public abstract Builder setAttributes(Attribute<?>... values);

        abstract ImmutableList<Attribute<?>> getAttributes();

        abstract ImmutableList.Builder<Attribute<?>> attributesBuilder();

        abstract AttributeGroup autoBuild();

        public AttributeGroup build() {
            // RFC2910: Within an attribute group, if two or more attributes have the same name, the attribute group
            // is malformed (see [RFC2911] section 3.1.3).
            // Throw if someone attempts this.
            Set<String> exist = new HashSet<>();
            for (Attribute<?> attribute : getAttributes()) {
                if (exist.contains(attribute.getName()) && !Hook.is(HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP)) {
                    throw new BuildError("Attribute Group contains more than one '" + attribute.getName() +
                            "' in " + getAttributes());
                }
                exist.add(attribute.getName());
            }
            return autoBuild();
        }

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
