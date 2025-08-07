package org.example;

import java.io.File;
import java.util.*;

public class CLI {

    public void showDuplicates(Map<String, List<File>> duplicates) {
        System.out.println("\nDUPLICATE APPLICATIONS FOUND");

        int groupNum = 1;
        for (Map.Entry<String, List<File>> entry : duplicates.entrySet()) {
            System.out.println("\nDuplicate Group " + groupNum + ":");
            List<File> files = entry.getValue();

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                System.out.printf("  %d. %s (%s) - %.2f MB\n",
                        i + 1,
                        file.getName(),
                        file.getParent(),
                        file.length() / (1024.0 * 1024.0));
            }
            groupNum++;
        }
    }

    public void promptAndDelete(Map<String, List<File>> duplicates, Scanner scanner, Logger logger) {
        System.out.println("\nDUPLICATE REMOVAL");
        System.out.print("Do you want to remove duplicates? (y/n): ");

        String response = scanner.nextLine().trim().toLowerCase();
        if (!response.equals("y") && !response.equals("yes")) {
            System.out.println("Skipping duplicate removal.");
            return;
        }

        int groupNum = 1;
        for (Map.Entry<String, List<File>> entry : duplicates.entrySet()) {
            List<File> files = entry.getValue();

            System.out.println("\nDuplicate Group " + groupNum + ":");
            for (int i = 0; i < files.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, files.get(i).getName());
            }

            System.out.printf("Which file to KEEP? (1-%d, or 's' to skip): ", files.size());
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("s")) {
                System.out.println("Skipped group " + groupNum);
                groupNum++;
                continue;
            }

            try {
                int keepIndex = Integer.parseInt(choice) - 1;
                if (keepIndex >= 0 && keepIndex < files.size()) {
                    File keepFile = files.get(keepIndex);

                    for (int i = 0; i < files.size(); i++) {
                        if (i != keepIndex) {
                            File deleteFile = files.get(i);
                            if (deleteFile.delete()) {
                                System.out.println("Deleted: " + deleteFile.getName());
                                logger.log("Deleted duplicate: " + deleteFile.getPath());
                                files.remove(i);
                                if (i < keepIndex) keepIndex--;
                                i--;
                            } else {
                                System.out.println("Failed to delete: " + deleteFile.getName());
                                logger.log("Failed to delete: " + deleteFile.getPath());
                            }
                        }
                    }
                } else {
                    System.out.println("Invalid choice. Skipping group " + groupNum);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Skipping group " + groupNum);
            }

            groupNum++;
        }
    }

    public void showCategories(Map<String, List<File>> categories) {
        System.out.println("\n=== CATEGORIZED APPLICATIONS ===");

        for (Map.Entry<String, List<File>> entry : categories.entrySet()) {
            String category = entry.getKey();
            List<File> files = entry.getValue();

            if (!files.isEmpty()) {
                System.out.println("\n-> " + category.toUpperCase() + " (" + files.size() + " files):");
                for (File file : files) {
                    if (file.exists()) {
                        System.out.printf("   - %s (%.2f MB)\n",
                                file.getName(),
                                file.length() / (1024.0 * 1024.0));
                    }
                }
            }
        }
    }
}
