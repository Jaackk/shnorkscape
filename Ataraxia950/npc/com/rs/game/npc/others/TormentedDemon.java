package com.rs.game.npc.others;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.HeadIcon;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles construction of Tormented Demon NPC.
 *
 * @author Noel
 */
public class TormentedDemon extends NPC implements Serializable {

    private static final long serialVersionUID = -4933607755489667042L;
    private static final HeadIcon[][] ICONS = { { new HeadIcon(440, 0) }, // MELEE
            { new HeadIcon(440, 1) }, // RANGE
            { new HeadIcon(440, 2) },/// MAGIC
    };
    private boolean[] demonPrayer;
    private int[] cachedDamage;
    private int shieldTimer, fixedAmount, prayerTimer, fixedCombatType, lastType;

    public TormentedDemon(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea,
                          final boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        shieldTimer = 0;
        cachedDamage = new int[3];
        demonPrayer = new boolean[3];
        switchPrayers(Utils.random(3));
    }

    public static boolean atTD(final WorldTile tile) {
        return (tile.getX() >= 2560 && tile.getX() <= 2630) && (tile.getY() >= 5710 && tile.getY() <= 5753);
    }

    public void switchPrayers(int type) {
        if (type == 1) {
            type = 2;
        } else if (type == 2) {
            type = 1;
        }
        setNextNPCTransformation(8349 + type);
        demonPrayer[type] = true;
        resetPrayerTimer();
        requestIconRefresh();
    }

    @Override
    public HeadIcon[] getIcons() {
        return ICONS[getId() - 8349];
    }

    private void resetPrayerTimer() {
        prayerTimer = 27;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (isDead()) {
            return;
        }
        if (Utils.random(100) <= 2) {
            sendRandomProjectile();
        }
        if (getCombat().process()) {// no point in processing
            if (shieldTimer > 0) {
                shieldTimer--;
            }
            if (prayerTimer > 0) {
                prayerTimer--;
            }
            if (prayerTimer == 0) {
                for (int i = 0; i < cachedDamage.length; i++) {
                    if (cachedDamage[i] >= 3100) {
                        demonPrayer = new boolean[3];
                        switchPrayers(i);
                        cachedDamage = new int[3];
                    }
                }
            }
            for (int i = 0; i < cachedDamage.length; i++) {
                if (cachedDamage[i] >= 3100) {
                    demonPrayer = new boolean[3];
                    switchPrayers(i);
                    cachedDamage = new int[3];
                }
            }
        }
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (hit.getSource() instanceof Player) {// darklight
            final Player player = (Player) hit.getSource();
            final int weaponId = player.getEquipment().getWeaponId();
            if (weaponId == 25202)
                return;
            if ((weaponId == 6746 || weaponId == 2402 || weaponId == 732) && (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE) && hit.getDamage() > 0) {
                if (shieldTimer <= 10) {
                    player.getPackets().sendGameMessage("The demon is temporarily weakened by your weapon.", true);
                }
                shieldTimer = 60;
            }
        }
        for (int attackType = 0; attackType < demonPrayer.length; attackType++) {
            if (hit.getLook().getMark() == 133 + (attackType * 3)) {
                lastType = attackType;
                if (demonPrayer[attackType]) {
                    hit.setDamage((int) (hit.getDamage() * .4));
                }
                cachedDamage[attackType] += hit.getDamage();
            } else if (hit.getLook() == HitLook.MISSED) {
                cachedDamage[lastType] += 200;
            }
        }
        if (shieldTimer <= 0) {// 75% of damage is absorbed
            hit.setDamage((int) (hit.getDamage() * 0.25));
            setNextGraphics(new Graphics(1885));
        }
        super.handleIngoingHit(hit);
    }

    private void sendRandomProjectile() {
        final WorldTile tile = new WorldTile(getX() + Utils.random(7), getY() + Utils.random(7), getPlane());
        setNextAnimation(new Animation(10918));
        World.sendProjectile(this, tile, 1887, 34, 16, 40, 35, 16, 0);
        for (final int regionId : getMapRegionsIds()) {
            final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (final int npcIndex : playerIndexes) {
                    final Player player = World.getPlayers().get(npcIndex);
                    if (player == null || player.isDead() || player.hasFinished() || !player.isActive()
                            || !player.withinDistance(tile, 3)) {
                        continue;
                    }
                    player.sendMessage("The demon's magical attack splashes on you.", true);
                    player.applyHit(new Hit(this, 281, HitLook.MAGIC_DAMAGE, 1));
                    player.setNextGraphics(new Graphics(1883, 0, 100));
                }
            }
        }
    }

    @Override
    public void setRespawnTask() {
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        final NPC npc = this;
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            setFinished(false);
            World.addNPC(npc);
            npc.setLastRegionId(0);
            World.updateEntityRegion(npc);
            loadMapRegions();
            shieldTimer = 0;
            fixedCombatType = 0;
            fixedAmount = 0;
            demonPrayer = new boolean[3];
            cachedDamage = new int[3];
        }, getCombatDefinitions().getRespawnDelay() * 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        setNextNPCTransformation(8351);
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                           plr.gimTracker.incrementBpGained(2);
                        }
                        ContractHandler.updateContract(plr, TormentedDemon.this);
                    }
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    setRespawnTask();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0;
    }

    public int getFixedCombatType() {
        return fixedCombatType;
    }

    public void setFixedCombatType(final int fixedCombatType) {
        this.fixedCombatType = fixedCombatType;
    }

    public int getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(final int fixedAmount) {
        this.fixedAmount = fixedAmount;
    }
}