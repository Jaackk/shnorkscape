package com.rs.game.player.actions.invention;

public class InventionConstants {

    public enum Perks {
        NONE(0, ""),
        // DONE
        BLUNTED(1, "Reduces the weapon's damage by 1% per rank."),
        // DONE
        INACCURATE(2, "Reduces the weapon's accuracy by 1% per rank."),
        // DONE
        BITING(3, "+2% chance per rank to critically hit opponents.<br><col=0x7592A0><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col></col>"),
        // DONE
        EQUILIBRIUM(4, "Increases minimum hit by 3% per rank and decreases maximum hit by 1% per rank. Does not stack with Equilibrium aura; aura takes precedence."),

        HONED(5, "Has a 2% per rank higher chance of successfully gathering items."),
        // DONE
        LUCKY(6, "0.5% chance per rank when hit that the damage dealt will be reduced to 1. Does not stack with the equivalent Warpriest effect.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        UNDEAD_SLAYER(7, "Deal 7% additional damage to undead."),
        // DONE
        DRAGON_SLAYER(8, "Deal 7% additional damage to dragons."),
        // DONE
        DEMON_SLAYER(9, "Deal 7% additional damage to demons."),
        // DONE
        UNDEAD_BAIT(10, "Deal 30% less damage to undead."),
        // DONE
        DRAGON_BAIT(11, "Deal 30% less damage to dragons."),
        // DONE
        DEMON_BAIT(12, "Deal 30% less damage to demons."),

        LOOTING(13, "Most enemies have a 25% chance to drop an additional high-level resource. (5 minute cooldown.)"),
        // DONE
        ENLIGHTENED(14, "+3% item XP per rank (for the item it's installed on)."),

        GLOW_WORM(15, "Provides light equivalent to a bullseye lantern."),
        // DONE
        ANTITHEISM(16, "Denies access to protect prayers/deflect curses."), // 36382
        // DONE
        HOARDING(17, "Protect Item protects two items instead of one. (Does not work in PvP areas.)"),
        // DONE
        TAUNTING(18, "10% chance upon killing an enemy to receive aggression potion effects for 30 seconds."),

        COMMITTED(19, "Always skulled while this item is equipped."),
        // DONE
        MOBILE(20, "Reduces cooldown of Surge, Escape, and Barge by 50%."),
        // DONE
        CAUTIOUS(21, "Cannot auto-retaliate while this item is equipped."),

        HALLUCINOGENIC(22, "Causes you to see strange things..."),

        TALKING(23, "Gives your gear more personality."),
        // DONE
        SCAVENGING(24, "1% chance per rank to get an uncommon Invention component as a drop from combat (with a 1% chance it will be a rare component instead).<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        TURTLING(25, "Freedom's cooldown is reduced by 3 seconds per rank."),
        // DONE
        BRIEF_RESPITE(26, "3% chance per rank to nullify the next melee, magic, or ranged hit that the player receives whilst a shield is equipped."),
        // DONE
        WISE(27, "While equipped, +1% per rank additional experience, up to 500k XP per day."), // 35330

        // DONE
        EFFICIENT(28, "Charge drain rate for this item is reduced by 6% per rank."),
        // DONE
        ABSORBATIVE(29, "20% chance to reduce an attack by 5% per rank.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        PRECISE(30, "Increases your minimum damage by 1.5% per rank of your maximum damage."),
        // DONE
        PROFANE(31, "Cannot consume prayer potions."),

        BRASSICAN(32, "Always sometimes cabbages."),

        FATIGUING(33, "-3% damage per rank."),
        // DONE
        GENOCIDAL(34, "Deal up to +7% extra damage to your current Slayer target proportional to progress through your current task."),
        // DONE
        CRACKLING(35, "Periodically zaps your combat target for 50% per rank of your weapon's damage (or 10% per rank in PvP). (1 minute cooldown)"), // 35321
        // DONE
        IMPATIENT(36, "After dealing <col=ffffff>5,000</col> damage you have a 5% chance per rank of restoring 20% special attack. This does not stack with Ultimatums and Energising."),
        // DONE
        INVIGORATING(37, "5% chance to restore 15 prayer points per rank upon killing an enemy."),
        // DONE
        VENOMBLOOD(38, "Regular poison damage is negated."),

        DEVOTED(39, "3% chance per rank on being hit that protection prayers will work at 100% (or 75% in PvP) for 3 seconds.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"), // 35319
        // DONE
        SPENDTHRIFT(40, "1% chance per rank to deal 1% extra damage per rank, at the cost of 1 gold coin per extra point of damage dealt.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        SHIELD_BASHING(41, "5% damage increase per rank with a shield equipped."),
        // DONE
        ULTIMATUMS(42, "3% chance per rank to reduce special attack cost by 50%. This does not stack with Impatient and Energising."),
        // DONE
        JUNK_FOOD(43, "Food gives 3% less health per rank."),
        // DONE
        ENERGISING(44, "Special attack regenerates 5% faster per rank. This does not stack with Ultimatums and Impatient."),
        // DONE
        TROPHY_TAKERS(45, "3% chance per rank a slain creature will add zero kills to its Slayer assignment; 2% chance per rank it will add 2 kills.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        CLEAR_HEADED(46, "0.5% accuracy bonus per rank."),
        // DONE
        REFLEXES(47, "Vengeance rebounds 150% of damage."),
        // DONE
        BULWARK(48, "Disruption Shield's cooldown is lowered by 5 seconds per rank."),
        // DONE
        PREPARATION(49, "Deflect curses reflect 3% more damage per rank."),
        // DONE
        MEDIOCRITY(50, "Reduces maximum hit by 3% per rank."),

        MYSTERIOUS(51, "Has a mysterious effect..."),
        // DONE
        FURNACE(52, "Has a 5% chance per rank of consuming a gathered resource for an extra 100% XP.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        POLISHING(53, "Has a 3% chance per rank of transmuting a gathered resource to a higher tier.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        //DONE
        CHEAPSKATE(54, "Has a 1% chance per rank of transmuting a gathered resource to a lower tier."),
        //DONE
        IMP_SOULED(55, "Has a 3% chance per rank on successful gathering to send the gathered resources to the bank at a cost of 30 prayer points.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        //DONE
        BUTTERFINGERS(56, "Has a 3% chance per rank of dropping the resource you have just gathered.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

        REFINED(57, "Has a 5% chance per rank of preventing a resource depleting when gathering.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        //DONE
        CHARITABLE(58, "Has a 1% chance per rank of putting an extra item by other nearby players after successfully gathering a resource.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        //DONE
        CONFUSED(59, "Has a 1% chance per rank of teleporting you randomly after successfully gathering a resource."),

        CAROMING(60, "Chain and Ricochet hit <col=ffffff>1</col> extra target per rank."),
        // DONE
        AFTERSHOCK(61, "After dealing <col=ffffff>5,000</col> damage, create an explosion centered on your current target, dealing up to <col=ffffff>40%</col> per rank weapon damage to nearby enemies."), // 35318
        // DONE
        LUNGING(62, "Defence is reduced by 15% and accuracy is increased by 1% per rank."),
        // DONE
        PLANTED_FEET(63, "Defence is increased by 10% and you deal 10% less damage."),
        // DONE
        ENHANCED_EFFICIENT(64, "Charge drain rate for this item is reduced by 9% per rank.  This does not stack with efficient."),
        // DONE
        FLANKING(65, "15% faster enemy stat drain per rank for Turmoil, Anguish, Torment and their tier 99 equivalent prayers."),

        ENHANCED_DEVOTED(66, "<col=ffffff>4.5%</col> chance per rank on being hit that protection prayers will work at <col=ffffff>100%</col> (or <col=ffffff>75%</col> in PvP) for 3 seconds. This does not stack with devoted.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"), // 35320

        CRYSTAL_SHIELD(67, "Has a 10% chance to activate on taking damage, lasting 10 seconds. 5% of damage taken per rank is totaled for this period, becoming temporary lifepoints afterwards. These last either 30 seconds or until depleted through further damage. (1 minute cooldown)<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // crystal shield cooldown 36377
        // DONE
        RAPID(68, "Has a 5% chance per rank to carry out work at a faster pace.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        TINKER(69, "Has a 5% chance per rank to carry out higher quality work, awarding an extra 25% XP<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        PYROMANIAC(70, "Has a 0.1% chance per rank of burning all logs of the same type from the inventory.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
        // DONE
        BREAKDOWN(71, "Has a 20% chance per rank of automatically disassembling items produced.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

        PROSPER(72, "Allows you to find clue scrolls whilst skilling.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),;
        private final int id;
        private String description;

        Perks(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public boolean hasIncreasedChance() {
            return description.toLowerCase().contains("increased chance to activate");
        }
        
        public static String getDesc(int perkId) {
            for(Perks perk : Perks.values())
                if(perk.getId() == perkId)
                    return perk.getDescription();
            return null;
        }

    }

}
