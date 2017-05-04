package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.JobState;

import java.io.IOException;
import java.util.List;

@AutoValue
public abstract class JobStatus {

    static List<String> getAttributeNames() {
        return ImmutableList.of(
                Attributes.JobState.getName(),
                Attributes.JobStateReasons.getName(),
                Attributes.JobStateMessage.getName(),
                Attributes.JobDetailedStatusMessages.getName());
    }

    static JobStatus of(AttributeGroup attributes) throws IOException {
        Optional<JobState> state = attributes.getValue(Attributes.JobState);
        if (!state.isPresent()) throw new IOException("Missing " + Attributes.JobState.getName());
        return new AutoValue_JobStatus(state.get(), attributes.getValues(Attributes.JobStateReasons),
                attributes.getValue(Attributes.JobStateMessage),
                attributes.getValues(Attributes.JobDetailedStatusMessages));
    }

    public abstract JobState getState();

    public abstract List<String> getReasons();

    public abstract Optional<String> getMessage();

    public abstract List<String> getDetailedMessages();

    @Override
    public String toString() {
        Optional<String> message = getMessage();

        return "Job{state=" + getState().getName() +
                (getReasons().isEmpty() ? "" : " r=" + getReasons()) +
                (message.isPresent() && !message.get().isEmpty() ? " m=" + message.get() : "") +
                (getDetailedMessages().isEmpty() ? "" : " x=" + getDetailedMessages()) +
                "}";
    }
}
