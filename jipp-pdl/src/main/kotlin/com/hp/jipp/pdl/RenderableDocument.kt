// © Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

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

    /**
     * Return a document with any necessary page insertions or re-orderings for two-sided output, padding if necessary
     * if `allowPadding` is true.
     */
    fun handleSides(settings: OutputSettings, allowPadding: Boolean) =
        when (settings.sides) {
            Sides.oneSided -> this
            else -> handleSidesExtraBlank(allowPadding)
        }.handleReversed(settings).handleCopies(settings.copies)

    companion object {
        /** For a two-sided output document, return a document with an extra blank page added if necessary. */
        private fun RenderableDocument.handleSidesExtraBlank(allowPadding: Boolean) =
            when {
                allowPadding && count().isOdd -> mapPages { it + listOf(it.last().blank()) }
                else -> this
            }

        /** For any document, return a document with the correct stacking order. */
        private fun RenderableDocument.handleReversed(settings: OutputSettings) =
            when {
                settings.reversed -> mapPages { it.reversed() }
                else -> this
            }

        /** For a document repeat by a certain number of copies. */
        private fun RenderableDocument.handleCopies(count: Int) =
            when {
                count <= 1 -> this
                else -> mapPages {
                    sequence {
                        (0 until count).forEach { _ ->
                            yieldAll(this@handleCopies)
                        }
                    }.asIterable()
                }
            }
    }
}
