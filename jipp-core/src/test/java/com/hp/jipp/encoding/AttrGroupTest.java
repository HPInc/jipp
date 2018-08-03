package com.hp.jipp.encoding;

import com.hp.jipp.pwg.DocumentState;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class AttrGroupTest {

    @Test public void emptyGroup() throws IOException {
        cycle(new AttributeGroup(Tag.printerAttributes, Collections.<Attribute<Object>>emptyList()));
    }

    @Test public void intAttr() throws IOException {
        cycle(new UnknownAttribute("integer", Arrays.asList(5)));
    }

    @Test public void boolAttr() throws IOException {
        cycle(new UnknownAttribute("boolean", Arrays.asList(true)));
    }

    @Test public void enumAttr() throws IOException {
        cycle(new UnknownAttribute("enum", Arrays.asList(new UntypedEnum(5))));
    }

    @Test public void multiTypeAttr() throws IOException {
        cycle(new UnknownAttribute("multi", Arrays.asList(new UntypedEnum(5), true)));
    }

    @Test public void emptyAttr() throws IOException {
        cycle(new EmptyAttribute("out-of-band", Tag.unknown));
    }

    @Test public void coerceAttr() {
        UnknownAttribute intAttr = new UnknownAttribute("document-state", new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6));
        DocumentState.Type documentStateType = new DocumentState.Type("document-state");
        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), documentStateType.coerce(intAttr));
    }

    @Test public void groupExtract() {
        AttributeType<Object> untypedDocumentState = new UnknownAttribute.Type("document-state");


        AttributeGroup group = new AttributeGroup(Tag.operationAttributes,
                untypedDocumentState.of(new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6)));

        DocumentState.Type documentStateType = new DocumentState.Type("document-state");

        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), group.get(documentStateType));
    }
}
