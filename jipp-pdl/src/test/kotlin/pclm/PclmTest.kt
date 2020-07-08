// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pclm

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.pclm.PclmSettings
import com.hp.jipp.pdl.pclm.PclmWriter
import java.io.ByteArrayOutputStream
import java.io.File
import org.junit.Test
import util.ByteWindow
import util.RandomDocument

class PclmTest {

    @Test
    fun `validate PCLM`() {
        cyclePclm(PclmSettings(stripHeight = 32))
    }

    @Test
    fun validateGeneratedPclmBw() {
        cyclePclm(PclmSettings(stripHeight = 32, output = OutputSettings(colorSpace = ColorSpace.Grayscale)))
    }

    private fun cyclePclm(caps: PclmSettings) {
        // Use a tall enough page so that we're assured there will be at least one blank area
        val randomDocument = RandomDocument(12345L, 2, 283.46457, 419.52756, 300)
        val bytesOut = ByteArrayOutputStream()
        PclmWriter(bytesOut, caps).use {
            it.write(randomDocument)
        }
        bytesOut.toByteArray().inputStream().copyTo(File(".", "testout.pclm.pdf").outputStream())

        // Ensure that the resulting PCLM follows all rules we have identified
        validatePclm(bytesOut.toByteArray())
    }

    private fun validatePclm(bytes: ByteArray) {
        ByteWindow(bytes).validatePclm()
    }
}
