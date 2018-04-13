package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import com.hp.jipp.model.IppPacket;
import com.hp.jipp.util.ParseError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kotlin.text.Charsets;

public class Cycler {

    public static final IppPacket.Parser sParser = IppPacket.parserOf(Types.all);
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
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(group)));
        return AttributeGroup.read(in, Tag.read(in), sAttributeTypeMap);
    }

    public static byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        group.write(out);
        Tag.endOfAttributes.write(out);
        return bytesOut.toByteArray();
    }

    /** Return an attribute encoded by itself and parsed through an AttributeType */
    @SuppressWarnings("unchecked")
    public static <T, U> Attribute<T> cycle(AttributeType<T> attributeType, Attribute<U> attribute)
            throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        Tag tag = Tag.read(in);
        String name = new String(IppEncodingsKt.readValueBytes(in), Charsets.UTF_8);
        return EncoderKt.readAttribute(in, attributeType.getEncoder(), sFinder, tag, name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return (Attribute<T>) IppEncodingsKt.readAttribute(in, sFinder, Tag.read(in));
    }

    public static byte[] toBytes(Attribute<?> attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new DataOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }

    public static IppPacket cycle(IppPacket in) throws IOException {
        return sParser.parse(new DataInputStream(new ByteArrayInputStream(toBytes(in))));
    }

    public static byte[] toBytes(IppPacket in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        in.write(out);
        return bytesOut.toByteArray();
    }
}
