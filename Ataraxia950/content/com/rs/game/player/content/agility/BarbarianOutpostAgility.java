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
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class BarbarianOutpostAgility {

    public static void climbObstacleNet(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 30) || player.getY() < 3545 || player.getY() > 3547)
            return;

        player.faceObject(object);
        player.useStairs(828, new WorldTile(object.getX() - 1, player.getY(), 1), 1, 2);
        player.getPackets().sendGameMessage("You climb the netting...", true);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getSkills().addXp(Skills.AGILITY, 80);
                if (getStage(player) == 1)
                    setStage(player, 2);
                stop();
            }

        }, 1);
    }

    public static void climbOverCrumblingWall(final Player player, WorldObject object) {
        if (Utils.currentTimeMillis() - player.getLastAnimationEnd() < 600)
            return;
        if (!Agility.hasLevel(player, 30))
            return;
        if (player.getX() >= object.getX()) {
            player.getPackets().sendGameMessage("You cannot climb that from this side.");
            return;
        }
        player.lock();
        player.getPackets().sendGameMessage("You climb the low wall...", true);
        player.setNextAnimation(new Animation(4853));
        final WorldTile toTile = new WorldTile(object.getX() + 1, object.getY(), object.getPlane());
        player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, ForceMovement.EAST));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextWorldTile(toTile);
                player.getSkills().addXp(Skills.AGILITY, 80);
                int stage = getStage(player);
                if (stage == 3) // only second wall for xp bonus
                    setStage(player, 4);
                if (stage == 4) {
                    removeStage(player);
                    player.addLapsRan();
                    player.getContracts().recordAction(Skills.AGILITY, 2);
                    double extra = player.getAuraManager().isUsingSurefooted() ? 1.03 : 1;
                    player.getSkills().addXp(Skills.AGILITY, 300 * extra);
                    if (player.jujuPotions.isActive(Pots.Effects.PERFECT_AGILITY_JUJU) && ThreadLocalRandom.current().nextInt(4) == 0) {
                        player.addItem(new Item(39922, Utils.rand(2, 5)));
                        player.sendMessage("Through the power of juju you receive some skilling tickets!");
                    }
                    player.sendMessage("You've completed the Agility course; laps ran: " + Colors.RED + Utils.getFormattedNumber(player.getLapsRan()) + "</col>.", true);
                    player.getAchievements().updateProgress(1, AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
                    Agility.checkAgilityRandom(player);
                }
                player.unlock();
                stop();
            }
        }, 1);
    }

    public static void climbUpWall(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 90))
            return;
        player.lock();
        player.useStairs(10023, new WorldTile(2536, 3546, 3), 2, 3);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextAnimation(new Animation(11794));
                player.getSkills().addXp(Skills.AGILITY, 80);
                if (getStage(player) == 2)
                    setStage(player, 3);
                player.unlock();
                stop();
            }
        }, 1);
    }

    public static void crossBalanceBeam(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 90))
            return;
        if (player.getX() > 2534 || player.getY() < 3552)
            return;
        player.lock();
        final WorldTile toTile = new WorldTile(2536, 3553, 3);
        player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.EAST));
        player.setNextAnimation(new Animation(16079));
        player.getAppearence().setRenderEmote(330);

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextWorldTile(toTile);
                player.getSkills().addXp(Skills.AGILITY, 80);
                if (getStage(player) == 4)
                    setStage(player, 5);
                player.setNextAnimation(new Animation(-1));
                player.unlock();
                stop();
            }
        }, 2);
    }

    public static void enterObstaclePipe(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 30))
            return;
        player.lock();
        player.setNextAnimation(new Animation(10580));
        final WorldTile toTile = new WorldTile(object.getX(), player.getY() >= 3561 ? 3558 : 3561, object.getPlane());
        player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, player.getY() >= 3561 ? ForceMovement.SOUTH : ForceMovement.NORTH));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextWorldTile(toTile);
                player.unlock();
                stop();
            }
        }, 1);
    }

    public static void fireSpringDevice(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 90))
            return;
        if (player.getY() > 3548)
            return;
        player.lock();
        player.addWalkSteps(2533, 3547, -1, false);
        WorldTasksManager.schedule(new WorldTask() {

            boolean secondLoop;

            @Override
            public void run() {
                final WorldTile toTile = new WorldTile(2532, 3553, 3);
                if (!secondLoop) {
                    player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.NORTH));
                    player.setNextAnimation(new Animation(4189));
                    World.sendObjectAnimation(player, object, new Animation(11819));
                    secondLoop = true;
                } else {
                    player.setNextWorldTile(toTile);
                    player.getSkills().addXp(Skills.AGILITY, 80);
                    if (getStage(player) == 3)
                        setStage(player, 4);
                    player.unlock();
                    stop();
                }
            }

        }, 1, 1);
    }

    public static int getStage(Player player) {
        Integer stage = (Integer) player.getTemporaryAttributtes().get("BarbarianOutpostCourse");
        if (stage == null)
            return -1;
        return stage;
    }

    public static void jumpOverGap(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 90) || player.getY() < 3552)
            return;
        player.lock();
        player.setNextAnimation(new Animation(2586));
        player.getAppearence().setRenderEmote(-1);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(new WorldTile(2538, 3553, 2));
                player.setNextAnimation(new Animation(2588));
                player.getSkills().addXp(Skills.AGILITY, 80);
                if (getStage(player) == 5)
                    setStage(player, 6);
                player.unlock();
                stop();
            }
        }, 0);
    }

    public static void removeStage(Player player) {
        player.getTemporaryAttributtes().remove("BarbarianOutpostCourse");
    }

    public static void runUpWall(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 90))
            return;
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {

            boolean secondLoop;

            @Override
            public void run() {
                final WorldTile toTile = new WorldTile(2538, 3545, 2);
                if (!secondLoop) {
                    player.setNextForceMovement(new ForceMovement(player, 7, toTile, 8, ForceMovement.NORTH));
                    player.setNextAnimation(new Animation(10492));
                    secondLoop = true;
                } else {
                    player.setNextAnimation(new Animation(10493));
                    player.setNextWorldTile(toTile);
                    player.getSkills().addXp(Skills.AGILITY, 80);
                    if (getStage(player) == 1)
                        setStage(player, 2);
                    player.unlock();
                    stop();
                }
            }
        }, 1, 6);
    }

    public static void setStage(Player player, int stage) {
        player.getTemporaryAttributtes().put("BarbarianOutpostCourse", stage);
    }

    public static void slideDownRoof(final Player player, final WorldObject object) {
        if (Utils.currentTimeMillis() - player.getLastAnimationEnd() < 600)
            return;
        if (!Agility.hasLevel(player, 90))
            return;
        if (player.getY() < 3551)
            return;
        player.lock();
        player.setNextAnimation(new Animation(11792));
        final WorldTile toTile = new WorldTile(2544, player.getY(), 0);
        player.setNextForceMovement(new ForceMovement(player, 0, toTile, 5, ForceMovement.EAST));

        WorldTasksManager.schedule(new WorldTask() {
            int stage;

            @Override
            public void run() {
                if (stage == 0) {
                    player.setNextWorldTile(new WorldTile(2541, player.getY(), 1));
                    player.setNextAnimation(new Animation(11790));
                    stage = 1;
                } else if (stage == 1) {
                    stage = 2;
                } else if (stage == 2) {
                    player.setNextAnimation(new Animation(11791));
                    stage = 3;
                } else if (stage == 3) {
                    player.setNextWorldTile(toTile);
                    player.setNextAnimation(new Animation(2588));
                    player.getSkills().addXp(Skills.AGILITY, 80);
                    if (getStage(player) == 6)
                        setStage(player, 7);
                    if (getStage(player) == 7) {
                        removeStage(player);
                        double extra = player.getAuraManager().isUsingSurefooted() ? 1.03 : 1;
                        player.getSkills().addXp(Skills.AGILITY, 1415 * extra);
                        player.addLapsRan();
                        player.getContracts().recordAction(Skills.AGILITY, 2);
                        if (player.jujuPotions.isActive(Pots.Effects.PERFECT_AGILITY_JUJU) && ThreadLocalRandom.current().nextInt(4) == 0) {
                            player.addItem(new Item(39922, Utils.rand(4, 6)));
                            player.sendMessage("Through the power of juju you receive some skilling tickets!");
                        }
                        player.sendMessage("You've completed the Agility course; laps ran: " + Colors.RED + Utils.getFormattedNumber(player.getLapsRan()) + "</col>.", true);
                        player.getAchievements().updateProgress(1, AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
                        Agility.checkAgilityRandom(player);
                        player.sendMessage("TimeDif: " + (Utils.currentTimeMillis() - player.getLastAnimationEnd()));
                    }
                    player.unlock();
                    stop();
                }
            }
        }, 0, 0);
    }

    public static void swingOnRopeSwing(final Player player, WorldObject object) {
        if (!Agility.hasLevel(player, 30))
            return;
        if (player.getY() != 3556) {
            player.sendMessage("You'll need to get closer to make this jump.");
            return;
        }
        if (player.getY() != 3556 && player.getX() == object.getX())
            player.addWalkSteps(object.getX(), 3556);

        player.lock();
        player.addWalkSteps(player.getX(), player.getY() - 2);
        WorldTile toTile = new WorldTile(object.getX(), 3549, object.getPlane());
        WorldTasksManager.schedule(new WorldTask() {

            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks == 1) {
                    player.setNextAnimation(new Animation(751));
                    World.sendObjectAnimation(player, object, new Animation(497));
                    player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.SOUTH));
                } else if (ticks == 2) {
                    player.getSkills().addXp(Skills.AGILITY, 80);
                    player.sendMessage("You skillfully swing across.", true);
                    player.setNextWorldTile(toTile);
                    player.unlock();
                    setStage(player, 0);
                    stop();
                }
            }

        }, 0, 1);
    }

    public static void walkAcrossBalancingLedge(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 30))
            return;
        if (player.getX() < 2536)
            return;
        player.sendMessage("You put your foot on the ledge and try to edge across...", true);
        player.lock();
        player.setNextAnimation(new Animation(753));
        player.getAppearence().setRenderEmote(157);
        final WorldTile toTile = new WorldTile(2532, object.getY(), object.getPlane());
        player.addWalkSteps(toTile.getX(), toTile.getY(), -1, false);

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextAnimation(new Animation(759));
                player.getAppearence().setRenderEmote(-1);
                player.getSkills().addXp(Skills.AGILITY, 80);
                player.sendMessage("You skillfully edge across the gap.", true);
                if (getStage(player) == 2)
                    setStage(player, 3);
                player.unlock();
                stop();
            }
        }, 3);
    }

    public static void walkAcrossLogBalance(final Player player, final WorldObject object) {
        if (!Agility.hasLevel(player, 30))
            return;
        player.addWalkSteps(2551, 3546, -1, false);
        player.lock();
        if (player.getY() != object.getY()) {
            player.faceObject(object);
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    walkAcrossLogBalanceEnd(player, object);
                    stop();
                }

            }, 1);
        } else
            walkAcrossLogBalanceEnd(player, object);

        player.unlock();
    }

    private static void walkAcrossLogBalanceEnd(final Player player, WorldObject object) {
        player.sendMessage("You walk carefully across the slippery log...", true);
        player.lock();
        player.setNextAnimation(new Animation(9908));
        final WorldTile toTile = new WorldTile(2541, object.getY(), object.getPlane());
        player.setNextForceMovement(new ForceMovement(player, 0, toTile, 16, ForceMovement.WEST));

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextWorldTile(toTile);
                player.getSkills().addXp(Skills.AGILITY, 80);
                player.sendMessage("... and make it safely to the other side.", true);
                if (getStage(player) == 0)
                    setStage(player, 1);
                player.unlock();
                stop();
            }
        }, 15);
    }
}