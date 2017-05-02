package com.hp.jipp.client;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class IppJobRequest {
    public static IppJobRequest of(IppPrinter printer, String jobName, IppDocument document) {
        return new AutoValue_IppJobRequest(printer, jobName, document);
    }

    public abstract IppPrinter getPrinter();

    public abstract String getName();

    public abstract IppDocument getDocument();
}
