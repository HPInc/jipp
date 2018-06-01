// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.EnumType

/**
 * An operation code as found in request packets and elsewhere.
 *
 * See [RFC2911 Section 5.2.2](https://tools.ietf.org/html/rfc2911.section-5.2.2)
 */
data class Operation(override val code: Int, override val name: String) : Code() {

    override fun toString() = super.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Operation
        if (name != other.name) return false
        if (code != other.code) return false
        return true
    }

    override fun hashCode(): Int {
        var hash = PRIME
        hash = hash * BIG_PRIME + name.hashCode()
        hash = hash * BIG_PRIME + code.hashCode()
        return hash
    }

    /** The [EnumType] for [Operation] attributes. */
    class Type(name: String) : EnumType<Operation>(Encoder, name)

    /** Raw codes which may be used for direct comparisons */
    object Code {
        const val printJob = 0x0002
        const val printUri = 0x0003
        const val validateJob = 0x0004
        const val createJob = 0x0005
        const val sendDocument = 0x0006
        const val sendUri = 0x0007
        const val cancelJob = 0x0008
        const val getJobAttributes = 0x0009
        const val getJobs = 0x000A
        const val getPrinterAttributes = 0x000B
        const val holdJob = 0x000C
        const val releaseJob = 0x000D
        const val restartJob = 0x000E
        const val pausePrinter = 0x0010
        const val resumePrinter = 0x0011
        const val purgeJobs = 0x0012
        const val closeJob = 0x003B
        const val identifyPrinter = 0x003C
    }

    companion object {
        private val PRIME = 17
        private val BIG_PRIME = 31

        @JvmField val printJob = Operation(Code.printJob, "Print-Job")
        @JvmField val printUri = Operation(Code.printUri, "Print-URI")
        @JvmField val validateJob = Operation(Code.validateJob, "Validate-Job")
        @JvmField val createJob = Operation(Code.createJob, "Create-Job")
        @JvmField val sendDocument = Operation(Code.sendDocument, "Send-Document")
        @JvmField val sendUri = Operation(Code.sendUri, "Send-URI")
        @JvmField val cancelJob = Operation(Code.cancelJob, "Cancel-Job")
        @JvmField val getJobAttributes = Operation(Code.getJobAttributes, "Get-Job-Attributes")
        @JvmField val getJobs = Operation(Code.getJobs, "Get-Jobs")
        @JvmField val getPrinterAttributes = Operation(Code.getPrinterAttributes, "Get-Printer-Attributes")
        @JvmField val holdJob = Operation(Code.holdJob, "Hold-Job")
        @JvmField val releaseJob = Operation(Code.releaseJob, "Release-Job")
        @JvmField val restartJob = Operation(Code.restartJob, "Restart-Job")
        @JvmField val pausePrinter = Operation(Code.pausePrinter, "Pause-Printer")
        @JvmField val resumePrinter = Operation(Code.resumePrinter, "Resume-Printer")
        @JvmField val purgeJobs = Operation(Code.purgeJobs, "Purge-Jobs")
        @JvmField val closeJob = Operation(Code.closeJob, "Close-Job")
        @JvmField val identifyPrinter = Operation(Code.identifyPrinter, "Identify-Printer")

        /** The encoder for converting integers to Operation objects  */
        @JvmField val Encoder = EnumType.Encoder(Operation::class.java) { code, name ->
            Operation(code, name)
        }
    }
}
