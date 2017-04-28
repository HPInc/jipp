package com.hp.jipp.client;


import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Attributes;

public class IppClientTest {
    URI printerUri = new URI("ipp://sample.com");
    IppPrinter printer = IppPrinter.of(ImmutableList.of(printerUri));
    FakeTransport transport = new FakeTransport();

    IppClient client = new IppClient(transport);

    URI sendUri;
    Packet sendPacket;
    Packet responsePacket;

    class FakeTransport implements IppClient.Transport {
        @Override
        public Packet send(URI uri, Packet packet) throws IOException {
            sendUri = uri;
            sendPacket = packet;
            return responsePacket;
        }
    }


    public IppClientTest() throws URISyntaxException {
    }


    @Test
    public void getPrinterAttributes() throws IOException {
        responsePacket = Packet.create(Status.Ok, 0x01, AttributeGroup.create(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername")));
        printer = client.getPrinterAttributes(printer);
        assertEquals(Operation.GetPrinterAttributes, sendPacket.getCode(Operation.ENCODER));
        assertEquals(printer.getUris(), ImmutableList.of(sendUri));
        assertEquals("printername", printer.getAttributes().getValue(Attributes.PrinterInfo).get());
    }

}
