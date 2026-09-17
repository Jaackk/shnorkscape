package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class CMLFletching extends Action {

    public static final int LOG_ID = 40338;
    public static final double LOG_XP = 96.0;
    public static final int LOG_LEVEL = 80;
    private final boolean portable;
    private int ticks;

    public CMLFletching(int amount, boolean portable) {
        this.ticks = amount;
        this.portable = portable;
    }

    @Override
    public boolean process(Player player) {
        if (ticks == 0)
            return false;
        if (!player.getInventory().containsItem(LOG_ID, 1)) {
            player.sendMessage("You need some more corrupted magic logs to do this.");
            return false;
        }
        if (player.getSkills().getLevel(Skills.FLETCHING) < LOG_LEVEL) {
            player.sendMessage("You need a Fletching level of " + LOG_LEVEL + " to fletch this.");
            return false;
        }
        if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
        }
        return true;
    }

    @Override
    public int processWithDelay(Player player) {
        ticks--;
        player.addItemsFletched();
        double experience = LOG_XP * (portable ? 1.1 : 1);
        player.setNextAnimation(new Animation(24939));
        player.getSkills().addXp(Skills.FLETCHING, experience);
        if (portable && ThreadLocalRandom.current().nextBoolean()) {
            player.sendMessage(Colors.GOLD + "<shad=000000>The portable fletcher saves you some resources.", true);
        } else {
            player.getInventory().deleteItem(new Item(LOG_ID));
        }
        player.sendMessage("You fletch the corrupted magic logs; items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>.", true);
        return player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER) ? 3 : 5;
    }

    @Override
    public boolean start(Player player) {
        if (!player.getInventory().containsItem(LOG_ID, 1)) {
            player.sendMessage("You need corrupted magic logs to do this.");
            return false;
        }
        if (player.getSkills().getLevel(Skills.FLETCHING) < LOG_LEVEL) {
            player.sendMessage("You need a Fletching level of " + LOG_LEVEL + " to fletch this.");
            return false;
        }
        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(player.clickedObject, player.clickedObject.getId()))
                return false;
        }
        if (!player.getInventory().containsItem(946, 1) && !player.getToolBelt().contains(946)) {
            player.sendMessage("You need a knife to fletch the corrupted magic logs.");
            return false;
        }
        return true;
    }

    @Override
    public void stop(Player player) {
        player.clickedObject = null;
    }
}