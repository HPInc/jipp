package com.hp.jipp.client;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JobRequest {
    public static JobRequest of(Printer printer, String jobName, Document document) {
        return new AutoValue_JobRequest(printer, jobName, document);
    }

    public abstract Printer getPrinter();

    public abstract String getName();

    public abstract Document getDocument();
}
