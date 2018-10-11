package pclm

import com.hp.jipp.pdl.pclm.PclmCapabilities
import com.hp.jipp.pdl.pclm.PclmWriter
import org.junit.Test
import util.ByteWindow
import util.RandomDocument
import java.io.ByteArrayOutputStream

class PclmTest {

    @Test
    fun validateGeneratedPclm() {
        cyclePclm(PclmCapabilities(32, true))
    }

    @Test
    fun validateGeneratedPclmBw() {
        cyclePclm(PclmCapabilities(32, false))
    }

    private fun cyclePclm(caps: PclmCapabilities) {
        // Use a tall enough page so that we're assured there will be at least one blank area
        val randomDocument = RandomDocument(12345L, 2, 72.0, 150.0, 300)
        val bytesOut = ByteArrayOutputStream()
        PclmWriter(bytesOut, caps).use {
            it.write(randomDocument)
        }

        // Ensure that the resulting PCLM follows all rules we have identified
        validatePclm(bytesOut.toByteArray())
    }

    private fun validatePclm(bytes: ByteArray) {
        ByteWindow(bytes).validatePclm()
    }
}
