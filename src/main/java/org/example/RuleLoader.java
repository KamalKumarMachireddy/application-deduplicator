package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class RuleLoader {

    public Map<String, List<String>> loadRules() {
        Map<String, List<String>> rules = new HashMap<>();

        rules.put("Development", Arrays.asList("java", "python", "sdk", "eclipse", "idea", "code", "git", "maven", "gradle"));
        rules.put("Media", Arrays.asList("vlc", "player", "music", "video", "media", "spotify", "itunes", "winamp"));
        rules.put("Security", Arrays.asList("antivirus", "firewall", "kaspersky", "norton", "security", "defender"));
        rules.put("Games", Arrays.asList("game", "steam", "origin", "battle", "launcher", "epic"));
        rules.put("Office", Arrays.asList("office", "word", "excel", "powerpoint", "pdf", "reader", "writer"));
        rules.put("Browser", Arrays.asList("chrome", "firefox", "safari", "edge", "browser", "opera"));
        rules.put("System", Arrays.asList("driver", "system", "windows", "utility", "tool", "manager"));

        try {
            File rulesFile = new File("rules.json");
            if (rulesFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<String>> loadedRules = mapper.readValue(rulesFile, Map.class);
                rules.putAll(loadedRules);
                System.out.println("Loaded custom rules from rules.json");
            } else {
                createDefaultRulesFile(rules);
            }
        } catch (Exception e) {
            System.out.println("Using default rules. Error loading rules.json: " + e.getMessage());
        }

        return rules;
    }

    private void createDefaultRulesFile(Map<String, List<String>> rules) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("rules.json"), rules);
            System.out.println("Created default rules.json file");
        } catch (Exception e) {
            System.out.println("Could not create rules.json: " + e.getMessage());
        }
    }
}
