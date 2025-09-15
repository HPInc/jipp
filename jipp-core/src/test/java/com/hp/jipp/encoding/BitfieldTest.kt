// © Copyright 2020 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.Cycler.cycle
import org.junit.Assert.assertEquals
import org.junit.Test

class BitfieldTest {
    private val printerType = BitfieldType("printer-type")

    @Test
    fun `large value`() {
        assertEquals(printerType.of(0x08000000), cycle(printerType, printerType.of(0x08000000)))
    }

    @Test
    fun `larger value`() {
        assertEquals(printerType.of(0x48000000), cycle(printerType, printerType.of(0x48000000)))
    }

    @Test
    fun `native representation`() {
        assertEquals(
            UnknownAttribute("printer-type", 0x40000000),
            cycle(printerType.of(0x40000000))
        )
    }
}
