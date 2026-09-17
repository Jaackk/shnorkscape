package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** The paired client's Hero / Skills page. See protocol-analysis/skill-guide-950.md. */
public final class Native950SkillGuide {
    private static final int ROOT = 1477, HOST = 715, WRAPPER = 708, SHELL = 1448;
    private static final int GUIDE = 1218, CONTENT = 1217, SKILLS_PANEL = 1466;
    private static final int HERO_KEY = 0, SKILLS_PAGE = 2, SKILLS_STRUCT = 21144;
    private static volatile Object verifiedStore;

    // Cache enum7674 -> struct param3440. This includes all 29 current skills.
    private static final int[] CELL_SKILLS = {
        0, 3, 14, 2, 16, 13, 1, 15, 10, 4, 17, 7, 5, 12, 11,
        6, 9, 8, 20, 18, 19, 22, 21, 23, 24, 25, 26, 27, 28
    };
    // Cache enum1482, indexed by server skill ID. Raw IDs are not guide arguments.
    private static final int[] GUIDE_ARGUMENTS = {
        1, 5, 2, 6, 3, 7, 4, 16, 18, 19, 15, 17, 11, 14, 13,
        9, 8, 10, 20, 21, 12, 23, 22, 24, 25, 26, 27, 28, 29
    };
    // Each entry is an actual1218 onOp5682 hook, paired with enum1482.
    private static final int[][] GUIDE_COMPONENTS = {
        {7,8}, {15,16}, {23,27}, {31,0}, {39,3}, {47,22}, {55,7},
        {63,12}, {71,1}, {79,25}, {87,24}, {95,19}, {103,11}, {111,10},
        {119,9}, {127,15}, {135,21}, {143,26}, {152,6}, {160,14},
        {168,28}, {176,5}, {184,4}, {192,20}, {200,18}, {208,13},
        {216,2}, {224,23}, {232,17}
    };
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private boolean cacheVerified;
    private final Native950Navigation navigation;
    private int selectedSkill = Skills.ATTACK;

    public Native950SkillGuide(Player player, Channel channel) {
        this(player, channel, Native950SkillGuide::verify);
    }
    Native950SkillGuide(Player player, Channel channel, Runnable verifier) {
        this.player = Objects.requireNonNull(player, "player");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        navigation = new Native950Navigation(player,channel,this);
        player.getInterfaceManager().setNative950SkillGuide(this);
    }
    public boolean isOpen() { return navigation.isOpen(); }
    public Native950Navigation navigation() { return navigation; }
    public void verifyBeforeOpen() {
        if (!cacheVerified) { verifier.run(); cacheVerified = true; }
    }
    public void bootstrap() {
        // Only View Skillguide is implemented; target-setting operations stay client-owned.
        channel.write(Native950Packets.interfaceEvents(SKILLS_PANEL, 7, 0, 28, 2));
        navigation.bootstrap();
    }
    public static boolean isOpenRequest(Native950Actions.InterfaceAction action) {
        return Native950Navigation.isOpenRequest(action) || isSkillRequest(action);
    }
    private static boolean isSkillRequest(Native950Actions.InterfaceAction action) {
        return action.option() == 1 && action.itemId() == -1
                && action.interfaceId() == SKILLS_PANEL && action.componentId() == 7
                && skillForCell(action.slot()) >= 0;
    }
    public boolean handle(Native950Actions.InterfaceAction action) {
        if (isSkillRequest(action)) {
            verifyBeforeOpen(); navigation.openSkills(skillForCell(action.slot())); return true;
        }
        if (navigation.handle(action)) return true;
        if (!navigation.isPage(0,2) || action.option() != 1 || action.itemId() != -1) return false;
        if (action.interfaceId() != GUIDE || action.slot() != -1) return false;
        int skill = skillForComponent(action.componentId());
        if (skill >= 0) {
            selectedSkill = skill;
            // The icon's cache onOp5682 already selected the skill and cleared its rows.
            // Fill those rows via1217's onLoad target, without repeating selection or
            // remounting the content and restarting the entire Hero layout.
            channel.write(Native950Packets.runClientScript(5690));
            return true;
        }
        if (action.componentId() == 258 || action.componentId() == 261) {
            // Native dropdown10438 ->11988 ->11070/11436 ->5691 owns category/sort.
            // Re-running5682 here would erase the category the player just selected.
            return true;
        }
        if (action.componentId() == 262) {
            // onOp14985 already changes the preference and checkbox. Only refresh results.
            channel.write(Native950Packets.runClientScript(5691, guideArgument(selectedSkill)));
            return true;
        }
        return false;
    }
    public static int skillForComponent(int component) {
        for (int[] row : GUIDE_COMPONENTS) if (row[0] == component) return row[1];
        return -1;
    }
    static int skillForCell(int slot) {
        return slot >= 0 && slot < CELL_SKILLS.length ? CELL_SKILLS[slot] : -1;
    }
    static int guideArgument(int skill) {
        if (skill < 0 || skill >= GUIDE_ARGUMENTS.length) throw new IllegalArgumentException("Unknown skill " + skill);
        return GUIDE_ARGUMENTS[skill];
    }
    void selectSkill(int skill,boolean refresh) {
        selectedSkill=skill;
        if(refresh){
            channel.write(Native950Packets.runClientScript(5682,guideArgument(skill)));
            channel.write(Native950Packets.runClientScript(5690));
        }
    }
    void attachContent() {
        channel.write(Native950Packets.runClientScript(5682,guideArgument(selectedSkill)));
        channel.write(Native950Packets.openSub(GUIDE,0,CONTENT,false));
        player.getInterfaceManager().registerNativeOpen(CONTENT,GUIDE,0);
    }
    void detachContent() {
        if(player.getInterfaceManager().getInterfaceParentId(CONTENT)!=(GUIDE<<16))return;
        channel.write(Native950Packets.closeSub(GUIDE,0));
        player.getInterfaceManager().unregisterNativeOpen(CONTENT);
    }
    // Session ticks do not remount native content or periodically reset selected rows.
    void tick() { }
    public void close() { navigation.close(); }
    public static synchronized void verify() {
        if (verifiedStore == Cache.STORE && verifiedStore != null) return;
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Skill guide requires the paired flat950 cache");
        Native950Navigation.verify();
        Native950CacheReader reader = new Native950CacheReader.Flat();
        require(reader.enumInt(7699, HERO_KEY) == 21142 && reader.structInt(21142,3449) == SKILLS_STRUCT,
                "Hero / Skills page");
        require(reader.structInt(SKILLS_STRUCT,3456) == GUIDE && reader.structInt(SKILLS_STRUCT,3457) == 0
                && reader.structInt(SKILLS_STRUCT,3458) == 0 && reader.structInt(SKILLS_STRUCT,3459) == 742
                && reader.structInt(SKILLS_STRUCT,3460) == 450, "Skills geometry");
        for (int slot = 0; slot < CELL_SKILLS.length; slot++) {
            int struct = reader.enumInt(7674,slot), skill = CELL_SKILLS[slot];
            require(struct >= 0 && reader.structInt(struct,3440) == skill
                    && reader.structInt(struct,3441) == guideArgument(skill)
                    && reader.enumInt(1482,skill) == guideArgument(skill), "skill cell " + slot);
        }
        pinInterface(1218,265,"69f0add2938a5522cb27458b4ddd36c7991112f76d20e92f43d5854993fbd798");
        pinInterface(1217,1,"9dbc90bc6d12ed868813fbc58f070d9b903c77b79636ee1b938e3567f9f77255");
        pinInterface(1466,15,"fba955893d9fa7ad623bcb2fab19c0f86ecb3adbb6a66284d8a3a74f3a100951");
        pin(12,441,0,"14de94d23b844205197a9639efe8348a9e9d55f3ae984644298568b430cdfd5e");
        pin(12,5682,0,"05398f59da8ad91110b647123ffb13807949045a8ec8b693c9486be3470bf020");
        pin(12,5683,0,"cfba55bc5331dfed7f94431a55cf5794c601dd5367fe1caa972955e42f0e2d0e");
        pin(12,5689,0,"23a750f48d49ee737abe49b358c9c0331b0b80446decc820691fc68545e124f1");
        pin(12,5690,0,"37e377523c6e278e7885baf024232d8a3b690638745ab817156252cbd2c2572a");
        pin(12,5691,0,"cd484501c9633c4900ebf3ebd5583cf775baf2059792093ba553abb5d28fc281");
        pin(12,8283,0,"f66af3100f2777485313489b2c09d2c666557ce817f3e01d9fd669ba082f8f1d");
        pin(12,8288,0,"bd7585328d7c15e6597038b0b19df5239fb44abd343253a17ec52355e42b0f59");
        pin(12,8186,0,"d2fcafc03a569cec8ae43f46144405f592243c6f601e364f1d179566790cf889");
        pin(12,10438,0,"ed2ea7f228d1bf4233bcdd0d987ad864ad38fd9c7ae2404a6a815728c489ca42");
        pin(12,11988,0,"4d7157a78a55dc13c2bc0dc4dd2f89e5d3c7eee8b04ae2db3959d17fa89132f0");
        pin(12,11070,0,"a3d057ce46bf6f2bc56681c7199d7ce9ec5b2f9ccbfc38048e01e9d17fc91ed3");
        pin(12,11436,0,"3c42ac1f5f88f643c930a7a168be257be39c4f9f58571c622d6340ee74cee99e");
        pin(12,14985,0,"6ee983e93320403821c49a2a0115c47e02b6363882c3ec9bbee08424b341b351");
        pin(2,69,18995,"3c99b88fdecc34e8f1084e4784832d384def1c30de1150bc5ea69dc6c34b699e");
        verifiedStore = Cache.STORE;
    }
    private static void require(boolean condition, String binding) {
        if (!condition) throw new IllegalStateException("950 skill guide cache changed: " + binding);
    }
    private static void pinInterface(int iface, int count, String expected) {
        require(Cache.STORE.getIndexes()[3].getValidFilesCount(iface) == count, "interface " + iface + " count");
        MessageDigest digest = digest();
        for (int file = 0; file < count; file++) digest.update(file(3,iface,file));
        requireHash(digest,expected,"interface " + iface);
    }
    private static void pin(int index, int group, int file, String expected) {
        MessageDigest digest = digest(); digest.update(file(index,group,file));
        requireHash(digest,expected,index + "/" + group + "/" + file);
    }
    private static byte[] file(int index, int group, int file) {
        byte[] bytes = Cache.STORE.getIndexes()[index].getFile(group,file);
        if (bytes == null) throw new IllegalStateException("Missing950 skill guide cache file " + index + "/" + group + "/" + file);
        return bytes;
    }
    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private static void requireHash(MessageDigest digest, String expected, String binding) {
        StringBuilder actual = new StringBuilder();
        for (byte value : digest.digest()) actual.append(String.format("%02x",value & 255));
        require(expected.equals(actual.toString()),binding);
    }
}
