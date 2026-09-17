package com.rs.game.player.actions.summoning.defs;

import com.rs.game.item.Item;

import java.util.HashMap;
import java.util.Map;

public enum SummoningPouches {

	SPIRIT_WOLF_POUCH(12047, 1, 4.8, 6829, 0.1, 1, new Item(12158), new Item(12155), new Item(2859), new Item(12183, 7)),

	DREADFOWL_POUCH(12043, 4, 9.3, 6825, 0.1, 1, new Item(12158), new Item(12155), new Item(2138), new Item(12183, 8)),

	SPIRIT_SPIDER_POUCH(12059, 10, 12.6, 6841, 0.2, 2, new Item(12158), new Item(12155), new Item(6291), new Item(12183, 8)),

	THORNY_SNAIL_POUCH(12019, 13, 12.6, 6806, 0.2, 2, new Item(12158), new Item(12155), new Item(3363), new Item(12183, 9)),

	GRANITE_CRAB_POUCH(12009, 16, 31.6, 6796, 0.2, 2, new Item(12158), new Item(12155), new Item(440), new Item(12183, 7)),

	SPIRIT_MOSQUITO_POUCH(12778, 17, 46.5, 7331, 0.5, 2, new Item(12158), new Item(12155), new Item(6319), new Item(12183, 1)),

	DESERT_WYRM_POUCH(12049, 18, 31.2, 6831, 0.4, 1, new Item(12159), new Item(12155), new Item(1783), new Item(12183, 45)),

	SPIRIT_SCORPION_POUCH(12055, 19, 83.2, 6837, 0.9, 2, new Item(12160), new Item(12155), new Item(3095), new Item(12183, 57)),

	SPIRIT_TZ_KIH_POUCH(12808, 22, 96.8, 7361, 1.1, 3, new Item(12160), new Item(12168), new Item(12155), new Item(12183, 64)),

	ALBINO_RAT_POUCH(12067, 23, 202.4, 6847, 2.3, 1, new Item(12163), new Item(12155), new Item(2134), new Item(12183, 75)),

	SPIRIT_KALPHITE_POUCH(12064, 25, 220, 6994, 2.5, 3, new Item(12163), new Item(12155), new Item(3138), new Item(12183, 51)),

	COMPOST_MOUND_POUCH(12091, 28, 49.8, 6872, 0.6, 6, new Item(12159), new Item(12155), new Item(6032), new Item(12183, 47)),

	GIANT_CHINCHOMPA_POUCH(12800, 29, 255.2, 7353, 2.9, 1, new Item(12163), new Item(12155), new Item(10033), new Item(12183, 84)),

	VAMPIRE_BAT_POUCH(12053, 31, 136, 6835, 1.5, 4, new Item(12160), new Item(12155), new Item(3325), new Item(12183, 81)),

	HONEY_BADGER_POUCH(12065, 32, 140.8, 6845, 1.6, 4, new Item(12160), new Item(12155), new Item(12156), new Item(12183, 84)),

	BEAVER_POUCH(12021, 33, 57.6, 6808, 0.7, 4, new Item(12159), new Item(12155), new Item(1519), new Item(12183, 72)),

	VOID_RAVAGER_POUCH(12818, 34, 59.6, 7370, 0.7, 4, new Item(12159), new Item(12164), new Item(12155), new Item(12183, 74)),

	VOID_SPINNER_POUCH(12781, 34, 59.6, 7333, 0.7, 4, new Item(12163), new Item(12166), new Item(12155), new Item(12183, 74)),

	VOID_TORCHER_POUCH(12798, 34, 59.6, 7351, 0.7, 4, new Item(12163), new Item(12167), new Item(12155), new Item(12183, 74)),

	VOID_SHIFTER_POUCH(12814, 34, 59.6, 7367, 0.7, 4, new Item(12163), new Item(12165), new Item(12155), new Item(12183, 74)),

	BRONZE_MINOTAUR_POUCH(12073, 36, 316.8, 6853, 3.6, 3, new Item(12163), new Item(12155), new Item(2349), new Item(12183, 102)),

	IRON_MINOTAUR_POUCH(12075, 46, 404.8, 6855, 4.6, 9, new Item(12163), new Item(12155), new Item(2351), new Item(12183, 125)),

	STEEL_MINOTAUR_POUCH(12077, 56, 492.8, 6857, 5.6, 9, new Item(12163), new Item(12155), new Item(2353), new Item(12183, 141)),

	MITHRIL_MINOTAUR_POUCH(12079, 66, 580.8, 6859, 6.6, 9, new Item(12163), new Item(12155), new Item(2359), new Item(12183, 152)),

	ADAMANT_MINOTAUR_POUCH(12081, 76, 668.8, 6861, 7.6, 9, new Item(12163), new Item(12155), new Item(2361), new Item(12183, 144)),

	RUNE_MINOTAUR_POUCH(12083, 86, 756.8, 6863, 8.6, 9, new Item(12163), new Item(12155), new Item(2363), new Item(12183, 1)),

	BULL_ANT_POUCH(12087, 40, 52.8, 6867, 0.6, 5, new Item(12158), new Item(12155), new Item(6010), new Item(12183, 11)),

	MACAW_POUCH(12071, 41, 72.4, 6851, 0.8, 5, new Item(12159), new Item(12155), new Item(249), new Item(12183, 78)),

	EVIL_TURNIP_POUCH(12051, 42, 184.8, 6833, 2.1, 5, new Item(12160), new Item(12155), new Item(12153), new Item(12183, 104)),

	SPIRIT_COCKATRICE_POUCH(12095, 43, 75.2, 6875, 0.9, 5, new Item(12159), new Item(12155), new Item(12109), new Item(12183, 88)),

	SPIRIT_GUTHATRICE_POUCH(12097, 43, 75.2, 6877, 0.9, 5, new Item(12159), new Item(12155), new Item(12111), new Item(12183, 88)),

	SPIRIT_SARATRICE_POUCH(12099, 43, 75.2, 6879, 0.9, 5, new Item(12159), new Item(12155), new Item(12113), new Item(12183, 88)),

	SPIRIT_ZAMATRICE_POUCH(12101, 43, 75.2, 6881, 0.9, 5, new Item(12159), new Item(12155), new Item(12115), new Item(12183, 88)),

	SPIRIT_PENGATRICE_POUCH(12103, 43, 75.2, 6883, 0.9, 5, new Item(12159), new Item(12155), new Item(12117), new Item(12183, 88)),

	SPIRIT_CORAXATRICE_POUCH(12105, 43, 75.2, 6885, 0.9, 5, new Item(12159), new Item(12155), new Item(12119), new Item(12183, 88)),

	SPIRIT_VULATRICE(12107, 43, 75.2, 6887, 0.9, 5, new Item(12159), new Item(12155), new Item(12121), new Item(12183, 88)),

	PYRELORD_POUCH(12816, 46, 202.4, 7377, 2.3, 5, new Item(12160), new Item(12155), new Item(590), new Item(12183, 111)),

	MAGPIE_POUCH(12041, 47, 83.2, 6823, 0.9, 5, new Item(12159), new Item(12155), new Item(1635), new Item(12183, 88)),

	BLOATED_LEECH_POUCH(12061, 49, 215.2, 6843, 2.4, 5, new Item(12160), new Item(12155), new Item(2132), new Item(12183, 117)),

	SPIRIT_TERRORBIRD_POUCH(12007, 52, 68.4, 6794, 0.8, 6, new Item(12158), new Item(12155), new Item(9978), new Item(12183, 12)),

	ABYSSAL_PARASITE_POUCH(12035, 54, 94.8, 6818, 1.1, 6, new Item(12159), new Item(12155), new Item(12161), new Item(12183, 106)),

	SPIRIT_JELLY_POUCH(12027, 55, 484, 6992, 5.5, 6, new Item(12163), new Item(12155), new Item(1937), new Item(12183, 151)),

	IBIS_POUCH(12531, 56, 98.8, 6991, 1.1, 6, new Item(12159), new Item(12155), new Item(311), new Item(12183, 109)),

	SPIRIT_KYATT_POUCH(12812, 57, 501.6, 7365, 5.7, 6, new Item(12163), new Item(12155), new Item(10103), new Item(12183, 153)),

	SPIRIT_LARUPIA_POUCH(12784, 57, 501.6, 7337, 5.7, 6, new Item(12163), new Item(12155), new Item(10095), new Item(12183, 155)),

	SPIRIT_GRAAHK_POUCH(12710, 57, 501.6, 7363, 5.7, 6, new Item(12163), new Item(12155), new Item(10099), new Item(12183, 154)),

	KARAMTHULHU_POUCH(12023, 58, 510.4, 6809, 5.8, 6, new Item(12163), new Item(12155), new Item(6667), new Item(12183, 144)),

	SMOKE_DEVIL_POUCH(12085, 61, 268, 6865, 3, 7, new Item(12160), new Item(12155), new Item(9736), new Item(12183, 141)),

	ABYSSAL_LUKRER(12037, 62, 109.6, 6820, 1.9, 9, new Item(12159), new Item(12155), new Item(12161), new Item(12183, 119)),

	SPIRIT_COBRA_POUCH(12015, 63, 276.8, 6802, 3.1, 6, new Item(12160), new Item(12155), new Item(6287), new Item(12183, 116)),

	STRANGER_PLANT_POUCH(12045, 64, 281.6, 6827, 3.2, 6, new Item(12160), new Item(12155), new Item(8431), new Item(12183, 128)),

	BARKER_TOAD_POUCH(12123, 66, 87, 6889, 1, 7, new Item(12158), new Item(12155), new Item(2150), new Item(12183, 11)),

	WAR_TORTOISE_POUCH(12031, 67, 58.6, 6815, 0.7, 7, new Item(12158), new Item(12155), new Item(7939), new Item(12183, 1)),

	BUNYIP_POUCH(12029, 68, 119.2, 6813, 1.4, 7, new Item(12159), new Item(12155), new Item(383), new Item(12183, 110)),

	FRUIT_BAT_POUCH(12033, 69, 121.2, 6817, 1.4, 8, new Item(12159), new Item(12155), new Item(1963), new Item(12183, 130)),

	RAVENOUS_LOCUST_POUCH(12820, 70, 132, 7372, 1.5, 4, new Item(12160), new Item(12155), new Item(1933), new Item(12183, 79)),

	ARCTIC_BEAR_POUCH(12057, 71, 93.2, 6839, 1.1, 8, new Item(12158), new Item(12155), new Item(10117), new Item(12183, 14)),

	PHOENIX_POUCH(14623, 72, 302, 8575, 1.1, 8, new Item(12160), new Item(12155), new Item(14616), new Item(12183, 165)),

	OBSIDIAN_GOLEM_POUCH(12792, 73, 642.4, 7345, 7.3, 8, new Item(12163), new Item(12155), new Item(12168), new Item(12183, 195)),

	GRANITE_LOBSTER_POUCH(12069, 74, 325.6, 6849, 3.7, 8, new Item(12160), new Item(12155), new Item(6983), new Item(12183, 166)),

	PRAYING_MANTIS_POUCH(12011, 75, 329.6, 6798, 3.6, 8, new Item(12160), new Item(12155), new Item(2460), new Item(12183, 168)),

	FORGE_REGENT_BEAST(12782, 76, 134, 7335, 1.5, 9, new Item(12159), new Item(12155), new Item(10020), new Item(12183, 141)),

	TALON_BEAST_POUCH(12794, 77, 1015.2, 7347, 3.8, 9, new Item(12160), new Item(12155), new Item(12162), new Item(12183, 174)),

	GIANT_ENT_POUCH(12013, 78, 136.8, 6800, 1.6, 8, new Item(12159), new Item(5933), new Item(12155), new Item(12183, 124)),

	HYDRA_POUCH(12025, 80, 140.8, 9488, 1.6, 9, new Item(12159), new Item(571), new Item(12183, 128)),

	SPIRIT_DAGANNOTH_POUCH(12017, 83, 364.8, 6804, 4.1, 9, new Item(12160), new Item(6155), new Item(12155), new Item(12183, 1)),

	WOLPERTINGER_POUCH(12089, 92, 404.8, 6869, 4.5, 10, new Item(12160), new Item(2859), new Item(3226), new Item(12155), new Item(12183, 203)),

	PACK_YAK_POUCH(12093, 96, 422.4, 6873, 4.8, 10, new Item(12160), new Item(10818), new Item(12183, 211)),

	FIRE_TITAN_POUCH(12802, 79, 695.2, 7355, 7.9, 9, new Item(12163), new Item(1442), new Item(12155), new Item(12183, 198)),

	MOSS_TITAN_POUCH(12804, 79, 695.2, 7357, 7.9, 9, new Item(12163), new Item(1440), new Item(12155), new Item(12183, 198)),

	ICE_TITAN_POUCH(12806, 79, 695.2, 7359, 7.9, 9, new Item(12163), new Item(1438), new Item(1444), new Item(12155), new Item(12183, 198)),

	LAVA_TITAN_POUCH(12788, 83, 730.4, 7341, 8.3, 9, new Item(12163), new Item(12168), new Item(12155), new Item(12183, 219)),

	SWAMP_TITAN_POUCH(12776, 85, 373.6, 7329, 4.2, 9, new Item(12160), new Item(10149), new Item(12155), new Item(12183, 150)),

	GEYSER_TITAN_POUCH(12786, 89, 783.2, 7339, 8.9, 9, new Item(12163), new Item(1444), new Item(12155), new Item(12183, 222)),

	ABYSSAL_TITAN_POUCH(12796, 93, 163.2, 7349, 1.9, 10, new Item(12159), new Item(12161), new Item(12155), new Item(12183, 113)),

	IRON_TITAN_POUCH(12822, 95, 417.6, 7375, 4.7, 10, new Item(12160), new Item(1115), new Item(12155), new Item(12183, 198)),

	UNICORN_STALLION_POUCH(12039, 88, 154.4, 6822, 1.8, 9, new Item(12159), new Item(237), new Item(12155), new Item(12183, 140)),

	STEEL_TITAN_POUCH(12790, 99, 435.2, 7343, 4.9, 10, new Item(12160), new Item(1119), new Item(12155), new Item(12183, 178)),
	
	NIGHTMARE_MUSPAH_POUCH(31328, 81, 98, 14912, 16, 10, new Item(31334), new Item(31330), new Item(31312, 150), new Item(12155)),
	
	BRAWLER_DEMON_POUCH(34137, 82, 99, 20611, 0, 9, new Item(12160), new Item(33934), new Item(12155), new Item(12183, 136)),
	
	EXECUTIONER_DEMON_POUCH(34139, 82, 99, 20613, 0, 9, new Item(12160), new Item(33934), new Item(12155), new Item(12183, 136)),
	
	DEACON_DEMON_POUCH(34141, 82, 99, 20615, 0, 9, new Item(12160), new Item(33934), new Item(12155), new Item(12183, 136)),
	
	BLOOD_NIHIL_POUCH(31410, 87, 100, 14957, 8.7, 10, new Item(31334), new Item(31418), new Item(31312, 150), new Item(12155)),
	
	SHADOW_NIHIL_POUCH(31412, 87, 101, 14961, 8.7, 10, new Item(31334), new Item(31420), new Item(31312, 150), new Item(12155)),
	
	SMOKE_NIHIL_POUCH(31414, 87, 102, 14953, 8.7, 10, new Item(31334), new Item(31421), new Item(31312, 150), new Item(12155)),
	
	ICE_NIHIL_POUCH(31416, 87, 103, 14965, 8.7, 10, new Item(31334), new Item(31419), new Item(31312, 150), new Item(12155)),
	
	LIGHT_CREATURE_POUCH(32829, 88, 104, 20306, 9, 10, new Item(12163), new Item(32609), new Item(12155), new Item(12183, 204)),
	
	PACK_MAMMOTH(36060, 99, 105, 22005, 10, 20, new Item(12160), new Item(36023), new Item(12155), new Item(12183, 222)),
	
	CUB_BLOODRAGER(17935, 1, 5.0, 11106, 0.5, 1, new Item(18017), new Item(17630)),
	LITTLE_BLOODRAGER(17936, 11, 19.5, 11108, 1, 2, new Item(18017), new Item(17632)),
	NAIVE_BLOODRAGER(17937, 21, 43, 11110, 1.5, 3, new Item(18017), new Item(17634)),
	KEEN_BLOODRAGER(17938, 31, 68.5, 11112, 2, 4, new Item(18018), new Item(17636)),
	BRAVE_BLOODRAGER(17939, 41, 99.5, 11114, 2.5, 5, new Item(18018), new Item(17638)),
	BRAH_BLOODRAGER(17940, 51, 157, 11116, 3, 6, new Item(18018), new Item(17640)),
	NAABE_BLOODRAGER(17941, 61, 220, 11118, 3.5, 7, new Item(18019), new Item(17642)),
	WISE_BLOODRAGER(17942, 71, 325, 11120, 4, 8, new Item(18019), new Item(17644)),
	ADEPT_BLOODRAGER(17943, 81, 517.5, 11122, 4.5, 9, new Item(18020), new Item(17646)),
	SACHEM_BLOODRAGER(17944, 91, 810, 11124, 5, 10, new Item(18020), new Item(17648)),
	CUB_DEATHSLINGER(17985, 2, 5.7, 11206, 0.6, 1, new Item(18017), new Item(17682, 2)),
	LITTLE_DEATHSLINGER(17986, 12, 20.5, 11208, 1.1, 2, new Item(18017), new Item(17684, 2)),
	NAIVE_DEATHSLINGER(1798722, 22, 44.4, 11210, 1.6, 3, new Item(18017), new Item(17686, 2)),
	KEEN_DEATHSLINGER(17988, 32, 70.4, 11212, 2.1, 4, new Item(18018), new Item(17688, 2)),
	BRAVE_DEATHSLINGER(17989, 42, 102, 11214, 2.6, 5, new Item(18018), new Item(17690, 2)),
	BRAH_DEATHSLINGER(17990, 52, 160.5, 11216, 3.1, 6, new Item(18018), new Item(17692, 2)),
	NAABE_DEATHSLINGER(17991, 62, 224.6, 11218, 3.6, 7, new Item(18019), new Item(17694, 2)),
	WISE_DEATHSLINGER(17992, 72, 330.8, 11220, 4.1, 8, new Item(18019), new Item(17696, 2)),
	ADEPT_DEATHSLINGER(17993, 82, 524.6, 11222, 4.6, 9, new Item(18020), new Item(17698, 2)),
	SACHEM_DEATHSLINGER(17994, 92, 818.5, 11224, 5.1, 10, new Item(18020), new Item(17700, 2)),
	CUB_STORMBRINGER(17945, 3, 6.4, 11126, 0.7, 1, new Item(18017), new Item(17448)),
	LITTLE_STORMBRINGER(17946, 13, 21.5, 11128, 1.2, 2, new Item(18017), new Item(17450)),
	NAIVE_STORMBRINGER(17947, 23, 45.8, 11130, 1.7, 3, new Item(18017), new Item(17452)),
	KEEN_STORMBRINGER(17948, 33, 72.3, 11132, 2.2, 4, new Item(18018), new Item(17454)),
	BRAVE_STORMBRINGER(17949, 43, 104.5, 11134, 2.7, 5, new Item(18018), new Item(17456)),
	BRAH_STORMBRINGER(17950, 53, 164, 11136, 3.2, 6, new Item(18018), new Item(17458)),
	NAABE_STORMBRINGER(17951, 63, 229.2, 11138, 3.7, 7, new Item(18019), new Item(17460)),
	WISE_STORMBRINGER(17952, 73, 336.6, 11140, 4.2, 8, new Item(18019), new Item(17462)),
	ADEPT_STORMBRINGER(17953, 83, 531.7, 11142, 4.7, 9, new Item(18020), new Item(17464)),
	SACHEM_STORMBRINGER(17954, 93, 827, 11144, 5.2, 10, new Item(18020), new Item(17466)),
	CUB_HOARDSTALKER(17955, 5, 7.1, 11146, 0.8, 1, new Item(18017), new Item(17424)),
	LITTLE_HOARDSTALKER(17956, 15, 22.5, 11148, 1.3, 2, new Item(18017), new Item(17426)),
	NAIVE_HOARDSTALKER(17957, 25, 47.2, 11150, 1.8, 3, new Item(18017), new Item(17428)),
	KEEN_HOARDSTALKER(17958, 35, 74.2, 11152, 2.3, 4, new Item(18018), new Item(17430)),
	BRAVE_HOARDSTALKER(17959, 45, 107, 11154, 2.8, 5, new Item(18018), new Item(17432)),
	BRAH_HOARDSTALKER(17960, 55, 167.5, 11156, 3.3, 6, new Item(18018), new Item(17434)),
	NAABE_HOARDSTALKER(17961, 65, 233.8, 11158, 3.8, 7, new Item(18019), new Item(17436)),
	WISE_HOARDSTALKER(17962, 75, 342.4, 11160, 4.3, 8, new Item(18019), new Item(17438)),
	ADEPT_HOARDSTALKER(17963, 85, 538.8, 11162, 4.8, 9, new Item(18020), new Item(17440)),
	SACHEM_HOARDSTALKER(17964, 95, 835.5, 11164, 5.3, 10, new Item(18020), new Item(17442)),
	CUB_WORLDBEARER(17975, 7, 7.8, 11186, 0.9, 1, new Item(18017), new Item(17995)),
	LITTLE_WORLDBEARER(17976, 17, 23.5, 11188, 1.4, 2, new Item(18017), new Item(17997)),
	NAIVE_WORLDBEARER(17977, 27, 48.6, 11190, 1.9, 3, new Item(18017), new Item(17999)),
	KEEN_WORLDBEARER(17978, 37, 76.1, 11192, 2.4, 4, new Item(18018), new Item(18001)),
	BRAVE_WORLDBEARER(17979, 47, 109.5, 11194, 2.9, 5, new Item(18018), new Item(18003)),
	BRAH_WORLDBEARER(17980, 57, 171, 11196, 3.4, 6, new Item(18018), new Item(18005)),
	NAABE_WORLDBEARER(17981, 67, 238.4, 11198, 3.9, 7, new Item(18019), new Item(18007)),
	WISE_WORLDBEARER(17982, 77, 348.2, 11200, 4.4, 8, new Item(18019), new Item(18009)),
	ADEPT_WORLDBEARER(17983, 87, 545.9, 11202, 4.9, 9, new Item(18020), new Item(18011)),
	SACHEM_WORLDBEARER(17984, 97, 844, 11204, 5.4, 10, new Item(18020), new Item(18013)),
	CUB_SKINWEAVER(17965, 9, 8.5, 11166, 1, 1, new Item(18017), new Item(18159, 2)),
	LITTLE_SKINWEAVER(17966, 19, 24.5, 11168, 1.5, 2, new Item(18017), new Item(18161, 2)),
	NAIVE_SKINWEAVER(17967, 29, 50, 11170, 2, 3, new Item(18017), new Item(18163, 2)),
	KEEN_SKINWEAVER(17968, 39, 78, 11172, 2.5, 4, new Item(18018), new Item(18165, 2)),
	BRAVE_SKINWEAVER(17969, 49, 112, 11174, 3, 5, new Item(18018), new Item(18167, 2)),
	BRAH_SKINWEAVER(17970, 59, 174.5, 11176, 3.5, 6, new Item(18018), new Item(18169, 2)),
	NAABE_SKINWEAVER(17971, 69, 243, 11178, 4, 7, new Item(18019), new Item(18171, 2)),
	WISE_SKINWEAVER(17972, 79, 354, 11180, 4.5, 8, new Item(18019), new Item(18173, 2)),
	ADEPT_SKINWEAVER(17973, 89, 553, 11182, 5, 9, new Item(18020), new Item(18175, 2)),
	SACHEM_SKINWEAVER(17974, 99, 852.5, 11184, 5.5, 10, new Item(18020), new Item(18177, 2));

	private static final Map<Integer, SummoningPouches> POUCHES = new HashMap<Integer, SummoningPouches>();

	static {
		for (SummoningPouches pouch : SummoningPouches.values()) {
			POUCHES.put(pouch.pouchId, pouch);
		}
	}

	private final int pouchId;
	private final int levelRequired;
	private final double createExperience;
	private final int npcId;
	private final double summonExperience;
	private final int summonCost;
	private final Item[] items;

	SummoningPouches(int pouchId, int levelRequired, double createExperience, int npcId, double summonExperience, int summonCost, Item... items) {
		this.pouchId = pouchId;
		this.levelRequired = levelRequired;
		this.createExperience = createExperience;
		this.npcId = npcId;
		this.summonExperience = summonExperience;
		this.summonCost = summonCost;
		this.items = items;
	}

	public static SummoningPouches get(int pouchId) {
		return POUCHES.get(pouchId);
	}

	public double getCreateExperience() {
		return createExperience;
	}

	public Item[] getItems() {
		return items;
	}

	public int getLevelRequired() {
		return levelRequired;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getPouchId() {
		return pouchId;
	}

	public int getSummonCost() {
		return summonCost;
	}

	public double getSummonExperience() {
		return summonExperience;
	}
}