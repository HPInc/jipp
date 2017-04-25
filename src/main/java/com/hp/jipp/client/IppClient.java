package com.hp.jipp.client;


import com.google.common.io.ByteStreams;

import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Basic IPP operations
 */
public class IppClient {

    /** Transport used to send packets and collect responses */
    public interface Transport {
        /**
         * Gets the packet, synchronously delivers it to the specified URL, and returns a response.
         */
        Packet send(URI url, Packet packet) throws IOException;
    }

    private final Transport mTransport;

    /** Creates new client instance based on the supplied transport */
    public IppClient(Transport transport) {
        mTransport = transport;
    }

    // TODO: We may need to pass along non-200 responses in the case of validate
    // TODO: Figure out mystery operations operation-id(x13), operation-id(x39), operation-id(x3b), operation-id(x3c) ...
    // TODO: Draw data directly from the input stream instead of bytes
    // TODO: Always allow access to the most recent entire packet response

    /** Fetch current printer attributes into a new copy of the printer */
    public IppPrinter getPrinterAttributes(IppPrinter printer) throws IOException {
        Packet request = Packet.create(Operation.GetPrinterAttributes, 0x01,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printer.getUri())));

        return printer.withResponse(mTransport.send(printer.getUri(), request));
    }

    /** Return a validated job based on information in the job request */
    public ValidatedJob validateJob(JobRequest jobRequest) throws IOException {
        URI uri = jobRequest.getPrinter().getUri();
        Packet request = Packet.create(Operation.ValidateJob, 0x02,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(uri),
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())
                        // Not sending job-name, ipp-attribute-fidelity, document-name, etc.
                ));

        return ValidatedJob.of(jobRequest,  mTransport.send(uri, request));
    }

    /**
     * Fetch a list of all jobs known by the printer.
     * <p>
     * Job records returned here will not contain any PrintJobRequest.
     */
    public List<PrintJob> getJobs(IppPrinter printer) throws IOException {
        Packet request = Packet.create(Operation.GetJobs, 0x03,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printer.getUri())));
        return PrintJob.toJobs(printer, mTransport.send(printer.getUri(), request));
    }

    /** Fetch current attributes into a new copy of the job */
    public PrintJob getJobAttributes(PrintJob job) throws IOException {
        Packet request = Packet.create(Operation.GetJobAttributes, 0x03,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.JobUri.of(job.getUri())));
        return job.withResponse(mTransport.send(job.getUri(), request));
    }

    /** Send a job request, including its document, returning a new print job. */
    public PrintJob printJob(JobRequest jobRequest) throws IOException {
        // Get all document bytes (non-streaming)
        byte[] bytes;
        try (InputStream inStream = jobRequest.getDocument().openDocument()) {
            // Copy from the source file
            bytes = ByteStreams.toByteArray(inStream);
        }

        final Packet request = Packet.builder(Operation.PrintJob, 0x04)
                .setAttributeGroups(AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(jobRequest.getPrinter().getUri()),
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())
                        // Not sending job-name, ipp-attribute-fidelity, document-name, etc.
                )).setData(bytes).build();

        return PrintJob.of(jobRequest, mTransport.send(jobRequest.getPrinter().getUri(), request));
    }

    /**
     * Send a job request not including its document, returning a new print job. Should be followed by
     * sendDocument to deliver document data.
     */
    public PrintJob createJob(JobRequest jobRequest) throws IOException {
        // Create a packet to be sent later
        Packet request = Packet.builder(Operation.CreateJob, 0x05)
                .setAttributeGroups(AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(jobRequest.getPrinter().getUri()))
                ).build();
        return PrintJob.of(jobRequest, mTransport.send(jobRequest.getPrinter().getUri(), request));
    }

    /** Deliver document data for a print job, returning the updated print job. */
    public PrintJob sendDocument(PrintJob job) throws IOException {
        // Get all document bytes (non-streaming)
        byte[] bytes;
        try (InputStream inStream = job.getJobRequest().getDocument().openDocument()) {
            // Copy from the source file
            bytes = ByteStreams.toByteArray(inStream);
        }

        // Create a packet to be sent later
        final Packet request = Packet.builder(Operation.SendDocument, 0x05)
                .setAttributeGroups(AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.JobUri.of(job.getUri()),
                        Attributes.LastDocument.of(true)))
                .setData(bytes).build();
        // Not sending document-name, compression, document-format, etc.
        return job.withResponse(mTransport.send(job.getUri(), request));
    }

    /** Send a print job cancellation request */
    public Packet cancelJob(PrintJob job) throws IOException {
        Packet request = Packet.create(Operation.CancelJob, 0x03,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.JobUri.of(job.getUri())));
        return mTransport.send(job.getUri(), request);
    }
}
