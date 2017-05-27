package com.hp.jipp.client

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.model.Attributes
import com.hp.jipp.model.JobState
import org.jetbrains.annotations.Nullable

import java.io.IOException

data class JobStatus(val state: JobState, val reasons: List<String>, @Nullable val message: String?,
                     val detailedMessages: List<String>) {

    override fun toString(): String {
        return "JobStatus{state=" + state.name +
                (if (reasons.isEmpty()) "" else " r=" + reasons) +
                (if (message == null) "m=$message" else "") +
                (if (detailedMessages.isEmpty()) "" else " x=" + detailedMessages) +
                "}"
    }

    companion object {
        @JvmField val AttributeNames: List<String> = listOf(
                    Attributes.JobState.name,
                    Attributes.JobStateReasons.name,
                    Attributes.JobStateMessage.name,
                    Attributes.JobDetailedStatusMessages.name)

        @Throws(IOException::class)
        @JvmStatic fun of(attributes: AttributeGroup): JobStatus {
            val state = attributes.getValue(Attributes.JobState) ?: throw IOException("Missing " + Attributes.JobState.name)
            return JobStatus(state,
                    attributes.getValues(Attributes.JobStateReasons),
                    attributes.getValue(Attributes.JobStateMessage),
                    attributes.getValues(Attributes.JobDetailedStatusMessages))
        }
    }
}
