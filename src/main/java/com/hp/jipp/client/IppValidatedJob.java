package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.Packet;

@AutoValue
public abstract class IppValidatedJob {

    static IppValidatedJob of(IppJobRequest request, Packet response) {
        return new AutoValue_IppValidatedJob(request, response);
    }

    /** The original job request leading to this validation */
    public abstract IppJobRequest getJobRequest();

    public abstract Packet getPacket();
}
