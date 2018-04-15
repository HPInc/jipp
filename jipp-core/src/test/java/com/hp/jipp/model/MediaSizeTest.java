package com.hp.jipp.model;

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
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void parse() throws Exception {
        MediaSize a10 = parse("iso_a10_26x37mm");
        assertEquals(2600, a10.getWidth());
        assertEquals(3700, a10.getHeight());
        assertEquals("iso_a10_26x37mm", a10.getName());

        MediaSize executive = parse("na_executive_7.25x10.5in");
        assertEquals(18415, executive.getWidth());
        assertEquals(26670, executive.getHeight());
    }

    @Test
    public void build() throws Exception {
        MediaSize.Type mediaSupportedType = new MediaSize.Type("media-supported");
        assertTrue(MediaSize.jisB7 == cycle(mediaSupportedType, mediaSupportedType.of(
                MediaSize.jisB7)).getValue(0));
    }

    @Test
    public void same() throws Exception {
        // Referential equality because this size is known
        assertTrue(MediaSize.isoA10 == parse("iso_a10_26x37mm"));
    }

    @Test
    public void noGood() throws Exception {
        assertEquals(0, parse("iso_a10_26").getHeight());
    }

    private MediaSize parse(String input) throws Exception {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        StringType.Encoder.writeValue(new DataOutputStream(outBytes), input);

        return MediaSize.Encoder.readValue(new DataInputStream(new ByteArrayInputStream(outBytes.toByteArray())),
                Tag.keyword);
    }
}
