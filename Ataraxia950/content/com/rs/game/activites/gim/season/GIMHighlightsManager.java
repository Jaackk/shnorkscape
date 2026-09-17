package com.rs.game.activites.gim.season;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cores.CoresManager;
import com.rs.external.api.json.JsonParser;
import com.rs.game.activites.gim.bank.GIMBank;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manages all highlights for the current season.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMHighlightsManager {

    /**
     * The gson instance.
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The highlights file path.
     */
    private final Path filePath = Paths.get("data", "gim", "highlights.json");

    /**
     * The highlights.
     */
    private final List<String> highlights = new ArrayList<>();

    /**
     * Loads the highlights cache.
     */
    public void load() {
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        }
        String[] loadedHighlights = new JsonParser(filePath.toString(), String[].class).getFileLoaded();
        if (loadedHighlights != null) {
            synchronized (highlights) {
                highlights.addAll(Arrays.asList(loadedHighlights));
            }
        }
    }

    /**
     * Adds a new highlight to the cache.
     */
    public void add(String msg) {
        CoresManager.getServiceProvider().executeNow(() -> {
            synchronized (highlights) {
                for (String next : highlights) {
                    if (next.equals(msg)) {
                        return;
                    }
                }
                highlights.add(msg);
                save();
            }
        });
    }

    /**
     * Retrieves {@code amount} highlights and deletes them from the cache.
     */
    public List<String> take(int amount) {
        synchronized (highlights) {
            Collections.shuffle(highlights);
            List<String> takeList = new ArrayList<>(amount);
            Iterator<String> iter = highlights.iterator();
            while (iter.hasNext()) {
                String next = iter.next();
                iter.remove();
                if (amount <= 0) {
                    continue;
                }
                takeList.add(next);
                amount--;
            }
            save();
            return takeList;
        }
    }

    /**
     * Should only be called within a synchronization block where {@link #highlights} is the mutex.
     */
    private void save() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                bw.write(gson.toJson(highlights));
            }
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }
}
