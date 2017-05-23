package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
data class Tag(override val name: String, override val code: Int) : NameCode() {

    /** Write this tag to the output stream  */
    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        out.writeByte(code.toByte().toInt())
    }

    /** Return true if this tag is a delimiter tag  */
    val isDelimiter: Boolean
        get() = code in 0x01..0x0F

    override fun toString() = name

    companion object {

        // TODO: camelcase all of these

        // Delimiter tags
        @JvmField val OperationAttributes = Tag("operation-attributes", 0x01)
        @JvmField val JobAttributes = Tag("job-attributes", 0x02)
        @JvmField val EndOfAttributes = Tag("end-of-attributes", 0x03)
        @JvmField val PrinterAttributes = Tag("printer-attributes", 0x04)
        @JvmField val UnsupportedAttributes = Tag("unsupported-attributes", 0x05)

        @JvmField val Unsupported = Tag("unsupported", 0x10)
        @JvmField val Unknown = Tag("unknown", 0x12)
        @JvmField val NoValue = Tag("no-value", 0x13)

        // Integer values
        @JvmField val IntegerValue = Tag("integer", 0x21)
        @JvmField val BooleanValue = Tag("boolean", 0x22)
        @JvmField val EnumValue = Tag("enum", 0x23)

        // Octet-string values
        @JvmField val OctetString = Tag("octetString", 0x30)
        @JvmField val DateTime = Tag("dateTime", 0x31)
        @JvmField val Resolution = Tag("resolution", 0x32)
        @JvmField val RangeOfInteger = Tag("rangeOfInteger", 0x33)
        @JvmField val BeginCollection = Tag("begCollection", 0x34)
        @JvmField val TextWithLanguage = Tag("textWithLanguage", 0x35)
        @JvmField val NameWithLanguage = Tag("nameWithLanguage", 0x36)
        @JvmField val EndCollection = Tag("endCollection", 0x37)

        // Character-string values
        @JvmField val TextWithoutLanguage = Tag("textWithoutLanguage", 0x41)
        @JvmField val NameWithoutLanguage = Tag("nameWithoutLanguage", 0x42)
        @JvmField val Keyword = Tag("keyword", 0x44)
        @JvmField val Uri = Tag("uri", 0x45)
        @JvmField val UriScheme = Tag("uriScheme", 0x46)
        @JvmField val Charset = Tag("charset", 0x47)
        @JvmField val NaturalLanguage = Tag("naturalLanguage", 0x48)
        @JvmField val MimeMediaType = Tag("mimeMediaType", 0x49)
        @JvmField val MemberAttributeName = Tag("memberAttrName", 0x4A)

        private val all: Collection<Tag> = NameCode.allFrom(Tag::class.java)
        private val codeMap = NameCode.toCodeMap(all)

        @JvmStatic fun get(code: Int) = codeMap[code] ?: Tag("tag(x%x)".format(code), code)

        /** Read and return a tag from the input stream  */
        @Throws(IOException::class)
        fun read(input: DataInputStream): Tag = get(input.readByte().toInt())
    }
}
