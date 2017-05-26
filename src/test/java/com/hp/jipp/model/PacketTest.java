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
import java.util.List;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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
    private Packet defaultPacket = Packet.builder().setVersionNumber(0x102)
            .setCode(Operation.HoldJob).setRequestId(0x50607).build();
    private Packet.Builder defaultBuilder = Packet.builder(defaultPacket);


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
        packet = Packet.builder(defaultPacket)
                .setData(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD})
                .build();
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
        packet = cycle(Packet.builder(defaultPacket).setData(data).build());
        assertArrayEquals(data, packet.getData());
    }


    @Test
    public void writeSingleEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> emptyGroup = new ArrayList<>();
        emptyGroup.add(AttributeGroup.Companion.of(Tag.OperationAttributes));
        packet = Packet.builder(defaultPacket).setAttributeGroups(emptyGroup).build();
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
        List<AttributeGroup> emptyGroup = new ArrayList<>();
        emptyGroup.add(AttributeGroup.Companion.of(Tag.OperationAttributes));
        packet = cycle(Packet.builder(defaultPacket).setAttributeGroups(emptyGroup).build());
        assertEquals(1, packet.getAttributeGroups().size());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getTag());
    }

    @Test
    public void readMultiEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> groups = new ArrayList<>();
        groups.add(AttributeGroup.Companion.of(Tag.OperationAttributes));
        groups.add(AttributeGroup.Companion.of(Tag.JobAttributes));
        groups.add(AttributeGroup.Companion.of(Tag.get((byte)0x08))); // reserved but legal
        Packet.Builder builder = defaultBuilder.setAttributeGroups(groups);

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
        group.add(AttributeGroup.Companion.of(Tag.OperationAttributes, simpleAttribute));
        packet = defaultBuilder.setAttributeGroups(group).build();
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
        group.add(AttributeGroup.Companion.of(Tag.OperationAttributes, stringAttribute));
        packet = cycle(defaultBuilder.setAttributeGroups(group));
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
        group.add(AttributeGroup.Companion.of(Tag.OperationAttributes, multiValueAttribute));
        packet = defaultBuilder.setAttributeGroups(group).build();
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
        packet = Packet.of(Operation.GetJobAttributes, 0x1010,
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("US-ASCII", "UTF-8")));
        packet = cycle(packet);
        System.out.println(packet); // Exercise debug output
        assertEquals(Operation.GetJobAttributes, packet.getOperation());
        assertEquals(0x1010, packet.getRequestId());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getTag());
        Attribute<String> attribute = packet.getAttributeGroups().get(0).get(Attributes.AttributesCharset);
        assertEquals(ImmutableList.of("US-ASCII", "UTF-8"), attribute.getValues());
    }

    @Test
    public void getValue() throws IOException {
        packet = cycle(Packet.of(Operation.GetJobAttributes, 0x1010,
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("US-ASCII", "UTF-8"))));
        // Wrong group
        assertNull(packet.getValue(Tag.JobAttributes, Attributes.AttributesNaturalLanguage));

        // Wrong attr
        assertNull(packet.getValue(Tag.OperationAttributes, Attributes.AttributesNaturalLanguage));

        // All good!
        assertEquals("US-ASCII", packet.getValue(Tag.OperationAttributes, Attributes.AttributesCharset));
    }

    @Test
    public void getValues() throws IOException {
        packet = cycle(Packet.of(Operation.GetJobAttributes, 0x1010,
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("US-ASCII", "UTF-8"))));

        // Wrong group
        assertEquals(ImmutableList.of(),
                packet.getValues(Tag.JobAttributes, Attributes.AttributesCharset));

        // Wrong attr
        assertEquals(ImmutableList.of("US-ASCII", "UTF-8"),
                packet.getValues(Tag.OperationAttributes, Attributes.AttributesCharset));

        // All good!
        assertEquals(ImmutableList.of(),
                packet.getValues(Tag.OperationAttributes, Attributes.AttributesNaturalLanguage));
    }

    @Test
    public void badStreamThrows() throws IOException {
        exception.expect(IOException.class);
        packet = Packet.builder(Operation.SendDocument, 0x01010).setInputStreamFactory(new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                throw new IOException("oops!");
            }
        }).build();

        packet.write(new DataOutputStream(new ByteArrayOutputStream()));
    }

    @Test
    public void badStreamThrowsBytes() throws IOException {
        exception.expect(IllegalArgumentException.class);
        packet = Packet.builder(Operation.SendDocument, 0x01010).setInputStreamFactory(new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                throw new IOException("oops!");
            }
        }).build();

        // Try to get all bytes for the packet
        getBytes(packet);
    }

    @Test
    public void printCorrectly() throws IOException {
        Packet packet = cycle(Packet.of(Status.Ok, 0x101, AttributeGroup.Companion.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob))));
        System.out.println(packet);
        assertTrue(packet.toString().contains(Operation.CreateJob.getName()));
    }

    @Test
    public void showStream() throws Exception {
        Packet packet = Packet.builder(Status.Ok, 0x101).setAttributeGroups(AttributeGroup.Companion.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob)))
                .setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return null;
                    }
                }).build();
        assertTrue(packet.toString().contains("stream"));
        assertTrue(packet.toString().contains("stream"));
        assertTrue(packet.prettyPrint(120, "  ").contains("stream"));
    }

    @Test
    public void showData() throws Exception {
        Packet packet = Packet.builder(Status.Ok, 0x101).setAttributeGroups(AttributeGroup.Companion.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CreateJob)))
                .setData(new byte[] { 0 })
                .build();
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
