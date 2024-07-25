// Copyright 2016 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.DelimiterTag;
import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Name;
import com.hp.jipp.encoding.OtherString;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.encoding.Cycler.toBytes;
import static com.hp.jipp.model.Types.attributesCharset;
import static com.hp.jipp.model.Types.attributesNaturalLanguage;
import static com.hp.jipp.model.Types.operationsSupported;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IppPacketTest {
    private URI uri = URI.create("ipp://192.168.0.101:631/ipp/print");

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
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x06
                // Missing bytes
//                (byte) 0x07
//                (byte) 0x01,
//                (byte) 0x03,
        };
        exception.expect(EOFException.class);
        new IppInputStream(new ByteArrayInputStream(in)).readPacket();
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
        // reserved but legal
        DelimiterTag otherAttributes = new DelimiterTag((byte)0x08, "other-attributes");
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes),
                groupOf(Tag.jobAttributes),
                groupOf(otherAttributes));
        packet = cycle(packet);
        assertEquals(3, packet.getAttributeGroups().size());
        assertEquals(Tag.operationAttributes, packet.getAttributeGroups().get(0).getTag());
        assertEquals(Tag.jobAttributes, packet.getAttributeGroups().get(1).getTag());
        assertEquals(otherAttributes, packet.getAttributeGroups().get(2).getTag());
    }

    @Test
    public void writeSingleAttributePacket() throws IOException {
        Attribute<String> simpleAttribute = new StringType(Tag.charset, "attributes-charset").of("us-ascii");

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

        new IppInputStream(new ByteArrayInputStream(bytes)).readPacket();
    }


    @Test
    public void readSingleAttributePacket() throws IOException {
        StringType attributesCharsetType = new StringType(Tag.charset, "attributes-charset");
        Attribute<String> attributesCharset = attributesCharsetType.of("us-ascii");
        IppPacket original = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.operationAttributes, attributesCharset));
        packet = cycle(original);

        Attribute<?> readAttribute = packet.getAttributeGroups().get(0).get(0);
        assertEquals("attributes-charset", readAttribute.getName());
        assertEquals(new OtherString(Tag.charset, "us-ascii"), readAttribute.get(0));
        assertEquals("us-ascii", packet.getValue(Tag.operationAttributes, attributesCharsetType));
    }

    @Test
    public void writeMultiValueAttributePacket() throws IOException {
        Attribute<String> multiValueAttribute = new StringType.Set(Tag.charset, "attributes-charset")
                .of(Arrays.asList("us-ascii", "utf-8")); // Illegal

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
                groupOf(Tag.operationAttributes, attributesCharset.of("us-ascii")));
        packet = cycle(packet);

        assertEquals(Operation.getJobAttributes, packet.getOperation());
        assertEquals(777, packet.getRequestId());
        assertEquals(Tag.operationAttributes, packet.getAttributeGroups().get(0).getTag());
        Attribute<String> attribute = packet.getAttributeGroups().get(0).get(Types.attributesCharset);
        assertEquals(Collections.singletonList("us-ascii"), attribute.strings());
    }

    @Test
    public void withAttributeGroups() {
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
                groupOf(Tag.operationAttributes, attributesCharset.of("us-ascii")));
        packet = cycle(packet);

        // Wrong group
        assertNull(packet.getValue(Tag.jobAttributes, attributesNaturalLanguage));

        // Wrong attr
        assertNull(packet.getValue(Tag.operationAttributes, attributesNaturalLanguage));

        // All good!
        assertEquals("us-ascii", packet.getValue(Tag.operationAttributes, Types.attributesCharset));
    }

    @Test
    public void getValues() throws IOException {
        packet = new IppPacket(0x0102, Operation.getJobAttributes.getCode(), 777,
                groupOf(Tag.operationAttributes, Types.compressionSupported.of(Compression.gzip)));
        packet = cycle(packet);

        // Wrong group
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.jobAttributes, Types.compressionSupported));

        // Wrong attr
        assertEquals(Collections.emptyList(),
                packet.getValues(Tag.operationAttributes, Types.finishings));

        // All good
        assertEquals(Collections.singletonList(Compression.gzip),
                packet.getStrings(Tag.operationAttributes, Types.compressionSupported));
    }

    @Test
    public void printCorrectly() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.printerAttributes, Types.operationsSupported.of(Operation.createJob)));
        packet = cycle(packet);
        System.out.println(packet.toString());
        assertTrue(packet.toString().contains(Operation.createJob.getName()));
    }

    @Test
    public void getStrings() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
                groupOf(Tag.printerAttributes, Types.operationsSupported.of(Operation.createJob),
                        Types.foldingDirectionSupported.noValue()));
        packet = cycle(packet);
        assertEquals("Create-Job(5)", packet.getString(Tag.printerAttributes, operationsSupported));
        assertNull(packet.getString(Tag.unsupportedAttributes, Types.foldingDirectionSupported));

        // Group, Attribute, and Value present
        assertEquals(Collections.singletonList("Create-Job(5)"), packet.getStrings(Tag.printerAttributes, operationsSupported));
        // Attribute present, no values inside
        assertEquals(Collections.emptyList(), packet.getStrings(Tag.printerAttributes, Types.foldingDirectionSupported));
        // Group present, no such attribute
        assertEquals(Collections.emptyList(), packet.getStrings(Tag.printerAttributes, Types.oauthAuthorizationScope));
        // Group not present
        assertEquals(Collections.emptyList(), packet.getStrings(Tag.unsupportedAttributes, Types.foldingDirectionSupported));
    }

    @Test
    public void operationString() throws IOException {
        packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607);
        packet = cycle(packet);
        assertThat(packet.toString(), containsString("Hold-Job"));
    }

    @Test
    public void statusString() throws IOException {
        packet = new IppPacket(0x0102, Status.clientErrorAccountLimitReached.getCode(), 0x50607);
        packet = cycle(packet);
        assertThat(packet.toString(), containsString("client-error-account-limit-reached"));
    }

    @Test
    public void unknownString() throws IOException {
        packet = new IppPacket(0x0102, 999, 0x50607);
        packet = cycle(packet);
        assertThat(packet.toString(), containsString("999"));
    }

    @Test
    public void builder() throws IOException {
        packet = new IppPacket.Builder(Status.successfulOk.getCode()).build();
        packet = cycle(packet);
        assertEquals(Status.successfulOk, packet.getStatus());
        assertEquals(IppPacket.DEFAULT_VERSION_NUMBER, packet.getVersionNumber());
    }

    @Test
    public void getPrinterAttributesBuilder() throws IOException {
        packet = IppPacket.getPrinterAttributes(uri, Types.mediaSupported).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(Operation.getPrinterAttributes, packet.getOperation());
        assertEquals(Collections.singletonList("media-supported"), packet.getValues(Tag.operationAttributes, Types.requestedAttributes));
    }

    @Test
    public void validateJobBuilder() throws IOException {
        packet = IppPacket.validateJob(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(Operation.validateJob, packet.getOperation());
    }

    @Test
    public void printJobBuilder() throws IOException {
        packet = IppPacket.printJob(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(Operation.printJob, packet.getOperation());
    }

    @Test
    public void createJobBuilder() throws IOException {
        packet = IppPacket.createJob(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(Operation.createJob, packet.getOperation());
    }

    @Test
    public void getJobsBuilder() throws IOException {
        packet = IppPacket.getJobs(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(Operation.getJobs, packet.getOperation());
    }

    @Test
    public void sendDocumentBuilder() throws IOException {
        packet = IppPacket.sendDocument(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.jobUri));
        assertEquals(Operation.sendDocument, packet.getOperation());
    }

    @Test
    public void sendDocumentBuilder2() throws IOException {
        packet = IppPacket.sendDocument(uri, 3).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(3, packet.getValue(Tag.operationAttributes, Types.jobId).intValue());
        assertEquals(Operation.sendDocument, packet.getOperation());
    }

    @Test
    public void getJobAttributesBuilder() throws IOException {
        packet = IppPacket.getJobAttributes(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.jobUri));
        assertEquals(Operation.getJobAttributes, packet.getOperation());
    }

    @Test
    public void getJobAttributesBuilder2() throws IOException {
        packet = IppPacket.getJobAttributes(uri, 3).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(3, packet.getValue(Tag.operationAttributes, Types.jobId).intValue());
        assertEquals(Operation.getJobAttributes, packet.getOperation());
    }

    @Test
    public void cancelJobBuilder() throws IOException {
        packet = IppPacket.cancelJob(uri).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.jobUri));
        assertEquals(Operation.cancelJob, packet.getOperation());
    }

    @Test
    public void cancelJobBuilder2() throws IOException {
        packet = IppPacket.cancelJob(uri, 3).build();
        packet = cycle(packet);
        assertEquals(uri, packet.getValue(Tag.operationAttributes, Types.printerUri));
        assertEquals(3, packet.getValue(Tag.operationAttributes, Types.jobId).intValue());
        assertEquals(Operation.cancelJob, packet.getOperation());
    }

    @Test
    public void packetEquality() throws IOException {
        packet = IppPacket.cancelJob(uri).putOperationAttributes(Types.requestedAttributes.unknown()).build();
        IppPacket packet2 = cycle(packet);
        assertEquals(packet, packet2);

        IppPacket packet3 = IppPacket.cancelJob(uri).putOperationAttributes(Types.requestedAttributes.noValue()).build();
        assertNotEquals(packet, packet3);
    }

   @Test
   public void attributeGroupBuilders() throws IOException {
        IppPacket.Builder builder = IppPacket.createJob(uri);

        // Looks nasty but lets us use the kotlin endpoints.
        builder.getOperationAttributes().invoke(attributes -> {
           attributes.put(Types.requestingUserName.of("tester"));
           return null;
        });
        builder.getJobAttributes().invoke(attributes -> {
           attributes.put(Types.copies.of(12));
           return null;
        });
        builder.getPrinterAttributes().invoke(attributes -> {
           attributes.put(Types.printerName.of("Test Printer"));
           return null;
        });
        builder.getUnsupportedAttributes().invoke(attributes -> {
           attributes.put(Types.finishingsCol.unknown());
           return null;
        });

        packet = cycle(builder.build());
        assertEquals("tester", packet.getString(Tag.operationAttributes, Types.requestingUserName));
        assertEquals(Integer.valueOf(12), packet.getValue(Tag.jobAttributes, Types.copies));
        assertEquals(new Name("Test Printer"), packet.getValue(Tag.printerAttributes, Types.printerName));
        assertEquals(0, packet.getValues(Tag.unsupportedAttributes, Types.finishingsCol).size());
   }
}
