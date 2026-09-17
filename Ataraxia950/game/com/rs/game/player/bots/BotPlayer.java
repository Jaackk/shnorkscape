package com.rs.game.player.bots;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.GlobalPlayerUpdater;
import com.rs.game.player.LoginManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.security.pin.AccountPin;
import com.rs.utils.Utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A server-controlled player with no backing client channel.
 */
public class BotPlayer extends Player {

    private static final long serialVersionUID = -1766030444075874133L;
    private static final AtomicInteger NEXT_APPEARANCE_VARIANT = new AtomicInteger();

    private final WorldTile spawnTile;
    private final int appearanceVariant;

    public BotPlayer(String username, String displayName, WorldTile spawnTile) {
        super("", "bot");
        this.spawnTile = new WorldTile(spawnTile);
        appearanceVariant = NEXT_APPEARANCE_VARIANT.getAndIncrement();
        setUsername(username);
        setDisplayName(displayName);
        setLocation(spawnTile);
        setAccountPin(new AccountPin(this));
        getAccountPin().setPinEntered();
        setCurrentMac("bot");
    }

    public WorldTile getSpawnTile() {
        return spawnTile;
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public boolean isAFK() {
        return false;
    }

    @Override
    public String getIP() {
        return "127.0.0.1";
    }

    @Override
    public void start() {
        getAccountPin().resetLocked();
        getAccountPin().setPinEntered();
        loadMapRegions();
        setClientHasLoadedMapRegion();
        if (!hasCompleted()) {
            setCompleted();
        }
        setRunning(true);
        enableRunMode();
        randomizeAppearance();
        getAppearence().generateAppearenceData();
        getControlerManager().login();
        setActive(true);
        World.updateEntityRegion(this);
    }

    void enableRunMode() {
        if (getRunEnergy() < 100) {
            setRunEnergy(100);
        }
        if (!getRun()) {
            setRunHidden(true);
        }
    }

    @Override
    public void drainRunEnergy() {
        // Bots do not have a client to manage run energy, so keep them moving at bot pace.
    }

    private void randomizeAppearance() {
        GlobalPlayerUpdater appearence = getAppearence();
        if (Utils.random(2) == 0) {
            appearence.male();
        } else {
            appearence.female();
        }
        PlayerLook.randomizeLook(appearence);
        applyDistinctBotColours(appearence, appearanceVariant);
    }

    private static void applyDistinctBotColours(GlobalPlayerUpdater appearence, int variant) {
        ClientScriptMap skinChoices = ClientScriptMap.getMap(7724);
        appearence.setSkinColor(ClientScriptMap.getMap(748).getIntValue(getVariantMapValue(skinChoices, variant, 1)));
        appearence.setHairColor(getVariantMapValue(ClientScriptMap.getMap(2345), variant, 3));
        appearence.setTopColor(getVariantMapValue(ClientScriptMap.getMap(3282), variant, 5));
        appearence.setLegsColor(getVariantMapValue(ClientScriptMap.getMap(3282), variant, 7));
        appearence.setBootsColor(getVariantMapValue(ClientScriptMap.getMap(3297), variant, 11));
    }

    private static int getVariantMapValue(ClientScriptMap map, int variant, int stride) {
        int size = map.getSize();
        if (size <= 0) {
            return 0;
        }
        int index = (int) (((long) variant * stride) % size);
        if (index < 0) {
            index += size;
        }
        return map.getIntValueAtIndex(index);
    }

    @Override
    public void realFinish() {
        destroy();
    }

    public void destroy() {
        if (hasFinished()) {
            return;
        }
        BotManager.unregister(this);
        try {
            stopAll();
            if (getControlerManager() != null) {
                getControlerManager().logout();
            }
        } catch (Exception ignored) {
            // Despawn must stay best-effort so one broken controller cannot strand a bot.
        }
        setActive(false);
        setRunning(false);
        setFinished(true);
        if (getLastRegionId() > 0) {
            World.getRegion(getLastRegionId()).removePlayerIndex(getIndex());
        }
        World.forceRemovePlayer(this);
    }

    public static BotPlayer create(String username, String displayName, WorldTile tile) {
        BotPlayer bot = new BotPlayer(username, displayName, tile);
        LoginManager.initBot(bot, username);
        bot.start();
        return bot;
    }
}
