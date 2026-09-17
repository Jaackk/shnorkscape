package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.content.distinctioncape.DistinctionCapeInterface;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

/**
 * Handles the Distinction Cape stand.
 *
 * @author Noel
 */
public class CompCape extends Dialogue {

    /**
     * This dialogue is sent upon the player clicking on object id: 112747.
     */

    private WorldObject object;
    private WorldTile tileSentTo;

    @Override
    public void start() {
        this.object = (WorldObject) parameters[0];
        sendOptionsDialogue("Achievement cape stand", "Requirements", "Max cape", "Completionist's cape", "Trimmed completionist's cape");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == -1) {
            if (componentId == OPTION_1) {
                DistinctionCapeInterface.open(player);
                end();
            } else if (componentId == OPTION_2) {
                if (!DistinctionCape.isMaxed(player)) {
                    notWorthy(player);
                } else {
                    sendOptionsDialogue("Are you sure you wish to buy it?", "Yes, I deserve it.", "No, not now.");
                    stage = 2;
                }
            } else if (componentId == OPTION_3) {
                if (!DistinctionCape.isWorthyCompCape(player)) {
                    notWorthy(player);
                } else {
                    sendOptionsDialogue("Are you sure you wish to buy it?", "Yes, I deserve it.", "No, not now.");
                    stage = 3;
                }
            } else if (componentId == OPTION_4) {
                if (!DistinctionCape.isWorthyCompCapeT(player)) {
                    notWorthy(player);
                } else {
                    sendOptionsDialogue("Are you sure you wish to buy it?", "Yes, I deserve it.", "No, not now.");
                    stage = 4;
                }
            }
        } else if (stage == 2) {
            if (componentId == OPTION_1) {
                if (!player.hasMoney(2475000)) {
                    sendNPCDialogue(4405, SAD, "Seems that you don't have enough money, come back later when you do.");
                    stage = 99;
                    return;
                }
                if (!player.isMax()) {
                    World.sendWorldMessage(Colors.RED + "<img=5><shad=000000>News: " + player.getDisplayName() + " has been awarded the Max Cape on " + player.getXPMode() + " mode.", false);
                    HcimNewsManager.getInstance().addNews(player,"<#player> was awarded the Max Cape!");
                            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/max.png\" " + "width=17> " + player.getDisplayName() + " has been awarded the Max Cape on " + player.getXPMode() + " mode."));
                }
                player.addItem(new Item(20767));
                player.addItem(new Item(20751));
                player.takeMoney(2475000);
                player.setMax(true);
                player.getAchievements().updateProgress(1, AchievementList.OBTAIN_MAX_CAPE);
                stage = 99;
                end();
            } else if (componentId == OPTION_2) {
                end();
            }
        } else if (stage == 3) {
            if (componentId == OPTION_1) {
                if (!player.hasMoney(5000000)) {
                    sendNPCDialogue(4405, SAD, "Seems that you don't have enough money, come back later when you do.");
                    stage = 99;
                    return;
                }
                if (!player.isComp()) {
                    World.sendWorldMessage(Colors.RED + "<img=5><shad=000000>News: " + player.getDisplayName() + " has been awarded the Completionist Cape on " + player.getXPMode() + " mode.", false);
                    HcimNewsManager.getInstance().addNews(player,"<#player> was awarded the Completionist Cape!");
                            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/comp.png\" " + "width=17> " + player.getDisplayName() + " has been awarded the Completionist Cape on " + player.getXPMode() + " mode."));
                }
                player.addItem(new Item(20769));
                player.addItem(new Item(20770));
                player.takeMoney(5000000);
                player.setComp(true);
                stage = 99;
                end();
            } else if (componentId == OPTION_2) {
                end();
            }
        } else if (stage == 4) {
            if (componentId == OPTION_1) {
                if (!player.hasMoney(10000000)) {
                    sendNPCDialogue(4405, SAD, "Seems that you don't have enough money, come back later when you do.");
                    stage = 99;
                    return;
                }
                if (!player.isCompT()) {
                    World.sendWorldMessage(Colors.RED + "<img=5><shad=000000>News: " + player.getDisplayName() + " has been awarded the Trimmed Completionist Cape on " + player.getXPMode() + " mode.", false);
                    HcimNewsManager.getInstance().addNews(player,"<#player> was awarded the Trimmed Completionist Cape!");
                            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/comp.png\" " + "width=17> " + player.getDisplayName() + " has been awarded the Trimmed Completionist Cape on " + player.getXPMode() + " mode."));
                }
                player.addItem(new Item(20771));
                player.addItem(new Item(20772));
                player.takeMoney(10000000);
                player.setCompT(true);
                stage = 99;
                end();
            } else if (componentId == OPTION_2) {
                end();
            }
        } else if (stage == 99) {
            end();
        }/* else if (stage == 20) {
			if (componentId == OPTION_1)
				player.getDistinctionCapes().sendMaxInterface();
			else if (componentId == OPTION_2)
				player.getDistinctionCapes().sendCompletionistInterface();
			else
				player.getDistinctionCapes().sendTrimmedCompletionistInterface();
			end();
		}*/
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    /**
     * Sends the hilarious animation if not worthy to claim a cape.
     *
     * @param player The player to send to.
     */
    private void notWorthy(final Player player) {
        String animationKey = "preforming_notWorthy_animation";
        if (!player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()), 3) || player.getTemporaryAttributtes().containsKey(animationKey)) {
            return;
        }
        player.getTemporaryAttributtes().put(animationKey, Boolean.TRUE);
        player.getInterfaceManager().closeChatBoxInterface();
        player.lock(15);
        player.setNextFaceWorldTile(object);
        WorldTasksManager.schedule(new WorldTask() {
            int phase = 0;

            @Override
            public void run() {
                int[] npcIds = {6935, 3283, 4344, 6966};
                switch (phase) {
                    case 0:
                        player.setNextAnimation(new Animation(857));
                        break;
                    case 1:
                        player.setNextAnimation(new Animation(915));
                        break;
                    case 2:
                        player.setNextAnimation(new Animation(857));
                        break;
                    case 3:
                        player.setNextGraphics(new Graphics(86));
                        player.getAppearence().transformIntoNPC(npcIds[Utils.random(npcIds.length - 1)]);
                        break;
                    case 4:
                        player.setNextForceTalk(new ForceTalk(".. what in Ataraxia is going on..!?"));
                        break;
                    case 5:
                        player.setNextGraphics(new Graphics(86));
                        player.getAppearence().transformIntoNPC(-1);
                        player.setNextAnimation(new Animation(10070));
                        tileSentTo = new WorldTile(object.getX(), object.getY() + 5, object.getPlane());
                        if (object.getRotation() == 3) {
                            tileSentTo = new WorldTile(object.getX() + 5, object.getY(), object.getPlane());
                        } else if (object.getRotation() == 1) {
                            tileSentTo = new WorldTile(object.getX() - 5, object.getY(), object.getPlane());
                        } else if (object.getRotation() == 0) {
                            tileSentTo = new WorldTile(object.getX(), object.getY() - 5, object.getPlane());
                        }
                        player.setNextForceMovement(new ForceMovement(player, 0, tileSentTo, 2, object.getRotation() == 3 ? ForceMovement.WEST : object.getRotation() == 1 ? ForceMovement.EAST : object.getRotation() == 0 ? ForceMovement.NORTH : ForceMovement.SOUTH));
                        break;
                    case 6:
                        player.getTemporaryAttributtes().remove(animationKey);
                        player.setNextWorldTile(tileSentTo);
                        sendNPCDialogue(4405, GOOFY_LAUGH, "Looks like Dahmaroc had a sense of humour!");
                        player.unlock();
                        stage = 99;
                        break;
                }
                phase++;
            }
        }, 0, 2);
    }
}