import com.hp.jipp.model.OutputBin
import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.PrinterOutputTray
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

    @Test fun faceUpLastToFirst() {
        val settings = OutputSettings(sides = Sides.twoSidedShortEdge,
            stackingOrder = PrinterOutputTray.StackingOrder.lastToFirst,
            outputBin = OutputBin.faceUp)
        assertEquals("b321", doc.handleSides(settings).joinToString("") { it.getPageName() })
    }

    @Test fun faceUpFirstToLast() {
        val settings = OutputSettings(sides = Sides.twoSidedShortEdge,
            stackingOrder = PrinterOutputTray.StackingOrder.firstToLast,
            outputBin = OutputBin.faceUp)
        assertEquals("123b", doc.handleSides(settings).joinToString("") { it.getPageName() })
    }

    @Test fun faceDownLastToFirst() {
        val settings = OutputSettings(sides = Sides.twoSidedShortEdge,
            stackingOrder = PrinterOutputTray.StackingOrder.lastToFirst,
            outputBin = OutputBin.faceDown)
        assertEquals("123b", doc.handleSides(settings).joinToString("") { it.getPageName() })
    }

    @Test fun faceDownFirstToLast() {
        val settings = OutputSettings(sides = Sides.twoSidedShortEdge,
            stackingOrder = PrinterOutputTray.StackingOrder.firstToLast,
            outputBin = OutputBin.faceDown)
        // Was 21b3 as per WFDS but printers don't need this.
        assertEquals("123b", doc.handleSides(settings).joinToString("") { it.getPageName() })
    }

    @Test fun faceUpLastToFirstSimplex() {
        val settings = OutputSettings(sides = Sides.oneSided,
            stackingOrder = PrinterOutputTray.StackingOrder.lastToFirst,
            outputBin = OutputBin.faceUp)
        assertEquals("321", doc.handleSides(settings).joinToString("") { it.getPageName() })
    }

    private fun RenderablePage.getPageName() =
        when {
            toString().contains("blank") -> "b"
            this is TestPage -> num.toString()
            else -> toString()
        }
}
