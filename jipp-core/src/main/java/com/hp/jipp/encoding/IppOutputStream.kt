// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.IppInputStream.Companion.LENGTH_LENGTH
import java.io.DataOutputStream
import java.io.OutputStream

class IppOutputStream(outputStream: OutputStream) : DataOutputStream(outputStream) {
    /** Write a series of bytes to the output stream, prefixed by length. */
    fun writeValueBytes(bytes: ByteArray) {
        writeShort(bytes.size)
        write(bytes)
    }

    /** Write a string to the output stream, prefixed by length. */
    fun writeString(string: String) {
        writeValueBytes(string.toByteArray(Charsets.UTF_8))
    }

    /** Write a Tag to the output stream */
    fun writeTag(tag: Tag) {
        tag.write(this)
    }

    /** Return the length of this string as it would be encoded in the output stream. */
    fun stringLength(string: String) = LENGTH_LENGTH + string.toByteArray(Charsets.UTF_8).size
}
