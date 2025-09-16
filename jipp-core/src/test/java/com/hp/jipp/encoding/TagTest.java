// © Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import com.hp.jipp.util.BuildError;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TagTest {
    @Test(expected = BuildError.class)
    public void badDelimiterTag() {
        new DelimiterTag(0x80, "invalid");
    }

    @Test(expected = BuildError.class)
    public void badOutOfBandTag() {
        new OutOfBandTag(0x80, "invalid");
    }

    @Test
    public void newOutOfBand() {
        new OutOfBandTag(0x1F, "vendor");
    }

    @Test
    public void equality() {
        Set<Tag> set = new HashSet<>();
        set.add(Tag.adminDefine);
        set.add(Tag.booleanValue);
        set.add(Tag.operationAttributes);
        assertTrue(set.contains(new OutOfBandTag(Tag.adminDefine.getCode(), "unnecessary tag redefinition")));
        assertTrue(set.contains(new DelimiterTag(Tag.operationAttributes.getCode(), "unnecessary tag redefinition")));
        assertEquals(new OutOfBandTag(0x1f, "vendor"), Tag.fromInt(0x1F));
        assertNotEquals(Tag.adminDefine, "not");
        assertNotEquals(Tag.operationAttributes, "not");
        assertNotEquals(Tag.booleanValue, "not");
    }

    @Test
    public void typeCheck() {
        assertTrue(Tag.beginCollection.isCollection());
        assertTrue(Tag.endCollection.isCollection());
        assertTrue(Tag.memberAttributeName.isCollection());
        assertTrue(Tag.integerValue.isInteger());
        assertTrue(Tag.keyword.isCharString());
        assertTrue(Tag.textWithLanguage.isOctetString());
        assertFalse(Tag.operationAttributes.isInteger());
        assertFalse(Tag.nameWithoutLanguage.isInteger());
        assertFalse(Tag.booleanValue.isCharString());
        assertFalse(new ValueTag(0x78, "weird").isCharString());
        assertFalse(Tag.endOfAttributes.isOctetString());
        assertTrue(Tag.integerValue.isInteger());
        assertFalse(Tag.mimeMediaType.isCollection());
    }

    @Test
    public void cycleWeirdAttributes() throws IOException {
        MutableAttributeGroup group = new MutableAttributeGroup(Tag.operationAttributes);
        group.put(Types.logoUriSchemesSupported.of("abc"));
        AttributeGroup cycled = cycle(group);
        assertEquals("abc", cycled.getValue(Types.logoUriSchemesSupported));
    }
}
