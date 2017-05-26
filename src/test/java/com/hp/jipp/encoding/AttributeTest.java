package com.hp.jipp.encoding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

import com.hp.jipp.model.Status;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.KotlinTest;
import com.hp.jipp.util.ParseError;
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
        assertEquals(Arrays.asList(true, false), attribute.getValues());
    }

    @Test
    public void multiInteger() throws IOException {
        AttributeType<Integer> integerType = new IntegerType(Tag.IntegerValue, "name");
        Attribute<Integer> attribute = cycle(integerType.of(-50505, 50505));
        assertEquals(Arrays.asList(-50505, 50505), attribute.getValues());
    }

    @Test
    public void enumAttribute() throws IOException {
        AttributeGroup group = Cycler.cycle(AttributeGroup.Companion.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(
                        Operation.CancelJob, Operation.GetJobAttributes, Operation.CreateJob)));
        assertEquals(Arrays.asList(Operation.CancelJob, Operation.GetJobAttributes, Operation.CreateJob),
                group.getValues(Attributes.OperationsSupported));
    }

    @Test
    public void surpriseEnum() throws IOException {
        AttributeGroup group = Cycler.cycle(AttributeGroup.Companion.of(Tag.PrinterAttributes,
                Attributes.OperationsSupported.of(
                        Operation.of("vendor-specific", 0x4040))));
        // We can't know it's called "vendor-specific" after parsing, since we just made it up.
        // So expect the unrecognized format
        assertEquals(Arrays.asList(Operation.of("Operation(x4040)", 0x4040)),
                group.getValues(Attributes.OperationsSupported));
    }

    @Test
    public void invalidTag() {
        exception.expect(BuildError.class);
        // String is not Integer; should throw.
        new StringType(Tag.IntegerValue, "something");
    }

    @Test
    public void missingEncoder() throws Exception {
        exception.expect(ParseError.class);
        byte[] bytes = new byte[] {
                0, 2, 'x', 0,
                1,
                0
        };
        Attribute.Companion.read(new DataInputStream(new ByteArrayInputStream(bytes)),
                new Encoder.Finder() {
                    @Override
                    public Encoder<?> find(Tag valueTag, String name) throws IOException {
                        throw new ParseError("");
                    }
                }, Tag.OctetString);
    }

    @Test
    public void insufficientLength() throws Exception {
        exception.expect(ParseError.class);
        exception.expectMessage("Bad attribute length: expected 4, got 1");
        byte[] bytes = new byte[] {
                0,
                0,
                0,
                1,
                0
        };
        Attribute.Companion.read(new DataInputStream(new ByteArrayInputStream(bytes)), Cycler.sFinder, Tag.IntegerValue);
    }

    @Test
    public void badTag() throws Exception {
        exception.expect(BuildError.class);
        exception.expectMessage("Invalid tag(x77) for Integer");
        new Attribute<>(Tag.get(0x77), "", Arrays.asList(5), IntegerType.ENCODER);
    }

    @Test
    public void tagNames() throws Exception {
        assertEquals("Integer", IntegerType.ENCODER.getType());
        assertEquals("OctetString", OctetStringType.ENCODER.getType());
        assertEquals("RangeOfInteger", RangeOfIntegerType.ENCODER.getType());
        assertEquals("Resolution", ResolutionType.ENCODER.getType());
        assertEquals("LangString", LangStringType.ENCODER.getType());
        assertEquals("Integer", IntegerType.ENCODER.getType());
        assertEquals("URI", UriType.ENCODER.getType());
        assertEquals("Collection", CollectionType.ENCODER.getType());
        assertEquals("Boolean", BooleanType.ENCODER.getType());
        assertEquals("String", StringType.ENCODER.getType());
        assertEquals("Status", Status.ENCODER.getType());
    }

    @Test
    public void resolutionUnits() throws Exception {
        byte[] bytes = new byte[] {
                0,
                9,
                0,
                0,
                1,
                0,
                0,
                0,
                2,
                0,
                5,
        };

        Resolution resolution = ResolutionType.ENCODER.readValue(
                new DataInputStream(new ByteArrayInputStream(bytes)), Tag.Resolution);
        assertEquals("256x512 ResolutionUnit(x5)", resolution.toString());

        KotlinTest.cover(resolution,
                resolution.copy(resolution.component1(), resolution.component2(), resolution.component3()),
                resolution.copy(777, resolution.component2(), resolution.component3()));

        ResolutionUnit unit = resolution.getUnit();
        KotlinTest.cover(unit, unit.copy(unit.component1(), unit.component2()),
                unit.copy("other", unit.component2()));
    }

    @Test
    public void shortRead() throws IOException {
        exception.expect(ParseError.class);
        exception.expectMessage("Value too short: expected 2 but got 1");
        byte[] bytes = new byte[] {
                0,
                2,
                0,
        };
        IppEncodingsKt.readValueBytes(new DataInputStream(new ByteArrayInputStream(bytes)));
    }

    @Test
    public void badConversion() throws IOException {
        assertNull(Attributes.JobId.of(Attributes.JobName.of("string")));
    }

    @Test
    public void goodConversion() throws IOException {
        assertEquals(Attributes.JobId.of(1),
                Attributes.JobId.of(new IntegerType(Tag.IntegerValue, "job-id").of(1)));
    }

    @Test
    public void printBinary() throws Exception {
        assertTrue(new OctetStringType(Tag.OctetString, "data").of(new byte[] { 1, 2, 3 }).toString().contains("x010203"));
    }

    @Test
    public void cover() throws IOException {
        Attribute<String> jobName = Attributes.JobName.of("hello");
        KotlinTest.cover(jobName,
                jobName.copy(jobName.component1(), jobName.component2(), jobName.component3(), jobName.component4()),
                jobName.copy(jobName.component1(), "goodbye", jobName.component3(), jobName.component4()));

    }
}
