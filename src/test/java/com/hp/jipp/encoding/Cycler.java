package com.hp.jipp.encoding;

import com.hp.jipp.model.PacketKt;
import com.hp.jipp.model.Types;
import com.hp.jipp.model.Packet;
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

    public static final Packet.Parser sParser = Packet.parserOf(Types.all);
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
            for (Encoder<?> encoder: AttributeGroup.ENCODERS) {
                if (encoder.valid(valueTag)) {
                    return encoder;
                }
            }
            throw new ParseError("No encoder found for " + name + "(" + valueTag + ")");
        }
    };

    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(group)));
        return AttributeGroupKt.readGroup(in, TagKt.readTag(in), sAttributeTypeMap);
    }

    public static byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        AttributeGroupKt.writeGroup(out, group);
        TagKt.writeTag(out, Tag.endOfAttributes);
        return bytesOut.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(AttributeType attributeType, Attribute<T> attribute)
            throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        Tag tag = TagKt.readTag(in);
        String name = new String(IppEncodingsKt.readValueBytes(in), Charsets.UTF_8);
        return EncoderKt.readAttribute(in, attributeType.getEncoder(), sFinder, tag, name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return (Attribute<T>) IppEncodingsKt.readAttribute(in, sFinder, TagKt.readTag(in));
    }

    public static byte[] toBytes(Attribute<?> attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        AttributeKt.writeAttribute(new DataOutputStream(bytesOut), attribute);
        return bytesOut.toByteArray();
    }

    public static Packet cycle(Packet in) throws IOException {
        return sParser.parse(new DataInputStream(new ByteArrayInputStream(toBytes(in))));
    }

    public static byte[] toBytes(Packet in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        PacketKt.writePacket(out, in);
        return bytesOut.toByteArray();
    }
}
