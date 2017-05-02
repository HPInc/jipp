package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;

import java.io.IOException;

@AutoValue
public abstract class IppJob {

    static IppJob of(int id, IppPrinter printer, AttributeGroup jobAttributes) {
        return new AutoValue_IppJob(id, printer, Optional.<IppJobRequest>absent(), jobAttributes);
    }

    static IppJob of(int id, IppJobRequest jobRequest, AttributeGroup jobAttributes) {
        return new AutoValue_IppJob(id, jobRequest.getPrinter(), Optional.of(jobRequest), jobAttributes);
    }

    public abstract int getId();

    public abstract IppPrinter getPrinter();

    /** Original job request, if accessible */
    public abstract Optional<IppJobRequest> getJobRequest();

    /** Printer-supplied attributes for the job */
    public abstract AttributeGroup getAttributes();

    /** Returns a new IppJob containing more current JobAttributes from the enclosed response packet */
    public IppJob withAttributes(AttributeGroup newAttributes) throws IOException {
        return new AutoValue_IppJob(getId(), getPrinter(), getJobRequest(), newAttributes);
    }
}
