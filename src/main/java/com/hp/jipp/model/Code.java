package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.NameCode;
import com.hp.jipp.encoding.NameCodeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A superset of Status and Operation */
@AutoValue
public abstract class Code extends NameCode {

    private static final List<Code> ALL;

    static {
        List<Code> codes = new ArrayList<>();

        for (NameCode status : Status.ENCODER.getMap().values()) {
            codes.add(new AutoValue_Code(status.getName(), status.getCode()));
        }
        for (NameCode operation : Operation.ENCODER.getMap().values()) {
            codes.add(new AutoValue_Code(operation.getName(), operation.getCode()));
        }
        ALL = Collections.unmodifiableList(codes);
    }

    public static final NameCodeType.Encoder<Code> ENCODER = NameCodeType.Encoder.of(
            "Code", ALL, new NameCode.Factory<Code>() {
                @Override
                public Code of(String name, int code) {
                    return new AutoValue_Code(name, code);
                }
            });
}
