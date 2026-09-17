package com.rs.game.player;

import java.io.Serializable;

import com.rs.game.item.Item;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

public class PlayerExamineManager implements Serializable {

    private static final long serialVersionUID = 6506692526193840181L;

    private transient Player player;

    private byte status;
    private String personalMessage;
    public int privacy;
    private transient Player examinedPlayer;
    private transient int tabId;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void init() {
        refreshStatus();
        refreshPrivacy();
    }

    public void openExamineSettings() {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsBankInterface()) {
            player.getPackets().sendGameMessage("Please finish what you are doing before opening the examine settings interface.");
            return;
        }
        player.stopAll();
        player.getInterfaceManager().sendInterface(1561);
        refreshPersonalMessage();
    }

    public void process() {
        refreshTargetInfo();
    }

    public void setStatus(int status) {
        this.status = (byte) status;
        refreshStatus();
    }

    public void clearPersonalMessage() {
        personalMessage = null;
        refreshPersonalMessage();
    }

    private void refreshPersonalMessage() {
        player.getPackets().sendGlobalString(4670, personalMessage == null ? "" : personalMessage);
    }

    public void changePersonalMessage() {
        player.sendIComponentInputString(1561, 18, 64, new InputStringEvent() {

            @Override
            public void run(Player player) {
                if (getString() == null || getString().isEmpty())
                    return;
                player.getPlayerExamineManager().setPersonalMessage(getString());
            }
        });
    }

    public void setPersonalMessage(String message) {
        personalMessage = message;
        refreshPersonalMessage();
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
        refreshPrivacy();
    }

    public void refreshStatus() {
        player.getVarBitManager().sendVarBit(26169, status);
    }

    public void refreshPrivacy() {
        player.getVarBitManager().sendVarBit(26171, privacy);
    }

    public void openExamineDetails(Player target) {
        this.examinedPlayer = target;
        boolean hidePrivateInfo = examinedPlayer.getPlayerExamineManager().privacy == 2 || (examinedPlayer.getPlayerExamineManager().privacy == 1 && !examinedPlayer.getFriendsIgnores().isFriend(player.getUsername()));
        if (examinedPlayer.isGroupIronman()) {
            Dialogue.sendSingleDialogue(player, examinedPlayer.getDisplayName() + " is in GIM group " + Colors.DARK_RED + examinedPlayer.gimName + "</col>.");
        }
        player.getInterfaceManager().sendPlayerInspectInterface(1560);
        player.getInterfaceManager().setInterface(true, 1560, 17, 1558);
        player.getInterfaceManager().setInterface(true, 1560, 21, 1892);
        player.getInterfaceManager().setInterface(true, 1558, 2, 1831);
        player.getPackets().sendIComponentSettings(1558, 8, 0, 18, 2046);
        player.getInterfaceManager().setInterface(true, 1560, 19, 1557);
        player.getInterfaceManager().setInterface(true, 1560, 18, 1559);
        player.getPackets().sendConfig(5005, examinedPlayer.getAppearence().getRenderEmote());
        refreshTargetInfo();
        openTab(hidePrivateInfo ? 0 : tabId);
    }

    private void refreshTargetInfo() {
        if (examinedPlayer == null)
            return;
        boolean hidePrivateInfo = examinedPlayer.getPlayerExamineManager().privacy == 2 || (examinedPlayer.getPlayerExamineManager().privacy == 1 && !examinedPlayer.getFriendsIgnores().isFriend(player.getUsername()));
        Item[] items = examinedPlayer.getEquipment().getItems().getItemsCopy();
        if (hidePrivateInfo) {
            items[Equipment.SLOT_RING] = null;
            items[Equipment.SLOT_ARROWS] = null;
            items[Equipment.SLOT_AURA] = null;
            items[Equipment.SLOT_POCKET] = null;
        }
        player.getPackets().sendItems(742, items);
        player.getPackets().sendItems(743, !hidePrivateInfo ? items : examinedPlayer.getEquipment().getCosmeticItems().getItems());
        player.getPackets().sendGlobalString(4669, examinedPlayer.getDisplayName());
        String personalMessage = examinedPlayer.getPlayerExamineManager().personalMessage;
        player.getPackets().sendGlobalString(4671, personalMessage == null ? "" : personalMessage);
        player.getPackets().sendGlobalString(4672, examinedPlayer.getClanManager() != null ? examinedPlayer.getClanName() : "");
        player.getPackets().sendOtherPlayerOnIComponent(1831, 10, examinedPlayer);
        player.getVarBitManager().sendVarBit(26172, examinedPlayer.getPlayerExamineManager().status);
        player.getVarBitManager().sendVarBit(26173, examinedPlayer.getAppearence().getTitle());
        player.getPackets().sendConfig(4985, hidePrivateInfo ? 1 : 0);
        for (int varbit = 26071; varbit < 26123; varbit++) {
            int skill = Skills.FIXED_SLOTS[(varbit - 26071) / 2];
            if (varbit != 0)
                player.getVarBitManager().sendVarBit(varbit, varbit % 2 != 0 ? examinedPlayer.getSkills().getLevel(skill) : examinedPlayer.getSkills().getLevelForXp(skill));
        }
        player.getVarBitManager().sendVarBit(30202, examinedPlayer.getSkills().getLevel(Skills.INVENTION));
        player.getVarBitManager().sendVarBit(30203, examinedPlayer.getSkills().getLevelForXp(Skills.INVENTION));
        player.getPackets().sendConfig(4934, (int) examinedPlayer.getSkills().getXp(Skills.ATTACK));
        player.getPackets().sendConfig(4935, (int) examinedPlayer.getSkills().getXp(Skills.STRENGTH));
        player.getPackets().sendConfig(4936, (int) examinedPlayer.getSkills().getXp(Skills.DEFENCE));
        player.getPackets().sendConfig(4937, (int) examinedPlayer.getSkills().getXp(Skills.RANGE));
        player.getPackets().sendConfig(4938, (int) examinedPlayer.getSkills().getXp(Skills.PRAYER));
        player.getPackets().sendConfig(4939, (int) examinedPlayer.getSkills().getXp(Skills.MAGIC));
        player.getPackets().sendConfig(4940, (int) examinedPlayer.getSkills().getXp(Skills.RUNECRAFTING));
        player.getPackets().sendConfig(4941, (int) examinedPlayer.getSkills().getXp(Skills.CONSTRUCTION));
        player.getPackets().sendConfig(4942, (int) examinedPlayer.getSkills().getXp(Skills.DUNGEONEERING));
        player.getPackets().sendConfig(4943, (int) examinedPlayer.getSkills().getXp(Skills.HITPOINTS));
        player.getPackets().sendConfig(4944, (int) examinedPlayer.getSkills().getXp(Skills.AGILITY));
        player.getPackets().sendConfig(4945, (int) examinedPlayer.getSkills().getXp(Skills.HERBLORE));
        player.getPackets().sendConfig(4946, (int) examinedPlayer.getSkills().getXp(Skills.THIEVING));
        player.getPackets().sendConfig(4947, (int) examinedPlayer.getSkills().getXp(Skills.CRAFTING));
        player.getPackets().sendConfig(4948, (int) examinedPlayer.getSkills().getXp(Skills.FLETCHING));
        player.getPackets().sendConfig(4949, (int) examinedPlayer.getSkills().getXp(Skills.SLAYER));
        player.getPackets().sendConfig(4950, (int) examinedPlayer.getSkills().getXp(Skills.HUNTER));
        player.getPackets().sendConfig(4951, (int) examinedPlayer.getSkills().getXp(Skills.DIVINATION));
        player.getPackets().sendConfig(4952, (int) examinedPlayer.getSkills().getXp(Skills.MINING));
        player.getPackets().sendConfig(4953, (int) examinedPlayer.getSkills().getXp(Skills.SMITHING));
        player.getPackets().sendConfig(4954, (int) examinedPlayer.getSkills().getXp(Skills.FISHING));
        player.getPackets().sendConfig(4955, (int) examinedPlayer.getSkills().getXp(Skills.COOKING));
        player.getPackets().sendConfig(4956, (int) examinedPlayer.getSkills().getXp(Skills.FIREMAKING));
        player.getPackets().sendConfig(4957, (int) examinedPlayer.getSkills().getXp(Skills.WOODCUTTING));
        player.getPackets().sendConfig(4958, (int) examinedPlayer.getSkills().getXp(Skills.FARMING));
        player.getPackets().sendConfig(4959, (int) examinedPlayer.getSkills().getXp(Skills.SUMMONING));
        player.getPackets().sendConfig(6100, (int) examinedPlayer.getSkills().getXp(Skills.INVENTION));

        player.getPackets().sendConfig(4962, examinedPlayer.getHitpoints() * 10);
        player.getPackets().sendConfig(4963, examinedPlayer.getPrayer().getPrayerpoints() * 10);
        player.getPackets().sendConfig(4964, examinedPlayer.getCombatDefinitions().getHandDamage(false) * 10);
        player.getPackets().sendConfig(4966, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.MELEE_ACCURACY_PENALTY]);
        player.getPackets().sendConfig(4967, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_ACCURACY_PENALTY]);
        player.getPackets().sendConfig(4968, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGE_ACCURACY_PENALTY]);
        player.getPackets().sendConfig(4969, examinedPlayer.getCombatDefinitions().getSkillAccuracy(false));
        player.getPackets().sendConfig(4970, examinedPlayer.getCombatDefinitions().getStyle(false));
        player.getPackets().sendConfigByFile(26124, examinedPlayer.getCombatDefinitions().getMainHandSpellId());

        player.getPackets().sendConfig(4972, examinedPlayer.getCombatDefinitions().getHandDamage(true) * 2 * 10);
        player.getPackets().sendConfig(4973, examinedPlayer.getCombatDefinitions().getSkillAccuracy(true));
        player.getPackets().sendConfig(4974, examinedPlayer.getCombatDefinitions().getStyle(true));
        player.getPackets().sendConfigByFile(26125, examinedPlayer.getCombatDefinitions().getOffHandSpellId());

        player.getPackets().sendConfig(4975, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.WORN_ARMOUR]);
        player.getPackets().sendConfig(4976, examinedPlayer.getCombatDefinitions().getDefenceArmor());
        player.getPackets().sendConfig(4977, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.MELEE_AFF]);
        player.getPackets().sendConfig(4978, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_AFF]);
        player.getPackets().sendConfig(4979, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGIC_AFF]);
        player.getPackets().sendConfig(4980, examinedPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.LIFE_B] * 10);
//      VARBIT[26126];PVM reduction tank armour
//      VARBIT[26127];PVM reduction shield
//      VAR_0[4981]PVP reduction armour
        int value = 1;
        value |= (DistinctionCape.isMaxed(examinedPlayer) ? 1 : 0) << 1;
        value |= (DistinctionCape.isWorthyCompCape(examinedPlayer) ? 1 : 0) << 2;
        value |= (DistinctionCape.isWorthyCompCapeT(examinedPlayer) ? 1 : 0) << 3;
        player.getVarBitManager().sendVar(7156, value);
    }

    public void closeExamineDetails() {
        player.getInterfaceManager().removePlayerInspectInterface();
    }

    public void openTab(int tabId) {
        this.tabId = tabId;
        player.getPackets().sendExecuteScriptReverse(10775, tabId);
        if (tabId == 2) {
            for (int i = 123; i <= 131; i++)
                player.getPackets().sendHideIComponent(1559, i, true);
        }
    }
}
