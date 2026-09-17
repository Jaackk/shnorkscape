package com.rs.game.player.content.dropcollection;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.TreasureTrails;
import com.rs.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ataraxia-server
 * paolo 07/06/2019
 * #Shnek6969
 */
public class DropCollectionHandler implements Serializable {
    /**
     * ser stuff
     */
    private static final long serialVersionUID = 1L;
    /**
     * Key : npcId
     * Value : Received drop ids, can contain dupes
     */
    @Getter @Setter
    public HashMap<Integer, List<Integer>> bossCollection;
    @Getter @Setter
    public HashMap<DropCollectionConstants.CLUE_TYPE, List<Integer>> clueCollection;
    @Getter @Setter
    public HashMap<Integer, List<Integer>> minigameCollection;
    private transient Player player;


    public void setPlayer(Player player){
        this.player = player;
        if(minigameCollection == null)
            minigameCollection = new HashMap<>();
    }

    public DropCollectionHandler(Player player){
        this.player = player;
        bossCollection = new HashMap<>();
        clueCollection = new HashMap<>();
        minigameCollection = new HashMap<>();
    }

    /**
     * for checking if a boss drop collection is 'complete'
     * @param data
     * @return
     */
    public boolean completedBossCollection(DropCollectionConstants.BOSS_DATA data){
        List<Integer> collectedDrops = getBossCollection().get(data.getNpcId());
        if(collectedDrops == null)
            return false;
        Set<Integer> uniqueDrops = new HashSet<>(collectedDrops);
        return uniqueDrops.size() == data.getDrops().length;
    }

    /**
     * returns the amount of unique drops you get from a certain colleciton
     * @return
     */
    public int getUniqueCount(List<Integer> receivedDrops, Item[] allLoot){
        int amount = 0;
        if(receivedDrops != null){
            for(Item i : allLoot) {
                if (receivedDrops.contains(i.getId())) {
                   amount++;
                }
            }
        }
        return amount;
    }

    /**
     * returns the drops the player already collected in an itemcontainer
     * @param data
     * @return
     */
    public ItemsContainer<Item> getReceivedMonsterDrops(DropCollectionConstants.BOSS_DATA data){
        return getReceivedDrops(getBossCollection().get(data.getNpcId()), data.getDrops());
    }

    /**
     * returns the loot you have already received from a certain clue type
     * @param clue
     * @return
     */
    public ItemsContainer<Item> getReceivedClueDrops(DropCollectionConstants.CLUE_TYPE clue){
        return getReceivedDrops(getClueCollection().get(clue),getClueLootByType(clue));
    }

    public ItemsContainer<Item> getReceivedMiniGame(DropCollectionConstants.MINIGAME_DATA data){
        return getReceivedDrops(getMinigameCollection().get(data.minigameId),data.getDrops());
    }
    /**
     * compares an list of your received drops with the possible drops so you get a new container with unique received drops
     * @param receivedDrops
     * @param possibleItems
     * @return
     */
    private ItemsContainer<Item> getReceivedDrops(List<Integer> receivedDrops, Item[] possibleItems){
        ItemsContainer<Item> container = new ItemsContainer<Item>(possibleItems.length, true);
        int baseNullItem = 0;
        if(receivedDrops != null){
            for(Item i : possibleItems) {
                if (receivedDrops.contains(i.getId())) {
                    int amount = receivedDrops.stream().filter(item -> item.equals(i.getId())).collect(Collectors.toList()).size();
                    container.add(new Item(i.getId(), amount));
                } else  //adding item with null inv model
                    container.add(new Item(252958 + baseNullItem++, 1)); //actually have to send null items
            }
        }
        return container;
    }

    /**
     * returns the array of all the loot you can get from a certain clue type
     * @param clue
     * @return
     */
    public Item[] getClueLootByType(DropCollectionConstants.CLUE_TYPE clue){
        Item[] items = null;
        switch(clue){
            case EASY:
                items = TreasureTrails.EASY_RARE_REWARDS;
                break;
            case MEDIUM:
                items = TreasureTrails.MEDIUM_RARE_REWARDS;
                break;
            case HARD:
                items = TreasureTrails.HARD_RARE_REWARDS;
                break;
            case ELITE:
                items = TreasureTrails.ELITE_RARE_REWARDS;
                break;
        }
        return items;
    }

    /**
     * returns the correct count of a certain boss
     * @param data
     * @return
     */
    public int getCorrectBossCount(DropCollectionConstants.BOSS_DATA data){
        switch(data){ //for  those who have different names than their definition
            case BARROW:
                return player.getBarrowsRunsDone();
            case ARRAXOR:
                return player.increaseKillStatistics("araxxor", false);
            case TWINS:
                return player.increaseKillStatistics("twin furies", false);
            case AOD:
                return player.increaseKillStatistics("Nex: Angel of Death", false);
            case ROTS:
                return player.increaseKillStatistics("rise of the six", false);
            case QBD:
                return player.increaseKillStatistics("queen black dragon", false);
            case TELOS:
                return player.increaseKillStatistics("telos", false);
        }
        NPCDefinitions def = NPCDefinitions.getNPCDefinitions(data.getNpcId());
        if(def != null) //when you try to get a name of a null npc you'll crash.
            return player.increaseKillStatistics(def.getName(), false);
        return 0;
    }
    /**
     * handles the boss counting
     * @param drop
     * @param npcId
     */
    public void handleBossKills(Item drop, int npcId){
        if(drop == null){ //some npc's actually do have null drops for some strange reason.
            return;
        }
        Item item = new Item(drop.getId(), drop.getAmount());
        if(player == null){
            Logger.getGlobal().info("player is null");
            return;
        }
        List<Integer> bossDrops = DropCollectionConstants.BOSS_DATA.getDropsByNPC().get(npcId);
        if(bossDrops != null) {
            if(item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
                item.setId(item.getDefinitions().getCertId());
            if(bossDrops.contains(item.getId())) {
                if (player.getDropCollectionHandler().getBossCollection().containsKey(npcId)) {// if npc already exists in the hashmap
                    for(int i=0;i<item.getAmount();i++)
                    player.getDropCollectionHandler().getBossCollection().get(npcId).add(item.getId());
                } else {
                    List<Integer> dropList = new ArrayList<>();
                    for(int i=0;i<item.getAmount();i++)
                    dropList.add(item.getId());
                    player.getDropCollectionHandler().getBossCollection().put(npcId, dropList);
                }
            }
        }
    }
    
    public boolean hasItemInCollectionLog(Item drop, int npcId) {
        List<Integer> collection = !player.getDropCollectionHandler().getBossCollection().containsKey(npcId) ? new ArrayList<Integer>() : player.getDropCollectionHandler().getBossCollection().get(npcId);
        return collection.contains(drop.getId());
    }
    
    public boolean hasItemInMinigameCollectionLog(DropCollectionConstants.MINIGAME_DATA data, int itemId) {
        List<Integer> collection = !player.getDropCollectionHandler().getMinigameCollection().containsKey(data.minigameId) ? new ArrayList<Integer>() : player.getDropCollectionHandler().getMinigameCollection().get(data.minigameId);
        return collection.contains(itemId);
    }
    /**
     * handles the clue loot
     * @param clue
     * @param itemId
     */
    public void handleClues(DropCollectionConstants.CLUE_TYPE clue, int itemId){
        if(getClueCollection().containsKey(clue)){
                getClueCollection().get(clue).add(itemId);
        } else { //if not already exists
            List<Integer> items = new ArrayList<>();
            items.add(itemId);
            getClueCollection().put(clue, items);
        }

    }

    public void handleMinigames(DropCollectionConstants.MINIGAME_DATA data, int itemId){
        if(getMinigameCollection().containsKey(data.minigameId)){
            getMinigameCollection().get(data.minigameId).add(itemId);
        } else { //if not already exists
            List<Integer> items = new ArrayList<>();
            items.add(itemId);
            getMinigameCollection().put(data.minigameId, items);
        }
    }


    public static void main(String[] args) {
        byte byte_ = 4;
        float float_ = 5.5f;
        float result = (float_ * byte_);
        Logger.getGlobal().info("f * b = " + result);
        // byte_ zal worden worden 'gepromote' naar een float
    }





}
