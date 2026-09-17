package com.rs.game.activites.dnd.eviltree;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeInstance extends BossInstance {

    private EvilTree tree;
    private EvilTreeType type;

    public EvilTreeInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
    }

    @Override
    public String getInstanceName() {
        if (type == null) {
            return super.getInstanceName();
        }
        return "Evil Tree ~ " + type.formattedName;
    }

    @Override
    public int[] getMapPos() {
        return new int[]{323, 595};
    }

    @Override
    public int[] getMapSize() {
        return new int[]{1, 1};
    }

    @Override
    public void loadMapInstance() {
        EvilTreeInstanceLevel instanceLvl = getOwner().etInstanceLvl;
        if (instanceLvl == null) {
            throw new IllegalStateException("Must set Player#etInstanceLevel first!");
        }
        World.spawnObject(new WorldObject(0, 10, 0, getTile(2608, 4777, 0)));
        World.spawnObject(new WorldObject(0, 10, 0, getTile(2604, 4782, 0)));
        type = Utils.randomFrom(instanceLvl.trees);
        tree = new EvilTree(getTile(2603, 4774, 0), type);
        tree.spawnInstancedTree(getTile(2604, 4782, 0));
        getOwner().etInstanceLvl = null;
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        player.setLargeSceneView(true);
        super.enterInstance(player, login);
    }

    @Override
    public void leaveInstance(Player player, int type) {
        player.setLargeSceneView(false);
        super.leaveInstance(player, type);
    }

    @Override
    public void onFinish() {
        if (tree.isAlive()) {
            tree.getTreeObject().killAndClear();
        }
    }

    public void destroy(String msg) {
        if (tree.getTreeObject() != null) {
            tree.getTreeObject().killAndClear();
        }
        if (getPlayers() != null) {
            for (Player player : getPlayers()) {
                if (player == null) {
                    continue;
                }
                player.getInterfaceManager().closeOverlay(false);
                leaveInstance(player, BossInstance.EXITED);
                player.getControlerManager().forceStop();
                player.sendMessage(Colors.CYAN + "[Instance]: " + msg);
            }
        }
        forceFinish();
    }

    public void deleteStump() {
        World.spawnObject(new WorldObject(0, 10, 0, getTile(2601, 4774, 0)));
    }

    public EvilTreeType getType() {
        return type;
    }

    public EvilTree getTree() {
        return tree;
    }

    public void setType(EvilTreeType type) {
        this.type = type;
    }
}