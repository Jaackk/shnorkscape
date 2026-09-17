package com.rs.game.player.combat;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.EffectsManager.Effect;
import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.npc.BloodReaver;
import com.rs.game.activities.aod.npc.Crystal;
import com.rs.game.activities.dfm.DemonFlashBoss;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedGemstoneDragon;
import com.rs.game.npc.NPC;
import com.rs.game.npc.camelwarrior.Mirage;
import com.rs.game.npc.eds.TheSanctumGuardian;
import com.rs.game.npc.familiar.Bloodnihil;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Icenihil;
import com.rs.game.npc.familiar.Shadownihil;
import com.rs.game.npc.familiar.Smokenihil;
import com.rs.game.npc.fightkiln.HarAken;
import com.rs.game.npc.fightkiln.HarAkenTentacle;
import com.rs.game.npc.glacor.Glacyte;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.npc.others.MirrorbackSpider;
import com.rs.game.npc.others.Muspahs;
import com.rs.game.npc.pest.PestPortal;
import com.rs.game.npc.pet.Pet;
import com.rs.game.npc.qbd.QueenBlackDragon;
import com.rs.game.npc.slayer.HarpieBugSwarm;
import com.rs.game.npc.slayer.Kurask;
import com.rs.game.npc.slayer.Turoth;
import com.rs.game.npc.telos.Telos;
import com.rs.game.npc.themagister.CorruptedSoulObelisk;
import com.rs.game.npc.themagister.SoulObelisk;
import com.rs.game.npc.vorago.Vorago;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.magic.SpellEffect;
import com.rs.game.player.actions.slayer.SlayerHelmet;
import com.rs.game.player.actions.slayer.elite.EliteNPC;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerNPC;
import com.rs.game.player.combat.CombatUtils.VoidCombatType;
import com.rs.game.player.combat.CombatUtils.VoidType;
import com.rs.game.player.combat.rs2.Rs2AtaraxiaNumerics;
import com.rs.game.player.combat.rs2.Rs2SpellOverride;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.SlayerTask.Master;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;

import lombok.val;

public class PlayerCombat extends Action {


    /*
     * EOC-mode-only post-formula damage scale. The RS2 combat path returns
     * before this is applied (see the Settings.RS2_COMBAT branch in getHit),
     * so flipping RS2 on effectively gives the 2009scape-style 1.0x scale
     * without needing to retune this constant.
     */
    private static final double GLOBAL_FINAL_DAMAGE_MULT = 1.2;
    public static final boolean LEGACY = false;
    private Entity target;
    private int max_hit;
    private double base_mage_xp;
    private Graphics mage_hit_gfx;
    private int magic_sound;
    private int max_poison_hit;
    private int freeze_time;
    @SuppressWarnings("unused")
    private boolean reduceAttack;
    private boolean blood_spell;
    private boolean block_tele;
    private boolean ancientSpell;

    public PlayerCombat(final Entity target) {
        this.target = target;
    }

    public static int getSpecialCost(final int weaponId) {
        if (weaponId == 33625 || weaponId == 33627)
            return 100;
        if (ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("dragon dagger"))
            return 25;
        if (ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("seren godbow"))
            return 60;
        if (ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("decimation"))
            return 50;
        return ItemDefinitions.getItemDefinitions(weaponId).getSpecialCost();
    }

    /**
     * Checks if we can execute the special attack move.
     *
     * @param player The player executing.
     * @return if can use special attack.
     */
    public static boolean specialExecute(final Player player) {
        final int weaponId = player.getEquipment().getWeaponId();
        player.getCombatDefinitions().switchUsingSpecialAttack();
        int specAmt = getSpecialCost(weaponId);
        if (specAmt == 0) {
            player.sendMessage("This weapon has no special attack move.");
            player.getCombatDefinitions().decreaseSpecialAttack(0);
            return false;
        }
        if (player.getCombatDefinitions().hasRingOfVigour()) {
            specAmt *= 0.9;
        }
        if (player.getCombatDefinitions().getSpecialAttackPercentage() < specAmt) {
            player.sendMessage("You don't have enough special attack energy.");
            player.getCombatDefinitions().decreaseSpecialAttack(0);
            return false;
        }
        if (player.getEquipment().getWeaponId() == 14484) {
            if (player.getEquipment().getShieldId() != 25555 && player.getEquipment().getShieldId() != 25952) {
                player.sendMessage("You will need the off-hand Dragon claw in order to use the special attack.");
                return false;
            }
        }
        player.getCombatDefinitions().decreaseSpecialAttack(specAmt);
        return true;
    }

    public static final boolean hasMeleeDistanceWeapon(final Player player) {
        final String name = player.getEquipment().getWeaponId() == -1 ? "null" : ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getName();
        return name.toLowerCase().contains("halberd") || name.toLowerCase().contains("scythe") || name.toLowerCase().contains("rider lance") || hasConeAttack(player.getEquipment().getWeaponId());
    }

    private static void addAttackedByDelay(final Entity player, final Entity target) {
        target.setAttackedBy(player);
        target.setAttackedByDelay(Utils.currentTimeMillis() + 6000); // 8seconds
        player.setAttackingDelay(Utils.currentTimeMillis() + 6000);
    }

    public static void addAttackingDelay(final Entity player) {
        player.setAttackingDelay(Utils.currentTimeMillis() + 6000);
    }

    public static Entity[] getTargetsInTiles(final Player player, Entity target, WorldTile... tiles) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        List<Integer> regionIds = new ArrayList<Integer>();
        for (int i = 0; i < tiles.length; i++)
            if (!regionIds.contains(tiles[i].getRegionId()))
                regionIds.add(tiles[i].getRegionId());
        List<Integer> playerIndexes = new ArrayList<Integer>();
        List<Integer> npcIndexes = new ArrayList<Integer>();
        for (final int regionId : regionIds) {
            final Region region = World.getRegion(regionId);
            final List<Integer> playerIndexesr = region.getPlayerIndexes();
            final List<Integer> npcIndexesr = region.getNPCsIndexes();
            if (playerIndexesr != null)
                for (final int playerIndex : playerIndexesr) {
                    if (!playerIndexes.contains(playerIndex))
                        playerIndexes.add(playerIndex);
                }
            if (npcIndexesr != null)
                for (final int npcIndex : npcIndexesr) {
                    if (!npcIndexes.contains(npcIndex))
                        npcIndexes.add(npcIndex);
                }
        }
        if (playerIndexes != null && player.isCanPvp())
            for (final int playerIndex : playerIndexes) {
                final Player p2 = World.getPlayers().get(playerIndex);
                if (target != p2 && !p2.isAtMultiArea())
                    continue;
                if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !player.getControlerManager().canHit(p2) || !player.clipedProjectile(p2, false) || (target != p2 && p2.getControlerManager().getControler() instanceof Wilderness && !World.isMultiArea(p2))) {
                    continue;
                }
                for (WorldTile tile : tiles)
                    if (p2.matches(tile))
                        possibleTargets.add(p2);

            }
        if (npcIndexes != null)
            for (final int npcIndex : npcIndexes) {
                final NPC n = World.getNPCs().get(npcIndex);
                if (n == null || (n instanceof Pet && !player.isCanPvp()) || n == player.getFamiliar() || n == player.mirrorback || n.isDead() || n.hasFinished() || !n.getDefinitions().hasAttackOption() || !player.getControlerManager().canHit(n) || !player.clipedProjectile(n, false) || (n instanceof Familiar) && !player.isCanPvp() || (n instanceof MirrorbackSpider) && !player.isCanPvp()) {
                    continue;
                }
                if (target != n && !n.isAtMultiArea())
                    continue;
                for (WorldTile tile : tiles)
                    if (Utils.colides(tile, n, 1, n.getSize()))
                        possibleTargets.add(n);

            }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public static Entity[] getIsOnRangeMultiAttackTargets(final Player player, final Entity target, int range) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        possibleTargets.add(target);
        for (final int regionId : target.getMapRegionsIds()) {
            final Region region = World.getRegion(regionId);
            if (target instanceof Player) {
                final List<Integer> playerIndexes = region.getPlayerIndexes();
                if (playerIndexes == null) {
                    continue;
                }
                for (final int playerIndex : playerIndexes) {
                    final Player p2 = World.getPlayers().get(playerIndex);
                    if (!p2.isAtMultiArea())
                        continue;
                    if (p2 == null || p2 == player || p2 == target || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !player.getControlerManager().canHit(p2) || !player.clipedProjectile(p2, false) || (p2.getControlerManager().getControler() instanceof Wilderness && !World.isMultiArea(p2)) || !Utils.isOnRange(target.getMiddleWorldTile(), p2, range, 1, 1)) {
                        continue;
                    }
                    if (possibleTargets.contains(p2))
                        continue;
                    possibleTargets.add(p2);
                }
            } else {
                final List<Integer> npcIndexes = region.getNPCsIndexes();
                if (npcIndexes == null) {
                    continue;
                }
                for (final int npcIndex : npcIndexes) {
                    final NPC n = World.getNPCs().get(npcIndex);
                    if (n == null || n == target || (n instanceof Pet && !player.isCanPvp()) || n == player.getFamiliar() || n == player.mirrorback || n.isDead() || n.hasFinished() || !n.getDefinitions().hasAttackOption() || !player.getControlerManager().canHit(n) || !player.clipedProjectile(n, false) || (n instanceof Familiar) && !player.isCanPvp() || (n instanceof MirrorbackSpider) && !player.isCanPvp()) {
                        continue;
                    }
                    if (!n.isAtMultiArea() || !Utils.isOnRange(target.getMiddleWorldTile(), n, range, 1, 1))
                        continue;
                    if (possibleTargets.contains(n))
                        continue;
                    possibleTargets.add(n);
                }
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public static Entity[] getMultiAttackTargets(final Player player, final Entity target) {
        return getMultiAttackTargets(player, target, 1, 9, false);
    }

    public static Entity[] getMultiAttackTargets(final Player player, final Entity target, final int maxDistance, final int maxAmtTargets) {
        return getMultiAttackTargets(player, target, maxDistance, maxAmtTargets, false);
    }

    public static Entity[] getMultiAttackTargets(final Player player, final Entity target, final int maxDistance, final int maxAmtTargets, final boolean usePlayerLoc) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        possibleTargets.add(target);
        y: for (final int regionId : target.getMapRegionsIds()) {
            final Region region = World.getRegion(regionId);
            if (target instanceof Player) {
                final List<Integer> playerIndexes = region.getPlayerIndexes();
                if (playerIndexes == null) {
                    continue;
                }
                for (final int playerIndex : playerIndexes) {
                    final Player p2 = World.getPlayers().get(playerIndex);
                    if (!p2.isAtMultiArea())
                        continue;
                    if (p2 == null || p2 == player || p2 == target || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !p2.withinDistance(usePlayerLoc ? player : target, maxDistance) || !player.getControlerManager().canHit(p2) || !player.clipedProjectile(p2, false) || (p2.getControlerManager().getControler() instanceof Wilderness && !World.isMultiArea(p2))) {
                        continue;
                    }
                    possibleTargets.add(p2);
                    if (possibleTargets.size() == maxAmtTargets) {
                        break y;
                    }
                }
            } else {
                final List<Integer> npcIndexes = region.getNPCsIndexes();
                if (npcIndexes == null) {
                    continue;
                }
                for (final int npcIndex : npcIndexes) {
                    final NPC n = World.getNPCs().get(npcIndex);
                    if (n == null || n == target || (n instanceof Pet && !player.isCanPvp()) || n == player.getFamiliar() || n == player.mirrorback || n.isDead() || n.hasFinished() || !n.withinDistance(usePlayerLoc ? player : target, maxDistance) || !n.getDefinitions().hasAttackOption() || !player.getControlerManager().canHit(n) || !player.clipedProjectile(n, false) || (n instanceof Familiar) && !player.isCanPvp() || (n instanceof MirrorbackSpider) && !player.isCanPvp()) {
                        continue;
                    }
                    if (!n.isAtMultiArea())
                        continue;
                    possibleTargets.add(n);
                    if (possibleTargets.size() == maxAmtTargets) {
                        break y;
                    }
                }
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public static boolean hasConeAttack(int weaponId) {
        if (weaponId <= 0)
            return false;
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(weaponId);
        String name = defs.getName().toLowerCase();
        return name.contains(" scythe") || name.contains(" lance") || name.toLowerCase().contains("halberd") || name.toLowerCase().contains("masuta's warspear");
    }

    public static Entity[] getConeMultiAttackTargets(final Player player, final Entity target, final int maxAmtTargets) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        possibleTargets.add(target);
        WorldTile[] coneLocations = new WorldTile[6];
        byte[] dir = Utils.getDirection(player.getDirection());
        coneLocations[0] = new WorldTile(player.getX() + (dir[1] != 0 ? -dir[1] : dir[1]), player.getY() + (dir[0] != 0 ? dir[0] : dir[0]), player.getPlane()).transform(dir[0], dir[1], 0);
        coneLocations[1] = new WorldTile(player).transform(2 * dir[0], 2 * dir[1], 0);
        coneLocations[2] = new WorldTile(player).transform(1 * dir[0], 1 * dir[1], 0);
        coneLocations[3] = new WorldTile(player.getX() - (dir[1] != 0 ? -dir[1] : dir[1]), player.getY() - (dir[0] != 0 ? dir[0] : dir[0]), player.getPlane()).transform(dir[0], dir[1], 0);
        coneLocations[4] = new WorldTile(player.getX() + (dir[1] != 0 ? -dir[1] : dir[1]), player.getY() + (dir[0] != 0 ? dir[0] : dir[0]), player.getPlane());
        coneLocations[5] = new WorldTile(player.getX() - (dir[1] != 0 ? -dir[1] : dir[1]), player.getY() - (dir[0] != 0 ? dir[0] : dir[0]), player.getPlane());
        y: for (final int regionId : target.getMapRegionsIds()) {
            final Region region = World.getRegion(regionId);
            if (target instanceof Player) {
                final List<Integer> playerIndexes = region.getPlayerIndexes();
                if (playerIndexes == null) {
                    continue;
                }
                for (final int playerIndex : playerIndexes) {
                    final Player p2 = World.getPlayers().get(playerIndex);
                    if (!p2.isAtMultiArea())
                        continue;
                    if (p2 == null || p2 == player || p2 == target || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !player.getControlerManager().canHit(p2) || !player.clipedProjectile(p2, false) || (p2.getControlerManager().getControler() instanceof Wilderness && !World.isMultiArea(p2))) {
                        continue;
                    }
                    boolean insideCone = false;
                    for (int i = 0; i < coneLocations.length; i++) {
                        if (p2.matches(coneLocations[i])) {
                            insideCone = true;
                            break;
                        }
                    }
                    if (!insideCone)
                        continue;
                    possibleTargets.add(p2);
                    if (possibleTargets.size() == maxAmtTargets) {
                        break y;
                    }
                }
            } else {
                final List<Integer> npcIndexes = region.getNPCsIndexes();
                if (npcIndexes == null) {
                    continue;
                }
                for (final int npcIndex : npcIndexes) {
                    final NPC n = World.getNPCs().get(npcIndex);
                    if (n == null || n == target || (n instanceof Pet && !player.isCanPvp()) || n == player.getFamiliar() || n == player.mirrorback || n.isDead() || n.hasFinished() || !n.getDefinitions().hasAttackOption() || !player.getControlerManager().canHit(n) || !player.clipedProjectile(n, false) || (n instanceof Familiar) && !player.isCanPvp() || (n instanceof MirrorbackSpider) && !player.isCanPvp()) {
                        continue;
                    }
                    if (!n.isAtMultiArea())
                        continue;
                    boolean insideCone = false;
                    for (int i = 0; i < coneLocations.length; i++) {
                        if (n.matches(coneLocations[i])) {
                            insideCone = true;
                            break;
                        }
                    }
                    if (!insideCone)
                        continue;
                    possibleTargets.add(n);
                    if (possibleTargets.size() == maxAmtTargets) {
                        break y;
                    }
                }
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public boolean getRandomBoolean() {
        final Random random = new Random();
        return random.nextBoolean();
    }

    @Override
    public boolean start(final Player player) {
        player.setNextFaceEntity(target);
        player.getTemporaryAttributtes().remove(Key.REMOVE_CRACKLING_TARGET_TIMER);
        if (checkAll(player)) {
            player.getInventionManager().setCracklingTarget(target);
            return true;
        }
        player.getInventionManager().setCracklingTarget(null);
        player.setNextFaceEntity(null);
        return false;
    }

    @Override
    public boolean process(final Player player) {
        return checkAll(player);
    }

    private static boolean forceCheckClipAsRange(final Entity target) {
        return target instanceof NexMinion || target instanceof HarAken || target instanceof HarAkenTentacle || target instanceof PestPortal || target instanceof QueenBlackDragon || (target instanceof Telos && ((Telos) target).getPhase() == 4) || target instanceof SoulObelisk || target instanceof CorruptedSoulObelisk || target instanceof Crystal || target instanceof TheSanctumGuardian;
    }

    @Override
    public int processWithDelay(final Player player) {
        if (target.isDead() || target.hasFinished() || target == null)
            return -1;
        int attackingHand = getAttackingHand(player);
        if (attackingHand == -1)
            return 0;
        boolean mainHand = attackingHand == 1;
        // player is walking to atm
        if (!isWithinDistance(mainHand, player, target)) // doesnt let u attack when u
            // under / while walking out,
            // remove this check if u want
            return 0;

        if (!player.getControlerManager().keepCombating(mainHand, target))
            return -1;
        int delay = attack(player, mainHand);
        return delay;
    }

    // -1 has delay, 1 mainHand, 0 offHand
    public static int getAttackingHand(Player player) {
        int mainHandDelay = 0;
        if (Settings.DUAL_COMBAT) {
            mainHandDelay = (int) (player.getCombatDefinitions().getMainHandDelay() - Utils.currentWorldCycle());
            if (mainHandDelay > 0) {
                if (!player.getEquipment().hasOffHand())
                    return -1;
                int offHandDelay = (int) (player.getCombatDefinitions().getOffHandDelay() - Utils.currentWorldCycle());
                if (offHandDelay > 0)
                    return -1;
            }
        }
        return (!Settings.DUAL_COMBAT || mainHandDelay <= 0) ? 1 : 0;
    }

    private int attack(Player player, boolean mainHand) {
        if (!player.getControlerManager().keepCombating(mainHand, target))
            return -1;
        player.resetWalkSteps();
        int combatType = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        int spellId = player.getCombatDefinitions().getSpellId(!mainHand);
        if (spellId > 0) {
            boolean manualCast = spellId >= 1000;
            int delay = mageAttack(player, mainHand, manualCast ? spellId - 1000 : spellId, !manualCast);
            if (manualCast)
                player.getCombatDefinitions().resetSpells(false);
            return delay == -1 ? delay : (delay + (!Settings.DUAL_COMBAT ? -1 : 0));
        } else {
            if (combatType == Combat.MAGIC_TYPE) {
                player.getPackets().sendMainInterfaceMessage(1, "You need to select a spell to cast in your magic book to use magic.", true);
                return -1;
            }
            switch (combatType) {
            case Combat.RANGE_TYPE:
                int delay = rangeAttack(player, mainHand);
                return delay == -1 ? delay : (delay + (!Settings.DUAL_COMBAT ? -1 : 0));
            case Combat.MELEE_TYPE:
            default:
                delay = meleeAttack(player, mainHand);
                return delay == -1 ? delay : (delay + (!Settings.DUAL_COMBAT ? -1 : 0));
            }
        }
    }

    private int rangeAttack(final Player player, boolean mainHand) {
        if (!checkAmmo(player, mainHand))
            return -1;
        int delay = !Settings.DUAL_COMBAT ? getAttackSpeed(player, mainHand) : 0;
        final int weaponId = mainHand ? player.getEquipment().getWeaponId() : player.getEquipment().getShieldId();
        final int attackStyle = -1;
        // int combatDelay = getRangeCombatDelay(weaponId, attackStyle);
        // Projectiles ID's now stored in the cache.

        ItemDefinitions defs = player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD).getDefinitions();
        int projectileGfx = defs.getCSOpcode(2940);
        if (projectileGfx == 0) { // uses ammo
            if (player.getEquipment().getItem(Equipment.SLOT_ARROWS) != null) {
                ItemDefinitions ammoDefs = player.getEquipment().getItem(Equipment.SLOT_ARROWS).getDefinitions();
                projectileGfx = ammoDefs.getCSOpcode(2940);
            }
        }
        if (mainHand && player.getCombatDefinitions().isUsingSpecialAttack()) {
            int specAmt = getSpecialCost(weaponId);
            if (specAmt == 0) {
                player.getPackets().sendGameMessage("This weapon has no special attack; report to an administrator.");
                player.getCombatDefinitions().decreaseSpecialAttack(0);
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
            if (player.getCombatDefinitions().hasRingOfVigour())
                specAmt *= 0.9;
            if (player.getCombatDefinitions().getSpecialAttackPercentage() < specAmt) {
                player.getPackets().sendGameMessage("You don't have enough power left.");
                player.getCombatDefinitions().decreaseSpecialAttack(0);
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
            Item wep = weaponId == -1 ? null : mainHand ? player.getEquipment().getItem(Equipment.SLOT_WEAPON) : player.getEquipment().getItem(Equipment.SLOT_SHIELD);
            int realItemId = wep.getInventionData() != null ? wep.getInventionData().getOriginalItemId() : wep.getChargesData() != null ? wep.getChargesData().getOrignalId() : weaponId;
            player.getCombatDefinitions().decreaseSpecialAttack(specAmt);
            switch (realItemId) {
            case 19149:// zamorak bow
            case 19151:
                player.setNextAnimation(new Animation(426));
                player.setNextGraphics(new Graphics(97));
                World.sendProjectile(player, target, 100, 41, 16, 25, 35, 16, 0);
                delayHit(1, weaponId, getHit(player, target, mainHand));
                removeAmmo(player, 1, 1);
                break;
            case 19146:
            case 19148:// guthix bow
                player.setNextAnimation(new Animation(426));
                player.setNextGraphics(new Graphics(95));
                World.sendProjectile(player, target, 98, 41, 16, 25, 35, 16, 0);
                delayHit(1, weaponId, getHit(player, target, mainHand));
                removeAmmo(player, 1, 1);
                break;
            case 19143:// saradomin bow
            case 19145:
                player.setNextAnimation(new Animation(426));
                player.setNextGraphics(new Graphics(96));
                World.sendProjectile(player, target, 99, 41, 16, 25, 35, 16, 0);
                delayHit(1, weaponId, getHit(player, target, mainHand));
                removeAmmo(player, 1, 1);
                break;
            case 859: // magic longbow
            case 861: // magic shortbow
            case 10284: // Magic composite bow
            case 18332: // Magic longbow (sighted)
                player.setNextAnimation(new Animation(1074));
                World.sendProjectile(player, target, 249, 41, 16, 31, 35, 18, 0);
                World.sendProjectile(player, target, 249, 41, 16, 25, 35, 21, 0);
                delayHit(2, weaponId, getHit(player, target, 1.05, 25.0, mainHand, false));
                removeAmmo(player, 1, 2);
                delayHit(3, weaponId, getHit(player, target, 1.0, 25.0, mainHand, false));
                removeAmmo(player, 1, 3);
                break;
            case 15241: // Hand cannon
                WorldTasksManager.schedule(new WorldTask() {
                    int loop = 0;

                    @Override
                    public void run() {
                        if ((target.isDead() || player.isDead() || loop > 1) && !World.getNPCs().contains(target)) {
                            stop();
                            return;
                        }
                        if (loop == 0) {
                            player.setNextAnimation(new Animation(12174));
                            player.setNextGraphics(new Graphics(2138));
                            World.sendProjectile(player, target, 2143, 18, 18, 50, 50, 0, 0);
                            delayHit(1, weaponId, getHit(player, target, 1.5, mainHand, false));
                        } else if (loop == 1) {
                            player.setNextAnimation(new Animation(12174));
                            player.setNextGraphics(new Graphics(2138));
                            World.sendProjectile(player, target, 2143, 18, 18, 50, 50, 0, 0);
                            delayHit(1, weaponId, getHit(player, target, 1.25, mainHand, false));
                            stop();
                        }
                        loop++;
                    }
                }, 0, (int) 0.25);
                break;
            case 11235:
            case 13405:
            case 15701:
            case 15702:
            case 15703:
            case 15704:
            case 34158:
                int ammoId = player.getEquipment().getAmmoId();
                player.setNextAnimation(new Animation(getAttackAnimation(player, mainHand)));
                player.setNextGraphics(new Graphics(projectileGfx, 0, 100));
                if (ammoId == 11212) {
                    int damage = getHit(player, target, 1.5, mainHand, false).getDamage();
                    if (damage < 80)
                        damage = 80;
                    int damage2 = getHit(player, target, 1.5, mainHand, false).getDamage();
                    if (damage2 < 80)
                        damage2 = 80;
                    World.sendProjectile(player, target, 1099, 41, 16, 31, 35, 16, 0);
                    World.sendProjectile(player, target, 1099, 41, 16, 25, 35, 21, 0);
                    delayHit(2, weaponId, getRangeHit(player, damage));
                    delayHit(3, weaponId, getRangeHit(player, damage2));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(1100, 0, 100));
                        }
                    }, 2);
                } else {
                    int damage = getHit(player, target, 1.3, mainHand, false).getDamage();
                    if (damage < 50)
                        damage = 50;
                    int damage2 = getHit(player, target, 1.3, mainHand, false).getDamage();
                    if (damage2 < 50)
                        damage2 = 50;
                    World.sendProjectile(player, target, 1101, 41, 16, 31, 35, 16, 0);
                    World.sendProjectile(player, target, 1101, 41, 16, 25, 35, 21, 0);
                    delayHit(2, weaponId, getRangeHit(player, damage));
                    delayHit(3, weaponId, getRangeHit(player, damage2));
                }
                removeAmmo(player, 2, 2);
                break;

            case 14684: // zanik cbow
                player.setNextAnimationForce(new Animation(getAttackAnimation(player, mainHand)));
                player.setNextGraphics(new Graphics(1714));
                World.sendProjectile(player, target, 2001, 41, 41, 41, 35, 0, 0);
                delayHit(2, weaponId, getRangeHit(player, getHit(player, target, mainHand).getDamage() + 30 + Utils.getRandom(120)));
                removeAmmo(player, 1, 2);
                break;
            case 13954:// morrigan javelin
            case 12955:
            case 13956:
            case 13879:
            case 13880:
            case 13881:
            case 13882:
                player.setNextGraphics(new Graphics(1836));
                player.setNextAnimation(new Animation(10501));
                World.sendProjectile(player, target, 1837, 41, 41, 41, 35, 0, 0);
                final int hit = getHit(player, target, mainHand).getDamage();
                delayHit(2, weaponId, getRangeHit(player, hit));
                if (hit > 0) {
                    final Entity finalTarget = target;
                    WorldTasksManager.schedule(new WorldTask() {
                        int damage = hit;

                        @Override
                        public void run() {
                            if (finalTarget.isDead() || finalTarget.hasFinished()) {
                                stop();
                                return;
                            }
                            if (damage > 50) {
                                damage -= 50;
                                finalTarget.applyHit(new Hit(player, 50, HitLook.REGULAR_DAMAGE));
                            } else {
                                finalTarget.applyHit(new Hit(player, damage, HitLook.REGULAR_DAMAGE));
                                stop();
                            }
                        }
                    }, 4, 2);
                }
                removeAmmo(player, -1, 2);
                break;

            /*
             * case 31733: // noxious case 31735: case 31736: case 33336: case 33337: case
             * 33402: case 33403: case 33468: case 33469: case 36339: case 36340:
             */
            case 37632: // seren godbows
            case 37634:
            case 40710:
            case 40711:
            case 40713:
            case 40714:
            case 40716:
            case 40717:
            case 40719:
            case 40720:
            case 42070:
            case 42071:
            case 42192:
            case 42193:
                player.setNextAnimation(new Animation(18503));
                player.setNextGraphics(new Graphics(3524));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (player == null || player.isDead() || player.hasFinished())
                            return;
                        WorldTile center = ((target.getSize() >= 3 && ThreadLocalRandom.current().nextBoolean()) || target.getSize() <= 2) ? target.getMiddleWorldTile() : new WorldTile(target.getX(), target.getY(), target.getPlane());
                        List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
                        for (int x = center.getX() - 2; x <= center.getX() + 2; x++) {
                            for (int y = center.getY() - 2; y <= center.getY() + 2; y++) {
                                WorldTile checkTile = new WorldTile(x, y, center.getPlane());
                                if (center.matches(checkTile))
                                    continue;
                                possibleTiles.add(checkTile);
                            }
                        }
                        Collections.shuffle(possibleTiles);
                        possibleTiles.set(0, center);
                        WorldTile[] tiles = new WorldTile[5];
                        for (int i = 0; i < tiles.length; i++)
                            tiles[i] = possibleTiles.get(i);
                        for (WorldTile tile : tiles)
                            World.sendGraphics(player, new Graphics(6287), tile);
                        Entity[] targets = getTargetsInTiles(player, target, tiles);
                        if (targets.length > 0) {
                            final Entity realTarget = target;
                            Hit[] hits = new Hit[targets.length];
                            for (int i = 0; i < hits.length; i++) {
                                target = targets[i];
                                double lower = 0.8;
                                double upper = 2.4001 - (0.27 * (i - 1));
                                if (lower > upper)
                                    upper = 0.10;
                                hits[i] = i > 0 ? (hits[0].getDamage() <= 0 ? hits[0] : getHit(player, targets[i], ThreadLocalRandom.current().nextDouble(lower, upper), true, false)) : getHit(player, targets[i], ThreadLocalRandom.current().nextDouble(0.8, 2.001), true, false);
                                delayHit(1, weaponId, hits[i]);
                            }
                            target = realTarget;
                        }
                    }
                }, 1);
                break;
            case 13883:
            case 13957:// morigan thrown axe
                player.setNextGraphics(new Graphics(1838));
                player.setNextAnimation(new Animation(10504));
                World.sendProjectile(player, target, 1839, 41, 41, 41, 35, 0, 0);
                delayHit(2, weaponId, getHit(player, target, mainHand));
                removeAmmo(player, -1, 2);
                break;
            case 32231:// crystal chakrams
            case 32232:
            case 32233:
            case 32655:
            case 32656:
                player.setNextAnimation(new Animation(18238));
                World.sendProjectile(player, target, 5146, 41, 36, 50, 0, 15, 0);
                delayHit(2, weaponId, getHit(player, target, mainHand));
                break;
            default:
                player.getPackets().sendGameMessage("This weapon has no special Attack, if you still see special bar please relogin.");
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
        } else {
            Animation attackAnim = new Animation(getAttackAnimation(player, mainHand));
            int combatStyle = player.getCombatDefinitions().getStyle(!mainHand);
            int projectileDelay;
            player.setNextAnimation(attackAnim);
            switch (combatStyle) {
            case Combat.ARROW_STYLE:
                projectileDelay = 40;
                // projectileDelay -= 15;
                if (Combat.hasDarkbow(player)) { // darkbow exeption
                    int slope = Utils.random(5);
                    Projectile projectile = World.sendProjectileCycles(player, target, projectileGfx, 40, 41, projectileDelay, projectileDelay + 10, slope, 5);
                    int delayhit = (int) (Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime()) / 600);
                    projectile = World.sendProjectileCycles(player, target, projectileGfx, 40, 41, projectileDelay, projectileDelay + 10, slope + 5, 5);
                    Hit hit = getHit(player, target, mainHand);
                    checkSwiftGlovesEffect(player, delayhit, attackStyle, weaponId, hit.getDamage(), projectileGfx, 38, 36, 41, 50, 10, 100);
                    delayHit(delayhit, weaponId, hit);
                    delayHit(delayhit, weaponId, getHit(player, target, mainHand));
                    removeAmmo(player, 2, delayhit);
                } else if (player.hasDecimationEffect()) {
                    Entity[] targets = getIsOnRangeMultiAttackTargets(player, target, 0);
                    Entity realTarget = target;
                    for (Entity e : targets) {
                        target = e;
                        Projectile projectile = World.sendProjectileCycles(player, e, projectileGfx, 40, 41, projectileDelay, projectileDelay + 10, Utils.random(5), 5);
                        int delayhit = (int) (Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime()) / 600);
                        Hit hit = getHit(player, e, mainHand);
                        checkSwiftGlovesEffect(player, delayhit, attackStyle, weaponId, hit.getDamage(), projectileGfx, 38, 36, 41, 50, 10, 100);
                        delayHit(delayhit, weaponId, hit);
                    }
                    target = realTarget;
                    if (defs.getCSOpcode(2940) == 0) // crystal bow doesnt use ammo
                        // for instance
                        removeAmmo(player, 1, defs.getName().contains("bow") ? 2 : -1);
                } else {
                    Projectile projectile = World.sendProjectileCycles(player, target, projectileGfx, 40, 41, projectileDelay, projectileDelay + 10, Utils.random(5), 5);
                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime());
                    int delayhit = (int) (projectileCycles / 600);
                    Hit hit = getHit(player, target, mainHand);
                    checkSwiftGlovesEffect(player, delayhit, attackStyle, weaponId, hit.getDamage(), projectileGfx, 38, 36, 41, 50, 10, 100);

                    delayHit(delayhit, weaponId, hit);
                    if (defs.getCSOpcode(2940) == 0) // crystal bow doesnt use ammo
                        // for instance
                        removeAmmo(player, 1, defs.getName().contains("bow") ? delayhit : -1);
                }
                break;
            case Combat.BOLT_STYLE: {
                projectileDelay = defs.getEquipType() == 5 ? 30 : 20;
                if (defs.getName().toLowerCase().contains("hand cannon") && !defs.getName().toLowerCase().contains("augmented") && Utils.random(player.getSkills().getLevel(Skills.FIREMAKING) << 1) == 0) {// handcannon
                    // exeption
                    player.setNextAnimation(new Animation(12175));
                    player.setNextGraphics(new Graphics(2140));
                    player.getEquipment().getItems().set(3, null);
                    player.getEquipment().refresh(3);
                    player.getAppearence().generateAppearenceData();
                    player.applyHit(new Hit(player, Utils.random(100) + 10, HitLook.REGULAR_DAMAGE));
                    setWeaponDelay(player, mainHand);
                    return delay;
                }
                // projectileDelay -= 20;
                Projectile projectile = World.sendProjectileCycles(player, target, projectileGfx, 40, 41, projectileDelay, projectileDelay + 7, Utils.random(5), 5);
                long projectileCycles = Utils.projectileTimeToMiliseconds1(projectile.getStartTime() + projectile.getEndTime() + 10);
                final int ammoId = player.getEquipment().getAmmoId();
                boolean enchancedSpec = ammoId != -1 && Utils.getRandom(10) == 5;
                int delayhit = (int) (projectileCycles / 600);
                Hit hit = getHit(player, target, mainHand);
                if (enchancedSpec)
                    switch (ammoId) {
                    case 9242:
                        int damage = (int) (target.getHitpoints() * 0.2);
                        if (damage > 1250) {
                            damage = 1250;// cap
                        }
                        hit.setDamage(damage);
                        break;
                    case 9243:
                        hit = getHit(player, target, 1.15, mainHand, false);
                        break;
                    case 9244:
                        hit = getHit(player, target, !Combat.hasAntiDragProtection(target) ? 1.45 : 1.0, mainHand, false);
                        break;
                    case 9245:
                        hit = getHit(player, target, 1.15, mainHand, false);
                        break;
                    }
                delayHit(delayhit, weaponId, hit);
                checkSwiftGlovesEffect(player, delayhit, attackStyle, weaponId, hit.getDamage(), projectileGfx, 38, 36, 41, 32, 5, 0);
                if (defs.getCSOpcode(2940) == 0)
                    removeAmmo(player, 1, (defs.getName().contains("crossbow") || defs.getName().contains("repriser")) ? delayhit : -1);
                if (enchancedSpec)
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            if (target.isDead() || target.hasFinished())
                                return;
                            switch (ammoId) {
                            case 9237:
                                target.setNextGraphics(new Graphics(755));
                                if (target instanceof Player) {
                                    final Player p2 = (Player) target;
                                    p2.stopAll();
                                } else {
                                    final NPC n = (NPC) target;
                                    n.setTarget(null);
                                }
                                break;
                            case 9242:
                                target.setNextGraphics(new Graphics(754));
                                player.applyHit(new Hit(target, player.getHitpoints() > 20 ? (int) (player.getHitpoints() * 0.1) : 1, HitLook.REFLECTED_DAMAGE));
                                break;
                            case 9243:
                                target.setNextGraphics(new Graphics(751));
                                break;
                            case 9244:
                                target.setNextGraphics(new Graphics(756));
                                break;
                            case 9245:
                                target.setNextGraphics(new Graphics(753));
                                player.heal((int) (player.getMaxHitpoints() * 0.25));
                                break;
                            }
                        }

                    }, delayhit);
                break;
            }
            case Combat.THROWN_STYLE:
                projectileDelay = 35;

                Projectile projectile = World.sendProjectileCycles(player, target, projectileGfx, 40, 41, 23, 30, Utils.random(5), 5);
                long projectileCycles = Utils.projectileTimeToMiliseconds1(projectile.getStartTime() + projectile.getEndTime() + 10);
                int delayhit = (int) (projectileCycles / 600);
                boolean usingChinchompa = defs.id == 10033 || defs.id == 10034;
                if (usingChinchompa) { // Chinchompa exeption
                    for (Entity target :

                    getMultiAttackTargets(player))
                        delayHit(Utils.projectileTimeToCycles(delayhit), weaponId, getHit(player, target, mainHand));
                } else

                    delayHit(Utils.projectileTimeToCycles(delayhit), weaponId, getHit(player, target, mainHand));



                boolean deleteAmmo = defs.id == 732 || usingChinchompa;

                if (!deleteAmmo) {
                    if (mainHand) {
                        player.getEquipment().removeAmmo(player.getEquipment().getWeaponId(), 1, true); // mainhand throw
                    } else {
                        player.getEquipment().removeAmmo(player.getEquipment().getShieldId(), 1, false); // offhand throw
                    }
                }




                if (mainHand && ItemDefinitions.getItemDefinitions(weaponId).getName().equalsIgnoreCase("mud pie") && target instanceof Player) {
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            if (target instanceof Player && ((Player) target).getRunEnergy() >= 1) {
                                ((Player) target).setRunEnergy(((Player) target).getRunEnergy() - 50 <= 0 ? 0 : ((Player) target).getRunEnergy() - 50);
                                player.getPackets().sendGameMessage("Your hit drained 50 run energy points from your target.");
                                ((Player) target).getPackets().sendGameMessage("Your run energy has been drained by 50 points");
                            }
                        }
                    }, Utils.projectileTimeToCycles(delayhit));
                }
            default:
                break;
            }

        }
        setWeaponDelay(player, mainHand);
        return delay;
    }

    private int meleeAttack(final Player player, boolean mainHand) {
        int weaponId = mainHand ? player.getEquipment().getWeaponId() : player.getEquipment().getShieldId();
        if (weaponId == -1 && !player.getEquipment().hasOffHand()) {
            Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
            if (gloves != null && gloves.getDefinitions().getName().contains("Goliath gloves"))
                weaponId = -2;
        }
        int delay = !Settings.DUAL_COMBAT ? getAttackSpeed(player, mainHand) : 0;
        if (mainHand && player.getCombatDefinitions().isUsingSpecialAttack()) {
            if (!specialExecute(player)) {
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
            if (getSpecialCost(weaponId) >= 35 && target instanceof CommanderZilyana) {
                ((CommanderZilyana) target).dealFinalBlow();
            }
            Item wep = weaponId == -1 ? null : mainHand ? player.getEquipment().getItem(Equipment.SLOT_WEAPON) : player.getEquipment().getItem(Equipment.SLOT_SHIELD);
            int realItemId = wep.getInventionData() != null ? wep.getInventionData().getOriginalItemId() : wep.getChargesData() != null ? wep.getChargesData().getOrignalId() : weaponId;
            switch (realItemId) {
            case 15442:// whip start
            case 15443:
            case 15444:
            case 15441:
            case 4151:
            case 21371:
            case 21372:
            case 21373:
            case 21374:
            case 21375:
            case 23691:
            case 34150:
                player.setNextAnimationForce(new Animation(11971));
                target.setNextGraphics(new Graphics(2108, 0, 100));
                if (target instanceof Player) {
                    final Player p2 = (Player) target;
                    p2.setRunEnergy(p2.getRunEnergy() > 25 ? p2.getRunEnergy() - 25 : 0);
                }
                delayNormalHit(realItemId, getHit(player, target, 1.2, true, false));
                break;
            case 11061:
                player.setNextAnimationForce(new Animation(10505));
                player.setNextGraphics(new Graphics(1052));
                if (target instanceof Player) {
                    final Player p2 = (Player) target;
                    final int hp = (int) (p2.getPrayer().getPrayerpoints() / 0.75);
                    p2.getPrayer().drainPrayer(hp);
                }
                delayNormalHit(realItemId, getHit(player, target, 1.2, true, false));
                break;
            case 11730: // sara sword
            case 23690:
                player.setNextAnimationForce(new Animation(11993));
                target.setNextGraphics(new Graphics(1194));
                Hit secondHit = getHit(player, target, 1.3, true, false);
                secondHit.setLook(HitLook.MAGIC_DAMAGE);
                delayNormalHit(realItemId, getHit(player, target, 1.1, true, false), secondHit);
                break;
            case 1249:// d spear
            case 1263:
            case 3176:
            case 5716:
            case 5730:
            case 13770:
            case 13772:
            case 13774:
            case 13776:
                player.setNextAnimationForce(new Animation(12017));
                player.stopAll();
                target.setNextGraphics(new Graphics(80, 5, 60));

                if (!target.addWalkSteps(target.getX() - player.getX() + target.getX(), target.getY() - player.getY() + target.getY(), 1)) {
                    player.setNextFaceEntity(target);
                }
                target.setNextFaceEntity(player);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        target.setNextFaceEntity(null);
                        player.setNextFaceEntity(null);
                    }
                });
                if (target instanceof Player) {
                    final Player other = (Player) target;
                    other.lock();
                    other.addFoodDelay(3000);
                    other.setDisableEquip(true);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            other.setDisableEquip(false);
                            other.unlock();
                        }
                    }, 5);
                } else {
                    final NPC n = (NPC) target;
                    n.setFreezeDelay(3000);
                    n.resetCombat();
                }
                break;
            case 11698: // sgs
            case 23681:
            case 32018:
                player.setNextAnimationForce(new Animation(23933));
                player.setNextGraphics(new Graphics(2109));
                final int sgsdamage = getHit(player, target, 1.1, 10.0, true, false).getDamage();
                player.heal(sgsdamage / 2);
                player.getPrayer().restorePrayer((sgsdamage / 4) * 10);
                delayNormalHit(realItemId, getMeleeHit(player, sgsdamage));
                break;
            case 6739: // d hatchet
                player.setNextAnimationForce(new Animation(2876));
                player.setNextGraphics(new Graphics(479));
                final int daxe = getHit(player, target, 1.1, true, false).getDamage();
                delayNormalHit(realItemId, getMeleeHit(player, daxe));
                if (daxe > 0)
                    target.getTemporaryModifiersManager().applyModifier(Key.AFFINITY_MODIFIER, 60000, 3);
                break;
            case 7158: // d2h
                player.setNextAnimationForce(new Animation(2876));
                player.setNextGraphics(new Graphics(479));
                delayNormalHit(realItemId, getHit(player, target, 1.1, true, false));
                break;
            case 11696: // bgs
            case 23680:
            case 32016:
                player.setNextAnimationForce(new Animation(11991));
                player.setNextGraphics(new Graphics(2114));
                final int damage = getHit(player, target, 1.2, 10.0, true, false).getDamage();
                delayNormalHit(realItemId, getMeleeHit(player, damage));
                if (target instanceof Player) {
                    final Player targetPlayer = ((Player) target);
                    int amountLeft;
                    if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.DEFENCE, damage / 10)) > 0) {
                        if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.STRENGTH, amountLeft)) > 0) {
                            if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.PRAYER, amountLeft)) > 0) {
                                if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.ATTACK, amountLeft)) > 0) {
                                    if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.MAGIC, amountLeft)) > 0) {
                                        if (targetPlayer.getSkills().drainLevel(Skills.RANGE, amountLeft) > 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (target instanceof NPC) {
                    NPC t = (NPC) target;
                    t.getStats().setDefenceLevel((int) Math.max(1, t.getStats().getDefenceLevel() - (t.getStats().getDefenceLevel() * ThreadLocalRandom.current().nextDouble(0.05, 0.11))));
                }
                break;
            case 11694: // ags
            case 23679:
            case 32014:
                player.setNextAnimationForce(new Animation(11989));
                player.setNextGraphics(new Graphics(2113));
                delayNormalHit(realItemId, getHit(player, target, 1.5, 25.0, true, false));
                break;
            case 13899: // vls
            case 13901:
            case 39121:
            case 39122:
                player.setNextAnimationForce(new Animation(10502));
                delayNormalHit(realItemId, getHit(player, target, 1.2, 25.0, true, false));
                break;
            case 13902: // statius hammer
            case 13904:
            case 39117:
            case 39118:
                player.setNextAnimationForce(new Animation(10505));
                player.setNextGraphics(new Graphics(1840));
                final int dmg = getHit(player, target, 1.25, 25.0, true, false).getDamage();
                delayNormalHit(realItemId, getMeleeHit(player, dmg));
                if (dmg > 0) {
                    target.getTemporaryModifiersManager().applyModifier(Key.AFFINITY_MODIFIER, 60000, 5);
                    if (target instanceof Player) {
                        final double modifier = (player.getSkills().getLevel(Skills.DEFENCE) * 0.7) + 1;
                        player.getSkills().drainLevel(Skills.DEFENCE, (int) modifier);
                        player.getSkills().refresh(Skills.DEFENCE);
                    } else if (target instanceof NPC) {
                        NPC npc = (NPC) target;
                        npc.getStats().setDefenceLevel((int) Math.max(1, npc.getStats().getDefenceLevel() - (npc.getStats().getDefenceLevel() * 0.3)));
                    }
                }
                break;
            case 13905: // vesta spear
            case 13907:
            case 39125:
            case 39126:
                player.setNextAnimationForce(new Animation(10499));
                player.setNextGraphics(new Graphics(1835));
                player.activateSpearWallEffect();
                final Entity[] targets = getMultiAttackTargetGrid(player, 3);
                final int wepId = realItemId;
                attackTarget(targets, new MultiAttack() {

                    @Override
                    public boolean attack() {
                        delayNormalHit(wepId, getHit(player, target, 1.1, 25.0, true, false));
                        return true;
                    }
                });
                break;

            /*
             * case 31729: // start nox staff case 31731: case 33399: case 33400: case
             * 33465: case 33466: case 33333: case 33334: case 36336: case 36337:
             * player.setNextAnimation(new Animation(18540)); delayHit(1, weaponId,
             * attackStyle, new Hit(player, Utils.random(200, 500), HitLook.MAGIC_DAMAGE));
             * delayHit(1, weaponId, new Hit(player, Utils.random(100, 200),
             * HitLook.DESEASE_DAMAGE)); break;
             */
            case 34155:
                if (Utils.random(10) >= 5) {
                    player.setNextAnimation(new Animation(18426));
                    player.setNextAnimationNoPriority(new Animation(18238));
                    // player.setNextAnimation(new Animation(18238));
                    delayHit(2, realItemId, getMagicHit(player, getHit(player, target, 1.5, true, false).getDamage()));
                    // target.setNextGraphics(new Graphics(3525));
                    World.sendProjectile(player, target, 3551, 41, 16, 15, 35, 24, 0);
                    target.applyHit(new Hit(player, Utils.random(Utils.random(0 - 250), Utils.random(250 - 500)), HitLook.REGULAR_DAMAGE));

                } else {
                    player.setNextAnimation(new Animation(18205));
                    delayHit(2, realItemId, getMagicHit(player, getHit(player, target, 1.5, true, false).getDamage()));
                    // target.setNextGraphics(new Graphics(3525));
                    World.sendProjectile(player, target, 3551, 41, 16, 15, 35, 24, 0);
                    target.applyHit(new Hit(player, Utils.random(Utils.random(0 - 250), Utils.random(250 - 500)), HitLook.REGULAR_DAMAGE));
                }
                break;

            case 19784: // korasi sword
            case 18786:
                player.setNextAnimationForce(new Animation(14788));
                player.setNextGraphics(new Graphics(1729));
                Hit magicHit = getHit(player, target, ThreadLocalRandom.current().nextDouble(1.33, 2.1), 50.0, true, false);
                magicHit.setLook(HitLook.MAGIC_DAMAGE);
                delayNormalHit(realItemId, magicHit);
                break;
            case 11700:
            case 23682:
            case 32020:
                final int zgsdamage = getHit(player, target, 1, true, false).getDamage();
                player.setNextAnimationForce(new Animation(7070));
                player.setNextGraphics(new Graphics(1221));
                if (zgsdamage != 0 && target.getSize() <= 1) { // freezes small
                    // npcs
                    target.setNextGraphics(new Graphics(2104));
                    target.addFreezeDelay(18000); // 18seconds
                }
                delayNormalHit(realItemId, getMeleeHit(player, zgsdamage));
                break;
            case 23695:// lucky dclaw
                player.setNextAnimationForce(new Animation(24010));
                player.setNextGraphics(new Graphics(1950));
                target.setNextGraphics(new Graphics(3528));
                int[] cya1 = new int[] { 0, 1 };
                int cya2 = getMaxHit(player, target, mainHand).getDamage();
                if (cya2 > 0)
                    cya1 = new int[] { cya2, cya2 / 2, (cya2 / 2) / 2, (cya2 / 2) - ((cya2 / 2) / 2) };
                else {
                    cya2 = getMaxHit(player, target, mainHand).getDamage();
                    if (cya2 > 0)
                        cya1 = new int[] { 0, cya2, cya2 / 2, cya2 - (cya2 / 2) };
                    else {
                        cya2 = getMaxHit(player, target, mainHand).getDamage();
                        if (cya2 > 0)
                            cya1 = new int[] { 0, 0, cya2 / 2, (cya2 / 2) + 10 };
                        else {
                            cya2 = getMaxHit(player, target, mainHand).getDamage();
                            if (cya2 > 0)
                                cya1 = new int[] { 0, 0, 0, (int) (cya2 * 1.5) };
                            else
                                cya1 = new int[] { 0, 0, 0, Utils.getRandom(7) };
                        }
                    }
                }
                for (int i = 0; i < cya1.length; i++) {
                    Hit hit = new Hit(player, cya1[i], HitLook.MELEE_DAMAGE);
                    if (i > 1)
                        delayHit(1, realItemId, hit);
                    else
                        delayNormalHit(realItemId, hit);
                }
                break;
            case 14484: // d claws
                player.setNextAnimationForce(new Animation(24010));
                player.setNextGraphics(new Graphics(1950));
                target.setNextGraphics(new Graphics(3527));
                int[] hits = new int[] { 0, 1 };
                int hit = getMaxHit(player, target, mainHand).getDamage();
                if (hit > 0)
                    hits = new int[] { hit, hit / 2, (hit / 2) / 2, (hit / 2) - ((hit / 2) / 2) };
                else {
                    hit = getMaxHit(player, target, mainHand).getDamage();
                    if (hit > 0)
                        hits = new int[] { 0, hit, hit / 2, hit - (hit / 2) };
                    else {
                        hit = getMaxHit(player, target, mainHand).getDamage();
                        if (hit > 0)
                            hits = new int[] { 0, 0, hit / 2, (hit / 2) + 10 };
                        else {
                            hit = getMaxHit(player, target, mainHand).getDamage();
                            if (hit > 0)
                                hits = new int[] { 0, 0, 0, (int) (hit * 1.5) };
                            else
                                hits = new int[] { 0, 0, 0, Utils.getRandom(7) };
                        }
                    }
                }
                for (int i = 0; i < hits.length; i++) {
                    Hit meleehit = new Hit(player, hits[i], HitLook.MELEE_DAMAGE);
                    if (i > 1)
                        delayHit(1, realItemId, meleehit);
                    else
                        delayNormalHit(realItemId, meleehit);
                }
                break;
            case 10887: // anchor
                player.setNextAnimationForce(new Animation(5870));
                player.setNextGraphics(new Graphics(1027));
                delayNormalHit(realItemId, getHit(player, target, 1.1, 50.0, true, false));
                break;
            case 24694: // aurora sword
            case 1305: // dragon long
                player.setNextAnimationForce(new Animation(12033));
                player.setNextGraphics(new Graphics(2117, 0, 75));
                delayNormalHit(realItemId, getHit(player, target, 1.25, 25.0, true, false));
                break;
            case 3204: // d hally
                player.setNextAnimationForce(new Animation(1665));
                player.setNextGraphics(new Graphics(282));
                if (target.getSize() < 3) {
                    target.setNextGraphics(new Graphics(254));
                }
                delayNormalHit(realItemId, getHit(player, target, 1.1, true, false));
                if (target.getSize() < 3) {
                    delayHit(1, realItemId, getHit(player, target, 1.1, true, false));
                }
                break;

            case 4587: // dragon sci
            case 24882:
                player.setNextAnimationForce(new Animation(12031));
                player.setNextGraphics(new Graphics(2118));
                final Hit hit1 = getHit(player, target, 1.00, true, false);
                if (target instanceof Player) {
                    final Player p2 = (Player) target;
                    if (hit1.getDamage() > 0) {
                        p2.setPrayerDelay(5000);// 5 seconds
                    }
                }
                delayNormalHit(realItemId, hit1);
                break;
            case 15259: // dragon pickaxe
                player.setNextAnimationForce(new Animation(12031));
                player.setNextGraphics(new Graphics(2144));
                delayNormalHit(realItemId, getHit(player, target, 1.1, true, false));
                break;
            case 1215: // dragon dagger
            case 5698: // dds
            case 1231:
            case 5680:
                player.setNextAnimationForce(new Animation(1062));
                player.setNextGraphics(new Graphics(252, 0, 100));
                delayNormalHit(realItemId, getHit(player, target, 1.15, 25.0, true, false), getHit(player, target, 1.15, 25.0, true, false));
                break;
            case 1434: // dragon mace
                player.setNextAnimationForce(new Animation(1060));
                player.setNextGraphics(new Graphics(251));
                delayNormalHit(realItemId, getHit(player, target, 1.45, 25.0, true, false));
                break;
            default:
                player.sendMessage("This weapon has no special Attack.");
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
            setWeaponDelay(player, true);
            setWeaponDelay(player, false);
            return delay;
        } else {
            if (weaponId == -2) {
                player.setNextAnimationForce(new Animation(14417));
                delayNormalHit(weaponId, getHit(player, target, 1.00 + (Math.random() <= 0.05 ? 0.25 : 0), true, false));
                setWeaponDelay(player, true);
                setWeaponDelay(player, false);
                return delay;
            }
            player.setNextAnimationForce(new Animation(getAttackAnimation(player, mainHand)));
            if (PlayerCombat.hasConeAttack(weaponId)) {
                final Entity[] targets = PlayerCombat.getConeMultiAttackTargets(player, target, 7);
                if (targets.length <= 1 || player.hasWalkSteps()) {
                    delayNormalHit(weaponId, getHit(player, target, mainHand));
                    setWeaponDelay(player, mainHand);
                    return delay;
                } else {
                    int wepId = weaponId;
                    attackTarget(targets, new MultiAttack() {

                        @Override
                        public boolean attack() {
                            delayNormalHit(wepId, getHit(player, target, 0.75, mainHand, false));
                            return true;
                        }

                    });
                }
            } else
                delayNormalHit(weaponId, getHit(player, target, mainHand));
            setWeaponDelay(player, mainHand);
            return delay;
        }
    }

    public static int getAttackAnimation(Player player, boolean mainHand) {
        Item item = getWeapon(player, mainHand);
        boolean legacy = false;
        int weaponType = player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
        if (weaponType == Combat.MAGIC_TYPE) {
            int spellId = player.getCombatDefinitions().getSpellId(!mainHand);
            if (spellId <= 0)
                return 15071;
            boolean manualCast = spellId >= 1000;
            return Magic.getSpellData(manualCast ? spellId - 1000 : spellId).getIntValue(item.getDefinitions().getEquipType() == 5 ? 2919 : 2914);
        }
        if (item == null)
            return mainHand ? (legacy ? 422 : 18224) : -1;
        return item.getName().toLowerCase().contains("skillchompa") ? 422 : item.getDefinitions().getCombatOpcode(mainHand ? (2914) : (2831));
    }

    public static void setWeaponAbilityDelay(Player player) {

        setWeaponDelay(player, true);
        setWeaponDelay(player, false);
        // delay furhter when using ability
        player.getCombatDefinitions().setMainHandDelay(player.getCombatDefinitions().getMainHandDelay() + 3);
        player.getCombatDefinitions().setOffHandDelay(player.getCombatDefinitions().getOffHandDelay() + 3);
    }

    public static void setWeaponDelay(Player player, boolean mainHand) {
        if (!Settings.DUAL_COMBAT)
            return;
        Item item = getWeapon(player, mainHand);
        boolean twohand = item != null && item.getDefinitions().getEquipType() == 5;
        long delay = getAttackSpeed(player, mainHand);
        long nextHitMin = 2;
        if (player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON) != player.getCombatDefinitions().getType(Equipment.SLOT_SHIELD) && player.getEquipment().hasOffHand() && !player.getEquipment().hasShield())
            nextHitMin += 1;
        long worldCycle = Utils.currentWorldCycle();
        delay += worldCycle;
        nextHitMin += worldCycle;
        if (mainHand) {
            if (player.getCombatDefinitions().getMainHandDelay() < delay)
                player.getCombatDefinitions().setMainHandDelay(delay);
            if (player.getCombatDefinitions().getOffHandDelay() < nextHitMin)
                player.getCombatDefinitions().setOffHandDelay(nextHitMin);
        }
        if (!mainHand || twohand) {
            if (twohand)
                delay += 1;
            if (player.getCombatDefinitions().getMainHandDelay() < delay)
                player.getCombatDefinitions().setOffHandDelay(delay);
            if (player.getCombatDefinitions().getMainHandDelay() < nextHitMin)
                player.getCombatDefinitions().setMainHandDelay(nextHitMin);
        }
    }

    private boolean checkAmmo(Player player, boolean mainHand) {
        Item wep = player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        int realItemId = wep.getInventionData() != null ? wep.getInventionData().getOriginalItemId() : wep.getChargesData() != null ? wep.getChargesData().getOrignalId() : wep.getId();
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(realItemId);
        if (defs.getCSOpcode(2940) != 0 || defs.id == 25202) // no need for ammo
            return true;
        Item ammo = player.getEquipment().getItem(Equipment.SLOT_ARROWS);
        if (ammo == null) {
            player.getPackets().sendPlayerMessage(1, 0xFFFFFF, "You have no ammo equipped.", true);
            return false;
        }
        int attackStyle = player.getCombatDefinitions().getStyle(!mainHand);
        boolean result;
        boolean onlyBoltRack = defs.getName().toLowerCase().contains("karil's pistol crossbow") || defs.getName().toLowerCase().contains("karil's crossbow") || defs.getName().toLowerCase().contains("karil's off-hand pistol crossbow") || defs.getName().toLowerCase().contains("tainted repriser");
        switch (attackStyle) {
        case Combat.ARROW_STYLE:
            result = ammo.getName().contains("arrow");
            break;
        case Combat.BOLT_STYLE:
            // handcannon exeption
            result = onlyBoltRack ? ammo.getId() == 4740 : defs.getName().toLowerCase().contains("hand cannon") ? ammo.getId() == 15243 : ammo.getName().contains("bolt");
            break;
        case Combat.THROWN_STYLE: // throwables not supposed to use ammo
        default:
            result = false;
            break;
        }

        if (result) {
            result = player.getSkills().getLevelForXp(4) >= ammo.getDefinitions().getCSOpcode(750);
            // result = defs.getCSOpcode(750) >=
            // ammo.getDefinitions().getCSOpcode(750);
        }
        if (!result)
            player.getPackets().sendEntityMessage(1, 0xFFFFFF, player, "You can't use that ammunition with your weapon.");
        return result;

    }

    // quantity -1 = weapon. -2 offhand weapon
    private void removeAmmo(final Player player, final int quantity, int delay) {
        final int ammoId = quantity == -1 ? player.getEquipment().getWeaponId() : quantity == -2 ? player.getEquipment().getShieldId() : player.getEquipment().getAmmoId();
        if (ItemDefinitions.getItemDefinitions(ammoId).getName().toLowerCase().contains("chakram") || ItemDefinitions.getItemDefinitions(ammoId).getName().toLowerCase().contains("glaive") || ItemDefinitions.getItemDefinitions(ammoId).getName().toLowerCase().contains("superior morrigan's javelin") || ItemDefinitions.getItemDefinitions(ammoId).getName().toLowerCase().contains("superior morrigan's throwing axe") || ItemDefinitions.getItemDefinitions(ammoId).getName().toLowerCase().contains("mud pie"))
            return;
        if (((ammoId == 21365 && Utils.getRandom(10) < 2) || (ammoId != 21365 && Utils.getRandom(3) > 0)) && (Combat.hasAvas(player) || player.getPerkManager().hasPerkActive(DonationPerk.AVAS_SECRET)) && !(ammoId == 25202 || (ammoId >= 13953 && ammoId <= 13957) || ammoId == 15243))
            return;
        player.getEquipment().removeAmmo(ammoId, quantity);
        if (delay != -1)
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    if (ammoId == 25202 || (ammoId >= 13953 && ammoId <= 13957) || ammoId == 15243)
                        return;
                    World.updateGroundItem(new Item(ammoId, quantity == -1 || quantity == -2 ? 1 : quantity), new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()), player, false);
                }
            }, delay);
    }

    public static boolean isWithinDistance(boolean mainHand, Player player, Entity target) {
        // player is walking to atm

        // doesnt
        return Utils.isOnRange(player, target, getAttackRange(player) // correct
                // extra
                // distance
                // for walk.
                // no
                // glitches
                // // this
                // way ^^.
                // even if
                // frozen
                + (player.hasWalkSteps() && target.hasWalkSteps() ? (player.getRun() && target.getRun() ? 2 : 1) : 0)) && !Utils.colides(player, target) && (target instanceof TheSanctumGuardian || player.clipedProjectile(target, hasMeleeDistanceWeapon(player) && !forceCheckClipAsRange(target) && player.getCombatDefinitions().getSpellId(!mainHand) <= 0));
    }

    public static int getAttackRange(Player player) {
        int mainHandRange = calculateAttackRange(player, true);
        int offHandRange = calculateAttackRange(player, false);
        if (offHandRange == -1)
            return mainHandRange;
        return Math.min(mainHandRange, offHandRange);
    }

    private static int calculateAttackRange(Player player, boolean mainHand) {
        int weaponType = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        Item item = player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        if (weaponType == Combat.MAGIC_TYPE || player.getCombatDefinitions().getSpellId(!mainHand) > 0)
            return 8;
        if (item == null || !mainHand && player.getEquipment().hasShield())
            return mainHand ? 0 : -1;
        String name = item.getDefinitions().getName();
        int id = item.getId();
        if (weaponType == Combat.MELEE_TYPE || weaponType == Combat.MAGIC_TYPE) {
            if (mainHand)
                if (name.contains("scythe") || name.contains("halberd") || name.contains("polearm") || name.contains("dragon rider lance") || name.contains("masuta's warspear"))
                    return 4;
            return 0;
        }
        int style = player.getCombatDefinitions().getStyle(!mainHand);
        int speed = item.getDefinitions().getAttackSpeed();
        if (style == Combat.THROWN_STYLE) {
            if (speed == 5)
                return 7;
            else if (speed == 6) {// darts, thrownaxe
                if (id == 13879 || id == 13953)
                    return 7;
                else if (id == 21364) // sagie
                    return 9;
                return 4;
            } else if (speed == 4) {// knives, javelin
                if (id == 30574 || id == 30575) // death lotus darts
                    return 6;
                return 5;
            }
        } else if (style == Combat.ARROW_STYLE || style == Combat.BOLT_STYLE) {
            if (speed == 4 || speed == 5 || speed == 12)
                return 7;
            else if (speed == 6) {
                if (id == 24338 || id == 24339 || id == 18331 || id == 18332 || id == 29634) // royal
                    // crossbow
                    // &
                    // slighted
                    // bows
                    return 9;
                return 8;
            }
        }
        return 0;
    }

    public static int getAttackSpeed(Player player, boolean mainHand) {
        Item item = getWeapon(player, mainHand);
        if (item == null)
            return 4;
        int speed = item.getDefinitions().getAttackSpeed() - (item.getDefinitions().equipType == 5 ? 2 : 0);
        if (item.getDefinitions().equipType == 5)
            speed += Settings.twohand_combat_speed_modifier;
        else
            speed += Settings.dual_combat_speed_modifier;
        if (speed < 1)
            speed = 1;
        return speed;
    }

    private static Item getWeapon(Player player, boolean mainHand) {
        return player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
    }

    public Entity[] getMultiAttackTargets(final Player player, final int maxDistance, final int maxAmtTargets) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        possibleTargets.add(target);
        if (target.isAtMultiArea()) {
            y: for (final int regionId : target.getMapRegionsIds()) {
                final Region region = World.getRegion(regionId);
                if (target instanceof Player) {
                    final List<Integer> playerIndexes = region.getPlayerIndexes();
                    if (playerIndexes == null) {
                        continue;
                    }
                    for (final int playerIndex : playerIndexes) {
                        final Player p2 = World.getPlayers().get(playerIndex);
                        if (p2 == null || p2 == player || p2 == target || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !p2.isAtMultiArea() || !p2.withinDistance(target, maxDistance)) {
                            continue;
                        }
                        possibleTargets.add(p2);
                        if (possibleTargets.size() == maxAmtTargets) {
                            break y;
                        }
                    }
                } else {
                    final List<Integer> npcIndexes = region.getNPCsIndexes();
                    if (npcIndexes == null) {
                        continue;
                    }
                    for (final int npcIndex : npcIndexes) {
                        final NPC n = World.getNPCs().get(npcIndex);
                        if (n == null || n == target || n == player.getFamiliar() || n.isDead() || n.hasFinished() || !n.isAtMultiArea() || !n.withinDistance(target, maxDistance) || !n.getDefinitions().hasAttackOption()) {
                            continue;
                        }
                        possibleTargets.add(n);
                        if (possibleTargets.size() == maxAmtTargets) {
                            break y;
                        }
                    }
                }
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public Entity[] getMultiAttackTargetGrid(final Player player, int size) {
        final List<Entity> possibleTargets = new ArrayList<Entity>();
        possibleTargets.add(target);
        if (size < 3) {
            return possibleTargets.toArray(new Entity[possibleTargets.size()]);
        }
        if (target.isAtMultiArea()) {
            for (final int regionId : target.getMapRegionsIds()) {
                final Region region = World.getRegion(regionId);
                if (target instanceof Player) {
                    final List<Integer> playerIndexes = region.getPlayerIndexes();
                    if (playerIndexes == null) {
                        continue;
                    }
                    for (final int playerIndex : playerIndexes) {
                        final Player p2 = World.getPlayers().get(playerIndex);
                        if (p2 == null || p2 == player || p2 == target || p2.isDead() || p2.hasFinished() || !p2.isCanPvp() || !p2.isAtMultiArea() || !Utils.isOnRange(player, p2, size - 3)) {
                            continue;
                        }
                        possibleTargets.add(p2);
                    }
                } else {
                    final List<Integer> npcIndexes = region.getNPCsIndexes();
                    if (npcIndexes == null) {
                        continue;
                    }
                    for (final int npcIndex : npcIndexes) {
                        final NPC n = World.getNPCs().get(npcIndex);
                        if (n == null || n == target || n == player.getFamiliar() || n.isDead() || n.hasFinished() || !n.isAtMultiArea() || !Utils.isOnRange(player, n, size - 3) || !n.getDefinitions().hasAttackOption()) {
                            continue;
                        }
                        possibleTargets.add(n);
                    }
                }
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    public int mageAttack(final Player player, boolean mainHand, int spellId, final boolean autocast) {
        int delay = !Settings.DUAL_COMBAT ? getAttackSpeed(player, mainHand) : 0;
        player.faceEntity(target);
        if (Utils.random(20) == 0 && player.getAuraManager().getDarkMagicBoost()) {
            WorldTasksManager.schedule(new WorldTask() {
                double multiplier = 1;
                int loop = 0;
                final int damage = (int) (player.getSkills().getLevelForXp(Skills.MAGIC) * 1.5);

                @Override
                public void run() {
                    if (target == null || target.isDead() || target.hasFinished()) {
                        stop();
                        return;
                    }
                    target.applyHit(new Hit(player, (int) (damage * multiplier), HitLook.REGULAR_DAMAGE));
                    multiplier = multiplier - (multiplier / 5);
                    if (loop == 3) {
                        stop();
                        return;
                    }
                    loop++;
                }
            }, 2, 3);
        }
        GeneralRequirementMap spellData = Magic.getSpellData(spellId);
        int spellBook = Magic.getSpellBook(spellData);
        /*
         * RS2 override: Ataraxia's cache leaves the spell-data keys for combat
         * spells empty (level=0, runes=[], XP=0). Look up authentic values
         * keyed on (button id, player's selected spell book) so casts cost the
         * correct runes and award the correct XP. Falls back to cache for any
         * spell not registered in Rs2SpellOverride.
         */
        Rs2SpellOverride.SpellData rs2Spell = Rs2SpellOverride.find(spellId, player.getCombatDefinitions().getSpellBook());
        if (!Magic.isUsingBorrowedPowerSpell(player) && rs2Spell == null && spellBook != 3 && spellBook != player.getCombatDefinitions().getSpellBook()) {
            player.getPackets().sendMainInterfaceMessage(1, "You are not using the correct spellbook for this spell.", true);
            return - 1;
        }
        Item weapon = player.getEquipment().getItem(!mainHand ? Equipment.SLOT_SHIELD : Equipment.SLOT_WEAPON);
        if (!autocast && Magic.isAutoCastSpell(spellData) && (weapon == null || !weapon.getDefinitions().isMagicTypeWeapon())) {
            player.getPackets().sendMainInterfaceMessage(1, "You need a magic weapon to cast this spell.", true);
            return -1;
        }
        if (rs2Spell != null) {
            if (player.getSkills().getLevelForXp(Skills.MAGIC) < rs2Spell.level) {
                player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
                if (autocast)
                    player.getCombatDefinitions().setAutoCast(-1);
                return -1;
            }
        } else if (!Magic.hasLevel(player, spellData)) {
            if (autocast)
                player.getCombatDefinitions().setAutoCast(-1);
            return -1;
        }
        if (!Magic.hasRequirements(player, spellData, mainHand))
            return -1;
        if (rs2Spell != null && !Magic.isUsingBorrowedPowerSpell(player) && !Magic.checkRunes(player, false, rs2Spell.runes))
            return -1;
        if (mainHand && player.getCombatDefinitions().isUsingSpecialAttack()) {// TODO
            return -1;
        }

        switch (spellData.getId()) {
        case 14795:
            if (target instanceof Player) {
                final Player p = (Player) target;
                if (p.getTeleBlockDelay() > Utils.currentTimeMillis()) {
                    player.sendMessage("A teleblock spell has already been cast on this player.");
                    return -1;
                }
            } else {
                player.getPackets().sendMainInterfaceMessage(1, "You can't use that against an NPC.", true);
                return -1;
            }
            break;
        }
        if (!Magic.isUsingBorrowedPowerSpell(player)) {
            if (rs2Spell != null)
                Magic.checkRunes(player, true, rs2Spell.runes);
            else
                Magic.checkRunes(player, spellData, true);
        }
        if (!Magic.isAutoCastSpell(spellData))
            Magic.resetSwapSpellBook(player);
        int type = Magic.getSpellType(spellData);
        int startGfx = spellData.getIntValue(2920);
        int projectileGfx = spellData.getIntValue(2940);
        player.setNextAnimation(new Animation(getAttackAnimation(player, mainHand)));
        if (startGfx != 0)
            player.setNextGraphics(new Graphics(startGfx));
        // TODO add sounds
//        playSound(spell.getSound(), player, target);
        magic_sound = -1;
        mage_hit_gfx = spellData.getIntValue(2933) == 0 ? null : new Graphics(spellData.getIntValue(2933), 0, spellBook == 1 ? 0 : spellData.getIntValue(2878) <= 1 ? 96 : spellData.getIntValue(2878));
        base_mage_xp = rs2Spell != null ? rs2Spell.xp : Magic.getSpellXP(spellData);
        if  (projectileGfx != 0)
            World.sendProjectileCycles(player, target, projectileGfx, spellBook == 1 ? 0 : 35, spellBook == 1 ? 0 : 35, 30, 45, 0, 0);
        String spellName = spellData.getStringValue(2794).toLowerCase();
        boolean multiTargets = spellName.contains("burst") || spellName.contains("barrage");
        Entity mainTarget = target;
        Entity[] targets = multiTargets ? getMultiAttackTargets(player) : new Entity[] { target };
        SpellEffect effect = Magic.getCombatSpellEffect(spellData);
        attackTarget(targets, new MultiAttack() {

            private boolean nextTarget;

            @Override
            public boolean attack() {
                double damageModifier = spellBook == 1 && target instanceof NPC && ((NPC) target).getDefinitions().getName().toLowerCase().contains("muspah") ? 2 : 1;
                if (spellBook == 1)
                    ancientSpell = true;
                if (target != mainTarget)
                    damageModifier = 0.35;
                if (type == Combat.TYPE_FIRE) {
                    if (target instanceof NPC) {
                        final NPC n = (NPC) target;
                        if (n.getId() == 9463 || n.getId() == 9462) {
                            damageModifier = 1.5;
                        } else if (n.getId() >= 14301 && n.getId() <= 14304) {
                            damageModifier = 2;
                        }
                    }
                }
                if (spellName.contains("ice")) {
                    final long currentTime = Utils.currentTimeMillis();
                    if (!(target.getFreezeDelay() >= currentTime || target.getFrozenBlockedDelay() >= currentTime) && !(target instanceof BloodReaver)) {
                        if (target instanceof Player) {
                            switch (spellName) {
                            case "ice rush":
                                freeze_time = 2400;
                                break;
                            case "ice burst":
                                freeze_time = 4800;
                                break;
                            case "ice blitz":
                                freeze_time = 7200;
                                break;
                            case "ice barrage":
                                freeze_time = 9600;
                                break;
                            }
                        } else
                            freeze_time = 10000;
                    }
                } else if (spellName.contains("blood ")) {
                    blood_spell = true;
                } else if (spellName.contains("shadow ")) {
                    reduceAttack = true;
                } else if (spellName.contains("smoke ")) {
                    switch (spellName) {
                    case "smoke rush":
                    case "smoke burst":
                        max_poison_hit = 20;
                        break;
                    case "smoke blitz":
                    case "smoke barrage":
                        max_poison_hit = 40;
                        break;
                    }
                }
                Hit hit = getHit(player, target, damageModifier, mainHand, false);
                final Entity t = target;
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (t instanceof Player) {
                            final Player p2 = (Player) t;
                            if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps()) {
                                p2.getActionManager().setAction(new PlayerCombat(player));
                            }
                        } else {
                            final NPC n = (NPC) t;
                            if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie()) {
                                n.setTarget(player);
                            }
                        }
                    }
                }, 2);
                if (effect != null) {
                    if (spellName.equalsIgnoreCase("Teleport Block")) {
                        effect.spellEffect(player, target, hit.getDamage(), mage_hit_gfx);
                    } else {
                        int dmg = hit.getDamage();
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                effect.spellEffect(player, target, dmg, mage_hit_gfx);
                            }
                        }, 2);
                    }
                    hit.setDamage(-2);
                }
                delayMagicHit(2, hit);
                if (targets != null && targets.length != 0 && targets[0].equals(target) && hit.getDamage() == 0)
                    return false;
                if (!nextTarget) {
                    nextTarget = true;
                }
                return nextTarget;
            }

        });
        Magic.resetUsingBorrowedPowerSpell(player);
        setWeaponDelay(player, mainHand);
        return delay;
    }

    public void attackTarget(final Entity[] targets, final MultiAttack perform) {
        final Entity realTarget = target;
        for (final Entity t : targets) {
            target = t;
            if (!perform.attack()) {
                break;
            }
        }
        target = realTarget;
    }

    private void checkSwiftGlovesEffect(final Player player, final int hitDelay, final int attackStyle, final int weaponId, final int hit, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        if (Utils.random(30) != 0) {
            return;
        }
        final Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
        if (gloves == null || !gloves.getDefinitions().getName().contains("Swift glove")) {
            return;
        }
        delayHit(hitDelay, weaponId, getRangeHit(player, (int) (hit * 0.25)));
        World.sendProjectile(player, target, gfxId, startHeight - 5, endHeight - 5, speed, delay, curve - 5 < 0 ? 0 : curve - 5, startDistanceOffset);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                target.addFreezeDelay(10000, true);
                target.setNextGraphics(new Graphics(181, 0, 96));
                if (target instanceof NPC) {
                    final NPC npc = (NPC) target;
                    @SuppressWarnings("unused")
                    final int bonus = npc.getBonus(0);
//      TODO              if (NPCBonusesDataParser.getBonuses(npc.getId()) != null && NPCBonusesDataParser.getBonuses(npc.getId())[0] * 0.93 < bonus) {
//                        npc.setBonus(0, (int) (NPCBonusesDataParser.getBonuses(npc.getId())[0] * 0.93));
//                    }
//                    if (NPCBonusesDataParser.getBonuses(npc.getId()) != null && NPCBonusesDataParser.getBonuses(npc.getId())[0] * 0.93 < bonus) {
//                        npc.setBonus(9, (int) (NPCBonusesDataParser.getBonuses(npc.getId())[9] * 0.93));
//                        // if (NPCBonuses.getBonuses(npc.getId()) != null &&
//                        // NPCBonuses.getBonuses(npc.getId())[0] * 0.93 < bonus)
//                        // npc.setBonus(10, (int) (NPCBonuses.getBonuses(npc.getId())[10] * 0.93));
//                    }
                } else if (target instanceof Player) {
                    final Player p2 = (Player) target;
                    if (p2.getSkills().getLevel(Skills.RANGE) > Math.ceil(p2.getSkills().getLevelForXp(Skills.RANGE) - 7)) {
                        p2.getSkills().drainLevel(Skills.RANGE, 7);
                    }
                    if (p2.getSkills().getLevel(Skills.DEFENCE) > Math.ceil(p2.getSkills().getLevelForXp(Skills.DEFENCE) - 7)) {
                        p2.getSkills().drainLevel(Skills.DEFENCE, 7);
                    }
                    if (p2.getSkills().getLevel(Skills.MAGIC) > Math.ceil(p2.getSkills().getLevelForXp(Skills.MAGIC) - 7)) {
                        p2.getSkills().drainLevel(Skills.MAGIC, 7);
                    }
                }
            }
        }, hitDelay);

    }

    public static void playSound(final int soundId, final Player player, final Entity target) {
        if (soundId == -1) {
            return;
        }
        player.getPackets().sendSound(soundId, 0, 1);
        if (target instanceof Player) {
            final Player p2 = (Player) target;
            p2.getPackets().sendSound(soundId, 0, 1);
        }
    }

    public boolean hasFireCape(final Player player) {
        final int capeId = player.getEquipment().getCapeId();
        return capeId == 6570 || capeId == 20769 || capeId == 20771 || capeId == 31603;
    }

    public void delayNormalHit(final int weaponId, final Hit... hits) {
        delayHit(0, weaponId, hits);
    }

    public Hit getMeleeHit(final Player player, final int damage) {
        return new Hit(player, damage, HitLook.MELEE_DAMAGE);
    }

    public Hit getRangeHit(final Player player, final int damage) {
        return new Hit(player, damage, HitLook.RANGE_DAMAGE);
    }

    public Hit getSpecialRangeHit(final Player player, final int damage, final boolean ruby) {
        return ruby ? new Hit(player, damage, HitLook.RANGE_DAMAGE, true) : new Hit(player, damage, HitLook.RANGE_DAMAGE);
    }

    public Hit getPoisonHit(final Player player, final int damage) {
        return new Hit(player, damage, HitLook.POISON_DAMAGE);
    }

    public Hit getMagicHit(final Player player, final int damage) {
        // if (hasPolyporeStaff(player); //damage = (damage >= 475) ? 475 :
        // damage;
        return new Hit(player, damage, HitLook.MAGIC_DAMAGE, ancientSpell);
    }

    private void delayMagicHit(final int delay, final Hit... hits) {
        delayHit(delay, -1, hits);
    }

    public void resetVariables() {
        base_mage_xp = 0;
        mage_hit_gfx = new Graphics(-1);
        magic_sound = 0;
        max_poison_hit = 0;
        freeze_time = 0;
        reduceAttack = false;
        blood_spell = false;
        block_tele = false;
    }

    private void delayHit(final int delay, final int weaponId, final Hit... hits) {
        addAttackedByDelay(hits[0].getSource(), target); // called separately
        // since some spells
        // dont do dmg

        final Entity target = this.target;
        final int max_hit = this.max_hit;
        final double base_mage_xp = this.base_mage_xp;
        final Graphics mage_hit_gfx = this.mage_hit_gfx;
        @SuppressWarnings("unused")
        final int magic_sound = this.magic_sound;
        final int max_poison_hit = this.max_poison_hit;
        final int freeze_time = this.freeze_time;
        final boolean blood_spell = this.blood_spell;
        final boolean block_tele = this.block_tele;
        // resetVariables();
        for (final Hit hit : hits) {
            final Player player = (Player) hit.getSource();
            player.getChargesManagerNew().useChargeOnHit(true);
            int damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
            if (damage == -2)
                damage = 0;
            if (target instanceof NPC)
                player.getInventionManager().processCombatXp(damage);
            if (target instanceof NPC) {
                final NPC tg = (NPC) target;
                int capDamage = Settings.USE_DAMAGE_CAP ? tg.getCapDamage() : -1;
                if (hit.getLook() == HitLook.RANGE_DAMAGE && target instanceof DemonFlashBoss) {
                    capDamage = 500;
                }
                if (capDamage > -1 && target instanceof DemonFlashBoss) {
                    if (hit.getDamage() > capDamage) {
                        hit.setDamage(capDamage);
                    }
                }

            }
            if (target instanceof HarpieBugSwarm) {
                if (!HarpieBugSwarm.canDamage(player)) {
                    hit.setDamage(0);
                }
            }

            if (target instanceof NPC) {
                final boolean deathtouched = player.getEquipment().getWeaponId() == 25202;
                if (deathtouched && hit.getLook() == HitLook.RANGE_DAMAGE) {
                    hit.setLook(HitLook.INSTANT_KILL_TYPE);
                    hit.setCriticalMark();
                    hit.setDamage(target.getHitpoints());
                }
            }
            damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
            if (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE) {
                final double combatXp = damage / 2.5;
                if (combatXp > 0) {
                    player.getAuraManager().checkSuccefulOutGoingHits(hit.getDamage());
                    if (hasReaperNecklace(player)) {
                        player.setReaperAccuracyBoost(player.getReaperHitChanceBoost() + 0.1 >= 3 ? 3 : (player.getReaperHitChanceBoost() + 0.1));
                        player.setLastReaperNecklaceEffect(Utils.currentTimeMillis() + 54000);
                    }
                    final double hpXp = damage / 7.5;
                    player.getCombatDefinitions().giveXp(target, combatXp, hpXp);
                }
            } else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
                if (mage_hit_gfx != null && mage_hit_gfx.getId() > 0 && damage > 0) {
                    if (freeze_time > 0 && !(target instanceof Muspahs)) {
                        target.addFreezeDelay(freeze_time, freeze_time == 0);
                        if (target instanceof Player) {
                            final Player frozen = (Player) target;
                            frozen.setRouteEvent(null);
                            frozen.resetWalkSteps();
                        }
                        target.addFrozenBlockedDelay(freeze_time + (5 * 1000));
                    }
                } else if (damage < 0) {
                    damage = 0;
                }
                if (base_mage_xp > 0)
                    player.getSkills().addXp(Skills.MAGIC, base_mage_xp);
                double combatXp = (damage / 5);
                if (combatXp > 0) {
                    player.getAuraManager().checkSuccefulOutGoingHits(hit.getDamage());
                    if (hasReaperNecklace(player)) {
                        player.setReaperAccuracyBoost(player.getReaperHitChanceBoost() + 0.1 >= 3 ? 3 : (player.getReaperHitChanceBoost() + 0.1));
                        player.setLastReaperNecklaceEffect(Utils.currentTimeMillis() + 54000);
                    }
                    final double hpXp = damage / 7.5;
                    player.getCombatDefinitions().giveXp(target, combatXp, hpXp);
                }
            }
        }
        // TODO Add off-hand combat here

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                for (final Hit hit : hits) {
                    boolean forceNoSplash = hit.getDamage() == -2;
                    if (forceNoSplash)
                        hit.setDamage(0);
                    boolean splash = false;
                    final Player player = (Player) hit.getSource();
                    if (player.isDead() || player.hasFinished() || target.isDead() || target.hasFinished()) {
                        return;
                    }

                    if (player.getEquipment().getWeaponId() != 33590) {
                        if (hit.getDamage() > -1) {
                            Perk afterShock = player.getInventionManager().hasPerk(Perks.AFTERSHOCK);
                            target.applyHit(hit);
                            if (afterShock != null)
                                player.getInventionManager().setAfterShockDamage(player.getInventionManager().getAfterShockDamage() + (hit.getDamage() * (target instanceof Player ? 2 : 1)));
                            Perk ultimatums = player.getInventionManager().hasPerk(Perks.ULTIMATUMS);
                            Perk energising = player.getInventionManager().hasPerk(Perks.ENERGISING);
                            Perk impatient = player.getInventionManager().hasPerk(Perks.IMPATIENT);
                            if (impatient != null && ultimatums == null && energising == null)
                                player.getInventionManager().setImpatientDamage(player.getInventionManager().getImpatientDamage() + hit.getDamage());
                        } else {
                            splash = true;
                            if (target instanceof Player && ((Player) target).getActionManager().getAction() != null) {
                                ((Player) target).getActionManager().forceStop();
                            }
                            hit.setDamage(0);
                        }
                    }
                    doDefenceEmotes();
                    if (player.getGemstoneArmour().useDragonfireSpecial()) {
                        hit.setDamage((int) (hit.getDamage() * 1.14));
                    }
                    final int damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
                    if (Combat.fullGuthanEquipped(player)) {
                        if (Utils.random(4) == 0)
                            if (player != null) {
                                int heal = damage;
                                if (heal > 0) {
                                    if (player.getHitpoints() < player.getMaxHitpoints()) {
                                        player.heal(heal);
                                        target.setNextGraphics(new Graphics(398));
                                    }
                                }
                            }
                    }
                    player.getGemstoneArmour().useOnyxSpecial(damage);
                    if (hit.getDamage() == 0 || damage == 0) {
                        splash = true;
                    }
                    if (player.getControlerManager().getControler() instanceof DungeonController) {
                        if (player.getEquipment().getAmuletId() == 15834 || player.getEquipment().getAmuletId() == 17291) {
                            if (Utils.currentTimeMillis() > player.getBloodNecklaceSpecial() && player.getHitpoints() < player.getMaxHitpoints()) {
                                player.setBloodNecklaceSpecial();
                                player.sendMessage("Your blood necklace siphons some life from your targets.", true);
                                int dg = 0;
                                for (final Entity i : getMultiAttackTargets(player, target, 16, 10, true)) {
                                    if (player.getDungeoneeringManager().getParty() == null) {
                                        continue;
                                    }
                                    if (player.getDungeoneeringManager().getParty().getDungeon() == null) {
                                        continue;
                                    }
                                    final DungeonManager dungeon = player.getDungeoneeringManager().getParty().getDungeon();
                                    if (!dungeon.getCurrentRoomReference(player).equals(dungeon.getCurrentRoomReference(i))) {
                                        continue;
                                    }
                                    final int d = Utils.random(20, 50);
                                    i.applyHit(new Hit(player, d, HitLook.REGULAR_DAMAGE));
                                    if (player.getHitpoints() < player.getMaxHitpoints()) {
                                        player.applyHit(new Hit(null, d + player.getHitpoints() <= player.getMaxHitpoints() ? d : player.getMaxHitpoints() - player.getHitpoints(), HitLook.HEALED_DAMAGE));
                                    }
                                    dg += d;
                                    if (dg >= 120) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if ((damage >= max_hit * 0.90) && (hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE)) {
                        hit.setCriticalMark();
                    }
                    if (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE) {
                        final double combatXp = damage / 2.5;
                        if (combatXp > 0) {
                            if (hit.getLook() == HitLook.RANGE_DAMAGE) {
                                if (weaponId != -1) {
                                    final String name = ItemDefinitions.getItemDefinitions(weaponId).getName();
                                    if (name.contains("(p++)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(48);
                                        }
                                    } else if (name.contains("(p+)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(38);
                                        }
                                    } else if (name.contains("(p)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(28);
                                        }
                                    }
                                }
                            } else {
                                if (weaponId != -1) {
                                    final String name = ItemDefinitions.getItemDefinitions(weaponId).getName();
                                    if (name.contains("(p++)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(68);
                                        }
                                    } else if (name.contains("(p+)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(58);
                                        }
                                    } else if (name.contains("(p)")) {
                                        if (Utils.getRandom(8) == 0) {
                                            target.getPoison().makePoisoned(48);
                                        }
                                    }
                                    if (target instanceof Player) {
                                        if (((Player) target).getPolDelay() >= Utils.currentTimeMillis()) {
                                            target.setNextGraphics(new Graphics(2320));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
                        if (splash) {
                            if (!forceNoSplash)
                                target.setNextGraphics(new Graphics(85, 0, 96));
                        } else {
                            if (mage_hit_gfx != null && mage_hit_gfx.getId() > 0) {
                                if (!forceNoSplash)
                                    target.setNextGraphics(mage_hit_gfx);
                                if (blood_spell) {
                                    int heal = (int) Math.ceil((double) damage * 0.05);
                                    if (heal > 0)
                                        player.heal(heal);
                                    if (target instanceof DemonFlashBoss) {
                                        hit.setLook(HitLook.HEALED_DAMAGE);
                                    }
                                }
                                if (block_tele) {
                                    if (target instanceof Player) {
                                        final Player targetPlayer = (Player) target;
                                        targetPlayer.setTeleBlockDelay((targetPlayer.getPrayer().isMageProtecting() ? 100000 : 300000));
                                        targetPlayer.sendMessage("You have been teleblocked.", true);
                                    }
                                }
                            }
//                            if (magic_sound > 0) {
//                                playSound(magic_sound, player, target);
//                            }
                        }
                    }
                    if (max_poison_hit > 0 && Utils.getRandom(10) == 0) {
                        if (!target.getPoison().isPoisoned()) {
                            target.getPoison().makePoisoned(max_poison_hit);
                        }
                    }
                    Perk afterShock = player.getInventionManager().hasPerk(Perks.AFTERSHOCK);
                    if (afterShock != null && player.getInventionManager().getAfterShockDamage() >= 5000) {
                        sendAfterShockHit(player, target);
                    }
                    Perk ultimatums = player.getInventionManager().hasPerk(Perks.ULTIMATUMS);
                    Perk energising = player.getInventionManager().hasPerk(Perks.ENERGISING);
                    Perk impatient = player.getInventionManager().hasPerk(Perks.IMPATIENT);
                    if (impatient != null && ultimatums == null && energising == null && player.getInventionManager().getImpatientDamage() >= 5000) {
                        player.getInventionManager().setImpatientDamage(0);
                        if (Math.random() <= (0.05 * (double) impatient.getRank())) {
                            player.getCombatDefinitions().restoreSpecialAttack(20);
                            player.getPackets().sendGameMessage("Your impatient perk effect restores 20% special attack percentage.", true);
                        }
                    }
                    if (target instanceof Player) {
                        final Player p2 = (Player) target;
                        // p2.closeInterfaces();
                        if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps()) {
                            p2.getActionManager().setAction(new PlayerCombat(player));
                        }
                    } else {
                        final NPC n = (NPC) target;
                        if (n instanceof Vorago) {
                            if (!((Vorago) n).cantBeAutoRetaliated() && !n.isUnderCombat()) {
                                n.setTarget(player);
                            }
                        } else {
                            if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie()) {
                                n.setTarget(player);
                            }
                        }
                    }
                }
            }
        }, delay);
    }

    public static void sendAfterShockHit(Player player, Entity target) {
        Perk afterShock = player.getInventionManager().hasPerk(Perks.AFTERSHOCK);
        if (afterShock == null || target == null)
            return;
        player.getInventionManager().setAfterShockDamage(0);
        target.setNextGraphics(new Graphics(6069));
        Entity[] targets = getMultiAttackTargets(player, target);
        for (Entity t : targets) {
            if (t == null || t.isDead() || t.hasFinished())
                continue;
            Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
            boolean mainHand = false;
            if (weapon != null && weapon.getInventionData() != null) {
                for (Gizmo gizmo : weapon.getInventionData().getGizmos()) {
                    if (gizmo != null)
                        for (Perk perk : gizmo.getPerks()) {
                            if (perk != null && perk.getId() == Perks.AFTERSHOCK.getId()) {
                                mainHand = true;
                                break;
                            }
                        }
                    if (mainHand)
                        break;
                }
            }
            int maxHit = player.getCombatDefinitions().getHandDamage(false);
            int attackStyle = player.getCombatDefinitions().getStyle(false);
            int attackType = Combat.getStyleType(attackStyle);
            int damage = (int) (maxHit * ThreadLocalRandom.current().nextDouble(target instanceof Player ? 0.2 : 0.4, (0.4 * (double) afterShock.getRank()) / (double) ((target instanceof Player) ? 2 : 1)));
            Hit afterShockHit = new Hit(player, damage, attackType == Combat.MELEE_TYPE || attackType == Combat.ALL_TYPE ? HitLook.MELEE_DAMAGE : attackType == Combat.RANGE_TYPE ? HitLook.RANGE_DAMAGE : attackType == Combat.MAGIC_TYPE ? HitLook.MAGIC_DAMAGE : HitLook.REGULAR_DAMAGE);
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    if (t == null || t.isDead() || t.hasFinished())
                        return;
                    t.applyHit(afterShockHit);
                }

            }, 2);
        }
    }

    private void doDefenceEmotes() {
        if (target.isCantDoDefenceEmote()) {
            return;
        }
        target.setNextAnimationNoPriority(new Animation(Combat.getDefenceEmote(target)));
    }

    /*
     * Called when fights end / stop Useful for resetting player-data
     */
    @Override
    public void stop(final Player player) {
        player.setNextFaceEntity(null);
        if ((player.getAttackedByDelay() - Utils.currentTimeMillis()) <= 0) {
            player.setAttackedBy(null);
            player.setAttackedByDelay(0);
        }
        player.getTemporaryAttributtes().put(Key.REMOVE_CRACKLING_TARGET_TIMER, Utils.currentTimeMillis() + 10000L);
        player.getTemporaryAttributtes().remove("last_target");
    }

    private boolean checkAll(final Player player) {
        // checks if target is ingame and not ded
        if (player.isDead() || player.hasFinished() || player.isCantWalk()) {
            return false;
        }
        if (target.isDead() || target.hasFinished()) {
            int mainHandDelay = (int) (player.getCombatDefinitions().getMainHandDelay() - Utils.currentWorldCycle());
            int offHandDelay = (int) (player.getCombatDefinitions().getOffHandDelay() - Utils.currentWorldCycle());
            if (mainHandDelay > 0 || offHandDelay > 0) {
                player.setNextAnimation(new Animation(-1));
            }
            return false;
        }
        if (!player.getControlerManager().canHit(target)) {
            return false;
        }
        if (player.isROTSLocked()) {
            return false;
        }
        if (target.isDead() || target.hasFinished()) {
            // fixes fact u had already started attacked and then target died
            player.setNextAnimation(new Animation(-1));
            return false;
        }
        if (player.isFrozen()) {
            player.faceEntity(target);
            return !Utils.colides(player, target);
        }
        if (target instanceof Player) {
            final Player p2 = (Player) target;
            if (!player.isCanPvp() || !p2.isCanPvp()) {
                return false;
            }
        } else {
            final NPC n = (NPC) target;
            if (n.isCantInteract()) {
                return false;
            }

            if (n instanceof EliteNPC) {
                val elite = (EliteNPC) n;
                if (elite.getOwner() != null && elite.getOwner() != player) {
                    player.sendMessage("You cannot attack that monster.");
                    return false;
                }
            }

            if (n instanceof Familiar) {
                final Familiar familiar = (Familiar) n;
                if (!familiar.canAttack(target)) {
                    return false;
                }
            } else {
                if (!n.canBeAttackFromOutOfArea() && !MapAreas.isAtArea(n.getMapAreaNameHash(), player)) {
                    return false;
                }
                if (!canAttackNpc(player, n, true)) {
                    return false;
                }
            }
        }
        if (!(target instanceof NPC && ((NPC) target).isForceMultiAttacked())) {
            if (!target.isAtMultiArea() || !player.isAtMultiArea()) {
                if (player.getAttackedBy() != target && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    return false;
                }
                if (target.getAttackedBy() != player && target.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    return false;
                }
            }
        }
        if (Utils.colides(player, target) && !target.hasWalkSteps()) {
            player.resetWalkSteps();
            return player.calcFollow(target, true);
        }
        if (hasMeleeDistanceWeapon(player) && Math.abs(player.getX() - target.getX()) == 1 && Math.abs(player.getY() - target.getY()) == 1 && !target.hasWalkSteps() && target.getSize() == 1) {
            player.resetWalkSteps();
            if (!player.addWalkSteps(target.getX(), player.getY(), 1)) {
                player.resetWalkSteps();
                player.addWalkSteps(player.getX(), target.getY(), 1);
            }
            return true;
        }
        if (!Utils.isOnRange(player, target, getAttackRange(player)) || (!(target instanceof TheSanctumGuardian) && !player.clipedProjectile(target, hasMeleeDistanceWeapon(player) && !forceCheckClipAsRange(target) && player.getCombatDefinitions().getSpellId(false) <= 0))) {
            if (!player.hasWalkSteps() || target.hasWalkSteps()) {
                player.resetWalkSteps();
                if (!player.calcFollow(target, player.getRun() ? 2 : 1, true, true, getAttackRange(player), false))
                    return false;
            }

        } else
            player.resetWalkSteps();
        player.getTemporaryAttributtes().put("last_target", target);
        if (target != null) {
            target.getTemporaryAttributtes().put("last_attacker", player);
        }
        if (player.getPolDelay() >= Utils.currentTimeMillis() && !(player.getEquipment().getWeaponId() == 15486 || player.getEquipment().getWeaponId() == 22207 || player.getEquipment().getWeaponId() == 22209 || player.getEquipment().getWeaponId() == 22211 || player.getEquipment().getWeaponId() == 22213 || player.getEquipment().getWeaponId() == 34155)) {
            player.setPolDelay(0);
        }
        if (player.getCombatDefinitions().isInstantAttack()) {
            player.getCombatDefinitions().setInstantAttack(false);
//            if (player.getCombatDefinitions().getAutoCastSpell() > 0) {
//                return true;
//            }
            if (player.getCombatDefinitions().isUsingSpecialAttack()) {
                if (!specialExecute(player)) {
                    return true;
                }
                player.getActionManager().setActionDelay(0);
                final int weaponId = player.getEquipment().getWeaponId();
                switch (weaponId) {
                case 4153:
                    player.setNextAnimation(new Animation(1667));
                    player.setNextGraphics(new Graphics(340, 0, 96 << 16));
                    delayNormalHit(weaponId, getHit(player, target, 1.1, true, false));
                    break;
                }
                player.getActionManager().setActionDelay(4);
            }
            return true;
        }
        return true;
    }

    /**
     * Checks if the player can attack an NPC.
     *
     * @return if can attack.
     */
    public static boolean canAttackNpc(final Player player, final NPC n) {
        return canAttackNpc(player, n, false);
    }

    public static boolean canAttackNpc(final Player player, final NPC n, boolean sendMessage) {
        final int slayerLevel = Combat.getSlayerLevelForNPC(n.getId());
        final String npcName = n.getDefinitions().name.toLowerCase();

        if (n.getId() == 21994 || n.getId() == 22001 || n.getId() == 22007) {
            if (player.getSkills().getLevel(Skills.SLAYER) < 96) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need a Slayer level of 96 to attack this monster.");
                return false;
            }
        }
        if (n.getId() == 24830) {
            if (player.getSkills().getLevel(Skills.SLAYER) < 104) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need a Slayer level of 104 to attack this monster.");
                return false;
            }
        }
        if (n.getId() == 24831) {
            if (player.getSkills().getLevel(Skills.SLAYER) < 106) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need a Slayer level of 106 to attack this monster.");
                return false;
            }
        }
        if (n.getId() == 24832) {
            if (player.getSkills().getLevel(Skills.SLAYER) < 108) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need a Slayer level of 108 to attack this monster.");
                return false;
            }
        }

        if (player.getEquipment().getWeaponId() == 33590) {
            if (sendMessage)
                player.sendMessage("How on Gielinor are you going to hurt something with a snowball?!");
            return false;
        }

        if (n.getId() >= 24170 && n.getId() <= 24172 && !(n instanceof InstancedGemstoneDragon)) {
            if (player.getGemstoneKC() == 0) {
                if (player.getTask() == null || player.getTask() != null && !player.getTask().getName(player).equals("Gemstone dragon")) {
                    if (sendMessage)
                        player.sendMessage("You cannot kill anymore gemstone dragons until you pay Kelhar.");
                    return false;
                }
            }
        }
        if (slayerLevel > player.getSkills().getLevel(Skills.SLAYER)) {
            if (slayerLevel > 99) {
                if (player.getSkills().getSlayerLevelForXp(Skills.SLAYER) < slayerLevel) {
                    if (sendMessage)
                        player.sendMessage("You need a Slayer level of at least " + slayerLevel + " to fight this.");
                    return false;
                }
            } else {
                if (sendMessage)
                    player.sendMessage("You need a Slayer level of at least " + slayerLevel + " to fight this.");
                return false;
            }
        }
        if (n instanceof SophanemSlayerNPC) {
            for (SophanemSlayerNPC.SlayerLevelRequirement slayerLevelRequirement : SophanemSlayerNPC.SlayerLevelRequirement.VALUES) {
                if (slayerLevelRequirement.getNpcId() == n.getId()) {
                    val requirement = slayerLevelRequirement.getRequirement();
                    if (slayerLevelRequirement.getLevelRequirementType() == SophanemSlayerNPC.SlayerLevelRequirement.LevelRequirementType.LEVEL) {
                        return player.getSkills().getLevel(Skills.SLAYER) >= requirement;
                    } else if (slayerLevelRequirement.getLevelRequirementType() == SophanemSlayerNPC.SlayerLevelRequirement.LevelRequirementType.EXPERIENCE) {
                        return player.getSkills().getXp(Skills.SLAYER) >= requirement;
                    }
                }
            }
            return false;
        }
        if (World.isAtKuradalsDungeon(player)) {
            if (player.getTask() == null || !SlayerTask.doCustomSlayerNPC(player, n) && !npcName.contains(player.getTask().getName(player).toLowerCase())) {
                if (sendMessage)
                    player.sendMessage("You can only attack this monster when on a Slayer task.");
                return false;
            }
        }
        if (npcName.equalsIgnoreCase("edimmu") && !(player.getControlerManager().getControler() instanceof DungeonController)) {
            if (player.getTask() == null || !SlayerTask.doCustomSlayerNPC(player, n) && !npcName.contains(player.getTask().getName(player).toLowerCase())) {
                if (sendMessage)
                    player.sendMessage("You can only attack this monster when on a Slayer task.");
                return false;
            }
        }
        if (player.getEquipment().getWeaponId() == 4084) {
            if (sendMessage)
                player.sendMessage("How would I do this wearing a sled?");
            return false;
        }
        if (n.getId() == 14578) {// TODO
            if (player.getEquipment().getWeaponId() != 2402) {
                if (sendMessage)
                    player.sendMessage("I'd better wield a Silverlight first.");
                return false;
            }
        }

        if (n.getId() >= 22002 & n.getId() <= 22004) {
            final Mirage mirage = (Mirage) n;
            if (mirage.getCamel().getTargetIndex() != -1 && player.getIndex() != mirage.getCamel().getTargetIndex()) {
                if (sendMessage)
                    player.sendMessage("This isn't your target.");
                return false;
            }
        }

        if (n.getId() == 14301 || n.getId() == 14302 || n.getId() == 14303 || n.getId() == 14304) {
            final Glacyte glacyte = (Glacyte) n;
            if (glacyte.getGlacor().getTargetIndex() != -1 && player.getIndex() != glacyte.getGlacor().getTargetIndex()) {
                if (sendMessage)
                    player.sendMessage("This isn't your target.");
                return false;
            }
        }

        if (n.getId() == 6222 || n.getId() == 6223 || n.getId() == 6225 || n.getId() == 6227) {
            int attackingHand = PlayerCombat.getAttackingHand(player);
            if (attackingHand == -1)
                return true;
            int combatType = player.getCombatDefinitions().getType(attackingHand == 1 ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
            if (combatType == Combat.MELEE_TYPE || combatType == Combat.ALL_TYPE) {
                if (sendMessage)
                    player.sendMessage("You can't reach that - you'll need a ranged weapon or magic spell.");
                return false;
            }
        }
        if (n instanceof Kurask || n instanceof Turoth) {
            int attackingHand = PlayerCombat.getAttackingHand(player);
            if (attackingHand == -1)
                return true;
            boolean mainHand = attackingHand == 1;
            Item weapon = player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
            int combatType = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
            boolean canAttack = false;
            switch (combatType) {
            case Combat.MELEE_TYPE:
                canAttack = weapon != null && weapon.getDefinitions().getName().toLowerCase().contains("leaf-bladed");
                break;
            case Combat.RANGE_TYPE:
                if (weapon == null)
                    break;
                Item ammo = player.getEquipment().getItem(Equipment.SLOT_ARROWS);
                int combatStyle = player.getCombatDefinitions().getStyle(!mainHand);
                if (ammo == null || weapon.getDefinitions().getCSOpcode(2940) != 0)
                    break;
                if (combatStyle == Combat.ARROW_STYLE)
                    canAttack = ammo.getDefinitions().getName().toLowerCase().contains("broad arrow");
                if (combatStyle == Combat.BOLT_STYLE)
                    canAttack = ammo.getDefinitions().getName().toLowerCase().contains("broad") || ammo.getDefinitions().getName().toLowerCase().contains("royal") || ammo.getDefinitions().getName().toLowerCase().contains("ascension");
                break;
            case Combat.MAGIC_TYPE:
                int spellId = player.getCombatDefinitions().getSpellId(!mainHand);
                if (spellId >= 1000)
                    spellId -= 1000;
                canAttack = spellId == 44;// slayer dart
                break;
            }
            if (!canAttack) {
                player.getPackets().sendGameMessage("You can't attack this monster with this attack style.");
                return false;
            }
            return true;
        }
        return true;
    }

    public Entity getTarget() {
        return target;
    }

    public Entity[] getMultiAttackTargets(final Player player) {
        return getMultiAttackTargets(player, target, 1, 9, false);
    }

    public interface MultiAttack {

        boolean attack();

    }

    public static void autoRelatie(Player player, Entity target) {
        if (target instanceof Player) {
            Player p2 = (Player) target;
            p2.closeInterfaces();
            if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps() && !p2.isLocked() && !p2.getEmotesManager().isDoingEmote())
                p2.getActionManager().setAction(new PlayerCombat(player));
        } else {
            NPC n = (NPC) target;
            if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie())
                n.setTarget(player);
        }
    }

    public Hit getHit(final Player player, Entity target, boolean mainHand) {
        return getHit(player, target, 1, mainHand, false, false);
    }

    public Hit getMaxHit(final Player player, Entity target, boolean mainHand) {
        return getHit(player, target, 1, mainHand, false, true);
    }

    public Hit getHit(final Player player, Entity target, double damageMultiplier, boolean mainHand, boolean ignoreDefence) {
        return getHit(player, target, damageMultiplier, mainHand, ignoreDefence, false);
    }

    /**
     * Special-attack flavour of getHit that adds a one-shot accuracy boost
     * (in percentage points) to the hit chance for this single call. Used to
     * replicate pre-EOC special accuracy bumps (e.g. DDS +25%, Korasi +50%)
     * that the base damageMultiplier doesn't capture. The boost is cleared
     * in a finally block so an exception inside getHit can't leak it into
     * the next attack.
     */
    public Hit getHit(final Player player, Entity target, double damageMultiplier, double specAccuracyBoost, boolean mainHand, boolean ignoreDefence) {
        if (specAccuracyBoost == 0.0)
            return getHit(player, target, damageMultiplier, mainHand, ignoreDefence, false);
        player.getTemporaryAttributtes().put(Key.SPEC_ACCURACY_BOOST, specAccuracyBoost);
        try {
            return getHit(player, target, damageMultiplier, mainHand, ignoreDefence, false);
        } finally {
            player.getTemporaryAttributtes().remove(Key.SPEC_ACCURACY_BOOST);
        }
    }

    public Hit getHit(final Player player, Entity target, double damageMultiplier, boolean mainHand, boolean ignoreDefence, boolean maxHit) {
        int attackStyle = player.getCombatDefinitions().getStyle(!mainHand);
        int attackType = Combat.getStyleType(attackStyle);
        double hitChance = 0;
        damageMultiplier += getDamageModifier(player, target, attackStyle, mainHand);
        boolean deathtouched = mainHand && player.getEquipment().getWeaponId() == 25202;
        if (((!maxHit && !ignoreDefence) || damageMultiplier <= 0) && !deathtouched) {
            hitChance = Combat.getHitChance(player, target, attackStyle, mainHand);
            hitChance += getHitChanceModifier(player, target, attackStyle, mainHand);
            if (Math.random() * 100 > hitChance || damageMultiplier <= 0)
                return new Hit(player, 0, attackType == Combat.MELEE_TYPE || attackType == Combat.ALL_TYPE ? HitLook.MELEE_DAMAGE : attackType == Combat.RANGE_TYPE ? HitLook.RANGE_DAMAGE : HitLook.MAGIC_DAMAGE);
        }
        if (Settings.RS2_COMBAT) {
            /*
             * RS2 path: skip the EOC perk + uniform-roll damage code below in
             * favour of the pre-EOC max-hit formula and 0..max distribution.
             * The aggregated damageMultiplier (special attack stack +
             * getDamageModifier -> getRs2DamageModifier) feeds in as the
             * max-hit multiplier. Since getRs2DamageModifier excludes prayer,
             * auras, and modern perks, no subtraction is needed here:
             * Rs2AtaraxiaNumerics applies prayer at the correct points in
             * the formula (effective-level boost for melee/ranged, spell
             * max-hit multiplier for magic).
             */
            max_hit = Rs2AtaraxiaNumerics.calculatePlayerMaxHitAtaraxia(
                    player, attackStyle, attackType, mainHand, damageMultiplier);
            int rs2Damage = maxHit ? max_hit : Rs2AtaraxiaNumerics.rollPlayerHit(
                    player, target, damageMultiplier, mainHand, true, false);
            HitLook rs2Look = attackType == Combat.MELEE_TYPE || attackType == Combat.ALL_TYPE ? HitLook.MELEE_DAMAGE
                    : attackType == Combat.RANGE_TYPE ? HitLook.RANGE_DAMAGE
                    : attackType == Combat.MAGIC_TYPE ? HitLook.MAGIC_DAMAGE
                    : HitLook.REGULAR_DAMAGE;
            if (Settings.DEBUG)
                player.getPackets().sendGameMessage("[RS2] hitchance=" + hitChance + ", maxHit=" + max_hit + ", damage=" + rs2Damage + ", damage mod=" + damageMultiplier);
            return new Hit(player, rs2Damage, rs2Look);
        }
        double critChance = 0.05;
        boolean procbiting = false;
        Perk biting = player.getInventionManager().hasPerk(Perks.BITING);
        if (biting != null)
            procbiting = Math.random() <= (0.02 * (double) biting.getRank() * (biting.hasIncreasedChance() ? 1.1 : 1.0));
        Perk equilibrium = player.getInventionManager().hasPerk(Perks.EQUILIBRIUM);
        Perk precise = player.getInventionManager().hasPerk(Perks.PRECISE);
        double minhitMod = 0.759;
        double maxhitMod = 2.30;
        if (precise != null) {
            minhitMod += (maxhitMod * 0.015 * (double) precise.getRank());
        }
        if (equilibrium != null) {
            minhitMod += (minhitMod * 0.03 * (double) equilibrium.getRank());
            maxhitMod -= (maxhitMod * 0.01 * (double) equilibrium.getRank());
        }
        max_hit = (int) ((double) player.getCombatDefinitions().getHandDamage(!mainHand) * 2.3 * damageMultiplier);
        double r = ThreadLocalRandom.current().nextDouble(minhitMod, maxhitMod + 0.01);
        int damage = (int) (((double) max_hit / 2.3) * r);
        Perk spendthrift = player.getInventionManager().hasPerk(Perks.SPENDTHRIFT);
        if (!(target instanceof Player) && spendthrift != null && Math.random() <= (0.01 * (double) spendthrift.getRank() * (spendthrift.hasIncreasedChance() ? 1.1 : 1.0))) {
            int extraDamage = (int) ((double) damage * (0.01 * (double) spendthrift.getRank()));
            if (extraDamage > player.getInventory().getCoinsAmount())
                extraDamage = player.getInventory().getCoinsAmount();
            if (extraDamage > 0) {
                player.getInventory().removeItemMoneyPouch(new Item(995, extraDamage));
                player.getPackets().sendGameMessage("Spendthrift has activated and dealt an additional " + extraDamage + " damage, costing that amount in coins too!");
                damage += extraDamage;
            }
        }
        Perk plantedFeet = player.getInventionManager().hasPerk(Perks.PLANTED_FEET);
        if (plantedFeet != null)
            damage -= damage * 0.10;
        Perk fatiguing = player.getInventionManager().hasPerk(Perks.FATIGUING);
        if (fatiguing != null)
            damage -= damage * 0.03 * fatiguing.getRank();
        if (!(target instanceof Player) && (Math.random() <= critChance || procbiting)) {
            damage = (int) (damage + max_hit * ThreadLocalRandom.current().nextDouble(0.2, 0.81));
            if (procbiting)
                player.getPackets().sendGameMessage("<col=00ff00>You deal a critical strike against your opponent!", true);
        }

        if (!deathtouched) {
            int dmgBefore = damage;
            int maxBefore = max_hit;


            int dmgAfter = (int) Math.round(dmgBefore * GLOBAL_FINAL_DAMAGE_MULT);
            int maxAfter = (int) Math.round(maxBefore * GLOBAL_FINAL_DAMAGE_MULT);


            damage = dmgAfter;
            max_hit = maxAfter;


                //player.getPackets().sendGameMessage(
                  //      "P dmg scaled: " + dmgBefore + " -> " + dmgAfter
                    //            + " | max: " + maxBefore + " -> " + maxAfter
                      //          + " (x" + GLOBAL_FINAL_DAMAGE_MULT + ")"
               // );

        }

        Hit hit = new Hit(player, damage, attackType == Combat.MELEE_TYPE || attackType == Combat.ALL_TYPE ? HitLook.MELEE_DAMAGE : attackType == Combat.RANGE_TYPE ? HitLook.RANGE_DAMAGE : attackType == Combat.MAGIC_TYPE ? HitLook.MAGIC_DAMAGE : HitLook.REGULAR_DAMAGE);
        if (maxHit)
            hit.setDamage(max_hit);
        if (Settings.DEBUG)
            player.getPackets().sendGameMessage("hitchance=" + (hitChance) + ", maxHit=" + max_hit + ", damage=" + damage + " , damage mod=" + damageMultiplier);
        return hit;
    }

    public double getHitChanceModifier(Player player, Entity target, int attackStyle, boolean mainHand) {
        double hitChanceModifier = 0.00;
        if (hasDragonBattleAxeBuff(player))
            hitChanceModifier -= 10.00;
        if (target instanceof NPC) {
            NPCDefinitions defs = ((NPC) target).getDefinitions();
            int specialWeakness = defs == null ? -1 : defs.getSpecialWeaknesses();
            if (mainHand && player.getEquipment().getWeaponId() != -1) {
                String weaponName = player.getEquipment().getItem(Equipment.SLOT_WEAPON).getName().toLowerCase();
                if (weaponName.contains("keris") && specialWeakness == Combat.KERIS_DAGGER_WEAKNESS) {
                    boolean hasDesertAmulet = player.getEquipment().getAmuletId() == 27095 || player.getEquipment().getAmuletId() == 27096;
                    hitChanceModifier += hasDesertAmulet ? 25.00 : 15.00;
                }
                if ((weaponName.contains("darklight") || weaponName.contains("silverlight")) && specialWeakness == Combat.SILVER_LIGHT_WEAKNESS)
                    hitChanceModifier += 20.00;
                if ((weaponName.contains("balmung")) && specialWeakness == Combat.BALMUNG_WEAKNESS)
                    hitChanceModifier += weaponName.equalsIgnoreCase("balmung") ? 30.00 : 45.00;
                if (weaponName.contains("hexhunter bow") && Combat.getStyleType(defs.getWeaknessStyle()) == Combat.RANGE_TYPE)
                    hitChanceModifier += 10.00;
                if ((weaponName.contains("blisterwood") || weaponName.contains("sunspear")) && specialWeakness == Combat.BLISTERWOOD_WEAKNESS)
                    hitChanceModifier += 50.00;
            }
            if ((attackStyle == Combat.BOLT_STYLE || attackStyle == Combat.ARROW_STYLE)) {
                ItemDefinitions weaponDefs = ItemDefinitions.getItemDefinitions(mainHand ? player.getEquipment().getWeaponId() : player.getEquipment().getShieldId());
                boolean usesAmmo = player.getEquipment().getAmmoId() != -1 && weaponDefs != null && weaponDefs.getCSOpcode(2940) == 0;
                if (usesAmmo) {
                    String ammoName = player.getEquipment().getItem(Equipment.SLOT_ARROWS).getName().toLowerCase();
                    if ((ammoName.contains("dragonbane") && specialWeakness == Combat.BANE_AMMUNITION_WEAKNESS) || (ammoName.contains("abyssalbane") && (defs.getName().toLowerCase().contains("abyssal leech") || defs.getName().toLowerCase().contains("abyssal guardian") || defs.getName().toLowerCase().contains("abyssal walker"))) || (ammoName.contains("basiliskbane") && defs.getName().toLowerCase().contains("basilisk")) || (ammoName.contains("wallasalkibane") && defs.getName().toLowerCase().contains("wallasalki")))
                        hitChanceModifier += 30.00;
                }
            }
        }
        if (player.getLastReaperNecklaceEffect() >= Utils.currentTimeMillis())
            hitChanceModifier += player.getReaperHitChanceBoost();
        if (player.getTemporaryModifiersManager().hasActiveModifier(Key.HIT_CHANCE_MODIFIER))
            hitChanceModifier += player.getTemporaryModifiersManager().getModifier(Key.HIT_CHANCE_MODIFIER);
        Object specAccuracy = player.getTemporaryAttributtes().get(Key.SPEC_ACCURACY_BOOST);
        if (specAccuracy instanceof Number)
            hitChanceModifier += ((Number) specAccuracy).doubleValue();
        return hitChanceModifier;
    }

    public static double getDamageModifier(Player player, Entity target, int attackStyle, boolean mainHand) {
        if (Settings.RS2_COMBAT) {
            return getRs2DamageModifier(player, target, attackStyle, mainHand);
        }
        double damageModifier = 0.00;
        int attackType = Combat.getStyleType(attackStyle);
        VoidType voidType = attackType == Combat.ALL_TYPE ? null : CombatUtils.getWornVoidType(player, attackType == Combat.MELEE_TYPE ? VoidCombatType.MELEE : attackType == Combat.RANGE_TYPE ? VoidCombatType.RANGER : VoidCombatType.MAGE);
        if (voidType != null)
            switch (voidType) {
            case REGULAR_VOID:
            case ELITE_VOID:
                damageModifier += 0.05;
                break;
            case SUPERIOR_ELITE_VOID:
            case SUPERIOR_VOID:
                damageModifier += 0.07;
                break;
            }
        if (target instanceof NPC) {
            NPCDefinitions defs = ((NPC) target).getDefinitions();
            if (attackType == Combat.MELEE_TYPE && player.getAuraManager().getMeleeDamageMultiplier() != 1)
                damageModifier += (player.getAuraManager().getMeleeDamageMultiplier() - 1.00);
            if (attackType == Combat.RANGE_TYPE && player.getAuraManager().getRangeDamageMultiplier() != 1)
                damageModifier += (player.getAuraManager().getRangeDamageMultiplier() - 1.00);
            if (attackType == Combat.MAGIC_TYPE && player.getAuraManager().getMagicDamageMultiplier() != 1)
                damageModifier += (player.getAuraManager().getMagicDamageMultiplier() - 1.00);

            int specialWeakness = defs == null ? -1 : defs.getSpecialWeaknesses();
            if (mainHand && player.getEquipment().getWeaponId() != -1) {
                String weaponName = player.getEquipment().getItem(Equipment.SLOT_WEAPON).getName().toLowerCase();
                if (weaponName.contains("keris") && specialWeakness == Combat.KERIS_DAGGER_WEAKNESS) {
                    boolean hasDesertAmulet = player.getEquipment().getAmuletId() == 27095 || player.getEquipment().getAmuletId() == 27096;
                    damageModifier += Math.random() <= (hasDesertAmulet ? 0.05 : 0.025) ? 2.00 : 0.333;
                }
                if ((weaponName.contains("darklight") || weaponName.contains("silverlight")) && specialWeakness == Combat.SILVER_LIGHT_WEAKNESS)
                    damageModifier += 1.00;
                if ((weaponName.contains("balmung")) && specialWeakness == Combat.BALMUNG_WEAKNESS)
                    damageModifier += weaponName.equalsIgnoreCase("balmung") ? 0.25 : 0.45;
                if (weaponName.contains("hexhunter bow") && Combat.getStyleType(defs.getWeaknessStyle()) == Combat.RANGE_TYPE)
                    damageModifier += 0.125;
                if ((weaponName.contains("blisterwood") || weaponName.contains("sunspear")) && specialWeakness == Combat.BLISTERWOOD_WEAKNESS)
                    damageModifier += Math.random() <= 0.05 ? 1.00 : 0;
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getEquipType() != 5)
                    damageModifier += 0.25;
            }
            if (specialWeakness == Combat.SALVE_AMULET_WEAKNESS) {
                Perk undeadSlayer = player.getInventionManager().hasPerk(Perks.UNDEAD_SLAYER);
                if (undeadSlayer != null)
                    damageModifier += 0.07;
                Perk undeadBait = player.getInventionManager().hasPerk(Perks.UNDEAD_BAIT);
                if (undeadBait != null)
                    damageModifier -= 0.30;
            }
            if (specialWeakness == Combat.BANE_AMMUNITION_WEAKNESS) {
                Perk dragonSlayer = player.getInventionManager().hasPerk(Perks.DRAGON_SLAYER);
                if (dragonSlayer != null)
                    damageModifier += 0.07;
                Perk dragonBait = player.getInventionManager().hasPerk(Perks.DRAGON_BAIT);
                if (dragonBait != null)
                    damageModifier -= 0.30;
            }
            if (specialWeakness == Combat.SILVER_LIGHT_WEAKNESS) {
                Perk demonSlayer = player.getInventionManager().hasPerk(Perks.DEMON_SLAYER);
                if (demonSlayer != null)
                    damageModifier += 0.07;
                Perk demonBait = player.getInventionManager().hasPerk(Perks.DEMON_BAIT);
                if (demonBait != null)
                    damageModifier -= 0.30;
            }
            if ((attackStyle == Combat.BOLT_STYLE || attackStyle == Combat.ARROW_STYLE)) {
                ItemDefinitions weaponDefs = ItemDefinitions.getItemDefinitions(mainHand ? player.getEquipment().getWeaponId() : player.getEquipment().getShieldId());
                boolean usesAmmo = player.getEquipment().getAmmoId() != -1 && weaponDefs != null && weaponDefs.getCSOpcode(2940) == 0;
                if (usesAmmo) {
                    String ammoName = player.getEquipment().getItem(Equipment.SLOT_ARROWS).getName().toLowerCase();
                    if ((ammoName.contains("dragonbane") && specialWeakness == Combat.BANE_AMMUNITION_WEAKNESS) || (ammoName.contains("abyssalbane") && (defs.getName().toLowerCase().contains("abyssal leech") || defs.getName().toLowerCase().contains("abyssal guardian") || defs.getName().toLowerCase().contains("abyssal walker"))) || (ammoName.contains("basiliskbane") && defs.getName().toLowerCase().contains("basilisk")) || (ammoName.contains("wallasalkibane") && defs.getName().toLowerCase().contains("wallasalki")))
                        damageModifier += 0.40;
                }
            }
            boolean isOnSlayerTask = player.getTask() != null && (SlayerTask.doCustomSlayerNPC(player, (NPC) target) || ((NPC) target).getDefinitions().name.toLowerCase().contains(player.getTask().getName(player).toLowerCase()));
            if (isOnSlayerTask) {
                if (attackType == Combat.RANGE_TYPE && player.getEquipment().getHatId() == 15490)
                    damageModifier += 0.125;
                if (attackType == Combat.MAGIC_TYPE && player.getEquipment().getHatId() == 15488)
                    damageModifier += 0.125;
                if (attackType == Combat.MELEE_TYPE && player.getEquipment().getHatId() != -1 && player.getEquipment().getItem(Equipment.SLOT_HAT).getDefinitions().getName().toLowerCase().contains("black mask"))
                    damageModifier += 0.125;
                if (attackType != Combat.ALL_TYPE)
                    damageModifier += SlayerHelmet.getSlayerBoost(player, attackType == Combat.MELEE_TYPE ? Skills.ATTACK : attackType == Combat.RANGE_TYPE ? Skills.RANGE : Skills.MAGIC) - 1.00;
                Perk genocidal = player.getInventionManager().hasPerk(Perks.GENOCIDAL);
                if (genocidal != null) {
                    Master master = player.getTask().getMaster();
                    int minAmount = (int) master.getData()[player.getTask().getTaskId()][3];
                    double mod = Math.min(0.07, ((double) (minAmount - player.getTask().getTaskAmount()) / (double) minAmount) * 0.07);
                    if (mod <= 0)
                        mod = 0;
                    damageModifier += mod;
                }
            }
            if (player.getContract() != null) {
                if (!player.getContract().hasCompleted()) {
                    final String npcName = ContractHandler.isNpcContract(player) ? NPCDefinitions.getNPCDefinitions(player.getContract().getNpcId()).getName().toLowerCase() : "null";
                    final NPC n = (NPC) target;
                    if (!npcName.equals("null") && n.getDefinitions().name.toLowerCase().contains(npcName)) {
                        if (hasReaperHood(player)) {
                            damageModifier += 0.15;
                        }
                    }
                }
            }
            if (player.getPerkManager() != null && player.getPerkManager().hasPerkActive(DonationPerk.THE_EXTERMINATOR) && player.getControlerManager() != null && player.getControlerManager().getControler() instanceof PestControlGame)
                damageModifier += 0.25;

            if (player.getCurrentPet() != null && player.getCurrentPet().getPerks().contains(PetPerk.POWER_EXCHANGE)) {
                int tier = PetPerkUtils.getPerkTier(PetPerk.POWER_EXCHANGE, player.getCurrentPet());
                double multiplier = (tier == 1 ? 0.1 : tier == 2 ? 0.2 : tier == 3 ? 0.25 : 0);
                damageModifier += multiplier;
            }
            Perk shieldBashing = player.getInventionManager().hasPerk(Perks.SHIELD_BASHING);
            if (shieldBashing != null && player.getEquipment().hasShield() && player.getEquipment().getItem(Equipment.SLOT_SHIELD).getDefinitions().getCombatMap() == null)
                damageModifier += 0.05 * shieldBashing.getRank();
        }
        if (Combat.fullDharokEquipped(player)) {
            double maxhp = player.getMaxHitpoints();
            double hp = maxhp - player.getHitpoints();
            if (hp > 0) {
                damageModifier += (hp / maxhp) >= 0.55 ? 0.55 : (hp / maxhp);
            }
        }
        if (Combat.fullVeracsEquipped(player)) {
            damageModifier += Utils.getRandom(4) == 0 ? ThreadLocalRandom.current().nextDouble(0.05, 0.15) : 0;
        }
//        if(Combat.fullVanguardEquipped(player) || Combat.fullTricksterEquipped(player) || Combat.fullBattlemageEquipped(player))
//            damageModifier += 0.1;
        if (attackType == Combat.MELEE_TYPE) {
            damageModifier += Combat.getVanguardDamageModifier(player);
        } else if (attackType == Combat.RANGE_TYPE) {
            damageModifier += Combat.getTricksterDamageModifier(player);
        } else if (attackType == Combat.MAGIC_TYPE) {
            damageModifier += Combat.getBattleMageDamageModifier(player);
        }
        if (hasDragonBattleAxeBuff(player))
            damageModifier += 0.20;
        if (player.getTemporaryModifiersManager().hasActiveModifier(Key.DAMAGE_DEALT_MODIFIER))
            damageModifier += player.getTemporaryModifiersManager().getModifier(Key.DAMAGE_DEALT_MODIFIER);
        if (target.getTemporaryModifiersManager().hasActiveModifier(Key.DAMAGE_RECIEVED_MODIFIER))
            damageModifier += target.getTemporaryModifiersManager().getModifier(Key.DAMAGE_RECIEVED_MODIFIER);
        damageModifier += player.getPrayer().getDamageMultiplier(attackType);
        damageModifier -= Combat.getDamageDebuff(player, attackType);
        damageModifier += Settings.static_damage_buff;// static damage modifier to all combat stats
        Perk mediocrity = player.getInventionManager().hasPerk(Perks.MEDIOCRITY);
        if (mediocrity != null)
            damageModifier -= 0.03 * mediocrity.getRank();
        if (mainHand && ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getEquipType() == 5)
            damageModifier += Settings.twohand_combat_dmg_modifier;
        else
            damageModifier += Settings.dual_combat_dmg_modifier;
        if (target instanceof Player)
            damageModifier += Settings.pvp_combat_dmg_modifier;
        return damageModifier;
    }

    public static double getAccuracyModifier(Player player, Entity target, int attackStyle, boolean mainHand) {
        if (Settings.RS2_COMBAT) {
            return getRs2AccuracyModifier(player, target, attackStyle, mainHand);
        }
        double accuracyModifier = 1.00;
        int attackType = Combat.getStyleType(attackStyle);
        if (player.getEquipment().getAmuletId() == 33671)
            accuracyModifier += 0.01;
        // scrimshaws TODO
        VoidType voidType = attackType == Combat.ALL_TYPE ? null : CombatUtils.getWornVoidType(player, attackType == Combat.MELEE_TYPE ? VoidCombatType.MELEE : attackType == Combat.RANGE_TYPE ? VoidCombatType.RANGER : VoidCombatType.MAGE);
        if (voidType != null)
            accuracyModifier += 0.03;
        if (CombatUtils.hasDefenderOrEquivalentEffect(player))
            accuracyModifier += player.getTemporaryAttributtes().remove(Key.DEFENDERS_PASSIVE_ACCURACY) != null ? 0.2 : 0.03;
        if (target instanceof NPC) {
            NPCDefinitions defs = ((NPC) target).getDefinitions();
            if (attackType == Combat.MELEE_TYPE && player.getAuraManager().getMeleeAccuracyMultiplier() != 1)
                accuracyModifier += (player.getAuraManager().getMeleeAccuracyMultiplier() - 1.00);
            if (attackType == Combat.RANGE_TYPE && player.getAuraManager().getRangeAccurayMultiplier() != 1)
                accuracyModifier += (player.getAuraManager().getRangeAccurayMultiplier() - 1.00);
            if (attackType == Combat.MAGIC_TYPE && player.getAuraManager().getMagicAccurayMultiplier() != 1)
                accuracyModifier += (player.getAuraManager().getMagicAccurayMultiplier() - 1.00);
            boolean isOnSlayerTask = player.getTask() != null && (SlayerTask.doCustomSlayerNPC(player, (NPC) target) || defs.name.toLowerCase().contains(player.getTask().getName(player).toLowerCase()));
            if (isOnSlayerTask) {
                if (attackType == Combat.RANGE_TYPE && player.getEquipment().getHatId() == 15490)
                    accuracyModifier += 0.125;
                if (attackType == Combat.MAGIC_TYPE && player.getEquipment().getHatId() == 15488)
                    accuracyModifier += 0.125;
                if (attackType == Combat.MELEE_TYPE && player.getEquipment().getHatId() != -1 && player.getEquipment().getItem(Equipment.SLOT_HAT).getDefinitions().getName().toLowerCase().contains("black mask"))
                    accuracyModifier += 0.125;
                if (attackType != Combat.ALL_TYPE)
                    accuracyModifier += SlayerHelmet.getSlayerBoost(player, attackType == Combat.MELEE_TYPE ? Skills.ATTACK : attackType == Combat.RANGE_TYPE ? Skills.RANGE : Skills.MAGIC) - 1.00;
            }
            int specialWeakness = defs == null ? -1 : defs.getSpecialWeaknesses();
            if (specialWeakness == Combat.SALVE_AMULET_WEAKNESS) {
                int amuletId = player.getEquipment().getAmuletId();
                accuracyModifier += amuletId == 4081 ? 0.15 : amuletId == 10588 ? 0.20 : 0;
            }
            if (player.getContract() != null) {
                if (!player.getContract().hasCompleted()) {
                    final String npcName = ContractHandler.isNpcContract(player) ? NPCDefinitions.getNPCDefinitions(player.getContract().getNpcId()).getName().toLowerCase() : "null";
                    final NPC n = (NPC) target;
                    if (!npcName.equals("null") && n.getDefinitions().name.toLowerCase().contains(npcName)) {
                        if (hasReaperHood(player)) {
                            accuracyModifier += 0.15;
                        }
                    }
                }
            }
            Perk clearHeaded = player.getInventionManager().hasPerk(Perks.CLEAR_HEADED);
            if (clearHeaded != null)
                accuracyModifier += 0.005 * clearHeaded.getRank();
            Perk lunging = player.getInventionManager().hasPerk(Perks.LUNGING);
            if (lunging != null)
                accuracyModifier += 0.01 * lunging.getRank();
        }
        if (player.getFamiliar() != null) {
            if (attackType == Combat.MELEE_TYPE && player.getFamiliar() instanceof Bloodnihil)
                accuracyModifier += 0.05;
            if (attackType == Combat.RANGE_TYPE && player.getFamiliar() instanceof Shadownihil)
                accuracyModifier += 0.05;
            if (attackType == Combat.MAGIC_TYPE && (player.getFamiliar() instanceof Smokenihil || player.getFamiliar() instanceof Icenihil))
                accuracyModifier += 0.05;
        }
        return accuracyModifier;
    }

    /*
     * RS2-mode damage modifier. Mirrors getDamageModifier above but only
     * includes 2009-era effects: void (with authentic style-specific values),
     * salve amulet damage, slayer helm/black mask on task, Dharok's set,
     * Verac's set, weapon-specific weakness multipliers, and the dragon
     * battle axe special. Modern post-2009 stacks (auras, invention perks,
     * donor perks, vanguard/trickster/battlemage gear, Reaper Hood/Necklace,
     * static_damage_buff, two-hand/dual-wield/PvP global modifiers, bane
     * ammunition damage) are intentionally excluded so the RS2 path produces
     * authentic pre-EOC numbers. Prayer is applied inside Rs2AtaraxiaNumerics
     * via the effective-level path, so it isn't added here either.
     */
    public static double getRs2DamageModifier(Player player, Entity target, int attackStyle, boolean mainHand) {
        double damageModifier = 0.00;
        int attackType = Combat.getStyleType(attackStyle);

        // 2009scape void: +10% melee dmg, +20% ranged dmg, no magic dmg (magic
        // void grants accuracy instead, applied in getRs2AccuracyModifier).
        VoidType voidType = attackType == Combat.ALL_TYPE ? null
                : CombatUtils.getWornVoidType(player, attackType == Combat.MELEE_TYPE ? VoidCombatType.MELEE
                        : attackType == Combat.RANGE_TYPE ? VoidCombatType.RANGER : VoidCombatType.MAGE);
        if (voidType != null && attackType != Combat.MAGIC_TYPE) {
            double meleeRanged = attackType == Combat.RANGE_TYPE ? 0.20 : 0.10;
            switch (voidType) {
                case REGULAR_VOID:
                case ELITE_VOID:
                    damageModifier += meleeRanged;
                    break;
                case SUPERIOR_VOID:
                case SUPERIOR_ELITE_VOID:
                    damageModifier += meleeRanged + 0.03; // small bump for superior tier
                    break;
            }
        }

        if (target instanceof NPC) {
            NPCDefinitions defs = ((NPC) target).getDefinitions();
            int specialWeakness = defs == null ? -1 : defs.getSpecialWeaknesses();

            // Era-authentic weapon vs monster bonuses (Keris, Darklight,
            // Silverlight, Balmung, Sunspear, Iban Blast).
            if (mainHand && player.getEquipment().getWeaponId() != -1) {
                String weaponName = player.getEquipment().getItem(Equipment.SLOT_WEAPON).getName().toLowerCase();
                if (weaponName.contains("keris") && specialWeakness == Combat.KERIS_DAGGER_WEAKNESS) {
                    boolean hasDesertAmulet = player.getEquipment().getAmuletId() == 27095 || player.getEquipment().getAmuletId() == 27096;
                    damageModifier += Math.random() <= (hasDesertAmulet ? 0.05 : 0.025) ? 2.00 : 0.333;
                }
                if ((weaponName.contains("darklight") || weaponName.contains("silverlight")) && specialWeakness == Combat.SILVER_LIGHT_WEAKNESS)
                    damageModifier += 1.00;
                if (weaponName.contains("balmung") && specialWeakness == Combat.BALMUNG_WEAKNESS)
                    damageModifier += weaponName.equalsIgnoreCase("balmung") ? 0.25 : 0.45;
                if ((weaponName.contains("blisterwood") || weaponName.contains("sunspear")) && specialWeakness == Combat.BLISTERWOOD_WEAKNESS)
                    damageModifier += Math.random() <= 0.05 ? 1.00 : 0;
            }

            // Salve amulet vs undead: 2009scape applies +15%/+20% to both
            // accuracy and damage. Ataraxia previously only had the accuracy
            // half - this restores the missing damage half.
            if (specialWeakness == Combat.SALVE_AMULET_WEAKNESS) {
                int amuletId = player.getEquipment().getAmuletId();
                if (amuletId == 4081)
                    damageModifier += 0.15;
                else if (amuletId == 10588)
                    damageModifier += 0.20;
            }

            // Slayer helm / black mask on task. Cap to the +12.5% legacy
            // bonus and skip the post-2009 SlayerHelmet.getSlayerBoost ladder.
            boolean isOnSlayerTask = player.getTask() != null
                    && (SlayerTask.doCustomSlayerNPC(player, (NPC) target)
                            || ((NPC) target).getDefinitions().name.toLowerCase().contains(player.getTask().getName(player).toLowerCase()));
            if (isOnSlayerTask) {
                if (attackType == Combat.RANGE_TYPE && player.getEquipment().getHatId() == 15490)
                    damageModifier += 0.125;
                if (attackType == Combat.MAGIC_TYPE && player.getEquipment().getHatId() == 15488)
                    damageModifier += 0.125;
                if (attackType == Combat.MELEE_TYPE && player.getEquipment().getHatId() != -1
                        && player.getEquipment().getItem(Equipment.SLOT_HAT).getDefinitions().getName().toLowerCase().contains("black mask"))
                    damageModifier += 0.125;
            }
        }

        // Era-authentic Barrows set effects.
        if (Combat.fullDharokEquipped(player)) {
            double maxhp = player.getMaxHitpoints();
            double hp = maxhp - player.getHitpoints();
            if (hp > 0) {
                damageModifier += (hp / maxhp) >= 0.55 ? 0.55 : (hp / maxhp);
            }
        }
        if (Combat.fullVeracsEquipped(player)) {
            damageModifier += Utils.getRandom(4) == 0 ? ThreadLocalRandom.current().nextDouble(0.05, 0.15) : 0;
        }

        // Dragon battle axe special (classic spec, period-appropriate).
        if (hasDragonBattleAxeBuff(player))
            damageModifier += 0.20;

        return damageModifier;
    }

    /*
     * RS2-mode accuracy modifier. Includes 2009-era multipliers only: magic
     * void (+30% accuracy), salve amulet (+15%/+20%), slayer helm/black mask
     * on task (+12.5%). Modern auras, invention/donor perks, contract
     * Reaper Hood, and familiar accuracy bonuses are excluded.
     */
    public static double getRs2AccuracyModifier(Player player, Entity target, int attackStyle, boolean mainHand) {
        double accuracyModifier = 1.00;
        int attackType = Combat.getStyleType(attackStyle);

        VoidType voidType = attackType == Combat.ALL_TYPE ? null
                : CombatUtils.getWornVoidType(player, attackType == Combat.MELEE_TYPE ? VoidCombatType.MELEE
                        : attackType == Combat.RANGE_TYPE ? VoidCombatType.RANGER : VoidCombatType.MAGE);
        if (voidType != null && attackType == Combat.MAGIC_TYPE) {
            // 2009scape gives magic void +30% accuracy (no damage).
            accuracyModifier += 0.30;
        }

        if (target instanceof NPC) {
            NPCDefinitions defs = ((NPC) target).getDefinitions();
            int specialWeakness = defs == null ? -1 : defs.getSpecialWeaknesses();

            if (specialWeakness == Combat.SALVE_AMULET_WEAKNESS) {
                int amuletId = player.getEquipment().getAmuletId();
                if (amuletId == 4081)
                    accuracyModifier += 0.15;
                else if (amuletId == 10588)
                    accuracyModifier += 0.20;
            }

            boolean isOnSlayerTask = player.getTask() != null
                    && (SlayerTask.doCustomSlayerNPC(player, (NPC) target)
                            || defs.name.toLowerCase().contains(player.getTask().getName(player).toLowerCase()));
            if (isOnSlayerTask) {
                if (attackType == Combat.RANGE_TYPE && player.getEquipment().getHatId() == 15490)
                    accuracyModifier += 0.125;
                if (attackType == Combat.MAGIC_TYPE && player.getEquipment().getHatId() == 15488)
                    accuracyModifier += 0.125;
                if (attackType == Combat.MELEE_TYPE && player.getEquipment().getHatId() != -1
                        && player.getEquipment().getItem(Equipment.SLOT_HAT).getDefinitions().getName().toLowerCase().contains("black mask"))
                    accuracyModifier += 0.125;
            }
        }

        return accuracyModifier;
    }

    public static boolean hasDragonBattleAxeBuff(Player player) {
        if (player.getTemporaryAttributtes().get(Key.D_BATTLEAXE_SPECIAL) == null)
            return false;
        long timeRemaining = (long) player.getTemporaryAttributtes().get(Key.D_BATTLEAXE_SPECIAL);
        return timeRemaining >= Utils.currentTimeMillis();
    }

    public static boolean hasReaperHood(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        return helmId == 11789;
    }

    public static boolean hasReaperNecklace(Player player) {
        int amuletId = player.getEquipment().getAmuletId();
        String amuletName = amuletId == -1 ? "" : player.getEquipment().getItem(Equipment.SLOT_AMULET).getName().toLowerCase();
        return !amuletName.contains("(broken)") && amuletName.contains("reaper necklace");
    }

    public static boolean pressFreedom(Player player) {
        Item shield = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
        if (!player.isDev() && !player.getEliteDungeonsManager().isInside() && (shield == null || shield.getDefinitions().getEquipmentOptions()[3] == null || !shield.getDefinitions().getEquipmentOptions()[3].equalsIgnoreCase("freedom"))) {
            player.getPackets().sendGameMessage("You need to have a shield with freedom ability to do this.");
            return false;
        }
        if (player.getEliteDungeonsManager().isInside()) {
            player.getEffectsManager().startEffect(new Effect(EffectType.FREEDOM, 10));
            return true;
        }
        player.setNextAnimation(new Animation(18070));
        player.setFreedomCooldown();
        return true;
    }

    public static void pressSurgeEscape(Player player, boolean surge) {
        if (!canSurge(player))
            return;

        final WorldTile tile = surge ? getSurgeTile(player, 0, 10, true) : getSurgeTile(player, 0, -10, false);
        if (tile == null || tile.matches(player)) {
            player.getPackets().sendGameMessage("Destination unreachable.");
            return;
        }
        byte[] dirs = Utils.getDirection(player.getDirection());
        player.lock(2);
        player.setSurgeEscapeCooldown();
        player.setNextAnimation(new Animation(18358));
        player.setNextGraphics(new Graphics(3537, 5, 0));
        player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngle(tile.getX() - player.getX(), tile.getY() - player.getY())));
        player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
        player.setNextFaceWorldTile(tile.transform(dirs[0], dirs[1], 0));

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextWorldTile(tile);
            }
        }, 1);
    }

    private static boolean canSurge(Player player) {
        if (!player.canUseSurgeEscape()) {
            player.getPackets().sendGameMessage("That ability is under cooldown. seconds remaining: " + TimeUnit.MILLISECONDS.toSeconds(player.getSurgeEscapeCooldown()));
            return false;
        }
        if (player.isLocked() || player.getFreezeDelay() >= Utils.currentTimeMillis())
            return false;
        Controller c = player.getControlerManager().getControler();
        boolean surgeDisabled = c == null || (c != null && !(c instanceof EliteDungeonController));
        if (surgeDisabled)
            player.getPackets().sendGameMessage("You are in an area which is preventing you from performing that ability.");
        return !surgeDisabled;
    }

    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment) {
        return getSurgeTile(player, start, end, increment, null);
    }

    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment, Entity target) {
        byte[] dirs = Utils.getDirection(player.getDirection());
        WorldTile lastStep = null;
        for (int steps = start; increment ? steps < end : steps > end; steps += (increment ? 1 : -1)) {
            WorldTile step = new WorldTile(player.getX() + (dirs[0] * steps), player.getY() + (dirs[1] * steps), player.getPlane());
            if (target != null && Utils.colides(target.getX(), target.getY(), target.getSize(), step.getX(), step.getY(), player.getSize()) || !player.getControlerManager().addWalkStep(player.getX(), player.getY(), step.getX(), step.getY())
            /* || !player.clipedProjectile(step, true) */
                    || !World.isTileFree(step.getPlane(), step.getX(), step.getY(), player.getSize()) || !World.canMoveNPC(step, player.getSize()))
                break;

            lastStep = step;
        }
        return lastStep;
    }

}