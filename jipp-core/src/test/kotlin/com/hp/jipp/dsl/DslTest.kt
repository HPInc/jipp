package com.hp.jipp.dsl

import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.encoding.Tag
import com.hp.jipp.encoding.MediaSizes
import com.hp.jipp.model.Types
import com.hp.jipp.model.Media
import com.hp.jipp.model.MediaCol
import com.hp.jipp.model.Operation
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class DslTest {
    private val uri = URI.create("ipp://192.168.0.101:631/ipp/print")
    private val mediaSize = MediaSizes.parse(Media.naLetter85X11In)!!

    @Test fun packetTest() {
        val packet = ippPacket(Operation.printJob) {
            operationAttributes {
                attr(Types.attributesCharset, "utf-8")
                attr(Types.attributesNaturalLanguage, "en")
                attr(Types.printerUri, uri)
                attr(Types.requestingUserName, "Test User")
            }
            jobAttributes {
                attr(Types.mediaCol, MediaCol(mediaSize = mediaSize))
            }
        }
        val cycled = cycle(packet)

        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, Types.attributesCharset))
        assertEquals(mediaSize, cycled.getValue(Tag.jobAttributes, Types.mediaCol)!!.mediaSize)
    }
}
