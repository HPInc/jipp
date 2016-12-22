package com.hp.jipp.encoding;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PacketTest {
    private Packet packet;
    private Packet defaultPacket = Packet.builder().setVersionNumber(0x102).setOperation(0x304)
            .setRequestId(0x50607).build();
    private Packet.Builder defaultBuilder = Packet.builder(defaultPacket);
    private ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private DataOutputStream out = new DataOutputStream(outBytes);
    @Test
    public void writeEmptyPacket() throws IOException {
        defaultPacket.write(out);
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x03,
        }, outBytes.toByteArray());
    }

    @Test
    public void readEmptyPacket() throws IOException {
        packet = cycle(defaultPacket);
        assertEquals(defaultPacket.getVersionNumber(), packet.getVersionNumber());
        assertEquals(defaultPacket.getOperation(), packet.getOperation());
        assertEquals(defaultPacket.getRequestId(), packet.getRequestId());
    }

    @Test
    public void writeDataPacket() throws IOException {
        packet = Packet.builder(defaultPacket)
                .setData(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD})
                .build();
        packet.write(out);
        assertArrayEquals(new byte[]{
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x03,
                (byte) 0xFF,
                (byte) 0xFE,
                (byte) 0xFD,
        }, outBytes.toByteArray());
    }

    @Test
    public void readDataPacket() throws IOException {
        byte data[] = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD };
        packet = cycle(Packet.builder(defaultPacket).setData(data).build());
        assertArrayEquals(data, packet.getData());
    }

    @Test
    public void writeEmptyAttributeGroupPacket() throws IOException {
        defaultPacket.write(out);
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x03,
        }, outBytes.toByteArray());
    }

    @Test
    public void writeSingleEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> emptyGroup = new ArrayList<>();
        emptyGroup.add(new AttributeGroup.Builder(Tag.OperationAttributes).build());
        packet = Packet.builder(defaultPacket).setAttributeGroups(emptyGroup).build();
        packet.write(out);
        assertArrayEquals(new byte[]{
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x01,
                (byte) 0x03,
        }, outBytes.toByteArray());
    }

    @Test
    public void readSingleEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> emptyGroup = new ArrayList<>();
        emptyGroup.add(new AttributeGroup.Builder(Tag.OperationAttributes).build());
        packet = cycle(Packet.builder(defaultPacket).setAttributeGroups(emptyGroup).build());
        assertEquals(1, packet.getAttributeGroups().size());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getStartTag());
    }

    @Test
    public void readMultiEmptyAttributeGroupPacket() throws IOException {
        List<AttributeGroup> groups = new ArrayList<>();
        groups.add(new AttributeGroup.Builder(Tag.OperationAttributes).build());
        groups.add(new AttributeGroup.Builder(Tag.JobAttributes).build());
        groups.add(new AttributeGroup.Builder(Tag.toTag((byte)0x08)).build()); // reserved but legal
        Packet.Builder builder = defaultBuilder.setAttributeGroups(groups);

        packet = cycle(builder.build());
        assertEquals(3, packet.getAttributeGroups().size());
        assertEquals(Tag.OperationAttributes, packet.getAttributeGroups().get(0).getStartTag());
        assertEquals(Tag.JobAttributes, packet.getAttributeGroups().get(1).getStartTag());
        assertEquals(Tag.toTag((byte)0x08), packet.getAttributeGroups().get(2).getStartTag());
    }

    @Test
    public void writeSingleAttributePacket() throws IOException {
        OctetAttribute simpleAttribute = new OctetAttribute(
                Tag.Charset,
                "attributes-charset",
                "US-ASCII".getBytes());
        List<AttributeGroup> group = new ArrayList<>();
        group.add(new AttributeGroup.Builder(Tag.OperationAttributes).addAttribute(simpleAttribute)
                .build());
        packet = defaultBuilder.setAttributeGroups(group).build();
        packet.write(out);
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
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
        }, outBytes.toByteArray());
    }

    @Test
    public void readSingleAttributePacket() throws IOException {
        StringAttribute stringAttribute = new StringAttribute(
                Tag.Charset,
                "attributes-charset",
                "US-ASCII");
        List<AttributeGroup> group = new ArrayList<>();
        group.add(new AttributeGroup.Builder(Tag.OperationAttributes)
                .addAttribute(stringAttribute)
                .build());
        packet = cycle(defaultBuilder.setAttributeGroups(group));
        Attribute readAttribute = packet.getAttributeGroups().get(0).getAttributes().get(0);
        assertEquals("attributes-charset", readAttribute.getName());
        assertEquals(Tag.Charset, readAttribute.getValueTag());
        assertEquals("US-ASCII", ((StringAttribute)readAttribute).getValues().get(0));
    }

    @Test
    public void writeMultiValueAttributePacket() throws IOException {
        List<String> values = new ArrayList<>();
        values.add("US-ASCII");
        values.add("UTF-8");
        StringAttribute multiValueAttribute = new StringAttribute(
                Tag.Charset,
                "attributes-charset",
                values);

        List<AttributeGroup> group = new ArrayList<>();
        group.add(new AttributeGroup.Builder(Tag.OperationAttributes).addAttribute(multiValueAttribute)
                .build());
        packet = defaultBuilder.setAttributeGroups(group).build();
        packet.write(out);
        assertArrayEquals(new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
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
        }, outBytes.toByteArray());
        System.out.println(packet.toString());
    }

    @Test
    public void readMultiValueAttributePacket() throws IOException {
        List<String> values = new ArrayList<>();
        values.add("US-ASCII");
        values.add("UTF-8");
        StringAttribute multiValueAttribute = new StringAttribute(
                Tag.Charset,
                "attributes-charset",
                values);

        List<AttributeGroup> group = new ArrayList<>();
        group.add(new AttributeGroup.Builder(Tag.OperationAttributes).addAttribute(multiValueAttribute)
                .build());
        packet = cycle(defaultBuilder.setAttributeGroups(group));
        System.out.println(packet); // Exercise debug output
        StringAttribute attribute = (StringAttribute)packet.getAttributeGroups().get(0).getAttributes().get(0);
        assertEquals("US-ASCII", attribute.getValues().get(0));
        assertEquals("UTF-8", attribute.getValues().get(1));
    }

    // Write it, read it, return it.
    private Packet cycle(Packet in) throws IOException {
        in.write(out);
        return Packet.read(new DataInputStream(new ByteArrayInputStream(outBytes.toByteArray())));
    }

    private Packet cycle(Packet.Builder in) throws IOException {
        return cycle(in.build());
    }
}