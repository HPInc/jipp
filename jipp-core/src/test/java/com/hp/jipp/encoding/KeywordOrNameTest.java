package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.model.Types.mediaSupported;
import static org.junit.Assert.assertEquals;

public class KeywordOrNameTest {
    @Test
    public void simpleAttr() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(Types.mediaSupported, mediaSupported.of(
                new KeywordOrName("keyword"), new KeywordOrName(new Name("name"))));
        assertEquals("\"name\" (name)", attribute.get(1).toString());
        assertEquals("keyword", attribute.get(0).toString());
    }

    @Test
    public void strings() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(Types.mediaSupported, mediaSupported.of(
                new KeywordOrName("keyword"), new KeywordOrName(new Name("name"))));
        assertEquals(Arrays.asList("keyword", "name"), attribute.strings());
    }

    @Test
    public void keywords() throws IOException {
        Attribute<KeywordOrName> attribute = cycle(Types.mediaSupported, mediaSupported.of("keyword", "keyword2"));
        assertEquals(Arrays.asList("keyword", "keyword2"), attribute.strings());
    }

    @Test
    public void asString() {
        AttributeGroup group = AttributeGroup.groupOf(Tag.operationAttributes, mediaSupported.of(
                new KeywordOrName("keyword"),
                new KeywordOrName(new Name("nombre", "es"))));
        assertEquals(Arrays.asList("keyword", "nombre"), group.getValues(Types.mediaSupported.getAsString()));
    }
}
