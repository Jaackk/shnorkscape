package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.activities.dfm.DemonFlashBoss;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.client.Native950MeleeCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.DTController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public final class CombatDefinitions implements Serializable {

    public static final int SHARED = -1;
    private static final long serialVersionUID = 2102201264836121104L;

    // saving stuff
    private transient Player player;
    private transient boolean usingSpecialAttack;
    private transient int[] bonuses;
    private byte specialAttackPercentage;
    private transient boolean infiniteAdrenaline;

    public boolean isInfiniteAdrenaline() { return infiniteAdrenaline; }
    public void setInfiniteAdrenaline(boolean enabled) { infiniteAdrenaline = enabled; }
    private boolean autoRetaliate;
    private boolean sheathe;
    private transient boolean forceNoSheathe;
    private transient boolean instantAttack;
    private transient boolean dungeonneringSpellBook;

//    private byte attackStyle;
//    private boolean defensiveCasting;

    private byte spellBook;
    /**
     * I wanna kill myself for how cheap of a fix this is.
     */
    @Getter
    @Setter
    private transient int spellBookBefore;

    private transient boolean combatStance;

    public CombatDefinitions() {
        specialAttackPercentage = 100;
        autoRetaliate = true;
        sheathe = true;
    }

    public static boolean hasPolyporeStaff(final Player player) {
        final int weaponId = player.getEquipment().getWeaponId();
        return weaponId != -1 && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("polypore staff");
    }

    public void decreaseSpecialAttack(final int percentAmount) {
        decreaseSpecialAttack(percentAmount, true);
    }

    public void decreaseSpecialAttack(int percentAmount, boolean disableUsingSpec) {
        // Retain special-attack cleanup while suppressing only its resource cost.
        if (infiniteAdrenaline) percentAmount = 0;
        if (percentAmount > 0) {
            Perk ultimatums = player.getInventionManager().hasPerk(Perks.ULTIMATUMS);
            Perk energising = player.getInventionManager().hasPerk(Perks.ENERGISING);
            Perk impatient = player.getInventionManager().hasPerk(Perks.IMPATIENT);
            if (ultimatums != null && energising == null && impatient == null && Math.random() <= (0.03 * (double)ultimatums.getRank())) {
                percentAmount /= 2;
                player.getPackets().sendGameMessage("You only use half special attack percentage for this attack, thanks to your ultimatums perk effect.", true);
            }
        }
        int finalAmount = (specialAttackPercentage - percentAmount) < 0 ? specialAttackPercentage : percentAmount;
        if (disableUsingSpec)
            usingSpecialAttack = false;
        refreshUsingSpecialAttack();
        if (finalAmount > 0) {
            specialAttackPercentage -= finalAmount;
            refreshSpecialAttackPercentage();
        }
    }

    public int[] getBonuses() {
        return bonuses;
    }

    public int getSpecialAttackPercentage() {
        return specialAttackPercentage;
    }

    public void setSpecialAttackPercentage(final int amount) {
        if (infiniteAdrenaline && amount < specialAttackPercentage) return;
        specialAttackPercentage = (byte) amount;
        refreshSpecialAttackPercentage();
    }

    public int getSpellBook() {
        if (Magic.isOnSwapSpellBook(player))
            return Magic.getSwappedSpellBookId(player);
        return spellBook;
    }

    public int getActualSpellBook() {
        return spellBook;
    }

    public void setSpellBook(final int id) {
        Magic.resetSwapSpellBook(player);
        if (id == 3) {
            dungeonneringSpellBook = true;
        } else {
            dungeonneringSpellBook = false;
            spellBook = (byte) id;
        }
        refreshSpellBook();
        setAutoCast(-1);
    }

    public int getSpellId(boolean offHand) {
//        if (player.isUsingBorrowedSpell() && player.getBorrowedSpellId() > 0) {
//            return player.getBorrowedSpellId();
//        }
        Item weapon = player.getEquipment().getItem(offHand ? Equipment.SLOT_SHIELD : Equipment.SLOT_WEAPON);
        if (!offHand) {
            final Integer tempCastSpell = (Integer) player.getTemporaryAttributtes().get("tempCastSpell");
            if (tempCastSpell != null) {
                return tempCastSpell + 1000;
            }
            return weapon != null && weapon.getDefinitions().isMagicTypeWeapon() ? mainHandSpellId : 0;
        }
        return weapon != null && weapon.getDefinitions().isMagicTypeWeapon() ? offHandSpellId : 0;
    }

    public boolean hasRingOfVigour() {
        return player.getEquipment().getRingId() == 19669;
    }

    public void init() {
        refreshUsingSpecialAttack();
        refreshSpecialAttackPercentage();
        refreshAutoRelatie();
        refreshSpellBook();
        refreshMeleeCombatExperience();
        refreshRangedCombatExperience();
        refreshMagicCombatExperience();

        refreshIsOnStrengthMenu();
        refreshIsOnConstitutionMenu();

        refreshManualCast();
        refreshManualSpellCasting();
        player.getPackets().sendConfig(3226, -1);
        refreshMainHandSpell();
        refreshOffHandSpell();
        refreshBorrowedSpell();
        refreshFilteredAbilities();
        refreshRunes();
    }

    public boolean isAutoRetaliate() {
        return autoRetaliate;
    }

    public void setAutoRetaliate(final boolean autoRetaliate) {
        this.autoRetaliate = autoRetaliate;
    }

    public boolean isDungeonneringSpellBook() {
        return dungeonneringSpellBook;
    }

    public boolean isInstantAttack() {
        return instantAttack;
    }

    public void setInstantAttack(final boolean instantAttack) {
        this.instantAttack = instantAttack;
    }

    public boolean isUsingSpecialAttack() {
        return usingSpecialAttack;
    }

    public void refreshAutoRelatie() {
        player.getPackets().sendConfig(462, autoRetaliate ? 0 : 1);
    }

    public static final int MAINHAND_DAMAGE = 0, MAINHAND_ACCURACY = 1, OFFHAND_DAMAGE = 2, OFFHAND_ACCURACY = 3, WORN_ARMOUR = 4, LIFE_B = 5, PRAYER_B = 6, DAMAGE_REDUCTN_PVP = 7, DAMAGE_REDUCTION_PVM = 8, MELEE_AFF = 9, RANGE_AFF = 10, MAGIC_AFF = 11, MELEE_ACCURACY_PENALTY = 12, RANGE_ACCURACY_PENALTY = 13, MAGE_ACCURACY_PENALTY = 14;

    public void refreshBonuses() {
        bonuses = new int[15];
        int mainHandType = player.getEquipment().getWeaponId() == -1 ? Combat.MELEE_TYPE : getType(Equipment.SLOT_WEAPON);
        int offHandType = getType(Equipment.SLOT_SHIELD);
        int meleeGearArmor = 0;
        int rangeGearArmor = 0;
        int mageGearArmor = 0;
        int allGearArmor = 0;
        Item shield = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
        boolean hasShield = player.getEquipment().hasShield() && shield.getDefinitions().isShield();
        val items = player.getEquipment().getItems().getItems();
        for (int i = items.length - 1; i >= 0; i--) {
            val item = items[i];
            if (item == null)
                continue;
            ItemDefinitions defs = item.getDefinitions();
            if (item.getId() == 9705)
                defs = ItemDefinitions.getItemDefinitions(857);
            if (item.getId() == 9703)
                defs = ItemDefinitions.getItemDefinitions(1303);
            if (item.getName().equalsIgnoreCase("christmas scythe"))
                defs = ItemDefinitions.getItemDefinitions(36333);
            if (item.getName().equalsIgnoreCase("soul reaper's blade"))
                defs = ItemDefinitions.getItemDefinitions(4587);
            if ((item.getName().equalsIgnoreCase("Dominion sword") || item.getName().equalsIgnoreCase("Dominion crossbow") || item.getName().equalsIgnoreCase("Dominion staff")) && !DTController.isInsideDominionTower(player))
                continue;
            if (item.getName().toLowerCase().contains("goliath gloves"))
                continue;
            if (!item.getDefinitions().getName().equalsIgnoreCase("Celestial handwraps") && (item.getName().toLowerCase().contains("primal") || item.getName().toLowerCase().contains("sagittarian") || item.getName().toLowerCase().contains("celestial")))
                continue;
            if (ItemConstants.getActualCapeId(item.getId()) != -1)
                defs = ItemDefinitions.getItemDefinitions(ItemConstants.getActualCapeId(item.getId()));
            int defenderType = defs.getName().toLowerCase().contains(" defender") ? Combat.MELEE_TYPE : defs.getName().toLowerCase().contains(" repriser") ? Combat.RANGE_TYPE : defs.getName().toLowerCase().contains(" rebounder") || defs.getName().toLowerCase().contains("ancient lantern") ? Combat.MAGIC_TYPE : -1;
            int type = getType(i);
            if (i != Equipment.SLOT_SHIELD && !(i == Equipment.SLOT_ARROWS && mainHandType == Combat.RANGE_TYPE && player.getEquipment().getItem(Equipment.SLOT_WEAPON).getDefinitions().getCSOpcode(2940) != 0)) {
                int damage = defs.getDamage(mainHandType);
                if (i == Equipment.SLOT_ARROWS && item.getId() == 29617 && !Combat.hasDarkbow(player))
                    damage /= 2;
                else if (i == Equipment.SLOT_ARROWS && (item.getName().toLowerCase().contains("ascendri bolts") || item.getName().toLowerCase().contains("ascension bolts")) && !Combat.hasAscensionCrossbow(player, true))
                    damage /= 2;
                if (i == Equipment.SLOT_ARROWS) { // cap arrows
                    Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
                    int maxDamage = weapon == null ? 0 : (weapon.getDefinitions().getRangedLevel() * 96);
                    if (damage > maxDamage)
                        damage = maxDamage;
                }
                if (defenderType != -1) {
                    damage = (int) (damage + (Math.ceil(damage * 0.25)));
                }
                int accuracy = defs.getAccuracy(mainHandType);
                Perk blunted = player.getInventionManager().hasPerk(Perks.BLUNTED);
                if (blunted != null)
                    damage -= (int) ((double) damage * ((double) blunted.getRank() * 0.01));
                Perk inaccurate = player.getInventionManager().hasPerk(Perks.INACCURATE);
                if (inaccurate != null)
                    accuracy -= (int) ((double) accuracy * ((double) inaccurate.getRank() * 0.01));

                bonuses[MAINHAND_DAMAGE] += damage;
                bonuses[MAINHAND_ACCURACY] += accuracy;
            }
            if (!hasShield && i != Equipment.SLOT_WEAPON && !(i == Equipment.SLOT_ARROWS && offHandType == Combat.RANGE_TYPE && player.getEquipment().getItem(Equipment.SLOT_SHIELD).getDefinitions().getCSOpcode(2940) != 0)) {
                int damage = defs.getDamage(offHandType) / 2;
                if (i == Equipment.SLOT_ARROWS && item.getId() == 29617 && !Combat.hasDarkbow(player))
                    damage /= 2;
                else if (i == Equipment.SLOT_ARROWS && (item.getName().toLowerCase().contains("ascendri bolts") || item.getName().toLowerCase().contains("ascension bolts")) && !Combat.hasAscensionCrossbow(player, false))
                    damage /= 2;
                if (i == Equipment.SLOT_ARROWS) { // cap arrows
                    Item weapon = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
                    int maxDamage = weapon == null ? 0 : (weapon.getDefinitions().getRangedLevel() * 96);
                    if (damage > maxDamage)
                        damage = maxDamage;
                }

                if (defenderType != -1) {
                    damage = (int) (damage + (Math.ceil(damage * 0.25)));
                }
                int accuracy = defs.getAccuracy(offHandType);
                Perk blunted = player.getInventionManager().hasPerk(Perks.BLUNTED);
                if (blunted != null)
                    damage -= (int) ((double) damage * ((double) blunted.getRank() * 0.01));
                Perk inaccurate = player.getInventionManager().hasPerk(Perks.INACCURATE);
                if (inaccurate != null)
                    accuracy -= (int) ((double) accuracy * ((double) inaccurate.getRank() * 0.01));
                if (Settings.DUAL_COMBAT || i == Equipment.SLOT_SHIELD) {
                    bonuses[OFFHAND_DAMAGE] += damage;
                    bonuses[OFFHAND_ACCURACY] += accuracy;
                }
            }
            bonuses[LIFE_B] += defs.getHealth() / 10;
            bonuses[PRAYER_B] += defs.getPrayerBonus();
            int armor = defs.getArmor();
            if (armor > 0) {
                bonuses[WORN_ARMOUR] += armor;

                if (type != Combat.ALL_TYPE && (i == Equipment.SLOT_CHEST || i == Equipment.SLOT_LEGS || i == Equipment.SLOT_FEET || i == Equipment.SLOT_HANDS || i == Equipment.SLOT_HAT || (i == Equipment.SLOT_SHIELD && !hasShield)) && player.getEquipment().getWeaponId() != -1 && type != mainHandType) {
                    if (mainHandType == Combat.MELEE_TYPE)
                        bonuses[MELEE_ACCURACY_PENALTY] += armor * (type == Combat.RANGE_TYPE ? 1.5 : 0.8);
                    else if (mainHandType == Combat.RANGE_TYPE)
                        bonuses[RANGE_ACCURACY_PENALTY] += armor * (type == Combat.MAGIC_TYPE ? 1.5 : 0.8);
                    else if (mainHandType == Combat.MAGIC_TYPE)
                        bonuses[MAGE_ACCURACY_PENALTY] += armor * (type == Combat.MELEE_TYPE ? 1.5 : 0.8);
                }
                if (type == Combat.ALL_TYPE) {
                    allGearArmor += armor;
                } else if (type == Combat.MELEE_TYPE) {
                    meleeGearArmor += armor;
                } else if (type == Combat.RANGE_TYPE) {
                    rangeGearArmor += armor;
                } else if (type == Combat.MAGIC_TYPE) {
                    mageGearArmor += armor;
                }
            }

        }
        if (player.getEquipment().getWeaponId() == -1 && !player.getEquipment().hasOffHand()) {
            Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
            if (gloves != null && gloves.getDefinitions().getName().toLowerCase().contains("goliath gloves")) {
                bonuses[MAINHAND_DAMAGE] += 10800;
                bonuses[MAINHAND_ACCURACY] += 1694;
            }
        }
        if (!Settings.DUAL_COMBAT) {
            bonuses[MAINHAND_DAMAGE] += bonuses[OFFHAND_DAMAGE];
            bonuses[MAINHAND_ACCURACY] = Math.max(bonuses[MAINHAND_ACCURACY], bonuses[OFFHAND_ACCURACY]);
        }
        bonuses[MELEE_AFF] = Combat.getArmourAffinity(bonuses[WORN_ARMOUR], mageGearArmor, meleeGearArmor + allGearArmor, rangeGearArmor);
        bonuses[RANGE_AFF] = Combat.getArmourAffinity(bonuses[WORN_ARMOUR], meleeGearArmor, rangeGearArmor + allGearArmor, mageGearArmor);
        bonuses[MAGIC_AFF] = Combat.getArmourAffinity(bonuses[WORN_ARMOUR], rangeGearArmor, mageGearArmor + allGearArmor, meleeGearArmor);
        player.getEquipment().refreshEquipmentInterfaceBonuses();
        refreshRunes();
    }

    public void refreshRunes() {
        int[] runeIds = { 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 4694, 4695, 4696, 4697, 4698, 4699, 9075, 21773 };
        int[] varIds = { 5888, 5887, 5886, 5889, 5902, 5896, 5901, 5899, 5898, 5900, 5897, 5904, 5905, 5893, 5891, 5890, 5892, 5894, 5895, 5903, 5906 };
        for (int i = 0; i < runeIds.length; i++)
            player.getPackets().sendGlobalConfig(varIds[i], Magic.getTotalAmountOfRune(player, runeIds[i], player.getEquipment().getWeaponId(), player.getEquipment().getShieldId()));
    }

    public void refreshSpecialAttackPercentage() {
        player.getPackets().sendConfig(679, specialAttackPercentage * 10);
    }

    public void refreshSpellBook() {
        player.getPackets().sendConfig(1725, isDungeonneringSpellBook() ? 11 : 0);
        player.getPackets().sendConfigByFile(0, getSpellBook());
        player.getPackets().sendExecuteScript(13513);
    }

    public void refreshUsingSpecialAttack() {
        player.getPackets().sendConfig(680, usingSpecialAttack ? 1 : 0);
    }

    public void removeDungeonneringBook() {
        if (dungeonneringSpellBook) {
            dungeonneringSpellBook = false;
            refreshSpellBook();
        }
    }

    public void resetSpecialAttack() {
        decreaseSpecialAttack(0);
        specialAttackPercentage = 100;
        refreshSpecialAttackPercentage();
    }

    public void restoreSpecialAttack() {
        if (player.getFamiliar() != null) {
            player.getFamiliar().restoreSpecialAttack(15);
        }
        if (specialAttackPercentage == 100) {
            return;
        }
        int amount = player.getTarget() != null && player.getTarget() instanceof DemonFlashBoss ? 5 : 10;
        Perk ultimatums = player.getInventionManager().hasPerk(Perks.ULTIMATUMS);
        Perk energising = player.getInventionManager().hasPerk(Perks.ENERGISING);
        Perk impatient = player.getInventionManager().hasPerk(Perks.IMPATIENT);
        if (energising != null && ultimatums == null && impatient == null)
            amount += 2 * energising.getRank();
        restoreSpecialAttack(amount);
        if (specialAttackPercentage == 100 || specialAttackPercentage == 50) {
            player.getPackets().sendGameMessage("<col=00FF00><shad=000000>Your special attack energy is now " + specialAttackPercentage + "%.", true);
        }
    }

    public void restoreSpecialAttack(final int percentage) {
        if (specialAttackPercentage >= 100 || player.getInterfaceManager().containsScreenInter()) {
            return;
        }
        specialAttackPercentage += specialAttackPercentage > (100 - percentage) ? 100 - specialAttackPercentage : percentage;
        refreshSpecialAttackPercentage();
    }

    public void setPlayer(final Player player) {
        this.player = player;
        bonuses = new int[15];
    }

    public void setSpecialAttack(final int special) {
        decreaseSpecialAttack(0);
        setSpecialAttackPercentage(special);
    }

    public void switchAutoRelatie() {
        autoRetaliate = !autoRetaliate;
        refreshAutoRelatie();
    }

    public void switchUsingSpecialAttack() {
        usingSpecialAttack = !usingSpecialAttack;
        refreshUsingSpecialAttack();
    }

    private boolean isForceNoSheathe() {
        return player.isCanPvp();
    }

    public boolean isCombatStance() {
        return combatStance;
    }

    public void setCombatStance(final boolean combatStance) {
        this.combatStance = combatStance;
    }

    public void processCombatStance() {
        final boolean forceSheathe = isForceNoSheathe();
        // Native 950 combat owns its target outside the legacy ActionManager. Its
        // attack timer is still maintained for logout and other legacy gates.
        Native950MeleeCombat nativeCombat = player.getNative950Combat();
        final boolean underCombat = player.isUnderCombat()
                || nativeCombat != null && nativeCombat.combatTarget(player) != null;

        if (!underCombat && !player.getActivityTimersManager().isInsideSpecialBoss()) {
            player.getActivityTimersManager().resetTimers();
        }

        if (forceNoSheathe != forceSheathe) {
            forceNoSheathe = forceSheathe;
            if (underCombat == combatStance) {
                player.getAppearence().generateAppearenceData();
            }
        }
        if (underCombat != combatStance) {
            // Legacy actions produce their emote during Player.processEntity.
            // Native attacks run after movement, then their mask is reset before
            // the next stance check. Target ownership, not that transient mask,
            // therefore drives native BAS entry (without forcing an animation).
            if (underCombat && nativeCombat == null && player.getNextAnimation() == null) {
                return;
            }
            combatStance = underCombat;
            if (!combatStance) {
                if (isSheathe()) {
                    player.setNextAnimationNoPriority(new Animation(18027));
                } else {
                    player.setNextAnimationNoPriority(new Animation(player.getEquipment().getWeaponEndCombatEmote()));
                }
            }
            player.getPackets().sendConfigByFile(1899, underCombat ? 1 : 0);
            if (player.getInterfaceManager().isMenuOpen())
                player.getInterfaceManager().closeMenu();
//            player.getDialogueManager().finishConfirmDialogue();
            if (!underCombat && currentTarget != null) {
                setCurrentTarget(null);
            }
            player.getAppearence().generateAppearenceData();
        }
        // The legacy current-target panel sends IF_OPENSUB_ACTIVE_*, which is not
        // a verified 950 packet. Native combat already owns player.getTarget().
        if (underCombat && nativeCombat == null) {
            Entity target = player.getActionManager().getAction() instanceof PlayerCombat
                    ? ((PlayerCombat) player.getActionManager().getAction()).getTarget() : null;
            if (target != null && currentTarget != target) {
                setCurrentTarget(target);
            } else if (currentTarget != null && (currentTarget.hasFinished() || !player.withinDistance(currentTarget, 16)))
                setCurrentTarget(null);
        }
    }

    public boolean isSheathe() {
        return !player.isUnderCombat() && !forceNoSheathe && sheathe;
    }

    public void switchSheathe() {
        if (player.isLocked()) {
            return;
        }
        if (player.isUnderCombat()) {
            player.sendMessage("You can't do that while in combat.");
            return;
        }
        sheathe = !sheathe;
        player.setNextAnimation(new Animation(sheathe ? 18027 : 18028));
        player.lock(1);
        player.getAppearence().generateAppearenceData();
        player.sendMessage("Sheathing is now turned: <shad=000000>" + (isSheathe() ? Colors.GREEN + "on" : Colors.RED + "off") + "</col></shad>.");
        player.getPackets().sendIComponentText(751, 16, "<shad=000000>" + (isSheathe() ? Colors.GREEN : Colors.RED) + "Sheathe</col></shad>");
    }

    public int getType(int slot) {
        Item item = player.getEquipment().getItem(slot);
        if (item != null) {
            ItemDefinitions defs = item.getDefinitions();
            if (defs.isMeleeTypeWeapon() || defs.isMeleeTypeGear())
                return Combat.MELEE_TYPE;
            if (defs.isRangeTypeWeapon() || defs.isRangeTypeGear())
                return Combat.RANGE_TYPE;
            if (defs.isMagicTypeWeapon() || defs.isMagicTypeGear())
                return Combat.MAGIC_TYPE;
        }
        return Combat.ALL_TYPE;
    }

    public int getStyle(boolean offhand) {
        Item weapon = player.getEquipment().getItem(offhand ? Equipment.SLOT_SHIELD : Equipment.SLOT_WEAPON);
        if (weapon != null && weapon.getDefinitions().isMagicTypeWeapon()) {
            int spellId = getSpellId(offhand);
            if (spellId > 0) {
                if (spellId >= 1000) // manual cast
                    spellId -= 1000;
                return Magic.getSpellType(Magic.getSpellData(spellId));
            }
            return Combat.NO_SPELL_SELECTED_STYLE;
        }
        if (weapon != null) {
            if (weapon.getName().toLowerCase().contains("repriser"))
                return Combat.BOLT_STYLE;
            if (weapon.getName().equalsIgnoreCase("mud pie"))
                return Combat.THROWN_STYLE;
            int attackType = Combat.getStyleType(weapon.getDefinitions().getCombatStyle());
            return attackType == Combat.MAGIC_TYPE ? Combat.NO_SPELL_SELECTED_STYLE : weapon.getDefinitions().getCombatStyle();
        }
        return Combat.CRUSH_STYLE;
    }

    public int getAffinity(int type) {
        if (type == Combat.MELEE_TYPE)
            return bonuses[MELEE_AFF];
        if (type == Combat.RANGE_TYPE)
            return bonuses[RANGE_AFF];
        if (type == Combat.MAGIC_TYPE)
            return bonuses[MAGIC_AFF];
        return 55;
    }

    public int getAccuracyPenalty(int type) {
        if (type == Combat.MELEE_TYPE)
            return bonuses[MELEE_ACCURACY_PENALTY];
        if (type == Combat.RANGE_TYPE)
            return bonuses[RANGE_ACCURACY_PENALTY];
        if (type == Combat.MAGIC_TYPE)
            return bonuses[MAGE_ACCURACY_PENALTY];
        return 0;
    }

    public int getStyleBonus(int type) {
        int deltaMelee = 55 - bonuses[MELEE_AFF];
        int deltaRange = 55 - bonuses[RANGE_AFF];
        int deltaMagic = 55 - bonuses[MAGIC_AFF];
        int totalArmour = (int) (bonuses[WORN_ARMOUR] + Combat.getStatBonus(player.getSkills().getLevel(Skills.DEFENCE)));
        if (type == Combat.MELEE_TYPE)
            return (int) (((double) deltaMelee * (double) totalArmour) / 100.00);
        if (type == Combat.RANGE_TYPE)
            return (int) (((double) deltaRange * (double) totalArmour) / 100.00);
        if (type == Combat.MAGIC_TYPE)
            return (int) (((double) deltaMagic * (double) totalArmour) / 100.00);
        return 0;
    }

    public int getHandDamage(boolean offhand) {
        int combatType = Combat.getStyleType(getStyle(offhand));
        if (combatType == Combat.MAGIC_TYPE) {
            int spellDmg = getSpellDamage(offhand);
            int damage = (int) Math.ceil(((double) (!offhand ? getBonuses()[MAINHAND_DAMAGE] : getBonuses()[OFFHAND_DAMAGE]) / 10.00 + (double) getSkillBonusDamage(offhand) + (double) spellDmg) / 10.00);
            return damage;
        }
        int damage = 0;
        if (Settings.DUAL_COMBAT) {
            damage = (int) Math.ceil(((double) (!offhand ? getBonuses()[MAINHAND_DAMAGE] : getBonuses()[OFFHAND_DAMAGE]) / 10.00 + getSkillBonusDamage(offhand)) / 10.00);
        } else
            damage = (int) Math.ceil(((double) (getBonuses()[MAINHAND_DAMAGE] + getBonuses()[OFFHAND_DAMAGE]) / 10.00 + getSkillBonusDamage(offhand)) / 10.00);
        return damage;
    }

    private int getSpellDamage(boolean offhand) {
        int weaponId = offhand ? player.getEquipment().getShieldId() : player.getEquipment().getWeaponId();
        ItemDefinitions defs = weaponId == -1 ? null : ItemDefinitions.getItemDefinitions(weaponId);
        if (defs == null)
            return 0;
        int spellId = getSpellId(offhand);
        if (spellId > 0) {
            boolean manualCast = spellId >= 1000;
            GeneralRequirementMap data = Magic.getSpellData(manualCast ? spellId - 1000 : spellId);
            int wearpos = defs.getEquipSlot();
            int wearpos2 = defs.getEquipType();
            int item32 = ClientScriptMap.getMap(7338).getIntValue(defs.getCSOpcode(3));
            if (defs.getName().toLowerCase().contains("of the cywir elders"))
                item32 = 80;
            int item34 = Math.max(0, (Math.min(item32, data.getIntValue(2879)) - data.getIntValue(2807)) * data.getIntValue(2878));
            int item29 = 0;
            if (wearpos > 0 && wearpos2 > 0) {
                item29 = (Math.min(ClientScriptMap.getMap(7443).getIntValue(item32), data.getIntValue(2877)) * 150 / 100 + 2 * (3975 * item32) * 1 / 100 + item34) / 10;
            } else {
                item29 = (Math.min(ClientScriptMap.getMap(7443).getIntValue(item32), data.getIntValue(2877)) + item34) / 10;
            }
            return item29 / (offhand ? 2 : 1);
        }
        return 0;
    }

    public int getSkillBonusDamage(boolean offhand) {
        int weaponId = offhand ? player.getEquipment().getShieldId() : player.getEquipment().getWeaponId();
        ItemDefinitions defs = weaponId == -1 ? null : ItemDefinitions.getItemDefinitions(weaponId);
        int specialWeaponType = defs == null ? 0 : defs.getCSOpcode(686) != 0 && defs.getCSOpcode(2827) != 0 ? 3 : defs.getCSOpcode(686) != 0 && defs.getCSOpcode(2826) != 0 ? 2 : defs.getCSOpcode(686) != 0 && defs.getCSOpcode(2825) != 0 ? 1 : 0;
        int skillId = specialWeaponType == 1 || specialWeaponType == 0 ? Skills.STRENGTH : specialWeaponType == 2 ? Skills.RANGE : Skills.MAGIC;
        int spellId = getSpellId(offhand);
        if (spellId > 0)
            skillId = Skills.MAGIC;
        int level = player.getSkills().getLevel(skillId) + player.getAuraManager().getStatModifier(skillId);
        int speed = Math.max(weaponId == -1 ? 4 : defs.getAttackSpeed(), 4);
        int statBonus = (int) ((100.00 + ((double) speed - 4.00) * 25.00) * ((double) level * 250.00 / 100.00) / 100.00 / (offhand ? 2.00 : 1.00));
        if (skillId == Skills.MAGIC && !offhand && spellId < 1)
            statBonus = level * 250 / 100;
        if (weaponId == -1 || offhand)
            return statBonus;
        int wearpos = defs.getEquipSlot();
        int wearpos2 = defs.getEquipType();
        switch (specialWeaponType) {
        case 1:
            if (wearpos > 0 && wearpos2 > 0)
                statBonus = statBonus * 150 / 100;
            break;
        case 2:
            statBonus = (100 + (Math.min(speed, 6) - 4) * 25) * (level * 250 / 100) / 100;
            if (wearpos > 0 && wearpos2 > 0 && defs.getCSOpcode(2832) != 1)
                statBonus = statBonus * 150 / 100;
            break;
        case 3:
            if (wearpos > 0 && wearpos2 > 0)
                statBonus = statBonus * 150 / 100;
            break;
        }
        return statBonus;
    }

    private transient long mainHandDelay, offHandDelay;

    public long getMainHandDelay() {
        return mainHandDelay;
    }

    public void setMainHandDelay(long mainHandDelay) {
        this.mainHandDelay = mainHandDelay;
    }

    public long getOffHandDelay() {
        return offHandDelay;
    }

    public void setOffHandDelay(long offHandDelay) {
        this.offHandDelay = offHandDelay;
    }

    @Getter
    private transient Entity currentTarget;
    @Getter
    private transient int currentTargetMaxHP, currentTargetHP;

    private transient Object[] currentTargetData;

    private void setCurrentTarget(Entity target) {
        currentTarget = target;
        refreshManualCast(); // makes cast spell appear or not
        player.getPackets().sendCurrentTarget(target);
        if (target != null) {
            player.getInterfaceManager().sendCombatTargetInterface();
            player.getPackets().closeInterface(InterfaceManager.getComponentUId(1488, 4));
            player.getPackets().sendEntityInterface(target, true, 1488, 4, 1490);
            refreshCurrentTargetData();
        } else {
            player.getPackets().closeInterface(InterfaceManager.getComponentUId(1488, 4));
        }
    }

    private void refreshCurrentTargetData() {
        currentTargetData = getCurrentTargetData();
        player.getPackets().sendExecuteScript(82, currentTarget.getName(), currentTarget.getCombatLevel(), getCurrentTargetWeakness(), 30, 30, 0);
        updateTargetBuffs(true);
    }

    public void refreshTargetBuffs() {
        Entity currentTarget = this.currentTarget; // to prevent syc issues
        if (currentTarget == null)
            return;
        Object[] data = getCurrentTargetData();
        if (data == null || currentTargetData == null || currentTargetData[0] == null)
            return;

        if (!currentTargetData[0].equals(data[0])) // index changed such as
            // disapearing for
            // awhile(dig)
            setCurrentTarget(currentTarget); // forces to even change index(move
        // inter)
        else if (!currentTargetData[1].equals(data[1]) || !currentTargetData[2].equals(data[2]) || !currentTargetData[3].equals(data[3]))
            refreshCurrentTargetData(); // forces to refresh name/lvl/weakness +
        // forcebuff to appear
        else
            // refresh buffs normaly if any changed
            updateTargetBuffs(false);
    }

    private void updateTargetBuffs(boolean update) {
        if (!player.hasBuffTimersEnabled())
            return;

        Entity currentTarget = this.currentTarget;
        if (currentTarget == null)// to prevent syc issues
            return;

        Player targetPlayer = currentTarget instanceof Player ? (Player) currentTarget : null;

        if (targetPlayer != null) {
            int maxHp = currentTarget.getMaxHitpoints();
            if (maxHp != currentTargetMaxHP)
                player.getPackets().sendGlobalConfig(3700, currentTargetMaxHP = (currentTarget.getMaxHitpoints() * 10));
            int hp = currentTarget.getHitpoints();
            if (hp != currentTargetHP)
                player.getPackets().sendGlobalConfig(3701, currentTargetHP = (hp * 10));
        }

        if (update)
            player.updateBuffs();
    }

    public Object[] getCurrentTargetData() {
        Entity currentTarget = this.currentTarget; // to prevent syc issues
        if (currentTarget == null)
            return null;
        return new Object[] { currentTarget.getIndex(), currentTarget.getName(), currentTarget.getCombatLevel(), getCurrentTargetWeakness() };
    }

    private int getCurrentTargetWeakness() {
        Entity currentTarget = this.currentTarget; // to prevent syc issues
        if (currentTarget == null)
            return 9286; // none
        int weakness = 0;
        if (currentTarget instanceof NPC) {
            weakness = ClientScriptMap.getMap(6745).getIntValue(((NPC) currentTarget).getDefinitions().getWeaknessStyle());
            if (weakness == 197) // mobs have no melee type(only styles), thats
                // none
                weakness = 9286;
        } else {
            int weaknessType = ((Player) currentTarget).getCombatDefinitions().getWeaknessType();
            weakness = weaknessType == Combat.MELEE_TYPE ? 197 : weaknessType == Combat.RANGE_TYPE ? 200 : weaknessType == Combat.MAGIC_TYPE ? 202 : 9286;
        }
        return weakness;
    }

    public int getWeaknessType() {
        if (bonuses[MELEE_AFF] > bonuses[RANGE_AFF] && bonuses[MELEE_AFF] > bonuses[MAGIC_AFF])
            return Combat.MELEE_TYPE;
        if (bonuses[RANGE_AFF] > bonuses[MELEE_AFF] && bonuses[RANGE_AFF] > bonuses[MAGIC_AFF])
            return Combat.RANGE_TYPE;
        if (bonuses[MAGIC_AFF] > bonuses[RANGE_AFF] && bonuses[MAGIC_AFF] > bonuses[MELEE_AFF])
            return Combat.MAGIC_TYPE;
        return Combat.ALL_TYPE;
    }

    public void refreshManualCast() {
        player.getPackets().sendConfig(616, 0);
        player.getPackets().sendConfig(623, 0);
//        player.getPackets().sendExecuteScript(13513);
    }

    public boolean isNeedTargetReticuleUpdate(Entity entity) {
        return player.isTargetReticule() && !entity.isDead() && (isAttackingPlayer(entity) || entity == currentTarget);
    }

    public Graphics getTargetReticule(Entity entity) {
        if (!isNeedTargetReticuleUpdate(entity))
            return null;
        int size = entity.getSize() - 1;
        if (size > 3)
            size = 3;
        return new Graphics(((entity == currentTarget ? (isAttackingPlayer(entity) ? 4188 : 4172) : 4180) + size * 2) - 1, 0, 0, 0, true);
    }

    private boolean isAttackingPlayer(Entity entity) {
        return (entity instanceof Player && ((Player) entity).getCombatDefinitions().getCurrentTarget() == player) || (entity instanceof NPC && ((NPC) entity).getCombat().getTarget() == player);
    }

    private boolean[] meleeCombatExperienceGain;
    private boolean[] rangedCombatExperienceGain;
    private boolean[] magicCombatExperienceGain;

    public void setCombatExperienceStyle(int style) {
        int type = player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
        if (type == Combat.ALL_TYPE || type == Combat.MELEE_TYPE)
            toggleMeleeExperience(style, false);
        else if (type == Combat.RANGE_TYPE)
            toggleRangedCombatExperience(style == 2 ? 1 : style, false);
        else if (type == Combat.MAGIC_TYPE)
            toggleMagicCombatExperience(style == 2 ? 1 : style, false);
    }

    public void toggleMeleeExperience(int xpType, boolean menu) {
        boolean canToggle = !meleeCombatExperienceGain[xpType];
        for (int i = 0; i < meleeCombatExperienceGain.length; i++) {
            if (i != xpType && meleeCombatExperienceGain[i])
                canToggle = true;
        }
        if (!canToggle) {
            player.getPackets().sendInterfaceMessage(menu ? 365 : 1503, menu ? 16 : 26, 1, -1, menu ? "You cannot untick all the boxes." : "You cannot disable all XP gains.", false);
            return;
        }
        meleeCombatExperienceGain[xpType] = !meleeCombatExperienceGain[xpType];
        refreshMeleeCombatExperience();
    }

    public void toggleRangedCombatExperience(int xpType, boolean menu) {
        boolean canToggle = !rangedCombatExperienceGain[xpType];
        for (int i = 0; i < rangedCombatExperienceGain.length; i++) {
            if (i != xpType && rangedCombatExperienceGain[i])
                canToggle = true;
        }
        if (!canToggle) {
            player.getPackets().sendInterfaceMessage(menu ? 365 : 1503, menu ? 16 : 26, 1, -1, menu ? "You cannot untick all the boxes." : "You cannot disable all XP gains.", false);
            return;
        }
        this.rangedCombatExperienceGain[xpType] = !rangedCombatExperienceGain[xpType];
        refreshRangedCombatExperience();
    }

    public void toggleMagicCombatExperience(int xpType, boolean menu) {
        boolean canToggle = !magicCombatExperienceGain[xpType];
        for (int i = 0; i < magicCombatExperienceGain.length; i++) {
            if (i != xpType && magicCombatExperienceGain[i])
                canToggle = true;
        }
        if (!canToggle) {
            player.getPackets().sendInterfaceMessage(menu ? 365 : 1503, menu ? 16 : 26, 1, -1, menu ? "You cannot untick all the boxes." : "You cannot disable all XP gains.", false);
            return;
        }
        this.magicCombatExperienceGain[xpType] = !magicCombatExperienceGain[xpType];
        refreshMagicCombatExperience();
    }

    public void refreshMeleeCombatExperience() {
        if (meleeCombatExperienceGain == null) {
            meleeCombatExperienceGain = new boolean[3];
            Arrays.fill(meleeCombatExperienceGain, true);
        }
        int value = 0;
        for (int i = 0; i < meleeCombatExperienceGain.length; i++)
            value |= (meleeCombatExperienceGain[i] ? 1 : 0) << i;
        player.getPackets().sendConfigByFile(1906, value);
    }

    public void refreshRangedCombatExperience() {
        if (rangedCombatExperienceGain == null) {
            rangedCombatExperienceGain = new boolean[2];
            Arrays.fill(rangedCombatExperienceGain, true);
        }
        int value = 0;
        for (int i = 0; i < rangedCombatExperienceGain.length; i++)
            value |= (rangedCombatExperienceGain[i] ? 1 : 0) << i;
        player.getPackets().sendConfigByFile(1907, value);
    }

    public void refreshMagicCombatExperience() {
        if (magicCombatExperienceGain == null) {
            magicCombatExperienceGain = new boolean[2];
            Arrays.fill(magicCombatExperienceGain, true);
        }
        int value = 0;
        for (int i = 0; i < magicCombatExperienceGain.length; i++)
            value |= (magicCombatExperienceGain[i] ? 1 : 0) << i;
        player.getPackets().sendConfigByFile(1908, value);
    }

    public boolean[] getMeleeCombatExperienceGain() {
        if (meleeCombatExperienceGain == null) {
            meleeCombatExperienceGain = new boolean[3];
            Arrays.fill(meleeCombatExperienceGain, true);
        }
        return meleeCombatExperienceGain;
    }

    public boolean[] getRangedCombatExperienceGain() {
        if (rangedCombatExperienceGain == null) {
            rangedCombatExperienceGain = new boolean[2];
            Arrays.fill(rangedCombatExperienceGain, true);
        }
        return rangedCombatExperienceGain;
    }

    public boolean[] getMagicCombatExperienceGain() {
        if (magicCombatExperienceGain == null) {
            magicCombatExperienceGain = new boolean[2];
            Arrays.fill(magicCombatExperienceGain, true);
        }
        return magicCombatExperienceGain;
    }

    public void giveXp(Entity target, double combatXp, double hpXp) {
        if (target instanceof Player && player.isBlockPvPXP())
            return;
        player.getSkills().addXp(Skills.HITPOINTS, hpXp);
        int combatStyle = player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
        if (combatStyle == Combat.MELEE_TYPE || combatStyle == Combat.ALL_TYPE) {
            int selectedCount = 0;
            boolean[] meleeExperienceGain = player.getCombatDefinitions().getMeleeCombatExperienceGain();
            for (int i = 0; i < meleeExperienceGain.length; i++) {
                if (meleeExperienceGain[i])
                    selectedCount++;
            }
            if (selectedCount <= 0) {
                return;
            }
            double xpGain = combatXp / selectedCount;
            for (int i = 0; i < meleeExperienceGain.length; i++) {
                if (meleeExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.ATTACK : i == 1 ? Skills.STRENGTH : Skills.DEFENCE;
                    player.getSkills().addXp(skillId, xpGain);
                }
            }
        } else if (combatStyle == Combat.RANGE_TYPE) {
            int selectedCount = 0;
            boolean[] rangedExperienceGain = player.getCombatDefinitions().getRangedCombatExperienceGain();
            for (int i = 0; i < rangedExperienceGain.length; i++) {
                if (rangedExperienceGain[i])
                    selectedCount++;
            }
            if (selectedCount <= 0) {
                return;
            }
            double xpGain = combatXp / selectedCount;
            for (int i = 0; i < rangedExperienceGain.length; i++) {
                if (rangedExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.RANGE : Skills.DEFENCE;
                    player.getSkills().addXp(skillId, xpGain);
                }
            }
        } else if (combatStyle == Combat.MAGIC_TYPE) {
            int selectedCount = 0;
            boolean[] magicExperienceGain = player.getCombatDefinitions().getMagicCombatExperienceGain();
            for (int i = 0; i < magicExperienceGain.length; i++) {
                if (magicExperienceGain[i])
                    selectedCount++;
            }
            if (selectedCount <= 0) {
                return;
            }
            double xpGain = combatXp / selectedCount;
            for (int i = 0; i < magicExperienceGain.length; i++) {
                if (magicExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.MAGIC : Skills.DEFENCE;
                    player.getSkills().addXp(skillId, xpGain);
                }
            }
        }
    }

    public List<Integer> getSelectedXpGains() {
        List<Integer> xpGains = new ArrayList<Integer>();
        int combatStyle = player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
        if (combatStyle == Combat.MELEE_TYPE || combatStyle == Combat.ALL_TYPE) {
            boolean[] meleeExperienceGain = player.getCombatDefinitions().getMeleeCombatExperienceGain();
            for (int i = 0; i < meleeExperienceGain.length; i++) {
                if (meleeExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.ATTACK : i == 1 ? Skills.STRENGTH : Skills.DEFENCE;
                    xpGains.add(skillId);
                }
            }
        } else if (combatStyle == Combat.RANGE_TYPE) {
            boolean[] rangedExperienceGain = player.getCombatDefinitions().getRangedCombatExperienceGain();
            for (int i = 0; i < rangedExperienceGain.length; i++) {
                if (rangedExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.RANGE : Skills.DEFENCE;
                    xpGains.add(skillId);
                }
            }
        } else if (combatStyle == Combat.MAGIC_TYPE) {
            boolean[] magicExperienceGain = player.getCombatDefinitions().getMagicCombatExperienceGain();
            for (int i = 0; i < magicExperienceGain.length; i++) {
                if (magicExperienceGain[i]) {
                    int skillId = i == 0 ? Skills.MAGIC : Skills.DEFENCE;
                    xpGains.add(skillId);
                }
            }
        }
        return xpGains;
    }

    public void unlockMagicAbilities() {
        int spellsSize = 204;
        player.getPackets().sendIComponentSettings(1461, 7, 7, 16, 8617038);
        player.getPackets().sendIComponentSettings(1461, 1, 0, spellsSize, 8617038);
        player.getPackets().sendIComponentSettings(1459, 7, 7, 16, 8617038);
        player.getPackets().sendIComponentSettings(1459, 1, 0, spellsSize, 8617038);
        player.getPackets().sendIComponentSettings(1884, 7, 5, 16, 2);
        player.getPackets().sendIComponentSettings(1884, 1, 0, spellsSize, 8617038);
        player.getPackets().sendIComponentSettings(1885, 7, 5, 16, 2);
        player.getPackets().sendIComponentSettings(1885, 1, 0, spellsSize, 8617038);
        player.getPackets().sendIComponentSettings(1886, 7, 5, 16, 2);
        player.getPackets().sendIComponentSettings(1886, 1, 0, spellsSize, 8617038);
        player.getPackets().sendIComponentSettings(1887, 7, 5, 16, 2);
        player.getPackets().sendIComponentSettings(1887, 1, 0, spellsSize, 8617038);

    }

    public void unlockMeleeAbilities() {
        player.getPackets().sendIComponentSettings(1460, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1460, 5, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1450, 0, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1450, 3, 0, 194, 10320966);
    }

    public void unlockRangeAbilities() {
        player.getPackets().sendIComponentSettings(1452, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1452, 1, 0, 194, 10320966);
        player.getPackets().sendIComponentSettings(1456, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1456, 1, 0, 194, 10320966);
    }

    public void unlockDefenceAbilities() {
        player.getPackets().sendIComponentSettings(1883, 1, 0, 194, 10320966);
        player.getPackets().sendIComponentSettings(1883, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1880, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1880, 1, 0, 194, 10320966);
    }


    @Getter
    private boolean isOnStrengthMenu, isOnConstitutionMenu;





    public void setIsOnStrengthMenu(boolean isOnStrengthMenu) {
        this.isOnStrengthMenu = isOnStrengthMenu;
        refreshIsOnStrengthMenu();
    }

    public void refreshIsOnStrengthMenu() {
        player.getPackets().sendConfigByFile(18786, isOnStrengthMenu ? 1 : 0);
        player.getPackets().sendConfigByFile(18787, isOnStrengthMenu ? 1 : 0);
    }

    public void setIsOnConstitutionMenu(boolean isOnConstitutionMenu) {
        this.isOnConstitutionMenu = isOnConstitutionMenu;
        refreshIsOnConstitutionMenu();
    }

    public void refreshIsOnConstitutionMenu() {
        player.getPackets().sendConfigByFile(36453, isOnConstitutionMenu ? 1 : 0);
        player.getPackets().sendConfigByFile(36454, isOnConstitutionMenu ? 1 : 0);
    }




    @Getter
    private int mainHandSpellId, offHandSpellId;

    @Getter
    private boolean manualSpellCasting;

    public void switchManualSpellCasting() {
        manualSpellCasting = !manualSpellCasting;
        refreshManualSpellCasting();
    }

    private void refreshManualSpellCasting() {
        player.getPackets().sendConfigByFile(22843, manualSpellCasting ? 1 : 0);
        player.getPackets().sendExecuteScript(13513);
        refreshManualCast();
    }

    public void setAutoCast(int spellId) {
        if (spellId == -1) {
            mainHandSpellId = 0;
            offHandSpellId = 0;
            refreshMainHandSpell();
            refreshOffHandSpell();
            refreshBonuses();
            return;
        }
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        Item offHand = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
        if (weapon != null && weapon.getDefinitions().getType() != Combat.MAGIC_TYPE)
            weapon = null;
        if (offHand != null && offHand.getDefinitions().getType() != Combat.MAGIC_TYPE)
            offHand = null;
        if ((weapon == null && offHand == null) || (weapon != null && offHand == null)) {
            mainHandSpellId = mainHandSpellId == spellId ? 0 : spellId;
            if (offHandSpellId == spellId)
                offHandSpellId = 0;
            if (weapon != null && weapon.getDefinitions().getEquipType() == 5)
                offHandSpellId = spellId;
            player.getPackets().sendGameMessage(mainHandSpellId == 0 ? "Auto-cast spell cleared." : "Main-hand spell set to: " + Magic.getSpellName(Magic.getSpellData(spellId)) + ".");
        } else if (weapon == null && offHand != null) {
            offHandSpellId = offHandSpellId == spellId ? 0 : spellId;
            if (mainHandSpellId == spellId)
                mainHandSpellId = 0;
            player.getPackets().sendGameMessage(offHandSpellId == 0 ? "Off-hand spell cleared." : "Off-hand spell set to: " + Magic.getSpellName(Magic.getSpellData(spellId)) + ".");
        } else {
            if (mainHandSpellId == spellId && offHandSpellId == spellId) {
                mainHandSpellId = 0;
                offHandSpellId = 0;
                player.getPackets().sendGameMessage("Auto-cast spells cleared.");
            } else {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        if (spellId != mainHandSpellId && spellId != offHandSpellId)
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as main hand.", "Set as off hand.", "Set main hand and off hand.");
                        else if (spellId == mainHandSpellId)
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Clear main hand.", "Set as off hand.");
                        else if (spellId == offHandSpellId)
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as main hand.", "Clear off hand.");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        switch (componentId) {
                        case OPTION_1:
                            if (spellId == mainHandSpellId) {
                                mainHandSpellId = 0;
                                player.getPackets().sendGameMessage("Main-hand spell cleared.");
                            } else {
                                mainHandSpellId = spellId;
                                player.getPackets().sendGameMessage("Main-hand spell set to: " + Magic.getSpellName(Magic.getSpellData(spellId)) + ".");
                            }
                            break;
                        case OPTION_2:
                            if (spellId == offHandSpellId) {
                                offHandSpellId = 0;
                                player.getPackets().sendGameMessage("Off-hand spell cleared.");
                            } else {
                                offHandSpellId = spellId;
                                player.getPackets().sendGameMessage("Off-hand spell set to: " + Magic.getSpellName(Magic.getSpellData(spellId)) + ".");
                            }
                            break;
                        case OPTION_3:
                            mainHandSpellId = spellId;
                            offHandSpellId = spellId;
                            player.getPackets().sendGameMessage("Main-hand and Off-hand spell set to: " + Magic.getSpellName(Magic.getSpellData(spellId)) + ".");
                            break;
                        }
                        end();
                    }

                    @Override
                    public void finish() {
                        refreshMainHandSpell();
                        refreshOffHandSpell();
                        refreshBonuses();
                    }
                });
                return;
            }
        }
        refreshMainHandSpell();
        refreshOffHandSpell();
        refreshBonuses();
        player.getPackets().sendExecuteScript(13513);
    }

    public void refreshMainHandSpell() {
        player.getPackets().sendConfigByFile(43, mainHandSpellId);
    }

    public void refreshOffHandSpell() {
        player.getPackets().sendConfigByFile(18050, offHandSpellId);
    }

    public int getSkillAccuracy(boolean offhand) {
        int handType = getType(offhand ? Equipment.SLOT_SHIELD : Equipment.SLOT_WEAPON);
        return (int) (Combat.getStatBonus(player.getSkills().getLevel(handType == Combat.MAGIC_TYPE ? Skills.MAGIC : handType == Combat.RANGE_TYPE ? Skills.RANGE : Skills.ATTACK)) * 1);
    }

    public int getDefenceArmor() {
        return (int) (Combat.getStatBonus(player.getSkills().getLevel(Skills.DEFENCE)) * 1);
    }

    public void resetSpells(final boolean removeAutoSpell) {
        player.getTemporaryAttributtes().remove("tempCastSpell");
        if (removeAutoSpell) {
            setAutoCast(-1);
        }
        player.getCombatDefinitions().refreshBonuses();
    }

    public void refreshBorrowedSpell() {
        player.getPackets().sendConfig(3170, player.getBorrowedSpellId() != 0 ? Magic.getSpellData(player.getBorrowedSpellId()).getId() : 0);
        player.getPackets().sendConfigByFile(38567, player.getBorrowedSpellId() != 0 ? 1 : 0);
        player.getPackets().sendExecuteScript(13513);
        refreshSpellBook();
    }

    private boolean filteredAbilities;

    public void toggleFilteredAbilities() {
        filteredAbilities = !filteredAbilities;
        refreshFilteredAbilities();
    }

    private void refreshFilteredAbilities() {
        player.getPackets().sendConfigByFile(27344, filteredAbilities ? 1 : 0);
        player.getPackets().sendConfigByFile(44637, filteredAbilities ? 1 : 0);
    }

    public void setDefenceMenu(int menu) {
        player.getVarsManager().setVarBit(18793, menu);
    }

    public void setMagicAbilityMenu(int menu) {
        player.getVarsManager().forceSendVarBit(18791, menu);
    }

    public void setStrengthMenu(int menu) {
        player.getVarsManager().setVarBit(18787, menu);
    }


}
