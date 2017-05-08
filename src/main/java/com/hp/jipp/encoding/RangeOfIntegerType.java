package com.hp.jipp.encoding;

import com.google.common.collect.Range;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RangeOfIntegerType extends AttributeType<Range<Integer>> {
    private final static String TYPE_NAME = "RangeOfInteger";

    static final Attribute.Encoder<Range<Integer>> ENCODER = new Attribute.Encoder<Range<Integer>>(TYPE_NAME) {
        @Override
        public Range<Integer> readValue(DataInputStream in, Tag valueTag) throws IOException {
            Attribute.expectLength(in, 8);
            int low = in.readInt();
            int high = in.readInt();
            return Range.closed(low, high);
        }

        @Override
        public void writeValue(DataOutputStream out, Range<Integer> value) throws IOException {
            out.writeShort(8);
            out.writeInt(value.lowerEndpoint());
            out.writeInt(value.upperEndpoint());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.RangeOfInteger;
        }
    };

    public RangeOfIntegerType(String name) {
        super(ENCODER, Tag.RangeOfInteger, name);
    }
}
