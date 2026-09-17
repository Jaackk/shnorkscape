package com.rs.tools;

import java.nio.file.Files;
import java.nio.file.Paths;

public class EnumScanner {
    public static void main(String[] args) {
        try {
            // Point this directly to the file you found
            byte[] data = Files.readAllBytes(Paths.get("dumped_index_17/49_46.txt"));

            System.out.println("Scanning Enum for hidden Sprite IDs...");

            // Read through the file byte by byte
            for (int i = 0; i < data.length - 4; i++) {
                // Combine 4 bytes into a single integer
                int val = ((data[i] & 0xFF) << 24) |
                        ((data[i+1] & 0xFF) << 16) |
                        ((data[i+2] & 0xFF) << 8) |
                        (data[i+3] & 0xFF);

                // We know from our previous dump that backgrounds are large numbers
                if (val > 1000 && val < 20000) {

                    // Grab a window of text surrounding this number
                    int start = Math.max(0, i - 35);
                    int end = Math.min(data.length, i + 35);

                    StringBuilder context = new StringBuilder();
                    for(int j = start; j < end; j++) {
                        char c = (char) data[j];
                        // Only print readable letters/numbers, replace weird binary bytes with a dot
                        if(c >= 32 && c <= 126) {
                            context.append(c);
                        } else {
                            context.append('.');
                        }
                    }

                    // Filter out the noise: only print if the surrounding text actually contains words
                    if (context.toString().matches(".*[a-zA-Z]{4,}.*")) {
                        System.out.println("Found Sprite ID: [" + val + "]");
                        System.out.println("Surrounding Text: " + context.toString());
                        System.out.println("-----------------------------------------");
                    }
                }
            }
            System.out.println("Scan complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}