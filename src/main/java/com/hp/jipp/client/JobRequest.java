package com.hp.jipp.client;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JobRequest {
    public static JobRequest of(IppPrinter printer, String jobName, BaseDocument document) {
        return new AutoValue_JobRequest(printer, jobName, document);
    }

    public abstract IppPrinter getPrinter();
    public abstract String getName();
    public abstract BaseDocument getDocument();
}
