package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class HunterContractList extends ContractList {

    public static void listen(Player player, TrapAction.HunterNPC hunted, int amount) {
        switch (hunted) {
            case CRIMSON_SWIFT:
                player.getContracts().recordAction(Skills.HUNTER, 0, amount);
                break;
            case TROPICAL_WAGTAIL:
                player.getContracts().recordAction(Skills.HUNTER, 1, amount);
                break;
            case COBALT_SKILLCHOMPA:
                player.getContracts().recordAction(Skills.HUNTER, 2, amount);
                break;
            case VIRIDIAN_SKILLCHOMPA:
                player.getContracts().recordAction(Skills.HUNTER, 3, amount);
                break;
            case AZURE_SKILLCHOMPA:
                player.getContracts().recordAction(Skills.HUNTER, 4, amount);
                break;
            case RED_CHINCHOMPA:
                player.getContracts().recordAction(Skills.HUNTER, 5, amount);
                break;
            case GRENWALL:
                player.getContracts().recordAction(Skills.HUNTER, 6, amount);
                break;
            case CRIMSON_SKILLCHOMPA:
                player.getContracts().recordAction(Skills.HUNTER, 7, amount);
                break;
        }
    }

    public HunterContractList() {
        super(Skills.HUNTER, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Catch <amount> crimson swifts.", 1, 25, 50),
                new SkillingContract(1, "Catch <amount> tropical wagtails.", 19, 25, 50),
                new SkillingContract(2, "Catch <amount> cobalt skillchompas.", 27, 25, 50),
                new SkillingContract(3, "Catch <amount> viridian skillchompas.", 46, 25, 50),
                new SkillingContract(4, "Catch <amount> azure skillchompas.", 68, 25, 50),
                new SkillingContract(5, "Catch <amount> red chinchompas.", 63, 25, 50, true),
                new SkillingContract(6, "Catch <amount> grenwalls.", 77, 25, 50),
                new SkillingContract(7, "Catch <amount> crimson skillchompas.", 89, 15, 30)
        );
    }
}