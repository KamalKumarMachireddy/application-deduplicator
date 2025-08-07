package org.example.appmanager;

import org.example.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AppCleanerGUI extends JFrame {

    private JTextField folderPathField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JPanel categorizedPanel;
    private Map<String, List<File>> currentDuplicates;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JDialog loadingDialog;

    // Custom Colors
    private final Color PRIMARY_BLUE = new Color(0, 100, 200);
    private final Color DANGER_RED = new Color(200, 0, 0);
    private final Color SUCCESS_GREEN = new Color(46, 139, 87);
    private final Color HOVER_COLOR = new Color(0, 120, 240);
    private final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);

    public AppCleanerGUI() {
        setTitle("Application Manager - Duplicate Cleaner");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initTopPanel();
        initCenterPanel();
        initStatusBar();

        // Apply system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Look and feel not set: " + e.getMessage());
        }

        setVisible(true);
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        folderPathField = new JTextField("Enter folder path or click 'Browse'", 35);
        folderPathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        folderPathField.setForeground(Color.GRAY);
        folderPathField.setPreferredSize(new Dimension(400, 32));
        folderPathField.setCaretColor(Color.BLACK);

        folderPathField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (folderPathField.getText().contains("Enter folder path")) {
                    folderPathField.setText("");
                    folderPathField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (folderPathField.getText().isEmpty()) {
                    folderPathField.setText("Enter folder path or click 'Browse'");
                    folderPathField.setForeground(Color.GRAY);
                }
            }
        });

        JButton browseButton = createStyledButton("Browse...", "Select a folder to scan", new Color(100, 100, 100), new Color(120, 120, 120));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton scanButton = createStyledButton("Start Scan", "Scan for duplicate files", PRIMARY_BLUE, HOVER_COLOR);
        scanButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        scanButton.setForeground(Color.BLACK);
        scanButton.addActionListener(e -> runApplicationManager(folderPathField.getText()));

        topPanel.add(new JLabel("Folder:"));
        topPanel.add(folderPathField);
        topPanel.add(browseButton);
        topPanel.add(scanButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void initCenterPanel() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(600);

        // === Left: Duplicate Files Table ===
        String[] columns = {"Select", "File Name", "Format", "Path", "Size (MB)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(28);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultTable.setGridColor(new Color(220, 220, 220));
        resultTable.setIntercellSpacing(new Dimension(2, 2));
        JScrollPane tableScroll = new JScrollPane(resultTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Duplicate Files"));
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // === Right: Categorized Panel + Buttons ===
        categorizedPanel = new JPanel();
        categorizedPanel.setLayout(new BoxLayout(categorizedPanel, BoxLayout.Y_AXIS));
        JScrollPane catScroll = new JScrollPane(categorizedPanel);
        catScroll.setPreferredSize(new Dimension(350, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        rightPanel.add(catScroll, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton deleteButton = createStyledButton("Delete Selected", "Delete selected duplicate files", DANGER_RED, new Color(220, 0, 0));
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setIcon(new DeleteIcon(16, 16));
        deleteButton.addActionListener(e -> deleteSelected());

        JButton exportButton = createStyledButton("Export Report", "Save a report of duplicates", SUCCESS_GREEN, new Color(60, 150, 100));
        exportButton.setIcon(new ExportIcon(16, 16));
        exportButton.addActionListener(e -> exportReport());

        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void initStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Ready ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // === Helper: Create Styled Button ===
    private JButton createStyledButton(String text, String tooltip, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) button.setToolTipText(tooltip);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // === Loading Dialog ===
    private void showLoadingDialog() {
        loadingDialog = new JDialog(this, "Scanning...", true);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // ✅ Fixed
        loadingDialog.setResizable(false);

        JLabel loadingLabel = new JLabel("Scanning, please wait...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setPreferredSize(new Dimension(300, 60));

        JProgressBar dialogBar = new JProgressBar();
        dialogBar.setIndeterminate(true);

        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogPanel.add(loadingLabel, BorderLayout.CENTER);
        dialogPanel.add(dialogBar, BorderLayout.SOUTH);

        loadingDialog.add(dialogPanel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setModal(true);

        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isVisible()) {
            loadingDialog.setVisible(false);
            loadingDialog.dispose();
        }
    }

    // === Main Scan Logic ===
    private void runApplicationManager(String directoryPath) {
        if (directoryPath.isEmpty() || !(new File(directoryPath).exists())) {
            JOptionPane.showMessageDialog(this, "Invalid folder path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showLoadingDialog(); // Show modal loading dialog

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                FileScanner fileScanner = new FileScanner();
                List<File> files = fileScanner.scanDirectory(directoryPath);

                if (files.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        hideLoadingDialog();
                        JOptionPane.showMessageDialog(AppCleanerGUI.this, "No application files found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    });
                    return null;
                }

                // Detect duplicates
                HashGenerator hashGen = new HashGenerator();
                DuplicateDetector duplicateDetector = new DuplicateDetector();
                Map<String, List<File>> duplicates = duplicateDetector.findDuplicates(files, hashGen);
                currentDuplicates = duplicates;

                // Categorize
                RuleLoader ruleLoader = new RuleLoader();
                Categorizer categorizer = new Categorizer();
                Map<String, List<String>> rules = ruleLoader.loadRules();
                Map<String, List<File>> categories = categorizer.categorize(files, rules);

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    updateTable(duplicates);
                    updateCategorizedPanel(categories);
                    hideLoadingDialog();
                    statusLabel.setText(" Scan complete. " + duplicates.size() + " duplicate groups found.");
                });

                return null;
            }
        };

        worker.execute(); // Run in background
    }

    private void updateTable(Map<String, List<File>> duplicates) {
        tableModel.setRowCount(0);
        int total = 0;
        for (List<File> group : duplicates.values()) {
            total += group.size();
            for (File file : group) {
                tableModel.addRow(new Object[]{
                        false,
                        file.getName(),
                        getFileExtension(file).toUpperCase(),
                        file.getAbsolutePath(),
                        String.format("%.2f", file.length() / (1024.0 * 1024.0))
                });
            }
        }
        statusLabel.setText(" Loaded " + total + " files in " + duplicates.size() + " groups.");
    }

    private void updateCategorizedPanel(Map<String, List<File>> categories) {
        categorizedPanel.removeAll();
        categorizedPanel.setBorder(BorderFactory.createTitledBorder("Categorized Files"));

        for (Map.Entry<String, List<File>> entry : categories.entrySet()) {
            List<File> files = entry.getValue();
            if (!files.isEmpty()) {
                CollapsiblePanel panel = new CollapsiblePanel(entry.getKey(), files.size() + " files");
                for (File f : files) {
                    panel.getContentPanel().add(new JLabel("📄 " + f.getName()));
                }
                categorizedPanel.add(panel);
            }
        }

        if (categories.isEmpty()) {
            categorizedPanel.add(new JLabel(" No files to display."));
        }

        categorizedPanel.revalidate();
        categorizedPanel.repaint();
    }

    private void deleteSelected() {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) count++;
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No files selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><b>Delete " + count + " selected files permanently?</b><br>This cannot be undone.</html>",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int deleted = 0;
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                String path = (String) tableModel.getValueAt(i, 3);
                File file = new File(path);
                if (file.exists() && file.delete()) {
                    tableModel.removeRow(i);
                    deleted++;
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "<html><b>" + deleted + " file(s) deleted successfully.</b><br>Removed from your system permanently.</html>",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportReport() {
        if (currentDuplicates == null || currentDuplicates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No duplicates to report.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Report");
        fc.setSelectedFile(new File("duplicate_report.txt"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".txt")) {
            file = new File(file.getParent(), file.getName() + ".txt");
        }

        try (FileWriter w = new FileWriter(file)) {
            w.write("=== Duplicate App Cleaner Report ===\n");
            w.write("Date: " + new Date() + "\n");
            w.write("Total Groups: " + currentDuplicates.size() + "\n\n");

            int g = 1;
            for (List<File> group : currentDuplicates.values()) {
                w.write("Group " + (g++) + " (" + group.size() + " files):\n");
                for (File f : group) {
                    w.write(String.format("  - %s (%.2f MB)\n",
                            f.getAbsolutePath(),
                            f.length() / (1024.0 * 1024.0)));
                }
                w.write("\n");
            }
            w.flush();
            JOptionPane.showMessageDialog(this,
                    "Report saved successfully!\n\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save report:\n" + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot > 0 && lastDot < name.length() - 1)
                ? name.substring(lastDot + 1).toLowerCase()
                : "unknown";
    }

    // === Collapsible Panel ===
    private static class CollapsiblePanel extends JPanel {
        private boolean expanded = true;

        public CollapsiblePanel(String title, String subtitle) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
            header.setBackground(new Color(240, 240, 240));

            JButton toggle = new JButton("▼");
            toggle.setFont(new Font("Arial", Font.BOLD, 10));
            toggle.setPreferredSize(new Dimension(20, 20));
            toggle.setFocusPainted(false);

            JLabel label = new JLabel(" " + title + " (" + subtitle + ")");
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            toggle.addActionListener(e -> {
                expanded = !expanded;
                toggle.setText(expanded ? "▼" : "▶");
                for (int i = 1; i < getComponentCount(); i++) {
                    getComponent(i).setVisible(expanded);
                }
                revalidate();
                repaint();
            });

            header.add(toggle);
            header.add(label);
            add(header);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            add(content);
        }

        public JPanel getContentPanel() {
            return (JPanel) getComponent(1);
        }
    }

    // === Simple Icons ===
    static class DeleteIcon implements Icon {
        private final int width, height;

        public DeleteIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x + 4, y + 4, x + width - 4, y + height - 4);
            g2.drawLine(x + width - 4, y + 4, x + 4, y + height - 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }
        @Override
        public int getIconHeight() { return height; }
    }

    static class ExportIcon implements Icon {
        private final int width, height;

        public ExportIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x + width / 2, y + 4, x + width / 2, y + height - 10);
            g2.drawLine(x + width / 2, y + height - 10, x + width / 2 - 6, y + height - 16);
            g2.drawLine(x + width / 2, y + height - 10, x + width / 2 + 6, y + height - 16);
            g2.drawRect(x + 4, y + 4, width - 8, height - 8);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }
        @Override
        public int getIconHeight() { return height; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppCleanerGUI());
    }
}