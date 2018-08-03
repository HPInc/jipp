
package com.hp.jipp.encoding;

import com.hp.jipp.model.IppPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;

public class Cycler {

    public static <T> Attribute<T> cycle(AttributeType<T> type, Attribute<T> attribute) throws IOException {
        return cycle(groupOf(Tag.printerAttributes, attribute)).get(type);
    }

    public static List<Attribute<?>> cycle(Attribute<?>... attribute) throws IOException {
        return cycle(new AttributeGroup(Tag.printerAttributes, Arrays.asList(attribute)))
                .getAttributes();
    }

    /** Write group to a byte stream and then read it back and assert that the contents are identical */
    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytes);
        group.write(output);
        output.close();

        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return AttributeGroup.read(input, Tag.read(input));
    }

    /** Return a packet that was written to a byte stream and read back in */
    public static IppPacket cycle(IppPacket packet) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytes);
        packet.write(output);
        output.close();

        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return IppPacket.read(input);
    }

    public static byte[] toBytes(IppPacket packet) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        packet.write(out);
        return bytesOut.toByteArray();
    }

    public static byte[] toBytes(Attribute<?> attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        AttributeGroup.Companion.writeAttribute(out, attribute);
        return bytesOut.toByteArray();
    }
}
