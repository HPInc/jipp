package com.hp.jipp.client;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JobRequest {
    public static JobRequest of(IppPrinter printer, BaseDocument document) {
        return new AutoValue_JobRequest(printer, document);
    }

    public abstract IppPrinter getPrinter();
    public abstract BaseDocument getDocument();
}
