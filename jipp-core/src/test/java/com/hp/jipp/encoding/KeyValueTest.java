// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import kotlin.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import java.util.*;

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
        assertEquals(value, cycle(keyValueType, attribute).getValue());
    }

    @Test
    public void construct() throws Exception {
        Attribute<KeyValues> attribute = keyValueType.of("one", "oneValue", "two", "twoValue");
        assertEquals(attribute.getValue(), cycle(keyValueType, attribute).getValue());
    }

    @Test
    public void map() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue");
        assertEquals("twoValue", kv.get("two"));
    }

    @Test
    public void pairs() throws Exception {
        @SuppressWarnings("unchecked")
        KeyValues kv = new KeyValues(new Pair<String, String>("one", "oneValue"));
        assertEquals("oneValue", kv.get("one"));
    }

    @Test
    public void preserveOrder() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue", "three", "threeValue");
        System.out.println(kv);
        assertEquals(Arrays.asList("one", "two", "three"), new ArrayList<String>(kv.getKeys()));
    }

    @Test
    public void nonCoerce() throws Exception {
        // Refuse to coerce anything but a bytearray or string
        assertEquals(new KeyValues("key5", "value5"), keyValueType.coerce("key5=value5"));
        assertEquals(new KeyValues("key5", "value5"), keyValueType.coerce("key5=value5;"));
        assertEquals(new KeyValues("key5", "value5"), keyValueType.coerce("key5=value5;".getBytes()));
        assertNull(keyValueType.coerce(5));
    }
}
