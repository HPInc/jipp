package com.hp.jipp.model

import com.hp.jipp.encoding.NameCodeType
import com.hp.jipp.encoding.NameCode

/**
 * An operation code as found in request packets and elsewhere.

 * @see [RFC2911 Section 5.2.2](https://tools.ietf.org/html/rfc2911.section-5.2.2)
 */
data class Operation(override val name: String, override val code: Int) : Code() {
    override fun toString() = name

    companion object {
        @JvmField val PrintJob = Operation("Print-Job", 0x0002)
        @JvmField val PrintUri = Operation("Print-URI", 0x0003)
        @JvmField val ValidateJob = Operation("Validate-Job", 0x0004)
        @JvmField val CreateJob = Operation("Create-Job", 0x0005)
        @JvmField val SendDocument = Operation("Send-Document", 0x0006)
        @JvmField val SendUri = Operation("Send-URI", 0x0007)
        @JvmField val CancelJob = Operation("Cancel-Job", 0x0008)
        @JvmField val GetJobAttributes = Operation("Get-Job-Attributes", 0x0009)
        @JvmField val GetJobs = Operation("Get-Jobs", 0x000A)
        @JvmField val GetPrinterAttributes = Operation("Get-Printer-Attributes", 0x000B)
        @JvmField val HoldJob = Operation("Hold-Job", 0x000C)
        @JvmField val ReleaseJob = Operation("Release-Job", 0x000D)
        @JvmField val RestartJob = Operation("Restart-Job", 0x000E)
        @JvmField val PausePrinter = Operation("Pause-Printer", 0x0010)
        @JvmField val ResumePrinter = Operation("Resume-Printer", 0x0011)
        @JvmField val PurgeJobs = Operation("Purge-Jobs", 0x0012)
        @JvmField val CloseJob = Operation("Close-Job", 0x003B)
        @JvmField val IdentifyPrinter = Operation("Identify-Printer", 0x003C)

        /** The encoder for converting integers to Operation objects  */
        @JvmField val ENCODER: NameCodeType.Encoder<Operation> = NameCodeType.Encoder.of(
                Operation::class.java, object : NameCode.Factory<Operation> {
            override fun of(name: String, code: Int) = Operation(name, code)
        })

        /** Create and return a [NameCodeType] based on this NameCode  */
        fun typeOf(attributeName: String): NameCodeType<Operation> {
            return NameCodeType(ENCODER, attributeName)
        }
    }
}
