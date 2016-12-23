package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Represents a group of attributes */
public class AttributeGroup {

    private Tag startTag;
    private List<Attribute> attributes = new ArrayList<>();

    private AttributeGroup(Tag startTag) {
        this.startTag = startTag;
    }

    public AttributeGroup(AttributeGroup other) {
        this(other.startTag);
        attributes = new ArrayList<>(other.attributes);
    }

    Tag getStartTag() {
        return startTag;
    }

    /** Return the list of attributes. Do not modify this list */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    void write(DataOutputStream out) throws IOException {
        out.writeByte(startTag.getValue());
        for(Attribute attribute : attributes) {
            attribute.write(out);
        }
    }

    static AttributeGroup read(Tag startTag, DataInputStream in) throws IOException {
        Builder builder = new Builder(startTag);
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

    @Override
    public String toString() {
        return "tag=" + startTag +
                " attrs=" + attributes;
    }

    public static class Builder {
        final AttributeGroup prototype;

        public Builder(Tag startTag) {
            prototype = new AttributeGroup(startTag);
        }

        public Builder addAttribute(Attribute attribute) {
            prototype.attributes.add(attribute);
            return this;
        }

        public AttributeGroup build() {
            return new AttributeGroup(prototype);
        }
    }
}
