package com.rs.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class StarterMap {

    private static final StarterMap INSTANCE = new StarterMap();
    private final String path = "data/starters.ini";
    public List<String> starters = new ArrayList<String>();
    private final File map = new File(path);

    public static StarterMap getSingleton() {
        return INSTANCE;
    }

    public void init() {
        try {
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new FileReader(map));
            String s;
            while ((s = reader.readLine()) != null) {
                starters.add(s);
            }
            Logger.getGlobal().info("Initiated " + starters.size() + " starter IP's...");
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    private void save() {
        BufferedWriter bf;
        try {
            clearMapFile();
            bf = new BufferedWriter(new FileWriter(path, true));
            for (String ip : starters) {
                bf.write(ip);
                bf.newLine();
            }
            bf.flush();
            bf.close();
        } catch (IOException e) {
            Logger.getGlobal().error("Error saving starter map!", e);
        }
    }

    private void clearMapFile() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(map);
            writer.print("");
            writer.close();
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public void addIP(String ip) {
        if (getCount(ip) >= 3)
            return;
        starters.add(ip);
        save();
    }

    public int getCount(String ip) {
        int count = 0;
        for (String i : starters) {
            if (i.equals(ip))
                count++;
        }
        return count;
    }
}