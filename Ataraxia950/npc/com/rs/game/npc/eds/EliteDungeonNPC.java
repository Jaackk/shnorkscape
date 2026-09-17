package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.AdrenalineHitBar;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent.ShadowEnigma;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class EliteDungeonNPC extends NPC {

    private static final long serialVersionUID = 4300252532386602342L;
    public static int aggro_dis = 7;
    @Getter
    private final transient EliteDungeonHandledRoom room;
    public WorldTile[] walkTiles;
    private transient AdrenalineHitBar bar;
    private transient Map<Integer, Long> cooldowns;
    @Getter
    @Setter
    private transient Entity ctarget;
    private transient int currentIndex = 0;
    private transient boolean revert;
    private final transient boolean usesAdrenaline;
    private transient boolean dieing;
    private transient boolean exactWalkTiles;
    private transient long waiting, changeTargetCooldown;
    public transient boolean spawned;
    @Getter
    private WorldTile absoluteTile;
    private final int originalId;
    @Getter
    @Setter
    private transient boolean marked;

    public EliteDungeonNPC(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, -1, true, true, false);
        this.room = room;
        this.originalId = id;
        absoluteTile = room.getAbsoluteTile(tile);
        bar = new AdrenalineHitBar(this);
        cooldowns = new HashMap<Integer, Long>();
        setForceMultiArea(true);
        setFinished(true);
        usesAdrenaline = getId() != 25586 && getId() != 25585;
        if (this.getId() == 25586 || this.getId() == 25585) {
            walkTiles = new WorldTile[2];
            boolean reverse = Utils.random(2) == 0;
            walkTiles[reverse ? 1 : 0] = tile.transform(-15, 0, 0);
            walkTiles[reverse ? 0 : 1] = tile.transform(+15, 0, 0);
        }
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            setCantFollowUnderCombat(true);
            List<EliteDungeonNPC> pylon = new ArrayList<EliteDungeonNPC>();
            pylon.add(this);
            room.addBlock(new WorldObject(-1, 10, 0, new WorldTile(this)), getId() == 25596 ? 111686 : 111687, pylon);
        }
    }

    @Override
    public void spawn() {
        if (spawned)
            return;
        setLocation(getRespawnTile());
        setNextWorldTile(getRespawnTile());
        spawned = true;
        bar = new AdrenalineHitBar(this);
        cooldowns = new HashMap<Integer, Long>();
        setForceMultiArea(true);
        if (this.getId() == 25586 || this.getId() == 25585) {
            walkTiles = new WorldTile[2];
            boolean reverse = Utils.random(2) == 0;
            walkTiles[reverse ? 1 : 0] = getRespawnTile().transform(-15, 0, 0);
            walkTiles[reverse ? 0 : 1] = getRespawnTile().transform(+15, 0, 0);
        }
        initEntity();
        reset();
        ctarget = null;
        getCombat().removeTarget();
        dieing = false;
        super.spawn();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);
        if (getName().equalsIgnoreCase("Defence Pylon") && getCombat().getTarget() == null && hit.getSource() instanceof Player) {
            getCombat().setTarget(hit.getSource());
        }
    }

    @Override
    public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
        boolean canWalkEverywhere = this.getId() == 25586 || this.getId() == 25585;
        return super.addWalkStep(nextX, nextY, lastX, lastY, !canWalkEverywhere && check);
    }

    @Override
    public boolean addWalkSteps(int destX, int destY, int maxStepsCount, boolean check) {
        boolean canWalkEverywhere = this.getId() == 25586 || this.getId() == 25585;
        return super.addWalkSteps(destX, destY, maxStepsCount, !canWalkEverywhere && check);
    }

    @Override
    public void processNPC() {
        if (isDead() || isLocked())
            return;
        processRemoveAgro();
        if (changeTargetCooldown == 0 || Utils.currentTimeMillis() >= changeTargetCooldown) {
            trySwitchTarget();
            changeTargetCooldown = Utils.currentTimeMillis() + 5000;// 5secs
        }
        if (usesAdrenaline)
            addAdrenalineBar();
        boolean canWalkEverywhere = this.getId() == 25586 || this.getId() == 25585;
        if (!getCombat().process()) {
            if (!checkAgressivity()) {
                if (isForceWalking()) {
                    if (getFreezeDelay() < Utils.currentTimeMillis()) {
                        if (getX() != getForceWalk().getX() || getY() != getForceWalk().getY()) {
                            if (!hasWalkSteps()) {
                                int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(forceWalk.getX(), forceWalk.getY()), true);
                                int[] bufferX = RouteFinder.getLastPathBufferX();
                                int[] bufferY = RouteFinder.getLastPathBufferY();
                                if (canWalkEverywhere) {
                                    steps = 1;
                                    bufferX[0] = forceWalk.getX();
                                    bufferY[0] = forceWalk.getY();
                                }
                                for (int i = steps - 1; i >= 0; i--) {
                                    if (!addWalkSteps(bufferX[i], bufferY[i], 25, true)) {
                                        break;
                                    }
                                }
                            }
                            if (!hasWalkSteps()) {
                                setNextWorldTile(new WorldTile(getForceWalk()));
                                forceWalk = null;
                            }
                        } else
                            forceWalk = null;
                    }
                    return;
                }
                if (waiting == 0 || Utils.currentTimeMillis() >= waiting) {
                    if (forceWalk == null && walkTiles != null) {
                        if (currentIndex == walkTiles.length - 1)
                            revert = true;
                        if (currentIndex == 0)
                            revert = false;
                        WorldTile toTile = walkTiles[currentIndex];
                        if (!matches(toTile)) {
                            setForceWalk(walkTiles[currentIndex]);
                        } else {
                            setForceWalk(null);
                            currentIndex = currentIndex + (exactWalkTiles ? 1 : (revert ? -1 : 1));
                            waiting = canWalkEverywhere && Utils.random(5) != 0 || exactWalkTiles ? 0 : Utils.currentTimeMillis() + 2400;
                        }
                        if (currentIndex == walkTiles.length && exactWalkTiles)
                            currentIndex = 0;
                    }
                }
            }
        }
        if (getId() == 25606 || getId() == 25603 || getId() == 25625) {
            if (!isForceWalking()) {
                if (!isCantInteract()) {
                    if (!checkAgressivity()) {
                        if (getFreezeDelay() < Utils.currentTimeMillis()) {
                            if (!hasWalkSteps() && (getWalkType() & NORMAL_WALK) != 0) {
                                boolean can = false;
                                for (int i = 0; i < 2; i++) {
                                    if (Math.random() * 1000.0 < 100.0) {
                                        can = true;
                                        break;
                                    }
                                }
                                if (can) {
                                    final int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
                                    final int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
                                    resetWalkSteps();
                                    if (getMapAreaNameHash() != -1) {
                                        if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
                                            forceWalkRespawnTile();
                                            return;
                                        }
                                        addWalkSteps(getX() + moveX, getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
                                    } else {
                                        addWalkSteps(getRespawnTile().getX() + moveX, getRespawnTile().getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void processRemoveAgro() {
        if (getCombat().getTarget() == null)
            return;
        if (getRoom().isInBossFightArea(getCombat().getTarget())) {
            getCombat().removeTarget();
            ctarget = null;
        }
    }

    @Override
    public void finish() {
//        removeMinions();
        super.finish();
        spawned = false;
    }

    @Override
    public void reset() {
        ctarget = null;
//        removeMinions();
        getCombat().removeTarget();
        super.reset();
        dieing = false;
        spawned = false;
    }

    @Override
    public void sendDeath(final Entity source) {
        if (dieing)
            return;
        dieing = true;
        if (marked) {
            if (getRoom() != null && getRoom().getRoom() != null && getRoom().getRoom().getDungeon() != null)
                getRoom().getRoom().getDungeon().removeMark();
            marked = false;
        }
        if (source instanceof Player && getReceivedDamage() != null)
            addReceivedDamage(source, 1);
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(null);
        if (getDeathGraphicsId() != -1)
            setNextGraphics(null);
//        removeMinions();
        room.handleDeath(this);
        final EliteDungeonNPC thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                    if (getDeathGraphicsId() != -1)
                        setNextGraphics(new Graphics(getDeathGraphicsId()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        plr.getControlerManager().processNPCDeath(thisNPC);
                        ContractHandler.updateContract(plr, thisNPC);
                    }
                    drop(source instanceof Player ? (Player) source : null);
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public static final int[] CS_DATA_ID = { 16000, 16001, 16002, 16003, 16004, 16005, 16006 };

    public void addAdrenalineBar() {
        if (getCombat().getTarget() == null)
            return;
        if (!getNextHitBars().contains(bar))
            getNextHitBars().add(bar);
        addHitBars();
    }

    public void addAdrenaline(int amount) {
        getCombat().increaseSpecialAttack(amount);
    }

    public void removeAdrenaline(int amount) {
        getCombat().decreaseSpecialAttack(amount);
    }

    public void setCooldown(int abilityMap, int cooldown) {
        long cd = getBarCycle() + cooldown * 3 + 1;// currentCycle + cooldown;
        cooldowns.put(abilityMap, cd);
    }

    public boolean hasCooldown(int abilityMap, int combatBook) {
        // long currentTime = Utils.currentWorldCycle();
        int mapId = RS3ClientScriptMap.getMap(CS_DATA_ID[combatBook - 1]).getIntValue(abilityMap);
        Long cooldown = cooldowns.get(mapId);
        return cooldown != null && cooldown > /* = */getBarCycle();
    }

    public long getBarCycle() {
        if (room == null)
            return 0;
        return room.getRoom().getDungeon().getNPCS_BAR_CYCLE();
    }

    @Override
    public boolean checkAgressivity() {
        if (getName().equalsIgnoreCase("Defence Pylon"))
            return false;
        final ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (ctarget != null && !ctarget.isDead() && !ctarget.hasFinished()) {
            if (getCombat().getTarget() == null) {
                forceWalk = null;
                getCombat().setTarget(ctarget);
                setLastAttackedByTarget(Utils.currentTimeMillis());
                ctarget.setAttackedBy(ctarget);
                ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
                return true;
            }
            forceWalk = null;
            return true;
        } else {
            ctarget = null;
        }
        for (Entity target : possibleTarget) {
            if (!(target instanceof Player))
                continue;
            if (Utils.isOnRange(this, target, aggro_dis)) {
                forceWalk = null;
                this.ctarget = target;
                getCombat().setTarget(target);
                setLastAttackedByTarget(Utils.currentTimeMillis());
                target.setAttackedBy(target);
                target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
                aggroNearBy((Player) target);
                return true;
            }
        }
        return false;
    }

    public void trySwitchTarget() {
//        final ArrayList<Entity> possibleTarget = getPossibleTargets();
//        if (possibleTarget.isEmpty() || getCombat().getTarget() == null)
//            return;
//        if (possibleTarget.size() == 1 && ctarget != possibleTarget.get(0)) {
//            setForceWalk(null);
//            ctarget = possibleTarget.get(0);
//            getCombat().setTarget(ctarget);
//            setLastAttackedByTarget(Utils.currentTimeMillis());
//            ctarget.setAttackedBy(ctarget);
//            ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
//            return;
//        }
//        if (possibleTarget.size() <= 1 || Utils.random(10) != 0)
//            return;
//        Entity randomTarget = null;
//        int tryCount = 0;
//        while (randomTarget == null || ctarget == randomTarget) {
//            randomTarget = possibleTarget.get(Utils.random(possibleTarget.size()));
//            if (tryCount++ >= 20)
//                break;
//        }
//        if (tryCount++ >= 20)
//            return;
//        setForceWalk(null);
//        ctarget = randomTarget;
//        getCombat().setTarget(ctarget);
//        setLastAttackedByTarget(Utils.currentTimeMillis());
//        ctarget.setAttackedBy(ctarget);
//        ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
    }

    public void aggroNearBy(Player target) {
        if (room == null)
            return;
        for (EliteDungeonNPC npc : room.getNpcs()) {
            if (npc == this || npc.ctarget != null || npc == null || npc.hasFinished() || npc.isDead() || npc.dieing || room.getKilledNPCs().contains(npc) || !Utils.isOnRange(npc, target, aggro_dis) || npc.getCombat().getTarget() != null)
                continue;
            npc.setForceWalk(null);
            npc.ctarget = target;
            npc.getCombat().setTarget(target);
            npc.setLastAttackedByTarget(Utils.currentTimeMillis());
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
            npc.aggroNearBy(target);
        }
    }

    public int getWeaponId() {
        switch (getId()) {
        case 25573:
        case 25578:
        case 25610:
        case 25611:
        case 25623:
        case 25624:
        case 25626:
        case 25628:
            return 1237;
        case 25574:
        case 25579:
        case 25621:
        case 25622:
            return 33879;
        case 25577:
        case 25575:
        case 25580:
        case 25582:
        case 25619:
        case 25620:
            return 6914;
        case 25583:
        case 25584:
        case 25631:
            return 25654;
        case 25576:
        case 25581:
        case 25614:
        case 25615:
            return 30574;
        case 25607:
            return 4718;
        case 25629:
            return 43054;
        }
        return -1;
    }

    public boolean isExactWalkTiles() {
        return exactWalkTiles;
    }

    public void setExactWalkTiles(boolean exactWalkTiles) {
        this.exactWalkTiles = exactWalkTiles;
    }

    public int getAttackEmote() {
        int itemId = getWeaponId();
        if (itemId != -1) {
            if (itemId == 33879)
                return 18245;
            if (itemId == 6914)
                return 18249;
            if (itemId == 1237)
                return 18223;
            if (getDefinitions().getName().equalsIgnoreCase("Elite Sakadagami"))
                return 18279;
            int emote = ItemDefinitions.getItemDefinitions(itemId).getCombatOpcode(2914);
            return emote == 0 ? 18224 : emote;
        }
        return getCombatDefinitions().getAttackAnim();
    }

    public int getDeathGraphicsId() {
        if (getCombatDefinitions().getDeathAnim() == 20164)
            return 3956;
        return -1;
    }

    public int getDefenceEmote() {
        int itemId = getWeaponId();
        if (itemId != -1) {
            if (itemId == 33879)
                return 18293;
            if (itemId == 1237)
                return 18298;
            int emote = ItemDefinitions.getItemDefinitions(itemId).getCombatOpcode(2917);
            return emote == 0 ? 18346 : emote;
        }
        return getCombatDefinitions().getDefenceEmote();
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (room == null)
            return possibleTarget;
        if (getCombat().getTarget() != null)
            possibleTarget.add(getCombat().getTarget());
        for (Player player : room.getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || !player.withinDistance(new WorldTile(this), 14) || player.getEliteDungeonsManager().isHidden() || possibleTarget.contains(player) || room.isInBossFightArea(player))
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;

    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public boolean switchTarget() {
        return false;
    }

    @Override
    public boolean switchTarget(boolean force) {
        return false;
    }

    @Override
    public void setTarget(Entity entity) {

    }

    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);
        if (hit.getSource() != null && this.getCombat().getTarget() == null && hit.getSource() instanceof Player && ctarget == null) {
            forceWalk = null;
            ctarget = hit.getSource();
            getCombat().setTarget(ctarget);
            setLastAttackedByTarget(Utils.currentTimeMillis());
            ctarget.setAttackedBy(ctarget);
            ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
        }
    }

    @Override
    public void setNextAnimationForce(Animation nextAnimation) {
        if (getId() == 25591) {
            super.setNextAnimationForce(new Animation(-1));
            return;
        }
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            super.setNextAnimationForce(new Animation(-1));
            return;
        }
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextAnimationForce(nextAnimation);
    }

    @Override
    public void setNextAnimation(Animation animation) {
        if (getId() == 25591) {
            super.setNextAnimation(new Animation(-1));
            return;
        }
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            super.setNextAnimationForce(new Animation(-1));
            return;
        }
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextAnimation(animation);
    }

    @Override
    public void setNextAnimationNoPriority(Animation nextAnimation) {
        if (getId() == 25591) {
            super.setNextAnimationNoPriority(new Animation(-1));
            return;
        }
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            super.setNextAnimationForce(new Animation(-1));
            return;
        }
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextAnimationNoPriority(nextAnimation);
    }

    @Override
    public void setNextGraphics(Graphics nextGraphics) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextGraphics(nextGraphics);
    }

    @Override
    public void setNextFaceEntity(Entity entity) {
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            super.setNextFaceEntity(null);
            return;
        }
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextFaceEntity(entity);
    }

    @Override
    public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        if (getName().equalsIgnoreCase("Defence Pylon")) {
            super.setNextFaceWorldTile(getRespawnTile().transform(0, -1, 0));
            return;
        }
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
            return;
        super.setNextFaceWorldTile(nextFaceWorldTile);
    }

    @SuppressWarnings("unchecked")
    public void removeMinions() {
        if (getTemporaryAttributtes() == null)
            return;
        List<EliteDungeonNPC> spawns = (ArrayList<EliteDungeonNPC>) getTemporaryAttributtes().remove("minions");
        if (spawns == null)
            return;
        for (EliteDungeonNPC m : spawns) {
            if (m == null || m.hasFinished() || m.isDead())
                continue;
            m.finish();
        }
        spawns.clear();
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.25;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.25;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.25;
    }

    @Override
    public void drop() {
        drop(null);
    }

    public void drop(Player player) {
        String name = getName().toLowerCase();
        if (name == null)
            return;
        if (!name.equalsIgnoreCase("Elite Sotapanna") && !name.equalsIgnoreCase("Elite Sakadagami") && !name.equalsIgnoreCase("Anagami") && !name.equalsIgnoreCase("Arhat") && !name.equalsIgnoreCase("Waterfiend") && !name.equalsIgnoreCase("Sea crocodile") && !name.equalsIgnoreCase("Renegade Menaphite soldier") && !name.equalsIgnoreCase("Hanto sellsword") && !name.equalsIgnoreCase("Eastern mercenary") && !name.equalsIgnoreCase("Cloaked zealot") && !name.equalsIgnoreCase("Death Lotus rogue"))
            return;
        final Player killer = getMostDamageReceivedSourcePlayer();
        if (killer == null) {
            return;
        }
        killer.getInventionManager().processScavengingPerk();
        increaseKillStatistics(killer, getName());
        handleRingOfDeath(killer);
        final NPCDrop[] possibleDrops = EliteDungeonsConstants.mobDrops;
        for (final NPCDrop drop : possibleDrops) {
            if (drop == null || drop.getRate() != 100)
                continue;
            if (!Ectoplasmator.scatterAshes(killer, this, drop.getItemId()))
                sendDrop(killer, drop, false);
        }
        int amountCharms = killer.getInventory().getAmountOf(43066);
        NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.02) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
        NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(possibleDrops);
        if (luckyCharm != null && amountCharms > 0)
            killer.getInventory().deleteItem(43066, 1);
        sendDrop(killer, mobDrop, false);
        if (Math.random() <= 0.005)
            sendDrop(killer, new NPCDrop(18778, 5, 1, 1), false);
    }

    @Override
    protected void sendDrop(Player player, NPCDrop drop, boolean lootbeam) {
        WorldTile tile = new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());
        final Item item = new Item(drop.getItemId());
        if (!World.canMoveNPC(tile, 1)) {
            tile = new WorldTile(player);
        }
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
            if (!player.getEliteDungeonsManager().isAutoLoot() && Math.random() <= 0.2) {
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

    public EliteDungeonNPC getNearByNeedsHealing() {
        if (getRoom() == null)
            return null;
        List<EliteDungeonNPC> possibleNPCs = new ArrayList<EliteDungeonNPC>();
        for (NPC n : getRoom().getNpcs()) {
            if (n == null || n == this || n.hasFinished() || n.isDead() || !(n instanceof EliteDungeonNPC) || getRoom().getKilledNPCs().contains(n) || !n.withinDistance(this, aggro_dis))
                continue;
            EliteDungeonNPC npc = (EliteDungeonNPC) n;
            if (npc.dieing || npc.getHitpoints() == npc.getMaxHitpoints())
                continue;
            possibleNPCs.add(npc);
        }
        if (possibleNPCs.isEmpty() || Utils.random(2) == 0)
            return null;
        return possibleNPCs.get(Utils.random(possibleNPCs.size()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (spawned ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(walkTiles);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this instanceof ShadowEnigma && obj instanceof ShadowEnigma) {
            if (!super.equals(obj))
                return false;
        }
        if (getClass() != obj.getClass())
            return false;
        EliteDungeonNPC other = (EliteDungeonNPC) obj;
        if (originalId != other.originalId)
            return false;
        if (absoluteTile == null && other.absoluteTile != null)
            return false;
        if (absoluteTile != null && other.absoluteTile == null)
            return false;
        return absoluteTile == null || other.absoluteTile == null || absoluteTile.matches(other.absoluteTile);
    }

    public EliteDungeonNPC setAbsoluteTile(WorldTile tile) {
        this.absoluteTile = tile;
        return this;
    }

}
