package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.FarmingManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.impl.DivineHerblore;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FarmingContractList extends ContractList {

    public static void listen(Player player, FarmingManager.ProductInfo info) {
        switch (info) {
            case Potato:
                player.getContracts().recordAction(Skills.FARMING, 0);
                break;
            case Guam:
                player.getContracts().recordAction(Skills.FARMING, 1);
                break;
            case Marrentill:
                player.getContracts().recordAction(Skills.FARMING, 2);
                break;
            case Tarromin:
                player.getContracts().recordAction(Skills.FARMING, 3);
                break;
            case Harralander:
                player.getContracts().recordAction(Skills.FARMING, 4);
                break;
            case Rannar:
                player.getContracts().recordAction(Skills.FARMING, 5);
                break;
            case Toadflax:
                player.getContracts().recordAction(Skills.FARMING, 6);
                break;
            case Irit:
                player.getContracts().recordAction(Skills.FARMING, 7);
                break;
            case Avantoe:
                player.getContracts().recordAction(Skills.FARMING, 8);
                break;
            case Kwuarm:
                player.getContracts().recordAction(Skills.FARMING, 9);
                break;
            case Snapdragon:
                player.getContracts().recordAction(Skills.FARMING, 10);
                break;
            case Cadantine:
                player.getContracts().recordAction(Skills.FARMING, 11);
                break;
            case Lantadyme:
                player.getContracts().recordAction(Skills.FARMING, 12);
                break;
            case Dwarf:
                player.getContracts().recordAction(Skills.FARMING, 13);
                break;
            case Torstol:
                player.getContracts().recordAction(Skills.FARMING, 14);
                break;
            case Fellstalk:
                player.getContracts().recordAction(Skills.FARMING, 15);
                break;
        }
    }
    public static void listen(Player player, DivineHerblore.Herbs herbs) {
        switch (herbs) {
            case GUAM:
                player.getContracts().recordAction(Skills.FARMING, 1);
                break;
            case MARRENTILL:
                player.getContracts().recordAction(Skills.FARMING, 2);
                break;
            case TARRONMIN:
                player.getContracts().recordAction(Skills.FARMING, 3);
                break;
            case HARRALANDER:
                player.getContracts().recordAction(Skills.FARMING, 4);
                break;
            case RANARR:
                player.getContracts().recordAction(Skills.FARMING, 5);
                break;
            case TOADFLAX:
                player.getContracts().recordAction(Skills.FARMING, 6);
                break;
            case IRIT:
                player.getContracts().recordAction(Skills.FARMING, 7);
                break;
            case AVANTOE:
                player.getContracts().recordAction(Skills.FARMING, 8);
                break;
            case KWUARM:
                player.getContracts().recordAction(Skills.FARMING, 9);
                break;
            case SNAPDRAGON:
                player.getContracts().recordAction(Skills.FARMING, 10);
                break;
            case CADANTINE:
                player.getContracts().recordAction(Skills.FARMING, 11);
                break;
            case LANTADYME:
                player.getContracts().recordAction(Skills.FARMING, 12);
                break;
            case DWARF_WEED:
                player.getContracts().recordAction(Skills.FARMING, 13);
                break;
            case TORSTOL:
                player.getContracts().recordAction(Skills.FARMING, 14);
                break;
            case FELLSTALK:
                player.getContracts().recordAction(Skills.FARMING, 15);
                break;
        }
    }
    public FarmingContractList() {
        super(Skills.FARMING, false);
    }

    @Override
    public ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Harvest <amount> potatoes.", 1, 10, 25),
                new SkillingContract(1, "Harvest <amount> guam herbs.", 9, 10, 25),
                new SkillingContract(2, "Harvest <amount> marrentill herbs.", 14, 10, 25),
                new SkillingContract(3, "Harvest <amount> tarromin herbs.", 19, 10, 25),
                new SkillingContract(4, "Harvest <amount> harralander herbs.", 26, 10, 25),
                new SkillingContract(5, "Harvest <amount> ranarr herbs.", 32, 10, 25),
                new SkillingContract(6, "Harvest <amount> toadflax herbs.", 38, 10, 25),
                new SkillingContract(7, "Harvest <amount> irit herbs.", 44, 10, 25),
                new SkillingContract(8, "Harvest <amount> avantoe herbs.", 50, 10, 25),
                new SkillingContract(9, "Harvest <amount> kwuarm herbs.", 54, 10, 25),
                new SkillingContract(10, "Harvest <amount> snapdragon herbs.", 62, 10, 25),
                new SkillingContract(11, "Harvest <amount> cadantine herbs.", 67, 10, 25),
                new SkillingContract(12, "Harvest <amount> lantadyme herbs.", 73, 10, 25),
                new SkillingContract(13, "Harvest <amount> dwarf weed herbs.", 79, 10, 25),
                new SkillingContract(14, "Harvest <amount> torstol herbs.", 85, 10, 25),
                new SkillingContract(15, "Harvest <amount> fellstalk herbs.", 91, 10, 25)
        );
    }
}