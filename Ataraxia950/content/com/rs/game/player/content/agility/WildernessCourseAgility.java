package com.rs.game.player.content.agility;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class WildernessCourseAgility {

    public static void climbCliff(final Player player, WorldObject object) {
        if (Utils.currentTimeMillis() - player.getLastAnimationEnd() < 600)
            return;
        if (player.getY() != 3939)
            return;
        player.lock(10);
        player.setNextAnimation(new Animation(3378));
        player.getPackets().sendGameMessage("You climb up the rock.", true);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                final WorldTile toTile = new WorldTile(2995, 3935, 0);
                if (Wilderness.isAtWild(player)) {
                    player.setNextWorldTile(toTile);
                    player.setNextAnimation(new Animation(-1));
                    int stage = getStage(player);
                    if (stage == 3) {
                        removeStage(player);
                        if (player.jujuPotions.isActive(Pots.Effects.PERFECT_AGILITY_JUJU) && ThreadLocalRandom.current().nextInt(4) == 0) {
                            player.addItem(new Item(39922, Utils.rand(10, 20)));
                            player.sendMessage("Through the power of juju you receive some skilling tickets!");
                        }
                        player.addLapsRan();
                        player.getContracts().recordAction(Skills.AGILITY, 3);
                        double extra = player.getAuraManager().isUsingSurefooted() ? 1.03 : 1;
                        player.getSkills().addXp(Skills.AGILITY, increasedExperience(player, 2000) * extra);
                        player.sendMessage("You've completed the Agility course; laps ran: " + Colors.RED + Utils.getFormattedNumber(player.getLapsRan()) + "</col>.", true);
                        player.getAchievements().updateProgress(1, AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
                        Agility.checkAgilityRandom(player);
                    }
                    player.unlock();
                }
            }
        }, 5);
    }

    public static void enterObstaclePipe(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 52))
            return;
        if (player.getY() == 3937 || player.getY() == 3938) {
            player.lock();
            player.faceObject(object);
            player.addWalkSteps(3004, 3938, -1, false);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.setNextAnimation(new Animation(10580));
                    stop();
                }
            }, 1);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(3004, 3944, 0));
                    }
                    player.setNextAnimation(new Animation(10580));
                    stop();
                }
            }, 3);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(3004, 3948, 0));
                    }
                    player.setNextAnimation(new Animation(10580));
                    stop();
                }
            }, 5);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.getSkills().addXp(Skills.AGILITY, increasedExperience(player, 220));
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(3004, 3949, 0));
                    }
                    setStage(player, 0);
                    player.unlock();
                    stop();
                }
            }, 6);
        }
    }

    public static int getStage(Player player) {
        Integer stage = (Integer) player.getTemporaryAttributtes().get("WildernessAgilityCourse");
        if (stage == null)
            return -1;
        return stage;
    }

    public static double increasedExperience(Player player, double totalXp) {
        if (Wilderness.isAtWild(player) && player.getEquipment().getGlovesId() == 13849)
            totalXp *= 1.030;
        return totalXp;
    }

    public static void removeStage(Player player) {
        player.getTemporaryAttributtes().remove("WildernessAgilityCourse");
    }

    public static void setStage(Player player, int stage) {
        player.getTemporaryAttributtes().put("WildernessAgilityCourse", stage);
    }

    public static void steppingStone(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 52))
            return;
        player.lock();
        player.faceObject(object);
        player.setNextWorldTile(new WorldTile(3001, 3960, 0));
        player.setNextAnimation(new Animation(741));
        WorldTasksManager.schedule(new WorldTask() {
            int jumpStone;

            @Override
            public void run() {
                if (jumpStone == 1) {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(3000, 3960, 0));
                    }
                    player.setNextAnimation(new Animation(741));
                }
                if (jumpStone == 2) {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(2999, 3960, 0));
                    }
                    player.setNextAnimation(new Animation(741));
                }
                if (jumpStone == 3) {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(2998, 3960, 0));
                    }
                    player.setNextAnimation(new Animation(741));
                }
                if (jumpStone == 4) {
                    if (Wilderness.isAtWild(player)) {
                        player.setNextWorldTile(new WorldTile(2997, 3960, 0));
                    }
                    player.setNextAnimation(new Animation(741));
                }
                if (jumpStone == 5) {
                    if (getStage(player) == 1)
                        setStage(player, 2);
                    player.setNextWorldTile(new WorldTile(2996, 3960, 0));
                    player.setNextAnimation(new Animation(741));
                    player.getSkills().addXp(Skills.AGILITY, increasedExperience(player, 220));
                    player.unlock();
                }
                if (jumpStone == 6) {
                    jumpStone = 0;
                    stop();
                }
                jumpStone++;
            }
        }, 0, 1);
    }

    public static void swingOnRopeSwing(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 52))
            return;
        if (player.getY() == 3958)
            return;
        player.lock(2);
        player.setNextAnimation(new Animation(751));
        World.sendObjectAnimation(player, object, new Animation(497));
        final WorldTile toTile = new WorldTile(object.getX(), 3958, object.getPlane());
        player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.NORTH));
        player.getSkills().addXp(Skills.AGILITY, increasedExperience(player, 220));
        player.getPackets().sendGameMessage("You skillfully swing across.", true);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (getStage(player) == 0)
                    setStage(player, 1);
                player.setNextWorldTile(toTile);
            }
        }, 1);
    }

    public static void walkAcrossLogBalance(final Player player) {
        if (!Agility.hasLevel(player, 52))
            return;
        player.lock();
        player.addWalkSteps(2994, 3945, -1, false);
        player.getPackets().sendGameMessage("You walk carefully across the balance log...", true);
        WorldTasksManager.schedule(new WorldTask() {
            boolean secondloop;

            @Override
            public void run() {
                if (!secondloop) {
                    secondloop = true;
                    player.getAppearence().setRenderEmote(155);
                } else {
                    if (getStage(player) == 2)
                        setStage(player, 3);
                    player.getAppearence().setRenderEmote(-1);
                    player.getSkills().addXp(Skills.AGILITY, increasedExperience(player, 220));
                    player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
                    player.unlock();
                    stop();
                }
            }
        }, 0, 5);
    }

    public static void walkBackGate(final Player player, WorldObject object) {
        player.faceObject(object);
        player.lock();
        player.getAppearence().setRenderEmote(155);
        player.addWalkSteps(2998, 3916, -1, false);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getAppearence().setRenderEmote(-1);
                player.getPackets().sendGameMessage("You made it safely to the other side.", true);
                player.unlock();
                stop();
            }
        }, 9);
    }

    public static void walkGate(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 52))
            return;
        player.faceObject(object);
        player.lock();
        player.getAppearence().setRenderEmote(155);
        player.addWalkSteps(2998, 3931, -1, false);
        player.getPackets().sendGameMessage("You go through the gate and try to edge over the ridge...", true);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getAppearence().setRenderEmote(-1);
                player.getPackets().sendGameMessage("You skillfully balance across the ridge.", true);
                player.unlock();
                stop();
            }
        }, 9);
    }
}