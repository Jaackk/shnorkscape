package com.rs.game.player;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.MusicHintsDataParser;

import java.io.Serializable;
import java.util.ArrayList;

public final class MusicsManager implements Serializable {

    public static final int DEATH_MUSIC_EFFECT = 148;
    private static final long serialVersionUID = 1020415702861567375L;
    private static final int[] CONFIG_IDS = new int[] { 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, 64, 65, 66, 67, 68, 69, 70, 71, 3551, 3691, 4263, 5019, 5868, 6221, 6448, 6920, 7035, 7923, 8282, 8661 };
    private static final int[] PLAY_LIST_CONFIG_IDS = new int[] { 76, 77, 78, 79, 80, 81, 5872, 5873, 5874, 5875, 5876, 5877, 5878, 5879, 5880 };

    private transient Player player;
    private transient int playingMusic;
    private transient long playingMusicDelay;
    private transient boolean settedMusic;
    private final ArrayList<Integer> unlockedMusics;
    private ArrayList<Integer> playList;

    private transient boolean playListOn;
    private transient int nextPlayListMusic;
    private transient boolean shuffleOn;

    public MusicsManager() {
        unlockedMusics = new ArrayList<Integer>();
        playList = new ArrayList<Integer>(PLAY_LIST_CONFIG_IDS.length * 2);
        unlockMusics();
    }

    private static final int[] AUTO_UNLOCKED_MUSICS = { 200, 517, 518, 519, 323, 1176, 931, 316, 336, 151, 411, 350, 360, 89, 321, 412, 1177, 377, 150, 1179, 103, 153, 152, 602, 717, 482, 650, 520, 611, 318, 196, 514 };

    private void unlockMusics() {
        for (int id : AUTO_UNLOCKED_MUSICS)
            unlockedMusics.add(id);
        int[] startZoneMusics = World.getRegion(Settings.RESPAWN_PLAYER_LOCATION.getRegionId()).getMusicIds();
        if (startZoneMusics != null)
            for (int musicId : startZoneMusics)
                if (musicId >= 0)
                    unlockedMusics.add(musicId);
    }

    public void passMusics(Player p) {
        for (int musicId : p.getMusicsManager().unlockedMusics) {
            if (!unlockedMusics.contains(musicId))
                unlockedMusics.add(musicId);
        }
    }

    public boolean hasMusic(int id) {
        return unlockedMusics.contains(id);
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (typeMute == null)
            typeMute = new boolean[4];
        if (playList.size() < (PLAY_LIST_CONFIG_IDS.length * 2)) {
            playList = new ArrayList<Integer>(PLAY_LIST_CONFIG_IDS.length * 2);
        }
        // Native950Session owns location-based playback after movement. Its cache
        // catalog does not use the legacy Region musicIds array or replay timer.
        playingMusic = player.isNative950() ? -1 : World.getRegion(player.getRegionId()).getRandomMusicId();
    }

    public void switchShuffleOn() {
        if (shuffleOn) {
            playListOn = false;
            refreshPlayListConfigs();
        }
        shuffleOn = !shuffleOn;
    }

    public void switchPlayListOn() {
        if (playListOn) {
            playListOn = false;
            shuffleOn = false;
            refreshPlayListConfigs();
        } else {
            playListOn = true;
            nextPlayListMusic = 0;
            replayMusic();
        }
    }

    public void clearPlayList() {
        if (playList.isEmpty())
            return;
        playList.clear();
        refreshPlayListConfigs();
    }

    public void addPlayingMusicToPlayList() {
        addToPlayList(playingMusic);
    }

    public int getArchiveId(int musicId) {
        Object value = ClientScriptMap.getMap(1351).getValue(musicId);
        return value instanceof Integer ? (Integer) value : -1;
    }

    public void addToPlayList(int musicId) {
        if (playList.size() == 12)
            return;
        if (musicId != -1 && unlockedMusics.contains(musicId) && !playList.contains(musicId)) {
            playList.add(musicId);
            if (playListOn)
                switchPlayListOn();
            else
                refreshPlayListConfigs();
        }
    }

    public void removeFromPlayList(int musicId) {
        if (musicId != -1 && unlockedMusics.contains(musicId) && playList.contains(musicId)) {
            playList.remove(musicId);
            if (playListOn)
                switchPlayListOn();
            else
                refreshPlayListConfigs();
        }
    }

    public void refreshPlayListConfigs() {
        int[] configValues = new int[PLAY_LIST_CONFIG_IDS.length];

        // default each pair-slot to empty
        for (int i = 0; i < configValues.length; i++) {
            configValues[i] = -1;
        }

        // pack two music indices per config value (low 15 bits, high 15 bits)
        int slot = 0;
        for (int i = 0; i < playList.size() && slot < PLAY_LIST_CONFIG_IDS.length; i += 2, slot++) {
            Integer musicId1 = playList.get(i);
            Integer musicId2 = (i + 1) < playList.size() ? playList.get(i + 1) : null;

            if (musicId1 == null)
                break;

            int value;

            if (musicId2 != null) {
                value = (musicId1 & 0x7FFF) | ((musicId2 & 0x7FFF) << 15);
            } else {
                // 0x7FFF means "empty" for the second slot
                value = (musicId1 & 0x7FFF) | (0x7FFF << 15);
            }

            configValues[slot] = value;
        }

        // push to client
        for (int i = 0; i < PLAY_LIST_CONFIG_IDS.length; i++) {
            int id = PLAY_LIST_CONFIG_IDS[i];
            if (id != -1) {
                player.getVarsManager().sendVar(id, configValues[i]);
                // If your client expects a global config instead, use:
                // player.getPackets().sendGlobalConfig(id, configValues[i]);
                // If it's truly a varp packet in your build, this also works:
                // player.getPackets().sendConfig(id, configValues[i]);
            }
        }
    }


    public void refreshListConfigs() {
        int[] configValues = new int[CONFIG_IDS.length];
        for (int musicId : unlockedMusics) {
            int index = getConfigIndex(musicId);
            if (index >= CONFIG_IDS.length)
                continue;
            configValues[index] |= 1 << (musicId - (index * 32));
        }
        for (int i = 0; i < CONFIG_IDS.length; i++) {
            if (CONFIG_IDS[i] != -1)
                player.getVarsManager().sendVar(CONFIG_IDS[i], configValues[i]);
        }
    }

    public void addMusic(int musicId) {
        unlockedMusics.add(musicId);
        refreshListConfigs();
        if (unlockedMusics.size() >= Settings.AIR_GUITAR_MUSICS_COUNT)
            player.getEmotesManager().unlockEmote(41);
    }

    public int getConfigIndex(int musicId) {
        return (musicId + 1) / 32;
    }

    public void unlockMusicPlayer() {
        player.getPackets().sendUnlockIComponentOptionSlots(187, 1, 0, CONFIG_IDS.length * 64, 0, 1, 2, 3);
    }

    public void init() {
        if (player.isNative950()) return;
        // unlock music inter all options
        if (playingMusic >= 0)
            playMusic(playingMusic);
        refreshListConfigs();
        refreshPlayListConfigs();
        refreshGlobalMute();
        refreshMutes();
    }

    public boolean musicEnded() {
        if (player != null && player.isNative950()) return false;
        return playingMusic != -2 && playingMusicDelay + (180000) < Utils.currentTimeMillis();
    }

    public void replayMusic() {
        if (player.isNative950()) return;
        if (playListOn && playList.size() > 0) {
            if (shuffleOn)
                playingMusic = playList.get(Utils.getRandom(playList.size() - 1));
            else {
                if (nextPlayListMusic >= playList.size())
                    nextPlayListMusic = 0;
                playingMusic = playList.get(nextPlayListMusic++);
            }
        } else if (unlockedMusics.size() > 0) // random music
            playingMusic = unlockedMusics.get(Utils.getRandom(unlockedMusics.size() - 1));
        playMusic(playingMusic);
    }

    public void checkMusic(int requestMusicId) {
        if (playListOn || settedMusic && playingMusicDelay + (180000) >= Utils.currentTimeMillis())
            return;
        settedMusic = false;
        if (playingMusic != requestMusicId)
            playMusic(requestMusicId);
    }

    public void forcePlayMusic(int musicId) {
        settedMusic = true;
        playMusic(musicId);
    }

    public void reset() {
        if (player.isNative950()) return;
        settedMusic = false;
        player.getMusicsManager().checkMusic(World.getRegion(player.getRegionId()).getRandomMusicId());
    }

    public void sendHint(int musicId) {
        if (musicId != -1) {
            player.getPackets().sendGameMessage("This track " + (unlockedMusics.contains(musicId) ? "was unlocked" : "unlocks") + " " + MusicHintsDataParser.getHint(musicId));
        }
    }

    public void playAnotherMusic(int musicId) {
        if (musicId != -1 && unlockedMusics.contains(musicId)) {
            settedMusic = true;
            if (playListOn)
                switchPlayListOn();
            playMusic(musicId);
        }

    }

    public void playMusic(final int musicId) {
        // Manual playlists/forced legacy tracks require their own native port.
        // Never enqueue a stale 910 callback that can overwrite regional playback
        // or send text/configuration to the unmounted legacy music interface.
        if (player.isNative950()) return;
        if (player.hasFinished())
            return;

        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                playingMusicDelay = Utils.currentTimeMillis();
                if (musicId == -2) {
                    playingMusic = musicId;
                    player.getPackets().sendMusic(-1);
                    refreshMusicInterface();
                    return;
                }
                int archiveId = getArchiveId(musicId);
                if (archiveId == -1) {
                    player.sendMessage("Unable to resolve music archive for track " + musicId + ".");
                    this.stop();
                    return;
                }
                player.getPackets().sendMusic(archiveId, playingMusic == -1 ? 0 : 100, 255);
                playingMusic = musicId;
                String musicName = ClientScriptMap.getMap(1345).getStringValue(musicId);
                if (musicName.equals(" "))
                    musicName = Region.getMusicName1(player.getRegionId());
                player.getPackets().sendIComponentText(187, 4, musicName != null ? musicName : "");
                if (!unlockedMusics.contains(musicId)) {
                    addMusic(musicId);
                    if (musicName != null)
                        player.sendMessage("<col=ff0000>You have unlocked a new music track: " + musicName + ".");
                }
                this.stop();
            }
        }, 2);
    }

    private void refreshMusicInterface() {
        refreshMusicInterface(false);
        if (player.getInterfaceManager().containsInterface(187))
            refreshMusicInterface(true);
    }

    public void refreshMusicInterface(boolean settings) {
        player.getPackets().sendIComponentText(settings ? 187 : 1416, settings ? 7 : 6, getMusicName());
    }

    public void unlockMusicPlayer(boolean menu) {
        player.getPackets().sendUnlockIComponentOptionSlots(menu ? 187 : 1416, menu ? 4 : 3, 0, CONFIG_IDS.length * 64, 0, 1, 2, 3);
        player.getPackets().sendUnlockIComponentOptionSlots(menu ? 187 : 1416, menu ? 12 : 11, 0, PLAY_LIST_CONFIG_IDS.length * 4, 0, 1, 2, 3);
    }

    public String getMusicName() {
        if (playingMusic == -2)
            return "";
        return ClientScriptMap.getMap(1345).getStringValue(playingMusic);
    }

    private boolean[] typeMute;
    private boolean globalMute;

    public void toggleGlobalMute() {
        globalMute = !globalMute;
        refreshGlobalMute();
    }

    private void refreshGlobalMute() {
        player.getPackets().sendConfigByFile(38837, globalMute ? 1 : 0);
    }

    public void switchIsTypeMuted(int index) {
        if (typeMute == null)
            typeMute = new boolean[4];
        typeMute[index] = !typeMute[index];
        refreshMutes();
    }

    public void refreshMutes() {
        if (typeMute == null)
            typeMute = new boolean[4];
        for (int i = 0; i < typeMute.length; i++)
            player.getPackets().sendConfigByFile(38838 + i, typeMute[i] ? 1 : 0);
    }

    public void searchMusic() {
        player.getInterfaceManager().sendInputTextInterface();
        player.getPackets().sendGlobalConfig(3497, 1);
    }

    // globalconfig 1077 ison playlist tab
    // varbit 190 (empty ? 32767 : playlist songs)
    // varbit 202 current playingsong
    // varbit 187 playlist on/off
    // varbit 38906 current playingsong for loop
    // varbit 38905 loop on/off

}
