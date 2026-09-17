package com.rs.game.map.bossInstance.impl;

import static com.rs.game.MapBuilder.destroyRegion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.route.Flags;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class SpiderBossInstance extends BossInstance {

    public static final int ARAXXOR_HEALTH_DATA_ID = 32142, ARAXXI_HEALTH_DATA_ID = 32143;
    public static final WorldTile[][] blockedPaths = { { new WorldTile(4507, 6273, 1), new WorldTile(4504, 6275, 1), new WorldTile(4510, 6272, 1), new WorldTile(4509, 6272, 1), new WorldTile(4508, 6273, 1), new WorldTile(4508, 6274, 1), new WorldTile(4507, 6274, 1), new WorldTile(4507, 6275, 1), new WorldTile(4506, 6274, 1), new WorldTile(4506, 6275, 1), new WorldTile(4505, 6275, 1), new WorldTile(4505, 6276, 1), new WorldTile(4509, 6272, 1), new WorldTile(4508, 6272, 1), new WorldTile(4504, 6276, 1) }// path1
            , { new WorldTile(4513, 6259, 1), new WorldTile(4513, 6260, 1), new WorldTile(4513, 6261, 1), new WorldTile(4513, 6262, 1), new WorldTile(4513, 6263, 1), new WorldTile(4513, 6264, 1), new WorldTile(4513, 6265, 1), new WorldTile(4513, 6266, 1), new WorldTile(4513, 6267, 1), new WorldTile(4513, 6268, 1), new WorldTile(4513, 6269, 1) }// path2
            , { new WorldTile(4506, 6249, 1), new WorldTile(4507, 6250, 1), new WorldTile(4508, 6251, 1), new WorldTile(4509, 6252, 1), new WorldTile(4510, 6253, 1), new WorldTile(4511, 6254, 1), new WorldTile(4512, 6255, 1), new WorldTile(4508, 6251, 1), new WorldTile(4508, 6252, 1), new WorldTile(4507, 6251, 1) }// path3
    };
    // minX,minY,maxX,maxY
    public static final int[][] phase_1_fight_area = { { 4490, 6261, 4490, 6268 }, { 4491, 6255, 4491, 6269 }, { 4492, 6254, 4492, 6270 }, { 4493, 6253, 4494, 6271 }, { 4495, 6252, 4498, 6271 }, { 4499, 6253, 4500, 6271 }, { 4501, 6253, 4501, 6272 }, { 4502, 6252, 4502, 6273 }, { 4503, 6251, 4503, 6273 }, { 4504, 6252, 4505, 6273 }, { 4506, 6253, 4506, 6272 }, { 4507, 6253, 4507, 6271 }, { 4508, 6254, 4508, 6270 }, { 4509, 6255, 4509, 6269 }, { 4510, 6257, 4510, 6268 }, { 4510, 6257, 4510, 6268 }, { 4511, 6260, 4511, 6268 } };
    // choosenPath , area(minX,minY,maxX,maxY)
    public static final int[][][] phase_2_fight_areas = { { { 4504, 6276, 4504, 6276 }, { 4505, 6276, 4505, 6277 }, { 4506, 6276, 4506, 6278 }, { 4507, 6275, 4507, 6279 }, { 4508, 6275, 4508, 6280 }, { 4509, 6274, 4509, 6279 }, { 4510, 6274, 4510, 6280 }, { 4511, 6274, 4511, 6280 }, { 4512, 6275, 4512, 6280 }, { 4513, 6276, 4551, 6280 } }, { { 4515, 6263, 4515, 6269 }, { 4516, 6264, 4518, 6269 }, { 4519, 6260, 4534, 6269 }, { 4535, 6260, 4537, 6268 }, { 4538, 6262, 4554, 6266 } }, { { 4508, 6247, 4508, 6248 }, { 4509, 6246, 4509, 6249 }, { 4510, 6245, 4510, 6251 }, { 4511, 6245, 4511, 6252 }, { 4512, 6246, 4512, 6253 }, { 4513, 6246, 4513, 6254 }, { 4514, 6246, 4514, 6253 }, { 4515, 6246, 4515, 6252 }, { 4516, 6247, 4551, 6251 } } };

    // minX,minY,maxX,maxY
    public static final int[][] phase_3_fight_area = { { 4552, 6247, 4553, 6252 }, { 4554, 6247, 4554, 6254 }, { 4555, 6247, 4555, 6253 }, { 4556, 6248, 4556, 6254 }, { 4557, 6249, 4557, 6258 }, { 4558, 6251, 4558, 6259 }, { 4552, 6275, 4552, 6279 }, { 4553, 6274, 4553, 6279 }, { 4554, 6274, 4554, 6279 }, { 4555, 6274, 4555, 6279 }, { 4556, 6272, 4556, 6279 }, { 4557, 6271, 4557, 6278 }, { 4558, 6270, 4558, 6278 }, { 4559, 6251, 4561, 6278 }, { 4562, 6252, 4563, 6278 }, { 4564, 6253, 4564, 6278 }, { 4565, 6254, 4565, 6277 }, { 4566, 6256, 4565, 6277 }, { 4567, 6257, 4567, 6275 }, { 4568, 6257, 4568, 6274 }, { 4569, 6258, 4569, 6274 }, { 4570, 6259, 4570, 6274 }, { 4571, 6259, 4571, 6273 }, { 4572, 6260, 4572, 6272 }, { 4573, 6261, 4573, 6271 } };
    // minX,minY,maxX,maxY
    public static final int[][] phase_4_fight_area = { { 4580, 6262, 4580, 6267 }, { 4581, 6260, 4581, 6271 }, { 4582, 6259, 4582, 6273 }, { 4583, 6258, 4583, 6274 }, { 4584, 6256, 4584, 6275 }, { 4585, 6255, 4586, 6275 }, { 4587, 6254, 4587, 6275 }, { 4588, 6254, 4588, 6276 }, { 4589, 6254, 4592, 6277 }, { 4593, 6254, 4593, 6276 }, { 4594, 6255, 4595, 6276 }, { 4596, 6255, 4596, 6274 }, { 4597, 6256, 4598, 6275 }, { 4599, 6259, 4599, 6274 }, { 4600, 6261, 4600, 6273 }, { 4601, 6262, 4601, 6272 } };

    public static final int[] darkness_path_sunlite_area = { 4515, 6247, 4541, 6249 };

    public static final WorldTile[][] PHASE_CHANGE_BY_WORLDTILE = { { new WorldTile(4506, 6276, 1), new WorldTile(4507, 6275, 1), new WorldTile(4507, 6274, 1), new WorldTile(4508, 6274, 1), new WorldTile(4509, 6274, 1), new WorldTile(4509, 6275, 1), new WorldTile(4508, 6276, 1), new WorldTile(4512, 6281, 1), new WorldTile(4511, 6275, 1) }, { new WorldTile(4515, 6270, 1), new WorldTile(4515, 6261, 1) }, { new WorldTile(4507, 6247, 1), new WorldTile(4508, 6248, 1), new WorldTile(4509, 6248, 1), new WorldTile(4508, 6249, 1), new WorldTile(4509, 6249, 1), new WorldTile(4509, 6250, 1), new WorldTile(4510, 6250, 1), new WorldTile(4510, 6249, 1), new WorldTile(4510, 6251, 1), new WorldTile(4511, 6251, 1), new WorldTile(4511, 6252, 1), new WorldTile(4512, 6253, 1), new WorldTile(4512, 6252, 1), new WorldTile(4513, 6252, 1), new WorldTile(4513, 6246, 1) } };
    public static WorldTile[] ACID_POOL_AREA = { new WorldTile(4525, 6259, 1), new WorldTile(4537, 6269, 1), new WorldTile(4543, 6261, 1), new WorldTile(4554, 6269, 1) };
    // web, burning web, "burn" web, "block"
    public static final int[][] PATH_SPECIFIC_OBJECT_IDS = { { 91504, 91505, 91667, 91508 }, { 91509, 91506, 91668, 91510 }, { 91511, 91507, 91669, 91512 } };
    public static final int ACID_GROUND_OBJECT_ID = 91520;
    public static final int BROKEN_RAMP_ID = 91670;
    public static final WorldTile BROKEN_RAMP_WORLDTILE = new WorldTile(4555, 6263, 1);
    public static final int DARKNESS_WALL_GROUND_OBJECT_ID = 91513;
    private String allowedFriendDisplayName;
    private Araxxor araxxor;
    private List<WorldObject> objects;
    private List<WorldTile> blockedTiles;
    private List<Player> playersInside;
    private WorldObject acidGround;
    private WorldObject darknessWall;
    private boolean resettingInstance;
    private int fightRotation;

    public SpiderBossInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
        playersInside = new ArrayList<Player>(2);
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 560, 776 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 2, 3 };
    }

    @Override
    public void loadMapInstance() {
        fightRotation = Settings.SPIDER_BOSS_ROTATION;
        List<WorldTile[]> extraBlockedTiles = new ArrayList<WorldTile[]>();
        objects = new ArrayList<WorldObject>();
        objects.add(new WorldObject(fightRotation == 0 ? PATH_SPECIFIC_OBJECT_IDS[2][3] : PATH_SPECIFIC_OBJECT_IDS[2][0], 10, 0, getTile(new WorldTile(4507, 6248, 1))));// path3
        objects.add(new WorldObject(fightRotation == 1 ? PATH_SPECIFIC_OBJECT_IDS[0][3] : PATH_SPECIFIC_OBJECT_IDS[0][0], 10, 0, getTile(new WorldTile(4504, 6272, 1))));// path1
        objects.add(new WorldObject(fightRotation == 2 ? PATH_SPECIFIC_OBJECT_IDS[1][3] : PATH_SPECIFIC_OBJECT_IDS[1][0], 10, 0, getTile(new WorldTile(4512, 6261, 1))));// path2
        if (fightRotation == 0) {
            extraBlockedTiles.add(new WorldTile[] { new WorldTile(4506, 6250, 1), new WorldTile(4508, 6253, 1), new WorldTile(4509, 6254, 1), new WorldTile(4510, 6254, 1), new WorldTile(4510, 6255, 1), new WorldTile(4511, 6255, 1) });
        } else if (fightRotation == 1) {
            extraBlockedTiles.add(new WorldTile[] { new WorldTile(4503, 6274, 1), new WorldTile(4507, 6271, 1), new WorldTile(4509, 6271, 1), new WorldTile(4508, 6271, 1) });
        } else if (fightRotation == 2) {
            extraBlockedTiles.add(new WorldTile[] { new WorldTile(4511, 6268, 1), new WorldTile(4511, 6267, 1), new WorldTile(4511, 6266, 1), new WorldTile(4511, 6265, 1), new WorldTile(4511, 6264, 1), new WorldTile(4511, 6263, 1) });
        }
        if (fightRotation != 0)
            objects.add(new WorldObject(PATH_SPECIFIC_OBJECT_IDS[2][2], 10, 0, getTile(new WorldTile(4509, 6252, 1))));
        if (fightRotation != 1)
            objects.add(new WorldObject(PATH_SPECIFIC_OBJECT_IDS[0][2], 10, 0, getTile(new WorldTile(4506, 6274, 1))));
        if (fightRotation != 2)
            objects.add(new WorldObject(PATH_SPECIFIC_OBJECT_IDS[1][2], 10, 0, getTile(new WorldTile(4513, 6264, 1))));
        acidGround = new WorldObject(ACID_GROUND_OBJECT_ID, 10, 0, getTile(new WorldTile(4536, 6260, 1)));
        objects.add(acidGround);// AcidGround
        darknessWall = new WorldObject(DARKNESS_WALL_GROUND_OBJECT_ID, 10, 0, getTile(new WorldTile(4550, 6246, 1)));
        objects.add(darknessWall);// DarknessWall
        for (WorldObject object : objects) {
            if (object == null)
                continue;
            World.getRegion(object.getRegionId(), true);
            World.spawnObject(object);
        }
        blockedTiles = new ArrayList<WorldTile>();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (blockedTiles == null)
                    blockedTiles = new ArrayList<WorldTile>();
                for (WorldTile[] tiles : blockedPaths) {
                    for (WorldTile tile : tiles) {
                        WorldTile instancedTile = getTile(tile);
                        World.forceSetMask(instancedTile.getPlane(), instancedTile.getX(), instancedTile.getY(), Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ_BLOCKSWALK_ALTERNATIVE);
                        blockedTiles.add(tile);
                    }
                }
                for (WorldTile[] tiles : extraBlockedTiles) {
                    for (WorldTile tile : tiles) {
                        WorldTile instancedTile = getTile(tile);
                        World.forceSetMask(instancedTile.getPlane(), instancedTile.getX(), instancedTile.getY(), Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ_BLOCKSWALK_ALTERNATIVE);
                        blockedTiles.add(tile);
                    }
                }

                WorldTile max = getTile(4559, 6265, 1);
                WorldTile min = getTile(4559, 6261, 1);
                for (int y = min.getY(); y <= max.getY(); y++) {
                    World.forceSetMask(1, max.getX(), y, Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ_BLOCKSWALK_ALTERNATIVE);
                    blockedTiles.add(new WorldTile(max.getX(), y, 1));
                }
                resettingInstance = false;
            }
        }, 5);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                WorldTile baseCoord = new WorldTile(4550, 6245, 1);
                for (int i = 0; i < 11; i++) {
                    WorldTile tile = baseCoord.transform(0, i, 0);
                    WorldTile instancedTile = getTile(tile);
                    World.forceSetMask(instancedTile.getPlane(), instancedTile.getX(), instancedTile.getY(), Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ_BLOCKSWALK_ALTERNATIVE);
                    blockedTiles.add(tile);
                }
            }
        }, 15);
    }

    private void reset() {
        if (!isPublic())
            return;
        blockedTiles = new ArrayList<WorldTile>();
        playersInside = new ArrayList<Player>(2);
        loadMapInstance();
    }

    @Override
    public void finish() {
        if (resettingInstance)
            return;
        resettingInstance = true;
        for (int i = 0; i < objects.size(); i++) {
            WorldObject object = objects.get(i);
            if (object == null)
                continue;
            if (World.containsObjectWithId(object, object.getId()))
                World.removeObject(object);
        }
        acidGround = null;
        darknessWall = null;
        araxxor = null;
        allowedFriendDisplayName = null;
        objects = null;
        if (blockedTiles != null) {
            clearBlockedPaths();
        }
        blockedTiles = null;
        playersInside = null;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                reset();
            }

        });
        super.finish();
    }

    private void clearBlockedPaths() {
        if (!isPublic() || blockedTiles == null)
            return;
        for (WorldTile tile : blockedTiles)
            World.setMask(tile.getPlane(), tile.getX(), tile.getY(), 0);
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        synchronized (BossInstanceHandler.LOCK) {
            getPlayers().add(player);
            if (isPublic())
                enterFightRoom(player);
            else {
                player.useStairs(-1, getTile(new WorldTile(player)), 0, 2);
                player.getPackets().sendGameMessage("Welcome to this session against <col=00FFFF>Araxxor</col>. This arena will expire in " + (getSettings().getTimeRemaining() / 60000) + " minutes.");
            }
            player.setLargeSceneView(true);
            player.getControlerManager().forceStop();
            player.getControlerManager().startControler(getSettings().getBoss().getControllerName(), this);
            player.setLastBossInstanceKey(getOwner() == null ? null : getOwner().getUsername());
        }
    }

    @Override
    public void leaveInstance(Player player, int type) {
        synchronized (BossInstanceHandler.LOCK) {
            player.stopAll();
            player.getTemporaryAttributtes().clear();
            player.setForceMultiArea(false);
            player.setLargeSceneView(true);
            player.getLocalNPCUpdate().reset();
            removeDarkness(player);
            player.getInterfaceManager().closeOverlay(true);
            player.lock();
            player.resetCombat();
            if (getPlayers().size() <= 1 && !this.isPublic()) {
                destroyRegion(player.getRegionId());
            }
            if (type == EXITED)
                player.useStairs(-1, getSettings().getBoss().getOutsideTile(), 0, 2);
            else if (type == LOGGED_OUT)
                player.setLocation(getSettings().getBoss().getOutsideTile());
            player.getMusicsManager().reset();
            getPlayers().remove(player);
            if (playersInside != null)
                playersInside.remove(player);
            if (playersInside == null || playersInside.isEmpty() || getPlayers().isEmpty() || (isPublic() && playersInside.isEmpty())) {
                finish();
            }
            player.unlock();
        }
    }

    public void enterFightRoom(Player player) {
        if (playersInside.size() >= 2)
            return;
        if (araxxor != null) {
            player.getPackets().sendGameMessage("Some mysterious power is preventing you from joining the fight.");
            return;
        }
        player.setForceMultiArea(true);
        playersInside.add(player);
        player.useStairs(-1, getTile(new WorldTile(4490, 6265, 1)), 0, 2);
        if (getPlayersInsideCount() == 1)
            startFight();
    }

    public void startFight() {
        SpiderBossInstance thisInstance = this;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (playersInside == null || playersInside.isEmpty()) {
                    stop();
                    return;
                }
                int random = Utils.random(3);
                int id = random == 0 ? 19457 : random == 1 ? 19462 : 19463;
                Player creater = playersInside.get(0) != null ? playersInside.get(0) : null;
                if (creater != null && (!isPublic() || creater.getInventory().containsItem(33870, 1))) {
                    int combatStyle = creater.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
                    id = combatStyle == Combat.MELEE_TYPE ? 19463 : combatStyle == Combat.RANGE_TYPE ? 19462 : 19457;
                }
                araxxor = new Araxxor(id, getTile(new WorldTile(4501, 6262, 1)), thisInstance);
                araxxor.setCombatStyle(id == 19457 ? NPCCombatDefinitionConstants.MELEE : id == 19462 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.RANGE);
                araxxor.setNextFaceWorldTile(getTile(new WorldTile(4495, 6264, 1)));
                araxxor.setNextAnimation(new Animation(24076));
                araxxor.setNextGraphics(new Graphics(4984));
            }
        }, 15);
    }

    public boolean playerIsInsideBattle(Player player) {
        if (playersInside == null)
            return false;
        return playersInside.contains(player);
    }

    public int getPlayersInsideCount() {
        int count = 0;
        for (Player player : playersInside) {
            if (player == null)
                continue;
            count++;
        }
        return count;
    }

    public List<Player> getPlayersInside() {
        return playersInside;
    }

    public String getAllowedFriendDisplayName() {
        return allowedFriendDisplayName;
    }

    public void setAllowedFriendDisplayName(String allowedFriendUsername) {
        this.allowedFriendDisplayName = allowedFriendUsername;
    }

    public Araxxor getAraxxor() {
        return araxxor;
    }

    public List<WorldTile> getBlockedTiles() {
        return blockedTiles;
    }

    public void updateInterface(boolean sendInterface) {
        if (playersInside == null || playersInside.isEmpty())
            return;
        for (Player player : playersInside) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            updateInterface(player, sendInterface);
        }
    }

    public void removeHealthBar() {
        if (playersInside == null || playersInside.isEmpty())
            return;
        for (Player player : playersInside) {
            if (player == null || player.isDead())
                continue;
            removeHealthBar(player);
        }
    }

    public void removeHealthBar(Player player) {
        player.getPackets().closeInterface(InterfaceManager.getComponentUId(1515, 14));
        player.getPackets().sendHideIComponent(1515, 14, true);
    }

    public void sendMessage(String message) {
        if (playersInside == null || playersInside.isEmpty())
            return;
        for (Player player : playersInside) {
            if (player == null || player.isDead())
                continue;
            sendMessage(player, message);
        }
    }

    public void sendMessage(Player player, String message) {
        player.getPackets().sendExecuteScript(1211, message, 5, -120, 1);
        player.getPackets().sendGameMessage(message, true);
    }

    public void sendDarkness(Player player) {
        player.getPackets().sendIComponentColour(1515, 12, 0);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;

            @Override
            public boolean repeat() {
                try {
                    if (loop == 180)
                        return false;
                    player.getPackets().sendExecuteScript(10097, 75);
                    loop++;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
                return true;
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void sendRedScreen(Player player, int transparency) {
        player.getPackets().sendIComponentColour(1515, 12, 51200);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;

            @Override
            public boolean repeat() {
                try {
                    if (loop == 180)
                        return false;
                    player.getPackets().sendExecuteScript(10097, 75);
                    loop++;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
                return true;
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void removeDarkness(Player player) {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;

            @Override
            public boolean repeat() {
                try {
                    if (loop == 180)
                        return false;
                    player.getPackets().sendExecuteScript(10097, 255);
                    loop++;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
                return true;
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void removeInterfaces() {
        if (playersInside == null || playersInside.isEmpty())
            return;
        for (Player player : playersInside) {
            if (player == null || player.isDead())
                continue;
            player.getInterfaceManager().closeOverlay(true);
        }
    }

    public void updateInterface(Player player, boolean sendInterface) {
        if (araxxor == null)
            return;
        if (sendInterface) {
            player.getPackets().sendHideIComponent(1515, 14, false);
            player.getPackets().closeInterface(InterfaceManager.getComponentUId(1515, 14));
            player.getInterfaceManager().setInterface(true, 1515, 14, 1648);
        }
        boolean araxxi = araxxor.getId() == 19464;
        boolean dead = araxxor.getTemporaryAttributtes().get("dead") != null || araxxor.hasFinished();
        player.getVarBitManager().sendVar(5776, araxxi ? ARAXXI_HEALTH_DATA_ID : ARAXXOR_HEALTH_DATA_ID);
        player.getVarBitManager().sendVarBit(32672, araxxor.getMaxHitpoints() * 10);
        player.getVarBitManager().sendVarBit(28663, araxxor.getHitpoints() * 10);
        player.getPackets().sendHideIComponent(1515, 21, araxxor.getSpidersLeft().isEmpty() || dead);// spidersLeft
        player.getPackets().sendHideIComponent(1515, 23, araxxor.getAcidInPool() == 0 || dead);// acidinpool
        player.getPackets().sendHideIComponent(1515, 24, araxxor.getExtraHP() == 0 || araxxor.getPhase() <= 1 || dead);// extrahp
        player.getPackets().sendHideIComponent(1515, 25, araxxor.getFightEnrage() == 0 || dead);// enrage
        player.getPackets().sendGlobalConfig(4276, araxxor.getSpidersLeft().size());
        player.getPackets().sendGlobalConfig(4277, araxxor.getAcidInPool());
        player.getPackets().sendGlobalConfig(4278, araxxor.getExtraHP());
        player.getPackets().sendGlobalConfig(4279, araxxor.getFightEnrage());
        if (araxxor.getPhase() != 0) {
            int barType = (araxxor.getPhase() == 1 && araxxor.getChoosenPath() == 2) ? -1337 : -1;
            boolean hidebar = barType == -1337 ? (!araxxor.isShowWallHealth()) : (araxxor.getAbsorpedAcid() == 0);
            player.getPackets().sendHideIComponent(1515, 18, hidebar || dead);
            player.getPackets().sendExecuteScript(10104, barType);
            player.getPackets().sendGlobalConfig(4274, araxxor.getAbsorpedAcid());
            player.getPackets().sendGlobalConfig(4275, (araxxor.getWallHealth() * 10) / 100);
        }
    }

    public void sendBurnCountDown() {
        for (Player player : playersInside) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendHideIComponent(1515, 18, false);
            player.getPackets().sendExecuteScript(10104, 60);// 60seconds
        }
    }

    public void removeBurnCountDown() {
        for (Player player : playersInside) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendHideIComponent(1515, 18, true);
        }
    }

    public WorldTile getRandomWorldTile() {
        return getRandomWorldTile(araxxor, 0, 7, false);
    }

    public boolean isInsidePhase4Area(WorldTile tile) {
        boolean insidePhase4 = false;
        for (int[] area : phase_4_fight_area) {
            WorldTile minTile = getTile(area[0], area[1], 1);
            WorldTile maxTile = getTile(area[2], area[3], 1);
            for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
                for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                    if (tile.matches(new WorldTile(x, y, 1))) {
                        insidePhase4 = true;
                        break;
                    }
                }
                if (insidePhase4)
                    break;
            }
        }
        return insidePhase4;
    }

    public WorldTile getRandomWorldTile(Entity checkEntity, int minDistance, int maxDistance, boolean skipUnder) {
        return getRandomWorldTile(checkEntity, minDistance, maxDistance, skipUnder, false, null);
    }

    public WorldTile getRandomWorldTile(int minDistance, int maxDistance, boolean sunlight) {
        return getRandomWorldTile(araxxor, minDistance, maxDistance, false, true, null);
    }

    public WorldTile getRandomWorldTile(Entity checkEntity, int minDistance, int maxDistance, boolean skipUnder, boolean sunlight, List<WorldTile> exceptions) {
        if (araxxor == null || araxxor.hasFinished() || checkEntity == null || checkEntity.hasFinished() || playersInside.isEmpty())
            return null;
        List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
        WorldTile checkTile = new WorldTile(checkEntity.getMiddleWorldTile());
        if (araxxor.getPhase() == 3) {
            for (int[] area : phase_4_fight_area) {
                WorldTile minTile = getTile(area[0], area[1], 1);
                WorldTile maxTile = getTile(area[2], area[3], 1);
                for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
                    for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                        WorldTile tile = new WorldTile(x, y, checkTile.getPlane());
                        if (World.getObjectWithType(tile, 10) != null || !World.canMoveNPC(tile.getPlane(), x, y, checkEntity.getSize()) || Utils.getDistance(checkTile, tile) > maxDistance || Utils.getDistance(checkTile, tile) < minDistance || (skipUnder && Utils.colides(checkTile, tile, checkEntity.getSize(), 1)))
                            continue;
                        if (exceptions != null && containsTile(exceptions, tile))
                            continue;
                        if (sunlight) {
                            boolean skip = false;
                            for (int i = tile.getX(); i <= tile.getX() + 2; i++) {
                                if (skip)
                                    break;
                                for (int j = tile.getY(); j <= tile.getY() + 2; j++) {
                                    if (skip)
                                        break;
                                    WorldTile testTile = new WorldTile(i, j, checkTile.getPlane());
                                    if (World.getObjectWithType(testTile, 10) != null || !World.canMoveNPC(testTile.getPlane(), i, j, 1)) {
                                        skip = true;
                                        continue;
                                    }
                                }
                            }
                            if (skip)
                                continue;
                        }
                        possibleTiles.add(tile);
                    }
                }
            }
            return possibleTiles.get(Utils.random(possibleTiles.size()));
        }
        if ((araxxor.getChoosenPath() == -1 || araxxor.getPhase() == 0 || (araxxor.getPhase() == 1) || (araxxor.getPhase() == 2 && (araxxor.getChoosenPath() != 1)))) {
            for (int[] area : phase_1_fight_area) {
                WorldTile minTile = getTile(area[0], area[1], 1);
                WorldTile maxTile = getTile(area[2], area[3], 1);
                for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
                    for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                        WorldTile tile = new WorldTile(x, y, checkTile.getPlane());
                        if (World.getObjectWithType(tile, 10) != null || !World.canMoveNPC(tile.getPlane(), x, y, checkEntity.getSize()) || Utils.getDistance(checkTile, tile) > maxDistance || Utils.getDistance(checkTile, tile) < minDistance || (skipUnder && Utils.colides(checkTile, tile, checkEntity.getSize(), 1)))
                            continue;
                        if (exceptions != null && containsTile(exceptions, tile))
                            continue;
                        if (sunlight) {
                            boolean skip = false;
                            for (int i = tile.getX(); i <= tile.getX() + 2; i++) {
                                if (skip)
                                    break;
                                for (int j = tile.getY(); j <= tile.getY() + 2; j++) {
                                    if (skip)
                                        break;
                                    WorldTile testTile = new WorldTile(i, j, checkTile.getPlane());
                                    if (World.getObjectWithType(testTile, 10) != null || !World.canMoveNPC(testTile.getPlane(), i, j, 1)) {
                                        skip = true;
                                        continue;
                                    }
                                }
                            }
                            if (skip)
                                continue;
                        }
                        possibleTiles.add(tile);
                    }
                }
            }
        }
        if (araxxor.getPhase() == 1 || (araxxor.getPhase() == 2)) {
            List<WorldTile> path2Tiles = new ArrayList<WorldTile>();
            for (int path = 0; path < phase_2_fight_areas.length; path++) {
                if (araxxor.getPhase() == 1 && path != araxxor.getChoosenPath() || (araxxor.getPhase() == 2 && path == 1) || (araxxor.getPhase() == 2 && araxxor.getChoosenPath() != path && path != 0))
                    continue;
                for (int[] area : phase_2_fight_areas[path]) {
                    WorldTile minTile = getTile(area[0], area[1], 1);
                    WorldTile maxTile = getTile(area[2], area[3], 1);
                    for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
                        for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                            WorldTile tile = new WorldTile(x, y, checkTile.getPlane());
                            if (World.getObjectWithType(tile, 10) != null || !World.canMoveNPC(tile.getPlane(), x, y, checkEntity.getSize()) || Utils.getDistance(checkTile, tile) > maxDistance || Utils.getDistance(checkTile, tile) < minDistance || (skipUnder && Utils.colides(checkTile, tile, checkEntity.getSize(), 1)))
                                continue;
                            if (exceptions != null && containsTile(exceptions, tile))
                                continue;
                            if (sunlight) {
                                boolean skip = false;
                                for (int i = tile.getX(); i <= tile.getX() + 2; i++) {
                                    if (skip)
                                        break;
                                    for (int j = tile.getY(); j <= tile.getY() + 2; j++) {
                                        if (skip)
                                            break;
                                        WorldTile testTile = new WorldTile(i, j, checkTile.getPlane());
                                        if (World.getObjectWithType(testTile, 10) != null || !World.canMoveNPC(testTile.getPlane(), i, j, 1)) {
                                            skip = true;
                                            continue;
                                        }
                                    }
                                }
                                if (skip)
                                    continue;
                            }
                            path2Tiles.add(tile);
                        }
                    }
                }
            }
            possibleTiles.addAll(path2Tiles);
        }
        if (araxxor.getPhase() == 2) {
            for (int[] area : phase_3_fight_area) {
                WorldTile minTile = getTile(area[0], area[1], 1);
                WorldTile maxTile = getTile(area[2], area[3], 1);
                for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
                    for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                        WorldTile tile = new WorldTile(x, y, checkTile.getPlane());
                        if (World.getObjectWithType(tile, 10) != null || !World.canMoveNPC(tile.getPlane(), x, y, checkEntity.getSize()) || Utils.getDistance(checkTile, tile) > maxDistance || Utils.getDistance(checkTile, tile) < minDistance || (skipUnder && Utils.colides(checkTile, tile, checkEntity.getSize(), 1)))
                            continue;
                        if (exceptions != null && containsTile(exceptions, tile))
                            continue;
                        if (sunlight) {
                            boolean skip = false;
                            for (int i = tile.getX(); i <= tile.getX() + 2; i++) {
                                if (skip)
                                    break;
                                for (int j = tile.getY(); j <= tile.getY() + 2; j++) {
                                    if (skip)
                                        break;
                                    WorldTile testTile = new WorldTile(i, j, checkTile.getPlane());
                                    if (World.getObjectWithType(testTile, 10) != null || !World.canMoveNPC(testTile.getPlane(), i, j, 1)) {
                                        skip = true;
                                        continue;
                                    }
                                }
                            }
                            if (skip)
                                continue;
                        }
                        possibleTiles.add(tile);
                    }
                }
            }
        }
        if (possibleTiles.isEmpty()) {
            return getRandomWorldTile(checkEntity, minDistance, maxDistance + 5, skipUnder, sunlight, null);
        }
        return possibleTiles.get(Utils.random(possibleTiles.size()));
    }

    public static boolean containsTile(List<WorldTile> list, WorldTile checkTile) {
        for (WorldTile tile : list)
            if (tile.matches(checkTile))
                return true;
        return false;
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public WorldObject getAcidGround() {
        return acidGround;
    }

    public WorldObject getDarknessWall() {
        return darknessWall;
    }

    public void spawnArraxorBody(WorldObject araxxorBody) {
        if (objects != null) {
            objects.add(araxxorBody);
            World.spawnObject(araxxorBody);
        }
    }

    public void openRewardChest(Player player) {
        if (araxxor == null || araxxor.getRewards().get(player) == null)
            return;
        player.getTemporaryAttributtes().put("ArraxorRewards", Boolean.TRUE);
        player.getInterfaceManager().sendInterface(1284);
        refreshRewardsInterface(player);
        player.setCloseInterfacesEvent(new Runnable() {

            @Override
            public void run() {
                player.getTemporaryAttributtes().remove("ArraxorRewards");
            }
        });
    }

    public void HandleButtons(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 1284) {
            if (araxxor == null || araxxor.getRewards().get(player) == null)
                return;
            switch (componentId) {
            case 8:
                player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                player.getBank().addItems(araxxor.getRewards().get(player).toArray(), true);
                araxxor.getRewards().get(player).clear();
                refreshRewardsInterface(player);
                player.getPackets().sendGameMessage("All the items were moved to your bank.");
                break;
            case 9:
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        int currentEnrage = araxxor.getFightEnrage() - araxxor.getBaseEnrage();
                        sendDialogue("Based on fight performance, your chance of receiving the pet was 1/" + (1000 - currentEnrage) + ".", "<col=ff0000>Gambling will forteil all items in the corpse but will increase your chance of winning the pet to:</col>", "1/" + ((1000 - currentEnrage) / 2) + "", "Roll a 0 to win!");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        switch (stage) {
                        case -1:
                            if (player.getTemporaryAttributtes().get("removePetChance") != null) {
                                end();
                                return;
                            }
                            stage = 0;
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Roll for pet", "Nevermind.");
                            break;
                        case 0:
                            if (componentId == OPTION_2 || player.getTemporaryAttributtes().get("removePetChance") != null) {
                                end();
                                return;
                            }
                            player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                            araxxor.getRewards().get(player).clear();
                            refreshRewardsInterface(player);
                            stage = 1;
                            int currentEnrage = araxxor.getFightEnrage() - araxxor.getBaseEnrage();
                            int roll = (int) (Math.random() * (1000.00 - (double) currentEnrage) / 2.00);
                            sendDialogue("You rolled " + "<col=" + (roll == 0 ? "00ff00" : "FF0000") + ">" + roll + "</col>", roll == 0 ? "<col=00ff00>CONGRATULATIONS - PET UNLOCKED" : "<col=FF0000>Better luck next time!");
                            if (roll == 0) {
                                int petId = Araxxor.ARAXXOR_PETS[Utils.random(Araxxor.ARAXXOR_PETS.length)];
                                Item pet = new Item(petId, 1);
                                World.sendNews(player, player.getDisplayName() + " has received " + pet.getName() + " drop!", World.WORLD_NEWS);
                                if (!player.getInventory().addItem(pet)) {
                                    player.getBank().addItem(pet, true);
                                    player.getPackets().sendGameMessage("Your pet has been added to your bank.");
                                    return;
                                }
                                player.getPackets().sendGameMessage("Your pet has been added to your inventory.");
                            }
                            break;
                        case 1:
                            end();
                            break;
                        }
                    }

                    @Override
                    public void finish() {
                    }
                });
                break;
            case 10:
                player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                for (int slot = 0; slot < araxxor.getRewards().get(player).toArray().length; slot++) {
                    Item item = araxxor.getRewards().get(player).get(slot);
                    if (item == null) {
                        continue;
                    }
                    boolean added = true;
                    if (item.getDefinitions().isStackable() || item.getAmount() < 2) {
                        added = player.getInventory().addItem(item);
                        if (added) {
                            araxxor.getRewards().get(player).toArray()[slot] = null;
                        }
                    } else {
                        for (int i = 0; i < item.getAmount(); i++) {
                            Item single = new Item(item.getId());
                            if (!player.getInventory().addItem(single)) {
                                added = false;
                                break;
                            }
                            araxxor.getRewards().get(player).remove(single);
                        }
                    }
                    refreshRewardsInterface(player);
                    if (!added) {
                        player.getPackets().sendGameMessage("You only had enough space in your inventory to accept some of the items.");
                        break;
                    }
                }
                break;
            case 7:
                Item item = araxxor.getRewards().get(player).get(slotId);
                if (item == null) {
                    return;
                }
                switch (packetId) {
                case PacketRepository.ACTION_BUTTON4_PACKET:
                    player.getPackets().sendGameMessage("It's a " + item.getDefinitions().getName());
                    return;
                case PacketRepository.ACTION_BUTTON3_PACKET:
                    player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                    araxxor.getRewards().get(player).toArray()[slotId] = null;
                    refreshRewardsInterface(player);
                    return;
                case PacketRepository.ACTION_BUTTON2_PACKET:
                    player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                    player.getBank().addItems(new Item[] { araxxor.getRewards().get(player).toArray()[slotId] }, true);
                    araxxor.getRewards().get(player).toArray()[slotId] = null;
                    refreshRewardsInterface(player);
                    return;
                case PacketRepository.ACTION_BUTTON1_PACKET:
                    player.getTemporaryAttributtes().put("removePetChance", Boolean.TRUE);
                    boolean added = true;
                    if (item.getDefinitions().isStackable() || item.getAmount() < 2) {
                        added = player.getInventory().addItem(item);
                        if (added) {
                            araxxor.getRewards().get(player).toArray()[slotId] = null;
                        }
                    } else {
                        for (int i = 0; i < item.getAmount(); i++) {
                            Item single = new Item(item.getId());
                            if (!player.getInventory().addItem(single)) {
                                added = false;
                                break;
                            }
                            araxxor.getRewards().get(player).remove(single);
                        }
                    }
                    refreshRewardsInterface(player);
                    if (!added) {
                        player.getPackets().sendGameMessage("You only had enough space in your inventory to accept some of the items.");
                        break;
                    }
                    return;
                }
            }
        }
    }

    public void refreshRewardsInterface(Player player) {
        if (araxxor.getRewards().get(player) == null)
            return;
        player.getInterfaceManager().closeChatBoxInterface();
        player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 3, "Take", "Bank", "Discard", "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 10, 0, 1, 2, 3);
        player.getPackets().sendIComponentText(1284, 42, "Reroll");
        player.getPackets().sendItems(99, araxxor.getRewards().get(player));
        player.getPackets().sendHideIComponent(1284, 9, player.getTemporaryAttributtes().get("removePetChance") != null);
    }

    public static void checkChangeRotation() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(Settings.SPIDER_BOSS_RELEASE_DATE, formatter);
        LocalDate currentDate = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(startDate, currentDate);
        int rotation = 0;
        for (int i = 0; i < daysPassed / Settings.SPIDER_BOSS_DAYS_TO_CHANGE_ROTATION; i++) {
            rotation = rotation + 1 >= Settings.SPIDER_BOSS_ROTATION_NAMES.length ? 0 : rotation + 1;
        }
        if (rotation != Settings.SPIDER_BOSS_ROTATION)
            checkChangeInstanceRotation();
        Settings.SPIDER_BOSS_ROTATION = rotation;
    }

    public static void checkChangeInstanceRotation() {
        SpiderBossInstance instance = (SpiderBossInstance) BossInstanceHandler.findInstance(Boss.Spider_Boss, "");
        if (instance != null && instance.getPlayersInside().isEmpty()) {
            instance.finish();
        }
    }

    public int getFightRotation() {
        return fightRotation;
    }

    public void setFightRotation(int fightRotation) {
        this.fightRotation = fightRotation;
    }

}