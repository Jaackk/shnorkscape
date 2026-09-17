package com.rs.game.activites.dnd.eviltree;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.entity.EvilRootObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilRootObject.RootSpot;
import com.rs.game.activites.dnd.eviltree.entity.EvilSaplingObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeHunterNPC;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilWeedsObject;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles a single instance of an Evil Tree and it's accessories.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTree {
    public static final int GROW_RATE = Settings.TEST_SERVER_MODE ? 150 : 1000; // 1.5 minutes/10 minutes.

    public static boolean canNurtureEntling(Player player) {
        return player.petNurture == null || LocalDateTime.now().isAfter(player.petNurture);
    }

    public static void sendEntlingMessage(Player player) {
        if (canNurtureEntling(player)) {
            int chance = ThreadLocalRandom.current().nextInt(4);
            switch (chance) {
                case 0:
                    player.sendMessage("Entling seems distant towards you.");
                    break;
                case 1:
                    player.sendMessage("Entling seems to ignore your presence.");
                    break;
                case 2:
                    player.sendMessage("Entling looks a little sad.");
                    break;
                case 3:
                    player.sendMessage("Entling glares at you with brooding eyes.");
                    break;
            }
        } else if (ThreadLocalRandom.current().nextBoolean()) {
            player.sendMessage("Entling looks happy to be around you.");
        } else if (ThreadLocalRandom.current().nextBoolean()) {
            player.sendMessage("Entling glares at you and smiles.");
        } else {
            player.sendMessage("Entling looks energized and ready to explore Ataraxia with you.");
        }
    }

    public static void transformEntling(Player player) {
        if (canNurtureEntling(player)) {
            player.sendMessage("Entling isn't in the mood for that.");
            return;
        }
        switch (player.entlingTransformId) {
            case 9591:
                player.entlingTransformId = 9592;
                break;
            case 9592:
                player.entlingTransformId = 9594;
                break;
            case 9594:
                player.entlingTransformId = 7709;
                break;
            default:
                player.entlingTransformId = 9591;
                break;
        }
        player.lock();
        player.getPet().setNextGraphics(new Graphics(1638));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.unlock();
                player.getPet().setNextNPCTransformation(player.entlingTransformId);
            }
        }, 2);
    }

    public static void nurtureEntling(Player player) {
        if (canNurtureEntling(player)) {
            player.lock();
            player.sendMessage("You nurture Entling...");
            player.setNextAnimation(new Animation(2295));
            if (player.getPet() != null && player.getPet().getType() == Pets.ENTLING) {
                player.faceEntity(player.getPet());
            }
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    player.petNurture = LocalDateTime.now().plusDays(1);
                    if (ThreadLocalRandom.current().nextInt(32) == 0) {
                        player.sendMessage(Colors.RED + "It yields some evil seeds!");
                        player.addItem(new Item(24778, ThreadLocalRandom.current().nextInt(1, 4)));
                    } else if (ThreadLocalRandom.current().nextInt(24) == 0) {
                        player.sendMessage(Colors.RED + "It yields some evil dust!");
                        player.addItem(new Item(3326, ThreadLocalRandom.current().nextInt(20, 40)));
                    } else if (ThreadLocalRandom.current().nextInt(16) == 0) {
                        player.sendMessage(Colors.RED + "It yields some evil herbs!");
                        player.addItem(new Item(24783, ThreadLocalRandom.current().nextInt(30, 60)));
                    } else if (ThreadLocalRandom.current().nextInt(8) == 0) {
                        player.sendMessage(Colors.RED + "It yields some evil bark!");
                        player.addItem(new Item(3240, ThreadLocalRandom.current().nextInt(40, 80)));
                    } else if (ThreadLocalRandom.current().nextBoolean()) {
                        player.sendMessage("It seems happy.");
                    } else if (ThreadLocalRandom.current().nextBoolean()) {
                        player.sendMessage("It seems delighted.");
                    } else if (ThreadLocalRandom.current().nextBoolean()) {
                        player.sendMessage("It seems to like you.");
                    }
                }
            }, 2);
        } else {
            player.sendMessage("Entling won't need to be nurtured again for awhile.");
        }
    }

    public static boolean isNearInstanceOrGlobal(Player player) {
        return isNearGlobal(player) || isNearInstance(player);
    }

    public static boolean isNearGlobal(Player player) {
        EvilTree tree = EvilTreeHandler.isAlive() ? EvilTreeHandler.current() : null;
        return tree != null && player.withinDistance(tree.getTreeTile(), 10);
    }

    public static boolean isNearInstance(Player player) {
        return BossInstanceHandler.inInstance(Boss.Evil_Tree, player);
    }

    public static void sendAllMessage(Object str) {
        World.sendWorldMessage("<img=5>" + Colors.RED + str, false);
    }

    public static void curseTeleport(Player player, WorldTile tile) {
        player.lock();
        int x = player.getX();
        int y = player.getY();
        int directionX;
        int directionY;
        WorldTile originalTile = new WorldTile(player.getX(), player.getY(), player.getPlane());
        if (player.addWalkSteps(x - 1, y)) {
            directionX = -1;
            directionY = 0;
        } else if (player.addWalkSteps(x + 1, y)) {
            directionX = 1;
            directionY = 0;
        } else if (player.addWalkSteps(x, y + 1)) {
            directionX = 0;
            directionY = 1;
        } else if (player.addWalkSteps(x, y - 1)) {
            directionX = 0;
            directionY = -1;
        } else {
            directionX = 0;
            directionY = 0;
        }

        WorldTasksManager.schedule(new WorldTask() {
            int loop = 0;

            @Override
            public void run() {
                if (loop == 0) {
                    player.resetWalkSteps();
                    loop = 1;
                } else if (loop == 1) {
                    if (directionX != 0 || directionY != 0) {
                        player.setNextFaceWorldTile(player.transform(-directionX, -directionY, player.getPlane()));
                    }
                    loop = 2;
                } else if (loop == 2) {
                    if (directionX != 0 || directionY != 0) {
                        World.sendGraphics(player, new Graphics(1638), originalTile);
                    } else {
                        player.setNextGraphics(new Graphics(1638));
                    }
                    loop = 3;
                } else if (loop == 3) {
                    player.setNextAnimation(new Animation(27450));
                    loop = 4;
                } else if (loop == 4) {
                    player.setNextWorldTile(tile);
                    player.setNextAnimation(new Animation(-1));
                    player.unlock();
                    stop();
                }
            }
        }, 1, 1);
    }

    public static void treeTeleport(Player player, Runnable onTele) {
        player.lock();
        player.setNextAnimation(new Animation(2291));
        WorldTasksManager.schedule(new WorldTask() {
            boolean looped = false;

            @Override
            public void run() {
                if (!looped) {
                    player.setNextGraphics(new Graphics(3895));
                    looped = true;
                } else {
                    player.unlock();
                    player.setNextGraphics(new Graphics(-1));
                    player.setNextAnimation(new Animation(-1));
                    onTele.run();
                    stop();
                }
            }
        }, 2, 14);
    }

    public static void treeTeleport(Player player, WorldTile tile) {
        treeTeleport(player, () -> player.setNextWorldTile(tile));
    }

    public static void openInter(Player player, EvilTree tree) {
        if (tree.isAlive()) {
            player.getInterfaceManager().sendMinigameHudInterface(46);
            player.evilTreeInter = true;
            updateInter(player, tree);
        }
    }

    public static void updateInter(Player player, EvilTreeInstanceController etController, EvilTree tree) {
        if (tree.isAlive()) {
            player.getPackets().sendIComponentText(46, 15, "Status:");
            if (tree.isSapling()) {
                int phase = tree.getSaplingObject().getNextPhase();
                player.getPackets().sendIComponentText(46, 12, Colors.GREEN + "Sapling (" + phase + "/4)");

                player.getPackets().sendIComponentText(46, 14, "Damage:");
                player.getPackets().sendIComponentText(46, 11, Colors.RED + "0");

                player.getPackets().sendIComponentText(46, 13, "Next phase in:");
                player.getPackets().sendIComponentText(46, 10, EvilTreeHandler.getNextPhaseIn(tree));
            } else if (tree.isTree()) {
                EvilTreeObject treeObject = tree.getTreeObject();
                int hp = treeObject.getHitpoints();
                if (hp > 0) {
                    player.getPackets().sendIComponentText(46, 12, Colors.GREEN + Utils.formatNumber(hp) + " HP");
                } else {
                    player.getPackets().sendIComponentText(46, 12, Colors.RED + "Dead");
                }
                int damage = treeObject.getDamageSet().count(player.getUsername());
                player.getPackets().sendIComponentText(46, 14, "Damage:");
                player.getPackets().sendIComponentText(46, 11, Colors.GREEN + Utils.formatNumber(damage));
                player.getPackets().sendIComponentText(46, 13, "Expires in:");
                if (tree.isInstanced()) {
                    if(etController != null) {
                        player.getPackets().sendIComponentText(46, 10, Colors.GREEN + (etController.getInstance().getSettings().getTimeRemaining() / 60000) + "m");
                    } else {
                        player.getPackets().sendIComponentText(46, 10, Colors.GREEN + "Unknown");
                    }
                } else {
                    player.getPackets().sendIComponentText(46, 10, Colors.GREEN + EvilTreeHandler.getTimeRemaining());
                }
            }
        }
    }

    public static void updateInter(Player player, EvilTree tree) {
        updateInter(player, null, tree);
    }

    private final EvilTreeLocation location;
    private final EvilTreeType type;
    private final WorldTile treeTile;
    private EvilTreeGrowthTask growthTask;
    private EvilSaplingObject saplingObject;
    private EvilTreeObject treeObject;
    private EvilWeedsObject weedsObject;
    private EvilRootObject northRoot;
    private EvilRootObject southRoot;
    private EvilRootObject westRoot;
    private EvilRootObject eastRoot;
    private EvilTreeHunterNPC treeHunterNpc;
    private WorldObject[] fires;
    private WorldTask restoreTreeTask;

    EvilTree(EvilTreeLocation location, EvilTreeType type) {
        this.type = type;
        this.location = location;
        treeTile = location.treeTile;
    }

    EvilTree(WorldTile treeTile, EvilTreeType type) {
        this.type = type;
        this.treeTile = treeTile;
        location = null;
    }

    public boolean allRewardsClaimed() {
        return isTree() &&
                treeObject.isDead() &&
                !treeObject.getRewards().getClaimedRewards().isEmpty() &&
                treeObject.getDamageSet().isEmpty();

    }

    public boolean isInstanced() {
        return location == null;
    }

    public void stunAndRepel(Player player, int spaces) {
        WorldTile movePos = player;
        int xDiff = player.getX() - treeTile.getX();
        int yDiff = player.getY() - treeTile.getY();
        if (yDiff > 0) {
            // north
            movePos = player.transform(0, spaces, 0);
        } else if (yDiff < 0) {
            //south
            movePos = player.transform(0, -spaces, 0);
        } else if (xDiff > 0) {
            // east
            movePos = player.transform(spaces, 0, 0);
        } else if (xDiff < 0) {
            // west
            movePos = player.transform(-spaces, 0, 0);
        }
        int maxHp = player.getSkills().getLevelForXp(Skills.HITPOINTS);
        int damage = (int) Utils.random(maxHp * 0.05, maxHp * 0.15);
        player.applyHit(new Hit(damage, Hit.HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
        player.lock();
        player.getActionManager().forceStop();
        player.resetWalkSteps();
        player.setNextForceMovement(new ForceMovement(movePos, spaces, player.getDirection()));
        player.getAppearence().setRenderEmote(2671);
        WorldTile finalMovePos = movePos;
        WorldTasksManager.schedule(new WorldTask() {
            boolean first = true;

            @Override
            public void run() {
                if (first) {
                    player.getAppearence().setRenderEmote(-1);
                    player.setNextWorldTile(finalMovePos);
                    player.setNextGraphics(new Graphics(80, 5, 60));
                    first = false;
                } else {
                    player.unlock();
                    stop();
                }
            }
        }, spaces, spaces + 2);
    }

    public void clearRoots() {
        if (northRoot != null) {
            World.removeObject(northRoot);
            northRoot = null;
        }
        if (southRoot != null) {
            World.removeObject(southRoot);
            southRoot = null;
        }
        if (eastRoot != null) {
            World.removeObject(eastRoot);
            eastRoot = null;
        }
        if (westRoot != null) {
            World.removeObject(westRoot);
            westRoot = null;
        }
    }

    public void clear() {
        if (growthTask != null) {
            growthTask.stop();
            growthTask = null;
        }
        if (saplingObject != null) {
            World.removeObject(saplingObject);
            saplingObject = null;
        }
        if (treeObject != null) {
            World.removeObject(treeObject);
            treeObject = null;
        }
        if (weedsObject != null) {
            World.removeObject(weedsObject);
            weedsObject = null;
        }
        if (treeHunterNpc != null) {
            treeHunterNpc.setNextWorldTile(new WorldTile(1, 1, 0));
            World.removeNPC(treeHunterNpc);
            treeHunterNpc = null;
        }
        clearFire();
        clearRoots();
    }

    public void clearFire() {
        if (fires != null) {
            for (WorldObject object : fires) {
                World.removeObject(object);
            }
            fires = null;
        }
    }

    void spawnSapling() {
        if (saplingObject == null && treeObject == null && location != null) {
            saplingObject = new EvilSaplingObject(this);
            weedsObject = new EvilWeedsObject(this, false);
            treeHunterNpc = new EvilTreeHunterNPC(this, location.hunterTile);
            growthTask = new EvilTreeGrowthTask(this);
            World.spawnObject(saplingObject);
            World.spawnObject(weedsObject);
            WorldTasksManager.schedule(growthTask, GROW_RATE, GROW_RATE);
        }
    }

    public void setOnFire() {
        if (isTree() && isAlive() && !isOnFire()) {
            fires = new WorldObject[6];
            fires[0] = new WorldObject(11425, 0, 1, treeObject.transform(1, 0, 0));
            fires[1] = new WorldObject(11425, 0, 1, treeObject.transform(1, 1, 0));
            fires[2] = new WorldObject(14169, 10, 1, treeObject.transform(1, 0, 0));
            fires[3] = new WorldObject(14169, 10, 1, treeObject.transform(0, 1, 0));
            fires[4] = new WorldObject(14169, 10, 1, treeObject.transform(2, 1, 0));
            fires[5] = new WorldObject(14169, 10, 1, treeObject.transform(1, 2, 0));
            for (WorldObject object : fires) {
                World.spawnObject(object);
            }
        }
    }

    public boolean isOnFire() {
        return fires != null;
    }

    public void spawnTree() {
        if (saplingObject != null) {
            treeObject = new EvilTreeObject(this);
            northRoot = new EvilRootObject(this, treeTile.transform(1, 3, 0), RootSpot.NORTH);
            southRoot = new EvilRootObject(this, treeTile.transform(1, -1, 0), RootSpot.SOUTH);
            westRoot = new EvilRootObject(this, treeTile.transform(-1, 1, 0), RootSpot.WEST);
            eastRoot = new EvilRootObject(this, treeTile.transform(3, 1, 0), RootSpot.EAST);

            World.removeObject(saplingObject);
            World.spawnObject(treeObject);
            World.spawnObject(northRoot);
            World.spawnObject(westRoot);
            World.spawnObject(eastRoot);
            World.spawnObject(southRoot);
            saplingObject = null;

            startTreeRestore();
        }
    }

    public void spawnInstancedTree(WorldTile hunterTile) {
        if (isInstanced()) {
            treeObject = new EvilTreeObject(this);
            northRoot = new EvilRootObject(this, treeTile.transform(1, 3, 0), RootSpot.NORTH);
            southRoot = new EvilRootObject(this, treeTile.transform(1, -1, 0), RootSpot.SOUTH);
            westRoot = new EvilRootObject(this, treeTile.transform(-1, 1, 0), RootSpot.WEST);
            eastRoot = new EvilRootObject(this, treeTile.transform(3, 1, 0), RootSpot.EAST);
            treeHunterNpc = new EvilTreeHunterNPC(this, hunterTile);

            World.spawnObject(treeObject);
            World.spawnObject(northRoot);
            World.spawnObject(westRoot);
            World.spawnObject(eastRoot);
            World.spawnObject(southRoot);

            startTreeRestore();
        }
    }

    public boolean isTree() {
        return treeObject != null;
    }

    public boolean isSapling() {
        return saplingObject != null;
    }

    public EvilRootObject getNorthRoot() {
        return northRoot;
    }

    public void setNorthRoot(EvilRootObject northRoot) {
        this.northRoot = northRoot;
    }

    public EvilRootObject getSouthRoot() {
        return southRoot;
    }

    public void setSouthRoot(EvilRootObject southRoot) {
        this.southRoot = southRoot;
    }

    public EvilRootObject getEastRoot() {
        return eastRoot;
    }

    public void setEastRoot(EvilRootObject eastRoot) {
        this.eastRoot = eastRoot;
    }

    public EvilRootObject getWestRoot() {
        return westRoot;
    }

    public void setWestRoot(EvilRootObject westRoot) {
        this.westRoot = westRoot;
    }

    public EvilTreeHunterNPC getTreeHunterNpc() {
        return treeHunterNpc;
    }

    public EvilWeedsObject getWeedsObject() {
        return weedsObject;
    }

    public void setWeedsObject(EvilWeedsObject weedsObject) {
        this.weedsObject = weedsObject;
    }

    public EvilTreeObject getTreeObject() {
        return treeObject;
    }

    public void setTreeObject(EvilTreeObject treeObject) {
        this.treeObject = treeObject;
    }

    public EvilTreeGrowthTask getGrowthTask() {
        return growthTask;
    }

    public EvilSaplingObject getSaplingObject() {
        return saplingObject;
    }

    public void setSaplingObject(EvilSaplingObject saplingObject) {
        this.saplingObject = saplingObject;
    }

    public void setGrowthTask(EvilTreeGrowthTask growthTask) {
        this.growthTask = growthTask;
    }

    public EvilTreeLocation getLocation() {
        return location;
    }

    public WorldTile getTreeTile() {
        return treeTile;
    }

    public EvilTreeType getType() {
        return type;
    }

    public EvilRootObject getRoot(RootSpot direction) {
        switch (direction) {
            case NORTH:
                return northRoot;
            case SOUTH:
                return southRoot;
            case EAST:
                return eastRoot;
            case WEST:
                return westRoot;
            default:
                throw new IllegalStateException("Invalid root direction.");
        }
    }

    public void setRoot(RootSpot direction, EvilRootObject value) {
        switch (direction) {
            case NORTH:
                northRoot = value;
                break;
            case SOUTH:
                southRoot = value;
                break;
            case EAST:
                eastRoot = value;
                break;
            case WEST:
                westRoot = value;
                break;
            default:
                throw new IllegalStateException("Invalid root direction.");
        }
    }

    public boolean isAlive() {
        return isSapling() || isTree() && !treeObject.isDead();
    }

    public void doRandomAttack(Player player) {
        if (player.treeAttacking)
            return;
        player.treeAttacking = true;
        boolean chance = player.bloodSerumDelay <= 0 || ThreadLocalRandom.current().nextInt(4) == 0;
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            player.setNextGraphics(new Graphics(4663));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.treeAttacking = false;
                    player.setNextGraphics(new Graphics(8));
                    player.setNextAnimation(new Animation(Combat.getDefenceEmote(player)));
                    if (!player.prayer.isMeleeProtecting() && chance) {
                        player.sendMessage("The Evil Tree attacks you with its roots!");
                        int maxHp = player.getSkills().getLevelForXp(Skills.HITPOINTS);
                        int damage = (int) Utils.random(maxHp * type.minDmg, maxHp * type.maxDmg);
                        player.applyHit(new Hit(damage, Hit.HitLook.MELEE_DAMAGE));
                    } else {
                        player.applyHit(new Hit(0, Hit.HitLook.MELEE_DAMAGE));
                    }
                }
            }, 14);
        } else {
            player.setNextGraphics(new Graphics(1847));
            WorldTasksManager.schedule(new WorldTask() {
                boolean executed;

                @Override
                public void run() {
                    if (!executed) {
                        player.setNextGraphics(new Graphics(4131));
                        executed = true;
                    } else {
                        stop();
                        player.treeAttacking = false;
                        player.setNextAnimation(new Animation(Combat.getDefenceEmote(player)));
                        if (!player.prayer.isMageProtecting() && chance) {
                            player.sendMessage("The Evil Tree attacks you with magic!");
                            int maxHp = player.getSkills().getLevelForXp(Skills.HITPOINTS);
                            int damage = (int) Utils.random(maxHp * type.minDmg, maxHp * type.maxDmg);
                            player.applyHit(new Hit(damage, Hit.HitLook.MAGIC_DAMAGE));
                        } else {
                            player.applyHit(new Hit(0, Hit.HitLook.MAGIC_DAMAGE));
                        }
                    }
                }
            }, 3, 1);
        }
    }

    private void startTreeRestore() {
        restoreTreeTask = new WorldTask() {
            @Override
            public void run() {
                if (isInstanced())
                    if (!isTree()) {
                        stop();
                        return;
                    }
                if (treeObject.isDead()) {
                    stop();
                    return;
                }
                if (World.getObjectWithId(treeTile, treeObject.getId()) == null) {
                    World.spawnObject(treeObject);
                }
                if (treeHunterNpc != null && World.getNPCs().indexOf(treeHunterNpc) == -1) {
                    treeHunterNpc = new EvilTreeHunterNPC(EvilTree.this, treeHunterNpc);
                }
            }
        };
        WorldTasksManager.schedule(restoreTreeTask, 2, 2);
    }

    public void stopTreeRestore() {
        if(restoreTreeTask != null) {
            restoreTreeTask.stop();
            restoreTreeTask = null;
        }
    }

    public void sendStatusReport(Player player) {
        player.sendMessage(Colors.PINK + "HP ~ " + (treeObject != null ? String.valueOf(treeObject.getHitpoints()) : "treeObj null"));
        player.sendMessage(Colors.PINK + "State ~ " + (treeObject != null ? String.valueOf(treeObject.getState()) : "treeObj null"));
        player.sendMessage(Colors.PINK + "Current HP ~ " + (treeObject != null ? String.valueOf(treeObject.getHitpoints()) : "treeObj null"));
        player.sendMessage(Colors.PINK + "Object exists? ~ " + (treeObject != null ? String.valueOf(World.getObjectWithId(treeObject, treeObject.getId()) != null) : "treeObj null"));
        player.sendMessage(Colors.PINK + "NPC exists? ~ " + (treeHunterNpc != null ? String.valueOf(World.getNPCs().indexOf(treeHunterNpc) != -1) : "treeHunter null"));
    }
}