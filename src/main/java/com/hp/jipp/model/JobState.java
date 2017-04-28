package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.NameCode;

/**
 * Job State values.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-4.3.7">RFC2911 Section 4.3.7</a>
 */
@AutoValue
public abstract class JobState extends NameCode {

    public static final JobState Pending = of("pending", 3);
    public static final JobState PendingHeld = of("pending-held", 4);
    public static final JobState Processing = of("processing", 5);
    public static final JobState ProcessingStopped = of("processing-stopped", 6);
    public static final JobState Canceled = of("canceled", 7);
    public static final JobState Aborted = of("aborted", 8);
    public static final JobState Completed = of("completed", 9);

    public final static NameCodeType.Encoder<JobState> ENCODER = NameCodeType.encoder(
            "job-state", ImmutableSet.of(
                    Pending, PendingHeld, Processing, ProcessingStopped, Canceled, Aborted, Completed
            ), new NameCode.Factory<JobState>() {
                @Override
                public JobState of(String name, int code) {
                    return JobState.of(name, code);
                }
            });

    public static JobState of(String name, int code) {
        return new AutoValue_JobState(name, code);
    }
}
