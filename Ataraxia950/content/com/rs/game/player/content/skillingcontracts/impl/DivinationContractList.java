package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.WispInfo;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DivinationContractList extends ContractList {

    public static void listen(Player player, WispInfo info) {
        switch (info) {
            case PALE:
                player.getContracts().recordAction(Skills.DIVINATION, 0);
                break;
            case FLICKERING:
                player.getContracts().recordAction(Skills.DIVINATION, 1);
                break;
            case BRIGHT:
                player.getContracts().recordAction(Skills.DIVINATION, 2);
                break;
            case GLOWING:
                player.getContracts().recordAction(Skills.DIVINATION, 3);
                break;
            case SPARKLING:
                player.getContracts().recordAction(Skills.DIVINATION, 4);
                break;
            case GLEAMING:
                player.getContracts().recordAction(Skills.DIVINATION, 5);
                break;
            case VIBRANT:
                player.getContracts().recordAction(Skills.DIVINATION, 6);
                break;
            case LUSTROUS:
                player.getContracts().recordAction(Skills.DIVINATION, 7);
                break;
            case BRILLIANT:
                player.getContracts().recordAction(Skills.DIVINATION, 8);
                break;
            case RADIANT:
                player.getContracts().recordAction(Skills.DIVINATION, 9);
                break;
            case LUMINOUS:
                player.getContracts().recordAction(Skills.DIVINATION, 10);
                break;
            case INCANDESCENT:
                player.getContracts().recordAction(Skills.DIVINATION, 11);
                break;
        }
    }

    public DivinationContractList() {
        super(Skills.DIVINATION, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Harvest <amount> Pale memories.", 1, 75, 150),
                new SkillingContract(1, "Harvest <amount> Flickering memories.", 10, 75, 150),
                new SkillingContract(2, "Harvest <amount> Bright memories.", 20, 75, 150),
                new SkillingContract(3, "Harvest <amount> Glowing memories.", 30, 75, 150),
                new SkillingContract(4, "Harvest <amount> Sparkling memories.", 40, 75, 150),
                new SkillingContract(5, "Harvest <amount> Gleaming memories.", 50, 75, 150),
                new SkillingContract(6, "Harvest <amount> Vibrant memories.", 60, 75, 150),
                new SkillingContract(7, "Harvest <amount> Lustrous memories.", 70, 75, 150, false),
                new SkillingContract(8, "Harvest <amount> Brilliant memories.", 80, 75, 150, false),
                new SkillingContract(9, "Harvest <amount> Radiant memories.", 85, 75, 150, false),
                new SkillingContract(10, "Harvest <amount> Luminous memories.", 90, 75, 150),
                new SkillingContract(11, "Harvest <amount> Incandescent memories.", 95, 75, 150));
    }
}