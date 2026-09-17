package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.crafting.GemCutting;
import com.rs.game.player.actions.crafting.LeatherCrafting;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CraftingContractList extends ContractList {

        public static void listenArmor(Player player, LeatherCrafting.LeatherData data) {
        switch (data) {
            case GREEN_D_HIDE_BODY:
                player.getContracts().recordAction(Skills.CRAFTING, 9);
                break;
            case BLUE_D_HIDE_BODY:
                player.getContracts().recordAction(Skills.CRAFTING, 10);
                break;
            case RED_D_HIDE_BODY:
                player.getContracts().recordAction(Skills.CRAFTING, 11);
                break;
            case BLACK_D_HIDE_BODY:
                player.getContracts().recordAction(Skills.CRAFTING, 12);
                break;
        }
    }

    public static void listenGem(Player player, GemCutting.Gem gem) {
        switch (gem) {
            case OPAL:
                player.getContracts().recordAction(Skills.CRAFTING, 0);
                break;
            case JADE:
                player.getContracts().recordAction(Skills.CRAFTING, 1);
                break;
            case RED_TOPAZ:
                player.getContracts().recordAction(Skills.CRAFTING, 2);
                break;
            case SAPPHIRE:
                player.getContracts().recordAction(Skills.CRAFTING, 3);
                break;
            case EMERALD:
                player.getContracts().recordAction(Skills.CRAFTING, 4);
                break;
            case RUBY:
                player.getContracts().recordAction(Skills.CRAFTING, 5);
                break;
            case DIAMOND:
                player.getContracts().recordAction(Skills.CRAFTING, 6);
                break;
            case DRAGONSTONE:
                player.getContracts().recordAction(Skills.CRAFTING, 7);
                break;
            case ONYX:
                player.getContracts().recordAction(Skills.CRAFTING, 8);
                break;
        }
    }

    public CraftingContractList() {
        super(Skills.CRAFTING, true);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Cut <amount> opal gems.", 1, 90, 150),
                new SkillingContract(1, "Cut <amount> jade gems.", 13, 90, 150),
                new SkillingContract(2, "Cut <amount> red topaz gems.", 16, 90, 150),
                new SkillingContract(3, "Cut <amount> sapphire gems.", 20, 70, 130),
                new SkillingContract(4, "Cut <amount> emerald gems.", 27, 70, 130),
                new SkillingContract(5, "Cut <amount> ruby gems.", 34, 70, 130),
                new SkillingContract(6, "Cut <amount> diamond gems.", 43, 70, 130),
                new SkillingContract(7, "Cut <amount> dragonstone gems.", 55, 50, 110, true),
                new SkillingContract(8, "Cut <amount> onyx gems.", 72, 5, 15),

                new SkillingContract(9, "Craft <amount> green d'hide bodies.", 63, 40, 60),
                new SkillingContract(10, "Craft <amount> blue d'hide bodies.", 71, 35, 55, false),
                new SkillingContract(11, "Craft <amount> red d'hide bodies.", 77, 30, 50, false),
                new SkillingContract(12, "Craft <amount> black d'hide bodies.", 84, 25, 45)
        );
    }
}