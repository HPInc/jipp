package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.hp.jipp.encoding.Cycler.*;

import static org.junit.Assert.*;

public class LangStringTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void languageStrings() throws Exception {
        LangStringType nameType = new LangStringType(Tag.NameWithLanguage, "job-name");
        Attribute<LangString> name = cycle(nameType.of(LangString.of("my job", "fr")));
        System.out.println("name: " + name);
        assertEquals("my job", name.getValue(0).getString());
        assertEquals("fr", name.getValue(0).getLang().get());
    }

    @Test
    public void nonStringFrom() throws Exception {
        // from() fails when you try to jam an integer into a string
        assertEquals(false, new StringType(Tag.TextWithoutLanguage, "test")
                .of(new IntegerType(Tag.IntegerValue, "integer").of(5)).isPresent());
        assertEquals(false, new LangStringType(Tag.TextWithLanguage, "test")
                .of(new IntegerType(Tag.IntegerValue, "integer").of(5)).isPresent());
    }

    @Test
    public void badMissingLang() throws Exception {
        exception.expect(BuildError.class);
        cycle(new LangStringType(Tag.TextWithLanguage, "something").of(LangString.of("oops")));
    }
}
