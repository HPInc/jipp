package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.model.Attributes;

import static com.hp.jipp.encoding.Cycler.*;


public class AttributeGroupTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void multiAttribute() throws Exception {
        AttributeGroup group = AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesNaturalLanguage.of("en"),
                Attributes.PrinterUri.of(URI.create("ipp://10.0.0.23/ipp/printer")));
        group = cycle(group);

        assertEquals(group.getTag(), Tag.OperationAttributes);
        assertTrue(group.get(Attributes.AttributesCharset).isPresent());
        assertTrue(group.get(Attributes.AttributesNaturalLanguage).isPresent());
        assertTrue(group.get(Attributes.PrinterUri).isPresent());
    }

    @Test
    public void multiMultiAttribute() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8","utf-16")));
        assertEquals(ImmutableList.of("utf-8", "utf-16"), group.getValues(Attributes.AttributesCharset));
    }

    @Test
    public void missingAttribute() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.PrinterUri.of(URI.create("ipp://10.0.0.23/ipp/printer"))));
        assertEquals(0, group.getValues(Attributes.AttributesNaturalLanguage).size());
    }

    @Test
    public void duplicateName() throws Exception {
        exception.expect(BuildError.class);
        AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesCharset.of("utf-8"));
    }

    @Test
    public void stringFromLang() throws Exception {
        LangStringType jobNameLang = new LangStringType(Tag.NameWithLanguage, "job-name");
        AttributeGroup group = cycle(AttributeGroup.of(Tag.JobAttributes,
                jobNameLang.of(LangString.of("my job", "fr"))));

        // If I don't care about the language encoding:
        StringType jobName = new StringType(Tag.NameWithoutLanguage, "job-name");
        assertEquals("my job", group.getValues(jobName).get(0));
    }

    @Test
    public void langFromString() throws Exception {
        StringType jobName = new StringType(Tag.NameWithoutLanguage, "job-name");
        AttributeGroup group = cycle(AttributeGroup.of(Tag.JobAttributes,
                jobName.of("my job")));

        // If I don't care about the language encoding:
        LangStringType jobNameLang = new LangStringType(Tag.NameWithLanguage, "job-name");
        assertEquals("my job", group.getValues(jobNameLang).get(0).getString());
        assertEquals(Optional.absent(),group.getValues(jobNameLang).get(0).getLang());
    }
}
