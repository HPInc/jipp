package com.hp.jipp.client;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.IdentifyAction;
import com.hp.jipp.model.InputStreamFactory;
import com.hp.jipp.model.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic IPP operations
 */
public class IppClient {
    private static final int FIRST_ID = 0x1001;
    private static final String DEFAULT_USERNAME = "anonymous";

    private AtomicInteger mId = new AtomicInteger(FIRST_ID);
    private String mUserName = DEFAULT_USERNAME;

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

    /** Update the username provided in all requests */
    public void setUserName(String userName) {
        mUserName = userName;
    }

    /**
     * Fetch current printer attributes into a new copy of the printer, or throw. Each uri is attempted
     * until one of them works, the resulting Printer includes the first successful URI.
     */
    public Printer getPrinterAttributes(UUID printerUuid, List<URI> uris) throws IOException {
        @Nullable IOException lastThrown = null;
        for (URI uri : uris) {
            try {
                return getPrinterAttributes(printerUuid, uri);
            } catch (IOException thrown) {
                lastThrown = thrown;
            }
        }
        if (lastThrown != null) {
            throw new IOException("Fail after trying uris " + uris, lastThrown);
        } else {
            throw new IllegalArgumentException("No printer URIs present");
        }
    }

    /**
     * Fetch printer attributes from a specific URI.
     */
    public Printer getPrinterAttributes(UUID printerUuid, URI printerUri) throws IOException {
        Packet request = Packet.of(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.RequestingUserName.of(mUserName)));
        Packet response = mTransport.send(printerUri, request);
        AttributeGroup printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (response.getStatus().equals(Status.Ok) && printerAttributes != null) {
            return Printer.of(printerUuid, printerUri, printerAttributes);
        } else {
            throw new IOException("No printer attributes in response");
        }
    }

    /**
     * Fetch the printer's current status. Uses the primary URI only.
     */
    public PrinterStatus getPrinterStatus(Printer printer) throws IOException {
        List<Attribute<?>> operationAttributes = Arrays.<Attribute<?>>asList(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printer.getUri()),
                Attributes.RequestingUserName.of(mUserName),
                Attributes.RequestedAttributes.of(
                    Attributes.PrinterState.getName(),
                    Attributes.PrinterStateReasons.getName(),
                    Attributes.PrinterStateMessage.getName()
                ));

        Packet request = Packet.of(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, operationAttributes));

        Packet response = mTransport.send(printer.getUri(), request);
        AttributeGroup printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (response.getStatus().equals(Status.Ok) && printerAttributes != null) {
            return PrinterStatus.of(printerAttributes);
        } else {
            throw new IOException("No printer-attributes from " + printer);
        }
    }

    /**
     * Request the printer identify itself to the user somehow
     */
    public Packet identifyPrinter(Printer printer, IdentifyAction action, String message) throws IOException {
        Packet request = Packet.of(Operation.IdentifyPrinter, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printer.getUri()),
                        Attributes.RequestingUserName.of(mUserName),
                        Attributes.Message.of(message),
                        Attributes.IdentifyActions.of(action)));
        return mTransport.send(printer.getUri(), request);
    }

    /** Validated a job based on the contents of a job request. */
    public ValidatedJob validateJob(JobRequest jobRequest) throws IOException {
        URI uri = jobRequest.getPrinter().getUri();
        Packet request = Packet.of(Operation.ValidateJob, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(uri),
                        Attributes.RequestingUserName.of(mUserName),
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())));

        Packet response = mTransport.send(uri, request);
        return ValidatedJob.of(jobRequest, response);
    }

    /** Send a job request, including its document, returning a new print job. */
    public Job printJob(final JobRequest jobRequest) throws IOException {
        // See https://tools.ietf.org/html/rfc2911#section-3.2.1.1

        URI printerUri = jobRequest.getPrinter().getUri();

        List<Attribute<?>> attributes = Arrays.<Attribute<?>>asList(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.RequestingUserName.of(mUserName),
                Attributes.JobName.of(jobRequest.getName()),
                Attributes.DocumentName.of(jobRequest.getDocument().getName()),
                Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())
        );

        Packet request = Packet.builder(Operation.PrintJob, mId.getAndIncrement())
                .setAttributeGroups(AttributeGroup.Companion.of(Tag.OperationAttributes, attributes))
                .setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return jobRequest.getDocument().openDocument();
                    }
                }).build();

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    private Job toPrintJob(JobRequest jobRequest, Packet response) throws IOException {
        AttributeGroup group = response.getAttributeGroup(Tag.JobAttributes);
        if (group == null) {
            throw new IOException("Missing job-attributes in response from " + jobRequest.getPrinter());
        }
        return Job.of(getJobId(group, jobRequest.getPrinter()), jobRequest, group);
    }

    private int getJobId(AttributeGroup group, Printer printer) throws IOException {
        Integer jobId = group.getValue(Attributes.JobId);
        if (jobId == null) {
            throw new IOException("Missing job-id job response from " + printer);
        }
        return jobId;
    }

    /**
     * Send a job request not including its document, returning a new print job. Should be followed by
     * sendDocument to deliver document data.
     */
    public Job createJob(JobRequest jobRequest) throws IOException {
        URI printerUri = jobRequest.getPrinter().getUri();

        List<Attribute<?>> attributes = Arrays.<Attribute<?>>asList(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.RequestingUserName.of(mUserName));

        Packet request = Packet.of(Operation.CreateJob, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, attributes));

        Packet response = mTransport.send(printerUri, request);
        return toPrintJob(jobRequest, response);
    }

    /** Deliver document data for a print job, returning the updated print job. */
    public Job sendDocument(Job job) throws IOException {
        final JobRequest jobRequest = job.getJobRequest();
        if (jobRequest == null) throw new IllegalArgumentException("No job request present");

        URI printerUri = job.getPrinter().getUri();

        // Send only document for this job.
        AttributeGroup operationAttributes = AttributeGroup.Companion.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.JobId.of(job.getId()),
                Attributes.RequestingUserName.of(mUserName),
                Attributes.DocumentName.of(jobRequest.getDocument().getName()),
                Attributes.LastDocument.of(true));

        Packet request = Packet.builder(Operation.SendDocument, mId.getAndIncrement())
                .setAttributeGroups(operationAttributes)
                .setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return jobRequest.getDocument().openDocument();
                    }
                }).build();

        Packet response = mTransport.send(printerUri, request);
        AttributeGroup group = response.getAttributeGroup(Tag.JobAttributes);
        if (group == null) throw new IOException("Missing job attributes");
        return job.withAttributes(group);
    }

    /**
     * Fetch a list of all jobs known by the printer.
     * <p>
     * Job records returned here will not contain any PrintJobRequest.
     */
    public List<Job> getJobs(Printer printer, Attribute<?>... extras) throws IOException {
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
    public List<Job> getJobs(Printer printer, List<Attribute<?>> extras) throws IOException {
        List<Attribute<?>> attributes = new ArrayList<>();
        attributes.addAll(Arrays.asList(Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printer.getUri()),
                        Attributes.RequestingUserName.of(mUserName)));
        attributes.addAll(extras);

        Packet request = Packet.of(Operation.GetJobs, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, attributes));

        Packet response = mTransport.send(printer.getUri(), request);

        List<Job> jobs = new ArrayList<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                jobs.add(Job.of(getJobId(group, printer), printer, group));
            }
        }

        return jobs;
    }

    /** Fetch new status for a job and return the updated job. */
    public Job getJobStatus(Job job) throws IOException {
        Packet request = Packet.of(Operation.GetJobAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(job.getPrinter().getUri()),
                        Attributes.JobId.of(job.getId()),
                        Attributes.RequestingUserName.of(mUserName),
                        Attributes.RequestedAttributes.of(JobStatus.getAttributeNames())));
        AttributeGroup jobAttributes = mTransport.send(job.getPrinter().getUri(), request)
                .getAttributeGroup(Tag.JobAttributes);
        if (jobAttributes == null) throw new IOException("Missing job attributes");
        return job.withStatus(JobStatus.of(jobAttributes));
    }

    /** Send a print job cancellation request */
    public Packet cancelJob(Job job) throws IOException {
        URI printerUri = job.getPrinter().getUri();
        Packet request = Packet.of(Operation.CancelJob, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.JobId.of(job.getId()),
                        Attributes.RequestingUserName.of(mUserName)));
        // The cancel response contains no JobAttributes
        return mTransport.send(printerUri, request);
    }
}
