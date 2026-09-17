package com.rs.tools;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.external.api.json.JsonParser;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser.NPCStatsFileTemplate;
import com.rs.utils.data.parsers.npcs.pojos.NPCStats;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class RuneWikiNPCStatsDumper {
    private static final Int2ObjectOpenHashMap<NPCStatsFileTemplate> NPC_Stats = new Int2ObjectOpenHashMap<>();
    public static List<NPCStatsFileTemplate> npcstats;
    public static final String path = "data/npcs/npcstats.json";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            Cache.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser(path, NPCStatsFileTemplate[].class);
        NPCStatsFileTemplate[] definitions = parser.getFileLoaded();
        npcstats = new ArrayList<NPCStatsFileTemplate>(Arrays.asList(definitions));
        for (NPCStatsFileTemplate stats : npcstats)
            NPC_Stats.put(stats.getNpcId(), stats);
        for (int i = 0; i <= Utils.getNPCDefinitionsSize(); i++) {
            if (NPC_Stats.containsKey(i))
                continue;
            if (!dumpNPC(i))
                System.out.println("Failed dumping npc: " + i + ", " + NPCDefinitions.getNPCDefinitions(i).name);
            else
                System.out.println("Dumped npc: " + i + ", " + NPCDefinitions.getNPCDefinitions(i).name);
        }
    }

    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().enableComplexMapKeySerialization().setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().setVersion(1.0).create();
    }


    public static void saveJsonFile(Object src, String dir, Type type) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(dir))) {
            writer.write(GSON.toJson(src, type));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean dumpNPC(int npcId) {
        NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(npcId);
        if (!defs.hasAttackOption())
            return true;
        String pageName = defs.name;
        if (pageName == null || pageName.equals("null"))
            return true;
        pageName = pageName.replaceAll(" ", "_");
        try {
            WebPage page = new WebPage("https://runescape.wiki/w/" + pageName);
            try {
                page.load();
            } catch (SocketTimeoutException e) {
                return dumpNPC(npcId);
            } catch (Exception e) {
                System.out.println("Invalid page: " + npcId + ", " + pageName);
                return false;
            }
            int attackLevel = -1, rangeLevel = -1, magicLevel = -1, defenceLevel = -1, weaknessAffinity = -1, meleeAffinity = -1, rangeAffinity = -1, magicAffinity = -1;
            for (int linesCount = 0; linesCount < page.getLines().size(); linesCount++) {
                String line = page.getLines().get(linesCount);
                if (line.contains("<table class=\"plainlinks")) {
                    attackLevel = !line.contains("<span>NPC Attack level: ") ? 1 : Integer.valueOf(line.substring(line.indexOf("<span>NPC Attack level: ") + 24, line.indexOf("</span>", line.indexOf("<span>NPC Attack level: ") + 24)));
                    rangeLevel = !line.contains("<span>NPC Ranged level: ") ? 1 : Integer.valueOf(line.substring(line.indexOf("<span>NPC Ranged level: ") + 24, line.indexOf("</span>", line.indexOf("<span>NPC Ranged level: ") + 24)));
                    magicLevel = !line.contains("<span>NPC Magic level: ") ? 1 : Integer.valueOf(line.substring(line.indexOf("<span>NPC Magic level: ") + 23, line.indexOf("</span>", line.indexOf("<span>NPC Magic level: ") + 23)));
                    defenceLevel = !line.contains("<span>NPC Defence level: ") ? 1 : Integer.valueOf(line.substring(line.indexOf("<span>NPC Defence level: ") + 25, line.indexOf("</span>", line.indexOf("<span>NPC Defence level: ") + 25)));
                    weaknessAffinity = !line.contains("<span>NPC weakness affinity: ") ? 0 : Integer.valueOf(line.substring(line.indexOf("<span>NPC weakness affinity: ") + 29, line.indexOf("</span>", line.indexOf("<span>NPC weakness affinity: ") + 29)));
                    meleeAffinity = !line.contains("<span>NPC melee affinity: ") ? 0 : Integer.valueOf(line.substring(line.indexOf("<span>NPC melee affinity: ") + 26, line.indexOf("</span>", line.indexOf("<span>NPC melee affinity: ") + 26)));
                    rangeAffinity = !line.contains("<span>NPC ranged affinity: ") ? 0 : Integer.valueOf(line.substring(line.indexOf("<span>NPC ranged affinity: ") + 27, line.indexOf("</span>", line.indexOf("<span>NPC ranged affinity: ") + 27)));
                    magicAffinity = !line.contains("<span>NPC magic affinity: ") ? 0 : Integer.valueOf(line.substring(line.indexOf("<span>NPC magic affinity: ") + 26, line.indexOf("</span>", line.indexOf("<span>NPC magic affinity: ") + 26)));
                }
            }
            if (attackLevel == -1 || rangeLevel == -1 || magicLevel == -1 || defenceLevel == -1 || weaknessAffinity == -1 || meleeAffinity == -1 || rangeAffinity == -1 || magicAffinity == -1)
                return false;
            npcstats.add(new NPCStatsFileTemplate(npcId, new NPCStats(attackLevel, rangeLevel, magicLevel, defenceLevel, weaknessAffinity, meleeAffinity, rangeAffinity, magicAffinity)));
            saveJsonFile(npcstats, path, ArrayList.class);
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
