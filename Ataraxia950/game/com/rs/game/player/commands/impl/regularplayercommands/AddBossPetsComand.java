package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.pet.Pets;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"addbosspets", "refreshcol"},
        description = "searches ur account for pets to add to collection log"
        )
public class AddBossPetsComand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        boolean foundAnyPets = false;
        for (DropCollectionConstants.BOSS_DATA data : DropCollectionConstants.BOSS_DATA.values()) {
            for (Item i : data.getDrops()) {
                Pets pet = Pets.forId(i.getId());
                if (pet != null && player.hasItem(i.getId()) && !player.getDropCollectionHandler().hasItemInCollectionLog(i, data.getNpcId())) {
                    player.getDropCollectionHandler().handleBossKills(i, data.getNpcId());
                    player.sm("Boss pet found and added: " + i.getDefinitions().name);
                    foundAnyPets = true;
                }
            }
        }
        for (DropCollectionConstants.MINIGAME_DATA data : DropCollectionConstants.MINIGAME_DATA.values()) {
            for (Item i : data.getDrops()) {
                Pets pet = Pets.forId(i.getId());
                if (pet != null && player.hasItem(i.getId()) && !player.getDropCollectionHandler().hasItemInMinigameCollectionLog(data, i.getId())) {
                    player.getDropCollectionHandler().handleMinigames(data, i.getId());
                    player.sm("Minigame pet found and added: " + i.getDefinitions().name);
                    foundAnyPets = true;
                }
            }
        }
        if (!foundAnyPets) {
            player.sm("No changes found for collection log.");
        }
    }
}
