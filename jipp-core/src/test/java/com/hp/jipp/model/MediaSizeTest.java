package com.hp.jipp.model;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static com.hp.jipp.encoding.Cycler.*;

public class MediaSizeTest {
    // TODO: Replace when MediaSize is working again
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();
//
//    @Test
//    public void parse() throws Exception {
//        MediaSize a10 = parse("iso_a10_26x37mm");
//        assertEquals(2600, a10.getWidth());
//        assertEquals(3700, a10.getHeight());
//        assertEquals("iso_a10_26x37mm", a10.getName());
//
//        MediaSize executive = parse("na_executive_7.25x10.5in");
//        assertEquals(18415, executive.getWidth());
//        assertEquals(26670, executive.getHeight());
//    }
//
//    @Test
//    public void build() throws Exception {
//        MediaSize.Type mediaSupportedType = new MediaSize.Type("media-supported");
//        assertSame(MediaSize.jisB7, cycle(mediaSupportedType, mediaSupportedType.of(
//                MediaSize.jisB7)).getValue());
//    }
//
//    @Test
//    public void same() throws Exception {
//        // Referential equality because this size is known
//        assertSame(MediaSize.isoA10, parse("iso_a10_26x37mm"));
//    }
//
//    @Test
//    public void noGood() throws Exception {
//        assertEquals(0, parse("iso_a10_26").getHeight());
//    }
//
//    private MediaSize parse(String string) throws Exception {
//        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
//        IppOutputStream output = new IppOutputStream(outBytes);
//
//        StringType.Encoder.writeValue(output, string);
//
//        IppInputStream input = new IppInputStream(new ByteArrayInputStream(outBytes.toByteArray()), sFinder);
//        return MediaSize.Encoder.readValue(input, Tag.keyword);
//    }
}
