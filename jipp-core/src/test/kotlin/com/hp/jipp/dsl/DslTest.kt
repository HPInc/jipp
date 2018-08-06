package com.hp.jipp.dsl

import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.MediaSizes
import com.hp.jipp.pwg.JobTemplateGroup
import com.hp.jipp.pwg.Media
import com.hp.jipp.pwg.MediaCol
import com.hp.jipp.pwg.Operation
import com.hp.jipp.pwg.OperationGroup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class DslTest {
    private val uri = URI.create("ipp://192.168.0.101:631/ipp/print")
    private val mediaSize = MediaSizes.parse(Media.naLetter85X11In)!!

    @Test fun packetTest() {
        val packet = ippPacket(Operation.printJob) {
            operationAttributes {
                attr(OperationGroup.attributesCharset, "utf-8")
                attr(OperationGroup.attributesNaturalLanguage, "en")
                attr(OperationGroup.printerUri, uri)
                attr(OperationGroup.requestingUserName, "Test User")
            }
            jobAttributes {
                attr(JobTemplateGroup.mediaCol, MediaCol(mediaSize = mediaSize))
            }
        }
        val cycled = cycle(packet)

        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, OperationGroup.attributesCharset))
        assertEquals(mediaSize, cycled.getValue(Tag.jobAttributes, JobTemplateGroup.mediaCol)!!.mediaSize)
    }
}
