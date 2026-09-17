package com.rs.tools;

import com.displee.cache.CacheLibrary;
import java.nio.file.Files;
import java.io.File;

public class LoginScreenPacker {

    public static void main(String[] args) {
        try {
            // 1. Point to your server's cache folder
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            // 2. Load your custom image from your project folder
            // Make sure your custom image is named exactly this and sits in your root project folder
            File newImage = new File("new_background.png");

            if (!newImage.exists()) {
                System.out.println("ERROR: Could not find 'new_background.png' in the project folder!");
                return;
            }

            byte[] imageData = Files.readAllBytes(newImage.toPath());

            // 3. Inject the image into the cache
            int indexId = 8;
            int backgroundArchiveId = 9040; // The ID you found from the Enum

            System.out.println("Overwriting Archive " + backgroundArchiveId + " with custom image...");
            library.put(indexId, backgroundArchiveId, 0, imageData);

            // 4. Save and Update the cache
            System.out.println("Saving cache...");
            boolean success = library.index(indexId).update();

            if (success) {
                System.out.println("Success! The default background has been replaced.");
            } else {
                System.out.println("Failed to save the cache.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}