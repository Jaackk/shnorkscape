package com.rs.tools;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.archive.Archive;
import java.io.File;
import java.io.FileOutputStream;

public class UIDumper {
    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            // We are targeting Interfaces (3) and Enums (17)
            int[] targetIndexes = {3, 17};

            for (int index : targetIndexes) {
                File outFolder = new File("dumped_index_" + index);
                if (!outFolder.exists()) {
                    outFolder.mkdir();
                }

                System.out.println("Dumping Index " + index + "...");
                Archive[] archives = library.index(index).archives();

                for (Archive archive : archives) {
                    if (archive != null) {
                        // Interfaces and Enums have multiple files per archive, so we loop through them
                        int[] fileIds = archive.fileIds();
                        for (int fileId : fileIds) {
                            byte[] data = library.data(index, archive.getId(), fileId);

                            if (data != null) {
                                // Saves as "ArchiveID_FileID.txt"
                                File outputFile = new File(outFolder, archive.getId() + "_" + fileId + ".txt");
                                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                    fos.write(data);
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Dump complete! Check the 'dumped_index_3' and 'dumped_index_17' folders.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}