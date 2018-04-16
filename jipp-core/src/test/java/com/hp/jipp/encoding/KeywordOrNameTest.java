package com.hp.jipp.encoding;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class KeywordOrNameTest {
    public static class Sample extends Keyword {
        // Must be public to be seen by .encoderOf()
        public static final Sample one = of("one");
        public static final Sample two = of("two");
        public static final Sample three = of("three");

        static final KeywordOrNameType.Encoder<Sample> ENCODER = KeywordOrNameType.Companion.encoderOf(
                Sample.class,
                new Keyword.Factory<Sample>() {
                    @NotNull
                    @Override
                    public Sample of(String name) {
                        return new Sample(name);
                    }
                });
        private final String name;

        public static Sample of(String name) {
            return new Sample(name);
        }

        // Note: an equality comparison of this type will not succeed for same wrapped data
        Sample(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private KeywordOrNameType<Sample> konType = new KeywordOrNameType<Sample>(Sample.ENCODER, "sample");

    private StringType nameType = new StringType(Tag.nameWithoutLanguage, "sample");

    @Test
    public void testAll() {
        assertThat(Sample.ENCODER.getAll(), hasItems(Sample.one, Sample.two, Sample.three));
    }

    @Test
    public void testKeyword() throws IOException {
        assertEquals(Sample.one, Cycler.cycle(konType, konType.of(Sample.one)).get(0));
    }

    @Test
    public void testName() throws IOException {
        // Wrap a non-keyword value
        assertEquals(Sample.of("four").name, Cycler.cycle(konType, konType.of(Sample.of("four"))).get(0).name);
    }

    @Test
    public void testNameEncodedName() throws IOException {
        // Encode it as Name, which is also legal
        assertEquals("three", Cycler.cycle(konType, nameType.of("three")).get(0).name);
    }
}
