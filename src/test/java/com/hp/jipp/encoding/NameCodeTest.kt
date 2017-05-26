package com.hp.jipp.encoding

import org.junit.Test

import org.junit.Assert.*

import com.hp.jipp.encoding.Cycler.*

import com.google.common.collect.ImmutableList
import com.hp.jipp.util.KotlinTest
import org.hamcrest.CoreMatchers.*

class NameCodeTest {
    /** An enumeration of possible printer states  */
    class Sample internal constructor(override val name: String, override val code: Int) : NameCode() {
        companion object {

            // TODO: How to reflect on these?
            @JvmField val One = Sample("one", 1)
            @JvmField val Two = Sample("two", 2)
            @JvmField val Three = Sample("three", 3)

            // Cannot be reached
            private val Secret = Sample("secret", 4)

            val ENCODER: NameCodeType.Encoder<Sample> = NameCodeType.Encoder.of(
                    Sample::class.java, object : NameCode.Factory<Sample> {
                override fun of(name: String, code: Int): Sample {
                    return Sample(name, code)
                }
            })
        }
    }

    internal var MySample = NameCodeType(Sample.ENCODER, "my-sample")

    @Test
    @Throws(Exception::class)
    fun sample() {
        assertEquals(ImmutableList.of(Sample.One), cycle(MySample, MySample.of(Sample.One)).values)
        assertThat(NameCode.allFrom<Sample>(Sample::class.java), hasItems(Sample.One, Sample.Two, Sample.Three))
    }

    @Test
    @Throws(Exception::class)
    fun fetchFromGroup() {
        assertEquals(ImmutableList.of(Sample.Two, Sample.Three),
                cycle(AttributeGroup.of(Tag.JobAttributes, MySample.of(Sample.Two, Sample.Three))).getValues(MySample))
    }

    @Test
    @Throws(Exception::class)
    fun cover() {
        val encoder = MySample.nameCodeEncoder
        KotlinTest.cover(encoder,
                encoder.copy(encoder.type, encoder.map, encoder.factory),
                encoder.copy("other", encoder.map, encoder.factory))
    }
}
