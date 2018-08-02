package com.hp.jipp.model;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

import com.hp.jipp.encoding.*;
import com.hp.jipp.pwg.JobState;
import com.hp.jipp.pwg.Operation;
import com.hp.jipp.pwg.PrinterState;
import com.hp.jipp.pwg.Status;
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
        Attribute<String> attribute = cycle(Types.attributesNaturalLanguage,
                Types.attributesNaturalLanguage.of("en"));
        assertEquals(Collections.singletonList("en"), attribute.strings());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                Types.attributesNaturalLanguage.of("en")));
        Attribute<String> attribute = group.get(Types.attributesNaturalLanguage);
        assertEquals(Collections.singletonList("en"), attribute.strings());
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
        System.out.println(group);
        assertEquals(Arrays.asList(Operation.cancelJob, Operation.createJob),
                group.get(Types.operationsSupported).getValues());
    }

    @Test
    public void rangeOfIntegers() throws Exception {
        IntRange range = cycle(Types.copiesSupported, Types.copiesSupported.of(new IntRange(0, 99))).getValue();
        assertEquals(0, range.getFirst());
        assertEquals(99, range.getLast());
    }

    @Test
    public void resolution() throws Exception {
        Resolution resolution = cycle(Types.printerResolutionDefault, Types.printerResolutionDefault.of(
                new Resolution(300, 600, ResolutionUnit.dotsPerInch))).getValue();
        assertEquals(300, resolution.getCrossFeedResolution());
        assertEquals(600, resolution.getFeedResolution());
        assertEquals(ResolutionUnit.dotsPerInch, resolution.getUnit());
    }

    @Test
    public void unknown() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unknown());
        assertEquals(0, attribute.getValues().size());
        assertEquals(Tag.unknown, attribute.getTag());
        assertTrue(attribute.isUnknown());
    }

    @Test
    public void noValue() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.noValue());
        assertEquals(0, attribute.getValues().size());
        assertEquals(Tag.noValue, attribute.getTag());
        assertTrue(attribute.isNoValue());
    }

    @Test
    public void unsupported() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unsupported());
        assertEquals(0, attribute.getValues().size());
        assertEquals(Tag.unsupported, attribute.getTag());
        assertTrue(attribute.isUnsupported());
    }

}
