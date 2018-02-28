package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.SimpleEncoder
import com.hp.jipp.encoding.StringType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.util.getStaticObjects
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

// TODO: Consider formal support for keyword | name(MAX)

data class JobPasswordEncryption(val name: String) {

    /** A media size type based solely on keyword values with width/height inferred  */
    class Type(name: String) : AttributeType<JobPasswordEncryption>(ENCODER, Tag.keyword, name)

    override fun toString() = "JobPasswordEncryption($name)"

    companion object {
        private const val TYPE_NAME = "JobPasswordEncryption"

        @JvmField val none = of("none")
        @JvmField val md2 = of("md2")
        @JvmField val md4 = of("md4")
        @JvmField val md5 = of("md5")
        @JvmField val sha = of("sha")

        private fun of(name: String) = JobPasswordEncryption(name)

        @JvmField val ENCODER = object : SimpleEncoder<JobPasswordEncryption>(TYPE_NAME) {

            private val all: Map<String, JobPasswordEncryption> = JobPasswordEncryption::class.java.getStaticObjects()
                    .filter { JobPasswordEncryption::class.java.isAssignableFrom(it.javaClass) }
                    .map { (it as JobPasswordEncryption).name to it }
                    .toMap()

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): JobPasswordEncryption {
                val name = StringType.ENCODER.readValue(input, valueTag)
                return all[name] ?: JobPasswordEncryption.of(name)
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: JobPasswordEncryption) {
                StringType.ENCODER.writeValue(out, value.name)
            }

            override fun valid(valueTag: Tag): Boolean {
                return Tag.keyword == valueTag || Tag.nameWithoutLanguage == valueTag
            }
        }
    }
}
