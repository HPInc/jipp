package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.PrinterState;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

// All interesting status fields of a printer
@AutoValue
public abstract class PrinterStatus {

    static PrinterStatus of(AttributeGroup attributes) throws IOException {
        PrinterState state = attributes.getValue(Attributes.PrinterState);
        List<String> reasons = attributes.getValues(Attributes.PrinterStateReasons);
        String message = attributes.getValue(Attributes.PrinterStateMessage);
        if (state == null) throw new IOException("Missing " + Attributes.PrinterState.getName());
        return new AutoValue_PrinterStatus(state, reasons, message);
    }

    public abstract PrinterState getState();

    public abstract List<String> getReasons();

    @Nullable
    public abstract String getMessage();

    public String toString() {
        return "Printer{state=" + getState().getName() +
                (getReasons().isEmpty() ? "" : " r=" + getReasons()) +
                (getMessage() == null ? "" : " m=" + getMessage()) +
                "}";
    }
}
