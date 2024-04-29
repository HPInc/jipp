// Copyright 2019 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import org.junit.Assert.assertEquals
import org.junit.Test

class RenderableDocumentTest {
    private val doc = object : RenderableDocument() {
        override val dpi = 100
        val pages = listOf(TestPage(1), TestPage(2), TestPage(3))
        override fun iterator() = pages.iterator()
    }

    class TestPage(val num: Int) : RenderablePage(20, 20) {
        override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
            throw NotImplementedError()
        }
    }

    @Test fun `non-reversed, simplex`() {
        val settings = OutputSettings(
            sides = Sides.oneSided,
            reversed = false
        )
        assertEquals("123", doc.handleSides(settings, true).joinToString("") { it.getPageName() })
    }

    @Test fun `reversed, simplex`() {
        val settings = OutputSettings(
            sides = Sides.oneSided,
            reversed = true
        )
        assertEquals("321", doc.handleSides(settings, true).joinToString("") { it.getPageName() })
    }

    @Test fun `non-reversed, duplex`() {
        val settings = OutputSettings(
            sides = Sides.twoSidedShortEdge,
            reversed = false
        )
        assertEquals("123b", doc.handleSides(settings, true).joinToString("") { it.getPageName() })
    }

    @Test fun `non-reversed, duplex, no padding`() {
        val settings = OutputSettings(
            sides = Sides.twoSidedShortEdge,
            reversed = false
        )
        assertEquals("123", doc.handleSides(settings, false).joinToString("") { it.getPageName() })
    }

    @Test fun `reversed, duplex`() {
        val settings = OutputSettings(
            sides = Sides.twoSidedShortEdge,
            reversed = true
        )
        assertEquals("b321", doc.handleSides(settings, true).joinToString("") { it.getPageName() })
    }

    private fun RenderablePage.getPageName() =
        when {
            toString().contains("blank") -> "b"
            this is TestPage -> num.toString()
            else -> toString()
        }
}
