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
public final class WoodcuttingContractList extends ContractList {

    @SuppressWarnings("incomplete-switch")
    public static void listen(Player player, Log log, int amount) {
        switch (log) {
        case NORMAL:
            player.getContracts().recordAction(Skills.WOODCUTTING, 0, amount);
            break;
        case OAK:
            player.getContracts().recordAction(Skills.WOODCUTTING, 1, amount);
            break;
        case WILLOW:
            player.getContracts().recordAction(Skills.WOODCUTTING, 2, amount);
            break;
        case TEAK:
            player.getContracts().recordAction(Skills.WOODCUTTING, 3, amount);
            break;
        case MAPLE:
            player.getContracts().recordAction(Skills.WOODCUTTING, 4, amount);
            break;
        case MAHOGANY:
            player.getContracts().recordAction(Skills.WOODCUTTING, 5, amount);
            break;
        case YEW:
            player.getContracts().recordAction(Skills.WOODCUTTING, 6, amount);
            break;
        case MAGIC:
            player.getContracts().recordAction(Skills.WOODCUTTING, 7, amount);
            break;
        case ELDER:
            player.getContracts().recordAction(Skills.WOODCUTTING, 8, amount);
            break;
        }
    }

    public WoodcuttingContractList() {
        super(Skills.WOODCUTTING, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(new SkillingContract(0, "Cut <amount> logs from regular trees.", 1, 75, 125), new SkillingContract(1, "Cut <amount> logs from oak trees.", 15, 75, 125), new SkillingContract(2, "Cut <amount> logs from willow trees.", 30, 60, 120), new SkillingContract(3, "Cut <amount> logs from teak trees.", 35, 60, 110), new SkillingContract(4, "Cut <amount> logs from maple trees.", 45, 50, 100), new SkillingContract(5, "Cut <amount> logs from mahogany trees.", 50, 50, 100), new SkillingContract(6, "Cut <amount> logs from yew trees.", 60, 40, 80), new SkillingContract(7, "Cut <amount> logs from magic trees.", 75, 30, 50), new SkillingContract(8, "Cut <amount> logs from elder trees.", 90, 20, 40), new SkillingContract(9, "Kill <amount> Evil trees.", 70, 1, 1) {
            @Override
            public boolean canAssign(Player player) {
                return player.evilTreeContracts;
            }
        });
    }
}