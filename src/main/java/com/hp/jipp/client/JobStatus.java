package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.JobState;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@AutoValue
public abstract class JobStatus {

    static List<String> getAttributeNames() {
        return Arrays.asList(
                Attributes.JobState.getName(),
                Attributes.JobStateReasons.getName(),
                Attributes.JobStateMessage.getName(),
                Attributes.JobDetailedStatusMessages.getName());
    }

    static JobStatus of(AttributeGroup attributes) throws IOException {
        JobState state = attributes.getValue(Attributes.JobState);
        if (state == null) throw new IOException("Missing " + Attributes.JobState.getName());
        return new AutoValue_JobStatus(state,
                attributes.getValues(Attributes.JobStateReasons),
                attributes.getValue(Attributes.JobStateMessage),
                attributes.getValues(Attributes.JobDetailedStatusMessages));
    }

    public abstract JobState getState();

    public abstract List<String> getReasons();

    @Nullable
    public abstract String getMessage();

    public abstract List<String> getDetailedMessages();

    @Override
    public String toString() {
        return "JobStatus{state=" + getState().getName() +
                (getReasons().isEmpty() ? "" : " r=" + getReasons()) +
                (getMessage() == null ? "" : "m+" + getMessage()) +
                (getDetailedMessages().isEmpty() ? "" : " x=" + getDetailedMessages()) +
                "}";
    }
}
