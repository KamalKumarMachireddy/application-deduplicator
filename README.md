# Application Manager

**Application Manager** is a Java-based tool for identifying and removing duplicate application files based on their **actual content**, not just metadata. It also categorizes applications using customizable, rule-based classification logic. Designed as a CLI tool, this project offers precise management of executable files, scripts, installers, and more.

---

## Features

- Detects duplicate application files using content-based hashing (MD5).
- Supports multiple common application file formats: `.exe`, `.jar`, `.apk`, `.msi`, `.sh`, `.bat`, `.py`, etc.
- Prompts the user to review and selectively delete duplicates.
- Categorizes applications using user-defined rules (stored as JSON).
- Logs all major actions for auditing and traceability.

---

## Technologies Used

- **Java 21**
- **Apache Commons IO**
- **Apache Commons Codec**
- **Jackson Databind (for JSON rule parsing)**
- **Maven** for dependency management and build automation

---

## Project Structure

```
application-deduplicator/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── appmanager/
│                       ├──AppCleanerGUI
│                   ├── App.java
│                   ├── FileScanner.java
│                   ├── HashGenerator.java
│                   ├── DuplicateDetector.java
│                   ├── Categorizer.java
│                   ├── RuleLoader.java
│                   ├── CLI.java
│                   └── Logger.java
├── rules.json
├── pom.xml
```

---

## How It Works

1. **Directory Input**: User provides the directory path to scan for application files.
2. **Duplicate Detection**: Files are hashed using MD5 and grouped by identical hashes.
3. **User Prompt**: User is prompted to keep one file from each duplicate group; others are deleted.
4. **Categorization**: Files are categorized based on keywords/rules defined in a JSON file.
5. **Logging**: All actions like deletions and categorization results are logged.

---

## Rule Configuration

Rules are defined in a `rules.json` file like:

```json
{
  "development": ["ide", "java", "python", "editor"],
  "media": ["music", "video", "player"],
  "browsers": ["chrome", "firefox", "edge"],
  "utilities": ["tool", "manager", "cleaner"]
}
```

The app name is checked against these keywords to assign a category.

---

## Getting Started

### Prerequisites

- Java 21 installed and set in your environment
- Maven installed and added to PATH
- IntelliJ IDEA (optional, for development)

### Build and Run

```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="org.example.App"
```

---

## Logs

All logs are stored in a file named `app-manager.log` in the root directory of the project.

---

## Limitations

- Does not extract or analyze compressed/installer contents.
- Hash-based matching won't detect functionally identical apps with different binary structures.

---

## Future Enhancements

- Integrate manifest/config analysis.
- Support file extraction for deep inspection (ZIP, JAR).
- Add database-backed rule engine.
