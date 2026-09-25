package com.rs.game.player.client;

import com.rs.game.player.Player;
import java.util.*;

/** Console adapter over the Equipment Library's existing safe index and grants. */
final class Native950DeveloperItems {
    static List<Native950EquipmentCatalogue.Entry> search(String text){
        if(text.trim().isEmpty()){
            Map<Integer,Native950EquipmentCatalogue.Entry> unique=new LinkedHashMap<>();
            for(Native950EquipmentCatalogue.Entry e:Native950EquipmentCatalogue.current().entries)unique.putIfAbsent(e.id,e);
            return new ArrayList<>(unique.values());
        }
        String query=text.trim().toLowerCase(Locale.ROOT);int exact=Native950DeveloperSearch.exactId(query);
        Set<Integer> symbols=new HashSet<>();
        for(Native950GamevalLookup.Entry symbol:Native950GamevalLookup.search(query))if(symbol.type.equals("item"))symbols.add(Integer.parseInt(symbol.id));
        List<Native950EquipmentCatalogue.Entry> matches=new ArrayList<>();
        for(Native950DeveloperSearch.Row row:Native950DeveloperSearch.current())
            if(exact>=0?row.item.id==exact:row.lower.contains(query)||symbols.contains(row.item.id))matches.add(row.item);
        Map<Integer,Integer> curated=new HashMap<>();
        for(Native950EquipmentCatalogue.Entry e:Native950EquipmentCatalogue.current().entries)curated.putIfAbsent(e.id,curated.size());
        matches.sort(Comparator.comparingInt(e->curated.getOrDefault(e.id,Integer.MAX_VALUE)));
        return matches;
    }
    static String details(int id){
        if(!Native950DeveloperSearch.verifiedItem(id))throw new IllegalArgumentException("Unverified item");
        com.rs.cache.loaders.ItemDefinitions d=Native950CacheItems.definition(id);
        Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);
        String[] slots={"Head","Cape","Neck","Main hand","Body","Off hand","","Legs","","Hands","Feet","","Ring","Ammunition","","Aura","","","Pocket"};
        String slot=d.equipSlot>=0&&d.equipSlot<slots.length?slots[d.equipSlot]:"Inventory";
        String style=type!=null&&type.requirements.containsKey(28)?"Necromancy":d.isMeleeTypeGear()?"Melee":d.isRangeTypeGear()?"Ranged":d.isMagicTypeGear()?"Magic":"General";
        int tier=d.getCSOpcode(23,0);
        return "ID "+id+"<br>"+slot+" | "+style+(tier>0?" | Tier "+tier:"");
    }
    static String give(Player player,int id,int quantity){
        if(quantity<1||quantity>1000000)throw new IllegalArgumentException("Quantity must be 1-1000000.");
        if(!Native950DeveloperSearch.verifiedItem(id))throw new IllegalArgumentException("This item is not in the safe library index.");
        if(!Native950Skilling.hasSpace(player,id,quantity))return "Not enough backpack space. Nothing was given.";
        return Native950Skilling.giveItem(player,id,quantity)?"Added "+quantity+" to your backpack.":"Your current activity refused this item.";
    }
    static String bank(Player player,int id,int quantity){
        if(quantity<1||quantity>1000000)throw new IllegalArgumentException("Quantity must be 1-1000000.");
        if(!Native950DeveloperSearch.verifiedItem(id))throw new IllegalArgumentException("This item is not in the safe library index.");
        if(player.getControlerManager().getControler()!=null)return "Leave this activity before adding developer items to your bank.";
        Native950Containers containers=Native950Skilling.containers(player);
        if(containers==null)return "Bank is unavailable.";
        return containers.receiveDeveloperBankItem(id,quantity).moved==quantity?"Added "+quantity+" to your bank.":"Your bank cannot hold that quantity. Nothing was changed.";
    }
    static String equip(Player player,int id){
        if(!Native950DeveloperSearch.verifiedItem(id))throw new IllegalArgumentException("This item is not in the safe library index.");
        Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);
        if(type==null)return "This item cannot be equipped.";
        String requirement=Native950EquipmentActions.missingRequirementsFromXp(type.requirements,player.getSkills()::getXp);
        if(requirement!=null)return requirement;
        Native950Containers containers=Native950Skilling.containers(player);
        if(containers==null)return "Inventory is unavailable.";
        // Give one only into a free slot: never take or change an existing copy.
        Native950Containers.Snapshot before=containers.inventorySnapshot();int slot=-1;
        for(int i=0;i<before.ids.length;i++)if(before.ids[i]<0){slot=i;break;}
        if(slot<0)return "Free one backpack slot before equipping a library item.";
        if(!Native950Skilling.giveItem(player,id,1))return "Could not give this item.";
        // Stackable equipment may join an existing stack. Keep it in inventory
        // rather than equipping the player's whole existing ammunition stack.
        if(player.getInventory().items.get(slot)==null||player.getInventory().items.get(slot).getId()!=id)
            return "Added one to your existing stack. Equip it from your backpack.";
        int option=0;for(int n=1;n<=5;n++)if(type.isWearOption(n)){option=n;break;}
        Native950EquipmentActions.Result result=Native950EquipmentActions.equip(player,containers,slot,id,option);
        return result.accepted?"Equipped. Displaced equipment is in your backpack.":result.reason+" The new item remains in your backpack.";
    }
}
