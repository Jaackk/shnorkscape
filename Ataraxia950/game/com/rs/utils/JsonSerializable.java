package com.rs.utils;

import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;

public final class JsonSerializable {

    public static <T> void load(Path path, Class<T> objectType, Consumer<T> onLoad) {
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                T loaded = Utils.GSON.fromJson(reader, objectType);
                if(loaded != null) {
                    onLoad.accept(loaded);
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public static void save(Path path, Object object) {
        try {
            Path parent = path.getParent();
            if(parent != null && !Files.exists(path.getParent())) {
                 Files.createDirectories(parent);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                Utils.GSON.toJson(object, writer);
            }
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }
}
