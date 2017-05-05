package com.hp.jipp.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class JobStateTest {
    @Test
    public void jobStateFinals() {
        assertEquals(false, JobState.Processing.isFinal());
        assertEquals(false, JobState.Pending.isFinal());
        assertEquals(false, JobState.PendingHeld.isFinal());
        assertEquals(false, JobState.ProcessingStopped.isFinal());

        assertEquals(true, JobState.Aborted.isFinal());
        assertEquals(true, JobState.Completed.isFinal());
        assertEquals(true, JobState.Canceled.isFinal());
    }
}
