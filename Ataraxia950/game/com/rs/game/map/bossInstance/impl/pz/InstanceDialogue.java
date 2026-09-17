package com.rs.game.map.bossInstance.impl.pz;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import lombok.val;

/**
 * @author Kris | 9. sept 2018 : 23:58:28
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class InstanceDialogue extends Dialogue {

    private int pageIndex;
    private final List<String> pages = new ArrayList<String>(5);

    private void sendOptions() {
        pages.clear();
        for (int i = pageIndex * 4; i < ((pageIndex * 4) + 4); i++) {
            if (i >= InstanceType.VALUES.length) {
                continue;
            }
            pages.add(InstanceType.VALUES[i].toString());
        }
        if (pages.isEmpty()) {
            pageIndex = 0;
            sendOptions();
            return;
        }
        pages.add("More options...");
        sendOptionsDialogue("Select the instance", pages.toArray(new String[0]));
    }

    @Override
    public void start() {
        sendOptions();
    }

    @Override
    public void run(final int interfaceId, final int componentId) {
        val optionIndex = getOrdinal(componentId);
        if (optionIndex == pages.size() - 1) {
            pageIndex++;
            if ((pageIndex * 4) >= InstanceType.VALUES.length) {
                pageIndex = 0;
            }
            sendOptions();
        } else {
            end();
            val type = InstanceType.VALUES[(pageIndex * 4) + optionIndex];
            player.setLastBossInstanceSettings(new InstanceSettings(Boss.PZ_BOSS));
            player.getLastBossInstanceSettings().setPractiseMode(false);
            InstanceSettings settings = player.getLastBossInstanceSettings();
            if (settings == null)
                return;
            settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
            settings.setMinCombat(1);
            settings.setSpawnSpeed(BossInstance.FASTEST);
            settings.setProtection(BossInstance.FFA);
            settings.setCreationTime(Utils.currentTimeMillis());
            String key = player == null ? "" : player.getUsername();
            BossInstance instance = BossInstanceHandler.findInstance(settings.getBoss(), key);
            if (instance != null) {
                settings.getBoss().getCachedInstances().remove(key);
            }
            instance = new PZInstance(player, settings,type);
            settings.getBoss().getCachedInstances().put(player.getUsername(), instance);
//            if (!player.instanceCreated) {
//                player.instanceCreated = true;
//                try {
//                    end();
//                    val type = InstanceType.VALUES[(pageIndex * 4) + optionIndex];
//                    val allocated = Builder.findEmptyChunk(InstanceConstants.INSTANCE_WIDTH, InstanceConstants.INSTANCE_HEIGHT,
//                            DynamicBoundary.UNSPECIFIED);
//                    player.lock();
//                    val area = new InstanceArea(type, allocated);
//                    area.constructRegion();
//                    WorldTasksManager.schedule(new WorldTask() {
//                        @Override
//                        public void run() {
//                            player.instanceCreated = false;
//                            player.setNextWorldTile(area.getLocation(new WorldTile(3039, 6047, 0)));
//                            player.unlock();
//                        }
//                    }, 1);
//                } catch (final NullPointerException | OutOfSpaceException | OutOfBoundaryException e) {
//                    Logger.getGlobal().catching(e);
//                } catch (Exception e) {
//                    player.unlock();
//                }
//            }
        }
    }

    @Override
    public void finish() {

    }

}
