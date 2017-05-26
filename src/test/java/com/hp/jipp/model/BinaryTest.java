package com.hp.jipp.model;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.hp.jipp.encoding.Tag;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;

public class BinaryTest {
    private Packet.Parser parser = Packet.parserOf(Attributes.All);

    @Test
    public void scanNames() throws Exception {
        for (File binFile : getBinFiles()) {
            Packet packet = parser.parse(new DataInputStream(new ByteArrayInputStream(Files.toByteArray(binFile))));
            if (packet.getAttributeGroup(Tag.PrinterAttributes).isPresent()) {
                System.out.println("For " + binFile);
                System.out.println("Printer info=" + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterInfo) +
                        " name=" + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterName) +
                        " uris= " + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterUriSupported));
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
            Packet packet = parser.parse(new DataInputStream(new ByteArrayInputStream(bytes)));
            System.out.println(packet.prettyPrint(200, "  "));

            Optional<?> inputTray = packet.getValue(Tag.PrinterAttributes, Attributes.PrinterInputTray);
            Optional<?> printerAlert = packet.getValue(Tag.PrinterAttributes, Attributes.PrinterAlert);
            if (!inputTray.isPresent() && !printerAlert.isPresent()) {
                // TODO: Deal with the fact that device encoding differs slightly for some of these items,
                // (terminating ; anyone?) causing binary mismatch.
                assertArrayEquals(bytes, PacketTest.getBytes(packet));
            }
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
            int reps = 100;
            for (int i = 0; i < reps; i++) {
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                    parser.parse(in);
                }
            }
            timer.stop();
            System.out.println("Rx " + reps + " of " + binFile.getName() + " (" + bytes.length + " bytes): " + timer);
        }
    }
}
