package com.rs.game.activites.quest.root_of_evil;

import com.google.common.collect.Iterables;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.MapInstance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.SinglePlayerDialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.val;

import java.util.ArrayList;

public final class EvilMageIntroductionCutscene extends Cutscene {

    private final MapInstance instance;
    private final VaultController vault;
    private final NPC evilMage;

    public EvilMageIntroductionCutscene(MapInstance instance,VaultController vault, NPC evilMage) {
        this.instance = instance;
        this.vault = vault;
        this.evilMage = evilMage;
    }

    @Override
    public CutsceneAction[] getActions(Player player) {
        val actions = new ArrayList<CutsceneAction>();
        actions.add(new CutsceneAction(-1, 3) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getPackets().sendCameraLook(instance.getCutsceneX(player, 2390), instance.getCutsceneY(player, 9821), 5000, 4, 0);
            }
        });
        actions.add(new CutsceneAction(-1, 7) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getPackets().sendCameraPos(instance.getCutsceneX(player, 2390), instance.getCutsceneY(player, 9810), 5000, 4, 0);
            }
        });
        actions.add(new CutsceneAction(-1, 3) {
            @Override
            public void process(Player player, Object[] cache) {
                evilMage.setNextForceTalk(new ForceTalk("Blasted machine! I should have a book that can help fix this..."));
                evilMage.setNextAnimation(new Animation(859));
            }
        });
        actions.add(new CutsceneAction(-1, 4) {
            @Override
            public void process(Player player, Object[] cache) {
                evilMage.setCannotMove(false);

                val walkTile = evilMage.transform(5, 0, 0);
                evilMage.addWalkSteps(walkTile.getX(), walkTile.getY());
            }
        });
        actions.add(new CutsceneAction(-1, -1) {
            @Override
            public void process(Player player, Object[] cache) {
                player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.SCARED, "I think someone's coming... I need to hide!"));
                player.getAppearence().setRenderEmote(295);
                confrontPlayer(player);
            }
        });
        return Iterables.toArray(actions, CutsceneAction.class);
    }

    @Override
    public boolean hiddenMinimap() {
        return false;
    }

    private void confrontPlayer(Player player) {
        WorldTasksManager.schedule(new WorldTask() {
            int stage = 0;

            @Override
            public void run() {
                boolean inCave = player.getControlerManager().getControler() instanceof VaultController;
                if(!inCave) {
                    stop();
                    return;
                }

                if (stage > 7) {
                    val mageLineOfSight = instance.getInstanceTile(2410, 9819, 0);
                    if (vault.vaultStage == 1 && player.getX() == mageLineOfSight.getX()) {
                        catchPlayer(player);
                        stop();
                        return;
                    }
                }
                if (stage == 0) {
                    evilMage.setNextFaceWorldTile(evilMage.transform(0, -1, 0));
                    evilMage.setNextWorldTile(instance.getInstanceTile(2408, 9828, 0));
                } else if (stage == 1) {
                    val walkTile = evilMage.transform(0, -6, 0);
                    evilMage.addWalkSteps(walkTile.getX(), walkTile.getY());
                } else if (stage == 2) {
                    if (!player.getAppearence().isHidden()) {
                        stop();
                        catchPlayer(player);
                    } else {
                        evilMage.setNextForceTalk(new ForceTalk("Hmm, that book should be around here somewhere..."));
                    }
                } else if (stage == 3) {
                    val walkTile = evilMage.transform(2, 0, 0);
                    evilMage.addWalkSteps(walkTile.getX(), walkTile.getY());
                } else if (stage == 5) {
                    val walkTile = evilMage.transform(0, -4, 0);
                    evilMage.addWalkSteps(walkTile.getX(), walkTile.getY());
                } else if (stage == 6) {
                    evilMage.setNextFaceWorldTile(evilMage.transform(1, -2, 0));
                } else if (stage == 7) {
                    evilMage.resetWalkSteps();
                    evilMage.setCannotMove(true);
                    player.setNextWorldTile(instance.getInstanceTile(2409, 9820, 0));
                    player.getAppearence().switchHidden();
                    vault.vaultStage = 1;
                    player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.NORMAL, "Now seems like a good time to sneak further inside..."));
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Grr... no, that's not it..."));
                } else if(stage == 8) {
                    player.unlock();
                } else if (stage == 9) {
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Not that one either..."));
                } else if (stage == 11) {
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Nope..."));
                } else if (stage == 13) {
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Hmmm..."));
                } else if (stage == 15) {
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Maybe here..."));
                } else if (stage == 17) {
                    evilMage.setNextAnimation(new Animation(857));
                    evilMage.setNextForceTalk(new ForceTalk("Maybe I'll check the table next..."));
                } else if (stage == 19) {
                    evilMage.setNextFaceWorldTile(evilMage.transform(-2, 0, 0));
                }else if(stage == 20) {
                    val baseTile = instance.getInstanceTile(2404, 9825, 0);
                    if(player.getY() <= baseTile.getY() && player.getX() >= baseTile.getX()) {
                        catchPlayer(player);
                    } else {
                        evilMage.setNextForceTalk(new ForceTalk("Ah... here it is."));
                        evilMage.setNextRenderAnimation(2340);
                    }
                    stop();
                }
                stage++;
            }
        }, 13, 2);
    }

   void catchPlayer(Player player) {
        stopWithoutReplacement(player);
        player.resetWalkSteps();
        player.lock();
        evilMage.resetWalkSteps();
        evilMage.setNextFaceEntity(player);
        evilMage.setNextForceTalk(new ForceTalk("This is private property, get out NOW!"));
        WorldTasksManager.schedule(new WorldTask() {
            int spellStage = 0;

            @Override
            public void run() {
                boolean inCave = player.getControlerManager().getControler() instanceof VaultController;
                if(!inCave) {
                    stop();
                    return;
                }
                if (spellStage == 0) {
                    evilMage.setNextGraphics(new Graphics(482));
                    evilMage.setNextAnimation(new Animation(1978));
                } else if (spellStage == 1) {
                    player.sendMessage("You are teleported outside by the wizard.");
                    FadingScreen.fade(player, 1000, () -> player.getControlerManager().forceStop());
                    stop();
                }
                spellStage++;
            }
        }, 2, 2);
}
}
