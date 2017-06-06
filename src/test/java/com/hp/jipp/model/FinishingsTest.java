package com.hp.jipp.model;

import com.hp.jipp.util.KotlinTest;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class FinishingsTest {
    @Test
    public void testFinishings() throws Exception {
        Finishings finishings[] = new Finishings[] { Finishings.Bind, Finishings.Cover };

        assertEquals(Arrays.asList(finishings),
                cycle(Attributes.FinishingsSupported.of(finishings)).getValues());
    }

    @Test
    public void cover() throws Exception {
        KotlinTest.cover(Attributes.FinishingsSupported.of(Finishings.EdgeStitch),
                cycle(Attributes.FinishingsSupported.of(Finishings.EdgeStitch)),
                Attributes.FinishingsSupported.of(Finishings.ENCODER.get(88)));
    }
}
