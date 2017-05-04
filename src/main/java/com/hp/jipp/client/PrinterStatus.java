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
public abstract class PrinterStatus {

    static PrinterStatus of(AttributeGroup attributes) throws IOException {
        Optional<PrinterState> state = attributes.getValue(Attributes.PrinterState);
        List<String> reasons = attributes.getValues(Attributes.PrinterStateReasons);
        Optional<String> message = attributes.getValue(Attributes.PrinterStateMessage);
        if (!state.isPresent()) throw new IOException("Missing " + Attributes.PrinterState.getName());
        return new AutoValue_PrinterStatus(state.get(), reasons, message);
    }

    public abstract PrinterState getState();

    public abstract List<String> getReasons();

    public abstract Optional<String> getMessage();

    public String toString() {
        Optional<String> message = getMessage();
        return "Printer{state=" + getState().getName() +
                (getReasons().isEmpty() ? "" : " r=" + getReasons()) +
                (message.isPresent() && !message.get().isEmpty() ? " m=" + message.get() : "") +
                "}";
    }
}
