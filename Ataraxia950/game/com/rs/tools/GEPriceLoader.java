package com.rs.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.game.player.content.grandExchange.GrandExchange;

public class GEPriceLoader {

    public static void main(String[] args) {
        try {
            new GEPriceLoader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GEPriceLoader() throws IOException {
        Cache.init();
        GrandExchange.init(); // Load current GE prices

        File file = new File("ge_prices_dump.txt");
        if (!file.exists()) {
            System.out.println("ge_prices_dump.txt not found!");
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int updated = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("//") || line.trim().isEmpty())
                continue; // skip header or empty lines
            try {
                String[] parts = line.split(" - ");
                if (parts.length < 3)
                    continue; // invalid line

                int itemId = Integer.parseInt(parts[0].trim());
                int price = Integer.parseInt(parts[parts.length - 1].replace("gp", "").trim());

                // (we can totally ignore the item name now because we're trusting ID)
                GrandExchange.setPrice(itemId, price);
                updated++;
            } catch (Exception e) {
                System.out.println("Failed to parse line: " + line);
                e.printStackTrace();
            }

        }
        reader.close();

        GrandExchange.savePrices(); // Save back to geprices.ser
        System.out.println("Finished loading GE prices. Updated " + updated + " items.");
    }
}
