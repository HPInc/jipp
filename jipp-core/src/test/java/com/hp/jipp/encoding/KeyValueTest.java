package com.hp.jipp.encoding;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import java.util.Collections;

public class KeyValueTest {

    private KeyValueType keyValueType = new KeyValueType("key-value");

    @Test
    public void empty() throws Exception {
        KeyValues value = new KeyValues();
        @SuppressWarnings("unchecked") Attribute<KeyValues> attribute = keyValueType.of(value);
        assertEquals(value.getPairs(), cycle(keyValueType, attribute).getValue().getPairs());
    }

    @Test
    public void notEmpty() throws Exception {
        KeyValues value = new KeyValues("one", "oneValue", "two", "twoValue");
        Attribute<KeyValues> attribute = keyValueType.of(Collections.singletonList(value));
        assertEquals(value.getPairs(), cycle(keyValueType, attribute).getValue().getPairs());
    }
}
