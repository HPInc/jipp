
package com.hp.jipp.encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.*;

public class Cycler {

    public static <T> Attribute<T> cycle(AttributeType<T> type, Attribute<T> attribute) throws IOException {
        return cycle(groupOf(Tag.printerAttributes, attribute)).get(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> cycle(Attribute<T> attribute) throws IOException {
        return (Attribute<T>) cycle(groupOf(Tag.printerAttributes, Collections.singletonList(attribute)))
                .get(0);
    }

    /** Write group to a byte stream and then read it back and assert that the contents are identical */
    public static AttributeGroup cycle(AttributeGroup group) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytes);
        group.write(output);
        output.close();

        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return AttributeGroup.read(input, Tag.read(input));
    }

    /** Return a packet that was written to a byte stream and read back in */
    public static IppPacket cycle(IppPacket packet) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytes);
        packet.write(output);
        output.close();

        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return input.readPacket();
    }

    public static byte[] toBytes(IppPacket packet) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        packet.write(out);
        return bytesOut.toByteArray();
    }

    static byte[] toBytes(Attribute<?> attribute)  {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream out = new IppOutputStream(bytesOut);
        AttributeGroup.Companion.writeAttribute(out, attribute, attribute.getName());
        return bytesOut.toByteArray();
    }

    static <T> void coverList(List<T> list, T firstValue, T notPresentValue) {

        assertFalse(list.isEmpty());
        assertTrue(list.contains(firstValue));
        assertEquals(0, list.indexOf(firstValue));
        assertEquals(0, list.lastIndexOf(firstValue));
        //noinspection RedundantCollectionOperation
        assertTrue(list.containsAll(Collections.singletonList(firstValue)));
        assertEquals(Collections.singletonList(firstValue), list.subList(0, 1));
        assertEquals(firstValue, list.toArray()[0]);
        assertEquals(firstValue, list.get(0));

        //noinspection LoopStatementThatDoesntLoop (We want to test iterator)
        for (T item : list) {
            assertEquals(item, firstValue);
            break;
        }

        try {
            list.clear();
            fail("clear() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.set(0, notPresentValue);
            fail("set() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.remove(firstValue);
            fail("remove() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.remove(0);
            fail("remove() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.add(firstValue);
            fail("add() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.addAll(Arrays.asList(firstValue, notPresentValue));
            fail("addAll() didn't throw");
        } catch (UnsupportedOperationException ignored) { }

        try {
            list.removeAll(Arrays.asList(firstValue, notPresentValue));
            fail("removeAll() didn't throw");
        } catch (UnsupportedOperationException ignored) { }
    }
}
