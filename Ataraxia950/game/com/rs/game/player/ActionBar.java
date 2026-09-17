package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.InventoryOptionsHandler;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class ActionBar implements Serializable {

    private static final long serialVersionUID = -3709650400651780336L;

    public static final int ITEM_SHORTCUT = 0, MELEE_ABILITY_SHORTCUT = 1, STRENGTH_ABILITY_SHORTCUT = 2, RANGED_ABILITY_SHORTCUT = 5, DEFENCE_ABILITY_SHORTCUT = 3, HEAL_ABILITY_SHORTCUT = 4, MAGIC_ABILITY_SHORTCUT = 6, PRAYER_SHORTCUT = 7, SUMMONING_ORB_SHORTCUT = 13;
    private static long BAR_CYCLE;

    public static final int[] CS_DATA_ID = { 6734, 6735, 6736, 6737, 6738, 6740, 6739, 3746  };

    public int currentBar;
    public boolean lockedBar;
    private boolean blockIncomingShareOffers;
    private Shortcut[][] shortcuts;
    private int[] multiActionBar;

    private transient Player player;
    private transient Map<Integer, Long> cooldowns;
    private transient long globalCooldown;

    public ActionBar() {
        shortcuts = new Shortcut[10][14];
        multiActionBar = new int[4];
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void init() {
        cooldowns = new HashMap<>();
        if (multiActionBar == null)
            multiActionBar = new int[4];
        if (shortcuts.length < 10) {
            Shortcut[][] tempShortcuts = new Shortcut[10][14];
            for (int i = 0; i < shortcuts.length; i++)
                for (int j = 0; j < shortcuts[i].length; j++)
                    tempShortcuts[i][j] = shortcuts[i][j];
            shortcuts = tempShortcuts;
        }
        refreshActionBar();
        refreshLockBar();
        refreshBlockIncomingShareOffers();
        refreshCurrentActionBarComponent();
    }

    public void refreshCurrentActionBarComponent() {
        player.getPackets().sendUnlockIComponentOptionSlots(1430, 254, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        for (int i = 0; i < multiActionBar.length; i++) {
            player.getPackets().sendUnlockIComponentOptionSlots(1670 + i, 6, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        }
    }

    public void switchLockBar() {
        lockedBar = !lockedBar;
        refreshLockBar();
        unlockShortcuts(false);
        player.getPackets().sendGameMessage("The action bar is now " + (lockedBar ? "" : "un") + "locked.");
    }

    public void refreshLockBar() {
        player.getPackets().sendConfigByFile(1892, lockedBar ? 1 : 0);
    }

    public void switchBlockIncomingShareOffers() {
        blockIncomingShareOffers = !blockIncomingShareOffers;
        refreshBlockIncomingShareOffers();
    }

    private void refreshBlockIncomingShareOffers() {
        player.getPackets().sendConfigByFile(22131, blockIncomingShareOffers ? 1 : 0);
    }

    public void increaseCurrentBar() {
        setCurrentBar(currentBar == shortcuts.length - 1 ? 0 : currentBar + 1);
    }

    public void decreaseCurrentBar() {
        setCurrentBar(currentBar == 0 ? shortcuts.length - 1 : currentBar - 1);
    }

    public void unlockActionBar(boolean menu) {
        if (shortcuts.length < 10) {
            Shortcut[][] tempShortcuts = new Shortcut[10][14];
            for (int i = 0; i < shortcuts.length; i++)
                for (int j = 0; j < shortcuts[i].length; j++)
                    tempShortcuts[i][j] = shortcuts[i][j];
            shortcuts = tempShortcuts;
        }
        unlockShortcuts(menu);
        if (!menu) {
            player.getPackets().sendIComponentSettings(1430, 17, -1, -1, 8388608);
            player.getPackets().sendIComponentSettings(1430, 11, -1, -1, 8650758);
            player.getPackets().sendIComponentSettings(1430, 22, -1, -1, 8388608);
            player.getPackets().sendIComponentSettings(1430, 16, -1, -1, 2046);
        }
        refreshButtons();
        refreshMultiActionBar();
    }

    public void refreshButtons() {
        player.getPackets().sendExecuteScript(6992);
    }

    public void unlockShortcuts(boolean menu) {
        for (int i = 0; i < shortcuts[currentBar].length; i++) {
            player.getPackets().sendIComponentSettings(menu ? 1436 : 1430, (menu ? 22 : 69) + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
            player.getPackets().sendIComponentSettings(menu ? 1436 : 1430, (menu ? 17 : 64) + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
        }
        for (int i = 0; i < shortcuts[currentBar].length; i++) {
            player.getPackets().sendIComponentSettings(1670, 23 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
            player.getPackets().sendIComponentSettings(1670, 18 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
        }
        for (int i = 0; i < shortcuts[currentBar].length; i++) {
            player.getPackets().sendIComponentSettings(1671, 18 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
            player.getPackets().sendIComponentSettings(1671, 13 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
        }
        for (int i = 0; i < shortcuts[currentBar].length; i++) {
            player.getPackets().sendIComponentSettings(1672, 18 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
            player.getPackets().sendIComponentSettings(1672, 13 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
        }
        for (int i = 0; i < shortcuts[currentBar].length; i++) {
            player.getPackets().sendIComponentSettings(1673, 18 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
            player.getPackets().sendIComponentSettings(1673, 13 + i * 13, -1, 1, !lockedBar || menu ? 11108350 : 2195454);
        }
    }

    public void refreshActionBar() {
        unlockActionBar(false);
        player.getPackets().sendConfigByFile(1893, currentBar + 1);
        for (int i = 0; i < shortcuts[currentBar].length; i++)
            refresh(i, false);
        refreshMultiActionBar();
        player.getPackets().sendConfigByFile(27893, 2);
    }

    public int getCurrentBar() {
        return currentBar;
    }

    public void refresh(int index, boolean force) {
        refresh(index, currentBar, force);
    }

    public void refresh(int index, int currentBar, boolean force) {
        Shortcut shortcut = shortcuts[currentBar][index];
        if (shortcut == null) {
            sendShortcutVar(index, currentBar, 0, -1, force);
            return;
        }
        sendShortcutVar(index, currentBar, shortcut.getType(), shortcut.getId(player), force);
    }

    public void sendShortcutVar(int barIndex, int currentBar, int type, int id, boolean force) {
        if (force) {
            player.getPackets().sendConfig(currentBar >= 5 ? (5335 + (14 * (currentBar - 5)) + barIndex) : (barIndex >= 12 ? (4429 + 2 * currentBar) + (barIndex - 12) : ((823 + 12 * currentBar) + barIndex)), type != 0 ? -1 : id);
            player.getPackets().sendConfig(currentBar >= 5 ? (5265 + (14 * (currentBar - 5)) + barIndex) : (barIndex >= 12 ? (4415 + 2 * currentBar) + (barIndex - 12) : ((739 + 12 * currentBar) + barIndex)), type != 0 ? (id << 4 | type) : 0);
        } else {
            player.getPackets().sendConfig(currentBar >= 5 ? (5335 + (14 * (currentBar - 5)) + barIndex) : (barIndex >= 12 ? (4429 + 2 * currentBar) + (barIndex - 12) : ((823 + 12 * currentBar) + barIndex)), type != 0 ? -1 : id);
            player.getPackets().sendConfig(currentBar >= 5 ? (5265 + (14 * (currentBar - 5)) + barIndex) : (barIndex >= 12 ? (4415 + 2 * currentBar) + (barIndex - 12) : ((739 + 12 * currentBar) + barIndex)), type != 0 ? (id << 4 | type) : 0);
        }
    }

    public int[] getMultiActionBar() {
        if (multiActionBar == null)
            multiActionBar = new int[4];
        return multiActionBar;
    }

    public void setMultiActionBar(int index, int bar) {
        if (bar == 0) {
            this.multiActionBar[index] = bar;
            refreshMultiActionBar();
            return;
        }
        if (multiActionBar[index] == bar)
            return;
        if (currentBar == bar - 1) {
            currentBar = multiActionBar[index] - 1;
            refreshActionBar();
        }
        for (int i = 0; i < multiActionBar.length; i++) {
            if (index == i)
                continue;
            if (multiActionBar[i] == bar) {
                multiActionBar[i] = multiActionBar[index];
                break;
            }
        }
        this.multiActionBar[index] = bar;
        refreshMultiActionBar();
    }

    public void refreshMultiActionBar() {
        if (multiActionBar == null)
            multiActionBar = new int[4];
        for (int i = 0; i < multiActionBar.length; i++) {
            player.getPackets().sendConfigByFile(29138 + i, (player.isInLegacyCombatMode() || player.isInLegacyInterfaceMode()) ? 0 : multiActionBar[i]);
            player.getPackets().sendExecuteScriptReverse((multiActionBar[i] == 0 || (player.isInLegacyCombatMode() || player.isInLegacyInterfaceMode())) ? 8320 : 8310, 1032 + i);
            if (multiActionBar[i] != 0)
                for (int j = 0; j < shortcuts[multiActionBar[i] - 1].length; j++)
                    refresh(j, multiActionBar[i] - 1, false);
        }
    }

    public void pushShortcut(int slotId2, int index) {
        pushShortcut(index, slotId2, PacketRepository.ACTION_BUTTON1_PACKET);
    }

    public void pushShortcut(int index, int slotId2, int packetId) {
        pushShortcut(currentBar, index, slotId2, packetId);
    }

    public void pushShortcut(int currentBar, int index, int slotId2, int packetId) {
        // examine replaced with customize keybind
        if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
            player.getInterfaceManager().openMenu(9, 3);
            return;
        }
        Shortcut shortcut = shortcuts[currentBar][index];
        if (shortcut == null)
            return;
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsBankInterface()) {
            player.getPackets().sendGameMessage("You're currently busy, and can't do that right now.");
            return;
        }
        processShortcut(shortcut, packetId, index, false);
    }

    public void processShortcut(Shortcut shortcut, int packetId, int shortcutIndex, boolean queue) {
        if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
            if ((shortcut.getType() == MELEE_ABILITY_SHORTCUT || shortcut.getType() == DEFENCE_ABILITY_SHORTCUT || shortcut.getType() == STRENGTH_ABILITY_SHORTCUT || shortcut.getType() == RANGED_ABILITY_SHORTCUT || shortcut.getType() == HEAL_ABILITY_SHORTCUT || shortcut.getType() == MAGIC_ABILITY_SHORTCUT) && hasCooldown(shortcut)) {
                player.getPackets().sendGameMessage("Ability not ready yet.");
                return;
            }
        }
        switch (shortcut.getType()) {
            case ITEM_SHORTCUT:
                if (player.getInterfaceManager().containsInventoryInter())
                    return;
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(shortcut.getId(player));
                if (defs.isWearItem() && packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    Item item = player.getEquipment().getItem(defs.getEquipSlot());
                    if (item != null && item.getId() == shortcut.getId(player)) {
                        player.getEquipment().handleEquipment(1464, 14, defs.getEquipSlot(), shortcut.getId(player), PacketRepository.ACTION_BUTTON1_PACKET);
                        return;
                    }
                }
                ItemShortcut ishortcut = (ItemShortcut) shortcut;
                int actualItemId = getActualUseItemId(ishortcut.getItem().getId());
                Item actualItem = new Item(actualItemId, ishortcut.getItem().getAmount(), ishortcut.getItem().getCharges()).setAttributes(ishortcut.getItem().getAttributes());
                int slotId = player.getInventory().getItems().lookupSlot(actualItem);
                Item item = player.getInventory().getItem(slotId);
                if (slotId == -1) {
                    player.getPackets().sendGameMessage("You don't have any left!");
                    return;
                }
                if ((item == null || (actualItemId == shortcut.getId(player) && item.getId() != shortcut.getId(player)))) {// && !Drinkables.isDrinkableId(player, item.getId())
                    player.getPackets().sendGameMessage("You don't have any left!");
                    return;
                }

                switch (packetId) {
                    case PacketRepository.ACTION_BUTTON1_PACKET:
                        InventoryOptionsHandler.handleItemOption1(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON2_PACKET:
                        InventoryOptionsHandler.handleItemOption2(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON3_PACKET:
                        InventoryOptionsHandler.handleItemOption3(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON4_PACKET:
                        InventoryOptionsHandler.handleItemOption4(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON5_PACKET:
                        InventoryOptionsHandler.handleItemOption5(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON6_PACKET:
                        InventoryOptionsHandler.handleItemOption6(player, slotId, shortcut.getId(player), item);
                        break;
                    case PacketRepository.ACTION_BUTTON7_PACKET:
                        InventoryOptionsHandler.handleItemOption7(player, slotId, shortcut.getId(player), item);
                        break;
                }
                break;
            case PRAYER_SHORTCUT:
                if (!((PrayerShortcut) shortcut).preset && ((PrayerShortcut) shortcut).curse != player.getPrayer().isAncientCurses()) {
                    player.getPackets().sendMainInterfaceMessage(1, "You need to be on " + (((PrayerShortcut) shortcut).curse ? "ancient curses " : "normal ") + " prayer book to use this prayer.", true);
                    return;
                }
                if (((PrayerShortcut) shortcut).preset)
                    player.getPrayer().switchQuickPrayers(((PrayerShortcut) shortcut).prayerId);
                else
                    player.getPrayer().delayUsePrayer(((PrayerShortcut) shortcut).prayerId, false);
                break;
            case MELEE_ABILITY_SHORTCUT:
            case DEFENCE_ABILITY_SHORTCUT:
            case RANGED_ABILITY_SHORTCUT:
            case HEAL_ABILITY_SHORTCUT:
            case STRENGTH_ABILITY_SHORTCUT:
                triggerAbility(shortcut);
                break;
            case MAGIC_ABILITY_SHORTCUT:
                int abilityId = shortcut.getId(player);
                // abilty ids here to distinguish from normal spells
                if (isMagicAbility(abilityId)) {
                    triggerAbility(shortcut);
                } else
                    Magic.handleSpell(player, abilityId, packetId);
                break;
            case SUMMONING_ORB_SHORTCUT:
//            Entity target = player.getCombatDefinitions().getCurrentTarget();
//            if (target == null || target.isDead() || target.hasFinished()) {
//                player.getPackets().sendGameMessage("You don't have a target.");
//                return false;
//            }
//            if (!player.withinDistance(target)) {
//                player.getPackets().sendGameMessage("Your target is too far away.");
//                return false;
//            }
//            switch (packetId) {
//            case PacketRepository.ACTION_BUTTON1_PACKET:
//                PetPerkInterface.sendInterface(player);
//                break;
//            case PacketRepository.ACTION_BUTTON2_PACKET:
//                player.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
//                if (player.getFamiliar() != null)
//                    player.getFamiliar().sendFollowerDetails();
//                if (player.getPet() != null)
//                    player.getPet().sendFollowerDetails();
//                break;
//            case PacketRepository.ACTION_BUTTON3_PACKET:
//                Familiar.selectLeftOption(player);
//                break;
//            }
                player.getPackets().sendGameMessage("This option is not added yet.(use the familiar orb)");
                break;
        }
    }
    private long surgeEscapeCooldown1;
    private void triggerAbility(Shortcut shortcut) {
        if (!player.getControlerManager().canUseAbility(shortcut))
            return;
        int bookId = shortcut.getType();
        int abilityId = shortcut.getId(player);

        int mapId = ClientScriptMap.getMap(CS_DATA_ID[bookId - 1]).getIntValue(abilityId);
        GeneralRequirementMap data = GeneralRequirementMap.getMap(mapId);
        if (!hasRequiriments(abilityId, data, true, false))
            return;
        int abilityType = data.getIntValue(2799);
        switch (bookId) {
            case MAGIC_ABILITY_SHORTCUT:
                switch (abilityId) {
                    case 2:// SURGE

                        final WorldTile tile = getSurgeTile(player, 0, 10, true);
                        if (tile == null || tile.matches(player)) {
                            player.getPackets().sendGameMessage("Destination unreachable.");
                            return;
                        }
                        player.lock(2);
                        player.setSurgeEscapeCooldown();
                        player.setNextAnimation(new Animation(18358));
                        player.setNextGraphics(new Graphics(3537, 5, 0));
                        player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(Utils.getAngle(tile.getX() - player.getX(), tile.getY() - player.getY()))));
                        player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                player.getEffectsManager().removeEffect(EffectType.DISMEMBER);
                                player.setNextWorldTile(tile);
                            }
                        }, 1);
                        break;
                }
                break;
            case RANGED_ABILITY_SHORTCUT:
                switch (abilityId) {
                    case 3:// ESCAPE

                        player.lock(2);
                        final WorldTile tile = getSurgeTile(player, 0, -10, false);
                        if (tile == null || tile.matches(player)) {
                            player.getPackets().sendGameMessage("Destination unreachable.");
                            return;
                        }
                        player.setNextAnimation(new Animation(18527));
                        player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
                        player.setNextGraphics(new Graphics(3526, 0, 0));
                        player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(Utils.getAngle(player.getX() - tile.getX(), player.getY() - tile.getY()))));
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                player.getEffectsManager().removeEffect(EffectType.DISMEMBER);
                                player.setNextWorldTile(tile);
                            }
                        }, 1);
                        break;
                }
                break;
            case MELEE_ABILITY_SHORTCUT:
            case STRENGTH_ABILITY_SHORTCUT:
                switch (abilityId) {
                    case 2:// BARGE

                        WorldTile tile = getSurgeTile(player, 0, 10, true);
                        if (tile == null || tile.matches(player)) {
                            player.getPackets().sendGameMessage("Destination unreachable.");
                            return;
                        }
                        Entity target = player.getCombatDefinitions().getCurrentTarget();
                        if (target != null) {
                            if (!player.withinDistance(target, 10)) {
                                player.getPackets().sendGameMessage("Too far from target to perform this ability!");
                                return;
                            }
                            PlayerCombat combat;
                            if (player.getActionManager().getAction() instanceof PlayerCombat)
                                combat = (PlayerCombat) player.getActionManager().getAction();
                            else {
                                combat = new PlayerCombat(target);
                                PlayerCombat.setWeaponAbilityDelay(player);
                                player.getActionManager().setAction(combat);
                            }
                            player.faceEntity(target);
                            tile = getSurgeTile(player, 0, 10, true, target);
                            if (tile == null) {
                                player.getPackets().sendGameMessage("Destination unreachable.");
                                return;
                            }
                        }
                        player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
                        player.lock(2);
                        player.setNextAnimation(new Animation(18147));
                        player.setNextGraphics(new Graphics(3580));
                        player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(player.getDirection())));
                        final WorldTile lastTile = tile;
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                player.setNextWorldTile(lastTile);
                            }
                        }, 1);
                        break;
                }
                break;
            case DEFENCE_ABILITY_SHORTCUT:
            case HEAL_ABILITY_SHORTCUT:
                switch (abilityId) {
                    case 2:// FREEDOM
                        if (!PlayerCombat.pressFreedom(player))
                            return;
                        break;
                    case 3:// PROVOKE
//              player.getEffectsManager().startEffect(effect = new Effect(EffectType.FREEDOM, 10));
                        break;
                }
                break;
        }
        // adrenaline drain {





        Perk mobile = player.getInventionManager().hasPerk(Perks.MOBILE);

        boolean hasMobileEffect = mobile != null && (((bookId == ActionBar.MAGIC_ABILITY_SHORTCUT || bookId == ActionBar.MELEE_ABILITY_SHORTCUT) && abilityId == 2) || (bookId == ActionBar.RANGED_ABILITY_SHORTCUT && abilityId == 3));
        Perk turtling = player.getInventionManager().hasPerk(Perks.TURTLING);
        int cd = data.getIntValue(2796) / (hasMobileEffect ? 2 : 1);
        if (turtling != null && ((bookId == ActionBar.DEFENCE_ABILITY_SHORTCUT || bookId == ActionBar.HEAL_ABILITY_SHORTCUT) && abilityId == 2))
            cd -= turtling.getRank() * 5;
        setCooldownShared(mapId, cd);
        if (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.DOUBLE_SURGE));






        setCooldown(14881, 3);
    }

    private static final String[] ABILITY_STYLES = { "melee", "melee", "ranged", "magic" };
    private static final int THRESHOLD_ABILITY = 1;

    private static boolean hasAbilityStyle(Player player, int abilityStyle, boolean mainHand) {
        int handStyle = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        return ((abilityStyle == 1 || abilityStyle == 2) && handStyle == Combat.MELEE_TYPE) || (abilityStyle == 3 && handStyle == Combat.RANGE_TYPE) || (abilityStyle == 4 && handStyle == Combat.MAGIC_TYPE) || abilityStyle == 6;
    }

    private boolean hasRequiriments(int abilityId, GeneralRequirementMap data, boolean warn, boolean setup) {
        int abilityStyle = data.getIntValue(2806);
        if (abilityStyle > 0) {
            int level = data.getIntValue(2807);
            if (level > 0) {
                int skill = abilityStyle == 1 ? Skills.ATTACK : abilityStyle == 2 ? Skills.STRENGTH : abilityStyle == 3 ? Skills.RANGE : abilityStyle == 4 ? Skills.MAGIC : abilityStyle == 5 ? Skills.DEFENCE : abilityStyle == 6 ? Skills.HITPOINTS : -1;
                if (skill != -1) {
                    if (player.getSkills().getLevel(skill) < level) {
                        if (warn)
                            player.getPackets().sendGameMessage("You require level " + level + " " + Skills.SKILL_NAME[skill] + " in order to use this ability.");
                        return false;
                    }
                }
            }
            if ((abilityStyle == 3 && abilityId == 3) || (abilityStyle == 4 && abilityId == 2) || (abilityStyle == 2 && abilityId == 2) || (abilityStyle == 1 && abilityId == 6))
                return true;// The 'jump' abilities and backhand/ kick
            if (abilityStyle == 5) {
                if (!player.getEquipment().hasShield() && data.getIntValue(2813) != 0) {
                    if (warn)
                        player.getPackets().sendGameMessage("This ability requires a shield.");
                    return false;
                }
            } else {
                if (!hasAbilityStyle(player, abilityStyle, true)) {
                    if (warn)
                        player.getPackets().sendGameMessage("This ability requires a " + ABILITY_STYLES[abilityStyle - 1] + " weapon in your main hand.");
                    return false;
                }
            }
            boolean offHandRequired = data.getIntValue(2811) == 1;
            if (offHandRequired) {
                boolean hasOffHand = player.getEquipment().hasOffHand();
                if (!hasOffHand || !hasAbilityStyle(player, abilityStyle, false)) {
                    if (warn)
                        player.getPackets().sendGameMessage("You need to be dual wielding " + ABILITY_STYLES[abilityStyle - 1] + " weapons to use this ability.");
                    return false;
                }
            }
            boolean twoHandRequired = data.getIntValue(2812) == 1;
            if (twoHandRequired && !player.getEquipment().hasTwoHandedWeapon()) {
                if (warn)
                    player.getPackets().sendGameMessage("You need a two-handed weapon to use this ability.");
                return false;
            }

            int abilityType = data.getIntValue(2799);
            // 0 - normal - 1 thresold - 2 - ultimate
            if (!setup && abilityType != 0 && player.getCombatDefinitions().getSpecialAttackPercentage() < (abilityType == THRESHOLD_ABILITY ? 50 : 100)) {
                if (warn)
                    player.getPackets().sendGameMessage((abilityType == THRESHOLD_ABILITY ? "Threshold" : "Ultimate") + " abilities require you to have " + (abilityType == THRESHOLD_ABILITY ? 50 : 100) + "% adrenaline before they can be used.");
                return false;
            }
        }
        return true;
    }



    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment) {
        return getSurgeTile(player, start, end, increment, null);
    }

    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment, Entity target) {
        byte[] dirs = Utils.getDirection(player.getDirection());
        WorldTile lastStep = null;
        for (int steps = start; increment ? steps < end : steps > end; steps += (increment ? 1 : -1)) {
            WorldTile step = new WorldTile(player.getX() + (dirs[0] * steps), player.getY() + (dirs[1] * steps), player.getPlane());
            if (target != null && Utils.colides(target.getX(), target.getY(), target.getSize(), step.getX(), step.getY(), player.getSize()) || !player.getControlerManager().addWalkStep(player.getX(), player.getY(), step.getX(), step.getY()) || !player.clipedProjectile(step, true) || !World.isTileFree(step.getPlane(), step.getX(), step.getY(), player.getSize()))
                break;
            lastStep = step;
        }
        return lastStep;
    }

    private static boolean isMagicAbility(int abilityId) {
        // abilty ids here to distinguish from normal spells
        return (abilityId == 7 || abilityId == 1 || abilityId == 12 || abilityId == 6 || abilityId == 165 || abilityId == 3 || abilityId == 166 || abilityId == 171 || abilityId == 5 || abilityId == 2 || abilityId == 8 || abilityId == 4 || abilityId == 9 || abilityId == 10 || abilityId == 11 || abilityId == 164 || abilityId == 169);
    }

    public void setShortcut(int currentBar, int index, Shortcut shortcut) {
        if (shortcuts[currentBar][index] != null && shortcuts[currentBar][index].getType() == shortcut.getType() && shortcuts[currentBar][index].getId(player) == shortcut.getId(player))
            return;
        if (shortcut instanceof ItemShortcut && shortcuts[currentBar][index] instanceof ItemShortcut) {
            Item item1 = ((ItemShortcut) shortcuts[currentBar][index]).getItem();
            Item item2 = ((ItemShortcut) shortcut).getItem();
            if (item1 != null && item2 != null && item1.exactMatch(item2))
                return;
        }
        shortcuts[currentBar][index] = shortcut;
        refresh(index, currentBar, true);
        refreshButtons();
    }

    public void clearShortcut(int index) {
        clearShortcut(currentBar, index);
    }

    public void clearShortcut(int currentBar, int index) {
        if (shortcuts[currentBar][index] == null)
            return;
        shortcuts[currentBar][index] = null;
        refresh(index, currentBar, true);
        refreshButtons();
    }

    public void clearBar(int currentBar) {
        if (currentBar == -1)
            currentBar = this.currentBar;
        shortcuts[currentBar] = new Shortcut[14];
        refreshActionBar();
        refreshButtons();
    }

    public void switchShortcut(int fromBar, int toBar, int fromIndex, int toIndex) {
        if (shortcuts[fromBar][fromIndex] == null)
            return;
        Shortcut fromShortcut = shortcuts[fromBar][fromIndex];
        Shortcut toShortcut = shortcuts[toBar][toIndex];
        shortcuts[fromBar][fromIndex] = toShortcut;
        shortcuts[toBar][toIndex] = fromShortcut;
        refresh(fromIndex, fromBar, true);
        refresh(toIndex, toBar, true);
        refreshButtons();
    }

    public void pushShortcutOnSomething(int currentBar, int index, Object target) {
        Shortcut shortcut = shortcuts[currentBar][index];
        if (shortcut == null)
            return;
        pushShortcutOnSomething(shortcut, target);
    }

    public void pushShortcutOnSomething(int index, Object target) {
        pushShortcutOnSomething(currentBar, index, target);
    }

    private void pushShortcutOnSomething(Shortcut shortcut, Object target) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsBankInterface()) {
            player.getPackets().sendGameMessage("You're currently busy, and can't do that right now.");
            return;
        }
        switch (shortcut.getType()) {
            case ITEM_SHORTCUT:
                break;
            case MAGIC_ABILITY_SHORTCUT:
                if (target instanceof FloorItem) {
                    Magic.handleSpellOnFloorItem(player, shortcut.getId(player), (FloorItem) target);
                } else if (target instanceof Item) {
                    int slotId = player.getInventory().getItems().lookupSlot(((Item) target));
                    if (slotId == -1)
                        return;
                    Magic.handleSpellOnItem(player, shortcut.getId(player), (byte) slotId, true);
                } else if (target instanceof Entity)
                    Magic.handleSpellOnEntity(player, shortcut.getId(player), (Entity) target);
                else if (target instanceof WorldObject)
                    Magic.handleSpellOnWorldObject(player, shortcut.getId(player), (WorldObject) target);
                break;
        }
    }

    public void useAbility(Shortcut shortcut, int packetId) {
        processShortcut(shortcut, packetId, -1, false);
    }

    public void useAbility(Shortcut shortcut, Object target) {
        pushShortcutOnSomething(shortcut, target);
    }

    public void setCurrentBar(int id) {
        setCurrentBar(id, true);
    }

    public void setCurrentBar(int id, boolean sendMessage) {
        if (currentBar == id)
            return;
        for (int i = 0; i < multiActionBar.length; i++) {
            if (multiActionBar[i] - 1 == id) {
                if (sendMessage)
                    player.getPackets().sendExecuteScript(1211, "That bar is already in use.", 0, -120, 1);
                return;
            }
        }
        currentBar = id;
        refreshActionBar();
    }

    public void helpTrashCan() {
        player.getPackets().sendGameMessage("To remove an icon from the action bar, drag it onto the trashcan or the game world. Alternatively, you can select 'clear all' from the trashcan to remove all icons on your action bar.");
    }

    public static GeneralRequirementMap getAbilityData(int type, int abilityId) {
        int id = ClientScriptMap.getMap(CS_DATA_ID[type - 5]).getIntValue(abilityId);
        return id == -5 ? null : GeneralRequirementMap.getMap(id);
    }

    public long getCoolDown(int abilityMap) {
        Long cd = cooldowns.get(abilityMap);
        if (cd == null || cd < BAR_CYCLE)
            return 0;
        return (cd - BAR_CYCLE);
    }

    public static void addActionBarTask() {
        CoresManager.getServiceProvider().scheduleRepeatingTask(new Runnable() {

            @Override
            public void run() {
                try {
                    BAR_CYCLE++;
                } catch (Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }

        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private static final int[][] SHARED_CD_MAPS = { { 14682, 14699, 14664, 14664,14666 }, // Binding
            // abilities

            { 14706, 14731 },// Asphxyiate, Destroy
    };

    // 2069 - Revenge

    public void setCooldownShared(int abilityMap, int cooldown) {
        for (int[] sharedAbilityMaps : SHARED_CD_MAPS) {
            for (int sharedAbilityId : sharedAbilityMaps) {
                if (sharedAbilityId == abilityMap) {
                    for (int sharedAbilityMap : sharedAbilityMaps)
                        setCooldown(sharedAbilityMap, cooldown);
                    break;
                }
            }
        }
        setCooldown(abilityMap, cooldown);
    }

    public void setCooldown(int abilityMap, int cooldown) {
        long currentCycle = Utils.currentWorldCycle();
        long cd = BAR_CYCLE + cooldown * 2 + 1;// currentCycle + cooldown;
        if (abilityMap == 14881)
            globalCooldown = cd;
        else
            cooldowns.put(abilityMap, cd);
        player.getPackets().sendExecuteScript(6570, abilityMap, (int) currentCycle, ((int) currentCycle) + cooldown, 1, 1);
        // player.getPackets().sendExecuteScript(7001, abilityMap, 0,
        // 200000000);
    }

    public void reduceCooldown(int abilityMap, int reduction) {
        Long cd = cooldowns.get(abilityMap);
        if (cd == null)
            return;
        // int nextCD = (int) ((cd - Utils.currentWorldCycle()) - reduction);
        int nextCD = (int) ((cd - BAR_CYCLE) - reduction * 3);
        setCooldown(abilityMap, nextCD < 0 ? 0 : nextCD);
    }

    public void increaseCooldown(int abilityMap, int increment) {
        Long cd = cooldowns.get(abilityMap);
        if (cd == null || cd <= BAR_CYCLE) {
            setCooldown(abilityMap, increment);
            return;
        }
        long currentCycle = Utils.currentWorldCycle();
        // int nextCD = (int) ((cd - Utils.currentWorldCycle()) - reduction);
        long previous = (cd - BAR_CYCLE);
        long nextCD = previous + BAR_CYCLE + increment * 3 + 1;// currentCycle + cooldown;
        cooldowns.put(abilityMap, nextCD);
        player.getPackets().sendExecuteScript(6570, abilityMap, (int) currentCycle, ((int) currentCycle) + (int) (previous / 3) + increment, 1, 1);
    }

    public boolean hasCooldown(Shortcut shortcut) {
        if (globalCooldown > BAR_CYCLE)
            return true;
        int mapId = ClientScriptMap.getMap(CS_DATA_ID[shortcut.getType() - 1]).getIntValue(shortcut.getId(player));
        Long cooldown = cooldowns.get(mapId);
        return cooldown != null && cooldown > /* = */BAR_CYCLE;
    }

    public static abstract class Shortcut implements Serializable {

        private static final long serialVersionUID = 585026886173513110L;

        public abstract int getId(Player player);

        public abstract int getType();

        public abstract boolean queue(Player player);
    }

    public static class PrayerShortcut extends Shortcut {

        /**
         *
         */
        private static final long serialVersionUID = 7336111838898227173L;

        private final int prayerId;
        private final boolean curse;
        private final boolean preset;

        public PrayerShortcut(int prayerId, boolean curse) {
            this(prayerId, curse, false);
        }

        public PrayerShortcut(int prayerId, boolean curse, boolean preset) {
            this.prayerId = prayerId;
            this.curse = curse;
            this.preset = preset;
        }

        @Override
        public int getType() {
            return PRAYER_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            if (preset) {
                return 500 + prayerId;
            }
            if (!curse) {
                switch (prayerId) {
                    case 0:
                        int level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 28 ? 10 : level >= 10 ? 4 : 1;
                    case 1:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 31 ? 11 : level >= 13 ? 5 : 2;
                    case 2:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 34 ? 12 : level >= 16 ? 6 : 3;
                    case 7:
                        return 7;
                    case 8:
                        return 8;
                    case 9:
                        return 9;
                    case 11:
                        return 13;
                    case 12:
                        return 14;
                    case 13:
                        return 15;
                    case 14:
                        return 16;
                    case 15:
                        return 17;
                    case 16:
                        return 18;
                    case 3:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 44 ? 23 : level >= 26 ? 21 : 19;
                    case 5:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 45 ? 24 : level >= 27 ? 22 : 20;
                    case 10:
                        return 25;
                    case 17:
                        return 26;
                    case 19:
                        return 27;
                    case 18:
                        return 28;
                    case 21:
                        return 29;
                    case 20:
                        return 30;
                    case 4:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 44 ? 53 : level >= 26 ? 52 : 51;
                    case 6:
                        level = player.getSkills().getLevelForXp(Skills.PRAYER);
                        return level >= 45 ? 56 : level >= 27 ? 55 : 54;
                }
            } else {
                switch (prayerId) {
                    case 0:
                        return 31;
                    case 1:
                        return 32;
                    case 2:
                        return 33;
                    case 4:
                        return 34;
                    case 6:
                        return 35;
                    case 9:
                        return 36;
                    case 10:
                        return 37;
                    case 11:
                        return 38;
                    case 12:
                        return 39;
                    case 13:
                        return 40;
                    case 14:
                        return 41;
                    case 15:
                        return 42;
                    case 17:
                        return 43;
                    case 19:
                        return 44;
                    case 22:
                        return 45;
                    case 23:
                        return 46;
                    case 24:
                        return 47;
                    case 27:
                        return 48;
                    case 30:
                        return 49;
                    case 32:
                        return 50;
                    case 3:
                        return 57;
                    case 16:
                        return 58;
                    case 5:
                        return 59;
                    case 18:
                        return 60;
                    case 34:
                        return 61;
                    case 33:
                        return 62;
                    case 8:
                        return 63;
                    case 7:
                        return 64;
                    case 31:
                        return 65;
                    case 20:
                        return 66;
                    case 21:
                        return 67;
                    case 26:
                        return 68;
                    case 28:
                        return 69;
                    case 25:
                        return 70;
                    case 29:
                        return 71;
                    case 35:
                        return 72;
                    case 36:
                        return 73;
                    case 37:
                        return 74;
                }
            }
            return 0;
        }

        @Override
        public boolean queue(Player player) {
            return false;
        }
    }

    public static class ItemShortcut extends Shortcut {

        private static final long serialVersionUID = 4248509971373414335L;

        private int itemId;
        private Item item;

        public ItemShortcut(Item item) {
            this.item = item != null ? new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes()) : null;
            this.itemId = -1;
        }

        @Override
        public int getType() {
            return ITEM_SHORTCUT;
        }

        @Override
        public int getId(Player player) {
            if (itemId != -1) {
                item = new Item(itemId);
                itemId = -1;
            }
            return item.getId();
        }

        public Item getItem() {
            return item;
        }

        @Override
        public boolean queue(Player player) {
            return false;
        }
    }

    public static class RangeAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = 2631054284301142532L;

        private final int abilityId;

        public RangeAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return RANGED_ABILITY_SHORTCUT;
        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 1]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
        }
    }

    public static class MeleeAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = 4307311904537253515L;

        private final int abilityId;

        public MeleeAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return MELEE_ABILITY_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 5]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
        }
    }

    public static class StrengthAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = -4563409545893188469L;

        private final int abilityId;

        public StrengthAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return STRENGTH_ABILITY_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 1]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
        }
    }

    public static class DefenceAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = 2285557519314659732L;

        private final int abilityId;

        public DefenceAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return DEFENCE_ABILITY_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 5]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
        }
    }

    public static class HealAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = 55418066282083274L;

        private final int abilityId;

        public HealAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return HEAL_ABILITY_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 5]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
        }
    }

    public static class MagicAbilityShortcut extends Shortcut {

        private static final long serialVersionUID = -1677214505228254289L;

        private final int abilityId;

        public MagicAbilityShortcut(int abilityId) {
            this.abilityId = abilityId;
        }

        @Override
        public int getType() {
            return MAGIC_ABILITY_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return abilityId;
        }

        @Override
        public boolean queue(Player player) {
            GeneralRequirementMap data = GeneralRequirementMap.getMap(ClientScriptMap.getMap(CS_DATA_ID[getType() - 1]).getIntValue(abilityId));
            return !data.getValues().containsKey((long) 3394);
             //Magic.isCombatSpell(data);
        }
    }

    public static class SummoningOrbShortcut extends Shortcut {

        private static final long serialVersionUID = 3327285699564970046L;
        private final int id;

        public SummoningOrbShortcut(int id) {
            this.id = id;
        }

        @Override
        public int getType() {
            return SUMMONING_ORB_SHORTCUT;

        }

        @Override
        public int getId(Player player) {
            return id;
        }

        @Override
        public boolean queue(Player player) {
            return false;
        }
    }

    public int getActualUseItemId(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        int mapId = defs.getCSOpcode(2281);
        if (mapId != 0 && defs.clientScriptData.containsKey(3744)) {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(mapId);
            int type = defs.getCSOpcode(3744);
            int minSearch = -1;
            int maxSearch = -1;
            switch (type) {
                case 0:
                    minSearch = 4;
                    maxSearch = 11;
                    break;
                case 1:
                case 2:
                case 4:
                case 5:
                    boolean isPot = false;
                    for (int i = 8; i <= 11; i++) {
                        int paramId = getParamIdForIndex(i);
                        if (map.getIntValue(paramId) != 0 && itemId == map.getIntValue(paramId)) {
                            isPot = true;
                            break;
                        }
                    }
                    minSearch = isPot ? 8 : 12;
                    maxSearch = isPot ? 11 : 17;
                    break;
                case 6:
                    minSearch = 12;
                    maxSearch = 17;
                    break;
                case 3:
                case 7:
                    minSearch = 10;
                    maxSearch = 11;
                    break;
            }
            if (minSearch != -1 && maxSearch != -1) {
                boolean containsItemInRange = false;
                for (int i = minSearch; i <= maxSearch; i++) {
                    int paramId = getParamIdForIndex(i);
                    if (map.getIntValue(paramId) != 0 && itemId == map.getIntValue(paramId)) {
                        containsItemInRange = true;
                        break;
                    }
                }
                if (containsItemInRange) {
                    for (int i = maxSearch; i >= minSearch; i--) {
                        int paramId = getParamIdForIndex(i);
                        int actualItemId = map.getIntValue(paramId);
                        if (actualItemId != 0 && player.getInventory().containsItem(actualItemId, 1))
                            return actualItemId;
                    }
                }
            }
        }
        return itemId;
    }

    public int getParamIdForIndex(int index) {
        switch (index) {
            case 0:
                return 2981;
            case 1:
                return 2982;
            case 2:
                return 2983;
            case 3:
                return 2984;
            case 4:
                return 532;
            case 5:
                return 533;
            case 6:
                return 534;
            case 7:
                return 535;
            case 8:
                return 588;
            case 9:
                return 589;
            case 10:
                return 590;
            case 11:
                return 591;
            case 12:
                return 2241;
            case 13:
                return 2242;
            case 14:
                return 2243;
            case 15:
                return 2244;
            case 16:
                return 2245;
            case 17:
                return 2246;
        }
        return -1;
    }
}
