package com.rs.tools;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.archive.Archive;
import java.io.File;
import java.io.FileOutputStream;

public class SpriteHunter {
    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            // Create a folder to hold the exported images
            File outFolder = new File("dumped_sprites");
            if (!outFolder.exists()) {
                outFolder.mkdir();
            }

            System.out.println("Dumping massive files to 'dumped_sprites' folder...");

            Archive[] archives = library.index(8).archives();

            for (Archive archive : archives) {
                if (archive != null) {
                    byte[] data = library.data(8, archive.getId(), 0);

                    if (data != null && data.length > 300000) {
                        // Write the raw bytes to a .png file
                        File outputFile = new File(outFolder, "archive_" + archive.getId() + ".png");
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(data);
                        }
                    }
                }
            }
            System.out.println("Dump complete! Check the 'dumped_sprites' folder in your project.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}