package com.rs.game.player.content.maxguild.combatportal;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.TaskTab;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author Xenthium.
 */

public class BossPortal {

    public static final int REGION_ID = 9011;
    public static final WorldTile PORTAL_TILE = new WorldTile(2284, 3302, 1);
    public static final String RETUNING_COMBAT_PORTAL_KEY = "RETUNING_COMBAT_PORTAL";

    public static void spawn(Player player) {
        if (player.getBossPortal() != null && player.getBossPortal() != BossPortalType.UNDIRECTED && player.getBossPortal() != BossPortalType.UNDIRECTED1) {
            player.getPackets().addSpawnedObject(new WorldObject(player.getBossPortal().getObjectId(), 11, 0, PORTAL_TILE));
        }
    }

    public static void handleTeleport(Player player, WorldObject object) {
        final int departureGfx = 537, arrivalGfx = 538, departureAnim = 7389, arrivalAnim = 9013;
        if (object == null || !objectIsCombatPortal(object.getId())) {
            return;
        }
        if (player.getBossPortal() == null || player.getBossPortal() == BossPortalType.UNDIRECTED || player.getBossPortal() == BossPortalType.UNDIRECTED1) {
            handleRetune(player, object);

            return;
        }
        player.stopAll();
        player.lock();
        player.getTemporaryAttributtes().put("teleporting", true);
        player.setNextGraphics(new Graphics(departureGfx));
        player.setNextAnimation(new Animation(departureAnim));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextGraphics(new Graphics(arrivalGfx));
                player.setNextAnimation(new Animation(arrivalAnim));
                player.setNextWorldTile(player.getBossPortal().getDestinationTile());
                player.getControlerManager().startControler(player.getBossPortal().getController());
                CoresManager.getServiceProvider().executeWithDelay(() -> player.getTemporaryAttributtes().remove("teleporting"), 4);
                player.unlock();
            }
        }, 3);
    }

    public static void handleRetune(Player player, WorldObject object) {
        if (object == null || !objectIsCombatPortal(object.getId())) {
            return;
        }
        if (canRetune(player)) {
            BossPortalInterfaceHandler.sendInterface(player, 1);
        }
    }

    public static boolean canRetune(Player player) {
        if (!player.isMax()) {
            player.sendMessage("You don't meed the requirements to use this; how'd you even get here?");
            return false;
        }
        if (player.getMoneySpent() < 500) {
            if (player.getCombatPortalRetuneDelay() > Utils.currentTimeMillis()) {
                player.sendMessage("You must wait another " + Colors.wrap(Colors.RED, TaskTab.format(player.getCombatPortalRetuneDelay() - Utils.currentTimeMillis())) + " before you can retune the portal.");
                return false;
            }
            if (player.getMoneyPouch().getTotal() < getRetuneCost(player)) {
                player.sendMessage("You need " + Colors.wrap(Colors.RED, Utils.formatNumber(getRetuneCost(player))) + " coins to retune the portal.");
                return false;
            }
        }
        return true;
    }

    public static boolean objectIsCombatPortal(int objectId) {
        return BossPortalType.getPortalForId().containsKey(objectId);
    }

    public static int getRetuneCost(Player player) {
        final int retuneBaseCost = 6_000_000;
        return (int) (retuneBaseCost * (player.getMoneySpent() >= 250 ? 0.3333334 : player.getMoneySpent() >= 100 ? 0.6666667 : 1));
    }

    public static long getRetuneDelay(Player player) {
        final int retuneBaseDelay = 43_200_000; // 12 hours.
        return Utils.currentTimeMillis() + (long) (retuneBaseDelay * (player.getMoneySpent() >= 250 ? 0.3333 : player.getMoneySpent() >= 100 ? 0.6666 : 1));
    }

}
