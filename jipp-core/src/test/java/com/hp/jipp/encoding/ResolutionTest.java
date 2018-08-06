package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResolutionTest {
    Resolution dpi600 = new Resolution(600, 600, ResolutionUnit.dotsPerInch);

    @Test
    public void equality() {
        KotlinTest.cover(dpi600,
                dpi600.copy(600, 600, ResolutionUnit.dotsPerInch),
                dpi600.copy(600, 599, ResolutionUnit.dotsPerInch));
    }
}
