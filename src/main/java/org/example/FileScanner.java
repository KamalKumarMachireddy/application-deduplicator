package org.example;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.*;

public class FileScanner {
    private static final String[] APP_EXTENSIONS = {
            "exe", "msi", "jar", "apk", "dmg", "deb", "rpm", "app", "bat", "cmd", "sh", "bar", "py", "class", "out", "desktop", "json", "dll", "java", "com", "gadget", "wsf", "pif", "vb", "vbs", "ps1", "msc", "cpl", "xap", "air", "nexe", "kext", "bin"
    };

    public List<File> scanDirectory(String path) {
        List<File> appFiles = new ArrayList<>();
        File directory = new File(path);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + path);
            return appFiles;
        }

        System.out.println("Scanning directory: " + path);

        try {
            Collection<File> files = FileUtils.listFiles(
                    directory,
                    APP_EXTENSIONS,
                    true
            );

            for (File file : files) {
                if (file.length() > 0) {
                    appFiles.add(file);
                }
            }

        } catch (Exception e) {
            System.err.println("Error scanning directory: " + e.getMessage());
        }

        return appFiles;
    }
}
