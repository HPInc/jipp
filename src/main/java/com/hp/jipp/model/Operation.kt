package com.hp.jipp.model

import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/**
 * An operation code as found in request packets and elsewhere.
 *
 * @see [RFC2911 Section 5.2.2](https://tools.ietf.org/html/rfc2911.section-5.2.2)
 */
data class Operation(override val name: String, override val code: Int) : Code() {
    override fun toString() = name

    /** The [EnumType] for [Operation] attributes. */
    class Type(name: String) : EnumType<Operation>(ENCODER, name)

    companion object {
        @JvmField val printJob = Operation("Print-Job", 0x0002)
        @JvmField val printUri = Operation("Print-URI", 0x0003)
        @JvmField val validateJob = Operation("Validate-Job", 0x0004)
        @JvmField val createJob = Operation("Create-Job", 0x0005)
        @JvmField val sendDocument = Operation("Send-Document", 0x0006)
        @JvmField val sendUri = Operation("Send-URI", 0x0007)
        @JvmField val cancelJob = Operation("Cancel-Job", 0x0008)
        @JvmField val getJobAttributes = Operation("Get-Job-Attributes", 0x0009)
        @JvmField val getJobs = Operation("Get-Jobs", 0x000A)
        @JvmField val getPrinterAttributes = Operation("Get-Printer-Attributes", 0x000B)
        @JvmField val holdJob = Operation("Hold-Job", 0x000C)
        @JvmField val releaseJob = Operation("Release-Job", 0x000D)
        @JvmField val restartJob = Operation("Restart-Job", 0x000E)
        @JvmField val pausePrinter = Operation("Pause-Printer", 0x0010)
        @JvmField val resumePrinter = Operation("Resume-Printer", 0x0011)
        @JvmField val purgeJobs = Operation("Purge-Jobs", 0x0012)
        @JvmField val closeJob = Operation("Close-Job", 0x003B)
        @JvmField val identifyPrinter = Operation("Identify-Printer", 0x003C)

        /** The encoder for converting integers to Operation objects  */
        @JvmField val ENCODER = encoderOf(Operation::class.java, { name, code -> Operation(name, code) })
    }
}
