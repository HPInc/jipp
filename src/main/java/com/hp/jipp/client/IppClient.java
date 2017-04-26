package com.hp.jipp.client;


import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import com.hp.jipp.encoding.Attribute;
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
         * Gets the packet, synchronously delivers it to the specified URL, and returns the response
         * or throws if the response is not 200 OK.
         */
        Packet send(URI uri, Packet packet) throws IOException;
    }

    private final Transport mTransport;

    /** Creates new client instance based on the supplied transport */
    public IppClient(Transport transport) {
        mTransport = transport;
    }

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
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())));

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

        Packet response = mTransport.send(printer.getUri(), request);

        ImmutableList.Builder<PrintJob> listBuilder = new ImmutableList.Builder<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                listBuilder.add(PrintJob.of(printer, group));
            }
        }

        return listBuilder.build();
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
        // See https://tools.ietf.org/html/rfc2911#section-3.2.1.1
        // Get all document bytes (non-streaming)
        byte[] bytes;
        try (InputStream inStream = jobRequest.getDocument().openDocument()) {
            // Copy from the source file
            bytes = ByteStreams.toByteArray(inStream);
        }

        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(jobRequest.getPrinter().getUri()),
                Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType()));

        // Add job and document names if request includes a document name
        if (jobRequest.getDocument().getName() != null) {
            attributes.add(Attributes.JobName.of(jobRequest.getDocument().getName()),
                    Attributes.DocumentName.of(jobRequest.getDocument().getName()));
        }

        final Packet request = Packet.builder(Operation.PrintJob, 0x04)
                .setAttributeGroups(AttributeGroup.create(Tag.OperationAttributes,
                        attributes.build()))
                .setData(bytes).build();

        return PrintJob.of(jobRequest, mTransport.send(jobRequest.getPrinter().getUri(), request));
    }

    /**
     * Send a job request not including its document, returning a new print job. Should be followed by
     * sendDocument to deliver document data.
     */
    public PrintJob createJob(JobRequest jobRequest) throws IOException {
        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(jobRequest.getPrinter().getUri()));

        Packet request = Packet.create(Operation.CreateJob, 0x05,
                AttributeGroup.create(Tag.OperationAttributes, attributes.build()));

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
