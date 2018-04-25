package pclm

import util.ByteWindow
import com.hp.jipp.doc.pclm.PclmCapabilities
import com.hp.jipp.doc.pclm.PclmWriter
import java.io.ByteArrayOutputStream
import org.junit.Test
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

    private fun cyclePclm(caps: PclmCapabilities) {

        val randomDocument = RandomDocument(12345L, 2, 72.0, 72.0)
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
