// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.codec
import com.hp.jipp.encoding.AttributeGroup.Companion.toUint
import com.hp.jipp.util.ParseError
import java.util.* // ktlint-disable

/** An attribute type based on [Calendar] type. */
@Suppress("MagicNumber")
open class DateTimeType(override val name: String) : AttributeType<Calendar> {

    override fun coerce(value: Any) =
        value as? Calendar

    companion object {
        val codec = codec<Calendar>(Tag.dateTime, {
            val bytes = readValueBytes()
            if (bytes.size != AttributeGroup.CALENDAR_LENGTH) {
                throw ParseError("Invalid byte count " + bytes.size +
                    " for dateTime, must be ${AttributeGroup.CALENDAR_LENGTH}")
            }
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar[Calendar.YEAR] = (bytes[0].toInt() shl 8) + (bytes[1].toUint())
            calendar[Calendar.MONTH] = bytes[2].toUint() - 1
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
            calendar
        }, {
            writeShort(AttributeGroup.CALENDAR_LENGTH)
            writeShort(it[Calendar.YEAR])
            writeByte(it[Calendar.MONTH] + 1)
            writeByte(it[Calendar.DAY_OF_MONTH])
            writeByte(it[Calendar.HOUR_OF_DAY])
            writeByte(it[Calendar.MINUTE])
            writeByte(it[Calendar.SECOND])
            writeByte(it[Calendar.MILLISECOND] / 100)

            var zone = it.timeZone.rawOffset
            if (zone < 0) {
                writeByte('-'.toInt())
                zone = -zone
            } else {
                writeByte('+'.toInt())
            }
            val offsetMinutes = zone / 1000 / 60
            writeByte(offsetMinutes / 60) // Hours
            writeByte(offsetMinutes % 60) // Minutes only
        })
    }
}
