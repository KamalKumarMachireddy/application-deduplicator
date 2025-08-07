package org.example;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.*;

public class HashGenerator {

    public String generateMD5Hash(File file) throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read file: " + file.getPath());
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        }
    }
}