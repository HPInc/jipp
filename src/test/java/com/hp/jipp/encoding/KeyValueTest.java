package com.hp.jipp.encoding;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class KeyValueTest {
    private KeyValueType keyValueType = new KeyValueType("key-value");

    @Test
    public void empty() throws Exception {
        Map<String, String> value = ImmutableMap.of();
        Attribute<Map<String, String>> attribute = keyValueType.of(ImmutableList.of(value));
        assertEquals(value, cycle(keyValueType, attribute).getValue(0));
    }

    @Test
    public void notEmpty() throws Exception {
        Map<String, String> value = ImmutableMap.of("one", "two", "three", "four");
        Attribute<Map<String, String>> attribute = keyValueType.of(ImmutableList.of(value));
        assertEquals(value, cycle(keyValueType, attribute).getValue(0));
    }

}
