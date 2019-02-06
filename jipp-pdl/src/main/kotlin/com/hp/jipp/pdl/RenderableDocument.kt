// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import com.hp.jipp.model.OutputBin
import com.hp.jipp.model.Sides

/** A document, consisting of a collection of [RenderablePage] objects. */
abstract class RenderableDocument : Iterable<RenderablePage> {
    /** Dots-per-inch used when rendering this document. */
    abstract val dpi: Int

    /** Return a new [RenderableDocument] from this one after transforming its pages. */
    fun mapPages(
        transform: (RenderableDocument) -> Iterable<RenderablePage>
    ): RenderableDocument =
        let { doc ->
            object : RenderableDocument() {
                override val dpi = doc.dpi
                private val pages = transform(doc)
                override fun iterator() = pages.iterator()
            }
        }

    /** Return a document with any necessary page insertions or re-orderings for two-sided output. */
    fun handleSides(settings: OutputSettings) =
        when {
            settings.sides == Sides.oneSided -> this
            else -> handleSidesExtraBlank().handleSidesStackingOrder(settings)
        }

    /** For a two-sided output document, return a document with an extra blank page added if necessary. */
    private fun handleSidesExtraBlank() =
        when {
            count().isOdd -> mapPages { it + listOf(it.last().blank()) }
            else -> this
        }

    /** For a two-sided output document, return a document with the correct stacking order. */
    private fun handleSidesStackingOrder(settings: OutputSettings) =
        when {
            settings.stackingOrder == PrinterOutputTray.StackingOrder.lastToFirst &&
                settings.outputBin == OutputBin.faceUp ->
                mapPages { it.reversed() }
            // NOTE: This behavior is required by WFDS but not actually expected by printers
//            settings.stackingOrder == PrinterOutputTray.StackingOrder.firstToLast &&
//                settings.outputBin == OutputBin.faceDown ->
//                mapPages { doc -> doc.toList().chunked(2).flatMap { it.reversed() } }
            else -> this
        }
}
