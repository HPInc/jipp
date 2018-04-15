package com.hp.jipp.model;

import com.hp.jipp.util.KotlinTest;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class FinishingsTest {
    @Test
    public void testFinishings() throws Exception {
        Finishings finishings[] = new Finishings[] { Finishings.bind, Finishings.cover};

        assertEquals(Arrays.asList(finishings),
                cycle(Types.finishingsSupported.of(finishings)).getValues());
    }

    @Test
    public void cover() throws Exception {
        KotlinTest.cover(Types.finishingsSupported.of(Finishings.edgeStitch),
                cycle(Types.finishingsSupported.of(Finishings.edgeStitch)),
                Types.finishingsSupported.of(Finishings.Encoder.get(88)));
    }
}
