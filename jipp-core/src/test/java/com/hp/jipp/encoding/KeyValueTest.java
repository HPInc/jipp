// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import kotlin.Pair;
import org.junit.Test;

import java.util.*;
import java.util.function.BiFunction;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;


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

    @Test
    public void equality() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue");
        assertNotEquals(kv, 5);
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "oneValue");
        map.put("two", "twoValue");
        assertEquals(kv, map);
    }

    @Test
    public void mapMethods() throws Exception {
        KeyValues kv = new KeyValues("key5", "value5", "key1", "value1");
        assertEquals(kv, kv);
        assertTrue(kv.containsKey("key1"));
        assertTrue(kv.containsValue("value1"));
        assertEquals("value5", kv.get("key5"));
        assertEquals(2, kv.size());
        assertEquals(kv.hashCode(), new KeyValues("key5", "value5", "key1", "value1").hashCode());
        assertFalse(kv.isEmpty());
        Map<String, String> map = new HashMap<String, String>(kv);
        assertEquals(map, kv);
        assertEquals(kv, map);
        assertNotEquals(5, kv);
        assertEquals(map.hashCode(), kv.hashCode());
        assertEquals(new HashSet<String>(Arrays.asList("key1", "key5")), kv.keySet());
        assertTrue(kv.getValues().containsAll(Arrays.asList("value1", "value5")));
        coverUnmodifiableMap(kv);
    }

    private void coverUnmodifiableMap(Map<String, String> map) {
        try {
            map.clear();
            fail("clear() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.remove("");
            fail("remove() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.remove("", "");
            fail("remove() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.put("", "");
            fail("put() didn't throw");
        } catch (UnsupportedOperationException ignored) { }


        try {
            map.putIfAbsent("", "");
            fail("putIfAbsent() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.putAll(new HashMap<>());
            fail("putAll() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.replace("one", "two");
            fail("replace() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.replace("one", "two", "three");
            fail("replace() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            map.replaceAll((s, s2) -> "");
            fail("replaceAll() didn't throw");
        } catch (UnsupportedOperationException ignored) { }
    }
}
