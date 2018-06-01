// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Describes a printing resolution.

 * See [RFC2911 Section 4.1.15](https://tools.ietf.org/html/rfc2911.section-4.1.15)
 */
data class Resolution (val crossFeedResolution: Int, val feedResolution: Int, val unit: ResolutionUnit) {

    val x: Int
        get() = crossFeedResolution

    val y: Int
        get() = feedResolution

    override fun toString() = "${x}x$y $unit"
}
