package com.rs.game.activites.dnd.eviltree;

import com.rs.game.activites.dnd.eviltree.entity.EvilSaplingObject;
import com.rs.game.tasks.WorldTask;

/**
 * A task that handles the growth of an {@link EvilSaplingObject}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeGrowthTask extends WorldTask {

    private final EvilTree tree;

    EvilTreeGrowthTask(EvilTree tree) {
        this.tree = tree;
    }

    @Override
    public void run() {
        if (tree.getSaplingObject() == null || !tree.getSaplingObject().grow()) {
            stop();
        }
    }

    @Override
    public void onStop() {
        tree.setGrowthTask(null);
    }
}