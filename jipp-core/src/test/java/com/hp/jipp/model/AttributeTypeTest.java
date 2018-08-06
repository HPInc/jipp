package com.hp.jipp.model;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

import com.hp.jipp.encoding.*;
import com.hp.jipp.pwg.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.hp.jipp.encoding.Cycler.*;

import java.util.Arrays;
import java.util.Collections;

import kotlin.ranges.IntRange;

public class AttributeTypeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void naturalLanguage() throws Exception {
        Attribute<String> attribute = cycle(OperationGroup.attributesNaturalLanguage,
                OperationGroup.attributesNaturalLanguage.of("en"));
        assertEquals(Collections.singletonList("en"), attribute.strings());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                OperationGroup.attributesNaturalLanguage.of("en")));
        Attribute<String> attribute = group.get(OperationGroup.attributesNaturalLanguage);
        assertEquals(Collections.singletonList("en"), attribute.strings());
    }

    @Test
    public void ignoreBadNameNaturalLanguage() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                new StringType(Tag.naturalLanguage, "attributes-NATURAL-language").of("en")));
        assertNull(group.get(OperationGroup.attributesNaturalLanguage));
    }

    @Test
    public void enumAttributeType() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.printerAttributes,
                PrinterDescriptionGroup.operationsSupported.of(Operation.cancelJob,
                        Operation.createJob)));
        System.out.println(group);
        assertEquals(Arrays.asList(Operation.cancelJob, Operation.createJob),
                group.get(PrinterDescriptionGroup.operationsSupported));
    }

    @Test
    public void rangeOfIntegers() throws Exception {
        IntRange range = cycle(PrinterDescriptionGroup.copiesSupported, PrinterDescriptionGroup.copiesSupported.of(new IntRange(0, 99))).get(0);
        assertEquals(0, range.getFirst());
        assertEquals(99, range.getLast());
    }

    @Test
    public void resolution() throws Exception {
        Resolution resolution = cycle(PrinterDescriptionGroup.printerResolutionDefault, PrinterDescriptionGroup.printerResolutionDefault.of(
                new Resolution(300, 600, ResolutionUnit.dotsPerInch))).get(0);
        assertEquals(300, resolution.getCrossFeedResolution());
        assertEquals(600, resolution.getFeedResolution());
        assertEquals(ResolutionUnit.dotsPerInch, resolution.getUnit());
    }

    @Test
    public void unknown() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unknown());
        assertEquals(0, attribute.size());
        assertEquals(Tag.unknown, attribute.getTag());
        assertTrue(attribute.isUnknown());
    }

    @Test
    public void noValue() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.noValue());
        assertEquals(0, attribute.size());
        assertEquals(Tag.noValue, attribute.getTag());
        assertTrue(attribute.isNoValue());
    }

    @Test
    public void unsupported() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unsupported());
        assertEquals(0, attribute.size());
        assertEquals(Tag.unsupported, attribute.getTag());
        assertTrue(attribute.isUnsupported());
    }

    @Test
    public void intOrRangeType() throws Exception {
        Attribute<?> attribute = cycle(
                PrinterDescriptionGroup.stitchingOffsetSupported.of(new IntOrIntRange(5), new IntOrIntRange(7, 10)));
        // We get the raw types here because we didn't use the type to cycle
        assertEquals(Arrays.asList(5, new IntRange(7, 10)), attribute);

        Attribute<?> attribute2 = cycle(PrinterDescriptionGroup.stitchingOffsetSupported,
                PrinterDescriptionGroup.stitchingOffsetSupported.of(new IntOrIntRange(5), new IntOrIntRange(7, 10)));
        assertEquals(new IntOrIntRange(5), attribute2.get(0));
        assertEquals(new IntOrIntRange(7, 10), attribute2.get(1));
        assertEquals(Arrays.asList(new IntOrIntRange(5), new IntOrIntRange(7, 10)), attribute2);
    }
}
