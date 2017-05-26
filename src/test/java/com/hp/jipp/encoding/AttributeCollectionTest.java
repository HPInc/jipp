package com.hp.jipp.encoding;

import static com.hp.jipp.encoding.Cycler.*;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.util.KotlinTest;
import com.hp.jipp.util.ParseError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AttributeCollectionTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private CollectionType mediaColType = new CollectionType("media-col");
    private CollectionType mediaSizeType = new CollectionType("media-size");
    private StringType colorType = new StringType(Tag.Keyword, "media-color");
    private IntegerType xDimensionType = new IntegerType(Tag.IntegerValue, "x-dimension");
    private IntegerType yDimensionType = new IntegerType(Tag.IntegerValue, "y-dimension");

    @Test
    public void badCollection() throws IOException {
        exception.expect(ParseError.class);
        exception.expectMessage("Bad tag in collection: printer-attributes");

        byte[] bytes = new byte[] {
//                (byte)Tag.BeginCollection.getCode(), // Read already
                (byte)0x00,
                (byte)0x09,
                'm', 'e', 'd' , 'i', 'a', '-', 'c', 'o', 'l',
                (byte)0x00,
                (byte)0x00,
                (byte) Tag.PrinterAttributes.getCode(), // NOT a good delimiter
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00
        };
        Attribute.Companion.read(new DataInputStream(new ByteArrayInputStream(bytes)), sFinder, Tag.BeginCollection);
    }

    @Test
    public void collection() throws IOException {
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


        Attribute<AttributeCollection> mediaCol = mediaColType.of(
                new AttributeCollection(
                        colorType.of("blue"),
                        mediaSizeType.of(
                                new AttributeCollection(
                                        xDimensionType.of(6),
                                        yDimensionType.of(4)),
                                new AttributeCollection(
                                        xDimensionType.of(12),
                                        yDimensionType.of(5))

                        )));

        mediaCol = cycle(mediaCol);

        // Spot-check elements of the collection
        assertEquals("media-col", mediaCol.getName());
        assertEquals("blue", mediaCol.getValues().get(0).values(colorType).get(0));
        assertEquals(Integer.valueOf(12),
                mediaCol.getValues().get(0)
                        .values(mediaSizeType).get(1)
                        .values(xDimensionType).get(0));

        // Make sure we're covering some empty cases
        assertFalse(mediaCol.getValues().get(0).get(xDimensionType).isPresent());
        assertEquals(0, mediaCol.getValues().get(0).values(xDimensionType).size());

        // Output is helpful for inspection
        System.out.println(mediaCol);
    }

    @Test
    public void testGet() {
        AttributeCollection collection =new AttributeCollection(
                        colorType.of("blue"),
                        mediaSizeType.of(
                                new AttributeCollection(
                                        xDimensionType.of(6),
                                        yDimensionType.of(4)),
                                new AttributeCollection(
                                        xDimensionType.of(12),
                                        yDimensionType.of(5))

                        ));

        assertEquals(colorType.of("blue"), collection.get(colorType).get());
        assertEquals(Optional.absent(), collection.get(new StringType(Tag.Keyword, "media-not-color")));
    }

    @Test
    public void emptyCollection() {
        AttributeCollection collection = new AttributeCollection();
        assertEquals(Optional.absent(), collection.get(colorType));
    }

    @Test
    public void cover() {
        AttributeCollection collection = new AttributeCollection();
        KotlinTest.cover(collection,
                collection.copy(collection.component1()),
                collection.copy(ImmutableList.of(colorType.of("blue"))));
    }
}
