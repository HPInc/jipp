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
        assertTrue(MediaSize.JisB7 == cycle(mediaSupportedType, mediaSupportedType.of(MediaSize.JisB7)).getValue(0));
    }

    @Test
    public void same() throws Exception {
        // Referential equality because this size is known
        assertTrue(MediaSize.IsoA10 == parse("iso_a10_26x37mm"));
    }

    @Test
    public void noGood() throws Exception {
        assertEquals(0, parse("iso_a10_26").getHeight());
    }

    // TODO: This type should be interpreted within collections but it seems not to be.
    // media(keyword): ["na_executive_7.25x10.5in", "na_letter_8.5x11in", "na_legal_8.5x14in", "na_govt-letter_8x10in", "na_invoice_5.5x8.5in", "iso_a5_148x210mm", "iso_a4_210x297mm", "iso_b5_176x250mm", "jis_b5_182x257mm", "na_monarch_3.875x7.5in", "na_number-10_4.125x9.5in", "iso_dl_110x220mm", "iso_c5_162x229mm", "iso_c6_114x162mm", "na_a2_4.375x5.75in", "na_personal_3.625x6.5in", "jpn_chou3_120x235mm", "jpn_chou4_90x205mm", "na_foolscap_8.5x13in"]]

    private MediaSize parse(String input) throws Exception {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        StringType.ENCODER.writeValue(new DataOutputStream(outBytes), input);

        return MediaSize.ENCODER.readValue(new DataInputStream(new ByteArrayInputStream(outBytes.toByteArray())),
                Tag.Keyword);
    }
}
