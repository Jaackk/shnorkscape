package com.rs.game.activites.quest.root_of_evil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.MapInstance;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.StillNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.NoContinueNpcDialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import lombok.val;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MisterWigglesCutscene extends Cutscene {

    private static final ImmutableList<WorldTile> FIRES = ImmutableList.of(
            new WorldTile(2522, 3238, 0),
            new WorldTile(2523, 3235, 0),
            new WorldTile(2518, 3231, 0),
            new WorldTile(2512, 3233, 0),
            new WorldTile(2511, 3236, 0),
            new WorldTile(2515, 3239, 0),
            new WorldTile(2518, 3240, 0)
    );
    private static final ImmutableList<WorldTile> INJURED_KNIGHTS = ImmutableList.of(
            new WorldTile(2510, 3240, 0),
            new WorldTile(2514, 3240, 0),
            new WorldTile(2517, 3240, 0),
            new WorldTile(2521, 3240, 0),
            new WorldTile(2523, 3238, 0),
            new WorldTile(2524, 3236, 0),
            new WorldTile(2517, 3232, 0),
            new WorldTile(2510, 3234, 0),
            new WorldTile(2511, 3235, 0),
            new WorldTile(2505, 3231, 0),
            new WorldTile(2511, 3230, 0),
            new WorldTile(2514, 3231, 0),
            new WorldTile(2518, 3232, 0),
            new WorldTile(2516, 3243, 0),
            new WorldTile(2526, 3240, 0),
            new WorldTile(2529, 3238, 0),
            new WorldTile(2532, 3238, 0),
            new WorldTile(2529, 3244, 0),
            new WorldTile(2531, 3247, 0)
    );
    private static final ImmutableList<String> KNIGHT_DEATH = ImmutableList.of("AHH!", "Gah!", "Ugh!");
    private static final ImmutableList<String> KNIGHT_HAPPY = ImmutableList.of("WE LOVE YOU BRYAN!",
            "Yay!", "Woo-hoo!", "We did it!!!", "How the f... ?!?", "I can't believe it...", "Go Bryan!",
            "Thank you Bryan!", "Our hero Bryan!");
    private static final ImmutableList<Integer> OFFSETS = ImmutableList.of(0, -1, 1);
    private boolean startFires;
    private int lastFire;
    private WorldTask fireTask;
    private WorldTask spellTask;
    private final MapInstance instance;

    List<NPC> mages = new LinkedList<>();
    List<NPC> knights = new LinkedList<>();
    List<NPC> hurtKnights = new LinkedList<>();
    volatile NPC bryanTheGreat;
    volatile NPC headMage;

    public MisterWigglesCutscene(MapInstance instance) {
        this.instance = instance;
    }

    @Override
    public void start(Player player) {

    }

    @Override
    public CutsceneAction[] getActions(Player player) {
        val actions = new ArrayList<CutsceneAction>();
        actions.add(new CutsceneAction(-1,6) {
            @Override
            public void process(Player player, Object[] cache) {
                setup();
                player.getPackets().sendCameraPos(instance.getCutsceneX(player, 2518), instance.getCutsceneY(player, 3230), 7000, 5, 0);
                player.getPackets().sendCameraLook(instance.getCutsceneX(player, 2516), instance.getCutsceneY(player, 3233), 3000, 5, 0);
            }
        });
        actions.add(new CutsceneAction(-1, 9) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getDialogueManager().startDialogue(new NoContinueNpcDialogue(2371,
                        "Just when we thought we were winning the war, a couple of",
                        "powerful mages completely turned the tide. The Kingdom was almost lost!"));
            }
        });
        actions.add(new CutsceneAction(-1, 1) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getInterfaceManager().closeChatBoxInterface();
                headMage.setNextForceTalk(new ForceTalk("Behold our evil power!"));
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC mage : mages) {
                    mage.setNextAnimation(new Animation(1979));
                    mage.setNextGraphics(new Graphics(482));
                    startFires = true;
                }
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC knight : knights) {
                    knight.applyHit(new Hit(knight.getHitpoints() - 1, Hit.HitLook.MAGIC_DAMAGE));
                    knight.setNextAnimation(new Animation(836));
                    knight.setNextGraphics(new Graphics(453));
                    knight.setNextForceTalk(new ForceTalk(Utils.randomFrom(KNIGHT_DEATH)));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC knight : knights) {
                    knight.setNextGraphics(new Graphics(-1));
                    knight.setNextWorldTile(instance.getInstanceTile(100, 100, 4));
                    World.updateEntityRegion(knight);
                }
                clearAndRemove(knights);
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC mage : mages) {
                    mage.setNextAnimation(new Animation(862));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC mage : mages) {
                    mage.setNextAnimation(new Animation(862));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 0) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getDialogueManager().startDialogue(new NoContinueNpcDialogue(2371,
                        "But Bryan the Great, the kingdom's most powerful wizard found a way to stop them!",
                        "He knew their souls were exceptionally weak to his style of magic..."));
            }
        });
        actions.add(new CutsceneAction(-1, 0) {
            @Override
            public void process(Player player, Object[] cache) {
                bryanTheGreat = new StillNPC(6375, instance.getInstanceTile(2517, 3250, 0));
                bryanTheGreat.setDirection(Utils.getAngle(0, 1));
            }
        });
        actions.add(new CutsceneAction(-1, 1) {
            @Override
            public void process(Player player, Object[] cache) {
                bryanTheGreat.setNextWorldTile(instance.getInstanceTile(2517, 3234, 0));
                bryanTheGreat.setNextAnimation(new Animation(8941));
                bryanTheGreat.setNextGraphics(new Graphics(1577));
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getInterfaceManager().closeChatBoxInterface();
                bryanTheGreat.setNextAnimation(new Animation(-1));
                headMage.setNextForceTalk(new ForceTalk("Who are you?!? Why can't we move?"));
            }
        });
        actions.add(new CutsceneAction(-1, 6) {
            @Override
            public void process(Player player, Object[] cache) {
                bryanTheGreat.setNextForceTalk(new ForceTalk("Die!"));
                spellTask = new WorldTask() {
                    int loop = 0;

                    @Override
                    public void run() {
                        if (loop == 3) {
                            stop();
                            return;
                        }
                        bryanTheGreat.setNextAnimation(new Animation(713));
                        bryanTheGreat.setNextGraphics(new Graphics(1618));
                        for (NPC npc : mages) {
                            npc.setNextGraphics(new Graphics(1638));
                            npc.setNextAnimation(new Animation(3926));
                        }
                        loop++;
                    }
                };
                WorldTasksManager.schedule(spellTask, 2, 2);
            }
        });
        actions.add(new CutsceneAction(-1, 5) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : mages) {
                    npc.setNextGraphics(new Graphics(1638));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 1) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : mages) {
                    npc.setNextForceTalk(new ForceTalk("Curse you!"));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 1) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : mages) {
                    npc.setNextAnimation(new Animation(836));
                    npc.setNextWorldTile(instance.getInstanceTile(100, 100, 4));
                    World.updateEntityRegion(npc);
                }
                clearAndRemove(mages);
                startFires = false;
                player.getPackets().sendCameraPos(instance.getCutsceneX(player, 2516), instance.getCutsceneY(player, 3231), 9000, 12, 0);
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : hurtKnights) {
                    npc.setNextForceTalk(new ForceTalk(Utils.randomFrom(KNIGHT_HAPPY)));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : hurtKnights) {
                    npc.setNextForceTalk(new ForceTalk(Utils.randomFrom(KNIGHT_HAPPY)));
                }
            }
        });
        actions.add(new CutsceneAction(-1, 2) {
            @Override
            public void process(Player player, Object[] cache) {
                for (NPC npc : hurtKnights) {
                    npc.setNextForceTalk(new ForceTalk(Utils.randomFrom(KNIGHT_HAPPY)));
                }
                player.getInterfaceManager().closeChatBoxInterface();
                FadingScreen.fade(player, 1500, () -> {
                    player.unlock();
                    if (player.getInventory().containsItem(9919, 1)) {
                        player.getAppearence().switchHidden();
                        player.setNextWorldTile(new WorldTile(3203, 3435, 1));
                        player.getInventory().deleteItem(9919, 1);
                        player.quests.advanceStage(RootOfEvil.class);
                        player.getDialogueManager().startDialogue(new MisterWigglesD());
                    } else {
                        player.getDialogueManager().startDialogue("SingleNPCDialogue", 2371, Dialogue.NORMAL,
                                new String[]{"What did you do with the root?! It's incredibly dangerous!"});
                    }
                });
            }
        });
        return Iterables.toArray(actions, CutsceneAction.class);
    }

    @Override
    public boolean hiddenMinimap() {
        return true;
    }

    @Override
    public void stopCutscene(Player player) {
        clear();
        super.stopCutscene(player);
    }

    private void setup() {
        // Spawn main row of mages and knights.
        val base = new WorldTile(2517, 3240, 0);
        for (int loop = -2; loop <= 2; loop++) {

            val mageNpc = new StillNPC(3245, instance.getInstanceTile(base.transform(loop, -4, 0)));
            if (loop == 0) {
                headMage = mageNpc;
            }
            val knightNpc = new StillNPC(1092, instance.getInstanceTile(base.transform(loop, -5, 0)));
            mageNpc.setDirection(Utils.getAngle(0, -1));
            knightNpc.setDirection(Utils.getAngle(0, 1));
            mages.add(mageNpc);
            knights.add(knightNpc);
        }

        // Spawn injured knights.
        for (val wt : INJURED_KNIGHTS) {
            val hurtKnightNpc = new StillNPC(15466 + (ThreadLocalRandom.current().nextBoolean() ? 1 : 0), instance.getInstanceTile(wt));
            int offsetX = ThreadLocalRandom.current().nextBoolean() ? 0 : 1;
            int offsetY = ThreadLocalRandom.current().nextBoolean() ? 0 : 1;
            if (offsetX != 0 && offsetY != 0) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    offsetX = 0;
                } else {
                    offsetY = 0;
                }
            }
            if (offsetX != 0 && ThreadLocalRandom.current().nextBoolean()) {
                offsetX = -offsetX;
            }
            if (offsetY != 0 && ThreadLocalRandom.current().nextBoolean()) {
                offsetY = -offsetY;
            }
            hurtKnightNpc.setDirection(Utils.getAngle(offsetX, offsetY));
            hurtKnights.add(hurtKnightNpc);
        }

        // Start task for fires.
        fireTask = new WorldTask() {
            @Override
            public void run() {
                if (startFires) {
                    for (WorldTile tile : FIRES) {
                        val newTile = tile.transform(Utils.randomFrom(OFFSETS), Utils.randomFrom(OFFSETS), 0);
                        World.sendGraphics(null, new Graphics(453), instance.getInstanceTile(newTile));
                    }
                }
            }
        };
        WorldTasksManager.schedule(fireTask, 5, 5);
    }

    void clearAndRemove(List<NPC> npcs) {
        Iterator<NPC> it = npcs.iterator();
        while (it.hasNext()) {
            World.removeNPC(it.next());
            it.remove();
        }
    }

    void clear() {
        clearAndRemove(mages);
        clearAndRemove(knights);
        clearAndRemove(hurtKnights);
        if (bryanTheGreat != null) {
            World.removeNPC(bryanTheGreat);
            bryanTheGreat = null;
        }
        if (fireTask != null) {
            fireTask.stop();
        }
        if (spellTask != null) {
            spellTask.stop();
        }
        headMage = null;
    }
}
