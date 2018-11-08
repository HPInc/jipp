package com.hp.jipp.encoding;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;

public class KeywordOrNameTest {
    private KeywordOrNameType type = new KeywordOrNameType("media");

    @Test
    public void simple() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(type, type.of(
                new KeywordOrName("keyword"),
                new KeywordOrName(new Name("name"))));
        assertEquals("\"name\" (name)", attribute.get(1).toString());
        assertEquals("keyword", attribute.get(0).toString());
    }

    @Test
    public void strings() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(type, type.of(
                new KeywordOrName("keyword"),
                new KeywordOrName(new Name("name"))));
        assertEquals(Arrays.asList("keyword", "name"), attribute.strings());
    }

    @Test
    public void keywords() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(type, type.of("keyword", "keyword2"));
        assertEquals(Arrays.asList("keyword", "keyword2"), attribute.strings());
    }
}
