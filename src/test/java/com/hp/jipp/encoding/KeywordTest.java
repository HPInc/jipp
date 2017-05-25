package com.hp.jipp.encoding;


import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class KeywordTest {

    // A basic keyword
    public static class Sample extends Keyword {
        public static final String ONE_NAME = "one";
        public static final Sample One = of(ONE_NAME);
        public static final Sample Two = of("two");
        public static final Sample Three = of("three");

        public static final KeywordType.Encoder<Sample> ENCODER = KeywordType.Encoder.Companion.of(
                Sample.class, new Keyword.Factory<Sample>() {
                    @Override
                    public Sample of(String name) {
                        return Sample.of(name);
                    }
                });
        private final String name;

        public static KeywordType<Sample> typeOf(String name) {
            return new KeywordType<>(ENCODER, name);
        }

        public static Sample of(String name) {
            return new Sample(name);
        }
        public Sample(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Test
    public void testAll() {
        assertThat(Sample.ENCODER.getAll(), hasItems(Sample.One, Sample.Two, Sample.Three));
    }

    @Test
    public void rejectInvalid() {
        assertFalse(Sample.ENCODER.valid(Tag.IntegerValue));
    }
}
