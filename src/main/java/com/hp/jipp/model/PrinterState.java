package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.NameCode;

@AutoValue
public abstract class PrinterState extends NameCode {

    public static final PrinterState Idle = create("idle", 3);
    public static final PrinterState Processing = create("processing", 4);
    public static final PrinterState Stopped = create("stopped", 5);

    public final static NameCodeType.Encoder<PrinterState> ENCODER = NameCodeType.encoder(
            "job-state", ImmutableSet.of(
                    Idle, Processing, Stopped
            ), new NameCode.Factory<PrinterState>() {
                @Override
                public PrinterState create(String name, int code) {
                    return PrinterState.create(name, code);
                }
            });

    public static PrinterState create(String name, int code) {
        return new AutoValue_PrinterState(name, code);
    }
}
