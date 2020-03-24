package com.hp.jipp.model;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.IppPacket;
import kotlin.io.FilesKt;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.hp.jipp.util.BytesKt.toWrappedHexString;
import static org.junit.Assert.*;

public class BinaryTest {
    @Test
    public void cycleBinaries() throws IOException {
        for (File binFile : getBinFiles()) {
            cycleBinary(binFile.getName(), FilesKt.readBytes(binFile));
        }
    }

    private void cycleBinary(String fileName, byte[] inputBytes) throws IOException {
        System.out.println("\n========= Parsing " + fileName);
        IppInputStream input = new IppInputStream(new ByteArrayInputStream(inputBytes));

        IppPacket packet = input.readPacket();
        System.out.println(packet.prettyPrint(120, "  "));

        // Now repack it and make sure the bytes are the same
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try (IppOutputStream output = new IppOutputStream(bytesOut)) {
            output.write(packet);
        }
        // Compare input bytes to output bytes in hex string format for easy comparison
        assertEquals(toWrappedHexString(inputBytes), toWrappedHexString(bytesOut.toByteArray()));
    }

    private List<File> getBinFiles() {
        File printerDir = new File(getResource("printer"));
        assertTrue(printerDir.isDirectory());

        List<File> files = new ArrayList<>();
        getBinFiles(files, printerDir);
        return files;
    }

    private String getResource(String path) {
        URL url = getClass().getClassLoader().getResource("printer");
        if (url != null) return url.getPath();

        // If running in AndroidStudio, manually adjust path
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
                IppInputStream in = new IppInputStream(new ByteArrayInputStream(bytes));
                in.readPacket();
                in.close();
            }
            System.out.println(((System.nanoTime() - nanos)/1000/reps) + "us for each rx of " + reps + " of " +
                    binFile.getName() + " (" + bytes.length + " bytes): ");
        }
    }
}
