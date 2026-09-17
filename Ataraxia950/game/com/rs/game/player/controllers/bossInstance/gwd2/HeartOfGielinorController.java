package com.rs.game.player.controllers.bossInstance.gwd2;

import static com.rs.game.player.content.HeartOfGielinor.SEREN;
import static com.rs.game.player.content.HeartOfGielinor.SLISKE;
import static com.rs.game.player.content.HeartOfGielinor.ZAMORAK;
import static com.rs.game.player.content.HeartOfGielinor.ZAROS;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.GregorovicInstance;
import com.rs.game.activities.instances.HelwyrInstance;
import com.rs.game.activities.instances.TwinFuriesInstance;
import com.rs.game.activities.instances.VindictaInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.SerenFaction;
import com.rs.game.npc.gwd2.SerenReinforcement;
import com.rs.game.npc.gwd2.SliskeFaction;
import com.rs.game.npc.gwd2.SliskeReinforcement;
import com.rs.game.npc.gwd2.ZamorakFaction;
import com.rs.game.npc.gwd2.ZamorakReinforcement;
import com.rs.game.npc.gwd2.ZarosFaction;
import com.rs.game.npc.gwd2.ZarosReinforcement;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.dialogue.impl.heartofgielinor.HeartOfGielinorInstanceD;
import com.rs.utils.Utils;

public class HeartOfGielinorController extends Controller {

	@Override
	public void start() {
		sendInterfaces();
	}

	@Override
	public void sendInterfaces() {
		if (player.getHeart().getMultiplier() < 100)
			player.getHeart().setMultiplier(100);
		player.getHeart().setMultiplierStage();
		player.getPackets().sendIComponentText(601, 13, "Seren kills:");
		player.getPackets().sendIComponentText(601, 14, "Sliske kills:");
		player.getPackets().sendIComponentText(601, 15, "Zaros kills:");
		player.getInterfaceManager().sendMinigameHudInterface(601);
        player.getInterfaceManager().sendOverlay(945, true);
        for (int i = 23; i < Utils.getInterfaceDefinitionsComponentsSize(945); i++)
            player.getPackets().sendHideIComponent(945, i, true);
        for (int i = 0; i < 6; i++)
            player.getPackets().sendHideIComponent(945, i, i != 4);
        player.getPackets().sendHideIComponent(945, 6, true);
		player.getHeart().refresh();
	}

	public void removeInterfaces() {
		player.getInterfaceManager().removeMinigameHudInterface();
		player.getInterfaceManager().closeOverlay(false);
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
		return !s.equals("b") && !s.contains("bank");
	}
	
	@Override
	public boolean canHit(Entity entity) {
		if (entity instanceof NPC && ((NPC) entity).isCantInteract())
			return false;
		if (player.getHeart().getInsigniaSettings()[2]) {
			final int god = player.getHeart().getActiveInsignia();
			if (god == -1)
				return true;
			if (god == HeartOfGielinor.SEREN && entity instanceof SerenFaction)
				return false;
			else if (god == HeartOfGielinor.SLISKE && entity instanceof SliskeFaction)
				return false;
			else if (god == HeartOfGielinor.ZAMORAK && entity instanceof ZamorakFaction)
				return false;
			else if (god == HeartOfGielinor.ZAROS && entity instanceof ZarosFaction)
				return false;
		}
		return (!(entity instanceof ZamorakReinforcement) || !((ZamorakReinforcement) entity).getOwner().equals(player))
				&& (!(entity instanceof ZarosReinforcement) || !((ZarosReinforcement) entity).getOwner().equals(player))
				&& (!(entity instanceof SliskeReinforcement) || !((SliskeReinforcement) entity).getOwner().equals(player))
				&& (!(entity instanceof SerenReinforcement) || !((SerenReinforcement) entity).getOwner().equals(player));
	}
	
	@Override
	public boolean processPlayerOption1(Entity entity) {
		if (player != null && player.getHeart() != null && player.getHeart().getInsigniaSettings() != null && player.getHeart().getInsigniaSettings()[2]) {
			final int god = player.getHeart().getActiveInsignia();
			if (god == -1)
				return true;
			if (god == HeartOfGielinor.SEREN && entity instanceof SerenFaction ||
					god == HeartOfGielinor.SLISKE && entity instanceof SliskeFaction ||
					god == HeartOfGielinor.ZAMORAK && entity instanceof ZamorakFaction ||
					god == HeartOfGielinor.ZAROS && entity instanceof ZarosFaction) {
				player.sendMessage("You cannot attack your faction.");
				return false;
			}
		}
		if (entity instanceof ZamorakReinforcement && ((ZamorakReinforcement) entity).getOwner().equals(player)
				|| entity instanceof ZarosReinforcement  && ((ZarosReinforcement) entity).getOwner().equals(player)
				|| entity instanceof SliskeReinforcement  && ((SliskeReinforcement) entity).getOwner().equals(player)
				|| entity instanceof SerenReinforcement && ((SerenReinforcement) entity).getOwner().equals(player)) {
			player.sendMessage("You cannot kill your own summoned reinforcements.");
			return false;
		}
		return true;
	}
	
	private void detransmogrify() {
		if (player.isShadow()) {
			player.setShadow(false);
			player.setNextAnimation(new Animation(-1));
			player.getAppearence().transformIntoNPC(-1);
		}
	}
	
	@Override
	public boolean canMove(int dir) {
		if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof HelwyrInstance) {
			HelwyrInstance instance = (HelwyrInstance) player.getCurrentInstance();
			if (instance.getTiles().isEmpty()) {
				if (!player.getRun())
					player.setRunHidden(true);
				return true;
			}
			for (WorldTile tile : instance.getTiles()) {
				if (tile.getDistance(player) < 2) {
					if (player.getRun()) {
						player.setRunHidden(false);
						return true;
					}
				}
			}
			if (!player.getRun())
				player.setRunHidden(true);
			return true;
		}
		return true;
	}
	
	public WorldTile getWaitingRoom(int bossId) {
		switch(bossId) {
		case SEREN:
			return new WorldTile(3277, 6898, 1);
		case ZAROS:
			return new WorldTile(3114, 6898, 1);
		case ZAMORAK:
			return new WorldTile(3116, 7059, 1);
		default:
			return new WorldTile(3286, 7062, 1);
		}
	}

	@Override
	public boolean logout() {
		detransmogrify();
		if (player.getCurrentInstance() != null/* && checkInstance()*/) {
			final WorldTile tile = getWaitingRoom(player.getCurrentInstance().bossId);
			if (tile != null)
				player.setLocation(tile);
			player.getCurrentInstance().removePlayer(player);
		}
		return false;
	}
	
	public boolean checkInstance() {
		if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
			if (player.getCurrentInstance() != null)
				player.getCurrentInstance().removePlayer(player);
			return true;
		}
		return false;
	}

	@Override
	public boolean sendDeath() {
		checkInstance();
		removeControler();
		removeInterfaces();
		detransmogrify();
		return true;
	}

	@Override
	public boolean login() {
		sendInterfaces();
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch(object.getId()) {
		case 103559:
			player.sendMessage("The door appears to be locked up.");
			return false;
		}
		if (object.getId() == 101909) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				player.setNextWorldTile(new WorldTile(3114, 6898, 1));
				player.setCurrentInstance(null);
				return false;
			}
			if (player.getY() >= 6902) {
				if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) || player.getHeart().getKillcount(ZAROS) >= (player.getHeart().getReputation(ZAROS) < 4500 ? 40 : 20) || hasActiveInstance(HeartOfGielinor.ZAROS)) {
					WorldTile tile = new WorldTile(3115, 6900, 1);
					player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
				} else
					player.getPackets().sendGameMessage("You must have at least 40 Zarosian killcount to enter.");
			} else {
				WorldTile tile = new WorldTile(3117, 6902, 1);
				player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
			}
			return false;
		} else if (object.getId() == 101906) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				player.setNextWorldTile(new WorldTile(3126, 7049, 1));
				player.setCurrentInstance(null);
				return false;
			}
			if (player.getY() <= 7046) {
				if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) || player.getHeart().getKillcount(ZAMORAK) >= (player.getHeart().getReputation(ZAMORAK) < 4500 ? 40 : 20) || hasActiveInstance(HeartOfGielinor.ZAMORAK)) {
					WorldTile tile = new WorldTile(3126, 7048, 1);
					player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
				} else
					player.getPackets().sendGameMessage("You must have at least 40 Zamorakian killcount to enter.");
			} else {
				WorldTile tile = new WorldTile(3128, 7046, 1);
				player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
			}
			return false;
		} else if (object.getId() == 101901) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				player.setNextWorldTile(new WorldTile(3280, 7056, 1));
				player.setCurrentInstance(null);
				return false;
			}
			if (player.getY() <= 7053) {
				if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) || player.getHeart().getKillcount(SLISKE) >= (player.getHeart().getReputation(SLISKE) < 4500 ? 40 : 20) || hasActiveInstance(HeartOfGielinor.SLISKE)) {
					WorldTile tile = new WorldTile(3279, 7055, 1);
					player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
				} else
					player.getPackets().sendGameMessage("You must have at least 40 Sliskean killcount to enter.");
			} else {
				WorldTile tile = new WorldTile(3277, 7053, 1);
				player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
			}
			return false;
		} else if (object.getId() == 101897) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				player.setNextWorldTile(new WorldTile(3274, 6901, 1));
				player.setCurrentInstance(null);
				return false;
			}
			if (player.getX() <= 3271) {
				if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) || player.getHeart().getKillcount(SEREN) >= (player.getHeart().getReputation(SEREN) < 4500 ? 40 : 20) || hasActiveInstance(HeartOfGielinor.SEREN))
					player.addWalkSteps(3274, 6901, -1, false);
				else
					player.getPackets().sendGameMessage("You must have at least 40 Serenist killcount to enter.");
			} else
				player.addWalkSteps(3271, 6903, -1, false);
			return false;
		} else if (object.getId() == 101865) {
			player.getDialogueManager().startDialogue("WoodenLiftD");
			return false;
		} else if (object.getId() == 101910) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof VindictaInstance) {
					if (player.getX() <= player.getCurrentInstance().getWorldTile(39, 38).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.ZAROS);
			return false;
		} else if (object.getId() == 101907) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof TwinFuriesInstance) {
					if (player.getX() < player.getCurrentInstance().getWorldTile(43, 20).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.ZAMORAK);
			return false;
		} else if (object.getId() == 101902) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof GregorovicInstance) {
					if (player.getX() > player.getCurrentInstance().getWorldTile(31, 30).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.SLISKE);
			return false;
		} else if (object.getId() == 101898) {
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof HelwyrInstance) {
					if (player.getY() <= player.getCurrentInstance().getWorldTile(25, 47).getY()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.SEREN);
			return false;
		}
		return true;
	}
	
	private final boolean hasActiveInstance(final int bossId) {
		for (int i = 0; i < World.getInstances().size(); i++)
			if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).isStable && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName()))
				return true;
		return false;
	}
	
	@Override
	public boolean processObjectClick2(WorldObject object) {
		switch (object.getId()) {
		case 103559:
			player.sendMessage("The door appears to be locked up.");
			return false;
		case 101898:
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof HelwyrInstance) {
					if (player.getY() <= player.getCurrentInstance().getWorldTile(25, 47).getY()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					} else {
						HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((HelwyrInstance) player.getCurrentInstance()).getHelwyr(), true);
						player.setNextWorldTile(player.getCurrentInstance().getWorldTile(25, 46));
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", 22438);
			return false;
		case 101910:
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof VindictaInstance) {
					if (player.getX() <= player.getCurrentInstance().getWorldTile(39, 38).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					} else {
						HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((VindictaInstance) player.getCurrentInstance()).getVindicta(), true);
						player.setNextWorldTile(player.getCurrentInstance().getWorldTile(38, 38));
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", 22460);
			return false;
		case 101907:
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof TwinFuriesInstance) {
					if (player.getX() < player.getCurrentInstance().getWorldTile(43, 20).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					} else {
						HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((TwinFuriesInstance) player.getCurrentInstance()).getAvaryss(), true);
						player.setNextWorldTile(player.getCurrentInstance().getWorldTile(40, 21));
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.ZAMORAK);
			return false;
		case 101902:
			if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
				if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof GregorovicInstance) {
					if (player.getX() > player.getCurrentInstance().getWorldTile(31, 30).getX()) {
						player.sendMessage("You cannot leave just like that.");
						return false;
					} else {
						HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((GregorovicInstance) player.getCurrentInstance()).getGregorovic(), true);
						player.setNextWorldTile(player.getCurrentInstance().getWorldTile(33, 33));
						return false;
					}
				}
			}
			player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", HeartOfGielinor.SLISKE);
			return false;
		}
		return true;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		checkInstance();
		removeControler();
		removeInterfaces();
		detransmogrify();
		return true;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		checkInstance();
		removeControler();
		removeInterfaces();
		detransmogrify();
		return true;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		checkInstance();
		removeControler();
		removeInterfaces();
		detransmogrify();
		return true;
	}

}
