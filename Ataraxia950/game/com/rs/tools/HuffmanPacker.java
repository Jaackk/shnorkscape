package com.rs.tools;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.Archive;

public class HuffmanPacker {

    public static void main(String[] args) {
        // 1. Set your directory paths here
        String cache910Path = "data/cache_910/"; // Path to your old, working 910 cache
        String cache921Path = "data/cache/";     // Path to your broken 921 cache

        System.out.println("Loading caches...");
        CacheLibrary cache910 = CacheLibrary.create(cache910Path);
        CacheLibrary cache921 = CacheLibrary.create(cache921Path);

        // 2. Index 10 is where the binary configs (like Huffman) live
        Index index10From = cache910.index(10);
        Index index10To = cache921.index(10);

        // 3. Find the Huffman archive ID from the 910 cache using its string name
        int huffmanId = index10From.archiveId("huffman");

        if (huffmanId == -1) {
            System.err.println("Could not find 'huffman' in the 910 cache. Check your 910 path!");
            return;
        }

        System.out.println("Found 'huffman' archive at ID: " + huffmanId);

        // 4. Retrieve the actual archive data
        Archive huffmanArchive = index10From.archive(huffmanId);

        if (huffmanArchive == null) {
            System.err.println("The Huffman archive is null inside the 910 cache.");
            return;
        }

        System.out.println("Packing 'huffman' into 921 cache...");

        // 5. Add the archive to the 921 cache
        // Passing the Archive object preserves its ID, Name Hash, and inner file data.
        index10To.add(huffmanArchive);

        // 6. Save the changes to the .dat2 and .idx files
        index10To.update();

        System.out.println("Successfully packed! You can now start your server.");
    }
}