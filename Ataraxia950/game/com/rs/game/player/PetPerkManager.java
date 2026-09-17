package com.rs.game.player;

import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.pet.Pet;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.dialogue.impl.PetPerksD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketDispatcher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author _jordan
 */
public class PetPerkManager implements Serializable {

	private static final long serialVersionUID = -4071648590005168725L;

	// unsaved vars
	private transient Player player;

	// saved vars
	public final LinkedHashMap<Integer, Map<PetPerks, Integer>> upgradedPets;
	private boolean turnedOff;
	private boolean turnedOffGlow;
	private boolean didTutorial;

	/**
	 * Constructs a new class.
	 */
	public PetPerkManager() {
		this.upgradedPets = new LinkedHashMap<>();
	}

	public void addPerkToPet(PetPerks perk) {
		Pet pet = player.getPet();
		if (pet == null) {
			player.getPackets().sendGameMessage("You currently do not have a pet.");
			return;
		}
		if (perk == null)
			return;
		if (!didTutorial) {
			player.getDialogueManager().startDialogue(PetPerksD.class.getSimpleName());
			return;
		}

		int slots = getNumberOfSlotsCanUse();
		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());
		if (perks == null || perks.isEmpty())// this happens when the pet used has no perks on it.
			perks = new HashMap<PetPerks, Integer>();

		if (perks.containsKey(perk)) {
			upgradePerk(pet, perk, perks);
			return;
		}

		if (perks.size() + 1 > slots/* || perks.containsKey(perk) */) {// if too many perks or already has the perk.
			player.sendMessage("You are unable to add this perk to your pet.");
			return;
		}

		add(pet, perk, perks, 1);// tier 1 automatically
	}

	private void upgradePerk(Pet pet, PetPerks perk, Map<PetPerks, Integer> perks) {
		int tier = perks.get(perk);
		if (tier + 1 > 3) {
			player.getPackets().sendGameMessage("You are unable to upgrade that many tiers.", true);
			return;
		}

		add(pet, perk, perks, tier + 1);
	}

	public boolean currentPetShouldGlow() {
		if (!hasActivePetPerk())// checks if pet is active basically
			return false;

		int number = 0;
		for (Entry<PetPerks, Integer> perks : upgradedPets.get(player.getPet().getItemId()).entrySet()) {
			if (perks.getValue() == 3)
				number++;
		}

		return (number >= 6);
	}

	public void glowPet() {
		if (turnedOffGlow)
			return;

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (player == null || player.hasFinished() || !hasActivePetPerk() || player.getPet() == null || turnedOffGlow) {
					stop();
					return;
				}

				player.getPet().setNextGraphics(new Graphics(3940));
			}

		}, 0, 50);
	}

	public void removeGlow() {
		if (!hasActivePetPerk())// checks if pet is active basically
			return;

		player.getPet().setNextGraphics(new Graphics(-1));
	}

	private void add(Pet pet, PetPerks perk, Map<PetPerks, Integer> perks, int tier) {
		player.getInventory().deleteItem(perk.itemId, 1);

		perks.put(perk, tier);// add perk
		upgradedPets.put(pet.getItemId(), perks);// add pet

		player.getPackets().sendGameMessage("You have successfully attached the " + perk.name + " perk at tier " + tier + " to your pet.");

		if (currentPetShouldGlow())
			glowPet();
	}

	public boolean hasActivePetPerk() {
		if (turnedOff)// checks if the player has pet perks setting turned on/off.
			return false;

		Pet pet = player.getPet();
		if (pet == null || !upgradedPets.containsKey(pet.getItemId()))
			return false;

		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());
		if (perks == null || perks.isEmpty())
			return false;

		return (player.getPet() != null && !perks.isEmpty());
	}

	public boolean ifActivePetHasPerk(PetPerks perk) {
		if (!hasActivePetPerk())// checks if pet is active basically
			return false;

		return (upgradedPets.get(player.getPet().getItemId()).containsKey(perk));
	}

	public double getProModifierForPerk(PetPerks perk) {
		if (!hasActivePetPerk())// checks if pet is active
			return 0;

		Pet pet = player.getPet();
		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());
		if (!perks.containsKey(perk))
			return 0;

		double rate = (perk == null ? 0 : Math.max(0.01, perk.proMultiplier * perks.get(perk)));
//		Logger.getGlobal().info("PRO MOD: " + rate + ", " + perk);

		// also return 0 for everything else cos thats how it has to be done lawl
		return rate;
	}

	public double getConModifierForPerk(PetPerks perk) {
		if (!hasActivePetPerk())// checks if pet is active
			return 0;

		Pet pet = player.getPet();
		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());
		if (!perks.containsKey(perk))
			return 0;

		double rate = (perk == null ? 0 : Math.max(0.01, (perk.conMultiplier / perks.get(perk))));
//		Logger.getGlobal().info("CON MOD: " + rate + ", " + perk);

		// also return 0 for everything else cos thats how it has to be done lawl
		return rate;
	}

	public String getCurrentPetPerkDescription(PetPerks perk) {
		int multiplier = 1;
		if (hasActivePetPerk() && ifActivePetHasPerk(perk))
			multiplier = upgradedPets.get(player.getPet().getItemId()).get(perk);

		return (perk.proDescription + " " + Math.max(1, (int) ((perk.proMultiplier * multiplier) * 100)) + "% " + (perk.conDescription == null ? "." : " and " + perk.conDescription + " " + Math.max(1, (int) ((perk.conMultiplier / multiplier) * 100)) + "%."));
	}

	public String getBasePerkDescription(PetPerks perk) {
		return (perk.proDescription + " " + (int) (perk.proMultiplier * 100) + "% and " + perk.conDescription + " " + (int) (perk.conMultiplier * 100) + "%.");
	}

	public void upgradePerk(PetPerks perk) {
		if (!hasActivePetPerk())// checks if pet is active
			return;

		Pet pet = player.getPet();
		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());

		if (!perks.containsKey(perk))// if the player does not have the perk we dont upgrade.
			return;

		int currentTier = perks.get(perk);
		if (currentTier + 1 <= 3) {// 3 is the max number of upgrades we can give to a single perk.
			perks.put(perk, currentTier + 1);
			player.sendMessage("You successfully upgrade your " + perk.getName() + " pet perk to tier " + perks.get(perk) + ".");
		}
	}

	public int getNumberOfSlotsCanUse() {
		int slots = 1;
		int spent = player.getMoneySpent();

		if (spent >= 1000 && player.isMasterDonator())
			slots = 7;
		else if (spent >= 500 && player.isUltimateDonator())
			slots = 6;
		else if (spent >= 250 && player.isLegendaryDonator())
			slots = 5;
		else if (spent >= 50 && player.isExtremeDonator())
			slots = 3;

		return slots;
	}

	public static boolean handleItemOnNPC(Player player, NPC npc, Item item) {
		if (!(npc instanceof Pet))// the npc in the world has to be a pet npc.
			return false;

		Pet pet = player.getPet();
		if (pet == null)
			return false;

		Pet petn = (Pet) npc;
		if (petn != pet) {
			player.sendMessage("This isn't your pet.", true);
			return false;
		}

		int itemId = item.getId();
		if (itemId <= 0)
			return false;

		if (!player.getInventory().containsItem(itemId, 1))
			return false;

		if (!ifItemIsPetPerk(itemId))// if the item used is not a pet perk item.
			return false;

		if (isExcludedPet(Pets.forId(pet.getItemId())))// if the current pet is a bossing/skilling pet.
			return false;

		PetPerks perk = getPerkForItemId(itemId);// the actual perk for the item id.
		if (perk == null)
			return false;

		player.getPetPerkManager().addPerkToPet(perk);

		return true;
	}

	private static boolean ifItemIsPetPerk(int itemId) {
		for (PetPerks perk : PetPerks.values()) {
			if (perk == null)
				continue;

			if (perk.itemId == itemId)
				return true;
		}

		return false;
	}

	private static PetPerks getPerkForItemId(int itemId) {
		for (PetPerks perk : PetPerks.values()) {
			if (perk == null)
				continue;

			if (perk.itemId == itemId)
				return perk;
		}

		return null;
	}

	private static boolean isExcludedPet(Pets pet) {
		// bossing and skilling pets.
		return (pet == Pets.VINDIDDY || pet == Pets.RAWRVEK || pet == Pets.LILWYR || pet == Pets.GREG || pet == Pets.NYLESSA || pet == Pets.AVA || pet == Pets.AHRIM || pet == Pets.DHAROK || pet == Pets.GUTHAN || pet == Pets.KARIL || pet == Pets.TORAG || pet == Pets.VERAC || pet == Pets.TUZZY || pet == Pets.KRARJNR || pet == Pets.YAKAMINU || pet == Pets.REEVES || pet == Pets.AGILITY || pet == Pets.CONSTRUCTION || pet == Pets.COOKING || pet == Pets.CRAFTING || pet == Pets.DIVINATION || pet == Pets.DUNGEONEERING || pet == Pets.FARMING || pet == Pets.FIREMAKING || pet == Pets.FISHING || pet == Pets.FLETCHING || pet == Pets.HERBLORE || pet == Pets.HUNTER || pet == Pets.INVENTION || pet == Pets.MINING || pet == Pets.RUNECRAFTING || pet == Pets.SLAYER || pet == Pets.SMITHING || pet == Pets.THIEVING || pet == Pets.WOODCUTTING || pet == Pets.DAVE || pet == Pets.STEVE || pet == Pets.PETE || pet == Pets.GAVIN || pet == Pets.LANA || pet == Pets.BILL || pet == Pets.DAG_PRIME || pet == Pets.DAG_SUPREME || pet == Pets.DAG_REX || pet == Pets.CHAOS_ELLIE || pet == Pets.CORP_BEAST || pet == Pets.KBD || pet == Pets.NEX || pet == Pets.BANDOS || pet == Pets.ZAMMY || pet == Pets.SARA || pet == Pets.ARMA || pet == Pets.BARRY || pet == Pets.MALLORY || pet == Pets.MOLLY || pet == Pets.SHRIMPY || pet == Pets.KALPHITE_KING || pet == Pets.QBD || pet == Pets.VITALIS || pet == Pets.BOMBI || pet == Pets.LEGIO_PRIMUS || pet == Pets.LEGIO_SECUNDUS || pet == Pets.LEGIO_TERTIUS || pet == Pets.LEGIO_QUARTUS || pet == Pets.LEGIO_QUINTUS || pet == Pets.LEGIO_SEXTUS || pet == Pets.KALPHITE_GRUBLET_I || pet == Pets.KALPHITE_GRUBLET_II || pet == Pets.ENDURING_GLACYTE || pet == Pets.SAPPING_GLACYTE || pet == Pets.UNSTABLE_GLACYTE || pet == Pets.EDDY);
	}

	public void sendPerksExamine(NPC npc) {
		if (!(npc instanceof Pet))// the npc in the world has to be a pet npc.
			return;

		if (!hasActivePetPerk())// checks for pet etc.
			return;

		Pet pet = player.getPet();
		Pet petn = (Pet) npc;
		if (petn != pet) {
			return;
		}

		// these just in case
		InterfaceManager manager = player.getInterfaceManager();
		if (manager == null)
			return;
		PacketDispatcher packets = player.getPackets();
		if (packets == null)
			return;

		// send interface
		manager.sendInterface(275);

		for (int i = 0; i < 309; i++) {
			packets.sendIComponentText(275, i, "");
		}

		packets.sendIComponentText(275, 1, pet.getName() + " perks");

		Map<PetPerks, Integer> perks = upgradedPets.get(pet.getItemId());

		if (perks != null) {

			int index = 11;
			for (Entry<PetPerks, Integer> perk : perks.entrySet()) {
				packets.sendIComponentText(275, index, perk.getKey().name + " - Tier " + perk.getValue());
				String string = getCurrentPetPerkDescription(perk.getKey());
				packets.sendIComponentText(275, index + 1, string.substring(0, string.length() > 62 ? 62 : string.length()));
				boolean f = false;
				if (string.length() > 62) {
					index++;
					f = true;
				}
				if (f)
					packets.sendIComponentText(275, index + 1, string.substring(62));
				index += 3;
			}
		}
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public LinkedHashMap<Integer, Map<PetPerks, Integer>> getUpgradedPets() {
		return upgradedPets;
	}

	public boolean isTurnedOff() {
		return turnedOff;
	}

	public void setTurnedOff(boolean turnedOff) {
		this.turnedOff = turnedOff;
	}

	public boolean isDidTutorial() {
		return didTutorial;
	}

	public void setDidTutorial(boolean didTutorial) {
		this.didTutorial = didTutorial;
	}

	public boolean isTurnedOffGlow() {
		return turnedOffGlow;
	}

	public void setTurnedOffGlow(boolean turnedOffGlow) {
		this.turnedOffGlow = turnedOffGlow;
	}

	public enum PetPerks {

		NICE_BUT_DIM("Nice but Dim", "Increases your drop rate", "decreases your experience gain", 0.05, 0.10, 41478), //
		POWER_EXCHANGE("Power Exchange", "Increases your combat damage", "increases the damage you take from all sources", 0.03, 0.10, 41471), //
		DOUBLE_TROUBLE("Double Trouble", "Increases the chance to double drops you receive", "has a chance to give you no drop by", 0.02, 0.05, 41473), //
		KURADAMN("Kuradamn", "Increases your Slayer experience gained by", null, 0.05, 0, 41475), //
		EXECUTIONERS_DEMISE("Executioner's Demise", "Increases your chance to fully restore your hitpoints during combat", "increases your chance to instantly die during combat", 0.0175, 0.02, 41477),//
		OVERLOADED("Overloaded", "Grants a permanent overload effect and increases your combat  accuracy", null, 0.05, 0, 41474);

		private final String name;
		private final String proDescription;
		private final String conDescription;
		private final double proMultiplier;
		private final double conMultiplier;
		private final int itemId;

		/**
		 * Constructs a new class.
		 */
		PetPerks(String name, String proDescription, String conDescription, double proMultiplier, double conMultiplier, int itemId) {
			this.name = name;
			this.proDescription = proDescription;
			this.conDescription = conDescription;
			this.proMultiplier = proMultiplier;
			this.conMultiplier = conMultiplier;
			this.itemId = itemId;
		}

		public String getName() {
			return name;
		}

		public String getProDescription() {
			return proDescription;
		}

		public String getConDescription() {
			return conDescription;
		}

		public double getProMultiplier() {
			return proMultiplier;
		}

		public double getConMultiplier() {
			return conMultiplier;
		}

		public int getItemId() {
			return itemId;
		}

	}

}
