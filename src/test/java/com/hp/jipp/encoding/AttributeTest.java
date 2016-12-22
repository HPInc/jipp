package com.hp.jipp.encoding;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AttributeTest {

    @Test
    public void octetString() throws IOException {
        OctetAttribute attribute = new OctetAttribute(Tag.NameWithoutLanguage, "name", "value".getBytes());
        assertArrayEquals(new byte[] {
                (byte)0x42,
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        }, toBytes(attribute));
        attribute = cycleOctet(attribute);
        assertEquals(Tag.NameWithoutLanguage, attribute.getValueTag());
        assertEquals("name", attribute.getName());
        assertArrayEquals("value".getBytes(), attribute.getValue(0));
    }

    @Test
    public void multiOctetString() throws IOException {
        OctetAttribute attribute = new OctetAttribute(Tag.NameWithoutLanguage, "name",
                "value".getBytes(),
                "value2".getBytes());
        assertArrayEquals("value".getBytes(), attribute.getValue(0));
        assertArrayEquals("value2".getBytes(), attribute.getValue(1));
    }


    @Test
    public void multiBoolean() throws IOException {
        BooleanAttribute attribute = cycleBoolean(new BooleanAttribute(Tag.BooleanValue, "name", true, false));
        assertEquals(new ArrayList<Boolean>() {{
            add(true);
            add(false);
        }}, attribute.getValues());
    }


    @Test
    public void multiInteger() throws IOException {
        IntegerAttribute attribute = cycleInteger(new IntegerAttribute(Tag.IntegerValue, "name", -50505, 50505));
        assertEquals(new ArrayList<Integer>() {{
            add(-50505);
            add(50505);
        }}, attribute.getValues());
    }

    @Test
    public void collection() throws IOException {
        Map<String, Attribute> media = new HashMap<>();
        Map<String, Attribute> mediaSize1= new HashMap<>();
        Map<String, Attribute> mediaSize2 = new HashMap<>();
        // Let's encode:
        // media-col = {
        //   media-color: blue,
        //   media-size: [ {
        //       x-dimension = 6,
        //       y-dimension = 4
        //     }, {
        //       x-dimension = 12,
        //       y-dimension = 5
        //     }
        //  }
        mediaSize1.put("x-dimension", new IntegerAttribute(Tag.IntegerValue, "", 6));
        mediaSize1.put("y-dimension", new IntegerAttribute(Tag.IntegerValue, "", 4));
        mediaSize2.put("x-dimension", new IntegerAttribute(Tag.IntegerValue, "", 12));
        mediaSize2.put("y-dimension", new IntegerAttribute(Tag.IntegerValue, "", 5));
        CollectionAttribute mediaSizes = new CollectionAttribute("", mediaSize1, mediaSize2);
        media.put("media-color", new StringAttribute(Tag.Keyword, "", "blue"));
        media.put("media-size", mediaSizes);
        CollectionAttribute mediaCol = cycleCollection(
                new CollectionAttribute("media-col", media));

        assertEquals("media-col", mediaCol.getName());
        assertEquals("blue", mediaCol.getValues().get(0).get("media-color").getValues().get(0));
        assertEquals(6, ((CollectionAttribute)mediaCol.getValue(0).get("media-size"))
                .getValue(0).get("x-dimension").getValue(0));
    }

    private OctetAttribute cycleOctet(OctetAttribute attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return OctetAttribute.read(in, Tag.read(in));
    }

    private BooleanAttribute cycleBoolean(BooleanAttribute attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return BooleanAttribute.read(in, Tag.read(in));
    }

    private IntegerAttribute cycleInteger(IntegerAttribute attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return IntegerAttribute.read(in, Tag.read(in));
    }

    private CollectionAttribute cycleCollection(CollectionAttribute attribute) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(toBytes(attribute)));
        return CollectionAttribute.read(in, Tag.read(in));
    }

    private byte[] toBytes(Attribute attribute) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        attribute.write(new DataOutputStream(bytesOut));
        return bytesOut.toByteArray();
    }
}
