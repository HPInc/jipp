package com.hp.jipp.model;


import com.hp.jipp.encoding.AttributeType;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

public class IdentifyTest {
    @Test
    public void actions() throws Exception {
        AttributeType<IdentifyAction> IdentifyActions = IdentifyAction.typeOf("identify-actions");
        assertTrue(IdentifyAction.Display == cycle(IdentifyActions,
                IdentifyActions.of(IdentifyAction.Display)).getValue(0));
        assertEquals("display", IdentifyAction.Display.toString());
    }

    @Test
    public void acceptOtherActions() throws Exception {
        AttributeType<IdentifyAction> IdentifyActions = IdentifyAction.typeOf("identify-actions");
        assertEquals("flare-gun",
                cycle(IdentifyActions, IdentifyActions.of(IdentifyAction.of("flare-gun"))).getValue(0).getName());
    }


}
