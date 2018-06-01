package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf
import org.junit.Test

import org.junit.Assert.* // ktlint-disable no-wildcard-imports

import com.hp.jipp.encoding.Cycler.* // ktlint-disable no-wildcard-imports

import org.hamcrest.CoreMatchers.* // ktlint-disable no-wildcard-imports

class EnumTest {
    /** An enumeration of possible printer states  */
    data class Sample constructor(override val code: Int, override val name: String) : Enum() {
        companion object {
            @JvmField val One = Sample(1, "one")
            @JvmField val Two = Sample(2, "two")
            @JvmField val Three = Sample(3, "three")

            // Cannot be reached
            private val Secret = Sample(4, "secret")

            // Use Enum.Factory for better Java code coverage
            val Encoder = EnumType.Encoder(Sample::class.java) { code, name -> Sample(code, name) }
        }
    }

    private var MySample = EnumType(Sample.Encoder, "my-sample")

    @Test
    @Throws(Exception::class)
    fun sample() {
        assertEquals(listOf(Sample.One), cycle(MySample, MySample.of(Sample.One)).values)
        assertThat(Enum.allFrom(Sample::class.java), hasItems(Sample.One, Sample.Two, Sample.Three))
    }

    @Test
    @Throws(Exception::class)
    fun custom() {
        val custom = Sample(0x77, "Unknown Sample")
        assertEquals(custom, cycle(MySample, MySample.of(custom))[0])
    }

    @Test
    @Throws(Exception::class)
    fun fetchFromGroup() {
        assertEquals(listOf(Sample.Two, Sample.Three),
                cycle(groupOf(Tag.jobAttributes, MySample.of(Sample.Two, Sample.Three))).getValues(MySample))
    }
}
