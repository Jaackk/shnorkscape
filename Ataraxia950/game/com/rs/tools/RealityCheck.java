package com.rs.tools;

import com.displee.cache.CacheLibrary;
import java.util.Arrays;

public class RealityCheck {
    public static void main(String[] args) {
        try {
            CacheLibrary library = CacheLibrary.create("data/cache");
            byte[] data = library.data(8, 9040, 0); // Checking one of the IDs we "packed"

            if (data == null) {
                System.out.println("ERROR: Archive 9040 is empty!");
                return;
            }

            // A PNG file always starts with these specific bytes: -119, 80, 78, 71
            if (data[0] == -119 && data[1] == 80 && data[2] == 78 && data[3] == 71) {
                System.out.println("VERIFIED: Archive 9040 contains a RAW PNG (Your custom image).");
            } else {
                System.out.println("STILL JAGEX: Archive 9040 contains Jagex header bytes (The injection failed).");
            }

            System.out.println("Data length: " + data.length + " bytes.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}