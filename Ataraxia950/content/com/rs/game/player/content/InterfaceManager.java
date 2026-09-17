package com.rs.game.player.content;

import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.player.CosmeticsManager;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950WorldMap;
import com.rs.game.player.client.Native950Settings;
import com.rs.game.player.client.Native950Lodestones;
import com.rs.game.player.client.Native950SkillGuide;
import com.rs.game.player.content.CosmeticsHandler.CosmeticType;
import com.rs.game.player.content.SlayerTask.Master;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import lombok.Getter;
import lombok.val;

public class InterfaceManager {

    public static final int FIXED_WINDOW_ID = 1477;
    public static final int RESIZABLE_WINDOW_ID = 1477;
    public static int playerSelected1156 = 0;
    private final Player player;
    private int windowsPane;
    private Native950WorldMap native947WorldMap;
    private Native950Settings native947Settings;
    private Native950Lodestones native950Lodestones;
    private Native950SkillGuide native950SkillGuide;
    private com.rs.game.player.client.Native950ProductionMenu native950ProductionMenu;
    private com.rs.game.player.client.Native950ToolbeltUi native950ToolbeltUi;
    private com.rs.game.player.client.Native950ForgeUi native950ForgeUi;

    private static final int EDIT_MODE_COMPONENT_ID = 692;
    public static final int DROP_DOWN_MENU_COMPONENT_ID = 822;
    public static final int DROP_DOWN_MENU_INTERFACE_COMPONENT_ID = 824;
    public static final int EXPAND_BUTTONS_COMPONENT_ID = 728;
    public static final int WORLD_MAP_COMPONENT_ID = 30;
    public static final int GAME_SCREEN_COMPONENT_ID = 23;
    public static final int LEVEL_UP_COMPONENT_ID = 734;
    public static final int FADING_COMPONENT_ID = 36;
    public static final int LOADING_SCREEN_COMPONENT_ID = 847;
    public static final int FREE_CAM_COMPONENT_ID = 29;

    private final ConcurrentHashMap<Integer, Integer> openedinterfaces = new ConcurrentHashMap<Integer, Integer>();

    /*
     * 0 - skill inter 1 - active task 2 - inventory 3 - equipment 4 - prayer book 5
     * - abilities 9 - emotes 14 - friend list 15 - friend chat info 16 - clan 18 -
     * chat 19 - friend chat
     */
    public static final int SKILLS_TAB = 0, ACTIVE_TASK_TAB = 1, INVENTORY_TAB = 2, EQUIPMENT_TAB = 3, PRAYER_BOOK_TAB = 4, MAGIC_BOOK_TAB = 5, MELEE_ABILITIES_TAB = 6, RANGE_ABILITIES_TAB = 7, DEFENCE_ABILITIES_TAB = 8, EMOTES_TAB = 9, NOTES_TAB = 11, SUMMONING_TAB = 12, MINIGAME_TAB = 17, ALL_CHAT_TAB = 18, MAGIC_ABILITIES_TAB = 33;
    private static final int TOTAL_SCREEN_OVERLAY_COMPONENT_ID = 35;
    // new combat styles 1503
    private boolean resizableScreen;

    public InterfaceManager(Player player) {
        this.player = player;
        currentMenu = -1;
    }

    public static int getComponentUId(int interfaceId, int componentId) {
        return interfaceId << 16 | componentId;
    }

    /* starts: handles new teleports system in quest tab */
    public static int getPlayerInterfaceSelected() {
        return playerSelected1156;
    }

    public static int setPlayerInterfaceSelected(int value) {
        return playerSelected1156 = value;
    }


    public void sendTaskSystem() {
        setWindowInterfaceByKey(32, 930);
        TaskTab.sendTab(player);
    }

    public boolean isResizableScreen() {
        return resizableScreen;
    }

    public void sendChatBoxInterface(int interfaceId) {
        if (player.isNative950()) {
            if (player.getNative950Dialogues() == null)
                throw new IllegalStateException("Native dialogue presentation is not attached");
            player.getNative950Dialogues().open(interfaceId);
            return;
        }
        setWindowInterface(false, MainInterfaceComponents.GAME_DIALOG.getComponentId(), interfaceId);
        refreshInterface(false);
    }

    public boolean containsChatBoxInterface() {
        if (player.isNative950())
            return player.getNative950Dialogues() != null && player.getNative950Dialogues().isOpen();
        return containsInterfaceIn(MainInterfaceComponents.GAME_DIALOG);
    }

    public boolean containsChatBoxInterface(int interfaceId) {
        if (player.isNative950())
            return player.getNative950Dialogues() != null && player.getNative950Dialogues().isOpen(interfaceId);
        return containsChatBoxInterface() && openedinterfaces.get(getComponentUId(FIXED_WINDOW_ID, MainInterfaceComponents.GAME_DIALOG.getComponentId())) == interfaceId;
    }

    public void closeChatBoxInterface() {
        if (player.isNative950()) {
            if (player.getNative950Dialogues() != null) player.getNative950Dialogues().close();
            return;
        }
        removeInterfaceIn(MainInterfaceComponents.GAME_DIALOG);
    }

    public void sendOverlay(int interfaceId, boolean fullScreen) {
        setWindowInterface(TOTAL_SCREEN_OVERLAY_COMPONENT_ID, interfaceId);
    }






    // RS3/NXT caches: 275 = simple scroll with many text lines
    private static final int SCROLL_IFACE = 275;
    private static final int TITLE_COMP   = 1;
    private static final int FIRST_LINE   = 10;   // adjust to 14 if your cache starts there
    private static final int LAST_LINE    = 309;  // plenty of lines to fill

    private void openScrollTab(Player player, String title, String... lines) {
        setMenuInterface(0, SCROLL_IFACE);

        // Title
        player.getPackets().sendIComponentText(SCROLL_IFACE, TITLE_COMP, title);

        // Clear + make each line clickable (primary click: mask 2 in your build)
        for (int c = FIRST_LINE; c <= LAST_LINE; c++) {
            player.getPackets().sendIComponentText(SCROLL_IFACE, c, "");
            player.getPackets().sendIComponentSettings(SCROLL_IFACE, c, 0, 0, 2);
        }

        // Fill lines
        int comp = FIRST_LINE;
        for (String s : lines) {
            if (comp > LAST_LINE) break;
            player.getPackets().sendIComponentText(SCROLL_IFACE, comp++, s);
        }
    }









    public void closeOverlay(boolean fullScreen) {
        player.evilTreeInter = false;
        removeWindowInterface(TOTAL_SCREEN_OVERLAY_COMPONENT_ID);
    }

    public void sendInterface(int interfaceId) {
        closeChatBoxInterface();
        removeCentralInterfaceLargeInterface();
        sendInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE, interfaceId);
    }

    public final void sendInterfaces() {
        if (player.getFamiliar() != null && player.isRunning())
            player.getFamiliar().unlock();
        player.getControlerManager().sendInterfaces();
        sendNISScreenInterfaces();
        unlockDefaultGameInterface();
        player.getInventory().unlockInventoryOptions(false);
        player.getEquipment().unlockEquipment(false);
        player.getNotes().unlockNotes(false);
        player.refreshInterfaceVars();
        player.setDefaultVariables();
        sendSquealOverlay();
        ClansManager.unlockBanList(player);
    }

    public void sendWindowPane(int id) {
        player.getPackets().sendWindowsPane(id, 0);
    }

    public void sendWindowPane() {
        setDefaultRootInterface();
    }

    public boolean containsInterface(int interfaceId) {
        if (interfaceId == windowsPane)
            return true;
        for (int value : openedinterfaces.values())
            if (value == interfaceId)
                return true;
        return false;
    }

    public boolean containsScreenInter() {
        return containsInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE) || containsCentralInterfaceLargeInterface()
                || containsPlayerInspectInterface();
    }

    public void closeScreenInterface() {
        removeCentralInterfaceLargeInterface();
        removeInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE);
    }

    public boolean containsChatBoxInter() {
        if (player.isNative950()) return containsChatBoxInterface();
        return containsInterfaceIn(MainInterfaceComponents.GAME_DIALOG);
    }

    public void sendFadingInterface(int backgroundInterface) {
        setWindowInterface(FADING_COMPONENT_ID, backgroundInterface);
    }

    public void closeFadingInterface() {
        removeWindowInterface(FADING_COMPONENT_ID);
    }

    public boolean hasRezizableScreen() {
        return resizableScreen;
    }

    public int getWindowsPane() {
        return windowsPane;
    }

    public void setWindowsPane(int windowsPane) {
        this.windowsPane = windowsPane;
    }

    public void gazeOrbOfOculus() {
        player.getPackets().sendGameMessage("This option has been removed, You can use the free cam mode instead (right-click on WorldMap).");
    }

    public void openGameTab(int tabId) {
        player.getPackets().sendExecuteScript(8310, tabId);
    }

    public void sendSquealOverlay() {
        if (Settings.DEBUG)// fuckin annoying when developing.
            return;
        if (player.getTreasureHunter().getTotalKeysAmount() >= 1) {
            setWindowInterface(50, 1252);
        }
    }

    public void closeTreasureHunterOverlay() {
        removeWindowInterface(50);
    }

    public void sendSlayerList() {
        player.closeInterfaces();
        // Placefolder for banned monster names
        String[] tasks = { "", "", "", "", "", "" };
        sendInterface(161);
        // Static information to paste over the interface
        player.getPackets().sendIComponentText(161, 19, "" + player.getSlayerPoints());
        player.getPackets().sendIComponentText(161, 23, "Reset your current slayer task");
        player.getPackets().sendIComponentText(161, 26, "(" + (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? " FREE! )" : 10 + " points)"));
        player.getPackets().sendIComponentText(161, 27, "(" + (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? 120 : 160) + " points)");

        // Check if player has any banned tasks
        if (player.getBannedTasks() != null) {
            for (int i = 0; i < player.getBannedTasks().size(); i++) {
                if (player.getBannedTasks().get(i) != null) {
                    SlayerTask task = new SlayerTask(Master.KURADAL, player.getBannedTasks().get(i), 1);
                    tasks[i] = task.getName(player).charAt(task.getName(player).length() - 1) == 's' ? task.getName(player) : task.getName(player) + "s";
                } else
                    player.banTask(false, i);
            }
        }
        for (int i = 0; i < tasks.length; i++)
            player.getPackets().sendIComponentText(161, 31 + i, (tasks[i] == "") ? "                  No task has been banned!" : Colors.GOLD + "      *         " + tasks[i]);
    }

    public void sendBossKillLog() {
        val raxKills = player.getRaxKC();
        val rotsCompleted = player.getRiseOfTheSixRunsCompleted();
        val runOrRuns = rotsCompleted == 1 ? " run" : " runs";
        player.closeInterfaces();
        String[] names = {
                /* GWD Bosses */
                "General Graardor", "Kree'arra", "K'ril Tsutsaroth", "Commander Zilyana", "Nex",
                /* Dagannoth Kings */
                "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme",
                /* Kalphites and Black dragons */
                "King Black Dragon", "Queen Black Dragon", "Kalphite Queen", "Kalphite King",
                /* High-level bosses and Chaos */
                "Vorago", "Corporeal Beast", "Chaos Elemental", "Glacor", "Tormented Demon",
                /* Legios */
                "Legio Primus", "Legio Secundus", "Legio Tertius", "Legio Quartus", "Legio Quintus", "Legio Sextus",
                /* GWD2 */
                "Vindicta", "Helwyr", "Gregorovic", "Twin Furies", "Telos", "The Magister" };
        sendInterface(227);
        player.getPackets().sendExecuteScript(8420, 14876723, 14876728, 14876724, 14876730, Colors.DARK_RED + Colors.SHAD + "Boss kill-log</col>", 21218, 1007);
        player.getPackets().sendIComponentText(227, 55, Colors.SALMON + Colors.SHAD + Utils.formatPlayerNameForDisplay(player.getUsername()) + "'s killed bosses");
        player.getPackets().sendIComponentText(227, 0, Colors.wrap(Colors.LPURPLE + Colors.SHAD, "Barrows: Rise of the Six: ") + (rotsCompleted == 0 ? Colors.wrap(Colors.DARK_RED, "none completed yet!") : Colors.wrap(Colors.LIME, rotsCompleted + runOrRuns + " completed!")));
        player.getPackets().sendIComponentText(227, 1, Colors.LPURPLE + Colors.SHAD + "Araxxor: " + Colors.LIME + "slain " + raxKills + " total!");
        for (int i = 2; i < 50; i++)
            player.getPackets().sendIComponentText(227, i, (i - 2) >= names.length ? "" : (Colors.LPURPLE + Colors.SHAD + names[i - 2] + ":</col> " + (player.increaseKillStatistics(names[i - 2].toLowerCase(), false) == 0 ? Colors.DARK_RED + "none" : Colors.LIME + "") + " slain " + ((player.increaseKillStatistics(names[i - 2].toLowerCase(), false) == 0) ? "yet!" : player.increaseKillStatistics(names[i - 2].toLowerCase(), false) + " total!")));
    }

    /**
     * Sends an interface with the possible NPC's that drop a mentioned item.
     *
     * @param defs The Item Definitions.
     */
    public void sendItemDrops(ItemDefinitions defs) {
        int i = 0;
        String dropEntry = "";
        player.getInterfaceManager().sendInterface(275);
        sendIComponentText(275, 1, "Drops: <col=9900FF><shad=000000>" + defs.name + "</col></shad>");
        for (i = 10; i < 310; i++)
            sendIComponentText(275, i, "");
        i = 0;
        for (int n = 0; n < Utils.getNPCDefinitionsSize(); n++) {
            NPCDefinitions def = NPCDefinitions.getNPCDefinitions(n);
            NPCDrop[] drops = NPCDropsDataParser.getDrops(def.getId());
            if (drops != null) {
                for (NPCDrop drop : drops) {
                    if (drop.getItemId() == 0)
                        continue;
                    ItemDefinitions itemDefs = ItemDefinitions.getItemDefinitions(drop.getItemId());
                    if (itemDefs.getId() != defs.getId() || !itemDefs.name.contains(defs.name) || !itemDefs.name.equalsIgnoreCase(defs.name))
                        continue;
                    if (def == null || def.getName().toLowerCase().contains("null"))
                        continue;
                    StringBuilder sb = new StringBuilder().append(def.name).append(": ").append(itemDefs.name).append(drop.getMaxAmount() == 1 ? ("") : drop.getMinAmount() == drop.getMaxAmount() ? (" (" + drop.getMaxAmount() + ")") : (" (" + drop.getMinAmount() + "-" + drop.getMaxAmount() + ")")).append(" [").append((int) drop.getRate()).append("% Chance]");
                    dropEntry = sb.toString();
                    if (i < 300)
                        sendIComponentText(275, 10 + i, dropEntry);
                    dropEntry = "";
                    i++;
                }
            }
        }
    }

    /**
     * Sends an interface with the possible item drops from the mentioned NPC
     *
     * @param defs The NPC Definitions.
     */
    public void sendNPCDrops(NPCDefinitions defs) {
        int i = 0;
        String dropEntry = "";
        player.getInterfaceManager().sendInterface(275);
        sendIComponentText(275, 1, "Drops: <col=9900FF><shad=000000>" + defs.name + "</col></shad>");
        for (i = 10; i < 310; i++)
            sendIComponentText(275, i, "");

        i = 0;
        NPCDrop[] drops = NPCDropsDataParser.getDrops(defs.getId());
        if (drops != null) {
            for (NPCDrop drop : drops) {
                if (drop.getItemId() == 0)
                    continue;
                ItemDefinitions itemDefs = ItemDefinitions.getItemDefinitions(drop.getItemId());
                StringBuilder sb = new StringBuilder().append(itemDefs.name).append(drop.getMaxAmount() == 1 ? ("") : drop.getMinAmount() == drop.getMaxAmount() ? (" (" + drop.getMaxAmount() + ")") : (" (" + drop.getMinAmount() + "-" + drop.getMaxAmount() + ")")).append(" [").append((int) drop.getRate()).append("% Chance]");
                dropEntry = sb.toString();
                if (i < 300)
                    sendIComponentText(275, 10 + i, dropEntry);
                dropEntry = "";
                i++;
            }
        }
    }

    /**
     * Just a shortener.
     *
     * @param interfaceId The interface ID.
     * @param componentId The component ID.
     * @param text        The text to display.
     */
    private void sendIComponentText(int interfaceId, int componentId, String text) {
        player.getPackets().sendIComponentText(interfaceId, componentId, text);
    }

    public void removeWindowInterface(int componentId) {
        removeInterfaceByParent(FIXED_WINDOW_ID, componentId);
    }

    public void sendHelpInterface() {
        player.getPackets().sendIComponentText(1245, 330, Colors.GREEN + Settings.SERVER_NAME + " </col>- " + Colors.RED + "");
        player.getPackets().sendIComponentText(1245, 13, "Welcome to <col=0033CC>" + Settings.SERVER_NAME + "</col>; Owner = <col=990000>xhybrid</col>.");
        player.getPackets().sendIComponentText(1245, 14, "How to teleport to bosses/skilling/training areas:");
        player.getPackets().sendIComponentText(1245, 15, "");
        player.getPackets().sendIComponentText(1245, 16, "You can teleport all around " + Settings.SERVER_NAME + " by using the Quest Tab button");
        player.getPackets().sendIComponentText(1245, 17, "");
        player.getPackets().sendIComponentText(1245, 18, "Basic Money Making methods:");
        player.getPackets().sendIComponentText(1245, 19, "One of the easiest and most reliable money sources is Voting!");
        player.getPackets().sendIComponentText(1245, 20, "");
        player.getPackets().sendIComponentText(1245, 20, "By doing ::vote you can receive up to 2m Gold for every vote book.");
        player.getPackets().sendIComponentText(1245, 21, "Another quick way of making money would be the Thieving Stalls at ::home");
        player.getPackets().sendIComponentText(1245, 22, "The main money making source is by defeating bosses though!");
        player.getPackets().sendIComponentText(1245, 23, "You can sell most of your items on the Grand Exchange.");
        player.getInterfaceManager().sendInterface(1245);
    }
    // ends:

    public void sendFamiliarInfo() {
//        removeInterface(662);
        setWindowInterfaceByKey(12, 3505, 662);
        sendLockGameTab(InterfaceManager.SUMMONING_TAB, false);
    }

    public boolean containsFamiliarInfoInterface() {
        return containsInterfaceAtParent(RESIZABLE_WINDOW_ID, InterfaceManager.getComponentIdByKey(7716, 12, 3505));
    }

    public void removeFamiliarInterface() {
        removeWindowInterface(InterfaceManager.getComponentIdByKey(7716, 12, 3505));
        sendLockGameTab(InterfaceManager.SUMMONING_TAB, true);
    }

    public void openDungTab() {
        openGameTab(InterfaceManager.MINIGAME_TAB);
    }

    public void sendDungPartyInterface() {
        sendMinigameTab(91);
    }

    public void closeDungPartyInterface() {
        closeMinigameTab();
    }

    public void setInterface(boolean clickThrought, int parentInterfaceId, int parentInterfaceComponentId, int interfaceId) {
        // even so lets set it for now
        if(player.getInterfaceManager().containsInterface(1904)) {
            player.getPackets().sendIComponentText(1904, 20, "");
        }

        int parentUID = getComponentUId(parentInterfaceId, parentInterfaceComponentId);
        int oldParentUID = getInterfaceParentId(interfaceId);

        Integer oldInterface = openedinterfaces.get(parentUID);
        if (oldInterface != null)
            clearChilds(oldInterface);

        openedinterfaces.put(parentUID, interfaceId); // replaces inter if
        // theres one in that
        // component already
        if (oldParentUID != -1 && oldParentUID != parentUID) {
            boolean removedInterface = openedinterfaces.remove(oldParentUID, interfaceId);
            if (removedInterface)
                bindUnBindHotKeys(interfaceId, false);
            player.getPackets().moveInterface(oldParentUID, parentUID);
        } else
            player.getPackets().sendInterface(clickThrought, parentUID, interfaceId);
        bindUnBindHotKeys(interfaceId, true);
    }

    public boolean containsInterfaceAtParent(int parentInterfaceId, int parentInterfaceComponentId) {
        return openedinterfaces.containsKey(getComponentUId(parentInterfaceId, parentInterfaceComponentId));
    }

    private void clearChilds(int parentInterfaceId) {
        for (int key : openedinterfaces.keySet()) {
            if (key >> 16 == parentInterfaceId) {
                int interfaceId = openedinterfaces.remove(key);
                bindUnBindHotKeys(interfaceId, false);
                player.getPackets().closeInterface(key);
            }
        }
    }

    public int getInterfaceParentId(int interfaceId) {
        if (interfaceId == windowsPane)
            return -1;
        for (int key : openedinterfaces.keySet()) {
            int value = openedinterfaces.get(key);
            if (value == interfaceId)
                return key;
        }
        return -1;
    }

    public void removeInterfaceByParent(int parentInterfaceId, int parentInterfaceComponentId) {
        removeInterfaceByParent(getComponentUId(parentInterfaceId, parentInterfaceComponentId));
    }

    public void removeInterfaceByParent(int parentUID) {
        Integer removedInterface = openedinterfaces.remove(parentUID);
        if (removedInterface != null) {
            clearChilds(removedInterface);
            bindUnBindHotKeys(removedInterface, false);
            player.getPackets().closeInterface(parentUID);
        }
    }

    public void setDefaultRootInterface() {
        player.getPackets().sendWindowsPane(FIXED_WINDOW_ID, 0);
    }

    public void setWindowInterface(int componentId, int interfaceId) {
        setWindowInterface(true, componentId, interfaceId);
    }

    public void setWindowInterface(boolean walkable, int componentId, int interfaceId) {
        setInterface(walkable, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, componentId, interfaceId);
    }

    public void sendGameMapInterface(int id) {
        setWindowInterface(GAME_SCREEN_COMPONENT_ID, id);
    }

    public boolean containsGameMapInterface() {
        if (player.isNative950())
            return getInterfaceParentId(1482) == getComponentUId(FIXED_WINDOW_ID, 30);
        return containsWindowInterfaceAtParent(GAME_SCREEN_COMPONENT_ID);
    }

    public void sendGameMapInterface() {
        sendGameMapInterface(1482);
    }

    public void removeInterface(int interfaceId) {
        int parentUID = getInterfaceParentId(interfaceId);
        if (parentUID == -1)
            return;
        player.getActionbar().unlockShortcuts(true);
        player.getActionbar().refreshLockBar();
        removeInterfaceByParent(parentUID);
    }

    public void sendMagicAbilities() {
        boolean legacyMode = player.isInLegacyInterfaceMode() && !player.isInLegacyCombatMode();

        sendMagicAbilitesTab(legacyMode ? 1617 : 1461);
        setWindowInterfaceByKey(33, 3505, 1884);// magic abilities
        setWindowInterfaceByKey(34, 3505, 1885);// combat spells
        setWindowInterfaceByKey(35, 3505, 1887);// teleport spells
        setWindowInterfaceByKey(36, 3505, 1886);// skilling spells
        if (legacyMode) {
            player.getInterfaceManager().setInterface(true, 1617, 6, 1461);
            player.getInterfaceManager().setInterface(true, 1617, 4, 1460);
            player.getInterfaceManager().setInterface(true, 1617, 7, 1883);
            player.getInterfaceManager().setInterface(true, 1617, 5, 1452);
            player.getPackets().sendIComponentSettings(1617, 0, 0, 31, 2);
        }
        sendLockGameTab(InterfaceManager.MAGIC_ABILITIES_TAB, player.isInLegacyCombatMode());
        player.getCombatDefinitions().unlockMagicAbilities();

    }


    public void sendMagicAbilitesTab(int interfaceId) {
        boolean legacyMode = player.isInLegacyInterfaceMode() && !player.isInLegacyCombatMode();
        if (legacyMode)
            player.getInterfaceManager().setInterface(true, 1617, 6, interfaceId);
        else
            setWindowInterface(274, interfaceId); // mage spellbook
        setWindowInterfaceByKey(5, 3505, interfaceId);
    }

    public void sendMeleeAbilities() {
        boolean legacyMode = player.isInLegacyInterfaceMode() || player.isInLegacyCombatMode();
        setWindowInterfaceByKey(6, 3505, legacyMode ? 1503 : 1460);
        player.getCombatDefinitions().unlockMeleeAbilities();
    }

    public void replaceMeleeAbilitesTab(int interfaceId) {
        setWindowInterfaceByKey(6, 3505, interfaceId);
    }

    public void sendRangedAbilities() {
        boolean legacyMode = player.isInLegacyInterfaceMode() || player.isInLegacyCombatMode();
        if (!legacyMode) {
            setWindowInterface(252, 1452); // range abilities
        } else {
            removeWindowInterface(252);
        }
        sendLockGameTab(InterfaceManager.RANGE_ABILITIES_TAB, player.isInLegacyCombatMode());
        player.getCombatDefinitions().unlockRangeAbilities();
    }

    public void sendDefenceAbilities() {
        boolean legacyMode = player.isInLegacyInterfaceMode() || player.isInLegacyCombatMode();
        if (!legacyMode) {
            setWindowInterface(318, 1883);
            setWindowInterface(329, 1449); // defence abilities
            setWindowInterface(340, 1882);
        } else {
            removeWindowInterface(318);
            removeWindowInterface(329);
            removeWindowInterface(340);
        }
        sendLockGameTab(InterfaceManager.DEFENCE_ABILITIES_TAB, legacyMode);
        player.getCombatDefinitions().unlockDefenceAbilities();
    }

    public void sendLegacyIcons() {
        if (player.isInLegacyInterfaceMode()) {
            player.getInterfaceManager().setInterface(true, 1920, 3, 1504);
            player.getInterfaceManager().setInterface(true, 1920, 5, 1506);
            player.getInterfaceManager().setInterface(true, 1920, 4, 1505);
            player.getPackets().sendUnlockIComponentOptionSlots(1505, 2, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        } else {
            removeInterfaceByParent(1920, 3);
            removeInterfaceByParent(1920, 4);
            removeInterfaceByParent(1920, 5);
        }
    }

    private void sendEpochTimer() {
        setWindowInterface(610, 635); // time
        player.getPackets().sendExecuteScript(7486, (int) (Utils.currentTimeMillis() / 60000), (635 << 16) + 1);
    }

    public void sendNISScreenInterfaces() {
        setDefaultRootInterface();
        sendGameMapInterface();
        sendMagicAbilities();
        sendMeleeAbilities();
        sendRangedAbilities();
        sendDefenceAbilities();
        sendLegacyIcons();
        sendEpochTimer();
        setWindowInterfaceByKey(0, 1466);
        setWindowInterfaceByKey(1, 930);
        setWindowInterfaceByKey(2, 1473);
        setWindowInterfaceByKey(3, 1464);
        setWindowInterfaceByKey(4, 1458);
        setWindowInterfaceByKey(9, 590);
        setWindowInterfaceByKey(10, 1416);
        setWindowInterfaceByKey(11, 1417);
        setWindowInterfaceByKey(14, 550);
        setWindowInterfaceByKey(15, 1427);
        setWindowInterfaceByKey(16, 1110);
        setWindowInterfaceByKey(18, 137);
        setWindowInterfaceByKey(19, 1467);
        setWindowInterfaceByKey(20, 1472);
        setWindowInterfaceByKey(21, 1471);
        setWindowInterfaceByKey(22, 1470);
        setWindowInterfaceByKey(23, 464);
        setWindowInterfaceByKey(24, 228);
        setWindowInterfaceByKey(25, 1529);
        setWindowInterfaceByKey(26, 231);
        setWindowInterfaceByKey(27, 1519);
        setWindowInterfaceByKey(28, 1588);
        setWindowInterfaceByKey(29, 1678);
        setWindowInterfaceByKey(31, 1904);
        sendTaskSystem();
        setWindowInterfaceByKey(35, 3513, 1215);
        setWindowInterfaceByKey(1002, 1431);
        setWindowInterface(633, 568);
        setWindowInterface(85, 1465);
        setInterface(true, 1465, 6, 1920);
        setWindowInterfaceByKey(1003, 1430);
        setInterface(true, 1430, 58, 1616);
        setWindowInterface(747, 1433);
        setWindowInterfaceByKey(1010, 1483);
        setWindowInterfaceByKey(1014, 745);
        setWindowInterfaceByKey(1009, 284);
        setWindowInterfaceByKey(1026, 1213);
        setWindowInterfaceByKey(1001, 1448);
        setWindowInterfaceByKey(1020, 557);
        setWindowInterfaceByKey(1038, 291);
        setWindowInterfaceByKey(1019, 182);
        setWindowInterfaceByKey(1032, 1670);
        setWindowInterfaceByKey(1033, 1671);
        setWindowInterfaceByKey(1034, 1672);
        setWindowInterfaceByKey(1035, 1673);
        setWindowInterface(86, 1919);
        setWindowInterface(838, 1847);
        setWindowInterface(34, 1680);
        sendSlayerCounterInterface();
        sendCombatTargetInterface();
    }

    private void unlockDefaultGameInterface() {
        sendIComponentSettingsByKey(7716, 1001, 3509, 1477, 0, 24, 2);
        player.getPackets().sendIComponentSettings(1466, 7, 0, 27, 30);
        player.getPackets().sendIComponentSettings(1458, 33, 0, 38, 8388610); // TODO needs update
        player.getPackets().sendIComponentSettings(1461, 1, 0, 194, 10320974);
        player.getPackets().sendIComponentSettings(1884, 1, 0, 194, 10320974);
        player.getPackets().sendIComponentSettings(1885, 1, 0, 194, 10320974);
        player.getPackets().sendIComponentSettings(1887, 1, 0, 194, 10320974);
        player.getPackets().sendIComponentSettings(1886, 1, 0, 194, 10320974);
        player.getPackets().sendIComponentSettings(1461, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1461, 7, 7, 10, 10319874);
        player.getPackets().sendIComponentSettings(1460, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1881, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1888, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1452, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1883, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1449, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1882, 1, 0, 194, 10320902);
        player.getPackets().sendIComponentSettings(1460, 5, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1460, 5, 7, 10, 10319874);
        player.getPackets().sendIComponentSettings(1452, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1883, 7, 7, 16, 2);
        player.getPackets().sendIComponentSettings(1883, 7, 7, 10, 10319874);
        player.getPackets().sendIComponentSettings(550, 14, 0, 500, 2046);
        player.getPackets().sendIComponentSettings(550, 66, 0, 500, 6);
        player.getPackets().sendIComponentSettings(1427, 30, 0, 600, 1040);
        player.getPackets().sendIComponentSettings(1110, 31, 0, 200, 2);
        player.getPackets().sendIComponentSettings(1110, 85, 0, 600, 2);
        player.getPackets().sendIComponentSettings(1110, 83, 0, 600, 1040);
        player.getPackets().sendIComponentSettings(1110, 38, 0, 600, 1040);
        player.getPackets().sendIComponentSettings(590, 8, 0, 216, 8388614);
        player.getPackets().sendIComponentSettings(1416, 3, 0, 2949, 62);
        player.getPackets().sendIComponentSettings(1416, 11, 0, 29, 2359334);
        player.getPackets().sendIComponentSettings(1416, 11, 30, 59, 4);
        player.getPackets().sendIComponentSettings(1416, 11, 60, 60, 2097152);
        player.getPackets().sendIComponentSettings(1417, 13, 0, 29, 2621470);
        sendIComponentSettingsByKey(169, 1, 152, 190, 0, 300, 14);
        player.getPackets().sendIComponentSettings(1854, 3, 0, 4, 2); // TODO needs update
        player.getPackets().sendIComponentSettings(1854, 5, 0, 4, 2); // TODO needs update
        player.getPackets().sendIComponentSettings(1854, 7, 0, 4, 2); // TODO needs update
        player.getPackets().sendIComponentSettings(1894, 15, 0, 0, 6); // TODO needs update
        player.getPackets().sendIComponentSettings(1894, 16, 0, 0, 6); // TODO needs update
        sendIComponentSettingsByKey(7716, 1002, 3507, 1477, 1, 1, 2);
        player.getPackets().sendIComponentSettings(1431, 0, 0, 47, 2);
        player.getPackets().sendIComponentSettings(568, 5, 0, 47, 2);
        sendIComponentSettingsByKey(7716, 1004, 3507, 1477, 1, 1, 6);
        player.getPackets().sendIComponentSettings(1433, 6, 0, 6, 2);
        sendIComponentSettingsByKey(7716, 1003, 3507, 1477, 1, 1, 4);
        player.getPackets().sendIComponentSettings(1458, 33, 0, 38, 8388610); // TODO needs update
        player.getPackets().sendIComponentSettings(1460, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1881, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1888, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1452, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1461, 1, 0, 194, 8617038);
        player.getPackets().sendIComponentSettings(1884, 1, 0, 194, 8617038);
        player.getPackets().sendIComponentSettings(1885, 1, 0, 194, 8617038);
        player.getPackets().sendIComponentSettings(1887, 1, 0, 194, 8617038);
        player.getPackets().sendIComponentSettings(1886, 1, 0, 194, 8617038);
        player.getPackets().sendIComponentSettings(1883, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1449, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(1882, 1, 0, 194, 8616966);
        player.getPackets().sendIComponentSettings(590, 8, 0, 216, 8388614);
        player.getPackets().sendIComponentSettings(137, 78, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(137, 65, 0, 11, 126);
        player.getPackets().sendIComponentSettings(137, 68, 0, 8, 126);
        player.getPackets().sendIComponentSettings(137, 62, 0, 2, 2);
        player.getPackets().sendIComponentSettings(1467, 62, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1467, 185, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1467, 189, 0, 8, 126);
        player.getPackets().sendIComponentSettings(1467, 191, 0, 2, 2);
        player.getPackets().sendIComponentSettings(1472, 62, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1472, 186, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1472, 190, 0, 8, 126);
        player.getPackets().sendIComponentSettings(1472, 192, 0, 2, 2);
        player.getPackets().sendIComponentSettings(1471, 62, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1471, 186, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1471, 190, 0, 8, 126);
        player.getPackets().sendIComponentSettings(1471, 192, 0, 2, 2);
        player.getPackets().sendIComponentSettings(1470, 62, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1470, 186, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1470, 190, 0, 8, 126);
        player.getPackets().sendIComponentSettings(1470, 192, 0, 2, 2);
        player.getPackets().sendIComponentSettings(464, 8, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(464, 186, 0, 11, 126);
        player.getPackets().sendIComponentSettings(464, 190, 0, 8, 126);
        player.getPackets().sendIComponentSettings(464, 192, 0, 2, 2);
        player.getPackets().sendIComponentSettings(1529, 62, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1529, 185, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1529, 189, 0, 8, 126);
        player.getPackets().sendIComponentSettings(1529, 191, 0, 2, 2);
        player.getPackets().sendIComponentSettings(228, 6, 0, 99, 1792);
        player.getPackets().sendIComponentSettings(1477, 17, -1, -1, 2097152);
        player.getPackets().sendIComponentSettings(1477, 13, -1, -1, 2);
        player.getPackets().sendIComponentSettings(1477, 13, 0, 41, 2);
        sendIComponentSettingsByKey(8602, 10, 6397, 1477, 0, 0, 2);
        sendIComponentSettingsByKey(7716, 1002, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1002, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1002, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1002, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1003, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1003, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1003, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1003, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1004, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1004, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1004, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1004, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1004, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 18, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 18, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 18, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 18, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 18, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 18, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 19, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 19, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 19, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 19, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 19, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 19, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 20, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 20, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 20, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 20, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 20, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 20, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 21, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 21, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 21, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 21, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 21, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 21, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 22, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 22, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 22, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 22, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 22, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 22, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 23, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 23, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 23, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 23, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 23, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 23, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 25, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 25, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 25, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 25, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 25, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 25, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 9, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 9, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 9, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 9, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 9, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 9, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 2, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 2, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 2, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 2, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 2, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 2, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 6, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 6, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 6, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 6, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 6, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 6, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 37, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 37, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 37, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 37, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 37, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 37, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 38, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 38, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 38, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 38, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 38, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 38, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 7, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 7, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 7, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 7, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 7, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 7, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 5, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 5, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 5, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 5, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 5, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 5, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 33, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 33, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 33, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 33, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 33, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 33, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 34, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 34, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 34, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 34, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 34, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 34, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 35, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 35, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 35, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 35, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 35, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 35, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 36, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 36, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 36, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 36, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 36, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 36, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 8, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 8, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 8, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 8, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 8, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 8, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 39, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 39, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 39, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 39, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 39, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 39, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 40, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 40, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 40, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 40, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 40, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 40, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 3, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 3, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 3, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 3, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 3, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 3, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 12, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 12, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 12, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 12, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 12, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 12, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 17, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 17, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 17, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 17, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 17, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 17, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 0, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 0, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 0, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 0, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 0, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 0, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 4, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 4, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 4, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 4, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 4, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 4, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 10, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 10, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 10, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 10, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 10, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 10, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 11, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 11, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 11, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 11, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 11, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 11, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 14, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 14, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 14, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 14, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 14, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 14, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 16, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 16, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 16, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 16, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 16, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 16, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 15, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 15, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 15, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 15, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 15, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 15, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1005, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1005, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1005, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1005, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1006, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1006, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1006, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1007, 3505, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1007, 3505, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1007, 3505, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1047, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1047, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1047, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1008, 3505, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1007, 3506, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1008, 3506, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1009, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1009, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1009, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 2008, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 2008, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 2008, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1010, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1010, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1010, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1011, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1011, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1011, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1012, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1012, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1012, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1013, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1013, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1013, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1014, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1014, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1014, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1015, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1015, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1015, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1016, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1016, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1016, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1017, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1017, 3506, 1477, 5, 5, 9175040);
        sendIComponentSettingsByKey(7716, 1017, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1017, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1017, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1018, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1018, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1018, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1019, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1019, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1019, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1019, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1001, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1001, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1001, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1001, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1001, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1000, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1000, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1000, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1000, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1000, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1020, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1020, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1020, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1021, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1021, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1021, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 26, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 26, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 26, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 26, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 26, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 26, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 24, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 24, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 24, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 24, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 24, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1023, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1023, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1023, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 27, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 27, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 27, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 27, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 27, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 27, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 6, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 11, 11, 9175040);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 13, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1024, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1025, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1025, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1025, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1026, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1026, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1026, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 28, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 28, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 28, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 28, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 28, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 28, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1027, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1027, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1027, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1029, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1029, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1029, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1028, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1028, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1028, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1028, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1030, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1030, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1030, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1031, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1031, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1031, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1032, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1032, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1032, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1032, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1033, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1033, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1033, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1033, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1034, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1034, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1034, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1034, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 1035, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1035, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1035, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1035, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 29, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 29, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 29, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 29, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 29, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 29, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1036, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1036, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1036, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 30, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 30, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 30, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 30, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 30, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 30, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1037, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1037, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1037, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 31, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 31, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 31, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 31, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 31, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 31, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1038, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1038, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1038, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 32, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 32, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 32, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 32, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 32, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 32, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 41, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 41, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 41, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 41, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 41, 3503, 1477, -1, -1, 2097152);
        sendIComponentSettingsByKey(7716, 41, 3507, 1477, 1, 1, 2);
        sendIComponentSettingsByKey(7716, 1040, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1040, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1040, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1041, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1041, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1041, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1045, 3506, 1477, 1, 2, 9175040);
        sendIComponentSettingsByKey(7716, 1045, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1045, 3506, 1477, 3, 4, 9175040);
        sendIComponentSettingsByKey(7716, 1046, 3506, 1477, 1, 7, 9175040);
        sendIComponentSettingsByKey(7716, 1046, 3506, 1477, 11, 13, 9175040);
        sendIComponentSettingsByKey(7716, 1046, 3506, 1477, 0, 0, 9175040);
        sendIComponentSettingsByKey(7716, 1046, 3506, 1477, 3, 4, 9175040);
        player.getPackets().sendIComponentSettings(1588, 27, 0, 78, 1730);
        player.getPackets().sendIComponentSettings(1588, 21, 0, 78, 1730);
        player.getPackets().sendIComponentSettings(1588, 15, 0, 78, 1730);
        player.getPackets().sendIComponentSettings(1588, 9, 0, 26, 28);
        player.getPackets().sendIComponentSettings(1588, 25, 0, 10, 6);
        player.getPackets().sendIComponentSettings(1588, 19, 0, 10, 6);
        player.getPackets().sendIComponentSettings(1588, 13, 0, 10, 6);
        player.getPackets().sendIComponentSettings(1588, 28, 0, 100, 2359302);
        player.getPackets().sendIComponentSettings(1588, 22, 0, 100, 2359302);
        player.getPackets().sendIComponentSettings(1588, 16, 0, 100, 2359302);
        player.getPackets().sendIComponentSettings(1588, 26, -1, -1, 2046);
        player.getPackets().sendIComponentSettings(1588, 20, -1, -1, 2046);
        player.getPackets().sendIComponentSettings(1588, 14, -1, -1, 2046);
        player.getPackets().sendIComponentSettings(1588, 29, 0, 0, 6);
        player.getPackets().sendIComponentSettings(1588, 23, 0, 0, 6);
        player.getPackets().sendIComponentSettings(1588, 17, 0, 0, 6);
    }

    public void sendSlayerCounterInterface() {
        setWindowInterfaceByKey(1031, 3503, 1639);
    }

    public void removeSlayerCounterInterface() {
        removeWindowInterface(InterfaceManager.getComponentIdByKey(7716, 1031, 3503));
    }

    public boolean containsSlayerCounterInterface() {
        return containsInterfaceAtParent(FIXED_WINDOW_ID, InterfaceManager.getComponentIdByKey(7716, 1031, 3503));
    }

    public void sendAbilities() {
        sendMagicAbilities();
        sendRangedAbilities();
        sendMeleeAbilities();
        sendDefenceAbilities();
        sendLegacyIcons();
    }

    public void setWindowInterfaceByKey(int key, int interfaceId) {
        setWindowInterfaceByKey(true, key, interfaceId);
    }

    public void setWindowInterfaceByKey(boolean walkable, int key, int interfaceId) {
        setWindowInterfaceByKey(walkable, key, 3505, interfaceId);
    }

    public void setWindowInterfaceByKey(int key, int gmapKey, int interfaceId) {
        setWindowInterfaceByKey(true, key, gmapKey, interfaceId);
    }

    public void setWindowInterfaceByKey(boolean walkAble, int key, int gmapKey, int interfaceId) {
        int componentId = getComponentIdByKey(7716, key, gmapKey);
        setWindowInterface(walkAble, componentId, interfaceId);
    }

    public void sendIComponentSettingsByKey(int enumId, int keyId, int gmapKey, int interfaceId, int fromSlot, int toSlot, int settings) {
        int componentId = getComponentIdByKey(enumId, keyId, gmapKey);
        player.getPackets().sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settings);
    }

    public void sendHideIComponentByKey(int enumId, int keyId, int gmapKey, int interfaceId, boolean hidden) {
        int componentId = getComponentIdByKey(enumId, keyId, gmapKey);
        player.getPackets().sendHideIComponent(interfaceId, componentId, hidden);
    }

    // ---------------------------------------------------------------- native 947

    /** Binding table the P5 router installs; null on the legacy cache. */
    private static volatile com.rs.game.player.client.ui.Native950Bindings native947Bindings;
    private static final java.util.Set<String> NATIVE_SLOT_WARNINGS =
            java.util.Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * Installs the verified 947 binding table so HUD slot lookups on the flat
     * cache use its validated attach/wrapper hashes (see {@link #getComponentIdByKey}).
     * Legacy lookups never consult it.
     */
    public static void setNative950Bindings(com.rs.game.player.client.ui.Native950Bindings bindings) {
        native947Bindings = bindings;
    }

    public static com.rs.game.player.client.ui.Native950Bindings getNative950Bindings() {
        return native947Bindings;
    }

    /**
     * Records an interface the native 947 recipes opened without going through
     * {@link #setInterface}: the open packets were already written by the verified
     * recipe, so only the bookkeeping is updated and no facade call is made. After
     * this, {@link #containsInterface}, {@link #getInterfaceParentId} and the
     * {@code containsXInterface} gates of the 910 handlers see the interface exactly
     * as if the 910 code had opened it. Native players only.
     *
     * @param interfaceId the 947 interface now attached
     * @param parent      the parent interface of the attach hash (the root 1477 for HUD slots)
     * @param component   the parent component of the attach hash (full 16-bit id, never masked)
     */
    public void registerNativeOpen(int interfaceId, int parent, int component) {
        if (!player.isNative950())
            throw new IllegalStateException("registerNativeOpen is only for native 947 players");
        int parentUID = getComponentUId(parent, component);
        int oldParentUID = getInterfaceParentId(interfaceId);
        if (oldParentUID != -1 && oldParentUID != parentUID)
            openedinterfaces.remove(oldParentUID, interfaceId);
        openedinterfaces.put(parentUID, interfaceId);
    }

    /**
     * Drops the bookkeeping for an interface the native 947 recipes closed. Native
     * players only; no packet is emitted.
     *
     * @return true when the interface was registered
     */
    public boolean unregisterNativeOpen(int interfaceId) {
        if (!player.isNative950())
            throw new IllegalStateException("unregisterNativeOpen is only for native 947 players");
        int parentUID = getInterfaceParentId(interfaceId);
        if (parentUID == -1)
            return false;
        openedinterfaces.remove(parentUID);
        return true;
    }

    /**
     * Flat-cache (947) slot resolution. The legacy code masks the struct value with
     * {@code & 0xfff}, which throws away the parent interface and would silently
     * accept a hash whose parent is not the root. On the 947 cache the value is a
     * full 32-bit {@code parent << 16 | component} hash: when the binding table
     * declares the slot (matched by its enum key) the table's validated hash is
     * used, otherwise the raw hash is accepted only when its parent is the fixed
     * root window. Anything else is -1 (logged once) so a caller cannot address a
     * component of the wrong interface.
     */
    private static int native947SlotComponent(int enumId, int keyId, int gmapKey, int rawHash) {
        com.rs.game.player.client.ui.Native950Bindings bindings = native947Bindings;
        if (bindings != null && enumId == bindings.slotEnum()) {
            for (String name : bindings.slotNames()) {
                com.rs.game.player.client.ui.Native950Bindings.Slot slot = bindings.slot(name);
                if (slot.enumKey != keyId)
                    continue;
                if (gmapKey == 3505)
                    return slot.attach & 0xffff;
                if (gmapKey == 3503)
                    return slot.wrapper & 0xffff;
                break;
            }
        }
        if (rawHash >= 0 && (rawHash >>> 16) == FIXED_WINDOW_ID)
            return rawHash & 0xffff;
        String key = enumId + ":" + keyId + ":" + gmapKey;
        if (NATIVE_SLOT_WARNINGS.add(key))
            System.out.println("[Ataraxia947] HUD slot enum " + enumId + " key " + keyId + " param " + gmapKey
                    + " resolves to " + rawHash + ", which is not a component of root " + FIXED_WINDOW_ID + "; rejected");
        return -1;
    }

    @Getter
    private int currentMenu;

    private static final int[] MENU_SLOT_COMPONENTS_ = { 3, 5, 7, 9, 11 };

    private static final int[] MENU_SUBMENU_VARS = { 18995, 29607, 18997, 18998, 18999, 19000, 19003, 19002, 30609, 19001, 36092, 36093 };

    public void sendLockGameTab(int tab, boolean lock, boolean closeMenu) {
        player.getPackets().sendRunScript(8862, lock ? 0 : 1, tab);
        if (closeMenu)
            closeMenu();
    }

    public void sendLockGameTab(int tab, boolean lock) {
        sendLockGameTab(tab, lock, true);
    }

    public boolean isMenuOpen() {
        return currentMenu != -1;
    }

    public void closeMenu() {
        if (currentMenu == 5)// GE
            sendLockGameTab(INVENTORY_TAB, false, false);
        setMenu(-1);
    }

    private void setMenu(int menu) {
        currentMenu = menu;
        //  if (player.getVarsManager().getBitValue(CosmeticsManager.COSMETIC_TYPE_MENU_VARBIT) != 0)
        // player.getCosmeticsManager().close();
        player.getPackets().sendGlobalConfig(2911, menu);
        for (int slot : MENU_SLOT_COMPONENTS_)
            if (containsInterfaceAtParent(1448, slot))
                removeInterfaceByParent(1448, slot);
        player.getPackets().sendHideIComponent(1448, 1, menu != -1);
    }

    public void switchMenu(int subMenu) {
        switchMenu(subMenu, 0);
    }

    public void switchMenu(int subMenu, int category) {
        if (!isMenuOpen())
            return;
        openMenu(currentMenu, subMenu, category, true);
    }

    public void openMenu(int menu, int subMenu) {
        openMenu(menu, subMenu, 0, false);
    }

    public void openMenu(int menu, int subMenu, int category) {
        openMenu(menu, subMenu, category, false);
    }

    public void openMenu(int menu, int subMenu, int category, boolean switchMenu) {
        if (player.isLocked()) {
            return;
        }
        if (player.isUnderCombat()) {
            closeMenu();
            player.getPackets().sendGameMessage("You can't do that while in combat.");
            return;
        }

        setMenu(menu);
        if (!switchMenu) {
            player.getVarsManager().forceSendVarBit(18994, menu);
            player.getVarsManager().forceSendVarBit(MENU_SUBMENU_VARS[menu], subMenu);
        }
        player.getSubMenus()[menu] = subMenu - 1;



        if (menu == 0) {
            if (subMenu == 1) { // Summary
                setMenuInterface(0, 320);
                setMenuInterface(1, 1446);
                player.getPackets().sendIComponentText(1446, 81, player.getDisplayName());
                player.getSkills().unlockSkills(true);
            } else if (subMenu == 2) { // Skills
                setMenuInterface(0, 1218);
                player.getSkills().sendSkillMenu(-1);
            } else if (subMenu == 3) { // Loadout
                setMenuInterface(0, 1474);
                setMenuInterface(1, 1463);
                setMenuInterface(2, 1462);
                player.getPackets().sendIComponentSettings(1474, 10, 0, 27, 4);
                player.getPackets().sendIComponentText(1463, 21, player.getDisplayName());
                player.getInventory().unlockInventoryOptions(true);
                player.getEquipment().unlockEquipment(true);

            } else if (subMenu == 5) { // Achievements
                setMenuInterface(0, 917);
                setMenuInterface(1, 1056);
                // ndCSVarInteger(1423, 63);
                player.getPackets().sendGlobalConfig(1424, 57);
                /*
                 * sendCSVarInteger(2017, 12); sendCSVarInteger(2018, 0);
                 * sendCSVarInteger(1963, -1); sendCSVarInteger(1964, -1);
                 * sendCSVarInteger(1966, -1); sendCSVarInteger(1965, -1);
                 * Runscripts: [8862, 2, 1] Runscripts: [8862, 3, 1]
                 */

            } else if (subMenu == 6) {
                setMenuInterface(0, 1628);
            }
        } else if (menu == 1) { // costomisations
            if (subMenu == 2) { // Wardrobe
                setMenuInterface(0, 1843);
                setInterface(true, 1843, 264, 1841);
                setInterface(true, 1841, 3, 1840);
                for (int i = 0; i < 8; i++)
                    setInterface(true, 1843, 252 + i, 1832 + i);
                player.getCosmeticsManager().open(CosmeticType.WARDROBE);
            } else if (subMenu == 3) { // animations
                setMenuInterface(0, 1311);
                player.getCosmeticsManager().open(CosmeticType.ANIMATION);
                // player.getPackets().sendExecuteScriptReverse(8862, 2, 0);
                // player.getPackets().sendExecuteScriptReverse(8862, 3, 0);
                for (int i = 0; i < 22; i++)
                    player.getPackets().sendUnlockIComponentOptionSlots(1311, 159 + i, 0, 3069, 0, 1);
                player.getPackets().sendIComponentSettings(1311, 470, 0, 3, 2);
                player.getPackets().sendIComponentSettings(1311, 498, 0, 204, 6);
                player.getPackets().sendExecuteScript(6874);
                /*
                 * Runscripts: [8862, 2, 0] Runscripts: [8862, 3, 0] sendCSVarInteger(2017, 12);
                 * sendCSVarInteger(2018, 0); sendCSVarInteger(2699, -1); Runscripts: [6874]
                 */
            } else if (subMenu == 4) { // appearance
                setMenuInterface(0, 1311);
                player.getCosmeticsManager().open(CosmeticType.APPEARENCE);
                // player.getPackets().sendExecuteScriptReverse(8862, 2, 0);
                // player.getPackets().sendExecuteScriptReverse(8862, 3, 0);
                player.getPackets().sendExecuteScript(2716, -1);
                for (int i = 0; i < 22; i++)
                    player.getPackets().sendUnlockIComponentOptionSlots(1311, 159 + i, 0, 3069, 0, 1);
                player.getPackets().sendIComponentSettings(1311, 470, 0, 3, 2);
                player.getPackets().sendIComponentSettings(1311, 498, 0, 204, 6);
                player.getPackets().sendExecuteScriptReverse(6874);

            } else if (subMenu == 5) { // titles
                setMenuInterface(0, 1311);
                player.getCosmeticsManager().open(CosmeticType.TITLE);
                // player.getPackets().sendExecuteScriptReverse(8862, 2, 0);
                // player.getPackets().sendExecuteScriptReverse(8862, 3, 0);
                player.getPackets().sendExecuteScript(2716, -1);
                for (int i = 0; i < 22; i++)
                    player.getPackets().sendUnlockIComponentOptionSlots(1311, 159 + i, 0, 3069, 0, 1);
                player.getPackets().sendIComponentSettings(1311, 470, 0, 3, 2);
                player.getPackets().sendIComponentSettings(1311, 498, 0, 204, 6);
                player.getPackets().sendExecuteScript(6874);

                /*
                 * Runscripts: [8862, 2, 0] Runscripts: [8862, 3, 0] Runscripts: [2716, -1]
                 * Runscripts: [6453, , 1] sendCSVarInteger(2017, 12); sendCSVarInteger(2018,
                 * 0); sendCSVarInteger(2017, 12); sendCSVarInteger(2018, 0);
                 * sendCSVarInteger(2699, -1); Runscripts: [6874]
                 */
            } else if (subMenu == 6) { // pets
                setMenuInterface(0, 1311);
                player.getCosmeticsManager().open(CosmeticType.PET);
                // player.getPackets().sendExecuteScriptReverse(8862, 2, 0);
                // player.getPackets().sendExecuteScriptReverse(8862, 3, 0);
                player.getPackets().sendExecuteScript(2716, -1);
                player.getPackets().sendExecuteScript(7422, -1, -1, 0, 0, 0);
                player.getPackets().sendExecuteScript(7425, "");
                for (int i = 0; i < 22; i++)
                    player.getPackets().sendUnlockIComponentOptionSlots(1311, 159 + i, 0, 3069, 0, 1);
                player.getPackets().sendIComponentSettings(1311, 470, 0, 3, 2);
                player.getPackets().sendIComponentSettings(1311, 498, 0, 204, 6);
                player.getPackets().sendExecuteScript(6874);
                player.getPackets().sendExecuteScript(2716, -1);
                player.getPackets().sendExecuteScript(3013, 85918266, 85918267, 0, 0, 0, 0, 1);
                player.getPackets().sendExecuteScript(3013, 85918271, 85918272, 0, 0, 0, 0, 1);
                player.getPackets().sendExecuteScript(7424, "Baby", "Warborn behemoth");
                player.getPackets().sendExecuteScript(7422, 17786, 20884, 0, 100, 100);
                player.getPackets().sendExecuteScript(10998);
                player.getPackets().sendExecuteScript(10999);
                player.getPackets().sendExecuteScript(11003, 1, 21091);
                player.getPackets().sendExecuteScript(6462);
            }
        } else if (menu == 2) { // powers
            player.getPackets().sendIComponentSettings(1477, 676, 0, 24, 2);
            player.getPackets().sendIComponentSettings(1477, 679, 1, 1, 2);
            player.getPackets().sendIComponentSettings(1477, 678, 1, 1, 2);
            if (subMenu == 1) { // melee
                setMenuInterface(0, 1450);
                setMenuInterface(1, 1454);
                setMenuInterface(2, 1435);
                setMenuInterface(3, 1436);
                player.getCombatDefinitions().unlockMeleeAbilities();
                player.getActionbar().unlockActionBar(true);
            } else if (subMenu == 2) { // ranged
                setMenuInterface(0, 1456);
                setMenuInterface(1, 1454);
                setMenuInterface(2, 1445);
                setMenuInterface(3, 1436);
                player.getCombatDefinitions().unlockRangeAbilities();
                player.getActionbar().unlockActionBar(true);
            } else if (subMenu == 3) { // magic
                setMenuInterface(0, 1459);
                setMenuInterface(1, 1454);
                setMenuInterface(2, 1437);
                setMenuInterface(3, 1436);
                player.getCombatDefinitions().unlockMagicAbilities();
                player.getActionbar().unlockActionBar(true);
            } else if (subMenu == 4) { // defensive
                // setWindowInterface(292, 1215);
                setMenuInterface(0, 1880);
                setMenuInterface(1, 1454);
                setMenuInterface(2, 1434);
                setMenuInterface(3, 1436);
                player.getCombatDefinitions().unlockDefenceAbilities();
                player.getActionbar().unlockActionBar(true);
            } else if (subMenu == 5) { // prayers
                setMenuInterface(0, 1457);
                setMenuInterface(1, 1454);
                setMenuInterface(2, 1439);
                setMenuInterface(3, 1436);
                player.getPrayer().unlockPrayerBookButtons(true);
                player.getActionbar().unlockActionBar(true);
            }
        }
/*
        else if (menu == 3) {
            switch (subMenu) {
                case 1:
                    openScrollTab(player, "Tab 1",
                            "Option A",
                            "Option B",
                            "Option C");
                    break;
                case 2:
                    openScrollTab(player, "Tab 2",
                            "Something here",
                            "Another line");
                    break;
                case 3:
                    openScrollTab(player, "Tab 3",
                            "Custom text 1",
                            "Custom text 2");
                    break;
                case 4:
                    openScrollTab(player, "Tab 4");
                    break;
                case 5:
                    openScrollTab(player, "Tab 5");
                    break;
                case 6:
                    openScrollTab(player, "Tab 6");
                    break;
            }
        }
*/

        else if (menu == 4) {// community
            if (subMenu == 3) {// social
                setMenuInterface(0, 1440);
                player.getPackets().sendIComponentSettings(1440, 10, 0, 200, 2);
                player.getPackets().sendIComponentSettings(1440, 62, 0, 600, 2);
                player.getPackets().sendIComponentSettings(1440, 60, 0, 600, 1042);
                player.getPackets().sendIComponentSettings(1440, 41, 0, 600, 1042);
                setMenuInterface(1, 1109);
                player.getPackets().sendIComponentSettings(1109, 32, 0, 600, 1024);
                setMenuInterface(2, 1438);
                player.getPackets().sendIComponentSettings(1438, 4, 0, 215, 8388614);
                setMenuInterface(3, 1441);
                setMenuInterface(4, 34);
                player.getNotes().unlockNotes(true);
//                ClansManager.unlockClanBanList(player, true);
            }
        }
        else if (menu == 5) { // ge
            if (subMenu == 1) {
                setMenuInterface(0, 105);
                setMenuInterface(1, 107);
                setMenuInterface(2, 651);
                setMenuInterface(3, 1666);
                player.getGEManager().openGrandExchange(true);
            } else if (subMenu == 2) {
                setMenuInterface(0, 1638);
                player.getGEManager().openHistory(true);
            } else if (subMenu == 3) {
                setMenuInterface(0, 1719);
                setMenuInterface(1, 1721);
                setMenuInterface(2, 1720);
                player.getGEManager().openItemSets(true);
            }
        }
        else if (menu == 7) {// extras
            if (subMenu == 1) {
                setMenuInterface(0, 1607);
                player.getPackets().sendIComponentText(1607, 30, "" + player.getTreasureHunter().getTotalKeysAmount());
                player.getPackets().sendIComponentText(1607, 33, "" + player.getTreasureHunter().getHeartsOfIce());
            }
        } else if (menu == 9) {// settings
            sendIComponentSettingsByKey(7716, 1001, 6320, 1477, 0, 24, 2);
            sendIComponentSettingsByKey(7716, 1001, 3508, 1477, 1, 1, 2);
            sendIComponentSettingsByKey(7716, 1001, 3506, 1477, 1, 1, 2);
            if (subMenu == 1) {// gameplay
                player.getPackets().sendExecuteScript(8320, 1001);
                player.resetGameSettingsInteractionsCount();
                player.resetSettingsTabClosed();
                player.refreshGameSettingsMenuId();
                unlockDropDownMenu(0, 500);
                setMenuInterface(0, 365);
                player.getPackets().sendIComponentSettings(365, 16, 0, 25, 2);
                player.getPackets().sendIComponentSettings(365, 11, 0, 34, 2);
                player.getPackets().sendIComponentSettings(365, 17, 0, 500, 30);
            } else if (subMenu == 2) {// graphic settings
                setMenuInterface(0, 1426);
                setInterface(true, 1426, 0, 742);
                unlockDropDownMenu(0, 18);
            } else if (subMenu == 3) {// controls
                setMenuInterface(0, 1444);
            } else if (subMenu == 4) { // audio settings
                setMenuInterface(0, 187);
                setMenuInterface(1, 429);
                player.getMusicsManager().refreshMusicInterface(true);
                player.getMusicsManager().unlockMusicPlayer(true);
            }
        }
    }

    public void setMenuInterface(int slot, int id) {
        setInterface(true, 1448, MENU_SLOT_COMPONENTS_[slot], id);
        player.getPackets().sendHideIComponent(1448, MENU_SLOT_COMPONENTS_[slot], false);
    }

    public void unlockDropDownMenu(int fromSlot, int toSlot) {
        player.getPackets().sendIComponentSettings(1477, DROP_DOWN_MENU_COMPONENT_ID, fromSlot, toSlot, 2);
        player.getPackets().sendHideIComponent(1477, DROP_DOWN_MENU_INTERFACE_COMPONENT_ID, true);
    }

    public boolean containsTreasureHunterInterface() {
        return containsInterface(1253);
    }

    public void openExtras() {
        if (player.isUnderCombat()) {
            player.getPackets().sendGameMessage("You can't do that while in combat.");
            return;
        }
        openMenu(7, player.getSubMenus()[7] + 1);
    }

    /**
     * Resolves a HUD slot component through enum {@code enumId}. Legacy cache:
     * the struct value masked with {@code & 0xfff}, byte-for-byte as before. Flat
     * 947 cache: the full hash validated against the root (or the binding table),
     * see {@link #native947SlotComponent}.
     */
    public static int getComponentIdByKey(int enumId, int keyId, int gmapKey) {
        int rawHash = GeneralRequirementMap.getMap(ClientScriptMap.getMap(enumId).getIntValue(keyId)).getIntValue(gmapKey);
        if (com.rs.cache.Cache.isFlatReadOnly())
            return native947SlotComponent(enumId, keyId, gmapKey, rawHash);
        return rawHash & 0xfff;
    }

    public void openEditMode() {
        openEditMode(false);
    }

    public void openEditMode(boolean fromSettings) {
        if (!fromSettings) {
            player.stopAll();
            closeMenu();
        }
        setWindowInterface(InterfaceManager.EDIT_MODE_COMPONENT_ID, 1475); // Edit
        // menu
        setEditMode(true);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                removeWindowInterface(InterfaceManager.EDIT_MODE_COMPONENT_ID);
                setEditMode(false);
            }
        });
    }

    private void setEditMode(boolean editMode) {
        player.getPackets().sendGlobalConfig(3477, editMode ? 1 : 0);
    }

    public void sendExpandOptionsInterface(int id) {
        setWindowInterface(EXPAND_BUTTONS_COMPONENT_ID, id);
        player.getPackets().sendIComponentSettings(1468, 2, 0, 11, 126);
        player.getPackets().sendIComponentSettings(1468, 5, 0, 15, 126);
    }

    public static int getNextStatus(int status) {
        return status == 2 ? 0 : (status + 1);
    }

    public void refreshInterface(boolean closing) {
        if (!player.getCutscenesManager().hasCutscene())
            player.getPackets().sendExecuteScript(7808);
        if (!closing)
            player.getPackets().sendExecuteScript(8178);
        player.getPackets().sendExecuteScript(1364);
    }

    public enum MainInterfaceComponents {
        AREA_STATUS(1014),

        BANK(1017),

        BOSS_TIMER(1021),

        BUFF_BAR(1009),

        BXP_COUNTDOWN(1036),

        CENTRAL_INTERFACE(1007),

        CENTRAL_INTERFACE_LARGE(1047),

        CENTRAL_OVERLAY_INTERFACE(1040),

        CLOCK(1025),

        CRAFTING_PROGRESS(1018),

        DAY_PLANNER(1046),

        DEBUFF_BAR(1038),

        DEBUG_TEXT(1029),

        DUNGEONEERING_MAP(1041),

        EVENTS(1037),

        EXTRA_ACTION_BUTTON(1045),

        GAME_DIALOG(1006),

        GAME_VIEW(1000),

        GRAVE_INTERFACE(1013),

        GRAVE_TIMER(1010),

        GROUP_INVITATIONS(1023),

        JMOD_TOOLBOX(1027),

        LOOT(1028),

        MINIGAME_HUD(1005),

        PLAYER_INSPECT(1024),

        SLAYER_COUNTER(1031),

        SUBSCRIBE(1016),

        TASK_COMPLETE(1012),

        XP_POPUPS(1026);

        private final int enumKey;

        MainInterfaceComponents(int enumKey) {
            this.enumKey = enumKey;
        }

        public int getComponentId() {
            GeneralRequirementMap gmap = GeneralRequirementMap.getMap(ClientScriptMap.getMap(7716).getIntValue(enumKey));
            return getComponentId(this == CENTRAL_OVERLAY_INTERFACE || gmap.getIntValue(3494) != 0 ? 3503 : 3505);
        }

        public int getComponentId(int gmapKey) {
            // Same resolution as getComponentIdByKey: legacy mask on the 910 cache,
            // validated full hash on the flat 947 cache.
            return getComponentIdByKey(7716, enumKey, gmapKey);
        }
    }

    private int interfaceSlotComponent(MainInterfaceComponents comp) {
        // Native bank redraw and compaction callbacks test the modal wrapper (struct3503).
        return player.isNative950() && comp == MainInterfaceComponents.BANK
                ? getComponentIdByKey(7716, 1017, 3503) : comp.getComponentId();
    }

    public void sendInterfaceIn(MainInterfaceComponents comp, int interfaceId) {
        if (player.isNative950() && comp == MainInterfaceComponents.BANK) {
            sendBankInterface(interfaceId);
            return;
        }
        setWindowInterface(comp.getComponentId(), interfaceId);
        refreshInterface(false);
    }

    public void removeInterfaceIn(MainInterfaceComponents comp) {
        if (player.isNative950() && comp == MainInterfaceComponents.BANK) {
            int interfaceId = getInterfaceIdIn(comp);
            if (interfaceId != -1) unregisterNativeOpen(interfaceId);
            return; // The native bank recipe owns the wire close and wrapper visibility.
        }
        removeWindowInterface(comp.getComponentId());
        refreshInterface(true);
    }

    public boolean containsInterfaceIn(MainInterfaceComponents comp) {
        return containsWindowInterfaceAtParent(interfaceSlotComponent(comp));
    }

    public boolean containsWindowInterfaceAtParent(int componentId) {
        return containsInterfaceAtParent(resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, componentId);
    }

    public int getInterfaceIdIn(MainInterfaceComponents comp) {
        if (!containsInterfaceIn(comp))
            return -1;
        return openedinterfaces.get(getComponentUId(1477, interfaceSlotComponent(comp)));
    }

    public void sendBankInterface(int interfaceId) {
        if (player.isNative950()) {
            // Bank.openBank supplies lifecycle state; the native recipe emits the sole mount.
            // Mounting here as well duplicates traversal and keyboard input for the same bank.
            registerNativeOpen(interfaceId, FIXED_WINDOW_ID, interfaceSlotComponent(MainInterfaceComponents.BANK));
            return;
        }
        setWindowInterface(MainInterfaceComponents.BANK.getComponentId(), interfaceId);
        sendLockGameTab(InterfaceManager.INVENTORY_TAB, true);
        sendLockGameTab(InterfaceManager.SUMMONING_TAB, true);
        sendLockGameTab(InterfaceManager.EQUIPMENT_TAB, true);
    }

    public boolean containsBankInterface() {
        return containsInterfaceIn(MainInterfaceComponents.BANK);
    }

    public void removeBankInterface() {
        boolean wasInBankInterface = getInterfaceIdIn(MainInterfaceComponents.BANK) == 517;
        removeInterfaceIn(MainInterfaceComponents.BANK);
        if (player.isNative950()) {
            if (wasInBankInterface) player.getBank().shiftItems();
            return; // Native close owns presentation and preserves saved preferences.
        }
        sendLockGameTab(InterfaceManager.INVENTORY_TAB, false);
        sendLockGameTab(InterfaceManager.SUMMONING_TAB, false);
        sendLockGameTab(InterfaceManager.EQUIPMENT_TAB, false);
        if (wasInBankInterface) {

            if (player.getBank().getWithdrawNotes())
                player.getBank().switchWithdrawNotes();
            player.getBank().refreshLeavePlaceHolders();
            player.getBank().refreshTabPosition();
            player.getBank().shiftItems();
            player.getBank().setSelectedPreset(0);
            player.getPackets().sendExecuteScript(9299);
            player.restoreBankAfterMetalBank();
        }
    }

    public void sendInputTextInterface() {
        sendInputTextInterface(1469);
    }

    public void sendInputTextInterface(int interfaceId) {
        if (player.isNative950())
            throw new UnsupportedOperationException("Native input requires an owned, verified quantity request");
        if (player.getTemporaryAttributtes().get("pluginString") == null && player.getTemporaryAttributtes().get("pluginLongString") == null)
            closeChatBoxInterface();
        setWindowInterface(MainInterfaceComponents.GAME_DIALOG.getComponentId(3503), 1418);
        setInterface(true, 1418, 2, interfaceId);
        refreshInterface(false);
    }

    public boolean containsInputTextInterface() {
        if (player.isNative950())
            return getInterfaceParentId(1418) == getComponentUId(1477, 749)
                    && getInterfaceParentId(1469) == getComponentUId(1418, 2);
        return containsWindowInterfaceAtParent(MainInterfaceComponents.GAME_DIALOG.getComponentId(3503));
    }

    public void removeInputTextInterface() {
        if (player.isNative950()) {
            if (player.getNative950Dialogues() != null) player.getNative950Dialogues().cancelInput();
            return;
        }
        removeWindowInterface(MainInterfaceComponents.GAME_DIALOG.getComponentId(3503));
        refreshInterface(true);
    }

    public static int[][] HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE = { /* { 1430, 5 }, */ /* { 1431, 2, 21, 49 }, */ { 517, 24 }, { 475, 25 }, { 11, 69 }, { 594, 26 }, { 389, 27 /* price checker search interface */ }, { 1183, 28 }, { 1253, 29 }, { 1585, 29 }, { 1870, 29 }, { 1092, 30 }, /* { 1422, 31, 32 }, */ /* { 1477, 1, 3, 4, 7, 18, 33, 42 }, */
            {105, 82}, { 219, 34 }, { 451, 35 }, { 131, 36 }, { 701, 37 }, { 91, 38 }, { 1179, 39 }, { 1370, 40 }, { 581, 43 }, { 600, 44 }, { 892, 45 }, { 1013, 46 }, { 1048, 48, 72 }, { 232, 52 }, { 1523, 53 }, { 1182, 54 }, { 1549, 56 }, { 138, 57 }, { 1552, 58 }, { 1081, 60 }, { 1587, 61 }, /* { 1465, 20, 61 }, */ { 1640, 62 }, { 1562, 63 }, { 796, 64 }, { 363, 65 }, { 998, 66 }, { 1692, 67 }, { 1706, 68 }, { 1622, 70 }, { 110, 71 }, { 1800, 73 }, { 1805, 74 }, { 1306, 75 }, { 1859, 76 }, { 1908, 77 }, { 1909, 78 }, { 1906, 79 }, { 1910, 80 }, { 1928, 81 }, /* { 1504, 5 }, */ /* { 1505, 5 }, */ /* { 1616, 5 }, */ { 1469, 10 }, { 1185, 12 }, { 1578, 13 }, { 1103, 14 }, { 1094, 15 }, { 1100, 16 }, { 1405, 17 }, { 1444, 22 }, { 850, 23 }, { 1387, 11 }, {1931, 65} };

    public void bindUnBindHotKeys(int interfaceId, boolean bind) {
        for (int i = 0; i < HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE.length; i++) {
            int interId = HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE[i][0];
            if (interId == interfaceId) {
                for (int j = 1; j < HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE[i].length; j++) {
                    int scriptIndex = HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE[i][j];
                    player.getPackets().sendExecuteScript(8841, scriptIndex, bind ? 1 : 0);
                }
            }
        }
    }

    public void sendWorldMapInterface(int id) {
        setWindowInterface(WORLD_MAP_COMPONENT_ID, id);
    }

    public boolean containsWorldMapInterface() {
        // The 910 map slot is the 947 game scene. Slot occupancy cannot identify
        // an open native map; use the adapter that owns its two attachments.
        if (player.isNative950())
            return native947WorldMap != null && native947WorldMap.isOpen();
        return containsWindowInterfaceAtParent(WORLD_MAP_COMPONENT_ID);
    }

    /** Connects legacy cleanup calls to the session's verified native map lifecycle. */
    public void setNative950WorldMap(Native950WorldMap worldMap) {
        if (!player.isNative950())
            throw new IllegalStateException("Native map ownership requires a native 947 player");
        native947WorldMap = java.util.Objects.requireNonNull(worldMap, "worldMap");
    }

    public void removeWorldMapInterface() {
        if (player.isNative950()) {
            if (native947WorldMap != null) native947WorldMap.close();
            return;
        }
        removeWindowInterface(WORLD_MAP_COMPONENT_ID);
        sendGameMapInterface();
        player.setNextAnimation(new Animation(22749));
        refreshInterface(true);
    }

    public void openSettings() {
        player.getPackets().sendExecuteScript(8177);
        // script 7997 hides everything except chat
    }

    public void closeSettings() {
        if (player.isNative950()) {
            if (native947Settings != null) native947Settings.close();
            return;
        }
        player.getPackets().sendExecuteScript(8179);
    }

    public void setNative950Settings(Native950Settings settings) {
        if (!player.isNative950())
            throw new IllegalStateException("Native settings ownership requires a native 947 player");
        native947Settings = java.util.Objects.requireNonNull(settings, "settings");
    }

    public boolean containsNative950Settings() {
        return native947Settings != null && native947Settings.isOpen();
    }

    /** Preserve native lodestone ownership when shared910 actions close interfaces. */
    public void setNative950ForgeUi(com.rs.game.player.client.Native950ForgeUi forge) {
        native950ForgeUi = java.util.Objects.requireNonNull(forge, "forge");
    }
    public void closeNative950ForgeUi() {
        if (native950ForgeUi != null) native950ForgeUi.close();
    }
    public boolean containsNative950ForgeUi() {
        return native950ForgeUi != null && native950ForgeUi.isOpen();
    }

    public void setNative950ToolbeltUi(com.rs.game.player.client.Native950ToolbeltUi belt) {
        native950ToolbeltUi = java.util.Objects.requireNonNull(belt, "belt");
    }
    public void closeNative950ToolbeltUi() {
        if (native950ToolbeltUi != null) native950ToolbeltUi.close();
    }
    public boolean containsNative950ToolbeltUi() {
        return native950ToolbeltUi != null && native950ToolbeltUi.isOpen();
    }

    public void setNative950ProductionMenu(com.rs.game.player.client.Native950ProductionMenu menu) {
        native950ProductionMenu = java.util.Objects.requireNonNull(menu, "menu");
    }
    public void closeNative950ProductionMenu() {
        if (native950ProductionMenu != null) native950ProductionMenu.close();
    }
    public boolean containsNative950ProductionMenu() {
        return native950ProductionMenu != null && native950ProductionMenu.isOpen();
    }

    public void setNative950SkillGuide(Native950SkillGuide guide) {
        native950SkillGuide = java.util.Objects.requireNonNull(guide, "guide");
    }
    public void closeNative950SkillGuide() {
        if (native950SkillGuide != null) native950SkillGuide.close();
    }
    public boolean containsNative950SkillGuide() {
        return native950SkillGuide != null && native950SkillGuide.isOpen();
    }

    public void setNative950Lodestones(Native950Lodestones lodestones) {
        if (!player.isNative950())
            throw new IllegalStateException("Native lodestone ownership requires a native950 player");
        native950Lodestones = java.util.Objects.requireNonNull(lodestones, "lodestones");
    }

    public void closeNative950Lodestones() {
        if (native950Lodestones != null) native950Lodestones.close();
    }

    public boolean containsNative950Lodestones() {
        return native950Lodestones != null && native950Lodestones.isOpen();
    }

    public void sendCentralOverlayInterface(int interfaceId) {
        sendInterfaceIn(MainInterfaceComponents.CENTRAL_OVERLAY_INTERFACE, interfaceId);
    }

    public void removeCentralOverlayInterface() {
        removeInterfaceIn(MainInterfaceComponents.CENTRAL_OVERLAY_INTERFACE);
    }

    public boolean containsCentralOverlayInterface() {
        return containsInterfaceIn(MainInterfaceComponents.CENTRAL_OVERLAY_INTERFACE);
    }

    public boolean containsCentralOverlayInterface(int interfaceId) {
        return containsCentralOverlayInterface() && openedinterfaces.get(getComponentUId(FIXED_WINDOW_ID, MainInterfaceComponents.CENTRAL_OVERLAY_INTERFACE.getComponentId())) == interfaceId;
    }

    public void sendMinigameHudInterface(int interfaceId) {
        sendInterfaceIn(MainInterfaceComponents.MINIGAME_HUD, interfaceId);
    }

    public void removeMinigameHudInterface() {
        removeInterfaceIn(MainInterfaceComponents.MINIGAME_HUD);
    }

    public boolean containsMinigameHudInterface() {
        return containsInterfaceIn(MainInterfaceComponents.MINIGAME_HUD);
    }

    public boolean containsMinigameHudInterface(int interfaceId) {
        return containsMinigameHudInterface() && openedinterfaces.get(getComponentUId(FIXED_WINDOW_ID, MainInterfaceComponents.MINIGAME_HUD.getComponentId())) == interfaceId;
    }

    public void sendMinigameTab(int interfaceId) {
        sendMinigameTab(interfaceId, true);
    }

    public void sendMinigameTab(int interfaceId, boolean showCloseButton) {
        setWindowInterfaceByKey(17, 3505, interfaceId);
        sendLockGameTab(MINIGAME_TAB, false);
        player.getPackets().sendConfig(3871, showCloseButton ? 1 : 0);
    }

    public boolean containsInventoryInter() {
        return containsWindowInterfaceAtParent(InterfaceManager.getComponentIdByKey(7716, 1008, 3504) + 1);
    }

    public void closeMinigameTab() {
        removeWindowInterface(InterfaceManager.getComponentIdByKey(7716, 17, 3505));
        refreshInterface(true);
        sendLockGameTab(MINIGAME_TAB, true);
    }

    public void sendInventoryInterface(int interfaceId) {
        setInterface(false, FIXED_WINDOW_ID, InterfaceManager.getComponentIdByKey(7716, 1008, 3504) + 1, interfaceId);
        sendLockGameTab(InterfaceManager.INVENTORY_TAB, true);
        refreshInterface(false);
    }

    public void closeInventoryInterface() {
        removeWindowInterface(InterfaceManager.getComponentIdByKey(7716, 1008, 3504) + 1);
        sendLockGameTab(InterfaceManager.INVENTORY_TAB, false);
        refreshInterface(true);
    }

    public void replaceInventoryTab(int interfaceId) {
        setWindowInterfaceByKey(2, interfaceId);
    }

    public void sendInventoryTab() {
        setWindowInterfaceByKey(2, 1473);
        player.getInventory().unlockInventoryOptions(false);
    }

    public void sendCentralInterfaceLargeInterface(int interfaceId) {
        closeChatBoxInterface();
        removeInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE);
        sendInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE_LARGE, interfaceId);
    }

    public void removeCentralInterfaceLargeInterface() {
        removeInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE_LARGE);
    }

    public boolean containsCentralInterfaceLargeInterface() {
        return containsInterfaceIn(MainInterfaceComponents.CENTRAL_INTERFACE_LARGE);
    }

    public boolean containsCentralInterfaceLargeInterface(int interfaceId) {
        return containsCentralInterfaceLargeInterface() && openedinterfaces.get(getComponentUId(FIXED_WINDOW_ID, MainInterfaceComponents.CENTRAL_INTERFACE_LARGE.getComponentId())) == interfaceId;
    }

    public void sendCombatTargetInterface() {
        removeInterface(1488);
        if (player.isTargetInformationUnDocked())
            setWindowInterfaceByKey(2008, 3503, 1488);
        else
            setWindowInterface(523, 1488);
    }

    public void sendExpandOptionsInterface(int id, int interfaceId, int componentId, int width, int height) {
        setWindowInterface(EXPAND_BUTTONS_COMPONENT_ID, id);
        player.getPackets().sendExecuteScript(8787, width, height, InterfaceManager.getComponentUId(interfaceId, componentId), -1, 2, -24, -6);
    }

    public void sendLoadingScreen(int interfaceId) {
        setInterface(true, FIXED_WINDOW_ID, LOADING_SCREEN_COMPONENT_ID, interfaceId);
        refreshInterface(false);
    }

    public boolean containsLoadingScreenInterface() {
        return containsWindowInterfaceAtParent(LOADING_SCREEN_COMPONENT_ID);
    }

    public boolean containsLoadingScreenInterface(int interfaceId) {
        return containsLoadingScreenInterface() && openedinterfaces.get(getComponentUId(FIXED_WINDOW_ID, LOADING_SCREEN_COMPONENT_ID)) == interfaceId;
    }

    public void closeLoadingScreen() {
        removeWindowInterface(LOADING_SCREEN_COMPONENT_ID);
        refreshInterface(true);
    }

    public void sendPlayerInspectInterface(int interfaceId) {
        sendInterfaceIn(MainInterfaceComponents.PLAYER_INSPECT, interfaceId);
    }

    public boolean containsPlayerInspectInterface() {
        return containsInterfaceIn(MainInterfaceComponents.PLAYER_INSPECT);
    }

    public void removePlayerInspectInterface() {
        removeInterfaceIn(MainInterfaceComponents.PLAYER_INSPECT);
    }

    public void sendJModToolBoxInterface() {
        sendInterfaceIn(MainInterfaceComponents.JMOD_TOOLBOX, 1610);
        sendIComponentText(1610, 85, "<col=00e1ff>GodMode");
        sendIComponentText(1610, 77, "<col=00e1ff>Commands");
        sendIComponentText(1610, 172, "<col=00e1ff>PartyHats");
        sendIComponentText(1610, 123, "<col=00e1ff>donator Zones");
        sendIComponentText(1610, 131, "<col=00e1ff>donator Perks");
        sendIComponentText(1610, 236, "<col=00e1ff>HelpFull ;;Commands");
        sendIComponentText(1610, 164, "<col=0000ff>BLOCKED");
        sendIComponentText(1610, 69, "<col=0000ff>BLOCKED");
        sendIComponentText(1610, 180, "<col=0000ff>BLOCKED");
        sendIComponentText(1610, 139, "<col=00e1ff>Bank");
        sendIComponentText(1610, 273, "<col=00e1ff>Give 2bill Cash");
        sendIComponentText(1610, 256, "<col=00e1ff>Teleports");
        sendIComponentText(1610, 148, "<col=00e1ff>Transfrom into Npc");
        sendIComponentText(1610, 196, "<col=00e1ff>Mod");
        sendIComponentText(1610, 140, "<col=00e1ff>Mods:");
        sendIComponentText(1610, 27, "<col=00e1ff>50 Npc To Transfrom Into:");
        sendIComponentText(1610, 264, "<col=00e1ff>Abilitys");
        sendIComponentText(1610, 156, "<col=00e1ff>Boss Teleports");
        sendIComponentText(1610, 294, "<col=00e1ff>TreasureHunter keys");
        sendIComponentText(1610, 302, "<col=00e1ff>EndGame Gear");
    }
    public boolean containsJModToolBoxInterface() {
        return containsInterfaceIn(MainInterfaceComponents.JMOD_TOOLBOX);
    }
    public void removeJModToolBoxInterface() {
        removeInterfaceIn(MainInterfaceComponents.JMOD_TOOLBOX);
    }
    public void openFreeCam() {
        player.getPackets().sendGlobalConfig(4718, 0);
        player.getInterfaceManager().setWindowInterface(FREE_CAM_COMPONENT_ID, 475);
        player.getInterfaceManager().refreshInterface(false);
        player.setCloseInterfacesEvent(new Runnable() {

            @Override
            public void run() {
                removeWindowInterface(FREE_CAM_COMPONENT_ID);
                refreshInterface(true);
            }

        });
    }


}
