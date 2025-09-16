// © Copyright 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.model.Media;
import com.hp.jipp.model.MediaCol;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MediaSizesTest {
    @Test public void parseA5() {
        MediaCol.MediaSize a5 = MediaSizes.parse(Media.isoA5_148x210mm);
        assertEquals(14800, a5.getXDimension().intValue());
        assertEquals(21000, a5.getYDimension().intValue());
    }
}
