package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import java.util.*;

/** Developer definitions and session custom slots. Never reads or writes Bank.preset storage. */
final class Native950DeveloperLoadouts {
    static final String[] NAMES={"Melee - Best","Ranged - Best","Magic - Best","Necromancy - Best",
            "Melee - T80","Ranged - T80","Magic - T80","Tank / Defence","Boss Testing",
            "Developer Custom 1","Developer Custom 2","Developer Custom 3"};
    private static Object store;
    private static List<Loadout> builtins;
    private final Loadout[] custom=new Loadout[3];
    static final class Loadout {
        final String name;
        final Item[] inventory,equipment;
        Loadout(String name,Item[] inventory,Item[] equipment){this.name=name;this.inventory=copy(inventory);this.equipment=copy(equipment);}
    }
    Loadout get(int index){return index>=0&&index<9?builtins().get(index):index>=9&&index<12?custom[index-9]:null;}
    void capture(int index,Player player){
        if(index<9||index>=12)throw new IllegalArgumentException("Only developer custom slots may be overwritten");
        Native950Containers containers=Native950Skilling.containers(player);
        // Preparing without committing validates all ordinary item state and never touches property.
        Item[] inv=copy(player.getInventory().items.getItems()),eq=copy(player.getEquipment().getItems().getItems());
        for(Item[] items:new Item[][]{player.getInventory().items.getItems(),player.getEquipment().getItems().getItems()})
            for(Item item:items)if(item!=null&&(item.getCharges()!=0||item.getAttributes()!=null||item.getInventionData()!=null))
                throw new IllegalArgumentException("Custom loadouts cannot copy charged or customised item state");
        validate(new Loadout(NAMES[index],inv,eq));
        containers.prepareDeveloperLoadout(inv,eq);custom[index-9]=new Loadout(NAMES[index],inv,eq);
    }
    static synchronized List<Loadout> builtins(){
        if(store==Cache.STORE&&builtins!=null)return builtins;
        List<Loadout> out=new ArrayList<>();
        // Exact cache-audited IDs; metadata, style, tier, conflicts and models are revalidated.
        out.add(make(0,0,new int[]{53375,53378,53381,52028,53384},16403,27913,59928,51469,59925));
        out.add(make(1,1,new int[]{56128,56114,56132,56124,56120},58223,-1,50471,51468,58036));
        out.add(make(2,2,new int[]{59344,59336,59346,59342,59340},27711,27961,50471,51467,52217));
        out.add(make(3,3,new int[]{59934,55490,55492,55494,55496},61333,61355,50471,56878,56142));
        out.add(make(4,0,new int[]{20135,20139,20143,25060,25064},18349,25991,50465,51469,38453));
        out.add(make(5,1,new int[]{20147,20151,20155,25058,25068},18357,25995,50465,51468,24336));
        out.add(make(6,2,new int[]{20159,20163,20167,25062,25066},25654,25664,50465,51467,38453));
        out.add(make(7,0,new int[]{35245,35248,35249,35246,35247},16403,13740,50465,51470,59925));
        out.add(make(8,3,new int[]{59934,55490,55492,55494,55496},61333,61355,50471,56878,56142));
        builtins=Collections.unmodifiableList(out);store=Cache.STORE;return builtins;
    }
    private static Loadout make(int index,int style,int[] armour,int main,int off,int necklace,int ring,int ammo){
        Item[] eq=new Item[19];int[] slots={0,4,7,9,10};
        for(int i=0;i<slots.length;i++){
            ItemDefinitions d=Native950CacheItems.definition(armour[i]);Native950EquipmentTypes.Type t=Native950EquipmentTypes.resolve(armour[i]);
            if(t==null||t.slot!=slots[i])throw new IllegalStateException("Loadout armour slot "+armour[i]);
            boolean matches=style==0?d.isMeleeTypeGear():style==1?d.isRangeTypeGear():style==2?d.isMagicTypeGear():t.requirements.containsKey(28);
            if(!matches)throw new IllegalStateException("Loadout armour style "+armour[i]);
            int level=t.requirements.getOrDefault(1,0);
            if(index>=4&&index<=6&&level!=80)throw new IllegalStateException("T80 armour tier mismatch");
            eq[slots[i]]=new Item(armour[i],1);
        }
        eq[1]=new Item(52504,1);eq[2]=new Item(necklace,1);eq[3]=new Item(main,1);
        if(off>=0)eq[5]=new Item(off,1);
        eq[12]=new Item(ring,1);eq[13]=new Item(ammo,Native950CacheItems.definition(ammo).stackable==1?10000:1);
        eq[17]=new Item(style==3?55678:42787,1);
        Native950CombatStyles.Profile profile=Native950CombatStyles.profile(main);
        int expected=index>=4&&index<=6?80:style==1?100:99;
        if(profile.style!=style||profile.tier!=expected)throw new IllegalStateException("Loadout weapon tier/style mismatch "+main);
        if(style==1&&!Native950CombatStyles.matchesAmmo(Native950CacheItems.definition(ammo),profile.ammoFamily))throw new IllegalStateException("Loadout ammunition family");
        List<Item> supplies=new ArrayList<>();
        if(style!=3)supplies.add(new Item(43357,1000));
        if(style==2)for(int id:new int[]{556,555,554,557,560,565,566})supplies.add(new Item(id,10000));
        if(style==3)for(int id:new int[]{55336,55337,55338,55339,55340})supplies.add(new Item(id,10000));
        if(style==0){supplies.add(new Item(563,1000));supplies.add(new Item(566,1000));}
        for(int id:new int[]{23531,23531,23399,23399,33186,23351,23351,50351})supplies.add(new Item(id,1));
        if(index==8){supplies.add(new Item(23489,1));supplies.add(new Item(23327,1));}
        while(supplies.size()<28)supplies.add(new Item(42251,1));
        Loadout result=new Loadout(NAMES[index],supplies.toArray(new Item[28]),eq);validate(result);return result;
    }
    static void validate(Loadout loadout){
        if(loadout.inventory.length!=28||loadout.equipment.length!=19)throw new IllegalArgumentException("Loadout size");
        for(Item[] items:new Item[][]{loadout.inventory,loadout.equipment})for(Item item:items)if(item!=null){
            ItemDefinitions d=Native950CacheItems.definition(item.getId());
            if(!Native950DeveloperSearch.verifiedItem(item.getId())||!Native950DeveloperSearch.safe(d)||item.getAmount()<1||d.stackable!=1&&item.getAmount()!=1)
                throw new IllegalArgumentException("Unsafe loadout item "+item.getId());
        }
        for(int slot=0;slot<19;slot++)if(loadout.equipment[slot]!=null){
            Native950EquipmentTypes.Type t=Native950EquipmentTypes.resolve(loadout.equipment[slot].getId());
            if(t==null||t.slot!=slot)throw new IllegalArgumentException("Loadout wear slot");
            for(int conflict:t.conflictSlots())if(conflict!=slot&&loadout.equipment[conflict]!=null)throw new IllegalArgumentException("Conflicting loadout equipment");
        }
    }
    static String apply(Player player,Loadout loadout){
        if(loadout==null)return "This developer custom slot is empty. Use Overwrite to capture your current setup.";
        if(!available(player))return "Finish your current activity before applying a developer loadout.";
        boolean committed=false;
        try{
            validate(loadout);
            Native950Containers containers=Native950Skilling.containers(player);
            Native950Containers.DeveloperLoadoutChange change=containers.prepareDeveloperLoadout(loadout.inventory,loadout.equipment);
            if(change.result.moved==0)return "Your bank cannot hold all displaced items. Nothing was changed.";
            for(Item item:player.getEquipment().getItems().getItems())if(item!=null&&!Native950EquipmentActions.cacheAllowsRemoval(Native950CacheItems.definition(item.getId())))
                return "An equipped item needs its own removal action. Nothing was changed.";
            for(Item item:loadout.equipment)if(item!=null){
                Native950EquipmentTypes.Type t=Native950EquipmentTypes.resolve(item.getId());
                String missing=Native950EquipmentActions.missingRequirementsFromXp(t.requirements,player.getSkills()::getXp);
                if(missing!=null)return missing;
                if(!t.genderSupported(player.getAppearence().isMale()))return "This equipment does not support your character model.";
            }
            if(!available(player))return "Your activity changed. Nothing was changed.";
            Native950Containers.Result result=change.commit();
            if(result.moved==0)return "Your items changed while preparing this loadout. Nothing was replaced.";
            committed=true;
            player.getAppearence().generateAppearenceData();
            return "Equipped "+loadout.name+". Previous inventory and equipment were safely moved to your bank.";
        }catch(IllegalArgumentException|IllegalStateException invalid){return committed?"Loadout applied and previous items banked, but the appearance refresh needs retrying."
                :"Loadout refused: "+invalid.getMessage()+". No items were replaced.";}
    }
    private static boolean available(Player p){return p.isActive()&&!p.hasFinished()&&!p.isDead()&&!p.isLocked()
            &&p.getControlerManager().getControler()==null&&!p.isNative950ForceMovementActive()&&p.getNextWorldTile()==null
            &&!Boolean.TRUE.equals(p.getTemporaryAttributtes().get("teleporting"));}
    private static Item[] copy(Item[] source){Item[] out=new Item[source.length];for(int i=0;i<out.length;i++)if(source[i]!=null)out[i]=new Item(source[i].getId(),source[i].getAmount());return out;}
}
