package com.hp.jipp.client;

import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.*;

public class PrinterTest {

    UUID uuid = UUID.randomUUID();
    URI printerUri = new URI("ipp://sample.com");

    Printer printer = new Printer(uuid, printerUri, AttributeGroup.Companion.of(Tag.PrinterAttributes,
            Attributes.PrinterInfo.of("printername")));

    @Test
    public void getInfo() {
        assertEquals("printername", printer.getInfo());
    }

    @Test
    public void getBlankInfo() {
        printer = new Printer(uuid, printerUri, AttributeGroup.Companion.of(Tag.PrinterAttributes));
        assertEquals("", printer.getInfo());
        assertTrue(!printer.toString().contains("info")); // Skip name if not present
    }

    public PrinterTest() throws URISyntaxException {
    }
}
