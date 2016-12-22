package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public Tag getStartTag() {
        return startTag;
    }

    /** Return the list of attributes. Do not modify this list */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(startTag.getValue());
        for(Attribute attribute : attributes) {
            attribute.write(out);
        }
    }

    public static AttributeGroup read(Tag startTag, DataInputStream in) throws IOException {
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

    public static Attribute readAttribute(DataInputStream in, Tag valueTag) throws IOException {
        Attribute attr;

        attr = IntegerAttribute.read(in, valueTag);
        if (attr != null) return attr;

        attr = StringAttribute.read(in, valueTag);
        if (attr != null) return attr;

        attr = BooleanAttribute.read(in, valueTag);
        if (attr != null) return attr;

        attr = CollectionAttribute.read(in, valueTag);
        if (attr != null) return attr;

        // TODO: RangeOfInteger attribute
        // TODO: 1setofX
        // TODO: resolution
        // TODO: dateTime
        // TODO: LanguageStringAttribute
        return OctetAttribute.read(in, valueTag);
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
