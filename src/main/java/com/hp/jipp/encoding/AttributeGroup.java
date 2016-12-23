package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Represents a group of attributes */
@AutoValue
abstract public class AttributeGroup {

    public static Builder builder(Tag startTag) {
        if (!startTag.isDelimiter()) throw new IllegalArgumentException("Not a delimiter: " + startTag);
        return new AutoValue_AttributeGroup.Builder().setStartTag(startTag);
    }

    public static AttributeGroup create(Tag startTag, Attribute<?>... attributes) {
        return builder(startTag).setAttributes(attributes).build();
    }

    abstract public Tag getStartTag();
    abstract public ImmutableList<Attribute<?>> getAttributes();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setStartTag(Tag startTag);
        public abstract Builder setAttributes(ImmutableList<Attribute<?>> attributes);
        public abstract Builder setAttributes(Attribute<?>... values);
        abstract ImmutableList.Builder<Attribute<?>> attributesBuilder();
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
        return null;
    }
}
