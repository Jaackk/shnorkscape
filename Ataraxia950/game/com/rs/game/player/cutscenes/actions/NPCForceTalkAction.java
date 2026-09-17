package com.rs.game.player.cutscenes.actions;

import com.rs.game.ForceTalk;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public class NPCForceTalkAction extends CutsceneAction {

    private NPC npc;
    private final String text;

    public NPCForceTalkAction(int cachedObjectIndex, String text, int actionDelay) {
        super(cachedObjectIndex, actionDelay);
        this.text = text;

    }

    public NPCForceTalkAction(NPC npc, String text, int actionDelay) {
        super(-1, actionDelay);
        this.npc = npc;
        this.text = text;
    }

    @Override
    public void process(Player player, Object[] cache) {
        NPC processNpc = npc == null ? (NPC) cache[getCachedObjectIndex()] : npc;
        processNpc.setNextForceTalk(new ForceTalk(text));
    }

}
