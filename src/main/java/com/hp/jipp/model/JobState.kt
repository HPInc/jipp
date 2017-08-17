package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/**
 * Job State values.

 * @see [RFC2911 Section 4.3.7](https://tools.ietf.org/html/rfc2911.section-4.3.7)
 */
data class JobState(override val name: String, override val code: Int) : Enum() {

    /** Return true if this state is the terminating state for a job  */
    val isFinal: Boolean
        get() = this == canceled || this == aborted || this == completed

    /** The attribute type for a [JobState] attribute */
    class Type(name: String) : EnumType<JobState>(ENCODER, name)

    companion object {
        @JvmField val pending = JobState("pending", 3)
        @JvmField val pendingHeld = JobState("pending-held", 4)
        @JvmField val processing = JobState("processing", 5)
        @JvmField val processingStopped = JobState("processing-stopped", 6)
        @JvmField val canceled = JobState("canceled", 7)
        @JvmField val aborted = JobState("aborted", 8)
        @JvmField val completed = JobState("completed", 9)

        @JvmField val ENCODER = encoderOf(JobState::class.java, { name, code -> JobState(name, code) })
    }
}
