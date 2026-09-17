package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.World;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.quest.root_of_evil.EvilTreeHunterQuestD;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

import java.util.Objects;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeHunterD extends Dialogue {

    private final EvilTree tree;

    public EvilTreeHunterD(EvilTree tree) {
        this.tree = tree;
    }

    private void startInstanced() {
        if (tree.isSapling()) {
            sendNPCDialogue(13790, NORMAL,
                    "The little bastard is still growing. I'll rake its patch while waiting...");
        } else if (tree.isTree()) {
            int health = tree.getTreeObject().getHealthPercent();
            if (health <= 0) {
                sendNPCDialogue(13790, GOOFY_LAUGH, "HAHA! We did it! It wasn't that tough.",
                        "Now we can loot its body...");
            } else if (health <= 60) {
                sendNPCDialogue(13790, LISTEN_LAUGH, "H-Hey! We're killing it!");
            } else if (health <= 100) {
                sendNPCDialogue(13790, SCARED, "Don't look at me, go kill the damn thing!");
            }
        } else {
            throw new IllegalStateException("Unexpected error: EvilTreeNPC present, EvilTreeObject absent.");
        }
        stage = 0;
    }

    private void startGlobal() {
        sendNPCDialogue(13790, NORMAL, "I am the Evil Tree hunter.",
                "Do not waste my time with talk, unless you'll help me slay these demons.");
        stage = 0;
    }

    private void startQuest() {
        sendOptionsDialogue("Select an option.",
                "Talk about " + Colors.RED + "Root of Evil</col>",
                "Other");
        stage = -1;
    }

    private void realStart() {
        if (tree != null) {
            startInstanced();
        } else {
            startGlobal();
        }
    }

    @Override
    public void start() {
        int currentStage = player.quests.getCurrentStage(RootOfEvil.class);
        if (currentStage == 0) {
            startQuest();
        } else {
            realStart();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue(new EvilTreeHunterQuestD());
                } else if (componentId == OPTION_2) {
                    realStart();
                }
                break;
            case 0:
                if (tree != null) {
                    sendOptionsDialogue("Select an option.",
                            "What are Evil Trees?",
                            "Tell me more about Evil Trees.",
                            "Can I view my bank?");
                } else {
                    int instanceCount = BossInstanceHandler.getCount(Boss.Evil_Tree);
                    if (instanceCount > 0) {
                        sendOptionsDialogue("Select an option.",
                                "What are Evil Trees?",
                                "Tell me more about Evil Trees.",
                                "Take me to the Evil Tree!",
                                "Join a private Evil Tree instance (" + instanceCount + ")");
                    } else {
                        sendOptionsDialogue("Select an option.",
                                "What are Evil Trees?",
                                "Tell me more about Evil Trees.",
                                "Take me to the Evil Tree!");
                    }
                }
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue("WhatAreTreesD");
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue("TellMeMoreD");
                } else if (componentId == OPTION_3) {
                    if (tree != null) {
                        player.getDialogueManager().startDialogue(new BankConfirmationD(tree, true));
                    } else {
                        player.getDialogueManager().startDialogue(new TeleportConfirmationD(true));
                    }
                } else if (componentId == OPTION_4) {
                    boolean isGim = player.isGroupIronman();
                    if (player.isATypeOfIronman() && !isGim) {
                        BossInstance joined = BossInstanceHandler.joinInstance(player, Boss.Evil_Tree, player.getUsername(), false);
                        if (joined != null) {
                            end();
                        } else {
                            sendNPCDialogue(13790, LAUGHING,
                                    "Nice try, I know what you are. Real ironmen create their own instances!");
                            stage = 0;
                        }
                    } else {
                        player.sendInputString("Enter the display name of the player who's instance you want to join.", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String playerName = getString().toLowerCase().trim();
                                if (isGim) {
                                    BossInstance instance = BossInstanceHandler.findInstance(Boss.Evil_Tree, playerName);
                                    Player instancePlayer = null;
                                    if (instance != null && instance.getOwner() != null) {
                                        instancePlayer = instance.getOwner();
                                    }
                                    if (instance != null && instancePlayer == null) {
                                        instancePlayer = World.getPlayerByDisplayName(playerName);
                                    }
                                    if (instancePlayer == null) {
                                        sendNPCDialogue(13790, NORMAL, "That player is offline, or has privacy mode enabled.");
                                        stage = 0;
                                        return;
                                    }
                                    if (player.isGroupIronman() && !player.canGimInteractWith(instancePlayer)) {
                                        sendNPCDialogue(13790, LAUGHING, "You cannot do this with players that aren't in your group.");
                                        stage = 0;
                                        return;
                                    }
                                    if (instancePlayer.isGroupIronman() && !instancePlayer.canGimInteractWith(player)) {
                                        sendNPCDialogue(13790, LAUGHING, "You cannot join the Evil Tree instances of Group Ironmen.");
                                        stage = 0;
                                        return;
                                    }
                                }
                                BossInstanceHandler.joinInstance(player, Boss.Evil_Tree, playerName, false);
                                end();
                            }
                        });
                    }
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}