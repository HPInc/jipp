// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/**
 * Job State values.

 * See [RFC2911 Section 4.3.7](https://tools.ietf.org/html/rfc2911.section-4.3.7)
 */
data class JobState( override val code: Int, override val name: String) : Enum() {

    /** Return true if this state is the terminating state for a job  */
    val isFinal: Boolean
        get() = this == canceled || this == aborted || this == completed

    /** The attribute type for a [JobState] attribute */
    class Type(name: String) : EnumType<JobState>(ENCODER, name)

    companion object {
        @JvmField val pending = JobState(3, "pending")
        @JvmField val pendingHeld = JobState(4, "pending-held")
        @JvmField val processing = JobState(5, "processing")
        @JvmField val processingStopped = JobState(6, "processing-stopped")
        @JvmField val canceled = JobState(7, "canceled")
        @JvmField val aborted = JobState(8, "aborted")
        @JvmField val completed = JobState(9, "completed")

        @JvmField val ENCODER = encoderOf(JobState::class.java, { code, name -> JobState(code, name) })
    }
}
