package com.rs.cache.loaders;

import com.google.common.collect.Lists;
import com.rs.cache.Cache;
import com.rs.game.player.actions.invention.InventionConstants;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;
import com.rs.utils.data.parsers.misc.PerkGenerationDataParser;
import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkComponent;
import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkComponentData;
import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkData;
import iter.Itertools;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class InventionDefinitions {

    private static final ConcurrentHashMap<Integer, InventionDefinitions> InventionDefs = new ConcurrentHashMap<Integer, InventionDefinitions>();

    public static void main1(String[] args) throws IOException {
        Cache.init();
        PerkGenerationDataParser.init();
        ItemDisassembleDataParser.init();
        int inventionLevel = 120;
        int gizmoType = 0;// weapon/armour/tool
        int[] selectedMaterials = { 71, 71, 71, 71, -1 };// middle, top, left, right, bottom
//        Map<List<Perk>, Double> possiblePerks = InventionDefinitions.getMaterialsProb(inventionLevel, gizmoType, selectedMaterials);
//
//        Map<List<Perk>, Integer> appear = new LinkedHashMap<List<Perk>, Integer>();
//        int totalTrys = 10000000;
//        for (int i = 0; i < totalTrys; i++) {
//            List<Perk> s = selectRandomPerk(possiblePerks);
//            if (appear.containsKey(s)) {
//                appear.put(s, appear.get(s) + 1);
//            } else {
//                appear.put(s, 1);
//            }
//        }
//        System.out.println("possible perks and chances:");
//        for(Entry<List<Perk>, Double> e : possiblePerks.entrySet()) 
//            System.out.println(e);
//        System.out.println("");
//        System.out.println("appearance:");
//        for(Entry<List<Perk>, Integer> e : appear.entrySet()) {
//            System.out.println(e.getKey() + " appeared "+ e.getValue() + ", perc="+(((double)e.getValue() / (double)totalTrys) * 100.00) + "%");
//        }
//        ItemDisassembleData data = ItemDisassembleDataParser.getItemDisassembleData(null, 385);
//        Map<Component, Integer> appear = new LinkedHashMap<Component, Integer>();
//        int totalTrys = 1000;
//        for (int i = 0; i < totalTrys; i++) {
//            Component s = selectRandomComponent(data.getComponents());
//            if (appear.containsKey(s)) {
//                appear.put(s, appear.get(s) + 1);
//            } else {
//                appear.put(s, 1);
//            }
//        }
//      System.out.println("possible perks and chances:");
//      for(Component e : data.getComponents()) 
//          System.out.println(e);
//      System.out.println("");
//      System.out.println("appearance:");
//      for(Entry<Component, Integer> e : appear.entrySet()) {
//          System.out.println(e.getKey() + " appeared "+ e.getValue() + ", perc="+(((double)e.getValue() / (double)totalTrys) * 100.00) + "%");
//      }
//        System.out.println(appear);

    }
    
    

    public static Component selectRandomComponent(double[] bins, List<Component> comps) {
        double r = new Random().nextDouble() * bins[bins.length - 1];
        for (int i = 0; i < bins.length; i++) {
            if (r <= bins[i])
                return comps.get(i);
        }
        return null;
    }

    public static double[] generateCompBins(List<Component> comps) {
        double[] arr = new double[comps.size()];
        for (int i = 0; i < comps.size(); i++) {
            if (i == 0)
                arr[i] = comps.get(i).getChance();
            else
                arr[i] = arr[i - 1] + comps.get(i).getChance();
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    public static List<Perk> selectRandomPerk(Map<List<Perk>, Double> possiblePerks) {
        double[] bins = generatePerkBins(possiblePerks);
        if (bins.length == 0)
            return null;
        double r = new Random().nextDouble() * bins[bins.length - 1];
        for (int i = 0; i < bins.length; i++) {
            if (r <= bins[i])
                return (List<Perk>) possiblePerks.keySet().toArray()[i];
        }
        return null;
    }

    public static double[] generatePerkBins(Map<List<Perk>, Double> possiblePerks) {
        double[] arr = new double[possiblePerks.size()];
        for (int i = 0; i < possiblePerks.size(); i++) {
            if (i == 0)
                arr[i] = possiblePerks.values().stream().toArray(Double[]::new)[i];
            else
                arr[i] = arr[i - 1] + possiblePerks.values().stream().toArray(Double[]::new)[i];
        }
        return arr;
    }

    public static Map<List<Perk>, Double> getMaterialsProb(int inventionLevel, int gizmoType, int[] selectedMaterials) {
        Map<Integer, Integer> bases = new LinkedHashMap<Integer, Integer>();
        Map<Integer, List<Integer>> dices = new LinkedHashMap<Integer, List<Integer>>();
        List<Integer> order = new ArrayList<Integer>();
        for (int materialId : selectedMaterials) {
            if (materialId == -1)
                continue;
            PerkComponent comp = PerkGenerationDataParser.DATA.getComps()[materialId];
            if (comp.getPerks(gizmoType) != null && comp.getPerks(gizmoType).length > 0) {
                for (PerkComponentData data : comp.getPerks(gizmoType)) {
                    if (order.size() >= 20) {
                        continue;
                    }
                    if (!bases.containsKey(data.getPerkId())) {
                        order.add(data.getPerkId());
                        bases.put(data.getPerkId(), data.getBase());
                        List<Integer> dice = new ArrayList<Integer>();
                        dice.add(data.getRoll());
                        dices.put(data.getPerkId(), dice);
                    } else {
                        bases.put(data.getPerkId(), bases.get(data.getPerkId()) + data.getBase());
                        dices.get(data.getPerkId()).add(data.getRoll());
                    }
                }
            }
        }
        if (order.isEmpty()) {
            return new LinkedHashMap<List<Perk>, Double>();
        }
        List<List<Map<String, Object>>> probabilities = new ArrayList<List<Map<String, Object>>>();
        Iterator<Integer> iter = order.iterator();
        while (iter.hasNext()) {
            int perkId = iter.next();
            List<Double> distribution = rollDice(dices.get(perkId), bases.get(perkId));
            List<Integer> ranks = new ArrayList<Integer>();
            ranks.add(0);
            PerkData data = PerkGenerationDataParser.DATA.getPerks()[perkId];
            for (int i = 0; i < data.getRanks().length; i++)
                ranks.add(data.getRanks()[i].getThreshold());
            ranks.add(9999);
            int rank = 0;
            List<Map<String, Object>> probs = new ArrayList<Map<String, Object>>();
            @SuppressWarnings("unchecked")
            List<List<Integer>> zipRanks = Lists.newArrayList(Itertools.izip(ranks.subList(0, ranks.size() - 1), ranks.subList(1, ranks.size())));
            for (int i3 = 0; i3 < zipRanks.size(); i3++) {
                Integer[] arr = zipRanks.get(i3).stream().toArray(Integer[]::new);
                int low = arr[0];
                int high = arr[1];
                List<Double> probability = Lists.newArrayList(Itertools.islice(distribution, low, high, 1));
                Double prop = probability.stream().reduce(0.0, (a, b) -> a + b);

                if (prop > 0) {
                    if (rank > 0) {
                        LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
                        m.put("rank", rank);
                        m.put("probability", prop);
                        m.put("cost", data.getRanks()[rank - 1].getCost());
                        m.put("perk", perkId);
                        probs.add(m);
                    } else {
                        LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
                        m.put("rank", rank);
                        m.put("probability", prop);
                        m.put("cost", 0);
                        m.put("perk", perkId);
                        probs.add(m);
                    }
                }
                rank += 1;
            }
            probabilities.add(probs);
        }
        List<Integer> toRoll = new ArrayList<Integer>();
        for (int i6 = 0; i6 < 5; i6++) { // 5 rolls
            toRoll.add(20 + ((inventionLevel / 2)));
        }
        List<Double> contribution = rollDice(toRoll, 0);
        contribution.set(inventionLevel, contribution.get(inventionLevel) + contribution.subList(0, inventionLevel).stream().reduce(0.0, (a, b) -> a + b));
        for (int i7 = 0; i7 < inventionLevel; i7++)
            contribution.set(i7, 0.0);
        List<List<Map<String, Object>>> combos = cartesianProduct(probabilities);
        Map<List<Integer>, Map<List<Integer>, Double>> cache = new LinkedHashMap<List<Integer>, Map<List<Integer>, Double>>();
        Map<List<Perk>, Double> output = new LinkedHashMap<List<Perk>, Double>();
        for (int i8 = 0; i8 < combos.size(); i8++) {
            List<Map<String, Object>> combo = combos.get(i8);
            List<Double> comboProbs = new ArrayList<Double>();
            for (int i9 = 0; i9 < combo.size(); i9++)
                comboProbs.add((Double) combo.get(i9).get("probability"));
            double combo_probability = _product(comboProbs);
            quickSort(0, combo.size() - 1, combo, (a, b) -> a - b);
            List<Integer> cache_key = new ArrayList<Integer>();
            for (int iterthing = 0; iterthing < combo.size(); iterthing++)
                cache_key.add((Integer) combo.get(iterthing).get("cost"));
            Map<List<Integer>, Double> inner_probs = cache.get(cache_key);
            if (inner_probs == null) {
                inner_probs = new LinkedHashMap<List<Integer>, Double>();
                int lowest_contribution = (int) (Math.floor(inventionLevel / 5) * 5);
                int highest_contribution = contribution.size() - 1;
                for (int candidate_contribution_iter = lowest_contribution; candidate_contribution_iter <= highest_contribution; candidate_contribution_iter += 5) {
                    int candidate_contribution = candidate_contribution_iter;
                    Double contribution_probability = Lists.newArrayList(Itertools.islice(contribution, candidate_contribution, (candidate_contribution + 5), 1)).stream().reduce(0.0, (a, b) -> a + b);
                    List<Integer> perk_indexes = new ArrayList<Integer>();
                    for (int i11 = combo.size() - 1; i11 >= 0; i11--) {
                        Map<String, Object> perk = combo.get(i11);
                        if ((int) perk.get("cost") == 0) {
                            continue;
                        }
                        if (candidate_contribution > (int) perk.get("cost")) {
                            perk_indexes.add(i11);
                            candidate_contribution -= (int) perk.get("cost");
                        }
                        if (perk_indexes.size() == 2) {
                            break;
                        }
                    }
                    if (inner_probs.get(perk_indexes) == null) {
                        inner_probs.put(perk_indexes, 0.0);
                    }

                    inner_probs.put(perk_indexes, inner_probs.get(perk_indexes) + contribution_probability);

                }
                cache.put(cache_key, inner_probs);
            }
            for (Entry<List<Integer>, Double> e : inner_probs.entrySet()) {
                List<Integer> perk_indexes = e.getKey();
                Double p = e.getValue();
                List<Perk> perksUsed = new ArrayList<Perk>();
                boolean hasdouble = false;
                for (Integer perkIndex : perk_indexes) {
                    Map<String, Object> perk = combo.get(perkIndex);
                    int perkId = (int) perk.get("perk");
                    int rank = (int) perk.get("rank");
                    PerkData data = PerkGenerationDataParser.DATA.getPerks()[perkId];
                    hasdouble = hasdouble || data.isDoubleslot();
                    perksUsed.add(new Perk(perkId, data.getRanks().length > 1 ? rank : 1));
                }
                if (hasdouble)
                    perksUsed = Lists.newArrayList(Itertools.islice(perksUsed, 0, 1, 1));
                if (perksUsed.size() > 1 && perksUsed.get(0).equals(perksUsed.get(1)))
                    continue;
                if (output.get(perksUsed) == null) {
                    output.put(perksUsed, 0.0);
                }
                output.put(perksUsed, output.get(perksUsed) + (combo_probability * p));
            }
        }

        return output;
    }

    protected static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            List<T> firstList = lists.get(0);
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    public static Double _product(List<Double> arr) {
        double p = 1.0;
        for (int i = 0; i < arr.size(); i++) {
            p *= arr.get(i);
        }
        return p;
    }

    public static List<Double> rollDice(List<Integer> dices, Integer base) {
        List<Double> probabilities = new ArrayList<Double>();
        probabilities.add(1.0);
        Iterator<Integer> iter = dices.iterator();
        while (iter.hasNext()) {
            int dice = iter.next();
            int newSize = probabilities.size() + dice - 1;
            List<Double> newArr = new ArrayList<Double>();
            for (int i2 = 0; i2 < newSize; i2++) {
                newArr.add(0.0);
            }
            double total = 0;
            for (int i3 = 0; i3 < newSize; i3++) {
                if (i3 < probabilities.size()) {
                    total += probabilities.get(i3);
                }
                if ((i3 - dice) >= 0) {
                    total -= probabilities.get(i3 - dice);
                }
                newArr.set(i3, (total * 1.0) / (double) dice);
            }
            probabilities = newArr;
        }
        List<Double> baseProbs = new ArrayList<Double>();
        for (int i4 = 0; i4 < base; i4++) {
            baseProbs.add(0.0);
        }
        baseProbs.addAll(probabilities);
        return baseProbs;
    }

    public static void quickSort(int low, int high, List<Map<String, Object>> arr, Comparator<Integer> compare) {
        int pivot_index = ((low + high) / 2);
        Map<String, Object> pivot_value = arr.get(pivot_index);
        arr.set(pivot_index, arr.get(high));
        arr.set(high, pivot_value);
        int counter = low;
        int loop_index = low;
        while (loop_index < high) {
            int cost1 = (int) arr.get(loop_index).get("cost");
            int cost2 = (int) pivot_value.get("cost");
            if (compare.compare(cost1, cost2) < (loop_index & 1)) {
                Map<String, Object> tmp = arr.get(loop_index);
                arr.set(loop_index, arr.get(counter));
                arr.set(counter, tmp);
                counter = counter + 1;
            }
            loop_index = loop_index + 1;
        }
        arr.set(high, arr.get(counter));
        arr.set(counter, pivot_value);
        if (low < (counter - 1)) {
            quickSort(low, counter - 1, arr, compare);
        }
        if ((counter + 1) < high) {
            quickSort(counter + 1, high, arr, compare);
        }
    }

    public static void main(String[] args) throws IOException {
        Cache.init();
        InventionDefinitions def = InventionDefinitions.getData(532);
        Object[] data = new Object[def.data.length];
        for (int j = 0; j < def.data.length; j++) { // if(def.data[j] != null &&
            // def.data[j].length > 1) // System.out.println("lollll");
            data[j] = Arrays.toString(def.data[j]);
        } // augmented item id key 5551
          // original item id key 5525
        System.out.println(ItemDefinitions.getItemDefinitions(36279).clientScriptData);
        Map<Integer, String> hashmap = new HashMap<Integer, String>();
        for (int i = 0; i < 2000; i++) {
            if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
                continue;
            try {
                def = InventionDefinitions.getData(i);
                data = new Object[def.data.length];
                for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                            // null &&
                    // def.data[j].length > 1) // System.out.println("lollll");
                    data[j] = Arrays.toString(def.data[j]);
                }
                if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
                    continue;
                if (def.getDataInIndex(0) == null || !(def.getDataInIndex(0) instanceof Integer))
                    continue;
                hashmap.put(i, Arrays.toString(data));
            } catch (Exception e) {

            }
        }
        Map<Integer, String> treeMap = new TreeMap<Integer, String>(hashmap);
        int[] dbrowIds = new int[treeMap.size()];
        String[] desc = new String[treeMap.size()];
        for (Integer id : treeMap.keySet()) {
            if (id == null)
                continue;
            String value = treeMap.get(id);
            int perkId = Integer.parseInt(value.substring(value.indexOf("[[")+2, value.indexOf("],")));
            dbrowIds[perkId-1] = id;
            desc[perkId-1] = "\""+InventionConstants.Perks.getDesc(perkId)+"\"";
            System.out.println("id=" + id + ", PerkId="+perkId+  ", data=" + treeMap.get(id).replace(", null", ""));
        }
        System.out.println("size=" + treeMap.size());
        System.out.println(Arrays.toString(dbrowIds).replace("[", "{").replace("]", "};"));
        System.out.println(Arrays.toString(desc).replace("[", "{").replace("]", "};"));
        System.out.println("===========materials================");
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        for (int i = 0; i < map.getSize(); i++) {
            def = InventionDefinitions.getData(map.getIntValue(i));
            data = new Object[def.data.length];
            for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                        // null &&
                // def.data[j].length > 1) // System.out.println("lollll");
                data[j] = Arrays.toString(def.data[j]);
            }
            System.out.println("materialId=" + i + ", " + Arrays.toString(data));
        }
        System.out.println("===========blue prints================");
        map = ClientScriptMap.getMap(10743);
        for (int i = 0; i < map.getSize(); i++) {
            def = InventionDefinitions.getData(map.getIntValue(i));
            data = new Object[def.data.length];
            for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                        // null &&
                // def.data[j].length > 1) // System.out.println("lollll");
                data[j] = Arrays.toString(def.data[j]);
            }
            System.out.println("i=" + i + " , " + Arrays.toString(data));
        }
        /*
         * System.out.println("===========materials================"); ClientScriptMap
         * map = ClientScriptMap.getMap(10742); for (int i = 0; i < map.getSize(); i++)
         * { InventionDefinitions def =
         * InventionDefinitions.getData(map.getIntValue(i)); Object[] data = new
         * Object[def.data.length]; for (int j = 0; j < def.data.length; j++) { //
         * if(def.data[j] != null && def.data[j].length > 1) //
         * System.out.println("lollll"); data[j] = Arrays.toString(def.data[j]); }
         * System.out.println("i=" + i + ",fieldId=" + map.getIntValue(i) + ", name=" +
         * def.getDataInIndex(1) + ", id=" + def.getDataInIndex(0) + ", data=" +
         * Arrays.toString(data)); }
         * System.out.println("===========blue prints================"); map =
         * ClientScriptMap.getMap(10743); Map<Integer, String> hashmap = new
         * HashMap<Integer, String>(); for (int i = 0; i < 5000; i++) { if
         * (!Cache.STORE.getIndexes()[2].fileExists(41, i)) { continue; } try {
         * InventionDefinitions def = InventionDefinitions.getData(i); Object[] data =
         * new Object[def.data.length]; for (int j = 0; j < def.data.length; j++)
         * data[j] = Arrays.toString(def.data[j]);
         * 
         * if (def.getDataInIndex(0) != null && def.getDataInIndex(2) instanceof Integer
         * && def.getDataInIndex(4) instanceof String) hashmap.put((Integer)
         * def.getDataInIndex(0), "i=" +i+" , "+ Arrays.toString(data)); } catch
         * (Exception e) { continue; } } Map<Integer, String> treeMap = new
         * TreeMap<Integer, String>(hashmap); for (Integer id : treeMap.keySet()) { if
         * (id == null) continue; System.out.println("id=" + id + ", data=" +
         * treeMap.get(id).replace(", null", "")); } System.out.println("size=" +
         * treeMap.size());
         */
        int prayerRestored = 0;
        int prayerLevel = 50;
        double mod = (545 / (prayerLevel * 4.3 + 120));
        for (int i = 0; i < 546; i++) {
            if ((int) (i % mod) == 0)
                prayerRestored++;
//            System.out.println(i % mod);
        }
//        System.out.println(prayerRestored);
    }

    private Object[][] data;

    public static final InventionDefinitions getData(int dataId) {
        InventionDefinitions def = InventionDefs.get(dataId);
        if (def != null && def.data != null)
            return def;
        def = new InventionDefinitions();
        byte[] data = Cache.STORE.getIndexes()[2].getFile(41, dataId);
        if (data != null)
            def.readValueLoop(new InputStream(data));
        return def;
    }

    private void readValueLoop(InputStream stream) {
        for (;;) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private void readValues(InputStream stream, int opcode) {
        if (opcode == 3) {
            int i_1_ = stream.readUnsignedByte();
            if (null == data) {
                data = new Object[i_1_][];
            }
            boolean[] bools = new boolean[i_1_];
            int[] numbers = new int[i_1_];
            for (int i_2_ = stream.readUnsignedByte(); 255 != i_2_; i_2_ = stream.getRemaining() > 0 ? stream.readUnsignedByte() : 255) {
                int i_3_ = stream.readUnsignedByte();

                for (int i_4_ = 0; i_4_ < i_3_; i_4_++) {
                    int smart = stream.readUnsignedSmart();
                    bools[i_2_] = smart != 36;
                    numbers[i_2_] = smart;
                }
                data[i_2_] = method8709(stream, i_3_, bools[i_2_], i_2_);
            }
        }
    }

    public static Object[] method8709(InputStream stream, int length, boolean bool, int lol) {
        int i_96_ = stream.readUnsignedSmart();
        Object[] objects = new Object[i_96_ * length];
        // System.out.println(i_96_ * length);
        // if (lol == 8)
        // return method59(stream);
        for (int i_97_ = 0; i_97_ < i_96_; i_97_++) {
            for (int i_98_ = 0; i_98_ < length; i_98_++) {
                int i_99_ = i_98_ + i_97_ * length;

                objects[i_99_] = bool ? Integer.valueOf(stream.readInt()) : stream.readString();
            }
        }
        return objects;
    }

    public static Object[] method13775(InputStream stream) {
        int i_3_ = stream.readUnsignedByte();
        if (i_3_ == 0)
            return null;
        i_3_--;
        stream.readByte();
        int i_4_ = stream.readInt();
        Object[] objects = new Object[i_3_];
        for (int i_5_ = 0; i_5_ < i_3_; i_5_++) {
            int i_6_ = stream.readUnsignedByte();
            if (i_6_ == 0)
                objects[i_5_] = stream.readInt();
            else if (i_6_ == 1)
                objects[i_5_] = stream.readString();
            else
                throw new IllegalStateException(new StringBuilder().append("Unrecognised type ID in deserialise: ").append(i_6_).toString());
        }
        return objects;
    }

    public static Object[] method59(InputStream stream) {
        Object[] objects = new Object[4];
        objects[0] = stream.readUnsignedByte();
        objects[1] = stream.readInt();
        objects[2] = stream.readInt();
        objects[3] = stream.readInt();
        return objects;
    }

    public Object[][] getData() {
        return data;
    }

    public Object getDataInIndex(int index) {
        if (data == null || data.length == 0 || data[index] == null || data[index].length == 0)
            return null;
        return data[index][0];
    }

    public static String getDataName(int dataId) {
        InventionDefinitions def = getData(dataId);
        return (String) def.getDataInIndex(1);
    }

    public static int getMaterialIndex(int materialId) {
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
            if (((int) def.getDataInIndex(0)) == materialId)
                return i;
        }
        return -1;
    }

    public static String getMaterialName(int materialIndex) {
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(materialIndex));
        return (String) def.getDataInIndex(1);
    }

    public static int getBluePrintIndex(int bluePrintId) {
        ClientScriptMap map = ClientScriptMap.getMap(10743);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
            if (((int) def.getDataInIndex(0)) == bluePrintId)
                return i;
        }
        return -1;
    }

    public static int getConfigIndex(int value) {
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
            int v = (int) def.getDataInIndex(0);
            if (v == value)
                return i;
        }
        return -1;
    }

    public static Object getDBField(int fieldId, int row, int index) {
        InventionDefinitions def = InventionDefinitions.getData(fieldId);
        if (def.data == null)
            return null;
        int dataIndex = row & 0xff;
        if (dataIndex >= def.data.length || def.data[dataIndex] == null)
            return null;
        return def.data[dataIndex][index];
    }

    public static int getPerkIdByName(String name) {
        for (int i = 0; i < 2000; i++) {
            if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
                continue;
            try {
                InventionDefinitions def = InventionDefinitions.getData(i);
                Object[] data = new Object[def.data.length];
                for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                            // null &&
                    // def.data[j].length > 1) // System.out.println("lollll");
                    data[j] = Arrays.toString(def.data[j]);
                }
                if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
                    continue;
                if (((String) def.getDataInIndex(1)).equalsIgnoreCase(name))
                    return (int) def.getDataInIndex(0);
            } catch (Exception e) {

            }
        }
        return -1;
    }

    public static Object[][] getAllPerks() {
        List<Object[]> perks = new ArrayList<Object[]>();
        for (int i = 0; i < 2000; i++) {
            if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
                continue;
            try {
                InventionDefinitions def = InventionDefinitions.getData(i);
                Object[] data = new Object[def.data.length];
                for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                            // null &&
                    // def.data[j].length > 1) // System.out.println("lollll");
                    data[j] = Arrays.toString(def.data[j]);
                }
                if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
                    continue;
                String perkName = (String) def.getDataInIndex(1);
                int id = (int) def.getDataInIndex(0);
                perks.add(new Object[] { id, perkName });
            } catch (Exception e) {

            }
        }
        Collections.sort(perks, new Comparator<Object[]>() {

            @Override
            public int compare(Object[] oa1, Object[] oa2) {
                int o1 = (int) oa1[0];
                int o2 = (int) oa2[0];
                if (o1 < o2) {
                    return -1;
                } else if (o1 > o2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return perks.toArray(new Object[perks.size()][]);
    }

    public static int getRandomMaterial(int rarity) {
        List<Integer> materials = new ArrayList<Integer>();
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions defs = InventionDefinitions.getData(map.getIntValue(i));
            if (defs == null || defs.data == null)
                continue;
            if (((int) defs.getDataInIndex(7)) == rarity)
                materials.add(i);
        }
        if (materials.isEmpty())
            return -1;
        return materials.get(Utils.random(materials.size()));
    }

    public static String getPerkNameById(int perkId) {
        for (int i = 0; i < 2000; i++) {
            if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
                continue;
            try {
                InventionDefinitions def = InventionDefinitions.getData(i);
                Object[] data = new Object[def.data.length];
                for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
                                                            // null &&
                    // def.data[j].length > 1) // System.out.println("lollll");
                    data[j] = Arrays.toString(def.data[j]);
                }
                if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
                    continue;
                if (((int) def.getDataInIndex(0)) == perkId)
                    return ((String) def.getDataInIndex(1));
            } catch (Exception e) {

            }
        }
        return "null";
    }

    public static int getBluePrintMapIndex(int blueprintId) {
        ClientScriptMap map = ClientScriptMap.getMap(10743);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
            if ((int) def.getDataInIndex(0) == blueprintId)
                return i;
        }
        return -1;
    }
}
