package com.hp.jipp.encoding;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.*;

import com.hp.jipp.model.Attributes;


public class AttributeGroupTest {

    @Test
    public void multiAttribute() throws Exception {
        AttributeGroup group = AttributeGroup.create(Tag.OperationAttributes,
                Attributes.AttributesCharset.create("utf-8"),
                Attributes.AttributesNaturalLanguage.create("en"),
                Attributes.PrinterUri.create(URI.create("ipp://10.0.0.23/ipp/printer")));
        group = cycle(group);

        assertEquals(group.getStartTag(), Tag.OperationAttributes);
        assertTrue(group.get("attributes-charset").isPresent());
        assertTrue(group.get("attributes-natural-language").isPresent());
        assertTrue(group.get("printer-uri").isPresent());
    }

    private AttributeGroup cycle(AttributeGroup group) throws IOException {
        return AttributeGroup.read(new DataInputStream(new ByteArrayInputStream(toBytes(group))));
    }

    private byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        group.write(out);
        Tag.EndOfAttributes.write(out);
        return bytesOut.toByteArray();
    }
}
