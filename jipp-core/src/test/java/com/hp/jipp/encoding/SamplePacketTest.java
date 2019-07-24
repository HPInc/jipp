package com.hp.jipp.encoding;

import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Types;
import com.hp.jipp.model.WhichJobs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.assertEquals;

public class SamplePacketTest {
    @Test
    public void validatePacket() throws Exception {
        URI uri = URI.create("ipp://127.0.0.1/ipp/printer");

        List<String> keywords = new ArrayList<>();
        keywords.add(Types.jobName.getName());
        keywords.add(Types.jobId.getName());
        keywords.add(Types.jobState.getName());
        keywords.add(Types.jobOriginatingUserName.getName());
        keywords.add(Types.jobUri.getName());
        keywords.add(Types.copies.getName());
        keywords.add(Types.jobMediaSheets.getName());
        keywords.add(Types.jobMediaSheetsCompleted.getName());

        String user = "me";

        IppPacket packet = new IppPacket(Operation.getJobs, 123,
                groupOf(Tag.operationAttributes,
                        Types.attributesCharset.of("utf-8"),
                        Types.attributesNaturalLanguage.of("en"),
                        Types.printerUri.of(uri),
                        Types.requestingUserName.of(user),
                        Types.requestedAttributes.of(keywords),
                        Types.whichJobs.of(WhichJobs.fetchable),
                        Types.myJobs.of(true)));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytes);
        packet.write(output);
        output.close();
        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes.toByteArray()));

        IppPacket inPacket = input.readPacket();
        String expected =
                "IppPacket(v=0x200, c=Get-Jobs(10), r=0x7b) {\n" +
                "  operation-attributes {\n" +
                "    attributes-charset = \"utf-8\" (charset),\n" +
                "    attributes-natural-language = \"en\" (naturalLanguage),\n" +
                "    printer-uri = ipp://127.0.0.1/ipp/printer,\n" +
                "    requesting-user-name = \"me\" (name),\n" +
                "    requested-attributes = [ job-name, job-id, job-state, job-originating-user-name,\n" +
                "      job-uri, copies, job-media-sheets, job-media-sheets-completed ],\n" +
                "    which-jobs = fetchable,\n" +
                "    my-jobs = true } }";
        assertEquals(expected, inPacket.prettyPrint(90, "  "));
    }
}
