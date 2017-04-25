package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.Packet;

@AutoValue
public abstract class ValidatedJob {

    static ValidatedJob of(JobRequest request, Packet response) {
        return new AutoValue_ValidatedJob(request, response);
    }

    /** The original job request leading to this validation */
    public abstract JobRequest getJobRequest();

    public abstract Packet getPacket();
}
