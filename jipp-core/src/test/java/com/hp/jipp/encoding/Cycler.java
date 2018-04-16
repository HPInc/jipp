package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import com.hp.jipp.model.IppPacket;
import com.hp.jipp.util.ParseError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kotlin.text.Charsets;

public class Cycler {

    public static final Map<String, AttributeType<?>> sAttributeTypeMap = new HashMap<String, AttributeType<?>>();
    static {
        for (AttributeType<?> entry: Types.all) {
            sAttributeTypeMap.put(entry.getName(), entry);
        }
    }

    public static final Encoder.Finder sFinder = new Encoder.Finder() {
        @Override
        public Encoder<?> find(Tag valueTag, String name) throws IOException {
            // Check for a matching attribute type
            if (sAttributeTypeMap.containsKey(name)) {
                AttributeType<?> attributeType = sAttributeTypeMap.get(name);
                if (attributeType.getEncoder().valid(valueTag)) {
                    return attributeType.getEncoder();
                }
            }

            // If no valid match above then try with each default encoder
            for (Encoder<?> encoder: AttributeGroup.encoders) {
                if (encoder.valid(valueTag)) {
                    return encoder;
                }
            }
            throw new ParseError("No encoder found for " + name + "(" + valueTag + ")");
        }
    };

    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        IppInputStream in = new IppInputStream(new ByteArrayInputStream(toBytes(group)), sFinder);
        return AttributeGroup.read(in, Tag.read(in));
    }

    public static byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        group.write(out);
        Tag.endOfAttributes.write(out);
        return bytesOut.toByteArray();
    }

    /** Return an attribute encoded by itself and parsed through an AttributeType */
    @SuppressWarnings("unchecked")
    public static <T, U> Attribute<T> cycle(AttributeType<T> attributeType, Attribute<U> attribute)
            throws IOException {
        IppInputStream in = new IppInputStream(new ByteArrayInputStream(toBytes(attribute)), sFinder);
        Tag tag = Tag.read(in);
        String name = new String(in.readValueBytes(), Charsets.UTF_8);
        return in.readAttribute(attributeType.getEncoder(), tag, name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        IppInputStream in = new IppInputStream(new ByteArrayInputStream(toBytes(attribute)), sFinder);
        return (Attribute<T>) in.readAttribute(Tag.read(in));
    }

    public static byte[] toBytes(Attribute<?> attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new IppOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }

    public static IppPacket cycle(IppPacket packet) throws IOException {
        return IppPacket.parse(new ByteArrayInputStream(toBytes(packet)));
    }

    public static byte[] toBytes(IppPacket packet) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        packet.write(out);
        return bytesOut.toByteArray();
    }
}
