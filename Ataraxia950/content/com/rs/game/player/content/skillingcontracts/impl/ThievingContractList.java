package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.actions.thieving.def.Stalls;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ThievingContractList extends ContractList {

    @SuppressWarnings("incomplete-switch")
    public static void listenPickpocket(Player player, PickPocketableNPC npc, int amount) {
        switch (npc) {
        case GUARD:
            player.getContracts().recordAction(Skills.THIEVING, 5, amount);
            break;
        case ARDOUGNE_KNIGHT:
            player.getContracts().recordAction(Skills.THIEVING, 6, amount);
            break;
        case PALADIN:
            player.getContracts().recordAction(Skills.THIEVING, 8, amount);
            break;
        case HERO:
            player.getContracts().recordAction(Skills.THIEVING, 9, amount);
            break;
        case DWARF_TRADER:
            player.getContracts().recordAction(Skills.THIEVING, 14, amount);
            break;
        }
    }

    @SuppressWarnings("incomplete-switch")
    public static void listenStall(Player player, Stalls stalls) {
        switch (stalls) {
        case CRAFTING:
            player.getContracts().recordAction(Skills.THIEVING, 0);
            break;
        case CAKE:
            player.getContracts().recordAction(Skills.THIEVING, 1);
            break;
        case SILK_STALL:
            player.getContracts().recordAction(Skills.THIEVING, 2);
            break;
        case MONKEY_FOOD:
            player.getContracts().recordAction(Skills.THIEVING, 3);
            break;
        case FUR_STALL:
            player.getContracts().recordAction(Skills.THIEVING, 4);
            break;
        case MONKEY_GENERAL:
            player.getContracts().recordAction(Skills.THIEVING, 7);
            break;
        case MAGIC_STALL:
            player.getContracts().recordAction(Skills.THIEVING, 10);
            break;
        case SCIMITAR_STALL:
            player.getContracts().recordAction(Skills.THIEVING, 11);
            break;
        }
    }

    public ThievingContractList() {
        super(Skills.THIEVING, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(new SkillingContract(0, "Steal from the Crafting Stall <amount> times.", 1, 75, 125), new SkillingContract(1, "Steal from the Baker's Stall <amount> times.", 5, 75, 125), new SkillingContract(2, "Steal from the Silk Stall <amount> times.", 20, 75, 125), new SkillingContract(3, "Steal from the Food Stall <amount> times.", 30, 75, 125), new SkillingContract(4, "Steal from the Fur Stall <amount> times.", 35, 75, 125), new SkillingContract(5, "Pickpocket Guards <amount> times.", 40, 75, 125), new SkillingContract(6, "Pickpocket Knights of Ardougne <amount> times.", 55, 75, 125), new SkillingContract(7, "Steal from the General Stall <amount> times.", 65, 75, 125), new SkillingContract(8, "Pickpocket Paladins <amount> times.", 70, 75, 125, false), new SkillingContract(9, "Pickpocket Heroes <amount> times.", 80, 75, 125), new SkillingContract(10, "Steal from the Magic Stall <amount> times.", 85, 75, 125, false), new SkillingContract(11, "Steal from the Scimitar Stall <amount> times.", 95, 75, 125), new SkillingContract(12, "Steal from the Gem Stall <amount> times.", 50, 75, 125, true) {
            @Override
            public boolean canAssign(Player player) {
                return !player.isGroupIronman() && player.getMoneySpent() >= 25;
            }
        }, new SkillingContract(13, "Steal from the Diamond Stall <amount> times.", 98, 75, 125) {
            @Override
            public boolean canAssign(Player player) {
                return player.isDiamondDonor();
            }
        }, new SkillingContract(14, "Pickpocket Dwarf Traders <amount> times.", 90, 75, 125));
    }
}