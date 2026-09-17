package com.rs.game.player.content.slayer;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.io.Serializable;

/**
 * Handles Cooperative Slayer and all necessary components.
 *
 * @author Noel.
 */
public class CooperativeSlayer implements Serializable {

    private static final long serialVersionUID = -5833463661237303707L;

    public void handleLogout(Player player) {
        Player newPartner;
        String host;
        host = player.getSlayerHost();
        newPartner = World.getPlayerByDisplayName(host);
        if (player.hasOngoingInvite && newPartner != null) {
            newPartner.hasInvited = false;
            newPartner.sendMessage("<col=C43140>" + player.getDisplayName()
                    + " has logged out, your invite request has been deleted.</col>");
        }
        resetPartner(player);
    }

    public void sendInvite(final Player player) {
        if (player.isATypeOfIronman() && !player.isGroupIronman()) {
            player.sendMessage("Ironmen cannot do Co-op slayer.");
            return;
        }
        if (player.isKingOfTheSkillGameMode()) {
            player.getKingOfTheSkillGameModeHandler().sendNoCoOpSlayerToPlayer();
            return;
        }
        Player host = World.getPlayerByDisplayName(player.getSlayerHost());
        if (host != null) {
            if (host.isGroupIronman() && !host.canGimInteractWith(player)) {
                host.sendGimCantInteract();
                return;
            } else if (player.isGroupIronman() && !player.canGimInteractWith(host)) {
                host.sendMessage("Ironmen cannot do Co-op slayer.");
                return;
            }
        }
        player.sendMessage(
                "<col=BF00C9>You have received a slayer invitation from " + player.getSlayerHost() + "</col>");
        player.hasHost = true;
        player.getInterfaceManager().sendInterface(1021);
        player.getPackets().sendIComponentText(1021, 8, "Combat level: " + host.getSkills().getCombatLevelWithSummoning());
        player.getPackets().sendIComponentText(1021, 2, "Slayer level: " + host.getSkills().getLevelForXp(Skills.SLAYER));
        player.getPackets().sendExecuteScript(8420, 66912259, 66912262, 66912260, 66912263, "Slayer invitiation: " + host.getDisplayName(), 21218, 1007);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                player.sendMessage("<col=C43140>You have declined " + player.getSlayerHost() + "'s invitation.</col>");
                host.sendMessage("<col=C43140>Your invitation has been declined.</col>");
                player.setSlayerHost(null);
                host.setSlayerInvite(null);
                player.hasHost = false;
                host.hasInvited = false;
                player.hasOngoingInvite = false;
            }
        });
    }

    public void handleInviteButtons(Player invited, int interfaceId, int componentId) {
        Player newPartner;
        String host;
        host = invited.getSlayerHost();
        newPartner = World.getPlayerByDisplayName(host);
        if (interfaceId == 1021) {
            if (newPartner == null) {
                invited.hasOngoingInvite = false;
                invited.setCloseInterfacesEvent(null);
                invited.closeInterfaces();
                invited.hasHost = false;
                invited.hasInvited = false;
                invited.hasGroup = false;
                invited.setSlayerHost(null);
                invited.setSlayerInvite(null);
                invited.setSlayerPartner(null);
                invited.sendMessage("<col=C43140>Your potential partner has not been found, disbanding party.</col>");
                return;
            }
            switch (componentId) {
                case 10:
                    invited.sendMessage("<col=22C78D>Accepted " + invited.getSlayerHost()
                            + "'s invitation, you are now their Slayer partner.</col>");
                    invited.setSlayerPartner(host);
                    invited.hasGroup = true;
                    invited.hasOngoingInvite = false;
                    invited.hasInvited = false;
                    newPartner.hasInvited = false;
                    newPartner.sendMessage("<col=22C78D>Your invitation to " + newPartner.getSlayerInvite()
                            + " has been accepted, you are now their slayer partner.</col>");
                    newPartner.setSlayerPartner(newPartner.getSlayerInvite());
                    newPartner.hasGroup = true;
                    invited.setCloseInterfacesEvent(null);
                    if (newPartner.getItemTransaction() != null && !newPartner.getItemTransaction().isInTransaction())
                        newPartner.closeInterfaces();
                    invited.closeInterfaces();
                    break;
                case 14:
                    invited.sendMessage(
                            "<col=C43140>You have declined " + invited.getSlayerHost() + "'s invitation.</col>");
                    newPartner.sendMessage("<col=C43140>Your invitation has been declined.</col>");
                    invited.setSlayerHost(null);
                    newPartner.setSlayerInvite(null);
                    invited.hasHost = false;
                    newPartner.hasInvited = false;
                    invited.hasOngoingInvite = false;
                    invited.setCloseInterfacesEvent(null);
                    invited.closeInterfaces();
                    break;
            }
        }
    }

    public void handleCoOpSlayerInterface(Player player, int componentId) {
        Player newPartner;
        String host;
        host = player.getSlayerHost();
        newPartner = World.getPlayerByDisplayName(host);
        switch (componentId) {
            case 5:
                player.sendMessage("Use an Enchanted gem, a Slayer helmet or a Ring of slaying on the player to invite!");
                break;
            case 8:
                if (!player.hasOngoingInvite) {
                    player.sendMessage("You have not been invited by anyone yet!");
                    return;
                }
                player.getInterfaceManager().sendInterface(1310);
                player.getPackets().sendIComponentText(1310, 8, "" + player.getSkills().getLevelForXp(Skills.SLAYER));
                player.getPackets().sendIComponentText(1310, 6, "" + player.getDisplayName());
                player.getPackets().sendIComponentText(1310, 10, "" + player.getSkills().getCombatLevelWithSummoning());
                break;
            case 11:
                if (!player.hasGroup || player.getSlayerPartner() == null) {
                    player.sendMessage("You're not in a Slayer party at the moment.");
                    return;
                }
                if (World.containsPlayer(player.getSlayerPartner()) && player.getSlayerHost() == null) {
                    host = player.getUsername();
                    newPartner = World.getPlayer(player.getSlayerPartner());
                }
                player.sendMessage("<col=C43140>You have left your current Slayer party.</col>");
                player.setSlayerPartner(null);
                player.setSlayerInvite(null);
                player.hasHost = false;
                player.hasGroup = false;
                player.hasOngoingInvite = false;
                if (World.containsPlayer(host)) {
                    newPartner.sendMessage("<col=C43140>Your Slayer party has been disbanded.</col>");
                    newPartner.setSlayerInvite(null);
                    newPartner.setSlayerPartner(null);
                    newPartner.setSlayerHost(null);
                    newPartner.hasInvited = false;
                    newPartner.hasGroup = false;
                    newPartner.hasHost = false;
                }
                player.setSlayerHost(null);
                player.closeInterfaces();
                break;
        }
    }

    public void resetPartner(Player player) {
        player.sendMessage("<col=C43140>Your Slayer party has been disbanded.</col>");
        player.hasInvited = false;
        player.hasOngoingInvite = false;
        player.setSlayerInvite(null);
    }

    public void cleanConfig(Player player, String message) {
        resetPartner(player);
        /* These four items should not be touched on login */
        player.hasHost = false;
        player.hasGroup = false;
        player.setSlayerPartner(null);
        player.setSlayerHost(null);
        if (message != "none")
            player.sendMessage(
                    (message != "") ? message : Colors.SALMON + "Your duo-slayer configuration has been reset!", false);
    }

    public void loginCheck(Player player) {
        if (player.getSlayerPartner() == null || player.getSlayerPartner().equalsIgnoreCase(""))
            return;
        Player partner = null;
        if (player.getSlayerPartner() != null)
            partner = World.getPlayerByDisplayName(player.getSlayerPartner());
        /* Check if player has a partner who is online */
        if (partner != null) {
            if (partner.getSlayerPartner() == null
                    || !partner.getSlayerPartner().equalsIgnoreCase(player.getDisplayName()))
                cleanConfig(player, Colors.SALMON
                        + "Your previous slayer partner has a new partner, duo settings have been cleared!</col>");
        } else {
            /*
             * Player has an existing partner who is offline - Will start a
             * dialogue offering them to reset current partner
             */
            if (player.getSlayerPartner() != null) {
                String user = Colors.DCYAN + Colors.SHAD + Utils.formatPlayerNameForDisplay(player.getSlayerPartner())
                        + "</shad></col>";
                player.sendMessage(Colors.PINK + Colors.SHAD + "Your slayer partner, " + user
                        + ", is offline. Type ::reset to reset your slayer configuration!", false);
                /*
                 * player.getDialogueManager().startDialogue(new Dialogue() {
                 *
                 * @Override public void start() { sendItemDialogue(4155, 1,
                 * "Your slayer partner, " + user +
                 * ", is offline. Would you like to reset your duo-slayer configuration?"
                 * ); stage = 0; }
                 *
                 * @Override public void run(int interfaceId, int componentId) {
                 * switch (stage) { case 0:
                 * sendOptionsDialogue("Reset your slayer partner?", "Yes",
                 * "No"); stage = 1; break; case 1: finish(); switch
                 * (componentId) { case OPTION_1: // Reset the slayer partner
                 * cleanConfig(player, ""); break; case OPTION_2: // Don't reset
                 * the slayer partner player.sendMessage(user +
                 * " will not be removed as your slayer partner."); break; }
                 * break; } }
                 *
                 * @Override public void finish() {
                 * player.getInterfaceManager().closeChatBoxInterface(); } });
                 */
            }
        }
    }

}