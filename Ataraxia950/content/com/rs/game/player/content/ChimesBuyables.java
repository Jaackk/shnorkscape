package com.rs.game.player.content;

import com.google.common.collect.ImmutableMap;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.utils.Logger;

import java.io.IOException;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Sep 25, 2018.
 */
public final class ChimesBuyables {

	public static void main(String[] args) {
		try {
			Cache.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.getGlobal().catching(e);
		}
		Logger.getGlobal().info(ItemDefinitions.getItemDefinitions(1159).getWearingSkillRequiriments());
	}

	public static final ImmutableMap<Integer, Integer> BUYABLES = ImmutableMap.<Integer, Integer>builder()
			/* Armadyl godsword
			.put(11694, 200)
			// Bandos godsword
			.put(11696, 200)
			// Saradomin godsword
			.put(11698, 200)
			// Zamorak godsword
			.put(11700, 200)
			// Zamorakian spear
			.put(11716, 50)
			// Saradomin sword
			.put(11730, 50)*/
			// Arcane spirit shield
			.put(13738, 500)
			// Divine spirit shield
			.put(13740, 1500)
			// Elysian spirit shield
			.put(13742, 1000)
			// Spectral spirit shield
			.put(13744, 200)
			// Morrigan's javelin
			.put(13879, 4)
			// Morrigan's javelin (p)
			.put(13880, 5)
			// Morrigan's javelin (p+)
			.put(13881, 6)
			// Morrigan's javelin (p++)
			.put(13882, 7)
			// Morrigan's throwing axe
			.put(13883, 4)
			// Staff of light
			//.put(15486, 50)
			// Korasi's sword
			.put(19784, 100)
			// Demon horn necklace
			.put(19888, 50)
			// Torva full helm
			.put(20135, 500)
			// Torva platebody
			.put(20139, 1000)
			// Torva platelegs
			.put(20143, 750)
			// Pernix cowl
			.put(20147, 500)
			// Pernix body
			.put(20151, 1000)
			// Pernix chaps
			.put(20155, 750)
			// Virtus mask
			.put(20159, 500)
			// Virtus robe top
			.put(20163, 1000)
			// Virtus robe legs
			.put(20167, 750)
			// Torva full helm
			.put(20137, 500)
			// Torva platebody
			.put(20141, 1000)
			// Torva platelegs
			.put(20145, 750)
			// Pernix cowl
			.put(20149, 500)
			// Pernix body
			.put(20153, 1000)
			// Pernix chaps
			.put(20157, 750)
			// Virtus mask
			.put(20161, 500)
			// Virtus robe top
			.put(20165, 1000)
			// Virtus robe legs
			.put(20169, 750)
			// Zaryte bow
			.put(20171, 1500)
			// Abyssal vine whip
			//.put(21371, 100)
			// Battle-mage helm
			.put(21537, 250)
			// Battle-mage robe
			.put(21539, 500)
			// Battle-mage robe legs
			.put(21541, 350)
			// Battle-mage gloves
			.put(21543, 150)
			// Battle-mage boots
			.put(21545, 150)
			// Trickster helm
			.put(21547, 250)
			// Trickster robe
			.put(21549, 500)
			// Trickster robe legs
			.put(21551, 350)
			// Trickster gloves
			.put(21553, 150)
			// Trickster boots
			.put(21555, 150)
			// Vanguard helm
			.put(21557, 250)
			// Vanguard body
			.put(21559, 500)
			// Vanguard legs
			.put(21561, 350)
			// Vanguard gloves
			.put(21563, 150)
			// Vanguard boots
			.put(21565, 150)
			// Armadyl battlestaff
			//.put(21777, 300)
			// GWD2 Essence
			.put(37030, 250)
			// GWD2 Essence
			.put(37031, 250)
			// GWD2 Essence
			.put(37032, 250)
			// GWD2 Essence
			.put(37033, 250)
			// Steadfast boots
			.put(21787, 300)
			// Glaiven boots
			.put(21790, 300)
			// Ragefire boots
			.put(21793, 300)
			/* Ganodermic visor
			.put(22482, 25)
			// Ganodermic leggings
			.put(22486, 50)
			// Ganodermic poncho
			.put(22490, 150)
			// Polypore staff
			.put(22494, 50)
			// Polypore stick
			.put(22498, 25)*/
			// Pernix gloves
			.put(24974, 400)
			// Torva gloves
			.put(24977, 400)
			// Virtus gloves
			.put(24980, 400)
			// Torva boots
			.put(24983, 400)
			// Virtus boots
			.put(24986, 400)
			// Pernix boots
			.put(24989, 400)
			// Armadyl crossbow
			//.put(25037, 300).put(34855, 250)
			// Virtus wand
			.put(25654, 500)
			// Virtus book
			.put(25664, 500)
			/* Ganodermic gloves
			.put(25978, 10)
			// Ganodermic boots
			.put(25980, 10)*/
			// Tetsu helm
			.put(26325, 50)
			// Tetsu body
			.put(26326, 100)
			// Tetsu platelegs
			.put(26327, 75)
			// Seasinger's hood
			.put(26337, 50)
			// Seasinger's robe top
			.put(26338, 100)
			// Seasinger's robe bottom
			.put(26339, 75)
			// Death Lotus hood
			.put(26346, 50)
			// Death Lotus chestplate
			.put(26347, 100)
			// Death Lotus chaps
			.put(26348, 75)
			// Drygore rapier
			.put(26579, 1500)
			// Drygore longsword
			.put(26587, 1500)
			// Drygore mace
			.put(26595, 2000)
			// Static gloves
			.put(27481, 500)
			// Tracking gloves
			.put(27484, 500)
			// Pneumatic gloves
			.put(27487, 500)
			// Ascension crossbow
			.put(28437, 1500)
			// Tectonic mask
			.put(28608, 1750)
			// Tectonic robe top
			.put(28611, 2250)
			// Tectonic robe bottom
			.put(28614, 2000)
			// Tectonic mask
			.put(28610, 500)
			// Tectonic robe top
			.put(28613, 1000)
			// Tectonic robe bottom
			.put(28616, 750)
			// Seismic wand
			.put(28617, 2000)
			// Seismic singularity
			.put(28621, 2000)
			// Sirenic mask
			.put(29854, 1500)
			// Sirenic hauberk
			.put(29857, 2000)
			// Sirenic chaps
			.put(29860, 1750)
			// Sirenic mask
			.put(29856, 500)
			// Sirenic hauberk
			.put(29859, 1000)
			// Sirenic chaps
			.put(29862, 750)
			// Malevolent helm
			.put(30005, 1750)
			// Malevolent cuirass
			.put(30008, 2250)
			// Malevolent greaves
			.put(30011, 2000)
			// Malevolent helm
			.put(30007, 500)
			// Malevolent cuirass
			.put(30010, 1000)
			// Malevolent greaves
			.put(30013, 750)
			// Malevolent kiteshield
			.put(30014, 1000)
			// Vengeful kiteshield
			.put(30018, 1000)
			// Merciless kiteshield
			.put(30022, 1000)
			// Razorback gauntlets
			.put(30213, 500)
			// Reefwalker's cape
			.put(30568, 150)
			// Leviathan ring
			.put(30576, 100)
			/* Abyssal wand
			.put(30825, 50)
			// Abyssal Orb
			.put(30828, 50)*/
			// Celestial handwraps
			.put(31189, 500)
			// Ascension grips
			.put(31203, 250)
			// Noxious scythe
			.put(31725, 2500)
			// Noxious staff
			.put(31729, 2500)
			// Noxious longbow
			.put(31733, 2500)
			// Attuned crystal shield
			.put(32627, 350)
			// Attuned crystal deflector
			.put(32629, 350)
			// Attuned crystal ward
			.put(32631, 350)
			// Attuned crystal halberd
			.put(32647, 350)
			// Attuned crystal dagger
			.put(32649, 350)
			// Attuned crystal bow
			.put(32653, 350)
			// Attuned crystal chakram
			.put(32655, 350)
			// Attuned crystal staff
			.put(32659, 350)
			// Attuned crystal wand
			.put(32661, 350)
			// Attuned crystal orb
			.put(32663, 350)
			// Ripper claw
			.put(36004, 1000)
			// Off-hand ripper claw
			.put(36008, 1000)
			// Camel staff
			.put(36019, 1000)
			// Dormant Anima Core helm/body/legs
			.put(37009, 75).put(37012, 75).put(37015, 75)
			// Anima Core helm of Zaros
			.put(37034, 500)
			// Anima Core Body of Zaros
			.put(37037, 500)
			// Anima Core Legs of Zaros
			.put(37040, 500)
			// Anima Core helm of Zamorak
			.put(37043, 500)
			// Anima Core body of Zamorak
			.put(37046, 500)
			// Anima Core legs of Zamorak
			.put(37049, 500)
			// Anima Core helm of Seren
			.put(37052, 500)
			// Anima Core body of Seren
			.put(37055, 500)
			// Refined Anima Core body of Seren
			.put(37057, 1000)
			// Anima Core legs of Seren
			.put(37058, 500)
			// Anima Core helm of Sliske
			.put(37061, 500)
			// Anima Core body of Sliske
			.put(37064, 500)
			// Anima Core legs of Sliske
			.put(37067, 500)
			// Dragon Rider lance
			.put(37070, 1500)
			// Shadow glaive
			.put(37075, 1500)
			// Wand of the Cywir elders
			.put(37085, 1500)
			// Blade of Nymora
			.put(37090, 1000)
			// Blade of Avaryss
			.put(37095, 1000)
			// Linza's helm
			.put(37433, 100)
			// Linza's cuirass
			.put(37437, 250)
			// Linza's greaves
			.put(37441, 200)
			// Linza's hammer
			.put(37445, 200)
			// Linza's shield
			.put(37449, 200)
			// Seren godbow
			.put(37632, 5000)
			// Staff of Sliske
			.put(37636, 5000)
			// Zaros godsword
			.put(37640, 5000)
			// Nightmare gauntlets
			.put(39248, 500)
			// Wand of the praesul
			.put(39574, 5000)
			// Imperium core
			.put(39579, 5000)
			// Gemstone helm
			.put(39893, 500)
			// Gemstone hauberk
			.put(39895, 1000)
			// Gemstone greaves
			.put(39897, 750)
			// Gemstone boots
			.put(39899, 500)
			// Gemstone gauntlets
			.put(39901, 500)
			// Gloves of passage
			.put(40320, 500)
			// Orb of the Cywir elders
			.put(40600, 1000)
			/* Bandos helmet
			.put(25022, 150)
			// Bandos chestplate
			.put(11724, 300)
			// Bandos tassets
			.put(11726, 250)
			// Bandos boots
			.put(11728, 150)
			// Bandos gloves
			.put(25025, 150)
			// Bandos warshield
			.put(25019, 200)
			// Armadyl helmet
			.put(11718, 100)
			// Armadyl chestplate
			.put(11720, 250)
			// Armadyl plateskirt
			.put(11722, 200)
			// Armadyl boots
			.put(25010, 100)
			// Armadyl buckler
			.put(25013, 150)
			// Armadyl gloves
			.put(25016, 100)
			// Hood of subjugation
			.put(24992, 100)
			// Garb of subjugation
			.put(24995, 250)
			// Gown of subjugation
			.put(24998, 200)
			// Ward of subjugation
			.put(25001, 175)
			// Boots of subjugation
			.put(25004, 100)
			// Gloves of subjugation
			.put(25007, 100)*/
			// Superior tetsu helm
			.put(26322, 150)
			// Superior tetsu body
			.put(26323, 200)
			// Superior tetsu legs
			.put(26324, 175)
			// Superior sea singer's headband
			.put(26334, 150)
			// Superior sea singer's robe top
			.put(26335, 200)
			// Superior sea singer's robe bottoms
			.put(26336, 175)
			// Superior death lotus hood
			.put(26352, 150)
			// Superior death lotus chestplate
			.put(26353, 200)
			// Superior death lotus chaps
			.put(26354, 175)
			// Superior leviathan ring
			.put(30579, 350)
			// Vesta's chainbody
			.put(13887, 175)
			// Vesta's plateskirt
			.put(13893, 150)
			// Vesta's longsword
			.put(13899, 200)
			// Vesta's spear
			.put(13905, 150)
			// Statius's platebody
			.put(13884, 175)
			// Statius's platelegs
			.put(13890, 150)
			// Statius's full helm
			.put(13896, 125)
			// Statius's warhammer
			.put(13902, 200)
			// Morrigan's leather body
			.put(13870, 175)
			// Morrigan's leather chaps
			.put(13873, 150)
			// Morrigan's coif
			.put(13876, 125)
			// Zuriel's robe top
			.put(13858, 175)
			// Zuriel's robe bottom
			.put(13861, 150)
			// Zuriel's hood
			.put(13864, 125)
			// Zuriel's staff
			.put(13867, 150)
			// Green H'ween
			.put(1053, 1500)
			// Blue H'ween
			.put(1055, 1500)
			// Red H'ween
			.put(1057, 1500)
			// Red Partyhat
			.put(1038, 2500)
			// White Partyhat
			.put(1048, 2500)
			// Yellow Partyhat
			.put(1040, 2500)
			// Blue Partyhat
			.put(1042, 2500)
			// Purple Partyhat
			.put(1046, 2500)
			// Green Partyhat
			.put(1044, 2500)
			// Santa Hat
			.put(1050, 2000)
			// Black Santa Hat
			.put(30412, 2500)
			// Barrows Dye
			.put(33294, 1000)
			// Shadow Dye
			.put(33296, 1000)
			// Third-Age Dye
			.put(33298, 1000)
			// Blood Dye
			.put(36274, 1500)
			// Third-age Druidic staff
			.put(19308, 1000)
			// Third-age Druidic Cloak
			.put(19311, 1000)
			// Third-age Druidic Wreath
			.put(19314, 1000)
			// Third-age Druidic Robe Top
			.put(19317, 1500)
			// Third-age Druidic Robe Bottom
			.put(19320, 1250)
			// Third-age Range Top
			.put(10330, 1000)
			// Third-age Range Bottom
			.put(10332, 750)
			// Third-age Range Coif
			.put(10334, 500)
			// Third-age Range Vambs
			.put(10336, 500)
			// Third-age Robe Top
			.put(10338, 1000)
			// Third-age Robe Bottom
			.put(10340, 750)
			// Third-age Mage Hat
			.put(10342, 500)
			// Third-age Amulet
			.put(10344, 500)
			// Third-age Platebody
			.put(10348, 1000)
			// Third-age Platelegs
			.put(10346, 750)
			// Third-age Full helm
			.put(10350, 500)
			// Third-age Kiteshield
			.put(10352, 500)
			// Hailfire Boots
			.put(34984, 1000)
			// Emberkeen Boots
			.put(34978, 1000)
			// Flarefrost Boots
			.put(34981, 1000)
			// Blood Amulet of Fury
			.put(32703, 500)
			/* Saradomin's Whisper
			.put(25028, 150)
			// Saradomin's Hiss
			.put(25031, 150)
			// Saradomin's Murmur
			.put(25034, 150)*/
			// Amulet of Souls
			.put(31875, 1000)
			// Deathtouch Bracelet
			.put(31878, 1000)
			// Reaper Necklace
			.put(31872, 1000)
			// Ring of Death
			.put(31869, 1000)
			// Reaper Hood
			.put(11789, 1250)
			// Scythe
			.put(1419, 750)
			// Abyssal Whip
			//.put(4151, 30)
			// Dark Bow
			//.put(11235, 40)
			// Whip Vine
			//.put(21369, 50)
			// Hexcrest
			//.put(15488, 100)
			// Focus Sight
			//.put(15490, 100)
			// Visage
			.put(11286, 150)
			// Seers' Ring
			.put(6731, 100)
			// Archer's Ring
			.put(6733, 100)
			// Berserker Ring
			.put(6737, 100)
			// Warrior's Ring
			.put(6735, 75)
			// Dragonstone Set
			.put(28539, 75).put(28541, 75).put(28543, 75).put(28545, 75).put(28537, 75)
			// Off-hand shadow glaive
			.put(37080, 1375).put(37081, 1375).put(37083, 1375)
			// end
			.build();

}
