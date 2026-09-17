package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FishingContractList extends ContractList {

    public static void listen(Player player, Fishing.Fish fish, int amount) {
        switch (fish) {
            case SHRIMP:
            case ANCHOVIES:
                player.getContracts().recordAction(Skills.FISHING, 0, amount);
                break;
            case HERRING:
            case SARDINES:
                player.getContracts().recordAction(Skills.FISHING, 1, amount);
                break;
            case TROUT:
            case SALMON:
                player.getContracts().recordAction(Skills.FISHING, 3, amount);
                break;
            case PIKE:
                player.getContracts().recordAction(Skills.FISHING, 2, amount);
                break;
            case TUNA:
            case SWORDFISH:
                player.getContracts().recordAction(Skills.FISHING, 4, amount);
                break;
            case LOBSTER:
                player.getContracts().recordAction(Skills.FISHING, 5, amount);
                break;
            case MONKFISH:
                player.getContracts().recordAction(Skills.FISHING, 6, amount);
                break;
            case SHARK:
                player.getContracts().recordAction(Skills.FISHING, 7, amount);
                break;
            case ROCKTAIL:
                player.getContracts().recordAction(Skills.FISHING, 8, amount);
                break;
            case GREAT_WHITE_SHARK:
                player.getContracts().recordAction(Skills.FISHING, 9, amount);
                break;
        }
    }

    public FishingContractList() {
        super(Skills.FISHING, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Catch <amount> raw shrimps or anchovies.", 1, 50, 120),
                new SkillingContract(1, "Catch <amount> raw sardines or herrings.", 5, 30, 50),
                new SkillingContract(2, "Catch <amount> raw pike.", 25, 30, 50),
                new SkillingContract(3, "Catch <amount> raw trout or salmon.", 30, 50, 90),
                new SkillingContract(4, "Catch <amount> raw tuna or swordfish.", 35, 50, 90),
                new SkillingContract(5, "Catch <amount> raw lobster.", 40, 50, 120),
                new SkillingContract(6, "Catch <amount> raw monkfish.", 62, 50, 120),
                new SkillingContract(7, "Catch <amount> raw sharks.", 76, 50, 120),
                new SkillingContract(8, "Catch <amount> raw rocktails.", 90, 50, 120),
                new SkillingContract(9, "Catch <amount> raw great white sharks.", 80, 50, 120) {
                    @Override
                    public boolean canAssign(Player player) {
                        return player.isDiamondDonor() && !player.isGroupIronman();
                    }
                }
        );
    }
}