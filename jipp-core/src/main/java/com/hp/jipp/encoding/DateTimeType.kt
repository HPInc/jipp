// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone

/**
 * An attribute type for DateAndTime octet strings as defined in [RFC2579](https://tools.ietf.org/html/rfc2579).
 */
@Suppress("MagicNumber")
class DateTimeType(override val name: String) : AttributeType<Calendar>(Encoder, Tag.dateTime) {

    companion object Encoder : SimpleEncoder<Calendar>("dateTime") {
        private const val BYTE_COUNT = 11

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag): Calendar {
            val bytes = input.readValueBytes()
            if (bytes.size != BYTE_COUNT) {
                throw ParseError("Invalid byte count " + bytes.size + " for dateTime, must be " + BYTE_COUNT)
            }
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar[Calendar.YEAR] = (bytes[0].toInt() shl 8) + (bytes[1].toUint())
            calendar[Calendar.MONTH] = bytes[2].toUint()
            calendar[Calendar.DAY_OF_MONTH] = bytes[3].toUint()
            calendar[Calendar.HOUR_OF_DAY] = bytes[4].toUint()
            calendar[Calendar.MINUTE] = bytes[5].toUint()
            calendar[Calendar.SECOND] = bytes[6].toUint()
            calendar[Calendar.MILLISECOND] = bytes[7].toUint() * 100
            val zoneString = String.format("GMT%s%02d%02d",
                bytes[8].toChar(), // - or +
                bytes[9].toUint(),
                bytes[10].toUint())
            calendar.timeZone = TimeZone.getTimeZone(zoneString)
            return calendar
        }

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: Calendar) {
            out.writeShort(BYTE_COUNT)
            out.writeShort(value[Calendar.YEAR])
            out.writeByte(value[Calendar.MONTH])
            out.writeByte(value[Calendar.DAY_OF_MONTH])
            out.writeByte(value[Calendar.HOUR_OF_DAY])
            out.writeByte(value[Calendar.MINUTE])
            out.writeByte(value[Calendar.SECOND])
            out.writeByte(value[Calendar.MILLISECOND] / 100)

            var zone = value.timeZone.rawOffset

            if (zone < 0) {
                out.writeByte('-'.toInt())
                zone = -zone
            } else {
                out.writeByte('+'.toInt())
            }
            val offsetMinutes = zone / 1000 / 60
            out.writeByte(offsetMinutes / 60) // Hours
            out.writeByte(offsetMinutes % 60) // Minutes only
        }

        override fun valid(valueTag: Tag) = Tag.dateTime == valueTag

        private fun Byte.toUint(): Int = this.toInt() and 0xFF
    }
}
