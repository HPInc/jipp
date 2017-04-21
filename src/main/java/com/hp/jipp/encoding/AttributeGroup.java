package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hp.jipp.util.Hook;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A specific group of attributes found in a packet.  */
@AutoValue
abstract public class AttributeGroup {
    public static final String HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP =
            AttributeGroup.class.getName() + ".HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP";

    /** Return a complete attribute group */
    public static AttributeGroup create(Tag startTag, Attribute<?>... attributes) {
        return create(startTag, Arrays.asList(attributes));
    }

    /** Return a complete attribute group */
    public static AttributeGroup create(Tag startTag, List<Attribute<?>> attributes) {
        if (!startTag.isDelimiter()) throw new BuildError("Not a delimiter: " + startTag);
        return new AutoValue_AttributeGroup.Builder()
                .setTag(startTag)
                .setAttributes(attributes).build();
    }

    @AutoValue.Builder
    abstract static class Builder {

        abstract Builder setTag(Tag startTag);

        public abstract Builder setAttributes(List<Attribute<?>> attributes);

        abstract List<Attribute<?>> getAttributes();

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
    }

    static AttributeGroup read(Tag startTag, DataInputStream in) throws IOException {
        boolean attributes = true;
        ImmutableList.Builder<Attribute<?>> attributesBuilder = new ImmutableList.Builder<>();

        while(attributes) {
            in.mark(1);
            Tag valueTag = Tag.toTag(in.readByte());
            if (valueTag.isDelimiter()) {
                in.reset();
                attributes = false;
            } else {
                attributesBuilder.add(Attribute.read(in, AttributeEncoders.ENCODERS, valueTag));
            }
        }
        return create(startTag, attributesBuilder.build());
    }

    /** Return the tag that delimits this group */
    abstract public Tag getTag();

    /** Return all attributes in this group */
    abstract public List<Attribute<?>> getAttributes();

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
            return attributeType.from(attribute);
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

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(getTag().getValue());
        for(Attribute<?> attribute : getAttributes()) {
            attribute.write(out, AttributeEncoders.ENCODERS);
        }
    }

    public static AttributeGroup read(DataInputStream in) throws IOException {
        return read(Tag.read(in), in);
    }

    /** Similar to toString but applies additional knowledge of enclosed attribute types */
    public String describe(final Map<String, AttributeType<?>> attributeTypeMap) {
        String attributes = Lists.transform(getAttributes(), new Function<Attribute<?>, Attribute<?>>() {
            @Override
            public Attribute<?> apply(Attribute<?> input) {
                if (input.getValueTag() == Tag.TextWithLanguage || input.getValueTag() == Tag.NameWithLanguage) {
                    // Don't convert these because the supplied attributeType might strip the language field
                    return input;
                }
                if (attributeTypeMap.containsKey(input.getName())) {
                    Optional<? extends Attribute<?>> optAttribute = attributeTypeMap.get(input.getName()).from(input);
                    if (optAttribute.isPresent()) return optAttribute.get();
                }
                return input;
            }
        }).toString();
        return "AttributeGroup{tag=" + getTag() + ", attributes=" + attributes;
    }
}
