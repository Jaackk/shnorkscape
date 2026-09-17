// SmeltingD.java  Shows all bars regardless of ores; robust label rendering
package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.Smelting;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.dialogue.Dialogue;

import java.util.concurrent.atomic.AtomicInteger;

public class SmeltingD extends Dialogue {

    private WorldObject object;
    private SmeltingBar[] bars;

    @Override
    public void start() {
        object = (WorldObject) parameters[0];
        bars = SmeltingBar.getFurnaceBars();

        int[] ids = new int[bars.length];
        for (int i = 0; i < bars.length; i++) {
            ids[i] = bars[i].getProducedBar().getId();
        }

        // Avoid relying on stable one-pass rendering; use a rolling index with modulo.
        final AtomicInteger idx = new AtomicInteger(0);

        SkillsDialogue.sendSkillsDialogue(
                player,
                SkillsDialogue.MAKE,
                "How many bars would you like to smelt?<br>Choose a number, then click the bar to begin.",
                28, // UI cap; the action should clamp to actual craftable amount
                ids,
                new ItemNameFilter() {
                    @Override
                    public String rename(String name) {
                        int i = idx.getAndIncrement() % bars.length;
                        SmeltingBar bar = bars[i];
                        boolean meets = player.getSkills().getLevel(Skills.SMITHING) >= bar.getLevelRequired();

                        // Name in green if level met, red otherwise; show required level on second line.
                        StringBuilder sb = new StringBuilder();
                        sb.append(meets ? "<col=00ff00>" : "<col=ff0000>").append(name);
                        sb.append("<br><col=ffff00>Level ").append(bar.getLevelRequired());
                        return sb.toString();
                    }
                }
        );
    }

    @Override
    public void run(int interfaceId, int componentId) {
        int slot = SkillsDialogue.getItemSlot(componentId);
        if (slot < 0 || slot >= bars.length) {
            end();
            return;
        }

        int quantity = SkillsDialogue.getQuantity(player);
        boolean isPortable = PortableStation.isPortableObject(object);

        player.getActionManager().setAction(new Smelting(bars[slot].getButtonId(), object, quantity, isPortable));
        end();
    }

    @Override
    public void finish() {
        // no-op
    }
}
