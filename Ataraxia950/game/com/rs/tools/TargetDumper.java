package com.rs.tools;

import com.displee.cache.CacheLibrary;
import java.io.File;
import java.io.FileOutputStream;

public class TargetDumper {
    public static void main(String[] args) {
        try {
            CacheLibrary library = CacheLibrary.create("data/cache");

            // Dumping the heavy hitter from Index 8
            byte[] data = library.data(8, 20453, 0);

            if (data != null) {
                File outFile = new File("check_this_file");
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(data);
                }
                System.out.println("Dump complete. Check your main project folder for 'check_this_file'.");
            } else {
                System.out.println("Error: Could not find data for Archive 20453.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}