package com.rs.game.npc.others;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.npc.NPC;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles the Star Sprite NPC.
 *
 * @author Noel
 */
public class StarSprite extends NPC {

    private static final long serialVersionUID = -8364410811522482722L;
    public WorldObject star;
    private int cycle;
    private boolean firstClick;
    private int starLife;
    private int visibleCycle;

    public StarSprite() {
        super(ShootingStar.SHADOW, ShootingStar.getNewLocation(), -1, true, true);
        this.setDirection(Utils.getAngle(-1, 0));
    }

    @Override
    public void processNPC() {
        if (cycle == 300) {
            setStar(38659);
            setNextNPCTransformation(ShootingStar.INVISIBLE);
        } else if (cycle == 322) {
            setStar(112899 + Utils.random(9));
            sendStarNews();
            ShootingStar.generateNextLocation();
            World.activeStar = true;
        } else if (cycle == ShootingStar.STAR_FALL_TIME) { // 4 hours
            cycle = 0;
            visibleCycle = 0;
            setNextWorldTile(ShootingStar.getNewLocation());
            setNextNPCTransformation(ShootingStar.SHADOW);
            removeStar();
            firstClick = false;
        } else if (getId() == ShootingStar.SPRITE) {
            visibleCycle++;
            if (visibleCycle == 1000) { // 10min max to talk with it
                setNextNPCTransformation(ShootingStar.INVISIBLE);
                World.activeStar = false;
            }
        }
        cycle++;
    }

    public boolean isReady() {
        return cycle > 22 && getId() == ShootingStar.INVISIBLE;
    }

    private void sendStarNews() {

        //World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=7>Server: " + "A Shooting star [size " + getStarSize() + "] has just landed in " + ShootingStar.getLocationName() + ".", false);
    }

    public int getStarSize() {
        return star == null ? 0 : (112908 - star.getId());
    }

    public int getMiningLevel() {
        return getStarSize() * 10;
    }

    public void openStar() {
        setNextNPCTransformation(ShootingStar.SPRITE);
        removeStar();
    }

    public boolean isFirstClick() {
        return firstClick;
    }

    public void setFirstClick() {
        firstClick = true;
    }

    public void setStar(int id) {
        star = new WorldObject(id, 10, 0, getX(), getY(), getPlane());
        World.spawnObject(star);
        if (star.getId() != 38659) {
            starLife = ShootingStar.STAR_DUST_Q[getStarSize() - 1]; // 1000 dust per layer atm
        }
    }

    public int getMinedPerc() {
        return 100 - (100 * starLife / ShootingStar.STAR_DUST_Q[getStarSize() - 1]);
    }

    public void reduceStarLife() {
        starLife--;
        if (starLife <= 0) {
            if (star.getId() == 112907) {
                openStar();
            } else {
                setStar(star.getId() + 1);
            }
        }
    }

    public int getStarCycle() {
        return cycle;
    }

    public void removeStar() {
        if (star == null) {
            return;
        }
        World.removeObject(star);
        star = null;
    }
}