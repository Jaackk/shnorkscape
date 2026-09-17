package com.rs.game.map.bossInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstance;
import com.rs.game.activites.dungeon_architect.DungeonArchitectInstance;
import com.rs.game.activites.quest.deathsbounty.SoulFightInstance;
import com.rs.game.map.bossInstance.impl.*;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.player.Player;
import com.rs.game.player.content.death.DeathInstance;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class BossInstanceHandler {

    // to make sure no issues
    public static final Object LOCK = new Object();

    public static void enterInstance(Player player, Boss boss) {
        player.getDialogueManager().startDialogue("BossInstanceD", boss);
    }

    private static void createInstance(Player player, Boss boss, int maxPlayers, int minCombat, int spawnSpeed, int protection, boolean practiseMode, boolean hardMode) {
        createInstance(player, new InstanceSettings(boss, maxPlayers, minCombat, spawnSpeed, protection, practiseMode, hardMode));
    }

    public static void createInstance(Player player, InstanceSettings settings) {
        synchronized (LOCK) {

            try {
                String key = player == null ? "" : player.getUsername();
                BossInstance instance = findInstance(settings.getBoss(), key);
                if (instance != null) {
                    settings.getBoss().cachedInstances.remove(key);
                }
                // if (instance == null) {
                if (player == null && !settings.getBoss().publicVersion) {
                    if (Settings.DEBUG)
                        Logger.getGlobal().warn("Not a public instance. Can't create it.");
                    return;
                }
                settings.setCreationTime(Utils.currentTimeMillis());
                instance = settings.getBoss().instance.getDeclaredConstructor(Player.class, InstanceSettings.class).newInstance(player, settings);
                settings.getBoss().cachedInstances.put(key, instance);
                
                /*
                 * //} else {// recreating the instance but not gonna replace // settings since already exists(instead, increase time)
                 * settings.setCreationTime(Utils.currentTimeMillis()); joinInstance(player, settings.getBoss(), key, false); // enter // the // instance // normally
                 * }
                 */
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }

        }
    }

    /*
     * login means reloging in a public instance(u cant login into private, but lets keep this in case rs lets u in future)
     */
    public static BossInstance joinInstance(Player player, Boss boss, String key, boolean login) {
        synchronized (LOCK) {

            BossInstance instance = findInstance(boss, key);
            if (instance == null) { // not username
                Player owner = World.getPlayerByDisplayName(key);
                if (owner != null) {
                    key = owner.getUsername();
                    instance = findInstance(boss, key);
                }
            }

            if (instance == null) {
                if (key.equals("")) { // supposed to be public instance
                    player.getPackets().sendGameMessage("This boss has no public instance.");
                    return null;
                }
                player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
                return null;
            }
            // loading
            if (!instance.isInstanceReady())
                return null;
            if (!key.equals("") && !player.getUsername().equals(key)) {
                if (instance.getSettings().getMinCombat() > player.getSkills().getCombatLevelWithSummoning()) {
                    player.getPackets().sendGameMessage("Your combat level is too low to enter this session.");
                    return null;
                }
                if (instance.getSettings().getProtection() == BossInstance.FRIENDS_ONLY) {
                    Player owner = World.getPlayer(key);
                    String formatted = Utils.formatPlayerNameForProtocol(player.getDisplayName());
                    if (owner == null || !owner.getFriendsIgnores().getFriends().contains(formatted)) {
                        player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
                        return null;
                    }
                }
                if (instance.getSettings().getMaxPlayers() - 1 <= instance.getPlayersCount()) {
                    player.getPackets().sendGameMessage("This instance is full.");
                    return null;
                }
            }
            instance.enterInstance(player, login);
            return instance;
        }
    }

    public static int getCount(Boss boss) {
        synchronized (LOCK) {
            return boss.cachedInstances.size();
        }
    }

    public static BossInstance findInstance(Boss boss, String key) {
        synchronized (LOCK) {
            return boss.cachedInstances.get(key);
        }
    }

    public static boolean inInstance(Boss boss, Player player) {
        synchronized (LOCK) {
            synchronized (boss.cachedInstances) {
                for (BossInstance instance : boss.cachedInstances.values()) {
                    if (instance.isPlayerInside(player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final void init() {
        for (Boss boss : Boss.values()) {
            if (!boss.publicVersion)
                continue;
            try {
                createInstance(null, boss, boss.maxPlayers, 1, BossInstance.STANDARD, BossInstance.FFA, false, false);
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public enum Boss {
        King_Black_Dragon(KingBlackDragonInstance.class, 1250000, 200, false, true, new WorldTile(3051, 3520, 0), new WorldTile(2273, 4681, 0), null, "BossInstanceController", 14),
        Kalphite_Queen(KalphiteQueenInstance.class, 1000000, 200, false, true, new WorldTile(3446, 9496, 0), new WorldTile(3508, 9494, 0), null, "KalphiteQueenInstanceController", 212),
        Dagannoth_Kings(DagannothKingsInstance.class, 1500000, 200, false, true, new WorldTile(1912, 4367, 0), new WorldTile(2900, 4449, 0), null, "DagannothKingsInstanceController", 365),
        Corporeal_Beast(CorporealBeastInstance.class, 3000000, 200, false, true, new WorldTile(2970, 4384, 2), new WorldTile(2974, 4384, 2), null, "CorporealBeastInstanceController", 617),

        Vorago(VoragoInstance.class, 3000000, 50, true, true, new WorldTile(2972, 3431, 0), new WorldTile(3043, 6100, 0), new WorldTile(3072, 6176, 0), "VoragoInstanceController", 1155),
        Kalphite_King(KalphiteKingInstance.class, 0, 200, false, false, new WorldTile(2971, 1656, 0), new WorldTile(2974, 1746, 0), new WorldTile(2947, 1645, 0), "KalphiteKingInstanceController", 212),
        Telos(TelosInstance.class, 0, 1, false, true, new WorldTile(3821, 7048, 0), new WorldTile(3814, 7053, 0), null, "TelosInstanceController", -1),
        Evil_Tree(EvilTreeInstance.class, 0, 4, false, false, new WorldTile(4128, 5847, 0), new WorldTile(2611, 4775, 0), new WorldTile(4128, 5849, 0), "EvilTreeInstanceController", -1),
        Husband_Mich(SoulFightInstance.class, 0, 1, false, false, new WorldTile(1813, 5982, 0), new WorldTile(3419, 5272, 0), null, "SoulFightInstanceController", -1),
        Dungeon_Architect(DungeonArchitectInstance.class, 0, 2, false, false, new WorldTile(3971, 5561, 0), new WorldTile(1376, 5911, 0), null, "DungeonArchitectController", -1),
        Spider_Boss(SpiderBossInstance.class, 200000, 2, false, true, new WorldTile(4486, 6266, 1), new WorldTile(4512, 6289, 1), new WorldTile(4712, 6231, 1), "SpiderBossInstanceController", 1224),
        Death(DeathInstance.class, 0, 1, false, false, new WorldTile(4123, 5855, 0), new WorldTile(3419, 5270, 0), null, "DeathController", -1),
        LEGIO_PRIMUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1025, 632, 1), new WorldTile(1023, 632, 1), null, "LegiosInstanceController", -1),
        LEGIO_SECUNDUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1106, 671, 1), new WorldTile(1106, 673, 1), null, "LegiosInstanceController", -1),
        LEGIO_TERTIUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1099, 665, 1), new WorldTile(1099, 663, 1), null, "LegiosInstanceController", -1),
        LEGIO_QUARTUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1176, 633, 1), new WorldTile(1174, 633, 1), null, "LegiosInstanceController", -1),
        LEGIO_QUINTUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1191, 634, 1), new WorldTile(1193, 634, 1), null, "LegiosInstanceController", -1),
        LEGIO_SEXTUS(LegiosInstance.class, 0, 1, false, false, new WorldTile(1184, 622, 1), new WorldTile(1184, 620, 1), null, "LegiosInstanceController", -1),
        THE_MAGISTER(TheMagisterInstance.class, 0, 1, false, false, new WorldTile(2464, 6729, 1), new WorldTile(2207, 6858, 0), null, "TheMagisterInstanceController", -1),
        PZ_BOSS(PZInstance.class, 0, 1, false, false, new WorldTile(4128, 5847, 0), new WorldTile(3039, 6047, 0), null, "PZInstanceController", -1)
        ;
        private final Map<String, BossInstance> cachedInstances = Collections.synchronizedMap(new HashMap<String, BossInstance>());
        private final Class<? extends BossInstance> instance;
        private final int initialCost;
        private final int maxPlayers;
        private final int musicId;
        private final boolean hasHM;
        private final boolean publicVersion;
        private final WorldTile insideTile;
        private final WorldTile outsideTile;
        private final WorldTile graveStoneTile;
        private final String controllerName;

        // hasHM = hasHardMode
        Boss(Class<? extends BossInstance> instance, int initialCost, int maxPlayers, boolean hasHM, boolean publicVersion, WorldTile outsideTile, WorldTile insideTile, WorldTile graveStoneTile, String controllerName, int musicId) {
            this.instance = instance;
            this.initialCost = initialCost;
            this.maxPlayers = maxPlayers;
            this.hasHM = hasHM;
            this.publicVersion = publicVersion;
            this.insideTile = insideTile;
            this.outsideTile = outsideTile;
            this.graveStoneTile = graveStoneTile;
            this.controllerName = controllerName;
            this.musicId = musicId;
        }

        public Map<String, BossInstance> getCachedInstances() {
            return cachedInstances;
        }

        public String getControllerName() {
            return controllerName;
        }

        public WorldTile getInsideTile() {
            return insideTile;
        }

        public WorldTile getOutsideTile() {
            return outsideTile;
        }

        public WorldTile getGraveStoneTile() {
            return graveStoneTile;
        }

        public boolean isHasHM() {
            return hasHM;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public int getInitialCost() {
            return initialCost;
        }

        public int getMusicId() {
            return musicId;
        }

        public boolean hasPublicVersion() {
            return publicVersion;
        }

    }
}