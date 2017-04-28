package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.NameCode;

@AutoValue
public abstract class PrinterState extends NameCode {

    public static final PrinterState Idle = of("idle", 3);
    public static final PrinterState Processing = of("processing", 4);
    public static final PrinterState Stopped = of("stopped", 5);

    public final static NameCodeType.Encoder<PrinterState> ENCODER = NameCodeType.encoder(
            "printer-state", ImmutableSet.of(
                    Idle, Processing, Stopped
            ), new NameCode.Factory<PrinterState>() {
                @Override
                public PrinterState of(String name, int code) {
                    return PrinterState.of(name, code);
                }
            });

    public static PrinterState of(String name, int code) {
        return new AutoValue_PrinterState(name, code);
    }
}
