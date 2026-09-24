package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Developer conveniences over native containers and diagnostic NPC ownership. */
final class Native950ContentCommands {
    private Native950ContentCommands() { }
    static boolean recognizes(String command) {
        switch(command) {
            case "melee": case "mage": case "range": case "necro": case "meleegear": case "magegear": case "rangegear": case "ragegear": case "necrogear": case "weapons": case "gear": case "gearhelp":
            case "search": case "find": case "si": case "itemid": case "finditem":
            case "clearobjects": case "clearobjs": case "gameval": case "findnpc": case "snpc": case "clearnpcs": case "removenpc": case "delnpc": case "npcs": return true;
            default: return false;
        }
    }
    static Item[] kit(String name) {
        int style=bestStyle(name);
        if(style>=0)return quickItems(Native950DeveloperLoadouts.builtins().get(style));
        int[] ids;
        switch(name) {
            case "weapons": ids=new int[]{52533,16403,27913,51848,42574,42582,55145,55109,55114,56429,56492};break;
            default: return null;
        }
        Item[] result=new Item[ids.length];
        for(int i=0;i<ids.length;i++)result[i]=new Item(ids[i],ids[i]==58036||ids[i]==58041?10000:1);
        return result;
    }
    static int bestStyle(String name){
        switch(name){
            case "melee": case "meleegear": return 0;
            case "range": case "rangegear": case "ragegear": return 1;
            case "mage": case "magegear": return 2;
            case "necro": case "necromancy": case "necrogear": return 3;
            default:return -1;
        }
    }
    static Native950DeveloperLoadouts.Loadout quickLoadout(Native950DeveloperLoadouts.Loadout source){
        Item[] supplies=new Item[28];int at=0;
        for(Item item:source.inventory)if(item!=null&&!quickFood(item.getId()))supplies[at++]=new Item(item.getId(),item.getAmount());
        return new Native950DeveloperLoadouts.Loadout(source.name+" (no food)",supplies,source.equipment);
    }
    static Item[] quickItems(Native950DeveloperLoadouts.Loadout loadout){
        List<Item> out=new ArrayList<>();
        for(Item[] source:new Item[][]{loadout.equipment,loadout.inventory})for(Item item:source)
            if(item!=null&&!quickFood(item.getId()))out.add(new Item(item.getId(),item.getAmount()));
        return out.toArray(new Item[0]);
    }
    private static boolean quickFood(int id){
        if(id==42251)return true;
        if(com.rs.cache.Cache.isFlatReadOnly())for(String option:Native950CacheItems.definition(id).inventoryOptions)
            if("Eat".equalsIgnoreCase(option))return true;
        return false;
    }
    static void handle(Player p,Channel c,String[] args) {
        String command=args[0];
        if(command.equals("clearobjects")||command.equals("clearobjs")){
            try{
                if(args.length>2)throw new IllegalArgumentException("Use ;;clearobjects [radius 0-128]; default 16.");
                int radius=args.length==2?Integer.parseInt(args[1]):16;
                String refusal=Native950DiagnosticSpawns.refusal(p);if(refusal!=null){reply(c,refusal);return;}
                int count=Native950DeveloperWorldEdits.clearObjects(p,radius);
                if(count>0)Native950DeveloperConsole.placementsChanged(p);
                reply(c,"Removed "+count+" of your placed objects within "+radius+" tiles on this plane (including saved placements).");
            }catch(IOException failure){reply(c,"Could not save object removal; nothing removed: "+failure.getMessage());}
            catch(IllegalArgumentException invalid){reply(c,"Use ;;clearobjects [radius 0-128]; default 16.");}
            return;
        }
        if(command.equals("gameval")){
            Native950DeveloperConsole.gamevalsFor(p,String.join(" ",Arrays.copyOfRange(args,1,args.length)));return;
        }
        if(command.equals("gearhelp")) {
            reply(c,";;melee / ;;range / ;;mage / ;;necro: the library Best loadouts, without food.");
            reply(c,"Previous items are safely banked, then Best gear is equipped; old *gear aliases still work.");
            reply(c,";;weapons: high-tier weapons for all four styles. ;;gear melee|mage|range|necro|weapons.");
            reply(c,";;search <item name> [page]; ;;findnpc <NPC name> [page]. Pages show 10 IDs.");
            reply(c,";;npc <id> [1-50] is one-life; ;;npcrepeat <id> [1-50] respawns; ;;npc <name> lists IDs.");
            reply(c,";;npcs lists nearby test spawns; ;;removenpc <index>; ;;clearnpcs [radius].");return;
        }
        if(command.equals("search")||command.equals("find")||command.equals("si")||command.equals("itemid")
                ||command.equals("finditem")||command.equals("findnpc")||command.equals("snpc")) {
            if(args.length<2){reply(c,"Use ;;"+command+" <name> [page].");return;}
            int end=args.length,page=1;
            try { if(end>2 && args[end-1].matches("-?\\d+")){page=Integer.parseInt(args[--end]);} }
            catch(NumberFormatException invalid){reply(c,"Invalid page number.");return;}
            String query=String.join(" ",Arrays.copyOfRange(args,1,end));
            if(page<1||page>10000||query.length()<2||query.length()>80){reply(c,"Use a 2-80 character name and a positive page number.");return;}
            boolean npc=command.equals("findnpc")||command.equals("snpc");
            List<String> matches=search(npc,query);
            int pages=Math.max(1,(matches.size()+9)/10);
            if(page>pages){reply(c,"Only "+pages+" page(s) of matches.");return;}
            reply(c,matches.size()+" matching "+(npc?"NPCs":"items")+"; page "+page+"/"+pages+".");
            for(int i=(page-1)*10;i<Math.min(page*10,matches.size());i++)reply(c,matches.get(i));
            return;
        }
        if(command.equals("clearnpcs")||command.equals("removenpc")||command.equals("delnpc")||command.equals("npcs")) {
            try {
                boolean remove=command.equals("removenpc")||command.equals("delnpc");
                if(args.length>(command.equals("npcs")?1:2)||(remove&&args.length!=2))throw new IllegalArgumentException();
                int value=args.length==2?Integer.parseInt(args[1]):-1;
                if(args.length==2&&(value<0||value>(remove?65534:128)))throw new IllegalArgumentException();
                for(String line:Native950DiagnosticSpawns.manage(p,command,value))reply(c,line);
            }catch(IllegalArgumentException invalid){reply(c,"Use ;;npcs, ;;removenpc <index>, or ;;clearnpcs [radius 0-128].");}
            return;
        }
        String name=command.equals("gear")?(args.length==2?args[1]:""):command;
        if(!command.equals("gear")&&args.length!=1){reply(c,"Use ;;gearhelp for the available kits.");return;}
        int style=bestStyle(name);
        if(style>=0){reply(c,Native950DeveloperLoadouts.apply(p,quickLoadout(Native950DeveloperLoadouts.builtins().get(style))));return;}
        Item[] items=kit(name);
        if(items==null||(!command.equals("gear")&&args.length!=1)){reply(c,"Use ;;gear melee|mage|range|necro|weapons or ;;gearhelp.");return;}
        for(Item item:items)if(Native950Skilling.itemType(p,item.getId())==null){reply(c,"This cache cannot supply kit item "+item.getId()+". Nothing added.");return;}
        if(!Native950Skilling.giveItems(p,items)){reply(c,"Not enough backpack space, stack capacity, or your current activity forbids receiving gear. Nothing added.");return;}
        reply(c,"Added "+name+" kit to your backpack. Equip it normally; skill requirements still apply.");
    }
    // Exported once from the paired950 cache: searches never decode thousands of definitions on the world thread.
    private static final class Names {
        static final List<String[]> ITEMS=load("items"),NPCS=load("npcs");
    }
    static final class ItemSearchEntry {
        final int id; final String name, variant;
        ItemSearchEntry(int id,String name,String variant){this.id=id;this.name=name;this.variant=variant;}
        String label(){return name+" (ID "+id+")"+(variant.isEmpty()?"":" ["+variant+"]");}
        @Override public boolean equals(Object other){
            if(this==other)return true;if(!(other instanceof ItemSearchEntry))return false;
            ItemSearchEntry entry=(ItemSearchEntry)other;return id==entry.id&&variant.equals(entry.variant);
        }
        @Override public int hashCode(){return 31*id+variant.hashCode();}
    }
    static List<ItemSearchEntry> itemMatches(String query,int limit) {
        String needle=query==null?"":query.trim().toLowerCase(Locale.ROOT);
        if(needle.isEmpty()||limit<1)return Collections.emptyList();
        Integer exactId=null;try{exactId=Integer.valueOf(needle);}catch(NumberFormatException ignored){ }
        List<ItemSearchEntry> exact=new ArrayList<>(),prefix=new ArrayList<>(),other=new ArrayList<>();
        Set<String> seen=new HashSet<>();
        for(String[] row:Names.ITEMS){
            int id;try{id=Integer.parseInt(row[0]);}catch(NumberFormatException invalid){continue;}
            boolean idMatch=exactId!=null&&id==exactId.intValue();
            if(!idMatch&&!row[3].contains(needle))continue;
            String key=id+":"+row[2];if(!seen.add(key))continue;
            ItemSearchEntry entry=new ItemSearchEntry(id,row[1],row[2]);
            if(idMatch||row[3].equals(needle))exact.add(entry);
            else if(row[3].startsWith(needle)||row[3].contains(" "+needle))prefix.add(entry);
            else other.add(entry);
        }
        Comparator<ItemSearchEntry> usefulFirst=Comparator.comparingInt(Native950ContentCommands::variantRank)
                .thenComparingInt(entry->entry.id);
        exact.sort(usefulFirst);prefix.sort(usefulFirst);other.sort(usefulFirst);
        exact.addAll(prefix);exact.addAll(other);
        return exact.size()<=limit?Collections.unmodifiableList(exact)
                :Collections.unmodifiableList(new ArrayList<ItemSearchEntry>(exact.subList(0,limit)));
    }
    /** Four cache-derived style loadouts and four shared items; keep the native 40-slot grid. */
    static List<ItemSearchEntry> testingKitItemBrowserEntries(){
        int[] ids={
                // Melee: Nooby's saved Vestments/Primal dual-wield setup.
                53375,53378,53381,52028,53384,16403,27913,51470,50465,
                // Magic: Shadow elite tectonic, specialist gloves/boots, staff, dual wield and ring.
                42991,43119,43121,52036,51092,51848,42574,42582,51467,
                // Ranged: Soul elite sirenic, specialist gloves/boots, 2H, dual wield and ammunition.
                55045,55051,55056,52032,51088,55145,55109,55114,58041,
                // Necromancy: Jaxa's saved First Necromancer/Omni Guard setup.
                56483,56450,56513,56476,56469,56429,56492,51469,59928,
                // Shared high-tier extras and a combat supply.
                52504,55678,23531,39230
        };
        List<ItemSearchEntry> result=new ArrayList<ItemSearchEntry>();
        for(int id:ids){ItemSearchEntry entry=itemById(id);if(entry!=null)result.add(entry);}
        return Collections.unmodifiableList(result);
    }
    private static ItemSearchEntry itemById(int wanted){
        for(String[] row:Names.ITEMS){
            int id;try{id=Integer.parseInt(row[0]);}catch(NumberFormatException invalid){continue;}
            if(id==wanted&&row[2].isEmpty())return new ItemSearchEntry(id,row[1],row[2]);
        }
        return null;
    }
    private static int variantRank(ItemSearchEntry entry){
        String value=(entry.name+" "+entry.variant).toLowerCase(Locale.ROOT);
        if(entry.variant.isEmpty()&&!value.contains("(broken)")&&!value.contains(" shard"))return 0;
        if(value.contains("noted"))return 3;
        if(value.contains("broken")||value.contains("shard")||value.contains("lent"))return 4;
        return 1;
    }
    static List<String> search(boolean npc,String query) {
        String needle=query.toLowerCase(Locale.ROOT);List<String> result=new ArrayList<>();
        for(String[] row:npc?Names.NPCS:Names.ITEMS)if(row[3].contains(needle))
            result.add(row[0]+": "+row[1]+(row[2].isEmpty()?"":" ["+row[2]+"]"));
        return result;
    }
    private static List<String[]> load(String kind) {
        List<String[]> rows=new ArrayList<>();
        try(InputStream stream=Native950ContentCommands.class.getResourceAsStream("/native950/search-"+kind+".tsv")) {
            if(stream==null)throw new IllegalStateException("Missing950 search catalog");
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
                String line;while((line=reader.readLine())!=null) {
                    if(line.startsWith("#")||line.startsWith("ID\t"))continue;
                    String[] row=line.split("\t",-1);
                    if(row.length==3&&!row[1].startsWith("<")) {
                        String[] indexed=Arrays.copyOf(row,4);indexed[3]=row[1].toLowerCase(Locale.ROOT);rows.add(indexed);
                    }
                }
            }
        }catch(IOException error){throw new IllegalStateException("Cannot read950 search catalog",error);}
        return Collections.unmodifiableList(rows);
    }
    private static void reply(Channel c,String text){c.write(Native950Packets.gameMessage(0,text.length()>180?text.substring(0,177)+"...":text));}
}
