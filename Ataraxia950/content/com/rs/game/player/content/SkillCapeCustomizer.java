package com.rs.game.player.content;

import java.util.Arrays;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.InputIntegerEvent;

public final class SkillCapeCustomizer {

    private SkillCapeCustomizer() {

    }

    public static int getCapeId(Player player) {
        return player.getVarBitManager().getValue(8573);
    }

    public static void handleSkillCapeCustomizer(Player player, int buttonId, int packetId) {
        int capeId = getCapeId(player);
        if (capeId == -1)
            return;
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(capeId);
        int type = defs.getCSOpcode(7896);
        if (type <= 0)
            return;
        int[] skillCape = type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized();
        if (buttonId == 103) { // reset
            sendConfirmAction(player, "Reset Colours", "Are you sure you want to reset your cape colours?", new Runnable() {

                @Override
                public void run() {
                    if (type == 1) {
                        player.setMaxedCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(capeId).originalModelColors, 4));
                    } else if (type == 2) {
                        player.setCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(capeId).originalModelColors, 4));
                    } else if (type == 3) {
                        player.setTrimmedCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(capeId).originalModelColors, 4));
                    }
                    player.getAppearence().generateAppearenceData();
                    for (int i = 0; i < 4; i++) {
                        player.getVarBitManager().sendVarBit(getVarbitId(capeId, i), skillCape[i]);
                    }
                    refreshComponentColors(player);
                }
            });
        } else if (buttonId == 10) { // detail top
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.getTemporaryAttributtes().put("SkillcapeCustomize", 0);
                player.getInterfaceManager().sendInterface(1106);
                player.getVarBitManager().sendVar(1111, skillCape[0]);
            } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                setHSL(player, capeId, 0);
            else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                getHSL(player, capeId, 0);
        } else if (buttonId == 7) { // background top
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.getTemporaryAttributtes().put("SkillcapeCustomize", 1);
                player.getInterfaceManager().sendInterface(1106);
                player.getVarBitManager().sendVar(1111, skillCape[1]);
            } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                setHSL(player, capeId, 1);
            else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                getHSL(player, capeId, 1);
        } else if (buttonId == 4) { // detail button
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.getTemporaryAttributtes().put("SkillcapeCustomize", 2);
                player.getInterfaceManager().sendInterface(1106);
                player.getVarBitManager().sendVar(1111, skillCape[2]);
            } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                setHSL(player, capeId, 2);
            else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                getHSL(player, capeId, 2);
        } else if (buttonId == 1) { // background button
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.getTemporaryAttributtes().put("SkillcapeCustomize", 3);
                player.getInterfaceManager().sendInterface(1106);
                player.getVarBitManager().sendVar(1111, skillCape[3]);
            } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                setHSL(player, capeId, 3);
            else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                getHSL(player, capeId, 3);
        } else if (buttonId >= 33 && buttonId <= 85) {
            int toPartId = buttonId >= 33 && buttonId <= 37 ? 0 : buttonId >= 53 && buttonId <= 57 ? 1 : buttonId >= 67 && buttonId <= 71 ? 2 : 3;
            int fromPartId = buttonId == 53 || buttonId == 67 || buttonId == 81 ? 0 : buttonId == 33 || buttonId == 69 || buttonId == 83 ? 1 : buttonId == 35 || buttonId == 55 || buttonId == 85 ? 2 : 3;
            skillCape[toPartId] = getColourValue(player, capeId, fromPartId);
            player.getAppearence().generateAppearenceData();
            player.getVarBitManager().sendVarBit(getVarbitId(capeId, toPartId), skillCape[toPartId]);
            player.getInterfaceManager().closeScreenInterface();
            startCustomizing(player, capeId);
        } else if (buttonId >= 128 && buttonId <= 131) {
            getHSL(player, player.getCapeCustomizationPresets()[0][buttonId - 128]);
        } else if (buttonId >= 189 && buttonId <= 192) {
            getHSL(player, player.getCapeCustomizationPresets()[1][buttonId - 189]);
        } else if (buttonId >= 193 && buttonId <= 196) {
            getHSL(player, player.getCapeCustomizationPresets()[2][buttonId - 193]);
        } else if (buttonId == 151 || buttonId == 158 || buttonId == 165) {
            sendConfirmAction(player, new Runnable() {

                @Override
                public void run() {
                    player.setCapeCustomizationPresets((buttonId - 151) / 7, Arrays.copyOf(skillCape, 4));
                }
            });
        } else if (buttonId == 172 || buttonId == 179 || buttonId == 186) {
            sendConfirmAction(player, new Runnable() {

                @Override
                public void run() {
                    if (type == 1) {
                        player.setMaxedCapeCustomized(Arrays.copyOf(player.getCapeCustomizationPresets()[(buttonId - 172) / 7], 4));
                    } else if (type == 2) {
                        player.setCompletionistCapeCustomized(Arrays.copyOf(player.getCapeCustomizationPresets()[(buttonId - 172) / 7], 4));
                    } else if (type == 3) {
                        player.setTrimmedCompletionistCapeCustomized(Arrays.copyOf(player.getCapeCustomizationPresets()[(buttonId - 172) / 7], 4));
                    }
                    player.getAppearence().generateAppearenceData();
                    for (int i = 0; i < 4; i++) {
                        player.getVarBitManager().sendVarBit(getVarbitId(capeId, i), skillCape[i]);
                    }
                    refreshComponentColors(player);
                }
            });
        } else if (buttonId == 109 || buttonId == 99) { // done / close
            player.getAppearence().generateAppearenceData();
            player.closeInterfaces();
        }
    }

    public static void handleSkillCapeCustomizerColor(Player player, int colorId) {
        int capeId = getCapeId(player);
        if (capeId == -1)
            return;
        Integer part = (Integer) player.getTemporaryAttributtes().get("SkillcapeCustomize");
        if (part == null)
            return;
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(capeId);
        int type = defs.getCSOpcode(7896);
        if (type <= 0)
            return;
        int[] skillCape = type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized();
        skillCape[part] = colorId;
        player.getAppearence().generateAppearenceData();
        player.getVarBitManager().sendVarBit(getVarbitId(capeId, part), colorId);
        player.getInterfaceManager().sendInterface(20);
    }

    public static void resetSkillCapes(Player player) {
        player.setMaxedCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20767).originalModelColors, 4));
        player.setCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20769).originalModelColors, 4));
        player.setTrimmedCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20771).originalModelColors, 4));
    }

    public static void startCustomizing(Player player, int itemId) {
        ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
        int type = def.getCSOpcode(7896);
        if (type <= 0)
            return;
        int[] skillCape = type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized();
        player.getVarBitManager().sendVar(8573, itemId);
        for (int i = 0; i < 4; i++)
            player.getVarBitManager().sendVarBit(getVarbitId(itemId, i), skillCape[i]);
        player.getInterfaceManager().sendInterface(20);
        player.refreshCapeCustomizationPresets();
        int modelId = itemId == 20768 || itemId == 32151 || itemId == 20767 ? 65300 : itemId == 20769 || itemId == 20770 || itemId == 32152 || itemId == 47885 ? 65297 : 65295;
        player.getPackets().sendIComponentModel(20, 102, modelId);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                refreshComponentColors(player);
            }
        });
        player.setCloseInterfacesEvent(new Runnable() {

            @Override
            public void run() {
                player.getAppearence().generateAppearenceData();
            }

        });
    }

    public static void handleCostumeColor(Player player, int color) {
        player.closeInterfaces();
    }

    public static int getVarbitId(int capeId, int partId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(capeId);
        int type = defs.getCSOpcode(7896);
        if (type <= 0)
            return -1;
        switch (type) {
        case 2:
            return 1039 + partId;
        case 1:
            return 44125 + partId;
        case 3:
            return 44129 + partId;
        }
        return -1;
    }

    public static void refreshComponentColors(Player player) {
        int[] componentIds = { 20, 23, 26, 29 };
        for (int comp : componentIds)
            player.getPackets().sendExecuteScript(4614, InterfaceManager.getComponentUId(20, comp));
        for (int i = 33; i <= 85; i++)
            player.getPackets().sendExecuteScript(4615, InterfaceManager.getComponentUId(20, i));
        player.getPackets().sendExecuteScript(4611);
    }

    public static int getColourValue(Player player, int capeId, int partId) {
        return player.getVarBitManager().getBitValue(getVarbitId(capeId, partId));
    }

    public static boolean isCustomizable(int itemId) {
        return ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(7896) > 0;
    }

    public static void sendConfirmAction(Player player, Runnable onAccept) {
        sendConfirmAction(player, null, null, onAccept);
    }

    public static void sendConfirmAction(Player player, String title, String question, Runnable onAccept) {
        final int capeId = getCapeId(player);
        if (capeId <= 0)
            return;
        player.setCloseInterfacesEvent(null);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                if (title != null && question != null) {
                    player.getPackets().sendExecuteScript(9554, InterfaceManager.getComponentUId(20, 199), InterfaceManager.getComponentUId(20, 204), InterfaceManager.getComponentUId(20, 200), title, 21217);
                    player.getPackets().sendHideIComponent(20, 210, true);
                    player.getPackets().sendIComponentText(20, 201, question);
                    player.getPackets().sendHideIComponent(20, 96, false);
                }
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (componentId == 209)
                    onAccept.run();
                player.getInterfaceManager().closeScreenInterface();
                startCustomizing(player, capeId);
            }

            @Override
            public void finish() {
                player.getAppearence().generateAppearenceData();
            }
        });
    }

    public static void getHSL(Player player, int capeId, int partId) {
        int hslColor = getColourValue(player, capeId, partId) & 0xffff;
        getHSL(player, hslColor);
    }

    public static void getHSL(Player player, int hslColor) {
        int hue = hslColor >> 10 & 63;
        int sat = hslColor >> 7 & 7;
        int lum = hslColor & 127;
        player.getPackets().sendGameMessage("Hue:" + hue + " Saturation:" + sat + " Luminosity:" + lum);
    }

    public static void setHSL(Player player, int capeId, int partId) {
        player.sendInputInteger("Enter Hue (0-63):", new InputIntegerEvent() {

            @Override
            public void run(Player player) {
                final int hue = getInteger() > 63 ? 63 : getInteger() < 0 ? 0 : getInteger();
                player.sendInputInteger("Enter Saturation (0-7):", new InputIntegerEvent() {

                    @Override
                    public void run(Player player) {
                        final int sat = getInteger() > 7 ? 7 : getInteger() < 0 ? 0 : getInteger();
                        player.sendInputInteger("Enter Luminosity (0-127):", new InputIntegerEvent() {

                            @Override
                            public void run(Player player) {
                                final int lum = getInteger() > 127 ? 127 : getInteger() < 0 ? 0 : getInteger();
                                int hsl = (hue << 10 | sat << 7 | lum) & 0xffff;
                                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(capeId);
                                int type = defs.getCSOpcode(7896);
                                if (type <= 0)
                                    return;
                                int[] skillCape = type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized();
                                skillCape[partId] = hsl;
                                player.getAppearence().generateAppearenceData();
                                player.getVarBitManager().sendVarBit(getVarbitId(capeId, partId), hsl);
                                player.getInterfaceManager().closeScreenInterface();
                                refreshComponentColors(player);
                                startCustomizing(player, capeId);
                            }
                        });

                    }
                });

            }
        });
    }
}
