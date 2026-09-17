package com.rs.game.player;

import com.rs.Settings;
import com.rs.game.item.Item;

import java.io.Serializable;

/**
 * Handles everything related to Cosmetical Overrides.
 *
 * @author Noel.
 */
public class CosmeticOverrides implements Serializable {

	/**
	 * The generated serial UID.
	 */
	private static final long serialVersionUID = -2927386818603200182L;
	/**
	 * Init the Cosmetic Override enum.
	 */
	public CosmeticItems outfit;
	/**
	 * Booleans handling of each override slot.
	 */
	public boolean showHelm = true, showBody = true, showLegs = true, showBoots = true, showGloves = true, showCape = true, showWeapon = true, showShield = true;
	/**
	 * Represents all available overrides. IF SOMETHING HERE IS ADDED, ADD IT TO {@link CosmeticOverrides#transferOverrides(Player, Player)}
	 */
	public boolean retroCapes, paladin, warlord, obsidian, kalphite, demonflesh, remokee, assassin, skeleton, goth, mummy, replicaDragon, sentinel, reaver, hiker, skyguard, vyrewatch, snowman, samurai, warmWinter, darkLord;
	/**
	 * The player instance.
	 */
	private transient Player player;

	/**
	 * The player instance saving to.
	 *
	 * @param player
	 *            The player.
	 */
	protected void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Sets a Cosmetical Override as an outfit.
	 *
	 * @param outfit
	 *            The outfit to use.
	 */
	public void setOutfit(CosmeticItems outfit) {
		resetCosmetics();
		if (outfit == null)
			return;
		setCosmetic((outfit.getHelmId() == 0 || showHelm) ? null : new Item(outfit.getHelmId()), Equipment.SLOT_HAT);
		setCosmetic((outfit.getChestId() == 0 || showBody) ? null : new Item(outfit.getChestId()), Equipment.SLOT_CHEST);
		setCosmetic((outfit.getLegsId() == 0 || showLegs) ? null : new Item(outfit.getLegsId()), Equipment.SLOT_LEGS);
		setCosmetic((outfit.getBootsId() == 0 || showBoots) ? null : new Item(outfit.getBootsId()), Equipment.SLOT_FEET);
		setCosmetic((outfit.getGlovesId() == 0 || showGloves) ? null : new Item(outfit.getGlovesId()), Equipment.SLOT_HANDS);
		setCosmetic((outfit.getCapeId() == 0 || showCape) ? null : new Item(outfit.getCapeId()), Equipment.SLOT_CAPE);
		player.getAppearence().generateAppearenceData();
		this.outfit = outfit;
	}

	/*
	 * Sets a specified slot as cosmetic
	 *
	 * @param item The cosmetic item
	 * 
	 * @param slot The slot to set
	 */
	private void setCosmetic(Item item, int slot) {
		player.getAppearence().cosmeticItems[slot] = item;
	}

	/**
	 * Clears the cosmetic data
	 */
	public void resetCosmetics() {
		player.getAppearence().cosmeticItems = new Item[14];
		this.outfit = null;
		player.getAppearence().generateAppearenceData();
	}

	/**
	 * Gets the Retro version of a skillcape as an override.
	 *
	 * @param cape
	 *            The cape ID to get.
	 * @return the Cape item ID.
	 */
	public int getRetroCapeId(int capeId) {
		switch (capeId) {
		case 9747: // attack
			return 34540;
		case 9748: // attack T
			return 34542;
		case 34246: // attack hooded
			return 34541;
		case 34247: // attack hooded T
			return 34543;
		case 9750: // strength
			return 34545;
		case 9751: // strength T
			return 34547;
		case 34248: // strength hooded
			return 34546;
		case 34249: // strength hooded T
			return 34548;
		case 9753: // defence
			return 34550;
		case 9754: // defence T
			return 34552;
		case 34250: // defence hooded
			return 34551;
		case 34251: // defence hooded T
			return 34553;
		case 9756: // ranging
			return 34555;
		case 9757: // ranging T
			return 34557;
		case 34556: // ranging hooded
			return 34556;
		case 34557: // ranging hooded T
			return 34558;
		case 9759: // prayer
			return 34560;
		case 9760: // prayer T
			return 34562;
		case 34254: // prayer hooded
			return 34561;
		case 34255: // prayer hooded T
			return 34563;
		case 9762: // magic
			return 34565;
		case 9763: // magic T
			return 34567;
		case 34256: // magic hooded
			return 34566;
		case 34257: // magic hooded T
			return 34568;
		case 9765: // runecrafting
			return 34570;
		case 9766: // runecrafting T
			return 34572;
		case 34258: // runecrafting hooded
			return 34571;
		case 34259: // runecrafting hooded T
			return 34573;
		case 9768: // constitution
			return 34580;
		case 9769: // constitution T
			return 34582;
		case 34262: // constitution hooded
			return 34581;
		case 34263: // constitution hooded T
			return 34583;
		case 9771: // agility
			return 34585;
		case 9772: // agility T
			return 34587;
		case 34264: // agility hooded
			return 34586;
		case 34265: // agility hooded T
			return 34588;
		case 9774: // herblore
			return 34590;
		case 9775: // herblore T
			return 34592;
		case 34266: // herblore hooded
			return 34591;
		case 34267: // herblore hooded T
			return 34593;
		case 9777: // thieving
			return 34595;
		case 9778: // thieving T
			return 34597;
		case 34268: // thieving hooded
			return 34596;
		case 34269: // thieving hooded T
			return 34598;
		case 9780: // crafting
			return 34600;
		case 9781: // crafting T
			return 34602;
		case 34270: // crafting hooded
			return 34601;
		case 34271: // crafting hooded T
			return 34603;
		case 9783: // fletching
			return 34605;
		case 9784: // fletching T
			return 34607;
		case 34272: // fletching hooded
			return 34606;
		case 34273: // fletching hooded T
			return 34608;
		case 9786: // slayer
			return 34610;
		case 9787: // slayer T
			return 34612;
		case 34274: // slayer hooded
			return 34611;
		case 34275: // slayer hooded T
			return 34613;
		case 9789: // construction
			return 34615;
		case 9790: // construction T
			return 34617;
		case 34276: // construction hooded
			return 34616;
		case 34277: // construction hooded T
			return 34618;
		case 9792: // mining
			return 34620;
		case 9793: // mining T
			return 34622;
		case 34278: // mining hooded
			return 34621;
		case 34279: // mining hooded T
			return 34623;
		case 9795: // smithing
			return 34625;
		case 9796: // smithing T
			return 34627;
		case 34280: // smithing hooded
			return 34626;
		case 34281: // smithing hooded T
			return 34628;
		case 9798: // fishing
			return 34630;
		case 9799: // fishing T
			return 34632;
		case 34282: // fishing hooded
			return 34631;
		case 34283: // fishing hooded T
			return 34633;
		case 9801: // cooking
			return 34635;
		case 9802: // cooking T
			return 34637;
		case 34284: // cooking hooded
			return 34636;
		case 34285: // cooking hooded T
			return 34638;
		case 9804: // firemaking
			return 34640;
		case 9805: // firemaking T
			return 34642;
		case 34286: // firemaking hooded
			return 34641;
		case 34287: // firemaking hooded T
			return 34643;
		case 9807: // woodcutting
			return 34645;
		case 9808: // woodcutting T
			return 34647;
		case 34288: // woodcutting hooded
			return 34646;
		case 34289: // woodcutting hooded T
			return 34648;
		case 9810: // farming
			return 34650;
		case 9811: // farming T
			return 34652;
		case 34290: // farming hooded
			return 34651;
		case 34291: // farming hooded T
			return 34653;
		case 9948: // hunter
			return 34575;
		case 9949: // hunter T
			return 34577;
		case 34260: // hunter hooded
			return 34576;
		case 34261: // hunter hooded T
			return 34578;
		case 12169: // summoning
			return 34665;
		case 12170: // summoning T
			return 34667;
		case 34296: // summoning hooded
			return 34666;
		case 34297: // summoning hooded T
			return 34668;
		case 18508: // dungeoneering
			return 34660;
		case 18509: // dungeoneering T
			return 34662;
		case 34294: // dungeoneering hooded
			return 34661;
		case 34295: // dungeoneering hooded T
			return 34663;
		case 29185: // divination
			return 34655;
		case 29186: // divination T
			return 34657;
		case 34292: // divination hooded
			return 34656;
		case 34293: // divination hooded T
			return 34658;
		case 36351: // invention
			return 36789;
		case 36352: // invention T
			return 36791;
		case 36353: // invention hooded
			return 36790;
		case 36354: // invention hooded T
			return 36792;
		}
		return -1;
	}

	/**
	 * Sets Paladin Outfit as the current Cosmetic Override.
	 */
	public void setPaladinOutfit() {
		if (!paladin && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.PALADIN);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Warlord Outfit as the current Cosmetic Override.
	 */
	public void setWarlordOutfit() {
		if (!warlord && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.WARLORD);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Obsidian Outfit as the current Cosmetic Override.
	 */
	public void setObsidianOutfit() {
		if (!obsidian && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.OBSIDIAN);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Kaklphite Outfit as the current Cosmetic Override.
	 */
	public void setKalphiteOutfit() {
		if (!kalphite && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.KALPHITE);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Demonflesh Outfit as the current Cosmetic Override.
	 */
	public void setDemonfleshOutfit() {
		if (!demonflesh && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.DEMONFLESH);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Remokee Outfit as the current Cosmetic Override.
	 */
	public void setRemokeeOutfit() {
		if (!remokee && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.REMOKEE);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Assassin Outfit as the current Cosmetic Override.
	 */
	public void setAssassinOutfit() {
		if (!assassin && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.ASSASSIN);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Skeleton Outfit as the current Cosmetic Override.
	 */
	public void setSkeletonOutfit() {
		if (!skeleton && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.SKELETON);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Goth Outfit as the current Cosmetic Override.
	 */
	public void setGothOutfit() {
		if (!goth && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.GOTH);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Mummy Outfit as the current Cosmetic Override.
	 */
	public void setMummyOutfit() {
		if (!mummy && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.MUMMY);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Replica Dragon Outfit as the current Cosmetic Override.
	 */
	public void setReplicaDragonOutfit() {
		if (!replicaDragon && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.REPLICA_DRAGON);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Sentinel Outfit as the current Cosmetic Override.
	 */
	public void setSentinelOutfit() {
		if (!sentinel && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.SENTINEL);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Reaver Outfit as the current Cosmetic Override.
	 */
	public void setReaverOutfit() {
		if (!reaver && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.REAVER);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Hiker Outfit as the current Cosmetic Override.
	 */
	public void setHikerOutfit() {
		if (!hiker && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.HIKER);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Skyguard Outfit as the current Cosmetic Override.
	 */
	public void setSkyguardOutfit() {
		if (!skyguard && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.SKYGUARD);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Vyrewatch Outfit as the current Cosmetic Override.
	 */
	public void setVyrewatchOutfit() {
		if (!vyrewatch && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.VYREWATCH);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Snowman Outfit as the current Cosmetic Override.
	 */
	public void setSnowmanOutfit() {
		if (!snowman && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.SNOWMAN);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Samurai Outfit as the current Cosmetic Override.
	 */
	public void setSamuraiOutfit() {
		if (!samurai && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.SAMURAI);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Warm Winter Outfit as the current Cosmetic Override.
	 */
	public void setWarmWinterOutfit() {
		if (!warmWinter && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.WARM_WINTER);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	/**
	 * Sets Dark Lord Outfit as the current Cosmetic Override.
	 */
	public void setDarkLordOutfit() {
		if (!darkLord && !Settings.DEBUG && !player.isOwner()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You have to unlock " + "the '" + outfit.getName() + "'s' Outfit by purchasing it from our ::store " + "page - Cosmetic Overrides - section!");
			return;
		}
		setOutfit(CosmeticItems.DARK_LORD);
		player.getDialogueManager().startDialogue("SimpleMessage", "You are now using '" + outfit.getName() + "'s' override!");
	}

	public static void transferOverrides(Player from, Player to) {
		if (from.getOverrides().retroCapes) {
			to.getOverrides().retroCapes = from.getOverrides().retroCapes;
		}
		if (from.getOverrides().paladin) {
			to.getOverrides().paladin = from.getOverrides().paladin;
		}
		if (from.getOverrides().warlord) {
			to.getOverrides().warlord = from.getOverrides().warlord;
		}
		if (from.getOverrides().obsidian) {
			to.getOverrides().obsidian = from.getOverrides().obsidian;
		}
		if (from.getOverrides().kalphite) {
			to.getOverrides().kalphite = from.getOverrides().kalphite;
		}
		if (from.getOverrides().demonflesh) {
			to.getOverrides().demonflesh = from.getOverrides().demonflesh;
		}
		if (from.getOverrides().remokee) {
			to.getOverrides().remokee = from.getOverrides().remokee;
		}
		if (from.getOverrides().assassin) {
			to.getOverrides().assassin = from.getOverrides().assassin;
		}
		if (from.getOverrides().skeleton) {
			to.getOverrides().skeleton = from.getOverrides().skeleton;
		}
		if (from.getOverrides().goth) {
			to.getOverrides().goth = from.getOverrides().goth;
		}
		if (from.getOverrides().mummy) {
			to.getOverrides().mummy = from.getOverrides().mummy;
		}
		if (from.getOverrides().replicaDragon) {
			to.getOverrides().replicaDragon = from.getOverrides().replicaDragon;
		}
		if (from.getOverrides().sentinel) {
			to.getOverrides().sentinel = from.getOverrides().sentinel;
		}
		if (from.getOverrides().reaver) {
			to.getOverrides().reaver = from.getOverrides().reaver;
		}
		if (from.getOverrides().hiker) {
			to.getOverrides().hiker = from.getOverrides().hiker;
		}
		if (from.getOverrides().skyguard) {
			to.getOverrides().skyguard = from.getOverrides().skyguard;
		}
		if (from.getOverrides().vyrewatch) {
			to.getOverrides().vyrewatch = from.getOverrides().vyrewatch;
		}
		if (from.getOverrides().snowman) {
			to.getOverrides().snowman = from.getOverrides().snowman;
		}
		if (from.getOverrides().samurai) {
			to.getOverrides().samurai = from.getOverrides().samurai;
		}
		if (from.getOverrides().warmWinter) {
			to.getOverrides().warmWinter = from.getOverrides().warmWinter;
		}
		if (from.getOverrides().darkLord) {
			to.getOverrides().darkLord = from.getOverrides().darkLord;
		}
	}

	/**
	 * An enum containing all Cosmetic Model ID's.
	 *
	 * @author Noel
	 */
	public enum CosmeticItems {

		PALADIN("Paladin", 26464, 26466, 26468, 26470, 26472, 0),
		WARLORD("Warlord", 26182, 26184, 26186, 26190, 26188, 0),
		OBSIDIAN("Obsidian", 26128, 26140, 26136, 26126, 26124, 0),
		KALPHITE("Kalphite", 27075, 27077, 27079, 27080, 27078, 27076),
		DEMONFLESH("Demonflesh", 27120, 27122, 27124, 27128, 27126, 27130),
		REMOKEE("Remokee", 27148, 27149, 27150, 27151, 27152, 0),
		ASSASSIN("Assassin", 27181, 27182, 27183, 27184, 27185, 27186),
		SKELETON("Skeleton", 29782, 29784, 29786, 29790, 29788, 29792),
		GOTH("Goth", 29766, 29768, 29770, 29774, 29772, 0),
		MUMMY("Mummy", 29958, 29962, 29960, 29966, 29964, 0),
		REPLICA_DRAGON("Replica Dragon", 30191, 30193, 30194, 30195, 30196, 0),
		SENTINEL("Sentinel", 30597, 30598, 30599, 30600, 30601, 30602),
		REAVER("Reaver", 30606, 30607, 30608, 30609, 30610, 30611),
		HIKER("Hiker", 31296, 31297, 31298, 31299, 31301, 31300),
		SKYGUARD("Skyguard", 31536, 31537, 31538, 31539, 31540, 31543),
		VYREWATCH("Vyrewatch", 31546, 31547, 31548, 31550, 31549, 31551),
		SNOWMAN("Snowman", 33593, 33594, 33595, 0, 0, 0),
		SAMURAI("Samurai", 33637, 33638, 33639, 33640, 33641, 33642),
		WARM_WINTER("Warm Winter", 33755, 33756, 33757, 33758, 33759, 0),
		DARK_LORD("Dark Lord", 34222, 34223, 34224, 34225, 34226, 0);

		private final String name;
		private final int helm;
		private final int chest;
		private final int legs;
		private final int boots;
		private final int gloves;
		private final int cape;

		CosmeticItems(String name, int helm, int chest, int legs, int boots, int gloves, int cape) {
			this.name = name;
			this.helm = helm;
			this.chest = chest;
			this.legs = legs;
			this.boots = boots;
			this.gloves = gloves;
			this.cape = cape;
		}

		public String getName() {
			return name;
		}

		public int getHelmId() {
			return helm;
		}

		public int getChestId() {
			return chest;
		}

		public int getLegsId() {
			return legs;
		}

		public int getBootsId() {
			return boots;
		}

		public int getGlovesId() {
			return gloves;
		}

		public int getCapeId() {
			return cape;
		}
	}
}