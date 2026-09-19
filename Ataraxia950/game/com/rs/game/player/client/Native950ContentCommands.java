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
            case "meleegear": case "magegear": case "rangegear": case "weapons": case "gear": case "gearhelp":
            case "search": case "find": case "si": case "itemid": case "finditem":
            case "findnpc": case "snpc": case "clearnpcs": case "removenpc": case "delnpc": case "npcs": return true;
            default: return false;
        }
    }
    static Item[] kit(String name) {
        int[] ids;
        switch(name) {
            case "melee": case "meleegear": ids=new int[]{20135,6570,6585,18349,20139,20143,7462,11732,6737};break;
            case "mage": case "magegear": ids=new int[]{20159,6570,6585,18355,20163,20167,7462,6920,6737};break;
            case "range": case "rangegear": ids=new int[]{20147,6570,6585,18357,20151,20155,7462,11732,6737,9244};break;
            case "weapons": ids=new int[]{31725,31729,31733,26579,26583};break;
            default: return null;
        }
        Item[] result=new Item[ids.length];
        for(int i=0;i<ids.length;i++)result[i]=new Item(ids[i],ids[i]==9244?10000:1);
        return result;
    }
    static void handle(Player p,Channel c,String[] args) {
        String command=args[0];
        if(command.equals("gearhelp")) {
            reply(c,";;meleegear (Torva), ;;magegear (Virtus), ;;rangegear (Pernix): armour + Chaotic weapon into backpack.");
            reply(c,";;weapons: Noxious scythe/staff/longbow and Drygore rapiers. ;;gear melee|mage|range|weapons.");
            reply(c,";;search <item name> [page]; ;;findnpc <NPC name> [page]. Pages show 10 IDs.");
            reply(c,";;npc <id> [1-50]; ;;npcs lists your nearby test spawns; ;;removenpc <index>; ;;clearnpcs [radius].");return;
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
        Item[] items=kit(name);
        if(items==null||(!command.equals("gear")&&args.length!=1)){reply(c,"Use ;;gear melee|mage|range|weapons or ;;gearhelp.");return;}
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
    /**
     * Small, cache-derived Combat Alpha loadout. The first three groups mirror Jaxa's known-good
     * melee equipment and the magic/ranged equipment already used during live combat testing.
     */
    static List<ItemSearchEntry> testingKitItemBrowserEntries(){
        int[] ids={
                36294,36297,36300,38350,7462,21787,6737,
                42991,43119,43121,38300,
                55045,55051,55056,55145,63284,
                23531,385,556
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
