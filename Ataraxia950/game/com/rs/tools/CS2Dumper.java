package com.rs.tools;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.archive.Archive;
import java.io.File;
import java.io.FileOutputStream;

public class CS2Dumper {
    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            File outFolder = new File("dumped_scripts");
            if (!outFolder.exists()) {
                outFolder.mkdir();
            }

            System.out.println("Dumping ClientScripts from Index 12...");

            // Index 12 is the ClientScript (CS2) index
            Archive[] archives = library.index(12).archives();

            for (Archive archive : archives) {
                if (archive != null) {
                    byte[] data = library.data(12, archive.getId(), 0);

                    if (data != null) {
                        File outputFile = new File(outFolder, archive.getId() + ".txt");
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(data);
                        }
                    }
                }
            }
            System.out.println("Dump complete! Open 'dumped_scripts' and search for your dropdown names.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}