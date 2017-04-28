package com.hp.jipp.model;

import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class BinaryTest {
    @Test
    public void scanUris() throws Exception {
        for (File binFile : getBinFiles()) {
            Packet packet = Packet.read(new DataInputStream(new ByteArrayInputStream(Files.toByteArray(binFile))));
            if (packet.getAttributeGroup(Tag.PrinterAttributes).isPresent()) {
                System.out.println("Printer: " + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterInfo) +
                        " has URIs: " + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterUriSupported));
            }
        }
    }

    @Test
    public void cycleBinaries() throws Exception {
        // For each bin file cycle and print
        for (File binFile : getBinFiles()) {
            byte[] bytes = Files.toByteArray(binFile);
            // Parse and build each packet to ensure that we can model it perfectly in memory
            System.out.println("\nParsing packet from " + binFile.getName());
            Packet packet = Packet.read(new DataInputStream(new ByteArrayInputStream(bytes)));
            System.out.println(packet.describe(Status.ENCODER, Attributes.All));
            assertArrayEquals(packet.getBytes(), bytes);
        }
    }

    private Iterable<File> getBinFiles() {
        File printerDir = new File(getClass().getResource("/printer").getPath());
        assertTrue(printerDir.isDirectory());
        return Iterables.filter(Files.fileTreeTraverser().breadthFirstTraversal(printerDir),
                new Predicate<File>() {
                    @Override
                    public boolean apply(File file) {
                        return file.getName().endsWith(".bin");
                    }
                });
    }

    @Test
    public void speedTest() throws Exception {
        for (File binFile : getBinFiles()) {
            byte[] bytes = Files.toByteArray(binFile);
            Stopwatch timer = Stopwatch.createStarted();
            int reps = 1000;
            for (int i = 0; i < reps; i++) {
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                    Packet.read(in);
                }
            }
            timer.stop();
            System.out.println("Rx " + reps + " of " + binFile.getName() + " (" + bytes.length + " bytes): " + timer);
        }
    }
}
