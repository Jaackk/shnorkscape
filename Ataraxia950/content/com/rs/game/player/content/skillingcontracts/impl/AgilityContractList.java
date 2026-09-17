package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AgilityContractList extends ContractList {

    public AgilityContractList() {
        super(Skills.AGILITY, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Complete <amount> laps at the beginner gnome agility course. ", 1, 10, 20),
                new SkillingContract(1, "Complete <amount> laps at the agility pyramid.", 50, 10, 20),
                new SkillingContract(2, "Complete <amount> laps at the barbarian outpost agility course.", 35, 10, 20),
                new SkillingContract(3, "Complete <amount> laps at the wilderness agility course.", 52, 10, 20),
                new SkillingContract(4, "Complete <amount> laps at the advanced gnome agility course.", 85, 10, 20),
                new SkillingContract(5, "Complete <amount> laps at the priffdinas agility course.", 77, 10, 20) {
                    @Override
                    public boolean canAssign(Player player) {
                        return player.hasAccessToPrifddinas();
                    }
                }
        );
    }
}