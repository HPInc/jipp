package com.hp.jipp.encoding;

import com.hp.jipp.model.DocumentState;
import com.hp.jipp.model.Types;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.hp.jipp.encoding.Cycler.coverList;
import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

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

    @Test public void emptyAttr() throws IOException {
        cycle(new EmptyAttribute<Void>("out-of-band", Tag.unknown));
    }

    @Test public void coerceAttr() {
        UnknownAttribute intAttr = new UnknownAttribute("document-state", new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6));
        DocumentState.Type documentStateType = new DocumentState.Type("document-state");
        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.procesing, DocumentState.processingStopped
        ), documentStateType.coerce(intAttr));
    }

    @Test public void emptyAttrs() throws IOException {
        // Different ways to get to the same empty attributes
        assertEquals(new IntType("novalue-type").unknown(), cycle(Attributes.unknown("unknown-type")));
        assertEquals(new IntType("novalue-type").noValue(), cycle(Attributes.noValue("novalue-type")));
        assertEquals(new IntType("unsupported-type").unsupported(),
                cycle(Attributes.unsupported("unsupported-type")));
    }

    @Test
    public void equality() {
        Attribute<String> charsetAttr = Types.attributesCharset.of("one");
        Attribute<String> charsetAttr2 = Types.attributesCharset.of("one");
        assertEquals(charsetAttr, charsetAttr);
        assertEquals(charsetAttr, charsetAttr2);
        assertNotEquals(charsetAttr, 5);
        assertNotEquals(charsetAttr, Types.attributesCharset.empty(Tag.unsupported));
        assertNotEquals(charsetAttr, Types.attributesNaturalLanguage.of("one"));

        // Different metadata means different object:
        Attribute<String> natLangAttr = Types.attributesNaturalLanguage.of("one");
        assertNotEquals(charsetAttr, natLangAttr);

        // equals must be symmetric with equivalent List
        assertEquals(Collections.singletonList("one"), charsetAttr);
        assertEquals(charsetAttr, Collections.singletonList("one"));
        assertEquals(Collections.singletonList("one").hashCode(), charsetAttr.hashCode());
    }

    @Test
    public void collectionOperations() {
        Attribute<String> attr = Types.attributesCharset.of("one");
        coverList(attr, "one", "four");
    }

    @Test
    public void empty() {
        Attribute<String> empty = Types.attributesCharset.empty(Tag.unsupported);
        assertEquals("attributes-charset(unsupported)", empty.toString());
        assertNull(empty.getValue());
    }
}
