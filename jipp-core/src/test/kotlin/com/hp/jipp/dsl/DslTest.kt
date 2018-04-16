package com.hp.jipp.dsl

import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.MediaSize
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Types
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class DslTest {
    private val uri = URI.create("ipp://192.168.0.101:631/ipp/print")
    private val mediaSize = MediaSize.naLetter

    @Test fun packetTest() {
        val packet = ippPacket(Operation.printJob) {
            operationAttributes {
                attr(Types.attributesCharset, "utf-8")
                attr(Types.attributesNaturalLanguage, "en")
                attr(Types.printerUri, uri)
                attr(Types.requestingUserName, "Test User")
            }
            jobAttributes {
                col(Types.mediaCol) {
                    col(Types.mediaSize) {
                        attr(Types.xDimension, mediaSize.width)
                        attr(Types.yDimension, mediaSize.height)
                    }
                }
            }
        }
        val cycled = cycle(packet)

        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, Types.attributesCharset))
        assertEquals(mediaSize.width, cycled.getValue(Tag.jobAttributes, Types.mediaCol)!!
            [Types.mediaSize]!![0][Types.xDimension]!![0])
    }
}
