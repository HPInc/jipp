// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResolutionTest {
    Resolution dpi600 = new Resolution(600, 600, ResolutionUnit.dotsPerInch);

    @Test
    public void equality() {
        KotlinTest.cover(dpi600,
                dpi600.copy(600, 600, ResolutionUnit.dotsPerInch),
                    dpi600.copy(600, 599, ResolutionUnit.dotsPerInch));
    }

    @Test
    public void create() {
        assertEquals(ResolutionUnit.dotsPerInch, ResolutionUnit.Companion.get(ResolutionUnit.Code.dotsPerInch));
        assertEquals(5, ResolutionUnit.Companion.get(5).getCode());
    }
}
