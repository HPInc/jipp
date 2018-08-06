package com.hp.jipp.encoding;

import com.hp.jipp.pwg.DocumentState;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

import com.hp.jipp.model.Types;
import com.hp.jipp.util.BuildError;

import static com.hp.jipp.encoding.Cycler.*;

public class AttributeGroupTest {

    @Test public void emptyGroup() throws IOException {
        cycle(new AttributeGroup(Tag.printerAttributes, Collections.<Attribute<Object>>emptyList()));
    }

    @Test public void emptyGroupOf() throws IOException {
        assertEquals(0, cycle(groupOf(Tag.printerAttributes, Collections.<Attribute<Object>>emptyList())).size());
    }

    @Test public void groupExtract() {
        AttributeType<Object> untypedDocumentStateType = new UnknownAttribute.Type("document-state");

        AttributeGroup group = new AttributeGroup(Tag.operationAttributes,
                untypedDocumentStateType.of(new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6)));

        DocumentState.Type documentStateType = new DocumentState.Type("document-state");

        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), group.get(documentStateType));
    }

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
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.get(Types.attributesCharset).strings());
    }

    @Test
    public void missingAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer"))));
        assertNull(group.get(Types.attributesNaturalLanguage));
    }

    @Test(expected = BuildError.class)
    public void duplicateName() throws Exception {
        groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test(expected = BuildError.class)
    public void badDelimiter() throws Exception {
        AttributeGroup group = new AttributeGroup(Tag.adminDefine);
    }

    @Test
    public void get() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        // Get by attribute type
        assertEquals(Types.attributesCharset.of("utf-8","utf-16"), group.get(Types.attributesCharset));
        // Get by attribute name (in some cases this will not be as well typed, e.g. collections)
        assertEquals(Types.attributesCharset.of("utf-8","utf-16"), group.get(Types.attributesCharset.getName()));
        assertNull(group.get(Types.printerName));
    }

    @Test
    public void unknownAttribute() throws Exception {
        UnknownAttribute attr = new UnknownAttribute("vendor-state",
                new UntypedEnum(3),
                new OtherOctets(Tag.fromInt(0x39), new byte[] { 0x01 }));
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes, attr));
        assertEquals(attr, group.get("vendor-state"));
    }

    @Test
    public void cover() throws Exception {
        coverList(groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16")),
                Types.attributesCharset.of("utf-8","utf-16"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test
    public void equality() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        List<Attribute<String>> attributes = Collections.singletonList(Types.attributesCharset.of("utf-8", "utf-16"));
        assertNotEquals(group, 5);
        assertEquals(group, group);
        assertEquals(attributes, group);
        //noinspection AssertEqualsBetweenInconvertibleTypes
        assertEquals(group, attributes);
        assertNotEquals(group, Collections.singletonList(Types.attributesCharset.of("utf-8")));
        assertNotEquals(group, groupOf(Tag.operationAttributes,
                Collections.singletonList(Types.attributesCharset.of("utf-8"))));
        assertNotEquals(group, groupOf(Tag.printerAttributes,
                Types.attributesCharset.of("utf-8","utf-16")));
        assertEquals(attributes.hashCode(), group.hashCode());
    }

    // TODO: Tests for string <--> Text or Name here?
//    @Test
//    public void stringFromLang() throws Exception {
//        LangStringType jobNameLang = new LangStringType(Tag.nameWithLanguage, "job-name");
//        AttributeGroup group = cycle(groupOf(Tag.jobAttributes,
//                jobNameLang.of(new LangString("my job", "fr"))));
//
//        // If I don't care about the language encoding:
//        StringType jobName = new StringType(Tag.nameWithoutLanguage, "job-name");
//        assertEquals("my job", group.getValues(jobName).get(0));
//    }
//
//    @Test
//    public void langFromString() throws Exception {
//        StringType jobName = new StringType(Tag.nameWithoutLanguage, "job-name");
//        AttributeGroup group = cycle(groupOf(Tag.jobAttributes,
//                jobName.of("my job")));
//
//        // If I don't care about the language encoding:
//        LangStringType jobNameLang = new LangStringType(Tag.nameWithLanguage, "job-name");
//        assertEquals("my job", group.getValues(jobNameLang).get(0).getString());
//        assertNull(group.getValues(jobNameLang).get(0).getLang());
//    }

    // Collections are different
//    @Test
//    public void findMediaInCollection() throws Exception {
//        CollectionType jobConstraintsSupported = new CollectionType("job-constraints-supported");
//        StringType resolverName = new StringType(Tag.nameWithoutLanguage, "resolver-name");
//
//        Attribute<AttributeCollection> jobConstraints = jobConstraintsSupported.of(
//                new AttributeCollection(
//                        resolverName.of("fullbleed-sizes"),
//                        Types.media.of(MediaSize.naLetter, MediaSize.isoA4)));
//
//        AttributeGroup group = cycle(groupOf(Tag.printerAttributes, jobConstraints));
//        System.out.println(group);
//        assertEquals(MediaSize.naLetter, group.getValue(jobConstraintsSupported).values(Types.media).get(0));
//        assertEquals(MediaSize.isoA4, group.getValue(jobConstraintsSupported).values(Types.media).get(1));
//    }

//    @Test
//    public void cover() throws Exception {
//        AttributeGroup group = groupOf(Tag.operationAttributes);
//        KotlinTest.cover(group,
//                group.copy(group.component1(), group.component2()),
//                group.copy(group.component1(), Collections.singletonList(Types.jobName.of("name"))));
//    }
}
