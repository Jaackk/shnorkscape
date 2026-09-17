package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CookingContractList extends ContractList {

    public static void listen(Player player, Cooking.Cookables cook) {
        switch (cook) {
            case RAW_SHRIMP:
                player.getContracts().recordAction(Skills.COOKING, 0);
                break;
            case RAW_SARDINE:
                player.getContracts().recordAction(Skills.COOKING, 1);
                break;
            case RAW_ANCHOVIES:
                player.getContracts().recordAction(Skills.COOKING, 2);
                break;
            case RAW_TROUT:
                player.getContracts().recordAction(Skills.COOKING, 3);
                break;
            case RAW_PIKE:
                player.getContracts().recordAction(Skills.COOKING, 4);
                break;
            case RAW_SALMON:
                player.getContracts().recordAction(Skills.COOKING, 5);
                break;
            case RAW_TUNA:
                player.getContracts().recordAction(Skills.COOKING, 6);
                break;
            case RAW_LOBSTER:
                player.getContracts().recordAction(Skills.COOKING, 7);
                break;
            case RAW_SWORDFISH:
                player.getContracts().recordAction(Skills.COOKING, 8);
                break;
            case RAW_MONKFISH:
                player.getContracts().recordAction(Skills.COOKING, 9);
                break;
            case RAW_SHARK:
                player.getContracts().recordAction(Skills.COOKING, 10);
                break;
            case RAW_ROCKTAIL:
                player.getContracts().recordAction(Skills.COOKING, 11);
                break;
            case GREAT_WHITE_RAW_SHARK:
                player.getContracts().recordAction(Skills.COOKING, 12);
                break;
        }
    }

    public CookingContractList() {
        super(Skills.COOKING, true);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Cook <amount> raw shrimps.", 1, 100, 200),
                new SkillingContract(1, "Cook <amount> raw sardines.", 1, 100, 200),
                new SkillingContract(2, "Cook <amount> raw anchovies.", 1, 100, 200),
                new SkillingContract(3, "Cook <amount> raw trout.", 15, 100, 200),
                new SkillingContract(4, "Cook <amount> raw pike.", 20, 100, 200),
                new SkillingContract(5, "Cook <amount> raw salmon.", 25, 100, 200),
                new SkillingContract(6, "Cook <amount> raw tuna.", 30, 100, 200),
                new SkillingContract(7, "Cook <amount> raw lobster.", 40, 100, 200),
                new SkillingContract(8, "Cook <amount> raw swordfish.", 45, 100, 200),
                new SkillingContract(9, "Cook <amount> raw monkfish.", 62, 100, 200),
                new SkillingContract(10, "Cook <amount> raw sharks.", 80, 100, 200),
                new SkillingContract(11, "Cook <amount> raw rocktails.", 92, 100, 200),
                new SkillingContract(12, "Cook <amount> raw great white sharks.", 84, 100, 200, true) {
                    @Override
                    public boolean canAssign(Player player) {
                        return player.isDiamondDonor() && !player.isGroupIronman();
                    }
                }
        );
    }
}