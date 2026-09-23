package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.util.*;

/** Immutable cache-derived catalogue. Never a Bank, Item[] save, or player-owned container. */
final class Native950EquipmentCatalogue {
    static final String[] CATEGORIES={"Best","Melee","Ranged","Magic","Necromancy","Hybrid / Defence","Supplies","Utility"};
    static final int CAPACITY=700;
    private static Object store;
    private static Native950EquipmentCatalogue cached;
    final List<Entry> entries;
    final int[] counts;
    static final class Entry {
        final int id,category,tier,slot,quantity,stackMode;
        final String name,group;
        Entry(int id,int category,int tier,int slot,int quantity,int stackMode,String name,String group){
            this.id=id;this.category=category;this.tier=tier;this.slot=slot;this.quantity=quantity;
            this.stackMode=stackMode;this.name=name;this.group=group;
        }
    }
    Native950EquipmentCatalogue(List<Entry> entries){
        if(entries.isEmpty()||entries.size()>CAPACITY)throw new IllegalArgumentException("Invalid catalogue capacity");
        this.entries=Collections.unmodifiableList(new ArrayList<>(entries));counts=new int[CATEGORIES.length];
        int previous=-1;
        Set<String> seen=new HashSet<>();
        for(Entry e:entries){
            if(e.id<0||e.id>Native950ItemCatalog.MAX_ITEM_ID||e.category<0||e.category<previous||e.category>=counts.length
                    ||e.quantity<1||e.name==null||e.name.isEmpty()||!seen.add(e.category+":"+e.id))
                throw new IllegalArgumentException("Invalid catalogue entry");
            previous=e.category;counts[e.category]++;
        }
    }
    static synchronized Native950EquipmentCatalogue current(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Catalogue requires the paired950 cache");
        if(store!=Cache.STORE){cached=load();store=Cache.STORE;}
        return cached;
    }
    private static Native950EquipmentCatalogue load(){
        List<Entry> rows=new ArrayList<>();
        try(java.io.InputStream stream=Native950EquipmentCatalogue.class.getResourceAsStream("/native950/equipment-library-950.tsv")){
            if(stream==null)throw new IllegalStateException("Missing generated950 equipment catalogue");
            java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8));
            String line;while((line=reader.readLine())!=null){
                if(line.startsWith("#")||line.isEmpty())continue;
                String[] row=line.split("\t",-1);if(row.length!=9)throw new IllegalStateException("Malformed equipment catalogue row");
                int id=Integer.parseInt(row[0]);
                String actual=hash(Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255));
                if(!actual.equals(row[8]))throw new IllegalStateException("Changed catalogue item "+id);
                rows.add(new Entry(id,Integer.parseInt(row[1]),Integer.parseInt(row[2]),Integer.parseInt(row[3]),Integer.parseInt(row[4]),Integer.parseInt(row[5]),row[6],row[7]));
            }
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot load equipment catalogue",e);}
        return new Native950EquipmentCatalogue(rows);
    }
    static String hash(byte[] bytes){
        if(bytes==null)throw new IllegalStateException("Missing catalogue cache asset");
        try{StringBuilder out=new StringBuilder();for(byte value:java.security.MessageDigest.getInstance("SHA-256").digest(bytes))out.append(String.format("%02x",value&255));return out.toString();}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    /** Offline regeneration only: live opening reads and verifies the bounded generated manifest. */
    static Native950EquipmentCatalogue generate(){
        List<List<Entry>> tabs=new ArrayList<>();for(int i=0;i<CATEGORIES.length;i++)tabs.add(new ArrayList<>());
        for(Native950ContentCommands.ItemSearchEntry old:Native950ContentCommands.testingKitItemBrowserEntries()) {
            ItemDefinitions d=Native950CacheItems.definition(old.id);
            if(valid(d))tabs.get(0).add(entry(d,0));
        }
        Map<String,Entry> names=new LinkedHashMap<>();
        for(int archive:Cache.STORE.getIndexes()[19].getTable().getValidArchiveIds())
            for(int file:Cache.STORE.getIndexes()[19].getTable().getArchives()[archive].getValidFileIds()) {
                int id=archive*256+file;if(id>Native950ItemCatalog.MAX_ITEM_ID)continue;
                ItemDefinitions d=Native950CacheItems.definition(id);
                if(!valid(d))continue;
                String n=d.name.toLowerCase(Locale.ROOT);
                if(n.matches(".*(broken|degraded|uncharged|cosmetic|replica|ornamental|dummy|placeholder|lucky).*"))continue;
                if(n.matches(".*\\((blood|soul|shadow|ice|third age|barrows|aurora|lent|bound|b|p|p\\+|p\\+\\+)\\).*"))continue;
                if(d.equipSlot>=0&&n.matches(".*\\([0-9]+\\).*"))continue;
                if(n.startsWith("augmented ")||n.contains("(t)")||n.contains("(g)")||n.contains("(or)"))continue;
                if(n.contains("custom-fit")||n.contains("spiked masterwork")||n.contains("(meagre)")||n.contains("(saturated)"))continue;
                if(n.startsWith("batch of ")||n.startsWith("deathmatch ")||n.startsWith("inverted ")||n.contains(" master cape"))continue;
                if(n.startsWith("golden ")||n.contains("juju")||n.matches(".*\\((black|blue|green|pink|purple|red|yellow|orange|white|i|o)\\).*"))continue;
                if(d.equipSlot>=0&&n.matches(".* (0|20|25|40|50|60|75|80|100)$"))continue;
                if(n.contains("pickaxe")||n.contains("hatchet")||n.contains("mattock")||n.equals("inferno adze"))continue;
                int category=category(d);if(category<1)continue;
                Entry candidate=entry(d,category),old=names.get(category+":"+n);
                if(old==null||candidate.tier>old.tier)names.put(category+":"+n,candidate);
            }
        for(Entry entry:names.values())tabs.get(entry.category).add(entry);
        // Dyed/augmented favourites stay in Best; style tabs contain clean base identities.
        // Small convenience overlay: supplies also exposes the basic combat resources.
        for(int id:new int[]{58036,58041,556,555,554,557,560,565,566,9075,563,562,561,564}){
            ItemDefinitions d=Native950CacheItems.definition(id);if(valid(d)&&!contains(tabs.get(6),id))tabs.get(6).add(entry(d,6));
        }
        List<Entry> out=new ArrayList<>();
        int[] limits={40,135,130,125,90,60,80,40};
        for(int category=0;category<tabs.size();category++){
            List<Entry> tab=tabs.get(category);
            if(category!=0) {
                // Smithing upgrade families contribute their highest usable version only.
                Map<String,Integer> upgrades=new HashMap<>();
                for(Entry e:tab)upgrades.put(upgradeBase(e.name),Math.max(upgrades.getOrDefault(upgradeBase(e.name),0),upgrade(e.name)));
                tab.removeIf(e->upgrade(e.name)<upgrades.get(upgradeBase(e.name)));
                if(category==6){
                    Set<String> flasks=new HashSet<>();for(Entry e:tab)if(e.name.endsWith("(6)"))flasks.add(doseBase(e.name));
                    tab.removeIf(e->e.name.endsWith("(4)")&&flasks.contains(doseBase(e.name)));
                }
                Map<String,Integer> tier=new HashMap<>();
                for(Entry e:tab)tier.put(e.group,Math.max(tier.getOrDefault(e.group,0),e.tier));
                tab.sort(Comparator.<Entry>comparingInt(e->section(e))
                        .thenComparing(Comparator.<Entry>comparingInt(e->tier.get(e.group)).reversed())
                        .thenComparingInt(e->armour(e.slot)?0:1)
                        .thenComparing(e->e.group).thenComparingInt(e->slotOrder(e.slot)).thenComparing(e->e.name));
            }
            // Bound the native grid, retaining complete groups rather than truncating a set.
            int taken=0;int[] bands=new int[4];
            for(int i=0;i<tab.size();) {
                int end=i+1;while(end<tab.size()&&tab.get(end).group.equals(tab.get(i).group))end++;
                Entry first=tab.get(i);int band=first.tier>=95?0:first.tier>=85?1:first.tier>=60?2:3;
                int bandLimit=category>=1&&category<=3?(band==0?35:band==1?50:band==2?30:25):limits[category];
                int limit=limits[category]-((category==2||category==3)&&section(first)<5?20:0);
                if(section(first)==5)bandLimit=limits[category];
                if(taken+end-i<=limit&&bands[band]+end-i<=bandLimit) {out.addAll(tab.subList(i,end));taken+=end-i;bands[band]+=end-i;}
                i=end;
            }
        }
        return new Native950EquipmentCatalogue(out);
    }
    private static boolean contains(List<Entry> entries,int id){for(Entry e:entries)if(e.id==id)return true;return false;}
    private static boolean valid(ItemDefinitions d){
        return d!=null&&d.name!=null&&!d.name.trim().isEmpty()&&!d.name.equalsIgnoreCase("null")
                &&!d.name.startsWith("<")&&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0;
    }
    private static int category(ItemDefinitions d){
        String n=d.name.toLowerCase(Locale.ROOT);
        if(d.equipSlot>=0&&Native950EquipmentTypes.resolve(d.getId())!=null){
            if(d.equipSlot==13)return n.contains("spikes")?1:2;
            if(n.contains("spirit shield"))return 5;
            if(d.getCSOpcode(8898)==1||n.contains("necromancer")||n.contains("deathwarden")||n.contains("deathdealer"))return 4;
            if(d.isMeleeTypeGear()||d.isMeleeTypeWeapon())return 1;
            if(d.isRangeTypeGear()||d.isRangeTypeWeapon())return 2;
            if(d.isMagicTypeGear()||d.isMagicTypeWeapon())return 3;
            if(d.equipSlot==1)return n.contains("igneous")||n.contains("tokhaar")||n.equals("fire cape")||n.contains("completionist")?5:-1;
            if(d.getCSOpcode(2870)>0||d.getCSOpcode(23)>0
                    ||n.matches(".*(essence of finality|amulet of souls|reaper necklace|amulet of fury|ring of death|reaver's ring|champion's ring|channeller's ring|stalker's ring|asylum surgeon|luck of the dwarves).*"))return 5;
        }
        if(n.matches("(spirit|bone|flesh|miasma) rune")||n.equals("ectoplasm"))return 4;
        if(n.matches("(air|water|earth|fire|mind|body|cosmic|chaos|nature|law|death|blood|soul|astral|armadyl|steam|lava|mud|smoke|mist|dust) rune"))return 3;
        if(n.matches(".*(overload|restore|prayer potion|adrenaline potion|saradomin brew|super antifire).*\\([46]\\)")
                ||n.matches("(sailfish|rocktail|shark|manta ray|cooked karambwan|blue blubber jellyfish|green blubber jellyfish|great gunkan|tuna potato)"))return 6;
        if(n.matches("(varrock|lumbridge|falador|camelot|ardougne|watchtower|teleport to house) teleport")
                ||n.matches("(charming imp|bonecrusher|herbicide|seedicide|spring cleaner|notepaper|magic notepaper|enhanced excalibur)"))return 7;
        return -1;
    }
    private static Entry entry(ItemDefinitions d,int category){
        int tier=d.getCSOpcode(23,0);
        Native950EquipmentTypes.Type type=d.equipSlot<0?null:Native950EquipmentTypes.resolve(d.getId());
        if(tier==0&&type!=null)for(int level:type.requirements.values())tier=Math.max(tier,level);
        int quantity=d.stackable==1?10000:d.equipSlot<0?1000:28;
        return new Entry(d.getId(),category,tier,d.equipSlot,quantity,d.stackable,d.name,group(d)+"@"+tier);
    }
    static String group(ItemDefinitions d){
        String n=d.name.toLowerCase(Locale.ROOT);
        if(d.equipSlot==0||d.equipSlot==4||d.equipSlot==7||d.equipSlot==9||d.equipSlot==10){
            for(String set:new String[]{"vestments of havoc","first necromancer","elite tectonic","elite sirenic","deathwarden","deathdealer","masterwork","tectonic","sirenic","torva","pernix","virtus","malevolent","cinderbane"})
                if(n.contains(set))return (n.contains("custom-fit")?"custom-fit ":n.contains("trimmed")?"trimmed ":"")+set+(n.contains("visage")?"~visage":"");
            return n.replaceAll("\\b(full helm|med helm|platebody|platelegs|plateskirt|chainbody|armoured|helm|helmet|mask|hood|hat|coif|cowl|hauberk|body|top|legs|bottoms|bottom|gloves|gauntlets|boots|shoes|robe|robetop|robeskirt|chaps|tunic|cuirass|greaves)\\b","").replaceAll("\\s+"," ").trim();
        }
        if(n.contains("dark shard of leng")||n.contains("dark sliver of leng"))return "leng dual wield";
        if(n.contains("wand of the praesul")||n.contains("imperium core"))return "praesul dual wield";
        if(n.contains("seismic wand")||n.contains("seismic singularity"))return "seismic dual wield";
        return n.replaceFirst("^off[- ]hand ","");
    }
    private static int slotOrder(int slot){switch(slot){case 0:return 0;case 4:return 1;case 7:return 2;case 9:return 3;case 10:return 4;default:return 10+slot;}}
    private static boolean armour(int slot){return slot==0||slot==4||slot==7||slot==9||slot==10;}
    private static String upgradeBase(String name){return name.replaceAll(" \\+ ?[1-5]$","").toLowerCase(Locale.ROOT);}
    private static int upgrade(String name){return name.matches(".* \\+ ?[1-5]$")?name.charAt(name.length()-1)-'0':0;}
    private static String doseBase(String name){return name.toLowerCase(Locale.ROOT).replace(" flask","").replaceAll(" \\([46]\\)$","");}
    /** Small editorial layer: iconic combat gear precedes generic cache families. */
    private static int section(Entry e){
        String n=e.name.toLowerCase(Locale.ROOT);
        if(e.category==6){if(n.matches("sailfish|rocktail|shark|blue blubber jellyfish"))return 0;
            if(n.contains("overload")||n.contains("restore")||n.contains("prayer potion"))return 1;return 2;}
        if(e.category>=1&&e.category<=4){
            if(n.matches(".*(vestments of havoc|trimmed masterwork|elite sirenic|elite tectonic|first necromancer).*"))return 0;
            if(n.matches(".*(dark shard of leng|dark sliver of leng|ek-zekkil|bow of the last guardian|blightbound|fractured staff of armadyl|wand of the praesul|imperium core|omni guard|soulbound lantern).*"))return 1;
            if(e.tier>=85&&n.matches(".*(noxious|drygore|malevolent|sirenic|tectonic|seismic|ascension|masterwork).*"))return 2;
            if(e.slot==13||e.slot<0)return 5; // Resource stacks after equipment.
            if(e.tier>=85||n.matches(".*(torva|pernix|virtus).*"))return 3;
            return 4;
        }
        if(e.category==5){
            if(n.contains("essence of finality")||n.contains("amulet of souls")||n.contains("reaver's ring")||n.contains("ring of death")||n.contains("igneous"))return 0;
            if(n.contains("warpriest")||n.contains("sliske")||n.contains("spirit shield"))return 1;
            return 2;
        }
        return 0;
    }
}
