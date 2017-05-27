package com.hp.jipp.client

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.model.Attributes
import com.hp.jipp.model.PrinterState
import org.jetbrains.annotations.Nullable

import java.io.IOException

// All interesting status fields of a printer
data class PrinterStatus(val state: PrinterState, val reasons: List<String>, @Nullable val message: String?) {

    override fun toString(): String {
        return "Printer{state=" + state.name +
                (if (reasons.isEmpty()) "" else " r=" + reasons) +
                (if (message == null) "" else " m=" + message) +
                "}"
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic fun of(attributes: AttributeGroup): PrinterStatus {
            val state = attributes.getValue(Attributes.PrinterState)
            val reasons = attributes.getValues(Attributes.PrinterStateReasons)
            val message = attributes.getValue(Attributes.PrinterStateMessage)
            if (state == null) throw IOException("Missing " + Attributes.PrinterState.name)
            return PrinterStatus(state, reasons, message)
        }
    }
}
