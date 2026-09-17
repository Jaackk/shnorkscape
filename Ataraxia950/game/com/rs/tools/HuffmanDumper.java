package com.rs.tools;

import com.displee.cache.CacheLibrary;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HuffmanDumper {
    public static void main(String[] args) {
        try {
            System.out.println("Reading 910 cache...");
            CacheLibrary cache910 = CacheLibrary.create("data/cache_910/");

            int archiveId = cache910.index(10).archiveId("huffman");
            // Extract the raw file bytes
            byte[] huffmanBytes = cache910.index(10).archive(archiveId).file(0).getData();

            // Save it as a standalone file in your data folder
            Files.write(Paths.get("data/huffman.dat"), huffmanBytes);
            System.out.println("Successfully dumped to data/huffman.dat!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}