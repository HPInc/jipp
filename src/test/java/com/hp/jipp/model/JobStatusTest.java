package com.hp.jipp.model;

import com.hp.jipp.client.JobStatus;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Tag;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class JobStatusTest {
    @Test
    public void jobStateFinals() {
        assertFalse(JobState.Processing.isFinal());
        assertFalse(JobState.Pending.isFinal());
        assertFalse(JobState.PendingHeld.isFinal());
        assertFalse(JobState.ProcessingStopped.isFinal());

        assertTrue(JobState.Aborted.isFinal());
        assertTrue(JobState.Completed.isFinal());
        assertTrue(JobState.Canceled.isFinal());
    }

    @Test
    public void autoPending() throws Exception {
        // Job State is required, but HP Officejet Pro 8600 may return JobAttributes without it during early job s
        // setup. So, we should assume Pending if no state is present.
        AttributeGroup jobAttributes = AttributeGroup.of(Tag.JobAttributes,
                Attributes.JobUri.of(new URI("ipp://something/ipp/printer/job-0028")),
                Attributes.JobId.of(28));
        assertEquals(JobState.Pending, JobStatus.of(jobAttributes).getState());
    }
}
