package com.hp.jipp.model;

import com.hp.jipp.encoding.*;
import com.hp.jipp.model.Operation;
import static com.hp.jipp.model.Types.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class IppPacketTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private IppPacket packet;

    @Test
    public void writeEmptyPacket() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607);

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
        }, toBytes(packet));
    }

    @Test
    public void readEmptyPacket() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607);
        packet = cycle(packet);
        assertEquals(packet.getVersionNumber(), packet.getVersionNumber());
        assertEquals(packet.getCode(), packet.getCode());
        assertEquals(packet.getRequestId(), packet.getRequestId());
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
        IppPacket.read(new ByteArrayInputStream(in));
    }

    @Test
    public void writeSingleEmptyAttributeGroupPacket() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes));
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
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes));
        packet = cycle(packet);
        assertEquals(1, packet.getAttributeGroups().size());
        assertEquals(Tag.operationAttributes, packet.getAttributeGroups().get(0).getTag());
    }

    @Test
    public void readMultiEmptyAttributeGroupPacket() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes),
                groupOf(Tag.jobAttributes),
                groupOf(Tag.fromInt((byte)0x08))); // reserved but legal
        packet = cycle(packet);
        assertEquals(3, packet.getAttributeGroups().size());
        assertEquals(Tag.operationAttributes, packet.getAttributeGroups().get(0).getTag());
        assertEquals(Tag.jobAttributes, packet.getAttributeGroups().get(1).getTag());
        assertEquals(Tag.fromInt(0x08), packet.getAttributeGroups().get(2).getTag());
    }

    @Test
    public void writeSingleAttributePacket() throws IOException {
        Attribute<String> simpleAttribute = new StringType(Tag.charset, "attributes-charset")
                .of("us-ascii");

        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes, simpleAttribute));

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
                'u', 's', '-', 'a', 's', 'c', 'i', 'i',
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
                'u', 's', '-', 'a', 's', 'c', 'i', 'i',
                (byte) 0x03,
        };

        IppPacket.read(new ByteArrayInputStream(bytes));
    }


    @Test
    public void readSingleAttributePacket() throws IOException {
        StringType attributesCharsetType = new StringType(Tag.charset, "attributes-charset");
        Attribute<String> attributesCharset = attributesCharsetType.of("us-ascii");
        IppPacket original = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes, attributesCharset));
        packet = cycle(original);

        Attribute readAttribute = packet.getAttributeGroups().get(0).get(0);
        assertEquals("attributes-charset", readAttribute.getName());
        assertEquals(new OtherString(Tag.charset, "us-ascii"), readAttribute.get(0));
        assertEquals("us-ascii", packet.getValue(Tag.operationAttributes, attributesCharsetType));
    }

    @Test
    public void writeMultiValueAttributePacket() throws IOException {
        Attribute<String> multiValueAttribute = new StringType(Tag.charset, "attributes-charset")
                .of("us-ascii", "utf-8");

        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes, multiValueAttribute));

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
                (byte) 0x47, // charset tag
                (byte) 0x00,
                (byte) 0x12,
                'a', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '-',
                'c', 'h', 'a', 'r', 's', 'e', 't',
                (byte) 0x00,
                (byte) 0x08,
                'u', 's', '-', 'a', 's', 'c', 'i', 'i',
                (byte) 0x47,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x05,
                'u', 't', 'f', '-', '8',
                (byte) 0x03,
        }, toBytes(packet));
        System.out.println(packet.toString());
    }

    @Test
    public void readMultiValueAttributePacket() throws IOException {
        packet = new IppPacket(0x0102, Operation.getJobAttributes.getCode(), 777,
                groupOf(Tag.operationAttributes, attributesCharset.of("us-ascii", "utf-8")));
        packet = cycle(packet);

        assertEquals(Operation.getJobAttributes, packet.getOperation());
        assertEquals(777, packet.getRequestId());
        assertEquals(Tag.operationAttributes, packet.getAttributeGroups().get(0).getTag());
        Attribute<String> attribute = packet.getAttributeGroups().get(0).get(attributesCharset);
        assertEquals(Arrays.asList("us-ascii", "utf-8"), attribute.strings());
    }

    @Test
    public void withAttributeGroups() throws IOException {
        IppPacket copyFrom = new IppPacket(0x0102, Operation.getJobAttributes.getCode(), 777);
        packet = new IppPacket(0x0102, Operation.getJobAttributes.getCode(), 777, groupOf(Tag.operationAttributes),
                groupOf(Tag.jobAttributes));
        List<AttributeGroup> groups = Arrays.asList(
                groupOf(Tag.operationAttributes),
                groupOf(Tag.jobAttributes));
        assertNotEquals(copyFrom, packet);
        assertEquals(copyFrom.withAttributeGroups(groups), packet);
    }

    @Test
    public void getValue() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 777,
                groupOf(Tag.operationAttributes, attributesCharset.of("us-ascii", "utf-8")));
        packet = cycle(packet);

        // Wrong group
        assertNull(packet.getValue(Tag.jobAttributes, attributesNaturalLanguage));

        // Wrong attr
        assertNull(packet.getValue(Tag.operationAttributes, attributesNaturalLanguage));

        // All good!
        assertEquals("us-ascii", packet.getValue(Tag.operationAttributes, attributesCharset));
    }

    @Test
    public void getValues() throws IOException {
        packet = new IppPacket(0x0102, Operation.getJobAttributes.getCode(), 777,
                groupOf(Tag.operationAttributes, attributesCharset.of("us-ascii", "utf-8")));
        packet = cycle(packet);

        // Wrong group
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.jobAttributes, attributesCharset));

        // Wrong attr
        assertEquals(Arrays.asList("us-ascii", "utf-8"),
                packet.getStrings(Tag.operationAttributes, attributesCharset));

        // All good!
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.operationAttributes, attributesNaturalLanguage));
    }

    @Test
    public void printCorrectly() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.printerAttributes, operationsSupported.of(Operation.createJob)));
        packet = cycle(packet);
        System.out.println(packet.toString());
        assertTrue(packet.toString().contains(Operation.createJob.getName()));
    }
}
