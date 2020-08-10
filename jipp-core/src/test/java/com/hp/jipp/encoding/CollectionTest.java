package com.hp.jipp.encoding;

import com.hp.jipp.model.CoveringName;
import com.hp.jipp.model.FinishingsCol;
import com.hp.jipp.model.FoldingDirection;
import com.hp.jipp.model.FoldingReferenceEdge;
import com.hp.jipp.model.MediaCol;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.KotlinTest;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Cycler.cycle;
import static com.hp.jipp.model.Types.finishingsColActual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CollectionTest {

    private FinishingsCol finishingsCol = new FinishingsCol();
    {
        finishingsCol.setCovering(new FinishingsCol.Covering(new KeywordOrName(CoveringName.preCut)));
        finishingsCol.setFolding(Arrays.asList(
                new FinishingsCol.Folding(FoldingDirection.inward, 22, FoldingReferenceEdge.bottom),
                new FinishingsCol.Folding(FoldingDirection.outward, 66, FoldingReferenceEdge.left)));
    }

    @Test
    public void finishingsEmpty() throws Exception {
        FinishingsCol empty = new FinishingsCol();
        assertEquals(empty, cycle(Types.finishingsColActual, finishingsColActual.of(empty)).get(0));
    }

    @Test
    public void finishings() throws Exception {
        FinishingsCol received = cycle(Types.finishingsColActual, finishingsColActual.of(finishingsCol)).get(0);
        assertEquals(finishingsCol, received);
    }

    @Test
    public void untypedCollection() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes, finishingsColActual.of(finishingsCol)));

        // We can use an untypedColType to extract the full data received for the attribute
        UntypedCollection.Type untypedColType = new UntypedCollection.Type(Types.finishingsColActual.getName());
        UntypedCollection untyped = group.getValue(untypedColType);
        for (Attribute<?> attribute : untyped.getAttributes()) {
            if (attribute.getName().equals(FinishingsCol.covering.getName())) return;
        }
        fail("No covering attribute found");
    }

    @Test
    public void untypedCover() throws Exception {
        AttributeGroup group = cycle(groupOf(Tag.operationAttributes, finishingsColActual.of(finishingsCol)));
        // We can use an untypedColType to extract the full data received for the attribute
        UntypedCollection.Type untypedColType = new UntypedCollection.Type(Types.finishingsColActual.getName());
        UntypedCollection untyped = group.getValue(untypedColType);
        KotlinTest.cover(untyped, untyped.copy(untyped.getAttributes()), untyped.copy(Collections.emptyList()));
    }

    @Test
    public void modify() throws Exception {
        assertTrue(finishingsCol.toString().contains("bottom"));
        finishingsCol.setFolding(null);
        assertFalse(finishingsCol.toString().contains("bottom"));
    }
}
