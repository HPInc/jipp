package com.hp.jipp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IntegerType;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class BinaryTest {
    @Test
    public void cycleBinaries() throws Exception {
        File printerDir = new File(getClass().getResource("/printer").getPath());
        assertTrue(printerDir.isDirectory());
        Iterable<File> binFiles = Iterables.filter(Files.fileTreeTraverser().breadthFirstTraversal(printerDir),
                new Predicate<File>() {
                    @Override
                    public boolean apply(File file) {
                        return file.getName().endsWith(".bin");
                    }
                });

        // For each bin file cycle and print
        for (File binFile : binFiles) {
            byte[] bytes = Files.toByteArray(binFile);
            // Parse and build each packet to ensure that we can model it perfectly in memory
            Packet packet = Packet.read(new DataInputStream(new ByteArrayInputStream(bytes)));
            assertArrayEquals(packet.getBytes(), bytes);

            System.out.println("\nPacket decode for " + binFile.getName());
            System.out.println(packet.describe(Status.ENCODER, Attributes.All));
        }
    }
}
