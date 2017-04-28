package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.util.Nullable;

import java.io.IOException;
import java.net.URI;

@AutoValue
public abstract class PrintJob {

    static PrintJob of(URI uri, IppPrinter printer, AttributeGroup jobAttributes) {
        return new AutoValue_PrintJob(uri, printer, null, jobAttributes);
    }

    static PrintJob of(URI uri, JobRequest jobRequest, AttributeGroup jobAttributes) {
        return new AutoValue_PrintJob(uri, jobRequest.getPrinter(), jobRequest, jobAttributes);
    }

    public abstract URI getUri();

    public abstract IppPrinter getPrinter();

    /** Original job request, if accessible */
    @Nullable
    public abstract JobRequest getJobRequest();

    /** Printer-supplied attributes for the job */
    public abstract AttributeGroup getAttributes();

    /** Returns a new PrintJob containing more current JobAttributes from the enclosed response packet */
    PrintJob withAttributes(AttributeGroup newAttributes) throws IOException {
        return new AutoValue_PrintJob(getUri(), getPrinter(), getJobRequest(), newAttributes);
    }
}
