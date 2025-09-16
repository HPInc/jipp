// © Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf
import com.hp.jipp.encoding.Cycler.cycle
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumTest {
    /** An enumeration of possible printer states  */
    data class Sample constructor(override val code: Int, override val name: String) : Enum() {
        // class Type(name: String) : EnumType<Sample>(name, { get(it) })
        class SetType(name: String) : EnumType.Set<Sample>(name, { get(it) })

        companion object {
            @JvmField val One = Sample(1, "one")
            @JvmField val Two = Sample(2, "two")
            @JvmField val Three = Sample(3, "three")
            @JvmField val all = listOf(One, Two, Three).map { it.code to it }.toMap()
            operator fun get(value: Int) =
                all[value] ?: Sample(value, "???")
        }
    }

    private var mySample = Sample.SetType("my-sample")

    @Test @Throws(Exception::class)
    fun sample() {
        assertEquals(listOf(Sample.One), cycle(mySample, mySample.of(Sample.One)))
    }

    @Test
    @Throws(Exception::class)
    fun custom() {
        val custom = Sample(0x77, "???")
        assertEquals(custom, cycle(mySample, mySample.of(custom)).getValue())
    }

    @Test
    @Throws(Exception::class)
    fun fetchFromGroup() {
        assertEquals(
            listOf(Sample.Two, Sample.Three),
            cycle(groupOf(Tag.jobAttributes, mySample.of(listOf(Sample.Two, Sample.Three))))[mySample]
        )
    }
}
