package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.util.Util;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.Operation;

import static com.hp.jipp.encoding.Cycler.*;

public class AttributeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void octetString() throws IOException {
        AttributeType<byte[]> octetStringType = new OctetStringType(Tag.OctetString, "name");
        Attribute<byte[]> attribute = octetStringType.of("value".getBytes(Util.UTF8));
        assertArrayEquals(new byte[] {
                (byte)0x30, // OctetString
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        }, toBytes(attribute));
        attribute = cycle(attribute);
        assertEquals(Tag.OctetString, attribute.getValueTag());
        assertEquals("name", attribute.getName());
        assertArrayEquals("value".getBytes(Util.UTF8), attribute.getValue(0));
    }

    @Test
    public void multiOctetString() throws IOException {
        AttributeType<byte[]> stringType = new OctetStringType(Tag.NameWithoutLanguage, "name");
        Attribute<byte[]> attribute = stringType.of("value".getBytes(Util.UTF8), "value2".getBytes(Util.UTF8));
        assertArrayEquals("value".getBytes(Util.UTF8), attribute.getValue(0));
        assertArrayEquals("value2".getBytes(Util.UTF8), attribute.getValue(1));
    }

    @Test
    public void multiBoolean() throws IOException {
        AttributeType<Boolean> booleanType = new BooleanType(Tag.BooleanValue, "name");
        Attribute<Boolean> attribute = cycle(booleanType.of(true, false));
        assertEquals(ImmutableList.of(true, false), attribute.getValues());
    }

    @Test
    public void multiInteger() throws IOException {
        AttributeType<Integer> integerType = new IntegerType(Tag.IntegerValue, "name");
        Attribute<Integer> attribute = cycle(integerType.of(-50505, 50505));
        assertEquals(ImmutableList.of(-50505, 50505), attribute.getValues());
    }

    @Test
    public void enumAttribute() throws IOException {
        AttributeGroup group = Cycler.cycle(AttributeGroup.create(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(
                        Operation.CancelJob, Operation.GetJobAttributes, Operation.CreateJob)));
        assertEquals(ImmutableList.of(Operation.CancelJob, Operation.GetJobAttributes, Operation.CreateJob),
                group.getValues(Attributes.OperationsSupported));
    }

    @Test
    public void surpriseEnum() throws IOException {
        AttributeGroup group = Cycler.cycle(AttributeGroup.create(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(
                        Operation.create("vendor-specific", 0x4040))));
        // We can't know it's called "vendor-specific" after parsing, since we just made it up.
        // So expect the unrecognized format
        assertEquals(ImmutableList.of(Operation.create("operation-id(x4040)", 0x4040)),
                group.getValues(Attributes.OperationsSupported));
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

        CollectionType mediaColType = new CollectionType("media-col");
        CollectionType mediaSizeType = new CollectionType("media-size");
        StringType colorType = new StringType(Tag.Keyword, "media-color");
        IntegerType xDimensionType = new IntegerType(Tag.IntegerValue, "x-dimension");
        IntegerType yDimensionType = new IntegerType(Tag.IntegerValue, "y-dimension");

        Attribute<AttributeCollection> mediaCol = mediaColType.of(
                AttributeCollection.of(
                        colorType.of("blue"),
                        mediaSizeType.of(
                                AttributeCollection.of(
                                        xDimensionType.of(6),
                                        yDimensionType.of(4)),
                                AttributeCollection.of(
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
    public void invalidTag() {
        exception.expect(BuildError.class);
        // String is not Integer; should throw.
        new StringType(Tag.IntegerValue, "something");
    }

    @Test
    public void languageStrings() throws Exception {
        LangStringType nameType = new LangStringType(Tag.NameWithLanguage, "job-name");
        Attribute<LangString> name = cycle(nameType.of(LangString.of("my job", "fr")));
        System.out.println("name: " + name);
        assertEquals("my job", name.getValue(0).getString());
        assertEquals("fr", name.getValue(0).getLang().get());
    }
}
