package com.hp.jipp.encoding;

import com.hp.jipp.util.KotlinTest;

import org.junit.Test;

public class TagTest {

    @Test
    public void cover() throws Exception {
        KotlinTest.cover(Tag.BooleanValue,
                Tag.BooleanValue.copy(Tag.BooleanValue.component1(), Tag.BooleanValue.component2()),
                Tag.Charset);
    }
}
