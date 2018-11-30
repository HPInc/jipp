// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

/** A document, consisting of a collection of [RenderablePage] objects. */
interface RenderableDocument : Iterable<RenderablePage> {
    /** Dots-per-inch used when rendering this document. */
    val dpi: Int
}
