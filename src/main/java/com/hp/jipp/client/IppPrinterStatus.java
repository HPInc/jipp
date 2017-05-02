package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.PrinterState;

import java.io.IOException;
import java.util.List;

// All interesting status fields of a printer
@AutoValue
public abstract class IppPrinterStatus {

    static IppPrinterStatus of(AttributeGroup attributes) throws IOException {
        Optional<PrinterState> state = attributes.getValue(Attributes.PrinterState);
        List<String> reasons = attributes.getValues(Attributes.PrinterStateReasons);
        Optional<String> message = attributes.getValue(Attributes.PrinterStateMessage);
        if (!state.isPresent()) throw new IOException("Missing " + Attributes.PrinterState.getName());
        return new AutoValue_IppPrinterStatus(state.get(), reasons, message);
    }

    abstract PrinterState getState();

    abstract List<String> getReasons();

    abstract Optional<String> getMessage();

    public String toString() {
        return "Printer{state=" + getState().getName() +
                (getReasons().isEmpty() ? "" : " r=" + getReasons()) +
                (getMessage().isPresent() ? " m=" + getMessage().get() : "");
    }
}
