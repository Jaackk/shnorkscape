package com.rs.game.player.actions.invention;

import java.util.concurrent.ConcurrentHashMap;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;

public class JunkRefiner extends Action {

    private Item item;
    private int ticks;

    public JunkRefiner(Item item, int ticks) {
        this.item = item;
        this.ticks = ticks;
    }

    @Override
    public boolean start(Player player) {

        return checkAll(player);
    }

    private boolean checkAll(Player player) {
        if (player.getInventionManager().getMaterials()[75] < 100) {
            player.getPackets().sendGameMessage("You need atleast 100 junk components to use the junk refiner.");
            return false;
        }
        if (item.getId() == 39659 && player.getInventory().getFreeSlots() == 0) {
            player.getPackets().sendGameMessage("You don't have enough inventory space to use the junk refiner.");
            return false;
        }

        return true;
    }

    @Override
    public boolean process(Player player) {

        return checkAll(player) && ticks > 0;
    }

    @Override
    public int processWithDelay(Player player) {
        ticks--;
        if (item.getId() == 39659 && player.getInventory().containsItem(item.getId(), 1)) {
            player.getInventory().deleteItem(39659, 1);
            Item junkRefiner = new Item(36388);
            ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
            attributes.put(Key.JUNK_REFINER_CHARGES_LEFT, getJunkRefinerMaxCharges(player));
            junkRefiner.setAttributes(attributes);
            item = junkRefiner;
            player.getInventory().addItem(junkRefiner);
        } else if (item.getId() == 36388 && item.getAttributes() == null) {
            Item invItem = player.getInventory().getItem(player.getInventory().getItemSlot(item));
            if (invItem == null)
                return -1;
            ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
            attributes.put(Key.JUNK_REFINER_CHARGES_LEFT, getJunkRefinerMaxCharges(player));
            invItem.setAttributes(attributes);
            player.getInventory().refresh();
        }
        if (item.getAttributes() == null)
            return -1;
        player.setNextGraphics(new Graphics(6003));
        player.setNextAnimation(new Animation(27997));
        int chargesLeft = getJunkRefinerChargesleft(player, item);
        chargesLeft -= 1;
        player.getInventionManager().getMaterials()[75] -= 100;
        player.getInventionManager().getMaterials()[68] += 1;
        player.getInventionManager().refreshMaterials();
        player.getPackets().sendGameMessage("The junk refiner converted 100 junk into a refined component.");
        if (chargesLeft > 0)
            item.getAttributes().put(Key.JUNK_REFINER_CHARGES_LEFT, chargesLeft);
        else {
            player.getPackets().sendExecuteScript(1211, "The junk refiner exploaded!", 0, -120, 0);
            player.setNextGraphics(new Graphics(6005));
            player.getInventory().deleteItem(item);
            return -1;
        }
        return 3;
    }

    @Override
    public void stop(Player player) {
        player.getInventionManager().refreshMaterials();
        setActionDelay(player, 3);
    }

    public static boolean openJunkRefiner(Player player, Item item) {
        if (!item.getDefinitions().getName().equalsIgnoreCase("junk refiner"))
            return false;
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                RS3SkillsDialogue.sendSkillDialogueByProduce(player, 36375);
                player.getPackets().sendGlobalString(2390, "Junk Refiner");
                RS3SkillsDialogue.setMaxQuantity(player, item.getId(), getJunkRefinerChargesleft(player, item));
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId == RS3SkillsDialogue.CONTINUE_OPTION) {
                    player.getActionManager().setAction(new JunkRefiner(item, result.getQuantity()));
                    end();
                }
            }

            @Override
            public void finish() {

            }
        });
        return true;
    }

    public static void check(Player player, Item item) {
        int chargesLeft = getJunkRefinerChargesleft(player, item);
        player.getPackets().sendGameMessage(((item.getAmount() > 1) ? item.getAmount() + " x " : "") + "Junk Refiner: " + chargesLeft + " Charges left!");
    }

    public static int getJunkRefinerChargesleft(Player player, Item item) {
        if (item.getAttributes() == null || !item.getAttributes().containsKey(Key.JUNK_REFINER_CHARGES_LEFT))
            return getJunkRefinerMaxCharges(player);
        return (int) item.getAttributes().get(Key.JUNK_REFINER_CHARGES_LEFT);
    }

    public static int getJunkRefinerMaxCharges(Player player) {
        int inv_level = player.getSkills().getLevelForXp(Skills.INVENTION);
        if (inv_level >= 120)
            return 20;
        else if (inv_level >= 110)
            return 19;
        else if (inv_level >= 100)
            return 18;
        else if (inv_level >= 90)
            return 17;
        else if (inv_level >= 80)
            return 16;
        else if (inv_level >= 70)
            return 15;
        else if (inv_level >= 60)
            return 14;
        else if (inv_level >= 50)
            return 13;
        else if (inv_level >= 40)
            return 12;
        else if (inv_level >= 30)
            return 11;
        else if (inv_level >= 22)
            return 10;
        return 9;
    }

}
