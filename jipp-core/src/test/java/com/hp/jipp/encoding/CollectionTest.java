package com.hp.jipp.encoding;

import com.hp.jipp.pwg.CoveringName;
import com.hp.jipp.pwg.FinishingsCol;
import com.hp.jipp.pwg.FoldingDirection;
import com.hp.jipp.pwg.FoldingReferenceEdge;
import org.junit.Test;

import java.util.Arrays;

import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.pwg.DocumentStatusGroup.finishingsColActual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CollectionTest {

    private FinishingsCol finishingsCol = new FinishingsCol();
    {
        finishingsCol.setCovering(new FinishingsCol.Covering(CoveringName.preCut));
        finishingsCol.setFolding(Arrays.asList(
                new FinishingsCol.Folding(FoldingDirection.inward, 22, FoldingReferenceEdge.bottom),
                new FinishingsCol.Folding(FoldingDirection.outward, 66, FoldingReferenceEdge.left)));
    }

    @Test
    public void finishingsEmpty() throws Exception {
        FinishingsCol empty = new FinishingsCol();
        assertEquals(empty, cycle(finishingsColActual, finishingsColActual.of(empty)).getValue());
    }

    @Test
    public void finishings() throws Exception {
        FinishingsCol received = cycle(finishingsColActual, finishingsColActual.of(finishingsCol)).getValue();
        assertEquals(finishingsCol, received);
    }

    @Test
    public void untypedCollection() throws Exception {
        AttributeGroup group = cycle(new AttributeGroup(Tag.operationAttributes, finishingsColActual.of(finishingsCol)));

        // We can use an untypedColType to extract the full data received for the attribute
        UntypedCollection.Type untypedColType = new UntypedCollection.Type(finishingsColActual.getName());
        UntypedCollection untyped = group.getValue(untypedColType);
        for (Attribute<?> attribute : untyped.getAttributes()) {
            if (attribute.getName().equals(FinishingsCol.Name.covering)) return;
        }
        fail("No covering attribute found");
    }
}
