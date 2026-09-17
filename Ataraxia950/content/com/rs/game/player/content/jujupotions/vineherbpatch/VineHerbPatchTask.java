package com.rs.game.player.content.jujupotions.vineherbpatch;

import com.rs.game.tasks.WorldTask;
import lombok.RequiredArgsConstructor;

/**
 * @author lare96 <http://github.com/lare96>
 */
@RequiredArgsConstructor
public final class VineHerbPatchTask extends WorldTask {

    private final VineHerbPatchManager patchManager;
    private int noChecks;

    @Override
    public void run() {
        for (VineHerbPatch patch : patchManager.getPatches().values()) {
            if (patch.isGrowing()) {
                patch.checkGrowth();
            } else if (patch.shouldWeedsGrow()) {
                patch.setState(VineHerbPatchState.WEEDED);
            } else {
                noChecks++;
            }
        }
        if (noChecks >= 2) {
            stop();
        }
    }
}
