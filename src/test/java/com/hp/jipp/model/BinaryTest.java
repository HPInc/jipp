package com.hp.jipp.model;

import org.junit.Test;

import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.util.Bytes;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BinaryTest {
    private Packet.Parser parser = Packet.parserOf(Attributes.All);

    @Test
    public void scanNames() throws Exception {
        for (File binFile : getBinFiles()) {
            Packet packet = parser.parse(new DataInputStream(new ByteArrayInputStream(Bytes.read(binFile))));
            if (packet.getAttributeGroup(Tag.PrinterAttributes) == null) continue;
            if (packet.getValues(Tag.PrinterAttributes, Attributes.PrinterInfo).isEmpty()) continue;

            System.out.println(binFile.getName() + "\t" + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterInfo) +
                    "\t" + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterName) +
                    "\t" + packet.getValues(Tag.PrinterAttributes, Attributes.PrinterDnsSdName));
        }
    }

    @Test
    public void cycleBinaries() throws Exception {
        // For each bin file cycle and print
        for (File binFile : getBinFiles()) {
            byte[] bytes = Bytes.read(binFile);
            // Parse and build each packet to ensure that we can model it perfectly in memory
            System.out.println("\nParsing packet from " + binFile.getName());
            Packet packet = parser.parse(new DataInputStream(new ByteArrayInputStream(bytes)));
            System.out.println(packet.prettyPrint(200, "  "));

            Object inputTray = packet.getValue(Tag.PrinterAttributes, Attributes.PrinterInputTray);
            Object printerAlert = packet.getValue(Tag.PrinterAttributes, Attributes.PrinterAlert);
            if (inputTray == null && printerAlert == null) {
                // TODO: Deal with the fact that device encoding differs slightly for some of these items,
                // (terminating ; anyone?) causing binary mismatch.
                assertArrayEquals(bytes, PacketTest.getBytes(packet));
            }
        }
    }

    private List<File> getBinFiles() {
        File printerDir = new File(getClass().getResource("/printer").getPath());
        assertTrue(printerDir.isDirectory());

        List<File> files = new ArrayList<>();
        getBinFiles(files, printerDir);
        return files;
    }

    private void getBinFiles(List<File> files, File dir) {
        File[] foundFiles = dir.listFiles();
        if (foundFiles == null) return;
        for (File file : foundFiles) {
            if (file.isDirectory()) getBinFiles(files, file);
            else if (file.getName().endsWith(".bin")) {
                files.add(file);
            }
        }
    }

    // Enable when necessary, we have too many test files to try this every time
//    @Test
//    public void speedTest() throws Exception {
//        for (File binFile : getBinFiles()) {
//            byte[] bytes = Bytes.read(binFile);
//            long nanos = System.nanoTime();
//            int reps = 100;
//            for (int i = 0; i < reps; i++) {
//                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
//                    parser.parse(in);
//                }
//            }
//            System.out.println("Rx " + reps + " of " + binFile.getName() + " (" + bytes.length + " bytes): " +
//                    ((System.nanoTime() - nanos)/1000) + "us");
//        }
//    }
}
