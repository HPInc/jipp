package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;

import java.io.IOException;

@AutoValue
public abstract class PrintJob {

    static PrintJob of(int id, IppPrinter printer, AttributeGroup jobAttributes) {
        return new AutoValue_PrintJob(id, printer, Optional.<JobRequest>absent(), jobAttributes);
    }

    static PrintJob of(int id, JobRequest jobRequest, AttributeGroup jobAttributes) {
        return new AutoValue_PrintJob(id, jobRequest.getPrinter(), Optional.of(jobRequest), jobAttributes);
    }

    public abstract int getId();

    public abstract IppPrinter getPrinter();

    /** Original job request, if accessible */
    public abstract Optional<JobRequest> getJobRequest();

    /** Printer-supplied attributes for the job */
    public abstract AttributeGroup getAttributes();

    /** Returns a new PrintJob containing more current JobAttributes from the enclosed response packet */
    public PrintJob withAttributes(AttributeGroup newAttributes) throws IOException {
        return new AutoValue_PrintJob(getId(), getPrinter(), getJobRequest(), newAttributes);
    }
}
