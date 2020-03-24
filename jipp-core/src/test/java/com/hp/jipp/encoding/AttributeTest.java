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
        assertEquals(Tag.unknown, cycle(Attributes.unknown("unknown-attribute")).getTag());
    }

    @SuppressWarnings("unchecked")
    @Test public void unsupportedAttr() throws IOException {
        assertEquals(Tag.unsupported, cycle(Attributes.unsupported("unsupported-attribute")).getTag());
    }

    @SuppressWarnings("unchecked")
    @Test public void noValueAttr() throws IOException {
        assertEquals(Tag.noValue, cycle(Attributes.noValue("no-value-attribute")).getTag());
    }

    @Test public void coerceAttr() {
        UnknownAttribute intAttr = new UnknownAttribute("document-state", new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6));
        DocumentState.Type documentStateType = new DocumentState.Type("document-state");
        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), documentStateType.coerce(intAttr));
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
