package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.network.protocol.modern950.Native950Packets;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Surface toolbelt ownership and current-cache upgrades; no legacy enum/packet defaults. */
public final class Native950Toolbelt {
    public static final int MAX_ITEMS=77;
    private static final Object KEY=new Object(), BUSY=new Object();
    private static final JsonObject DATA=load();
    private static final List<Row> ROWS=read();
    private static final Map<Integer,Variant> ITEMS=items();
    private static Object verified;
    private Native950Toolbelt(){}
    static Map<Integer,Integer> state(Player p){
        Object old=p.getTemporaryAttributtes().get(KEY);
        if(old instanceof Map)return (Map<Integer,Integer>)old;
        Map<Integer,Integer> s=new TreeMap<>();for(Row r:ROWS)if(r.seed)s.put(r.base,r.base);
        p.getTemporaryAttributtes().put(KEY,s);return s;
    }
    /** Base tools remain valid aliases when the same tool family has been upgraded. */
    public static boolean has(Player p,int id){if(p==null||!p.isNative950())return false;Variant v=ITEMS.get(id);if(v==null)return false;Integer held=state(p).get(v.row.base);return held!=null&&(held==id||v.row.base==id);}
    public static int[] tools(Player p){return snapshot(p);}
    public static int[] snapshot(Player p){return state(p).values().stream().mapToInt(Integer::intValue).sorted().toArray();}
    public static int[] validateSnapshot(int[] a){
        if(a==null||a.length>MAX_ITEMS)throw new IllegalArgumentException("Invalid toolbelt length");
        Set<Integer> slots=new HashSet<>();int previous=-1;
        for(int id:a){Variant v=ITEMS.get(id);if(id<=previous||v==null||!slots.add(v.row.base)||(!v.addable&&!v.row.seed))throw new IllegalArgumentException("Invalid saved toolbelt item "+id);previous=id;}
        return a.clone();
    }
    public static void restore(Player p,int[] a){int[] copy=validateSnapshot(a);Map<Integer,Integer>s=new TreeMap<>();for(Row r:ROWS)if(r.seed)s.put(r.base,r.base);for(int id:copy){Variant v=ITEMS.get(id);s.put(v.row.base,id);}p.getTemporaryAttributtes().put(KEY,s);}
    public static boolean isAddOption(String s){return s!=null&&s.replace("-","").replace(" ","").equalsIgnoreCase("addtotoolbelt");}
    public static boolean add(Player p,int slot,int id,String option){
        if(!isAddOption(option))return false;
        String why=addRefusal(p,slot,id);if(why!=null){p.sendMessage(why);return true;}
        Variant v=ITEMS.get(id);Row r=v.row;Map<Integer,Integer>s=state(p);Integer before=s.get(r.base);
        Item[] returned=before!=null&&r.removable&&!(r.seed&&before==r.base)?new Item[]{new Item(before,1)}:new Item[0];
        p.getTemporaryAttributtes().put(BUSY,Boolean.TRUE);
        try {
        if(!Native950Skilling.exchangeSlot(p,slot,id,1,returned)){p.sendMessage("The tool could not be moved. Check backpack space and area restrictions.");return true;}
        s.put(r.base,id);sync(p);p.sendMessage("You add your "+v.name.toLowerCase(Locale.ROOT)+" to the tool belt."+(returned.length>0?" Your previous tool returns to your backpack.":""));return true;
        } finally {p.getTemporaryAttributtes().remove(BUSY);}
    }
    static String addRefusal(Player p,int slot,int id){
        if(!ready(p))return "You cannot change your tool belt during this action.";
        verifyCacheBindings();Variant v=ITEMS.get(id);
        if(v==null||!v.addable)return "That tool's belt unlock or special function is not available yet.";
        Item held=slot<0||slot>=28?null:p.getInventory().getItem(slot);
        if(!ordinary(held)||held.getId()!=id)return "The ordinary tool in that backpack slot has changed.";
        ItemDefinitions d=Native950CacheItems.definition(id);boolean option=false;if(d.inventoryOptions!=null)for(String op:d.inventoryOptions)option|=isAddOption(op);
        if(!option)return "This item cannot be added to the tool belt.";
        if(!skillSupported(v))return "This tool's current skill action has not been ported yet. Keep it in your backpack for now.";
        if(v.skill>=0&&v.skill<Skills.SKILL_NAME.length&&p.getSkills().getLevelForXp(v.skill)<v.level)return "You need "+Skills.SKILL_NAME[v.skill]+" level "+v.level+" to add this tool.";
        Integer old=state(p).get(v.row.base);if(old!=null&&old==id)return "That tool is already on your tool belt.";
        if(old!=null&&!v.row.removable&&ITEMS.get(old).rank>=v.rank)return "You already have that tool or a better version on your tool belt.";
        return null;
    }
    static boolean skillSupported(Variant v){
        if(v.row.base==1265){for(Native950Mining.PickaxeDef p:Native950Mining.PICKAXES)if(p.itemId==v.id)return Native950Mining.itemEntry(v.id)!=null&&Native950Mining.animation(p)>=0;return false;}
        if(v.row.base==1351){for(com.rs.game.player.actions.woodcutting.AxeDef a:com.rs.game.player.actions.woodcutting.AxeDef.ALL)if(a.itemId==v.id)return Native950Woodcutting.itemEntry(v.id)!=null&&Native950Woodcutting.animation(a)>=0;return false;}
        return v.row.base!=49539||Native950Archaeology.precision(v.id)>0;
    }
    public static String remove(Player p,int uiSlot){
        if(!ready(p))return "You cannot change your tool belt during this action.";
        verifyCacheBindings();Row r=row(uiSlot);if(r==null||!r.removable)return "That tool cannot be removed from the tool belt.";
        Integer id=state(p).get(r.base);if(id==null)return "That tool is not stored.";
        if(r.seed&&id==r.base)return "The free basic tool cannot be removed.";
        p.getTemporaryAttributtes().put(BUSY,Boolean.TRUE);
        try {
        if(!Native950Skilling.giveItems(p,new Item[]{new Item(id,1)}))return "You need a free backpack slot, and permission to receive the tool here.";
        if(r.seed)state(p).put(r.base,r.base);else state(p).remove(r.base);
        sync(p);return "You remove your "+ITEMS.get(id).name.toLowerCase(Locale.ROOT)+" from the tool belt.";
        } finally {p.getTemporaryAttributtes().remove(BUSY);}
    }
    public static String describe(Player p,int uiSlot){
        Row r=row(uiSlot);if(r==null)return "Select a tool.";
        Integer id=state(p).get(r.base);Variant v=ITEMS.get(id==null?r.base:id);
        if(id==null)return v.name+" is not stored on your tool belt."+(v.addable?" Add a supported tool from your backpack.":" Its special unlock or action is not available in this port.");
        if(r.seed&&id==r.base)return v.name+" is a free basic tool on your tool belt and cannot be removed."+(r.removable?" You can replace it with a supported upgrade.":"");
        return v.name+" is stored on your tool belt."+(r.removable?" Right-click to remove it. Adding another supported tool returns this one to your backpack.":" This tool stays on your tool belt.");
    }
    public static int currentItem(Player p,int uiSlot){Row r=row(uiSlot);return r==null?-1:state(p).getOrDefault(r.base,r.base);}
    static Row row(int slot){return slot>=0&&slot<ROWS.size()?ROWS.get(slot):null;}
    public static void sync(Player p){
        if(p==null||p.getRealChannel()==null||Cache.STORE==null)return;
        verifyCacheBindings();Map<Integer,Integer> writes=new TreeMap<>();
        for(Row r:ROWS){if(!r.managed())continue;Integer held=state(p).get(r.base);Variant v=held==null?null:ITEMS.get(held);if(r.varbit>=0)writes.put(r.varbit,v==null?0:v.value);if(r.upgrade>=0)writes.put(r.upgrade,v==null?0:v.rank);}
        for(Map.Entry<Integer,Integer> e:writes.entrySet())p.getRealChannel().write(Native950Packets.varbitLarge(e.getKey(),e.getValue()));
    }
    static boolean ready(Player p){return p!=null&&p.isNative950()&&!p.getTemporaryAttributtes().containsKey(BUSY)&&Native950Production.Recipe.ready(p)&&!p.getActionManager().hasSkillWorking()&&!p.isNative950ForceMovementActive()&&p.getNextForceMovement()==null&&p.getNextWorldTile()==null&&!p.hasTeleported()&&!Native950Dungeoneering.interrupted(p);}
    static boolean ordinary(Item i){return i!=null&&i.getAmount()>0&&i.getAttributes()==null&&i.getCharges()==0&&i.getInventionData()==null;}
    public static synchronized void verifyCacheBindings(){if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Tool belt requires current950 cache");if(verified==Cache.STORE)return;Native950CacheReader c=new Native950CacheReader.Flat();for(Map.Entry<String,JsonElement> e:DATA.getAsJsonObject("files").entrySet()){String[] p=e.getKey().split("/");if(!e.getValue().getAsString().equals(c.sha256(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2]))))throw new IllegalStateException("Changed950 toolbelt binding "+e.getKey());}for(Variant v:ITEMS.values()){ItemDefinitions d=Native950CacheItems.definition(v.id);if(!v.name.equals(d.name)||d.certTemplateId>=0||d.lendTemplateId>=0||d.bindTemplateId>=0||d.shardTemplateId>=0)throw new IllegalStateException("Invalid toolbelt item "+v.id);}verified=Cache.STORE;}
    private static JsonObject load(){try(InputStream in=Native950Toolbelt.class.getResourceAsStream("/native950/toolbelt-950.json")){if(in==null)throw new IllegalStateException("Missing toolbelt metadata");JsonObject o=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();if(o.get("revision").getAsInt()!=950)throw new IllegalStateException("Toolbelt revision mismatch");return o;}catch(IOException e){throw new IllegalStateException(e);}}
    private static List<Row> read(){List<Row> rows=new ArrayList<>();for(JsonElement e:DATA.getAsJsonArray("rows")){Row r=new Row(e.getAsJsonObject());if(r.slot!=rows.size())throw new IllegalStateException("Toolbelt row order");rows.add(r);}if(rows.size()!=MAX_ITEMS)throw new IllegalStateException("Toolbelt row count");return Collections.unmodifiableList(rows);}
    private static Map<Integer,Variant> items(){Map<Integer,Variant> out=new HashMap<>();for(Row r:ROWS)for(Variant v:r.variants)if(out.put(v.id,v)!=null)throw new IllegalStateException("Duplicate belt identity");return Collections.unmodifiableMap(out);}
    static final class Row{final int slot,base,varbit,upgrade;final boolean seed,removable;final List<Variant>variants=new ArrayList<>();Row(JsonObject o){slot=o.get("slot").getAsInt();base=o.get("base").getAsInt();varbit=o.get("varbit").getAsInt();upgrade=o.get("upgradeVarbit").getAsInt();seed=o.get("default").getAsBoolean();removable=o.get("removable").getAsBoolean();for(JsonElement e:o.getAsJsonArray("variants"))variants.add(new Variant(this,e.getAsJsonObject()));}boolean managed(){if(seed)return true;for(Variant v:variants)if(v.addable)return true;return false;}}
    static final class Variant{final Row row;final int id,rank,value,skill,level;final String name;final boolean addable;Variant(Row r,JsonObject o){row=r;id=o.get("id").getAsInt();rank=o.get("rank").getAsInt();value=o.get("value").getAsInt();skill=o.get("skill").getAsInt();level=o.get("level").getAsInt();name=o.get("name").getAsString();addable=o.get("addable").getAsBoolean();}}
}

