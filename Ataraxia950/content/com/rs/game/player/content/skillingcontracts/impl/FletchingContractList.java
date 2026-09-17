package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FletchingContractList extends ContractList {

    public static void listen(Player player, int id) {
        switch (id) {
            case 892: // Fletch rune arrows (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 14, 15);
                break;
            case 890: // Fletch adamant arrows (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 15, 15);
                break;
            case 811: // Fletch rune darts (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 17, 15);
                break;
            case 810: // Fletch adamant darts (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 18, 15);
                break;
            case 9144: // Fletch rune bolts (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 16, 15);
                break;
            case 9341: // Fletch dragonstone bolts (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 19, 15);
                break;
            case 9342: // Fletch onyx bolts (x15).
                player.getContracts().recordAction(Skills.FLETCHING, 20, 15);
                break;
            case 48: // Fletch shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 0);
                break;
            case 839: // String shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 1);
                break;
            case 56: // Fletch oak shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 2);
                break;
            case 845: // String oak shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 3);
                break;
            case 58: // Fletch willow shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 4);
                break;
            case 847: // String willow shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 5);
                break;
            case 62: // Fletch maple shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 6);
                break;
            case 851: // String maple shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 7);
                break;
            case 66: // Fletch yew shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 8);
                break;
            case 855: // String yew shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 9);
                break;
            case 70: // Fletch magic shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 10);
                break;
            case 859: // String magic shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 11);
                break;
            case 29734: // Fletch elder shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 12);
                break;
            case 29611: // String elder shieldbow.
                player.getContracts().recordAction(Skills.FLETCHING, 13);
                break;
        }
    }

    public FletchingContractList() {
        super(Skills.FLETCHING, true);
    }

    @Override
    public ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Fletch <amount> shieldbow (u)'s.", 1, 100, 200),
                new SkillingContract(1, "String <amount> shieldbow (u)'s.", 1, 100, 200),
                new SkillingContract(2, "Fletch <amount> oak shieldbow (u)'s.", 20, 100, 200),
                new SkillingContract(3, "String <amount> oak shieldbow (u)'s.", 20, 100, 200),
                new SkillingContract(4, "Fletch <amount> willow shieldbow (u)'s.", 35, 100, 200),
                new SkillingContract(5, "String <amount> willow shieldbow (u)'s.", 35, 100, 200),
                new SkillingContract(6, "Fletch <amount> maple shieldbow (u)'s.", 50, 100, 200),
                new SkillingContract(7, "String <amount> maple shieldbow (u)'s.", 50, 100, 200),
                new SkillingContract(8, "Fletch <amount> yew shieldbow (u)'s.", 70, 100, 200, false),
                new SkillingContract(9, "String <amount> yew shieldbow (u)'s.", 70, 100, 200, false),
                new SkillingContract(10, "Fletch <amount> magic shieldbow (u)'s.", 85, 100, 200),
                new SkillingContract(11, "String <amount> magic shieldbow (u)'s.", 85, 100, 200),
                new SkillingContract(12, "Fletch <amount> elder shieldbow (u)'s.", 95, 100, 200),
                new SkillingContract(13, "String <amount> elder shieldbow (u)'s.", 95, 100, 200),
                new SkillingContract(14, "Fletch <amount> rune arrows.", 75, 350, 700),
                new SkillingContract(15, "Fletch <amount> adamant arrows.", 60, 350, 700),
                new SkillingContract(16, "Feather <amount> rune bolts (unf).", 69, 350, 700, true),
                new SkillingContract(17, "Fletch <amount> rune darts.", 81, 350, 700),
                new SkillingContract(18, "Fletch <amount> adamant darts.", 67, 350, 700),
                new SkillingContract(19, "Tip <amount> rune bolts with dragonstone bolt tips.", 71, 200, 500),
                new SkillingContract(20, "Tip <amount> rune bolts with onyx bolt tips.", 73, 100, 200)
        );
    }
}