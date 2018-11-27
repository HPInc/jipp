package com.hp.jipp.encoding;

import com.hp.jipp.model.DocumentState;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.BuildError;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import kotlin.text.Charsets;

public class AttributeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test public void intAttr() throws IOException {
        cycle(new UnknownAttribute("integer", Collections.singletonList(5)));
    }

    @Test public void boolAttr() throws IOException {
        cycle(new UnknownAttribute("boolean", Collections.singletonList(true)));
    }

    @Test public void enumAttr() throws IOException {
        cycle(new UnknownAttribute("enum", Collections.singletonList(new UntypedEnum(5))));
    }

    @Test public void multiTypeAttr() throws IOException {
        cycle(new UnknownAttribute("multi", Arrays.asList(new UntypedEnum(5), true)));
    }

    @SuppressWarnings("unchecked")
    @Test public void emptyAttr() throws IOException {
        cycle(new EmptyAttribute("out-of-band", Tag.unknown));
    }

    @SuppressWarnings("unchecked")
    @Test public void unknownAttr() throws IOException {
        assertEquals(Tag.unknown, cycle(Attribute.unknown("unknown-attribute")).getTag());
    }

    @SuppressWarnings("unchecked")
    @Test public void unsupportedAttr() throws IOException {
        assertEquals(Tag.unsupported, cycle(Attribute.unsupported("unsupported-attribute")).getTag());
    }

    @SuppressWarnings("unchecked")
    @Test public void noValueAttr() throws IOException {
        assertEquals(Tag.noValue, cycle(Attribute.noValue("no-value-attribute")).getTag());
    }

    @Test public void coerceAttr() {
        UnknownAttribute intAttr = new UnknownAttribute("document-state", new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6));
        DocumentState.Type documentStateType = new DocumentState.Type("document-state");
        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), documentStateType.coerce(intAttr));
    }

    @Test
    public void octetString() throws Exception {
        AttributeType<byte[]> octetsType = new OctetsType("name");
        Attribute<byte[]> attribute = octetsType.of("value".getBytes(Charsets.UTF_8));
        assertArrayEquals(new byte[] {
                (byte)0x30, // octetString
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        }, toBytes(attribute));
        attribute = cycle(attribute);
        assertNull(attribute.getTag());
        assertEquals("name", attribute.getName());
        assertArrayEquals("value".getBytes(Charsets.UTF_8), attribute.get(0));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void parseOtherOctet() {
        byte[] bytes = new byte[] {
                (byte)0x39, // Reserved octetString type
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        };
        Attribute<?> attribute = AttributeGroup.Companion.readNextAttribute(new IppInputStream(new ByteArrayInputStream(bytes)));
        assertEquals("name", attribute.getName());
        OtherOctets expected = new OtherOctets(Tag.fromInt(0x39), "value".getBytes(Charsets.UTF_8));
        assertEquals(OtherOctets.class, attribute.getValue().getClass());
        assertArrayEquals("value".getBytes(Charsets.UTF_8), ((OtherOctets)attribute.getValue()).getValue());
        assertEquals(expected, attribute.getValue());
        assertEquals(expected.hashCode(), attribute.getValue().hashCode());
        assertTrue(attribute.getValue().toString().contains("76616c7565"));
        assertTrue(attribute.getValue().toString().contains("tag(x39)"));
    }

    @Test
    public void equality() {
        Attribute<String> charsetAttr = Types.attributesCharset.of("one", "two", "three");
        Attribute<String> charsetAttr2 = Types.attributesCharset.of("one", "two", "three");
        assertEquals(charsetAttr, charsetAttr);
        assertEquals(charsetAttr, charsetAttr2);
        assertNotEquals(charsetAttr, 5);
        assertNotEquals(charsetAttr, Types.attributesCharset.empty(Tag.unsupported));
        assertNotEquals(charsetAttr, Types.attributesNaturalLanguage.of("one", "two", "three"));

        // Different metadata means different object:
        Attribute<String> natLangAttr = Types.attributesNaturalLanguage.of("one", "two", "three");
        assertNotEquals(charsetAttr, natLangAttr);

        // equals must be symmetric with equivalent List
        assertEquals(Arrays.asList("one", "two", "three"), charsetAttr);
        assertEquals(charsetAttr, Arrays.asList("one", "two", "three"));
        assertEquals(Arrays.asList("one", "two", "three").hashCode(), charsetAttr.hashCode());
    }

    @Test
    public void collectionOperations() {
        Attribute<String> attr = Types.attributesCharset.of("one", "two", "three");
        coverList(attr, "one", "four");
    }

    @Test
    public void empty() {
        Attribute<String> empty = Types.attributesCharset.empty(Tag.unsupported);
        assertEquals("attributes-charset(unsupported)", empty.toString());
        assertNull(empty.getValue());
    }

    @Test
    public void failEmpty() {
        try {
            Types.attributesCharset.empty(Tag.octetString);
            fail("Didn't throw build error");
        } catch (BuildError ignored) {
        }

        try {
            new BaseAttribute<>("name", null, null, Collections.<String>emptyList());
            fail("Didn't throw build error");
        } catch (BuildError ignored) {
        }
    }
}
