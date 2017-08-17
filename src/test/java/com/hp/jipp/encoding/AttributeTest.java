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
import com.hp.jipp.model.Types;
import com.hp.jipp.model.Operation;

import static com.hp.jipp.encoding.AttributeGroupKt.groupOf;
import static com.hp.jipp.encoding.Cycler.*;

import kotlin.text.Charsets;

public class AttributeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void octetString() throws IOException {
        AttributeType<byte[]> octetStringType = new OctetStringType(Tag.octetString, "name");
        Attribute<byte[]> attribute = octetStringType.of("value".getBytes(Charsets.UTF_8));
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
        assertEquals(Tag.octetString, attribute.getValueTag());
        assertEquals("name", attribute.getName());
        assertArrayEquals("value".getBytes(Charsets.UTF_8), attribute.getValue(0));
    }

    @Test
    public void multiOctetString() throws IOException {
        AttributeType<byte[]> stringType = new OctetStringType(Tag.nameWithoutLanguage, "name");
        Attribute<byte[]> attribute = stringType.of("value".getBytes(Charsets.UTF_8), "value2".getBytes(Charsets.UTF_8));
        assertArrayEquals("value".getBytes(Charsets.UTF_8), attribute.getValue(0));
        assertArrayEquals("value2".getBytes(Charsets.UTF_8), attribute.getValue(1));
    }

    @Test
    public void multiBoolean() throws IOException {
        AttributeType<Boolean> booleanType = new BooleanType(Tag.booleanValue, "name");
        Attribute<Boolean> attribute = cycle(booleanType.of(true, false));
        assertEquals(Arrays.asList(true, false), attribute.getValues());
    }

    @Test
    public void multiInteger() throws IOException {
        AttributeType<Integer> integerType = new IntegerType(Tag.integerValue, "name");
        Attribute<Integer> attribute = cycle(integerType.of(-50505, 50505));
        assertEquals(Arrays.asList(-50505, 50505), attribute.getValues());
    }

    @Test
    public void enumAttribute() throws IOException {
        AttributeGroup group = Cycler.cycle(groupOf(Tag.printerAttributes,
                Types.operationsSupported.of(
                        Operation.cancelJob, Operation.getJobAttributes,
                        Operation.createJob)));
        assertEquals(Arrays.asList(Operation.cancelJob, Operation.getJobAttributes,
                Operation.createJob),
                group.getValues(Types.operationsSupported));
    }

    @Test
    public void surpriseEnum() throws IOException {
        AttributeGroup group = Cycler.cycle(groupOf(Tag.printerAttributes,
                Types.operationsSupported.of(
                        new Operation("vendor-specific", 0x4040))));
        // We can't know it's called "vendor-specific" after parsing, since we just made it up.
        // So expect the unrecognized format
        assertEquals(Arrays.asList(new Operation("Operation(x4040)", 0x4040)),
                group.getValues(Types.operationsSupported));
    }

    @Test
    public void invalidTag() {
        exception.expect(BuildError.class);
        // String is not Integer; should throw.
        new StringType(Tag.integerValue, "something");
    }

    @Test
    public void missingEncoder() throws Exception {
        exception.expect(ParseError.class);
        byte[] bytes = new byte[] {
                0, 2, 'x', 0,
                1,
                0
        };
        IppEncodingsKt.readAttribute(new DataInputStream(new ByteArrayInputStream(bytes)),
                new Encoder.Finder() {
                    @Override
                    public Encoder<?> find(Tag valueTag, String name) throws IOException {
                        throw new ParseError("");
                    }
                }, Tag.octetString);
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
        IppEncodingsKt.readAttribute(new DataInputStream(new ByteArrayInputStream(bytes)), Cycler.sFinder, Tag.integerValue);
    }

    @Test
    public void badTag() throws Exception {
        exception.expect(BuildError.class);
        exception.expectMessage("Invalid tag(x77) for Integer");
        new Attribute<Integer>(TagKt.toTag(0x77), "", Arrays.asList(5), IntegerType.ENCODER);
    }

    @Test
    public void tagNames() throws Exception {
        assertEquals("Integer", IntegerType.ENCODER.getType());
        assertEquals("octetString", OctetStringType.ENCODER.getType());
        assertEquals("rangeOfInteger", RangeOfIntegerType.ENCODER.getType());
        assertEquals("resolution", ResolutionType.ENCODER.getType());
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
                new DataInputStream(new ByteArrayInputStream(bytes)), Tag.resolution);
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
        assertNull(Types.jobId.of(Types.jobName.of("string")));
    }

    @Test
    public void goodConversion() throws IOException {
        assertEquals(Types.jobId.of(1),
                Types.jobId.of(new IntegerType(Tag.integerValue, "job-id").of(1)));
    }

    @Test
    public void printBinary() throws Exception {
        System.out.println(new OctetStringType(Tag.octetString, "data").of(new byte[] { 1, 2, 3 }).toString());
        assertTrue(new OctetStringType(Tag.octetString, "data").of(new byte[] { 1, 2, 3 }).toString().contains("x010203"));
    }

    @Test
    public void cover() throws IOException {
        Attribute<String> jobName = Types.jobName.of("hello");
        KotlinTest.cover(jobName,
                jobName.copy(jobName.component1(), jobName.component2(), jobName.component3(), jobName.component4()),
                jobName.copy(jobName.component1(), "goodbye", jobName.component3(), jobName.component4()));

    }
}
