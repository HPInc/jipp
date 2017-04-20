package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableList;

import java.util.List;

class AttributeEncoders {
    /** Encoders available to parse incoming data */
    static final List<Attribute.Encoder<?>> ENCODERS = ImmutableList.of(
            IntegerType.ENCODER, StringType.ENCODER, UriType.ENCODER, BooleanType.ENCODER, LangStringType.ENCODER,
            CollectionType.ENCODER, OctetStringType.ENCODER);
            // TODO: RangeOfInteger attribute
            // TODO: 1setofX
            // TODO: resolution
            // TODO: dateTime
}
