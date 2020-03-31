package com.hp.jipp.model;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.MutableAttributeGroup;
import com.hp.jipp.encoding.Name;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.trans.IppPacket;
import java.util.Collections;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.AttributeGroup.mutableGroupOf;
import static com.hp.jipp.encoding.Tag.operationAttributes;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("deprecation")
public class DeprecatedTest {
    IppPacket packet = new IppPacket(0x0102, Operation.holdJob.getCode(), 0x50607,
            groupOf(Tag.printerAttributes, Types.operationsSupported.of(Operation.createJob)));

    @Test
    public void mutableGroupAccessors() {
        MutableAttributeGroup mutableGroup = mutableGroupOf(operationAttributes);
        Attribute<Name> printerName = Types.printerName.of("myprinter");
        mutableGroup.add(Types.printerName.of(new Name("myprinter")));
        assertEquals(0, mutableGroup.lastIndexOf(printerName));
        assertEquals(printerName, mutableGroup.get(Types.printerName));

        mutableGroup.addAll(1, Collections.singletonList(Types.documentName.of("mydocument")));
        assertEquals("mydocument", mutableGroup.getValue(Types.documentName).asString());

        mutableGroup.add(1, Types.documentName.of("mydocument2"));
        assertEquals("mydocument2", mutableGroup.getValue(Types.documentName).asString());

        mutableGroup.add(Types.printerDnsSdName.of("myprinter"), Types.printerFaxModemName.of("mymodem"));
        assertEquals("myprinter", mutableGroup.getValue(Types.printerDnsSdName).asString());
        assertEquals("mymodem", mutableGroup.getValue(Types.printerFaxModemName).asString());
    }
}
