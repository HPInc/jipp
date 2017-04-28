package com.hp.jipp.client;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Attributes;

public class PrintJobTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    URI printerUri = new URI("ipp://sample.com");
    URI jobUri = new URI("ipp://sample.com/jobs/0001");
    IppPrinter printer = IppPrinter.of(ImmutableList.of(printerUri));
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

    public PrintJobTest() throws URISyntaxException {
    }

    @Test
    public void missingJobAttributes() throws Exception {
        // Missing JobAttributes group
        Packet packet = Packet.create(Status.Ok, 0x05);
        exception.expect(IOException.class);
        PrintJob.of(jobRequest, packet);
    }

    @Test
    public void missingJobUrl() throws Exception {
        // Missing JobAttributes group
        Packet packet = Packet.create(Status.Ok, 0x05,
                AttributeGroup.create(Tag.JobAttributes));
        exception.expect(IOException.class);
        PrintJob.of(jobRequest, packet);
    }

    @Test
    public void createFromRequest() throws Exception {
        // Missing JobAttributes group
        Packet packet = Packet.create(Status.Ok, 0x05,
                AttributeGroup.create(Tag.JobAttributes, Attributes.JobUri.of(jobUri)));
        PrintJob printJob = PrintJob.of(jobRequest, packet);
        assertEquals(printJob.getUri(), jobUri);
    }

    @Test
    public void createFromPrinter() throws Exception {
        AttributeGroup group = AttributeGroup.create(Tag.JobAttributes, Attributes.JobUri.of(jobUri));
        PrintJob printJob = PrintJob.of(printer, group);

        assertEquals(printJob.getUri(), jobUri);
        assertEquals(null, printJob.getJobRequest());
    }

    @Test
    public void updateWithAttributes() throws Exception {
        Packet packet = Packet.create(Status.Ok, 0x05,
                AttributeGroup.create(Tag.JobAttributes, Attributes.JobUri.of(jobUri)));

        Packet newPacket = Packet.create(Status.Ok, 0x05,
                AttributeGroup.create(Tag.JobAttributes,
                        Attributes.JobUri.of(jobUri),
                        Attributes.JobName.of("green")));

        PrintJob printJob = PrintJob.of(jobRequest, packet).withResponse(newPacket);
        assertEquals("green", printJob.getAttributes().getValue(Attributes.JobName).get());
    }

}
