package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Utils;

public class GEPriceDumper {

    public static void main(String[] args) {
        try {
            new GEPriceDumper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GEPriceDumper() throws IOException {
        Cache.init();
        GrandExchange.init(); // Make sure PRICES map is loaded!

        File file = new File("ge_prices_dump.txt");
        if (file.exists())
            file.delete();
        else
            file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("// Grand Exchange Price Dump\n");
        writer.flush();
        for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            if (def == null || def.getName() == null || def.getName().equalsIgnoreCase("null"))
                continue;
            int price = GrandExchange.getPrice(id); // ? fixed to use id
            if (price <= 0)
                continue;
            writer.append(id + " - " + def.getName() + " - " + price + " gp");
            writer.newLine();
            writer.flush();
        }
        writer.close();
        System.out.println("Finished dumping GE prices.");
    }
}
