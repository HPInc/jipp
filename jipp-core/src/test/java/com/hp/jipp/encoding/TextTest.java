package com.hp.jipp.encoding;

import com.hp.jipp.pwg.JobStatusGroup;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test behavior of TextType.
 */
public class TextTest {

    private TextType jobTextType = new TextType("job-name");

    @Test
    public void withoutLanguage() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType.of(new Text("my job"));
        assertEquals(Tag.textWithoutLanguage, jobTextAttr.getValue().getTag());
        Attribute<Text> result = cycle(jobTextType, jobTextAttr);
        assertEquals("my job", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void simpleStrings() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType.of("my job");
        assertEquals(Tag.textWithoutLanguage, jobTextAttr.getValue().getTag());
        Attribute<Text> result = cycle(jobTextType, jobTextAttr);
        assertEquals("my job", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void withLanguage() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType.of(new Text("my job", "en"));
        assertEquals(Tag.textWithLanguage, jobTextAttr.getValue().getTag());
        Attribute<Text> result = cycle(jobTextType, jobTextAttr);
        assertEquals("my job", result.getValue().getValue());
        assertEquals("en", result.getValue().getLang());
    }

    @Test
    public void mixed() throws IOException {
        // Totally allowable
        assertEquals(Arrays.asList("my job", "another job"),
                cycle(jobTextType, jobTextType.of(new Text("my job", "en"), new Text("another job"))).strings());
    }

    @Test
    public void ofStrings() throws IOException {
        Attribute<Text> attr = jobTextType.ofStrings(Arrays.asList("one", "two"));
        assertEquals(jobTextType.of(new Text("one"), new Text("two")), attr);
    }

    @Test
    public void failCoerce() {
        KeywordType wrongType = new KeywordType("job-name");
        assertNull(groupOf(Tag.jobAttributes, wrongType.of("utf-8")).get(jobTextType));
    }
}
