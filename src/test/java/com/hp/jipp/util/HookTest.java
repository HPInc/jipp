package com.hp.jipp.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.InputStreamFactory;
import com.hp.jipp.encoding.OctetStringType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.util.Util;

import static com.hp.jipp.encoding.Cycler.*;

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
        new Util();
        new Hook();
    }
}
