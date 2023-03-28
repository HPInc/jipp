package com.hp.jipp.dsl

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf
import com.hp.jipp.encoding.AttributeGroup.Companion.mutableGroupOf
import com.hp.jipp.encoding.Cycler.cycle
import com.hp.jipp.encoding.IntOrIntRange
import com.hp.jipp.encoding.IppPacket
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_REQUEST_ID
import com.hp.jipp.encoding.KeywordOrName
import com.hp.jipp.encoding.MediaSizes
import com.hp.jipp.encoding.MediaSizes.toMediaColDatabaseMediaSize
import com.hp.jipp.encoding.Name
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.BindingType
import com.hp.jipp.model.Media
import com.hp.jipp.model.MediaCol
import com.hp.jipp.model.MediaColDatabase
import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.MediaType
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Status
import com.hp.jipp.model.Types
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

/** Test for some Kotlin-specific and some deprecated elements */
class DslTest {
    private val uri = URI.create("ipp://192.168.0.101:631/ipp/print")
    private val mediaSize = MediaSizes.parse(Media.naLetter8p5x11in)!!

    @Test
    fun `java-style build`() {
        val packet = IppPacket.printJob(uri)
            .putOperationAttributes(Types.requestingUserName.of("Test User"))
            .setOperation(Operation.createJob)
            .putJobAttributes(
                Types.mediaCol.of(MediaCol(mediaSize = mediaSize)),
                Types.documentMessage.of("A description of the document")
            )
            .putPrinterAttributes(Types.bindingTypeSupported.of(BindingType.adhesive))
            .putUnsupportedAttributes(Types.outputBin.noValue())
            .putSubscriptionAttributes(Types.notifyAttributes.noValue())
            .putEventNotificationAttributes(Types.notifyEvents.noValue())
            .putDocumentAttributes(Types.compression.noValue())
            .build()

        val cycled = cycle(packet)
        assertEquals(Operation.createJob, cycled.operation)
        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, Types.attributesCharset))
        assertEquals(mediaSize, cycled.getValue(Tag.jobAttributes, Types.mediaCol)!!.mediaSize)
        assertEquals(listOf(BindingType.adhesive), cycled.getValues(Tag.printerAttributes, Types.bindingTypeSupported))
        assertEquals(Types.outputBin.noValue(), cycled[Tag.unsupportedAttributes]?.get(Types.outputBin))
        assertEquals("A description of the document", cycled[Tag.jobAttributes]?.getValue(Types.documentMessage)?.value)
    }

    @Test
    @Suppress("DEPRECATION")
    fun packetTest() {
        val packet = ippPacket(Operation.printJob) {
            operation = Operation.getJobAttributes
            operationAttributes {
                attr(Types.attributesCharset, "utf-8")
                attr(Types.attributesNaturalLanguage, "en")
                attr(Types.printerUri, uri)
                attr(Types.requestingUserName, "Test User")
            }
            jobAttributes {
                attr(Types.mediaCol, MediaCol(mediaSize = mediaSize))
                attr(Types.documentMessage, "A description of the document")
            }
            printerAttributes {
                attr(Types.bindingTypeSupported, BindingType.adhesive)
            }
            unsupportedAttributes {
                attr(Types.outputBin.noValue())
            }
            subscriptionAttributes {
                attr(Types.notifyAttributes.noValue())
            }
            eventNotificationAttributes {
                attr(Types.notifyEvents.noValue())
            }
            documentAttributes {
                attr(Types.compression.noValue())
            }
        }
        val cycled = cycle(packet)
        assertEquals(Operation.getJobAttributes, cycled.operation)
        assertEquals("utf-8", cycled.getValue(Tag.operationAttributes, Types.attributesCharset))
        assertEquals(mediaSize, cycled.getValue(Tag.jobAttributes, Types.mediaCol)!!.mediaSize)
        assertEquals(listOf(BindingType.adhesive), cycled.getValues(Tag.printerAttributes, Types.bindingTypeSupported))
        assertEquals(Types.outputBin.noValue(), cycled[Tag.unsupportedAttributes]?.get(Types.outputBin))
        assertEquals("A description of the document", cycled[Tag.jobAttributes]?.getValue(Types.documentMessage)?.value)
    }

    @Test
    fun setStatus() {
        val packet = IppPacket.Builder(Operation.printJob)
            .setStatus(Status.clientErrorCharsetNotSupported)
            .build()
        assertEquals(Status.clientErrorCharsetNotSupported, packet.status)
    }

    @Test
    fun intOrIntRange() {
        val packet = IppPacket.response(Status.successfulOk)
            .putPrinterAttributes(Types.numberUpSupported.of(5..6))
            .build()
        Assert.assertNotEquals(
            listOf<IntOrIntRange>(),
            packet.getValues(Tag.printerAttributes, Types.numberUpSupported)
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun `extend a group`() {
        val packet = ippPacket(Operation.printJob) {
            // We can get and set the status if we want to
            assertEquals(Operation.printJob.code, status.code)
            status = Status.clientErrorBadRequest
            operationAttributes {
                attr(Types.attributesCharset, "utf-8")
                attr(Types.attributesNaturalLanguage, "en")
            }
            extend(Tag.operationAttributes) {
                attr(Types.printerUri, uri)
                attr(Types.attributesCharset, "utf-16") // replace extant
                attr(Types.requestingUserName, "Test User")
            }
        }
        // Status and code go in the same place
        assertEquals(Status.clientErrorBadRequest.code, packet.code)

        // Only a single group is added
        assertEquals(1, packet.attributeGroups.size)
        assertEquals(Name("Test User"), packet.getValue(Tag.operationAttributes, Types.requestingUserName))

        // Earlier entries are replaced
        assertEquals("utf-16", packet.getValue(Tag.operationAttributes, Types.attributesCharset))

        // Order is preserved
        assertEquals(
            listOf(
                Types.attributesCharset, Types.attributesNaturalLanguage, Types.printerUri,
                Types.requestingUserName
            ),
            packet[Tag.operationAttributes]!!.map { it.type }
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun `modify a group`() {
        val packet = ippPacket(Operation.printJob) {
            // We can get and set the status if we want to
            assertEquals(Operation.printJob.code, status.code)
            status = Status.clientErrorBadRequest
            operationAttributes {
                // Try a variety of accessors
                this += Types.attributesCharset.of("utf-8")
                this += Types.attributesNaturalLanguage.of("en")
            }
            extend(Tag.operationAttributes) {
                add(Types.printerUri.of(uri))
                addAll(listOf(Types.attributesCharset.of("utf-16"))) // replace prior attributesCharset
                attr(listOf(Types.requestingUserName.of("Test User")))
            }
        }
        // Status and code go in the same place
        assertEquals(Status.clientErrorBadRequest.code, packet.code)

        // Only a single group is added
        assertEquals(1, packet.attributeGroups.size)
        assertEquals(Name("Test User"), packet.getValue(Tag.operationAttributes, Types.requestingUserName))

        // Earlier entries are replaced
        assertEquals("utf-16", packet.getValue(Tag.operationAttributes, Types.attributesCharset))

        // Order is preserved
        assertEquals(
            listOf(
                Types.attributesCharset, Types.attributesNaturalLanguage, Types.printerUri,
                Types.requestingUserName
            ),
            packet[Tag.operationAttributes]!!.map { it.type }
        )
    }

    @Test
    fun `extend a non-existent group`() {
        val packet = IppPacket.printJob(uri)
            // Extend a tag that's not there
            .putPrinterAttributes(Types.jobAccountId.of("25"))
            .build()

        assertEquals(Name("25"), packet.getValue(Tag.printerAttributes, Types.jobAccountId))
    }

    @Test
    fun `mess with packet fields`() {
        val packet = IppPacket.printJob(uri).apply {
            code = Operation.createJob.code
            versionNumber = 2000
            requestId++
        }.build()

        assertEquals(Operation.createJob, packet.operation)
        assertEquals(2000, packet.versionNumber)
        assertEquals(DEFAULT_REQUEST_ID + 1, packet.requestId)
    }

    @Test
    fun `mess with packet fields via builders`() {
        val packet = IppPacket.printJob(uri)
            .setCode(Operation.createJob.code)
            .setVersionNumber(2000)
            .setRequestId(101)
            .build()

        assertEquals(Operation.createJob, packet.operation)
        assertEquals(2000, packet.versionNumber)
        assertEquals(101, packet.requestId)
    }

    @Test
    fun `construct with status`() {
        val packet = IppPacket.Builder(Status.successfulOk, 2000, 101)
            .build()

        assertEquals(Status.successfulOk, packet.status)
        assertEquals(2000, packet.versionNumber)
        assertEquals(101, packet.requestId)
    }

    @Test
    fun `construct with operation`() {
        val packet = IppPacket.Builder(Operation.fetchJob).build()
        assertEquals(Operation.fetchJob, packet.operation)
    }

    @Test
    fun `coerce types from mutableGroups`() {
        val mediaType1 = MediaColDatabase().apply {
            mediaSize = MediaSizes.parse(Media.naLetter8p5x11in).toMediaColDatabaseMediaSize()
            mediaLeftMargin = 750
            mediaRightMargin = 750
            mediaBottomMargin = 750
            mediaTopMargin = 750
            mediaSource = KeywordOrName(MediaSource.main)
            mediaType = KeywordOrName(MediaType.stationery)
        }
        val packet = IppPacket(
            Status.successfulOk, 1234, groupOf(Tag.operationAttributes),
            groupOf(Tag.printerAttributes, Types.mediaColReady.of(mediaType1))
        )
        val printerAttributes = mutableGroupOf(Tag.printerAttributes, cycle(packet)[Tag.printerAttributes]!!)
        assertEquals(listOf(mediaType1), printerAttributes.getValues(Types.mediaColReady))
        assertEquals(mediaType1.mediaSize, printerAttributes.getValues(Types.mediaColReady).first().mediaSize)
    }
}
