package com.hp.jipp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Bytes {
    private static final int BUFFER_SIZE = 12 * 1024;

    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long size = 0;
        while (true) {
            int readSize = input.read(buffer);
            if (readSize == -1) break;
            output.write(buffer, 0, readSize);
            size += readSize;
        }
        return size;
    }

    public static byte[] read(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bytes.copy(new FileInputStream(file), out);
        return out.toByteArray();
    }
}
