// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/**
 * An operation code as found in request packets and elsewhere.
 *
 * @see [RFC2911 Section 5.2.2](https://tools.ietf.org/html/rfc2911.section-5.2.2)
 */
open class Operation(override val code: Int, override val name: String) : Code() {
    override fun toString() = name

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
    class Type(name: String) : EnumType<Operation>(ENCODER, name)

    companion object {
        private val PRIME = 17
        private val BIG_PRIME = 31

        @JvmField val printJob = Operation(0x0002, "Print-Job")
        @JvmField val printUri = Operation(0x0003, "Print-URI")
        @JvmField val validateJob = Operation(0x0004, "Validate-Job")
        @JvmField val createJob = Operation(0x0005, "Create-Job")
        @JvmField val sendDocument = Operation(0x0006, "Send-Document")
        @JvmField val sendUri = Operation(0x0007, "Send-URI")
        @JvmField val cancelJob = Operation(0x0008, "Cancel-Job")
        @JvmField val getJobAttributes = Operation(0x0009, "Get-Job-Attributes")
        @JvmField val getJobs = Operation(0x000A, "Get-Jobs")
        @JvmField val getPrinterAttributes = Operation(0x000B, "Get-Printer-Attributes")
        @JvmField val holdJob = Operation(0x000C, "Hold-Job")
        @JvmField val releaseJob = Operation(0x000D, "Release-Job")
        @JvmField val restartJob = Operation(0x000E, "Restart-Job")
        @JvmField val pausePrinter = Operation(0x0010, "Pause-Printer")
        @JvmField val resumePrinter = Operation(0x0011, "Resume-Printer")
        @JvmField val purgeJobs = Operation(0x0012, "Purge-Jobs")
        @JvmField val closeJob = Operation(0x003B, "Close-Job")
        @JvmField val identifyPrinter = Operation(0x003C, "Identify-Printer")

        /** The encoder for converting integers to Operation objects  */
        @JvmField val ENCODER = encoderOf(Operation::class.java, { code, name -> Operation(code, name) })
    }
}
