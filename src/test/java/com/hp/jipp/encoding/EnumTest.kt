package com.hp.jipp.encoding

import org.junit.Test

import org.junit.Assert.* // ktlint-disable no-wildcard-imports

import com.hp.jipp.encoding.Cycler.* // ktlint-disable no-wildcard-imports

import com.hp.jipp.util.KotlinTest
import org.hamcrest.CoreMatchers.* // ktlint-disable no-wildcard-imports

class EnumTest {
    /** An enumeration of possible printer states  */
    data class Sample internal constructor(override val name: String, override val code: Int) : Enum() {
        companion object {
            @JvmField val One = Sample("one", 1)
            @JvmField val Two = Sample("two", 2)
            @JvmField val Three = Sample("three", 3)

            // Cannot be reached
            private val Secret = Sample("secret", 4)

            // Use Enum.Factory for Java code coverage
            val ENCODER: EnumType.Encoder<Sample> = encoderOf(Sample::class.java, { name, code -> Sample(name, code) })
        }
    }

    internal var MySample = EnumType(Sample.ENCODER, "my-sample")

    @Test
    @Throws(Exception::class)
    fun sample() {
        assertEquals(listOf(Sample.One), cycle(MySample, MySample.of(Sample.One)).values)
        assertThat(Enum.allFrom<Sample>(Sample::class.java), hasItems(Sample.One, Sample.Two, Sample.Three))
    }

    @Test
    @Throws(Exception::class)
    fun custom() {
        val custom = Sample("Sample(x77)", 0x77)
        assertEquals(custom, cycle(MySample, MySample.of(custom)).getValue(0))
    }

    @Test
    @Throws(Exception::class)
    fun fetchFromGroup() {
        assertEquals(listOf(Sample.Two, Sample.Three),
                cycle(groupOf(Tag.jobAttributes, MySample.of(Sample.Two, Sample.Three))).getValues(MySample))
    }

    @Test
    @Throws(Exception::class)
    fun cover() {
        val encoder = MySample.enumEncoder
        KotlinTest.cover(encoder,
                encoder.copy(encoder.type, encoder.map, encoder.factory),
                encoder.copy("other", encoder.map, encoder.factory))
    }
}
