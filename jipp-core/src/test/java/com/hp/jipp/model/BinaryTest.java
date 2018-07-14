package com.hp.jipp.model;

import com.hp.jipp.encoding.*;
import org.junit.Test;

import static com.hp.jipp.util.BytesKt.toHexString;
import static com.hp.jipp.util.BytesKt.toWrappedHexString;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kotlin.io.FilesKt;

public class BinaryTest {

//    @Test
//    public void scanNames() throws Exception {
//        for (File binFile : getBinFiles()) {
//            IppPacket packet = IppPacket.parse(new DataInputStream(new ByteArrayInputStream(FilesKt.readBytes(binFile))));
//            if (packet.getAttributeGroup(Tag.printerAttributes) == null) continue;
//            if (packet.getValues(Tag.printerAttributes, Types.printerInfo).isEmpty()) continue;
//
//            System.out.println(binFile.getName() + "\t" + packet.getValues(Tag.printerAttributes, Types.printerInfo) +
//                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerName) +
//                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerDnsSdName) +
//                    "\t" + packet.getValues(Tag.printerAttributes, Types.printerUuid));
//        }
//    }
//


    @Test
    public void cycleBinaries() throws IOException {
        for (File binFile : getBinFiles()) {
            cycleBinary(binFile.getName(), FilesKt.readBytes(binFile));
        }
    }

    private void cycleBinary(String fileName, byte[] inputBytes) throws IOException {
        System.out.println("\n========= Parsing " + fileName);
        IppInputStream input = new IppInputStream(new ByteArrayInputStream(inputBytes));

        IppPacket packet = IppPacket.read(input);
        System.out.println(fileName + packet.prettyPrint(120, "  "));

        // Now repack it and make sure the bytes are the same
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IppOutputStream output = new IppOutputStream(bytesOut);
        packet.write(output);
        output.close();
        assertEquals(toWrappedHexString(inputBytes), toWrappedHexString(bytesOut.toByteArray()));
    }

    private List<File> getBinFiles() {
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

    @Test
    public void speedTest() throws Exception {
        for (File binFile : getBinFiles()) {
            byte[] bytes = FilesKt.readBytes(binFile);
            long nanos = System.nanoTime();
            int reps = 50;
            for (int i = 0; i < reps; i++) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
                IppPacket.read(in);
                in.close();
            }
            System.out.println(((System.nanoTime() - nanos)/1000/reps) + "us for each rx of " + reps + " of " +
                    binFile.getName() + " (" + bytes.length + " bytes): ");
        }
    }
}
