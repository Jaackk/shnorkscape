package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class RunecraftingContractList extends ContractList {

    @AllArgsConstructor
    @Getter
    public static final class CraftAmount {
        private final int min;
        private final int max;
    }

    public static void listen(Player player, int rune, int amount) {
        switch (rune) {
            case 556: // Air rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 0, amount);
                break;
            case 558: // Mind rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 1, amount);
                break;
            case 555: // Water rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 2, amount);
                break;
            case 557: // Earth rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 3, amount);
                break;
            case 554: // Fire rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 4, amount);
                break;
            case 559: // Body rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 5, amount);
                break;
            case 564: // Cosmic rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 6, amount);
                break;
            case 562: // Chaos rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 7, amount);
                break;
            case 9075: // Astral rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 8, amount);
                break;
            case 561: // Nature rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 9, amount);
                break;
            case 563: // Law rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 10, amount);
                break;
            case 560: // Death rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 11, amount);
                break;
            case 565: // Blood rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 12, amount);
                break;
            case 566: // Soul rune.
                player.getContracts().recordAction(Skills.RUNECRAFTING, 13, amount);
                break;
        }
    }

    public RunecraftingContractList() {
        super(Skills.RUNECRAFTING, false);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
       CraftAmount airAmount = getAmount("air rune", 150, 400);
        CraftAmount mindAmount = getAmount("mind rune", 150, 400);
        CraftAmount waterAmount = getAmount("water rune", 150, 400);
        CraftAmount earthAmount = getAmount("earth rune", 150, 400);
        CraftAmount fireAmount = getAmount("fire rune", 150, 400);
        CraftAmount bodyAmount = getAmount("body rune", 150, 400);
        CraftAmount cosmicAmount = getAmount("cosmic rune", 150, 400);
        CraftAmount chaosAmount = getAmount("chaos rune", 150, 300);
        CraftAmount astralAmount = getAmount("astral rune", 150, 300);
        CraftAmount natureAmount = getAmount("nature rune", 150, 300);
        CraftAmount lawAmount = getAmount("law rune", 150, 300);
        CraftAmount deathAmount = getAmount("death rune", 150, 300);
        CraftAmount bloodAmount = getAmount("blood rune", 150, 300);
        CraftAmount soulAmount = getAmount("soul rune", 150, 300);
        return ImmutableList.of(
                new SkillingContract(0, "Craft <amount> air runes.", 1, airAmount),
                new SkillingContract(1, "Craft <amount> mind runes.", 1, mindAmount),
                new SkillingContract(2, "Craft <amount> water runes.", 5, waterAmount),
                new SkillingContract(3, "Craft <amount> earth runes.", 9,earthAmount),
                new SkillingContract(4, "Craft <amount> fire runes.", 14, fireAmount),
                new SkillingContract(5, "Craft <amount> body runes.", 20, bodyAmount),
                new SkillingContract(6, "Craft <amount> cosmic runes.", 27, cosmicAmount),
                new SkillingContract(7, "Craft <amount> chaos runes.", 35, chaosAmount),
                new SkillingContract(8, "Craft <amount> astral runes.", 40, astralAmount),
                new SkillingContract(9, "Craft <amount> nature runes.", 44, natureAmount),
                new SkillingContract(10, "Craft <amount> law runes.", 54, lawAmount),
                new SkillingContract(11, "Craft <amount> death runes.", 65, deathAmount),
                new SkillingContract(12, "Craft <amount> blood runes.", 77, bloodAmount),
                new SkillingContract(13, "Craft <amount> soul runes.", 90, soulAmount)
        );
    }

    private CraftAmount getAmount(String runeName, int baseMin, int baseMax) {
        int multiplier = RuneCrafting.getRuneMultiplier(runeName);
        int min = baseMin * multiplier;
        int max = baseMax * multiplier;
        return new CraftAmount(min, max);
    }
}