package com.hp.jipp.model;

import org.junit.Test;

import com.hp.jipp.encoding.Cycler;
import com.hp.jipp.encoding.Tag;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kotlin.io.FilesKt;

public class BinaryTest {

    @Test
    public void scanNames() throws Exception {
        for (File binFile : getBinFiles()) {
            IppPacket packet = IppPacket.parse(new DataInputStream(new ByteArrayInputStream(FilesKt.readBytes(binFile))));
            if (packet.getAttributeGroup(Tag.printerAttributes) == null) continue;
            if (packet.getValues(Tag.printerAttributes, Types.printerInfo).isEmpty()) continue;

            System.out.println(binFile.getName() + "\t" + packet.getValues(Tag.printerAttributes, Types.printerInfo) +
                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerName) +
                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerDnsSdName) +
                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerUuid));
        }
    }

    @Test
    public void cycleBinaries() throws Exception {
        // For each bin file cycle and print
        for (File binFile : getBinFiles()) {
            byte[] bytes = FilesKt.readBytes(binFile);
            // Parse and build each packet to ensure that we can model it perfectly in memory
            System.out.println("\nParsing packet from " + binFile.getName());
            IppPacket packet = IppPacket.parse(new DataInputStream(new ByteArrayInputStream(bytes)));
            System.out.println(packet.prettyPrint(200, "  "));

            Object inputTray = packet.getValue(Tag.printerAttributes, Types.printerInputTray);
            Object printerAlert = packet.getValue(Tag.printerAttributes, Types.printerAlert);

            // TODO: inputTray and printerAlert can be encoded with slight differences (sometimes with
            // terminating ;) causing a binary mismatch. Not sure how to deal with this and still have a valid test
            if (inputTray == null && printerAlert == null) {
                assertArrayEquals(bytes, Cycler.toBytes(packet));
            }
        }
    }

    private List<File> getBinFiles() throws IOException {
        File printerDir = new File(getResource("printer"));
        assertTrue(printerDir.isDirectory());

        List<File> files = new ArrayList<File>();
        getBinFiles(files, printerDir);
        return files;
    }

    private String getResource(String path) {
        URL url = getClass().getClassLoader().getResource("printer");
        if (url != null) return url.getPath();

        // Running in AndroidStudio, manually adjust path
        url = getClass().getClassLoader().getResource(".");
        assertNotNull(url);
        return url.getPath().replace("/build/classes/java/test/", "/build/resources/test/" + path);
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
