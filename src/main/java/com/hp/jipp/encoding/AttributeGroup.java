package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttributeGroup {
    private byte startTag;
    private List<Attribute> attributes = new ArrayList<>();

    private AttributeGroup(byte startTag) {
        this.startTag = startTag;
    }

    public AttributeGroup(AttributeGroup other) {
        this(other.startTag);
        attributes = new ArrayList<>(other.attributes);
    }

    public byte getStartTag() {
        return startTag;
    }

    /** Return the list of attributes. Do not modify this list */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(startTag);
        for(Attribute attribute : attributes) {
            attribute.write(out);
        }
    }

    public static AttributeGroup read(int startTag, DataInputStream in) throws IOException {
        Builder builder = new Builder((byte) startTag);
        boolean attributes = true;
        while(attributes) {
            in.mark(1);
            byte valueTag = in.readByte();
            if (Tags.isDelimiter(valueTag)) {
                in.reset();
                attributes = false;
            } else {

                builder.addAttribute(readAttribute(in, valueTag));
            }
        }
        return builder.build();
    }

    public static Attribute readAttribute(DataInputStream in, byte valueTag) throws IOException {
        if (IntegerAttribute.hasTag(valueTag)) {
            return IntegerAttribute.read(in, valueTag);
        } else if (StringAttribute.hasTag(valueTag)) {
            return StringAttribute.read(in, valueTag);
        } else if (BooleanAttribute.hasTag(valueTag)) {
            return BooleanAttribute.read(in, valueTag);
        } else if (CollectionAttribute.hasTag(valueTag)) {
            return CollectionAttribute.read(in, valueTag);
        }

        // TODO: RangeOfInteger attribute
        // TODO: 1setofX
        // TODO: resolution
        // TODO: dateTime
        // TODO: LanguageStringAttribute
        // TODO: Collection! ugh.
        return OctetAttribute.read(in, valueTag);
    }

    @Override
    public String toString() {
        return "tag=" + Tags.toString(startTag) +
                " attrs=" + attributes;
    }

    public static class Builder {
        final AttributeGroup prototype;

        public Builder(byte startTag) {
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
