// Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test behavior of NameType.
 *
 * From https://tools.ietf.org/html/rfc8011#section-5.1.3:
 *
 * <blockquote>
 *   This syntax type is used for user-friendly strings, such as a Printer
 *   name, that, for humans, are more meaningful than identifiers.  Names
 *   are never translated from one natural language to another.  The
 *   'name' attribute syntax is essentially the same as 'text', including
 *   the REQUIRED support of UTF-8, except that the sequence of characters
 *   is limited so that its encoded form MUST NOT exceed 255 (MAX) octets.
 *
 *   Also, like 'text', 'name' is really an abbreviated notation for
 *   either 'nameWithoutLanguage' or 'nameWithLanguage'.  That is, all IPP
 *   objects and Clients MUST support both the 'nameWithoutLanguage' and
 *   'nameWithLanguage' attribute syntaxes.  However, in actual usage and
 *   protocol execution, IPP objects and Clients accept and return only
 *   one of the two syntaxes per attribute.  The syntax 'name' never
 *   appears "on-the-wire".
 *
 *    Only the 'text' and 'name' attribute syntaxes permit the Natural
 *    Language Override mechanism.
 * </blockquote>
 *
 * This means we should be able to create an attribute type which takes
 * strings OR named strings to produce an attribute which retains the
 * appropriate value tag and encodes them properly, and parses such
 * attributes correctly, and preserving the language when given.
 */
public class NameTest {

    private NameType.Set jobNameType = new NameType.Set("job-name");

    @Test
    public void withoutLanguage() throws IOException {
        Attribute<Name> jobNameAttr = jobNameType.of(new Name("my job"));
        Attribute<Name> result = cycle(jobNameType, jobNameAttr);
        assertEquals("my job", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void simpleStrings() throws IOException {
        Attribute<Name> jobNameAttr = jobNameType.of("my job");
        Attribute<Name> result = cycle(jobNameType, jobNameAttr);
        assertEquals("my job", result.get(0).getValue());
        assertNull(result.get(0).getLang());
    }

    @Test
    public void withLanguage() throws IOException {
        Attribute<Name> jobNameAttr = jobNameType.of(new Name("my job", "en"));
        Attribute<Name> result = cycle(jobNameType, jobNameAttr);
        assertEquals("my job", result.getValue().getValue());
        assertEquals("en", result.getValue().getLang());
    }

    @Test
    public void mixed() throws IOException {
        // Totally allowable
        assertEquals(Arrays.asList("my job", "another job"),
                cycle(jobNameType, jobNameType.of(new Name("my job", "en"), new Name("another job"))).strings());
    }
}
