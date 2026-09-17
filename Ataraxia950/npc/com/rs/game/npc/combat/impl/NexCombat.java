package com.rs.game.npc.combat.impl;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.Colour;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.ZarosGodwars;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.godwars.zaros.Nex.NexPhase;
import com.rs.game.player.Player;
import com.rs.game.player.cutscenes.NexCutScene;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NexCombat extends CombatScript {

    /**
     * The teleport coordinates used during nex's "no escape" attack.
     */
    public static final WorldTile[] NO_ESCAPE_TELEPORTS = { new WorldTile(0, 10, 0), // north
            new WorldTile(10, 0, 0), // east,
            new WorldTile(0, -10, 0), // south
            new WorldTile(-10, 0, 0), }; // west

    @Override
    public Object[] getKeys() {
        return new Object[] { 13447, 13448, 13449, 13450 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof Nex))
            return 0;
        final Nex nex = (Nex) npc;
        if (nex == null || target == null)
            return 0;
        nex.setForceFollowClose(Utils.random(2) == 0);
        boolean hasDistance = false;
        int distanceX = target.getX() - npc.getX();
        int distanceY = target.getY() - npc.getY();
        int size = npc.getDefinitions().size;
        if (distanceX > size + 1 || distanceX < -1 || distanceY > size + 1 || distanceY < -1)
            hasDistance = true;
        nex.resetLastAttack();
        if (nex.isSiphioning() || nex.isFlying())
            return 0;
        switch (nex.getInstance() == null ? ZarosGodwars.nex.getCurrentPhase().getPhaseValue() : nex.getInstance().getCurrentPhase().getPhaseValue()) {
        case 1: // Smoke
            if (nex.isFirstStageAttack()) {
                sendVirusAttack(nex);
                nex.setFirstStageAttack(false);
                return nex.getAttackSpeed();
            }
            switch (hasDistance ? Utils.random(7) : Utils.random(11)) {
            case 0:
            case 1:
            case 2:
                sendMagicAttack(nex, false);
                break;
            case 3:
                if (Utils.random(1) == 2)
                    sendPullAttack(nex);
                else
                    sendMagicAttack(nex, false);
                break;
            case 4:
                if (nex.getLastVirusAttack() > Utils.currentTimeMillis())
                    return 0;
                sendVirusAttack(nex);
                break;
            case 5:
            case 6:
                sendMagicAttack(nex, true);
                break;
            case 7:
            case 8:
            case 9:
                sendMeleeAttack(nex, target);
                break;
            case 10:
                sendNoEscape(nex, target);
                break;
            }
            return nex.getAttackSpeed();
        case 2:// Shadow
            if (nex.isFirstStageAttack()) {
                nex.setFirstStageAttack(false);
                sendShadowTraps(nex);
            }
            switch (Utils.random(4)) {
            case 0:// shadow attack
                return sendShadowTraps(nex);
            case 1:
            case 2:
                sendRangeAttack(nex);
            default:
                if (Utils.random(5) == 0)
                    sendEmbraceDarkness(nex);
                else
                    sendRangeAttack(nex);
            }
            return nex.getAttackSpeed();
        case 3:// Blood
            if (nex.getNexAttack() == 0)
                sendSipionAttack(nex, target);
            else if (nex.getNexAttack() >= 1 && nex.getNexAttack() <= 4)
                sendNormalBloodAttack(hasDistance, nex, target);
            else if (nex.getNexAttack() == 5)
                return sendBloodSacrifice(nex, target);
            else if (nex.getNexAttack() >= 6 && nex.getNexAttack() <= 10)
                sendNormalBloodAttack(hasDistance, nex, target);
            else if (nex.getNexAttack() == 11) {
                nex.resetNexAttack();
                sendSipionAttack(nex, target);
            }
            nex.incrementNexAttack();
            return nex.getAttackSpeed();
        case 4:// Ice
            if (nex.getNexAttack() == 0)
                sendIcePrison(nex, true);
            else if (nex.getNexAttack() >= 1 && nex.getNexAttack() <= 5 || nex.getNexAttack() >= 7 && nex.getNexAttack() <= 9)
                sendNormalIceAttacks(hasDistance, nex, target);
            else if (nex.getNexAttack() == 6)
                sendIceBarricade(nex);
            if (nex.getNexAttack() == 10) {
                nex.resetNexAttack();
                sendIcePrison(nex, true);
            }
            nex.incrementNexAttack();
            return nex.getAttackSpeed();
        case 5:// POZ (Power of Zaros)
            switch (hasDistance ? Utils.random(3) : Utils.random(11) + 3) {
            case 0:
            case 1:
                sendMagicAttack(nex, false);
                break;
            case 2:
                sendMagicAttack(nex, true);
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                sendMeleeAttack(nex, target);
                break;
            case 13:
                sendNoEscape(nex, target);
                break;
            }
            return nex.getAttackSpeed();
        }
        return 0;
    }

    private void sendNormalIceAttacks(boolean hasDistance, final Nex nex, final Entity target) {
        switch (hasDistance ? 0 : Utils.random(3)) {
        case 0:
            sendIceBarrage(nex);
            break;
        case 1:
        case 2:
            sendMeleeAttack(nex, target);
            break;
        }
    }

    /**
     * Send's nex's ice attack.
     *
     * @param nex The instance of {@code Nex} being used.
     */
    private void sendIceBarrage(Nex nex) {
        boolean usingPrayer = false;
        nex.setNextAnimation(new Animation(17414));
        nex.setNextGraphics(new Graphics(3375));
        for (final Entity possibleTarget : nex.getPossibleTargets()) {
            World.sendProjectile(nex, possibleTarget, 362, 20, 20, 20, 2, 10, 0);
            int damage = getRandomMaxHit(nex, nex.getCombatDefinitions().getMaxHit(), NPCCombatDefinitionConstants.MAGE, possibleTarget);
            delayHit(nex, 1, possibleTarget, getMagicHit(nex, damage));
            if (damage > 0) {
                if (possibleTarget instanceof Player) {
                    final Player player = (Player) possibleTarget;
                    player.getPrayer().drainPrayer(damage / 4);
                    if (player.getPrayer().isMageProtecting())
                        usingPrayer = true;
                }
                final boolean finalPrayer = usingPrayer;
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (Utils.random(finalPrayer ? 6 : 3) == 0) {
                            possibleTarget.setNextGraphics(new Graphics(369));
                        }
                    }
                }, 2);
            }
        }
    }

    /**
     * Traps a player within an ice boundary.
     *
     * @param nex   The instance of {@code Nex} being used.
     * @param start If the target is dead or a null to choose another target.
     */
    private void sendIcePrison(final Nex nex, boolean start) {
        if (start) {
            nex.setNextForceTalk(new ForceTalk("Die now, in a prison of ice!"));
            nex.playSoundEffect(3308);
            nex.setNextAnimation(new Animation(17414));
            nex.setNextGraphics(new Graphics(3375));
        }
        ArrayList<Player> players = new ArrayList<Player>();
        for (Player player : nex.getInstance() == null ? ZarosGodwars.getPlayers() : nex.getInstance().getPlayers()) {
            if (!player.withinDistance(nex, 14))
                continue;
            players.add(player);
        }
        if (players.size() > 0) {
            final Player player = players.get(Utils.random(players.size()));
            if (player == null || player.isDead()) {
                sendIcePrison(nex, false);
                return;
            }
            player.getTemporaryAttributtes().remove("insideIcePrison");
            World.sendProjectile(nex, player, 362, 20, 20, 20, 2, 10, 0);
            final WorldTile base = player;
            player.resetWalkSteps();
            player.getPrayer().closeProtectionPrayers();
            player.getTemporaryAttributtes().put("insideIcePrison", Boolean.TRUE);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    final WorldTile tile = base.transform(x, y, 0);
                    final WorldObject object = new WorldObject(57263, 10, 0, tile);
                    if (!tile.matches(player) && World.canMoveNPC(tile, 1))
                        World.spawnObject(object);
                    WorldTasksManager.schedule(new WorldTask() {
                        boolean remove = false;

                        @Override
                        public void run() {
                            if (remove) {
                                if (World.containsObjectWithId(object, object.getId()))
                                    World.removeObject(object);
                                stop();
                                return;
                            }
                            remove = true;
                            if (player.getTemporaryAttributtes().get("insideIcePrison") != null && player.getX() == tile.getX() && player.getY() == tile.getY()) {
                                player.getPackets().sendGameMessage("The centre of the ice prison freezes you to the bone!");
                                player.resetWalkSteps();
                                player.getTemporaryAttributtes().remove("insideIcePrison");
                                player.applyHit(new Hit(nex, Utils.random(600, 800), HitLook.REGULAR_DAMAGE));
                            }
                        }
                    }, 8, 0);
                }
            }
        } else {
            return;
        }
    }

    /**
     * Send's a barricade made of ice fragments around nex.
     *
     * @param nex The instance of {@code Nex} being used.
     */
    private void sendIceBarricade(final Nex nex) {
        nex.getTemporaryAttributtes().put("cantMove", Boolean.TRUE);
        nex.resetWalkSteps();
        nex.setNextForceTalk(new ForceTalk("Contain this!"));
        nex.playSoundEffect(3316);
        nex.setNextAnimation(new Animation(17407));
        nex.setNextGraphics(new Graphics(3362));
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 5) {
                    int[][] tileTransforms = { { 1, 1 }, { -1, -1 }, { 1, -1 }, { -1, 1 } };
                    for (int i = 0; i < tileTransforms.length; i++) {
                        WorldTile tile = nex.transform(tileTransforms[i][0], tileTransforms[i][1], 0);
                        if (World.canMoveNPC(tile, 1))
                            World.spawnObjectTemporary(new WorldObject(57262, 10, 0, tile), 7000);
                    }
                    for (Player player : nex.getInstance() == null ? ZarosGodwars.getPlayers() : nex.getInstance().getPlayers()) {
                        if (player == null || player.isDead() || player.hasFinished() || !Utils.isOnRange(nex, player, 0))
                            continue;
                        player.setNextAnimation(new Animation(1113));
                        player.applyHit(new Hit(nex, Utils.random(150, 350), HitLook.REGULAR_DAMAGE));
                        player.getPackets().sendGameMessage("The icicle spikes you to the spot!");
                        player.getPackets().sendGameMessage("You've been injured and can't use " + (player.getPrayer().isAncientCurses() ? "deflect curses" : "protection prayers ") + "!");
                        player.resetWalkSteps();
                        player.getPrayer().closeProtectionPrayers();
                        player.setPrayerDelay(7000);
                    }
                } else if (loop == 6) {
                    nex.getTemporaryAttributtes().remove("cantMove");
                    stop();
                }
                loop++;
            }
        }, 0, 0);
    }

    /**
     * Sends the 'normal' attacks during nex's blood stage.
     *
     * @param hasDistance If the {@code Nex} has distance between the target.
     * @param nex         The instance of {@code Nex} being used.
     * @param target      The target being attacked.
     */
    private void sendNormalBloodAttack(boolean hasDistance, Nex nex, Entity target) {
        switch (hasDistance ? 0 : Utils.random(3)) {
        case 0:
            for (Player player : nex.getInstance() == null ? ZarosGodwars.getPlayers() : nex.getInstance().getPlayers()) {
                if (player.withinDistance(nex, 1))
                    continue;
                sendBloodAttack(nex, player);
            }
            break;
        case 1:
        case 2:
            sendMeleeAttack(nex, target);
            break;
        }
    }

    /**
     * Sends nex's blood sacrifice attack.
     *
     * @param nex    The instance of {@code Nex} being used.
     * @param target The entity chosen to be sacrificed.
     * @return The delay of the attack.
     */
    private int sendBloodSacrifice(final Nex nex, Entity target) {
        nex.setNextForceTalk(new ForceTalk("I demand a blood sacrifice!"));
        nex.playSound(3293, 2);
        if (target instanceof Player) {
            final Player player = (Player) target;
            player.sendMessage("<col=480000>Nex has marked you as a sacrifice; RUN!");
            player.setNextColour(new Colour(0, 240, 50, 110, 70, 130));
            player.getTemporaryAttributtes().put("bloodSacrificeTarget", nex);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (player.getTemporaryAttributtes().get("bloodSacrificeTarget") != null) {
                        player.getTemporaryAttributtes().remove("bloodSacrificeTarget");
                        player.setNextColour(new Colour(0, 0, 0, 0, 0, 0));
                        player.sendMessage("You didn't make it far enough in time - Nex fires a punishing attack!");
                        nex.setNextAnimation(new Animation(17414));
                        nex.setNextGraphics(new Graphics(3375));
                        int maxHp = player.getMaxHitpoints() > 1500 ? Math.min(990, player.getMaxHitpoints()) : player.getMaxHitpoints();
                        nex.heal(maxHp, 0, 0, true);
                        for (Entity other : nex.getPossibleTargets()) {
                            if (other instanceof Player) {
                                Player plr = (Player) other;
                                sendBloodAttack(nex, plr);
                                WorldTasksManager.schedule(new WorldTask() {
                                    @Override
                                    public void run() {
                                        if (plr.getPrayer().getPrayerpoints() > 0)
                                            plr.getPrayer().drainPrayer(plr.getPrayer().getPrayerpoints() / 2);
                                        if (plr == player) {
                                            plr.applyHit(new Hit(nex, (int) (maxHp * 0.10), HitLook.MAGIC_DAMAGE));
                                        }
                                    }
                                }, 2);
                            }
                        }
                    }
                }
            }, 8);
            nex.incrementNexAttack();
            return nex.getCombatDefinitions().getAttackDelay() * 2;
        } else {
            nex.setNexAttack(5);
            return -1;
        }
    }

    /**
     * Sends a blood barrage during the blood stage.
     *
     * @param nex    The instance of {@code Nex} being used.
     * @param target The target being attacked.
     */
    private void sendBloodAttack(final Nex nex, final Entity target) {
        final int damage = getRandomMaxHit(nex, 290, NPCCombatDefinitionConstants.MAGE, target);
        nex.setNextAnimation(new Animation(17414));
        nex.setNextGraphics(new Graphics(3375));
        World.sendProjectile(nex, target, 374, 20, 20, 20, 2, 0, 0);
        delayHit(nex, 2, target, getMagicHit(nex, damage));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                target.setNextGraphics(new Graphics(376));
                nex.heal(damage / 4);
            }
        }, 2);
    }

    /**
     * Begin's nex's siphion attack during the blood stage.
     *
     * @param nex The instance of {@code Nex} being used.
     */
    private void sendSipionAttack(final Nex nex, Entity target) {
        nex.setNextForceTalk(new ForceTalk("A siphon will solve this!"));
        nex.playSound(3317, 2);
        nex.setNextAnimation(new Animation(17409));
        nex.setNextGraphics(new Graphics(3370));
        nex.setSiphioning(true);
        int size = NPCDefinitions.getNPCDefinitions(13458).size;
        int[][] dirs = Utils.getCoordOffsetsNear(size);
        int maximumAmount = Utils.random(3);
        int count = 0;
        if (maximumAmount != 0) {
            for (int dir = 0; dir < dirs[0].length; dir++) {
                final WorldTile tile = new WorldTile(new WorldTile(target.getX() + dirs[0][dir], target.getY() + dirs[1][dir], target.getPlane()));
                if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), size)) {
                    NPC npc = nex.getBloodReavers()[count++] = new NPC(13458, tile, -1, true, true);
                    npc.setNextGraphics(new Graphics(1315));
                }
                if (count == maximumAmount)
                    break;
            }
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                nex.setSiphioning(false);
            }
        }, 8);
    }

    private void sendEmbraceDarkness(final Nex nex) {
        nex.setNextForceTalk(new ForceTalk("Embrace darkness!"));
        nex.playSoundEffect(3322);
        nex.setNextAnimation(new Animation(17412));
        nex.setNextGraphics(new Graphics(3353));

        /*
         * WorldTasksManager.schedule(new WorldTask() {
         *
         * @Override public void run() { if (nex.hasFinished() || nex.getCurrentPhase()
         * != NexPhase.SHADOW) { for (Entity entity : nex.getPossibleTargets()) { if
         * (entity instanceof Player) { Player player = (Player) entity;
         * player.getPackets().sendGlobalConfig(1435, 255); } } stop(); return; } if
         * (Utils.random(2) == 0) { for (Entity entity : nex.getPossibleTargets()) { if
         * (entity instanceof Player) { Player player = (Player) entity; int distance =
         * Utils.getDistance(player.getX(), player.getY(), nex.getX(), nex.getY()); if
         * (distance > 30) distance = 30; player.getPackets().sendGlobalConfig(1435,
         * (distance * 255 / 30)); } } } } }, 0, 0);
         */
    }

    private void sendNoEscape(final Nex nex, final Entity target) {
        nex.setNextForceTalk(new ForceTalk("There is..."));
        nex.playSoundEffect(3294);
        nex.setCantInteract(true);
        final int index = Utils.random(NO_ESCAPE_TELEPORTS.length);
        final WorldTile dir = NO_ESCAPE_TELEPORTS[index];
        final WorldTile center = (nex.getInstance() == null ? ZarosGodwars.SPAWN : nex.getInstance().getSpawnTile());
        WorldTasksManager.schedule(new WorldTask() {
            private int count;

            @Override
            public void run() {
                if (count == 0) {
                    nex.setNextAnimation(new Animation(17411));
                    nex.setNextGraphics(new Graphics(1216));
                } else if (count == 1) {
                    WorldTile next = new WorldTile(center.getX() + dir.getX(), center.getY() + dir.getY(), center.getPlane() + dir.getPlane());
                    nex.setNextWorldTile(next);
                    nex.setNextForceTalk(new ForceTalk("NO ESCAPE!"));
                    nex.playSoundEffect(3292);
                    nex.setNextForceMovement(new ForceMovement(next, 1, center, 3, index == 3 ? 1 : index == 2 ? 0 : index == 1 ? 3 : 2));
                    for (Entity entity : nex.calculatePossibleTargets(center, next, index == 0 || index == 2)) {
                        if (entity instanceof Player) {
                            final Player player = (Player) entity;
                            player.setAttackedBy(null);
                            player.stopAll();
                            player.getCutscenesManager().play(new NexCutScene(next, index));
                            Hit hit = new Hit(nex, Utils.random(700), HitLook.REGULAR_DAMAGE);
                            if (nex.getInstance() == null ? ZarosGodwars.nex.getCurrentPhase() == NexPhase.ZAROS : nex.getInstance().getCurrentPhase() == NexPhase.ZAROS)
                                sendSoulSplit(hit, nex, entity);
                            player.applyHit(hit);
                            player.setNextAnimation(new Animation(10070));
                            player.setNextForceMovement(new ForceMovement(center, 2, index == 3 ? 3 : index == 2 ? 2 : index == 1 ? 1 : 0));
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    player.setNextWorldTile(center);
                                }
                            }, 2);
                        }
                    }
                } else if (count == 3) {
                    nex.setNextWorldTile(center);
                } else if (count == 4) {
                    nex.setCantInteract(false);
                    nex.setTarget(target);
                    stop();
                    return;
                }
                count++;
            }
        }, 0, 1);
    }

    protected void sendSoulSplit(Hit hit, Nex nex, Entity target) {
        if(!(target instanceof Player))
            return;
        if (nex.getId() == 13448) {
            if (hit.getDamage() > 0) {
                World.sendProjectile(nex, target, 2263, 11, 11, 20, 5, 0, 0);
            }
            final double heal = 5.0;

            nex.heal((int) (hit.getDamage() / heal));
            ((Player) target).getPrayer().drainPrayer(hit.getDamage() / 5);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(2264));
                    if (hit.getDamage() > 0) {
                        World.sendProjectile(target, nex, 2263, 11, 11, 20, 5, 0, 0);
                    }
                }
            }, 0);
        }
    }


    private int sendShadowTraps(final Nex nex) {
        if (!nex.hasShadowTraps()) {
            nex.setHasShadowTraps(true);
            nex.setNextForceTalk(new ForceTalk("Fear the shadow!"));
            nex.playSoundEffect(3314);
            nex.setNextAnimation(new Animation(17407));
            nex.setNextGraphics(new Graphics(3362));

            final List<WorldTile> tiles = new LinkedList<WorldTile>();
            for (Entity t : nex.getPossibleTargets()) {
                WorldTile tile = new WorldTile(t);
                if (!tiles.contains(t)) {
                    tiles.add(tile);
                    World.spawnObjectTemporary(new WorldObject(57261, 10, 0, t.getX(), t.getY(), 0), 2400);
                }
            }
            WorldTasksManager.schedule(new WorldTask() {
                private boolean firstCall;

                @Override
                public void run() {
                    if (!firstCall) {
                        for (WorldTile tile : tiles) {
                            World.sendGraphics(null, new Graphics(383), new WorldTile(tile.getX(), tile.getY(), tile.getPlane()));
                            for (Entity t : nex.getPossibleTargets())
                                if (t.getX() == tile.getX() && t.getY() == tile.getY() && t.getPlane() == tile.getPlane())
                                    t.applyHit(new Hit(nex, (Math.min(t.getHitpoints(), 1500) * (Utils.random(10) + 70)) / 100, HitLook.REGULAR_DAMAGE));
                        }
                        firstCall = true;
                    } else {
                        nex.setHasShadowTraps(false);
                        stop();
                    }
                }
            }, 3, 3);
            return nex.getAttackSpeed();
        }
        return 0;
    }

    private void sendMeleeAttack(Nex nex, Entity target) {
        nex.setNextAnimation(new Animation(17453));
        Hit hit = getMeleeHit(nex, getRandomMaxHit(nex, nex.getCombatDefinitions().getMaxHit(), NPCCombatDefinitionConstants.MELEE, target));
        delayHit(nex, 0, target, hit);
        if (nex.getInstance() == null ? ZarosGodwars.nex.getCurrentPhase() == NexPhase.ZAROS : nex.getInstance().getCurrentPhase() == NexPhase.ZAROS)
            sendSoulSplit(hit, nex, target);
    }

    private void sendPullAttack(final Nex nex) {
        Entity target = nex.getFarthestTarget();
        if (target instanceof Player) {
            final Player player = (Player) target;
            player.lock(3);
            player.getActionManager().setActionDelay(10);
            player.resetWalkSteps();
            player.setNextAnimation(new Animation(-1));
            WorldTasksManager.schedule(new WorldTask() {
                int ticks;

                @Override
                public void run() {
                    ticks++;

                    if (ticks == 1) {
                        player.setNextAnimation(new Animation(14388));
                        player.setNextGraphics(new Graphics(2767));
                        player.setNextForceMovement(new ForceMovement(nex, 2, Utils.getMoveDirection(nex.getCoordFaceX(player.getSize()) - player.getX(), nex.getCoordFaceY(player.getSize()) - player.getY())));
                        nex.setTarget(player);
                    } else if (ticks == 3) {
                        player.setNextWorldTile(nex);
                        player.sendMessage("You've been injured and you cannot use " + (player.getPrayer().isAncientCurses() ? "protective curses" : "protective prayers") + "!");
                        player.setPrayerDelay(Utils.getRandom(20000) + 5);
                        player.resetWalkSteps();
                        int delay = 5 + Utils.random(5);
                        player.getActionManager().setActionDelay(delay);
                        player.addFreezeDelay(delay * 1000, true);
                        this.stop();
                    }
                }
            }, 0, 0);
        }
    }

    private void sendRangeAttack(final Nex nex) {
        nex.setNextAnimation(new Animation(17413));
        int damage = 0;
        for (final Entity t : nex.getPossibleTargets()) {
            int distance = Utils.getDistance(t.getX(), t.getY(), nex.getX(), nex.getY());
            if (distance <= 10)
                damage = 400 - (distance * 400 / 11);
            else
                damage = 300 + Utils.random(75);
            World.sendProjectile(nex, t, 380, 25, 25, 32, 0, 0, 0);
            delayHit(nex, 2, t, getRangeHit(nex, getRandomMaxHit(nex, damage, NPCCombatDefinitionConstants.RANGE, t)));
        }
    }

    private void sendVirusAttack(Nex nex) {
        nex.addVirusAttackDelay(45 + Utils.random(15));
        nex.setNextForceTalk(new ForceTalk("Let the virus flow through you!"));
        nex.playSoundEffect(3296);
        nex.setNextAnimation(new Animation(17414));
        nex.setNextGraphics(new Graphics(3375));
        for (Entity entity : nex.getPossibleTargets()) {
            if (!entity.withinDistance(nex.getFarthestTarget(), 2))
                continue;
            entity.setNextForceTalk(new ForceTalk("*Cough*"));
            entity.applyHit(new Hit(nex, Utils.getRandom(100), HitLook.REGULAR_DAMAGE));
            entity.getTemporaryAttributtes().put("nex_infected", true);
        }
        nex.playSound(3296, 2);
    }

    private void sendMagicAttack(final Nex nex, final boolean secondAttack) {
        for (final Entity t : nex.getPossibleTargets()) {
            nex.setNextAnimation(new Animation(17413));
            nex.setNextGraphics(new Graphics(1214));
            World.sendProjectile(nex, t, 3371, 25, 25, 32, 0, 0, 0);

            final int damage = getRandomMaxHit(nex, nex.getCombatDefinitions().getMaxHit(), NPCCombatDefinitionConstants.MAGE, t);
            Hit hit = getMagicHit(nex, damage);
            delayHit(nex, 2, t, hit);
            if (nex.getInstance() == null ? ZarosGodwars.nex.getCurrentPhase() == NexPhase.ZAROS : nex.getInstance().getCurrentPhase() == NexPhase.ZAROS)
                sendSoulSplit(hit, nex, t);
            if (damage > 0) {
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (nex == null || ZarosGodwars.nex == null || ZarosGodwars.nex.getCurrentPhase() == null || (nex.getInstance() != null && nex.getInstance().getCurrentPhase() == null)) {
                            stop();
                            return;
                        }
                        t.setNextGraphics(new Graphics(3373));
                        if ((nex.getInstance() == null ? ZarosGodwars.nex.getCurrentPhase() == NexPhase.SMOKE : nex.getInstance().getCurrentPhase() == NexPhase.SMOKE) && !secondAttack) {
                            if (damage > 0 && Utils.random(5) == 0)
                                t.getPoison().makePoisoned(80);
                        }
                    }
                }, 2);
            }
        }
    }
}
