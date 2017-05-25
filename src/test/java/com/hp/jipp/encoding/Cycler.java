package com.hp.jipp.encoding;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Packet;
import com.hp.jipp.util.ParseError;
import com.hp.jipp.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

public class Cycler {

    public static final Packet.Parser sParser = Packet.parserOf(Attributes.All);
    public static final Function<? super AttributeType<?>, String> sAttributeNameProjector = new Function<AttributeType<?>, String>() {
        @Override
        public String apply(@Nonnull AttributeType<?> input) {
            return input.getName();
        }
    };
    public static final Map<String, AttributeType<?>> sAttributeTypeMap = Maps.uniqueIndex(Attributes.All,
            sAttributeNameProjector);

    public static final Attribute.EncoderFinder sFinder = new Attribute.EncoderFinder() {
        @Override
        public Attribute.BaseEncoder<?> find(Tag valueTag, String name) throws IOException {
            // Check for a matching attribute type
            if (sAttributeTypeMap.containsKey(name)) {
                AttributeType<?> attributeType = sAttributeTypeMap.get(name);
                if (attributeType.getEncoder().valid(valueTag)) {
                    return attributeType.getEncoder();
                }
            }

            // If no valid match above then try with each default encoder
            for (Attribute.BaseEncoder<?> encoder: AttributeGroup.ENCODERS) {
                if (encoder.valid(valueTag)) {
                    return encoder;
                }
            }
            throw new ParseError("No encoder found for " + name + "(" + valueTag + ")");
        }
    };

    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        Function<? super AttributeType<?>, String> projector =
                new Function<AttributeType<?>, String>() {
                    @Override
                    public String apply(@Nonnull AttributeType<?> input) {
                        return input.getName();
                    }
                };
        final Map<String, AttributeType<?>> attributeTypeMap = Maps.uniqueIndex(Attributes.All, projector);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(group)));
        return AttributeGroup.read(Tag.Companion.read(in), attributeTypeMap, in);
    }

    public static byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        group.write(out);
        Tag.EndOfAttributes.write(out);
        return bytesOut.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(AttributeType attributeType, Attribute<T> attribute)
            throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        Tag tag = Tag.Companion.read(in);
        String name = new String(Attribute.readValueBytes2(in), Util.UTF8);
        return attributeType.getEncoder().read(in, sFinder, tag, name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return (Attribute<T>) Attribute.Companion.read(in, sFinder, Tag.Companion.read(in));
    }


    public static byte[] toBytes(Attribute<?> attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new DataOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }

    public static Packet cycle(Packet in) throws IOException {
        return sParser.parse(new DataInputStream(new ByteArrayInputStream(toBytes(in))));
    }

    public static Packet cycle(Packet.Builder in) throws IOException {
        return cycle(in.build());
    }

    public static byte[] toBytes(Packet in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        in.write(out);
        return bytesOut.toByteArray();
    }
}
