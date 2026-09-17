package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ConstructionContractList extends ContractList {

    public static void listen(Player player, HouseConstants.HObject object) {
        switch (object) {
            case WOODEN_CHAIR:
            case CRUDE_WOODEN_CHAIR:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 0);
                break;
            case WOODEN_LARDER:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 1);
                break;
            case WOOD_BENCH:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 2);
                break;
            case WOOD_DINING:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 3);
                break;
            case WOODEN_KITCHEN_TABLE:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 4);
                break;
            case OAK_CHAIR:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 5);
                break;
            case OAK_DINING:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 6);
                break;
            case OAK_BENCH:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 7);
                break;
            case OAK_KITCHEN_TABLE:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 8);
                break;
            case OAK_LARDER:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 9);
                break;
            case TEAK_BENCH:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 10);
                break;
            case TEAK_DINING:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 11);
                break;
            case TEAK_LARDER:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 12);
                break;
            case TEAK_KITCHEN_TABLE:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 13);
                break;
            case MAHOGANY_BENCH:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 14);
                break;
            case MAHOGANY_DINING:
                player.getContracts().recordAction(Skills.CONSTRUCTION, 15);
                break;
        }
    }

    public ConstructionContractList() {
        super(Skills.CONSTRUCTION, true);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Build <amount> (crude) wooden chairs.", 1, 30, 50),
                new SkillingContract(1, "Build <amount> wooden larders.", 9, 25, 40),
                new SkillingContract(2, "Build <amount> wooden benches.", 10, 25, 40),
                new SkillingContract(3, "Build <amount> wooden dining tables.", 10, 30, 45),
                new SkillingContract(4, "Build <amount> wooden kitchen tables.", 12, 30, 45),
                new SkillingContract(5, "Build <amount> oak chairs.", 19, 25, 40),
                new SkillingContract(6, "Build <amount> oak dining tables.", 22, 30, 45),
                new SkillingContract(7, "Build <amount> oak benches.", 22, 25, 40),
                new SkillingContract(8, "Build <amount> oak kitchen tables.", 32, 30, 45),
                new SkillingContract(9, "Build <amount> oak larders.", 33, 25, 40),
                new SkillingContract(10, "Build <amount> teak dining benches.", 38, 25, 40),
                new SkillingContract(11, "Build <amount> teak dining tables.", 38, 30, 45),
                new SkillingContract(12, "Build <amount> teak larders.", 43, 25, 40),
                new SkillingContract(13, "Build <amount> teak kitchen tables.", 52, 30, 45),
                new SkillingContract(14, "Build <amount> mahogany benches.", 52, 25, 45),
                new SkillingContract(15, "Build <amount> mahogany dining tables.", 52, 30, 45, true)
        );
    }
}