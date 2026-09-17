package com.rs.tools;

import com.displee.cache.CacheLibrary;
import java.nio.file.Files;
import java.io.File;

public class MassPacker {

    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            File newImage = new File("new_background.png");
            if (!newImage.exists()) {
                System.out.println("ERROR: 'new_background.png' not found in root folder!");
                return;
            }
            byte[] imageData = Files.readAllBytes(newImage.toPath());

            // Every ID found in your Enum scan
            int[] idsToOverwrite = {
                    11588, 9261, 9517, 1104, 1363, 1603, 1864, 2125, 2377, 2637,
                    2898, 3140, 3411, 3650, 3917, 4173, 4429, 4685, 4935, 5188,
                    5460, 5712, 5956, 6227, 6472, 6740, 6980, 7248, 7508, 7766,
                    8013, 8276, 8532, 8787, 9040
            };

            System.out.println("Beginning mass injection into Index 8...");

            for (int id : idsToOverwrite) {
                // We use put to overwrite the first file (0) in each archive
                library.put(8, id, 0, imageData);
                System.out.println("Packed ID: " + id);
            }

            System.out.println("Saving changes to cache...");
            boolean success = library.index(8).update();

            if (success) {
                System.out.println("SUCCESS: All background IDs have been overwritten.");
            } else {
                System.out.println("FAILED: Could not update the cache index.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}