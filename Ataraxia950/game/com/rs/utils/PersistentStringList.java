package com.rs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cores.CoresManager;
import com.rs.external.api.json.JsonParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PersistentStringList {

     static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path filePath;
    private final List<String> list = new ArrayList<>();

    public PersistentStringList(Path filePath) {
        this.filePath = filePath;
    }

    public PersistentStringList(String first, String... more) {
        this(Paths.get(first, more));
    }

    public void clear() {
        CoresManager.getServiceProvider().executeNow(() -> {
            synchronized (list) {
                list.clear();
                save();
            }
        });
    }

    public void load() {
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        }
        String[] loaded = new JsonParser(filePath.toString(), String[].class).getFileLoaded();
        if (loaded != null) {
            synchronized (list) {
                list.addAll(Arrays.asList(loaded));
            }
        }
    }

    public void add(String msg) {
        CoresManager.getServiceProvider().executeNow(() -> {
            synchronized (list) {
                for (String next : list) {
                    if (next.equals(msg)) {
                        return;
                    }
                }
                list.add(msg);
                save();
            }
        });
    }

    public List<String> take(int amount) {
        synchronized (list) {
            int listSize = list.size();
            if (amount > listSize) {
                amount = listSize;
            }
            Collections.shuffle(list);
            List<String> takeList = new ArrayList<>(amount);
            Iterator<String> iter = list.iterator();
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

    public List<String> takeAll() {
        return take(Integer.MAX_VALUE);
    }

    /**
     * Should only be called within a synchronization block where {@link #list} is the mutex.
     */
    private void save() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                bw.write(GSON.toJson(list));
            }
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }
}
