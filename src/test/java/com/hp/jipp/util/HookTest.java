package com.hp.jipp.util;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class HookTest {
    private static final String TEST_HOOK_NAME = "test";
    @Rule
    public HookRule hooks = new HookRule();

    @Test
    public void setHook() {
        assertTrue(!Hook.is(TEST_HOOK_NAME));
        Hook.set(TEST_HOOK_NAME, true);
        assertTrue(Hook.is(TEST_HOOK_NAME));
    }

    @Test
    public void utilCoverage() {
        new Hook();
    }
}
