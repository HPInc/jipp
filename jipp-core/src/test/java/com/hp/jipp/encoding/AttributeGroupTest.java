package com.hp.jipp.encoding;

import com.hp.jipp.model.DocumentState;
import com.hp.jipp.model.Types;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

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
    public void getValues() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.getValues(Types.attributesCharset));
        assertEquals(Collections.emptyList(), group.getValues(Types.attributesNaturalLanguage));
    }

    @Test
    public void getStrings() throws Exception {
        AttributeGroup group = groupOf(Tag.operationAttributes,
                Types.printerName.of(new Name("myprinter")));
        assertEquals(Collections.singletonList("myprinter"), group.getStrings(Types.printerName));
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
}
