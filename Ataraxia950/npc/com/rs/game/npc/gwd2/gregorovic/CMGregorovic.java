package com.rs.game.npc.gwd2.gregorovic;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.GregorovicInstance;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CMGregorovic extends Gregorovic {

    private static final long serialVersionUID = 8570959065996178551L;

    public CMGregorovic(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, GregorovicInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
    }

    private List<WightHunter> hunters;
    private int count;

    @Override
    public void handleIngoingHit(Hit hit) {
        if (getCapDamage() != -1 && hit.getDamage() > getCapDamage())
            hit.setDamage(getCapDamage());
        int hp = getHitpoints() - hit.getDamage();
        if (hp <= 0 && (hunters == null || hunters.size() < 6))
            hp = 1;
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            HeartOfGielinor.refreshHealth(instance, hp, getMaxHitpoints());
            return;
        }
        handlePrayers(hit);
        HeartOfGielinor.refreshHealth(instance, hp, getMaxHitpoints());
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (getInstance().getPlayers().size() == 0 && !hasFinished()) {
            finish();
            if (getShadows() != null)
                for (Shadow s : getShadows()) {
                    if (s != null && !s.hasFinished() && !s.isDead())
                        s.sendDeath(null);
                }
            if (getSpirits() != null)
                for (Spirit s : getSpirits()) {
                    if (s != null && !s.hasFinished() && !s.isDead())
                        s.sendDeath(null);
                }
            if (hunters != null)
                for (WightHunter hunter : hunters) {
                    if (hunter != null && !hunter.hasFinished() && !hunter.isDead())
                        hunter.sendDeath(null);
                }
            return;
        }
        if (getHitpoints() <= 14000 && shadowStage == 0 && !isDead()) {
            shadowStage++;
            for (int i = 0; i < (getInstance().isHardMode() ? 3 : 2); i++)
                shadows.add(new Shadow(22444, new WorldTile(instance.getWorldTile(Utils.random(33, 54), Utils.random(33, 54))), -1, true, true));
        } else if (getHitpoints() <= 6000 && shadowStage == 1 && !isDead()) {
            shadowStage++;
            for (int i = 0; i < (getInstance().isHardMode() ? 4 : 3); i++)
                shadows.add(new Shadow(22444, new WorldTile(instance.getWorldTile(Utils.random(33, 54), Utils.random(33, 54))), -1, true, true));
        }
        if (lastSwitch > 12) {
            for (Shadow s : shadows) {
                if (s == null || s.isDead() || s.hasFinished() || isCantInteract())
                    continue;
                if (Utils.random(2) == 1) {
                    final WorldTile shadow = new WorldTile(s);
                    final WorldTile greg = new WorldTile(this);
                    s.setNextWorldTile(greg);
                    setNextWorldTile(shadow);
                    setNextGraphics(new Graphics(6137));
                    s.setNextGraphics(new Graphics(6137));
                    lastSwitch = 0;
                    break;
                }
            }

        }
        lastSwitch++;
    }

    public List<WightHunter> getHunters() {
        return hunters;
    }

    private void consumeHealth() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (isCantInteract()) {
                    setNextAnimation(new Animation(28500));
                    setNextGraphics(new Graphics(6140));
                    if (getHitpoints() <= 0)
                        setHitpoints(1);
                    applyHit(new Hit(null, 1000, HitLook.HEALED_DAMAGE));
                    if (getHitpoints() > 6000 && shadowStage == 2)
                        shadowStage--;
                    else if (getHitpoints() > 14000 && shadowStage == 1)
                        shadowStage--;
                } else {
                    setNextAnimation(new Animation(28501));
                    setNextGraphics(new Graphics(-1));
                    stop();
                }
            }
        }, 7, 8);
    }

    public void checkHunter(final int id) {
        if (++count >= 6) {
            setCantInteract(false);
            this.setNextGraphics(new Graphics(-1));
            this.setNextAnimation(new Animation(-1));
            count = 0;
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    setNextGraphics(new Graphics(-1));
                    setNextAnimation(new Animation(28501));
                    setNextForceTalk(new ForceTalk("No! Allow yourself to be consumed!"));
                    Player p = instance.getPlayers().size() == 0 ? null : instance.getPlayers().get(0);
                    if (p != null) {
                        getCombat().setTarget(p);
                        p.setShadow(false);
                        p.setNextAnimation(new Animation(-1));
                        p.getAppearence().transformIntoNPC(-1);
                    }

                }
            });
            return;
        }
        int h = 0;
        for (WightHunter hunter : hunters) {
            if (hunter == null)
                continue;
            if (hunter.getId() == id)
                h++;
        }
        if (h < 2)
            hunters.add(new WightHunter(id, getInstance().getWorldTile(Utils.random(33, 54), Utils.random(33, 54)), -1, true, this));
    }

    @Override
    public void sendDeath(final Entity source) {
        if (isCantInteract())
            return;
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player)
            source.deathResetCombat();
        setNextAnimation(null);
        if (instance.isHardMode() && hunters == null) {
            if (instance.getPlayers().size() == 0)
                return;
            if (source == null)
                return;
            if (!(source instanceof Player))
                return;
            transmogrify((Player) source);
            setHitpoints(30000);
            setNextForceTalk(new ForceTalk("JOIN ME! JOIN THE COLLECTION!"));
            setCantInteract(true);
            source.setNextForceTalk(new ForceTalk("What's happening?"));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (getHitpoints() <= 0 || getHitpoints() > 10000)
                        setHitpoints(1);
                    applyHit(new Hit(null, 500, HitLook.HEALED_DAMAGE));
                    setNextAnimation(new Animation(28500, -1, -1, -1, -1, 0));
                    setNextGraphics(new Graphics(6140));
                    consumeHealth();
                    setNextForceTalk(new ForceTalk("EMBRACE THE DARKNESS!"));
                    hunters = new ArrayList<WightHunter>();
                    for (int i = 0; i < 3; i++)
                        hunters.add(new WightHunter(22447 + i, getInstance().getWorldTile(Utils.random(33, 54), Utils.random(33, 54)), -1, true, CMGregorovic.this));
                }
            }, 1);
            return;
        }
        for (Shadow s : getShadows()) {
            if (s != null && !s.hasFinished() && !s.isDead())
                s.sendDeath(source);
        }
        for (Spirit s : getSpirits()) {
            if (s != null && !s.hasFinished() && !s.isDead())
                s.sendDeath(source);
        }
        for (WightHunter hunter : hunters) {
            if (hunter != null && !hunter.hasFinished() && !hunter.isDead())
                hunter.sendDeath(source);
        }
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0)
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        ContractHandler.updateContract(((Player) source), CMGregorovic.this);
                    }
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    @Override
    public void applyHit(Hit hit) {
        if (isDead())
            return;
        getReceivedHits().add(hit);
        handleIngoingHit(hit);
    }

    private final void transmogrify(final Player player) {
        player.setShadow(true);
        player.setNextAnimation(new Animation(-1));
        player.getAppearence().transformIntoNPC(22444);
    }

}
