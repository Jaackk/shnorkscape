package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.Combat;

/**
 * ataraxia-server paolo 08/09/2019 #Shnek6969
 */
@CommandInfo(rank = CommandRights.NORMAL, possibleCommands = { "maxhit" }, description = "Displays your current maxhit")
public class MaxHitCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        int mainHandMaxHit = (int) ((double) player.getCombatDefinitions().getHandDamage(false) * 2.3 * (1.0 + Settings.static_damage_buff));
        int mainHandattackStyle = player.getCombatDefinitions().getStyle(false);
        int offHandattackStyle = player.getCombatDefinitions().getStyle(true);
        int offHandMaxHit = (int) ((double) player.getCombatDefinitions().getHandDamage(true) * 2.3 * (1.0 + Settings.static_damage_buff));
        boolean isMainHand = player.getEquipment().getWeaponId() != -1 && !Equipment.isTwoHandedWeapon(new Item(player.getEquipment().getWeaponId()));
        boolean hasOffHand = player.getEquipment().getShieldId() != -1 && player.getEquipment().getItem(Equipment.SLOT_SHIELD).getDefinitions().getCombatMap() != null && offHandattackStyle != 0;

        int displayMainHandMaxHit = mainHandMaxHit / 10;   // integer division (truncates)
        int displayOffHandMaxHit  = offHandMaxHit / 10;



        if (!isMainHand && !hasOffHand) {
            player.sendMessage("Your current maximum hit with " + getCombatStyleName(mainHandattackStyle) + " is " + displayMainHandMaxHit + ".");
        } else {
            player.sendMessage("Your current main hand maximum hit with " + getCombatStyleName(mainHandattackStyle) + " is " + displayMainHandMaxHit + ".");
            if (hasOffHand) { 
                player.sendMessage("Your current off hand maximum hit with " + getCombatStyleName(offHandattackStyle) + " is " + displayOffHandMaxHit + ".");
            }
        }
    }

    public static String getCombatStyleName(int attackStyle) {
        int attackType = Combat.getStyleType(attackStyle);
        return attackType == 2 ? "magic" : attackType == 1 ? "ranged" : "melee";
    }
}
