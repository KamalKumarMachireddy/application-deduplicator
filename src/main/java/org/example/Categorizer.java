package org.example;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Categorizer {

    public Map<String, List<File>> categorize(List<File> files, Map<String, List<String>> rules) {
        Map<String, List<File>> categories = new HashMap<>();

        for (String category : rules.keySet()) {
            categories.put(category, new ArrayList<>());
        }
        categories.put("Uncategorized", new ArrayList<>());

        System.out.println("Categorizing applications...");

        for (File file : files) {
            if (!file.exists()) continue;

            String fileName = file.getName().toLowerCase();
            String filePath = file.getPath().toLowerCase();
            String fileBaseName = getBaseName(fileName);
            boolean categorized = false;

            System.out.println("Analyzing file: " + fileName);

            for (Map.Entry<String, List<String>> rule : rules.entrySet()) {
                String category = rule.getKey();
                List<String> keywords = rule.getValue();

                for (String keyword : keywords) {
                    String lowerKeyword = keyword.toLowerCase();

                    if (isMatch(fileName, filePath, fileBaseName, lowerKeyword)) {
                        categories.get(category).add(file);
                        categorized = true;
                        System.out.println("  -> Categorized as: " + category + " (matched: " + keyword + ")");
                        break;
                    }
                }

                if (categorized) break;
            }

            if (!categorized) {
                categories.get("Uncategorized").add(file);
                System.out.println("  -> Uncategorized: " + fileName);
            }
        }

        System.out.println("\n=== CATEGORIZATION SUMMARY ===");
        for (Map.Entry<String, List<File>> entry : categories.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().size() + " files");
            }
        }

        return categories;
    }

    private boolean isMatch(String fileName, String filePath, String fileBaseName, String keyword) {
        if (fileName.contains(keyword)) {
            return true;
        }
        if (fileBaseName.contains(keyword)) {
            return true;
        }
        if (filePath.contains(keyword)) {
            return true;
        }
        if (containsWholeWord(fileName, keyword) ||
                containsWholeWord(filePath, keyword) ||
                containsWholeWord(fileBaseName, keyword)) {
            return true;
        }
        if (isFuzzyMatch(fileName, keyword) ||
                isFuzzyMatch(fileBaseName, keyword)) {
            return true;
        }

        return false;
    }

    private String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot).toLowerCase();
        }
        return fileName.toLowerCase();
    }

    private boolean containsWholeWord(String text, String word) {
        String pattern = ".*\\b" + Pattern.quote(word) + "\\b.*";
        return text.matches(pattern);
    }

    private boolean isFuzzyMatch(String text, String keyword) {
        Map<String, String[]> fuzzyRules = Map.ofEntries(
                Map.entry("chrome", new String[]{"google", "chromium"}),
                Map.entry("firefox", new String[]{"mozilla", "ff"}),
                Map.entry("java", new String[]{"jdk", "jre", "openjdk"}),
                Map.entry("python", new String[]{"py", "python3"}),
                Map.entry("office", new String[]{"msoffice", "microsoft"}),
                Map.entry("photoshop", new String[]{"ps", "adobe"}),
                Map.entry("antivirus", new String[]{"av", "security"}),
                Map.entry("game", new String[]{"gaming", "play"}),
                Map.entry("media", new String[]{"multimedia", "player"}),
                Map.entry("browser", new String[]{"web", "internet"}),
                Map.entry("ide", new String[]{"editor", "development"}),
                Map.entry("git", new String[]{"github", "version"})
        );

        if (fuzzyRules.containsKey(keyword)) {
            for (String alternative : fuzzyRules.get(keyword)) {
                if (text.contains(alternative)) {
                    return true;
                }
            }
        }

        for (Map.Entry<String, String[]> entry : fuzzyRules.entrySet()) {
            if (text.contains(entry.getKey())) {
                for (String alternative : entry.getValue()) {
                    if (alternative.equals(keyword)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}