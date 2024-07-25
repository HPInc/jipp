// Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import com.hp.jipp.util.KotlinTest;
import kotlin.ranges.IntRange;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.model.Types.copiesSupported;
import static com.hp.jipp.model.Types.mediaOrderCountSupported;
import static com.hp.jipp.model.Types.numberUpSupported;
import static com.hp.jipp.model.Types.printSpeedSupported;
import static com.hp.jipp.model.Types.copiesSupported;
import static com.hp.jipp.model.Types.mediaOrderCountSupported;
import static com.hp.jipp.model.Types.numberUpSupported;
import static com.hp.jipp.model.Types.printSpeedSupported;
import static org.junit.Assert.*;

public class IntOrIntRangeTest {
    @Test
    public void simpleRangeAttr() throws IOException {
        Attribute<IntRange> range = cycle(Types.copiesSupported, copiesSupported.of(new IntRange(0, 100)));
        assertEquals(0, range.get(0).getStart().intValue());
        assertEquals(100, range.get(0).getEndInclusive().intValue());
    }

    @Test
    public void multiRange() throws IOException {
        Attribute<IntRange> ranges = cycle(Types.mediaOrderCountSupported, mediaOrderCountSupported.of(new IntRange(0, 100), new IntRange(90, 1000)));
        assertEquals(0, ranges.get(0).getStart().intValue());
        assertEquals(100, ranges.get(0).getEndInclusive().intValue());
        assertEquals(90, ranges.get(1).getStart().intValue());
        assertEquals(1000, ranges.get(1).getEndInclusive().intValue());
    }

    @Test
    public void rangeOrInt() throws IOException {
        Attribute<IntOrIntRange> ranges = cycle(Types.numberUpSupported, numberUpSupported.of(5));
        assertTrue(ranges.get(0).getSimpleInt());
        assertEquals(5, ranges.getValue().getRange().getEndInclusive().intValue());
    }

    @Test
    public void rangeOrIntMixed() {
        Attribute<IntOrIntRange> ranges = printSpeedSupported.of(new IntOrIntRange(5), new IntOrIntRange(5, 6));
        assertEquals(5, ranges.get(0).getStart());
    }

    @Test
    public void range() {
        IntOrIntRange range = new IntOrIntRange(5, 6);
        assertEquals(new IntRange(5, 6), range.getValue());
    }

    @Test
    public void cover() {
        KotlinTest.cover(new IntOrIntRange(5), new IntOrIntRange(5), new IntOrIntRange(5, 6));
    }

    @Test
    public void equality() {
        IntOrIntRange value = new IntOrIntRange(5);
        assertEquals(value, value);
        assertEquals(new IntOrIntRange(5), value);
        assertNotEquals(new IntOrIntRange(6), value);
        assertNotEquals(5, value);
    }

    @Test
    public void of() {
        assertEquals(
                printSpeedSupported.of(new IntOrIntRange(new IntRange(1, 2)), new IntOrIntRange(new IntRange(3, 4))),
                printSpeedSupported.of(new IntRange(1, 2), new IntRange(3, 4)));
    }

    @Test
    public void failCoerce() {
        KeywordType wrongType = new KeywordType("range-or-int");
        assertNull(groupOf(Tag.jobAttributes, wrongType.of("utf-8")).get(Types.printSpeedSupported));
    }
}
