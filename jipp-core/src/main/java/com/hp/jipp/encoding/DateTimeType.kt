// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.util.Calendar
import java.util.TimeZone

/** An [AttributeType] for a DateTime value represented as a [Calendar] object. */
@Suppress("MagicNumber")
open class DateTimeType(name: String) : AttributeTypeImpl<Calendar>(name, Calendar::class.java) {
    /** An [AttributeType] for multiple DateTime values represented as [Calendar] objects. */
    class Set(name: String) : DateTimeType(name), AttributeSetType<Calendar> {
        override fun toString() = "DateTimeType.Set($name)"
    }

    override fun toString() = "DateTimeType($name)"

    companion object {
        private const val BYTE_MASK = 0xFF
        private const val CALENDAR_LENGTH = 11
        private fun Byte.toUint(): Int = this.toInt() and BYTE_MASK

        val codec = Codec<Calendar>(
            Tag.dateTime,
            {
                val bytes = readValueBytes()
                if (bytes.size != CALENDAR_LENGTH) {
                    throw ParseError(
                        "Invalid byte count " + bytes.size +
                            " for dateTime, must be $CALENDAR_LENGTH"
                    )
                }
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar[Calendar.YEAR] = (bytes[0].toInt() shl 8) + (bytes[1].toUint())
                calendar[Calendar.MONTH] = bytes[2].toUint() - 1
                calendar[Calendar.DAY_OF_MONTH] = bytes[3].toUint()
                calendar[Calendar.HOUR_OF_DAY] = bytes[4].toUint()
                calendar[Calendar.MINUTE] = bytes[5].toUint()
                calendar[Calendar.SECOND] = bytes[6].toUint()
                calendar[Calendar.MILLISECOND] = bytes[7].toUint() * 100
                val zoneString = String.format(
                    "GMT%s%02d%02d",
                    bytes[8].toInt().toChar(), // - or +
                    bytes[9].toUint(),
                    bytes[10].toUint()
                )
                calendar.timeZone = TimeZone.getTimeZone(zoneString)
                calendar
            },
            {
                writeShort(CALENDAR_LENGTH)
                writeShort(it[Calendar.YEAR])
                writeByte(it[Calendar.MONTH] + 1)
                writeByte(it[Calendar.DAY_OF_MONTH])
                writeByte(it[Calendar.HOUR_OF_DAY])
                writeByte(it[Calendar.MINUTE])
                writeByte(it[Calendar.SECOND])
                writeByte(it[Calendar.MILLISECOND] / 100)

                var zone = it.timeZone.rawOffset
                if (zone < 0) {
                    writeByte('-'.code)
                    zone = -zone
                } else {
                    writeByte('+'.code)
                }
                val offsetMinutes = zone / 1000 / 60
                writeByte(offsetMinutes / 60) // Hours
                writeByte(offsetMinutes % 60) // Minutes only
            }
        )
    }
}
