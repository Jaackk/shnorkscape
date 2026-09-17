package com.rs.tools;


import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class RemoteFileMaker {
    public static void main(String[] args) throws IOException {
        Cache.init();
        File file = new File("C:/Users/Kris/Desktop/remoteList.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < 3500; i++) {
            RenderAnimDefinitions def = RenderAnimDefinitions.getRenderAnimDefinitions(i);


            if (!AnimationDefinitions.findDesciption(def.standAnimation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.walkAnimation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.runAnimation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.runRotate90Animation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.runRotate90CounterAnimation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.rotate180Animation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.rotate90Animation).startsWith("Not a player")
                    || !AnimationDefinitions.findDesciption(def.rotate90CounterAnimation).startsWith("Not a player")) {
                writer.write("Remote ID: " + i);
                writer.newLine();
                if (!AnimationDefinitions.findDesciption(def.standAnimation).startsWith("Not a player")) {
                    writer.write("\tIdle Animation: " + AnimationDefinitions.findDesciption(def.standAnimation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.walkAnimation).startsWith("Not a player")) {
                    writer.write("\tWalk Animation: " + AnimationDefinitions.findDesciption(def.walkAnimation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.runAnimation).startsWith("Not a player")) {
                    writer.write("\tRun Animation: " + AnimationDefinitions.findDesciption(def.runAnimation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.runRotate90Animation).startsWith("Not a player")) {
                    writer.write("\tTurning Animation A: " + AnimationDefinitions.findDesciption(def.runRotate90Animation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.runRotate90CounterAnimation).startsWith("Not a player")) {
                    writer.write("\tTurning Animation B: " + AnimationDefinitions.findDesciption(def.runRotate90CounterAnimation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.rotate180Animation).startsWith("Not a player")) {
                    writer.write("\tBackwards Walk Animation: " + AnimationDefinitions.findDesciption(def.rotate180Animation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.rotate90Animation).startsWith("Not a player")) {
                    writer.write("\tSidestep Animation A: " + AnimationDefinitions.findDesciption(def.rotate90Animation));
                    writer.newLine();
                }
                if (!AnimationDefinitions.findDesciption(def.runRotate90CounterAnimation).startsWith("Not a player")) {
                    writer.write("\tSidestep Animation B: " + AnimationDefinitions.findDesciption(def.runRotate90CounterAnimation));
                    writer.newLine();
                }
                writer.newLine();
            }
        }
        writer.close();
    }
}