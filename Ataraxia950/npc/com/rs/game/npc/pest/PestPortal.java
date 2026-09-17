package com.rs.game.npc.pest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.activites.pest.PestControl.PestData;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
public class PestPortal extends NPC {
    private int nextEarthquake;
    private int nextFirewall;
    private int firewallActive;
    boolean isLocked;
    final PestControl control;
    int ticks;
    final boolean knight;
    final ImmutableSet<WorldTile> fireWallTiles = ImmutableSet.of(
            transform(2, 0, 0),
            transform(2, 1, 0),
            transform(2, -1, 0),
            transform(2, -2, 0),
            transform(0, 2, 0),
            transform(1, 2, 0),
            transform(2, 2, 0),
            transform(-1, 2, 0),
            transform(-2, 2, 0),
            transform(-2, 0, 0),
            transform(-2, 1, 0),
            transform(-2, -1, 0),
            transform(-2, -2, 0),
            transform(0, -2, 0),
            transform(1, -2, 0),
            transform(-1, -2, 0));

    public PestPortal(int id, boolean canbeAttackedOutOfArea, WorldTile tile, PestControl control, boolean knight) {
        super(id, tile, -1, canbeAttackedOutOfArea, true);
        this.control = control;
        this.knight = knight;
        setCantFollowUnderCombat(true);
        setForceMultiArea(true);
        setCapDamage(400);
        isLocked = true;
        nextEarthquake = getNextEarthquake();
        nextFirewall = getNextFirewall();
        if (id > 6000)
            for (int i = 0; i < getBonuses().length; i++)
                setBonus(i, 200 + ((control.getPestData().ordinal() + 1) * 100));
    }

    private int getNextFirewall() {
        if (control.hardMode) {
            return Utils.random(33, 66);
        }
        return Utils.random(33, 99);
    }

    private void startFirewall() {
        firewallActive = 34;
        for (Player player : control.getPlayers()) {
            if (player == null || player.hasFinished() || !player.isActive()) {
                continue;
            }
            player.pcFirewall = this;
        }
    }

    public void checkFirewall(Player player) {
        if (isDead()) {
            player.pcFirewall = null;
        } else if (firewallActive > 0) {
            int chance = control.hardMode ? 2 : 3;
            int dmg = Utils.random(getFirewallDamage());
            if (fireWallTiles.stream().anyMatch(it -> it.getX() == player.getX() &&
                    it.getY() == player.getY() &&
                    it.getPlane() == player.getPlane())) {
                player.applyHit(new Hit(dmg, Hit.HitLook.REGULAR_DAMAGE));
            } else if (ThreadLocalRandom.current().nextInt(chance) == 0 && withinDistance(player, 1)) {
                dmg -= 50;
                if (dmg > 0) {
                    player.applyHit(new Hit(dmg, Hit.HitLook.REGULAR_DAMAGE));
                }
            }
        }
    }

    private Range<Integer> getFirewallDamage() {
        if (control.hardMode) {
            return Range.closed(50, 150);
        }
        return Range.closed(50, 100);
    }

    private int getNextEarthquake() {
        if (control.hardMode) {
            return Utils.random(33, 66);
        }
        return Utils.random(33, 99);
    }

    private int getIndexForId() {
        switch (getId()) {
            case 6146:
            case 6142:
                return 0;
            case 6147:
            case 6143:
                return 1;
            case 6148:
            case 6144:
                return 2;
            case 6149:
            case 6145:
                return 3;
            case 3782:
            case 3784:
            case 3785:
                return 4;
        }
        return -1;
    }

    private String getStringForId() {
        switch (getId()) {
            case 6142:
                return "<col=9900CC>The purple, western</col>";
            case 6143:
                return "<col=0099FF>The blue, eastern</col>";
            case 6144:
                return "<col=FFFF33>The yellow, south-eastern</col>";
            case 6145:
                return "<col=D80000>The red, south-western</col>";
            default:
                return null;
        }
    }

    private int getSpawnRate() {
        int spawnRate;
        int size = control.getPlayers().size();
        if (size <= 3) {
            spawnRate = 20; // Every 15 seconds.
        } else if (size <= 12) {
            spawnRate = 15; // Every 9 seconds.
        } else if (size <= 25) {
            spawnRate = 10; // Every 6 seconds.
        } else {
            throw new RuntimeException("size above 25?");
        }

        switch (control.getPestData()) {
            case INTERMEDIATE:
                spawnRate -= 2; // 1 second faster.
                break;
            case VETERAN:
                spawnRate -= 4; // 2 seconds faster.
                break;
        }
        int limit = control.hardMode ? 3 : 5;
        if (spawnRate < limit) {
            spawnRate = limit;
        }
        return spawnRate;
    }

    private Range<Integer> getEarthquakeDamage() {
        if (control.hardMode) {
            return Range.closed(200, 450);
        }
        return Range.closed(150, 400);
    }

    @Override
    public void processHit(Hit hit) {
        if (control.getPestData() == PestControl.PestData.VETERAN && knight) {
            int damage = hit.getDamage();
            int rerollChance = 15;
            double damageBoost = 1.05;
            if (damage == 0 && ThreadLocalRandom.current().nextInt(rerollChance) == 0) {
                damage = ThreadLocalRandom.current().nextInt(0, getMaxHit() + 1);
            }
            hit.setDamage((int) (damage * damageBoost));
        }
        super.processHit(hit);
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        ticks++;
        if (ticks % getSpawnRate() == 0) {
            if (control.createPestNPC(getIndexForId())) {
                // 1/3 Chance for double spawn.
                if (Utils.random(3) == 0) {
                    control.createPestNPC(getIndexForId());
                }
            }
        }
        String string = getStringForId();
        if (knight && control.getPestData() == PestData.VETERAN) {
            if (nextFirewall == 10) {
                for (Player player : control.getPlayers()) {
                    if (player == null || player.hasFinished() || !player.isActive()) {
                        continue;
                    }
                    player.sendMessage(Colors.RED + "The portals are preparing a barrier around the knight!");
                }
            }
            if (nextFirewall == 0) {
                startFirewall();
                nextFirewall = getNextFirewall();
                sendFirewallGfx();
            } else if (firewallActive > 0) {
                if (firewallActive % 4 == 0) {
                    sendFirewallGfx();
                }
                firewallActive--;
            } else if (nextFirewall > 0) {
                nextFirewall--;
            }

        } else if (string != null && control.getPestData() == PestData.VETERAN) {
            if (nextEarthquake == 17) {
                sendGfx(3861);

                for (Player player : control.getPlayers()) {
                    if (player == null || player.hasFinished() || !player.isActive()) {
                        continue;
                    }
                    player.sendMessage(string + " portal's energy has started an earthquake!");
                }
            }

            if (nextEarthquake == 13) {
                sendGfx(3861);
            }
            if (nextEarthquake == 8) {
                sendGfx(3861);
            }
            if (nextEarthquake == 0) {
                sendGfx(3862);
                for (Player player : control.getPlayers()) {
                    if (player == null || player.hasFinished() || !player.isActive()) {
                        continue;
                    }
                    if (withinDistance(player, 7)) {
                        player.setNextGraphics(new Graphics(3862));
                        if (!player.prayer.isRangeProtecting()) {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                player.sendFilteredMessage(Colors.RED + "You are hit by some falling debris. You feel weakened.");
                                player.skills.drainLevel(Skills.DEFENCE, 5);
                            } else {
                                player.sendFilteredMessage(Colors.RED + "You are hit by some falling debris. You are stunned.");
                                player.setFreezeDelay(5);
                            }
                            player.applyHit(new Hit(player, Utils.random(getEarthquakeDamage()), Hit.HitLook.RANGE_DAMAGE));
                        }
                    }
                }
                nextEarthquake = getNextEarthquake();
            }
            if (nextEarthquake > 0) {
                nextEarthquake--;
            }
        }
        if (isDead() || isLocked)
            return;
        cancelFaceEntityNoCheck();
    }

    private void sendFirewallGfx() {
        for (WorldTile tile : fireWallTiles) {
            val graphics = new Graphics(1333);
            World.sendGraphics(this, graphics, tile);
        }
    }

    private void sendGfx(int id) {
        val tile = getMiddleWorldTile();
        val graphics = new Graphics(id);
        World.sendGraphics(this, graphics, tile);
        World.sendGraphics(this, graphics, tile.transform(1, 0, 0));
        World.sendGraphics(this, graphics, tile.transform(-1, 0, 0));
        World.sendGraphics(this, graphics, tile.transform(0, 1, 0));
        World.sendGraphics(this, graphics, tile.transform(0, -1, 0));
        World.sendGraphics(this, graphics, tile.transform(1, 0, 0));
        World.sendGraphics(this, graphics, tile.transform(-1, 0, 0));
        World.sendGraphics(this, graphics, tile.transform(0, 1, 0));
        World.sendGraphics(this, graphics, tile.transform(0, -1, 0));
    }

    @Override
    public void sendDeath(Entity source) {
        resetWalkSteps();
        setNextAnimation(null);
        if (getIndexForId() != 4) {
            control.getKnight().heal(control.getPestData() != PestData.VETERAN ? 500 : 600);
        }
        finish();
    }

    public void unlock() {
        if (getId() >= 6146) {
            this.transformIntoNPC(getId() - 4);
            control.sendTeamMessage(getStringForId() + " portal shield has been dropped!");
        }
        isLocked = false;
    }

    @Override
    public int getMaxHitpoints() {
        if (control == null)
            return 50;
        PestData data = control.getPestData();
        if (getId() >= 6142 && getId() <= 6157) {
            // Health of portals.
            switch (data) {
                case NOVICE:
                    return 2000;
                case INTERMEDIATE:
                    return 2500;
                case VETERAN:
                    return control.hardMode ? 10_000 : 7500;
            }

        } else {
            // Health of void knights.
            switch (data) {
                case NOVICE:
                case INTERMEDIATE:
                case VETERAN:
                    return 2000;
            }
        }
        throw new IllegalStateException("unreachable.");
    }
}