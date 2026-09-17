package com.rs.game.activites.quest.root_of_evil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rs.game.Graphics;
import com.rs.game.MapInstance;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.entity.EvilWeedsObject;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class EndQuestCutscene extends Cutscene {

    private static final ImmutableList<Integer> AREA = ImmutableList.of(2383, 9811, 2396, 9829);

    private final VaultController vault;
    private final MapInstance instance;
    private WorldTask fireTask;

    public EndQuestCutscene(VaultController vault) {
        this.vault = vault;
        instance = vault.instance;
    }

    @Override
    public CutsceneAction[] getActions(Player player) {
        val actions = new ArrayList<CutsceneAction>();
        actions.add(new CutsceneAction(-1, 5) {
            @Override
            public void process(Player player, Object[] cache) {
                fireTask = new WorldTask() {
                    @Override
                    public void run() {
                        if (instance.getStage() == MapInstance.Stages.DESTROYING) {
                            stop();
                            return;
                        }
                        doFire();
                    }
                };
                WorldTasksManager.schedule(fireTask, 0, 1);
                player.getPackets().sendCameraShake(3, 25, 50, 25, 50);
            }
        });
        actions.add(new CutsceneAction(-1, 5) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getPackets().sendCameraShake(3, 25, 100, 50, 100);
            }
        });
        actions.add(new CutsceneAction(-1, 5) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getPackets().sendCameraLook(instance.getCutsceneX(player, 2390), instance.getCutsceneY(player, 9821), 8000, 3, 0);
            }
        });
        actions.add(new CutsceneAction(-1, -1) {
            @Override
            public void process(Player player, Object[] cache) {
                vault.finished = true;
                player.getControlerManager().forceStop();
                fireTask.stop();
                FadingScreen.fade(player, 600, () -> {
                    player.lock();
                    player.quests.advanceStage(RootOfEvil.class);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.unlock();
                        }
                    }, 2);
                });
            }
        });
        return Iterables.toArray(actions, CutsceneAction.class);
    }

    private void doFire() {
        Graphics fireGraphics = new Graphics(453);
        for (int loop = 0; loop < 5; loop++) {
            val nextPos = getRandomPos();
            World.sendGraphics(null, fireGraphics, nextPos);
        }
    }

    private WorldTile getRandomPos() {
        int swX = AREA.get(0);
        int swY = AREA.get(1);
        int neX = AREA.get(2);
        int neY = AREA.get(3);
        int randomX = ThreadLocalRandom.current().nextInt((neX - swX) + 1) + swX;
        int randomY = ThreadLocalRandom.current().nextInt((neY - swY) + 1) + swY;
        return new WorldTile(randomX, randomY, 0);
    }

    @Override
    public boolean hiddenMinimap() {
        return false;
    }
}
