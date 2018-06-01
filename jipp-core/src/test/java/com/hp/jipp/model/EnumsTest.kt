// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.util.KotlinTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class EnumsTest {
    @Test
    fun jobStateTest() {
        KotlinTest.cover(Types.jobState.of(JobState.Code.pendingHeld),
            cycle<JobState>(Types.jobState.of(JobState.pendingHeld)),
            Types.jobState.of(JobState.Encoder[77]))

        assertTrue(JobState.aborted.isFinal)
        assertFalse(JobState.pendingHeld.isFinal)
    }

    @Test
    fun printQualityTest() {
        KotlinTest.cover(Types.printQuality.of(PrintQuality.Code.high),
            cycle<PrintQuality>(Types.printQuality.of(PrintQuality.high)),
            Types.printQuality.of(PrintQuality.Encoder[123]))
    }
}