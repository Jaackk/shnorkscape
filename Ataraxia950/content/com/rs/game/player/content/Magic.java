package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.clanwars.FfaZone;
import com.rs.game.activites.clanwars.RequestController;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.item.Item;
import com.rs.game.item.MagicOnItem;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.SpellOnSomethingWalkAction;
import com.rs.game.player.actions.invention.Disassemble;
import com.rs.game.player.actions.magic.BoltEnchanting;
import com.rs.game.player.actions.magic.SpellEffect;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.player.actions.magic.lunar.NPCSpell;
import com.rs.game.player.actions.magic.lunar.ObjectSpell;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/*
 * content package used for static stuff
 */
public class Magic {

    public static final int MAGIC_TELEPORT = 0, ITEM_TELEPORT = 1, OBJECT_TELEPORT = 2;
    public static final int EARTH_RUNE = 557;
    public static final int ASTRAL_RUNE = 9075;
    public static final int AIR_RUNE = 556, WATER_RUNE = 555;
    public static final int FIRE_RUNE = 554;
    public static final int MIND_RUNE = 558;
    public static final int NATURE_RUNE = 561;
    public static final int CHAOS_RUNE = 562;
    public static final int DEATH_RUNE = 560;
    public static final int BLOOD_RUNE = 565;
    public static final int SOUL_RUNE = 566;
    private static final int LAW_RUNE = 563, ELEMENTAL_RUNE = 12850, CATALYTIC_RUNE = 12851;

    public static final int LUNARS_BOOK = 2;
    public static final int ANCIENTS_BOOK = 1;
    public static final int NORMAL_BOOK = 0;

    @SuppressWarnings("unused")
    private static final int BANANA = 1963;

    @SuppressWarnings("unused")
    private static final int STEAM_RUNE = 4694;

    @SuppressWarnings("unused")
    private static final int MIST_RUNE = 4695;

    @SuppressWarnings("unused")
    private static final int DUST_RUNE = 4696;

    @SuppressWarnings("unused")
    private static final int SMOKE_RUNE = 4697;

    @SuppressWarnings("unused")
    private static final int MUD_RUNE = 4698;

    @SuppressWarnings("unused")
    private static final int LAVA_RUNE = 4699;
    @SuppressWarnings("unused")
    private static final int ARMADYL_RUNE = 21773;

    public static final int BODY_RUNE = 559;

    public static final int COSMIC_RUNE = 564;
    @SuppressWarnings("unused")
    private static final int D_AIR_RUNE = 17780, D_WATER_RUNE = 17781, D_EARTH_RUNE = 17782, D_FIRE_RUNE = 17783, D_MIND_RUNE = 17784, D_CHAOS_RUNE = 17785, D_DEATH_RUNE = 17786, D_BLOOD_RUNE = 17787, D_BODY_RUNE = 17788, D_COSMIC_RUNE = 17789, D_ASTRAL_RUNE = 17790, D_NATURE_RUNE = 17791, D_LAW_RUNE = 17792, D_SOUL_RUNE = 17793;

    private final static WorldTile[] TABS = { new WorldTile(3217, 3426, 0), new WorldTile(3222, 3218, 0), new WorldTile(2965, 3379, 0), new WorldTile(2758, 3478, 0), new WorldTile(2660, 3306, 0) };

    private final static WorldTile[] SCROLLS = { new WorldTile(3361, 2970, 0), new WorldTile(3171, 2982, 0), new WorldTile(2515, 3861, 0), new WorldTile(3535, 5189, 0), new WorldTile(2796, 3085, 0), new WorldTile(3308, 3491, 0) };

    public static final Map<Integer, DefaultSpell> DEFAULT_LUNAR_SPELLS = new HashMap<Integer, DefaultSpell>();
    public static final Map<Integer, PlayerSpell> PLAYER_LUNAR_SPELLS = new HashMap<Integer, PlayerSpell>();
    public static final Map<Integer, ObjectSpell> OBJECT_LUNAR_SPELLS = new HashMap<Integer, ObjectSpell>();
    public static final Map<Integer, NPCSpell> NPC_LUNAR_SPELLS = new HashMap<Integer, NPCSpell>();
    public static final Map<Integer, ItemSpell> ITEM_LUNAR_SPELLS = new HashMap<Integer, ItemSpell>();

    public static final boolean checkSpellRequirements(Player player, int level, boolean delete, int... runes) {
        final int lvl = player.getSkills().getLevelForXp(Skills.MAGIC);
        if (player.getSkills().getLevel(Skills.MAGIC) < level && lvl < level) {
            player.sendMessage("Your Magic level is not high enough for this spell.");
            return false;
        }
        return checkRunes(player, delete, runes);
    }
    
    public static final boolean checkSpellRequirements(Player player, int level, boolean delete, Item[] runes) {
        final int lvl = player.getSkills().getLevelForXp(Skills.MAGIC);
        if (player.getSkills().getLevel(Skills.MAGIC) < level && lvl < level) {
            player.sendMessage("Your Magic level is not high enough for this spell.");
            return false;
        }
        return checkRunes(player, delete, runes);
    }

    public static final boolean hasInfiniteRunes(int runeId, int weaponId, int shieldId) {
        String name = ItemDefinitions.getName(weaponId).toLowerCase();
        if (runeId == AIR_RUNE) {
            switch (weaponId) {
            case 1381:
            case 21777:
            case 1397:
            case 1405:
            case 15598:
                return true;
            default:
                return false;
            }
        } else if (runeId == WATER_RUNE) {
            if (shieldId == 18346)
                return true;
            switch (weaponId) {
            case 1383:
            case 1395:
            case 1403:
            case 6562:
            case 6563:
            case 11736:
            case 11738:
                return true;
            default:
                return false;
            }
        } else if (runeId == EARTH_RUNE) {
            switch (weaponId) {
            case 1385:
            case 1399:
            case 1407:
            case 3053:
            case 3054:
            case 6562:
            case 6563:
                return true;
            default:
                return false;
            }
        } else if (runeId == FIRE_RUNE) {
            if (name.equalsIgnoreCase("Camel staff"))
                return true;
            switch (weaponId) {
            case 1387:
            case 1401:
            case 3053:
            case 1393:
            case 3054:
            case 11736:
            case 11738:
            case 36019:
                return true;
            default:
                return false;
            }
        } else if (runeId == 16091 || runeId == 17780) {
            switch (weaponId) {
            case 16169:
            case 16170:
            case 17009:
            case 17011:
                return true;
            }
        } else if (runeId == 16093 || runeId == 17782) {
            switch (weaponId) {
            case 16165:
            case 16166:
            case 17001:
            case 17003:
                return true;
            }
        } else if (runeId == 16092 || runeId == 17781) {
            switch (weaponId) {
            case 16163:
            case 16164:
            case 16997:
            case 16999:
                return true;
            }
        } else if (runeId == 16094 || runeId == 17783) {
            switch (weaponId) {
            case 16167:
            case 16168:
            case 17005:
            case 17007:
                return true;
            }
        }
        switch (runeId) {
        case 16091:
        case 17780:
        case 16093:
        case 17782:
        case 16092:
        case 17781:
        case 16094:
        case 17783:
            switch (weaponId) {
            case 15835:
            case 16153:
            case 16154:
            case 16155:
            case 16156:
            case 16157:
            case 16158:
            case 16159:
            case 16160:
            case 16161:
            case 16162:
            case 16171:
            case 16172:
            case 16173:
            case 16967:
            case 16969:
            case 16977:
            case 16979:
            case 16981:
            case 16983:
            case 16985:
            case 16987:
            case 16989:
            case 16991:
            case 16993:
            case 16995:
            case 17013:
            case 17015:
            case 17017:
            case 17293:
            case 27687:
            case 27689:
            case 27691:
            case 27693:
            case 27695:
            case 27697:
            case 27699:
            case 27701:
            case 27703:
            case 27705:
            case 27707:
            case 27709:
            case 27711:
            case 27768:
            case 27769:
            case 27770:
            case 27771:
            case 27772:
            case 27773:
            case 27774:
            case 27775:
            case 27776:
            case 27777:
            case 27778:
            case 27779:
            case 27780:
                return true;
            default:
                return false;
            }
        default:
            return false;
        }
    }

    public static boolean hasStaffOfLight(int weaponId) {
        return weaponId == 15486 || weaponId == 15502 || weaponId == 22207 || weaponId == 22211 || weaponId == 22209 || weaponId == 22213 || weaponId == 34155 || weaponId == 36627 || weaponId == 36629;
    }

    public static boolean checkSpellLevel(Player player, int level) {
        if (player.getSkills().getLevelForXp(Skills.MAGIC) < level) {
            player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
            return false;
        }
        return true;
    }

    private static final void sendTeleportInterface(Player player, Player p2, String location) {
        p2.getInterfaceManager().sendInterface(326);
        p2.getPackets().sendIComponentText(326, 1, player.getDisplayName());
        p2.getPackets().sendIComponentText(326, 3, location);
        p2.getTemporaryAttributtes().put("groupteleport", getTileByType(player, location));
    }

    private static final WorldTile getTileByType(final Player player, String type) {
        switch (type.toLowerCase()) {
        case "trollheim":
            return new WorldTile(2814, 3680, 0);
        case "<col=ff0000>ice plateau [dangerous]":
            return new WorldTile(2974, 3940, 0);
        case "fishing guild":
            return new WorldTile(2612, 3383, 0);
        case "port khazard":
            return new WorldTile(2635, 3166, 0);
        case "barbarian outpost":
            return new WorldTile(2518, 3570, 0);
        case "waterbirth island":
            return new WorldTile(2546, 3758, 0);
        case "moonclan island":
            return new WorldTile(2114, 3914, 0);
        case "catherby":
            return new WorldTile(2802, 3443, 0);
        default:
            return player.getHomeTile();
        }
    }

    public static final void sendTeleother(final Player player, final Player p2, final int level, final int exp, String area, int... runes) {
        if (player.getLunarDelay() > Utils.currentTimeMillis())
            return;
        if (!checkSpellRequirements(player, level, true, runes))
            return;
        player.setLunarDelay(4000);
        if (p2 == null || p2.isDead() || !p2.isActive() || p2.hasFinished() || !player.getControlerManager().canHit(p2) || p2.isLocked() || p2.isCanPvp()) {
            player.sendMessage("You cannot do this right now.");
            return;
        }
        if (!p2.isAcceptingAid()) {
            player.sendMessage("The targeted player isn't accepting aid.");
            return;
        } else if (p2.getInterfaceManager().containsScreenInter()) {
            player.sendMessage("Other player is busy.");
            return;
        }
        p2.getInterfaceManager().sendInterface(326);
        p2.getPackets().sendIComponentText(326, 1, player.getDisplayName());
        p2.getPackets().sendIComponentText(326, 3, area);
        p2.getTemporaryAttributtes().put("teleotherteleport", area.equals("Lumbridge") ? new WorldTile(3222, 3218, 0) : area.equals("Falador") ? new WorldTile(2964, 3379, 0) : new WorldTile(2757, 3478, 0));
        p2.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                p2.getTemporaryAttributtes().remove("teleotherteleport");
            }
        });
    }

    @SuppressWarnings("unused")
    private static final void sendGroupTeleport(Player player, int level, int exp, String area, int... runes) {
        if (player.getLunarDelay() > Utils.currentTimeMillis())
            return;
        if (!checkSpellRequirements(player, level, true, runes))
            return;
        player.setLunarDelay(4000);
        for (int regionId : player.getMapRegionsIds()) {
            List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes == null)
                continue;
            for (int playerIndex : playerIndexes) {
                final Player p2 = World.getPlayers().get(playerIndex);
                if (p2 == null || p2 == player || p2.isDead() || !p2.isActive() || p2.hasFinished() || !p2.withinDistance(player, 3) || !player.getControlerManager().canHit(p2) || !p2.isAcceptingAid() || p2.getInterfaceManager().containsScreenInter() || p2.isLocked() || p2.isCanPvp())
                    continue;
                else if (p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof DuelArena)
                    continue;
                sendTeleportInterface(player, p2, area);
                p2.setCloseInterfacesEvent(new Runnable() {
                    @Override
                    public void run() {
                        p2.getTemporaryAttributtes().remove("groupteleport");
                    }
                });
            }
            Magic.sendLunarTeleportSpell(player, 0, exp, getTileByType(player, area));
        }
    }

    public static void pushLeverTeleport(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processObjectTeleport(tile))
            return;
        player.setNextAnimation(new Animation(2140));
        player.lock(2);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.unlock();
                Magic.sendObjectTeleportSpell(player, false, tile);
            }
        }, 1);
    }

    public static final void sendAncientTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
        sendTeleportSpell(player, 1979, -1, 1681, -1, level, xp, tile, 4, true, MAGIC_TELEPORT, runes);
    }

    public static final void sendDelayedObjectTeleportSpell(Player player, int delay, boolean randomize, WorldTile tile) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, delay, randomize, OBJECT_TELEPORT);
    }

    public static final boolean sendItemTeleportSpell(Player player, boolean randomize, int upEmoteId, int upGraphicId, int delay, WorldTile tile) {
        return sendTeleportSpell(player, upEmoteId, -2, upGraphicId, -1, 0, 0, tile, delay, randomize, ITEM_TELEPORT);
    }

    public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, true, MAGIC_TELEPORT, runes);
    }

    public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, boolean randomize, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, randomize, MAGIC_TELEPORT, runes);
    }

    public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, boolean randomize, boolean closeInterfaces, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, randomize, MAGIC_TELEPORT, closeInterfaces, runes);
    }

    public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, boolean randomize, boolean closeInterfaces, boolean lock, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, randomize, MAGIC_TELEPORT, closeInterfaces, lock, runes);
    }

    public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, boolean randomize, boolean closeInterfaces, boolean lock, boolean forced, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, randomize, MAGIC_TELEPORT, closeInterfaces, lock, forced, runes);
    }

    public static final void sendCrushTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
        sendTeleportSpell(player, 17542, 8941, 3402, 1577, level, xp, tile, 10, true, MAGIC_TELEPORT, runes);
    }

    public static final void sendObjectTeleportSpell(Player player, boolean randomize, WorldTile tile) {
        sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, 3, randomize, OBJECT_TELEPORT);
    }

    public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, int... runes) {
        return sendTeleportSpell(player, upEmoteId, downEmoteId, upGraphicId, downGraphicId, level, xp, tile, delay, randomize, teleType, true, runes);
    }

    public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, boolean closeInterfaces, int... runes) {
        return sendTeleportSpell(player, upEmoteId, downEmoteId, upGraphicId, downGraphicId, level, xp, tile, delay, randomize, teleType, closeInterfaces, true, runes);
    }

    public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, boolean closeInterfaces, boolean lock, int... runes) {
        return sendTeleportSpell(player, upEmoteId, downEmoteId, upGraphicId, downGraphicId, level, xp, tile, delay, randomize, teleType, closeInterfaces, lock, false, runes);
    }

    public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, boolean closeInterfaces, boolean lock, boolean forced, int... runes) {
        if (!forced) {
            long currentTime = Utils.currentTimeMillis();
            if (player.getLockDelay() > currentTime)
                return false;
        }
        if (player.isCanPvp() && !tile.matches(new WorldTile(2539, 4712, 0)) && player.getAttackedBy() != null && player.getTemporaryAttributtes().remove("obelisktele") == null || player.getX() >= 2956 && player.getX() <= 3067 && player.getY() >= 5512 && player.getY() <= 5630 || (player.getX() >= 2756 && player.getX() <= 2875 && player.getY() >= 5512 && player.getY() <= 5627)) {

            player.getPackets().sendGameMessage("A magical force is blocking you from teleporting.");
            return false;
        }
        if (player.getSkills().getLevel(Skills.MAGIC) < level) {
            player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
            return false;
        }
        if (!checkRunes(player, false, runes))
            return false;
        if (teleType == MAGIC_TELEPORT) {
            if (!player.getControlerManager().processMagicTeleport(tile))
                return false;
        } else if (teleType == ITEM_TELEPORT) {
            if (!player.getControlerManager().processItemTeleport(tile))
                return false;
        } else if (teleType == OBJECT_TELEPORT) {
            if (!player.getControlerManager().processObjectTeleport(tile))
                return false;
        }
        checkRunes(player, true, runes);
        player.stopAll(true, closeInterfaces);
        if (upEmoteId != -1)
            player.setNextAnimation(new Animation(upEmoteId));
        if (upGraphicId != -1)
            player.setNextGraphics(new Graphics(upGraphicId));
        if (teleType == MAGIC_TELEPORT)
            player.getPackets().sendSound(5527, 0, 2);
        if (lock) {
            player.lock(3 + delay);
        }
        WorldTasksManager.schedule(new WorldTask() {

            boolean removeDamage;

            @Override
            public void run() {
                if (!removeDamage) {
                    WorldTile teleTile = tile;
                    if (randomize) {
                        // attemps to randomize tile by 4x4 area
                        for (int trycount = 0; trycount < 10; trycount++) {
                            if (tile == null)
                                break;
                            teleTile = new WorldTile(tile, 2);
                            if (tile == null || teleTile == null)
                                break;
                            if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                                break;
                            teleTile = tile;
                        }
                    }
                    player.setNextWorldTile(teleTile);
                    player.getControlerManager().magicTeleported(teleType);
                    if (player.getCurrentInstance() != null)
                        player.getCurrentInstance().removePlayer(player);
                    if (player.getControlerManager().getControler() == null)
                        teleControlersCheck(player, teleTile);
                    if (xp != 0)
                        player.getSkills().addXp(Skills.MAGIC, xp);
                    if (downEmoteId != -1)
                        player.setNextAnimation(new Animation(downEmoteId == -2 ? -1 : downEmoteId));
                    if (downGraphicId != -1)
                        player.setNextGraphics(new Graphics(downGraphicId));
                    if (teleType == MAGIC_TELEPORT) {
                        player.getPackets().sendSound(5524, 0, 2);
                        player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
                        player.setDirection(6);
                    }
                    player.getTemporaryAttributtes().put("teleporting", true);
                    removeDamage = true;
                } else {
                    CoresManager.getServiceProvider().executeWithDelay(() -> player.getTemporaryAttributtes().remove("teleporting"), 5);
                    player.resetReceivedDamage();
                    player.resetReceivedHits();
                    stop();
                }
            }
        }, delay, 0);
        return true;
    }

    public static void teleControlersCheck(Player player, WorldTile teleTile) {
        if (player.getRegionId() == 13626 || player.getRegionId() == 13625)
            player.getControlerManager().startControler("DungeoneeringLobby");
        else if (Wilderness.isAtWild(teleTile) || Wilderness.isAtDitch(teleTile))
            player.getControlerManager().startControler("Wilderness");
        else if (RequestController.inWarRequest(player))
            player.getControlerManager().startControler("clan_wars_request");
        else if (FfaZone.inArea(player))
            player.getControlerManager().startControler("clan_wars_ffa");
    }

    public static boolean useTabTeleport(final Player player, final int itemId) {
        if (itemId == 8013) {
            if (useTeleTab(player, player.getHomeTile()))
                player.getInventory().deleteItem(itemId, 1);
            return true;
        }
        if (itemId < 8007 || itemId > 8007 + TABS.length - 1)
            return false;
        if (useTeleTab(player, TABS[itemId - 8007]))
            player.getInventory().deleteItem(itemId, 1);
        return true;
    }

    public static boolean useTeleTab(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processItemTeleport(tile))
            return false;
        Logger.getGlobal().info(player.isLocked() + " " + player.getLockDelay());
        if (player.isLocked())
            return false;
        player.lock();
        player.setNextAnimation(new Animation(9597));
        player.setNextGraphics(new Graphics(1680));
        WorldTasksManager.schedule(new WorldTask() {
            int stage;

            @Override
            public void run() {
                if (stage == 0) {
                    player.setNextAnimation(new Animation(4731));
                    stage = 1;
                } else if (stage == 1) {
                    WorldTile teleTile = tile;
                    // attemps to randomize tile by 4x4 area
                    for (int trycount = 0; trycount < 10; trycount++) {
                        teleTile = new WorldTile(tile, 2);
                        if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                            break;
                        teleTile = tile;
                    }
                    player.setNextWorldTile(teleTile);
                    player.getControlerManager().magicTeleported(ITEM_TELEPORT);
                    if (player.getControlerManager().getControler() == null)
                        teleControlersCheck(player, teleTile);
                    player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
                    player.setDirection(6);
                    player.setNextAnimation(new Animation(-1));
                    stage = 2;
                } else if (stage == 2) {
                    player.resetReceivedDamage();
                    player.unlock();
                    stop();
                }
            }
        }, 2, 1);
        return true;
    }

    /**
     * Sends the ectophial teleport.
     * 
     * @param player The player that uses the ectophial.
     * @param item   The ectophial item.
     */
    public static void dispatchEctophial(final Player player, Item item) {
        player.setNextGraphics(new Graphics(1688));
        player.setNextAnimation(new Animation(9609));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                sendTeleportSpell(player, 8939, 8941, 1678, 1679, 0, 0, new WorldTile(3662, 3518, 0), 4, true, ITEM_TELEPORT);
            }
        }, 6);
    }
    public static void mineTeleport(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processMagicTeleport(tile))
            return;
        if (player.getControlerManager().getControler() instanceof DungeonController)
            return;
        if (player.isLocked() || player.getTeleBlockDelay() > Utils.currentTimeMillis())
            return;
        player.getTemporaryAttributtes().put("teleporting", true);
        player.resetReceivedHits();
        player.resetReceivedDamage();
        player.lock();
        player.stopAll();
        player.setNextGraphics(new Graphics(2451));
        player.setNextAnimation(new Animation(27123));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                WorldTile teleTile = tile;
                player.resetReceivedHits();
                player.resetReceivedDamage();
                for (int trycount = 0; trycount < 10; trycount++) {
                    teleTile = new WorldTile(tile, 2);
                    if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                        break;
                    teleTile = tile;
                }
                player.setNextAnimation(new Animation(27124));
                player.setNextGraphics(new Graphics(2452));
                player.setNextWorldTile(teleTile);
                player.getControlerManager().magicTeleported(MAGIC_TELEPORT);
                player.checkMovement(teleTile.getX(), teleTile.getY(), teleTile.getPlane());
                if (player.getControlerManager().getControler() == null)
                    teleControlersCheck(player, teleTile);
                player.unlock();
                stop();
                CoresManager.getServiceProvider().executeWithDelay(() -> player.getTemporaryAttributtes().remove("teleporting"), 5);
            }
        }, 4);
    }


    public static void vineTeleport(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processMagicTeleport(tile))
            return;
        if (player.getControlerManager().getControler() instanceof DungeonController)
            return;
        if (player.isLocked() || player.getTeleBlockDelay() > Utils.currentTimeMillis())
            return;
        player.getTemporaryAttributtes().put("teleporting", true);
        player.resetReceivedHits();
        player.resetReceivedDamage();
        player.lock();
        player.stopAll();
        player.setNextGraphics(new Graphics(1229));
        player.setNextAnimation(new Animation(7082));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                WorldTile teleTile = tile;
                player.resetReceivedHits();
                player.resetReceivedDamage();
                for (int trycount = 0; trycount < 10; trycount++) {
                    teleTile = new WorldTile(tile, 2);
                    if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                        break;
                    teleTile = tile;
                }
                player.setNextAnimation(new Animation(7084));
                player.setNextGraphics(new Graphics(1228));
                player.setNextWorldTile(teleTile);
                player.getControlerManager().magicTeleported(MAGIC_TELEPORT);
                player.checkMovement(teleTile.getX(), teleTile.getY(), teleTile.getPlane());
                if (player.getControlerManager().getControler() == null)
                    teleControlersCheck(player, teleTile);
                player.unlock();
                stop();
                CoresManager.getServiceProvider().executeWithDelay(() -> player.getTemporaryAttributtes().remove("teleporting"), 5);
            }
        }, 4);
    }

    public static void sendDiamondZonePortalTeleport(final Player player, final WorldTile destination, final String controller) {
        final int DEPARTURE_GFX = 537, ARRIVAL_GFX = 538;
        final int DEPARTURE_ANIM = 7389, ARRIVAL_ANIM = 9013;
        if (player.isLocked()) {
            return;
        }
        player.getTemporaryAttributtes().put("teleporting", true);
        player.lock();
        player.stopAll();
        player.setNextGraphics(new Graphics(DEPARTURE_GFX));
        player.setNextAnimation(new Animation(DEPARTURE_ANIM));
        if (player.getControlerManager().getControler() != null) {
            player.getControlerManager().removeControlerWithoutCheck();
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.resetReceivedHits();
                player.resetReceivedDamage();
                player.setNextGraphics(new Graphics(ARRIVAL_GFX));
                player.setNextAnimation(new Animation(ARRIVAL_ANIM));
                player.setNextWorldTile(destination);
                if (controller != null) {
                    player.getControlerManager().startControler(controller);
                }
                player.unlock();
                stop();
                CoresManager.getServiceProvider().executeWithDelay(() -> player.getTemporaryAttributtes().remove("teleporting"), 5);
            }
        }, 4);
    }

    public static final void sendEctophialTeleport(Player player, int level, double xp, WorldTile tile, int... runes) {
        sendTeleportSpell(player, 8939, 8941, 1678, 1679, level, xp, tile, 3, true, MAGIC_TELEPORT, runes);
    }

    public static void daemonheimTeleport(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processItemTeleport(tile))
            return;
        if (player.isLocked())
            return;
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(13652));
                    player.setNextGraphics(new Graphics(2602));
                    player.getControlerManager().processItemTeleport(tile);
                }
                if (loop == 5) {
                    player.setNextWorldTile(tile);
                    player.getControlerManager().processItemTeleport(tile);
                }
                if (loop == 6) {
                    player.setNextAnimation(new Animation(13654));
                    player.setNextGraphics(new Graphics(2603));
                    player.getControlerManager().magicTeleported(ITEM_TELEPORT);
                    if (player.getControlerManager().getControler() == null)
                        teleControlersCheck(player, tile);
                    else {
                        player.getControlerManager().processItemTeleport(tile);
                    }
                    player.setNextFaceWorldTile(new WorldTile(tile.getX(), tile.getY() - 1, tile.getPlane()));
                    player.setDirection(6);
                    player.resetReceivedDamage();
                    player.unlock();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public static void puroPuroTeleport(final Player player, final WorldTile tile) {
        Logger.getGlobal().info("FDAJHFBHA");
        if (!player.getControlerManager().processMagicTeleport(tile))
            return;
        if (player.getControlerManager().getControler() instanceof DungeonController)
            return;
        if (player.isLocked() || player.getTeleBlockDelay() > Utils.currentTimeMillis())
            return;
        player.lock();
        player.stopAll();
        player.setNextGraphics(new Graphics(1118));
        player.setNextAnimation(new Animation(6601));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                WorldTile teleTile = tile;
                for (int trycount = 0; trycount < 10; trycount++) {
                    teleTile = new WorldTile(tile, 2);
                    if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                        break;
                    teleTile = tile;
                }
                player.setNextWorldTile(teleTile);
                player.getControlerManager().magicTeleported(MAGIC_TELEPORT);
                player.checkMovement(teleTile.getX(), teleTile.getY(), teleTile.getPlane());
                if (player.getControlerManager().getControler() == null)
                    teleControlersCheck(player, teleTile);
                player.unlock();
                stop();
            }
        }, 9);
    }

    /**
     * Handles the scroll teleporting.
     *
     * @param player The player teleporting.
     * @param itemId The scroll ID.
     * @return if can Teleport.
     */
    public static boolean useScrollTeleport(final Player player, final int itemId) {
        if (itemId < 19475 || itemId > 19475 + SCROLLS.length - 1)
            return false;
        if (useTeleScroll(player, SCROLLS[itemId - 19475]))
            player.getInventory().deleteItem(itemId, 1);
        return true;
    }

    /**
     * Uses the teleport scroll.
     *
     * @param player The player using the scroll.
     * @param tile   The worldtile to teleport to.
     * @return if Successful.
     */
    public static boolean useTeleScroll(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processItemTeleport(tile))
            return false;
        if (player.isLocked())
            return false;
        player.lock();
        player.setNextAnimation(new Animation(14293));
        player.setNextGraphics(new Graphics(94));
        WorldTasksManager.schedule(new WorldTask() {
            int stage;

            @Override
            public void run() {
                stage++;
                if (stage == 3) {
                    WorldTile teleTile = tile;
                    for (int trycount = 0; trycount < 10; trycount++) {
                        teleTile = new WorldTile(tile, 2);
                        if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                            break;
                        teleTile = tile;
                    }
                    player.setNextWorldTile(teleTile);
                    player.getControlerManager().magicTeleported(ITEM_TELEPORT);
                    if (player.getControlerManager().getControler() == null)
                        teleControlersCheck(player, teleTile);
                    player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
                    player.setDirection(6);
                    player.setNextAnimation(new Animation(-1));
                    player.resetReceivedDamage();
                    player.unlock();
                    stop();
                }
            }
        }, 1, 1);
        return true;
    }

    public static void compCapeTeleport(final Player player, final int x, final int y, final int h) {
        final WorldTile teleTile = new WorldTile(x, y, h);
        if (!player.getControlerManager().processItemTeleport(teleTile))
            return;
        if (player.isLocked())
            return;
        player.lock();
        player.setNextAnimation(new Animation(3254));
        player.setNextGraphics(new Graphics(2670));
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            public void run() {
                if (loop == 1) {
                    player.setNextWorldTile(teleTile);
                    player.setNextAnimation(new Animation(3255));
                    player.getControlerManager().magicTeleported(ITEM_TELEPORT);
                    if (player.getControlerManager().getControler() == null)
                        teleControlersCheck(player, teleTile);
                    player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
                    player.setDirection(6);
                    player.resetReceivedDamage();
                    player.unlock();
                    player.setNextGraphics(new Graphics(2671));
                    this.stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public static void fairyRingTeleport(final Player player, final WorldTile tile) {
        if (!player.getControlerManager().processObjectTeleport(tile))
            return;
        if (player.isLocked())
            return;
        player.setNextAnimation(new Animation(3254));
        player.setNextGraphics(new Graphics(2670));
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(tile);
                player.setNextAnimation(new Animation(3255));
                player.setNextGraphics(new Graphics(2670));
                player.getControlerManager().magicTeleported(OBJECT_TELEPORT);
                if (player.getControlerManager().getControler() == null)
                    teleControlersCheck(player, tile);
                player.setNextFaceWorldTile(new WorldTile(tile.getX(), tile.getY() - 1, tile.getPlane()));
                player.setDirection(6);
                player.resetReceivedDamage();
                player.unlock();
            }
        }, 2);
    }

    public static void resourcesTeleport(final Player player, final WorldTile tile) {
        resourcesTeleport(player, tile, 1);
    }

    public static void resourcesTeleport(final Player player, final WorldTile tile, int level) {
        if (!player.getControlerManager().processObjectTeleport(tile))
            return;
        if (player.isLocked())
            return;
        if (player.getSkills().getLevelForXp(Skills.DUNGEONEERING) < level) {
            player.sendMessage("You need a Dungeoneering level of at least " + level + " to enter this dungeon.");
            return;
        }
        player.setNextAnimation(new Animation(13288));
        player.setNextGraphics(new Graphics(2516));
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(tile);
                player.setNextAnimation(new Animation(13285));
                player.setNextGraphics(new Graphics(2517));
                player.getControlerManager().magicTeleported(OBJECT_TELEPORT);
                if (player.getControlerManager().getControler() == null)
                    teleControlersCheck(player, tile);
                player.setNextFaceWorldTile(new WorldTile(tile.getX(), tile.getY() - 1, tile.getPlane()));
                player.setDirection(6);
                player.resetReceivedDamage();
                player.unlock();
            }
        }, 1);
    }

    public static final void sendLunarTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
        sendTeleportSpell(player, 9606, -2, 1685, -1, level, xp, tile, 5, true, MAGIC_TELEPORT, runes);
    }

    public static void openLodestoneNetwork(Player player) {
        if (player.isUnderCombat()) {
            player.getPackets().sendGameMessage("You can't do this while under combat.");
            return;
        }
        if (player.getTemporaryAttributtes().get(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY) == Boolean.TRUE) {
            player.getPackets().sendGameMessage("You're too busy to be doing that!");
            return;
        }
        player.getInterfaceManager().sendInterface(1092);
        player.getPackets().sendIComponentSettings(1092, 7, 0, 2, 0x2 | 0x4);
    }

    public static void handleSpellOnFloorItem(Player player, int spellId, FloorItem orignalItem) {
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null)
            return;
        int spellBook = getSpellBook(data);// spellbook 3 means shared by all
        // books
        if (!hasLevel(player, data))
            return;
        if (spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return;
        }
        if (orignalItem == null || orignalItem.getTile() == null)
            return;
        player.stopAll();
        player.getActionManager().setAction(new SpellOnSomethingWalkAction(orignalItem, new Runnable() {

            @Override
            public void run() {
                final FloorItem item = World.getRegion(orignalItem.getTile().getRegionId()).getGroundItem(orignalItem.getId(), orignalItem.getTile(), player);
                if (item == null || !item.exactMatch(orignalItem))
                    return;

                player.setNextFaceWorldTile(item.getTile());
                switch (data.getId()) {
                case 14874:// borrowed spell
                case 14757:// Telekinetic Grab
                    boolean usingBorrowed = data.getId() == 14874;
                    if (usingBorrowed && player.getBorrowedSpellId() != 32)
                        break;
                    if (player.isKingOfTheSkillGameMode()) {
                        player.getKingOfTheSkillGameModeHandler().sendVagueNoMessageToPlayer();
                        return;
                    }
                    if (player.isATypeOfIronman()) {
                        player.sendMessage("You cannot do this as an ironman!");
                        return;
                    }
                    if (usingBorrowed) {
                        if (!Magic.checkRunes(player, data, false))
                            return;
                    } else {
                        if (!Magic.checkSpellRequirements(player, 33, false, 556, 1, 563, 1))
                            return;
                    }
                    if (!item.getDefinitions().isStackable() && player.getInventory().getFreeSlots() == 0 || (item.getDefinitions().isStackable() && player.getInventory().getAmountOf(item) == 0 && player.getInventory().getFreeSlots() == 0)) {
                        player.sendMessage("You need some more room in your inventory before you can cast this.");
                        return;
                    }
                    if (player.isGrabbing())
                        return;
                    player.lock();
                    player.setGrabbing(true);
                    if (!usingBorrowed)
                        Magic.checkSpellRequirements(player, 33, true, 556, 1, 563, 1);
                    player.setNextAnimation(new Animation(711));
                    player.getSkills().addXp(Skills.MAGIC, 10);
                    player.setNextGraphics(new Graphics(141, 0, 100));
                    Projectile projectile = World.sendProjectileCycles(player, item.getTile(), 142, 20, 2, 30, 80, 0, 0);
                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                    CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                        FloorItem item;

                        @Override
                        public boolean repeat() {
                            try {
                                if (player.isDead() || player.hasFinished()) {
                                    player.setGrabbing(false);
                                    player.unlock();
                                    return false;
                                }
                                if (item == null) {
                                    item = World.getRegion(orignalItem.getTile().getRegionId()).getGroundItem(orignalItem.getId(), orignalItem.getTile(), player);
                                    if (item == null || !item.exactMatch(orignalItem)) {
                                        item = null;
                                        player.setGrabbing(false);
                                        player.unlock();
                                        player.sendMessage("Too late! It's already gone.");
                                        return false;
                                    }
                                    World.removeGroundItem(item);
                                    World.sendGraphics(player, new Graphics(144), item.getTile());
                                    return true;
                                }
                                Projectile projectile = World.sendProjectileCycles(item.getTile(), player, 142, 2, 10, 0, 80, 0, 0);
                                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                                    @Override
                                    public boolean repeat() {
                                        try {
                                            if (player.isDead() || player.hasFinished()) {
                                                player.setGrabbing(false);
                                                player.unlock();
                                                return false;
                                            }
                                            player.getInventory().addItem(new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes()));
                                            player.setGrabbing(false);
                                            player.unlock();
                                            return false;
                                        } catch (Exception e) {
                                            Logger.getGlobal().catching(e);
                                            return false;
                                        }
                                    }
                                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                                return false;
                            } catch (Exception e) {
                                Logger.getGlobal().catching(e);
                                return false;
                            }
                        }
                    }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                    Magic.resetUsingBorrowedPowerSpell(player);
                    break;
                }
                Magic.resetSwapSpellBook(player);
            }
        }));
    }

    public static void handleSpellOnItem(Player player, int spellId, byte slot, boolean fromActionbar) {
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null)
            return;
        int spellBook = getSpellBook(data);// spellbook 3 means shared by all
        // books
        if (!hasLevel(player, data))
            return;
        if (spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return;
        }
        final Item target = player.getInventory().getItem(slot);
        if (target == null)
            return;
        if (!fromActionbar)
            player.stopAll();
        ItemSpell spell = ITEM_LUNAR_SPELLS.get(data.getId());
        if (spell != null) {
            if (player.getLunarDelay() > Utils.currentTimeMillis())
                return;
            if (player.isLocked())
                return;
            if (!checkSpellRequirements(player, spell.getLevel(), false, spell.getRunes()))
                return;
            if (spell.spellEffect(player, target)) {
                player.setLunarDelay(spell.getDelay());
                checkRunes(player, true, spell.getRunes());
                Magic.resetSwapSpellBook(player);
            }
            return;
        }
        switch (data.getId()) {
        case 32942:// Analyse
            player.getInventionManager().openAnalysisInterface(target.getId());
            return;
        case 32943:// Disassemble
            player.getActionManager().setAction(new Disassemble(target));
            return;
        case 14751:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LOW_ALCHEMY, target))
                return;
            break;
        case 14874:
        case 14770:
            boolean usingBorrowed = data.getId() == 14874;
            if (usingBorrowed && player.getBorrowedSpellId() != 47)
                break;
            if (!MagicOnItem.handleMagic(player, MagicOnItem.HIGH_ALCHEMY, target, usingBorrowed))
                return;
            break;
        case 14762:// Superheat Item
            if (!MagicOnItem.handleMagic(player, MagicOnItem.SUPER_HEAT, target))
                return;
            break;
        case 14742:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV1_ENCHANT, target))
                return;
            break;
        case 14754:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV2_ENCHANT, target))
                return;
            break;
        case 14765:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV3_ENCHANT, target))
                return;
            break;
        case 14772:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV4_ENCHANT, target))
                return;
            break;
        case 14785:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV5_ENCHANT, target))
                return;
            break;
        case 14797:
            if (!MagicOnItem.handleMagic(player, MagicOnItem.LV6_ENCHANT, target))
                return;
            break;
        default:
            System.out.println("missing spellon Item handling for spellId=" + spellId + ", name=" + data.getStringValue(2794) + ", dataId=" + data.getId());
            break;
        }
        Magic.resetSwapSpellBook(player);
    }

    public static void handleSpellOnWorldObject(final Player player, int spellId, WorldObject object) {
        player.faceObject(object);
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null)
            return;
        int spellBook = getSpellBook(data);
        if (!hasLevel(player, data))
            return;
        if (spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return;
        }
        player.stopAll();
        player.getActionManager().setAction(new SpellOnSomethingWalkAction(object, new Runnable() {

            @Override
            public void run() {
                player.faceObject(object);
                ObjectSpell spell = OBJECT_LUNAR_SPELLS.get(data.getId());
                if (spell != null) {
                    if (player.getLunarDelay() > Utils.currentTimeMillis())
                        return;
                    if (player.isLocked())
                        return;
                    if (!checkSpellRequirements(player, spell.getLevel(), false, spell.getRunes()))
                        return;
                    if (spell.spellEffect(player, object)) {
                        player.setLunarDelay(spell.getDelay());
                        checkRunes(player, true, spell.getRunes());
                        Magic.resetSwapSpellBook(player);
                    }
                    return;
                }
            }
        }));
        switch (data.getId()) {
        default:
            System.out.println("missing spellon Object handling for spellId=" + spellId + ", name=" + data.getStringValue(2794) + ", dataId=" + data.getId());
            break;
        }
    }

    public static void handleSpellOnEntity(final Player player, int spellId, Entity target) {
        player.setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null)
            return;
        int spellBook = getSpellBook(data);
        if (!hasLevel(player, data))
            return;
        if (spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return;
        }
        if (isCombatSpell(data)) {
            if (isAutoCastSpell(data) && Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use combat spells on swapped spellbooks.", true);
                return;
            }
            if (handleCombatSpell(player, spellId, 0))
                setCombat(player, target);
            return;
        }
        player.stopAll();
        player.getActionManager().setAction(new SpellOnSomethingWalkAction(target, new Runnable() {

            @Override
            public void run() {
                player.faceEntity(target);
                NPCSpell npcSpell = NPC_LUNAR_SPELLS.get(data.getId());
                if (npcSpell != null) {
                    if (!(target instanceof NPC)) {
                        player.getPackets().sendMainInterfaceMessage(1, "You can't use that spell on players.", true);
                        return;
                    }
                    if (player.getLunarDelay() > Utils.currentTimeMillis())
                        return;
                    if (player.isLocked())
                        return;
                    if (!checkSpellRequirements(player, npcSpell.getLevel(), false, npcSpell.getRunes()))
                        return;
                    if (npcSpell.spellEffect(player, (NPC) target)) {
                        player.setLunarDelay(npcSpell.getDelay());
                        checkRunes(player, true, npcSpell.getRunes());
                        Magic.resetSwapSpellBook(player);
                    }
                    return;
                }
                PlayerSpell pSpell = PLAYER_LUNAR_SPELLS.get(data.getId());
                if (pSpell != null) {
                    if (!(target instanceof Player)) {
                        player.getPackets().sendMainInterfaceMessage(1, "You can't use that spell on npcs.", true);
                        return;
                    }
                    if (player.getLunarDelay() > Utils.currentTimeMillis())
                        return;
                    if (player.isLocked())
                        return;
                    if (!checkSpellRequirements(player, pSpell.getLevel(), false, pSpell.getRunes()))
                        return;
                    if (pSpell.spellEffect(player, (Player) target)) {
                        player.setLunarDelay(pSpell.getDelay());
                        checkRunes(player, true, pSpell.getRunes());
                        Magic.resetSwapSpellBook(player);
                    }
                    return;
                }
            }
        }));
        switch (data.getId()) {
        default:
            System.out.println("missing spellon Entity handling for spellId=" + spellId + ", name=" + data.getStringValue(2794) + ", dataId=" + data.getId());
            break;
        }
    }

    public static GeneralRequirementMap getSpellData(int spellId) {
        int id = ClientScriptMap.getMap(6740).getIntValue(spellId);
        return id == -1 ? null : GeneralRequirementMap.getMap(id);
    }

    public static String getSpellName(GeneralRequirementMap data) {
        return data.getStringValue(2794);
    }

    public static boolean isAutoCastSpell(GeneralRequirementMap data) {
        return data.getIntValue(2874) == 1;
    }

    /*
     * 1 - air, 2 - water, 3 - earth, 4 - fire, others no type
     */
    public static int getSpellType(GeneralRequirementMap data) {
        return data.getIntValue(2873);
    }

    public static int getSpellBook(GeneralRequirementMap data) {
        return data.getIntValue(2871);
    }

    public static boolean hasLevel(Player player, GeneralRequirementMap data) {
        return Magic.checkSpellLevel(player, getSpellLevel(data));
    }

    private static int getSpellLevel(GeneralRequirementMap data) {
        return data.getIntValue(2807);
    }

    private static boolean isCombatSpell(GeneralRequirementMap data) {
        return getSpellType(data) != 0;
    }

    private static boolean isTeleportSpell(GeneralRequirementMap data) {
        return data.getIntValue(2941) != 0;
    }

    private static WorldTile getSpellTeleportLocation(GeneralRequirementMap data) {
        return new WorldTile(data.getIntValue(2941));
    }

    public static void handleSpell(Player player, int spellId, int packetId) {
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null)
            return;
        int spellBook = getSpellBook(data);// spellbook 3 means shared by all
        // books
        if (!hasLevel(player, data))
            return;
        if (spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return;
        }
        if (isCombatSpell(data)) {
            if (isAutoCastSpell(data) && Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use combat spells on swapped spellbooks.", true);
                return;
            }
            int autoCastPacketId = player.getCombatDefinitions().isManualSpellCasting() ? PacketRepository.ACTION_BUTTON9_PACKET : PacketRepository.ACTION_BUTTON2_PACKET;
            handleCombatSpell(player, spellId, packetId == autoCastPacketId ? 1 : 2);
            return;
        }
        if (data.getIntValue(2880) == 4) {
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.getDialogueManager().startDialogue("HomeTeleportD");
            } else {
//                player.stopAll();
//                HomeTeleport.useLodestone(player, player.getPreviousLodestone());
            }
            return;
        }
        if (isTeleportSpell(data)) {
            int upEmote = spellBook == 0 ? 8939 : spellBook == 1 ? 1979 : 9606;
            int downEmote = spellBook == 0 ? 8941 : spellBook == 1 ? -1 : -2;
            int upGfx = spellBook == 0 ? 1576 : spellBook == 1 ? 1681 : 1685;
            int downGfx = spellBook == 0 ? 1577 : -1;
            int[] runes = getRequiredRunes(data);
            int delay = 3 + spellBook;
            double xp = getSpellXP(data);
            int level = getSpellLevel(data);
            WorldTile tile = getSpellTeleportLocation(data);
            switch (data.getId()) {
            case 14781:
                if (player.getInventory().getAmountOf(1963) < 1) {
                    player.sendMessage("You do not have enough " + ItemDefinitions.getItemDefinitions(1963).getName() + "s to cast this spell.");
                    return;
                }
                if (sendTeleportSpell(player, upEmote, downEmote, upGfx, downGfx, level, xp, tile, delay, true, MAGIC_TELEPORT, runes)) {
                    player.getInventory().deleteItem(1963, 1);
                    Magic.resetSwapSpellBook(player);
                }
                return;
            }
            if (sendTeleportSpell(player, upEmote, downEmote, upGfx, downGfx, level, xp, tile, delay, true, MAGIC_TELEPORT, runes))
                Magic.resetSwapSpellBook(player);
            return;
        }
        DefaultSpell spell = DEFAULT_LUNAR_SPELLS.get(data.getId());
        if (spell != null) {
            if (player.getLunarDelay() > Utils.currentTimeMillis())
                return;
            if (player.isLocked())
                return;
            if (!checkSpellRequirements(player, spell.getLevel(), false, spell.getRunes()))
                return;
            if (spell.spellEffect(player)) {
                player.setLunarDelay(spell.getDelay());
                checkRunes(player, true, spell.getRunes());
                Magic.resetSwapSpellBook(player);
            }
            return;
        }
        switch (data.getId()) {
        case 14740:
            if (Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use this spell while on swapped spellbook.", true);
                return;
            }
            BoltEnchanting.sendBoltEnchantingInterface(player);
            return;
        case 14747:// Bones To Bananas
            int bonesAmt = 0;
            for (int x = 0; x < 28; x++)
                if (player.getInventory().getItem(x) != null && player.getInventory().getItem(x).getName().contains("bones") && !player.getInventory().getItem(x).getDefinitions().isNoted() || player.getInventory().getItem(x) != null && !player.getInventory().getItem(x).getDefinitions().isNoted() && player.getInventory().getItem(x).getName().contains("Bones"))
                    bonesAmt++;
            if (bonesAmt == 0) {
                player.sendMessage("You have no bones in your inventory which you could transform to bananas.");
                return;
            }
            if (!Magic.checkSpellRequirements(player, 60, false, 557, 2, 555, 2, 561, 1))
                return;
            for (int i = 0; i < 28; i++) {
                if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("bones") && !player.getInventory().getItem(i).getDefinitions().isNoted() || player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("Bones") && !player.getInventory().getItem(i).getDefinitions().isNoted())
                    player.getInventory().getItem(i).setId(1963);
                player.getInventory().refresh(i);
            }
            Magic.checkSpellRequirements(player, 15, true, 557, 2, 555, 2, 561, 1);
            player.getSkills().addXp(Skills.MAGIC, 25);
            player.setNextAnimation(new Animation(722));
            player.setNextGraphics(new Graphics(141, 0, 100));
            player.sendMessage("You transform all the bones in your inventory into bananas.");
            break;
        case 14776:// Bones To Peaches
            bonesAmt = 0;
            for (int x = 0; x < 28; x++)
                if (player.getInventory().getItem(x) != null && player.getInventory().getItem(x).getName().contains("bones") && !player.getInventory().getItem(x).getDefinitions().isNoted() || player.getInventory().getItem(x) != null && !player.getInventory().getItem(x).getDefinitions().isNoted() && player.getInventory().getItem(x).getName().contains("Bones"))
                    bonesAmt++;
            if (bonesAmt == 0) {
                player.sendMessage("You have no bones in your inventory which you could transform to peaches.");
                return;
            }
            if (!Magic.checkSpellRequirements(player, 60, false, 557, 4, 555, 4, 561, 2))
                return;
            for (int i = 0; i < 28; i++) {
                if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("bones") && !player.getInventory().getItem(i).getDefinitions().isNoted() || player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("Bones") && !player.getInventory().getItem(i).getDefinitions().isNoted())
                    player.getInventory().getItem(i).setId(6883);
                player.getInventory().refresh(i);
            }
            Magic.checkSpellRequirements(player, 60, true, 557, 4, 555, 4, 561, 2);
            player.getSkills().addXp(Skills.MAGIC, 35.5);
            player.setNextAnimation(new Animation(722));
            player.setNextGraphics(new Graphics(141));
            player.sendMessage("You transform all the bones in your inventory into peaches.");
            break;
        case 6845:// Spellbook Swap (Standard)
        case 6846:// Spellbook Swap (Ancient)
        case 6847:// Spellbook Swap (Standard)
        case 6848:// Spellbook Swap (Lunar)
            if (Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use this spell while on swapped spellbook.", true);
                return;
            }
            if (!Magic.hasRequirements(player, data, true))
                return;
            Magic.checkRunes(player, data, true);
            int toBookId = data.getId() == 6845 || data.getId() == 6847 ? 0 : data.getId() == 6846 ? 1 : 2;
            Magic.swapSpellBook(player, toBookId);
            return;
        case 14873://
            if (Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use this spell while on swapped spellbook.", true);
                return;
            }
            if (!Magic.hasRequirements(player, data, true))
                return;
            int option = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 1 : 2;
            if (option == 0) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Swap to Standard", "Swap to Ancient");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        end();
                        if (componentId == OPTION_1) {
                            Magic.checkRunes(player, data, true);
                            Magic.swapSpellBook(player, 0);
                        } else if (componentId == OPTION_2) {
                            Magic.checkRunes(player, data, true);
                            Magic.swapSpellBook(player, 1);
                        }
                    }

                    @Override
                    public void finish() {

                    }

                });
                return;
            }
            Magic.checkRunes(player, data, true);
            Magic.swapSpellBook(player, option - 1);
            return;
        case 14874:// Borrowed Power
            if (Magic.isOnSwapSpellBook(player)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use this spell while on swapped spellbook.", true);
                return;
            }
            if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                if (player.getBorrowedSpellId() == 0)
                    Magic.sendSetBorrowedPowerConfirmation(player);
                else
                    Magic.sendUnSetBorrowedPowerConfirmation(player);
                return;
            }
            if (player.getBorrowedSpellId() == 0) {
                player.getPackets().sendMainInterfaceMessage(1, "You have to set a spell first (right-click \"set\").", true);
                return;
            }
            if (!checkRunes(player, data, false))
                return;
            player.getTemporaryAttributtes().put(Key.USING_BORROWED_POWER, Boolean.TRUE);
            GeneralRequirementMap borrowedData = getSpellData(player.getBorrowedSpellId());
            if (isCombatSpell(borrowedData)) {
                handleCombatSpell(player, player.getBorrowedSpellId(), 2, true);
                return;
            }
            switch (borrowedData.getId()) {
            case 14760:// house teleport
                double xp = getSpellXP(borrowedData);
                int level = getSpellLevel(borrowedData);
                WorldTile tile = getSpellTeleportLocation(borrowedData);
                if (sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, true, MAGIC_TELEPORT))
                    Magic.resetUsingBorrowedPowerSpell(player);
                break;
            case 14747:// Bones To Bananas
                bonesAmt = 0;
                for (int x = 0; x < 28; x++)
                    if (player.getInventory().getItem(x) != null && player.getInventory().getItem(x).getName().contains("bones") && !player.getInventory().getItem(x).getDefinitions().isNoted() || player.getInventory().getItem(x) != null && !player.getInventory().getItem(x).getDefinitions().isNoted() && player.getInventory().getItem(x).getName().contains("Bones"))
                        bonesAmt++;
                if (bonesAmt == 0) {
                    player.sendMessage("You have no bones in your inventory which you could transform to bananas.");
                    return;
                }
                for (int i = 0; i < 28; i++) {
                    if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("bones") && !player.getInventory().getItem(i).getDefinitions().isNoted() || player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getName().contains("Bones") && !player.getInventory().getItem(i).getDefinitions().isNoted())
                        player.getInventory().getItem(i).setId(1963);
                    player.getInventory().refresh(i);
                }
                player.getSkills().addXp(Skills.MAGIC, 25);
                player.setNextAnimation(new Animation(722));
                player.setNextGraphics(new Graphics(141, 0, 100));
                player.sendMessage("You transform all the bones in your inventory into bananas.");
                Magic.resetUsingBorrowedPowerSpell(player);
                break;
            }
            break;
        default:
            System.out.println("missing spell handling for spellId=" + spellId + ", name=" + data.getStringValue(2794) + ", dataId=" + data.getId());
            break;
        }
        Magic.resetSwapSpellBook(player);
    }

    private static boolean handleCombatSpell(Player player, int spellId, int set) {
        return handleCombatSpell(player, spellId, set, false);
    }

    private static boolean handleCombatSpell(Player player, int spellId, int set, boolean borrowed) {
        GeneralRequirementMap data = getSpellData(spellId);
        if (data == null || !hasLevel(player, data))
            return false;
        int spellBook = getSpellBook(data);
        if (!hasLevel(player, data))
            return false;
        if (!borrowed && spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return false;
        }
        if (set >= 0) {
            if (set == 0 || set == 2) {
                if (set == 2 && !(player.getActionManager().getAction() instanceof PlayerCombat)) {
                    Entity target = player.getCombatDefinitions().getCurrentTarget();
                    if (target == null || target.isDead() || target.hasFinished()) {
                        player.getPackets().sendGameMessage("You don't have a target.");
                        return false;
                    }
                    if (!player.withinDistance(target)) {
                        player.getPackets().sendGameMessage("Your target is too far away.");
                        return false;
                    }
                    setCombat(player, target);
                }
                player.getTemporaryAttributtes().put("tempCastSpell", spellId);
            } else if (isAutoCastSpell(data))
                player.getCombatDefinitions().setAutoCast(spellId);
        }
        return true;

    }

    private static void setCombat(Player player, Entity target) {
        player.setNextFaceWorldTile(target.getMiddleWorldTile());
        if (!player.getControlerManager().canHit(target))
            return;
        if (target instanceof Player) {
            Player p2 = (Player) target;
            if (!player.isCanPvp() || !p2.isCanPvp()) {
                player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
                return;
            }
            if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
                if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    player.getPackets().sendGameMessage("You are already in combat.", true);
                    return;
                }
                if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    if (p2.getAttackedBy() instanceof NPC) {
                        p2.setAttackedBy(player); // changes enemy to player,
                        // player has priority over
                        // npc on single areas
                    } else {
                        player.getPackets().sendGameMessage("That player is already in combat.", true);
                        return;
                    }
                }
            }
        } else if (target instanceof Familiar) {
            Familiar familiar = (Familiar) target;
            if (familiar == player.getFamiliar()) {
                player.getPackets().sendGameMessage("You can't attack your own familiar.");
                return;
            }
            if (!familiar.canAttack(player)) {
                player.getPackets().sendGameMessage("You can't attack this npc.");
                return;
            }
        } else if (target instanceof NPC) {
            if (!((NPC) target).getDefinitions().hasAttackOption()) {
                player.getPackets().sendGameMessage("You can't attack this npc.");
                return;
            }
            else if (!((NPC) target).isForceMultiAttacked()) {
                if (!target.isAtMultiArea() || !player.isAtMultiArea()) {
                    if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != target && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        player.getPackets().sendGameMessage("You are already in combat.", true);
                        return;
                    }
                    if (target.getAttackedBy() != player && target.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        player.getPackets().sendGameMessage("This npc is already in combat.", true);
                        return;
                    }
                }
            }
        }
        player.stopAll(true);
        player.getActionManager().setAction(new PlayerCombat(target));
    }

    public static boolean hasRequirements(Player player, GeneralRequirementMap data, boolean mainHand) {
        int weaponId = mainHand ? player.getEquipment().getWeaponId() : player.getEquipment().getShieldId();
        if (ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("polypore staff") && data.getId() != 14880) {
            player.getPackets().sendGameMessage("You can't cast this spell with this weapon.");
            return false;
        }
        switch (data.getId()) {
        case 14767:// slayer dart
            if (!hasStaffOfLight(weaponId) && (weaponId == 4170 || weaponId == 30828 || weaponId == 36637 || weaponId == 30825 || weaponId == 30827 || weaponId == 36635 || weaponId == 30830))
                return true;
            player.getPackets().sendGameMessage("You can't cast this spell with this weapon.");
            return false;
        case 14880:// polypore strike
            if (!ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("polypore staff")) {
                player.getPackets().sendGameMessage("You can't cast this spell with this weapon.");
                return false;
            }
            return true;
        case 14771:// charge orb spells
        case 14775:
        case 14780:
        case 14783:
            return player.getInventory().getAmountOf(567) > 0;
            case 14781:// ape atoll teleport
            return player.getInventory().getAmountOf(1963) > 0;
            case 32942:// analysis
        case 32943:// disassemble
            return true;
        }
        if (isUsingBorrowedPowerSpell(player))
            return true;
        return checkRunes(player, data, false);
    }

    public static int[] getRequiredRunes(GeneralRequirementMap data) {
        List<Integer> reqs = new ArrayList<>();
        int airRunes = data.getIntValue(2898);
        if (airRunes > 0) {
            reqs.add(AIR_RUNE);
            reqs.add(airRunes);
        }
        int mindRunes = data.getIntValue(2902);
        if (mindRunes > 0) {
            reqs.add(MIND_RUNE);
            reqs.add(mindRunes);
        }
        int waterRunes = data.getIntValue(2900);
        if (waterRunes > 0) {
            reqs.add(WATER_RUNE);
            reqs.add(waterRunes);
        }
        int earthRunes = data.getIntValue(2899);
        if (earthRunes > 0) {
            reqs.add(EARTH_RUNE);
            reqs.add(earthRunes);
        }
        int fireRunes = data.getIntValue(2901);
        if (fireRunes > 0) {
            reqs.add(FIRE_RUNE);
            reqs.add(fireRunes);
        }
        int bodyRunes = data.getIntValue(2903);
        if (bodyRunes > 0) {
            reqs.add(BODY_RUNE);
            reqs.add(bodyRunes);
        }
        int cosmicRunes = data.getIntValue(2910);
        if (cosmicRunes > 0) {
            reqs.add(COSMIC_RUNE);
            reqs.add(cosmicRunes);
        }
        int chaosRunes = data.getIntValue(2904);
        if (chaosRunes > 0) {
            reqs.add(CHAOS_RUNE);
            reqs.add(chaosRunes);
        }
        int astralRunes = data.getIntValue(2908);
        if (astralRunes > 0) {
            reqs.add(ASTRAL_RUNE);
            reqs.add(astralRunes);
        }
        int natureRunes = data.getIntValue(2909);
        if (natureRunes > 0) {
            reqs.add(NATURE_RUNE);
            reqs.add(natureRunes);
        }
        int lawRunes = data.getIntValue(2911);
        if (lawRunes > 0) {
            reqs.add(LAW_RUNE);
            reqs.add(lawRunes);
        }
        int deathRunes = data.getIntValue(2905);
        if (deathRunes > 0) {
            reqs.add(DEATH_RUNE);
            reqs.add(deathRunes);
        }
        int bloodRunes = data.getIntValue(2906);
        if (bloodRunes > 0) {
            reqs.add(BLOOD_RUNE);
            reqs.add(bloodRunes);
        }
        int soulRunes = data.getIntValue(2907);
        if (soulRunes > 0) {
            reqs.add(SOUL_RUNE);
            reqs.add(soulRunes);
        }
        int armadylRunes = data.getIntValue(2912);
        if (armadylRunes > 0) {
            reqs.add(ARMADYL_RUNE);
            reqs.add(armadylRunes);
        }

        int[] values = new int[reqs.size()];
        for (int i = 0; i < reqs.size(); i++)
            values[i] = reqs.get(i);
        return values;
    }

    public static double getSpellXP(GeneralRequirementMap data) {
        return data.getIntValue(2891) / 10.0;
    }

    public static SpellEffect getCombatSpellEffect(GeneralRequirementMap data) {
        switch (data.getId()) {
        case 14739:// confuse
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage == 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.HIT_CHANCE_MODIFIER, 60000, -5);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14745:// weaken
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage == 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_DEALT_MODIFIER, 60000, -0.05);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14749:// curse
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage == 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_RECIEVED_MODIFIER, 60000, 0.05);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14750:// bind
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage > 0) {
                        target.addFreezeDelay(5000, true);
                        if (mage_hit_gfx != null)
                            target.setNextGraphics(mage_hit_gfx);
                        player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                    }
                }
            };
        case 14766:// snare
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage > 0) {
                        target.addFreezeDelay(10000, true);
                        if (mage_hit_gfx != null)
                            target.setNextGraphics(mage_hit_gfx);
                        player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                    }
                }
            };
        case 14784:// VULNERABILITY
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage == 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_RECIEVED_MODIFIER, 60000, 0.10);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14787:// enfeeble
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage <= 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_DEALT_MODIFIER, 60000, -0.1);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14791:// entangle
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage > 0) {
                        target.addFreezeDelay(15000, true);
                        if (mage_hit_gfx != null)
                            target.setNextGraphics(mage_hit_gfx);
                        player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                    }
                }
            };
        case 14792:// stagger
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage == 0)
                        return;
                    target.getTemporaryModifiersManager().applyModifier(Key.HIT_CHANCE_MODIFIER, 60000, -10);
                    if (mage_hit_gfx != null)
                        target.setNextGraphics(mage_hit_gfx);
                    player.getPackets().sendGameMessage("<col=00ff00>You have successfully applied that hex to your target.");
                }
            };
        case 14795:// teleport block
            return new SpellEffect() {
                @Override
                public void spellEffect(Player player, Entity target, int damage, final Graphics mage_hit_gfx) {
                    if (damage > 0 && target instanceof Player) {
                        final Player p2 = (Player) target;
                        final boolean halved = p2.getPrayer().isMageProtecting();
                        p2.sendMessage(Colors.LPURPLE + "A teleblock has been cast on you! It will wear off in about " + (halved ? "2 minutes and 30 seconds." : "5 minutes."));
                        p2.setTeleBlockDelay(halved ? 150000 : 300000);
                        if (mage_hit_gfx != null)
                            target.setNextGraphics(mage_hit_gfx);
                    }
                }
            };
        }
        return null;
    }

    public static boolean isOnSwapSpellBook(Player player) {
        return player.getTemporaryAttributtes().get(Key.SWAP_BOOK_ID) != null;
    }

    public static Integer getSwappedSpellBookId(Player player) {
        return (Integer) player.getTemporaryAttributtes().get(Key.SWAP_BOOK_ID);
    }

    public static void resetSwapSpellBook(Player player) {
        player.getTemporaryAttributtes().remove(Key.SWAP_BOOK_ID);
        player.getCombatDefinitions().refreshSpellBook();
    }

    public static void swapSpellBook(Player player, int id) {
        player.getTemporaryAttributtes().put(Key.SWAP_BOOK_ID, id);
        player.getTemporaryAttributtes().put(Key.SWAP_BOOK_COOLDOWN, Utils.currentTimeMillis() + 120000L);
        player.getCombatDefinitions().refreshSpellBook();
        int actualSpellBook = player.getCombatDefinitions().getActualSpellBook();
        String spellBookName = actualSpellBook == 0 ? "Standard" : actualSpellBook == 1 ? "Ancient" : "Lunar";
        player.getPackets().sendMainInterfaceMessage(1, "You have 2 minutes before your SpellBook changes back to the " + spellBookName + " spellbook.", true);
    }

    public static void checkRemoveSwapedSpellBook(Player player) {
        if (!isOnSwapSpellBook(player))
            return;
        Long cd = (Long) player.getTemporaryAttributtes().get(Key.SWAP_BOOK_COOLDOWN);
        if (cd == null || cd >= Utils.currentTimeMillis())
            return;
        int actualSpellBook = player.getCombatDefinitions().getActualSpellBook();
        String spellBookName = actualSpellBook == 0 ? "Standard" : actualSpellBook == 1 ? "Ancient" : "Lunar";
        resetSwapSpellBook(player);
        player.getPackets().sendMainInterfaceMessage(1, "Your spellbook reverts back to the " + spellBookName + " spellbook.", true);
    }

    public static boolean isUsingBorrowedPowerSpell(Player player) {
        return player.getTemporaryAttributtes().get(Key.USING_BORROWED_POWER) != null;
    }

    public static void resetUsingBorrowedPowerSpell(Player player) {
        if (!Magic.isUsingBorrowedPowerSpell(player))
            return;
        player.getTemporaryAttributtes().remove(Key.USING_BORROWED_POWER);
        Magic.checkRunes(player, Magic.getSpellData(157), true);
        player.useBorrowedSpellCharge();
        player.getPackets().sendGameMessage("You have " + player.getBorrowedSpellChargesRemaining() + " casts on ur borrowed power spell.", true);
    }

    public static void sendSetBorrowedPowerSpellId(Player player, int spellId) {
        GeneralRequirementMap data = Magic.getSpellData(spellId);
        int[] runes = Magic.getRequiredRunes(data);
        int runesCount = 0;
        while (runesCount < runes.length) {
            int runeId = runes[runesCount++];
            int amount = runes[runesCount++] * 1000;
            if (player.getInventory().getAmountOf(runeId) < amount) {
                player.sendMessage("You need atleast " + amount + " x " + ItemDefinitions.getItemDefinitions(runeId).getName() + " to set this spell.");
                return;
            }
        }
        runesCount = 0;
        while (runesCount < runes.length) {
            int runeId = runes[runesCount++];
            int amount = runes[runesCount++] * 1000;
            player.getInventory().deleteItem(runeId, amount);
        }
        player.setBorrowedSpellId(spellId);
        player.getPackets().sendGameMessage("You set borrowed power spell to: " + Magic.getSpellName(data) + ", You now have 1000 casts remaining.");
    }

    public static void sendSetBorrowedPowerConfirmation(Player player) {
        if (player.getBorrowedSpellId() != 0)
            return;
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as Air Wave.", "Set as Water Wave.", "Set as Earth Wave.", "Set as Fire Wave.", "More options.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    switch (componentId) {
                    case OPTION_1:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 58);
                        break;
                    case OPTION_2:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 61);
                        break;
                    case OPTION_3:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 65);
                        break;
                    case OPTION_4:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 68);
                        break;
                    case OPTION_5:
                        stage = 0;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as Slayer Dart.", "Set as Vulnerability.", "Set as Enfeeble.", "Set as Entangle.", "More options.");
                        break;
                    }
                    break;
                case 0:
                    switch (componentId) {
                    case OPTION_1:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 44);
                        break;
                    case OPTION_2:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 63);
                        break;
                    case OPTION_3:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 66);
                        break;
                    case OPTION_4:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 70);
                        break;
                    case OPTION_5:
                        stage = 1;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as House Teleport.", "Set as Telekinetic Grab.", "Set as High Level Alchemy.", "Set as Bones To Peaches.", "Back to begining.");
                        break;
                    }
                    break;
                case 1:
                    switch (componentId) {
                    case OPTION_1:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 36);
                        break;
                    case OPTION_2:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 32);
                        break;
                    case OPTION_3:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 47);
                        break;
                    case OPTION_4:
                        end();
                        Magic.sendSetBorrowedPowerSpellId(player, 53);
                        break;
                    case OPTION_5:
                        stage = -1;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Set as Air Wave.", "Set as Water Wave.", "Set as Earth Wave.", "Set as Fire Wave.", "More options.");
                        break;
                    }
                    break;
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    public static void sendUnSetBorrowedPowerConfirmation(Player player) {
        if (player.getBorrowedSpellId() == 0 || player.getBorrowedSpellChargesRemaining() == 0)
            return;
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("ARE YOU SURE YOU WANT TO UNSET YOUR BORROWED POWER SPELL?", "Yes, I am sure", "Nevermind.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (componentId == OPTION_1) {
                    GeneralRequirementMap data = Magic.getSpellData(player.getBorrowedSpellId());
                    int[] runes = Magic.getRequiredRunes(data);
                    int runesCount = 0;
                    while (runesCount < runes.length) {
                        int runeId = runes[runesCount++];
                        int amount = runes[runesCount++] * player.getBorrowedSpellChargesRemaining();
                        player.addItem(runeId, amount);
                    }
                    player.setBorrowedSpellId(0);
                    player.getPackets().sendGameMessage("You have unset your borrowed power spell.");
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    public static boolean checkRunes(Player player, GeneralRequirementMap data, boolean delete) {
        int[] values = getRequiredRunes(data);
        return checkRunes(player, delete, values);
    }

    private static boolean containsRune(int rune, Item[] items) {
        for (Item item : items) {
            if (item != null && item.getId() == rune)
                return true;
        }
        return false;
    }

    public static final int getTotalAmountOfRune(Player player, int runeId, int weaponId, int shieldId) {
        if (hasInfiniteRunes(runeId, weaponId, shieldId))
            return 99999999;
        int amount = player.getInventory().getAmountOf(runeId);
        if (weaponId == 18342 && runeId == LAW_RUNE)
            amount += player.getLawRunes();
        if (weaponId == 18341 && runeId == NATURE_RUNE)
            amount += player.getNatureRunes();
        if (player.getInventory().containsItem(ELEMENTAL_RUNE, 1)) {
            if (runeId == AIR_RUNE || runeId == WATER_RUNE || runeId == EARTH_RUNE || runeId == FIRE_RUNE)
                amount += player.getInventory().getAmountOf(ELEMENTAL_RUNE);
        }
        if (player.getInventory().containsItem(CATALYTIC_RUNE, 1)) {
            if (runeId == MIND_RUNE || runeId == CHAOS_RUNE || runeId == DEATH_RUNE || runeId == BLOOD_RUNE || runeId == BODY_RUNE || runeId == NATURE_RUNE || runeId == ASTRAL_RUNE || runeId == SOUL_RUNE || runeId == LAW_RUNE)
                amount += player.getInventory().getAmountOf(CATALYTIC_RUNE);
        }
        Item[] runesInPouch = player.getEquipment().getAmmoId() == 38453 || player.getInventory().containsItem(38453, 1) ? player.getBigRunePouch().values().toArray(new Item[player.getSmallRunePouch().values().size()]) : player.getInventory().containsItem(38451, 1) ? player.getSmallRunePouch().values().toArray(new Item[player.getBigRunePouch().values().size()]) : null;
        if (runesInPouch != null)
            for (Item item : runesInPouch)
                if (item != null && item.getId() == runeId)
                    amount += item.getAmount();
        return amount;
    }
    
    public static boolean checkRunes(Player player, boolean delete, int... runes) {
        List<Item> runesL = new ArrayList<Item>();
        int runesCount = 0;
        while (runesCount < runes.length) {
            int runeId = runes[runesCount++];
            int ammount = runes[runesCount++];
            runesL.add(new Item(runeId, ammount));
        }
        return checkRunes( player,  delete, runesL.toArray(new Item[runesL.size()]));
    }

    public static final boolean checkRunes(Player player, boolean delete, Item... runes) {
        int weaponId = player.getEquipment().getWeaponId();
        int shieldId = player.getEquipment().getShieldId();
        Item[] runesInPouch = player.getEquipment().getAmmoId() == 38453 || player.getInventory().containsItem(38453, 1) ? player.getBigRunePouch().values().toArray(new Item[player.getSmallRunePouch().values().size()]) : player.getInventory().containsItem(38451, 1) ? player.getSmallRunePouch().values().toArray(new Item[player.getBigRunePouch().values().size()]) : null;
        for (Item item : runes) {
            if (item == null || item.getId() == -1)
                continue;
            int runeId = item.getId() > 17779 && item.getId() < 18000 ? item.getId() - 1689 : item.getId();
            if (getTotalAmountOfRune(player, runeId, weaponId, shieldId) < item.getAmount()) {
                player.sendMessage("You do not have enough " + ItemDefinitions.getItemDefinitions(runeId).getName().replace("rune", "Rune") + "s to cast this spell.");
                return false;
            }
        }
        if (!delete || player.getPerkManager().hasPerkActive(DonationPerk.AUBURY__S_APPRENTICE) && Math.random() <= 0.30)
            return true;
        boolean staffActivated = hasStaffOfLight(weaponId) && !containsRune(LAW_RUNE, runes) && !containsRune(NATURE_RUNE, runes) && Utils.random(8) == 0;
        if (staffActivated) {
            player.getPackets().sendGameMessage("The power of your staff of light saves some runes from being drained.", true);
            return true;
        }
        for (Item item : runes) {
            int runeId = item.getId();
            if (hasInfiniteRunes(item.getId() > 17779 && item.getId() < 18000 ? (item.getId() - 1689) : item.getId(), weaponId, shieldId))
                continue;
            if (weaponId == 18342 && runeId == LAW_RUNE) {
                if (player.getLawRunes() >= item.getAmount()) {
                    player.setLawRunes(player.getLawRunes() - item.getAmount());
                    item.forceSetAmount(0);
                } else {
                    item.setAmount(item.getAmount() - player.getLawRunes());
                    player.setLawRunes(0);
                }
            }
            if (weaponId == 18341 && runeId == NATURE_RUNE) {
                if (player.getNatureRunes() >= item.getAmount()) {
                    player.setNatureRunes(player.getNatureRunes() - item.getAmount());
                    item.forceSetAmount(0);
                } else {
                    item.setAmount(item.getAmount() - player.getNatureRunes());
                    player.setNatureRunes(0);
                }
            }
            if (runesInPouch != null) {
                boolean large = player.getEquipment().getAmmoId() == 38453 || player.getInventory().containsItem(38453, 1);
                for (Item pouchRune : runesInPouch) {
                    if (pouchRune == null || runeId != pouchRune.getId())
                        continue;
                    if (pouchRune.getAmount() >= item.getAmount()) {
                        player.removeRune(new Item(runeId, item.getAmount()), !large);
                        item.forceSetAmount(0);
                    } else {
                        item.forceSetAmount(item.getAmount() - pouchRune.getAmount());
                        player.removeRune(new Item(runeId, pouchRune.getAmount()), !large);
                    }
                }
            }
            if ((runeId == AIR_RUNE || runeId == EARTH_RUNE || runeId == FIRE_RUNE || runeId == WATER_RUNE)) {
                if (player.getInventory().getAmountOf(12850) >= item.getAmount()) {
                    player.getInventory().deleteItem(12850, item.getAmount());
                    item.forceSetAmount(0);
                } else if (player.getInventory().containsItem(12850, 1)) {
                    item.forceSetAmount(item.getAmount() - player.getInventory().getAmountOf(12850));
                    player.getInventory().deleteItem(12850, player.getInventory().getAmountOf(12850));
                }
            }
            if ((runeId == MIND_RUNE || runeId == CHAOS_RUNE || runeId == DEATH_RUNE || runeId == 559 || runeId == SOUL_RUNE || runeId == 564 || runeId == ASTRAL_RUNE || runeId == NATURE_RUNE || runeId == LAW_RUNE || runeId == 21773)) {
                if (player.getInventory().getAmountOf(12851) >= item.getAmount()) {
                    player.getInventory().deleteItem(12851, item.getAmount());
                    item.forceSetAmount(0);
                } else if (player.getInventory().containsItem(12851, 1)) {
                    item.forceSetAmount(item.getAmount() - player.getInventory().getAmountOf(12851));
                    player.getInventory().deleteItem(12851, player.getInventory().getAmountOf(12851));
                }
            }
            if (item.getAmount() == 0)
                continue;
            player.getInventory().deleteItem(item.getId(), item.getAmount());
        }
        return true;
    }
}
