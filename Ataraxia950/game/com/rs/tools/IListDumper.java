package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.utils.Utils;

public class IListDumper {

    public static void main(String[] args) {
        try {
            new IListDumper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IListDumper() throws IOException {
        Cache.init();
        File file = new File("itemsList.txt"); // = new
        if (file.exists())
            file.delete();
        else
            file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("//Version = 910\n");
        writer.flush();
        for (int id = 0; id < Utils.getItemDefinitionsSize()+1; id++) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            /*
             * if (def.getName().equals("null")) continue;
             */
            writer.append(id + " - " + def.getName() + " ");
            writer.newLine();
            writer.flush();
        }
        writer.close();
    }

    public static int convertInt(String str) {
        try {
            int i = Integer.parseInt(str);
            return i;
        } catch (NumberFormatException e) {
        }
        return 0;
    }

}
