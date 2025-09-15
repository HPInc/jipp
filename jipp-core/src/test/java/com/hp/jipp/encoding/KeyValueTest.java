// © Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.model.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import kotlin.Pair;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KeyValueTest {

    private KeyValuesType keyValuesType = new KeyValuesType("key-value");

    @Test
    public void empty() throws Exception {
        KeyValues value = new KeyValues();
        Attribute<KeyValues> attribute = keyValuesType.of(value);
        assertEquals(value.getPairs(), cycle(keyValuesType, attribute).getValue().getPairs());
    }


    @Test
    public void notEmpty() throws Exception {
        KeyValues value = new KeyValues("one", "oneValue", "two", "twoValue");
        Attribute<KeyValues> attribute = keyValuesType.of(value);
        assertEquals(value, cycle(keyValuesType, attribute).getValue());
    }

    @Test
    public void construct() throws Exception {
        Attribute<KeyValues> attribute = keyValuesType.of(new KeyValues("one", "oneValue", "two", "twoValue"));
        assertEquals(attribute.getValue(), cycle(keyValuesType, attribute).getValue());
    }

    @Test
    public void map() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue");
        assertEquals("twoValue", kv.get("two"));
    }

    @Test
    public void pairs() throws Exception {
        @SuppressWarnings("unchecked")
        KeyValues kv = new KeyValues(new Pair<>("one", "oneValue"));
        assertEquals("oneValue", kv.get("one"));
    }

    @Test
    public void preserveOrder() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue", "three", "threeValue");
        System.out.println(kv);
        assertEquals(Arrays.asList("one", "two", "three"), new ArrayList<>(kv.getKeys()));
    }

    @Test
    public void nonCoerce() throws Exception {
        // Refuse to coerce anything but a bytearray or string
        assertEquals(new KeyValues("key5", "value5"), keyValuesType.coerce("key5=value5"));
        assertEquals(new KeyValues("key5", "value5"), keyValuesType.coerce("key5=value5;"));
        assertEquals(new KeyValues("key5", "value5"), keyValuesType.coerce("key5=value5;".getBytes()));
        assertNull(keyValuesType.coerce(5));
    }

    @Test
    public void equality() throws Exception {
        KeyValues kv = new KeyValues("one", "oneValue", "two", "twoValue");
        assertNotEquals(kv, 5);
        Map<String, String> map = new HashMap<>();
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
        Map<String, String> map = new HashMap<>(kv);
        assertEquals(map, kv);
        assertEquals(kv, map);
        assertNotEquals(5, kv);
        assertEquals(map.hashCode(), kv.hashCode());
        assertEquals(new HashSet<>(Arrays.asList("key1", "key5")), kv.keySet());
        assertTrue(kv.getValues().containsAll(Arrays.asList("value1", "value5")));
        coverUnmodifiableMap(kv);
    }

    @Test
    public void localPacket() throws IOException {
        Attribute<KeyValues> kv = keyValuesType.of(new KeyValues("one", "oneValue", "two", "twoValue"));
        // Note: NOT cycled
        IppPacket packet = new IppPacket(Status.successfulOk, 0x50607, groupOf(Tag.printerAttributes,
                kv));
        assertEquals(kv.getValue(), packet.getValue(Tag.printerAttributes, keyValuesType));
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
