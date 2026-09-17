package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Native companion ownership over the original pouch table, current paired item/NPC links and world tick. */
public final class Native950Familiars {
    public static final int MAX_TICKS=14400;
    private static final Object KEY=new Object();
    private static final Map<NPC,Player> OWNERS=new IdentityHashMap<>();
    private static final Properties PINS=loadPins();
    private static Object store;
    private static Map<Integer,Profile> profiles;
    private Native950Familiars() { }
    public static synchronized Collection<Profile> profiles() {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();
        if(store==Cache.STORE&&profiles!=null)return profiles.values();
        Map<Integer,Profile> next=new TreeMap<>();
        for(Pouches old:Pouches.values()) {
            int id=old.getPouchId();
            if(!PINS.containsKey("pouch."+id))continue;
            ItemDefinitions item=Native950CacheItems.definition(id);
            int npcId=item.getCSOpcode(74,-1),level=item.getCSOpcode(394,-1),points=item.getCSOpcode(396,-1);
            int minutes=Integer.parseInt(PINS.getProperty("minutes."+id));
            byte[] raw=file(18,npcId,7);
            if(!pin("pouch."+id,file(19,id,8))||!pin("npc."+npcId,raw))throw new IllegalStateException("Changed950 familiar binding: "+id);
            NPCDefinitions npc=NPCDefinitions.decodeStrict947(npcId,raw,null);
            String name=item.name==null?"":item.name.replace(" pouch","");
            if(item.certTemplateId!=-1||item.lendTemplateId!=-1||item.bindTemplateId!=-1||item.shardTemplateId!=-1
                ||item.inventoryOptions==null||!"Summon".equals(item.inventoryOptions[0])
                ||npc.transformTo!=null||npc.models==null||npc.models.length==0||npc.size<1||npc.size>4
                ||!name.equalsIgnoreCase(npc.name)||npc.hasOption("Attack")||npc.clientScriptData==null
                ||!Integer.valueOf(id).equals(npc.clientScriptData.get(75))||level<1||level>120
                ||points<100||points>12000||points%100!=0||minutes<1||minutes>144)
                throw new IllegalStateException("Invalid950 familiar metadata: "+id);
            next.put(id,new Profile(id,npcId,npc.size,level,points/100,minutes*100,name));
        }
        profiles=Collections.unmodifiableMap(next);store=Cache.STORE;return profiles.values();
    }
    public static Profile profile(int id){profiles();return profiles==null?null:profiles.get(id);}
    public static void verifyCacheBindings(){if(profiles().size()<60)throw new IllegalStateException("Familiar catalogue incomplete");}
    private static State state(Player p){
        Object found=p.getTemporaryAttributtes().get(KEY);if(found instanceof State)return (State)found;
        State made=new State();p.getTemporaryAttributtes().put(KEY,made);return made;
    }
    public static boolean handlesItem(int id,String option){return "Summon".equalsIgnoreCase(option)&&Pouches.forId(id)!=null;}
    public static boolean item(Player p,int slot,int id,String option){
        if(!handlesItem(id,option))return false;
        if(!ready(p))return true;
        Profile profile=profile(id);
        if(profile==null){p.sendMessage("This familiar's current-cache identity is not supported yet.");return true;}
        State state=state(p);
        if(state.pouchId!=0){
            p.getDialogueManager().startDialogue(new Native950FamiliarDialogue(state.npc));return true;
        }
        String refusal=summon(p,slot,profile);if(refusal!=null)p.sendMessage(refusal);return true;
    }
    static String summon(Player p,int slot,Profile profile){
        if(!ready(p)||profile==null||state(p).pouchId!=0)return "You already have a familiar, or cannot summon right now.";
        if(!allowed(p))return "You cannot summon a familiar here.";
        if(p.getFamiliar()!=null||p.getPet()!=null)return "You already have a follower.";
        if(p.getSkills().getLevelForXp(Skills.SUMMONING)<profile.level)return "You need Summoning level "+profile.level+" to summon this familiar.";
        if(p.getSkills().getLevel(Skills.SUMMONING)<profile.pointCost)return "You need "+(profile.pointCost*10)+" Summoning points. Renew your points at a Summoning obelisk.";
        Item selected=slot<0||slot>=28?null:p.getInventory().getItem(slot);
        if(!ordinary(selected)||selected.getId()!=profile.pouchId)return "The pouch in that backpack slot has changed.";
        WorldTile tile=findSpace(p,profile.size);if(tile==null)return "There is not enough clear space beside you for this familiar.";
        if(!Native950Skilling.canExchangeSlot(p,slot,profile.pouchId,1,new Item[0]))return "That pouch cannot be consumed right now.";
        NPC npc=spawn(p,profile,tile);
        boolean committed=false;
        try {
            if(p.getInventory().getItem(slot)!=selected||!Native950Skilling.exchangeSlot(p,slot,profile.pouchId,1,new Item[0]))return "That pouch cannot be consumed right now.";
            State s=state(p);s.pouchId=profile.pouchId;s.remaining=profile.durationTicks;s.npc=npc;s.drain=0;s.heal=0;
            p.getSkills().drainSummoning(profile.pointCost);committed=true;
            p.sendMessage("You summon a "+profile.name.toLowerCase(Locale.ROOT)+" for "+(profile.durationTicks/100)+" minutes. Interact with it to check, recall, renew or dismiss it.");
            p.sendMessage("This familiar follows you; combat, scrolls and carried-item storage are not available yet.");
            return null;
        } finally {if(!committed)remove(npc);}
    }
    static NPC spawn(Player p,Profile profile,WorldTile tile){
        NPC npc=NPC.createNative950Diagnostic(profile.npcId,tile);
        try {
            npc.enableNative950Movement();World.addNative950Npc(npc);World.updateEntityRegion(npc);
            Native950World.getInstance().nativeNpcs().add(npc);OWNERS.put(npc,p);return npc;
        }catch(RuntimeException failure){remove(npc);throw failure;}
    }
    public static boolean isFamiliar(NPC npc){return OWNERS.containsKey(npc);}
    public static boolean handlesNpc(NPC npc,int option){return isFamiliar(npc)&&option>=1&&option<=5&&npc.getNative950MenuOption(option)!=null;}
    public static boolean interact(Player p,NPC npc,int option){
        if(!handlesNpc(npc,option))return false;
        if(OWNERS.get(npc)!=p){if(p!=null)p.sendMessage("That familiar belongs to another adventurer.");return true;}
        if(!ready(p)||npc.hasFinished()||npc.isDead()||p.getPlane()!=npc.getPlane()||!p.withinDistance(npc,5))return true;
        p.getDialogueManager().startDialogue(new Native950FamiliarDialogue(npc));return true;
    }
    static boolean controls(Player p,NPC expected){return ready(p)&&state(p).npc==expected&&expected!=null&&OWNERS.get(expected)==p&&!expected.hasFinished()&&!expected.isDead();}
    static String status(Player p){
        State s=state(p);Profile f=profile(s.pouchId);
        if(f==null)return "You have no active familiar.";
        return f.name+": "+((s.remaining*6+9)/10)+" seconds remaining. Summoning points: "+(p.getSkills().getLevel(Skills.SUMMONING)*10)+". "+
            (s.pouchId==12029?"The bunyip restores 2% of your maximum life points every 15 seconds.":"Following is available; familiar combat, scrolls and carried-item storage are not available yet.");
    }
    static String renew(Player p,NPC expected){
        if(!controls(p,expected))return "That familiar is no longer active.";
        State s=state(p);Profile f=profile(s.pouchId);
        if(p.getSkills().getLevelForXp(Skills.SUMMONING)<f.level||p.getSkills().getLevel(Skills.SUMMONING)<f.pointCost)return "You need the familiar's required Summoning level and "+(f.pointCost*10)+" Summoning points to renew it.";
        int slot=-1;for(int i=0;i<28;i++){Item held=p.getInventory().getItem(i);if(ordinary(held)&&held.getId()==f.pouchId){slot=i;break;}}
        if(slot==-1)return "Bring another "+f.name.toLowerCase(Locale.ROOT)+" pouch to renew your familiar.";
        if(!Native950Skilling.exchangeSlot(p,slot,f.pouchId,1,new Item[0]))return "Your pouch could not be consumed.";
        p.getSkills().drainSummoning(f.pointCost);s.remaining=f.durationTicks;return "You renew your familiar's full lifetime.";
    }
    static String recall(Player p,NPC expected){
        if(!controls(p,expected))return "That familiar is no longer active.";
        Profile f=profile(state(p).pouchId);WorldTile tile=findSpace(p,f.size);
        if(tile==null)return "There is not enough clear space beside you to recall your familiar.";
        expected.resetWalkSteps();expected.setNextWorldTile(tile);state(p).stuck=0;return "You call your familiar to your side.";
    }
    static void dismiss(Player p,NPC expected){if(!controls(p,expected))return;clear(p);p.sendMessage("You dismiss your familiar.");}
    /** One world-thread tick. Lifetimes pause offline; player death does not destroy a modern familiar. */
    public static void tick(Player p){
        if(p==null||!p.isNative950())return;State s=state(p);if(s.pouchId==0)return;
        if(p.hasFinished()||!p.isActive())return;
        if(s.npc!=null&&(s.npc.isDead()||s.npc.hasFinished()||!World.containsNPC(s.npc))){clear(p);return;}
        // Restricted areas suspend the companion, including a pending login restore. No new
        // pouch/points are charged when returning; the hidden companion's timer is paused.
        if(!allowed(p)){remove(s.npc);s.npc=null;return;}
        if(--s.remaining<=0){clear(p);p.sendMessage("Your familiar's time has run out.");return;}
        Profile f=profile(s.pouchId);
        if(f==null){clear(p);p.sendMessage("Your saved familiar is unavailable in this cache.");return;}
        s.remaining=Math.min(s.remaining,f.durationTicks);
        if(s.remaining==100||s.remaining==50)p.sendMessage("Your familiar will vanish in "+(s.remaining==100?"one minute":"30 seconds")+".");
        if(++s.drain>=100){s.drain=0;p.getSkills().drainSummoning(1);}
        if(p.isDead()||p.getNextWorldTile()!=null||p.hasTeleported())return;
        if(s.npc==null){WorldTile tile=findSpace(p,f.size);if(tile!=null)s.npc=spawn(p,f,tile);return;}
        NPC npc=s.npc;
        if(npc.isDead()||npc.hasFinished()||!World.containsNPC(npc)){clear(p);return;}
        int distance=Math.max(Math.abs(npc.getX()-p.getX()),Math.abs(npc.getY()-p.getY()));
        if(npc.getPlane()!=p.getPlane()||distance>12||s.stuck>=8){WorldTile tile=findSpace(p,f.size);if(tile!=null){npc.resetWalkSteps();npc.setNextWorldTile(tile);s.stuck=0;}return;}
        if(p.isLocked()||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)return;
        if(distance>2){
            npc.resetWalkSteps();npc.setRun(p.getRun());npc.calcFollow(p,2,true,true);
            if(npc.hasWalkSteps())s.stuck=0;else s.stuck++;
        }else {npc.resetWalkSteps();s.stuck=0;}
        if(++s.heal>=25){s.heal=0;if(s.pouchId==12029&&p.withinDistance(npc,12))p.heal(Math.max(0,p.getMaxHitpoints()/50));}
    }
    public static int[] snapshot(Player p){State s=state(p);return new int[]{s.pouchId,s.remaining};}
    public static int[] validateSnapshot(int[] a){if(a==null||a.length!=2||a[0]<0||a[0]>65534||a[1]<0||a[1]>MAX_TICKS||(a[0]==0)!=(a[1]==0))throw new IllegalArgumentException("Invalid saved familiar");return a.clone();}
    public static void restore(Player p,int[] snapshot){validateSnapshot(snapshot);State s=state(p);if(s.npc!=null)throw new IllegalStateException("Cannot restore over a live familiar");int[] a=snapshot.clone();s.pouchId=a[0];s.remaining=a[1];s.drain=0;s.heal=0;s.stuck=0;}
    /** Call after capturing state; removes the world entity without erasing the saved remaining lifetime. */
    public static void onLogout(Player p){if(p==null||!p.isNative950())return;State s=state(p);remove(s.npc);s.npc=null;}
    public static void clear(Player p){State s=state(p);remove(s.npc);s.npc=null;s.pouchId=0;s.remaining=0;}
    static NPC current(Player p){return state(p).npc;}
    static void remove(NPC npc){if(npc==null)return;OWNERS.remove(npc);Native950World.getInstance().nativeNpcs().remove(npc);if(!npc.hasFinished())World.removeNative950Npc(npc);}
    static boolean allowed(Player p){return p.getControlerManager().canSummonFamiliar()&&!Native950Dungeoneering.inAnyRoom(p)&&!Native950Dungeoneering.interrupted(p);}
    static boolean ready(Player p){return Native950Production.Recipe.ready(p)&&p.getNextWorldTile()==null&&!p.hasTeleported()&&!p.isNative950ForceMovementActive()&&p.getNextForceMovement()==null;}
    static boolean ordinary(Item i){return i!=null&&i.getAmount()>0&&i.getAttributes()==null&&i.getCharges()==0&&i.getInventionData()==null;}
    static WorldTile findSpace(Player p,int size){
        for(int radius=1;radius<=5;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++){
            if(Math.max(Math.abs(dx),Math.abs(dy))!=radius)continue;
            int x=p.getX()+dx,y=p.getY()+dy;if(x<0||y<0||x+size>16384||y+size>16384)continue;
            if(x<=p.getX()&&x+size>p.getX()&&y<=p.getY()&&y+size>p.getY())continue;
            if(!World.isFloorFree(p.getPlane(),x,y,size))continue;
            WorldTile tile=new WorldTile(x,y,p.getPlane());boolean occupied=false;
            for(NPC other:World.getNPCs())if(other!=state(p).npc&&other.getPlane()==tile.getPlane()&&!other.hasFinished()&&!other.isDead()
                &&x<other.getX()+other.getSize()&&x+size>other.getX()&&y<other.getY()+other.getSize()&&y+size>other.getY()){occupied=true;break;}
            if(!occupied)for(Player other:World.getPlayers())if(other!=p&&!other.hasFinished()&&other.isActive()&&other.getPlane()==tile.getPlane()&&x<=other.getX()&&x+size>other.getX()&&y<=other.getY()&&y+size>other.getY()){occupied=true;break;}
            if(!occupied)return tile;
        }return null;
    }
    private static byte[] file(int index,int id,int shift){return id<0?null:Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));}
    private static boolean pin(String key,byte[] raw){if(raw==null)return false;try{StringBuilder value=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))value.append(String.format("%02x",b&255));return value.toString().equals(PINS.getProperty(key));}catch(Exception e){throw new IllegalStateException(e);}}
    private static Properties loadPins(){Properties p=new Properties();try(InputStream in=Native950Familiars.class.getResourceAsStream("/native950/summoning-familiars-950.properties")){if(in==null)throw new IllegalStateException("Missing950 familiar bindings");p.load(in);}catch(java.io.IOException e){throw new IllegalStateException(e);}if(!"950".equals(p.getProperty("revision")))throw new IllegalStateException("Wrong familiar binding revision");return p;}
    public static final class Profile {public final int pouchId,npcId,size,level,pointCost,durationTicks;public final String name;Profile(int p,int n,int s,int l,int c,int t,String name){pouchId=p;npcId=n;size=s;level=l;pointCost=c;durationTicks=t;this.name=name;}}
    private static final class State {int pouchId,remaining,drain,heal,stuck;NPC npc;}
}
