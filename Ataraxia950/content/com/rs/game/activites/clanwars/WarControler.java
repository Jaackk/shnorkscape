package com.rs.game.activites.clanwars;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.clanwars.ClanWars.Rules;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.content.Pots.Pot;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * A controler subclass handling players in the clan wars activity.
 *
 * @author Emperor
 */
public final class WarControler extends Controller {

    /**
     * The clan wars instance.
     */
    private transient ClanWars clanWars;

    /**
     * Constructs a new {@code WarControler} {@code Object}.
     */
    public WarControler() {
        /*
         * empty.
         */
    }

    @Override
    public void start() {
        this.clanWars = (ClanWars) super.getArguments()[0];
        player.setCanPvp(true);
        moved();
    }

    @Override
    public boolean sendDeath() {
        player.lock(7);
        player.stopAll();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (player == null) {
                    stop();
                    return;
                }
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    if (clanWars.get(Rules.ITEMS_LOST)) {
                        Player killer = player.getMostDamageReceivedSourcePlayer();
                        if (killer != null) {
                            killer.removeDamage(player);
                            killer.increaseKillCount(player);
                            player.getDeathManager().handleDeathInPvP(killer);
                            //player.sendItemsOnDeath(killer, true);
                            if(player.isGroupIronman()) {
                                player.gimTracker.incrementDeaths("<#player> died fighting in Clan Wars (Arena)");
                            }
                        }
                    }
                    player.setCanPvp(false);
                    player.reset();
                    if (player.getCurrentFriendChat().getClanWars() != null) {
                        if (clanWars.getFirstTeam() == player.getCurrentFriendChat()) {
                            player.setNextWorldTile(
                                    clanWars.getBaseLocation().transform(clanWars.getAreaType().getFirstDeathOffsetX(),
                                            clanWars.getAreaType().getFirstDeathOffsetY(), 0));
                            clanWars.getFirstPlayers().remove(player);
                            int firstKills = clanWars.getKills() & 0xFFFF;
                            int secondKills = (clanWars.getKills() >> 24 & 0xFFFF) + 1;
                            clanWars.setKills(firstKills | (secondKills << 24));
                        } else {
                            WorldTile northEast = clanWars.getBaseLocation().transform(
                                    clanWars.getAreaType().getNorthEastTile().getX()
                                            - clanWars.getAreaType().getSouthWestTile().getX(),
                                    clanWars.getAreaType().getNorthEastTile().getY()
                                            - clanWars.getAreaType().getSouthWestTile().getY(),
                                    0);
                            player.setNextWorldTile(northEast.transform(clanWars.getAreaType().getSecondDeathOffsetX(),
                                    clanWars.getAreaType().getSecondDeathOffsetY(), 0));
                            clanWars.getSecondPlayers().remove(player);
                            int firstKills = (clanWars.getKills() & 0xFFFF) + 1;
                            int secondKills = clanWars.getKills() >> 24 & 0xFFFF;
                            clanWars.setKills(firstKills | (secondKills << 24));
                        }
                    }
                    clanWars.updateWar();
                    player.setNextAnimation(new Animation(-1));
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public boolean canEat(Food food) {
        if (clanWars.get(Rules.NO_FOOD)) {
            player.getPackets().sendGameMessage("Food has been disabled during this war.");
            return false;
        }
        return true;
    }

    @Override
    public boolean canPot(Pot pot) {
        if (clanWars.get(Rules.NO_POTIONS)) {
            player.getPackets().sendGameMessage("Potions has been disabled during this war.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getPackets().sendGameMessage("You can't teleport out of a clan war!");
        return false;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        player.getPackets().sendGameMessage("You can't teleport out of a clan war!");
        return false;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        switch (object.getId()) {
            case 38697:
            case 28140:
                clanWars.leave(player, true);
            case 38696:
            case 38695:
            case 28139:
            case 38694:
            case 28214:
                clanWars.leave(player, true);
                return false;
        }
        return false;
    }

    @Override
    public boolean processPlayerOption1(Entity target) {
        if (!clanWars.getFirstPlayers().contains(player) && !clanWars.getSecondPlayers().contains(player)) {
            return false;
        }
        if (clanWars.getFirstPlayers().contains(player) && clanWars.getFirstPlayers().contains(target)) {
            player.getPackets().sendGameMessage("You can't attack players in your own team.");
            return false;
        }
        if (clanWars.getSecondPlayers().contains(player) && clanWars.getSecondPlayers().contains(target)) {
            player.getPackets().sendGameMessage("You can't attack players in your own team.");
            return false;
        }
        return clanWars.getTimer().isStarted();
    }

    @Override
    public boolean keepCombating(boolean mainHand, Entity victim) {
        boolean isRanging = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD) == Combat.RANGE_TYPE;
        if (player.getCombatDefinitions().getSpellId(!mainHand) > 0) {
            switch (clanWars.getMagicRuleCount()) {
                case 1: // Standard spells only.
                    if (player.getCombatDefinitions().getSpellBook() != 0) {
                        player.getPackets().sendGameMessage("You can only use modern spells during this war!");
                        return false;
                    }
                    break;
                case 2: // Bind/Snare/Entangle only.
                    if (player.getCombatDefinitions().getSpellBook() != 0) {
                        player.getPackets().sendGameMessage("You can only use binding spells during this war!");
                        return false;
                    }
                    int spellId = player.getCombatDefinitions().getSpellId(!mainHand);
                    if (spellId >= 1000)
                        spellId -= 1000;
                    switch (spellId) {
                        case 25://bind
                        case 43://snare
                        case 70://entangle
                            break;
                        default:
                            player.getPackets().sendGameMessage("You can only use binding spells during this war!");
                            return false;
                    }
                    break;
                case 3: // No magic at all.
                    player.getPackets().sendGameMessage("Magic combat is not allowed during this war!");
                    return false;
            }
        }
        if (isRanging && clanWars.get(Rules.NO_RANGE)) {
            player.getPackets().sendGameMessage("Ranged combat is not allowed during this war!");
            return false;
        }
        if (!isRanging && clanWars.get(Rules.NO_MELEE) && player.getCombatDefinitions().getSpellId(!mainHand) <= 0) {
            player.getPackets().sendGameMessage("Melee combat is not allowed during this war!");
            return false;
        }
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void moved() {
        switch (clanWars.getAreaType()) {
            case PLATEAU:
            case TURRETS:
                player.setForceMultiArea(true);
                break;
            case FORSAKEN_QUARRY:
                WorldTile northEast = clanWars.getBaseLocation().transform(
                        clanWars.getAreaType().getNorthEastTile().getX() - clanWars.getAreaType().getSouthWestTile().getX(),
                        clanWars.getAreaType().getNorthEastTile().getY() - clanWars.getAreaType().getSouthWestTile().getY(),
                        0).transform(-16, -16, 0);
                WorldTile southWest = clanWars.getBaseLocation().transform(16, 16, 0);
                player.setForceMultiArea(player.getX() >= southWest.getX() && player.getY() >= southWest.getY()
                        && player.getX() <= northEast.getX() && player.getY() <= northEast.getY());
                break;
        }
    }

    @Override
    public void forceClose() {
        if (clanWars != null) {
            clanWars.leave(player, true);
        } else
            player.setNextWorldTile(new WorldTile(2992, 9676, 0));
    }

    @Override
    public void magicTeleported(int type) {
        clanWars.leave(player, true);
    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public boolean logout() {
        return false;
    }
}