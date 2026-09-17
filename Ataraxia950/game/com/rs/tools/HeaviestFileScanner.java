package com.rs.tools;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.Archive;

public class HeaviestFileScanner {
    public static void main(String[] args) {
        try {
            CacheLibrary library = CacheLibrary.create("data/cache");

            // We'll scan the most likely indexes for large files
            int[] indexesToScan = {8, 40, 41, 47};

            for (int indexId : indexesToScan) {
                Index index = library.index(indexId);
                System.out.println("Scanning Index " + indexId + "...");

                int heaviestId = -1;
                long maxWeight = 0;

                for (Archive archive : index.archives()) {
                    if (archive == null) continue;
                    byte[] data = library.data(indexId, archive.getId(), 0);
                    if (data != null && data.length > maxWeight) {
                        maxWeight = data.length;
                        heaviestId = archive.getId();
                    }
                }
                System.out.println("Index " + indexId + " Heaviest Archive: " + heaviestId + " (" + (maxWeight / 1024) + " KB)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}