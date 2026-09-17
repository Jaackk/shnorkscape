package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class HerbloreContractList extends ContractList {

    public static void listenComplete(Player player, int potionId) {
        switch (potionId) {
            case 121: // Attack potion.
                player.getContracts().recordAction(Skills.HERBLORE, 0);
                break;
            case 115: // Strength potion.
                player.getContracts().recordAction(Skills.HERBLORE, 1);
                break;
            case 133: // Defence potion.
                player.getContracts().recordAction(Skills.HERBLORE, 2);
                break;
            case 3010: //  Energy potion.
                player.getContracts().recordAction(Skills.HERBLORE, 3);
                break;
            case 139: // Prayer potion.
                player.getContracts().recordAction(Skills.HERBLORE, 4);
                break;
            case 6687: // Saradomin brew.
                player.getContracts().recordAction(Skills.HERBLORE, 5);
                break;
            case 145: // Super attack potion.
                player.getContracts().recordAction(Skills.HERBLORE, 6);
                break;
            case 3018: // Super energy potion.
                player.getContracts().recordAction(Skills.HERBLORE, 7);
                break;
            case 157: // Super strength potion.
                player.getContracts().recordAction(Skills.HERBLORE, 8);
                break;
            case 3026: // Super restore potion.
                player.getContracts().recordAction(Skills.HERBLORE, 9);
                break;
            case 163: // Super defence potion.
                player.getContracts().recordAction(Skills.HERBLORE, 10);
                break;
            case 2454: // Antifire potion.
                player.getContracts().recordAction(Skills.HERBLORE, 11);
                break;
            case 169: // Super ranging potion.
                player.getContracts().recordAction(Skills.HERBLORE, 12);
                break;
            case 189: // Zamorak brew.
                player.getContracts().recordAction(Skills.HERBLORE, 13);
                break;
            case 21632: // Prayer renewal potion.
                player.getContracts().recordAction(Skills.HERBLORE, 14);
                break;
            case 15309: // Extreme attack potion.
                player.getContracts().recordAction(Skills.HERBLORE, 15);
                break;
            case 15312:
            case 15313: // Extreme strength potion.
            case 15314:
            case 115315:
                player.getContracts().recordAction(Skills.HERBLORE, 16);
                break;
            case 15317: // Extreme defence potion.
                player.getContracts().recordAction(Skills.HERBLORE, 17);
                break;
            case 15321: // Extreme magic potion.
                player.getContracts().recordAction(Skills.HERBLORE, 18);
                break;
            case 15325: // Extreme ranging potion.
                player.getContracts().recordAction(Skills.HERBLORE, 19);
                break;
            case 15332: // Overload potion.
                player.getContracts().recordAction(Skills.HERBLORE, 20);
                break;
        }
    }

    public HerbloreContractList() {
        super(Skills.HERBLORE, true);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                new SkillingContract(0, "Make <amount> attack potions.", 1, 100, 200),
                new SkillingContract(1, "Make <amount> strength potions.", 7, 100, 200),
                new SkillingContract(2, "Make <amount> defence potions.", 9, 100, 200),
                new SkillingContract(3, "Make <amount> energy potions.", 26, 100, 200),
                new SkillingContract(4, "Make <amount> prayer potions.", 38, 100, 200),
                new SkillingContract(5, "Make <amount> saradomin brews.", 81, 100, 200),
                new SkillingContract(6, "Make <amount> super attack potions.", 45, 100, 200),
                new SkillingContract(7, "Make <amount> super energy potions.", 52, 100, 200),
                new SkillingContract(8, "Make <amount> super strength potions.", 55, 100, 200),
                new SkillingContract(9, "Make <amount> super restore potions.", 63, 100, 200, true),
                new SkillingContract(10, "Make <amount> super defence potions.", 66, 100, 200),
                new SkillingContract(11, "Make <amount> antifire potions.", 69, 100, 200),
                new SkillingContract(12, "Make <amount> super ranging potions.", 72, 100, 200, false),
                new SkillingContract(13, "Make <amount> zamorak brews.", 78, 100, 200, false),
                new SkillingContract(14, "Make <amount> prayer renewal potions.", 94, 100, 200,false),
                new SkillingContract(15, "Make <amount> extreme attack potions.", 88, 100, 200),
                new SkillingContract(16, "Make <amount> extreme strength potions.", 89, 100, 200),
                new SkillingContract(17, "Make <amount> extreme defence potions.", 90, 100, 200),
                new SkillingContract(18, "Make <amount> extreme magic potions.", 91, 100, 200),
                new SkillingContract(19, "Make <amount> extreme ranging potions.", 92, 100, 200),
                new SkillingContract(20, "Make <amount> overload potions.", 96, 20, 30)
        );
    }
}