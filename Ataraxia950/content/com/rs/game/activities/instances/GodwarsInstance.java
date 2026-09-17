package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zammorak.KrilTsutsaroth;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.ZGDController;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/ }
 */
public class GodwarsInstance extends Instance {

    private final NPC[] minions;

    public GodwarsInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        minions = new NPC[3];
        chunksToBind = getChunks();
        sizes = new int[] { 7, 7 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    private final int[] getChunks() {
        switch (bossId) {
        case 13447:
            return new int[] { 362, 648 };
        case 6260:
            return new int[] { 355, 667 };
        case 6203:
            return new int[] { 362, 663 };
        case 6247:
            return new int[] { 362, 654 };
        default:
            return new int[] { 350, 659 };
        }
    }

    public WorldTile getWaitingRoomCoords() {
        switch (bossId) {
        case 13447:
            return getWorldTile(8, 19);
        case 6260:
            return getWorldTile(17, 21);
        case 6203:
            return getWorldTile(29, 35);
        case 6247:
            return getWorldTile(23, 32);
        default:
            return getWorldTile(30, 12);
        }
    }

    private int[] getNPCIds() {
        switch (bossId) {
        case 13447:
            return new int[] { 13451, 13452, 13453, 13454 };
        case 6260:
            return new int[] { 6261, 6263, 6265 };
        case 6203:
            return new int[] { 6204, 6206, 6208 };
        case 6247:
            return new int[] { 6248, 6250, 6252 };
        default:
            return new int[] { 6223, 6225, 6227 };
        }
    }

    public WorldTile getWaitingRoom() {
        switch (bossId) {
        case 6260:
            return new WorldTile(2860, 5357, 0);
        case 6203:
            return new WorldTile(2925, 5336, 0);
        case 6247:
            return new WorldTile(2919, 5260, 0);
        default:
            return new WorldTile(2827, 5289, 0);
        }
    }

    public void initiateSpawningSequence() {
        Instance instance = this;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int seconds;
            private boolean resetSeconds;

            @Override
            public boolean repeat() {
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    if (players.size() > 0) {
                        players.forEach(player -> {
                            if (player != null && player.getCurrentInstance() == instance)
                                player.setNextWorldTile(getWaitingRoom());
                        });
                    }
                    destroyInstance();
                    if (boss != null)
                        boss.finish();
                    for (int i = 0; i < 3; i++) {
                        if (minions[i] != null)
                            minions[i].finish();
                    }
                    return false;
                }
                if (!isOwnerInstance()) {
                    if (boss != null)
                        boss.finish();
                    for (int i = 0; i < 3; i++) {
                        if (minions[i] != null)
                            minions[i].finish();
                    }
                    return false;
                }
                if (seconds == 0 && !finished) {
                    resetSeconds = false;
                    switch (bossId) {
                    case 13447:
                        if (boss == null || boss.hasFinished()) {
                            if (players.size() > 0) {
                                players.forEach(player -> {
                                    if (player != null && player.getCurrentInstance() == instance)
                                        player.getControlerManager().startControler(ZGDController.class.getSimpleName(), getWorldTile(29, 19), hardMode, false);
                                });
                            }
                        }
                        break;
                    case 6260:
                        if (boss == null || boss.hasFinished()) {
                            boss = new GeneralGraardor(6260, getWorldTile(30, 27), -1, true, true, hardMode);
                            boss.setForceMultiArea(true);
                            if (hardMode) {
                                int[] bonuses = new int[boss.getBonuses().length];
                                for (int i = 0; i < bonuses.length; i++)
                                    bonuses[i] = (int) (boss.getBonus(i) * 1.2);
                                boss.setBonuses(bonuses);
                            }
                        }
                        break;
                    case 6203:
                        if (boss == null || boss.hasFinished()) {
                            boss = new KrilTsutsaroth(6203, getWorldTile(31, 20), -1, true, true, hardMode);
                            boss.setForceMultiArea(true);
                            if (hardMode) {
                                int[] bonuses = new int[boss.getBonuses().length];
                                for (int i = 0; i < bonuses.length; i++)
                                    bonuses[i] = (int) (boss.getBonus(i) * 1.2);
                                boss.setBonuses(bonuses);
                            }
                        }
                        break;
                    case 6247:
                        if (boss == null || boss.hasFinished()) {
                            boss = new CommanderZilyana(6247, getWorldTile(27, 17), -1, true, true, hardMode);
                            boss.setForceMultiArea(true);
                            if (hardMode) {
                                int[] bonuses = new int[boss.getBonuses().length];
                                for (int i = 0; i < bonuses.length; i++)
                                    bonuses[i] = (int) (boss.getBonus(i) * (i <= 4 ? 2 : 1.1));
                                boss.setBonuses(bonuses);
                            }
                        }
                        break;
                    default:
                        if (boss == null || boss.hasFinished()) {
                            boss = new KreeArra(6222, getWorldTile(27, 30), -1, true, true, hardMode);
                            if (hardMode) {
                                for (int i = 0; i < 4; i++)
                                    ((KreeArra) boss).spawnWhirlwind();
                            }
                            boss.setForceMultiArea(true);
                            if (hardMode) {
                                int[] bonuses = new int[boss.getBonuses().length];
                                for (int i = 0; i < bonuses.length; i++)
                                    bonuses[i] = (int) (boss.getBonus(i) * 1.2);
                                boss.setBonuses(bonuses);
                            }
                        }
                        break;
                    }
                    if (bossId != 13447) {
                        for (int i = 0; i < 3; i++) {
                            if (minions[i] == null || minions[i].hasFinished() || minions[i].isDead()) {
                                minions[i] = new GodwarsMinion(getNPCIds()[i], getMinionSpawningTile(getNPCIds()[i]), -1, true, true);
                                minions[i].setForceMultiArea(true);
                            }
                        }
                    }
                }
                if (boss != null && boss.hasFinished() && !resetSeconds) {
                    seconds = 0 - respawnSpeed;
                    resetSeconds = true;
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration) {
                    finished = true;
                    players.forEach(player -> player.sendMessage("The instance has ended. No more monsters will be spawned in this instance."));
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration - 2) {
                    players.forEach(player -> player.sendMessage("The instance will remain open for two more minutes."));
                    isStable = false;
                }
                seconds++;
                totalSeconds++;
                return true;
            }

        }, 0, 1);
    }

    private WorldTile getMinionSpawningTile(int npcId) {
        switch (npcId) {
        case 6261:
            return new WorldTile(getWorldTile(26, 26));
        case 6263:
            return new WorldTile(getWorldTile(29, 19));
        case 6265:
            return new WorldTile(getWorldTile(34, 24));
        case 6204:
            return new WorldTile(getWorldTile(36, 25));
        case 6206:
            return new WorldTile(getWorldTile(25, 25));
        case 6208:
            return new WorldTile(getWorldTile(25, 16));
        case 6248:
            return new WorldTile(getWorldTile(22, 18));
        case 6250:
            return new WorldTile(getWorldTile(27, 14));
        case 6252:
            return new WorldTile(getWorldTile(32, 17));
        case 6223:
            return new WorldTile(getWorldTile(34, 33));
        case 6225:
            return new WorldTile(getWorldTile(33, 27));
        case 6227:
            return new WorldTile(getWorldTile(23, 26));
        default:
            return null;
        }
    }

    public static NPCDrop[] getDrops(NPC npc) {
        if (npc instanceof KreeArra) {
            if (((KreeArra) npc).isHardMode())
                return NPCDropsDataParser.getDrops(17095);
        } else if (npc instanceof CommanderZilyana) {
            if (((CommanderZilyana) npc).isHardMode())
                return NPCDropsDataParser.getDrops(17084);
        } else if (npc instanceof KrilTsutsaroth) {
            if (((KrilTsutsaroth) npc).isHardMode())
                return NPCDropsDataParser.getDrops(17016);
        } else if (npc instanceof GeneralGraardor) {
            if (((GeneralGraardor) npc).isHardMode())
                return NPCDropsDataParser.getDrops(17098);
        }
        return null;
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return getWaitingRoom();
    }

    @Override
    public NPC getBossNPC() {
        return null;
    }

    @Override
    public void performOnSpawn() {
    }
}
