package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;
import kotlin.ranges.IntRange;
import org.junit.Test;

import java.io.IOException;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.*;

public class IntOrIntRangeTest {
    private IntRangeType rangeType = new IntRangeType("range-only");
    private IntOrIntRangeType rangeOrIntType = new IntOrIntRangeType("range-or-int");

    @Test
    public void simpleRange() throws IOException {
        Attribute<IntRange> range = cycle(rangeType, rangeType.of(new IntRange(0, 100)));
        assertEquals(0, range.get(0).getStart().intValue());
        assertEquals(100, range.get(0).getEndInclusive().intValue());
    }

    @Test
    public void multiRange() throws IOException {
        Attribute<IntRange> ranges = cycle(rangeType, rangeType.of(new IntRange(0, 100), new IntRange(90, 1000)));
        assertEquals(0, ranges.get(0).getStart().intValue());
        assertEquals(100, ranges.get(0).getEndInclusive().intValue());
        assertEquals(90, ranges.get(1).getStart().intValue());
        assertEquals(1000, ranges.get(1).getEndInclusive().intValue());
    }

    @Test
    public void rangeOrInt() throws IOException {
        Attribute<IntOrIntRange> ranges = cycle(rangeOrIntType, rangeOrIntType.of(5));
        assertTrue(ranges.get(0).getSimpleInt());
        assertEquals(5, ranges.getValue().getRange().getEndInclusive().intValue());
    }

    @Test
    public void rangeOrIntMixed() throws IOException {
        Attribute<IntOrIntRange> ranges = rangeOrIntType.of(new IntOrIntRange(5), new IntOrIntRange(5, 6));
        assertEquals(5, ranges.get(0).getStart());
    }

    @Test
    public void range() throws Exception {
        IntOrIntRange range = new IntOrIntRange(5, 6);
        assertEquals(new IntRange(5, 6), range.getValue());
    }

    @Test
    public void cover() throws Exception {
        KotlinTest.cover(new IntOrIntRange(5), new IntOrIntRange(5), new IntOrIntRange(5, 6));
    }

    @Test
    public void equality() throws Exception {
        IntOrIntRange value = new IntOrIntRange(5);
        assertEquals(value, value);
        assertEquals(new IntOrIntRange(5), value);
        assertNotEquals(new IntOrIntRange(6), value);
        assertNotEquals(5, value);
    }
}
