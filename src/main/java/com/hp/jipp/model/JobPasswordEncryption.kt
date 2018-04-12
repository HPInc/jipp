// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Keyword
import com.hp.jipp.encoding.KeywordOrNameType
import com.hp.jipp.encoding.Tag

data class JobPasswordEncryption(override val name: String) : Keyword() {

    /** A media size type based solely on keyword values with width/height inferred  */
    class Type(override val name: String) : AttributeType<JobPasswordEncryption>(ENCODER, Tag.keyword)

    override fun toString() = name

    companion object {
        @JvmField val none = of("none")
        @JvmField val md2 = of("md2")
        @JvmField val md4 = of("md4")
        @JvmField val md5 = of("md5")
        @JvmField val sha = of("sha")

        private fun of(name: String) = JobPasswordEncryption(name)

        @JvmField val ENCODER = KeywordOrNameType.encoderOf(JobPasswordEncryption::class.java) {
            JobPasswordEncryption(it)
        }
    }
}
