package com.hp.jipp.dsl

import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.encoding.IntOrIntRange
import com.hp.jipp.encoding.MediaSizes
import com.hp.jipp.encoding.Name
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.BindingType
import com.hp.jipp.model.Media
import com.hp.jipp.model.MediaCol
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Status
import com.hp.jipp.model.Types
import java.net.URI
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class DslTest {
    private val uri = URI.create("ipp://192.168.0.101:631/ipp/print")
    private val mediaSize = MediaSizes.parse(Media.naLetter8p5x11in)!!

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
            printerAttributes {
                attr(Types.bindingTypeSupported, BindingType.adhesive)
            }
            unsupportedAttributes {
                attr(Types.outputBin.noValue())
            }
        }
        val cycled = cycle(packet)

        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, Types.attributesCharset))
        assertEquals(mediaSize, cycled.getValue(Tag.jobAttributes, Types.mediaCol)!!.mediaSize)
        assertEquals(listOf(BindingType.adhesive), cycled.getValues(Tag.printerAttributes, Types.bindingTypeSupported))
        assertEquals(Types.outputBin.noValue(), cycled[Tag.unsupportedAttributes]?.get(Types.outputBin))
    }

    @Test fun intOrIntRange() {
        val packet = ippPacket(Status.successfulOk) {
            group(Tag.printerAttributes) {
                attr(Types.numberUpSupported, IntOrIntRange(5..6))
            }
        }
        Assert.assertNotEquals(listOf<IntOrIntRange>(),
            packet.getValues(Tag.printerAttributes, Types.numberUpSupported))
    }

    @Test fun multiGroup() {
        val packet = ippPacket(Operation.printJob) {
            operationAttributes {
                attr(Types.attributesCharset, "utf-8")
                attr(Types.attributesNaturalLanguage, "en")
            }
            operationAttributes {
                attr(Types.printerUri, uri)
                attr(Types.attributesCharset, "utf-16") // replace extant
                attr(Types.requestingUserName, "Test User")
            }
        }
        // Only a single group is added
        assertEquals(1, packet.attributeGroups.size)
        assertEquals(Name("Test User"), packet.getValue(Tag.operationAttributes, Types.requestingUserName))

        // Earlier entries are replaced
        assertEquals("utf-16", packet.getValue(Tag.operationAttributes, Types.attributesCharset))

        // Order is preserved
        assertEquals(listOf(Types.attributesCharset, Types.attributesNaturalLanguage, Types.printerUri,
            Types.requestingUserName), packet[Tag.operationAttributes]!!.map { it.type })
    }
}
