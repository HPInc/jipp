package com.hp.jipp.client;


import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    BaseDocument document = new BaseDocument() {
        @Override
        public String getDocumentType() {
            return "application/whatever";
        }

        @Override
        public InputStream openDocument() throws IOException {
            byte[] bytes = new byte[] {
                    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
            };
            return new ByteArrayInputStream(bytes);
        }
    };

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
        responsePacket = Packet.of(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername")));
        printer = client.getPrinterAttributes(printer);
        assertEquals(Operation.GetPrinterAttributes, sendPacket.getCode(Operation.ENCODER));
        assertEquals(printer.getUris(), ImmutableList.of(sendUri));
        assertEquals("printername", printer.getAttributes().getValue(Attributes.PrinterInfo).get());
    }

    @Test
    public void createJob() throws IOException {
        responsePacket = Packet.of(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111)));
        JobRequest jobRequest = JobRequest.of(printer, document);
        PrintJob job = client.createJob(jobRequest);
        assertEquals(111, job.getId());
        job.getJobRequest().get().getDocument();
    }
}
