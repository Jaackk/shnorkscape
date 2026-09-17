//package com.rs.tools;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Writer;
//import java.lang.reflect.Type;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.text.DateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map.Entry;
//
//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//
//import com.google.gson.FieldNamingPolicy;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.rs.cache.Cache;
//import com.rs.cache.loaders.ClientScriptMap;
//import com.rs.cache.loaders.InventionDefinitions;
//import com.rs.utils.data.parsers.misc.PerkGenerationDataParser;
//import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData;
//import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkComponent;
//import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkComponentData;
//import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkData;
//import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkRank;
//
//import jdk.nashorn.api.scripting.ScriptObjectMirror;
//
//public class TestJS {
//
//    public static void main(String[] args) throws Exception {
//        Cache.init();
//        MaterialsData.init();
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("nashorn");
//        ScriptObjectMirror obj = (ScriptObjectMirror) engine.eval(new FileReader("C:\\Users\\armar\\Desktop\\data.js"));
//        Object[][] perksDefinition = InventionDefinitions.getAllPerks();
//        List<PerkComponent> comps = new ArrayList<PerkComponent>();
//        for (Entry<String, Object> e : ((ScriptObjectMirror) obj.getMember("comps")).entrySet()) {
//            if (!(e.getValue() instanceof ScriptObjectMirror))
//                continue;
//            String compName = e.getKey();
//            int id = getComponentId(compName);
//            PerkComponentData[][] perks = new PerkComponentData[3][];
//            for (Entry<String, Object> e2 : ((ScriptObjectMirror) e.getValue()).entrySet()) {
//                if (!(e2.getValue() instanceof ScriptObjectMirror))
//                    continue;
//                ScriptObjectMirror arr = (ScriptObjectMirror) e2.getValue();
//                int index = e2.getKey().equals("weapon") ? 0 : e2.getKey().equals("tool") ? 1 : 2;
//                perks[index] = new PerkComponentData[((Integer) arr.get("length"))];
//                for (int i = 0; i < ((Integer) arr.get("length")); i++) {
//                    ScriptObjectMirror object = (ScriptObjectMirror) arr.get("" + i);
//                    String name = (String) object.getMember("perk");
//                    int perkId = (int) getPerkData(perksDefinition, name)[0];
//                    int base = (int) object.getMember("base");
//                    int roll = (int) object.getMember("roll");
//                    perks[index][i] = new PerkComponentData(perkId, name, base, roll);
//                }
//            }
//            MaterialData mData = MaterialsData.getMaterialData(id);
//            if(mData == null)
//                System.out.println("id"+id);
//            comps.add(new PerkComponent(id, compName, mData == null ? 0 : mData.getXp(), perks[0], perks[2], perks[1]));
//        }
//        Collections.sort(comps, new Comparator<PerkComponent>() {
//            @Override
//            public int compare(PerkComponent o1, PerkComponent o2) {
//                if (o1.getCompId() < o2.getCompId()) {
//                    return -1;
//                } else if (o1.getCompId() > o2.getCompId()) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            }
//           
//        });
//        List<PerkData> perks = new ArrayList<PerkData>();
//        for (Entry<String, Object> e : ((ScriptObjectMirror) obj.getMember("perks")).entrySet()) {
//            String perkName = e.getKey();
//            ScriptObjectMirror arr = (ScriptObjectMirror) ((ScriptObjectMirror) e.getValue()).getMember("ranks");
//            PerkRank[] ranks = new PerkRank[((Integer) arr.get("length"))];
//            for (int i = 0; i < ((Integer) arr.get("length")); i++) {
//                ScriptObjectMirror object = (ScriptObjectMirror) arr.get("" + i);
//                int threshold = (int) object.getMember("threshold");
//                int cost = (int) object.getMember("cost");
//                ranks[i] = new PerkRank(threshold, cost);
//            }
//            boolean doubleslot = (((ScriptObjectMirror) e.getValue()).getMember("doubleslot") instanceof Boolean) ? (boolean) ((ScriptObjectMirror) e.getValue()).getMember("doubleslot") : false;
//            perks.add(new PerkData((int) getPerkData(perksDefinition, perkName)[0], perkName, doubleslot, ranks));
//        }
//        Collections.sort(perks, new Comparator<PerkData>() {
//            @Override
//            public int compare(PerkData o1, PerkData o2) {
//                if (o1.getPerkId() < o2.getPerkId()) {
//                    return -1;
//                } else if (o1.getPerkId() > o2.getPerkId()) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            }
//           
//        });
//        saveJsonFile(new PerkGenerationData(comps.toArray(new PerkComponent[comps.size()]), perks.toArray(new PerkData[perks.size()])), PerkGenerationDataParser.DEFINITIONS_FILE_PATH, PerkGenerationData.class);
//    }
//
//    public static final Gson GSON;
//
//    static {
//        GSON = new GsonBuilder().enableComplexMapKeySerialization().setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().setVersion(1.0).create();
//    }
//
//    public static void saveJsonFile(Object src, String dir, Type type) {
//        try (Writer writer = Files.newBufferedWriter(Paths.get(dir))) {
//            writer.write(GSON.toJson(src, type));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static Object[] getPerkData(Object[][] perksDefinition, String name) {
//        for (Object[] data : perksDefinition) {
//            if (((String) data[1]).equalsIgnoreCase(name))
//                return data;
//        }
//        if (name.equalsIgnoreCase("No effect"))
//            return new Object[] { 0, "No effect" };
//        return null;
//    }
//
//    public static int getComponentId(String name) {
//        ClientScriptMap map = ClientScriptMap.getMap(10742);
//        for (int i = 0; i < map.getSize(); i++) {
//            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
//            String partName = (String) def.getDataInIndex(1);
//            if (partName != null && partName.equalsIgnoreCase(name))
//                return i;
//        }
//        return -1;
//    }
//
//}
