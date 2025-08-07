package org.example;

import java.io.File;
import java.util.*;

public class DuplicateDetector {

    public Map<String, List<File>> findDuplicates(List<File> files, HashGenerator hashGen) {
        Map<String, List<File>> hashToFiles = new HashMap<>();
        Map<String, List<File>> duplicates = new HashMap<>();

        System.out.println("Generating MD5 hashes and detecting duplicates...");

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);

            try {
                String hash = hashGen.generateMD5Hash(file);

                hashToFiles.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);

                if ((i + 1) % 10 == 0 || i == files.size() - 1) {
                    System.out.printf("Progress: %d/%d files processed%n", i + 1, files.size());
                }

            } catch (Exception e) {
                System.err.println("Error hashing file " + file.getName() + ": " + e.getMessage());
            }
        }

        for (Map.Entry<String, List<File>> entry : hashToFiles.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println("Found " + duplicates.size() + " duplicate groups.");
        return duplicates;
    }
}
