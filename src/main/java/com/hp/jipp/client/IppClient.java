package com.hp.jipp.client;

import com.gladed.func.F1;
import com.gladed.late.Late;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.IdentifyAction;
import com.hp.jipp.model.InputStreamFactory;
import com.hp.jipp.model.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
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
         * Gets the packet, synchronously delivers it to the specified URL, and asynchronously returns the response
         * Packet or a thrown error (if the response is anything but 200 OK with avalid packet).
         */
        Late<Packet> send(URI uri, Packet packet);
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
    public Late<Printer> getPrinterAttributes(UUID printerUuid, List<URI> uris) {
        final Stack<URI> uriStack = new Stack<>();
        uriStack.addAll(uris);
        return nextPrinterUriAttributes(printerUuid, uriStack,
                Late.<Printer>fail(new IllegalArgumentException("No URIs")));
    }

    private Late<Printer> nextPrinterUriAttributes(final UUID printerUuid, final Stack<URI> uris,
                                                    Late<Printer> previous) {
        if (uris.isEmpty()) return previous;
        return previous.flatRecover(new F1<Exception, Late<Printer>>() {
            @Override
            public Late<Printer> apply(Exception e) {
                return nextPrinterUriAttributes(printerUuid, uris, getPrinterAttributes(printerUuid, uris.pop()));
            }
        });
    }

    /**
     * Fetch printer attributes from a specific URI.
     */
    public Late<Printer> getPrinterAttributes(final UUID printerUuid, final URI printerUri) {
        Packet request = new Packet(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printerUri),
                        Attributes.RequestingUserName.of(mUserName)));
        return mTransport.send(printerUri, request).map(new F1<Packet, Printer>() {
            @Override
            public Printer apply(Packet response) throws IOException {
                AttributeGroup printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
                if (response.getStatus().equals(Status.Ok) && printerAttributes != null) {
                    return new Printer(printerUuid, printerUri, printerAttributes);
                } else {
                    throw new IOException("No printer attributes in response");
                }
            }
        });
    }

    /**
     * Fetch the printer's current status. Uses the primary URI only.
     */
    public Late<PrinterStatus> getPrinterStatus(final Printer printer) {
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

        Packet request = new Packet(Operation.GetPrinterAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, operationAttributes));

        return mTransport.send(printer.getUri(), request).map(new F1<Packet, PrinterStatus>() {
            @Override
            public PrinterStatus apply(Packet response) throws IOException {
                AttributeGroup printerAttributes = response.getAttributeGroup(Tag.PrinterAttributes);
                if (response.getStatus().equals(Status.Ok) && printerAttributes != null) {
                    return PrinterStatus.Companion.of(printerAttributes);
                } else {
                    throw new IOException("No printer-attributes from " + printer);
                }
            }
        });
    }

    /**
     * Request the printer identify itself to the user somehow
     */
    public Late<Packet> identifyPrinter(Printer printer, IdentifyAction action, String message) {
        Packet request = new Packet(Operation.IdentifyPrinter, mId.getAndIncrement(),
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
    public Late<ValidatedJob> validateJob(final JobRequest jobRequest) {
        URI uri = jobRequest.getPrinter().getUri();
        Packet request = new Packet(Operation.ValidateJob, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(uri),
                        Attributes.RequestingUserName.of(mUserName),
                        Attributes.DocumentFormat.of(jobRequest.getDocument().getDocumentType())));

        return mTransport.send(uri, request).map(new F1<Packet, ValidatedJob>() {
            public ValidatedJob apply(Packet response) {
                return new ValidatedJob(jobRequest, response);
            }
        });
    }

    /** Send a job request, including its document, returning a new print job. */
    public Late<Job> printJob(final JobRequest jobRequest) {
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

        Packet.Builder builder = new Packet.Builder(Operation.PrintJob, mId.getAndIncrement());
        builder.setAttributeGroups(AttributeGroup.Companion.of(Tag.OperationAttributes, attributes));
        builder.setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return jobRequest.getDocument().openDocument();
                    }
                });

        return mTransport.send(printerUri, builder.build()).map(new F1<Packet, Job>() {
            @Override
            public Job apply(Packet response) throws IOException {
                return toPrintJob(jobRequest, response);
            }
        });
    }

    private Job toPrintJob(JobRequest jobRequest, Packet response) throws IOException {
        AttributeGroup group = response.getAttributeGroup(Tag.JobAttributes);
        if (group == null) {
            // TODO: In server-error-busy or other server error cases we throw without conveying reason
            throw new IOException("Missing job-attributes in response from " + jobRequest.getPrinter());
        }
        return new Job(getJobId(group, jobRequest.getPrinter()), jobRequest, group);
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
    public Late<Job> createJob(final JobRequest jobRequest) {
        URI printerUri = jobRequest.getPrinter().getUri();

        List<Attribute<?>> attributes = Arrays.<Attribute<?>>asList(
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(printerUri),
                Attributes.RequestingUserName.of(mUserName));

        Packet request = new Packet(Operation.CreateJob, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, attributes));

        return mTransport.send(printerUri, request).map(new F1<Packet, Job>() {
            @Override
            public Job apply(Packet response) throws IOException {
                return toPrintJob(jobRequest, response);
            }
        });
    }

    /** Deliver document data for a print job, returning the updated print job. */
    public Late<Job> sendDocument(final Job job) {
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

        Packet.Builder builder = new Packet.Builder(Operation.SendDocument, mId.getAndIncrement());
        builder.setAttributeGroups(operationAttributes);
        builder.setInputStreamFactory(new InputStreamFactory() {
                    @Override
                    public InputStream createInputStream() throws IOException {
                        return jobRequest.getDocument().openDocument();
                    }
                });

        return mTransport.send(printerUri, builder.build()).map(new F1<Packet, Job>() {
            @Override
            public Job apply(Packet response) throws IOException {
                AttributeGroup group = response.getAttributeGroup(Tag.JobAttributes);
                if (group == null) throw new IOException("Missing job attributes");
                return job.withAttributes(group);
            }
        });
    }

    /**
     * Fetch a list of all jobs known by the printer.
     * <p>
     * Job records returned here will not contain any PrintJobRequest.
     */
    public Late<List<Job>> getJobs(Printer printer, Attribute<?>... extras) {
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
    public Late<List<Job>> getJobs(final Printer printer, List<Attribute<?>> extras) {
        List<Attribute<?>> attributes = new ArrayList<>();
        attributes.addAll(Arrays.asList(Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(printer.getUri()),
                        Attributes.RequestingUserName.of(mUserName)));
        attributes.addAll(extras);

        Packet request = new Packet(Operation.GetJobs, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes, attributes));

        return mTransport.send(printer.getUri(), request).map(new F1<Packet, List<Job>>() {
            @Override
            public List<Job> apply(Packet response) throws Exception {
                List<Job> jobs = new ArrayList<>();
                for (AttributeGroup group : response.getAttributeGroups()) {
                    if (group.getTag().equals(Tag.JobAttributes)) {
                        jobs.add(new Job(getJobId(group, printer), printer, group));
                    }
                }
                return jobs;
            }
        });
    }

    /** Fetch new status for a job and return the updated job. */
    public Late<Job> getJobStatus(final Job job) {
        Packet request = new Packet(Operation.GetJobAttributes, mId.getAndIncrement(),
                AttributeGroup.Companion.of(Tag.OperationAttributes,
                        Attributes.AttributesCharset.of("utf-8"),
                        Attributes.AttributesNaturalLanguage.of("en"),
                        Attributes.PrinterUri.of(job.getPrinter().getUri()),
                        Attributes.JobId.of(job.getId()),
                        Attributes.RequestingUserName.of(mUserName),
                        Attributes.RequestedAttributes.of(JobStatus.AttributeNames)));
        return mTransport.send(job.getPrinter().getUri(), request).map(new F1<Packet, Job>() {
            @Override
            public Job apply(Packet response) throws IOException {
                AttributeGroup jobAttributes = response.getAttributeGroup(Tag.JobAttributes);
                if (jobAttributes == null) throw new IOException("Missing job attributes");
                return job.withStatus(JobStatus.of(jobAttributes));
            }
        });
    }

    /** Send a print job cancellation request */
    public Late<Packet> cancelJob(Job job) {
        URI printerUri = job.getPrinter().getUri();
        Packet request = new Packet(Operation.CancelJob, mId.getAndIncrement(),
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
