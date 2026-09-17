package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.Hit;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Original FlyingEntities levels, catch XP and loot; current cache identity and atomic jar exchange. */
public final class Native950Implings {
    public static final int EMPTY_JAR = 11260;
    private static final Properties PINS = load();
    private static final Map<String,Boolean> VERIFIED = new HashMap<>();
    private static Object store;
    private Native950Implings() { }

    static synchronized boolean pin(String kind,int id) {
        if (Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        if (store != Cache.STORE) { store=Cache.STORE; VERIFIED.clear(); }
        String key=kind+"."+id;
        if (VERIFIED.containsKey(key)) return VERIFIED.get(key);
        boolean matches=false;
        try {
            int index=kind.equals("npc")?18:kind.equals("item")?19:20, shift=index==19?8:7;
            byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
            if (raw!=null && PINS.containsKey(key)) {
                StringBuilder hash=new StringBuilder();
                for(byte b:MessageDigest.getInstance("SHA-256").digest(raw)) hash.append(String.format("%02x",b&255));
                matches=PINS.getProperty(key).equals(hash.toString());
            }
        } catch (Exception invalid) { matches=false; }
        VERIFIED.put(key,matches);return matches;
    }
    static boolean item(int id) { return pin("item",id) && Native950CacheItems.entry(id)!=null; }
    public static boolean supports(FlyingEntities data) {
        if(data==null || !data.isImpling() || !pin("npc",data.getNpcId()) || !pin("sequence",6606)
                || !item(EMPTY_JAR) || !item(data.getReward()) || !item(10010) || !item(11259)) return false;
        Native950ItemCatalog.Entry jar=Native950CacheItems.entry(data.getReward());
        return jar!=null && "Loot".equalsIgnoreCase(jar.option(3));
    }
    public static boolean verifyCacheBindings() {
        for(String key:PINS.stringPropertyNames()) {
            String[] parts=key.split("\\.");
            if(parts.length==2 && (parts[0].equals("item") || parts[0].equals("npc") || parts[0].equals("sequence")))
                if(!pin(parts[0],Integer.parseInt(parts[1]))) return false;
        }
        for(FlyingEntities data:FlyingEntities.values()) if(data.isImpling()) {
            if(!supports(data))return false;
            for(Item[] bucket:buckets(data)) if(bucket!=null) for(Item reward:bucket) if(!item(reward.getId()) || reward.getAmount()<1)return false;
        }
        return true;
    }
    static Item[][] buckets(FlyingEntities data) {
        return new Item[][]{data.getRarleyCommon(),data.getCommon(),data.getRare(),data.getExtremelyRare()};
    }
    /** Original 60/30/9.8/0.2 weights; absent rarity falls back to the next populated lower tier. */
    static Item[] bucket(FlyingEntities data,int roll) {
        if(roll<0 || roll>=1000)throw new IllegalArgumentException("Loot roll outside bounds");
        int tier=roll<600?0:roll<900?1:roll<998?2:3;Item[][] rows=buckets(data);
        while(tier>0 && (rows[tier]==null || rows[tier].length==0))tier--;
        return rows[tier];
    }
    public static boolean isJar(int id) { return supports(FlyingEntities.forId(id)); }
    public static boolean loot(Player player,int slot,int id,int option) {
        return loot(player,slot,id,option,bound->ThreadLocalRandom.current().nextInt(bound));
    }
    static boolean loot(Player player,int slot,int id,int option,IntUnaryOperator rolls) {
        if(!Native950ThievingHunterAssets.ready(player) || Native950Thieving.underCombat(player)
                || slot<0 || slot>=28 || option!=3 || !isJar(id))return false;
        Item held=player.getInventory().getItem(slot);
        if(held==null || held.getId()!=id || held.getAmount()!=1 || held.getAttributes()!=null || held.getCharges()!=0)return false;
        FlyingEntities data=FlyingEntities.forId(id);
        Item[] pool=bucket(data,rolls.applyAsInt(1000));
        if(pool==null || pool.length==0)return false;
        Item row=pool[rolls.applyAsInt(pool.length)];
        if(!item(row.getId()) || row.getAmount()<1)return false;
        List<Item> rewards=new ArrayList<>();
        rewards.add(new Item(row.getId(),1+rolls.applyAsInt(row.getAmount())));
        boolean breaks=rolls.applyAsInt(4)==0;
        if(!breaks)rewards.add(new Item(EMPTY_JAR,1));
        // The original Spirit effect could select a zero-size stack; one charm is the intended reward.
        if(data==FlyingEntities.SPIRIT_IMPLING && rolls.applyAsInt(2)==0) {
            int[] charms={12158,12159,12160,12163};int charm=charms[rolls.applyAsInt(charms.length)];
            if(!item(charm))return false;
            rewards.add(new Item(charm,1));
        }
        Item[] output=rewards.toArray(new Item[rewards.size()]);
        if(!Native950Skilling.canExchangeSlot(player,slot,id,1,output)) {
            player.sendMessage("Free some backpack space before looting this impling jar.");return false;
        }
        if(!Native950Skilling.exchangeSlot(player,slot,id,1,output))return false;
        player.getActionManager().forceStop();
        player.sendMessage("You loot the impling jar.");
        if(breaks) {
            player.sendMessage("The empty jar shatters in your hands.");
            // Match the original minor cut through the verified native hit path; never kill during an item action.
            int damage=Math.min(Math.max(0,player.getHitpoints()-1),5+rolls.applyAsInt(10));
            if(damage>0)player.applyHit(new Hit(player,damage,Hit.HitLook.REGULAR_DAMAGE));
        }
        return true;
    }
    private static Properties load() {
        Properties p=new Properties();
        try(InputStream input=Native950Implings.class.getResourceAsStream("/native950/impling-assets-950.properties")) {
            if(input==null)throw new IllegalStateException("Missing950 impling pins");p.load(input);
            if(!"950".equals(p.getProperty("revision")))throw new IllegalStateException("Wrong impling revision");
            return p;
        } catch(java.io.IOException failure) {throw new IllegalStateException(failure);}
    }
}
