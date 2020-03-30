package com.hp.jipp.model;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.MutableAttributeGroup;
import com.hp.jipp.encoding.Name;
import com.hp.jipp.encoding.Tag;
import java.util.Collections;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.AttributeGroup.mutableGroupOf;
import static com.hp.jipp.encoding.Tag.operationAttributes;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings("deprecation")
public class DeprecatedTest {
    IppPacket packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
            groupOf(Tag.printerAttributes, Types.operationsSupported.of(Operation.createJob)));
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void parseFromInputStream() throws IOException {
        packet.write(out);
        IppPacket readPacket = IppPacket.parse(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(packet, readPacket);
    }

    @Test
    public void parseFromIppInputStream() throws IOException {
        packet.write(out);
        IppPacket readPacket = IppPacket.parse(new IppInputStream(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(packet, readPacket);
        readPacket = IppPacket.read(new IppInputStream(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(packet, readPacket);
    }

    @Test
    public void readFromInputStream() throws IOException {
        packet.write(out);
        IppPacket readPacket = IppPacket.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(packet.getAttributeGroups().get(0).get(0).getValue(), readPacket.getAttributeGroups().get(0).get(0).getValue());
        assertEquals(packet.getAttributeGroups().get(0).get(0), readPacket.getAttributeGroups().get(0).get(0));
        assertEquals(packet.getAttributeGroups().get(0), readPacket.getAttributeGroups().get(0));
        assertEquals(packet.getAttributeGroups(), readPacket.getAttributeGroups());
        assertEquals(packet, readPacket);
    }

    @Test
    public void mutableGroupAccessors() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes);
        Attribute<Name> printerName = Types.printerName.of("myprinter");
        mutableGroup.add(Types.printerName.of(new Name("myprinter")));
        assertEquals(0, mutableGroup.lastIndexOf(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName));

        mutableGroup.addAll(1, Collections.singletonList(Types.documentName.of("mydocument")));
        assertEquals("mydocument", mutableGroup.getValue(Types.documentName).asString());

        mutableGroup.add(1, Types.documentName.of("mydocument2"));
        assertEquals("mydocument2", mutableGroup.getValue(Types.documentName).asString());

        mutableGroup.add(Types.printerDnsSdName.of("myprinter"), Types.printerFaxModemName.of("mymodem"));
        assertEquals("myprinter", mutableGroup.getValue(Types.printerDnsSdName).asString());
        assertEquals("mymodem", mutableGroup.getValue(Types.printerFaxModemName).asString());
    }
}
