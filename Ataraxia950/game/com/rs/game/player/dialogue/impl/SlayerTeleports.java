package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Handles the Slayer Teleports dialogue.
 *
 * @author Noel
 */
public class SlayerTeleports extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Choose your destination", "Kuradal's dungeon", "Jadinko lair", "Polypore dungeon",
                "Slayer tower", Colors.RED + "More Options..");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == -1) {
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(1690, 5286, 1));
                end();
            }
            if (componentId == OPTION_2) {
                if (player.getSkills().getLevel(Skills.SLAYER) < 80) {
                    sendDialogue("You need a slayer level of 80 to use this teleport.");
                    end();
                    return;
                }
                Magic.vineTeleport(player, new WorldTile(3012, 9274, 0));
                end();
            }
            if (componentId == OPTION_3) {
                Magic.vineTeleport(player, new WorldTile(4625, 5457, 3));
                end();
            }
            if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(3423, 3543, 0));
                end();
            }
            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Ancient Cavern", "Brimhaven Dungeon",
                        "Fremennik Dungeon", "Taverley Dungeon", Colors.RED + "More Options..");
                stage = 0;

            }
        } else if (stage == 0) {
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(1763, 5365, 1));
                end();
            }
            if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(2699, 9564, 0));
                end();
            }
            if (componentId == OPTION_3) {
                Magic.vineTeleport(player, new WorldTile(2808, 10002, 0));
                end();
            }
            if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(2884, 9799, 0));
                end();
            }
            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Jungle Strykewyrms", "Desert Strykewyrms",
                        "Ice Strykewyrms", "Celestial dragons", Colors.RED + "More Options..");
                stage = 1;
            }
        } else if (stage == 1) {
            if (componentId == OPTION_1) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 73) {
                    sendDialogue("You need at least a level of 73 Slayer to go there!");
                    return;
                }
                Magic.vineTeleport(player, new WorldTile(2452, 2911,
                        0)); /** Jungle Strykewyrms **/
                end();
            }
            if (componentId == OPTION_2) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 77) {
                    sendDialogue("You need at least a level of 77 Slayer to go there!");
                    return;
                }
                Magic.vineTeleport(player, new WorldTile(3356, 3160,
                        0)); /** Desert Strykewyrms **/
                end();
            }
            if (componentId == OPTION_3) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 93) {
                    sendDialogue("You need at least a level of 93 Slayer to go there!");
                    return;
                }
                Magic.vineTeleport(player,
                        new WorldTile(3435, 5648, 0)); /** Ice Strykewyrms **/
                end();
            }
            if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(2285, 5972, 0));
                end();
            }

            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Kal'gerion Dungeon", "Guthix Cave", "Nihils", "Muspahs", Colors.RED + "More Options..");
                stage = 2;
            }

        } else if (stage == 2) {
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(1312, 1312, 0));
                end();
            }
            if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(1814, 5985, 0));
                end();
            }
            if (componentId == OPTION_3) {
                Magic.vineTeleport(player, new WorldTile(4060, 6249, 0));
                end();
            }
            if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(4268, 6320, 0));
                end();
            }
            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Camel Warriors", "Ripper Demons", "Gemstone dragons", "Crystal Shapeshifters", Colors.RED + "More Options..");
                stage = 3;
            }
        } else if (stage == 3) {
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(3383, 2722, 0));
                end();
            }
            if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(5160, 7590, 0));
                end();
            }
            if (componentId == OPTION_3) {
                Magic.vineTeleport(player, new WorldTile(2838, 9395, 0));
                end();
            }
            if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(4116, 6570, 0));
                end();
            }
            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Dagannoths", "Sophanem Slayer Dungeon", "Edimmus", "Ascension Dungeon",Colors.RED + "More Options..");
                stage = 4;
            }

        } else if (stage == 4) {
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(2485, 10147, 0));
                end();
            }
            if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(3284, 2744, 0));
                end();
            }
            if (componentId == OPTION_3) {
                if(player.hasAccessToPrifddinas()) {
                    Magic.vineTeleport(player, new WorldTile(2234, 3396, 1));
                } else {
                    player.sendMessage("You do not meet the requirements to access Priffdinas.");
                }
                end();
            }
            if(componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(2502, 2886, 0));
                end();
            }
            if (componentId == OPTION_5) {
                sendOptionsDialogue("Choose your destination", "Kuradal's dungeon", "Jadinko lair", "Polypore dungeon",
                        "Slayer tower", Colors.RED + "More Options..");
                stage = -1;
            }
        }
    }

    @Override
    public void finish() {
    }
}