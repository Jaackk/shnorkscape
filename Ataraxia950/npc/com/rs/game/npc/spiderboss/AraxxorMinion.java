package com.rs.game.npc.spiderboss;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class AraxxorMinion extends NPC {

    private final Araxxor araxxor;
    private Entity target;
    private Entity followTarget;
    private final long startDelay;
    private long healDelay;
    private long acidicSpiderDeathCycle;
    private boolean exploding;
    private int acidAmount;
    private boolean fromMinionAssist;

    public AraxxorMinion(int id, WorldTile tile, Araxxor araxxor) {
        super(id, tile, id == 19471 || id == 19470 ? -1 : 0, true, true);
        setForceMultiArea(true);
        this.araxxor = araxxor;
        setIntelligentRouteFinder(true);
        setNoDistanceCheck(true);
        startDelay = Utils.currentTimeMillis() + 1300;
        if (getId() == 19469 && araxxor != null)
            setRandomWalk(0);
        healDelay = Utils.currentTimeMillis() + 3600; // 6 ticks
    }

    @Override
    public void processNPC() {
        if (!hasFinished() && (araxxor == null || araxxor.hasFinished() || araxxor.getPossibleTargets().isEmpty() || (followTarget != null && (followTarget.isDead() || followTarget.hasFinished() || !araxxor.getInstance().getPlayersInside().contains(followTarget))))) {
            finish();
            return;
        }
        if (hasFinished())
            return;
        if (startDelay > Utils.currentTimeMillis())
            return;
        if (araxxor.getTemporaryAttributtes().get("charging") != null || araxxor.getTemporaryAttributtes().get("phase4cutscene") != null)
            return;
        if (getId() == 19469)
            sendFollow(araxxor);
        if (followTarget != null)
            sendFollow(followTarget);
        if (followTarget != null && acidicSpiderDeathCycle != 0 && acidicSpiderDeathCycle > Utils.currentTimeMillis()) {
            getNextHitBars().add(new AcidicSpiderHitBar(this));
        }
        if (getId() == 19471 && followTarget != null && (Utils.isOnRange(this, araxxor.getMiddleWorldTile(), 0, 1, 1))) {
            if (!exploding) {
                exploding = true;
                araxxor.setAbsorpedAcid(araxxor.getAbsorpedAcid() + acidAmount);
                araxxor.getInstance().sendMessage("Araxxor absorbs the highly acidic spider!");
                araxxor.heal(500, 0, 0, true);
                araxxor.getInstance().updateInterface(false);
                setNextAnimation(new Animation(24122));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        finish();
                        exploding = false;
                    }
                }, 2);
            }
        }
        if (followTarget != null && acidicSpiderDeathCycle != 0 && (Utils.currentTimeMillis() >= acidicSpiderDeathCycle || Utils.isOnRange(this, followTarget, 0, 1, 1))) {
            if (!exploding) {
                exploding = true;
                acidicSpiderDeathCycle = 0;
                setNextAnimation(new Animation(24122));
                if (Utils.isOnRange(this, followTarget, 0))
                    followTarget.applyHit(new Hit(this, Utils.random(15) == 0 ? (Utils.random(300, 1201)) : 3200, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        finish();
                        exploding = false;
                    }
                }, 2);
            }
        }
        if (getId() == 19469 && Utils.currentTimeMillis() >= healDelay) {
            healAraxxor();
            healDelay = Utils.currentTimeMillis() + 3600; // 6 ticks
        }
        super.processNPC();
    }

    @Override
    public void forceWalkRespawnTile() {

    }

    @Override
    public void setTarget(final Entity entity) {
        getCombat().setTarget(entity);
        setLastAttackedByTarget(Utils.currentTimeMillis());
    }

    private void sendFollow(Entity target) {
        if (getLastFaceEntity() != target.getClientIndex())
            setNextFaceEntity(target);
        int size = getSize();
        int targetSize = target.getSize();
        if (Utils.colides(getX(), getY(), size, target.getX(), target.getY(), targetSize) && !target.hasWalkSteps()) {
            resetWalkSteps();
            if (!addWalkSteps(target.getX() + targetSize, getY())) {
                resetWalkSteps();
                if (!addWalkSteps(target.getX() - size, getY())) {
                    resetWalkSteps();
                    if (!addWalkSteps(getX(), target.getY() + targetSize)) {
                        resetWalkSteps();
                        if (!addWalkSteps(getX(), target.getY() - size)) {
                            return;
                        }
                    }
                }
            }
            return;
        }
        resetWalkSteps();
        if (!clipedProjectile(target, true) || !Utils.isOnRange(getX(), getY(), size, target.getX(), target.getY(), targetSize, 0))
            calcFollow(target, 2, true, true);
    }

    private void healAraxxor() {
        int healAmount = 500;
        boolean aboveMaxHP = araxxor.getHitpoints() + healAmount >= getMaxHitpoints();
        int hp = aboveMaxHP ? araxxor.getMaxHitpoints() : araxxor.getHitpoints() + healAmount;
        int healed = hp - araxxor.getHitpoints();
        if (healed <= 0)
            return;
        setNextAnimation(new Animation(24071));
        Projectile projectile = World.sendProjectileCycles(this, araxxor.getMiddleWorldTile(), 5003, 12, 37, 0, 120, 10 + Utils.random(5), 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    araxxor.heal(healAmount, 0, 0, false);
                    araxxor.getInstance().updateInterface(false);
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }

        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean checkAgressivity() {
        if (startDelay > Utils.currentTimeMillis())
            return false;
        if (((target == null || target.isDead() || target.hasFinished()) && getId() == 19468) || getId() == 19469 || getId() == 19471 || getId() == 19470)
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }

        return !possibleTarget.isEmpty();
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return super.getPossibleTargets();
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (target != null && !target.isDead() && !target.hasFinished()) {
            possibleTarget.add(target);
            return possibleTarget;
        }
        for (Player player : araxxor.getInstance().getPlayersInside()) {
            if (player == null || player.isDead())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public void processHit(Hit hit) {
        if (hit.getSource() != null && (hit.getSource() instanceof Player)) {
            if (getId() == 19469 && target == null)
                target = hit.getSource();
            if (getId() == 19468) {
                Hit reflectedHit = new Hit(araxxor, hit.getDamage(), HitLook.REFLECTED_DAMAGE);
                if (araxxor.getPhase() != 3 && (araxxor.getHitpoints() - reflectedHit.getDamage()) <= 0) {
                    araxxor.unlockChallenge((Player) hit.getSource(), 2);
                }
                araxxor.applyHit(reflectedHit);
            }
        }
        super.processHit(hit);
    }

    public Araxxor getAraxxor() {
        return araxxor;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public Entity getFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(Entity followTarget) {
        this.followTarget = followTarget;
        this.setRandomWalk(0);
        if (getId() == 19470) {
            this.setRun(false);
            this.acidicSpiderDeathCycle = Utils.currentTimeMillis() + 7300;
        }
    }

    public long getAcidicSpiderDeathCycle() {
        return acidicSpiderDeathCycle;
    }

    public int getAcidAmount() {
        return acidAmount;
    }

    public void setAcidAmount(int acidAmount) {
        this.acidAmount = acidAmount;
    }

    public boolean isFromMinionAssist() {
        return fromMinionAssist;
    }

    public void setFromMinionAssist(boolean fromMinionAssist) {
        this.fromMinionAssist = fromMinionAssist;
    }

}
