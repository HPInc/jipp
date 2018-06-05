// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Keyword
import com.hp.jipp.encoding.KeywordType

/** A keyword attribute describing the action by which a printer may identify itself. */
class IdentifyAction(override val name: String) : Keyword() {

    /** An attribute type for attributes of this type. */
    class Type(name: String) : KeywordType<IdentifyAction>(Encoder, name)

    override fun toString() = name

    companion object {
        @JvmField val display = IdentifyAction("display")
        @JvmField val flash = IdentifyAction("flash")
        @JvmField val sound = IdentifyAction("sound")
        @JvmField val speak = IdentifyAction("speak")

        @JvmStatic
        val Encoder = KeywordType.encoderOf(IdentifyAction::class.java) { IdentifyAction(it) }

        /** Return a new [KeywordType] for a type with the specified name */
        @JvmStatic fun of(name: String) = KeywordType(Encoder, name)
    }
}
