package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.utils.Utils;

public class ModelIdDumper {

    public static void main(String[] args) {
        try {
            new ModelIdDumper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ModelIdDumper() throws IOException {
        Cache.init();

        File file = new File("model_id_dump.txt");
        if (file.exists())
            file.delete();
        else
            file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("// Model ID Dump (ItemID - Item Name - Base Model - Male Equip Model - Female Equip Model)\n");
        writer.flush();

        for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            if (def == null || def.getName() == null || def.getName().equalsIgnoreCase("null"))
                continue;

            if (def.baseModel <= 0 && def.maleEquip1 <= 0 && def.femaleEquip1 <= 0)
                continue; // Skip items with no models

            writer.append(id + " - " + def.getName()
                    + " - BaseModel: " + def.baseModel
                    + " - MaleEquip1: " + def.maleEquip1
                    + " - FemaleEquip1: " + def.femaleEquip1);
            writer.newLine();
            writer.flush();
        }

        writer.close();
        System.out.println("Finished dumping Model IDs.");
    }
}
