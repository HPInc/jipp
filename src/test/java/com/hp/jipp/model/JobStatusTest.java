package com.hp.jipp.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class JobStateTest {
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
    public void autoPending() {

    }
}
