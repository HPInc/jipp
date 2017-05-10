package com.hp.jipp.encoding;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import com.google.common.collect.ImmutableList;

public class NameCodeTest {
    /** An enumeration of possible printer states */
    public static class Sample extends NameCode {

        public static final Sample One = new Sample("one", 1);
        public static final Sample Two = new Sample("two", 2);
        public static final Sample Three = new Sample("three", 3);
        // Cannot be reached
        private static final Sample Secret = new Sample("secret", 4);

        public static final NameCodeType.Encoder<Sample> ENCODER = NameCodeType.Encoder.of(
                Sample.class, new NameCode.Factory<Sample>() {
                    @Override
                    public Sample of(String name, int code) {
                        return new Sample(name, code);
                    }
                });
        private final int code;
        private final String name;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getCode() {
            return code;
        }

        Sample(String name, int code) {
            this.name = name;
            this.code = code;
        }
    }
    NameCodeType<Sample> MySample = NameCodeType.typeOf(Sample.ENCODER, "my-sample");

    @Test
    public void sample() throws Exception {
        assertEquals(ImmutableList.of(Sample.One), cycle(MySample, MySample.of(Sample.One)).getValues());
    }

    @Test
    public void fetchFromGroup() throws Exception {
        assertEquals(ImmutableList.of(Sample.Two, Sample.Three),
                cycle(AttributeGroup.of(Tag.JobAttributes, MySample.of(Sample.Two, Sample.Three))).getValues(MySample));
    }
}
