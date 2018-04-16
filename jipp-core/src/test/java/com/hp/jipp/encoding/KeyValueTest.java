package com.hp.jipp.encoding;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KeyValueTest {
    private KeyValueType keyValueType = new KeyValueType("key-value");

    @Test
    public void empty() throws Exception {
        Map<String, String> value = Collections.emptyMap();
        @SuppressWarnings("unchecked") Attribute<Map<String, String>> attribute = keyValueType.of(value);
        assertEquals(value, cycle(keyValueType, attribute).get(0));
    }

    @Test
    public void notEmpty() throws Exception {
        Map<String, String> value = new HashMap<String, String>();
        value.put("one", "two");
        value.put("three", "four");
        Attribute<Map<String, String>> attribute = keyValueType.of(Collections.singletonList(value));
        assertEquals(value, cycle(keyValueType, attribute).get(0));
    }
}
