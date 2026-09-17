package com.rs.game.player.content.jujupotions.vineherbpatch;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
@RequiredArgsConstructor
public final class VineHerbPatch implements Serializable {

    private static final long serialVersionUID = 8423406331183800650L;

    @Getter
    private final WorldTile tile;

    @Getter
    private VineHerbPatchState state = VineHerbPatchState.WEEDED;

    int herbAmount;

    @Getter
    @Setter
    private transient Player player;

    @Getter
    @Setter
    private LocalDateTime growthDate;

    @Getter
    @Setter
    private VineHerbSeed seed;

    private LocalDateTime growWeedsDate;

    public void checkGrowth() {
        if (state == VineHerbPatchState.CLEAR ||
                state == VineHerbPatchState.WEEDED ||
                state == VineHerbPatchState.GROWN) {
            return;
        }
        if (growthDate == null) {
            resetGrowthDate();
            throw new IllegalStateException("Growth date invalid! Starting new one...");
        }
        if (LocalDateTime.now().isAfter(growthDate)) {
            switch (state) {
                case SEEDED:
                    setState(VineHerbPatchState.GROWING_1);
                    break;
                case GROWING_1:
                    setState(VineHerbPatchState.GROWING_2);
                    break;
                case GROWING_2:
                    setState(VineHerbPatchState.GROWING_3);
                    break;
                case GROWING_3:
                    setState(VineHerbPatchState.GROWN);
                    break;
            }
        }
    }

    public void resetGrowthDate() {
        growthDate = LocalDateTime.now().plusMinutes(VineHerbPatchManager.GROWTH_MINS);
    }

    public boolean shouldWeedsGrow() {
        return state == VineHerbPatchState.CLEAR && growWeedsDate != null && LocalDateTime.now().isAfter(growWeedsDate);
    }

    public boolean isGrowing() {
        switch (state) {
            case SEEDED:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
                return true;
        }
        return false;
    }

    public void spawn() {
        player.getPackets().addSpawnedObject(new WorldObject(state.getObjectId(), 10, 1, tile));
    }

    public void setState(VineHerbPatchState newState) {
        switch (newState) {
            case WEEDED:
                growthDate = null;
                growWeedsDate = null;
                herbAmount = 0;
                seed = null;
                break;
            case CLEAR:
                player.vineHerbPatches.startCheckPatchesTask();
                growthDate = null;
                seed = null;
                growWeedsDate = LocalDateTime.now().plusMinutes(10);
                herbAmount = 0;
                break;
            case SEEDED:
                if (seed == null)
                    throw new IllegalStateException("Seed must be set before setting state to SEEDED.");
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
                player.vineHerbPatches.startCheckPatchesTask();
                resetGrowthDate();
                growWeedsDate = null;
                herbAmount = 0;
                break;
            case GROWN:
                growthDate = null;
                growWeedsDate = null;
                herbAmount = ThreadLocalRandom.current().nextInt(seed.getYieldMin(), seed.getYieldMax());
                break;
        }
        state = newState;
        spawn();
    }
}
