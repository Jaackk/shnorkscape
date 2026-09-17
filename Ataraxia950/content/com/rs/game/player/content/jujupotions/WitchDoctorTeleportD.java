package com.rs.game.player.content.jujupotions;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.jujupotions.jadinkos.JadinkoManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WitchDoctorTeleportD extends Dialogue {

    @Override
    public void start() {
        sendMain();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMain();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2225, 3397, 1));
                    } else {
                        sendNPCDialogue(17508, NORMAL,
                                "Harmony pillars are located in Prifddinas, so I cannot take you there with your current skillset.");
                    }
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2953, 2902, 0));
                } else if (componentId == OPTION_3) {
                    String godJadinkos = "God jadinkos (lvl 81)";
                    sendOptionsDialogue("Select an option.",
                            "Common jadinkos (lvl 74-80)",
                            JadinkoManager.isGodActive() ? Colors.GREEN + godJadinkos : Colors.RED + godJadinkos);
                    stage = 1;
                }
                break;
            case 1:
                if (componentId == OPTION_1) {
                    JadinkoManager.commonInstance.enter(player);
                } else if (componentId == OPTION_2) {
                    if (JadinkoManager.isGodActive()) {
                        end();
                        JadinkoManager.godInstance.enter(player);
                    } else {
                        sendNPCDialogue(17508, NORMAL,
                                "Looks like there aren't any God jadinkos around at the moment...",
                                "If my intuition is correct, I'd say they'll be back in around " + Colors.DARK_RED + JadinkoManager.getGodTimeRemaining(true) + "</col>.");
                        stage = -1;
                    }
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendMain() {
        sendOptionsDialogue("Select an option.",
                "Harmony pillars",
                "Vine herb patches",
                "Jadinkos");
        stage = 0;
    }
}
