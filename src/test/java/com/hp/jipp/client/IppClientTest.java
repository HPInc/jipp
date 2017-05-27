package com.hp.jipp.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.IdentifyAction;
import com.hp.jipp.model.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.PrinterState;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Attributes;

public class IppClientTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    UUID uuid = UUID.randomUUID();
    URI printerUri = new URI("ipp://sample.com");
    URI printerUri2 = new URI("ipps://sample.com:443");

    Printer printer = new Printer(uuid, printerUri, AttributeGroup.of(Tag.PrinterAttributes,
            Attributes.PrinterInfo.of("printername")));
    FakeTransport transport = new FakeTransport();
    Document document = new Document() {
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

        @Override
        public String getName() {
            return "document";
        }
    };
    JobRequest jobRequest = new JobRequest(printer, "job", document);
    Job job;

    IppClient client = new IppClient(transport);

    URI sendUri;
    Packet request;
    Packet response;

    class FakeTransport implements IppClient.Transport {
        @Override
        public Packet send(URI uri, Packet packet) throws IOException {
            sendUri = uri;
            request = packet;

            // Deliver the data to nowhere
            DataOutputStream out = new DataOutputStream(new ByteArrayOutputStream());
            packet.write(out);

            List<Attribute<?>> attributes = request.getAttributeGroup(Tag.OperationAttributes).getAttributes();

            // First 2 Operation Attributes must ALWAYS be charset/natlang
            assertEquals(Attributes.AttributesCharset.of("utf-8"), attributes.get(0));
            assertEquals(Attributes.AttributesNaturalLanguage.of("en"), attributes.get(1));

            return response;
        }
    }

    public IppClientTest() throws URISyntaxException {
    }

    @Test
    public void getPrinterAttributesRequest() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();
        printer = client.getPrinterAttributes(uuid, printerUri);
        System.out.println(printer);

        List<Attribute<?>> attributes = request.getAttributeGroup(Tag.OperationAttributes).getAttributes();

        // First 3 attributes must be charset/natlang/printeruri
        assertEquals(Attributes.AttributesCharset.of("utf-8"), attributes.get(0));
        assertEquals(Attributes.AttributesNaturalLanguage.of("en"), attributes.get(1));
        assertEquals(Attributes.PrinterUri.of(printerUri), attributes.get(2));
    }

    @Test
    public void getPrinterAttributesResult() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();
        printer = client.getPrinterAttributes(uuid, Arrays.asList(printerUri));
        assertEquals(Operation.GetPrinterAttributes, request.getOperation());
        assertEquals(printer.getUri(), sendUri);
        assertEquals("printername", printer.getAttributes().getValue(Attributes.PrinterInfo));
    }

    @Test
    public void getPrinterAttributesBadResult() throws IOException {
        response = new Packet.Builder(Status.ServerErrorBusy, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();

        // Throw because what else
        exception.expect(IOException.class);
        exception.expectMessage("No printer attributes in response");
        printer = client.getPrinterAttributes(uuid, printerUri);
    }

    @Test
    public void getPrinterAttributesNoneGood() throws IOException {
        response = new Packet.Builder(Status.ServerErrorBusy, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();

        List<URI> uris = Arrays.asList(printerUri, printerUri2);
        // Throw because what else
        exception.expect(IOException.class);
        exception.expectMessage("Fail after trying uris " + uris);
        printer = client.getPrinterAttributes(uuid, uris);
    }

    @Test
    public void getPrinterAttributesEmpty() throws IOException {
        // Throw because what else
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No printer URIs present");
        printer = client.getPrinterAttributes(uuid, Collections.<URI>emptyList());
    }

    @Test
    public void validateJob() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01).build();;
        ValidatedJob validatedJob = client.validateJob(jobRequest);
        assertEquals(Status.Ok, validatedJob.getPacket().getStatus());
    }

    @Test
    public void getJobs() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01,
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("UTF-8"),
                        Attributes.AttributesNaturalLanguage.of("en")),
                AttributeGroup.of(Tag.JobAttributes,
                        Attributes.JobId.of(2), Attributes.JobState.of(JobState.Processing)),
                AttributeGroup.of(Tag.JobAttributes,
                        Attributes.JobId.of(3), Attributes.JobState.of(JobState.PendingHeld)))
                .build();
        List<Job> jobs = client.getJobs(printer);
        assertEquals(Integer.valueOf(2), jobs.get(0).getAttributes().getValue(Attributes.JobId));
        assertEquals(Integer.valueOf(3), jobs.get(1).getAttributes().getValue(Attributes.JobId));
    }


    @Test
    public void getPrinterStatus() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterState.of(PrinterState.Stopped),
                        Attributes.PrinterStateReasons.of("bored", "tired"),
                        Attributes.PrinterStateMessage.of("it's complicated"))).build();
        PrinterStatus status = client.getPrinterStatus(printer);
        System.out.println(status);
        assertEquals("it's complicated", status.getMessage());
        assertEquals(Arrays.asList("bored", "tired"), status.getReasons());
        assertEquals(PrinterState.Stopped, status.getState());
    }

    @Test
    public void identifyPrinter() throws Exception {
        response = new Packet.Builder(Status.Ok, 0x01).build();;
        client.identifyPrinter(printer, IdentifyAction.Sound, "happy-tune");
        assertEquals(IdentifyAction.Sound, request.getValue(Tag.OperationAttributes, Attributes.IdentifyActions));
        assertEquals("happy-tune", request.getValue(Tag.OperationAttributes, Attributes.Message));
    }

    @Test
    public void badPrinterStatus() throws Exception {
        exception.expect(IOException.class);
        response = new Packet.Builder(Status.ServerErrorInternalError, 0x01).build();;
        client.getPrinterStatus(printer);
    }

    @Test
    public void badPrinterStatusNoState() throws Exception {
        exception.expect(IOException.class);
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterStateReasons.of("bored", "tired"),
                Attributes.PrinterStateMessage.of("it's complicated"))).build();
        client.getPrinterStatus(printer);
    }

    @Test
    public void printJob() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01,
                AttributeGroup.of(Tag.JobAttributes,
                        Attributes.JobId.of(101),
                        Attributes.JobState.of(JobState.Processing),
                        Attributes.JobStateReasons.of("none"))).build();

        job = client.printJob(jobRequest);
        assertEquals(101, job.getId());
        assertEquals(JobState.Processing, job.getStatus().getState());
        assertEquals(Arrays.asList("none"), job.getStatus().getReasons());
        assertNull(job.getStatus().getMessage());
    }

    @Test
    public void createJob() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Processing))).build();
        job = client.createJob(jobRequest);
        assertEquals(111, job.getId());
        job.getJobRequest().getDocument();
    }

    @Test
    public void badCreateJobResponse() throws Exception {
        exception.expect(IOException.class);
        response = new Packet.Builder(Status.ServerErrorBusy, 0x01).build();;
        job = client.createJob(jobRequest);
    }

    @Test
    public void badCreateJobResponseAttributes() throws Exception {
        exception.expect(IOException.class);
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes)).build();;
        job = client.createJob(jobRequest);
    }

    @Test
    public void sendDocument() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Pending))).build();
        job = client.createJob(jobRequest);

        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Processing))).build();
        job = client.sendDocument(job);
        assertEquals("document",
                request.getAttributeGroup(Tag.OperationAttributes).getValues(Attributes.DocumentName).get(0));
        assertEquals(JobState.Processing, job.getStatus().getState());
    }

    @Test
    public void getJobStatus() throws IOException {
        // Set up a job
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Pending))).build();
        job = client.createJob(jobRequest);

        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Processing))).build();
        job = client.getJobStatus(job);
        System.out.println(job);
        assertEquals(JobState.Processing, job.getStatus().getState());
    }

    @Test
    public void cancelJob() throws IOException {
        // Set up a job
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobId.of(111), Attributes.JobState.of(JobState.Pending))).build();
        job = client.createJob(jobRequest);

        // Cancel it
        response = new Packet.Builder(Status.Ok, 0x01).build();;
        Packet canceled = client.cancelJob(job);
        assertEquals(Status.Ok, canceled.getStatus());
    }

    @Test
    public void checkUserName() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();
        printer = client.getPrinterAttributes(uuid, printerUri);

        List<Attribute<?>> attributes = request.getAttributeGroup(Tag.OperationAttributes).getAttributes();

        // 4th attribute is username
        assertEquals(Attributes.RequestingUserName.of("anonymous"), attributes.get(3));
    }

    @Test
    public void changeUserName() throws IOException {
        response = new Packet.Builder(Status.Ok, 0x01, AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.PrinterInfo.of("printername"))).build();
        client.setUserName("donald_duck");
        printer = client.getPrinterAttributes(uuid, printerUri);

        List<Attribute<?>> attributes = request.getAttributeGroup(Tag.OperationAttributes).getAttributes();

        // 4th attribute is username
        assertEquals(Attributes.RequestingUserName.of("donald_duck"), attributes.get(3));
    }

}
