package com.hp.jipp.encoding;


import com.hp.jipp.util.BuildError;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class KeywordTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    // A basic keyword
    public static class Sample extends Keyword {
        public static final String ONE_NAME = "one";
        public static final Sample One = of(ONE_NAME);
        public static final Sample Two = of("two");
        public static final Sample Three = of("three");

        public static final KeywordType.Encoder<Sample> ENCODER = KeywordType.Companion.encoderOf(Sample.class,
                new Keyword.Factory<Sample>() {
                    @Override
                    public Sample of(String name) {
                        return Sample.of(name);
                    }
                });
        private final String name;

        public static KeywordType<Sample> typeOf(String name) {
            return new KeywordType<Sample>(ENCODER, name);
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
        assertFalse(Sample.ENCODER.valid(Tag.integerValue));
    }

    @Test
    public void rejectName() throws IOException {
        StringType nameType = new StringType(Tag.nameWithoutLanguage, "sample");

        // Show that we *cannot* encode nameType as Sample
        exception.expect(BuildError.class);
        Cycler.cycle(Sample.typeOf("sample"), nameType.of("three"));
    }
}
