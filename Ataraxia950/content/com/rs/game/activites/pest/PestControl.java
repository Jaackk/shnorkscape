package com.rs.game.activites.pest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.npc.NPC;
import com.rs.game.npc.pest.*;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.items.HerbloreBox;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.val;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PestControl {

    public boolean hardMode;
    private final static int[][] PORTAL_LOCATIONS = {{4, 56, 45, 21, 33}, {31, 28, 10, 9, 34}};
    private final static int[] KNIGHT_IDS = {3782, 3784, 3785};
    private int[] boundChunks;
    private final int[] pestCounts = new int[5];
    private final List<Player> team;
    private final PestPortal[] portals = new PestPortal[4];
    private PestPortal knight;
    private final PestData data;
    private final List<Brawler> brawlers;
    private final List<NPC> NPCS = new ArrayList<NPC>();
    private final List<PestControlObject> pestControlObjects;

    public PestControl(final List<Player> team, final PestData data) {
        this.team = Collections.synchronizedList(team);
        this.data = data;
        brawlers = new ArrayList<Brawler>();
        pestControlObjects = new ArrayList<PestControlObject>();
    }

    private boolean canFinish() {
        if (knight == null || knight.isDead()) {
            return true;
        }
        return getPortalsAliveCount() == 0;
    }

    public PestControl create() {
        final PestControl instance = this;

        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                if (team.size() >= 10 && data == PestData.VETERAN) {
                    hardMode = true;
                }
                boundChunks = MapBuilder.findEmptyChunkBound(8, 8);
                MapBuilder.copyAllPlanesMap(328, 320, boundChunks[0], boundChunks[1], 8);
                sendBeginningWave();
                new NPC(3781, getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))), -1, false);
                for (final Player player : team) {
                    player.getControlerManager().removeControlerWithoutCheck();
                    player.useStairs(-1, getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))), 1, 2);
                    player.getControlerManager().startControler("PestControlGame", instance);
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", 3781, "You must defend the Void Knight while the portals are unsummoned. The ritual takes twenty minutes though, so you can help out by destroying them yourselves!<br>Now Go GO GO!");
                }
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new PestGameTimer(), 1, 1);
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
            }
        });
        return instance;
    }

    public void addNPC(final NPC npc) {
        NPCS.add(npc);
    }

    public boolean containsNPC(final NPC npc) {
        return NPCS.contains(npc);
    }

    public boolean createPestNPC(final int index) {
        if (pestCounts[index] >= (index == 4 ? 4 : (portals[index] != null && portals[index].isLocked()) ? 5 : 15)) {
            return false;
        }
        pestCounts[index]++;
        final WorldTile baseTile = getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]);
        WorldTile teleTile = baseTile;
        final int npcId = index == 4 ? data.getShifters()[Utils.random(data.getShifters().length)]
                : data.getPests()[Utils.random(data.getPests().length)];

        final NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(npcId);
        for (int trycount = 0; trycount < 10; trycount++) {
            teleTile = new WorldTile(baseTile, 5);
            if (!teleTile.matches(baseTile)
                    && World.canMoveNPC(baseTile.getPlane(), teleTile.getX(), teleTile.getY(), defs.size)) {
                break;
            }
            teleTile = baseTile;
        }
        final String name = defs.name.toLowerCase();
  /*      if (data == PestData.VETERAN && !name.contains("spinner") && !name.contains("splatter")) {
            int chance = hardMode ? 5 : 10;
            if (ThreadLocalRandom.current().nextInt(chance) == 0) {
                if (ThreadLocalRandom.current().nextInt(3) == 0) {
                    new Spinner(npcId, teleTile, -1, index, this);
                } else {
                    new Splatter(npcId, teleTile, -1, index, this);
                }
                return true;
            }
        }*/
        if (name.contains("shifter")) {
            new Shifter(npcId, teleTile, -1, index, this);
        } else if (name.contains("splatter")) {
            new Splatter(npcId, teleTile, -1, index, this);
        } else if (name.contains("spinner")) {
            new Spinner(npcId, teleTile, -1, index, this);
        } else if (name.contains("brawler")) {
            brawlers.add(new Brawler(npcId, teleTile, -1, index, this));
        } else if (name.contains("ravager")) {
            new Ravager(npcId, teleTile, -1, index, this);
        } else if (name.contains("defiler")) {
            new Defiler(npcId, teleTile, -1, index, this);
        } else {
            new PestMonsters(npcId, teleTile, -1, index, this);
        }
        return true;
    }

    public void endGame() {
        final List<Player> team = new LinkedList<Player>();
        team.addAll(this.team);
        NPCS.forEach(npc -> {
            if (npc != null && !npc.hasFinished() && !npc.isDead()) {
                npc.finish();
            }
        });
        NPCS.clear();
        this.team.clear();
        for (final Player player : team) {
            if(player.getControlerManager().getControler() instanceof PestControlGame) {
                final int knightZeal = (int) ((PestControlGame) player.getControlerManager().getControler()).getPoints();
                leave(player, false);
                player.getControlerManager().removeControlerWithoutCheck();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        sendFinalReward(player, knightZeal);
                    }
                }, 1);
            }
        }
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                MapBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, 6000, TimeUnit.MILLISECONDS);
    }

    public NPC getKnight() {
        return knight;
    }

    public int[] getPestCounts() {
        return pestCounts;
    }

    public PestData getPestData() {
        return data;
    }

    public List<Player> getPlayers() {
        return team;
    }

    public int getPortalsAliveCount() {
        return Arrays.stream(portals).filter(p -> !p.isDead() && !p.hasFinished()).toArray().length;
    }

    public PestPortal[] getPortals() {
        return portals;
    }

    public List<Brawler> getBrawlers() {
        return brawlers;
    }

    public List<PestControlObject> getPestControlObjects() {
        return pestControlObjects;
    }

    public WorldTile getWorldTile(final int mapX, final int mapY) {
        return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
    }

    private void sendBeginningWave() {
        knight = new PestPortal(KNIGHT_IDS[Utils.random(KNIGHT_IDS.length)], true,
                getWorldTile(PORTAL_LOCATIONS[0][4], PORTAL_LOCATIONS[1][4]), this, true);
        knight.unlock();
        knight.setLocked(false);
        knight.setHitpoints(knight.getMaxHitpoints());
        for (int index = 0; index < portals.length; index++) {
            final PestPortal portal = portals[index] = new PestPortal(6146 + index, true,
                    getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]), this, false);
            portal.setHitpoints(portal.getMaxHitpoints());
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                final List<WorldObject> objects = World.getRegion(knight.getRegionId()).getAllObjects();
                if (objects == null) {
                    return;
                }
                for (final WorldObject object : objects) {
                    if (object == null) {
                        continue;
                    }
                    if (object.getId() == 91332 || (object.getId() >= 14224 && object.getId() <= 14226)) {
                        pestControlObjects.add(new PestControlObject(object));
                        World.removeObject(object);
                        World.spawnObject(new PestControlObject(object));
                    } else if (object.getId() >= 14227 && object.getId() <= 14229) {
                        object.setId(object.getId() - 3);
                        pestControlObjects.add(new PestControlObject(object));
                        World.removeObject(object);
                        World.spawnObject(new PestControlObject(object));
                    }
                }
                stop();
            }
        }, 1, 1);
    }

    public void leave(final Player player, final boolean logout) {
        team.remove(player);
        final WorldTile outSide = Lander.getLanders()[data.ordinal()].getLanderRequirement().getExitTile();
        if (logout) {
            player.setLocation(outSide);
        } else {
            player.useStairs(-1, outSide, 1, 2);
        }
        player.setForceMultiArea(false);
        player.getInterfaceManager().removeMinigameHudInterface();
        player.getTemporaryAttributtes().put("autocast", Boolean.FALSE);
        //player.reset();
        resetPlayer(player); //<-- using a custom one since players should keep their spell selection, boosts and prayer active:)
    }

    private static void resetPlayer(Player player){
        player.setRunEnergy(100);
        player.removeDamage(player);
        player.getCombatDefinitions().resetSpecialAttack();
        player.getPrayer().restorePrayer(player.getSkills().getLevelForXp(Skills.PRAYER) * 10);
        player.setHitpoints(player.getMaxHitpoints());
        player.refreshHitPoints();
        player.getReceivedHits().clear();
        player.resetCombat();
        player.getWalkSteps().clear();
        player.getPoison().reset();
        player.resetReceivedDamage();
        player.getAppearence().generateAppearenceData();
    }

    private void sendFinalReward(final Player player, final int knightZeal) {
        if (knight.isDead()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You failed to protect the void knight and have not been awarded.");
        } else if (knightZeal < 250) {
            player.getDialogueManager().startDialogue("SimpleMessage", "The knights noticed your lack of activity and have chosen not to award you with anything.");
        } else {
            int coinsAmount = data.getCoins();
            final int pointsAmount = data.getPoints() * Settings.PC_MODIFIER * (player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) ? 2 : 1);
            player.getDialogueManager().startDialogue("SimpleMessage", "Congratulations! You have successfully kept the lander safe and have been awarded " + Colors.DARK_RED + Utils.getFormattedNumber(coinsAmount) + "</col> gold coins and " + Colors.DARK_RED + pointsAmount + "</col> commendation points. See the chatbox for additional rewards.");
            player.addMoney(coinsAmount);
            player.setPestPoints(player.getPestPoints() + pointsAmount);
            player.setPestControlGames(player.getPestControlGames() + 1);
            data.giveRewards(player);
            player.sendMessage("Total games played: " + Colors.RED + Utils.getFormattedNumber(player.getPestControlGames()) + "</col>; " + " Total commendation: " + Colors.RED + Utils.getFormattedNumber(player.getPestPoints()) + "</col>.");
//            HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.PEST_CONTROL);
            player.getAchievements().updateProgress(1, AchievementList.WIN_10_PEST_CONTROL_GAMES);
            ChristmasSeasonalEvent.awardSmallPresent(player);
        }
    }

    private void sendPortalInterfaces() {
		/*for (Player player : team) {
			for (int i = 13; i < 17; i++) {
				PestPortal npc = portals[i - 13];
				if (npc != null)
					player.getPackets().sendIComponentText(408, i,
							npc.isDead() || npc.hasFinished() ? "0" : npc.getHitpoints() + "");
				player.getPackets().sendHideIComponent(408, 17 + ((i - 13) * 2), !npc.isLocked());
			}
			player.getPackets().sendIComponentText(408, 1, "" + knight.getHitpoints());
		}*/

        for (int index = team.size() - 1; index >= 0; index--) {
            final Player player = team.get(index);
            for (int count = 0; count < portals.length; count++) {
                final PestPortal portal = portals[count];
                if (portal != null) {
                    player.getPackets().sendIComponentText(408, count + 9, portal.getHitpoints() + "");
                    if (portal.isDead() || !portal.isLocked()) {
                        player.getPackets().sendHideIComponent(408, count + (portal.isDead() ? 25 : 13), false);
                        player.getPackets().sendHideIComponent(408, count + 21, true);
                    }
                }
            }
            player.getPackets().sendIComponentText(408, 5, "" + knight.getHitpoints());
            //player.getPackets().sendIComponentText(408, 4, timer.seconds / 60 + " min");
        }
    }

    public void sendTeamMessage(final String message) {
        for (final Player player : team) {
            player.sendMessage(message);
        }
    }

    public void unlockPortal() {
        if (getPortalsAliveCount() == 0) {
            return;
        }
        final List<PestPortal> alivePortals = Arrays.asList(portals).stream()
                .filter(p -> !p.isDead() && !p.hasFinished() && p.isLocked()).collect(Collectors.toList());
        if (alivePortals.isEmpty()) {
            return;
        }
        final int random = Utils.random(alivePortals.size());
        final PestPortal portal = alivePortals.get(random);
        portal.unlock();
        sendPortalInterfaces();
    }

    private void updateTime(final int seconds) {
        int minutes = seconds / 60;
        int secs = minutes <= 1 ? seconds : seconds % 60;
        for (final Player player : team) {
            player.getPackets().sendIComponentText(408, 4, minutes + "m" + secs + "s left");
        }
    }

    public static final ImmutableList<Integer> SECONDARIES =
            ImmutableList.of(222, 226, 240, 246, 3139, 4698, 26779, 5973);

    public enum PestData {

        NOVICE(new int[]{
                /* Shifters */3732, 3733, 3734, 3735,
                /* Ravagers */3742, 3743, 3744,
                /* Brawler */3772, 3773, 3775,
                /* Splatter */3727, 3728, 3729,
                /* Spinner */3747, 3748, 3749,
                /* Torcher */3752, 3753, 3754, 3755,
                /* Defiler */3762, 3763, 3764, 3765},
                new int[]{3732, 3733, 3734, 3735}, 8,
                50_000),

        INTERMEDIATE(new int[]{
                /* Shifters */3734, 3735, 3736, 3737, 3738, 3739,
                /* Ravagers */3744, 3743, 3745,
                /* Brawler */3773, 3775, 3776,
                /* Splatter */3728, 3729, 3730,
                /* Spinner */3748, 3749, 3750, 3751,
                /* Torcher */3754, 3755, 3756, 3757, 3758, 3759,
                /* Defiler */ 3764, 3765, 3766, 3767, 3768, 3769},
                new int[]{3734, 3735, 3736, 3737, 3738, 3739}, 16,
                100_000),

        VETERAN(new int[]{
                /* Shifters */3738, 3739, 3740, 3741,
                /* Ravagers */ 3744, 3745, 3746,
                /* Brawler */3772, 3773, 3774, 3775, 3776,
                /* Splatter */3729, 3730, 3731,
                /* Spinner */3749, 3750, 3751,
                /* Torcher */3758, 3759, 3760, 3761,
                /* Defiler */3768, 3769, 3770, 3771},
                new int[]{3736, 3737, 3738, 3739, 3740, 3741}, 24,
                200_000,
                new PestControlReward(() -> {
                    val id = Utils.randomFrom(HerbloreBox.RARES).getId();
                    val def = ItemDefinitions.getItemDefinitions(id);
                    return def.isStackable() ? id : def.certId;
                },10, 30, 2, true), // Random herbs!
                new PestControlReward(() -> Utils.randomFrom(SECONDARIES), 10, 30, 2, true), // Random secondaries!
                new PestControlReward(28257, 5, 15, 2, true), // Wine of saradomin

               /* new PestControlReward(() -> {
                    val id = Utils.randomFrom(ProteanReward.VALUES).getId();
                    val def = ItemDefinitions.getItemDefinitions(id);
                    return def.isStackable() ? id : def.certId;
                }, 5, 10, 2, false), // Random protean items!

                */
                new PestControlReward(1626, 110, 125, 1, false), // Opal.
                new PestControlReward(1628, 95, 110, 1, false), // Jade.
                new PestControlReward(1630, 80, 95, 1, false), // Red topaz.
                new PestControlReward(1624, 65, 80, 1, false), // Sapphire.
                new PestControlReward(1622, 50, 65, 1, false), // Emerald.
                new PestControlReward(1620, 35, 50, 1, false), // Ruby.
                new PestControlReward(1618, 20, 35, 2, false), // Diamond.
                new PestControlReward(1632, 5, 20, 4, false), // Dragonstone.
                new PestControlReward(6572, 1, 16, false)); // Onyx.
        private final int[] pests;
        private final int[] shifters;
        private final int points;
        private final int coins;
        private final PestControlReward[] rewards;
        private final List<PestControlReward> rewardView;

        PestData(final int[] pests, final int[] shifters, final int points, int coins, PestControlReward... rewards) {
            this.pests = pests;
            this.shifters = shifters;
            this.points = points;
            this.coins = coins;
            this.rewards = rewards;
            rewardView = Arrays.asList(rewards);
        }

        public int[] getPests() {
            return pests;
        }

        public int getPoints() {
            return points;
        }

        public int getCoins() {
            return coins;
        }

        public void giveRewards(Player player, boolean debug) {
            if (this == NOVICE || this == INTERMEDIATE) {
                player.sendFilteredMessage("Join the Veteran lander to receive additional loot!");
                return;
            }
            boolean added = false;
            LinkedList<PestControlReward> rollList = new LinkedList<>(rewardView);
            Iterator<PestControlReward> it = rollList.iterator();
            while (it.hasNext()) {
                PestControlReward reward = it.next();
                if (reward.isAlwaysRoll() && reward.roll(player, debug)) {
                    added = true;
                    it.remove();
                }
            }
            if (rollList.size() > 0) {
                if (rollList.size() > 1)
                    Collections.shuffle(rollList);
                int rolls = PestControlReward.DROP_TABLE_ROLLS;
                if (player.getPerkManager().hasPerkActive(DonationPerk.THE_EXTERMINATOR))
                    rolls *= 2;
                for (int i = 0; i < rolls; i++) {
                    if (rollList.isEmpty()) {
                        break;
                    }
                    if (rollList.removeFirst().roll(player, debug)) {
                        added = true;
                    }
                }
            }
            if (!added) {
                player.sendMessage("You didn't receive any additional loot. Better luck next time!");
            }
        }

        public void giveRewards(Player player) {
            giveRewards(player, false);
        }

        public int[] getShifters() {
            return shifters;
        }

        public List<PestControlReward> getRewardView() {
            return rewardView;
        }
    }

    private class PestGameTimer extends FixedLengthRunnable {
        private int seconds = 1200;
        private int lastUnlock;

        private int nextMagicAttack = getNextMagicAttack();

        private int getNextMagicAttack() {
            return Utils.random(20, 35);
        }

        private Range<Integer> getMagicDamage() {
            return Range.closed(10, 25);
        }

        @Override
        public boolean repeat() {
            try {
                if (team.size() == 0) {
                    endGame();
                }
                updateTime(seconds);
                if (seconds == 0 || canFinish()) {
                    endGame();
                    return false;
                }
                if (seconds != 1200 && seconds % 15 == 0) {
                    if (lastUnlock-- == 0) {
                        unlockPortal();
                        lastUnlock = 1;
                    }
                }
                if (data == PestData.VETERAN) {
                    if (nextMagicAttack == 5) {
                        for (Player player : team) {
                            if (player == null || player.hasFinished() || !player.isActive()) {
                                continue;
                            }
                            player.sendFilteredMessage(Colors.RED + "The portals are preparing a magical attack on you!");
                            player.setNextGraphics(new Graphics(482));
                        }
                    }
                    if (nextMagicAttack == 0) {
                        for (Player player : team) {
                            if (player == null || player.hasFinished() || !player.isActive()) {
                                continue;
                            }
                            player.setNextGraphics(new Graphics(453));
                            if (!player.prayer.isMageProtecting()) {
                                player.sendMessage(Colors.RED + "You were unable to resist the magical attack!");
                                player.applyHit(new Hit(player, Utils.random(getMagicDamage()), Hit.HitLook.MAGIC_DAMAGE));
                            }
                        }
                        nextMagicAttack = getNextMagicAttack();
                    }
                    if (nextMagicAttack > 0) {
                        nextMagicAttack--;
                    }
                }
                if (seconds % 2 == 0) {
                    sendPortalInterfaces();
                }
                seconds--;
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
            }
            return true;
        }
    }
}
