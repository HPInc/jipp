package com.hp.jipp.encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Cycler {
    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        return AttributeGroup.read(new DataInputStream(new ByteArrayInputStream(toBytes(group))));
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
        return attributeType.getEncoder().read(in, Tag.read(in));
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return (Attribute<T>) Attribute.read(in, Tag.read(in));
    }

    public static byte[] toBytes(Attribute attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new DataOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }

    public static Packet cycle(Packet in) throws IOException {
        return Packet.read(new DataInputStream(new ByteArrayInputStream(toBytes(in))));
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
