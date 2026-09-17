package com.rs.game.player.dialogue;

import java.security.InvalidParameterException;
import java.util.function.Consumer;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.impl.SingleDialogue;
import com.rs.game.player.dialogue.impl.SinglePlayerDialogue;

public abstract class Dialogue {

    public static final byte IS_NPC = 1;
    public static final int OPTION_1 = 8, OPTION_2 = 13, OPTION_3 = 18, OPTION_4 = 23, OPTION_5 = 28;
    public static final int WHY1 = 10;
    protected static final short SEND_1_TEXT_INFO = 210;
    protected static final short SEND_2_TEXT_INFO = 211;
    protected static final short SEND_3_TEXT_INFO = 212;
    protected static final short SEND_4_TEXT_INFO = 213;
    protected static final String SEND_DEFAULT_OPTIONS_TITLE = "Select an option.";
    protected static final short SEND_2_OPTIONS = 236;
    protected static final short SEND_3_OPTIONS = 230;
    protected static final short SEND_4_OPTIONS = 237;
    protected static final short SEND_5_OPTIONS = 238;
    protected static final short SEND_2_LARGE_OPTIONS = 229;
    protected static final short SEND_3_LARGE_OPTIONS = 231;
    protected static final short SEND_1_TEXT_CHAT = 241;
    protected static final short SEND_2_TEXT_CHAT = 242;
    protected static final short SEND_3_TEXT_CHAT = 243;
    protected static final short SEND_4_TEXT_CHAT = 244;
    protected static final short SEND_NO_CONTINUE_1_TEXT_CHAT = 245;
    protected static final short SEND_NO_CONTINUE_2_TEXT_CHAT = 246;
    protected static final short SEND_NO_CONTINUE_3_TEXT_CHAT = 247;
    protected static final short SEND_NO_CONTINUE_4_TEXT_CHAT = 248;
    protected static final short SEND_NO_EMOTE = -1;
    protected static final byte IS_NOTHING = -1;
    protected static final byte IS_PLAYER = 0;
    protected static final byte IS_ITEM = 2;
    protected static final byte IS_PETS = 3;
    protected static final short THE_OPTION = 1185;

    public static final int NO_EXPRESSION = 9760, SAD = 9764, SAD_TWO = 9768, NO_EXPRESSION_TWO = 9772, WHY = 9776;
    public static final int SCARED = 9780, MIDLY_ANGRY = 9784, ANGRY = 9788, VERY_ANGRY = 9792, ANGRY_TWO = 9796;
    public static final int JUST_LISTEN = 9804, CALM_TALKING = 9808, CALM = 9808, LOOK_DOWN = 9812;
    public static final int WHAT_THE = 9816, WHAT_THE_TWO = 9820, CROOKED_HEAD = 9828;
    public static final int GLANCE_DOWN = 9832, UNSURE = 9836, LISTEN_LAUGH = 9840, TALK_SWING = 9844, NORMAL = 9847;
    public static final int GOOFY_LAUGH = 9851, ANGERY = 9790;
    public static final int QUESTIONS = 9827, MOCK = 9878, HAPPY = 9850, CONFUSED = 9830, MAD = 9785, LAUGHING = 9840;
    public static final int HAPPY_FACE = 9843;
    public static final int ASKING_FACE = 9829;
    public static final int BLANK_FACE = 9772;
    public static final int SAD_FACE = 9768;
    public static final int UPSET_FACE = 9776;
    public static final int SCARED_FACE = 9780;
    public static final int MILDLY_ANGRY_FACE = 9784;
    public static final int ANGRY_FACE = 9788;
    public static final int VERY_ANGRY_FACE = 9792;
    public static final int MANIAC_FACE = 9800;
    public static final int NOT_TALKING_JUST_LISTENING_FACE = 9804;
    public static final int PLAIN_TALKING_FACE = 9808;
    public static final int WTF_FACE = 9820;
    public static final int SHAKING_NO_FACE = 9824;
    public static final int LISTENS_THEN_LAUGHS_FACE = 9840;
    public static final int GOOFY_LAUGH_FACE = 9851;
    public static final int THINKING_THEN_TALKING_FACE = 9859;
    public static final int NONONO_FACE = 9844;
    public static final int SAD_WTF_FACE = 9776;

    protected Player player;

    public Object[] parameters;
    protected byte stage = -1;

    public static void sendSingleItemDialogue(Player player, int id, int amount, String s) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendItemDialogue(id, amount, s);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
            }

            @Override
            public void finish() {

            }
        });
    }

    public void setStage(int stage) {
        this.stage = (byte) stage;
    }

    protected int componentToOption(int componentId) {
        switch (componentId) {
            case OPTION_1:
                return 1;
            case OPTION_2:
                return 2;
            case OPTION_3:
                return 3;
            case OPTION_4:
                return 4;
            case OPTION_5:
                return 5;
        }
        throw new IllegalStateException("Invalid component: " + componentId);
    }

    protected static final int componentAsIndex(final int componentId) {
        return (componentId - 8) / 5;
    }

    private static int[] getIComponentsIds(short interId) {
        int[] childOptions;
        switch (interId) {

            case SEND_1_TEXT_INFO:
                childOptions = new int[1];
                childOptions[0] = 1;
                break;
            case SEND_2_TEXT_INFO:
                childOptions = new int[2];
                childOptions[0] = 1;
                childOptions[1] = 2;
                break;
            case SEND_3_TEXT_INFO:
                childOptions = new int[3];
                childOptions[0] = 1;
                childOptions[1] = 2;
                childOptions[2] = 3;
                break;
            case SEND_4_TEXT_INFO:
                childOptions = new int[4];
                childOptions[0] = 1;
                childOptions[1] = 2;
                childOptions[2] = 3;
                childOptions[3] = 4;
                break;
            case SEND_2_LARGE_OPTIONS:
                childOptions = new int[3];
                childOptions[0] = 1;
                childOptions[1] = 2;
                childOptions[2] = 3;
                break;
            case SEND_3_LARGE_OPTIONS:
                childOptions = new int[4];
                childOptions[0] = 1;
                childOptions[1] = 2;
                childOptions[2] = 3;
                childOptions[3] = 4;
                break;
            case SEND_2_OPTIONS:
                childOptions = new int[3];
                childOptions[0] = 0;
                childOptions[1] = 1;
                childOptions[2] = 2;
                break;
            case SEND_3_OPTIONS:
                childOptions = new int[4];
                childOptions[0] = 1;
                childOptions[1] = 2;
                childOptions[2] = 3;
                childOptions[3] = 4;
                break;
            case SEND_4_OPTIONS:
                childOptions = new int[5];
                childOptions[0] = 0;
                childOptions[1] = 1;
                childOptions[2] = 2;
                childOptions[3] = 3;
                childOptions[4] = 4;
                break;
            case SEND_5_OPTIONS:
                childOptions = new int[6];
                childOptions[0] = 0;
                childOptions[1] = 1;
                childOptions[2] = 2;
                childOptions[3] = 3;
                childOptions[4] = 4;
                childOptions[5] = 5;
                break;
            case THE_OPTION:
                childOptions = new int[3];
                childOptions[0] = 0;
                childOptions[1] = 1;
                childOptions[2] = 2;
                break;
            case SEND_1_TEXT_CHAT:
            case SEND_NO_CONTINUE_1_TEXT_CHAT:
                childOptions = new int[2];
                childOptions[0] = 3;
                childOptions[1] = 4;
                break;
            case SEND_2_TEXT_CHAT:
            case SEND_NO_CONTINUE_2_TEXT_CHAT:
                childOptions = new int[3];
                childOptions[0] = 3;
                childOptions[1] = 4;
                childOptions[2] = 5;
                break;
            case SEND_3_TEXT_CHAT:
            case SEND_NO_CONTINUE_3_TEXT_CHAT:
                childOptions = new int[4];
                childOptions[0] = 3;
                childOptions[1] = 4;
                childOptions[2] = 5;
                childOptions[3] = 6;
                break;
            case SEND_4_TEXT_CHAT:
            case SEND_NO_CONTINUE_4_TEXT_CHAT:
                childOptions = new int[5];
                childOptions[0] = 3;
                childOptions[1] = 4;
                childOptions[2] = 5;
                childOptions[3] = 6;
                childOptions[4] = 7;
                break;
            default:
                return null;
        }
        return childOptions;
    }

    public static boolean sendNPCDialogueNoContinue(Player player, int npcId, int animationId, String... text) {
        return sendEntityDialogueNoContinue(player, IS_NPC, npcId, animationId, text);
    }

    public static boolean sendPlayerDialogueNoContinue(Player player, int animationId, String... text) {
        return sendEntityDialogueNoContinue(player, IS_PLAYER, -1, animationId, text);
    }

    /*
     *
     * auto selects title, new dialogues
     */
    public static boolean sendEntityDialogueNoContinue(Player player, int type, int entityId, int animationId, String... text) {
        String title = "";
        if (type == IS_PLAYER) {
            title = player.getDisplayName();
        } else if (type == IS_NPC) {
            title = NPCDefinitions.getNPCDefinitions(entityId).name;
        } else if (type == IS_ITEM)
            title = ItemDefinitions.getItemDefinitions(entityId).getName();
        return sendEntityDialogueNoContinue(player, type, title, entityId, animationId, text);
    }

    public static boolean sendEntityDialogueNoContinue(Player player, int type, String title, int entityId, int animationId, String... texts) {
        if (player.isNative950() && (type == IS_NPC || type == IS_PLAYER)) {
            player.getNative950Dialogues().speech(type == IS_PLAYER, entityId, animationId, title, false, texts);
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (String text1 : texts)
            builder.append(" " + text1);
        String text = builder.toString();
        player.getInterfaceManager().sendChatBoxInterface(1184);
        player.getPackets().sendHideIComponent(1184, 12, true);
        player.getPackets().sendIComponentText(1184, 4, title);
        player.getPackets().sendIComponentText(1184, 10, text);
        player.getPackets().sendEntityOnIComponent(type == IS_PLAYER, entityId, 1184, 8); // there
        if (animationId != -1)
            player.getPackets().sendIComponentAnimation(animationId, 1184, 8);
        return true;
    }

    public static void closeNoContinueDialogue(Player player) {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    public static void sendPlayerDialogue(Player player, int anim, String... strings) {
        sendEntityDialogue(player, IS_PLAYER, -1, anim, strings);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public abstract void start();

    public abstract void run(int interfaceId, int componentId);

    public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {

    }

    public abstract void finish();

    public final void end() {
        player.getDialogueManager().finishDialogue();
    }

    public static void npcDialogues(Player player, int id, int animid, String... text) {
        sendEntityDialogue(player, IS_NPC, id, animid, text);
    }

    public static void sendSingleNPCDialogue(Player player, int npcId, int animationId, String... text) {
        sendSingleNPCDialogue(player, npcId, animationId, null, text);
    }

    public static void sendSingleNPCDialogue(Player player, int npcId, int animationId, Consumer<Dialogue> action, String... text) {
        player.getDialogueManager().startDialogue("SingleNPCDialogue", npcId, animationId, text, action);
    }

    public static void sendSingleDialogue(Player player, String... text) {
        sendSingleDialogue(player, Dialogue::end, text);
    }

    public static void sendSinglePlayerDialogue(Player player, int animationId, String... text) {
        player.getDialogueManager().startDialogue(new SinglePlayerDialogue(animationId, Dialogue::end, text));
    }

    public static void sendSingleDialogue(Player player, Consumer<Dialogue> action, String... text) {
        player.getDialogueManager().startDialogue(new SingleDialogue(action, text));
    }

    public boolean sendNPCDialogue(int npcId, int animationId, String... text) {
        return sendEntityDialogue(IS_NPC, npcId, animationId, text);
    }

    public static boolean sendNPCDialogue(Player player, int npcId, int animationId, String... text) {
        return sendEntityDialogue(player, IS_NPC, npcId, animationId, text);
    }

    public boolean sendPlayerDialogue(int animationId, String... text) {
        return sendEntityDialogue(IS_PLAYER, -1, animationId, text);
    }

    public boolean sendItemDialogue(int itemId, int amount, String... text) {
        return sendEntityDialogue(IS_ITEM, itemId, amount, text);
    }

    public boolean sendPetDialogue(int itemId, int amount, String... text) {
        return sendEntityDialogue(IS_PETS, itemId, amount, text);
    }

    /*
     *
     * auto selects title, new dialogues
     */
    public boolean sendEntityDialogue(int type, int entityId, int animationId, String... text) {
        String title = "";
        if (type == IS_PLAYER) {
            title = player.getDisplayName();
        } else if (type == IS_NPC) {
            title = NPCDefinitions.getNPCDefinitions(entityId).name;
        } else if (type == IS_ITEM)
            title = ItemDefinitions.getItemDefinitions(entityId).getName();
        return sendEntityDialogue(type, title, entityId, animationId, text);
    }

    public static boolean sendEntityDialogue(Player player, int type, int entityId, int animationId, String... text) {
        String title = "";
        if (type == IS_PLAYER) {
            title = player.getDisplayName();
        } else if (type == IS_NPC) {
            title = NPCDefinitions.getNPCDefinitions(entityId).name;
        } else if (type == IS_ITEM)
            title = ItemDefinitions.getItemDefinitions(entityId).getName();
        return sendEntityDialogue(player, type, title, entityId, animationId, text);
    }

    /*
     * idk what it for
     */
    public int getP() {
        return 1;
    }

    public static boolean sendOptionsDialogueStatic(Player player, String question, String... options) {
        if (player.isNative950()) {
            player.getNative950Dialogues().options(question, options);
            return true;
        }
        if (options.length > 5) {
            throw new InvalidParameterException("The max options length is 5.");
        }
        String[] newopts = new String[5];
        for (int i = 0; i < 5; i++)
            newopts[i] = "";
        int ptr = 0;
        for (String s : options) {
            if (s != null) {
                newopts[ptr++] = s;
            }
        }
        player.getInterfaceManager().sendChatBoxInterface(1188);
        player.getPackets().sendExecuteScriptReverse(5589, newopts[4], newopts[3], newopts[2], newopts[1], newopts[0],
                question, options.length);
        return true;
    }

    public boolean sendOptionsDialogue(String title, String... options) {
        if (player.isNative950()) return sendOptionsDialogueStatic(player, title, options);
        if (options.length > 5) {
            throw new InvalidParameterException("The max options length is 5.");
        }
        String[] newopts = new String[5];
        for (int i = 0; i < 5; i++)
            newopts[i] = "";
        int ptr = 0;
        for (String s : options) {
            if (s != null) {
                newopts[ptr++] = s;
            }
        }
        player.getInterfaceManager().sendChatBoxInterface(1188);
        player.getPackets().sendExecuteScriptReverse(5589, newopts[4], newopts[3], newopts[2], newopts[1], newopts[0],
                title, options.length);
        return true;
    }

    public boolean send3option(String title, String... options) {
        // private static final int OPTION_1 = 11, OPTION_2 = 13, OPTION_3 = 14,
        // OPTION_4 = 15, OPTION_5 = 16;
        player.getInterfaceManager().sendChatBoxInterface(231);
        player.getPackets().sendIComponentText(231, 1, title);
        for (int line = 0; line < 3; line++) {
            if (line < options.length) {
                if (line == 0)
                    player.getPackets().sendIComponentText(231, 2, options[line]);
                else if (line == 1)
                    player.getPackets().sendIComponentText(231, 3, options[line]);
                else if (line == 2)
                    player.getPackets().sendIComponentText(231, 4, options[line]);
            }
        }
        return true;
    }

    public boolean Options2(String title, String... options) {
        player.getInterfaceManager().sendChatBoxInterface(1185);
        player.getPackets().sendIComponentText(1185, 14, title);
        for (int line = 0; line < 2; line++) {
            if (line < options.length) {
                if (line == 0)
                    player.getPackets().sendIComponentText(1185, 20, options[line]);
                else if (line == 1)
                    player.getPackets().sendIComponentText(1185, 25, options[line]);
            }
        }
        return true;
    }

    public boolean Item(String... options) {
        player.getInterfaceManager().sendChatBoxInterface(1189);
        for (int line = 0; line < 1; line++) {
            if (line < options.length) {
                if (line == 0)
                    player.getPackets().sendIComponentText(1189, 4, options[line]);
            }
        }
        return true;
    }

    public static boolean sendEntityDialogue(Player player, int type, String title, int entityId, int animationId, String... texts) {
        if (player.isNative950() && (type == IS_NPC || type == IS_PLAYER)) {
            player.getNative950Dialogues().speech(type == IS_PLAYER, entityId, animationId, title, true, texts);
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append(" " + texts[line]);
        String text = builder.toString();
        if (type == IS_NPC) {
            player.getInterfaceManager().sendChatBoxInterface(1184);
            player.getPackets().sendIComponentText(1184, 4, title);
            player.getPackets().sendIComponentText(1184, 10, text);
            player.getPackets().sendNPCOnIComponent(1184, 8, entityId);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, 1184, 8);
        } else if (type == IS_PLAYER) {
            player.getInterfaceManager().sendChatBoxInterface(1191);
            player.getPackets().sendIComponentText(1191, 4, title);
            player.getPackets().sendIComponentText(1191, 10, text);
            player.getPackets().sendPlayerOnIComponent(1191, 8);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, 1191, 8);
        } else if (type == IS_ITEM) {
            player.getInterfaceManager().sendChatBoxInterface(1189);
            player.getPackets().sendItemOnIComponent(1189, 8, entityId, 1);
            player.getPackets().sendHideIComponent(1189, 15, false);
            player.getPackets().sendHideIComponent(1189, 4, false);
            player.getPackets().sendHideIComponent(1189, 9, true);
            player.getPackets().sendHideIComponent(1189, 7, true);
            player.getPackets().sendHideIComponent(1189, 8, false);
            player.getPackets().sendIComponentText(1189, 3, text);
        } else if (type == IS_PETS) {
            player.getInterfaceManager().sendChatBoxInterface(668);
        }
        return true;
    }

    /*
     * new dialogues
     */
    public boolean sendEntityDialogue(int type, String title, int entityId, int animationId, String... texts) {
        if (player.isNative950() && (type == IS_NPC || type == IS_PLAYER))
            return sendEntityDialogue(player, type, title, entityId, animationId, texts);
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append(" " + texts[line]);
        String text = builder.toString();
        if (type == IS_NPC) {
            player.getInterfaceManager().sendChatBoxInterface(1184);
            player.getPackets().sendIComponentText(1184, 4, title);
            player.getPackets().sendIComponentText(1184, 10, text);
            player.getPackets().sendNPCOnIComponent(1184, 8, entityId);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, 1184, 8);
        } else if (type == IS_PLAYER) {
            player.getInterfaceManager().sendChatBoxInterface(1191);
            player.getPackets().sendIComponentText(1191, 4, title);
            player.getPackets().sendIComponentText(1191, 10, text);
            player.getPackets().sendPlayerOnIComponent(1191, 8);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, 1191, 8);
        } else if (type == IS_ITEM) {
            player.getInterfaceManager().sendChatBoxInterface(1189);
            player.getPackets().sendHideIComponent(1189, 15, false);
            player.getPackets().sendHideIComponent(1189, 4, false);
            player.getPackets().sendHideIComponent(1189, 9, true);
            player.getPackets().sendHideIComponent(1189, 7, true);
            player.getPackets().sendHideIComponent(1189, 8, false);
            player.getPackets().sendIComponentText(1189, 3, text);
            player.getPackets().sendItemOnIComponent(1189, 8, entityId, 1);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, 1189, 8);
        } else if (type == IS_PETS) {
            player.getInterfaceManager().sendChatBoxInterface(668);
        }
        return true;
    }
    
    public boolean sendDialogue(String... texts) { 
        return sendDialogue(false, texts);
    }
    
    public boolean sendDialogue(boolean noContinue, String... texts) {
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append((line == 0 ? "<p=" + getP() + ">" : "<br>") + texts[line]);
        String text = builder.toString();
        if (player.isNative950()) {
            player.getNative950Dialogues().message(!noContinue, text);
            return true;
        }
        player.getInterfaceManager().sendChatBoxInterface(1186);
        player.getPackets().sendIComponentText(1186, 3, text);
        player.getPackets().sendHideIComponent(1186, 4, noContinue);
        return true;
    }

    public static boolean sendDialogue(Player player, String... texts) {
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append((line == 0 ? "<p=" + 1 + ">" : "<br>") + texts[line]);
        String text = builder.toString();
        if (player.isNative950()) {
            player.getNative950Dialogues().message(true, text);
            return true;
        }
        player.getInterfaceManager().sendChatBoxInterface(1186);
        player.getPackets().sendIComponentText(1186, 3, text);
        return true;
    }

    public boolean sendEntityDialogue(short interId, String[] talkDefinitons, byte type, int entityId, int animationId) {
        if (type == IS_PLAYER || type == IS_NPC) { // auto convert to new
            // dialogue all old
            // dialogues
            String[] texts = new String[talkDefinitons.length - 1];
            for (int i = 0; i < texts.length; i++)
                texts[i] = talkDefinitons[i + 1];
            sendEntityDialogue(type, talkDefinitons[0], entityId, animationId, texts);
            return true;
        }
        int[] componentOptions = getIComponentsIds(interId);
        if (componentOptions == null)
            return false;
        player.getInterfaceManager().sendChatBoxInterface(interId);
        if (talkDefinitons.length != componentOptions.length)
            return false;
        for (int childOptionId = 0; childOptionId < componentOptions.length; childOptionId++)
            player.getPackets().sendIComponentText(interId, componentOptions[childOptionId], talkDefinitons[childOptionId]);
        if (type == IS_PLAYER || type == IS_NPC) {
            player.getPackets().sendEntityOnIComponent(type == IS_PLAYER, entityId, interId, 2);
            if (animationId != -1)
                player.getPackets().sendIComponentAnimation(animationId, interId, 2);
        } else if (type == IS_ITEM)
            player.getPackets().sendItemOnIComponent(interId, 2, entityId, animationId);
        return true;
    }

    public void player(String message) {
        sendPlayerDialogue(9827, message);
    }

    public void player(int animation, String message) {
        sendPlayerDialogue(animation, message);
    }

    public void npc(int npcId, String message) {
        sendNPCDialogue(npcId, 9827, message);
    }

    public void npc(int npcId, int animation, String message) {
        sendNPCDialogue(npcId, animation, message);
    }

    public int getOrdinal(int componentId) {
        return componentToOption(componentId) - 1;
    }
}
