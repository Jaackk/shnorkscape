package com.rs.game.activites.dnd.eviltree.entity;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeHunterNPC extends NPC {
    private final EvilTree tree;
    private final Set<String> paidForBank = new HashSet<>();

    public EvilTreeHunterNPC(EvilTree tree, WorldTile tile) {
        super(13790, tile, -1, false);
        this.tree = tree;
        setDirection(Utils.getAngle(0, -1));
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (ThreadLocalRandom.current().nextInt(12) == 0) {
            if (tree.isSapling()) {
                setNextAnimation(new Animation(2282)); // Searching the patch.
            } else if (tree.isTree()) {
                int health = tree.getTreeObject().getHealthPercent();
                if (health <= 60) {
                    setNextAnimation(new Animation(862)); // Cheering!
                } else if (health <= 100) {
                    setNextAnimation(new Animation(2836)); // Scared...
                }
            }
        }
    }

    public EvilTree getTree() {
        return tree;
    }

    public Set<String> getPaidForBank() {
        return paidForBank;
    }
}