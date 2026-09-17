package com.rs.utils;

import com.rs.external.api.json.JsonParser;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author lare96 <http://github.com/lare96>
 */
@Getter
@Setter
public final class PersistentObject<T> {

    private final Class<T> type;
    private final Path filePath;
    private T value;

    public PersistentObject(Class<T> type, Path filePath) {
        this.type = type;
        this.filePath = filePath;
    }

    public PersistentObject(Class<T> type, String first, String... more) {
        this(type, Paths.get(first, more));
    }

    public void load() {
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
            value = null;
            return;
        }
        value = new JsonParser(filePath.toString(), type).getFileLoaded();
    }

    public void save() {
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                bw.write(PersistentStringList.GSON.toJson(value));
            }
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }
}
