package com.hp.jipp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.io.BaseEncoding;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.IntegerType;
import com.hp.jipp.encoding.Resolution;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

import static com.hp.jipp.encoding.Cycler.*;

public class AttributeTypeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void bogus() {
        // Jacoco: Fake coverage of utility class
        new Attributes();
    }

    @Test
    public void naturalLanguage() throws Exception {
        Attribute<String> attribute = cycle(Attributes.AttributesNaturalLanguage,
                Attributes.AttributesNaturalLanguage.of("en"));
        assertEquals(ImmutableList.of("en"), attribute.getValues());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.of(Tag.OperationAttributes,
                Attributes.AttributesNaturalLanguage.of("en")));

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
        AttributeGroup group = cycle(AttributeGroup.of(Tag.OperationAttributes,
                new StringType(Tag.NaturalLanguage, "attributes-NATURAL-language").of("en")));
        assertFalse(group.get(Attributes.AttributesNaturalLanguage).isPresent());
    }

    @Test
    public void enumAttributeType() throws Exception {
        AttributeGroup group = cycle(AttributeGroup.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(Operation.CancelJob, Operation.CreateJob)));
        assertEquals(ImmutableList.of(Operation.CancelJob, Operation.CreateJob),
                group.get(Attributes.OperationsSupported).get().getValues());
    }

    @Test
    public void customEnumValue() throws Exception {
    }

    @Test
    public void verifyWeirdEnums() throws Exception {
        assertEquals("JobState(xff)", JobState.ENCODER.get(0xFF).getName());
        assertEquals(0xFF, JobState.ENCODER.get(0xFF).getCode());

        assertEquals("PrinterState(x7)", PrinterState.ENCODER.get(7).getName());
        assertEquals("Status(x777)", Status.ENCODER.get(0x777).getName());
    }

    @Test
    public void rangeOfIntegers() throws Exception {
        Range range = cycle(Attributes.CopiesSupported.of(Range.closed(0, 99))).getValue(0);
        assertEquals(0, range.lowerEndpoint());
        assertEquals(99, range.upperEndpoint());
    }

    @Test
    public void resolution() throws Exception {
        Resolution resolution = cycle(Attributes.PrinterResolutionDefault.of(
                Resolution.of(300, 600, Resolution.Unit.DotsPerInch))).getValue(0);
        assertEquals(300, resolution.getCrossFeedResolution());
        assertEquals(600, resolution.getFeedResolution());
        assertEquals(Resolution.Unit.DotsPerInch, resolution.getUnit());
    }

}
