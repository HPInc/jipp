// Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;
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

    private TextType.Set jobTextType = new TextType.Set("job-name");
    private Attribute<Text> jobTextType(Text job) { return jobTextType.of(job); }
    private Attribute<Text> jobTextType(String job) { return jobTextType.of(new Text(job)); }

    @Test
    public void withoutLanguage() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType("my job");
        Attribute<Text> result = cycle(jobTextType, jobTextAttr);
        assertEquals("my job", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void simpleStrings() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType("my job2");
        Attribute<Text> result = cycle(jobTextType, jobTextAttr);
        assertEquals("my job2", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void withLanguage() throws IOException {
        Attribute<Text> jobTextAttr = jobTextType(new Text("my job", "en"));
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
        Attribute<Text> attr = jobTextType.of("one", "two");
        assertEquals(jobTextType.of(new Text("one"), new Text("two")), attr);
    }

    @Test
    public void failCoerce() {
        KeywordType wrongType = new KeywordType("job-name");
        assertNull(groupOf(Tag.jobAttributes, wrongType.of("utf-8")).get(jobTextType));
    }

    @Test
    public void cover() {
        Text hi = new Text("hi");
        KotlinTest.cover(hi, hi.copy("hi", null), new Text("hola", "sp"));
    }
}
