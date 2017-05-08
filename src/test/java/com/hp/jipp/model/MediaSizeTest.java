package com.hp.jipp.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MediaSizeTest {
    @Test
    public void parse() {
        MediaSize a10 = MediaSize.of("iso_a10_26x37mm");
        assertEquals(2600, a10.getWidth());
        assertEquals(3700, a10.getHeight());

        MediaSize executive = MediaSize.of("na_executive_7.25x10.5in");
        assertEquals(18415, executive.getWidth());
        assertEquals(26670, executive.getHeight());
    }
}
