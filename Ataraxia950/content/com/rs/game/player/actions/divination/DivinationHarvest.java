package com.rs.game.player.actions.divination;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.DivinationRandomEvent;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.content.skillingcontracts.impl.DivinationContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Used to handle Wisp harvesting.
 *
 * @author Noel
 */
public class DivinationHarvest extends Action {

    private final Wisp wisp;
    private final WispInfo info;
    private boolean usingVaccum;
    private com.rs.game.player.client.Native950Divination.HarvestJourney native950Journey;

    /** The original action owns scheduling; paired-cache admission and inventory stay at the native boundary. */
    public DivinationHarvest(NPC npc, WispInfo info) {
        this.wisp = null;
        this.info = info;
        this.native950Journey = new com.rs.game.player.client.Native950Divination.HarvestJourney(npc, info);
    }
    public static int ordinaryMemoryChance(int level, WispInfo info) {
        return Math.min(92, 62 + Math.max(0, level - info.getLevel()) / 2);
    }
    public static int ordinaryEnrichedChance(int level, WispInfo info) {
        return Math.min(60, 3 + Math.max(0, level - info.getLevel()) / 4);
    }
    public static int ordinaryEnergyAmount(int level) { return level >= 75 ? 3 : level >= 55 ? 2 : 1; }


    public DivinationHarvest(Player player, Object[] args) {
        this.wisp = (Wisp) args[0];
        this.info = (WispInfo) args[1];
    }

    public static boolean checkAll(Player player, WispInfo info) {
        if (player.getSkills().getLevel(Skills.DIVINATION) < info.getLevel()) {
            player.sendMessage("You need a Divination level of " + info.getLevel() + " to harvest from this spring.");
            return false;
        }
        if (player.getInventory().getFreeSlots() == 0) {
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return false;
        }
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            int requiredLevel = skillChompa == 40995 ? 71 : 31 + (10 * (skillChompa - 31595));
            if (player.getSkills().getLevel(Skills.DIVINATION) < requiredLevel) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Divination level of " + requiredLevel + " to use this skillchompa.");
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the player has one of the Divination outfits.
     *
     * @param player The player to check.
     * @return if has an outfit.
     */
    public static boolean hasDivinationOutfit(Player player) {
        if (player.getEquipment().getHatId() == 35963 && player.getEquipment().getChestId() == 35964 && player.getEquipment().getLegsId() == 35965 && player.getEquipment().getGlovesId() == 35966 && player.getEquipment().getBootsId() == 35967)
            return true;
        if (player.getEquipment().getHatId() == 35968 && player.getEquipment().getChestId() == 35969 && player.getEquipment().getLegsId() == 35970 && player.getEquipment().getGlovesId() == 35971 && player.getEquipment().getBootsId() == 35972)
            return true;
        return player.getEquipment().getHatId() == 35973 && player.getEquipment().getChestId() == 35974 && player.getEquipment().getLegsId() == 35975 && player.getEquipment().getGlovesId() == 35976 && player.getEquipment().getBootsId() == 35977;
    }

    /**
     * Checks if the player has one of the Divination outfits.
     *
     * @param player The player to check.
     * @return if has an outfit.
     */
    public static boolean hasElderDivinationOutfit(Player player) {
        return player.getEquipment().getHatId() == 35978 && player.getEquipment().getChestId() == 35979 && player.getEquipment().getLegsId() == 35980 && player.getEquipment().getGlovesId() == 35981 && player.getEquipment().getBootsId() == 35982;
    }

    public NPC getWisp() {
        return wisp;
    }

    public WispInfo getInfo() {
        return info;
    }

    @Override
    public boolean start(Player player) {
        if (native950Journey != null) { setActionDelay(player, 1); return native950Journey.start(player); }
        if (!checkAll(player, info))
            return false;
        usingVaccum = player.getEquipment().getWeaponId() == 41083 && player.getInventionManager().getEmptyDivineChargesStored() > 0;
        if (player.getEquipment().getWeaponId() == 41083) {
            player.setNextAnimation(new Animation(31055));
        } else {
            int skillChompa = TrapAction.getSkillChompa(player);
            if (skillChompa != -1) {
                player.faceEntity(wisp);
                player.setNextAnimation(new Animation(23793));
                return true;
            }
            if (player.getAnimations().hasPowerDivination && player.getAnimations().powerDivination)
                player.setNextAnimation(new Animation(22856));
            else if (player.getAnimations().hasAncestralDivination1 && player.getAnimations().ancestralDivination1)
                player.setNextAnimation(new Animation(29522));
            else if (player.getAnimations().hasAncestralDivination2 && player.getAnimations().ancestralDivination2)
                player.setNextAnimation(new Animation(29523));
            else
                player.setNextAnimation(new Animation(21231));
        }
        player.faceEntity(wisp);
        wisp.resetWalkSteps();
        wisp.lock(Integer.MAX_VALUE);
        return true;
    }

    @Override
    public boolean process(Player player) {
        if (native950Journey != null) return native950Journey.process(player);
        if (wisp == null || wisp.isUsedUp())
            return false;
        if (!checkAll(player, info))
            return false;
        player.faceEntity(wisp);
        wisp.resetWalkSteps();
        usingVaccum = player.getEquipment().getWeaponId() == 41083 && player.getInventionManager().getEmptyDivineChargesStored() > 0;
        return true;
    }

    @Override
    public void stop(Player player) {
        if (native950Journey != null) { native950Journey.stop(player); setActionDelay(player, 1); return; }
        if (player.getEquipment().getWeaponId() == 41083)
            player.setNextAnimation(new Animation(-1));
        else {
            int skillChompa = TrapAction.getSkillChompa(player);
            if (skillChompa == -1) {
                if (player.getAnimations().hasAncestralDivination1 && player.getAnimations().ancestralDivination1)
                    player.setNextAnimation(new Animation(29503));
                else
                    player.setNextAnimation(new Animation(21229));
            }
        }
    }

    private int getEnrichedChance(Player player) {
        int boost = ordinaryEnrichedChance(player.getSkills().getLevel(Skills.DIVINATION), info);
        if (player.getPerkManager().hasPerkActive(DonationPerk.DEDICATED_DIVINATION))
            boost += 5;
        return Math.min(60, boost + player.getAuraManager().getDivinationEnrichment());
    }

    private int getMemoryChance(Player player) {
        int chance = ordinaryMemoryChance(player.getSkills().getLevel(Skills.DIVINATION), info);
        if (player.getPerkManager().hasPerkActive(DonationPerk.DEDICATED_DIVINATION))
            chance += 5;
        return Math.min(92, chance);
    }

    private int getEnergyAmount(Player player) {
        return ordinaryEnergyAmount(player.getSkills().getLevel(Skills.DIVINATION));
    }

    private int getDoubleHarvestChance(Player player) {
        if (hasElderDivinationOutfit(player))
            return 7;
        if (hasDivinationOutfit(player))
            return 5;
        return 0;
    }

    private double getSkillchompaEnrichedBonus(Player player) {
        switch (TrapAction.getSkillChompa(player)) {
            case 31595:
                return 3.0;
            case 31596:
                return 6.0;
            case 31597:
                return 10.0;
            case 31598:
                return 15.0;
            case 40995:
                return 22.5;
            default:
                return 0.0;
        }
    }

    private void spawnChronicleFragment(Player player) {
        if (Utils.random(825) == 0) {
            new DivinationRandomEvent(player, player);
            player.sendFilteredMessage(Colors.RED + "<shad=000000>A chronicle escapes from the spring!");
        }
    }

    private boolean isEnrichedSpring() {
        return wisp.getId() == info.getEnrichedSpringNpcId() || wisp.getId() == info.getEnrichedNpcId();
    }

    private void sendHarvestAnimation(Player player) {
        if (player.getEquipment().getWeaponId() == 41083) {
            player.setNextAnimation(new Animation(31055));
            return;
        }
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            wisp.setNextGraphics(new Graphics(3037));
            player.getEquipment().removeAmmo(skillChompa, -1);
            player.setNextAnimation(new Animation(23793));
            return;
        }
        if (player.getAnimations().hasPowerDivination && player.getAnimations().powerDivination)
            player.setNextAnimation(new Animation(22856));
        else if (player.getAnimations().hasAncestralDivination1 && player.getAnimations().ancestralDivination1)
            player.setNextAnimation(new Animation(29522));
        else if (player.getAnimations().hasAncestralDivination2 && player.getAnimations().ancestralDivination2)
            player.setNextAnimation(new Animation(29523));
        else
            player.setNextAnimation(new Animation(21231));
    }

    private void sendHarvestGraphics(Player player, boolean enrichedMemory) {
        if (player.getAnimations().hasAgileDivination && player.getAnimations().agileDivination) {
            player.setNextAnimation(new Animation(22857));
            player.setNextGraphics(new Graphics(4620));
        } else if (player.getAnimations().ancestralDivination2 && player.getAnimations().hasAncestralDivination2) {
            player.setNextAnimation(new Animation(29490));
            player.setNextGraphics(new Graphics(6384));
        } else if (enrichedMemory) {
            player.setNextGraphics(new Graphics(4236));
        } else if (player.getAnimations().hasPowerDivination && player.getAnimations().powerDivination) {
            player.setNextGraphics(new Graphics(4619));
        } else {
            player.setNextGraphics(new Graphics(4235));
        }
    }

    @Override
    public int processWithDelay(Player player) {
        if (native950Journey != null) return native950Journey.harvest(player);
        wisp.addFreezeDelay(1800);
        wisp.resetWalkSteps();
        boolean enrichedSpring = isEnrichedSpring();
        boolean receivesMemory = Utils.random(100) < getMemoryChance(player);
        boolean enrichedMemory = receivesMemory && info != WispInfo.PALE
                && (enrichedSpring || Utils.randomDouble() * 100 < getEnrichedChance(player) + getSkillchompaEnrichedBonus(player));
        boolean doubleHarvest = receivesMemory && Utils.random(100) < getDoubleHarvestChance(player);
        int energyAmount = getEnergyAmount(player) * (doubleHarvest ? 2 : 1);
        if (hasElderDivinationOutfit(player) && Utils.random(100) < 5)
            energyAmount *= 5;
        Item energy = new Item(info.getEnergyId(), energyAmount);

        if (usingVaccum)
            player.getInventionManager().increaseChargesFilled(info.ordinal(), energy.getAmount());
        else
            player.getInventory().addItem(energy);

        double exp = info.getHarvestXp();
        if (enrichedMemory || enrichedSpring)
            exp *= 2;
        if (TrapAction.getSkillChompa(player) != -1)
            exp *= receivesMemory ? 1.1 : 1.05;
        if (doubleHarvest)
            exp *= 2;

        sendHarvestAnimation(player);
        sendHarvestGraphics(player, enrichedMemory);
        player.getSkills().addXp(Skills.DIVINATION, exp);

        if (receivesMemory) {
            boolean vacuumConvert = usingVaccum && player.getInventionManager().isConvertMemories();
            int memoryId = enrichedMemory ? info.getEnrichedMemoryId() : info.getMemoryId();
            int memoryAmount = doubleHarvest ? 2 : 1;
            if (vacuumConvert)
                player.getInventionManager().increaseChargesFilled(info.ordinal(), (enrichedMemory ? 6 : 3) * memoryAmount);
            else
                player.getInventory().addItem(memoryId, memoryAmount);
            for (int i = 0; i < memoryAmount; i++) {
                player.addMemoriesCollected();
                DivinationContractList.listen(player, info);
            }
            player.sendMessage("You've harvested " + (enrichedMemory ? "an enriched memory" : "a memory") + "; memories harvested: " + Colors.RED + Utils.getFormattedNumber(player.getMemoriesCollected()) + "</col>.", true);
        }
        spawnChronicleFragment(player);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        return 3;
    }
}
