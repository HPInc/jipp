package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.util.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@AutoValue
public abstract class PrintJob {

    // TODO: Too much repeated code here

    static PrintJob of(JobRequest request, Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) throw new IOException("Missing JobAttributes in response from " + request.getPrinter());
        Optional<URI> uri = group.get().getValue(Attributes.JobUri);
        if (!uri.isPresent()) throw new IOException("Missing URI in job response from " + request.getPrinter());
        return new AutoValue_PrintJob(uri.get(), request.getPrinter(), request, group.get());
    }

    static PrintJob of(IppPrinter printer, AttributeGroup group) throws IOException {
        Optional<URI> uri = group.getValue(Attributes.JobUri);
        if (!uri.isPresent()) throw new IOException("Missing URI in job response from " + printer);
        return new AutoValue_PrintJob(uri.get(), printer, null, group);
    }

    static List<PrintJob> toJobs(IppPrinter printer, Packet response) throws IOException {
        ImmutableList.Builder<PrintJob> listBuilder = new ImmutableList.Builder<>();
        for (AttributeGroup group : response.getAttributeGroups()) {
            if (group.getTag().equals(Tag.JobAttributes)) {
                listBuilder.add(PrintJob.of(printer, group));
            }
        }
        return listBuilder.build();
    }

    public abstract URI getUri();

    public abstract IppPrinter getPrinter();

    /** Original job request, if accessible */
    @Nullable
    public abstract JobRequest getJobRequest();

    /** Printer-supplied attributes for the job */
    public abstract AttributeGroup getAttributes();

    /** Returns a new PrintJob containing more current JobAttributes from the enclosed response packet */
    PrintJob withResponse(Packet response) throws IOException {
        Optional<AttributeGroup> group = response.getAttributeGroup(Tag.JobAttributes);
        if (!group.isPresent()) throw new IOException("Missing job attributes");
        return new AutoValue_PrintJob(getUri(), getPrinter(), getJobRequest(), group.get());
    }
}
