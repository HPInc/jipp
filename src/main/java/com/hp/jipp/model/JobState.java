package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.EnumType;
import com.hp.jipp.encoding.NameCode;

/**
 * Job State values.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-4.3.7">RFC2911 Section 4.3.7</a>
 */
@AutoValue
public abstract class JobState extends NameCode {

    public static final JobState Pending = create("pending", 3);
    public static final JobState PendingHeld = create("pending-held", 4);
    public static final JobState Processing = create("processing", 5);
    public static final JobState ProcessingStopped = create("processing-stopped", 6);
    public static final JobState Canceled = create("canceled", 7);
    public static final JobState Aborted = create("aborted", 8);
    public static final JobState Completed = create("completed", 9);

    public final static EnumType.Encoder<JobState> ENCODER = EnumType.encoder(
            "job-state", ImmutableSet.of(
                    Pending, PendingHeld, Processing, ProcessingStopped, Canceled, Aborted, Completed
            ), new NameCode.Factory<JobState>() {
                @Override
                public JobState create(String name, int code) {
                    return create(name, code);
                }
            });

    public static JobState create(String name, int code) {
        return new AutoValue_JobState(name, code);
    }
}
