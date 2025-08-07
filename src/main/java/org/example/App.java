package org.example;

import java.io.File;
import java.util.*;

public class App {
    public static void main(String[] args) {
        System.out.println("**** Application Manager - JP-001 ****");

        Scanner scanner = new Scanner(System.in);
        Logger logger = new Logger();

        try {
            System.out.print("Enter directory to scan: ");
            String directory = scanner.nextLine();

            if (directory.isEmpty()) {
                directory = System.getProperty("user.home");
            }

            FileScanner fileScanner = new FileScanner();
            List<File> files = fileScanner.scanDirectory(directory);

            System.out.println("Found " + files.size() + " application files.");
            logger.log("Scanned directory: " + directory + ", Found: " + files.size() + " files");

            if (files.isEmpty()) {
                System.out.println("No application files found. Exiting.");
                return;
            }

            HashGenerator hashGen = new HashGenerator();
            DuplicateDetector duplicateDetector = new DuplicateDetector();
            Map<String, List<File>> duplicates = duplicateDetector.findDuplicates(files, hashGen);

            CLI cli = new CLI();
            if (!duplicates.isEmpty()) {
                cli.showDuplicates(duplicates);
                cli.promptAndDelete(duplicates, scanner, logger);
            } else {
                System.out.println("No duplicates found!");
            }

            RuleLoader ruleLoader = new RuleLoader();
            Map<String, List<String>> rules = ruleLoader.loadRules();

            Categorizer categorizer = new Categorizer();
            Map<String, List<File>> categories = categorizer.categorize(files, rules);

            cli.showCategories(categories);

            logger.log("Application management completed successfully.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.log("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
