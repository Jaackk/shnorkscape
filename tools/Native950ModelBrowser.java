package com.rs.tools.modelviewer;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.modern.FlatCacheRepository;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Standalone 950 object/NPC browser. Lists every named object and NPC definition found in a
 * flat 950 cache and hands you the exact {@code ;;obj}/{@code ;;npc} developer command to spawn
 * it at your feet in a live, loopback-connected 950 client session (see
 * Native950DevelopmentCommands and Native950DiagnosticSpawns). The server never sends model
 * geometry over the wire; spawning an id makes the real client resolve and render the model
 * itself from its own cache, so this reuses the actual, correct renderer rather than decoding
 * or drawing anything here.
 *
 * <p>Deliberately outside the engine build, same as the other tools/ utilities: compile with
 * javac against the packaged engine jar and run with java, no server boot required.
 */
public final class Native950ModelBrowser {
    private Native950ModelBrowser() { }

    public static void main(String[] args) throws Exception {
        Path cachePath = Paths.get(args.length > 0 ? args[0] : "cache");
        if (!Files.isDirectory(cachePath.resolve("255"))) {
            System.err.println("Not a flat-file cache (missing 255/): " + cachePath.toAbsolutePath());
            System.exit(1);
        }
        FlatCacheRepository repository = new FlatCacheRepository(cachePath);
        Cache.initFlatReadOnly(cachePath);
        SwingUtilities.invokeLater(() -> new BrowserFrame(repository).setVisible(true));
    }

    // ------------------------------------------------------------------ scanning

    /** One row: id, display name, a short status note, and whether spawning should work. */
    private static final class Entry {
        final int id;
        final String name;
        final String note;
        final boolean spawnable;

        Entry(int id, String name, String note, boolean spawnable) {
            this.id = id;
            this.name = name;
            this.note = note;
            this.spawnable = spawnable;
        }
    }

    private enum Category {
        OBJECT(16, 8, "obj", "Objects"),
        NPC(18, 7, "npc", "NPCs");

        final int indexId;
        final int shift;
        final String command;
        final String label;

        Category(int indexId, int shift, String command, String label) {
            this.indexId = indexId;
            this.shift = shift;
            this.command = command;
            this.label = label;
        }
    }

    /** Scans a whole category from the cache. Runs on a background thread. */
    private static List<Entry> scan(FlatCacheRepository repository, Category category,
                                     java.util.function.IntConsumer progress) throws Exception {
        FlatCacheRepository.Index index = repository.getIndexes().get(category.indexId);
        if (index == null) throw new IllegalStateException("Missing cache index " + category.indexId);
        List<Entry> entries = new ArrayList<>();
        int scanned = 0;
        for (FlatCacheRepository.Group group : index.getGroups().values()) {
            Map<Integer, byte[]> files = category == Category.NPC
                    ? safeReadGroup(repository, category.indexId, group.id) : null;
            for (int file : group.getFileIds()) {
                int id = (group.id << category.shift) | file;
                try {
                    Entry entry = category == Category.OBJECT
                            ? decodeObject(id)
                            : decodeNpc(id, files == null ? null : files.get(file));
                    if (entry != null) entries.add(entry);
                } catch (Exception unreadable) {
                    // skip definitions that fail to decode rather than showing garbage
                }
                if (++scanned % 1000 == 0) progress.accept(scanned);
            }
        }
        progress.accept(scanned);
        return entries;
    }

    private static Map<Integer, byte[]> safeReadGroup(FlatCacheRepository repository, int indexId, int groupId) {
        try {
            return repository.readGroup(indexId, groupId);
        } catch (Exception unreadable) {
            return null;
        }
    }

    private static Entry decodeObject(int id) {
        ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(id);
        if (!def.loaded) return null;
        boolean hasModel = hasAnyModel(def.models);
        boolean variableForm = def.transforms != null;
        String note = variableForm ? "variable form (won't spawn directly)"
                : !hasModel ? "no world model" : "";
        return new Entry(id, cleanName(def.name), note, hasModel && !variableForm);
    }

    private static Entry decodeNpc(int id, byte[] raw) {
        if (raw == null) return null;
        NPCDefinitions def = NPCDefinitions.decodeStrict947(id, raw, null);
        boolean hasModel = def.models != null && def.models.length > 0;
        String note = !hasModel ? "no model" : "combat level " + def.combatLevel;
        return new Entry(id, cleanName(def.name), note, hasModel);
    }

    private static boolean hasAnyModel(int[][] models) {
        if (models == null) return false;
        for (int[] shape : models) if (shape != null && shape.length > 0) return true;
        return false;
    }

    private static String cleanName(String name) {
        return name == null || "null".equals(name) || name.trim().isEmpty() ? "<unnamed>" : name;
    }

    // ------------------------------------------------------------------ UI

    private static final class BrowserFrame extends JFrame {
        BrowserFrame(FlatCacheRepository repository) {
            super("950 Model Browser");
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JLabel hint = new JLabel(
                    "<html>Pick an object or NPC below, then type the shown command into your 950 client's chat box "
                            + "(loopback dev session, started with <code>-Dataraxia950.devTools=true</code>) to spawn it "
                            + "at your character's feet in the real client. Rotate the camera in-game to look it over.</html>");
            hint.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            JTabbedPane tabs = new JTabbedPane();
            for (Category category : Category.values()) {
                tabs.addTab(category.label, new CategoryPanel(repository, category));
            }

            setLayout(new BorderLayout());
            add(hint, BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);
            setSize(860, 620);
            setMinimumSize(new Dimension(600, 400));
            setLocationRelativeTo(null);
        }
    }

    private static final class CategoryPanel extends JPanel {
        private final Category category;
        private final EntryTableModel tableModel = new EntryTableModel();
        private final JTable table = new JTable(tableModel);
        private final JTextField search = new JTextField();
        private final JLabel status = new JLabel(" scanning…");
        private final JTextField commandField = new JTextField();
        private final JLabel detail = new JLabel(" ");
        private final JButton copyButton = new JButton("Copy Command");

        CategoryPanel(FlatCacheRepository repository, Category category) {
            super(new BorderLayout(4, 4));
            this.category = category;

            TableRowSorter<EntryTableModel> sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            table.setAutoCreateRowSorter(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(e -> onSelect());
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) copy();
                }
            });

            search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(sorter); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(sorter); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(sorter); }
            });

            JPanel north = new JPanel(new BorderLayout(4, 4));
            north.add(new JLabel("Search name or id:"), BorderLayout.WEST);
            north.add(search, BorderLayout.CENTER);
            north.add(status, BorderLayout.EAST);

            commandField.setEditable(false);
            commandField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            copyButton.setEnabled(false);
            copyButton.addActionListener(e -> copy());

            JPanel south = new JPanel(new BorderLayout(4, 4));
            south.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JPanel commandRow = new JPanel(new BorderLayout(4, 4));
            commandRow.add(commandField, BorderLayout.CENTER);
            commandRow.add(copyButton, BorderLayout.EAST);
            south.add(detail, BorderLayout.NORTH);
            south.add(commandRow, BorderLayout.CENTER);

            add(north, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);

            startLoading(repository);
        }

        private void applyFilter(TableRowSorter<EntryTableModel> sorter) {
            String text = search.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
                return;
            }
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
            } catch (PatternSyntaxException badPattern) {
                sorter.setRowFilter(null);
            }
        }

        private void onSelect() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                commandField.setText("");
                detail.setText(" ");
                copyButton.setEnabled(false);
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Entry entry = tableModel.entryAt(modelRow);
            commandField.setText(";;" + category.command + " " + entry.id);
            detail.setText(entry.spawnable
                    ? " " + entry.name + " (" + entry.id + ") — " + (entry.note.isEmpty() ? "ready to spawn" : entry.note)
                    : "<html><font color='#b00020'> " + entry.name + " (" + entry.id + ") — " + entry.note
                            + "; this command will likely be refused</font></html>");
            copyButton.setEnabled(true);
        }

        private void copy() {
            String text = commandField.getText();
            if (text.isEmpty()) return;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            status.setText(" copied \"" + text + "\"");
        }

        private void startLoading(FlatCacheRepository repository) {
            SwingWorker<List<Entry>, Integer> worker = new SwingWorker<List<Entry>, Integer>() {
                @Override
                protected List<Entry> doInBackground() throws Exception {
                    return scan(repository, category, this::publish);
                }

                @Override
                protected void process(List<Integer> chunks) {
                    if (chunks.isEmpty()) return;
                    status.setText(" scanned " + String.format("%,d", chunks.get(chunks.size() - 1)) + "…");
                }

                @Override
                protected void done() {
                    try {
                        List<Entry> entries = get();
                        tableModel.setEntries(entries);
                        long spawnable = entries.stream().filter(e -> e.spawnable).count();
                        status.setText(String.format(" %,d entries (%,d spawnable)", entries.size(), spawnable));
                    } catch (Exception failure) {
                        status.setText(" failed to scan: " + failure.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private static final class EntryTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Name", "Note"};
        private List<Entry> entries = new ArrayList<>();

        void setEntries(List<Entry> entries) {
            this.entries = entries;
            fireTableDataChanged();
        }

        Entry entryAt(int row) {
            return entries.get(row);
        }

        @Override public int getRowCount() { return entries.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Entry entry = entries.get(rowIndex);
            switch (columnIndex) {
                case 0: return entry.id;
                case 1: return entry.name;
                default: return entry.note;
            }
        }
    }
}
