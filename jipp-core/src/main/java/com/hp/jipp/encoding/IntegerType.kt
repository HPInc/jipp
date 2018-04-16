// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.IOException

/** Attribute type for [Int] attributes. May also encode an enum type if it is not recognized. */
open class IntegerType(tag: Tag, override val name: String) : AttributeType<Int>(Encoder, tag) {

    constructor(name: String) : this(Tag.integerValue, name)

    companion object Encoder : SimpleEncoder<Int>("Integer") {
        const val INT_LENGTH = 4

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag): Int {
            input.takeLength(INT_LENGTH)
            return input.readInt()
        }

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: Int) {
            out.writeShort(INT_LENGTH)
            out.writeInt(value)
        }

        override fun valid(valueTag: Tag): Boolean {
            return Tag.integerValue == valueTag || Tag.enumValue == valueTag
        }
    }
}
