// © Copyright 2017 - 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A machine-readable integer code paired with a human-readable name.
 *
 * Note: native Java enums are not used because they cannot be extended at runtime to accept unrecognized values.
 */
abstract class Enum {

    abstract val code: Int

    abstract val name: String

    override fun toString() = "$name($code)"

    companion object {
        /** Convert a List of T into a Map of integer codes to T, where T is a Enum subclass. */
        fun <T : Enum> toCodeMap(nameCodes: Iterable<T>): Map<Int, T> =
            nameCodes.map { it.code to it }.toMap()
    }
}
