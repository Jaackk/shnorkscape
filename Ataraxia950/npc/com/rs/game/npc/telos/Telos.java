package com.rs.game.npc.telos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.EffectsManager.Effect;
import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Region;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.impl.TelosInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Telos extends NPC {

    private final TelosInstance instance;
    private int phase;
    private int autoAttacks;
    private int attackRotation;
    private int anima;
    private boolean wakingUp, switchingPhase;
    private WorldObject tendril;
    private long spawnStreamCycles;
    private long spawnGolemsCycles;
    private long rockFallCycles;
    private final AnimaGolem[] golems;
    private final Font[] fonts;
    private int fontAttackRotation;
    private final ColoredAnimaGolem[] coloredGolems;
    private long resetCombatDelayCycle;
    private boolean dieing;
    private final List<Integer> animaBombAttackRotations;

    public Telos(int id, WorldTile tile, TelosInstance instance) {
        super(id, tile, -1, true, true);
        setIntelligentRouteFinder(true);
        autoAttacks = 7;
        this.instance = instance;
        golems = new AnimaGolem[3];
        fonts = new Font[3];
        coloredGolems = new ColoredAnimaGolem[4];
        animaBombAttackRotations = new ArrayList<Integer>(3);
        setForceFollowClose(true);
        setCapDamage(1200);
        setNoDistanceCheck(true);
        setForceTargetDistance(50);
    }

    public TelosInstance getInstance() {
        return instance;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
        updateInterface();
    }

    public int getAutoAttacks() {
        return autoAttacks;
    }

    public void setAutoAttacks(int autoAttacks) {
        this.autoAttacks = autoAttacks;
    }

    public void decreaseAutoAttacks() {
        this.autoAttacks = autoAttacks - 1 <= 0 ? 0 : autoAttacks - 1;
    }

    public void startFight() {
        if (wakingUp || getId() != 22892)
            return;
        wakingUp = true;
        setNextForceTalk(new ForceTalk("You have made a grave mistake."));
        setNextAnimation(new Animation(28962));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setNextNPCTransformation(getId() - 1);
                setDefinitions();
                setNPCStats();
                getPlayer().getControlerManager().sendInterfaces();
                wakingUp = false;
                spawnStreamCycles = Utils.currentTimeMillis() + 25000;
                spawnFonts();
                getCombat().setTarget(getPlayer());
            }
        }, 2);
    }

    public void spawnFonts() {
        for (int i = 0; i < fonts.length; i++)
            fonts[i] = new Font((phase == 4 ? 22912 : 22893) + i, instance.getTile(TelosInstance.FONT_SPAWM_TILE[i]), this);
    }

    public void removeFonts() {
        for (int i = 0; i < fonts.length; i++)
            if (fonts[i] != null) {
                fonts[i].finish();
                fonts[i] = null;
            }
    }

    public void chargeFont(ColoredAnimaGolem golem) {
        for (int i = 0; i < fonts.length; i++)
            if (fonts[i] != null && !fonts[i].hasFinished())
                fonts[i].processOnGolemDeath(golem);
    }

    public void spawnColoredGolems() {
        if (getPlayer() == null && phase != 3)
            return;
        WorldTile checkTile = getPlayer();
        for (int i = 0; i < 3; i++) {
            WorldTile spawnTile = new WorldTile(getPlayer());
            // attemps to randomize tile by 4x4 area
            for (int trycount = 0; trycount < 10; trycount++) {
                spawnTile = new WorldTile(checkTile, 2);
                if (World.isTileFree(checkTile.getPlane(), spawnTile.getX(), spawnTile.getY(), 1) && World.canMoveNPC(checkTile.getPlane(), spawnTile.getX(), spawnTile.getY(), 1))
                    break;
                spawnTile = checkTile;
            }
            ColoredAnimaGolem golem = new ColoredAnimaGolem(22905 + i, spawnTile, this);
            golem.setForceMultiArea(true);
            golem.setTarget(getPlayer());
        }
    }

    private long gripCycle;

    @Override
    public void processNPC() {
        if (switchingPhase || dieing)
            return;
        resetCombatDelay();
        processStream();
        addGolems();
        processRockFall();
        useFreedom();
        if (getTemporaryAttributtes().get("STUNNED") != null || dieing)
            return;
        if (getTemporaryAttributtes().get("GripAttack") != null) {
            if (gripCycle != 0 && Utils.currentTimeMillis() >= gripCycle) {
                if (isDead() || hasFinished() || isLocked() || dieing || switchingPhase)
                    return;
                if (getPlayer().isDead())
                    return;
                int enrage = getPlayer().getTelosEnrage() >= 2700 ? 2700 : getPlayer().getTelosEnrage();
                int healAmount = (int) (75 + (0.5 * (double) enrage));// here is the heal
                if (healAmount > 500)// u can change this, this will make it cap at a certain amount aight ty
                    healAmount = 500;
                heal(healAmount, 0, 0, true);
                getPlayer().applyHit(new Hit(this, Utils.random(healAmount / 7, healAmount / 6), HitLook.REGULAR_DAMAGE));
                int specDrainAmount = 5;
                if (getPlayer().getCombatDefinitions().getSpecialAttackPercentage() >= specDrainAmount) {
                    getPlayer().getCombatDefinitions().decreaseSpecialAttack(specDrainAmount, false);
                }
                increaseAnima(1);
                gripCycle = Utils.currentTimeMillis() + 600;
            }
            return;
        }
        if (getTemporaryAttributtes().get("CantMove") != null)
            return;
        super.processNPC();
    }

    public void useFreedom() {
        if (getPlayer() == null || hasFinished() || dieing || isDead())
            return;
        int enrage = getPlayer().getTelosEnrage();
        if (enrage < 150)
            return;
        int random = enrage >= 150 && enrage <= 999 ? 5 : 10;
        if ((isStunned()) && Utils.random(100) <= (random)) {
            this.setFreezeDelay(0);
            this.setStunDelay(0);
            if (enrage >= 250 && phase == 5 && !isStunImmune()) {
                setStunImmune(true);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        setStunImmune(false);
                    }
                }, 6);
            }
            getPlayer().getPackets().sendEntityMessage(1, 15263739, this, "Telos breaks free from its bindings.", true);
        }
    }

    public void processRockFall() {
        if (rockFallCycles != 0 && Utils.currentTimeMillis() >= rockFallCycles && !hasFinished() && !dieing && !isDead()) {
            rockFallCycles = Utils.currentTimeMillis() + 12000;
            sendRockFall();
        }
    }

    private void sendRockFall() {
        WorldTile tile = new WorldTile(getPlayer());
        World.sendGraphics(null, new Graphics(5917), tile);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (getPlayer() == null || tile == null)
                    return;
                World.sendGraphics(null, new Graphics(6216), tile);
                if (getPlayer().withinDistance(tile, 1))
                    getPlayer().applyHit(new Hit(Telos.this, Utils.random(200, 301), HitLook.REGULAR_DAMAGE));
            }
        }, 3);
    }

    private void addGolems() {
        if (phase == 2 && spawnGolemsCycles != 0 && Utils.currentTimeMillis() >= spawnGolemsCycles) {
            for (int i = 0; i < golems.length; i++) {
                if (golems[i] == null) {
                    golems[i] = new AnimaGolem(22918, instance.getTile(TelosInstance.ANIMA_GOLEM_SPAWM_TILE[i]), this);
                    golems[i].setForceMultiArea(true);
                    break;
                }
            }
            spawnGolemsCycles = Utils.currentTimeMillis() + 30000;
        }
    }

    public void processStream() {
        if (phase != 3 && spawnStreamCycles != 0 && Utils.currentTimeMillis() >= spawnStreamCycles) {
            spawnStreamCycles = Utils.currentTimeMillis() + 20000;
            if (switchingPhase)
                return;
            instance.createStream();
        }
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if (nullDamages())
            hit.setDamage(0);
        if (getCapDamage() != -1 && hit.getDamage() > getCapDamage())
            hit.setDamage(getCapDamage());
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
            return;
        Entity source = hit.getSource();
        if (source == null)
            return;
        if (source instanceof Player) {
            ((Player) source).getControlerManager().processIncomingHit(hit, this);
            if (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE) {
                handlePrayers(hit);
            }
        }
        if (getEffectsManager().hasActiveEffect(EffectType.BLACK_STREAM))
            hit.setDamage((int) (hit.getDamage() * 0.7));
        if (getEffectsManager().hasActiveEffect(EffectType.RED_STREAM))
            hit.setDamage((int) (hit.getDamage() * 1.3));
        if (getTemporaryAttributtes().get("GripAttack") != null) {
            int damageLeft = (int) getTemporaryAttributtes().get("GripAttack");
            if (damageLeft - hit.getDamage() <= 0) {
                getTemporaryAttributtes().remove("GripAttack");
                setNextAnimation(new Animation(28932));
                setCantDoDefenceEmote(false);
                if (getTendril() != null)
                    World.removeObject(getTendril());
                getCombat().setCombatDelay(getAttackSpeed());
                setTendril(null);
                gripCycle = 0;
            } else
                getTemporaryAttributtes().put("GripAttack", damageLeft - hit.getDamage());
        }

    }

    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);
        if (((getHitpoints() <= (getMaxHitpoints() * 0.75) && phase == 0) || (getHitpoints() <= (getMaxHitpoints() * 0.50) && phase == 1) || (getHitpoints() <= (getMaxHitpoints() * 0.25) && phase == 2))) {
            switchPhase();
        }
        updateInterface();
    }

    public boolean startInstantBombAttack() {
        if (phase == 3 && ((fontAttackRotation == 0 && getHitpoints() <= (getMaxHitpoints() * 0.1875)) || (fontAttackRotation == 1 && getHitpoints() <= (getMaxHitpoints() * 0.125)) || (fontAttackRotation == 2 && getHitpoints() <= (getMaxHitpoints() * 0.0625)))) {
            fontAttackRotation++;
            return true;
        }
        return false;
    }

    @Override
    public boolean checkAgressivity() {
        if (getHitpoints() == 0)
            return false;
        if (wakingUp || getId() == 22892)
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (Player player : instance.getPlayers()) {
            if (player == null || player.isDead())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public boolean isStunImmune() {
        return false;
    }

    public void switchPhase() {
        if (switchingPhase || phase >= 3)
            return;
        final WorldTile telosSpawnTile = instance.getTile(TelosInstance.TELOS_SPAWN_TILE[phase]);
        final WorldTile playerJumpTile = instance.getTile(TelosInstance.PLAYER_JUMP_TILE[phase]);
        final WorldTile playerStartTile = instance.getTile(TelosInstance.PLAYER_START_TILE[phase + 1]);
        final WorldTile telosStartTile = instance.getTile(TelosInstance.TELOS_SPAWN_TILE[phase + 1]);
        switchingPhase = true;
        Player player = getPlayer();
        if (player == null || player.isDead())
            return;
        setCantInteract(true);
        setNextFaceEntity(null);
        setNextFaceWorldTile(telosSpawnTile);
        setNextAnimation(new Animation(28963));
        boolean jump = false;
        if (!Utils.colides(this, telosSpawnTile, getSize(), 1)) {
            byte[] dir = Utils.getDirection(Utils.getAngle(telosSpawnTile.getX() - getX(), telosSpawnTile.getY() - getY()));
            resetWalkSteps();
            setNextForceMovement(new ForceMovement(telosSpawnTile, 1, Utils.getAngle(dir[0], dir[1])));
            jump = true;
        }
        getTemporaryAttributtes().remove("GripAttack");
        player.unlock();
        player.stopAll(true, true, false);
        player.resetWalkSteps();
        player.resetReceivedDamage();
        resetPlayerEffects();
        player.lock();
        player.setRun(true);
        player.addWalkSteps(playerJumpTile.getX(), playerJumpTile.getY(), 100, false);
        player.getPackets().sendEntityMessage(1, 15263739, player, "Telos begins to smash the platform!", true);
        final boolean tele = jump;
        Region region = World.getRegion(player.getRegionId());
        if (region != null)
            region.removeProjectiles();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;
            int jumpLoop;

            @Override
            public void run() {
                if (player.isDead() || instance.isFinished()) {
                    stop();
                    return;
                }
                if (jumpLoop == 0 && loop > 40 && !player.matches(playerJumpTile)) {
                    player.setNextWorldTile(playerJumpTile);
                }
                resetPlayerEffects();
                if (tele && loop == 0)
                    setNextWorldTile(telosSpawnTile);
                if (jumpLoop == 0 && !player.matches(playerJumpTile)) {
                    player.stopAll(false, true, true);
                    player.addWalkSteps(playerJumpTile.getX(), playerJumpTile.getY(), 100, false);
                }
                if (jumpLoop == 0 && player.matches(playerJumpTile)) {
                    jumpLoop = loop;
                    player.setNextAnimation(new Animation(29009));
                } else if (jumpLoop != 0 && loop == jumpLoop + 1) {
                    player.getInterfaceManager().sendFadingInterface(120);
                } else if (jumpLoop != 0 && loop == jumpLoop + 3) {
                    player.getInterfaceManager().closeFadingInterface();
                    player.setNextWorldTile(playerStartTile);
                    player.getLocalNPCUpdate().reset();
                    player.getPackets().sendLocalNPCsUpdate();
                    setNextWorldTile(telosStartTile);
                    player.setNextAnimation(new Animation(29010));
                    setNextAnimation(new Animation(28930));
                    setPhase(phase + 1);
                } else if (jumpLoop != 0 && loop == jumpLoop + 4) {
                    switch (phase) {
                    case 1:
                        player.getPackets().sendCameraPos(3897, 6969, 2900, -1, -1);
                        player.getPackets().sendCameraLook(3864, 6936, 2900);
                        break;
                    case 2:
                        player.getPackets().sendCameraPos(3882, 6969, 2900, -1, -1);
                        player.getPackets().sendCameraLook(3914, 6936, 2900);
                        break;
                    case 3:
                        player.getPackets().sendCameraPos(3901, 6956, 2900, -1, -1);
                        player.getPackets().sendCameraLook(3860, 6979, 2900);
                        break;
                    }
                } else if (jumpLoop != 0 && loop == jumpLoop + 6) {
                    player.unlock();
                    player.getPackets().sendResetCamera();
                    resetWalkSteps();
                    setCantDoDefenceEmote(true);

                    player.resetWalkSteps();
                    getTemporaryAttributtes().put("CantMove", true);
                } else if (jumpLoop != 0 && loop == jumpLoop + 8) {
                    switchingPhase = false;
                    resetOnPhaseStart();
                    resetPlayerEffects();
                    player.getActionManager().setAction(new PlayerCombat(Telos.this));
                    setNextFaceEntity(player);
                    setTarget(player);
                    stop();
                    return;
                }
                if (jumpLoop == 0 || (jumpLoop != 0 && loop >= jumpLoop && loop <= jumpLoop + 2)) {
                    if (loop % 1 == 0)
                        setNextAnimation(new Animation(28963));
                }
                loop++;
            }
        }, 1, 1);
    }

    private void resetOnPhaseStart() {
        clearArea();
        spawnStreamCycles = Utils.currentTimeMillis() + (phase == 4 ? 800 : 8000);
        setAnima(phase == 2 ? 50 : 0);
        resetPlayerEffects();
        getCombat().addCombatDelay(getAttackSpeed());
        getTemporaryAttributtes().clear();
        setCantDoDefenceEmote(false);
        setCantInteract(false);
        setForceFollowClose(phase <= 2);
        gripCycle = 0;
        spawnGolemsCycles = phase == 2 ? Utils.currentTimeMillis() + 30000 : 0;
        autoAttacks = phase == 4 ? 0 : phase == 2 ? 7 : 3;
        attackRotation = phase == 4 ? 0 : attackRotation;
        rockFallCycles = (phase == 4 || phase == 3 && getPlayer().getTelosEnrage() >= 1000) ? (Utils.currentTimeMillis() + 20000) : 0;
        setCantFollowUnderCombat(phase == 4);
    }

    public void resetCombatDelay() {
        if (resetCombatDelayCycle != 0 && Utils.currentTimeMillis() >= resetCombatDelayCycle) {
            getCombat().setCombatDelay(1);
            addResetAttackDelayCycle();
        }
    }

    public void addResetAttackDelayCycle() {
        resetCombatDelayCycle = Utils.currentTimeMillis() + 30000;
    }

    public void processRedBarSpecialAttack(boolean triple) {
        if (getPlayer() == null)
            return;
        if (phase != 2)
            return;
        Player player = getPlayer();
        int enrage = player.getTelosEnrage() >= 250 ? 250 : player.getTelosEnrage();
        int min = 20 + (enrage / 10);
        int max = 80 - (enrage / 10);
        if (anima <= min) {// drain
            int specDrainAmount = 10 + ((int) (((double) player.getTelosEnrage() / 4000.00) * 10));
            if (player.getCombatDefinitions().getSpecialAttackPercentage() >= specDrainAmount) {
                player.getCombatDefinitions().decreaseSpecialAttack(specDrainAmount, false);
            }
            player.getPrayer().drainPrayer(10 + ((int) (((double) player.getTelosEnrage() / 4000.00) * 10)));
        } else if (anima >= max) {// shockwave
            int phase = this.phase;
            Telos thisTelos = this;
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    if (getPhase() != phase || switchingPhase) {
                        stop();
                        return;
                    }
                    if (isDead() || getInstance().isFinished() || getPlayer() == null || getPlayer().isDead()) {
                        stop();
                        return;
                    }

                    int damage = 200 + ((int) (((double) player.getTelosEnrage() / 4000.00) * 200.00));
                    World.sendGraphics(thisTelos, new Graphics(4869), new WorldTile(thisTelos));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.applyHit(new Hit(thisTelos, damage, HitLook.REGULAR_DAMAGE));

                        }
                    }, 1);
                    if (!triple || loop >= 2) {
                        stop();
                        return;
                    }
                    loop++;
                }

            }, 0, 1);
        }
    }

    private void clearArea() {
        if (getTendril() != null) {
            World.removeObject(getTendril());
            setTendril(null);
        }
        for (int i = 0; i < golems.length; i++)
            if (golems[i] != null) {
                golems[i].sendDeath(this);
                golems[i] = null;
            }
        if (instance != null)
            instance.destroyStreams();
    }

    public void resetPlayerEffects() {
        if (getPlayer() == null)
            return;
        Player player = getPlayer();
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.GREEN_VIRUS);
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.BLACK_VIRUS);
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.RED_VIRUS);
        player.getTemporaryAttributtes().remove(Key.FORCE_REMOVE_EFFECT_1);
        player.getEffectsManager().removeEffect(EffectType.GREEN_STREAM);
        player.getEffectsManager().removeEffect(EffectType.BLACK_STREAM);
        player.getEffectsManager().removeEffect(EffectType.RED_STREAM);
        player.getEffectsManager().removeEffect(EffectType.ANIMA_CONSUMPTION);
        updateInterface();
    }

    public boolean nullDamages() {
        return switchingPhase || hasFinished() || isDead() || dieing;
    }

    public boolean isDieing() {
        return dieing;
    }

    public void setDieing(boolean dieing) {
        this.dieing = dieing;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Hit handleOutgoingHit(Hit hit, Entity target) {
        if (nullDamages())
            hit.setDamage(0);
        if (getEffectsManager().hasActiveEffect(EffectType.BLACK_STREAM))
            hit.setDamage((int) (hit.getDamage() * 0.7));
        if (getEffectsManager().hasActiveEffect(EffectType.RED_STREAM))
            hit.setDamage((int) (hit.getDamage() * 1.3));
        return super.handleOutgoingHit(hit, target);
    }

    public Player getPlayer() {
        return instance.getPlayer();
    }

    @Override
    public int getMaxHitpoints() {
        if (instance == null)
            return 1;
        int increase = getPlayer() == null ? 0 : (phase <= 3 ? ((getPlayer().getTelosEnrage() >= 200 ? 200 : getPlayer().getTelosEnrage()) * 100) : (((getPlayer().getTelosEnrage() >= 300 ? 300 : getPlayer().getTelosEnrage()) - 100) * 50));
        return phase <= 3 ? (40000 + (increase)) : 10000 + increase;
    }

    public void setDefinitions() {
        setHitpoints(getMaxHitpoints());
        setBonuses();
    }

    public void setBonuses() {
        int[] bonuses = getDefinitions().getCacheBonuses();
        // Don't use getPlayer() in here, will throw NPE on startup. - lare96i
        // int enrage = getPlayer().getTelosEnrage();
        for (int i = 0; i < bonuses.length; i++)
            bonuses[i] = (int) ((double) bonuses[i] + ((double) bonuses[i] * 0.001));
        this.setBonuses(bonuses);
    }

    public int getAnima() {
        return anima;
    }

    public void setAnima(int anima) {
        this.anima = anima;
        if (anima > 0)
            addAnimaBar();
        updateInterface();
    }

    public void increaseAnima(int amount) {
        if (anima >= 100 || switchingPhase)
            return;
        setAnima(anima + amount >= 100 ? 100 : anima + amount);
    }

    public void decreaseAnima(int amount) {
        if (anima <= 0 || switchingPhase)
            return;
        setAnima(anima - amount <= 0 ? 0 : anima - amount);
    }

    public void addAnimaBar() {
        // getNextHitBars().add(new AnimaHitBar(this));
    }

    public void setAnimaBombAttackRotation() {
        if (animaBombAttackRotations.isEmpty()) {
            for (int i = 0; i < 3; i++)
                animaBombAttackRotations.add(i);
        }
        Collections.shuffle(animaBombAttackRotations);
    }

    public void removeSelectedAnimaBombAttackRotation() {
        if (animaBombAttackRotations.size() <= 1)
            animaBombAttackRotations.clear();
        else
            animaBombAttackRotations.remove(0);
        if (animaBombAttackRotations.isEmpty()) {
            for (int i = 0; i < 3; i++)
                animaBombAttackRotations.add(i);
        }
    }

    public int getSelectedAnimaBombAttackRotation() {
        return animaBombAttackRotations.get(0);
    }

    public void updateInterface() {
        Player player = getPlayer();
        if (player == null)
            return;
        player.getPackets().sendHideIComponent(1770, 0, getPhase() == 2);
        player.getPackets().sendHideIComponent(1770, 1, getPhase() != 2);
        player.getPackets().sendHideIComponent(1770, 2, getPhase() != 2);
        player.getPackets().sendHideIComponent(1770, 3, getPhase() != 2);
        player.getPackets().sendConfig(5776, 21174);
        player.getVarBitManager().sendVarBit(32672, getMaxHitpoints() * 10);
        player.getVarBitManager().sendVarBit(28663, getHitpoints() * 10);
        player.getVarBitManager().sendVarBit(32640, getPhase());
        player.getVarBitManager().sendVarBit(32626, player.getTelosEnrage());
        player.getVarBitManager().sendVarBit(32639, getAnima());
    }

    public WorldObject getTendril() {
        return tendril;
    }

    public void setTendril(WorldObject tendril) {
        this.tendril = tendril;
    }

    @Override
    public boolean canWalkNPC(int toX, int toY) {
        return getTemporaryAttributtes().get("GripAttack") == null && getTemporaryAttributtes().get("CantMove") == null && super.canWalkNPC(toX, toY);
    }

    @Override
    public int getAttackSpeed() {
        int attackSpeed = 4;
        if (this.getEffectsManager().hasActiveEffect(EffectType.GREEN_STREAM))
            attackSpeed--;
        return attackSpeed;
    }

    @Override
    public void finish() {
        clearArea();
        removeFonts();
        super.finish();
    }

    @Override
    public void sendDeath(Entity source) {
        Player player = getPlayer();
        if (player == null || player.isDead())
            return;
        if (dieing)
            return;
        dieing = true;
        spawnStreamCycles = 0;
        clearArea();
        removeFonts();
        resetWalkSteps();
        resetPlayerEffects();
        getCombat().removeTarget();
        setCantInteract(true);
        setNextFaceEntity(null);
        if (phase == 4) {
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    resetPlayerEffects();
                    if (loop == 0) {
                        setNextAnimation(new Animation(28957));
                    } else if (loop >= 5) {
                        spawnStreamCycles = 0;
                        prepareReward();
                        instance.getExitBeam().setId(103568);
                        World.spawnObject(instance.getExitBeam());
                        player.getPackets().closeInterface(InterfaceManager.getComponentUId(1648, 25));
                        player.getInterfaceManager().closeOverlay(true);
                        reset();
                        finish();
                        getTemporaryAttributtes().put("dead", Boolean.TRUE);
                        if (source != null && source.getAttackedBy() == Telos.this) {
                            // after u kill
                            source.setAttackedByDelay(0);
                            source.setAttackedBy(null);
                            source.setFindTargetDelay(0);
                        }
                        instance.destroyStreams();
                        stop();
                    }
                    loop++;
                }
            }, 0, 1);
            return;
        }
        faceEntity(player);
        player.lock();
        player.stopAll(true, true, true);
        player.getInterfaceManager().sendFadingInterface(120);
        WorldTile telosDeathWorldTile = instance.getTile(TelosInstance.TELOS_DEATH_TILE);
        WorldTile playerStartTile = instance.getTile(TelosInstance.PLAYER_START_TILE[4]);
        WorldTile telosSpawnLocation = instance.getTile(TelosInstance.TELOS_SPAWN_TILE[4]);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                resetPlayerEffects();
                if (loop == 1) {
                    setNextWorldTile(telosDeathWorldTile);
                    player.setNextWorldTile(playerStartTile);
                    World.removeObject(instance.getExitBeam());
                    instance.getExitBeam().setId(103568);
                    setNextFaceWorldTile(new WorldTile(telosDeathWorldTile.getX() + 1, telosDeathWorldTile.getY() - getSize(), getPlane()));
                    player.faceEntity(Telos.this);
                    player.getPackets().sendCameraPos(3903, 6930, 3700, -1, -1);
                    player.getPackets().sendCameraLook(3905, 7167, 2900);
                } else if (loop == 2) {
                    player.getInterfaceManager().closeFadingInterface();
                    setNextAnimation(new Animation(28964));
                    setNextGraphics(new Graphics(6260));
                } else if (loop == 6) {
                    finish();
                    if (source != null && source.getAttackedBy() == Telos.this) {
                        // after u kill
                        source.setAttackedByDelay(0);
                        source.setAttackedBy(null);
                        source.setFindTargetDelay(0);
                    }
                } else if (loop == 7) {
                    player.getPackets().closeInterface(InterfaceManager.getComponentUId(1648, 25));
                    player.getInterfaceManager().closeOverlay(true);
                } else if (loop == 8) {// respawn this npc for phase 5/ spawn
                                       // replace exit object
                    if (player.getTelosEnrage() >= 100) {
                        reset();
                        setNextNPCTransformation(22908);

                        setPhase(phase + 1);
                        setDefinitions();
                        resetOnPhaseStart();
                        spawnFonts();
                        setLocation(telosSpawnLocation);
                        spawn();
                        faceEntity(player);
                        setNextAnimation(new Animation(28941));

                    } else {
                        instance.destroyStreams();
                        getTemporaryAttributtes().put("dead", Boolean.TRUE);
                        prepareReward();
                        World.spawnObject(instance.getExitBeam());
                    }
                } else if (loop == 9) {
                    dieing = false;
                    player.unlock();
                    player.getPackets().sendResetCamera();
                    if (phase == 4) {
                        getPlayer().getControlerManager().sendInterfaces();
                        if (getPlayer() != null && !getPlayer().hasFinished())
                            getCombat().setTarget(getPlayer());
                    }
                    stop();
                    // send controler interface and spawn exit object if enrage
                    // <= 99
                }
                loop++;
            }

        }, 0, 1);
    }

    public static final int[][] REGULAR_DROPS = { { 995, 225000, 5512000, 1 }, { 2361, 91, 528, 1 }, { 1751, 99, 693, 1 }, { 3049, 68, 444, 1 }, { 219, 69, 425, 1 }, { 217, 68, 312, 1 }, { 1444, 48, 265, 1 }, { 989, 13, 134, 1 }, { 1432, 68, 639, 1 }, { 15270, 138, 1020, 1 }, { 1631, 46, 383, 1 }, { 6571, 1, 12, 1 }, { 9194, 107, 833, 1 }, { 11232, 500, 3337, 1 }, { 11237, 1137, 7312, 1 }, { 2363, 30, 378, 1 }, { 28257, 13, 99, 1 } };

    public static final int[][] UNIQUE_DROPS = { { 37619, 1, 1, 1 }, { 37620, 1, 1, 1 }, { 37621, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37619, 1, 1, 1 }, { 37622, 1, 1, 1 }, { 37624, 1, 1, 1 }, { 37626, 1, 1, 1 }/* , { 37630, 1, 1, 1 } */ };

    public static final int TELOS_PET = 37679;

    private final int KILLSTREAK_LIMIT = 100;

    public void prepareReward() {
        Player player = getPlayer();
        if (player == null)
            return;
        int enrage = getPlayer().getTelosEnrage();
        int killStreak = player.getTelosStreak();
        if (killStreak < 1) {
            killStreak = 1;
        }
        if (killStreak > KILLSTREAK_LIMIT) {
            killStreak = KILLSTREAK_LIMIT;
        }
        double uniqueChance = 1 / (10000.00 / (10.00 + (0.04 * (enrage)) + (1.00 * killStreak)));
        double petChance = enrage >= 100 ? (1.00 / 700.00) : (1.00 / 1400.00);
        if(ContractHandler.isContractNpc(player, this)) {
            uniqueChance += uniqueChance * 0.05;
            petChance += petChance * 0.05;
        }
        double roll = Utils.randomDouble();
        if (enrage >= 0 && enrage <= 24)
            uniqueChance += (uniqueChance / 1.50);
        else if (enrage >= 25 && enrage <= 99)
            uniqueChance += uniqueChance;
        else
            uniqueChance += (uniqueChance * 1.25);
        // do u have custom drop rates? yeah uhh
        uniqueChance *= Settings.getDropQuantityRate(player);
        boolean hasPet = false;
        if (!player.getTelosRewards().isEmpty())
            for (Item item : player.getTelosRewards()) {
                if (item == null)
                    continue;
                if (item.getId() == TELOS_PET)
                    hasPet = true;
            }
        if (!hasPet)
            hasPet = player.getInventory().containsItem(TELOS_PET, 1) || player.getBank().containsItem(TELOS_PET, 1) || (player.getPet() != null && player.getPet().getItemId() == TELOS_PET) || PetPerkUtils.getObtainedPetByItem(player, TELOS_PET) != null;
        if (!hasPet && roll <= petChance) {
            Item reward = new Item(TELOS_PET, 1);
            player.setCurrentTelosReward(reward);
            World.sendNews(player, player.getDisplayName() + " has received " + reward.getName() + " drop!", World.WORLD_NEWS);
        } else if (roll <= uniqueChance/* || player.isOwner()*/) {
            int rewardIndex = Utils.random(UNIQUE_DROPS.length);
            Item reward = new Item(UNIQUE_DROPS[rewardIndex][0], 1);
            if (reward.getId() >= 37619 && reward.getId() <= 37621) {
                int[] amounts = new int[3];
                int lowest = Integer.MAX_VALUE;
                if (!player.getTelosRewards().isEmpty())
                    for (Item item : player.getTelosRewards()) {
                        if (item == null)
                            continue;
                        if (item.getId() >= 37619 && item.getId() <= 37621)
                            amounts[item.getId() - 37619] += item.getAmount();
                    }
                for (Bank bank : player.getBanks()) {
                    if (bank == null)
                        continue;
                    for (int i = 37619; i <= 37621; i++) {
                        Item item = bank.getItem(i);
                        if (item == null)
                            continue;
                        amounts[item.getId() - 37619] += item.getAmount();
                    }
                }
                int index = 0;
                for (int i = 0; i < 3; i++)
                    if ((player.getInventory().getAmountOf(37619 + i) + amounts[i]) < lowest) {
                        index = i;
                        lowest = (player.getInventory().getAmountOf(37619 + i) + amounts[i]);
                    }
                reward = new Item(37619 + index, 1);
            }
            player.getDropCollectionHandler().handleBossKills(reward, DropCollectionConstants.TELOS_ID);
            player.setCurrentTelosReward(reward);
            World.sendNews(player, player.getDisplayName() + " has received " + reward.getName() + " drop!", World.WORLD_NEWS);
        } else {
            int rewardIndex = Utils.random(REGULAR_DROPS.length);
            int min = REGULAR_DROPS[rewardIndex][1];
            int max = REGULAR_DROPS[rewardIndex][2];
            int amount = (int) (min + ((max - min) * ((double) killStreak / 200.00)));
            Item reward = new Item(REGULAR_DROPS[rewardIndex][0], amount);
            player.setCurrentTelosReward(reward);
        }
        player.increaseKillStatistics("telos", true);
        player.sendMessage(Colors.wrap(Colors.RED, getName()) + " Kill count: " + Colors.wrap(Colors.RED, String.valueOf(player.getKillStatistics(133))));
        ContractHandler.updateContract(player, this);
        HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.TELOS_THE_WARDEN);
        if (player.isGroupIronman()) {
            int bp;
            if(enrage >= 100) {
                bp = 10;
            } else if(enrage >= 75) {
                bp = 9;
            } else if(enrage >= 50) {
                bp = 8;
            } else if(enrage >= 25) {
                bp = 7;
            } else {
                bp = 6;
            }
            player.gimTracker.incrementBpGained(bp);
        }
        // add telos kc here okay? TODO
        // player.getMonstersKillCountManager().increaseMonsterKills(Monsters.TELOS);
        // add telos kc
        // player.getTimersManager().removeTimer(RecordKey.TELOS);
        // int amount = TimersManager.getBossPoints(RecordKey.TELOS);
        // player.setBossPoints(player.getBossPoints() + amount *
        // (Settings.DOUBLE_BOSS_POINTS ? 2 : 1));
        // player.getPackets().sendGameMessage(Color.PURPLE,
        // "You received " + amount * (Settings.DOUBLE_BOSS_POINTS ? 2 : 1) + "
        // boss "
        // + (amount > 1 ? "points" : "point") + " and now have " +
        // player.getBossPoints() + " "
        // + (player.getBossPoints() > 1 ? "points" : "point") + ".",
        // true);
    }

    public void stun(Font font) {
        if (getTemporaryAttributtes().get("STUNNED") != null)
            return;
        int fontColor = font.getId() - 22915;
        if (getSelectedAnimaBombAttackRotation() != fontColor) {
            getPlayer().getPackets().sendGameMessage("Using this font wont interrupt the attack.");
            return;
        }
        getTemporaryAttributtes().put("STUNNED", Boolean.TRUE);
        removeSelectedAnimaBombAttackRotation();
        setNextAnimation(new Animation(28954));
        setNextGraphics(new Graphics(-1));
        setNextFaceEntity(null);
        setNextFaceWorldTile(getPlayer());
        setCantDoDefenceEmote(true);
        font.setCharge(font.getCharge() - 4);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                setNextAnimation(new Animation(28955));
                setNextAnimation(new Animation(-1));
                setNextFaceEntity(getPlayer());
                setCantDoDefenceEmote(false);
                getCombat().setCombatDelay(0);
            }
        }, 15);
    }

    @Override
    public boolean isDead() {
        return !dieing && getHitpoints() == 0;
    }

    @Override
    public boolean restoreHitPoints() {
        return !dieing && super.restoreHitPoints();
    }

    @Override
    public int getMaxHit() {
        return 600;
    }

    @Override
    public int getMaxHit(int style) {
        return 600;
    }

    public int getAttackRotation() {
        return attackRotation;
    }

    public void setAttackRotation(int attackRotation) {
        this.attackRotation = attackRotation;
    }

    public void increaseAttackRotation() {
        this.attackRotation = attackRotation + 1 >= SpecialAttacks[phase].length ? 0 : attackRotation + 1;
    }

    public static final TelosAttacks[][] SpecialAttacks = { { TelosAttacks.GRIP, TelosAttacks.UPPERCUT, TelosAttacks.STUN, null, null }, { TelosAttacks.GRIP, TelosAttacks.MAGIC_ONSLAUGHT, TelosAttacks.STUN, TelosAttacks.VIRUS, TelosAttacks.UPPERCUT }, { TelosAttacks.UPPERCUT, TelosAttacks.STUN, TelosAttacks.VIRUS, null, null }, { TelosAttacks.UPPERCUT, TelosAttacks.WEAK_ANIMA_BOMB, TelosAttacks.STUN, null, null }, { TelosAttacks.MINIONS, TelosAttacks.VIRUS, TelosAttacks.ANIMA_BOMB, null, null } };

    public enum TelosAttacks {
        SWIPE_MAGICAL_DISCHARGE() {
            public int sendAttack(Telos telos, Player target) {
                if (Utils.isOnRange(telos, target, 0) && telos.getPhase() != 2) {
                    telos.setNextAnimation(new Animation(28973));
                    CombatScript.delayHit(telos, 0, target, CombatScript.getMeleeHit(telos, CombatScript.getMaxHit(telos, telos.getMaxHit(NPCCombatDefinitionConstants.MELEE), NPCCombatDefinitionConstants.MELEE, target)));
                } else {
                    telos.setNextAnimation(new Animation(28959));
                    if (telos.getPhase() <= 2)
                        telos.decreaseAnima(2);
                    int damage = CombatScript.getMaxHit(telos, NPCCombatDefinitionConstants.MAGE, target);
                    int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6261, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                    CombatScript.delayHit(telos, delay, target, CombatScript.getMagicHit(telos, damage));

                }
                return telos.getAttackSpeed();
            }
        },
        GRIP() {
            @Override
            public int sendAttack(Telos telos, Player target) {
                if (!Utils.isOnRange(telos, target, PlayerCombat.getAttackRange(target))) {
                    byte[] dir = Utils.getDirection(telos.getDirection());
                    WorldTile dragTo = telos.getMiddleWorldTile().transform((3 * dir[0]), (3 * dir[1]), 0);
                    target.resetWalkSteps();
                    target.setNextForceMovement(new ForceMovement(dragTo, 1, Utils.getAngle(dir[0], dir[1])));
                    target.setNextAnimation(new Animation(14388));
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            target.setNextWorldTile(dragTo);
                        }
                    });
                }
                telos.setNextForceTalk(new ForceTalk("Your anima will return to the source!"));
                int enrage = telos.getPlayer().getTelosEnrage() >= 2700 ? 2700 : telos.getPlayer().getTelosEnrage();
                final int damageToBreak = (300 + (enrage));
                telos.resetWalkSteps();
                telos.getTemporaryAttributtes().put("GripAttack", damageToBreak);
                telos.setCantDoDefenceEmote(true);
                telos.setNextAnimation(new Animation(28931));
                target.resetWalkSteps();
                target.lock();
                int phase = telos.getPhase();
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    boolean started;

                    @Override
                    public boolean repeat() {
                        if (telos.getPhase() != phase || telos.switchingPhase || telos.dieing) {
                            telos.setCantDoDefenceEmote(false);
                            if (telos.getTendril() != null)
                                World.removeObject(telos.getTendril());
                            telos.setTendril(null);
                            telos.gripCycle = 0;
                            target.unlock();
                            telos.getTemporaryAttributtes().remove("GripAttack");
                            return false;
                        }
                        if (telos.dieing || telos.isDead() || telos.getInstance().isFinished() || telos.getTemporaryAttributtes().get("GripAttack") == null || telos.switchingPhase || target == null || target.isDead()) {
                            telos.setCantDoDefenceEmote(false);
                            if (telos.getTendril() != null)
                                World.removeObject(telos.getTendril());
                            telos.setTendril(null);
                            telos.gripCycle = 0;
                            target.unlock();
                            return false;
                        }
                        if (!started) {
                            WorldObject tendril = new WorldObject(103553, 4, 3, new WorldTile(target.getX() - 1, target.getY(), target.getPlane()));
                            telos.setTendril(tendril);
                            World.spawnObject(tendril);
                            target.unlock();
                            target.getPackets().sendEntityMessage(1, 15263739, target, "Deal " + damageToBreak + " damage to break Telos's grip!", true);
                            telos.gripCycle = Utils.currentTimeMillis() + 600;
                            started = true;
                            return false;
                        }
                        target.unlock();
                        telos.setNextAnimation(new Animation(28932));
                        telos.getTemporaryAttributtes().remove("GripAttack");
                        telos.setCantDoDefenceEmote(false);
                        World.removeObject(telos.getTendril());
                        telos.getCombat().setCombatDelay(telos.getAttackSpeed());
                        telos.setTendril(null);
                        telos.gripCycle = 0;
                        return true;
                    }

                }, 600, 600 * 25, TimeUnit.MILLISECONDS);
                return -1;
            }

        },

        UPPERCUT() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                telos.resetWalkSteps();
                telos.setNextForceTalk(new ForceTalk("Ataraxia, give me strength!"));
                telos.setNextAnimation(new Animation(28938));
                telos.setNextGraphics(new Graphics(6246));
                telos.setCantDoDefenceEmote(true);
                telos.decreaseAnima(10);
                telos.setNextFaceEntity(null);
                telos.setNextFaceWorldTile(target);
                int phase = telos.getPhase();
                WorldTasksManager.schedule(new WorldTask() {
                    int loop = 0;
                    WorldTile flyTo;
                    boolean fly;

                    @Override
                    public void run() {
                        if (telos.getPhase() != phase || telos.switchingPhase) {
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setCantDoDefenceEmote(false);
                            stop();
                            return;
                        }
                        if (target == null || target.isDead() || telos.isDead() || telos.dieing) {
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setCantDoDefenceEmote(false);
                            stop();
                            return;
                        }
                        if (loop == 1)
                            flyTo = new WorldTile(target).transform(-2, -2, 0);
                        if (loop == 2) {
                            byte[] dir = Utils.getDirection(Utils.getAngle(target.getX() - telos.getX(), target.getY() - telos.getY()));
                            if (!Utils.isOnRange(telos, target, 0)) {
                                telos.resetWalkSteps();
                                telos.setNextForceMovement(new ForceMovement(flyTo, 1, Utils.getAngle(dir[0], dir[1])));
                                fly = true;
                            }
                        } else if (loop >= 3) {
                            if (fly)
                                telos.setNextWorldTile(flyTo);
                            if (Utils.isOnRange(flyTo, target, 0, telos.getSize(), target.getSize())) {
                                int damage = Utils.random(100, 301);
                                target.applyHit(new Hit(telos, damage, HitLook.REGULAR_DAMAGE));
                            }
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setCantDoDefenceEmote(false);
                            telos.setNextFaceEntity(target);
                            stop();
                        }
                        loop++;
                    }
                }, 0, 1);
                return -1;
            }

        },

        STUN() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                telos.resetWalkSteps();
                telos.setNextForceTalk(new ForceTalk("Hold still, invader."));
                telos.setNextGraphics(new Graphics(6245));
                telos.setNextAnimation(new Animation(28935));
                telos.getTemporaryAttributtes().put("CantMove", Boolean.TRUE);
                if (Utils.isOnRange(telos, target, 0)) {
                    CombatScript.delayHit(telos, 0, target, CombatScript.getMeleeHit(telos, CombatScript.getMaxHit(telos, telos.getMaxHit(NPCCombatDefinitionConstants.MELEE), NPCCombatDefinitionConstants.MELEE, target)));
                } else {
                    int damage = CombatScript.getMaxHit(telos, NPCCombatDefinitionConstants.MAGE, target);
                    if (telos.getPhase() <= 2)
                        telos.decreaseAnima(2);
                    int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6261, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                    CombatScript.delayHit(telos, delay, target, CombatScript.getMagicHit(telos, damage));
                }
                int phase = telos.getPhase();
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;
                    WorldTile flyTo;

                    @Override
                    public void run() {
                        if (telos.getPhase() != phase || telos.switchingPhase) {
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            stop();
                            return;
                        }
                        if (target == null || target.isDead() || telos.isDead() || telos.dieing) {
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            stop();
                            return;
                        }
                        if (loop == 1) {
                            CombatScript.delayHit(telos, 0, target, CombatScript.getMeleeHit(telos, CombatScript.getMaxHit(telos, telos.getMaxHit(NPCCombatDefinitionConstants.MELEE), NPCCombatDefinitionConstants.MELEE, target)));
                            target.resetWalkSteps();
                            target.addFreezeDelay(2000);
                            telos.resetWalkSteps();
                            flyTo = new WorldTile(target).transform(-2, -2, 0);
                            telos.setNextFaceEntity(null);
                            telos.setNextFaceWorldTile(target);
                        } else if (loop == 3) {
                            byte[] dir = Utils.getDirection(Utils.getAngle(target.getX() - telos.getX(), target.getY() - telos.getY()));
                            telos.resetWalkSteps();
                            telos.setNextForceMovement(new ForceMovement(flyTo, 2, Utils.getAngle(dir[0], dir[1])));
                            World.sendGraphics(null, new Graphics(6244), flyTo.transform(2, 2, 0));
                        } else if (loop >= 4) {
                            telos.setNextWorldTile(flyTo);
                            if (Utils.isOnRange(telos, target, 0)) {
                                int damage = Utils.random(300, 450);
                                target.applyHit(new Hit(telos, damage, HitLook.REGULAR_DAMAGE));
                                target.getPrayer().closeProtectionPrayers();
                            }
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.resetWalkSteps();
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setNextFaceEntity(target);
                            stop();
                        }
                        loop++;
                    }
                }, 0, 1);
                return -1;
            }

        },

        ANIMA_ULTIMATE() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                telos.setNextForceTalk(new ForceTalk("SO. MUCH. POWER!"));
                telos.setNextAnimation(new Animation(28942));
                telos.decreaseAnima(100);
                if (Utils.isOnRange(telos, target, 0)) {
                    CombatScript.delayHit(telos, 0, target, CombatScript.getMeleeHit(telos, CombatScript.getMaxHit(telos, telos.getMaxHit(NPCCombatDefinitionConstants.MELEE), NPCCombatDefinitionConstants.MELEE, target)));
                } else {
                    int damage = CombatScript.getMaxHit(telos, NPCCombatDefinitionConstants.MAGE, target);
                    int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6261, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                    CombatScript.delayHit(telos, delay, target, CombatScript.getMagicHit(telos, damage));
                }
                telos.getTemporaryAttributtes().put("CantMove", Boolean.TRUE);
                int phase = telos.getPhase();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (telos.getPhase() != phase || telos.switchingPhase) {
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            stop();
                            return;
                        }
                        int enrage = target.getTelosEnrage() >= 100 ? 100 : target.getTelosEnrage();
                        int damage = (int) (555 + (11.1 * Math.floor(enrage / 10)));
                        int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6248, 350, 39, 30, 1, 16, 5).getEndTime()) - 1;

                        telos.getTemporaryAttributtes().remove("CantMove");
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                CombatScript.delayHit(telos, 0, target, CombatScript.getRegularHit(telos, damage));
                                target.setNextGraphics(new Graphics(6249, 0, 100));
                            }

                        }, delay);
                        telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                    }
                });
                return -1;
            }

        },

        MAGIC_ONSLAUGHT() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                telos.resetWalkSteps();
                telos.getTemporaryAttributtes().put("CantMove", Boolean.TRUE);
                int phase = telos.getPhase();
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;

                    @Override
                    public void run() {
                        if (telos.getPhase() != phase || telos.switchingPhase) {
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.getTemporaryAttributtes().remove("CantMove");
                            stop();
                            return;
                        }
                        if (loop % 2 == 0) {
                            telos.setNextAnimation(new Animation(28959));
                            int enrage = target.getTelosEnrage();

                            int damage = Math.max(Utils.random(50, 55) + (enrage / 10), Utils.random(telos.getMaxHit() + (enrage / 10)) + 1) + ((loop / 2) * (40 + (enrage / 10)));
                            if (damage > 450)
                                damage = 450;
                            int projectileId = 5700 + (((loop / 2) / 3) >= 2 ? 2 : ((loop / 2) / 3));
                            int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, projectileId, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                            CombatScript.delayHit(telos, delay, target, CombatScript.getMagicHit(telos, damage));
                            telos.addResetAttackDelayCycle();
                            telos.decreaseAnima(10);
                        }
                        if (telos.getAnima() <= 0 || target == null || target.isDead() || telos.isDead() || telos.dieing) {
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.getTemporaryAttributtes().remove("CantMove");
                            stop();
                            return;
                        }
                        loop++;
                    }
                }, 0, 1);
                return -1;
            }

        },

        VIRUS() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                if (target.getTelosEnrage() < 50) {
                    return telos.getAttackSpeed();
                }
                if (telos.getPhase() <= 2)
                    telos.decreaseAnima(2);
                int effect = telos.getPhase() <= 2 ? telos.getPhase() : Utils.random(3);
                int enrage = target.getTelosEnrage();
                int damage = Utils.random(40, 45) + (enrage / 100);
                int projectileId = 6262 + effect;
                int delay = telos.getPhase() == 4 ? 0 : Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, projectileId, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                if (telos.getPhase() <= 2)
                    telos.setNextAnimation(new Animation(28959));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        EffectType type = effect == 0 ? EffectType.GREEN_VIRUS : effect == 1 ? EffectType.BLACK_VIRUS : EffectType.RED_VIRUS;
                        target.getEffectsManager().startEffect(new Effect(type, 51, HitLook.REGULAR_DAMAGE, effect == 0 ? 6279 : effect == 1 ? 6281 : 6280, damage, 3));
                    }

                }, delay);
                return telos.getAttackSpeed();
            }

        },

        WEAK_ANIMA_BOMB() {

            public int sendAttack(Telos telos, Player target) {
                telos.setNextForceTalk(new ForceTalk("Let the anima consume you!"));
                telos.setNextAnimation(new Animation(28959));
                int enrage = target.getTelosEnrage() >= 250 ? 250 : target.getTelosEnrage();
                int damage = Utils.random(250, 250 + (enrage));
                int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6278, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        CombatScript.delayHit(telos, 0, target, CombatScript.getMagicHit(telos, damage));
                        target.getEffectsManager().startEffect(new Effect(EffectType.ANIMA_CONSUMPTION, 250, (int) (damage * 0.1)));
                    }

                }, delay);
                return telos.getAttackSpeed();
            }

        },

        FONT_SIPHON() {

            public int sendAttack(Telos telos, Player target) {
                telos.resetWalkSteps();
                telos.setCantInteract(true);
                telos.setNextFaceEntity(null);
                telos.getTemporaryAttributtes().put("CantMove", true);
                int attackIndex = telos.fontAttackRotation - 1;
                boolean jump = false;
                WorldTile siphonTile = telos.getInstance().getTile(TelosInstance.TELOS_SIPHON_TILE[attackIndex]);
                final Font font = telos.fonts[attackIndex];
                if (Utils.getDistance(telos, font) > 6 && Utils.getDistance(telos, siphonTile) > 6) {
                    telos.setNextAnimation(new Animation(28963));
                    byte[] dir = Utils.getDirection(Utils.getAngle(siphonTile.getX() - telos.getX(), siphonTile.getY() - telos.getY()));
                    telos.setNextForceMovement(new ForceMovement(siphonTile, 1, Utils.getAngle(dir[0], dir[1])));
                    jump = true;
                }
                final boolean tele = jump;
                font.startRegularChargeCycle();
                target.getPackets().sendEntityMessage(1, 15263739, target, "Telos begins to drain anima from the font. Absorb the anima first!", true);
                telos.spawnColoredGolems();
                int phase = telos.getPhase();
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;
                    int secondLoop;

                    @Override
                    public void run() {
                        if (telos.getPhase() != phase || telos.switchingPhase) {
                            telos.resetWalkSteps();
                            telos.getTemporaryAttributtes().remove("Siphoning");
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setCantInteract(false);
                            stop();
                            return;
                        }
                        if (target == null || target.isDead() || telos.isDead() || telos.dieing) {
                            telos.resetWalkSteps();
                            telos.getTemporaryAttributtes().remove("Siphoning");
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                            telos.setCantInteract(false);
                            stop();
                            return;
                        }
                        Integer currentChargePercentage = (Integer) telos.getTemporaryAttributtes().get("Siphoning");
                        if (currentChargePercentage == null)
                            currentChargePercentage = 0;
                        if (tele && loop == 0)
                            telos.setNextWorldTile(new WorldTile(siphonTile));
                        else if (loop == 1) {
                            telos.faceEntity(font);
                            telos.getTemporaryAttributtes().put("Siphoning", currentChargePercentage);
                            telos.setNextAnimation(new Animation(28931));
                            telos.addAnimaBar();
                            target.getPackets().sendEntityMessage(1, 15263739, target, "Telos is preparing to fire an anima bomb!", true);
                        }
                        if (secondLoop == 0 && currentChargePercentage < 100 && loop % 2 == 0) {
                            int charge = currentChargePercentage + 15 >= 100 ? 100 : currentChargePercentage + 15;
                            telos.getTemporaryAttributtes().put("Siphoning", charge);
                            telos.increaseAnima(7);
                            telos.addAnimaBar();
                            if (charge >= 100) {
                                telos.setNextAnimation(new Animation(28966));
                                int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6276, 300, 39, 30, 1, 16, 5).getEndTime());
                                telos.resetWalkSteps();
                                telos.setCantInteract(false);
                                telos.setNextFaceEntity(target);
                                telos.getCombat().addCombatDelay(telos.getAttackSpeed());
                                telos.getTemporaryAttributtes().remove("Siphoning");
                                telos.addAnimaBar();
                                secondLoop = loop + delay;
                            }
                        }
                        if (secondLoop != 0 && loop == secondLoop) {
                            target.setNextGraphics(new Graphics(6277, 0, 100));
                            if (Utils.isOnRange(target, font, 1) && font.isShieldActive()) {
                                target.getPackets().sendEntityMessage(1, 15263739, target, "You survived the anima bomb!", true);
                            } else {
                                target.applyHit(new Hit(telos, Integer.MAX_VALUE, HitLook.INSTANT_KILL_TYPE));
                            }
                            telos.setTarget(target);
                            telos.getTemporaryAttributtes().remove("Siphoning");
                            telos.getTemporaryAttributtes().remove("CantMove");
                            telos.updateInterface();
                            font.sendDeath(telos);
                            stop();
                            return;
                        }
                        loop++;
                    }
                }, 1, 1);
                return -1;
            }

        },

        MAGICAL_DISCHARGE() {

            @Override
            public int sendAttack(Telos telos, Player target) {
                telos.setNextAnimation(new Animation(28950));
                telos.setNextGraphics(new Graphics(6250));
                int damage = CombatScript.getMaxHit(telos, NPCCombatDefinitionConstants.MAGE, target);
                int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(telos, target, 6248, 350, 39, 24, 2, 16, 40).getEndTime()) - 1;
                CombatScript.delayHit(telos, delay, target, CombatScript.getMagicHit(telos, damage));
                return telos.getAttackSpeed();
            }

        },

        MINIONS() {

            public int sendAttack(Telos telos, Player target) {
                telos.getTemporaryAttributtes().remove("STUNNED");
                target.getPackets().sendEntityMessage(1, 15263739, target, "Kill the anima-golems at the correct font to charge it!", true);
                telos.setAnimaBombAttackRotation();
                for (int i = 0; i < telos.coloredGolems.length; i++) {
                    if (telos.coloredGolems[i] != null && !telos.coloredGolems[i].isDead() && !telos.coloredGolems[i].hasFinished()) {
                        telos.coloredGolems[i].setNextNPCTransformation(22905 + telos.getSelectedAnimaBombAttackRotation());
                        telos.coloredGolems[i].setForceMultiArea(true);
                        telos.coloredGolems[i].setTarget(telos.getPlayer());
                    }
                }
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;

                    @Override
                    public void run() {
                        if (target == null || target.isDead() || telos.isDead() || telos.dieing) {
                            stop();
                            return;
                        }
                        if (loop >= telos.coloredGolems.length) {
                            stop();
                            return;
                        }
                        ColoredAnimaGolem golem = telos.coloredGolems[loop];
                        if (golem == null || golem.isDead() || golem.hasFinished()) {
                            WorldTile checkTile = new WorldTile(target);

                            WorldTile spawnTile = new WorldTile(target);
                            // attemps to randomize tile by 4x4 area
                            for (int trycount = 0; trycount < 10; trycount++) {
                                spawnTile = new WorldTile(checkTile, 3);
                                if (World.isTileFree(checkTile.getPlane(), spawnTile.getX(), spawnTile.getY(), 1) && World.canMoveNPC(checkTile.getPlane(), spawnTile.getX(), spawnTile.getY(), 1))
                                    break;
                                spawnTile = checkTile;
                            }
                            telos.coloredGolems[loop] = new ColoredAnimaGolem(22905 + telos.getSelectedAnimaBombAttackRotation(), spawnTile, telos);
                            telos.coloredGolems[loop].setForceMultiArea(true);
                            telos.coloredGolems[loop].setTarget(telos.getPlayer());
                        }
                        loop++;
                    }
                }, 1, 1);
                return 1;
            }

        },

        ANIMA_BOMB() {

            public int sendAttack(Telos telos, Player target) {
                int animaAttackRotation = telos.getSelectedAnimaBombAttackRotation();
                telos.setNextAnimation(new Animation(28952));
                telos.setNextGraphics(new Graphics(6257 + animaAttackRotation));
                target.getPackets().sendEntityMessage(1, 15263739, target, "Telos is charging a pure bomb. Use the font to interrupt it!", true);
                World.sendGraphics(telos, new Graphics(6251 + animaAttackRotation), new WorldTile(telos));
                telos.setCantDoDefenceEmote(true);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (target == null || target.isDead() || telos.isDead() || telos.dieing || telos.getSelectedAnimaBombAttackRotation() != animaAttackRotation || telos.getTemporaryAttributtes().get("STUNNED") != null) {
                            telos.getTemporaryAttributtes().remove("STUNNED");
                            stop();
                            return;
                        }
                        telos.removeSelectedAnimaBombAttackRotation();
                        target.setNextGraphics(new Graphics(6254 + animaAttackRotation));
                        target.applyHit(new Hit(telos, Integer.MAX_VALUE, HitLook.INSTANT_KILL_TYPE));
                        telos.setCantDoDefenceEmote(false);
                        telos.getTemporaryAttributtes().remove("STUNNED");
                    }
                }, 20);
                return 23;
            }

        };

        public int sendAttack(Telos telos, Player target) {
            return 0;
        }
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.5;
    }
    

}
