package com.rs.game.player.content.jujupotions.vineherbpatch;

import com.google.common.collect.ImmutableList;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Pots;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Setter;
import lombok.val;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.rs.game.player.FarmingManager.HERB_PICKING_ANIMATION;
import static com.rs.game.player.FarmingManager.RAKING_ANIMATION;
import static com.rs.game.player.FarmingManager.SEED_DIPPING_ANIMATION;
import static com.rs.game.player.FarmingManager.SPADE_ANIMATION;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class VineHerbPatchManager implements Serializable {

    private static final long serialVersionUID = -1516601904958936491L;

    static final int GROWTH_MINS = 20;
    private static final ImmutableList<WorldTile> ALL_PATCHES = ImmutableList.of(
            new WorldTile(2956, 2906, 0),
            new WorldTile(2957, 2911, 0),
            new WorldTile(2946, 2912, 0),
            new WorldTile(2947, 2905, 0)
    );

    private final Map<WorldTile, VineHerbPatch> patches = new HashMap<>(ALL_PATCHES.size());

    @Setter
    private transient Player player;

    private transient WorldTask checkPatchesTask;

    public boolean plant(int seedId, WorldObject patchObj) {
        val patch = patches.get(patchObj);
        if (patch == null) {
            return false;
        }
        val seed = VineHerbSeed.ALL.get(seedId);
        if (seed == null) {
            player.sendMessage("You can only plant vine herb seeds here.");
            return false;
        }
        if (player.getSkills().getLevel(Skills.FARMING) < seed.getLevel()) {
            player.sendMessage("You need a Farming level of " + seed.getLevel() + " to plant this seed.");
            return false;
        }
        if (!player.getInventory().containsOneItem(5343)) {
            player.sendMessage("You need a seed dibber to do this.");
            return false;
        }
        if (!player.getInventory().containsOneItem(5325)) {
            player.sendMessage("You need a gardening trowel to do this.");
            return false;
        }
        player.sendMessage("You plant the " + seed.getName() + " in the vine herb patch.");
        player.setNextAnimation(SEED_DIPPING_ANIMATION);
        player.getSkills().addXp(Skills.FARMING, seed.getPlantXp());
        player.getInventory().deleteItem(new Item(seedId, 1));
        patch.setSeed(seed);
        patch.setState(VineHerbPatchState.SEEDED);
        return true;
    }

    public boolean handleOption1(WorldObject patchObj) {
        val patch = patches.get(patchObj);
        if (patch == null) {
            return false;
        }
        switch (patch.getState()) {
            case GROWN:
                return pick(patch);
            case WEEDED:
                return rake(patch);
        }
        return false;
    }

    public boolean handleOption2(WorldObject patchObj) {
        val patch = patches.get(patchObj);
        if (patch == null) {
            return false;
        }
        switch (patch.getState()) {
            case GROWN:
            case WEEDED:
            case CLEAR:
            case SEEDED:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
                return inspect(patch);
        }
        return false;
    }

    public boolean handleOption3(WorldObject patchObj) {
        val patch = patches.get(patchObj);
        if (patch == null) {
            return false;
        }

        switch (patch.getState()) {
            case GROWN:
                return clear(patch);
        }
        return false;
    }

    public boolean handleOption4(WorldObject patchObj) {
        val patch = patches.get(patchObj);
        if (patch == null) {
            return false;
        }

        switch (patch.getState()) {
            case GROWN:
            case CLEAR:
            case WEEDED:
            case SEEDED:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
                return guide(patch);
        }
        return false;
    }

    private boolean pick(VineHerbPatch patch) {
        if (patch.getState() == VineHerbPatchState.GROWN) {
            player.getActionManager().setAction(new Action() {

                @Override
                public boolean start(Player player) {
                    return true;
                }

                @Override
                public boolean process(Player player) {
                    if (patch.herbAmount > 0) {
                        return true;
                    } else {
                        player.sendMessage("You pick all of the herbs from the patch; produce harvested: " + Colors.RED + Utils.getFormattedNumber(player.produceGathered) + "</col>.", true);
                        player.setNextAnimation(new Animation(-1));
                        patch.setState(VineHerbPatchState.CLEAR);
                        return false;
                    }
                }

                @Override
                public int processWithDelay(Player player) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("You don't have enough space in your inventory.", true);
                        return -1;
                    }
                    player.sendMessage("You pick some herbs.");
                    player.setNextAnimation(HERB_PICKING_ANIMATION);
                    player.addProduceGathered();
                    player.getSkills().addXp(Skills.FARMING, patch.getSeed().getHarvestXp());
                    boolean jujuActive = player.jujuPotions.isActive(Pots.Effects.FARMING_JUJU) &&
                            ThreadLocalRandom.current().nextInt(3) == 0;
                    int amount = 1;
                    if (jujuActive) {
                        amount++;
                        player.sendMessage("Through the power of juju, you an receive an extra herb!");
                    }
                    player.getInventory().addItem(patch.getSeed().getGrimyHerbId(), amount);
                    patch.herbAmount--;
                    return 2;
                }

                @Override
                public void stop(Player player) {
                    setActionDelay(player, 1);
                }
            });
            return true;
        }
        return false;
    }

    private boolean rake(VineHerbPatch patch) {
        if (patch.getState() == VineHerbPatchState.WEEDED) {
            player.getActionManager().setAction(new Action() {
                private int loops = 3;

                @Override
                public boolean start(Player player) {
                    if (!player.getInventory().containsOneItem(5341)) {
                        player.sendMessage("You need a rake to do this.");
                        return false;
                    }
                    return true;
                }

                @Override
                public boolean process(Player player) {
                    return loops > 0;
                }

                @Override
                public int processWithDelay(Player player) {
                    player.setNextAnimation(RAKING_ANIMATION);
                    if (--loops == 0) {
                        patch.setState(VineHerbPatchState.CLEAR);
                        return -1;
                    }
                    return 1;
                }

                @Override
                public void stop(Player player) {
                    setActionDelay(player, 1);
                }
            });
            return true;
        }
        return false;
    }

    private void sendGrowingProgress(VineHerbPatch patch, int stage) {
        Duration diff = Duration.between(LocalDateTime.now(), patch.getGrowthDate());
        long minutesPart = diff.toMinutes();
        long secondsPart = diff.minusMinutes(minutesPart).getSeconds();
        Dialogue.sendSingleDialogue(player, Colors.DARK_RED+patch.getSeed().getName()+"</col>; Growing stage ~ " + Colors.DARK_RED + stage + "/4</col>; Time until next stage ~ " + Colors.DARK_RED + minutesPart + "m " + secondsPart + "s" + "</col>.");
    }

    public boolean inspect(VineHerbPatch patch) {
        switch (patch.getState()) {
            case WEEDED:
                Dialogue.sendSingleDialogue(player, "This patch needs to be raked before a vine herb seed can be planted.");
                break;
            case CLEAR:
                Dialogue.sendSingleDialogue(player, "A vine herb seed can be planted here.");
                break;
            case SEEDED:
                sendGrowingProgress(patch, 1);
                break;
            case GROWING_1:
                sendGrowingProgress(patch, 2);
                break;
            case GROWING_2:
                sendGrowingProgress(patch, 3);
                break;
            case GROWING_3:
                sendGrowingProgress(patch, 4);
                break;
            case GROWN:
                Dialogue.sendSingleDialogue(player, "This patch is ready for harvest!");
                break;
        }
        return true;
    }

    public boolean guide(VineHerbPatch patch) {
        switch (patch.getState()) {
            case WEEDED:
            case CLEAR:
            case SEEDED:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
            case GROWN:
                Dialogue.sendSingleDialogue(player, "Google 'RS Wiki Farming' if you need help with training Farming.");
                break;
        }
        return true;
    }

    public boolean clear(VineHerbPatch patch) {
        player.getActionManager().setAction(new Action() {
            private int loops = 2;

            @Override
            public boolean start(Player player) {
                if (!player.getInventory().containsOneItem(952) && !player.getToolBelt().contains(952)) {
                    player.sendMessage("You need a spade to do this.");
                    return false;
                }
                player.sendMessage("You start clearing the patch.");
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (loops <= 0) {
                    player.sendMessage("You finish clearing the patch.");
                    player.setNextAnimation(new Animation(-1));
                    patch.setState(VineHerbPatchState.CLEAR);
                    return false;
                }
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(SPADE_ANIMATION);
                if (Utils.random(3) == 0)
                    loops--;
                return 2;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 3);
            }
        });
        return true;
    }

    public void init() {
        for (WorldTile tile : ALL_PATCHES) {
            VineHerbPatch found = patches.get(tile);
            if (found == null) { // Register all patch locations.
                VineHerbPatch newPatch = new VineHerbPatch(tile);
                patches.put(tile, newPatch);
                found = newPatch;
            }
            found.setPlayer(player);
            CoresManager.getServiceProvider().addGameTask(found::spawn);
        }
        startCheckPatchesTask();
    }

    void startCheckPatchesTask() {
        if (checkPatchesTask != null && !checkPatchesTask.isCancelled()) {
            // Task already running.
            return;
        }
        checkPatchesTask = new VineHerbPatchTask(this);
        WorldTasksManager.schedule(checkPatchesTask, 1, 100);
    }

    Map<WorldTile, VineHerbPatch> getPatches() {
        return patches;
    }
}
