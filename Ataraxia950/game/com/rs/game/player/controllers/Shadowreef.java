package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonBoss;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.solak.Solak;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles the Dungeoneering controller.
 *
 * @author Noel
 */
public class Shadowreef extends Controller {


    public static void handleDrop(Player player, NPC npc) {
        switch (npc.getName().toLowerCase()) {
            case "crassian scout":
                handleReward(player, Utils.random(5, 30), Utils.random(10, 40));
                break;
            case "crassian scuttler":
                handleReward(player, Utils.random(15, 50), Utils.random(20, 80));
                break;
            case "zombie":
                handleReward(player, Utils.random(30, 60), Utils.random(25, 85));
                break;
            case "armoured zombie":
                handleReward(player, Utils.random(35, 65), Utils.random(30, 90));
                break;
            case "crassian warrior":
                handleReward(player, Utils.random(45, 75), Utils.random(35, 95));
                break;
            case "cloaked zealot":
                handleReward(player, Utils.random(55, 80), Utils.random(40, 100));
                break;
            case "sea horror":
                handleReward(player, Utils.random(120, 155), Utils.random(110, 155));
                break;
        }
    }

    /**
     * Used to handle NPC rewards in the dungeon.
     *
     * @param player The killer.
     * @param xp     The amount of exp to give.
     * @param tokens The amount of tokens to give.
     */
    private static void handleReward(Player player, int xp, int tokens) {
        int exp = (int) (xp * (player.isNovice() ? 0.70 : 1));
        player.dungKills++;
        player.getDungeoneeringManager().addTokens((int) (tokens * (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1.25 : 1)));
        player.getSkills().addXp(Skills.DUNGEONEERING, (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1.25 : 1) * exp * (Settings.DUNG_MODIFIER));
        player.getInterfaceManager().sendOverlay(10, false);
        player.getPackets().sendIComponentText(10, 2, String.valueOf(player.dungKills));
        player.getPackets().sendIComponentText(10, 1, "ShadowReef Kills:");
    }

    @Override
    public void start() {
        sendInterfaces();
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().sendOverlay(10, false);
        player.getPackets().sendIComponentText(10, 2, String.valueOf(player.dungKills));
        player.getPackets().sendIComponentText(10, 1,  "ShadowReef Kills:");
    }

    @Override
    public boolean login() {
        player.setNextWorldTile(new WorldTile(5414, 2339, 0));
        removeControler();//this is disabled for testing saves me running bk to solak all the time i can just spawn back when logging in
        return true;
    }

    @Override
    public boolean logout() {

        return false;
    }

    @Override
    public void magicTeleported(int teleType) {
        player.inDungeoneering = false;
        player.getInterfaceManager().closeOverlay(true);

        player.getControlerManager().forceStop();
    }

    private transient Shadowreef current;




    /**
     * Gets the required kills amount to enter next room.
     *
     * @return the amount as Integer.
     */
    private int getKcRequired() {
        if (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER))
            return 3;
        return 6;
    }
    private int getKcRequired1() {
        if (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER))
            return 1;
        return 2;
    }
    private int getKcRequired2() {
        if (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER))
            return 3;
        return 5;
    }

    /**
     * Handles Dungeoneering magical barriers.
     *
     * @param destX     The X coordinate to walk to.
     * @param destY     The Y coordinate to walk to.
     * @param requireKC if requires killcount to pass.
     */
    private void handleDoor(int destX, int destY, boolean requireKC, int requireLVL) {
        if (requireKC) {
            if (player.getSkills().getLevel(Skills.DUNGEONEERING) < requireLVL) {
                player.sendMessage("You'll need a Dungeoneering level of " + requireLVL + " to enter this room.");
                return;
            }
            if (player.dungKills < getKcRequired()) {
                player.sendMessage("You need " + Colors.RED + getKcRequired() + "</col> kills to enter this room; " + "you only have " + Colors.RED + player.dungKills + "</col>.");
                return;
            }
            player.dungKills = 0;
            sendInterfaces();
        }
        player.addWalkSteps(destX, destY, 2, false);
        player.lock(2);
    }
    private void handleDoor1(int destX, int destY, boolean requireKC, int requireLVL) {
        if (requireKC) {
            if (player.getSkills().getLevel(Skills.DUNGEONEERING) < requireLVL) {
                player.sendMessage("You'll need a Dungeoneering level of " + requireLVL + " to enter this room.");
                return;
            }
            if (player.dungKills < getKcRequired1()) {
                player.sendMessage("You need " + Colors.RED + getKcRequired1() + "</col> kills to enter this room; " + "you only have " + Colors.RED + player.dungKills + "</col>.");
                return;
            }
            player.dungKills = 0;
            sendInterfaces();
        }
        player.addWalkSteps(destX, destY, 2, false);
        player.lock(2);
    }
    private void handleDoor2(int destX, int destY, boolean requireKC, int requireLVL) {
        if (requireKC) {
            if (player.getSkills().getLevel(Skills.DUNGEONEERING) < requireLVL) {
                player.sendMessage("You'll need a Dungeoneering level of " + requireLVL + " to enter this room.");
                return;
            }
            if (player.dungKills < getKcRequired2()) {
                player.sendMessage("You need " + Colors.RED + getKcRequired2() + "</col> kills to enter this room; " + "you only have " + Colors.RED + player.dungKills + "</col>.");
                return;
            }
            player.dungKills = 0;
            sendInterfaces();
        }
        player.addWalkSteps(destX, destY, 2, false);
        player.lock(2);
    }

    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (object.getId() == 5992) {

            player.getBank().openBank();

            return false;
        }
        if (object.getId() == 11005) {
            player.lock(1);
            if (object.getX() == 3978 && object.getY() == 5552) {
                player.dungKills = 0;
                player.inDungeoneering = false;
                player.addWalkSteps(3977, 5552, 2, false);
                player.getControlerManager().forceStop();
                return false;
            }
            if (object.getX() == 5530 && object.getY() == 9098) {
                if (player.getY() >= 5529)
                    handleDoor(5531, 9098, true, 10);

                return false;
            }
            if (object.getX() == 5533 && object.getY() == 9071) {
                if (player.getX() <= 5534)
                    handleDoor(5531, 9071, true, 20);
                return false;
            }
            if (object.getX() == 5510 && object.getY() == 9062) {
                if (player.getY() <= 9063)
                    handleDoor(5510, 9061, true, 30);
                return false;
            }
            if (object.getX() == 5528 && object.getY() == 9052) {
                if (player.getX() <= 5527)
                    handleDoor1(5530, 9052, true, 40);
                return false;
            }
            if (object.getX() == 5519 && object.getY() == 9034) {
                if (player.getX() <= 5520)
                    handleDoor(5517, 9034, true, 50);
                return false;
            }
            if (object.getX() == 5491 && object.getY() == 9052) {
                if (player.getY() <= 9051)
                    handleDoor(5491, 9054, true, 60);
                return false;
            }
            if (object.getX() == 5496 && object.getY() == 9083) {
                if (player.getY() <= 9082)
                    handleDoor2(5496, 9085, true, 70);
                return false;
            }
            if (object.getX() == 5477 && object.getY() == 9096) {
                if (player.getX() <= 5478)
                    handleDoor(5475, 9096, true, 80);
                return false;
            }


        }
        return true;
    }

    @Override
    public boolean sendDeath() {
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                    player.sendMessage("Oh dear, you have died.");
                }
                if (loop == 3) {
                    player.setNextWorldTile(new WorldTile(5414, 2339, 0));
                    player.setNextAnimation(new Animation(-1));
                    player.getControlerManager().forceStop();
                    player.getPackets().sendMusicEffect(90);
                    player.dungKills = 0;
                    player.inDungeoneering = false;
                    player.reset();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public void forceClose() {
        player.getInterfaceManager().closeOverlay(false);
        player.dungKills = 0;
    }
}