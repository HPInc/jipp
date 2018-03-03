package com.hp.jipp.model;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IntegerType;
import com.hp.jipp.encoding.Resolution;
import com.hp.jipp.encoding.ResolutionUnit;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

import static com.hp.jipp.encoding.AttributeGroupKt.groupOf;
import static com.hp.jipp.encoding.Cycler.*;

import java.util.Arrays;
import java.util.Collections;

import kotlin.ranges.IntRange;

public class AttributeTypeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void naturalLanguage() throws Exception {
        Attribute<String> attribute = cycle(Types.attributesNaturalLanguage,
                Types.attributesNaturalLanguage.of("en"));
        assertEquals(Collections.singletonList("en"), attribute.getValues());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                Types.attributesNaturalLanguage.of("en")));

        Attribute<String> attribute = group.get(Types.attributesNaturalLanguage);
        assertEquals(Collections.singletonList("en"), attribute.getValues());
    }

    @Test
    public void ignoreBadTypeNaturalLanguage() throws Exception {
        exception.expect(RuntimeException.class);
        // Throws because naturalLanguage is not 5
        new IntegerType(Tag.naturalLanguage, "attributes-natural-language");
    }

    @Test
    public void ignoreBadNameNaturalLanguage() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                new StringType(Tag.naturalLanguage, "attributes-NATURAL-language").of("en")));
        assertNull(group.get(Types.attributesNaturalLanguage));
    }

    @Test
    public void enumAttributeType() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.printerAttributes,
                Types.operationsSupported.of(Operation.cancelJob,
                        Operation.createJob)));
        assertEquals(Arrays.asList(Operation.cancelJob, Operation.createJob),
                group.get(Types.operationsSupported).getValues());
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
        IntRange range = cycle(Types.copiesSupported.of(new IntRange(0, 99))).getValue(0);
        assertEquals(0, range.getFirst());
        assertEquals(99, range.getLast());
    }

    @Test
    public void resolution() throws Exception {
        Resolution resolution = cycle(Types.printerResolutionDefault.of(
                new Resolution(300, 600, ResolutionUnit.dotsPerInch))).getValue(0);
        assertEquals(300, resolution.getCrossFeedResolution());
        assertEquals(600, resolution.getFeedResolution());
        assertEquals(ResolutionUnit.dotsPerInch, resolution.getUnit());
    }
}
