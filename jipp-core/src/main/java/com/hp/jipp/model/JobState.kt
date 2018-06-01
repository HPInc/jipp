// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/**
 * Job State values.

 * See [RFC2911 Section 4.3.7](https://tools.ietf.org/html/rfc2911.section-4.3.7)
 */
data class JobState(override val code: Int, override val name: String) : Enum() {

    override fun toString() = super.toString()

    /** Return true if this state is the terminating state for a job  */
    val isFinal: Boolean
        get() = this == canceled || this == aborted || this == completed

    /** The attribute type for a [JobState] attribute */
    class Type(name: String) : EnumType<JobState>(Encoder, name)

    /** Raw codes which may be used for direct comparisons */
    object Code {
        const val pending = 3
        const val pendingHeld = 4
        const val processing = 5
        const val processingStopped = 6
        const val canceled = 7
        const val aborted = 8
        const val completed = 9
    }

    companion object {
        @JvmField val pending = JobState(Code.pending, "pending")
        @JvmField val pendingHeld = JobState(Code.pendingHeld, "pending-held")
        @JvmField val processing = JobState(Code.processing, "processing")
        @JvmField val processingStopped = JobState(Code.processingStopped, "processing-stopped")
        @JvmField val canceled = JobState(Code.canceled, "canceled")
        @JvmField val aborted = JobState(Code.aborted, "aborted")
        @JvmField val completed = JobState(Code.completed, "completed")

        @JvmField val Encoder = EnumType.Encoder(JobState::class.java) { code, name ->
            JobState(code, name)
        }
    }
}
