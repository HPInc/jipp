// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for [Int] attributes. May also encode an enum type if it is not recognized. */
open class IntegerType(tag: Tag, override val name: String) : AttributeType<Int>(IntegerType.ENCODER, tag) {

    constructor(name: String) : this(Tag.integerValue, name)

    companion object {
        private const val TYPE_NAME = "Integer"

        @JvmField val ENCODER: SimpleEncoder<Int> = object : SimpleEncoder<Int>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): Int {
                input.takeLength(INT_LEN)
                return input.readInt()
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: Int) {
                out.writeShort(INT_LEN)
                out.writeInt(value)
            }

            override fun valid(valueTag: Tag): Boolean {
                return Tag.integerValue == valueTag || Tag.enumValue == valueTag
            }
        }
    }
}
