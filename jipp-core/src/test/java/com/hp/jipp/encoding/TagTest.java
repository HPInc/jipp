package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;

import org.junit.Test;

public class TagTest {

    @Test
    public void cover() throws Exception {
        KotlinTest.cover(Tag.booleanValue,
                Tag.booleanValue.copy(Tag.booleanValue.component1(), Tag.booleanValue.component2()),
                Tag.charset);
    }
}
