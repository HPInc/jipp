package com.hp.jipp.encoding;

import com.hp.jipp.model.CoveringName;
import com.hp.jipp.model.DocumentState;
import com.hp.jipp.model.FinishingsCol;
import com.hp.jipp.model.IdentifyAction;
import com.hp.jipp.model.ImpositionTemplate;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.JobStateReason;
import com.hp.jipp.model.MediaCol;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.BuildError;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.AttributeGroup.mutableGroupOf;
import static com.hp.jipp.encoding.Cycler.coverList;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.encoding.Tag.jobAttributes;
import static com.hp.jipp.encoding.Tag.operationAttributes;
import static com.hp.jipp.encoding.Tag.printerAttributes;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
public class AttributeGroupTest {

    @Test public void emptyGroup() throws IOException {
        cycle(groupOf(printerAttributes, Collections.<Attribute<Object>>emptyList()));
    }

    @Test public void emptyGroupOf() throws IOException {
        assertEquals(0, cycle(groupOf(printerAttributes, Collections.<Attribute<Object>>emptyList())).size());
    }

    AttributeSetType<Object> untypedDocumentStateType = new UnknownAttribute.SetType("document-state");

    @Test public void groupExtract() {

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
                .putAttributes(operationAttributes, Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer")))
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
                Types.identifyActions.of(IdentifyAction.speak, IdentifyAction.display)));
        assertEquals(Arrays.asList(IdentifyAction.speak, IdentifyAction.display), group.get(Types.identifyActions).strings());
    }

    @Test
    public void missingAttribute() throws Exception {
        AttributeGroup group = cycle(groupOf(operationAttributes,
                Types.printerUri.of(URI.create("ipp://10.0.0.23/ipp/printer"))));
        assertNull(group.get(Types.attributesNaturalLanguage));
    }

    @Test(expected = BuildError.class)
    public void duplicateName() {
        groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.attributesCharset.of("utf-8"));
    }

    @Test
    public void get() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        // Get by attribute type
        assertEquals(Types.attributesCharset.of("utf-8"), group.get(Types.attributesCharset));
        // Get by attribute name (in some cases this will not be as well typed, e.g. collections)
        assertEquals(Types.attributesCharset.of("utf-8"), group.get(Types.attributesCharset.getName()));
        assertNull(group.get(Types.printerName));
    }

    @Test
    public void getValues() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        assertEquals(Collections.singletonList("utf-8"), group.getValues(Types.attributesCharset));
        assertEquals(Collections.emptyList(), group.getValues(Types.attributesNaturalLanguage));
    }

    @Test
    public void getStrings() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.printerName.of("myprinter"));
        assertEquals(Collections.singletonList("myprinter"), group.getStrings(Types.printerName));
    }

    @Test
    public void unknownAttribute() throws IOException {
        ValueTag vendorTag = new ValueTag((byte)0x39, "vendor-enum");
        UnknownAttribute attr = new UnknownAttribute("vendor-state",
                new UntypedEnum(3),
                new OtherOctets(vendorTag, new byte[] { 0x01 }));
        AttributeGroup group = cycle(groupOf(operationAttributes, attr));
        assertEquals(attr, group.get("vendor-state"));
    }

    @Test
    public void cover() {
        coverList(groupOf(operationAttributes, Types.attributesCharset.of("utf-8")),
                Types.attributesCharset.of("utf-8"),
                Types.attributesCharset.of("utf-16"));
    }

    @Test
    public void equality() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        List<Attribute<String>> attributes = Collections.singletonList(Types.attributesCharset.of("utf-8"));
        assertNotEquals(group, 5);
        assertEquals(group, group);
        assertEquals(attributes, group);
        assertEquals(group, attributes);
        assertNotEquals(group, Collections.singletonList(Types.attributesCharset.of("utf-16")));
        assertNotEquals(group, groupOf(operationAttributes,
                Collections.singletonList(Types.attributesCharset.of("utf-16"))));
        assertNotEquals(group, groupOf(printerAttributes, Types.attributesCharset.of("utf-16")));
        assertEquals(attributes.hashCode(), group.hashCode());
    }

    @Test
    public void mutableEquality() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        assertEquals(group, mutableGroup);
        assertEquals(mutableGroup, group);
        assertEquals(group, group.toMutable());

        mutableGroup.put(Types.requestingUserName.of("test"));
        assertNotEquals(group, mutableGroup);
    }

    @Test
    public void mutablePutSingle() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        mutableGroup.set(Types.attributesCharset, "utf-16");
        assertEquals(1, mutableGroup.size());
        assertEquals("utf-16", mutableGroup.get(Types.attributesCharset).getValue());
    }

    @Test
    public void mutablePutMultiple() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        mutableGroup.put(Types.attributesCharset.of("utf-8"),
                Types.attributesNaturalLanguage.of("sp"));
        assertEquals("sp", mutableGroup.getValue(Types.attributesNaturalLanguage));
        assertEquals(Collections.singletonList("utf-8"), mutableGroup.getValues(Types.attributesCharset));
    }

    @Test
    public void mutableSetMultiple() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes);
        mutableGroup.set(Types.mediaColDatabase, Arrays.asList(new MediaCol(), new MediaCol()));
        assertEquals(2, mutableGroup.get(Types.mediaColDatabase).size());
    }

    @Test
    public void mutableGroupOperations() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        assertTrue(mutableGroup.contains(Types.attributesCharset.of("utf-8")));
        assertEquals(0, mutableGroup.indexOf(Types.attributesCharset.of("utf-8")));

        Attribute<Name> printerName = Types.printerName.of("myprinter");
        mutableGroup.put(Types.printerName.of("myprinter"));
        assertEquals(1, mutableGroup.lastIndexOf(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName));

        mutableGroup.plusAssign(Collections.singletonList(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName.getName()));

        mutableGroup.put(Types.printerName.of("first"));
        assertEquals(Types.printerName.of("first"), mutableGroup.get(Types.printerName));

        mutableGroup.put(Types.printerName.of(new Name("third")));
        assertEquals(Types.printerName.of("third"), mutableGroup.get(Types.printerName));

        mutableGroup.put(Types.printerOrganization.of("mine"));
        assertEquals(Types.printerOrganization.of(new Text("mine")), mutableGroup.get(Types.printerOrganization));

        assertNull(mutableGroup.get(Types.documentFormat.getName()));
        assertThat(mutableGroup.toString(), startsWith("MutableAttributeGroup"));
        mutableGroup.setTag(Tag.jobAttributes);
    }

    @Test
    public void groupPlus() {
        AttributeGroup group = groupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.operationsSupported.of(Operation.printJob),
                Types.documentFormatSupported.of("application/octet-stream"));

        AttributeGroup group2 = group.plus(groupOf(operationAttributes, Types.operationsSupported.of(Operation.fetchJob)));

        List<Attribute<?>> all = new ArrayList<>(group2);
        assertEquals(Types.documentFormatSupported.getName(), all.get(2).getName());
        assertEquals(Types.operationsSupported.of(Operation.fetchJob), all.get(1));
    }

    @Test
    public void mutableGroupPutOperations() {
        MutableAttributeGroup group = mutableGroupOf(operationAttributes);

        group.put(Types.documentCharsetSupported, Arrays.asList("utf-8", "utf-16"));
        assertEquals(Arrays.asList("utf-8", "utf-16"), group.get(Types.documentCharsetSupported));

        group.put(Types.documentCharsetSupported, "utf-16", "utf-8");
        assertEquals(Arrays.asList("utf-16", "utf-8"), group.get(Types.documentCharsetSupported));

        group.put(Types.documentCharsetSupported, "utf-32");
        assertEquals(Collections.singletonList("utf-32"), group.get(Types.documentCharsetSupported));

        group.put(Types.jobDetailedStatusMessages, "all", "good");
        assertEquals(Arrays.asList("all", "good"), group.getStrings(Types.jobDetailedStatusMessages));

        group.put(Types.jobDetailedStatusMessages, "one");
        assertEquals(Collections.singletonList("one"), group.getStrings(Types.jobDetailedStatusMessages));

        group.put(Types.outputDeviceSupported, "all", "good");
        assertEquals(Arrays.asList("all", "good"), group.getStrings(Types.outputDeviceSupported));

        group.put(Types.outputDeviceSupported, "one");
        assertEquals(Collections.singletonList("one"), group.getStrings(Types.outputDeviceSupported));

        group.put(Types.coveringNameSupported, CoveringName.plain, CoveringName.preCut);
        assertEquals(Arrays.asList(CoveringName.plain, CoveringName.preCut),
                group.getStrings(Types.coveringNameSupported));

        group.put(Types.impositionTemplateDefault, ImpositionTemplate.signature);
        assertEquals(ImpositionTemplate.signature, group.getString(Types.impositionTemplateDefault));
    }

    @Test
    public void mutableGroupDrop() {
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
    public void minus() {
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
        assertNotEquals(mutableGroup, new ArrayList<>());
        assertNotEquals(mutableGroup, groupOf(Tag.operationAttributes, Types.attributesCharset.of("utf-9")));
        assertNotEquals(mutableGroup, groupOf(printerAttributes, Types.attributesCharset.of("utf-8")));
    }

    @Test
    public void prettyPrint() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"));
        assertEquals("operation-attributes { attributes-charset = utf-8 }",mutableGroup.prettyPrint(120,"    "));
    }

    @Test
    public void modifyMutableInPlace() {
        AttributeGroup group = mutableGroupOf(operationAttributes, Types.finishingsCol.of(new FinishingsCol()));
        group.get(Types.finishingsCol).get(0).setImpositionTemplate(new KeywordOrName(ImpositionTemplate.signature));
        assertEquals(ImpositionTemplate.signature, group.get(Types.finishingsCol).get(0).getImpositionTemplate().getKeyword());
    }

    @Test
    public void modifyInPlace() {
        AttributeGroup group = groupOf(operationAttributes, Types.finishingsCol.of(new FinishingsCol()));
        group.get(Types.finishingsCol).get(0).setImpositionTemplate(new KeywordOrName(ImpositionTemplate.signature));
        assertEquals(ImpositionTemplate.signature, group.get(Types.finishingsCol).get(0).getImpositionTemplate().getKeyword());
    }

    @Ignore // See issue #26
    @Test
    public void prettyPrint2() {
        MediaCol mediaCol = new MediaCol();
        MediaCol.MediaSize mediaSize = new MediaCol.MediaSize();
        mediaSize.setXDimension(1000);
        mediaSize.setYDimension(2000);
        mediaCol.setMediaSize(mediaSize);
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes,
                Types.attributesCharset.of("utf-8"),
                Types.operationsSupported.of(Operation.getJobAttributes, Operation.getJobs),
                Types.mediaCol.of(mediaCol));
        assertEquals("operation-attributes {\n  attributes-charset = utf-8,\n  operations-supported = [\n    Get-Job-Attributes(9),\n    Get-Jobs(10) ],\n  media-col = {\n    media-size = { x-dimension = 1000, y-dimension = 2000 } } }",mutableGroup.prettyPrint(60,"  "));
    }
}
