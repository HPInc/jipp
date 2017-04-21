package com.hp.jipp.encoding;


import com.google.common.collect.Range;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RangeOfIntegerType extends AttributeType<Range<Integer>> {

    static final Attribute.Encoder<Range<Integer>> ENCODER = new Attribute.Encoder<Range<Integer>>() {

        @Override
        Range<Integer> readValue(DataInputStream in, Tag valueTag) throws IOException {
            expectLength(in, 8);
            int low = in.readInt();
            int high = in.readInt();
            return Range.closed(low, high);
        }

        @Override
        void writeValue(DataOutputStream out, Range<Integer> value) throws IOException {
            out.writeShort(8);
            out.writeInt(value.lowerEndpoint());
            out.writeInt(value.upperEndpoint());
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.RangeOfInteger;
        }
    };

    public RangeOfIntegerType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
