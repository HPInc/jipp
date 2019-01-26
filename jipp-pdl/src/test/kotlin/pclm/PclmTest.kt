// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pclm

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.pclm.PclmSettings
import com.hp.jipp.pdl.pclm.PclmWriter
import org.junit.Test
import util.ByteWindow
import util.RandomDocument
import java.io.ByteArrayOutputStream

class PclmTest {

    @Test
    fun validateGeneratedPclm() {
        cyclePclm(PclmSettings(stripHeight = 32))
    }

    @Test
    fun validateGeneratedPclmBw() {
        cyclePclm(PclmSettings(stripHeight = 32, colorSpace = ColorSpace.Grayscale))
    }

    private fun cyclePclm(caps: PclmSettings) {
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
