package com.hp.jipp.encoding;

import java.io.IOException;
import java.util.Arrays;
import kotlin.ranges.IntRange;
import org.junit.Test;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntRangeTypeTest {
    private IntRangeType.Set rangeType = new IntRangeType.Set("range-only");
    private IntOrIntRangeType.Set rangeOrIntType = new IntOrIntRangeType.Set("range-or-int");

    @Test
    public void simpleRange() throws IOException {
        Attribute<IntRange> range = cycle(rangeType, rangeType.of(new IntRange(0, 100)));
        assertEquals(0, range.getValue().getStart().intValue());
        assertEquals(100, range.getValue().getEndInclusive().intValue());
    }

    @Test
    public void multiRange() throws IOException {
        Attribute<IntRange> ranges = cycle(rangeType, rangeType.of(new IntRange(0, 100), new IntRange(90, 1000)));
        assertEquals(0, ranges.getValue().getStart().intValue());
        assertEquals(100, ranges.getValue().getEndInclusive().intValue());
        assertEquals(90, ranges.get(1).getStart().intValue());
        assertEquals(1000, ranges.get(1).getEndInclusive().intValue());
    }

    @Test
    public void rangeOrInt() throws IOException {
        Attribute<IntOrIntRange> ranges = cycle(rangeOrIntType, rangeOrIntType.of(new IntOrIntRange(5)));
        assertTrue(ranges.getValue().getSimpleInt());
        assertEquals(5, ranges.getValue().getRange().getEndInclusive().intValue());
    }

    @Test
    public void rangeOrIntMixed() throws IOException {
        // It's legal to include both types, so coerce them to ranges
        Attribute<IntOrIntRange> ranges = rangeOrIntType.of(new IntOrIntRange(5), new IntOrIntRange(5, 6));
        assertTrue(ranges.get(0).getSimpleInt());
    }
}
