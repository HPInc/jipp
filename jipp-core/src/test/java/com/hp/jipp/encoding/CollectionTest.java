package com.hp.jipp.encoding;

import com.hp.jipp.model.CoveringName;
import com.hp.jipp.model.FinishingsCol;
import com.hp.jipp.model.FoldingDirection;
import com.hp.jipp.model.FoldingReferenceEdge;
import com.hp.jipp.model.MediaCol;
import com.hp.jipp.model.Sides;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.KotlinTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        UntypedCollection.Type untypedColType = new UntypedCollection.Type(Types.finishingsColActual.getName());
        UntypedCollection untyped = group.getValue(untypedColType);
        for (Attribute<?> attribute : untyped.getAttributes()) {
            if (attribute.getName().equals(FinishingsCol.covering.getName())) return;
        }
        fail("No covering attribute found");
    }

    @Test
    public void demoJobResolversSupported() throws Exception {
        // Note: the default implementation of JobResolversSupported does not handle attribute content.
        // This shows what is necessary to make it work.

        // Extract attribute data
        CustomJobResolversSupported.SetType<CustomJobResolversSupported> customJobResolversSupportedType =
                new CustomJobResolversSupported.SetType<>(Types.jobResolversSupported.getName(), CustomJobResolversSupported.converter);

        MediaCol fullBleedSizesMedia = new MediaCol();
        fullBleedSizesMedia.setMediaTopMargin(296);
        fullBleedSizesMedia.setMediaBottomMargin(296);
        fullBleedSizesMedia.setMediaLeftMargin(296);
        fullBleedSizesMedia.setMediaRightMargin(296);

        Attribute<?> jobResolversSupported = customJobResolversSupportedType.of(
            new CustomJobResolversSupported("fullbleed-sizes", fullBleedSizesMedia, null),
            new CustomJobResolversSupported("duplex-types", null, Sides.oneSided),
            new CustomJobResolversSupported("duplex-sizes", null, Sides.oneSided)
        );

        AttributeGroup group = cycle(groupOf(Tag.printerAttributes, jobResolversSupported));
        System.out.println(group.prettyPrint(120, "  "));

        List<CustomJobResolversSupported> resolvers = group.getValues(customJobResolversSupportedType);
        for (CustomJobResolversSupported resolver : resolvers) {
            System.out.println(resolver.resolverName + ": mediaCol=" + resolver.mediaCol + ", sides=" + resolver.sides);
        }
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
