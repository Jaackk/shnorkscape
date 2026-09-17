package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.fletching.Fletching;
import com.rs.game.player.actions.fletching.defs.Fletchables;
import com.rs.game.player.actions.crafting.GemCutting.Gem;
import com.rs.game.player.actions.herblore.HerbCleaning.Herbs;
import com.rs.game.player.actions.herblore.Herblore;
import java.util.*;
import java.io.InputStream;
import java.security.MessageDigest;

/** Ordinary910 recipes on the native inventory boundary; ActionManager remains the scheduler. */
public final class Native950Production {
    private Native950Production() { }
    public static final class Recipe {
        public final String label;
        public final int skill, level, animation, delay;
        public final double xp;
        private final Item[] consumed, produced;
        private final int[] tools;
        private final Map<Integer,int[]> alternatives=new LinkedHashMap<>();
        public Recipe(String label,int skill,int level,double xp,int animation,int delay,
                      Item[] consumed,Item[] produced,int... tools) {
            if (label==null || label.isEmpty() || skill<0 || skill>=29 || level<1 || xp<0
                    || !Double.isFinite(xp) || animation < -1 || delay<0 || consumed==null
                    || consumed.length==0 || produced==null || tools==null)
                throw new IllegalArgumentException("Invalid production recipe");
            this.label=label;this.skill=skill;this.level=level;this.xp=xp;this.animation=animation;this.delay=delay;
            this.consumed=copy(consumed);this.produced=copy(produced);this.tools=tools.clone();
        }
        private static Item[] copy(Item[] items) {
            Item[] result=new Item[items.length];
            for(int i=0;i<items.length;i++) {
                Item item=items[i];
                if(item==null || item.getId()<1 || item.getId()>65534 || item.getAmount()<1
                        || item.getAttributes()!=null || item.getCharges()!=0 || item.getInventionData()!=null) throw new IllegalArgumentException("Invalid recipe item");
                result[i]=new Item(item);
            }
            return result;
        }
        /** Alternatives are immutable recipe data; each completion resolves one entire material batch. */
        public Recipe withAlternatives(int canonical,int... choices) {
            if(!amounts().containsKey(canonical)||choices==null||choices.length==0)throw new IllegalArgumentException("Missing alternative input");
            LinkedHashSet<Integer> ids=new LinkedHashSet<>();ids.add(canonical);
            for(int id:choices){if(id<1||id>65534||(id!=canonical&&amounts().containsKey(id)))throw new IllegalArgumentException("Overlapping alternative input");ids.add(id);}
            for(Map.Entry<Integer,int[]> e:alternatives.entrySet())if(e.getKey()!=canonical)for(int id:e.getValue())if(ids.contains(id))throw new IllegalArgumentException("Overlapping alternative family");
            Recipe result=new Recipe(label,skill,level,xp,animation,delay,consumed,produced,tools);
            for(Map.Entry<Integer,int[]> e:alternatives.entrySet())result.alternatives.put(e.getKey(),e.getValue().clone());
            int[] values=new int[ids.size()];int i=0;for(int id:ids)values[i++]=id;
            result.alternatives.put(canonical,values);return result;
        }
        public boolean usesInput(int id){for(int key:amounts().keySet()){if(key==id)return true;for(int alternative:alternatives.getOrDefault(key,new int[0]))if(alternative==id)return true;}return false;}
        private int selected(Player p,int canonical,long required){
            int selected=canonical;long most=-1;
            for(int id:alternatives.getOrDefault(canonical,new int[]{canonical})){long count=held(p,id);if(count>=required)return id;if(count>most){most=count;selected=id;}}
            return selected;
        }
        private Map<Integer,Long> amounts(Player p){Map<Integer,Long> result=new LinkedHashMap<>();for(Map.Entry<Integer,Long> e:amounts().entrySet())result.put(selected(p,e.getKey(),e.getValue()),e.getValue());return result;}
        public Item[] consumed(Player p){List<Item> result=new ArrayList<>();for(Map.Entry<Integer,Long> e:amounts(p).entrySet()){if(e.getValue()>Integer.MAX_VALUE)throw new IllegalStateException("Recipe input exceeds inventory limit");result.add(new Item(e.getKey(),e.getValue().intValue()));}return result.toArray(new Item[0]);}
        public Item[] consumed(){return copy(consumed);}
        public Item[] produced(){return copy(produced);}
        public int[] tools(){return tools.clone();}
        /** Aggregate repeated inputs before previewing; no preview reserves or changes inventory. */
        private Map<Integer,Long> amounts() {
            Map<Integer,Long> amounts=new LinkedHashMap<>();
            for(Item item:consumed)amounts.put(item.getId(),amounts.getOrDefault(item.getId(),0L)+item.getAmount());
            return amounts;
        }
        private static long held(Player player,int id) {
            long amount=0;
            for(Item item:player.getInventory().items.getItems())if(item!=null&&item.getId()==id)amount+=item.getAmount();
            return amount;
        }
        /** Material bound only: space and controller permissions are rechecked for every completion. */
        public int maximumByMaterials(Player player) {
            if(player==null)return 0;long maximum=10000;
            for(Map.Entry<Integer,Long> input:amounts().entrySet()){long batches=0;for(int id:alternatives.getOrDefault(input.getKey(),new int[]{input.getKey()}))batches+=held(player,id)/input.getValue();maximum=Math.min(maximum,batches);}
            return (int)maximum;
        }
        public String productName() {
            return produced.length==0?label:name(produced[0].getId());
        }
        public String[] details(Player player) {
            List<String> lines=new ArrayList<>();
            lines.add(productName()+": level "+level+" "+Skills.SKILL_NAME[skill]+", "+xp+" base XP per batch.");
            List<String> materials=new ArrayList<>();
            for(Map.Entry<Integer,Long> input:amounts(player).entrySet())materials.add(input.getValue()+" x "+name(input.getKey())+" (have "+held(player,input.getKey())+")");
            lines.add("Materials per batch: "+String.join(", ",materials)+".");
            for(int[] ids:alternatives.values()){List<String> names=new ArrayList<>();for(int id:ids)names.add(name(id));lines.add("Interchangeable materials (one kind per batch): "+String.join(", ",names)+".");}
            if(tools.length>0){List<String> names=new ArrayList<>();for(int id:tools)names.add(name(id));lines.add("Tools in your backpack or tool belt: "+String.join(", ",names)+".");}
            if(produced.length>0){List<String> outputs=new ArrayList<>();for(Item item:produced)outputs.add(item.getAmount()+" x "+name(item.getId()));lines.add("Each batch produces "+String.join(", ",outputs)+".");}
            return lines.toArray(new String[0]);
        }
        public static boolean ready(Player player) {
            return player!=null&&player.isNative950()&&player.isActive()&&!player.hasFinished()&&!player.isDead()
                &&!player.isLocked()&&!player.isNative950ForceMovementActive()&&!player.closeInterfaceLocked
                &&player.getNextForceMovement()==null&&!player.hasTeleported()&&player.getNextWorldTile()==null&&!player.hasWalkSteps();
        }
        public String refusal(Player player) {
            if(!ready(player))return "Finish moving or your current interaction before making this.";
            if(player.getSkills().getLevel(skill)<level)return "You need level "+level+" in "+Skills.SKILL_NAME[skill]+" to make "+productName()+".";
            for(int tool:tools)if(!player.getInventory().containsItem(tool,1)&&!Native950Toolbelt.has(player,tool))return "You need "+name(tool)+" in your backpack or tool belt.";
            Map<Integer,Long> inputs=amounts(player);
            // Never let an ID-only recipe consume a charged, augmented or otherwise personalised instance.
            for(Item item:player.getInventory().items.getItems())if(item!=null&&inputs.containsKey(item.getId())
                &&(item.getCharges()!=0||item.getAttributes()!=null||item.getInventionData()!=null))
                return "Bank the charged or customised "+name(item.getId())+" before using ordinary materials for this recipe.";
            for(Map.Entry<Integer,Long> input:inputs.entrySet()){
                long count=held(player,input.getKey());
                if(count<input.getValue())return "You need "+(input.getValue()-count)+" more "+name(input.getKey())+" ("+input.getValue()+" required, "+count+" in your backpack).";
            }
            if(!Native950Skilling.canExchange(player,consumed(player),produced))return "Your backpack cannot hold the result. Free some space or reduce an existing stack.";
            return null;
        }
        public boolean complete(Player player) {
            if(refusal(player)!=null || !Native950Skilling.exchange(player,consumed(player),produced))return false;
            if(xp>0)player.getSkills().addXp(skill,xp);
            return true;
        }
    }
    /** The enum rows are kept as the source of levels, experience and material quantities. */
    public static List<Recipe> pair(int first,int second) {
        List<Recipe> result=new ArrayList<>();
        for(Recipe recipe:recipes()) if(matchesPair(recipe,first,second) && verified(recipe)) result.add(recipe);
        return result;
    }
    private static boolean matchesPair(Recipe r,int first,int second) {
        if(first==second)return false;
        boolean a=false,b=false;
        for(Item item:r.consumed){a|=item.getId()==first;b|=item.getId()==second;}
        for(int tool:r.tools){a|=tool==first;b|=tool==second;}
        return a && b;
    }
    public static Recipe cleaning(int item) {
        for(Recipe recipe:recipes()) if(recipe.label.startsWith("Clean ")
                &&recipe.consumed[0].getId()==item &&verified(recipe))return recipe;
        return null;
    }
    /** Admit only the specific operation actually present on this950 item. */
    public static List<Recipe> inventoryRecipes(int item,String operation) {
        List<Recipe> result=new ArrayList<>();
        for(Recipe recipe:recipes()) if(matchesInventory(recipe,item,operation)&&verified(recipe))result.add(recipe);
        return result;
    }
    private static boolean matchesInventory(Recipe r,int item,String operation) {
        if(operation==null)return false;
        boolean ingredient=false;
        for(Item input:r.consumed)ingredient|=input.getId()==item;
        if(!ingredient)return false;
        String op=operation.toLowerCase(Locale.ROOT);
        if(op.equals("clean"))return r.label.startsWith("Clean ");
        if(op.equals("grind")||op.equals("powder"))return r.label.startsWith("Grind ");
        if(op.equals("mix")||op.equals("make"))return r.label.startsWith("Mix ")||r.label.startsWith("Prepare:");
        if(op.equals("craft")||op.equals("fletch")||op.equals("string")||op.equals("tip")||op.equals("feather"))
            return r.skill==Skills.FLETCHING||r.skill==Skills.CRAFTING||r.label.startsWith("Prepare:");
        return false;
    }
    public static Native950ItemCatalog.Entry itemEntry(int id) {
        if(!itemPinned(id))return null;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        if(!d.loaded || d.isNoted() || d.isLended())return null;
        String[] menu=new String[5];boolean supported=false;
        for(int i=0;d.inventoryOptions!=null&&i<Math.min(5,d.inventoryOptions.length);i++) {
            for(Recipe r:recipes())if(matchesInventory(r,id,d.inventoryOptions[i])){
                menu[i]=d.inventoryOptions[i];supported=true;break;
            }
        }
        return supported?new Native950ItemCatalog.Entry(id,d.getName(),d.isStackable(),menu):null;
    }
    private static volatile List<Recipe> all;
    public static synchronized List<Recipe> recipes() {
        if(all!=null)return Native950ProductionRecipes.appendTo(all);
        List<Recipe> rows=new ArrayList<>();
        for(Gem gem:Gem.values()) rows.add(new Recipe("Cut gem "+gem.name(),Skills.CRAFTING,gem.getLevelRequired(),
                gem.getExperience(),gem.getEmote(),1,items(gem.getUncut(),1),items(gem.getCut(),1),1755));
        for(Herbs herb:Herbs.values()) rows.add(new Recipe("Clean "+herb.name(),Skills.HERBLORE,herb.getLevel(),
                herb.getExperience(),-1,0,items(herb.getHerbId(),1),items(herb.getCleanId(),1)));
        for(Fletchables f:Fletchables.values()) {
            // Dungeoneering, Slayer unlocks and specialist recipes have separate prerequisites.
            if(f.getSelected()==17754 || f.name().startsWith("BROAD_") || f.name().contains("BANE")
                    || f.name().startsWith("SAG") || f.name().equals("BOLAS") || f.name().startsWith("BAKR")
                    || f.name().startsWith("ASCEN"))continue;
            for(int i=0;i<f.getProduct().length;i++) {
                int amount=Fletching.getAmountProduced(f,i);
                int primary=Fletching.getPrimaryAmountRequired(f,i);
                boolean tool=Fletching.requiresTool(f);
                Item[] inputs=tool?items(f.getId(),primary):new Item[]{new Item(f.getId(),primary),new Item(f.getSelected(),amount)};
                rows.add(new Recipe("Fletch "+f.name()+" "+i,Skills.FLETCHING,f.getLevel()[i],f.getXp()[i]*amount,
                        f.getAnim().getIds()[0],1,inputs,items(f.getProduct()[i],amount),tool?new int[]{f.getSelected()}:new int[0]));
            }
        }
        for(Herblore.Ingredients h:Herblore.Ingredients.values()) {
            // Quest products and multi-ingredient overloads are deliberately not two-item recipes.
            if(h.name().startsWith("EVIL_") || h==Herblore.Ingredients.HARMONY_MOSS || h==Herblore.Ingredients.CHOPPED_ONION
                    || Herblore.JUJU_INGREDIENTS.contains(h) || h==Herblore.Ingredients.STARFLOWER
                    || h==Herblore.Ingredients.CRUSHED_GORAK_CLAW || h==Herblore.Ingredients.RUBIUM
                    || h==Herblore.Ingredients.PHARMAKOS_BERRIES || h==Herblore.Ingredients.SHRUNK_OGLEROOT)continue;
            for(int i=0;i<h.getOtherItems().length;i++) {
                int other=h.getOtherItems()[i];
                if(other==Herblore.SWAMP_TAR || h==Herblore.Ingredients.TORSTOL&&other!=Herblore.VIAL)continue;
                int level=Math.max(1,h.getLevels()[i]);
                rows.add(new Recipe("Mix "+h.name()+" "+i,Skills.HERBLORE,level,h.getExperience()[i],363,1,
                        new Item[]{new Item(h.getItemId(),h==Herblore.Ingredients.GRENWALL_SPIKES?5:1),new Item(other,1)},
                        items(h.getRewards()[i],1)));
            }
        }
        for(Herblore.RawIngredient raw:Herblore.RawIngredient.values()) {
            // Sq'irk juices require multiple fruits/glass; urchins have variable quantities.
            if(raw.name().contains("IRK")||raw.name().startsWith("URCHIN"))continue;
            rows.add(new Recipe("Grind "+raw.name(),Skills.HERBLORE,1,0,364,1,items(raw.getRawId(),1),
                    new Item[]{new Item(raw.getCrushedItem())},233));
        }
        all=Collections.unmodifiableList(rows);return Native950ProductionRecipes.appendTo(all);
    }
    private static Item[] items(int id,int amount){return new Item[]{new Item(id,amount)};}
    public static String name(int id) {
        if(Cache.STORE==null)return "item "+id;
        ItemDefinitions item=ItemDefinitions.getItemDefinitions(id);
        return item.loaded?item.getName():"item "+id;
    }
    public static boolean verified(Recipe recipe) {
        for(Item item:recipe.consumed)if(!itemPinned(item.getId()))return false;
        for(Item item:recipe.produced)if(!itemPinned(item.getId()))return false;
        for(int tool:recipe.tools)if(!itemPinned(tool))return false;
        return recipe.animation==-1 || pin("sequence",recipe.animation,20,7);
    }
    private static boolean itemPinned(int id) {
        if(!pin("item",id,19,8))return false;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return d.loaded&&!d.isNoted()&&!d.isLended();
    }
    private static Properties pins;
    private static Object pinnedStore;
    private static final Map<String,Boolean> verified=new HashMap<>();
    private static synchronized boolean pin(String kind,int id,int index,int shift) {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return false;
        if(pins==null) {
            pins=new Properties();
            try(InputStream in=Native950Production.class.getResourceAsStream("/native950/production-assets-950.properties")) {
                if(in==null)throw new IllegalStateException("Missing950 production asset bindings");
                pins.load(in);
            } catch(java.io.IOException e){throw new IllegalStateException(e);}
        }
        if(pinnedStore!=Cache.STORE){verified.clear();pinnedStore=Cache.STORE;}
        String key=kind+"."+id;
        if(!verified.containsKey(key)) {
            String expected=pins.getProperty(key);boolean valid=false;
            if(expected!=null)try {
                byte[] bytes=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
                if(bytes!=null){StringBuilder hex=new StringBuilder();
                    for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hex.append(String.format("%02x",b&255));
                    valid=expected.equals(hex.toString());}
            }catch(Exception invalid){valid=false;}
            verified.put(key,valid);
        }
        return verified.get(key);
    }
}
