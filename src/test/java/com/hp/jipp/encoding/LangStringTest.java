package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.KotlinTest;

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
        Attribute<LangString> name = cycle(nameType.of(new LangString("my job", "fr")));
        System.out.println("name: " + name);
        assertEquals("my job", name.getValue(0).getString());
        assertEquals("fr", name.getValue(0).getLang());
    }

    @Test
    public void nonStringFrom() throws Exception {
        // from() fails when you try to jam an integer into a string
        assertNull(new StringType(Tag.TextWithoutLanguage, "test")
                .of(new IntegerType(Tag.IntegerValue, "integer").of(5)));
        assertNull(new LangStringType(Tag.TextWithLanguage, "test")
                .of(new IntegerType(Tag.IntegerValue, "integer").of(5)));
    }

    @Test
    public void badMissingLang() throws Exception {
        exception.expect(BuildError.class);
        cycle(new LangStringType(Tag.TextWithLanguage, "something").of(new LangString("oops")));
    }

    @Test
    public void cover() throws Exception {
        LangString ls = new LangString("hi", "en");
        KotlinTest.cover(ls,
                ls.copy(ls.component1(), ls.component2()),
                ls.copy("hello", ls.component2()));
    }
}
