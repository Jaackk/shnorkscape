package com.rs.external.api.json;

import com.google.gson.Gson;
import com.rs.utils.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class JsonParser {

    private final String filePath;
    private final Class clazz;

    public <T> JsonParser(String filePath, Class<T> classOfT) {
        this.filePath = filePath;
        clazz = classOfT;
    }
    public <T> JsonParser(Path path, Class<T> classOfT) {
        filePath = path.toString();
        clazz = classOfT;
    }
    public <T> T getFileLoaded() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Gson gson = new Gson();

            T object = gson.fromJson(br, (Class<T>) clazz);
            return object;
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }
}
