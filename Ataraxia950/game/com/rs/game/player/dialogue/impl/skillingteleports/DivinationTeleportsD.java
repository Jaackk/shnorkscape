package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DivinationTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Choose your destination",
                "Pale wisp [Level 1]",
                "Flickering wisp [Level 10]",
                "Bright wisp [Level 20]",
                "Glowing wisp [Level 30]",
                Colors.RED + "Next...");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3129, 3215, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(3002, 3403, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(3309, 3400, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2730, 3420, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Choose your destination",
                            "Sparkling wisp [Level 40]",
                            "Gleaming wisp [Level 50]",
                            "Vibrant wisp [Level 60]",
                            "Lustrous wisp [Level 70]",
                            Colors.RED + "Next..");
                    stage = 1;
                }
                break;
            case 1:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2777, 3598, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2888, 3041, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2418, 2860, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(3460, 3539, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Choose your destination",
                            "Brilliant wisp [Level 80]",
                            "Radiant wisp [Level 85]",
                            "Luminous wisp [Level 90]",
                            "Incandescent wisp [Level 95]",
                            Colors.RED + "Home..");
                    stage = 2;
                }
                break;
            case 2:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3403, 3300, 0));
                    end();
                }
                if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(3800, 3551, 0));
                    end();
                }
                if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(3309, 2651, 0));
                    end();
                }
                if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2284, 3053, 0));
                    end();
                }
                if (componentId == OPTION_5) {
                    start();
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}