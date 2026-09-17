package com.rs.game.player.content.eds;

import java.io.Serializable;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.player.controllers.EliteDungeonsLobby;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.Setter;

public class EliteDungeonsManager implements Serializable {

    private static final long serialVersionUID = -4587254542577439695L;

    private transient Player player;
    private transient EliteDungeonPartyManager party;
    public transient Player invitingPlayer;
    private Object rejoinKey;
    @Getter
    @Setter
    private transient boolean hidden;
    @Getter
    private ItemsContainer<Item> rewards;
    @Getter
    @Setter
    private boolean autoLoot;

    public EliteDungeonsManager() {
        rewards = new ItemsContainer<Item>(30, true);
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (rewards == null)
            rewards = new ItemsContainer<Item>(30, true);
    }

    public EliteDungeonPartyManager getParty() {
        return party;
    }

    public void setParty(EliteDungeonPartyManager party) {
        this.party = party;
    }

    public void refreshPartyDetailsComponents() {
        if (!player.getInterfaceManager().containsInterface(91))
            return;
        
        player.getPackets().sendHideIComponent(91, 35, party != null);
        player.getPackets().sendHideIComponent(91, 17, party == null || !party.isLeader(player)); // leave 1
        player.getPackets().sendHideIComponent(91, 34, party == null || party.isLeader(player)); // leave 2
        player.getPackets().sendHideIComponent(91, 36, party == null || !party.isLeader(player));// invite
        player.getPackets().sendHideIComponent(91, 62, party == null || (party != null && !isInside()));// reset
        player.getPackets().sendHideIComponent(91, 2, party != null && party.isLeader(player) && !isInside());// complexity
        player.getPackets().sendHideIComponent(91, 4, party != null && party.isLeader(player) && !isInside());// floor
        player.getPackets().sendHideIComponent(91, 43, party != null && party.isLeader(player) && !isInside());// guide
                                                                                                                // mode
        for (int i = 0; i < 5; i++) {
            Player p2 = party == null || i >= party.getTeam().size() ? null : party.getTeam().get(i);
            player.getPackets().sendHideIComponent(91, 19 + (i * 3), p2 == null);
            player.getPackets().sendHideIComponent(91, 20 + (i * 3), p2 == null);
        }
        boolean eliteDungeon = true;
        for (int i = 20; i <= 32; i += 3)
        player.getPackets().sendHideIComponent(91, i, eliteDungeon);
        for (int i = 0; i <= 3; i ++)
            player.getPackets().sendHideIComponent(91, i, eliteDungeon);
        player.getPackets().sendHideIComponent(91, 13, eliteDungeon);
        player.getPackets().sendHideIComponent(91, 15, eliteDungeon);
        player.getPackets().sendHideIComponent(91, 16, eliteDungeon);
    }

    public void refreshNames() {
        if (party == null) {
            return;
        }
        int index = 0;
        for (Player p2 : party.getTeam())
            player.getPackets().sendGlobalString(2376 + (index++),
                    "<col=FFFFFF><shad>" + p2.getDisplayName() + "</col></shad>");
    }

    public void resetInvitation() {
        if (invitingPlayer == null)
            return;
        invitingPlayer = null;
    }

    public void expireInvitation() {
        if (invitingPlayer == null)
            return;
        player.getPackets().sendGameMessage("Your elite dungeon party invitation to " + invitingPlayer.getDisplayName() + " has expired.");
        invitingPlayer.getPackets().sendGameMessage("An elite dungeon party invitation from " + player.getDisplayName() + " has expired.");
        invitingPlayer = null;
    }

    public void openPartyInterface() {
        player.getInterfaceManager().openDungTab();
        player.getInterfaceManager().sendDungPartyInterface();
        refreshPartyDetailsComponents();
        refreshNames();
    }

    public void invite() {
        if (party == null || !party.isLeader(player))
            return;
        player.stopAll();
        if (party.getDungeon() != null) {
            player.getPackets().sendGameMessage("You can't invite players while having a dungeon progress.");
            return;
        }
                player.sendInputName("Enter name:", new InputNameEvent() {

                    @Override
                    public void run(Player player) {
                        String name = getString();
                        player.getEliteDungeonsManager().invite(name);
                    }
                });
    }

    public void invite(String name) {
        player.stopAll();
        if (party == null) {
            final Player p2 = World.getPlayerByDisplayName(name);
            if (p2 == null || !p2.withinDistance(player, 14)) {
                player.getPackets().sendGameMessage("Unable to find " + name);
                return;
            }
            EliteDungeonPartyManager party = p2.getEliteDungeonsManager().getParty();
            if (player.isInsideAnyDungParty()) {
                if (p2.getEliteDungeonsManager().invitingPlayer == player)
                    p2.getEliteDungeonsManager().expireInvitation();
                if (p2.getDungeoneeringManager().invitingPlayer == player)
                    p2.getDungeoneeringManager().expireInvitation();
                player.getPackets().sendGameMessage("You can only enter one party at a time.");
                return;
            }
            if (party.getDungeon() != null) {
                player.getPackets().sendGameMessage("You can't join the party ");
                return;
            }
            if (p2.getEliteDungeonsManager().invitingPlayer != player || party == null || !party.isLeader(p2)) {
                player.getPackets().sendGameMessage((p2.getEliteDungeonsManager().invitingPlayer != player) ? "You can't do that right now, Make sure your in a party and your the leader." : party == null ? "You need to be in a party to do that." : "Only the leader can invite players.");
                return;
            }
            player.getTemporaryAttributtes().put(Key.ELITE_DUNGEON_TYPE, Boolean.TRUE);
            player.getTemporaryAttributtes().put(Key.DUNGEON_INVITED_BY, p2);
            player.getInterfaceManager().sendInterface(949);
            for (int i = 0; i < 5; i++) {
                Player teamMate = i >= party.getTeam().size() ? null : party.getTeam().get(i);
                player.getPackets().sendGlobalString(2371 + i, teamMate == null ? "" : teamMate.getDisplayName());
                player.getPackets().sendGlobalConfig(1153 + i,
                        teamMate == null ? 0 : teamMate.getSkills().getCombatLevel());
                player.getPackets().sendGlobalConfig(1158 + i,
                        teamMate == null ? 0 : teamMate.getSkills().getLevel(Skills.DUNGEONEERING));
                player.getPackets().sendGlobalConfig(1163 + i,
                        teamMate == null ? 0 : teamMate.getSkills().getHighestSkillLevel());
                player.getPackets().sendGlobalConfig(1168 + i,
                        teamMate == null ? 0 : teamMate.getSkills().getTotalLevel());
            }
            player.setCloseInterfacesEvent(new Runnable() {

                @Override
                public void run() {
                    p2.getEliteDungeonsManager().expireInvitation();
                    player.getTemporaryAttributtes().remove(Key.DUNGEON_INVITED_BY);
                }

            });
        } else {
            if (!party.isLeader(player)) {
                player.getPackets().sendGameMessage("You can't do that right now.");
                return;
            }
            if (party.getSize() >= 3) {
                player.getPackets().sendGameMessage("Your party is full.");
                return;
            }
            if (party.getDungeon() != null) {
                player.getPackets().sendGameMessage("You can't invite players while having a dungeon progress.");
                return;
            }
            Player p2 = World.getPlayerByDisplayName(name);
            if (p2 == null) {
                player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
                return;
            }
            if (p2.isInsideAnyDungParty()) {
                player.getPackets().sendGameMessage(p2.getDisplayName() + " is already in a party.");
                return;
            }
            if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade() || p2.isLocked()) {
                player.getPackets().sendGameMessage("The other player is busy.");
                return;
            }
            expireInvitation();
            invitingPlayer = p2;
            p2.getTemporaryAttributtes().put(Key.ELITE_DUNGEON_TYPE, Boolean.TRUE);
            p2.getTemporaryAttributtes().put(Key.DUNGEON_INVITED_BY, p2);
            player.getPackets().sendGameMessage("Sending party invitation to " + p2.getDisplayName() + "...");
            p2.getPackets().sendDungeonneringRequestMessage(player, true);
        }
    }

    public void pressOption(int playerIndex, int option) {
        player.stopAll();
        if (party == null || playerIndex >= party.getTeam().size())
            return;
        Player player = party.getTeam().get(playerIndex);
        if (player == null)
            return;
        /*
         * EliteDungeonsManager dungeon = party.getDungeon(); if (option == 0) { if
         * (dungeon == null) { this.player.getPackets().
         * sendGameMessage("You must be in a dungeon to do that."); return; }
         * 
         * if (player == this.player) { this.player.getPackets().
         * sendGameMessage("Why don't you just use your inventory and stat interfaces?"
         * ); return; }
         * this.player.getTemporaryAttributtes().put("DUNG_INSPECTED_PLAYER", player);
         * openInspectInterface(0); } else if (option == 1) { if (player == this.player)
         * { this.player.getPackets().sendGameMessage("You can't kick yourself!");
         * return; } if (!party.isLeader(this.player)) { this.player.getPackets().
         * sendGameMessage("Only your party's leader can kick a party member!"); return;
         * } if (player.isLocked() || dungeon != null && dungeon.isBossOpen()) {
         * this.player.getPackets().
         * sendGameMessage("You can't kick this player right now."); return; }
         * player.getEliteDungManager().leaveParty(); } else if (option == 2) { if
         * (party.isLeader(player)) { this.player.getPackets().
         * sendGameMessage("You can't promote the party leader."); return; } if
         * (!party.isLeader(this.player)) { this.player.getPackets().
         * sendGameMessage("Only your party's leader can promote a leader!"); return; }
         * party.setLeader(player); for (Player p2 : party.getTeam())
         * party.refreshPartyDetails(p2); } else if (option == 3) { if (player !=
         * this.player) { this.player.getPackets().
         * sendGameMessage("You can't switch another player shared-xp."); return; }
         * player.getPackets().sendGameMessage("Shared xp is currently disabled."); }
         */
    }

    public void formParty() {
        if (party != null)
            return;
        player.stopAll();
        new EliteDungeonPartyManager().add(player);
    }

    public void checkLeaveParty() {
        if (party == null)
            return;
        if (party.getDungeon() != null) {
            player.getDialogueManager().startDialogue(new Dialogue() {
                @Override
                public void start() {
                    sendDialogue("Leaving the party will make you lose all the progress you have made in your dungeon, Are you sure you want to leave?");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case -1:
                        stage = 0;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes.", "No.");
                        break;
                    case 0:
                        end();
                        if (componentId == OPTION_1) {
                            if (party.getDungeon().isInside(player))
                                party.getDungeon().leaveDungeon(player);
                            player.getEliteDungeonsManager().setRejoinKey(null);
                            leaveParty();
                        }
                        break;
                    }
                }

                @Override
                public void finish() {

                }
            });
        } else
            leaveParty();
    }

    public void leaveParty() {
        if (party != null)
            party.leaveParty(player, false);
    }

    public void acceptInvite() {
        Player invitedBy = (Player) player.getTemporaryAttributtes().remove(Key.DUNGEON_INVITED_BY);
        if (invitedBy == null)
            return;
        EliteDungeonPartyManager party = invitedBy.getEliteDungeonsManager().getParty();
        if (invitedBy.getEliteDungeonsManager().invitingPlayer != player || party == null || !party.isLeader(invitedBy)) {
            player.closeInterfaces();
            player.getPackets().sendGameMessage("You can't do that right now.");
            return;
        }
        if (party.getTeam().size() >= 3) {
            player.closeInterfaces();
            player.getPackets().sendGameMessage("The party is full.");
            return;
        }
        invitedBy.getEliteDungeonsManager().resetInvitation();
        invitedBy.getEliteDungeonsManager().getParty().add(player);
        player.stopAll();
        invitedBy.stopAll();
    }

    public Object getRejoinKey() {
        return rejoinKey;
    }

    public void setRejoinKey(final Object rejoinKey) {
        this.rejoinKey = rejoinKey;
    }

    public void finish() {
        if (party != null) {
            party.leaveParty(player, true);
        }
    }

    public boolean isInside() {
        return party != null && party.getDungeon() != null && party.getDungeon().isInside(player);
    }

    public void openRewardsChest() {
        if (player.getControlerManager().getControler() == null || (!(player.getControlerManager().getControler() instanceof EliteDungeonController) && !(player.getControlerManager().getControler() instanceof EliteDungeonsLobby)))
            return;
        player.getInterfaceManager().sendInterface(168);
        refreshRewardsInterface();
    }

    public void refreshRewardsInterface() {
        player.getPackets().sendExecuteScript(8420, 11010049, 11010083, 11010050, 11010084, "Elite Dungeon Chest", 21218, 1007);
        player.getPackets().sendIComponentText(168, 33, "Current Reward Value: " + (Utils.formatNumber(getRewardsValue())));
        player.getPackets().sendInterSetItemsOptionsScript(168, 34, 99, 8, 4, "Take", "Bank", "Discard", "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(168, 34, 0, 30, 0, 1, 2, 3);
        player.getPackets().sendItems(99, rewards);
    }

    public long getRewardsValue() {
        long totalValue = 0;
        for (int i = 0; i < rewards.getItems().length; i++) {
            Item item = rewards.get(i);
            if (item == null)
                continue;
            totalValue += (long) item.getDefinitions().getValue() * (long) item.getAmount();
        }
        return totalValue;
    }

    public void HandleButtons(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (player.getControlerManager().getControler() == null || (!(player.getControlerManager().getControler() instanceof EliteDungeonController) && !(player.getControlerManager().getControler() instanceof EliteDungeonsLobby)))
            return;
        if (interfaceId == 168) {
            if (rewards.isEmpty())
                return;
            switch (componentId) {
            case 27:
            case 18:
                for (int i = 0; i < rewards.getItems().length; i++)
                    claimReward(i, componentId == 27);
                rewards.shift();
                refreshRewardsInterface();
                player.getPackets().sendGameMessage(rewards.isEmpty() ? "All the items were moved to your " + (componentId == 31 ? "bank" : "inventory") + "." : "You didn't have enough " + (componentId == 31 ? "bank" : "inventory") + " space for some of your items.");
                break;
            case 9:
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendOptionsDialogue("ARE YOU SURE YOU WANT TO ABANDON ALL OF YOUR ITEMS?", "Yes, I am sure", "No, Nevermind.");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        end();
                        if (componentId == OPTION_1) {
                            rewards.clear();
                            rewards.shift();
                            refreshRewardsInterface();
                            player.getPackets().sendGameMessage("You have discarded all of your items.");
                        }
                    }

                    @Override
                    public void finish() {
                    }
                });
                break;
            case 34:
                Item item = rewards.get(slotId);
                if (item == null) {
                    return;
                }
                switch (packetId) {
                case PacketRepository.ACTION_BUTTON4_PACKET:
                    player.getPackets().sendGameMessage("It's a " + item.getDefinitions().getName());
                    return;
                case PacketRepository.ACTION_BUTTON3_PACKET:
                    rewards.set(slotId, null);
                    rewards.shift();
                    refreshRewardsInterface();
                    return;
                case PacketRepository.ACTION_BUTTON2_PACKET:
                case PacketRepository.ACTION_BUTTON1_PACKET:
                    claimReward(slotId, packetId == PacketRepository.ACTION_BUTTON2_PACKET);
                    if (rewards.get(slotId) != null)
                        player.getPackets().sendGameMessage("You only had enough space in your " + (packetId == PacketRepository.ACTION_BUTTON2_PACKET ? "bank" : "inventory") + " to accept some of the items.");
                    rewards.shift();
                    refreshRewardsInterface();
                    return;
                }
            }
        }
    }

    public void claimReward(int index, boolean bank) {
        if (player.getControlerManager().getControler() == null || (!(player.getControlerManager().getControler() instanceof EliteDungeonController) && !(player.getControlerManager().getControler() instanceof EliteDungeonsLobby)))
            return;
        Item item = rewards.get(index);
        if (item == null)
            return;
        if (!bank) {
            if (!item.getDefinitions().isStackable() && !item.getDefinitions().isNoted()) {
                for (int i = 0; i < item.getAmount(); i++) {
                    if (!player.getInventory().addItemToInventory(item.getId(), 1))
                        break;
                    if (item.getAmount() - 1 <= 0) {
                        rewards.set(index, null);
                    } else
                        item.setAmount(item.getAmount() - 1);
                }
                return;
            }
            long amountToTake = item.getAmount();
            int amountInInv = player.getInventory().getItems().getNumberOf(item.getId());
            if (item.getId() == 995) {
                int amountInPouch = player.getMoneyPouch().getTotal();
                if (amountToTake > ((long) (Integer.MAX_VALUE - (amountInInv == 0 && player.getInventory().getFreeSlots() == 0 ? Integer.MAX_VALUE : amountInInv)) + (long) (Integer.MAX_VALUE - amountInPouch)))
                    amountToTake = (long) (Integer.MAX_VALUE - (amountInInv == 0 && player.getInventory().getFreeSlots() == 0 ? Integer.MAX_VALUE : amountInInv)) + (long) (Integer.MAX_VALUE - amountInPouch);
            } else {
                if (amountInInv == 0 && player.getInventory().getFreeSlots() == 0)
                    return;
                if (amountToTake > (Integer.MAX_VALUE - amountInInv))
                    amountToTake = (Integer.MAX_VALUE - amountInInv);
            }
            if (amountToTake == 0)
                return;
            if (item.getAmount() - amountToTake <= 0) {
                rewards.set(index, null);
            } else
                item.setAmount(item.getAmount() - (int) amountToTake);
            if (item.getId() == 995) {
                int amountInPouch = player.getMoneyPouch().getTotal();
                int amountToPouch = (int) (amountToTake > (long) (Integer.MAX_VALUE - amountInPouch) ? (Integer.MAX_VALUE - amountInPouch) : amountToTake);
                if (amountToPouch > 0)
                    player.getMoneyPouch().addMoneyMisc(amountToPouch);
                amountToTake -= amountToPouch;
            }
            if (amountToTake > 0)
                player.getInventory().addItemToInventory(item.getId(), (int) amountToTake);
            return;
        }
        Item bankItem = player.getBank().getItemIncludingPlaceHolders(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId());
        if (bankItem == null) {
            if (player.getBank().isFull())
                return;
            player.getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), item.getAmount(), 0, null, true);
            rewards.set(index, null);
            return;
        }
        int amountToTake = item.getAmount();
        if (((long) bankItem.getAmount() + (long) amountToTake) > Integer.MAX_VALUE)
            amountToTake = Integer.MAX_VALUE - bankItem.getAmount();
        if (amountToTake == 0)
            return;
        player.getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountToTake, 0, null, true);
        if (item.getAmount() - amountToTake <= 0) {
            rewards.set(index, null);
        } else
            item.setAmount(item.getAmount() - amountToTake);
    }

    public boolean addReward(Item item) {
        if (rewards.getFreeSlots() == 0 && rewards.getNumberOf(item.getId()) == 0)
            return false;
        int amountInside = rewards.getNumberOf(item.getId());
        if (((long) amountInside + (long) item.getAmount()) > Integer.MAX_VALUE)
            return false;
        return rewards.add(item);
    }

    public void toggleAutoLoot() {
        autoLoot = !autoLoot;
        player.getPackets().sendGameMessage(autoLoot ? "The chest will now collect loot for you." : "The chest will no longer collect loot for you. You will have a chance to receive double drops.");
    }
}
