package com.hp.jipp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IntegerType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

public class AttributeTypeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void naturalLanguage() throws Exception {
        Attribute<String> attribute = cycle(Attributes.AttributesNaturalLanguage,
                Attributes.AttributesNaturalLanguage.create("en"));
        assertEquals(ImmutableList.of("en"), attribute.getValues());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.create(Tag.OperationAttributes,
                Attributes.AttributesNaturalLanguage.create("en")));

        Attribute<String> attribute = group.get(Attributes.AttributesNaturalLanguage).get();
        assertEquals(ImmutableList.of("en"), attribute.getValues());
    }

    @Test
    public void ignoreBadTypeNaturalLanguage() throws Exception {
        exception.expect(RuntimeException.class);
        // Throws because NaturalLanguage is not 5
        new IntegerType(Tag.NaturalLanguage, "attributes-natural-language");
    }

    @Test
    public void ignoreBadNameNaturalLanguage() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.create(Tag.OperationAttributes,
                new StringType(Tag.NaturalLanguage, "attributes-NATURAL-language").create("en")));
        assertFalse(group.get(Attributes.AttributesNaturalLanguage).isPresent());
    }

    @Test
    public void enumAttributeType() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.create(Tag.PrinterAttributes,
                Attributes.OperationsSupported.create(Operation.CancelJob, Operation.CreateJob)));
        assertEquals(ImmutableList.of(Operation.CancelJob, Operation.CreateJob),
                group.get(Attributes.OperationsSupported).get().getValues());
    }

    @SuppressWarnings("unchecked")
    private <T> Attribute<T> cycle(com.hp.jipp.encoding.AttributeType attributeType, Attribute<T> attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return attributeType.getEncoder().read(in, Tag.read(in));
    }

    private byte[] toBytes(Attribute attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new DataOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }

    private AttributeGroup cycle(AttributeGroup group) throws IOException {
        return AttributeGroup.read(new DataInputStream(new ByteArrayInputStream(toBytes(group))));
    }

    private byte[] toBytes(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytesOut);
        group.write(out);
        Tag.EndOfAttributes.write(out);
        return bytesOut.toByteArray();
    }

}
