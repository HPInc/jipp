package com.hp.jipp.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.InputStreamFactory;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic IPP operations
 */
public class IppClient {
    private static final int FIRST_ID = 0x1001;

    private AtomicInteger mId = new AtomicInteger(FIRST_ID);

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

    /**
     * Fetch current printer attributes into a new copy of the printer, or throw. Each available Printer URI is polled
     * until one of them works; results are returned with that URL moved to the front.
     */
    public IppPrinter getPrinterAttributes(IppPrinter printer) throws IOException {
        Optional<IOException> lastThrown = Optional.absent();
        for (URI uri : printer.getUris()) {
            try {
                return getPrinterAttributes(printer, uri);
            } catch (IOException thrown) {
                lastThrown = Optional.of(thrown);
            }
        }
        if (lastThrown.isPresent()) {
            throw new IOException("Fail after trying " + printer.getUris(), lastThrown.get());
        } else {
            throw new IllegalArgumentException("No printer URIs present");
        }
    }

    /**
     * Fetch the printer's current status. Uses the primary URI only.
     */
    public IppPrinterStatus getPrinterStatus(IppPrinter printer) throws IOException {
       ImmutableList.Builder<Attribute<?>> operationAttributes = new ImmutableList.Builder<>();
        URI printerUri = printer.getUris().get(0);
        operationAttributes.add(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.RequestedAttributes.of(
                    Attributes.PrinterState.getName(),
                    Attributes.PrinterStateReasons.getName(),
                    Attributes.PrinterStateMessage.getName()
                ));
        Packet request = Packet.of(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes, operationAttributes.build()));

        Packet response = mTransport.send(printerUri, request);
        Optional<AttributeGroup> printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (response.getCode(Status.ENCODER).equals(Status.Ok) && printerAttributes.isPresent()) {
            return IppPrinterStatus.of(printerAttributes.get());
        } else {
            throw new IOException("No attributes");
        }
    }

    private IppPrinter getPrinterAttributes(IppPrinter printer, URI printerUri) throws IOException {
        Packet request = Packet.of(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri)));
        Packet response = mTransport.send(printerUri, request);
        Optional<AttributeGroup> printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (response.getCode(Status.ENCODER).equals(Status.Ok) && printerAttributes.isPresent()) {
            // Sort the first working URI to the top of the list.
            ImmutableList.Builder<URI> newUris = new ImmutableList.Builder<>();
            newUris.add(printerUri);
            for (URI oldUri : printer.getUris()) {
                if (!oldUri.equals(printerUri)) newUris.add(oldUri);
            }
            return IppPrinter.of(newUris.build(), printerAttributes.get());
        } else {
            throw new IOException("No attributes");
        }
    }

    /** Validated a job based on the contents of a job request. */
    public IppValidatedJob validateJob(IppJobRequest jobRequest) throws IOException {
        URI uri = jobRequest.getPrinter().getUris().get(0);
        Packet request = Packet.of(Operation.ValidateJob, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(uri),
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())));

        Packet response = mTransport.send(uri, request);
        return IppValidatedJob.of(jobRequest, response);
    }

    /**
     * Fetch a list of all jobs known by the printer.
     * <p>
     * Job records returned here will not contain any PrintJobRequest.
     */
    public List<IppJob> getJobs(IppPrinter printer, Attribute<?>... extras) throws IOException {
        return getJobs(printer, Arrays.asList(extras));
    }

    /**
     * Fetch a list of all jobs known by the printer.
     * <p>
     * Job records returned here will not contain any PrintJobRequest.
     *
     * @param extras Additional Operation Attributes to send along with the request
     * @see <a href="https://tools.ietf.org/html/rfc2911#section-3.2.6.1">RFC2911 Section 3.2.6.1</a>
     */
    public List<IppJob> getJobs(IppPrinter printer, List<Attribute<?>> extras) throws IOException {
        URI printerUri = printer.getUris().get(0);
        ImmutableList.Builder<Attribute<?>> attributesBuilder = new ImmutableList.Builder<Attribute<?>>()
                .add(Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri))
                .addAll(extras);

        Packet request = Packet.of(Operation.GetJobs, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes, attributesBuilder.build()));

        Packet response = mTransport.send(printerUri, request);

        ImmutableList.Builder<IppJob> listBuilder = new ImmutableList.Builder<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                Optional<Integer> id = group.getValue(Attributes.JobId);
                if (!id.isPresent()) {
                    throw new IOException("Missing Job-ID in job response from " + printer);
                }
                listBuilder.add(IppJob.of(id.get(), printer, group));
            }
        }

        return listBuilder.build();
    }

    private IppJob jobWithNewAttributes(IppJob job, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) throw new IOException("Missing job attributes");
        return job.withAttributes(group.get());
    }

    /** Return the most current job status for a job */
    public IppJobStatus getJobStatus(IppJob job) throws IOException {
        URI printerUri = job.getPrinter().getUris().get(0);
        Packet request = Packet.of(Operation.GetJobAttributes, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(job.getPrinter().getUris()),
                        Attributes.JobId.of(job.getId()),
                        Attributes.RequestedAttributes.of(IppJobStatus.getAttributeNames())));
        Optional<AttributeGroup> jobAttributes = mTransport.send(printerUri, request)
                .getAttributeGroup(Tag.JobAttributes);
        if (!jobAttributes.isPresent()) throw new IOException("Missing job attributes");
        return IppJobStatus.of(jobAttributes.get());
    }

    /** Send a job request, including its document, returning a new print job. */
    public IppJob printJob(final IppJobRequest jobRequest) throws IOException {
        // See https://tools.ietf.org/html/rfc2911#section-3.2.1.1

        URI printerUri = jobRequest.getPrinter().getUris().get(0);

        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.JobName.of(jobRequest.getName()),
                Attributes.DocumentName.of(jobRequest.getDocument().getName()),
                Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())
        );

        Packet request = Packet.builder(Operation.PrintJob, mId.getAndIncrement())
                .setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                        attributes.build()))
                .setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return jobRequest.getDocument().openDocument();
                    }
                }).build();

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    private IppJob toPrintJob(IppJobRequest jobRequest, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) {
            throw new IOException("Missing JobAttributes in response from " + jobRequest.getPrinter());
        }
        Optional<Integer> jobId = group.get().getValue(Attributes.JobId);
        if (!jobId.isPresent()) {
            throw new IOException("Missing URI in job response from " + jobRequest.getPrinter());
        }
        return IppJob.of(jobId.get(), jobRequest, group.get());
    }

    /**
     * Send a job request not including its document, returning a new print job. Should be followed by
     * sendDocument to deliver document data.
     */
    public IppJob createJob(IppJobRequest jobRequest) throws IOException {
        URI printerUri = jobRequest.getPrinter().getUris().get(0);

        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri));

        Packet request = Packet.of(Operation.CreateJob, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes, attributes.build()));

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    /** Deliver document data for a print job, returning the updated print job. */
    public IppJob sendDocument(IppJob job) throws IOException {
        // Get all document bytes (non-streaming)
        byte[] bytes;
        Optional<IppJobRequest> jobRequest = job.getJobRequest();
        if (!jobRequest.isPresent()) throw new IllegalArgumentException("No job request present");

        try (InputStream inStream = jobRequest.get().getDocument().openDocument()) {
            // Copy from the source file
            bytes = ByteStreams.toByteArray(inStream);
        }

        URI printerUri = job.getPrinter().getUris().get(0);

        // Create a packet to be sent later
        final Packet request = Packet.builder(Operation.SendDocument, mId.getAndIncrement())
                .setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.JobId.of(job.getId()),
                        Attributes.DocumentName.of(jobRequest.get().getDocument().getName()),
                        Attributes.LastDocument.of(true)))
                .setData(bytes).build();

        return jobWithNewAttributes(job, mTransport.send(printerUri, request));
    }

    /** Send a print job cancellation request */
    public Packet cancelJob(IppJob job) throws IOException {
        URI printerUri = job.getPrinter().getUris().get(0);
        Packet request = Packet.of(Operation.CancelJob, mId.getAndIncrement(),
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.JobId.of(job.getId())));
        return mTransport.send(printerUri, request);
    }
}
