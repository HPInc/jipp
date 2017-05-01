package com.hp.jipp.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
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

    /**
     * Fetch current printer attributes into a new copy of the printer, or throw.
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
     * Fetch printer attributes
     */
    public AttributeGroup getPrinterAttributes(IppPrinter printer, Attribute<?>... extras) throws IOException {
       ImmutableList.Builder<Attribute<?>> operationAttributes = new ImmutableList.Builder<>();
        URI printerUri = printer.getUris().get(0);
        operationAttributes.add(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri));
        operationAttributes.add(extras);
        Packet request = Packet.of(Operation.GetPrinterAttributes, 0x01,
                AttributeGroup.of(Tag.OperationAttributes, operationAttributes.build()));

        Packet response = mTransport.send(printerUri, request);
        Optional<AttributeGroup> printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (response.getCode(Status.ENCODER).equals(Status.Ok) && printerAttributes.isPresent()) {
            return printerAttributes.get();
        } else {
            throw new IOException("No attributes");
        }
    }

    private IppPrinter getPrinterAttributes(IppPrinter printer, URI printerUri) throws IOException {
        Packet request = Packet.of(Operation.GetPrinterAttributes, 0x01,
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri)
//                        Attributes.RequestedAttributes.of(
//                                "charset-configured",
//                                "charset-supported",
//                                "color-supported",
//                                "compression-supported",
//                                "copies-supported",
//                                "document-format-details-supported",
//                                "epcl-version-supported",
//                                "finishings-supported",
//                                "number-up-default",
//                                "number-up-supported",
//                                "output-bin-supported",
//                                "pclm-compression-method-preferred",
//                                "pclm-raster-back-side",
//                                "pclm-source-resolution-supported",
//                                "pclm-strip-height-preferred",
//                                "pclm-strip-height-supported",
//                                "pdf-fit-to-page-supported",
//                                "presentation-direction-number-up-default",
//                                "presentation-direction-number-up-supported",
//                                "document-format-supported",
//                                "fit-to-page-default",
//                                "ipp-versions-supported",
//                                "job-account-id-supported",
//                                "job-accounting-user-id-supported",
//                                "job-creation-attributes-supported",
//                                "job-password-encryption-supported",
//                                "job-password-supported",
//                                "media-bottom-margin-supported",
//                                "media-col-default",
//                                "media-col-supported",
//                                "media-default",
//                                "media-left-margin-supported",
//                                "media-right-margin-supported",
//                                "media-size-supported",
//                                "media-source-supported",
//                                "media-supported",
//                                "media-top-margin-supported",
//                                "media-type-supported",
//                                "operations-supported",
//                                "page-bottom-default",
//                                "page-left-default",
//                                "page-right-default",
//                                "page-top-default",
//                                "print-color-mode-supported",
//                                "printer-device-id",
//                                "printer-dns-sd-name",
//                                "printer-icons",
//                                "printer-info",
//                                "printer-location",
//                                "printer-make-and-model",
//                                "printer-more-info",
//                                "printer-name",
//                                "printer-resolution-supported",
//                                "printer-settable-attributes-supported",
//                                "printer-supply-info-uri",
//                                "printer-type",
//                                "printer-uri-supported",
//                                "print-quality-default",
//                                "print-quality-supported",
//                                "reference-uri-schemes-supported",
//                                "sides-supported",
//                                "uri-authentication-supported",
//                                "uri-security-supported"
//                                )
                ));
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

    /** Return a validated job based on information in the job request */
    public ValidatedJob validateJob(JobRequest jobRequest) throws IOException {
        URI uri = jobRequest.getPrinter().getUris().get(0);

        Packet request = Packet.of(Operation.ValidateJob, 0x02,
                AttributeGroup.of(Tag.OperationAttributes,
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
    public List<PrintJob> getJobs(IppPrinter printer, Attribute<?>... extras) throws IOException {
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
    public List<PrintJob> getJobs(IppPrinter printer, List<Attribute<?>> extras) throws IOException {
        URI printerUri = printer.getUris().get(0);
        ImmutableList.Builder<Attribute<?>> attributesBuilder = new ImmutableList.Builder<Attribute<?>>()
                .add(Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri))
                .addAll(extras);

        Packet request = Packet.of(Operation.GetJobs, 0x03,
                AttributeGroup.of(Tag.OperationAttributes, attributesBuilder.build()));

        Packet response = mTransport.send(printerUri, request);

        ImmutableList.Builder<PrintJob> listBuilder = new ImmutableList.Builder<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                Optional<Integer> id= group.getValue(Attributes.JobId);
                if (!id.isPresent()) {
                    throw new IOException("Missing Job-ID in job response from " + printer);
                }
                listBuilder.add(PrintJob.of(id.get(), printer, group));
            }
        }

        return listBuilder.build();
    }

    /** Fetch current attributes into a new copy of the job */
    public PrintJob getJobAttributes(PrintJob job) throws IOException {
        URI printerUri = job.getPrinter().getUris().get(0);
        Packet request = Packet.of(Operation.GetJobAttributes, 0x03,
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(job.getPrinter().getUris()),
                        Attributes.JobId.of(job.getId())));
        return jobWithNewAttributes(job, mTransport.send(printerUri, request));
    }

    private PrintJob jobWithNewAttributes(PrintJob job, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) throw new IOException("Missing job attributes");
        return job.withAttributes(group.get());
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
        URI printerUri = jobRequest.getPrinter().getUris().get(0);

        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.JobName.of(jobRequest.getName()),
                Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())
        );

        // Note: sending document-name causes failure on Photosmart 6510
        Packet request = Packet.builder(Operation.PrintJob, 0x04)
                .setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                        attributes.build()))
                .setData(bytes).build();

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    private PrintJob toPrintJob(JobRequest jobRequest, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) {
            throw new IOException("Missing JobAttributes in response from " + jobRequest.getPrinter());
        }
        Optional<Integer> jobId = group.get().getValue(Attributes.JobId);
        if (!jobId.isPresent()) {
            throw new IOException("Missing URI in job response from " + jobRequest.getPrinter());
        }
        return PrintJob.of(jobId.get(), jobRequest, group.get());
    }

    /**
     * Send a job request not including its document, returning a new print job. Should be followed by
     * sendDocument to deliver document data.
     */
    public PrintJob createJob(JobRequest jobRequest) throws IOException {
        URI printerUri = jobRequest.getPrinter().getUris().get(0);

        ImmutableList.Builder<Attribute<?>> attributes = new ImmutableList.Builder<>();
        attributes.add(Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri));

        Packet request = Packet.of(Operation.CreateJob, 0x05,
                AttributeGroup.of(Tag.OperationAttributes, attributes.build()));

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    /** Deliver document data for a print job, returning the updated print job. */
    public PrintJob sendDocument(PrintJob job) throws IOException {
        // Get all document bytes (non-streaming)
        byte[] bytes;
        Optional<JobRequest> jobRequest = job.getJobRequest();
        if (!jobRequest.isPresent()) throw new IllegalArgumentException("No job request present");

        try (InputStream inStream = jobRequest.get().getDocument().openDocument()) {
            // Copy from the source file
            bytes = ByteStreams.toByteArray(inStream);
        }

        URI printerUri = job.getPrinter().getUris().get(0);

        // Create a packet to be sent later
        final Packet request = Packet.builder(Operation.SendDocument, 0x05)
                .setAttributeGroups(AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.JobId.of(job.getId()),
                        Attributes.LastDocument.of(true)))
                .setData(bytes).build();

        // Not sending document-name, compression, document-format, etc.
        return jobWithNewAttributes(job, mTransport.send(printerUri, request));
    }

    /** Send a print job cancellation request */
    public Packet cancelJob(PrintJob job) throws IOException {
        URI printerUri = job.getPrinter().getUris().get(0);
        Packet request = Packet.of(Operation.CancelJob, 0x03,
                AttributeGroup.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.JobId.of(job.getId())));
        return mTransport.send(printerUri, request);
    }
}
