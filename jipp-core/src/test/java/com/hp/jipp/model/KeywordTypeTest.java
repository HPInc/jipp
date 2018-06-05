package com.hp.jipp.model;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class KeywordTypeTest {
    @Test
    public void identifyActions() throws Exception {
        assertEquals("display", IdentifyAction.display.toString());
        assertEquals(IdentifyAction.display,
                cycle(Types.identifyActions, Types.identifyActions.of(IdentifyAction.display)).get(0));
    }

    @Test
    public void acceptOtherIdentifyActions() throws Exception {
        IdentifyAction flareGun = new IdentifyAction("flare-gun");
        assertEquals("flare-gun",
                cycle(Types.identifyActions, Types.identifyActions.of(flareGun)).get(0).getName());
    }

    @Test
    public void outputBinActions() throws Exception {
        assertEquals("large-capacity", OutputBin.largeCapacity.toString());
        assertEquals(OutputBin.myMailbox,
                cycle(Types.outputBinSupported, Types.outputBinSupported.of(OutputBin.myMailbox)).get(0));
    }
}
