package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Colour;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.ZarosGodwars;
import com.rs.game.activities.instances.NexInstance;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DialogueOptionEvent;

public class ZGDController extends Controller {

    @Override
    public void forceClose() {
        remove();
    }

    @Override
    public boolean login() {
        sendInterfaces();
        forceClose();
        player.getControlerManager().startControler(GodWars.class.getSimpleName());
        player.setNextWorldTile(new WorldTile(2904, 5203, 0));
        return false; // so doesnt remove script
    }

    @Override
    public boolean canMove(int dir) {
        if (player.getTemporaryAttributtes().get("insideIcePrison") != null)
            return false;
        return super.canMove(dir);
    }

    @Override
    public boolean canWalk() {
        if (player.getTemporaryAttributtes().get("insideIcePrison") != null)
            return false;
        return super.canWalk();
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        if (player.getTemporaryAttributtes().get("insideIcePrison") != null)
            return false;
        Nex nex = (Nex) player.getTemporaryAttributtes().get("bloodSacrificeTarget");
        if (nex != null && !new WorldTile(lastX, lastY, player.getPlane()).withinDistance(nex, 6)) {
            player.getTemporaryAttributtes().remove("bloodSacrificeTarget");
            player.setNextColour(new Colour(0, 0, 0, 0, 0, 0));
            player.getPackets().sendGameMessage("You successfully escape the blood sacrifice.");
        }
        return super.checkWalkStep(lastX, lastY, nextX, nextY);
    }

    @Override
    public boolean logout() {
        ZarosGodwars.removePlayer(player);
        return false; // so doesnt remove script
    }

    @Override
    public void magicTeleported(int type) {
        remove();
        removeControler();
    }

    public void remove() {
        player.getInterfaceManager().closeOverlay(false);
        if (player.getCurrentInstance() != null) {
            if (player.getCurrentInstance() instanceof NexInstance) {
                NexInstance instance = (NexInstance) player.getCurrentInstance();
                instance.removePlayerFromArena(player);
            }
            player.getCurrentInstance().removePlayer(player);
        }
        ZarosGodwars.removePlayer(player);
        removeControler();
    }

    @Override
    public boolean sendDeath() {
        remove();
        removeControler();
        return true;
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().sendOverlay(601, false);
    }

    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (object.getId() == 57263) {
            if (player.getTemporaryAttributtes().get("insideIcePrison") != null) {
                if (!player.hasFreedomActive()) {
                    player.getPackets().sendGameMessage("You can only free your self while having freedom active.");
                    return false;
                }
            }
            player.faceObject(object);
            player.setNextAnimation(new Animation(422));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    double failChance = 0.10;
                    if (Math.random() <= failChance) {
                        player.getPackets().sendGameMessage("You fail to break the ice stalagmite.");
                        return;
                    }
                    NexInstance instance = player.getCurrentInstance() != null && player.getCurrentInstance() instanceof NexInstance ? (NexInstance) player.getCurrentInstance() : null;
                    for (Player p : instance == null ? ZarosGodwars.getPlayers() : instance.getPlayers()) {
                        if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof ZGDController))
                            continue;
                        p.getTemporaryAttributtes().remove("insideIcePrison");
                    }
                    if (World.containsObjectWithId(object, object.getId()))
                        World.removeObject(object);
                    player.getPackets().sendGameMessage("You successfully break the ice stalagmite");
                }
            });
            return false;
        }
        if (player.getCurrentInstance() == null || !(player.getCurrentInstance() instanceof NexInstance))
            return false;
        if (object.getId() == 57225) {
            NexInstance instance = (NexInstance) player.getCurrentInstance();
            if (instance == null)
                return false;

            if (player.getX() <= 77) {
                player.sendOptionsDialogue("Do you wish to enter the fight?", new String[] { "Climb down.", "Stay here." }, new DialogueOptionEvent() {

                    @Override
                    public void run(Player player) {
                        if (getOption() == OPTION_1) {
                            if (!instance.nexSpawned) {
                                instance.nexSpawned = true;
                            }
                            player.setNextWorldTile(instance.getWorldTile(15, 19));
                            instance.addPlayerToArena(player);
                        }
                    }
                });
                return true;
            } else {
                player.getDialogueManager().startDialogue("SimpleMessage", "The prison barrier is closed from this side.");
                return false;
            }
        }
//		if (object.getId() == 57225) {
//			if (player.getCurrentInstance() != null) {
//				if (player.getX() <= 77) {
//					if (player.getCurrentInstance().getBoss() != 13447)
//						return false;
//					player.lock();
//					player.sendOptionsDialogue("Are you sure you wish to climb down?", new String[] { "Climb down.", "Stay here." }, new DialogueOptionEvent() {
//
//						@Override
//						public void run(Player player) {
//							int option = getOption();
//							if (option == OPTION_1) {
//								player.setNextWorldTile(player.getCurrentInstance().getWorldTile(15, 19));
//								zaros.addPlayer(player);
//							}
//						}
//					});
//					player.unlock();
//					return false;
//				} else {
//					player.getDialogueManager().startDialogue("SimpleMessage", "The prison barrier is closed from this side.");
//					return false;
//				}
//			} else {
//				if (player.getX() > object.getX()) {
//					player.getDialogueManager().startDialogue("SimpleMessage", "The prison barrier is closed from this side.");
//					return false;
//				}
//			}
//		} else if (object.getId() == 57258) {
//			if (player.getCurrentInstance() != null && player.getCurrentInstance().getBoss() == 13447) {
//				player.getCurrentInstance().removePlayer(player);
//				forceClose();
//				player.setNextWorldTile(new WorldTile(2900, 5203, 0));
//				player.getControlerManager().startControler(GodWars.class.getSimpleName());
//			}
//		}
        return true;
    }

    @Override
    public void start() {
        sendInterfaces();
    }

}