package com.hp.jipp.client

import com.hp.jipp.encoding.AttributeGroup

import java.io.IOException

/**
 * @param attributes Full job attributes as originally received from print server
 * @param status Current status of job (may differ from contents of attributes)
 */
data class Job(val id: Int, val printer: Printer, val jobRequest: JobRequest?, val attributes: AttributeGroup,
               val status: JobStatus) {

    constructor(id: Int, printer: Printer, jobAttributes: AttributeGroup): this(id, printer, null, jobAttributes,
                JobStatus.of(jobAttributes))

    constructor(id: Int, jobRequest: JobRequest, jobAttributes: AttributeGroup): this(id, jobRequest.printer,
            jobRequest, jobAttributes, JobStatus.of(jobAttributes))

    /** Returns a new Job containing more current JobAttributes from the enclosed response packet  */
    @Throws(IOException::class)
    fun withAttributes(newAttributes: AttributeGroup) = copy(attributes = newAttributes,
            status = JobStatus.of(newAttributes))

    /** Returns a new Job containing the same attributes but a new JobStatus */
    fun withStatus(jobStatus: JobStatus) = copy(status = jobStatus)

    companion object {

        @Throws(IOException::class)
        fun of(id: Int, printer: Printer, jobAttributes: AttributeGroup): Job {
            return Job(id, printer, null, jobAttributes,
                    JobStatus.of(jobAttributes))
        }

    }
}
