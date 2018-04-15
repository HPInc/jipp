package com.hp.jipp.model;

import com.hp.jipp.encoding.AttributeType;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class IdentifyTest {
    @Test
    public void actions() throws Exception {
        AttributeType<IdentifyAction> IdentifyActions = IdentifyAction.of("identify-actions");
        assertTrue(IdentifyAction.display == cycle(IdentifyActions,
                IdentifyActions.of(IdentifyAction.display)).getValue(0));
        assertEquals("display", IdentifyAction.display.toString());
    }

    @Test
    public void acceptOtherActions() throws Exception {
        AttributeType<IdentifyAction> IdentifyActions = IdentifyAction.of("identify-actions");
        assertEquals("flare-gun",
                cycle(IdentifyActions, IdentifyActions.of(new IdentifyAction("flare-gun"))).getValue(0).getName());
    }
}
