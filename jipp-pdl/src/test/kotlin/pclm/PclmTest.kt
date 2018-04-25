package pclm

import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.pclm.PclmCapabilities
import com.hp.jipp.pdl.pclm.PclmWriter
import util.ByteWindow
import util.RandomDocument

class PclmTest {

    @Test
    fun validateGeneratedPclm() {
        cyclePclm(PclmCapabilities(32, true))
    }

    @Test
    fun validateGeneratedPclmBw() {
        cyclePclm(PclmCapabilities(32, false))
    }

    @Test
    fun pixelsToPoints() {
        assertEquals(72.0, RenderablePage.pixelsToPoints(300, RenderablePage.pointsToPixels(300, 72.0)), 0.0001)
    }

    private fun cyclePclm(caps: PclmCapabilities) {
        // Use a tall enough page so that we're assured there will be at least one blank area
        val randomDocument = RandomDocument(12345L, 2, 72.0, 150.0)
        val bytesOut = ByteArrayOutputStream()
        PclmWriter(bytesOut, caps, 300).use {
            it.write(randomDocument)
        }

        // Ensure that the resulting PCLM follows all rules we have identified
        validatePclm(bytesOut.toByteArray())
    }

    private fun validatePclm(bytes: ByteArray) {
        ByteWindow(bytes).validatePclm()
    }
}
