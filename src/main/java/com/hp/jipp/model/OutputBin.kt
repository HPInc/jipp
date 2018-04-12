// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Keyword
import com.hp.jipp.encoding.KeywordOrNameType
import com.hp.jipp.encoding.Tag

/** Output Bin as specified in PWG 5100.2 */
data class OutputBin(override val name: String) : Keyword() {

    /** A media size type based solely on keyword values with width/height inferred  */
    class Type(override val name: String) : AttributeType<OutputBin>(ENCODER, Tag.keyword)

    override fun toString() = name

    companion object {
        @JvmField val top = of("top")
        @JvmField val middle = of("middle")
        @JvmField val bottom = of("bottom")
        @JvmField val side = of("side")
        @JvmField val left = of("left")
        @JvmField val right = of("right")
        @JvmField val center = of("center")
        @JvmField val rear = of("rear")
        @JvmField val faceUp = of("face-up")
        @JvmField val faceDown = of("face-down")
        @JvmField val largeCapacity = of("large-capacity")
        @JvmField val myMailbox = of("my-mailbox")
        // Note: there are also stacker-N, mailbox-N, and tray-N posibilities

        private fun of(name: String) = OutputBin(name)

        @JvmField val ENCODER = KeywordOrNameType.encoderOf(OutputBin::class.java) {
            OutputBin(it)
        }
    }
}
