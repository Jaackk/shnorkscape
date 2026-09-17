package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FiremakingContractList extends ContractList {

    public static void listen(Player player, Log log) {
        switch (log) {
            case NORMAL:
                player.getContracts().recordAction(Skills.FIREMAKING, 0);
                break;
            case OAK:
                player.getContracts().recordAction(Skills.FIREMAKING, 1);
                break;
            case WILLOW:
                player.getContracts().recordAction(Skills.FIREMAKING, 2);
                break;
            case MAPLE:
                player.getContracts().recordAction(Skills.FIREMAKING, 3);
                break;
            case TEAK:
                player.getContracts().recordAction(Skills.FIREMAKING, 4);
                break;
            case MAHOGANY:
                player.getContracts().recordAction(Skills.FIREMAKING, 5);
                break;
            case YEW:
                player.getContracts().recordAction(Skills.FIREMAKING, 6);
                break;
            case MAGIC:
                player.getContracts().recordAction(Skills.FIREMAKING, 7);
                break;
            case ELDER:
                player.getContracts().recordAction(Skills.FIREMAKING, 8);
                break;
        }
    }

    public FiremakingContractList() {
        super(Skills.FIREMAKING, true);
    }

    @Override
    public ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Light <amount> regular logs on fire.", 1, 100, 200),
                new SkillingContract(1, "Light <amount> oak logs on fire.", 15, 100, 200),
                new SkillingContract(2, "Light <amount> willow logs on fire.", 30, 100,200),
                new SkillingContract(3, "Light <amount> maple logs on fire.", 45, 100, 200),
                new SkillingContract(4, "Light <amount> teak logs on fire.", 35, 100, 200),
                new SkillingContract(5, "Light <amount> mahogany logs on fire.", 50, 100, 200),
                new SkillingContract(6, "Light <amount> yew logs on fire.", 60, 100, 200),
                new SkillingContract(7, "Light <amount> magic logs on fire.", 75, 100, 200),
                new SkillingContract(8, "Light <amount> elder logs on fire.", 90, 100, 200),
                new SkillingContract(9, "Kill <amount> Evil Trees.", 70, 3, 10) {
                    @Override
                    public boolean canAssign(Player player) {
                        return player.evilTreeContracts;
                    }
                }
        );
    }
}