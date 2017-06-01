package com.hp.jipp.model

import com.hp.jipp.encoding.NameCode
import com.hp.jipp.encoding.NameCodeType

/**
 * Job State values.

 * @see [RFC2911 Section 4.3.7](https://tools.ietf.org/html/rfc2911.section-4.3.7)
 */
data class JobState(override val name: String, override val code: Int) : NameCode() {

    /** Return true if this state is the terminating state for a job  */
    val isFinal: Boolean
        get() = this === Canceled || this === Aborted || this === Completed

    override fun toString() = name

    companion object {

        @JvmField val Pending = JobState("pending", 3)
        @JvmField val PendingHeld = JobState("pending-held", 4)
        @JvmField val Processing = JobState("processing", 5)
        @JvmField val ProcessingStopped = JobState("processing-stopped", 6)
        @JvmField val Canceled = JobState("canceled", 7)
        @JvmField val Aborted = JobState("aborted", 8)
        @JvmField val Completed = JobState("completed", 9)

        @JvmField val ENCODER: NameCodeType.Encoder<JobState> = NameCodeType.Encoder.of(
                JobState::class.java, object : NameCode.Factory<JobState> {
            override fun of(name: String, code: Int) = JobState(name, code)
        })
    }
}
