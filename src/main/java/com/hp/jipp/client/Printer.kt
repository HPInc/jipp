package com.hp.jipp.client

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.model.Attributes

import java.net.URI
import java.util.UUID

data class Printer(val uuid: UUID, val uri: URI, val attributes: AttributeGroup) {

    // List<MediaSize> getMediaSizesSupported();

    // List<MediaSize> getMediaSizeReady();

    /** Return the printer's "info" field or a blank string  */
    val info: String
        get() = attributes.getValue(Attributes.PrinterInfo) ?: ""

    override fun toString(): String {
        return "Printer{uri=" + uri + (if (info.isEmpty()) "" else " info=" + info) + "}"
    }
}
