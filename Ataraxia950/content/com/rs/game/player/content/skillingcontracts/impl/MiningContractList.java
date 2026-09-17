package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class MiningContractList extends ContractList {

    @SuppressWarnings("incomplete-switch")
    public static void listen(Player player, RockDefinitions ore, int amount) {
        switch (ore) {
            case Tin_Ore:
                player.getContracts().recordAction(Skills.MINING, 0, amount);
                break;
            case Copper_Ore:
                player.getContracts().recordAction(Skills.MINING, 1, amount);
                break;
            case Iron_Ore:
                player.getContracts().recordAction(Skills.MINING, 2, amount);
                break;
            case Coal_Ore:
            case LRC_Coal_Ore:
                player.getContracts().recordAction(Skills.MINING, 3, amount);
                break;
            case Gold_Ore:
            case LRC_Gold_Ore:
                player.getContracts().recordAction(Skills.MINING, 4, amount);
                break;
            case Mithril_Ore:
                player.getContracts().recordAction(Skills.MINING, 5, amount);
                break;
            case Adamant_Ore:
                player.getContracts().recordAction(Skills.MINING, 6, amount);
                break;
            case Runite_Ore:
                player.getContracts().recordAction(Skills.MINING, 7, amount);
                break;
            case SEREN_STONE:
                player.getContracts().recordAction(Skills.MINING, 8, amount);
                break;
        }
    }

    public MiningContractList() {
        super(Skills.MINING, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Mine <amount> tin ore.", 1, 45, 90),
                new SkillingContract(1, "Mine <amount> copper ore.", 1, 45, 90),
                new SkillingContract(2, "Mine <amount> iron ore.", 15, 45, 80),
                new SkillingContract(3, "Mine <amount> coal.", 30, 40, 80),
                new SkillingContract(4, "Mine <amount> gold ore.", 40, 40, 80),
                new SkillingContract(5, "Mine <amount> mithril ore.", 55, 35, 65),
                new SkillingContract(6, "Mine <amount> adamant ore.", 70, 30, 50),
                new SkillingContract(7, "Mine <amount> runite ore.", 85, 15, 35),
                new SkillingContract(8, "Mine <amount> corrupted ore.", 89, 15, 35) {
                    @Override
                    public boolean canAssign(Player player) {
                        return player.getMoneySpent() >= 250 || player.getSkills().getTotalLevel() >= 2250 || player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND);
                    }
                }
        );
    }
}