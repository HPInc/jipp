package com.hp.jipp.client;


import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Attributes;

public class IppClientTest {
    URI printerUri = new URI("ipp://sample.com");
    URI jobUri = new URI("ipp://sample.com/jobs/0001");
    IppPrinter printer = IppPrinter.of(printerUri);
    BaseDocument document = new BaseDocument() {
        @Override
        public String getDocumentType() {
            return "application/txt";
        }

        @Override
        public InputStream openDocument() throws IOException {
            return null;
        }
    };
    JobRequest jobRequest = JobRequest.of(printer, document);
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
        assertEquals("printername", printer.getAttributes().getValue(Attributes.PrinterInfo).get());
    }

}
