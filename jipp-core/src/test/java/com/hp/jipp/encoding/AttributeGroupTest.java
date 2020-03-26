package com.hp.jipp.encoding;

import com.hp.jipp.model.DocumentState;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.JobStateReason;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.BuildError;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.*;
import static com.hp.jipp.encoding.Cycler.coverList;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.encoding.Tag.*;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class AttributeGroupTest {

    @Test public void emptyGroup() throws IOException {
        cycle(groupOf(printerAttributes, Collections.<Attribute<Object>>emptyList()));
    }

    @Test public void emptyGroupOf() throws IOException {
        assertEquals(0, cycle(groupOf(printerAttributes, Collections.<Attribute<Object>>emptyList())).size());
    }

    @Test public void groupExtract() {
        AttributeType<Object> untypedDocumentStateType = new UnknownAttribute.Type("document-state");

        AttributeGroup group = groupOf(operationAttributes,
                untypedDocumentStateType.of(new UntypedEnum(3), new UntypedEnum(5), new UntypedEnum(6)));

        DocumentState.Type documentStateType = new DocumentState.Type("document-state");

        assertEquals(Arrays.asList(
                DocumentState.pending, DocumentState.processing, DocumentState.processingStopped
        ), group.get(documentStateType));
    }

    @Test
    public void multiAttribute() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesNaturalLanguage.of("en"),
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer")));
        group = cycle(group);

        assertEquals(group.getTag(), operationAttributes);
        assertNotNull(group.get(Types.attributesCharset));
        assertNotNull(group.get(Types.attributesNaturalLanguage));
        assertNotNull(group.get(Types.printerUri));
    }

    @Test
    public void operationGroupWithUri() throws Exception {
        IppPacket packet = IppPacket.jobResponse(Status.successfulOk, 1, URI.create("ipp://10.0.0.23/ipp/printer/job/1"),
                JobState.pending,
                Collections.singletonList(JobStateReason.accountClosed))
                .addAttributes(operationAttributes, Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer")))
                .build();
        packet = cycle(packet);
        AttributeGroup group = packet.get(operationAttributes);
        assertEquals(group.getTag(), operationAttributes);
        assertNotNull(group.get(Types.attributesCharset));
        assertNotNull(group.get(Types.attributesNaturalLanguage));
        assertNotNull(group.get(Types.printerUri));
        AttributeGroup jobGroup = packet.get(jobAttributes);
        assertNotNull(jobGroup.get(Types.jobUri));
        assertNotNull(jobGroup.get(Types.jobId));
        assertEquals(Collections.singletonList(JobStateReason.accountClosed), jobGroup.get(Types.jobStateReasons));
    }

    @Test
    public void multipleJobGroups() throws Exception {
        IppPacket packet = IppPacket.jobResponse(Status.successfulOk, 1, URI.create("ipp://10.0.0.23/ipp/printer/job/1"),
                JobState.pending)
                .addJobAttributesGroup(1, URI.create("ipp://10.0.0.23/ipp/printer/job/2"), JobState.aborted)
                .build();
        packet = cycle(packet);
        assertEquals(Tag.jobAttributes, packet.getAttributeGroups().get(2).getTag());
    }

    @Test
    public void multiMultiAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8", "utf-16")));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.get(Types.attributesCharset).strings());
    }

    @Test
    public void missingAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(operationAttributes,
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer"))));
        assertNull(group.get(Types.attributesNaturalLanguage));
    }

    @Test(expected = BuildError.class)
    public void duplicateName() throws Exception {
        groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test(expected = BuildError.class)
    public void badDelimiter() throws Exception {
        AttributeGroup group = groupOf(Tag.adminDefine);
    }

    @Test
    public void get() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        // Get by attribute type
        assertEquals(Types.attributesCharset.of("utf-8","utf-16"), group.get(Types.attributesCharset));
        // Get by attribute name (in some cases this will not be as well typed, e.g. collections)
        assertEquals(Types.attributesCharset.of("utf-8","utf-16"), group.get(Types.attributesCharset.getName()));
        assertNull(group.get(Types.printerName));
    }

    @Test
    public void getValues() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.getValues(Types.attributesCharset));
        assertEquals(Collections.emptyList(), group.getValues(Types.attributesNaturalLanguage));
    }

    @Test
    public void getStrings() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.printerName.of(new Name("myprinter")));
        assertEquals(Collections.singletonList("myprinter"), group.getStrings(Types.printerName));
    }

    @Test
    public void unknownAttribute() throws Exception {
        UnknownAttribute attr = new UnknownAttribute("vendor-state",
                new UntypedEnum(3),
                new OtherOctets(Tag.fromInt(0x39), new byte[] { 0x01 }));
        AttributeGroup group = cycle(groupOf(operationAttributes, attr));
        assertEquals(attr, group.get("vendor-state"));
    }

    @Test
    public void cover() throws Exception {
        coverList(groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16")),
                Types.attributesCharset.of("utf-8","utf-16"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test
    public void equality() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8","utf-16"));
        List<Attribute<String>> attributes = Collections.singletonList(Types.attributesCharset.of("utf-8", "utf-16"));
        assertNotEquals(group, 5);
        assertEquals(group, group);
        assertEquals(attributes, group);
        //noinspection AssertEqualsBetweenInconvertibleTypes
        assertEquals(group, attributes);
        assertNotEquals(group, Collections.singletonList(Types.attributesCharset.of("utf-8")));
        assertNotEquals(group, groupOf(operationAttributes,
                Collections.singletonList(Types.attributesCharset.of("utf-8"))));
        assertNotEquals(group, groupOf(printerAttributes,
                Types.attributesCharset.of("utf-8","utf-16")));
        assertEquals(attributes.hashCode(), group.hashCode());
    }

    @Test
    public void mutableEquality() throws Exception {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8", "utf-16"));
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8", "utf-16"));
        assertEquals(group, mutableGroup);
        assertEquals(mutableGroup, group);
        assertEquals(group, group.toMutable());

        mutableGroup.put(Types.requestingUserName.of("test"));
        assertNotEquals(group, mutableGroup);
    }

    @Test
    public void mutableAddMultiple() throws Exception {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8", "utf-16"));
        mutableGroup.put(Types.attributesCharset.of("utf-8", "utf-16"),
                Types.attributesNaturalLanguage.of("sp"));
        assertEquals("sp", mutableGroup.getValue(Types.attributesNaturalLanguage));
        assertEquals(Arrays.asList("utf-8", "utf-16"), mutableGroup.getValues(Types.attributesCharset));
    }

    @Test
    public void mutableGroupOperations() throws Exception {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        assertTrue(mutableGroup.contains(Types.attributesCharset.of("utf-8")));
        assertEquals(0, mutableGroup.indexOf(Types.attributesCharset.of("utf-8")));

        Attribute<Name> printerName = Types.printerName.of("myprinter");
        mutableGroup.put(Types.printerName.of(new Name("myprinter")));
        assertEquals(1, mutableGroup.lastIndexOf(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName));

        mutableGroup.plusAssign(Collections.singletonList(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName.getName()));

        mutableGroup.put(Types.printerName, "first", "second");
        assertEquals(Types.printerName.of("first", "second"), mutableGroup.get(Types.printerName));

        mutableGroup.put(Types.printerName, new Name("third"), new Name("fourth"));
        assertEquals(Types.printerName.of("third", "fourth"), mutableGroup.get(Types.printerName));

        mutableGroup.put(Types.printerOrganization, "mine", "still mine");
        assertEquals(Types.printerOrganization.of("mine", "still mine"), mutableGroup.get(Types.printerOrganization));

        assertNull(mutableGroup.get(Types.documentFormat.getName()));
        assertThat(mutableGroup.toString(), startsWith("MutableAttributeGroup"));
        mutableGroup.setTag(Tag.jobAttributes);
    }

    @Test
    public void mutableGroupDrop() throws Exception {
        Attribute<Name> printerName = Types.printerName.of("jim");
        MutableAttributeGroup mutableGroup = mutableGroupOf(printerAttributes, printerName);
        assertEquals(printerName, mutableGroup.drop(Types.printerName));
        assertFalse(mutableGroup.drop(printerName)); // Not there anymore

        // Put it back and drop it again
        mutableGroup.put(printerName);
        assertTrue(mutableGroup.drop(printerName));
        assertFalse(mutableGroup.drop(printerName));
    }

    @Test
    public void minus() throws Exception {
        Attribute<Name> printerName = Types.printerName.of("jim");
        MutableAttributeGroup mutableGroup = mutableGroupOf(printerAttributes, printerName);
        mutableGroup.minusAssign(Types.printerName);
        assertNull(mutableGroup.get(Types.printerName));

        mutableGroup.put(printerName);
        assertNotNull(mutableGroup.get(Types.printerName));
        mutableGroup.minusAssign(printerName);
        assertNull(mutableGroup.get(Types.printerName));
    }


    @Test
    public void cycleMutableGroup() throws Exception {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        AttributeGroup cycledGroup = cycle(mutableGroup);
        assertEquals(mutableGroup.get(Types.attributesCharset), cycledGroup.get(Types.attributesCharset));
        assertEquals(mutableGroup, mutableGroup);
        assertEquals(mutableGroup, cycledGroup);
        assertEquals(cycledGroup, mutableGroup);
        assertEquals(cycledGroup.hashCode(), mutableGroup.hashCode());
        assertEquals(mutableGroup, new ArrayList<>(cycledGroup));
        assertEquals(cycledGroup, new ArrayList<>(mutableGroup));
        assertNotEquals(mutableGroup, "other");
        assertNotEquals(mutableGroup, 5);
        assertNotEquals(mutableGroup, new ArrayList<>());
        assertNotEquals(mutableGroup, groupOf(Tag.operationAttributes, Types.attributesCharset.of("utf-9")));
        assertNotEquals(mutableGroup, groupOf(printerAttributes, Types.attributesCharset.of("utf-8")));
    }
}
