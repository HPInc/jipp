package com.hp.jipp.encoding

import org.junit.Test

import org.junit.Assert.* // ktlint-disable no-wildcard-imports

import com.hp.jipp.encoding.Cycler.* // ktlint-disable no-wildcard-imports

class EnumTest {
    /** An enumeration of possible printer states  */
    data class Sample constructor(override val code: Int, override val name: String) : Enum() {
        class Type(name: String) : EnumType<Sample>(name, { get(it) })
        companion object {
            @JvmField val One = Sample(1, "one")
            @JvmField val Two = Sample(2, "two")
            @JvmField val Three = Sample(3, "three")
            @JvmField val all = listOf(One, Two, Three).map { it.code to it }.toMap()
            operator fun get(value: Int) =
                all[value] ?: Sample(value, "???")
        }
    }

    private var mySample = Sample.Type("my-sample")

    @Test
    @Throws(Exception::class)
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
        assertEquals(listOf(Sample.Two, Sample.Three),
                cycle(AttributeGroup(Tag.jobAttributes, mySample.of(Sample.Two, Sample.Three)))[mySample])
    }
}
