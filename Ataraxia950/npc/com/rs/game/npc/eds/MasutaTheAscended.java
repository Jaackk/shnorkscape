package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.hitbar.HitBar;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom.CombinationBlock;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.val;

public class MasutaTheAscended extends EliteDungeonBoss {

    private static final long serialVersionUID = 8900342897357064141L;

    private transient int phase;
    private transient int maxThrashingWatersCount;
    private transient List<ThrashingWater> thrashingWaters;
    private transient ThrashingWaterCooldownHitBar bar;
    private transient long thrashingWaterCooldown;
    private transient int autoAttacksLeft = 0;
    private transient int attackProgress;
    private transient long spawnClownDelay = 0;
    private transient int talkIndex;
    private transient long talkCooldown;
    public static String[][] forceTalkDialogues = { { "You interfere with a matter larger than us all.", "We are coming for everything they said we couldn't have.", "Who could possibly stop us?", "The answer is... Not you.", "Not today." },

            { "Seiryu grants us power.", "Though it may not be willingly, it is essential.", "We will melt every crown.", "True tranquility can only be achieved through true destruction.", "We will spread this influence to the edges of the universe." } };
    private boolean addedBossBlock;

    public MasutaTheAscended(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
        maxThrashingWatersCount = 0;
        thrashingWaters = new ArrayList<ThrashingWater>();
        setIntelligentRouteFinder(true);
        setForceFollowClose(true);
        autoAttacksLeft = Utils.random(2, 5);
        attackProgress = 0;
        talkIndex = 0;
        phase = 0;
    }

    @Override
    public void spawn() {
        super.spawn();
        maxThrashingWatersCount = 0;
        autoAttacksLeft = Utils.random(2, 5);
        thrashingWaters = new ArrayList<ThrashingWater>();
        attackProgress = 0;
        talkIndex = 0;
        phase = 0;
        setIntelligentRouteFinder(true);
        setForceFollowClose(true);
        addedBossBlock = false;
    }

    @Override
    public void reset() {
        super.reset();
        setIntelligentRouteFinder(true);
        setForceFollowClose(true);
        maxThrashingWatersCount = 0;
        autoAttacksLeft = Utils.random(2, 5);
        attackProgress = 0;
        talkIndex = 0;
        phase = 0;
    }

    public void processForceTalk() {
        if (phase == 2 || getCombat().getTarget() == null)
            return;
        if (talkCooldown != 0 && talkCooldown >= Utils.currentTimeMillis())
            return;
        if (talkIndex >= forceTalkDialogues[phase].length)
            return;
        setNextForceTalk(new ForceTalk(forceTalkDialogues[phase][talkIndex]));
        talkIndex++;
        talkCooldown = Utils.currentTimeMillis() + 3000;
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if (phase == 0) {
            if (hit != null && hit.getDamage() > 0) {
                int damage = (getHitpoints() <= (int) ((double) getMaxHitpoints() * 0.50d)) ? 0 : hit.getDamage();
                hit.setDamage(damage);
            }
        }
        if (phase == 1) {
            if (hit != null && hit.getDamage() > 0) {
                int damage = (int) ((getHitpoints() <= (int) ((double) getMaxHitpoints() * 0.25d)) ? 0 : hit.getDamage() * 0.1);
                hit.setDamage(damage);
            }
        }
        super.handleIngoingHit(hit);
        if (getCombat().getTarget() == null) {
            List<Entity> targets = getPossibleTargets();
            if (!targets.isEmpty())
                getCombat().setTarget(targets.get(Utils.random(targets.size())));
        }
    }

    @Override
    public void processHit(Hit hit) {
        if (phase == 0) {
            if (hit != null && hit.getDamage() > 0) {
                int damage = (getHitpoints() <= (int) ((double) getMaxHitpoints() * 0.50d)) ? 0 : hit.getDamage();
                hit.setDamage(damage);
            }
        }
        if (phase == 1) {
            if (hit != null && hit.getDamage() > 0) {
                int damage = (int) ((getHitpoints() <= (int) ((double) getMaxHitpoints() * 0.25d)) ? 0 : hit.getDamage() * 0.1);
                hit.setDamage(damage);
            }
        }
        super.processHit(hit);
    }

    public void processPhaseChange() {
        if ((getHitpoints() <= (int) ((double) getMaxHitpoints() * 0.5d)) && phase == 0) {
            phase = 1;
            talkCooldown = Utils.currentTimeMillis() + 500;
            talkIndex = 0;
            setForceFollowClose(false);
            setNextNPCTransformation(25591);
            this.setNextAnimation(new Animation(-1));
            long totalTime = 3000;
            thrashingWaterCooldown = 3000 + Utils.currentTimeMillis();
            bar = new ThrashingWaterCooldownHitBar(totalTime, thrashingWaterCooldown);
            getNextHitBars().add(bar);
        }
    }

    public void addCooldownHitBar() {
        if (getCombat().getTarget() == null)
            return;
        if (bar != null && !getNextHitBars().contains(bar))
            getNextHitBars().add(bar);
        addHitBars();
    }

    @Override
    public void processNPC() {
        if (isDead() || hasFinished())
            return;
        super.processNPC();
        processBossBlock();
        addCooldownHitBar();
        processForceTalk();
        if (Utils.currentTimeMillis() > thrashingWaterCooldown) {
            bar = null;
        }
        setNextRenderAnimation(NPCDefinitions.getNPCDefinitions(getId()).renderEmote);
    }

    private CombinationBlock bossBlock1, bossBlock2;

    public void processBossBlock() {
        List<Entity> targets = getPossibleTargets();
        if (addedBossBlock && ((getCombat().getTarget() == null && targets.isEmpty()) || (getCombat().getTarget() != null && !isInsideFightArea(getCombat().getTarget())))) {
            if (!targets.isEmpty()) {
                getCombat().setTarget(targets.get(Utils.random(targets.size())));
            } else {
                addedBossBlock = false;
                reset();
                resetWalkSteps();
                setNextWorldTile(getRespawnTile());
                clearArea();
                setNextNPCTransformation(25589);
                setNextFaceWorldTile(getRespawnTile().transform(0, 1, 0));
                setNextAnimation(new Animation(-1));
                setNextGraphics(new Graphics(-1));
                setNextRenderAnimation(-1);
                bossBlock1.remove();
                bossBlock2.remove();
            }
        } else if (!addedBossBlock && getCombat().getTarget() != null) {
            addedBossBlock = true;
            List<EliteDungeonNPC> npcs = new ArrayList<EliteDungeonNPC>();
            npcs.add(this);
            bossBlock1 = getRoom().addBlock(new WorldObject(111700, 10, 3, getRoom().getTile(new WorldTile(4625, 9240, 1))), npcs);
            bossBlock2 = getRoom().addBlock(new WorldObject(111700, 10, 1, getRoom().getTile(new WorldTile(4654, 9240, 1))), npcs);
        }
    }

    @Override
    public boolean restoreHitPoints() {
        if (getRoom() == null)
            return false;
        for (Player p : getRoom().getRoom().getPlayers()) {
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof EliteDungeonController))
                continue;
            EliteDungeonController c = (EliteDungeonController) p.getControlerManager().getControler();
            if (c.getCurrent() != this)
                continue;
            c.updateInterface();
        }
        return false;
    }

    @Override
    public int getBossMapId() {
        return 40395;
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (getRoom() == null)
            return possibleTarget;
        if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget()))
            possibleTarget.add(getCombat().getTarget());
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || !Utils.isOnRange(this, player, 30) || player.getEliteDungeonsManager().isHidden() || possibleTarget.contains(player) || !isInsideFightArea(player))
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;

    }

    public boolean isInsideFightArea(WorldTile tile) {
        if (getRoom() == null)
            return false;
        WorldTile min1 = getRoom().getTile(new WorldTile(4625, 9233, 1));
        WorldTile max1 = getRoom().getTile(new WorldTile(4653, 9254, 1));
        WorldTile min2 = getRoom().getTile(new WorldTile(4623, 9249, 1));
        WorldTile max2 = getRoom().getTile(new WorldTile(4655, 9269, 1));
        return (tile.getX() >= min1.getX() && tile.getY() >= min1.getY() && tile.getX() <= max1.getX() && tile.getY() <= max1.getY()) || (tile.getX() >= min2.getX() && tile.getY() >= min2.getY() && tile.getX() <= max2.getX() && tile.getY() <= max2.getY());
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public void finish() {
        clearArea();
        super.finish();
    }

    public void clearArea() {
        if (thrashingWaters != null) {
            for (ThrashingWater water : thrashingWaters) {
                if (water != null && !water.isDead() && !water.hasFinished())
                    water.finish();
            }
            thrashingWaters.clear();
        }
    }

    public boolean stopAttack() {
        Entity target = getCombat().getTarget();
        return (target == null && getPossibleTargets().isEmpty()) || (target != null && !isInsideFightArea(target) && getPossibleTargets().isEmpty());
    }

    public void spawnPurpleEnergy(WorldTile tile) {
        MasutaTheAscended boss = this;
        World.sendGraphics(boss, new Graphics(7193), tile);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (boss == null || boss.hasFinished() || boss.isDead() || boss.stopAttack())
                    return;
                int angle = Utils.random(8);
                Entity target = null;
                List<Entity> inRangeTargets = new ArrayList<Entity>();
                for (Entity e : boss.getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished() || !Utils.isOnRange(tile, e, 2, 1, 1))
                        continue;
                    inRangeTargets.add(e);
                }
                target = inRangeTargets.isEmpty() ? null : inRangeTargets.get(Utils.random(inRangeTargets.size()));
                if (target != null && Utils.isOnRange(tile, target, 2, 1, 1)) {
                    angle = Utils.getAngle(target.getX() - tile.getX(), target.getY() - tile.getY()) >> 11;
                }
                byte[] dir = Utils.getDirByV(angle);
                WorldTile hitTile = tile.transform(3 * dir[0], 3 * dir[1], 0);
                World.sendGraphics(boss, new Graphics(6960, 0, 0, (angle & 0x7)), tile);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (boss == null || boss.hasFinished() || boss.isDead() || boss.stopAttack())
                            return;
                        for (Entity e : boss.getPossibleTargets()) {
                            if (e == null || e.isDead() || e.hasFinished())
                                continue;
                            if (Utils.isOnRange(hitTile, e, 0, 1, 1)) {
                                int damage = Utils.random(600, 701);
                                CombatScript.delayHit(boss, 0, e, CombatScript.getMagicHit(boss, damage));
                            }
                        }
                    }
                }, 2);
            }
        }, 10);
    }

    public Entity[] getTsunamiTargets() {
        List<Entity> possibleTargets = new ArrayList<Entity>();
        List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
        WorldTile[] start = new WorldTile[3];
        byte[] dir = Utils.getDirection(getDirection());
        start[0] = new WorldTile(getX() + (dir[1] != 0 ? -dir[1] : dir[1]), getY() + (dir[0] != 0 ? dir[0] : dir[0]), getPlane());
        start[1] = new WorldTile(this);
        start[2] = new WorldTile(getX() - (dir[1] != 0 ? -dir[1] : dir[1]), getY() - (dir[0] != 0 ? dir[0] : dir[0]), getPlane());
        for (int i = 0; i < start.length; i++) {
            for (int j = 1; j < 5; j++) {
                possibleTiles.add(new WorldTile(start[i].getX() + (j * dir[0]), start[i].getY() + (j * dir[1]), getPlane()));
            }
        }
        for (Entity e : getPossibleTargets()) {
            if (e == null || e.isDead() || e.hasFinished())
                continue;
            for (WorldTile tile : possibleTiles) {
                if (tile.matches(e))
                    possibleTargets.add(e);
            }
        }
        return possibleTargets.toArray(new Entity[possibleTargets.size()]);
    }

    @Override
    public void sendDeath(Entity source) {
        setNextForceTalk(new ForceTalk("No! I was not finished! I need to fulfil... I..."));
        super.sendDeath(source);
    }

    public void spawnThrashingWater() {
        if (maxThrashingWatersCount == 0) {
            maxThrashingWatersCount = getPossibleTargets().size() == 1 ? Utils.random(13, 15) : Utils.random(8, 10);
            for (Entity e : getPossibleTargets()) {
                if (e == null || e.isDead() || e.hasFinished())
                    continue;
                e.getTemporaryAttributtes().remove("waterbuff");
            }
        }
        if (Utils.currentTimeMillis() <= thrashingWaterCooldown || phase >= 2)
            return;
        if (thrashingWaters.size() >= maxThrashingWatersCount) {
            setNextForceTalk(new ForceTalk("These waters will choke you where you stand!"));
            phase = 2;
            attackProgress = 0;
            autoAttacksLeft = Utils.random(3, 5);
            setForceFollowClose(false);
            setNextNPCTransformation(25589);
            return;// end phase
        }
        List<WorldTile> tiles = new ArrayList<WorldTile>();
        for (int x = getX() - 30; x <= getX() + 30; x++) {
            for (int y = getY() - 30; y <= getY() + 30; y++) {
                WorldTile tile = new WorldTile(x, y, getPlane());
                if (!isInsideFightArea(tile) || !World.canMoveNPC(tile, 1) || !Utils.isOnRange(this, tile, 30, 1, 1))
                    continue;
                boolean usedTile = false;
                for (NPC w : thrashingWaters) {
                    if (w != null && !w.hasFinished() && !w.isDead() && w.matches(tile)) {
                        usedTile = true;
                        break;
                    }
                }
                if (usedTile)
                    continue;
                tiles.add(tile);
            }
        }
        Collections.shuffle(tiles);
        thrashingWaters.add(new ThrashingWater(25592, tiles.get(0), this));
    }

    public static void applyThrashingWaterBuff(Entity e) {
        if (e == null || e.isDead() || e.hasFinished())
            return;
        e.getTemporaryAttributtes().put("waterbuff", getThrashingWaterBuff(e) + 1);
    }

    public static int getThrashingWaterBuff(Entity e) {
        if (e == null || e.isDead() || e.hasFinished())
            return 0;
        return e.getTemporaryAttributtes().containsKey("waterbuff") ? (int) e.getTemporaryAttributtes().get("waterbuff") : 0;
    }

    public static class ThrashingWaterCooldownHitBar extends HitBar {
        private long totalTime = 0;
        private final long timeToRemove;

        public ThrashingWaterCooldownHitBar(long totalTime, long timeToExplode) {
            this.totalTime = totalTime;
            this.timeToRemove = timeToExplode;
        }

        @Override
        public int getType() {
            return 9;
        }

        @Override
        public int getPercentage() {
            if (Utils.currentTimeMillis() > timeToRemove)
                return 0;
            long timeRemaining = timeToRemove - Utils.currentTimeMillis();
            int percentage = (int) (((((double) timeRemaining / (double) totalTime) * 100) * 255) / 100);
            return percentage;
        }

        @Override
        public boolean display(Player player) {
            return getPercentage() != 0;
        }

    }

    public static class ThrashingWater extends EliteDungeonNPC {

        private static final long serialVersionUID = 3583510067667887993L;

        private final MasutaTheAscended boss;

        public ThrashingWater(int id, WorldTile tile, MasutaTheAscended boss) {
            super(id, tile, boss.getRoom());
            setCantFollowUnderCombat(true);
            this.boss = boss;
            spawn();
        }

        @Override
        public void spawn() {
            super.spawn();
            setNextGraphics(new Graphics(6965));
            if (boss != null && !boss.getPossibleTargets().isEmpty())
                getCombat().setTarget(boss.getPossibleTargets().get(Utils.random(boss.getPossibleTargets().size())));
        }

        @Override
        public void processNPC() {
            if (isDead() || hasFinished())
                return;
            if (boss != null && (boss.hasFinished() || boss.isDead()) || boss.stopAttack()) {
                finish();
                return;
            }
            super.processNPC();
        }

        @Override
        public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
            if (boss != null)
                return boss.getPossibleTargets(checkNPCs, checkPlayers);
            return super.getPossibleTargets(checkNPCs, checkPlayers);
        }

        @Override
        public void sendDeath(Entity source) {
            setNextGraphics(new Graphics(6958));
            for (Entity e : getPossibleTargets()) {
                if (e == null || e.isDead() || e.hasFinished())
                    continue;
                if (Utils.isOnRange(this, e, 0))
                    applyThrashingWaterBuff(e);
            }
            super.sendDeath(source);
        }

        @Override
        public ArrayList<Entity> getPossibleTargets() {
            if (boss != null)
                return boss.getPossibleTargets();
            return super.getPossibleTargets();
        }
    }

    public enum MasutaTheAscendedAttacks {
        ATTACK() {

            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.processPhaseChange();
                int autoAttacksLeft = boss.autoAttacksLeft;
                switch (boss.phase) {
                case 0:
                    if (autoAttacksLeft > 0) {
                        if (boss.attackProgress > 0 && Utils.random(3) == 0 && (boss.spawnClownDelay == 0 || Utils.currentTimeMillis() > boss.spawnClownDelay)) {
                            return CLONE_ATTACK.sendAttack(boss, target);
                        }
                        MasutaTheAscendedAttacks attack = Utils.isOnRange(boss, target, 0) && Utils.random(2) == 0 ? MELEE_ATTACK : RANGE_ATTACK;
                        boss.autoAttacksLeft = boss.autoAttacksLeft - 1 <= 0 ? 0 : boss.autoAttacksLeft - 1;
                        return attack.sendAttack(boss, target);
                    }
                    boss.attackProgress++;
                    MasutaTheAscendedAttacks attack = boss.attackProgress % 2 == 0 ? PURPLE_ENERGY : boss.attackProgress % 3 == 0 ? TSUNAMI : Utils.random(3) == 0 ? PULVERISE : HURRICANE;
                    boss.autoAttacksLeft = Utils.random(3, 5);
                    return attack.sendAttack(boss, target);
                case 1:
                    return SPAWN_THRASHING_WATER.sendAttack(boss, target);
                case 2:
                    if (autoAttacksLeft > 0) {
                        attack = RANGE_ATTACK;
                        boss.autoAttacksLeft = boss.autoAttacksLeft - 1 <= 0 ? 0 : boss.autoAttacksLeft - 1;
                        return attack.sendAttack(boss, target);
                    }
                    boss.autoAttacksLeft = Utils.random(3, 5);
                    return PURPLE_WATERS.sendAttack(boss, target);
                }
                return 0;
            }
        },

        MELEE_ATTACK() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.setNextAnimation(new Animation(boss.getAttackEmote()));
                int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
                CombatScript.delayHit(boss, 0, target, CombatScript.getMeleeHit(boss, damage));
                return boss.getAttackSpeed();
            }
        },
        RANGE_ATTACK() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.setNextAnimation(new Animation(18186));
                List<WorldTile> attacks = new ArrayList<WorldTile>();
                for (int x = boss.getX() - 7; x <= boss.getX() + 7; x++) {
                    for (int y = boss.getY() - 7; y <= boss.getY() + 7; y++) {
                        WorldTile tile = new WorldTile(x, y, boss.getPlane());
                        if (Utils.isOnRange(boss, tile, 2, 1, 1))
                            continue;
                        attacks.add(tile);
                    }
                }
                Collections.shuffle(attacks);
                List<Entity> possibleTargets = boss.getPossibleTargets();
                for (int i = 0; i < 8; i++) {
                    int targetIndex = i == 2 || i == 4 || i == 6 ? ((i / 2) - 1) : -1;
                    final Entity e = targetIndex != -1 && targetIndex < possibleTargets.size() ? possibleTargets.get(targetIndex) : null;
                    Projectile projectile = World.sendProjectileCycles(boss, e != null ? e : attacks.get(i), 5704, 35, 35, 1 + (i * 5), 60 + (i * 5), Utils.random(5), 0);
                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                    if (e != null)
                        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                            @Override
                            public boolean repeat() {
                                try {
                                    if (boss.isDead() || boss.hasFinished() || boss.stopAttack() || e == null || e.hasFinished() || e.isDead())
                                        return false;
                                    int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.RANGE, e);
                                    CombatScript.delayHit(boss, 0, e, CombatScript.getRangeHit(boss, damage));
                                    return false;
                                } catch (Exception e) {
                                    Logger.getGlobal().catching(e);
                                    return false;
                                }
                            }
                        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                }
                return boss.getAttackSpeed();
            }
        },
        HURRICANE() {

            public int sendAttack(MasutaTheAscended boss, Player target) {
                WorldTasksManager.schedule(new WorldTask() {
                    private int ticks;
                    private final int startDamage = Utils.random(100, 151);

                    @Override
                    public void run() {
                        if (boss.hasFinished() || boss.isDead() || boss.stopAttack() || ticks >= 20) {
                            boss.getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
                            boss.setNextGraphics(new Graphics(-1));
                            boss.setNextRenderAnimation(boss.getDefinitions().getRenderAnimation());
                            boss.setRun(true);
                            stop();
                            return;
                        } else if (ticks == 0) {
                            boss.setRun(false);
                            boss.setNextRenderAnimation(2989);
                            boss.getTemporaryAttributtes().put("cantDoAnimationOrGFX", Boolean.TRUE);
                        } else {
                            boss.setNextRenderAnimation(2989);
                            boss.forceSetNextGraphics(new Graphics(4415));
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.hasFinished() || e.isDead() || !(e instanceof Player))
                                    continue;
                                if (Utils.isOnRange(boss, e, 0)) {
                                    int damage = startDamage + (ticks * 25);
                                    if (damage > 350)
                                        damage = 350;
                                    e.applyHit(new Hit(boss, damage, HitLook.MELEE_DAMAGE));
                                }
                            }
                        }
                        ticks++;
                    }
                }, 0, 0);
                return 22;
            }
        },
        PURPLE_ENERGY() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                int energiesCount = Utils.random(4, 7);
                List<WorldTile> tiles = new ArrayList<WorldTile>();
                for (int x = boss.getX() - 30; x <= boss.getX() + 30; x++) {
                    for (int y = boss.getY() - 30; y <= boss.getY() + 30; y++) {
                        WorldTile tile = new WorldTile(x, y, boss.getPlane());
                        if (!boss.isInsideFightArea(tile) || !World.canMoveNPC(tile, 1) || !Utils.isOnRange(boss, tile, 11, 1, 1))
                            continue;
                        tiles.add(tile);
                    }
                }
                Collections.shuffle(tiles);
                WorldTasksManager.schedule(new WorldTask() {
                    int index = 0;

                    @Override
                    public void run() {
                        if (index >= energiesCount || boss.hasFinished() || boss.isDead() || boss.stopAttack()) {
                            stop();
                            return;
                        }
                        boss.spawnPurpleEnergy(tiles.get(index));
                        index++;
                    }

                }, 0, 0);
                return 1;
            }
        },
        TSUNAMI() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.setNextAnimation(new Animation(18417));
                int angle = (int) Math.round(Math.toDegrees(Math.atan2((boss.getX() * 2 + boss.getSize()) - (target.getX() * 2 + target.getSize()), (boss.getY() * 2 + boss.getSize()) - (target.getY() * 2 + target.getSize()))) / 45d) & 0x7;
                World.sendGraphics(boss, new Graphics(5572, 0, 0, (angle + 4) & 0x7), new WorldTile(target));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        for (Entity t : boss.getTsunamiTargets()) {
                            int damage = CombatScript.getRandomMaxHit(boss, 200, NPCCombatDefinitionConstants.MAGE, t, 2.00 + ThreadLocalRandom.current().nextDouble(1), false);
                            CombatScript.delayHit(boss, 0, t, CombatScript.getMagicHit(boss, damage));
                        }
                    }

                }, 1);
                return boss.getAttackSpeed();
            }
        },
        PULVERISE() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.setNextForceTalk(new ForceTalk("I'll pulverise you!"));
                boss.setNextFaceEntity(null);
                boss.setCantFollowUnderCombat(true);
                boss.setNextFaceWorldTile(new WorldTile(target));
                boss.getTemporaryAttributtes().put("cantDoAnimationOrGFX", Boolean.TRUE);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    boolean sentAttack;

                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                return false;
                            if (!sentAttack) {
                                boss.getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
                                boss.setNextAnimation(new Animation(31908));
                                boss.setNextGraphics(new Graphics(3473));
                                boss.getTemporaryAttributtes().put("cantDoAnimationOrGFX", Boolean.TRUE);
                                for (Entity e : boss.getPossibleTargets()) {
                                    if (e == null || e.isDead() || e.hasFinished() || !Utils.isOnRange(boss, e, 1))
                                        continue;
                                    if (boss.getDirection() == Utils.getAngle(e.getX() - boss.getX(), e.getY() - boss.getY())) {
                                        int damage = CombatScript.getRandomMaxHit(boss, 200, NPCCombatDefinitionConstants.MELEE, e, 2.5 + ThreadLocalRandom.current().nextDouble(1), true);
                                        CombatScript.delayHit(boss, 0, e, CombatScript.getMeleeHit(boss, damage));
                                    }
                                }
                                WorldTasksManager.schedule(new WorldTask() {

                                    @Override
                                    public void run() {
                                        byte[] dir = Utils.getDirection(boss.getDirection());
                                        WorldTile[] tiles = new WorldTile[4];
                                        tiles[0] = boss.transform(0, 0, 0);
                                        tiles[1] = boss.transform(1 * dir[0], 1 * dir[1], 0);
                                        tiles[2] = boss.transform(2 * dir[0], 2 * dir[1], 0);
                                        tiles[3] = boss.transform(3 * dir[0], 3 * dir[1], 0);
                                        for (int i = 0; i < 3; i++)
                                            World.sendProjectileCycles(tiles[i], tiles[3], 462, 10, 30 + (i * 10), 0, 40 - (i * 10), 0, 0);
                                    }

                                }, 1);
                                //
                                sentAttack = true;
                                return true;
                            }
                            boss.setCantFollowUnderCombat(false);
                            boss.getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
                            if (target != null && !target.hasFinished() && !target.isDead())
                                boss.setNextFaceEntity(target);
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, 1000, 3000, TimeUnit.MILLISECONDS);
                return 8;
            }
        },
        CLONE_ATTACK() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.spawnClownDelay = Utils.currentTimeMillis() + 4000;
                int clonesCount = boss.getPossibleTargets().size() > 1 ? 2 : 1;
                EliteDungeonNPC[] clones = new EliteDungeonNPC[clonesCount];
                List<WorldTile> possibleSpawnTile = new ArrayList<WorldTile>();
                for (int x = boss.getX() - 10; x <= boss.getX() + 10; x++) {
                    for (int y = boss.getY() - 10; y <= boss.getY() + 10; y++) {
                        WorldTile tile = new WorldTile(x, y, boss.getPlane());
                        if (!boss.isInsideFightArea(tile) || !World.canMoveNPC(tile, 1) || !Utils.isOnRange(boss, tile, 1, 1, 1) || Utils.colides(boss, tile, 1, 1))
                            continue;
                        possibleSpawnTile.add(tile);
                    }
                }
                Collections.shuffle(possibleSpawnTile);
                if (possibleSpawnTile.size() < clonesCount) {
                    for (int i = 0; i < clonesCount; i++)
                        possibleSpawnTile.add(new WorldTile(boss));
                }
                for (int i = 0; i < clones.length; i++) {
                    final int index = i;
                    clones[index] = new EliteDungeonNPC(25590, possibleSpawnTile.get(i), boss.getRoom());
                    clones[index].spawn();
                    clones[index].setNextFaceEntity(boss.getPossibleTargets().get(Utils.random(boss.getPossibleTargets().size())));
                    WorldTasksManager.schedule(new WorldTask() {
                        private int ticks;

                        @Override
                        public void run() {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack()) {
                                clones[index].finish();
                                stop();
                                return;
                            }
                            if (ticks == 0) {
                                clones[index].setNextAnimation(new Animation(18186));
                                for (Entity e : boss.getPossibleTargets()) {
                                    if (e == null || e.hasFinished() || e.isDead())
                                        continue;
                                    Projectile projectile = World.sendProjectileCycles(clones[index], e, 462, 30, 25, 0, 30, Utils.random(5), 0);
                                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                                    CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

                                        @Override
                                        public boolean repeat() {
                                            try {
                                                if (boss.isDead() || boss.hasFinished())
                                                    return false;
                                                int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.MELEE, e);
                                                CombatScript.delayHit(boss, 0, e, CombatScript.getMeleeHit(boss, damage));
                                                return false;
                                            } catch (Exception e) {
                                                Logger.getGlobal().catching(e);
                                                return false;
                                            }
                                        }
                                    }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                                }
                            }
                            if (ticks >= 4) {
                                clones[index].finish();
                                stop();
                                return;
                            }
                            ticks++;
                        }

                    }, 1, 0);
                }
                return 1;
            }
        },
        PURPLE_WATERS() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.setNextAnimation(new Animation(31913));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        byte[] dir = Utils.getDirection(boss.getDirection());
                        if (!Utils.isOnRange(boss, target, 1))
                            World.sendProjectileCycles(boss.transform(-2 * dir[0], -2 * dir[1], 0), boss.transform(15 * dir[0], 15 * dir[1], 0), 6961, 0, 0, 10, 60, 0, 0);
                        for (Entity e : boss.getPossibleTargets()) {
                            if (e == null || e.isDead() || e.hasFinished())
                                continue;
                            int damage = Utils.random(500, 801);
                            CombatScript.delayHit(boss, 0, e, CombatScript.getMagicHit(boss, damage));
                        }
                    }
                }, 2);
                return boss.getAttackSpeed();
            }
        },
        SPAWN_THRASHING_WATER() {
            public int sendAttack(MasutaTheAscended boss, Player target) {
                boss.spawnThrashingWater();
                return 9;
            }
        };

        public int sendAttack(MasutaTheAscended boss, Player target) {
            return 0;
        }
    }

    @Override
    public void drop(Player killer) {
        String name = getName().toLowerCase();
        if (name == null)
            return;
        List<Player> players = getDropPlayers();
        if (killer != null && !players.contains(killer))
            players.add(killer);
        for (Player player : players) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            int extra = player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1250 : 0;
            player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() + 5000 + extra);
            player.getPackets().sendGameMessage(!player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? "You received 5,000 dungeoneering tokens." : "You received 6,250 dungeoneering tokens, An extra 1250 tokens for having the dungeons master perk.");
            player.getInventionManager().processScavengingPerk();
            increaseKillStatistics(player, getName());
            handleRingOfDeath(player);
            sendDrop(player, Math.random() <= 0.05 ? new NPCDrop(43053, 0.005, 1, 1) : new NPCDrop(42954, 40, 5, 34), false);
            final NPCDrop[] possibleDrops = EliteDungeonsConstants.mobDrops;
            for (final NPCDrop drop : possibleDrops) {
                if (drop == null || drop.getRate() < 100)
                    continue;
                if (!Ectoplasmator.scatterAshes(player, this, drop.getItemId()))
                    sendDrop(player, drop, false);
            }
            int amountCharms = player.getInventory().getAmountOf(43066);
            NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.04) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
            NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(possibleDrops);
            if (luckyCharm != null && amountCharms > 0)
                player.getInventory().deleteItem(43066, 1);
            sendDrop(player, mobDrop, false);
            if (Math.random() <= 0.005)
                sendDrop(player, new NPCDrop(18778, 5, 1, 1), false);
        }
    }

    @Override
    protected void sendDrop(Player player, NPCDrop drop, boolean lootbeam) {
        WorldTile tile = getRespawnTile();
        final Item item = new Item(drop.getItemId());
        if (player.getInventory().containsItem(19675, 1)) {
            if (Herbicide.handleDrop(player, item)) {
                return;
            }
        }
        if (player.getInventory().containsItem(18337, 1)) {
            if (Bonecrusher.handleDrop(player, item)) {
                return;
            }
        }
        if (!(drop.getItemId() == 995 && CoinAccumulator.handleCoinAccumulator(player, this, drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())))) {
            int id = drop.getItemId();
            int amount = drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount());
            if (Settings.DOUBLE_DROPS) {
                amount *= 2;
            }
            if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069 || player.getEquipment().getRingId() == 48483) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            if (player.getCurrentPet() != null) {
                if (player.getCurrentPet().getPerks().contains(PetPerk.DOUBLE_TROUBLE)) {
                    if (Utils.random(100) <= PetPerkUtils.getConModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop suddenly vanishes because of your pet perk!");
                        return;
                    } else if (Utils.random(100) <= PetPerkUtils.getProModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        amount *= 2;
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your pet perk!");
                    }
                }
            }
            if (!player.getEliteDungeonsManager().isAutoLoot() && Math.random() <= 0.05) {
                amount *= 2;
                player.getPackets().sendGameMessage("You received 2x " + item.getName() + ".");
            }
            val loot = new Item(id, amount);
            if (!player.getEliteDungeonsManager().isAutoLoot() || !player.getEliteDungeonsManager().addReward(loot)) {
                if (item.getAmount() > 1 && !item.getDefinitions().isStackable() && !item.getDefinitions().isNoted()) {
                    for (int i = 0; i < item.getAmount(); i++)
                        World.updateGroundItem(new Item(loot.getId(), 1), tile, player, 60, 0, false);
                } else
                    World.updateGroundItem(loot, tile, player, 60, 0, false);
            }
        }
        player.getDropCollectionHandler().handleBossKills(new Item(drop.getItemId()), id);
    }

}
