package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.NameCode;
import com.hp.jipp.encoding.NameCodeType;

import java.util.List;

/** A superset of Status and Operation */
@AutoValue
public abstract class Code extends NameCode {

    public static final List<Code> ALL;

    static {
        ImmutableList.Builder<Code> builder = ImmutableList.builder();

        for (NameCode status : Status.ENCODER.getMap().values()) {
            builder.add(new AutoValue_Code(status.getName(), status.getCode()));
        }
        for (NameCode operation : Operation.ENCODER.getMap().values()) {
            builder.add(new AutoValue_Code(operation.getName(), operation.getCode()));
        }
        ALL = builder.build();
    }

    public static final NameCodeType.Encoder<Code> ENCODER = NameCodeType.Encoder.of(
            "Code", ALL, new NameCode.Factory<Code>() {
                @Override
                public Code of(String name, int code) {
                    return new AutoValue_Code(name, code);
                }
            });
}
