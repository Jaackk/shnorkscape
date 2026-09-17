package com.rs.game.player.commands.impl.regularplayercommands;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.rs.game.item.Item;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@CommandInfo(rank = CommandRights.NORMAL, possibleCommands = { "testtelosdrops", "simtelos" }, description = "Simulates telos drop rates including enrage and kill streak, Usage ::simtelos (Kills 1 - 5000) (Enrage 0 - 4000) (Streak 0 - 100)")
public class TelosDropsSimulatorCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        int amountKills = 0;
        int enrage = 0;
        int streak = 0;
        try {
            amountKills = Integer.valueOf(args[1]);
            enrage = Integer.valueOf(args[2]);
            streak = Integer.valueOf(args[3]);
        } catch (Exception e) {
            player.getPackets().sendGameMessage("Incorrect Usage! Do ::testtelosdrops (Kills 1 - 5000) (Enrage 0 - 4000) (Streak 0 - 100)");
            return;
        }
        if (player.getRights() != 2 && (amountKills <= 0 || amountKills > 5000)) {
            player.getPackets().sendGameMessage("Please choose a number of kills between 1 and 5000.");
            return;
        }
        if (enrage < 0 || enrage > 4000) {
            player.getPackets().sendGameMessage("Please choose an enrage between 0 and 4000.");
            return;
        }
        if (streak < 0 || streak > 100) {
            player.getPackets().sendGameMessage("Please choose a streak between 0 and 100.");
            return;
        }
        Map<Integer, Integer> itemsArray = new HashMap<Integer, Integer>();
        for (int i = 0; i <= amountKills; i++) {
            double uniqueChance = 1 / (10000.00 / (10.00 + (0.04 * (enrage)) + (1.00 * streak)));
            double petChance = enrage >= 100 ? (1.00 / 700.00) : (1.00 / 1400.00);
            double roll = Utils.randomDouble();
            if (enrage >= 0 && enrage <= 24)
                uniqueChance += (uniqueChance / 1.50);
            else if (enrage >= 25 && enrage <= 99)
                uniqueChance += uniqueChance;
            else
                uniqueChance += (uniqueChance * 1.25);
            boolean hasPet = false;
            if (!player.getTelosRewards().isEmpty())
                for (Item item : player.getTelosRewards()) {
                    if (item == null)
                        continue;
                    if (item.getId() == 37679)
                        hasPet = true;
                }
            if (!hasPet)
                hasPet = player.getInventory().containsItem(37679, 1) || player.getBank().containsItem(new Item(37679)) || (player.getPet() != null && player.getPet().getItemId() == 37679) || itemsArray.containsKey(37679) || PetPerkUtils.getObtainedPetByItem(player, 37679) != null;
            Item reward = null;
            if (!hasPet && roll <= petChance) {
                reward = new Item(37679, 1);
            } else if (roll <= uniqueChance) {
                int rewardIndex = Utils.random(Telos.UNIQUE_DROPS.length);
                reward = new Item(Telos.UNIQUE_DROPS[rewardIndex][0], 1);
                if (reward.getId() >= 37619 && reward.getId() <= 37621) {
                    int lowest = Integer.MAX_VALUE;
                    int index = 0;
                    for (int j = 0; j < 3; j++)
                        if ((itemsArray.containsKey(37619 + j) ? itemsArray.get(37619 + j) : 0) < lowest) {
                            index = j;
                            lowest = (itemsArray.containsKey(37619 + j) ? itemsArray.get(37619 + j) : 0);
                        }
                    reward = new Item(37619 + index, 1);
                }
            } else {
                int rewardIndex = Utils.random(Telos.REGULAR_DROPS.length);
                int min = Telos.REGULAR_DROPS[rewardIndex][1];
                int max = Telos.REGULAR_DROPS[rewardIndex][2];
                int amount = (int) (min + ((max - min) * ((double) streak / 200.00)));
                reward = new Item(Telos.REGULAR_DROPS[rewardIndex][0], amount);
            }
            if (itemsArray.containsKey(reward.getId()))
                itemsArray.put(reward.getId(), itemsArray.get(reward.getId()) + reward.getAmount());
            else
                itemsArray.put(reward.getId(), reward.getAmount());
        }
        Map<Integer, Integer> treeMap = new TreeMap<Integer, Integer>(itemsArray);
        Item[] bankTabs = new Item[treeMap.size()];
        int count = 0;
        for (Integer itemId : treeMap.keySet()) {
            if (itemId == null)
                continue;
            int itema = treeMap.get(itemId);
            bankTabs[count] = new Item(itemId, itema);
            count++;
        }
        player.getInterfaceManager().closeScreenInterface();
        player.getPackets().sendItems(797, bankTabs);
        player.getTemporaryAttributtes().put("ViewDrops", Boolean.TRUE);
        player.getVarsManager().sendVarBit(32635, streak);
        player.getVarsManager().sendVarBit(32626, enrage);
        player.getPackets().sendHideIComponent(1771, 46, true);
        player.getPackets().sendHideIComponent(1771, 41, true);
        player.getPackets().sendHideIComponent(1771, 42, true);
        player.getPackets().sendHideIComponent(1771, 48, true);
        player.getPackets().sendHideIComponent(1771, 111, true);
        player.getPackets().sendHideIComponent(1771, 102, true);
        player.getPackets().sendHideIComponent(1771, 39, enrage >= 100);
        player.getPackets().sendIComponentText(1771, 16, enrage + "%");
        player.getPackets().sendHideIComponent(1771, 98, true);
        player.getPackets().sendHideIComponent(1771, 99, true);
        final int kills = amountKills;
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getInterfaceManager().sendInterface(1771);
                player.getPackets().sendExecuteScript(6269, 116064291, 116064310, 116064292, 116064311, "Loot From " + kills + " Telos Kills", 21218, 1007);
                player.setCloseInterfacesEvent(new Runnable() {

                    @Override
                    public void run() {
                        player.getVarsManager().sendVarBit(32635, player.getTelosStreak());
                        player.getVarsManager().sendVarBit(32626, player.getTelosEnrage());
                        player.getTemporaryAttributtes().remove("ViewDrops");
                    }
                });
            }
        });

    }

}
