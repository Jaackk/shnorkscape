package com.rs.game.activities.aod.npc;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.ability.AoDAbility;
import com.rs.game.activities.aod.ability.ElementsInstantKill;
import com.rs.game.activities.aod.ability.Escalation;
import com.rs.game.activities.aod.ability.Freeze;
import com.rs.game.activities.aod.ability.IcePrison;
import com.rs.game.activities.aod.ability.LastStand;
import com.rs.game.activities.aod.ability.PowerFromTheElements;
import com.rs.game.activities.aod.ability.PraesulSummon;
import com.rs.game.activities.aod.ability.ShadowOrbs;
import com.rs.game.activities.aod.ability.ShadowPool;
import com.rs.game.activities.aod.ability.ShadowTraps;
import com.rs.game.activities.aod.ability.SmokeWall;
import com.rs.game.activities.aod.ability.Virus;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A NPC class for the AoD Nex herself. Contains all variables associated with Nex alone.
 *
 * @author Kris | 30. sept 2017 : 17:12.15
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDNex extends NPC {

    private static final long serialVersionUID = -6618225411227621272L;

    public AoDNex(int id, WorldTile tile, AngelOfDeath instance) {
        super(id, tile, -1, true, true);
        this.instance = instance;
        this.enrage = instance.getPlayers().size() * -10;
        setHitpoints(maxHitpoints = calculateMaxHitpoints(instance));
        setRun(true);
        setForceMultiArea(true);
        setForceTargetDistance(100);
        specialDelay = Utils.currentTimeMillis() + Utils.random(10000, 20000);
    }

    private static final Animation DEATH = new Animation(30137);
    private static final Graphics DEATH_GFX = new Graphics(6530);

    public static int calculateMaxHitpoints(AngelOfDeath instance) {
        int playerCount = instance.getPlayers().size();

        if (playerCount < 7) {
            return 125_000;
        }

        return 125_000 + ((Math.floorDiv(playerCount, 5)) * 25_000);
    }

    private final AngelOfDeath instance;
    private AoDAbility ability;
    private int enrage, lastSemiAttack;
    private long poison, specialDelay;
    private boolean vulnerability, heal;
    private final Map<Player, Long[]> enrageSpecificPlayers = new ConcurrentHashMap<Player, Long[]>();
    private Player target;
    private final int maxHitpoints;


    @Override
    public boolean canWalkNPC(int toX, int toY) {
        return true;
    }

    @Override
    public int getCapDamage() {
        return 1000;
    }

    @Override
    public int getMaxHitpoints() {
        return maxHitpoints;
    }

    public boolean heal() {
        return heal;
    }

    public void setHeal(final boolean heal) {
        this.heal = heal;
    }

    public boolean isPoisonous() {
        return poison > Utils.currentTimeMillis();
    }

    public void setPoisonous(long time) {
        this.poison = Utils.currentTimeMillis() + time;
    }

    public long getSpecialDelay() {
        return specialDelay;
    }

    public AoDAbility generateNextAbility() {
        specialDelay = Utils.currentTimeMillis() + (Utils.random(30000, 50000));
        if (ability == null)
            return new PowerFromTheElements(this, instance);
        switch (instance.getStage()) {
            case 0:
            case 1:
            case 2:
                if (ability instanceof PowerFromTheElements)
                    return new ShadowOrbs(this, instance);
                return new PowerFromTheElements(this, instance);
            case 3:
                if (ability instanceof SmokeWall)
                    return new ShadowPool(this, instance);
                else if (ability instanceof ShadowPool)
                    return new IcePrison(this, instance);
                return new SmokeWall(this, instance);
            default:
                if (Utils.randomBool()) {
                    if (lastSemiAttack > 2)
                        lastSemiAttack = 0;
                    switch (lastSemiAttack++) {
                        case 0:
                            return new Freeze(this, instance);
                        case 1:
                            return new ShadowTraps(this, instance);
                        default:
                            return new Virus(this, instance);
                    }
                }
                return new ElementsInstantKill(this, instance);
        }
    }

    /**
     * Sets the vulnerability state of Nex.
     *
     * @param vulnerable {@value true - is vulnerable, false - isn't vulnerable.}
     */
    public void setVulnerability(boolean vulnerable) {
        vulnerability = vulnerable;
    }

    /**
     * A boolean determining whether Nex is currently vulnerable or not.
     *
     * @return vulnerability state.
     */
    public boolean isVulnerable() {
        return vulnerability;
    }

    @Override
    public boolean isIntelligentRouteFinder() {
        return true;
    }

    /**
     * Adds enrage towards a specific player. If the player isn't in the list of enrage players
     * it will be added and its enrage starts off at 0%, increasing with every melee
     * attack that Nex performs. Enrage will be cleared if Nex doesn't attack
     * the given target for 10 seconds with melee.
     *
     * @param player entity to add.
     */
    public void addEnragePlayer(final Player player) {
        if (target != player && !enrageSpecificPlayers.containsKey(player)) {
            player.getPackets().sendPlayerMessage(1, 15263739, "Nex lets out a slight hiss as she lands a hit on you.", true);
            enrageSpecificPlayers.put(player, new Long[]{Utils.currentTimeMillis() + 10000, (long) 0});
        } else if (enrageSpecificPlayers.containsKey(player))
            enrageSpecificPlayers.put(player, new Long[]{Utils.currentTimeMillis() + 10000, enrageSpecificPlayers.get(player)[1] + 3});
        target = player;
        enrageSpecificPlayers.forEach((k, v) -> {
            if (v[0] < Utils.currentTimeMillis()) {
                enrageSpecificPlayers.remove(k);
                k.getPackets().sendPlayerMessage(1, 15263739, "Nex has begun directing her full attention to someone else..", true);
            }
        });
    }

    /**
     * Refreshes the enrage stack on all players at the end of a special ability, otherwise it will
     * always immediately wear off after each special.
     */
    public void refreshAllEnrage() {
        enrageSpecificPlayers.forEach((k, v) -> enrageSpecificPlayers.put(k, new Long[]{Utils.currentTimeMillis(), v[1]}));
    }

    /**
     * Gets the enrage multiplier against a specific player. Includes the generic enrage by Nex herself.
     *
     * @param player whose enrage to add.
     * @return total enrage against the player-
     */
    public double getEnrageMultiplier(final Player player) {
        double multiplier = 1 + (enrage / 100d);
        if (enrageSpecificPlayers.containsKey(player))
            multiplier += enrageSpecificPlayers.get(player)[1] / 100d;
        return multiplier;
    }

    /**
     * Gets Nex's current target.
     *
     * @return target
     */
    public Player getTargetedPlayer() {
        if (target == null && instance.getPlayers().size() > 0)
            return instance.getPlayers().get(Utils.random(instance.getPlayers().size()));
        return target;
    }

    /**
     * Gets the instance class.
     *
     * @return instance.
     */
    public AngelOfDeath getInstance() {
        return instance;
    }

    /**
     * Starts an ability and sets the existing ability to match this one.
     *
     * @param ability to launch.
     */
    public void useAbility(final AoDAbility ability) {
        this.ability = ability;
        WorldTasksManager.schedule(ability, 0, 0);
    }

    /**
     * Gets the current running ability.
     *
     * @return current ability.
     */
    public AoDAbility getAbility() {
        return ability;
    }

    /**
     * Gets the current enrage percent.
     *
     * @return enrage percent.
     */
    public int getEnrage() {
        return enrage;
    }

    /**
     * Adds enrage to Nex.
     *
     * @param amount to add in percentage.
     */
    public void addEnrage(final int amount) {
        enrage += amount;
    }

    /**
     * Sends a message to all players in the instance, as well as a force chat above Nex herself.
     *
     * @param message
     */
    public void sendMessage(final String message) {
        setNextForceTalk(new ForceTalk(message));
        instance.sendMessage("<col=ff0000>Nex: </col><col=9df2a5>" + message);
    }

    @Override
    public final void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        instance.clearBombs();
        WorldTasksManager.schedule(new WorldTask() {
            private int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                            plr.gimTracker.incrementBpGained(7);
                        }
                    }
                    increaseKillStatisticsForAll();
                    instance.getPlayers().forEach(player -> {
                        ContractHandler.updateContract(player, AoDNex.this);
                        HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.NEX_ANGEL_OF_DEATH);
                    });
                    setNextAnimation(DEATH);
                    setNextGraphics(DEATH_GFX);
                } else if (loop >= defs.getDeathDelay()) {
                    instance.getPlayers().forEach(p -> p.getActivityTimersManager().finishBossTimer(AoDNex.this));
                    instance.generateRewards(getReceivedDamage());
                    World.spawnObject(new WorldObject(100823, 10, 0, instance.getWorldTile(2848, 1827)));
                    reset();
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private void increaseKillStatisticsForAll() {
        instance.getPlayers().forEach(p -> increaseKillStatistics(p, "nex: angel of death"));
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        double hitpointsPercent = (double) this.getHitpoints() / (double) this.getMaxHitpoints();

        if (instance.getStage() == 1 && hitpointsPercent < 0.7) {
            instance.setStage(2);
            useAbility(new PraesulSummon(this, instance));
        } else if (instance.getStage() == 2 && hitpointsPercent < 0.6) {
            instance.setStage(3);
            useAbility(new Escalation(this, instance));
        } else if (instance.getStage() == 3 && hitpointsPercent < 0.2) {
            instance.setStage(4);
            useAbility(new LastStand(this, instance));
        }
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (hit.getSource() instanceof Player) {
            Player player = (Player) hit.getSource();
            int weaponId = player.getEquipment().getWeaponId();
            if (instance.isGimInside() && weaponId == 25202) {
                player.sendMessage("Deathtouched darts have no effect when GIM are inside the instance.");
                return;
            }
        }
        super.handleIngoingHit(hit);
        int remainingHealth = getHitpoints() - hit.getDamage();
        if (remainingHealth < 0)
            remainingHealth = 0;
        final int health = remainingHealth;
        instance.getAllPlayers().forEach(p -> p.getPackets().sendIComponentText(1073, 3, Utils.formatNumber(health)));
    }

    @Override
    public void setTarget(Entity entity) {
        if (isForceWalking())
            return;
        if (entity instanceof Familiar) {
            if (((Familiar) entity).getOwner() != null) {
                getCombat().setTarget(((Familiar) entity).getOwner());
                setLastAttackedByTarget(Utils.currentTimeMillis());
                return;
            }
        }
        if (entity instanceof Player)
            target = (Player) entity;
        getCombat().setTarget(entity);
        setLastAttackedByTarget(Utils.currentTimeMillis());
    }

    @Override
    public boolean isFreezeImmune() {
        return true;
    }

    @Override
    public boolean isStunImmune() {
        return true;
    }
}
