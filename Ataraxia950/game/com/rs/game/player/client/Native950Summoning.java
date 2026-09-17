package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.summoning.defs.SummoningPouches;
import com.rs.game.player.actions.summoning.defs.SummoningScrolls.SummoningScroll;
import java.util.*;
/** Ordinary pouch/scroll infusion. Familiar spawning and control are a separate port. */
public final class Native950Summoning {
    private Native950Summoning(){}
    public static void verifyCacheBindings(){Native950Familiars.verifyCacheBindings();Native950CraftingAssets.verifyAll();if(recipes().size()!=161)throw new IllegalStateException("Unexpected950 Summoning recipe count: "+recipes().size());}
    private static Object store;private static List<Native950Production.Recipe> cached;
    public static synchronized List<Native950Production.Recipe> recipes(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();if(store==Cache.STORE&&cached!=null)return cached;store=Cache.STORE;
        List<Native950Production.Recipe> list=new ArrayList<>();Set<Integer> admitted=new HashSet<>(),scrolls=new HashSet<>();
        for(SummoningPouches old:SummoningPouches.values()){
            int id=canonicalPouchId(old);if(id>=17000&&id<19000||id>65534)continue;
            Native950Production.Recipe r=Native950CraftingAssets.product("Infuse pouch",id,Skills.SUMMONING,725,2,old.getItems());
            if(r==null||!ordinaryPouch(r))continue;list.add(r);admitted.add(id);
        }
        for(SummoningScroll old:SummoningScroll.values()){
            if(scrolls.contains(old.getItemId()))continue;
            Native950Production.Recipe r=Native950CraftingAssets.product("Create scrolls",old.getItemId(),Skills.SUMMONING,725,2,new Item[]{old.getPouch()});
            if(r==null||r.consumed().length!=1||!admitted.contains(r.consumed()[0].getId())||r.produced()[0].getAmount()!=10)continue;
            list.add(r);scrolls.add(old.getItemId());
        }
        // Current950 separates these familiars' scroll recipes instead of using one legacy shared id.
        for(int id=25590;id<=25600;id++){
            Native950Production.Recipe r=Native950CraftingAssets.product("Create scrolls",id,Skills.SUMMONING,725,2,new Item[0]);
            if(r!=null&&r.consumed().length==1&&admitted.contains(r.consumed()[0].getId())&&r.produced()[0].getAmount()==10&&!scrolls.contains(id)){list.add(r);scrolls.add(id);}
        }
        cached=Collections.unmodifiableList(list);return cached;
    }
    static int canonicalPouchId(SummoningPouches old){
        switch(old){
            case SPIRIT_KALPHITE_POUCH:return com.rs.game.player.actions.summoning.Summoning.Pouches.SPIRIT_KALPHITE.getPouchId();
            case VOID_SPINNER_POUCH:return com.rs.game.player.actions.summoning.Summoning.Pouches.VOID_SPINNER.getPouchId();
            case SPIRIT_GRAAHK_POUCH:return com.rs.game.player.actions.summoning.Summoning.Pouches.SPIRIT_GRAAHK.getPouchId();
            default:return old.getPouchId();
        }
    }
    private static boolean ordinaryPouch(Native950Production.Recipe r){
        boolean pouch=false,charm=false,shards=false;
        for(Item i:r.consumed()){pouch|=i.getId()==12155;charm|=i.getId()==12158||i.getId()==12159||i.getId()==12160||i.getId()==12163;shards|=i.getId()==12183;}
        return pouch&&charm&&shards&&r.produced()[0].getAmount()==1;
    }
    public static boolean isObelisk(WorldObject o){
        if(o==null||o.getType()<0||o.getType()>22||!Native950CraftingAssets.pin("object",o.getId(),16,8))return false;
        ObjectDefinitions d=o.getDefinitions();if(!d.loaded||d.transforms!=null||!"Obelisk".equalsIgnoreCase(d.name)||d.sizeX<1||d.sizeX>16||d.sizeY<1||d.sizeY>16)return false;
        if(d.options!=null)for(String op:d.options)if("Infuse-pouch".equalsIgnoreCase(op))return true;return false;
    }
    public static boolean accepts(WorldObject o,int option){if(!isObelisk(o)||option<1||option>5)return false;String[] ops=o.getDefinitions().options;return ops!=null&&option<=ops.length&&("Infuse-pouch".equalsIgnoreCase(ops[option-1])||"Renew-points".equalsIgnoreCase(ops[option-1]));}
    public static boolean isRenewOption(WorldObject o,int option){return accepts(o,option)&&"Renew-points".equalsIgnoreCase(o.getDefinitions().options[option-1]);}
    public static boolean renewPoints(Player p,WorldObject o){
        if(!inReach(p,o)||!Native950Production.Recipe.ready(p))return false;
        int max=p.getSkills().getLevelForXp(Skills.SUMMONING);
        if(p.getSkills().getLevel(Skills.SUMMONING)>=max){p.sendMessage("You already have full Summoning points.");return true;}
        p.getActionManager().forceStop();p.setNextFaceWorldTile(o);p.getSkills().restoreSummoning();
        p.sendMessage("The obelisk restores your Summoning points.");return true;
    }
    public static boolean inReach(Player p,WorldObject o){return p!=null&&isObelisk(o)&&p.getNextWalkDirection()==-1&&!p.hasTeleported()&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);}
    public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){
        List<Native950ProductionMenu.Choice> list=new ArrayList<>();if(!inReach(p,o))return list;
        for(Native950Production.Recipe r:recipes()){
            // Show relevant products instead of dozens of unrelated recipes whenever any generic shard/charm is held.
            boolean relevant=false;for(Item i:r.consumed())if(i.getId()!=12183&&i.getId()!=12155&&i.getId()!=12158&&i.getId()!=12159&&i.getId()!=12160&&i.getId()!=12163)relevant|=p.getInventory().containsItem(i.getId(),1);
            if(relevant)list.add(new Native950ProductionMenu.Choice(Native950Production.name(r.produced()[0].getId())+" (level "+r.level+")",r,n->start(p,o,r,n)));
        }return list;
    }
    public static boolean start(Player p,WorldObject o,Native950Production.Recipe r,int quantity){
        if(p==null||r==null||quantity<1||quantity>10000||!recipes().contains(r)||!inReach(p,o))return false;p.setNextFaceWorldTile(o);
        return p.getActionManager().setAction(new Native950ProductionAction(r,quantity){@Override protected boolean environment(Player p){return inReach(p,o);}});
    }
    public static Native950ItemCatalog.Entry itemEntry(int id){for(Native950Production.Recipe r:recipes())if(Native950CraftingAssets.includes(r,id))return Native950CraftingAssets.entry(id,new String[5]);return null;}
}
