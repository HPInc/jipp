package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
public abstract class AttributeGroup {
    public static final String HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP =
            AttributeGroup.class.getName() + ".HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP";

    /** Default encoders available to parse incoming data */
    public static final List<Attribute.BaseEncoder<?>> ENCODERS = ImmutableList.of(
            IntegerType.ENCODER, UriType.ENCODER, StringType.ENCODER, BooleanType.ENCODER, LangStringType.ENCODER,
            CollectionType.ENCODER, RangeOfIntegerType.ENCODER, ResolutionType.ENCODER, OctetStringType.ENCODER);
    // TODO: dateTime?
    // TODO: Move to Packet

    /** Return a complete attribute group */
    public static AttributeGroup of(Tag startTag, Attribute<?>... attributes) {
        return of(startTag, Arrays.asList(attributes));
    }

    /** Return a complete attribute group */
    public static AttributeGroup of(Tag startTag, List<Attribute<?>> attributes) {
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

    public static AttributeGroup read(Tag startTag, DataInputStream in) throws IOException {
        return read(startTag, ImmutableMap.<String, AttributeType<?>>of(), in);
    }

    static Attribute.EncoderFinder finderOf(final Map<String, AttributeType<?>> attributeTypes,
            final List<Attribute.BaseEncoder<?>> encoders) {
        return new Attribute.EncoderFinder() {
            @Override
            public Attribute.BaseEncoder<?> find(Tag valueTag, String name) throws IOException {
                // Check for a matching attribute type
                if (attributeTypes.containsKey(name)) {
                    AttributeType<?> attributeType = attributeTypes.get(name);
                    if (attributeType.getEncoder().valid(valueTag)) {
                        return attributeType.getEncoder();
                    }
                }

                // If no valid match above then try with each default encoder
                for (Attribute.BaseEncoder<?> encoder : encoders) {
                    if (encoder.valid(valueTag)) {
                        return encoder;
                    }
                }
                throw new ParseError("No encoder found for " + name + "(" + valueTag + ")");
            }
        };
    }

    public static AttributeGroup read(Tag startTag, Map<String, AttributeType<?>> attributeTypes, DataInputStream in)
            throws IOException {
        boolean attributes = true;
        ImmutableList.Builder<Attribute<?>> attributesBuilder = new ImmutableList.Builder<>();

        Attribute.EncoderFinder finder = finderOf(attributeTypes, AttributeGroup.ENCODERS);

        while (attributes) {
            in.mark(1);
            Tag valueTag = Tag.read(in);
            if (valueTag.isDelimiter()) {
                in.reset();
                attributes = false;
            } else {
                attributesBuilder.add(Attribute.read(in, finder, valueTag));
            }
        }
        return of(startTag, attributesBuilder.build());
    }

    /** Return the tag that delimits this group */
    public abstract Tag getTag();

    /** Return all attributes in this group */
    public abstract List<Attribute<?>> getAttributes();

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

    /** Return a lazily-created, parse-only map of attribute name to a list of matching attributes */
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
            return attributeType.of(attribute);
        }
    }

    /**
     * Return values for the specified attribute type in this group, or an empty list if not present
     */
    public <T> List<T> getValues(AttributeType<T> attributeType) {
        Optional<Attribute<T>> attribute = get(attributeType);
        if (!attribute.isPresent()) return ImmutableList.of();
        return attribute.get().getValues();
    }

    /**
     * Return a single value, if any exist for this attribute
     */
    public <T> Optional<T> getValue(AttributeType<T> attributeType) {
        List<T> values = getValues(attributeType);
        if (values.isEmpty()) return Optional.absent();
        return Optional.of(values.get(0));
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(getTag().getCode());
        for (Attribute<?> attribute : getAttributes()) {
            attribute.write(out);
        }
    }

    public static AttributeGroup read(DataInputStream in) throws IOException {
        return read(Tag.read(in), in);
    }
}
