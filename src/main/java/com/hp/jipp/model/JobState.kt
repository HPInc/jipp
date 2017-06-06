package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/**
 * Job State values.

 * @see [RFC2911 Section 4.3.7](https://tools.ietf.org/html/rfc2911.section-4.3.7)
 */
data class JobState(override val name: String, override val code: Int) : Enum() {

    /** Return true if this state is the terminating state for a job  */
    val isFinal: Boolean
        get() = this == Canceled || this == Aborted || this == Completed

    override fun toString() = name

    class Type(name: String) : EnumType<JobState>(ENCODER, name)

    companion object {

        @JvmField val Pending = JobState("pending", 3)
        @JvmField val PendingHeld = JobState("pending-held", 4)
        @JvmField val Processing = JobState("processing", 5)
        @JvmField val ProcessingStopped = JobState("processing-stopped", 6)
        @JvmField val Canceled = JobState("canceled", 7)
        @JvmField val Aborted = JobState("aborted", 8)
        @JvmField val Completed = JobState("completed", 9)

        @JvmField val ENCODER = EnumType.Encoder(JobState::class.java, { name, code -> JobState(name, code) })
    }
}
