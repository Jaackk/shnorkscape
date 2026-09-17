package com.rs.game.map.bossInstance.impl;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.EffectsManager.Effect;
import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class TelosInstance extends BossInstance {

    public static final int[][] FIGHT_AREA = new int[][] { { 3786, 3809, 7050, 7073 }, { 3850, 3873, 7050, 7073 }, { 3786, 3809, 6986, 7009 }, { 3852, 3889, 6989, 7026 }, { 3852, 3889, 6989, 7026 } };

    public static final WorldTile[] TELOS_SPAWN_TILE = { new WorldTile(3795, 7060, 0), new WorldTile(3860, 7060, 0), new WorldTile(3795, 6995, 0), new WorldTile(3873, 7020, 1), new WorldTile(3866, 7003, 1) };

    public static final WorldTile[] PLAYER_START_TILE = { new WorldTile(3809, 7053, 0), new WorldTile(3858, 7058, 0), new WorldTile(3803, 6992, 0), new WorldTile(3862, 7016, 1), new WorldTile(3871, 6995, 1) };

    public static final WorldTile[] PLAYER_JUMP_TILE = { new WorldTile(3809, 7073, 0), new WorldTile(3850, 7072, 0), new WorldTile(3809, 6988, 0), null, null };

    public static final WorldTile[] ANIMA_GOLEM_SPAWM_TILE = { new WorldTile(3802, 6992, 0), new WorldTile(3799, 7002, 0), new WorldTile(3793, 6992, 0) };

    public static final WorldTile[] FONT_SPAWM_TILE = { new WorldTile(3858, 7019, 1), new WorldTile(3885, 7013, 1), new WorldTile(3869, 6990, 1) };

    public static final WorldTile[] TELOS_SIPHON_TILE = { new WorldTile(3866, 7016, 1), new WorldTile(3876, 7015, 1), new WorldTile(3871, 6995, 1) };

    public static final WorldTile TELOS_DEATH_TILE = new WorldTile(3868, 6996, 1);

    public static final int GREEN_STREAM = 0, BLACK_STREAM = 1, RED_STREAM = 2;
    public static final int STREAM_BASE_ID = 103555;
    public static final int[][] STREAMS = new int[][] { { 103545, 103551, 103552 }, { 103547, 103581, 103583 }, { 103549, 103582, 103584 } };// piece/endplayer/endtelos

    private transient Telos telos;
    private final transient Stream[] streams;
    private transient WorldObject exitBeam;
    private NPC soothSayer;
    private WorldObject bank;

    public TelosInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
        streams = new Stream[3];
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 472, 872 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 2, 2 };
    }

    public Player getPlayer() {
        return getPlayers().isEmpty() ? null : getPlayers().get(0);
    }

    @Override
    public void loadMapInstance() {
        exitBeam = new WorldObject(103562, 10, 0, getTile(3870, 7006, 1));
        telos = new Telos(22892, getTile(TELOS_SPAWN_TILE[0]), this);
        telos.setNextFaceWorldTile(new WorldTile(telos.getX() + telos.getSize(), telos.getY(), telos.getPlane()));
        telos.setForceMultiArea(true);
        soothSayer = new NPC(22889, getTile(new WorldTile(3819, 7048, 0)), 0, false);
        bank = new WorldObject(4483, 10, 0, getTile(new WorldTile(3816, 7056, 0)));
        World.spawnObject(bank);
        addStreamsTask();
        World.spawnObject(new WorldObject(exitBeam));
        /*
         * telos = new Telos(22908, getTile(3867, 7004, 1), this);
         * telos.setNextFaceWorldTile(getTile(3875, 7023, 1));
         */
    }

    public String getInstanceName() {
        return "Telos, the Warden";
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        super.enterInstance(player, login);
        player.setForceMultiArea(true);
        player.setLargeSceneView(true);
    }

    @Override
    public void leaveInstance(Player player, int type) {
        player.getInterfaceManager().closeOverlay(true);
        telos.resetPlayerEffects();
        player.setForceMultiArea(false);
        player.setLargeSceneView(false);
        super.leaveInstance(player, type);
    }

    @Override
    public void finish() {
        if (telos != null)
            telos.finish();
        if (World.containsObjectWithId(exitBeam, exitBeam.getId()))
            World.removeObject(exitBeam);
        destroyStreams();
        World.removeObject(bank);
        soothSayer.finish();
        super.finish();
    }

    public void skipPhase() {
        if (telos != null) {
            if (telos.getPhase() <= 2) {
                telos.setHitpoints((int) (telos.getMaxHitpoints() * (0.75 - (0.25 * telos.getPhase()))));
                telos.switchPhase();
            } else {
                if (getPlayer() != null)
                    telos.sendDeath(getPlayer());
            }
        }
    }

    private void addStreamsTask() {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                try {
                    if (isFinished() || isPublic() || telos.getTemporaryAttributtes().get("dead") != null) {
                        destroyStreams();
                        return !allStreamsDestroyed();
                    }
                    updateStreams();
                    return true;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }

        }, 600, 600, TimeUnit.MILLISECONDS);
    }

    public boolean allStreamsDestroyed() {
        for (int i = 0; i < streams.length; i++)
            if (streams[i] != null)
                return false;
        return true;
    }

    public Telos getTelos() {
        return telos;
    }

    public void createStream() {
        if (telos.getPhase() == 4) {
            int firstRotation = Utils.random(3);
            int firstColor = Utils.random(3);
            for (int i = 0; i < streams.length; i++) {
                if (streams[i] != null) {
                    streams[i].destroy();
                    streams[i] = null;
                }
            }
            for (int i = 0; i < streams.length; i++) {
                streams[i] = new Stream(firstColor + i > 2 ? ((firstColor + i) - 3) : firstColor + i, firstRotation + i > 2 ? ((firstRotation + i) - 3) : firstRotation + i, this);
            }
            return;
        }
        if (streams[0] != null) {
            streams[0].destroy();
            streams[0] = null;
            if (telos.getPhase() == 0)
                return;
        }
        streams[0] = new Stream(this);
    }

    public void destroyStreams() {
        for (Stream stream : streams)
            if (stream != null)
                stream.destroy();
    }

    public void updateStreams() {
        for (Stream stream : streams)
            if (stream != null)
                stream.checkUpdateStream();
    }

    public WorldObject getExitBeam() {
        return exitBeam;
    }

    public static final class Stream {

        private final int type;
        private final int rotation;
        private int startX;
        private int startY;
        private transient WorldObject[] stream;
        private final transient TelosInstance instance;
        private transient Entity effectEntity;

        public Stream(int type, int rotation, TelosInstance instance) {
            this.type = type;
            this.rotation = rotation;
            this.instance = instance;
            // North,EAST,WEST,SOUTH
            stream = new WorldObject[25];
            boolean onTelos = instance.getTelos().getPhase() != 2;
            if (onTelos) {
                WorldTile middleWorldTile = instance.getTelos().getMiddleWorldTile();
                int[] area = FIGHT_AREA[instance.getTelos().getPhase()];
                WorldTile minTile = instance.getTile(area[0], area[2], instance.getTelos().getPlane());
                WorldTile maxTile = instance.getTile(area[1], area[3], instance.getTelos().getPlane());
                area = new int[] { minTile.getX(), maxTile.getX(), minTile.getY(), maxTile.getY() };
                startX = (rotation == 0 || rotation == 3) ? middleWorldTile.getX() : rotation == 1 ? area[1] : area[0];
                startY = rotation == 0 ? area[3] : (rotation == 1 || rotation == 2) ? middleWorldTile.getY() : area[2];
            } else {
                int[] area = FIGHT_AREA[instance.getTelos().getPhase()];
                WorldTile minTile = instance.getTile(area[0], area[2], instance.getTelos().getPlane());
                WorldTile maxTile = instance.getTile(area[1], area[3], instance.getTelos().getPlane());
                area = new int[] { minTile.getX(), maxTile.getX(), minTile.getY(), maxTile.getY() };
                startX = (rotation == 0 || rotation == 3) ? Utils.random(area[0] + 3, area[1] - 2) : rotation == 1 ? area[1] : area[0];
                startY = rotation == 0 ? area[3] : (rotation == 1 || rotation == 2) ? Utils.random(area[2] + 3, area[3] - 2) : area[2];
            }
            if (rotation == 0 || rotation == 3)
                startY += rotation == 0 ? 1 : -1;
            if (rotation == 1 || rotation == 2)
                startX += rotation == 1 ? 1 : -1;
            if (rotation != 0 && instance.getTelos().getPhase() == 4)
                startY -= 5;
            updateStream();
        }

        public WorldTile getWorldTileByIndex(int i) {
            int x = (rotation == 0 || rotation == 3) ? startX : rotation == 1 ? (startX - i) : (startX + i);
            int y = rotation == 0 ? (startY - i) : rotation == 1 || rotation == 2 ? startY : (startY + i);
            int plane = instance.getTelos().getPlane();
            WorldTile spawnTile = new WorldTile(x, y, plane);
            return spawnTile;
        }

        public Entity getEntityByIndex(int i) {
            int x = (rotation == 0 || rotation == 3) ? startX : rotation == 1 ? (startX - i) : (startX + i);
            int y = rotation == 0 ? (startY - i) : rotation == 1 || rotation == 2 ? startY : (startY + i);
            int plane = instance.getTelos().getPlane();
            WorldTile spawnTile = new WorldTile(x, y, plane);
            if (instance.getTelos().getPhase() == 4 && i >= 14) {
                if ((i == 14 && instance.getPlayer().matches(getWorldTileByIndex(14))) || (i == 15 && instance.getPlayer().matches(getWorldTileByIndex(15)))) {
                    return instance.getPlayer();
                } else if ((rotation == 2 && i == 14 && !instance.getPlayer().matches(getWorldTileByIndex(15))) || (rotation != 2 && i == 15)) {
                    return instance.getTelos();
                }
                return null;
            } else {
                if (instance.getPlayer() != null && !instance.getPlayer().isDead() && (Utils.colides(spawnTile, instance.getPlayer(), 1, 1))) {
                    return instance.getPlayer();
                }
                if (!(i == 14 && instance.getPlayer().matches(instance.getTile(3866, 7002, 1))) && instance.getTelos() != null && !instance.getTelos().isDead() && (instance.getTelos().getPhase() == 4 ? spawnTile.withinArea(instance.getTelos().getX() - 1, instance.getTelos().getY() - 1, instance.getTelos().getX() + 8, instance.getTelos().getY() + 8) : Utils.colides(spawnTile, instance.getTelos(), 1, instance.getTelos().getSize()))) {
                    return instance.getTelos();
                }
                return null;
            }

        }

        public void updateStream() {
            destroy();
            effectEntity = null;
            for (int i = 0; i < stream.length; i++) {
                int x = (rotation == 0 || rotation == 3) ? startX : rotation == 1 ? (startX - i) : (startX + i);
                int y = rotation == 0 ? (startY - i) : rotation == 1 || rotation == 2 ? startY : (startY + i);
                int plane = instance.getTelos().getPlane();
                WorldObject object = null;
                WorldTile spawnTile = new WorldTile(x, y, plane);
                if (i == 0) {// spawn base object
                    int baseRot = rotation == 0 ? 2 : rotation == 1 ? 3 : rotation == 2 ? 1 : 0;
                    object = new WorldObject(STREAM_BASE_ID, 4, baseRot, spawnTile);
                } else {
                    int pieceRot = rotation == 0 || rotation == 1 ? rotation : rotation == 2 ? 3 : 2;
                    int endRot = rotation == 0 ? 2 : rotation == 1 ? 3 : rotation == 2 ? 1 : 0;
                    if (instance.getTelos().getPhase() == 4 && i >= 14) {
                        if ((i == 14 && instance.getPlayer().matches(getWorldTileByIndex(14))) || (i == 15 && instance.getPlayer().matches(getWorldTileByIndex(15)))) {
                            object = new WorldObject(STREAMS[type][1], 10, endRot, spawnTile);
                            World.spawnObject(object);
                            stream[i] = object;
                            effectEntity = instance.getPlayer();
                            break;
                        } else if ((rotation == 2 && i == 14 && !instance.getPlayer().matches(getWorldTileByIndex(15))) || (rotation != 2 && i == 15)) {
                            object = new WorldObject(STREAMS[type][2], 10, endRot, spawnTile);
                            World.spawnObject(object);
                            stream[i] = object;
                            effectEntity = instance.getTelos();
                            break;
                        }
                    } else {
                        if (instance.getPlayer() != null && !instance.getPlayer().isDead() && (Utils.colides(spawnTile, instance.getPlayer(), 1, 1))) {
                            object = new WorldObject(STREAMS[type][1], 10, endRot, spawnTile);
                            World.spawnObject(object);
                            stream[i] = object;
                            effectEntity = instance.getPlayer();
                            break;
                        }
                        if (!(i == 14 && instance.getPlayer().matches(instance.getTile(3866, 7002, 1))) && instance.getTelos() != null && !instance.getTelos().isDead() && (instance.getTelos().getPhase() == 4 ? spawnTile.withinArea(instance.getTelos().getX() - 1, instance.getTelos().getY() - 1, instance.getTelos().getX() + 8, instance.getTelos().getY() + 8) : Utils.colides(spawnTile, instance.getTelos(), 1, instance.getTelos().getSize()))) {
                            object = new WorldObject(STREAMS[type][2], 10, endRot, spawnTile);
                            World.spawnObject(object);
                            stream[i] = object;
                            effectEntity = instance.getTelos();
                            break;
                        }
                    }
                    object = new WorldObject(STREAMS[type][0], 10, pieceRot, spawnTile);
                }
                World.spawnObject(object);
                stream[i] = object;
            }
            EffectType effectType = type == 0 ? EffectType.GREEN_STREAM : type == 1 ? EffectType.BLACK_STREAM : EffectType.RED_STREAM;
            if (effectEntity != null) {
                effectEntity.getEffectsManager().startEffect(new Effect(effectType, -1));
                if (effectEntity instanceof Player) {
                    if (instance.getTelos() != null)
                        instance.getTelos().getEffectsManager().removeEffect(effectType);
                } else {
                    if (instance.getPlayer() != null)
                        instance.getPlayer().getEffectsManager().removeEffect(effectType);
                }
            }
            if (effectEntity == null) {
                if (instance.getTelos() != null)
                    instance.getTelos().getEffectsManager().removeEffect(effectType);
                if (instance.getPlayer() != null)
                    instance.getPlayer().getEffectsManager().removeEffect(effectType);
            }
        }

        public void checkUpdateStream() {
            if (stream == null)
                return;
            if (getCurrentStreamLength() != inStream() || forceUpdate()) {
                updateStream();
            }
        }

        private boolean forceUpdate() {
            return getEntityByIndex(getCurrentStreamLength()) != effectEntity;
        }

        private int inStream() {
            for (int i = 0; i < stream.length; i++) {
                int x = (rotation == 0 || rotation == 3) ? startX : rotation == 1 ? (startX - i) : (startX + i);
                int y = rotation == 0 ? (startY - i) : rotation == 1 || rotation == 2 ? startY : (startY + i);
                int plane = instance.getTelos().getPlane();
                WorldTile checkTile = new WorldTile(x, y, plane);
                if (instance.getTelos().getPhase() == 4 && i >= 14) {
                    if ((i == 14 && instance.getPlayer().matches(getWorldTileByIndex(14))) || (i == 15 && instance.getPlayer().matches(getWorldTileByIndex(15)))) {
                        return i;
                    } else if ((rotation == 2 && i == 14 && !instance.getPlayer().matches(getWorldTileByIndex(15))) || (rotation != 2 && i == 15)) {
                        return i;
                    }
                } else {
                    if (instance.getPlayer() != null && !instance.getPlayer().isDead() && (Utils.colides(checkTile, instance.getPlayer(), 1, 1))) {
                        return i;
                    }
                    if (instance.getTelos() != null && !instance.getTelos().isDead() && (instance.getTelos().getPhase() == 4 ? checkTile.withinArea(instance.getTelos().getX() - 1, instance.getTelos().getY() - 1, instance.getTelos().getX() + 8, instance.getTelos().getY() + 8) : Utils.colides(checkTile, instance.getTelos(), 1, instance.getTelos().getSize()))) {
                        return i;
                    }
                }
            }
            return stream.length - 1;
        }

        private int getCurrentStreamLength() {
            int index = 0;
            for (int i = 0; i < stream.length; i++) {
                if (stream[i] != null)
                    index = i;
            }
            return index;
        }

        public void destroy() {
            if (stream == null)
                stream = new WorldObject[25];
            for (int i = 0; i < stream.length; i++) {
                if (stream[i] == null || !World.containsObjectWithId(stream[i], stream[i].getId()))
                    continue;
                World.removeObject(stream[i]);
                stream[i] = null;
            }
            EffectType effectType = type == 0 ? EffectType.GREEN_STREAM : type == 1 ? EffectType.BLACK_STREAM : EffectType.RED_STREAM;
            if (instance.getTelos() != null)
                instance.getTelos().getEffectsManager().removeEffect(effectType);
            if (instance.getPlayer() != null)
                instance.getPlayer().getEffectsManager().removeEffect(effectType);
        }

        public Stream(TelosInstance instance) {
            this(instance.getTelos().getPhase(), Utils.random(4), instance);
        }

    }

}
