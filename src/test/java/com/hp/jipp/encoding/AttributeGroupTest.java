package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.MediaSize;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.KotlinTest;
import com.hp.jipp.util.ParseError;

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
        assertNotNull(group.get(Attributes.AttributesCharset));
        assertNotNull(group.get(Attributes.AttributesNaturalLanguage));
        assertNotNull(group.get(Attributes.PrinterUri));
    }

    @Test
    public void multiMultiAttribute() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8","utf-16")));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.getValues(Attributes.AttributesCharset));
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
        AttributeGroup.Companion.of(Tag.OperationAttributes,
                Attributes.AttributesCharset.of("utf-8"),
                Attributes.AttributesCharset.of("utf-8"));
    }

    @Test
    public void stringFromLang() throws Exception {
        LangStringType jobNameLang = new LangStringType(Tag.NameWithLanguage, "job-name");
        AttributeGroup group = cycle(AttributeGroup.of(Tag.JobAttributes,
                jobNameLang.of(new LangString("my job", "fr"))));

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
        assertNull(group.getValues(jobNameLang).get(0).getLang());
    }

    @Test
    public void missingEncoder() throws Exception {
        // This cannot happen but if it did it would throw nicely.
        exception.expect(ParseError.class);
        AttributeGroup.finderOf(Collections.<String, AttributeType<?>>emptyMap(),
                Arrays.<Encoder<?>>asList())
                .find(Tag.NameWithLanguage, "haha");
    }

    @Test
    public void findMediaInCollection() throws Exception {
        CollectionType jobConstraintsSupported = new CollectionType("job-constraints-supported");
        StringType resolverName = new StringType(Tag.NameWithoutLanguage, "resolver-name");

        Attribute<AttributeCollection> jobConstraints = jobConstraintsSupported.of(
                new AttributeCollection(
                        resolverName.of("fullbleed-sizes"),
                        Attributes.Media.of(MediaSize.NaLetter, MediaSize.IsoA4)));

        AttributeGroup group = cycle(AttributeGroup.Companion.of(Tag.PrinterAttributes, jobConstraints));
        System.out.println(group);
        assertEquals(MediaSize.NaLetter, group.getValue(jobConstraintsSupported).values(Attributes.Media).get(0));
        assertEquals(MediaSize.IsoA4, group.getValue(jobConstraintsSupported).values(Attributes.Media).get(1));
    }

    @Test
    public void cover() throws Exception {
        AttributeGroup group = AttributeGroup.of(Tag.OperationAttributes);
        KotlinTest.cover(group,
                group.copy(group.component1(), group.component2()),
                group.copy(group.component1(), Arrays.asList(Attributes.JobName.of("name"))));
    }
}
