package com.hp.jipp.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.OctetStringType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.util.Util;

import static com.hp.jipp.encoding.Cycler.*;

public class PacketTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private Packet.Parser parser = Packet.parserOf(Attributes.All);

    private Packet packet;
    private final Packet defaultPacket;
    private Packet.Builder builder;

    public PacketTest() {
        builder = new Packet.Builder(Operation.HoldJob, 0x50607);
        builder.setVersionNumber(0x102);
        defaultPacket = builder.build();
    }


    @Test
    public void writeEmptyPacket() throws IOException {
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x03,
        }, toBytes(defaultPacket));
    }

    @Test
    public void readEmptyPacket() throws IOException {
        packet = cycle(defaultPacket);
        assertEquals(defaultPacket.getVersionNumber(), packet.getVersionNumber());
        assertEquals(defaultPacket.getCode(), packet.getCode());
        assertEquals(defaultPacket.getRequestId(), packet.getRequestId());
    }

    @Test
    public void readShortPacket() throws IOException {
        byte[] in = new byte[] {
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
                (byte) 0x09,
        };
        exception.expect(EOFException.class);
        parser.parse(new DataInputStream(new ByteArrayInputStream(in)));
    }

    @Test
    public void writeDataPacket() throws IOException {
        builder.setData(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD});
        packet = builder.build();
        assertArrayEquals(new byte[]{
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x03,
                (byte) 0xFF,
                (byte) 0xFE,
                (byte) 0xFD,
        }, toBytes(packet));
    }

    @Test
    public void readDataPacket() throws IOException {
        byte data[] = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD };
        builder.setData(data);
        packet = cycle(builder.build());
        assertArrayEquals(data, packet.getData());
    }


    @Test
    public void writeSingleEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> emptyGroup = new ArrayList<>();
        emptyGroup.add(AttributeGroup.of(Tag.OperationAttributes));
        builder.setAttributeGroups(emptyGroup);
        packet = builder.build();
        assertArrayEquals(new byte[]{
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x01,
                (byte) 0x03,
        }, toBytes(packet));
    }

    @Test
    public void readSingleEmptyAttributeGroupPacket() throws IOException {
        builder.setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes));
        packet = cycle(builder.build());
        assertEquals(1, packet.getAttributeGroups().size());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getTag());
    }

    @Test
    public void readMultiEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> groups = new ArrayList<>();
        groups.add(AttributeGroup.of(Tag.OperationAttributes));
        groups.add(AttributeGroup.of(Tag.JobAttributes));
        groups.add(AttributeGroup.of(Tag.get((byte)0x08))); // reserved but legal
        builder.setAttributeGroups(groups);

        packet = cycle(builder.build());
        assertEquals(3, packet.getAttributeGroups().size());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getTag());
        assertEquals(Tag.JobAttributes, packet.getAttributeGroups().get(1).getTag());
        assertEquals(Tag.get(0x08), packet.getAttributeGroups().get(2).getTag());
    }

    @Test
    public void writeSingleAttributePacket() throws IOException {
        Attribute<byte[]> simpleAttribute = new OctetStringType(Tag.Charset, "attributes-charset")
                .of("US-ASCII".getBytes(Util.UTF8));
        List<AttributeGroup> group = new ArrayList<>();
        group.add(AttributeGroup.of(Tag.OperationAttributes, simpleAttribute));
        builder.setAttributeGroups(group);
        packet = builder.build();
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x01,
                (byte) 0x47,
                (byte) 0x00,
                (byte) 0x12,
                'a', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '-',
                'c', 'h', 'a', 'r', 's', 'e', 't',
                (byte) 0x00,
                (byte) 0x08,
                'U', 'S', '-', 'A', 'S', 'C', 'I', 'I',
                (byte) 0x03,
        }, toBytes(packet));
    }

    @Test
    public void parseErrorBadDelimiter() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("Illegal delimiter tag(x77)");

        byte[] bytes = new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x77, // NOT a Delimiter
                (byte) 0x47,
                (byte) 0x00,
                (byte) 0x12,
                'a', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '-',
                'c', 'h', 'a', 'r', 's', 'e', 't',
                (byte) 0x00,
                (byte) 0x08,
                'U', 'S', '-', 'A', 'S', 'C', 'I', 'I',
                (byte) 0x03,
        };

        parser.parse(new DataInputStream(new ByteArrayInputStream(bytes)));
    }


    @Test
    public void readSingleAttributePacket() throws IOException {
        Attribute<String> stringAttribute = new StringType(Tag.Charset, "attributes-charset")
                .of("US-ASCII");
        List<AttributeGroup> group = new ArrayList<>();
        group.add(AttributeGroup.of(Tag.OperationAttributes, stringAttribute));
        builder.setAttributeGroups(group);
        packet = cycle(builder.build());
        Attribute readAttribute = packet.getAttributeGroups().get(0).getAttributes().get(0);
        assertEquals("attributes-charset", readAttribute.getName());
        assertEquals(Tag.Charset, readAttribute.getValueTag());
        assertEquals("US-ASCII", readAttribute.getValue(0));
    }

    @Test
    public void writeMultiValueAttributePacket() throws IOException {
        Attribute<String> multiValueAttribute = new StringType(Tag.Charset, "attributes-charset")
                .of("US-ASCII", "UTF-8");

        List<AttributeGroup> group = new ArrayList<>();
        group.add(AttributeGroup.of(Tag.OperationAttributes, multiValueAttribute));
        builder.setAttributeGroups(group);
        packet = builder.build();
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x01,
                (byte) 0x47,
                (byte) 0x00,
                (byte) 0x12,
                'a', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '-',
                'c', 'h', 'a', 'r', 's', 'e', 't',
                (byte) 0x00,
                (byte) 0x08,
                'U', 'S', '-', 'A', 'S', 'C', 'I', 'I',
                (byte) 0x47,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x05,
                'U', 'T', 'F', '-', '8',
                (byte) 0x03,
        }, toBytes(packet));
        System.out.println(packet.toString());
    }

    @Test
    public void readMultiValueAttributePacket() throws IOException {
        builder.setCode(Operation.GetJobAttributes);
        builder.setRequestId(777);
        builder.setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("US-ASCII", "UTF-8")));
        packet = cycle(builder.build());

        assertEquals(Operation.GetJobAttributes, packet.getOperation());
        assertEquals(777, packet.getRequestId());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getTag());
        Attribute<String> attribute = packet.getAttributeGroups().get(0).get(Attributes.AttributesCharset);
        assertEquals(Arrays.asList("US-ASCII", "UTF-8"), attribute.getValues());
    }

    @Test
    public void getValue() throws IOException {
        builder.setCode(Operation.GetJobAttributes);
        builder.setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("US-ASCII", "UTF-8")));
        packet = cycle(builder.build());

        // Wrong group
        assertNull(packet.getValue(Tag.JobAttributes, Attributes.AttributesNaturalLanguage));

        // Wrong attr
        assertNull(packet.getValue(Tag.OperationAttributes, Attributes.AttributesNaturalLanguage));

        // All good!
        assertEquals("US-ASCII", packet.getValue(Tag.OperationAttributes, Attributes.AttributesCharset));
    }

    @Test
    public void getValues() throws IOException {
        builder.setCode(Operation.GetJobAttributes);
        builder.setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("US-ASCII", "UTF-8")));
        packet = cycle(builder.build());
        // Wrong group
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.JobAttributes, Attributes.AttributesCharset));

        // Wrong attr
        assertEquals(Arrays.asList("US-ASCII", "UTF-8"),
                packet.getValues(Tag.OperationAttributes, Attributes.AttributesCharset));

        // All good!
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.OperationAttributes, Attributes.AttributesNaturalLanguage));
    }

    @Test
    public void badStreamThrows() throws IOException {
        exception.expect(IOException.class);
        builder.setInputStreamFactory(new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                throw new IOException("oops!");
            }
        });
        packet = builder.build();

        packet.write(new DataOutputStream(new ByteArrayOutputStream()));
    }

    @Test
    public void badStreamThrowsBytes() throws IOException {
        exception.expect(IllegalArgumentException.class);
        builder.setInputStreamFactory(new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                throw new IOException("oops!");
            }
        });
        packet = builder.build();

        // Try to get all bytes for the packet
        getBytes(packet);
    }

    @Test
    public void printCorrectly() throws IOException {
        builder.setAttributeGroups(AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob)));
        Packet packet = cycle(builder.build());
        assertTrue(packet.toString().contains(Operation.CreateJob.getName()));
    }

    @Test
    public void showStream() throws Exception {
        builder.setAttributeGroups(AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob)));
        builder.setInputStreamFactory(new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return null;
            }
        });
        Packet packet = builder.build();
        assertTrue(packet.toString().contains("stream"));
        assertTrue(packet.toString().contains("stream"));
        assertTrue(packet.prettyPrint(120, "  ").contains("stream"));
    }

    @Test
    public void showData() throws Exception {
        builder.setAttributeGroups(AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob)));
        builder.setData(new byte[] { 0 });
        packet = builder.build();
        assertTrue(packet.toString().contains("dLen=1"));
        assertTrue(packet.toString().contains("dLen=1"));
        assertTrue(packet.prettyPrint(120, "  ").contains("dLen=1"));
    }

    /** Write the entire contents of this packet to a single byte array */
    public static byte[] getBytes(Packet packet) {
        try (ByteArrayOutputStream outBytes = new ByteArrayOutputStream()) {
            packet.write(new DataOutputStream(outBytes));
            return outBytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Packet could not be written", e);
        }
    }
}
