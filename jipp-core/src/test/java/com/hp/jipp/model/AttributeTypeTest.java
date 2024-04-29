// Copyright 2017 - 2022 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IntOrIntRange;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.KeywordOrName;
import com.hp.jipp.encoding.MediaSizes;
import com.hp.jipp.encoding.Resolution;
import com.hp.jipp.encoding.ResolutionUnit;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.ranges.IntRange;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.encoding.MediaSizes.toMediaColDatabaseMediaSize;
import static com.hp.jipp.model.Types.attributesNaturalLanguage;
import static com.hp.jipp.model.Types.copiesSupported;
import static com.hp.jipp.model.Types.operationsSupported;
import static com.hp.jipp.model.Types.printerResolutionDefault;
import static com.hp.jipp.model.Types.stitchingOffsetSupported;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AttributeTypeTest {

    @Test
    public void naturalLanguage() throws Exception {
        Attribute<String> attribute = cycle(Types.attributesNaturalLanguage,
                attributesNaturalLanguage.of("en"));
        assertEquals(Collections.singletonList("en"), attribute.strings());
    }

    @Test
    public void naturalLanguageFromGroup() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                attributesNaturalLanguage.of("en")));
        Attribute<String> attribute = group.get(Types.attributesNaturalLanguage);
        assertEquals(Collections.singletonList("en"), attribute.strings());
    }

    @Test
    public void ignoreBadNameNaturalLanguage() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes,
                new StringType(Tag.naturalLanguage, "attributes-NATURAL-language").of("en")));
        assertNull(group.get(Types.attributesNaturalLanguage));
    }

    @Test
    public void enumAttributeType() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.printerAttributes,
                Types.operationsSupported.of(Operation.cancelJob, Operation.createJob)));
        System.out.println(group);
        assertEquals(Arrays.asList(Operation.cancelJob, Operation.createJob),
                group.get(operationsSupported));
    }

    @Test
    public void rangeOfIntegers() throws Exception {
        IntRange range = cycle(copiesSupported, Types.copiesSupported.of(new IntRange(0, 99))).get(0);
        assertEquals(0, range.getFirst());
        assertEquals(99, range.getLast());
    }

    @Test
    public void resolution() throws Exception {
        Resolution resolution = cycle(printerResolutionDefault, Types.printerResolutionDefault.of(
                new Resolution(300, 600, ResolutionUnit.dotsPerInch))).get(0);
        assertEquals(300, resolution.getCrossFeedResolution());
        assertEquals(600, resolution.getFeedResolution());
        assertEquals(ResolutionUnit.dotsPerInch, resolution.getUnit());
    }

    @Test
    public void unknown() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unknown());
        assertEquals(0, attribute.size());
        assertTrue(attribute.isUnknown());
    }

    @Test
    public void noValue() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.noValue());
        assertEquals(0, attribute.size());
        assertTrue(attribute.isNoValue());
    }

    @Test
    public void unsupported() throws Exception {
        JobState.Type jobStateType = new JobState.Type("job-state");
        Attribute<JobState> attribute = cycle(jobStateType, jobStateType.unsupported());
        assertEquals(0, attribute.size());
        assertTrue(attribute.isUnsupported());
    }

    @Test
    public void intOrRangeType() throws Exception {
        Attribute<?> attribute = cycle(
                Types.stitchingOffsetSupported.of(new IntOrIntRange(5), new IntOrIntRange(7, 10)));
        // We get the raw types here because we didn't use the type to cycle
        assertEquals(Arrays.asList(5, new IntRange(7, 10)), attribute);

        Attribute<?> attribute2 = cycle(stitchingOffsetSupported,
                Types.stitchingOffsetSupported.of(new IntOrIntRange(5), new IntOrIntRange(7, 10)));
        assertEquals(new IntOrIntRange(5), attribute2.get(0));
        assertEquals(new IntOrIntRange(7, 10), attribute2.get(1));
        assertEquals(Arrays.asList(new IntOrIntRange(5), new IntOrIntRange(7, 10)), attribute2);
    }

    @Test
    public void mediaColTypeTest() throws IOException {
        MediaCol mediaType1 = new MediaCol();
        mediaType1.setMediaSize(MediaSizes.parse(Media.naLetter8p5x11in));
        mediaType1.setMediaLeftMargin(750);
        mediaType1.setMediaRightMargin(750);
        mediaType1.setMediaBottomMargin(750);
        mediaType1.setMediaTopMargin(750);
        mediaType1.setMediaSource(new KeywordOrName(MediaSource.main));
        mediaType1.setMediaType(new KeywordOrName(MediaType.stationery));
        // And others...

        List<MediaCol> actualList = new ArrayList<>();
        actualList.add(mediaType1);
        // Etc.

        IppPacket packet = new IppPacket(Status.successfulOk, 1234,
                groupOf(Tag.operationAttributes /* +default operation attributes */),
                groupOf(Tag.printerAttributes,
                        Types.mediaColActual.of(actualList) /* + other requested attributes */ ));

        MediaCol first = cycle(packet).get(Tag.printerAttributes).get(Types.mediaColActual).get(0);
        assertEquals(mediaType1, first);
    }

    @Test
    public void mediaColDatabaseTypeTest() throws IOException {
        MediaColDatabase mediaType1 = new MediaColDatabase();
        mediaType1.setMediaSize(toMediaColDatabaseMediaSize(MediaSizes.parse(Media.naLetter8p5x11in)));
        mediaType1.setMediaLeftMargin(750);
        mediaType1.setMediaRightMargin(750);
        mediaType1.setMediaBottomMargin(750);
        mediaType1.setMediaTopMargin(750);
        mediaType1.setMediaSource(new KeywordOrName(MediaSource.main));
        mediaType1.setMediaType(new KeywordOrName(MediaType.stationery));
        // And others...

        List<MediaColDatabase> readyList = new ArrayList<>();
        readyList.add(mediaType1);
        // Etc.

        IppPacket packet = new IppPacket(Status.successfulOk, 1234,
                groupOf(Tag.operationAttributes /* +default operation attributes */),
                groupOf(Tag.printerAttributes,
                        Types.mediaColReady.of(readyList),
                        Types.mediaColDatabase.of(readyList) /* + other requested attributes */ ));

        MediaColDatabase first = cycle(packet).get(Tag.printerAttributes).get(Types.mediaColReady).get(0);
        assertEquals(mediaType1, first);
    }

    @Test
    public void printMediaCol() {
        MediaCol mediaType1 = new MediaCol();
        mediaType1.setMediaSize(MediaSizes.parse(Media.naLetter8p5x11in));
        mediaType1.setMediaLeftMargin(750);
        mediaType1.setMediaRightMargin(750);
        mediaType1.setMediaBottomMargin(750);
        mediaType1.setMediaTopMargin(750);
        mediaType1.setMediaSource(new KeywordOrName(MediaSource.main));
        mediaType1.setMediaType(new KeywordOrName(MediaType.stationery));
        String out = mediaType1.toString();
        assertThat(out, containsString("media-left-margin=750"));
    }
}
