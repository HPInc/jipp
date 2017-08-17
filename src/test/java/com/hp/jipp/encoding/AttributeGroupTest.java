package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

import com.hp.jipp.model.Types;
import com.hp.jipp.model.MediaSize;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.KotlinTest;
import com.hp.jipp.util.ParseError;

import static com.hp.jipp.encoding.AttributeGroupKt.groupOf;
import static com.hp.jipp.encoding.Cycler.*;

public class AttributeGroupTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void multiAttribute() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesNaturalLanguage.of("en"),
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer")));
        group = cycle(group);

        assertEquals(group.getTag(), Tag.operationAttributes);
        assertNotNull(group.get(Types.attributesCharset));
        assertNotNull(group.get(Types.attributesNaturalLanguage));
        assertNotNull(group.get(Types.printerUri));
    }

    @Test
    public void multiMultiAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16")));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.getValues(Types.attributesCharset));
    }

    @Test
    public void missingAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer"))));
        assertEquals(0, group.getValues(Types.attributesNaturalLanguage).size());
    }

    @Test
    public void duplicateName() throws Exception {
        exception.expect(BuildError.class);
        groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test
    public void stringFromLang() throws Exception {
        LangStringType jobNameLang = new LangStringType(Tag.nameWithLanguage, "job-name");
        AttributeGroup group = cycle(groupOf(Tag.jobAttributes,
                jobNameLang.of(new LangString("my job", "fr"))));

        // If I don't care about the language encoding:
        StringType jobName = new StringType(Tag.nameWithoutLanguage, "job-name");
        assertEquals("my job", group.getValues(jobName).get(0));
    }

    @Test
    public void langFromString() throws Exception {
        StringType jobName = new StringType(Tag.nameWithoutLanguage, "job-name");
        AttributeGroup group = cycle(groupOf(Tag.jobAttributes,
                jobName.of("my job")));

        // If I don't care about the language encoding:
        LangStringType jobNameLang = new LangStringType(Tag.nameWithLanguage, "job-name");
        assertEquals("my job", group.getValues(jobNameLang).get(0).getString());
        assertNull(group.getValues(jobNameLang).get(0).getLang());
    }

    @Test
    public void missingEncoder() throws Exception {
        // This cannot happen but if it did it would throw nicely.
        exception.expect(ParseError.class);
        AttributeGroup.finderOf(Collections.<String, AttributeType<?>>emptyMap(),
                Arrays.<Encoder<?>>asList())
                .find(Tag.nameWithLanguage, "haha");
    }

    @Test
    public void findMediaInCollection() throws Exception {
        CollectionType jobConstraintsSupported = new CollectionType("job-constraints-supported");
        StringType resolverName = new StringType(Tag.nameWithoutLanguage, "resolver-name");

        Attribute<AttributeCollection> jobConstraints = jobConstraintsSupported.of(
                new AttributeCollection(
                        resolverName.of("fullbleed-sizes"),
                        Types.media.of(MediaSize.naLetter, MediaSize.isoA4)));

        AttributeGroup group = cycle(groupOf(Tag.printerAttributes, jobConstraints));
        System.out.println(group);
        assertEquals(MediaSize.naLetter, group.getValue(jobConstraintsSupported).values(Types.media).get(0));
        assertEquals(MediaSize.isoA4, group.getValue(jobConstraintsSupported).values(Types.media).get(1));
    }

    @Test
    public void cover() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes);
        KotlinTest.cover(group,
                group.copy(group.component1(), group.component2()),
                group.copy(group.component1(), Collections.singletonList(Types.jobName.of("name"))));
    }
}
