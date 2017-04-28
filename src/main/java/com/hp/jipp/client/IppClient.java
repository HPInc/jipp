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

    /** Fetch current printer attributes into a new copy of the printer, or throws if not possible */
    public IppPrinter getPrinterAttributes(IppPrinter printer) throws IOException {
        IppPrinter ippPrinter = null;

        for (URI uri : printer.getUris()) {
            Packet request = Packet.create(Operation.GetPrinterAttributes, 0x01,
                    AttributeGroup.create(Tag.OperationAttributes,
                            Attributes.AttributesCharset.of("utf-8"),
                            Attributes.AttributesNaturalLanguage.of("en"),
                            Attributes.PrinterUri.of(uri)
//                            Attributes.RequestedAttributes.of(
//                                    "charset-configured",
//                                    "charset-supported",
//                                    "color-supported",
//                                    "compression-supported",
//                                    "copies-supported",
////                                    "document-format-details-supported",
////                                    "epcl-version-supported",
////                                    "finishings-supported",
//
////                                    "number-up-default",
////                                    "number-up-supported",
////                                    "output-bin-supported",
////                                    "pclm-compression-method-preferred",
////                                    "pclm-raster-back-side",
////                                    "pclm-source-resolution-supported",
////                                    "pclm-strip-height-preferred",
////                                    "pclm-strip-height-supported",
////                                    "pdf-fit-to-page-supported",
////                                    "presentation-direction-number-up-default",
////                                    "presentation-direction-number-up-supported",
//
//                                    "document-format-supported",
////                                    "fit-to-page-default",
//                                    "ipp-versions-supported",
////                                    "job-account-id-supported",
////                                    "job-accounting-user-id-supported",
////                                    "job-creation-attributes-supported",
////                                    "job-password-encryption-supported",
////                                    "job-password-supported",
//                                    "media-bottom-margin-supported",
//                                    "media-col-default",
//                                    "media-col-supported",
//                                    "media-default",
//                                    "media-left-margin-supported",
//                                    "media-right-margin-supported",
//                                    "media-size-supported",
//                                    "media-source-supported",
//                                    "media-supported",
//                                    "media-top-margin-supported",
//                                    "media-type-supported",
//                                    "operations-supported",
//                                    "page-bottom-default",
//                                    "page-left-default",
//                                    "page-right-default",
//                                    "page-top-default",
//                                    "print-color-mode-supported",
//                                    "printer-device-id",
////                                    "printer-dns-sd-name",
//                                    "printer-icons",
//                                    "printer-info",
//                                    "printer-location",
//                                    "printer-make-and-model",
//                                    "printer-more-info",
//                                    "printer-name",
//                                    "printer-resolution-supported",
//                                    "printer-settable-attributes-supported",
//                                    "printer-supply-info-uri",
//                                    "printer-type",
//                                    "printer-uri-supported",
//                                    "print-quality-default",
//                                    "print-quality-supported",
////                                    "reference-uri-schemes-supported",
////                                    "sides-supported",
//                                    "uri-authentication-supported",
//                                    "uri-security-supported"
//                                    )
                    ));
            Packet response = mTransport.send(uri, request);
            Optional<AttributeGroup> printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
            if (response.getCode(Status.ENCODER).equals(Status.Ok) && printerAttributes.isPresent()) {
                // Sort the first working URI to the top of the list.
                ImmutableList.Builder<URI> newUris = new ImmutableList.Builder<>();
                newUris.add(uri);
                for (URI oldUri : printer.getUris()) {
                    if (!oldUri.equals(uri)) newUris.add(oldUri);
                }
                ippPrinter = IppPrinter.of(newUris.build(), printerAttributes.get());
                break;
            }
        }

        if (ippPrinter == null) {
            throw new IOException("No valid attributes returned for " + printer);
        } else {
            return ippPrinter;
        }
    }

    /** Return a validated job based on information in the job request */
    public ValidatedJob validateJob(JobRequest jobRequest) throws IOException {
        URI uri = jobRequest.getPrinter().getUris().get(0);

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
        URI printerUri = printer.getUris().get(0);
        Packet request = Packet.create(Operation.GetJobs, 0x03,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri)));

        Packet response = mTransport.send(printerUri, request);

        ImmutableList.Builder<PrintJob> listBuilder = new ImmutableList.Builder<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                Optional<URI> uri = group.getValue(Attributes.JobUri);
                if (!uri.isPresent()) {
                    throw new IOException("Missing URI in job response from " + printer);
                }
                URI jobUri = swapAuthority(uri.get(), printerUri);
                listBuilder.add(PrintJob.of(jobUri, printer, group));
            }
        }

        return listBuilder.build();
    }


    /** Replace the authority in a URI with a known-good authority */
    private URI swapAuthority(URI uri, URI goodAuthority) {
        try {
            return new URI(goodAuthority.getScheme(), goodAuthority.getAuthority(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to convert URI " + uri, e);
        }
    }

    /** Fetch current attributes into a new copy of the job */
    public PrintJob getJobAttributes(PrintJob job) throws IOException {
        Packet request = Packet.create(Operation.GetJobAttributes, 0x03,
                AttributeGroup.create(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.JobUri.of(job.getUri())));

        return jobWithNewAttributes(job, mTransport.send(job.getUri(), request));
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
                Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType()));

        // Add job and document names if request includes a document name
        if (jobRequest.getDocument().getName() != null) {
            attributes.add(Attributes.JobName.of(jobRequest.getDocument().getName()),
                    Attributes.DocumentName.of(jobRequest.getDocument().getName()));
        }

        Packet request = Packet.builder(Operation.PrintJob, 0x04)
                .setAttributeGroups(AttributeGroup.create(Tag.OperationAttributes,
                        attributes.build()))
                .setData(bytes).build();

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, printerUri, response);
    }

    private PrintJob toPrintJob(JobRequest jobRequest, URI printerUri, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) {
            throw new IOException("Missing JobAttributes in response from " + jobRequest.getPrinter());
        }
        Optional<URI> uri = group.get().getValue(Attributes.JobUri);
        if (!uri.isPresent()) {
            throw new IOException("Missing URI in job response from " + jobRequest.getPrinter());
        }
        return PrintJob.of(swapAuthority(uri.get(), printerUri), jobRequest, group.get());
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

        Packet request = Packet.create(Operation.CreateJob, 0x05,
                AttributeGroup.create(Tag.OperationAttributes, attributes.build()));

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, printerUri, response);
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
        return jobWithNewAttributes(job, mTransport.send(job.getUri(), request));
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
