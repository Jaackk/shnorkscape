package com.rs.game.player.bots.trading;

import com.rs.game.item.Item;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public enum MarketItemCategory {

    FOOD(
            entry(315, 75, 1200), // shrimp
            entry(319, 75, 1200), // anchovies
            entry(333, 75, 1100), // trout
            entry(329, 75, 1000), // salmon
            entry(361, 75, 900), // tuna
            entry(379, 75, 900), // lobster
            entry(373, 60, 800), // swordfish
            entry(7946, 50, 650), // monkfish
            entry(385, 50, 800), // shark
            entry(391, 40, 450), // manta ray
            entry(15266, 35, 420), // cavefish
            entry(15272, 30, 380), // rocktail
            entry(325, 80, 1300), // sardine
            entry(347, 80, 1300), // herring
            entry(351, 75, 1100), // pike
            entry(339, 75, 1100), // cod
            entry(365, 60, 900), // bass
            entry(3144, 50, 700), // cooked karambwan
            entry(397, 35, 420), // sea turtle
            entry(19948, 25, 300), // baron shark
            entry(26313, 20, 250), // rocktail soup
            entry(34729, 20, 240), // great white shark
            entry(1851, 50, 900), // Tenti pineapple
            entry(1859, 50, 900), // Raw ugthanki meat
            entry(1889, 50, 900), // Uncooked cake
            entry(1891, 50, 900), // Cake
            entry(1893, 50, 900), // 2/3 cake
            entry(1895, 50, 900), // Slice of cake
            entry(1897, 50, 900), // Chocolate cake
            entry(1899, 50, 900), // 2/3 chocolate cake
            entry(1973, 50, 900), // Chocolate bar
            entry(1997, 50, 900), // Incomplete stew
            entry(2001, 50, 900), // Uncooked stew
            entry(2003, 50, 900), // Stew
            entry(2136, 50, 900), // Raw bear meat
            entry(2140, 50, 900), // Cooked chicken
            entry(2142, 50, 900), // Cooked meat
            entry(2285, 50, 900), // Incomplete pizza
            entry(2287, 50, 900), // Uncooked pizza
            entry(2289, 50, 900), // Plain pizza
            entry(2291, 50, 900), // Half plain pizza
            entry(2293, 50, 900), // Meat pizza
            entry(2295, 50, 900), // Half meat pizza
            entry(2297, 50, 900), // Anchovy pizza
            entry(2299, 50, 900), // Half anchovy pizza
            entry(2301, 50, 900), // Pineapple pizza
            entry(2303, 50, 900), // Half pineapple pizza
            entry(2317, 50, 900), // Uncooked apple pie
            entry(2319, 50, 900), // Uncooked meat pie
            entry(2321, 50, 900), // Uncooked berry pie
            entry(2323, 50, 900), // Apple pie
            entry(2325, 50, 900), // Redberry pie
            entry(2327, 50, 900), // Meat pie
            entry(2331, 50, 900), // Half a meat pie
            entry(2333, 50, 900), // Half a redberry pie
            entry(2335, 50, 900), // Half an apple pie
            entry(2343, 50, 900), // Cooked oomlie wrap
            entry(2878, 50, 900), // Cooked chompy
            entry(3146, 50, 900), // Poison karambwan
            entry(3228, 50, 900), // Cooked rabbit
            entry(3381, 50, 900), // Cooked slimy eel
            entry(3742, 50, 900), // Red herring
            entry(4016, 50, 900), // Banana stew
            entry(4289, 50, 900), // Raw undead chicken
            entry(4291, 50, 900), // Cooked undead chicken
            entry(4293, 50, 900), // Cooked undead meat
            entry(5089, 50, 900), // Big swordfish
            entry(5091, 50, 900), // Big bass
            entry(5093, 50, 900), // Big shark
            entry(6703, 50, 900), // Potato with butter
            entry(6705, 50, 900), // Potato with cheese
            entry(7086, 50, 900), // Chopped tuna
            entry(7164, 50, 900), // Part mud pie
            entry(7168, 50, 900), // Raw mud pie
            entry(7170, 50, 900), // Mud pie
            entry(7172, 50, 900), // Part garden pie
            entry(7176, 50, 900), // Raw garden pie
            entry(7178, 50, 900), // Garden pie
            entry(7180, 50, 900), // Half a garden pie
            entry(7182, 50, 900), // Part fish pie
            entry(7186, 50, 900), // Raw fish pie
            entry(7188, 50, 900), // Fish pie
            entry(7190, 50, 900), // Half a fish pie
            entry(7192, 50, 900), // Part admiral pie
            entry(7196, 50, 900), // Raw admiral pie
            entry(7198, 50, 900), // Admiral pie
            entry(7200, 50, 900), // Half an admiral pie
            entry(7202, 50, 900), // Part wild pie
            entry(7206, 50, 900), // Raw wild pie
            entry(7208, 50, 900), // Wild pie
            entry(7210, 50, 900), // Half a wild pie
            entry(7212, 50, 900), // Part summer pie
            entry(7216, 50, 900), // Raw summer pie
            entry(7218, 50, 900), // Summer pie
            entry(7220, 50, 900), // Half a summer pie
            entry(7509, 50, 900), // Dwarven rock cake
            entry(7521, 50, 900), // Cooked crab meat
            entry(7528, 50, 900), // Ground cod
            entry(7530, 50, 900), // Cooked fishcake
            entry(7568, 50, 900), // Cooked jubbly
            entry(7580, 50, 900), // Snake over-cooked
            entry(7942, 50, 900), // Fresh monkfish
            entry(8267, 50, 900), // Mounted bass
            entry(8268, 50, 900), // Mounted swordfish
            entry(8269, 50, 900), // Mounted shark
            entry(9475, 50, 900), // Mint cake
            entry(9986, 50, 900), // Raw beast meat
            entry(10816, 50, 900), // Raw yak meat
            entry(10841, 50, 900), // Apricot cream pie
            entry(11328, 50, 900), // Leaping trout
            entry(11330, 50, 900), // Leaping salmon
            entry(12535, 50, 900), // Raw pawya meat
            entry(14540, 50, 900), // Cooked turkey
            entry(14543, 50, 900), // Cooked turkey drumstick
            entry(20111, 50, 900), // 10th anniversary cake
            entry(20179, 50, 900), // Celebration cake
            entry(20429, 50, 900), // Fury shark
            entry(21521, 50, 900), // Tiger shark
            entry(23058, 50, 900), // Raw wolf meat
            entry(23060, 50, 900), // Poorly-cooked bird meat
            entry(23062, 50, 900), // Poorly-cooked beast meat
            entry(24402, 50, 900), // Steak and kidney pie
            entry(25566, 50, 900), // Gut leaping trout
            entry(25567, 50, 900), // Gut leaping salmon
            entry(26315, 50, 900), // Shark soup
            entry(26490, 50, 900), // Mince pie
            entry(28655, 50, 900), // Uncooked pork pie
            entry(28656, 50, 900), // Pork pie
            entry(34746, 25, 350), // Mounted great white shark
            entry(34753, 50, 900), // Sayln the shark
            entry(35199, 50, 900), // Cooked eeligator
            entry(37750, 50, 900), // Uncooked shark soup
            entry(43736, 25, 350)  // Bandosian bantam chicken
    ),

    RAW_FISH(
            entry(317, 75, 1200), // raw shrimp
            entry(321, 75, 1200), // raw anchovies
            entry(335, 75, 1100), // raw trout
            entry(331, 75, 1000), // raw salmon
            entry(359, 75, 900), // raw tuna
            entry(377, 75, 900), // raw lobster
            entry(371, 60, 800), // raw swordfish
            entry(7944, 50, 650), // raw monkfish
            entry(383, 50, 800), // raw shark
            entry(389, 40, 450), // raw manta ray
            entry(15264, 35, 420), // raw cavefish
            entry(15270, 30, 380), // raw rocktail
            entry(327, 80, 1300), // raw sardine
            entry(345, 80, 1300), // raw herring
            entry(349, 75, 1100), // raw pike
            entry(341, 75, 1100), // raw cod
            entry(363, 60, 900), // raw bass
            entry(3142, 50, 700), // raw karambwan
            entry(395, 35, 420), // raw sea turtle
            entry(19947, 25, 300), // raw baron shark
            entry(34727, 20, 240), // raw great white shark
            entry(7529, 50, 900), // Raw fishcake
            entry(10138, 50, 900), // Raw rainbow fish
            entry(13435, 50, 900), // Raw crayfish
            entry(21520, 50, 900), // Raw tiger shark
            entry(35106, 50, 900)  // Raw tropical trout
    ),

    RUNES(
            entry(1436, 600, 6000), // rune essence
            entry(7936, 600, 6000), // pure essence
            entry(556, 600, 6000), // air rune
            entry(558, 600, 6000), // mind rune
            entry(555, 600, 6000), // water rune
            entry(557, 600, 6000), // earth rune
            entry(554, 600, 6000), // fire rune
            entry(559, 400, 5000), // body rune
            entry(564, 250, 3500), // cosmic rune
            entry(562, 250, 3500), // chaos rune
            entry(561, 250, 3500), // nature rune
            entry(563, 250, 3500), // law rune
            entry(560, 250, 3500), // death rune
            entry(565, 200, 3500), // blood rune
            entry(566, 100, 2500), // soul rune
            entry(9075, 100, 2500), // astral rune
            entry(21773, 50, 1200), // armadyl rune
            entry(4694, 150, 3000), // steam rune
            entry(4695, 150, 3000), // mist rune
            entry(4696, 150, 3000), // dust rune
            entry(4697, 150, 3000), // smoke rune
            entry(4698, 150, 3000), // mud rune
            entry(4699, 150, 3000), // lava rune
            entry(37032, 25, 350)  // Zamorakian essence
    ),

    TALISMANS(
            entry(1438, 2, 35), // air talisman
            entry(1448, 2, 35), // mind talisman
            entry(1444, 2, 35), // water talisman
            entry(1440, 2, 35), // earth talisman
            entry(1442, 2, 35), // fire talisman
            entry(1446, 2, 35), // body talisman
            entry(1454, 1, 25), // cosmic talisman
            entry(1452, 1, 25), // chaos talisman
            entry(1462, 1, 25), // nature talisman
            entry(1458, 1, 20), // law talisman
            entry(1456, 1, 20), // death talisman
            entry(1450, 1, 15), // blood talisman
            entry(1460, 1, 15), // soul talisman
            entry(681, 40, 800), // Ancient talisman
            entry(3696, 40, 800), // Hunters' talisman
            entry(4023, 40, 800), // Monkey talisman
            entry(5516, 40, 800)  // Elemental talisman
    ),

    LOGS(
            entry(1511, 100, 1600), // logs
            entry(1521, 100, 1400), // oak logs
            entry(1519, 100, 1200), // willow logs
            entry(6333, 80, 1000), // teak logs
            entry(1517, 80, 1000), // maple logs
            entry(6332, 60, 850), // mahogany logs
            entry(12581, 45, 650), // eucalyptus logs
            entry(1515, 40, 850), // yew logs
            entry(1513, 25, 400), // magic logs
            entry(40285, 40, 700), // acadia logs
            entry(29556, 20, 350), // elder logs
            entry(2862, 40, 800), // Achey tree logs
            entry(3438, 40, 800), // Pyre logs
            entry(3440, 40, 800), // Oak pyre logs
            entry(3442, 40, 800), // Willow pyre logs
            entry(3444, 40, 800), // Maple pyre logs
            entry(3446, 40, 800), // Yew pyre logs
            entry(3448, 40, 800), // Magic pyre logs
            entry(4445, 40, 800), // Red mahogany logs
            entry(6211, 40, 800), // Teak pyre logs
            entry(6213, 40, 800), // Mahogany pyre logs
            entry(7404, 40, 800), // Red logs
            entry(7405, 40, 800), // Green logs
            entry(7406, 40, 800), // Blue logs
            entry(8934, 40, 800), // Scrapey tree logs
            entry(9067, 40, 800), // Dream log
            entry(10328, 40, 800), // White logs
            entry(10329, 40, 800), // Purple logs
            entry(10808, 40, 800), // Arctic pyre logs
            entry(10810, 40, 800), // Arctic pine logs
            entry(10812, 40, 800), // Split log
            entry(12583, 40, 800), // Eucalyptus pyre logs
            entry(13567, 40, 800), // Cursed magic logs
            entry(13756, 40, 800), // Cursed willow logs
            entry(15430, 40, 800), // Yule log
            entry(21600, 40, 800), // Blisterwood logs
            entry(24292, 40, 800), // Dyed logs
            entry(24293, 40, 800), // Cursed logs
            entry(29635, 40, 800), // Elder pyre logs
            entry(40338, 40, 800), // Corrupted magic logs
            entry(47942, 40, 800)  // Jungle logs
    ),

    ORES(
            entry(436, 100, 1600), // copper ore
            entry(438, 100, 1600), // tin ore
            entry(440, 100, 1500), // iron ore
            entry(442, 80, 1000), // silver ore
            entry(453, 100, 1800), // coal
            entry(444, 80, 1000), // gold ore
            entry(447, 50, 700), // mithril ore
            entry(449, 25, 350), // adamantite ore
            entry(451, 10, 180), // runite ore
            entry(44820, 60, 900), // luminite
            entry(44822, 45, 700), // orichalcite ore
            entry(44824, 45, 700), // drakolith
            entry(44826, 35, 550), // necrite ore
            entry(44828, 35, 550), // phasmatite
            entry(44830, 20, 300), // light animica
            entry(44832, 20, 300), // dark animica
            entry(21781, 10, 160), // basiliskbane ore
            entry(21782, 10, 160), // abyssalbane ore
            entry(668, 40, 800), // Blurite ore
            entry(2892, 40, 800), // Elemental ore
            entry(3211, 40, 800), // Limestone
            entry(6971, 40, 800), // Sandstone (1kg)
            entry(6973, 40, 800), // Sandstone (2kg)
            entry(6975, 40, 800), // Sandstone (5kg)
            entry(6977, 40, 800), // Sandstone (10kg)
            entry(9632, 40, 800), // Daeyalt ore
            entry(24769, 40, 800), // Green ore
            entry(32262, 40, 800), // Corrupted ore
            entry(41710, 40, 800), // Granite (2.5kg)
            entry(44775, 40, 800)  // Copper and tin ore
    ),

    BARS(
            entry(2349, 80, 1000), // bronze bar
            entry(2351, 80, 900), // iron bar
            entry(2353, 60, 800), // steel bar
            entry(2355, 40, 500), // silver bar
            entry(2357, 40, 500), // gold bar
            entry(2359, 30, 400), // mithril bar
            entry(2361, 20, 300), // adamant bar
            entry(2363, 10, 180), // rune bar
            entry(44838, 30, 420), // orikalkum bar
            entry(44840, 25, 350), // necronium bar
            entry(44842, 20, 260), // bane bar
            entry(44844, 10, 160), // elder rune bar
            entry(21785, 8, 120), // basiliskbane bar
            entry(21786, 8, 120), // abyssalbane bar
            entry(2893, 40, 800), // Elemental bar
            entry(4007, 40, 800), // Enchanted bar
            entry(9467, 40, 800), // Blurite bar
            entry(9727, 40, 800)  // Primed bar
    ),

    HERBS(
            entry(249, 30, 260), // guam
            entry(251, 30, 240), // marrentill
            entry(253, 30, 220), // tarromin
            entry(255, 25, 200), // harralander
            entry(257, 20, 180), // ranarr
            entry(2998, 20, 160), // toadflax
            entry(259, 20, 160), // irit
            entry(261, 18, 150), // avantoe
            entry(263, 15, 140), // kwuarm
            entry(3000, 15, 120), // snapdragon
            entry(265, 15, 120), // cadantine
            entry(2481, 12, 110), // lantadyme
            entry(267, 10, 100), // dwarf weed
            entry(269, 10, 80), // torstol
            entry(199, 30, 260), // grimy guam
            entry(201, 30, 240), // grimy marrentill
            entry(203, 30, 220), // grimy tarromin
            entry(205, 25, 200), // grimy harralander
            entry(207, 20, 180), // grimy ranarr
            entry(3049, 20, 160), // grimy toadflax
            entry(209, 20, 160), // grimy irit
            entry(211, 18, 150), // grimy avantoe
            entry(213, 15, 140), // grimy kwuarm
            entry(3051, 15, 120), // grimy snapdragon
            entry(215, 15, 120), // grimy cadantine
            entry(2485, 12, 110), // grimy lantadyme
            entry(217, 10, 100), // grimy dwarf weed
            entry(219, 10, 80), // grimy torstol
            entry(12172, 12, 110), // clean spirit weed
            entry(12174, 12, 110), // grimy spirit weed
            entry(14854, 12, 110), // clean wergali
            entry(14836, 12, 110), // grimy wergali
            entry(21624, 8, 70), // clean fellstalk
            entry(21626, 8, 70), // grimy fellstalk
            entry(19989, 20, 160), // clean erzille
            entry(19984, 20, 160), // grimy erzille
            entry(19990, 18, 150), // clean argway
            entry(19985, 18, 150), // grimy argway
            entry(19991, 15, 130), // clean ugune
            entry(19986, 15, 130), // grimy ugune
            entry(19992, 12, 110), // clean shengo
            entry(19987, 12, 110), // grimy shengo
            entry(19993, 10, 90), // clean samaden
            entry(19988, 10, 90), // grimy samaden
            entry(1525, 20, 350), // Grimy snake weed
            entry(1526, 20, 350), // Clean snake weed
            entry(1527, 20, 350), // Grimy ardrigal
            entry(1528, 20, 350), // Clean ardrigal
            entry(1529, 20, 350), // Grimy sito foil
            entry(1530, 20, 350), // Clean sito foil
            entry(1531, 20, 350), // Grimy volencia moss
            entry(1532, 20, 350), // Clean volencia moss
            entry(1533, 20, 350), // Grimy rogue's purse
            entry(1534, 20, 350), // Clean rogue's purse
            entry(6677, 20, 350), // Guam-in-a-box
            entry(6678, 20, 350), // Guam-in-a-box?
            entry(10142, 20, 350), // Guam tar
            entry(10143, 20, 350), // Marrentill tar
            entry(10144, 20, 350), // Tarromin tar
            entry(10145, 20, 350)  // Harralander tar
    ),

    POTIONS(
            entry(2428, 6, 90), // attack potion(4)
            entry(113, 6, 90), // strength potion(4)
            entry(2432, 6, 90), // defence potion(4)
            entry(2430, 6, 90), // restore potion(4)
            entry(3008, 6, 90), // energy potion(4)
            entry(3032, 6, 80), // agility potion(4)
            entry(2434, 6, 90), // prayer potion(4)
            entry(2436, 5, 90), // super attack(4)
            entry(2440, 5, 90), // super strength(4)
            entry(2442, 5, 90), // super defence(4)
            entry(2444, 5, 80), // ranging potion(4)
            entry(3040, 5, 80), // magic potion(4)
            entry(3024, 5, 90), // super restore(4)
            entry(6685, 4, 80), // saradomin brew(4)
            entry(2446, 4, 70), // antipoison(4)
            entry(2448, 4, 70), // super antipoison(4)
            entry(2452, 4, 70), // antifire potion(4)
            entry(3016, 4, 70), // super energy(4)
            entry(12140, 4, 70), // summoning potion(4)
            entry(15300, 3, 60), // adrenaline potion(4)
            entry(15304, 3, 60), // super antifire(4)
            entry(15328, 3, 60), // super prayer(4)
            entry(15332, 2, 45), // overload(4)
            entry(21630, 3, 60), // prayer renewal(4)
            entry(28191, 3, 50), // super saradomin brew(4)
            entry(39212, 2, 40), // super adrenaline potion(4)
            entry(48213, 2, 40), // extended super antifire(4)
            entry(20003, 3, 60), // juju mining potion(4)
            entry(20007, 3, 60), // juju cooking potion(4)
            entry(20011, 3, 60), // juju farming potion(4)
            entry(20015, 3, 60), // juju woodcutting potion(4)
            entry(20019, 3, 60), // juju fishing potion(4)
            entry(20023, 3, 60), // juju hunter potion(4)
            entry(32759, 2, 35), // perfect juju woodcutting potion(4)
            entry(32767, 2, 35), // perfect juju farming potion(4)
            entry(32775, 2, 35), // perfect juju mining potion(4)
            entry(32783, 2, 35), // perfect juju smithing potion(4)
            entry(32791, 2, 35), // perfect juju agility potion(4)
            entry(32799, 2, 35), // perfect juju prayer potion(4)
            entry(32807, 2, 35), // perfect juju herblore potion(4)
            entry(35741, 2, 35), // perfect juju fishing potion(4)
            entry(23351, 2, 35), // saradomin brew flask(6)
            entry(23399, 2, 35), // super restore flask(6)
            entry(23483, 2, 35), // adrenaline flask(6)
            entry(23489, 2, 35), // super antifire flask(6)
            entry(23531, 1, 25), // overload flask(6)
            entry(23609, 2, 35), // prayer renewal flask(6)
            entry(25509, 2, 35), // weapon poison flask(6)
            entry(25521, 2, 35), // weapon poison+ flask(6)
            entry(25533, 2, 35), // weapon poison++ flask(6)
            entry(32859, 1, 25), // perfect juju woodcutting flask(6)
            entry(32871, 1, 25), // perfect juju farming flask(6)
            entry(32883, 1, 25), // perfect juju mining flask(6)
            entry(32895, 1, 25), // perfect juju smithing flask(6)
            entry(32907, 1, 25), // perfect juju agility flask(6)
            entry(32919, 1, 25), // perfect juju prayer flask(6)
            entry(32931, 1, 25), // perfect juju herblore flask(6)
            entry(35754, 1, 25), // perfect juju fishing flask(6)
            entry(91, 20, 220), // Guam potion (unf)
            entry(93, 20, 220), // Marrentill potion (unf)
            entry(95, 20, 220), // Tarromin potion (unf)
            entry(97, 20, 220), // Harralander potion (unf)
            entry(99, 20, 220), // Ranarr potion (unf)
            entry(101, 20, 220), // Irit potion (unf)
            entry(103, 20, 220), // Avantoe potion (unf)
            entry(105, 20, 220), // Kwuarm potion (unf)
            entry(107, 20, 220), // Cadantine potion (unf)
            entry(109, 20, 220), // Dwarf weed potion (unf)
            entry(111, 8, 80), // Torstol potion (unf)
            entry(2438, 20, 220), // Fishing potion (4)
            entry(2450, 20, 220), // Zamorak brew (4)
            entry(2483, 20, 220), // Lantadyme potion (unf)
            entry(3002, 20, 220), // Toadflax potion (unf)
            entry(3004, 20, 220), // Snapdragon potion (unf)
            entry(6470, 20, 220), // Compost potion (4)
            entry(9739, 20, 220), // Combat potion (4)
            entry(9998, 20, 220), // Hunter potion (4)
            entry(11809, 20, 220), // Goblin potion (4)
            entry(12181, 20, 220), // Spirit weed potion (unf)
            entry(14249, 20, 220), // Ranging potion (4)
            entry(14269, 20, 220), // Magic potion (4)
            entry(14838, 20, 220), // Crafting potion (4)
            entry(14846, 20, 220), // Fletching potion (4)
            entry(14856, 20, 220), // Wergali potion (unf)
            entry(18715, 20, 220), // Cw super attack potion (4)
            entry(18719, 20, 220), // Cw super strength potion (4)
            entry(18723, 20, 220), // Cw super defence potion (4)
            entry(18727, 20, 220), // Cw super energy potion (4)
            entry(18731, 20, 220), // Cw super ranging potion (4)
            entry(18735, 20, 220), // Cw super magic potion (4)
            entry(19967, 20, 220), // Juju teleport spiritbag
            entry(19994, 20, 220), // Juju vial of water
            entry(20027, 20, 220), // Scentless potion (4)
            entry(23131, 20, 220), // Juju mining flask (6)
            entry(23137, 20, 220), // Juju cooking flask (6)
            entry(23143, 20, 220), // Juju farming flask (6)
            entry(23149, 20, 220), // Juju woodcutting flask (6)
            entry(23155, 20, 220), // Juju fishing flask (6)
            entry(23161, 20, 220), // Juju hunter flask (6)
            entry(23167, 20, 220), // Scentless flask (6)
            entry(23173, 20, 220), // Saradomin's blessing flask (6)
            entry(23179, 20, 220), // Guthix's gift flask (6)
            entry(23185, 20, 220), // Zamorak's favour flask (6)
            entry(23195, 20, 220), // Attack flask (6)
            entry(23207, 20, 220), // Strength flask (6)
            entry(23219, 20, 220), // Restore flask (6)
            entry(23231, 20, 220), // Defence flask (6)
            entry(23243, 20, 220), // Prayer flask (6)
            entry(23255, 20, 220), // Super attack flask (6)
            entry(23267, 20, 220), // Fishing flask (6)
            entry(23279, 20, 220), // Super strength flask (6)
            entry(23291, 20, 220), // Super defence flask (6)
            entry(23303, 20, 220), // Super ranging flask (6)
            entry(23315, 20, 220), // Antipoison flask (6)
            entry(23327, 20, 220), // Super antipoison flask (6)
            entry(23339, 20, 220), // Zamorak brew flask (6)
            entry(23363, 20, 220), // Antifire flask (6)
            entry(23375, 20, 220), // Energy flask (6)
            entry(23387, 20, 220), // Super energy flask (6)
            entry(23411, 20, 220), // Agility flask (6)
            entry(23423, 20, 220), // Super magic flask (6)
            entry(23435, 20, 220), // Hunter flask (6)
            entry(23447, 20, 220), // Combat flask (6)
            entry(23459, 20, 220), // Crafting flask (6)
            entry(23471, 20, 220), // Fletching flask (6)
            entry(23525, 20, 220), // Super prayer flask (6)
            entry(23537, 20, 220), // Relicym's balm flask (6)
            entry(23549, 20, 220), // Serum 207 flask (6)
            entry(23555, 20, 220), // Guthix balance flask (6)
            entry(23567, 20, 220), // Sanfew serum flask (6)
            entry(23579, 20, 220), // Antipoison+ flask (6)
            entry(23591, 20, 220), // Antipoison++ flask (6)
            entry(23603, 20, 220), // Serum 208 flask (6)
            entry(23621, 20, 220), // Summoning flask (6)
            entry(23633, 20, 220), // Magic essence flask (6)
            entry(25485, 20, 220), // Weapon poison (4)
            entry(27520, 20, 220), // Ranging flask (6)
            entry(27532, 20, 220), // Magic flask (6)
            entry(28199, 20, 220), // Super Zamorak brew (4)
            entry(28215, 20, 220), // Super Zamorak brew flask (6)
            entry(28227, 8, 80), // Super Saradomin brew flask (6)
            entry(28239, 20, 220), // Super Guthix brew flask (6)
            entry(29448, 20, 220), // Guthix rest flask (6)
            entry(30770, 20, 220), // Ultra-growth potion (4)
            entry(48235, 8, 80)  // Aggroverload (4)
    ),

    SUPPLIES(
            entry(2434, 5, 90), // prayer potion(4)
            entry(3024, 5, 90), // super restore(4)
            entry(6685, 4, 80), // saradomin brew(4)
            entry(2440, 4, 80), // super strength(4)
            entry(2442, 4, 80), // super defence(4)
            entry(2436, 4, 80), // super attack(4)
            entry(2444, 4, 70), // ranging potion(4)
            entry(3040, 4, 70), // magic potion(4)
            entry(9244, 50, 700), // dragon bolts(e)
            entry(9243, 50, 700), // diamond bolts(e)
            entry(9144, 150, 2500), // runite bolts
            entry(892, 250, 2500), // rune arrows
            entry(385, 50, 800), // shark
            entry(15272, 30, 380), // rocktail
            entry(15300, 3, 60), // adrenaline potion(4)
            entry(15332, 2, 45), // overload(4)
            entry(21630, 3, 60), // prayer renewal(4)
            entry(23399, 2, 35), // super restore flask(6)
            entry(23531, 1, 25), // overload flask(6)
            entry(34729, 20, 240), // great white shark
            entry(26313, 20, 250), // rocktail soup
            entry(28465, 60, 900), // ascension bolts
            entry(11212, 80, 1200), // dragon arrows
            entry(11230, 50, 800), // dragon darts
            entry(9976, 20, 350), // chinchompa
            entry(9977, 20, 350), // red chinchompa
            entry(985, 1, 10), // tooth half of a key
            entry(987, 1, 10), // loop half of a key
            entry(989, 1, 8), // crystal key
            entry(30915, 40, 700), // silverhawk feathers
            entry(32092, 25, 350), // vis wax
            entry(40303, 20, 300), // feather of ma'at
            entry(26283, 1, 8), // log-splitting scrimshaw
            entry(26285, 1, 6), // superior log-splitting scrimshaw
            entry(26286, 1, 8), // rock-crushing scrimshaw
            entry(26288, 1, 6), // superior rock-crushing scrimshaw
            entry(26292, 1, 8), // gem-finding scrimshaw
            entry(26294, 1, 6), // superior gem-finding scrimshaw
            entry(26295, 1, 6), // scrimshaw of vampyrism
            entry(26297, 1, 4), // superior scrimshaw of vampyrism
            entry(26298, 1, 6), // scrimshaw of attack
            entry(26300, 1, 4), // superior scrimshaw of attack
            entry(26301, 1, 6), // scrimshaw of the elements
            entry(26303, 1, 4), // superior scrimshaw of the elements
            entry(26304, 1, 6), // scrimshaw of magic
            entry(26306, 1, 4), // superior scrimshaw of magic
            entry(26307, 1, 6), // scrimshaw of cruelty
            entry(26309, 1, 4), // superior scrimshaw of cruelty
            entry(26310, 1, 6), // scrimshaw of ranging
            entry(26312, 1, 4), // superior scrimshaw of ranging
            entry(38828, 1, 6), // energy-gathering scrimshaw
            entry(38831, 1, 4), // superior energy-gathering scrimshaw
            entry(47699, 10, 160), // guam incense sticks
            entry(47700, 10, 160), // tarromin incense sticks
            entry(47701, 10, 160), // marrentill incense sticks
            entry(47702, 10, 160), // harralander incense sticks
            entry(47703, 10, 160), // ranarr incense sticks
            entry(47704, 10, 160), // toadflax incense sticks
            entry(47706, 10, 160), // irit incense sticks
            entry(47708, 10, 160), // avantoe incense sticks
            entry(47709, 10, 160), // kwuarm incense sticks
            entry(47711, 10, 160), // snapdragon incense sticks
            entry(47712, 10, 160), // cadantine incense sticks
            entry(47713, 10, 160), // lantadyme incense sticks
            entry(47714, 10, 160), // dwarf weed incense sticks
            entry(47715, 10, 160), // torstol incense sticks
            entry(47716, 10, 160), // fellstalk incense sticks
            entry(227, 40, 800), // Vial of water
            entry(869, 40, 800), // Black knife
            entry(946, 40, 800), // Knife
            entry(3420, 40, 800), // Limestone brick
            entry(7121, 40, 800), // Repair plank
            entry(7447, 40, 800), // Kitchen knife
            entry(8778, 40, 800), // Oak plank
            entry(8780, 40, 800), // Teak plank
            entry(8782, 40, 800), // Mahogany plank
            entry(12652, 40, 800), // Bucket of coal
            entry(13238, 40, 800), // Treated oak plank
            entry(14103, 40, 800), // Volatile clay fletching knife
            entry(15292, 40, 800), // Short plank
            entry(15293, 40, 800), // Long plank
            entry(15294, 40, 800), // Diagonal-cut plank
            entry(15295, 40, 800), // Tooth plank
            entry(15296, 40, 800), // Groove plank
            entry(15297, 40, 800), // Curved plank
            entry(15363, 40, 800), // Vial of water pack
            entry(25896, 40, 800), // Off-hand iron knife
            entry(25897, 40, 800), // Off-hand bronze knife
            entry(25898, 40, 800), // Off-hand steel knife
            entry(25899, 40, 800), // Off-hand mithril knife
            entry(25900, 40, 800), // Off-hand adamant knife
            entry(25901, 40, 800), // Off-hand rune knife
            entry(25902, 40, 800), // Off-hand black knife
            entry(31375, 40, 800), // Dragon knife
            entry(31376, 40, 800)  // Off-hand dragon knife
    ),

    SKILLING_INPUTS(
            entry(1436, 250, 3000), // rune essence
            entry(7936, 250, 3000), // pure essence
            entry(1779, 150, 1800), // flax
            entry(1777, 150, 1800), // bow string
            entry(2357, 50, 400), // gold bar
            entry(2353, 50, 500), // steel bar
            entry(2359, 25, 300), // mithril bar
            entry(2361, 20, 250), // adamant bar
            entry(1623, 20, 160), // uncut sapphire
            entry(1621, 20, 160), // uncut emerald
            entry(1619, 15, 120), // uncut ruby
            entry(1617, 10, 100), // uncut diamond
            entry(314, 500, 8000), // feather
            entry(52, 500, 8000), // arrow shaft
            entry(53, 500, 8000), // headless arrow
            entry(39, 250, 3500), // bronze arrowheads
            entry(40, 250, 3500), // iron arrowheads
            entry(41, 250, 3500), // steel arrowheads
            entry(42, 200, 3000), // mithril arrowheads
            entry(43, 150, 2500), // adamant arrowheads
            entry(44, 100, 1800), // rune arrowheads
            entry(819, 250, 3500), // bronze dart tip
            entry(820, 250, 3500), // iron dart tip
            entry(821, 250, 3500), // steel dart tip
            entry(822, 200, 3000), // mithril dart tip
            entry(823, 150, 2500), // adamant dart tip
            entry(824, 100, 1800), // rune dart tip
            entry(11232, 30, 500), // dragon dart tip
            entry(45, 100, 1600), // opal bolt tips
            entry(46, 100, 1600), // pearl bolt tips
            entry(9189, 80, 1200), // sapphire bolt tips
            entry(9190, 80, 1200), // emerald bolt tips
            entry(9191, 60, 900), // ruby bolt tips
            entry(9192, 50, 800), // diamond bolt tips
            entry(9193, 30, 500), // dragon bolt tips
            entry(9194, 20, 300), // onyx bolt tips
            entry(13278, 200, 2500), // broad arrowheads
            entry(13279, 200, 2500), // unfinished broad bolts
            entry(44799, 100, 1600), // copper stone spirit
            entry(44800, 100, 1600), // tin stone spirit
            entry(44801, 100, 1500), // iron stone spirit
            entry(44802, 80, 1000), // silver stone spirit
            entry(44803, 80, 1000), // gold stone spirit
            entry(44804, 100, 1800), // coal stone spirit
            entry(44805, 50, 700), // mithril stone spirit
            entry(44806, 60, 900), // luminite stone spirit
            entry(44807, 25, 350), // adamantite stone spirit
            entry(44808, 10, 180), // runite stone spirit
            entry(44809, 45, 700), // orichalcite stone spirit
            entry(44810, 45, 700), // drakolith stone spirit
            entry(44811, 35, 550), // necrite stone spirit
            entry(44812, 35, 550), // phasmatite stone spirit
            entry(44813, 20, 300), // banite stone spirit
            entry(44814, 20, 300), // light animica stone spirit
            entry(44815, 20, 300), // dark animica stone spirit
            entry(6693, 40, 800), // Crushed nest
            entry(9442, 40, 800), // Oak stock
            entry(9444, 40, 800), // Willow stock
            entry(9448, 40, 800), // Maple stock
            entry(9452, 40, 800), // Yew stock
            entry(39450, 2, 35), // Armadyl's feather
            entry(42240, 40, 800)  // Great white shark bait
    ),

    SHORTBOWS(
            entry(841, 2, 30), // shortbow
            entry(843, 2, 30), // oak shortbow
            entry(849, 2, 25), // willow shortbow
            entry(853, 2, 25), // maple shortbow
            entry(857, 1, 20), // yew shortbow
            entry(861, 1, 15), // magic shortbow
            entry(839, 2, 30), // shieldbow
            entry(845, 2, 30), // oak shieldbow
            entry(847, 2, 25), // willow shieldbow
            entry(851, 2, 25), // maple shieldbow
            entry(855, 1, 20), // yew shieldbow
            entry(859, 1, 15), // magic shieldbow
            entry(1777, 150, 1800), // bow string
            entry(1779, 150, 1800), // flax
            entry(50, 8, 100), // shortbow(u)
            entry(48, 8, 100), // shieldbow(u)
            entry(54, 8, 100), // oak shortbow(u)
            entry(56, 8, 100), // oak shieldbow(u)
            entry(60, 8, 90), // willow shortbow(u)
            entry(58, 8, 90), // willow shieldbow(u)
            entry(64, 6, 75), // maple shortbow(u)
            entry(62, 6, 75), // maple shieldbow(u)
            entry(68, 4, 60), // yew shortbow(u)
            entry(66, 4, 60), // yew shieldbow(u)
            entry(72, 3, 45), // magic shortbow(u)
            entry(70, 3, 45), // magic shieldbow(u)
            entry(29736, 2, 30), // elder shortbow(u)
            entry(29734, 2, 30)  // elder shieldbow(u)
    ),

    GEMS(
            entry(1625, 20, 180), // uncut opal
            entry(1609, 20, 180), // opal
            entry(1627, 20, 180), // uncut jade
            entry(1611, 20, 180), // jade
            entry(1629, 20, 160), // uncut red topaz
            entry(1613, 20, 160), // red topaz
            entry(1623, 20, 160), // uncut sapphire
            entry(1607, 20, 160), // sapphire
            entry(1621, 20, 150), // uncut emerald
            entry(1605, 20, 150), // emerald
            entry(1619, 15, 120), // uncut ruby
            entry(1603, 15, 120), // ruby
            entry(1617, 10, 100), // uncut diamond
            entry(1601, 10, 100), // diamond
            entry(1631, 4, 40), // uncut dragonstone
            entry(1615, 4, 40), // dragonstone
            entry(6571, 1, 8), // uncut onyx
            entry(6573, 1, 8), // onyx
            entry(31853, 1, 6), // uncut hydrix
            entry(31855, 1, 6), // hydrix
            entry(31867, 10, 160), // hydrix bolt tips
            entry(1633, 20, 350), // Crushed gem
            entry(4155, 20, 350), // Enchanted gem
            entry(4670, 20, 350), // Blood diamond
            entry(4671, 20, 350), // Ice diamond
            entry(4672, 20, 350), // Smoke diamond
            entry(4673, 20, 350), // Shadow diamond
            entry(8016, 20, 350), // Enchant sapphire
            entry(8017, 20, 350), // Enchant emerald
            entry(8018, 20, 350), // Enchant ruby
            entry(8019, 20, 350), // Enchant diamond
            entry(8021, 5, 100), // Enchant onyx
            entry(15477, 20, 350), // Enchanted emerald
            entry(21345, 20, 350), // Uncut lapis lazuli
            entry(21346, 20, 350)  // Lapis lazuli gem
    ),

    CRAFTING_INPUTS(
            entry(1759, 80, 1400), // ball of wool
            entry(1739, 80, 1200), // cowhide
            entry(1741, 80, 1200), // leather
            entry(1743, 60, 900), // hard leather
            entry(1745, 40, 700), // green dragon leather
            entry(2505, 35, 600), // blue dragon leather
            entry(2507, 25, 450), // red dragon leather
            entry(2509, 20, 350), // black dragon leather
            entry(24374, 8, 120), // royal dragon leather
            entry(6289, 30, 400), // snakeskin
            entry(1733, 8, 40), // needle
            entry(1734, 100, 1600), // thread
            entry(1755, 8, 50), // chisel
            entry(1775, 100, 1200), // molten glass
            entry(1781, 80, 1000), // soda ash
            entry(1783, 80, 1000), // bucket of sand
            entry(401, 80, 1000), // seaweed
            entry(434, 80, 1000), // clay
            entry(1761, 80, 1000), // soft clay
            entry(2355, 50, 500), // silver bar
            entry(2357, 50, 500), // gold bar
            entry(5525, 20, 250), // tiara
            entry(1747, 40, 700), // black dragonhide
            entry(1749, 40, 700), // red dragonhide
            entry(1751, 50, 800), // blue dragonhide
            entry(1753, 60, 900), // green dragonhide
            entry(24372, 8, 120), // royal dragonhide
            entry(567, 60, 900), // unpowered orb
            entry(569, 40, 600), // fire orb
            entry(571, 40, 600), // water orb
            entry(573, 40, 600), // air orb
            entry(575, 40, 600), // earth orb
            entry(1391, 4, 50), // battlestaff
            entry(1393, 3, 40), // fire battlestaff
            entry(1395, 3, 40), // water battlestaff
            entry(1397, 3, 40), // air battlestaff
            entry(1399, 3, 40), // earth battlestaff
            entry(3053, 1, 20), // lava battlestaff
            entry(6562, 1, 20), // mud battlestaff
            entry(11736, 1, 20), // steam battlestaff
            entry(21777, 1, 10), // armadyl battlestaff
            entry(23191, 20, 250), // potion flask
            entry(23193, 20, 250), // robust glass
            entry(23194, 20, 250), // red sandstone
            entry(32847, 15, 180), // crystal-flecked sandstone
            entry(47685, 20, 300), // wooden incense sticks
            entry(47686, 20, 300), // oak incense sticks
            entry(47687, 20, 300), // willow incense sticks
            entry(47688, 20, 300), // maple incense sticks
            entry(47689, 20, 300), // acadia incense sticks
            entry(47690, 15, 220), // yew incense sticks
            entry(47691, 10, 160), // magic incense sticks
            entry(1737, 40, 800), // Wool
            entry(3694, 40, 800), // Golden wool
            entry(3702, 40, 800), // Custom bowstring
            entry(5518, 40, 800), // Scrying orb
            entry(6155, 40, 800), // Dagannoth hide
            entry(6169, 40, 800), // Circular hide
            entry(6171, 40, 800), // Flattened hide
            entry(6173, 40, 800), // Stretched hide
            entry(6287, 40, 800), // Snake hide
            entry(7370, 40, 800), // Green dragonhide body (g)
            entry(7372, 40, 800), // Green dragonhide body (t)
            entry(7374, 40, 800), // Blue dragonhide body (g)
            entry(7376, 40, 800), // Blue dragonhide body (t)
            entry(7378, 40, 800), // Dragonhide chaps (g)
            entry(7380, 40, 800), // Dragonhide chaps (t)
            entry(7382, 40, 800), // Blue dragonhide chaps (g)
            entry(7384, 40, 800), // Blue dragonhide chaps (t)
            entry(7532, 40, 800), // Mudskipper hide
            entry(9080, 40, 800), // Suqah hide
            entry(9081, 40, 800), // Suqah leather
            entry(9438, 40, 800), // Crossbow string
            entry(10790, 40, 800), // Zamorak dragonhide
            entry(10792, 40, 800), // Saradomin dragonhide
            entry(10794, 40, 800), // Guthix dragonhide
            entry(10820, 40, 800), // Cured yak-hide
            entry(10822, 40, 800), // Yak-hide armour (top)
            entry(10824, 40, 800), // Yak-hide armour (legs)
            entry(10973, 40, 800), // Light orb
            entry(10980, 40, 800), // Empty light orb
            entry(11912, 40, 800), // Green dragonhide trimmed set
            entry(11914, 40, 800), // Green dragonhide gold-trimmed set
            entry(11916, 40, 800), // Blue dragonhide trimmed set
            entry(11918, 40, 800), // Blue dragonhide gold-trimmed set
            entry(11920, 40, 800), // Guthix dragonhide blessed set
            entry(11922, 40, 800), // Saradomin dragonhide blessed set
            entry(11924, 40, 800), // Zamorak dragonhide blessed set
            entry(12937, 40, 800), // Green dragonhide coif 80
            entry(12938, 40, 800), // Green dragonhide coif 60
            entry(12939, 40, 800), // Green dragonhide coif 40
            entry(12940, 40, 800), // Green dragonhide coif 20
            entry(12944, 40, 800), // Blue dragonhide coif 80
            entry(12945, 40, 800), // Blue dragonhide coif 60
            entry(12946, 40, 800), // Blue dragonhide coif 40
            entry(12947, 40, 800), // Blue dragonhide coif 20
            entry(12951, 40, 800), // Red dragonhide coif 80
            entry(12952, 40, 800), // Red dragonhide coif 60
            entry(12953, 40, 800), // Red dragonhide coif 40
            entry(12954, 40, 800), // Red dragonhide coif 20
            entry(12958, 40, 800), // Black dragonhide coif 80
            entry(12959, 40, 800), // Black dragonhide coif 60
            entry(12960, 40, 800), // Black dragonhide coif 40
            entry(12961, 40, 800), // Black dragonhide coif 20
            entry(15193, 40, 800), // Unicorn hide
            entry(15194, 40, 800), // Scrubbed unicorn hide
            entry(15195, 40, 800), // Tanned unicorn hide
            entry(15196, 40, 800), // Black unicorn hide
            entry(15197, 40, 800), // Scrubbed black unicorn hide
            entry(15198, 40, 800), // Tanned black unicorn hide
            entry(15415, 40, 800), // Black wool
            entry(15416, 40, 800), // Ball of black wool
            entry(19517, 40, 800), // Ancient dragonhide
            entry(19518, 40, 800), // Bandos dragonhide
            entry(19519, 40, 800), // Armadyl dragonhide
            entry(19582, 40, 800), // Bandos dragonhide blessed set
            entry(19584, 40, 800), // Ancient dragonhide blessed set
            entry(19586, 40, 800), // Armadyl dragonhide blessed set
            entry(32216, 40, 800), // Crystal orb
            entry(32663, 40, 800)  // Attuned crystal orb
    ),

    JEWELRY(
            entry(1635, 3, 45), // gold ring
            entry(1637, 3, 35), // sapphire ring
            entry(1639, 3, 35), // emerald ring
            entry(1641, 2, 25), // ruby ring
            entry(1643, 2, 20), // diamond ring
            entry(1645, 1, 12), // dragonstone ring
            entry(6575, 1, 6), // onyx ring
            entry(1654, 3, 45), // gold necklace
            entry(1656, 3, 35), // sapphire necklace
            entry(1658, 3, 35), // emerald necklace
            entry(1660, 2, 25), // ruby necklace
            entry(1662, 2, 20), // diamond necklace
            entry(1664, 1, 12), // dragon necklace
            entry(6577, 1, 6), // onyx necklace
            entry(11069, 3, 45), // gold bracelet
            entry(11072, 3, 35), // sapphire bracelet
            entry(11076, 3, 35), // emerald bracelet
            entry(11085, 2, 25), // ruby bracelet
            entry(11092, 2, 20), // diamond bracelet
            entry(11115, 1, 12), // dragon bracelet
            entry(11130, 1, 6), // onyx bracelet
            entry(1692, 3, 45), // gold amulet
            entry(1694, 3, 35), // sapphire amulet
            entry(1696, 3, 35), // emerald amulet
            entry(1698, 2, 25), // ruby amulet
            entry(1700, 2, 20), // diamond amulet
            entry(1702, 1, 12), // dragonstone amulet
            entry(6579, 1, 6), // onyx amulet
            entry(1725, 2, 20), // amulet of strength
            entry(1727, 2, 20), // amulet of magic
            entry(1729, 2, 20), // amulet of defence
            entry(1731, 2, 20), // amulet of power
            entry(1704, 1, 10), // amulet of glory
            entry(11113, 1, 8), // skills necklace
            entry(11126, 1, 8), // combat bracelet
            entry(6585, 1, 5), // amulet of fury
            entry(15126, 1, 5), // amulet of ranging
            entry(31857, 1, 4), // hydrix ring
            entry(31859, 1, 4), // hydrix necklace
            entry(31861, 1, 4), // hydrix amulet
            entry(31865, 1, 4), // hydrix bracelet
            entry(31869, 1, 3), // ring of death
            entry(31872, 1, 3), // reaper necklace
            entry(31875, 1, 3), // amulet of souls
            entry(31878, 1, 3), // deathtouch bracelet
            entry(39810, 1, 3), // alchemical onyx ring
            entry(44546, 1, 3), // alchemical onyx necklace
            entry(39812, 1, 3), // luck of the dwarves
            entry(44548, 1, 3), // grace of the elves
            entry(87, 1, 2), // Armadyl pendant
            entry(295, 1, 8), // Glarial's amulet
            entry(421, 1, 8), // Lathas' amulet
            entry(552, 1, 8), // Ghostspeak amulet
            entry(589, 1, 8), // Gnome amulet
            entry(1009, 1, 8), // Brass necklace
            entry(1796, 1, 8), // Silver necklace
            entry(2118, 1, 8), // Pineapple ring
            entry(4021, 1, 8), // Monkeyspeak amulet
            entry(4022, 1, 8), // M'speak amulet
            entry(4081, 1, 8), // Salve amulet
            entry(4183, 1, 8), // Star amulet
            entry(4187, 1, 8), // Marble amulet
            entry(4188, 1, 8), // Obsidian amulet
            entry(4489, 1, 8), // Asleif's necklace
            entry(4677, 1, 8), // Catspeak amulet
            entry(5521, 1, 8), // Binding necklace
            entry(6041, 1, 8), // Pre-nature amulet
            entry(6208, 1, 8), // Manspeak amulet
            entry(6707, 1, 8), // Camulet
            entry(7803, 1, 8), // Yin yang amulet
            entry(7927, 1, 8), // Easter ring
            entry(8023, 1, 8), // Boxing ring
            entry(8024, 1, 8), // Fencing ring
            entry(8025, 1, 8), // Combat ring
            entry(10344, 1, 8), // Third-age amulet
            entry(10872, 1, 8), // Pipe ring
            entry(11090, 1, 8), // Phoenix necklace
            entry(11128, 1, 8), // Berserker necklace
            entry(11133, 1, 8), // Regen bracelet
            entry(15511, 1, 8), // Royal amulet
            entry(20053, 1, 8), // Clay ring
            entry(20064, 1, 8), // Cramulet
            entry(20870, 1, 8), // Taevas's engagement ring
            entry(21526, 1, 8), // Shark's tooth necklace
            entry(24301, 1, 8), // Remora's cutscene necklace
            entry(30576, 1, 8), // Leviathan ring
            entry(30579, 1, 8)  // Superior leviathan ring
    ),

    SUMMONING_REAGENTS(
            entry(12155, 100, 2500), // pouch
            entry(12183, 500, 8000), // spirit shards
            entry(2859, 25, 500), // wolf bones
            entry(2138, 25, 500), // raw chicken
            entry(2132, 25, 500), // raw beef
            entry(2134, 25, 500), // raw rat meat
            entry(9978, 25, 500), // raw bird meat
            entry(6291, 15, 300), // spider carcass
            entry(3369, 15, 300), // thin snail meat
            entry(2150, 15, 300), // swamp toad
            entry(2876, 8, 180), // raw chompy
            entry(237, 10, 180), // unicorn horn
            entry(235, 10, 180), // unicorn horn dust
            entry(9736, 10, 180), // goat horn dust
            entry(1444, 5, 120), // water talisman
            entry(12156, 15, 300), // honeycomb
            entry(6032, 20, 300), // compost
            entry(8431, 5, 80), // bagged plant 1
            entry(8433, 5, 80), // bagged plant 2
            entry(8435, 5, 80), // bagged plant 3
            entry(6979, 10, 180), // granite (500g)
            entry(6981, 8, 140), // granite (2kg)
            entry(6983, 6, 100), // granite (5kg)
            entry(7939, 6, 100), // tortoise shell
            entry(10818, 10, 180), // yak-hide
            entry(3138, 10, 180), // potato cactus
            entry(14616, 1, 20), // phoenix quill
            entry(12109, 2, 35), // cockatrice egg
            entry(11964, 2, 35), // raven egg
            entry(11965, 2, 35), // vulture egg
            entry(12009, 2, 40), // granite crab pouch
            entry(12007, 2, 40), // spirit terrorbird pouch
            entry(12021, 2, 35), // beaver pouch
            entry(12029, 2, 35), // bunyip pouch
            entry(12031, 2, 35), // war tortoise pouch
            entry(12039, 1, 25), // unicorn stallion pouch
            entry(12059, 2, 40), // spirit spider pouch
            entry(12069, 2, 35), // granite lobster pouch
            entry(12089, 1, 25), // wolpertinger pouch
            entry(12093, 1, 25), // pack yak pouch
            entry(12786, 1, 20), // geyser titan pouch
            entry(12790, 1, 20), // steel titan pouch
            entry(12796, 1, 20), // abyssal titan pouch
            entry(12812, 1, 20), // spirit kyatt pouch
            entry(31409, 1, 15), // nihil pouch
            entry(31410, 1, 15), // blood nihil pouch
            entry(31412, 1, 15), // shadow nihil pouch
            entry(31414, 1, 15), // smoke nihil pouch
            entry(31416, 1, 15), // ice nihil pouch
            entry(12015, 40, 800), // Spirit cobra pouch
            entry(12017, 40, 800), // Spirit dagannoth pouch
            entry(12025, 40, 800), // Hydra pouch
            entry(12027, 40, 800), // Spirit jelly pouch
            entry(12033, 40, 800), // Fruit bat pouch
            entry(12041, 40, 800), // Magpie pouch
            entry(12043, 40, 800), // Dreadfowl pouch
            entry(12045, 40, 800), // Stranger plant pouch
            entry(12047, 40, 800), // Spirit wolf pouch
            entry(12049, 40, 800), // Desert wyrm pouch
            entry(12053, 40, 800), // Vampyre bat pouch
            entry(12055, 40, 800), // Spirit scorpion pouch
            entry(12063, 40, 800), // Spirit kalphite pouch
            entry(12071, 40, 800), // Macaw pouch
            entry(12073, 40, 800), // Bronze minotaur pouch
            entry(12075, 40, 800), // Iron minotaur pouch
            entry(12077, 40, 800), // Steel minotaur pouch
            entry(12079, 40, 800), // Mithril minotaur pouch
            entry(12081, 40, 800), // Adamant minotaur pouch
            entry(12083, 40, 800), // Rune minotaur pouch
            entry(12087, 40, 800), // Bull ant pouch
            entry(12095, 40, 800), // Spirit cockatrice pouch
            entry(12097, 40, 800), // Spirit guthatrice pouch
            entry(12099, 40, 800), // Spirit saratrice pouch
            entry(12101, 40, 800), // Spirit zamatrice pouch
            entry(12103, 40, 800), // Spirit pengatrice pouch
            entry(12105, 40, 800), // Spirit coraxatrice pouch
            entry(12107, 40, 800), // Spirit vulatrice pouch
            entry(12123, 40, 800), // Barker toad pouch
            entry(12531, 40, 800), // Ibis pouch
            entry(12776, 40, 800), // Swamp titan pouch
            entry(12778, 40, 800), // Spirit mosquito pouch
            entry(12784, 40, 800), // Spirit larupia pouch
            entry(12788, 40, 800), // Lava titan pouch
            entry(12792, 40, 800), // Obsidian golem pouch
            entry(12802, 40, 800), // Fire titan pouch
            entry(12804, 40, 800), // Moss titan pouch
            entry(12806, 40, 800), // Ice titan pouch
            entry(12808, 40, 800), // Spirit tz-kih pouch
            entry(12810, 40, 800), // Spirit graahk pouch
            entry(12820, 40, 800), // Ravenous locust pouch
            entry(12822, 40, 800), // Iron titan pouch
            entry(14623, 40, 800), // Phoenix pouch
            entry(15262, 40, 800)  // Spirit shard pack
    ),

    DIVINATION_ENERGY(
            entry(29314, 150, 2200), // pale energy
            entry(29315, 150, 2200), // flickering energy
            entry(29316, 150, 2200), // bright energy
            entry(29317, 150, 2200), // glowing energy
            entry(29318, 150, 2200), // sparkling energy
            entry(29319, 150, 2200), // gleaming energy
            entry(29320, 150, 2200), // vibrant energy
            entry(29321, 150, 2200), // lustrous energy
            entry(29322, 100, 1800), // brilliant energy
            entry(29323, 100, 1800), // radiant energy
            entry(29324, 100, 1600), // luminous energy
            entry(29325, 75, 1400), // incandescent energy
            entry(37941, 75, 1400), // cursed energy
            entry(36390, 10, 150), // divine charge
            entry(41073, 10, 150), // empty divine charge
            entry(29290, 1, 20), // sign of life
            entry(31322, 1, 20), // sign of death
            entry(29289, 1, 20), // portent of item protection
            entry(29292, 1, 20), // portent of life
            entry(31324, 1, 20), // portent of death
            entry(31312, 200, 5000), // Elder energy
            entry(36826, 200, 5000), // Shard of energy
            entry(43165, 200, 5000)  // Draconic energy
    ),

    BONES(
            entry(526, 100, 1600), // bones
            entry(530, 80, 1000), // bat bones
            entry(532, 80, 1000), // big bones
            entry(534, 50, 800), // babydragon bones
            entry(536, 30, 500), // dragon bones
            entry(6729, 20, 250), // dagannoth bones
            entry(6812, 20, 250), // wyvern bones
            entry(4834, 10, 180), // ourg bones
            entry(18830, 10, 150), // frost dragon bones
            entry(18832, 10, 150), // reinforced dragon bones
            entry(20266, 40, 700), // accursed ashes
            entry(20268, 30, 500), // infernal ashes
            entry(30209, 10, 140), // airut bones
            entry(32945, 20, 250), // tortured ashes
            entry(34159, 15, 200), // searing ashes
            entry(35008, 10, 150), // hardened dragon bones
            entry(35010, 10, 150), // reinforced dragon bones
            entry(592, 20, 350), // Ashes
            entry(1502, 20, 350), // Iban's ashes
            entry(2391, 20, 350), // Ground bat bones
            entry(3123, 20, 350), // Shaikahan bones
            entry(3125, 20, 350), // Jogre bones
            entry(3128, 20, 350), // Pasty jogre bones
            entry(3130, 20, 350), // Marinated jogre bones
            entry(3179, 20, 350), // Small ninja monkey bones
            entry(3180, 20, 350), // Medium ninja monkey bones
            entry(3181, 20, 350), // Gorilla bones
            entry(3182, 20, 350), // Bearded gorilla bones
            entry(3183, 20, 350), // Monkey bones
            entry(3185, 20, 350), // Small zombie monkey bones
            entry(3186, 20, 350), // Large zombie monkey bones
            entry(4812, 20, 350), // Zogre bones
            entry(4830, 20, 350), // Fayrg bones
            entry(4832, 20, 350), // Raurg bones
            entry(6904, 20, 350), // Animals' bones
            entry(8865, 20, 350), // Ground ashes
            entry(20264, 20, 350), // Impious ashes
            entry(23031, 20, 350), // Troll general's bones
            entry(23032, 20, 350), // Troll lieutenant's bones
            entry(28190, 20, 350), // Grotesque bones
            entry(32494, 20, 350), // Human ashes
            entry(34723, 20, 350), // Shark jawbone
            entry(40340, 20, 350), // Blurb bones
            entry(41563, 20, 350), // Rudolph's ashes
            entry(48075, 20, 350)  // Dinosaur bones
    ),

    FARMING_SEEDS(
            entry(5318, 10, 180), // potato seed
            entry(5319, 10, 180), // onion seed
            entry(5324, 10, 180), // cabbage seed
            entry(5322, 10, 160), // tomato seed
            entry(5320, 10, 150), // sweetcorn seed
            entry(5323, 8, 140), // strawberry seed
            entry(5321, 6, 120), // watermelon seed
            entry(5291, 8, 120), // guam seed
            entry(5292, 8, 120), // marrentill seed
            entry(5293, 8, 120), // tarromin seed
            entry(5294, 6, 100), // harralander seed
            entry(5295, 4, 90), // ranarr seed
            entry(5296, 4, 90), // toadflax seed
            entry(5297, 4, 90), // irit seed
            entry(5298, 4, 80), // avantoe seed
            entry(5299, 4, 80), // kwuarm seed
            entry(5300, 3, 70), // snapdragon seed
            entry(5301, 3, 70), // cadantine seed
            entry(5302, 3, 60), // lantadyme seed
            entry(5303, 3, 60), // dwarf weed seed
            entry(5304, 2, 50), // torstol seed
            entry(5096, 10, 120), // marigold seed
            entry(5097, 10, 120), // rosemary seed
            entry(5098, 10, 120), // nasturtium seed
            entry(5099, 10, 120), // woad seed
            entry(5100, 8, 100), // limpwurt seed
            entry(5312, 2, 30), // acorn
            entry(5313, 2, 25), // willow seed
            entry(5314, 1, 20), // maple seed
            entry(5315, 1, 15), // yew seed
            entry(5316, 1, 10), // magic seed
            entry(5283, 2, 25), // apple tree seed
            entry(5284, 2, 25), // banana tree seed
            entry(5285, 2, 20), // orange tree seed
            entry(5286, 1, 18), // curry tree seed
            entry(5287, 1, 15), // pineapple seed
            entry(5288, 1, 12), // papaya tree seed
            entry(5289, 1, 10), // palm tree seed
            entry(5305, 10, 120), // barley seed
            entry(5306, 10, 120), // jute seed
            entry(5307, 10, 120), // hammerstone seed
            entry(5308, 10, 120), // asgarnian seed
            entry(5309, 10, 120), // yanillian seed
            entry(5310, 10, 120), // krandorian seed
            entry(5311, 8, 100), // wildblood seed
            entry(5282, 8, 90), // bittercap mushroom spore
            entry(12176, 4, 80), // spirit weed seed
            entry(14870, 4, 80), // wergali seed
            entry(21620, 3, 60), // morchella mushroom spore
            entry(21621, 3, 60), // fellstalk seed
            entry(28259, 5, 90), // prickly pear seed
            entry(28260, 5, 90), // fly trap seed
            entry(735, 8, 150), // Yommi tree seeds
            entry(5101, 8, 150), // Redberry seed
            entry(5102, 8, 150), // Cadavaberry seed
            entry(5103, 8, 150), // Dwellberry seed
            entry(5104, 8, 150), // Jangerberry seed
            entry(5105, 8, 150), // Whiteberry seed
            entry(5106, 8, 150), // Poison ivy seed
            entry(5280, 8, 150), // Cactus seed
            entry(5281, 8, 150), // Belladonna seed
            entry(5290, 1, 15), // Calquat tree seed
            entry(5317, 8, 150), // Spirit seed
            entry(5370, 8, 150), // Oak sapling
            entry(5371, 8, 150), // Willow sapling
            entry(5372, 8, 150), // Maple sapling
            entry(5373, 8, 150), // Yew sapling
            entry(5374, 8, 150), // Magic sapling
            entry(5375, 8, 150), // Spirit sapling
            entry(5496, 8, 150), // Apple sapling
            entry(5497, 8, 150), // Banana sapling
            entry(5498, 8, 150), // Orange sapling
            entry(5499, 8, 150), // Curry sapling
            entry(5500, 8, 150), // Pineapple sapling
            entry(5501, 8, 150), // Papaya sapling
            entry(5502, 1, 15), // Palm sapling
            entry(5503, 1, 15), // Calquat sapling
            entry(6103, 8, 150), // Crystal teleport seed
            entry(6710, 8, 150), // Blindweed seed
            entry(7950, 8, 150), // Bone seeds
            entry(10178, 8, 150), // Odd bird seed
            entry(12148, 8, 150), // Evil turnip seed
            entry(14589, 8, 150), // White lily seed
            entry(15075, 8, 150), // Marker seeds
            entry(19897, 8, 150), // Erzille seed
            entry(19902, 8, 150), // Ugune seed
            entry(19907, 8, 150), // Argway seed
            entry(19912, 8, 150), // Shengo seed
            entry(19917, 8, 150), // Samaden seed
            entry(19922, 8, 150), // Red blossom seed
            entry(19927, 8, 150), // Blue blossom seed
            entry(19932, 8, 150), // Green blossom seed
            entry(19937, 8, 150), // Lergberry seed
            entry(19942, 8, 150), // Kalferberry seed
            entry(22448, 8, 150), // Polypore spore
            entry(24777, 8, 150), // Gold seeds
            entry(24778, 8, 150), // Evil seeds
            entry(24779, 8, 150), // Green seeds
            entry(28258, 8, 150), // Potato cactus seed
            entry(28261, 8, 150), // Sunchoke seed
            entry(28262, 8, 150), // Snape grass seed
            entry(28263, 8, 150)  // Reed seed
    ),

    FARMING_PRODUCTS(
            entry(1942, 50, 900), // potato
            entry(1957, 50, 900), // onion
            entry(1965, 50, 900), // cabbage
            entry(1982, 40, 700), // tomato
            entry(5986, 40, 700), // sweetcorn
            entry(5504, 30, 600), // strawberry
            entry(5982, 25, 500), // watermelon
            entry(225, 30, 350), // limpwurt root
            entry(231, 30, 350), // snape grass
            entry(239, 20, 250), // white berries
            entry(6016, 15, 200), // cactus spine
            entry(6018, 15, 200), // poison ivy berries
            entry(1955, 30, 500), // cooking apple
            entry(1963, 30, 500), // banana
            entry(2108, 25, 450), // orange
            entry(2114, 20, 400), // pineapple
            entry(5972, 15, 250), // papaya fruit
            entry(5974, 15, 250), // coconut
            entry(2970, 20, 300), // mort myre fungus
            entry(5931, 40, 700), // jute fibre
            entry(5994, 40, 700), // hammerstone hops
            entry(5996, 40, 700), // asgarnian hops
            entry(5998, 40, 700), // yanillian hops
            entry(6000, 30, 600), // krandorian hops
            entry(6002, 25, 500), // wildblood hops
            entry(6004, 20, 350), // bittercap mushroom
            entry(6006, 40, 700), // barley
            entry(6008, 40, 700), // barley malt
            entry(1869, 40, 800), // Chopped tomato
            entry(1871, 40, 800), // Chopped onion
            entry(1875, 40, 800), // Onion & tomato
            entry(2518, 40, 800), // Rotten tomato
            entry(4620, 40, 800), // Black mushroom
            entry(5440, 40, 800), // Onions (1)
            entry(5442, 40, 800), // Onions (2)
            entry(5444, 40, 800), // Onions (3)
            entry(5446, 40, 800), // Onions (4)
            entry(5448, 40, 800), // Onions (5)
            entry(5450, 40, 800), // Onions (6)
            entry(5452, 40, 800), // Onions (7)
            entry(5454, 40, 800), // Onions (8)
            entry(5456, 40, 800), // Onions (9)
            entry(5458, 40, 800), // Onions (10)
            entry(5460, 40, 800), // Cabbages (1)
            entry(5462, 40, 800), // Cabbages (2)
            entry(5464, 40, 800), // Cabbages (3)
            entry(5466, 40, 800), // Cabbages (4)
            entry(5468, 40, 800), // Cabbages (5)
            entry(5470, 40, 800), // Cabbages (6)
            entry(5472, 40, 800), // Cabbages (7)
            entry(5474, 40, 800), // Cabbages (8)
            entry(5476, 40, 800), // Cabbages (9)
            entry(5478, 40, 800), // Cabbages (10)
            entry(5485, 8, 120), // Papaya seedling
            entry(5493, 8, 120), // Papaya seedling (w)
            entry(5733, 40, 800), // Rotten potato
            entry(5935, 40, 800), // Coconut milk
            entry(5960, 40, 800), // Tomatoes (1)
            entry(5962, 40, 800), // Tomatoes (2)
            entry(5964, 40, 800), // Tomatoes (3)
            entry(5966, 40, 800), // Tomatoes (4)
            entry(5968, 40, 800), // Tomatoes (5)
            entry(5978, 40, 800), // Coconut shell
            entry(5984, 40, 800), // Watermelon slice
            entry(5988, 40, 800), // Cooked sweetcorn
            entry(6701, 40, 800), // Baked potato
            entry(7054, 40, 800), // Chilli potato
            entry(7056, 40, 800), // Egg potato
            entry(7058, 40, 800), // Mushroom potato
            entry(7060, 40, 800), // Tuna potato
            entry(7064, 40, 800), // Egg and tomato
            entry(7066, 40, 800), // Mushroom & onion
            entry(7084, 40, 800), // Fried onions
            entry(9994, 40, 800), // Spicy tomato
            entry(12051, 40, 800), // Evil turnip pouch
            entry(12091, 40, 800), // Compost mound pouch
            entry(13563, 40, 800), // Button mushroom
            entry(19950, 40, 800), // Strange potato
            entry(21622, 40, 800), // Morchella mushroom
            entry(22446, 40, 800)  // Gorajian mushroom
    ),

    RANGED_AMMO(
            entry(882, 250, 3500), // bronze arrow
            entry(884, 250, 3500), // iron arrow
            entry(886, 250, 3500), // steel arrow
            entry(888, 250, 3000), // mithril arrow
            entry(890, 200, 2500), // adamant arrow
            entry(892, 200, 2500), // rune arrow
            entry(11212, 50, 900), // dragon arrow
            entry(877, 250, 3500), // bronze bolts
            entry(9140, 250, 3500), // iron bolts
            entry(9141, 250, 3000), // steel bolts
            entry(9142, 200, 2500), // mithril bolts
            entry(9143, 150, 2000), // adamant bolts
            entry(9144, 150, 2000), // runite bolts
            entry(9243, 50, 700), // diamond bolts(e)
            entry(9244, 50, 700), // dragon bolts(e)
            entry(806, 200, 2500), // bronze dart
            entry(807, 200, 2500), // iron dart
            entry(808, 200, 2500), // steel dart
            entry(809, 150, 2000), // mithril dart
            entry(810, 120, 1800), // adamant dart
            entry(811, 100, 1500), // rune dart
            entry(11230, 30, 500), // dragon dart
            entry(864, 150, 2000), // bronze knife
            entry(863, 150, 2000), // iron knife
            entry(865, 150, 2000), // steel knife
            entry(866, 120, 1800), // mithril knife
            entry(867, 100, 1500), // adamant knife
            entry(868, 80, 1200), // rune knife
            entry(2, 300, 4500), // cannonball
            entry(8882, 250, 3500), // bone bolts
            entry(9139, 250, 3500), // blurite bolts
            entry(9145, 250, 3500), // silver bolts
            entry(9236, 80, 1200), // opal bolts(e)
            entry(9237, 80, 1200), // jade bolts(e)
            entry(9238, 80, 1200), // pearl bolts(e)
            entry(9239, 80, 1200), // topaz bolts(e)
            entry(9240, 80, 1200), // sapphire bolts(e)
            entry(9241, 80, 1200), // emerald bolts(e)
            entry(9242, 60, 900), // ruby bolts(e)
            entry(9245, 20, 300), // onyx bolts(e)
            entry(9335, 80, 1200), // jade bolts
            entry(9336, 80, 1200), // topaz bolts
            entry(9337, 80, 1200), // sapphire bolts
            entry(9338, 80, 1200), // emerald bolts
            entry(9339, 60, 900), // ruby bolts
            entry(9340, 50, 800), // diamond bolts
            entry(9341, 30, 500), // dragon bolts
            entry(9342, 20, 300), // onyx bolts
            entry(13280, 200, 2500), // broad-tipped bolts
            entry(9976, 20, 350), // chinchompa
            entry(9977, 20, 350), // red chinchompa
            entry(10033, 20, 350), // chinchompa
            entry(10034, 20, 350), // red chinchompa
            entry(12539, 20, 350), // grenwall spikes
            entry(24116, 80, 1200), // bakriminel bolts
            entry(28465, 60, 900), // ascension bolts
            entry(31737, 60, 900), // araxyte arrows
            entry(31868, 50, 750), // ascendri bolts
            entry(31881, 40, 650), // ascendri bolts(e)
            entry(41623, 40, 650), // opal bakriminel bolts(e)
            entry(41624, 40, 650), // sapphire bakriminel bolts(e)
            entry(41625, 40, 650), // jade bakriminel bolts(e)
            entry(41626, 40, 650), // pearl bakriminel bolts(e)
            entry(41627, 40, 650), // emerald bakriminel bolts(e)
            entry(41628, 40, 650), // topaz bakriminel bolts(e)
            entry(41629, 35, 550), // ruby bakriminel bolts(e)
            entry(41630, 35, 550), // diamond bakriminel bolts(e)
            entry(41631, 25, 450), // dragonstone bakriminel bolts(e)
            entry(41632, 20, 350), // onyx bakriminel bolts(e)
            entry(41633, 15, 250), // hydrix bakriminel bolts(e)
            entry(41634, 40, 650), // opal bakriminel bolts
            entry(41639, 40, 650), // sapphire bakriminel bolts
            entry(41644, 40, 650), // jade bakriminel bolts
            entry(41649, 40, 650), // pearl bakriminel bolts
            entry(41654, 40, 650), // emerald bakriminel bolts
            entry(41659, 40, 650), // topaz bakriminel bolts
            entry(41664, 35, 550), // ruby bakriminel bolts
            entry(41669, 35, 550), // diamond bakriminel bolts
            entry(41674, 25, 450), // dragonstone bakriminel bolts
            entry(41679, 20, 350), // onyx bakriminel bolts
            entry(41684, 15, 250), // hydrix bakriminel bolts
            entry(78, 250, 5000), // Ice arrows
            entry(598, 250, 5000), // Bronze fire arrows
            entry(800, 250, 5000), // Bronze throwing axe
            entry(801, 250, 5000), // Iron throwing axe
            entry(802, 250, 5000), // Steel throwing axe
            entry(803, 250, 5000), // Mithril throwing axe
            entry(804, 250, 5000), // Adamant throwing axe
            entry(805, 250, 5000), // Rune throwing axe
            entry(825, 250, 5000), // Bronze javelin
            entry(826, 250, 5000), // Iron javelin
            entry(827, 250, 5000), // Steel javelin
            entry(828, 250, 5000), // Mithril javelin
            entry(829, 250, 5000), // Adamant javelin
            entry(830, 250, 5000), // Rune javelin
            entry(879, 250, 5000), // Opal bolts
            entry(880, 250, 5000), // Pearl bolts
            entry(881, 250, 5000), // Barbed bolts
            entry(1417, 250, 5000), // Javelin
            entry(1849, 250, 5000), // Prototype dart
            entry(2532, 250, 5000), // Iron fire arrows
            entry(2534, 250, 5000), // Steel fire arrows
            entry(2536, 250, 5000), // Mithril fire arrows
            entry(2538, 250, 5000), // Adamant fire arrows
            entry(2540, 250, 5000), // Rune fire arrows
            entry(2865, 250, 5000), // Flighted ogre arrow
            entry(2866, 250, 5000), // Ogre arrow
            entry(3093, 250, 5000), // Black dart
            entry(4150, 250, 5000), // Broad arrows
            entry(4160, 250, 5000), // Broad arrow
            entry(7684, 250, 5000), // Dart
            entry(7686, 250, 5000), // Bow and arrow
            entry(10158, 250, 5000), // Kebbit bolts
            entry(10159, 250, 5000), // Long kebbit bolts
            entry(11217, 75, 1200), // Dragon fire arrows
            entry(12800, 75, 1200), // Giant chinchompa pouch
            entry(13083, 250, 5000), // Black bolts
            entry(13879, 250, 5000), // Morrigan's javelin
            entry(13883, 250, 5000), // Morrigan's throwing axe
            entry(13953, 250, 5000), // Corrupt Morrigan's javelin
            entry(13957, 250, 5000), // Corrupt Morrigan's throwing axe
            entry(19152, 250, 5000), // Saradomin arrows
            entry(19157, 250, 5000), // Guthix arrows
            entry(19162, 250, 5000), // Zamorak arrows
            entry(21650, 250, 5000), // Basiliskbane arrow
            entry(21655, 250, 5000), // Abyssalbane arrow
            entry(21670, 250, 5000), // Basiliskbane bolt
            entry(21675, 250, 5000), // Abyssalbane bolt
            entry(24304, 250, 5000), // Coral bolts
            entry(24336, 250, 5000), // Royal bolts
            entry(25903, 250, 5000), // Off-hand bronze throwing axe
            entry(25904, 250, 5000), // Off-hand iron throwing axe
            entry(25905, 250, 5000), // Off-hand steel throwing axe
            entry(25906, 250, 5000), // Off-hand mithril throwing axe
            entry(25907, 250, 5000), // Off-hand adamant throwing axe
            entry(25908, 250, 5000), // Off-hand rune throwing axe
            entry(25909, 250, 5000), // Off-hand bronze dart
            entry(25910, 250, 5000), // Off-hand iron dart
            entry(25911, 250, 5000), // Off-hand steel dart
            entry(25912, 250, 5000), // Off-hand black dart
            entry(25913, 250, 5000), // Off-hand mithril dart
            entry(25914, 250, 5000), // Off-hand adamant dart
            entry(25915, 250, 5000), // Off-hand rune dart
            entry(25916, 75, 1200), // Off-hand dragon dart
            entry(27374, 250, 5000), // Cupid arrow
            entry(29543, 75, 1200), // Dragon throwing axe
            entry(29544, 250, 5000), // Off-hand dragon throwing axe
            entry(29617, 250, 5000), // Dark arrow
            entry(29622, 250, 5000), // Lit dark fire arrows
            entry(29627, 250, 5000), // Unlit dark fire arrows
            entry(30343, 250, 5000), // Orkish throwing axe
            entry(30344, 250, 5000), // Off-hand orkish throwing axe
            entry(30575, 250, 5000), // Off-hand Death Lotus dart
            entry(34235, 250, 5000), // Wild arrow
            entry(35115, 250, 5000), // Dragon javelin
            entry(35116, 75, 1200), // Off-hand dragon javelin
            entry(36391, 250, 5000), // Mechanised chinchompa
            entry(37666, 250, 5000), // Manticore throwing axe
            entry(37667, 250, 5000), // Manticore off-hand throwing axe
            entry(41575, 250, 5000), // Stalker arrow
            entry(42748, 250, 5000), // Blight bolts
            entry(43068, 250, 5000), // Elite Death Lotus dart
            entry(43069, 250, 5000)  // Off-hand Elite Death Lotus dart
    ),

    MELEE_GEAR(
            entry(1321, 1, 8), // bronze scimitar
            entry(1323, 1, 8), // iron scimitar
            entry(1325, 1, 8), // steel scimitar
            entry(1329, 1, 8), // mithril scimitar
            entry(1331, 1, 8), // adamant scimitar
            entry(1333, 1, 8), // rune scimitar
            entry(4587, 1, 5), // dragon scimitar
            entry(1215, 1, 5), // dragon dagger
            entry(5698, 1, 5), // dragon dagger p++
            entry(1305, 1, 5), // dragon longsword
            entry(1377, 1, 5), // dragon battleaxe
            entry(1434, 1, 5), // dragon mace
            entry(4151, 1, 4), // abyssal whip
            entry(1163, 1, 8), // rune full helm
            entry(1127, 1, 8), // rune platebody
            entry(1079, 1, 8), // rune platelegs
            entry(1201, 1, 8), // rune kiteshield
            entry(1149, 1, 5), // dragon med helm
            entry(24947, 1, 4), // dragon helm
            entry(3140, 1, 4), // dragon chainbody
            entry(4087, 1, 4), // dragon platelegs
            entry(4585, 1, 4), // dragon plateskirt
            entry(11732, 1, 5), // dragon boots
            entry(1249, 1, 4), // dragon spear
            entry(3204, 1, 4), // dragon halberd
            entry(7158, 1, 4), // dragon 2h sword
            entry(6739, 1, 4), // dragon hatchet
            entry(11283, 1, 3), // dragonfire shield
            entry(11335, 1, 3), // dragon full helm
            entry(14479, 1, 3), // dragon platebody
            entry(15259, 1, 3), // dragon pickaxe
            entry(21369, 1, 5), // whip vine
            entry(21371, 1, 3), // abyssal vine whip
            entry(31377, 1, 3), // dragon hasta
            entry(44660, 1, 2), // orikalkum armour set
            entry(44666, 1, 2), // orikalkum armour set + 3
            entry(44668, 1, 2), // necronium armour set
            entry(44676, 1, 2), // necronium armour set + 4
            entry(44678, 1, 2), // banite armour set
            entry(44686, 1, 2), // banite armour set + 4
            entry(44688, 1, 2), // elder rune armour set
            entry(44698, 1, 2), // elder rune armour set + 5
            entry(45549, 1, 3), // elder rune longsword
            entry(45574, 1, 2), // elder rune longsword + 5
            entry(45611, 1, 3), // elder rune 2h sword
            entry(45636, 1, 2), // elder rune 2h sword + 5
            entry(45655, 1, 3), // elder rune full helm
            entry(45680, 1, 2), // elder rune full helm + 5
            entry(45686, 1, 3), // elder rune platelegs
            entry(45711, 1, 2), // elder rune platelegs + 5
            entry(45717, 1, 3), // elder rune platebody
            entry(45742, 1, 2), // elder rune platebody + 5
            entry(45779, 1, 2), // elder rune armoured boots
            entry(45810, 1, 2), // elder rune gauntlets
            entry(1067, 1, 8), // Iron platelegs
            entry(1069, 1, 8), // Steel platelegs
            entry(1071, 1, 8), // Mithril platelegs
            entry(1073, 1, 8), // Adamant platelegs
            entry(1075, 1, 8), // Bronze platelegs
            entry(1077, 1, 8), // Black platelegs
            entry(1081, 1, 8), // Iron plateskirt
            entry(1083, 1, 8), // Steel plateskirt
            entry(1085, 1, 8), // Mithril plateskirt
            entry(1087, 1, 8), // Bronze plateskirt
            entry(1089, 1, 8), // Black plateskirt
            entry(1091, 1, 8), // Adamant plateskirt
            entry(1093, 1, 8), // Rune plateskirt
            entry(1101, 1, 8), // Iron chainbody
            entry(1103, 1, 8), // Bronze chainbody
            entry(1105, 1, 8), // Steel chainbody
            entry(1107, 1, 8), // Black chainbody
            entry(1109, 1, 8), // Mithril chainbody
            entry(1111, 1, 8), // Adamant chainbody
            entry(1113, 1, 8), // Rune chainbody
            entry(1115, 1, 8), // Iron platebody
            entry(1117, 1, 8), // Bronze platebody
            entry(1119, 1, 8), // Steel platebody
            entry(1121, 1, 8), // Mithril platebody
            entry(1123, 1, 8), // Adamant platebody
            entry(1125, 1, 8), // Black platebody
            entry(1137, 1, 8), // Iron helm
            entry(1139, 1, 8), // Bronze helm
            entry(1141, 1, 8), // Steel helm
            entry(1143, 1, 8), // Mithril helm
            entry(1145, 1, 8), // Adamant helm
            entry(1147, 1, 8), // Rune helm
            entry(1151, 1, 8), // Black helm
            entry(1153, 1, 8), // Iron full helm
            entry(1155, 1, 8), // Bronze full helm
            entry(1157, 1, 8), // Steel full helm
            entry(1159, 1, 8), // Mithril full helm
            entry(1161, 1, 8), // Adamant full helm
            entry(1165, 1, 8), // Black full helm
            entry(1173, 1, 8), // Bronze sq shield
            entry(1175, 1, 8), // Iron sq shield
            entry(1177, 1, 8), // Steel sq shield
            entry(1179, 1, 8), // Black sq shield
            entry(1181, 1, 8), // Mithril sq shield
            entry(1183, 1, 8), // Adamant sq shield
            entry(1185, 1, 8), // Rune sq shield
            entry(1187, 1, 4), // Dragon sq shield
            entry(1189, 1, 8), // Bronze kiteshield
            entry(1191, 1, 8), // Iron kiteshield
            entry(1193, 1, 8), // Steel kiteshield
            entry(1195, 1, 8), // Black kiteshield
            entry(1197, 1, 8), // Mithril kiteshield
            entry(1199, 1, 8), // Adamant kiteshield
            entry(1203, 1, 8), // Iron dagger
            entry(1205, 1, 8), // Bronze dagger
            entry(1207, 1, 8), // Steel dagger
            entry(1209, 1, 8), // Mithril dagger
            entry(1211, 1, 8), // Adamant dagger
            entry(1213, 1, 8), // Rune dagger
            entry(1217, 1, 8), // Black dagger
            entry(1237, 1, 8), // Bronze spear
            entry(1239, 1, 8), // Iron spear
            entry(1241, 1, 8), // Steel spear
            entry(1243, 1, 8), // Mithril spear
            entry(1245, 1, 8), // Adamant spear
            entry(1247, 1, 8), // Rune spear
            entry(1277, 1, 8), // Bronze sword
            entry(1279, 1, 8), // Iron sword
            entry(1281, 1, 8), // Steel sword
            entry(1283, 1, 8), // Black sword
            entry(1285, 1, 8), // Mithril sword
            entry(1287, 1, 8), // Adamant sword
            entry(1289, 1, 8), // Rune sword
            entry(1291, 1, 8), // Bronze longsword
            entry(1293, 1, 8), // Iron longsword
            entry(1295, 1, 8), // Steel longsword
            entry(1297, 1, 8), // Black longsword
            entry(1299, 1, 8), // Mithril longsword
            entry(1301, 1, 8), // Adamant longsword
            entry(1303, 1, 8), // Rune longsword
            entry(1307, 1, 8), // Bronze 2h sword
            entry(1309, 1, 8), // Iron 2h sword
            entry(1311, 1, 8), // Steel 2h sword
            entry(1313, 1, 8), // Black 2h sword
            entry(1315, 1, 8), // Mithril 2h sword
            entry(1317, 1, 8), // Adamant 2h sword
            entry(1319, 1, 8), // Rune 2h sword
            entry(1327, 1, 8), // Black scimitar
            entry(1335, 1, 8), // Iron warhammer
            entry(1337, 1, 8), // Bronze warhammer
            entry(1339, 1, 8), // Steel warhammer
            entry(1341, 1, 8), // Black warhammer
            entry(1343, 1, 8), // Mithril warhammer
            entry(1345, 1, 8), // Adamant warhammer
            entry(1347, 1, 8), // Rune warhammer
            entry(1363, 1, 8), // Iron battleaxe
            entry(1365, 1, 8), // Steel battleaxe
            entry(1367, 1, 8), // Black battleaxe
            entry(1369, 1, 8), // Mithril battleaxe
            entry(1371, 1, 8), // Adamant battleaxe
            entry(1373, 1, 8), // Rune battleaxe
            entry(1375, 1, 8), // Bronze battleaxe
            entry(1420, 1, 8), // Iron mace
            entry(1422, 1, 8), // Bronze mace
            entry(1424, 1, 8), // Steel mace
            entry(1426, 1, 8), // Black mace
            entry(1428, 1, 8), // Mithril mace
            entry(1430, 1, 8), // Adamant mace
            entry(1432, 1, 8), // Rune mace
            entry(3190, 1, 8), // Bronze halberd
            entry(3192, 1, 8), // Iron halberd
            entry(3194, 1, 8), // Steel halberd
            entry(3196, 1, 8), // Black halberd
            entry(3198, 1, 8), // Mithril halberd
            entry(3200, 1, 8), // Adamant halberd
            entry(3202, 1, 8), // Rune halberd
            entry(3385, 1, 8), // Splitbark helm
            entry(3387, 1, 8), // Splitbark body
            entry(3391, 1, 8), // Splitbark gauntlets
            entry(3393, 1, 8), // Splitbark boots
            entry(4119, 1, 8), // Bronze boots
            entry(4121, 1, 8), // Iron boots
            entry(4123, 1, 8), // Steel boots
            entry(4125, 1, 8), // Black boots
            entry(4127, 1, 8), // Mithril boots
            entry(4129, 1, 8), // Adamant boots
            entry(4131, 1, 8), // Rune boots
            entry(4580, 1, 8), // Black spear
            entry(4726, 1, 4), // Guthan's warspear
            entry(4728, 1, 4), // Guthan's platebody
            entry(4730, 1, 4), // Guthan's chainskirt
            entry(4747, 1, 4), // Torag's hammer
            entry(4749, 1, 4), // Torag's platebody
            entry(4751, 1, 4), // Torag's platelegs
            entry(4753, 1, 4), // Verac's helm
            entry(4755, 1, 4), // Verac's flail
            entry(4757, 1, 4), // Verac's brassard
            entry(4759, 1, 4), // Verac's plateskirt
            entry(6131, 1, 8), // Spined helm
            entry(6133, 1, 8), // Spined body
            entry(6137, 1, 8), // Skeletal helm
            entry(6143, 1, 8), // Spined boots
            entry(6147, 1, 8), // Skeletal boots
            entry(6149, 1, 8), // Spined gloves
            entry(6153, 1, 8), // Skeletal gloves
            entry(6589, 1, 8), // White battleaxe
            entry(6591, 1, 8), // White dagger
            entry(6599, 1, 8), // White halberd
            entry(6601, 1, 8), // White mace
            entry(6605, 1, 8), // White sword
            entry(6607, 1, 8), // White longsword
            entry(6609, 1, 8), // White 2h sword
            entry(6611, 1, 8), // White scimitar
            entry(6613, 1, 8), // White warhammer
            entry(6615, 1, 8), // White chainbody
            entry(6617, 1, 8), // White platebody
            entry(6619, 1, 8), // White boots
            entry(6621, 1, 8), // White helm
            entry(6623, 1, 8), // White full helm
            entry(6625, 1, 8), // White platelegs
            entry(6627, 1, 8), // White plateskirt
            entry(6629, 1, 8), // White gauntlets
            entry(6631, 1, 8), // White sq shield
            entry(6633, 1, 8), // White kiteshield
            entry(8464, 1, 8), // Rune heraldic helm
            entry(8682, 1, 8), // Steel heraldic helm
            entry(11367, 1, 8), // Bronze hasta
            entry(11369, 1, 8), // Iron hasta
            entry(11371, 1, 8), // Steel hasta
            entry(11373, 1, 8), // Mithril hasta
            entry(11375, 1, 8), // Adamant hasta
            entry(11377, 1, 8), // Rune hasta
            entry(11848, 1, 4), // Barrows - Dharok's set
            entry(11850, 1, 4), // Barrows - Guthan's set
            entry(11854, 1, 4), // Barrows - Torag's set
            entry(11856, 1, 4), // Barrows - Verac's set
            entry(12861, 1, 8), // Shark gloves
            entry(12985, 1, 8), // Bronze gauntlets
            entry(12986, 1, 8), // Worn-out bronze gauntlets
            entry(12988, 1, 8), // Iron gauntlets
            entry(12989, 1, 8), // Worn-out iron gauntlets
            entry(12991, 1, 8), // Steel gauntlets
            entry(12992, 1, 8), // Worn-out steel gauntlets
            entry(12994, 1, 8), // Black gauntlets
            entry(12995, 1, 8), // Worn-out black gauntlets
            entry(12997, 1, 8), // Mithril gauntlets
            entry(12998, 1, 8), // Worn-out mithril gauntlets
            entry(13000, 1, 8), // Adamant gauntlets
            entry(13001, 1, 8), // Worn-out adamant gauntlets
            entry(13003, 1, 8), // Rune gauntlets
            entry(13004, 1, 8), // Worn-out rune gauntlets
            entry(13006, 1, 4), // Dragon gauntlets
            entry(13007, 1, 4), // Worn-out dragon gauntlets
            entry(13618, 1, 8), // Runecrafter gloves
            entry(13958, 1, 4), // Corrupt dragon chainbody
            entry(13961, 1, 4), // Corrupt dragon helm
            entry(13964, 1, 4), // Corrupt dragon sq shield
            entry(13967, 1, 4), // Corrupt dragon plateskirt
            entry(13970, 1, 4), // Corrupt dragon platelegs
            entry(13973, 1, 4), // Corrupt dragon battleaxe
            entry(13976, 1, 4), // Corrupt dragon dagger
            entry(13979, 1, 4), // Corrupt dragon scimitar
            entry(13982, 1, 4), // Corrupt dragon longsword
            entry(13985, 1, 4), // Corrupt dragon mace
            entry(13988, 1, 4), // Corrupt dragon spear
            entry(14490, 1, 8), // Elite black platelegs
            entry(14492, 1, 8), // Elite black platebody
            entry(14494, 1, 8), // Elite black full helm
            entry(24359, 1, 4), // Dragonbone full helm
            entry(24360, 1, 4), // Dragonbone platebody
            entry(24361, 1, 4), // Dragonbone gloves
            entry(24362, 1, 4), // Dragonbone boots
            entry(24363, 1, 4), // Dragonbone platelegs
            entry(24364, 1, 4), // Dragonbone plateskirt
            entry(25316, 1, 4), // Dragon rider boots
            entry(33294, 1, 4), // Barrows Dye
            entry(45580, 1, 4)  // Elder rune off hand longsword
    ),

    RANGED_GEAR(
            entry(861, 1, 12), // magic shortbow
            entry(9185, 1, 10), // rune crossbow
            entry(11235, 1, 5), // dark bow
            entry(2491, 1, 8), // black d'hide vambraces
            entry(2497, 1, 8), // black d'hide chaps
            entry(2503, 1, 8), // black d'hide body
            entry(4732, 1, 3), // karil's coif
            entry(4734, 1, 3), // karil's crossbow
            entry(4736, 1, 3), // karil's leathertop
            entry(4738, 1, 3), // karil's leatherskirt
            entry(1065, 1, 10), // green d'hide vambraces
            entry(1099, 1, 10), // green d'hide chaps
            entry(1135, 1, 10), // green d'hide body
            entry(2487, 1, 10), // blue d'hide vambraces
            entry(2493, 1, 10), // blue d'hide chaps
            entry(2499, 1, 10), // blue d'hide body
            entry(2489, 1, 10), // red d'hide vambraces
            entry(2495, 1, 10), // red d'hide chaps
            entry(2501, 1, 10), // red d'hide body
            entry(12936, 1, 8), // green dragonhide coif
            entry(12943, 1, 8), // blue dragonhide coif
            entry(12950, 1, 8), // red dragonhide coif
            entry(12957, 1, 8), // black dragonhide coif
            entry(11864, 1, 4), // green dragonhide set
            entry(11866, 1, 4), // blue dragonhide set
            entry(11868, 1, 4), // red dragonhide set
            entry(11870, 1, 4), // black dragonhide set
            entry(24376, 1, 4), // royal d'hide vambraces
            entry(24379, 1, 4), // royal d'hide chaps
            entry(24382, 1, 4), // royal d'hide body
            entry(24388, 1, 4), // royal d'hide coif
            entry(24420, 1, 3), // royal dragonhide set
            entry(25794, 1, 6), // green dragonhide shield
            entry(25796, 1, 6), // blue dragonhide shield
            entry(25798, 1, 6), // red dragonhide shield
            entry(25800, 1, 6), // black dragonhide shield
            entry(20147, 1, 2), // pernix cowl
            entry(20151, 1, 2), // pernix body
            entry(20155, 1, 2), // pernix chaps
            entry(20171, 1, 2), // zaryte bow
            entry(24974, 1, 2), // pernix gloves
            entry(24989, 1, 2), // pernix boots
            entry(28437, 1, 2), // ascension crossbow
            entry(28441, 1, 2), // off-hand ascension crossbow
            entry(31203, 1, 2), // ascension grips
            entry(37077, 1, 2), // shadow glaive
            entry(37082, 1, 2), // off-hand shadow glaive
            entry(6135, 1, 8), // Spined chaps
            entry(9174, 1, 8), // Bronze crossbow
            entry(9177, 1, 8), // Iron crossbow
            entry(9179, 1, 8), // Steel crossbow
            entry(9181, 1, 8), // Mithril crossbow
            entry(9183, 1, 8), // Adamant crossbow
            entry(11852, 1, 4), // Barrows - Karil's set
            entry(13081, 1, 8), // Black crossbow
            entry(25894, 1, 4), // Off-hand dragon crossbow
            entry(25917, 1, 4), // Dragon crossbow
            entry(25918, 1, 4), // Karil's pistol crossbow
            entry(25932, 1, 4), // Dragon 2h crossbow
            entry(36801, 1, 4)  // Karil's pistol crossbow (used)
    ),

    MAGE_GEAR(
            entry(1381, 1, 15), // staff of air
            entry(1383, 1, 15), // staff of water
            entry(1385, 1, 15), // staff of earth
            entry(1387, 1, 15), // staff of fire
            entry(4675, 1, 5), // ancient staff
            entry(6914, 1, 4), // master wand
            entry(4089, 1, 8), // mystic hat
            entry(4091, 1, 8), // mystic robe top
            entry(4093, 1, 8), // mystic robe bottom
            entry(4095, 1, 8), // mystic gloves
            entry(4097, 1, 8), // mystic boots
            entry(4708, 1, 3), // ahrim's hood
            entry(4710, 1, 3), // ahrim's staff
            entry(4712, 1, 3), // ahrim's robetop
            entry(4714, 1, 3), // ahrim's robeskirt
            entry(1391, 1, 12), // battlestaff
            entry(1393, 1, 10), // fire battlestaff
            entry(1395, 1, 10), // water battlestaff
            entry(1397, 1, 10), // air battlestaff
            entry(1399, 1, 10), // earth battlestaff
            entry(3053, 1, 5), // lava battlestaff
            entry(6562, 1, 5), // mud battlestaff
            entry(11736, 1, 5), // steam battlestaff
            entry(21777, 1, 2), // armadyl battlestaff
            entry(22482, 1, 4), // ganodermic visor
            entry(22486, 1, 4), // ganodermic leggings
            entry(22490, 1, 4), // ganodermic poncho
            entry(22494, 1, 4), // polypore staff
            entry(25978, 1, 4), // ganodermic gloves
            entry(25980, 1, 4), // ganodermic boots
            entry(24992, 1, 2), // hood of subjugation
            entry(24995, 1, 2), // garb of subjugation
            entry(24998, 1, 2), // gown of subjugation
            entry(25001, 1, 2), // ward of subjugation
            entry(25004, 1, 2), // boots of subjugation
            entry(25007, 1, 2), // gloves of subjugation
            entry(20159, 1, 2), // virtus mask
            entry(20163, 1, 2), // virtus robe top
            entry(20167, 1, 2), // virtus robe legs
            entry(24980, 1, 2), // virtus gloves
            entry(24986, 1, 2), // virtus boots
            entry(25654, 1, 2), // virtus wand
            entry(25664, 1, 2), // virtus book
            entry(30825, 1, 3), // abyssal wand
            entry(30828, 1, 3), // abyssal orb
            entry(28617, 1, 2), // seismic wand
            entry(28621, 1, 2), // seismic singularity
            entry(1401, 1, 8), // Mystic fire staff
            entry(1403, 1, 8), // Mystic water staff
            entry(1405, 1, 8), // Mystic air staff
            entry(1407, 1, 8), // Mystic earth staff
            entry(3054, 1, 8), // Mystic lava staff
            entry(4105, 1, 8), // Dark mystic gloves
            entry(4107, 1, 8), // Dark mystic boots
            entry(4115, 1, 8), // Light mystic gloves
            entry(4117, 1, 8), // Light mystic boots
            entry(6038, 1, 8), // Magic string
            entry(6563, 1, 8), // Mystic mud staff
            entry(6603, 1, 8), // White magic staff
            entry(6752, 1, 8), // Black desert robe
            entry(7162, 1, 8), // Pie recipe book
            entry(11738, 1, 8), // Mystic steam staff
            entry(13614, 1, 8), // Runecrafter robe
            entry(13629, 1, 8), // Runecrafting staff
            entry(19323, 1, 4), // Dragon staff
            entry(21494, 1, 8), // Skeletal battlestaff of fire
            entry(21495, 1, 8), // Skeletal battlestaff of water
            entry(21496, 1, 8), // Skeletal battlestaff of air
            entry(21497, 1, 8), // Skeletal battlestaff of earth
            entry(21502, 1, 8), // Skeletal lava battlestaff
            entry(21504, 1, 8), // Skeletal mud battlestaff
            entry(21506, 1, 8), // Skeletal steam battlestaff
            entry(24357, 1, 4), // Dragonbone mage gloves
            entry(24358, 1, 4), // Dragonbone mage boots
            entry(25483, 1, 8), // Magic stock
            entry(25652, 1, 4), // Ahrim's wand
            entry(25672, 1, 4)  // Ahrim's book of magic
    ),

    BOSS_LOOT(
            entry(536, 20, 250), // dragon bones
            entry(6729, 10, 160), // dagannoth bones
            entry(5300, 5, 90), // snapdragon seed
            entry(5304, 3, 60), // torstol seed
            entry(6731, 1, 3), // seers ring
            entry(6733, 1, 3), // archers ring
            entry(6735, 1, 3), // warrior ring
            entry(6737, 1, 3), // berserker ring
            entry(11286, 1, 2), // draconic visage
            entry(11732, 1, 5), // dragon boots
            entry(14484, 1, 3), // dragon claws
            entry(11716, 1, 3), // zamorakian spear
            entry(11730, 1, 3), // saradomin sword
            entry(11690, 1, 2), // godsword blade
            entry(11702, 1, 2), // armadyl hilt
            entry(11704, 1, 2), // bandos hilt
            entry(11706, 1, 2), // saradomin hilt
            entry(11708, 1, 2), // zamorak hilt
            entry(11710, 1, 3), // godsword shard 1
            entry(11712, 1, 3), // godsword shard 2
            entry(11714, 1, 3), // godsword shard 3
            entry(13734, 1, 2), // spirit shield
            entry(13736, 1, 2), // blessed spirit shield
            entry(13738, 1, 2), // arcane spirit shield
            entry(13740, 1, 2), // divine spirit shield
            entry(13742, 1, 2), // elysian spirit shield
            entry(13744, 1, 2), // spectral spirit shield
            entry(13746, 1, 2), // arcane sigil
            entry(13748, 1, 2), // divine sigil
            entry(13750, 1, 2), // elysian sigil
            entry(13752, 1, 2), // spectral sigil
            entry(24340, 1, 2), // royal bolt stabiliser
            entry(24342, 1, 2), // royal frame
            entry(24344, 1, 2), // royal sight
            entry(24346, 1, 2), // royal torsion spring
            entry(24352, 1, 2), // dragonbone upgrade kit
            entry(25174, 1, 2), // kalphite greathelm
            entry(28436, 5, 80), // ascension shard
            entry(28445, 1, 5), // ascension keystone primus
            entry(28447, 1, 5), // ascension keystone secundus
            entry(28449, 1, 5), // ascension keystone tertius
            entry(28451, 1, 5), // ascension keystone quartus
            entry(28453, 1, 5), // ascension keystone quintus
            entry(28455, 1, 5), // ascension keystone sextus
            entry(28627, 5, 60), // tectonic energy
            entry(29863, 10, 120), // sirenic scale
            entry(30027, 5, 60), // malevolent energy
            entry(31718, 1, 2), // spider leg top
            entry(31719, 1, 2), // spider leg middle
            entry(31720, 1, 2), // spider leg bottom
            entry(31721, 1, 2), // spider leg
            entry(37018, 1, 2), // crest of zaros
            entry(37021, 1, 2), // crest of sliske
            entry(37024, 1, 2), // crest of zamorak
            entry(37027, 1, 2), // crest of seren
            entry(37622, 1, 2), // dormant seren godbow
            entry(37624, 1, 2), // dormant staff of sliske
            entry(37626, 1, 2), // dormant zaros godsword
            entry(35148, 1, 2), // mazcab ability codex
            entry(35150, 1, 2), // storm shards and shatter codex
            entry(35151, 1, 2), // corruption shot codex
            entry(35152, 1, 2), // corruption blast codex
            entry(35153, 1, 2), // onslaught codex
            entry(37628, 1, 2)  // dormant zamorakian ability codex
    ),

    RARES(
            entry(962, 1, 1), // christmas cracker
            entry(981, 1, 1), // disk of returning
            entry(1961, 1, 1), // easter egg
            entry(1989, 1, 1), // half full wine jug
            entry(1419, 1, 1), // scythe
            entry(1050, 1, 1), // santa hat
            entry(29571, 1, 1), // black santa hat
            entry(1038, 1, 1), // red partyhat
            entry(1040, 1, 1), // yellow partyhat
            entry(1042, 1, 1), // blue partyhat
            entry(1044, 1, 1), // green partyhat
            entry(1046, 1, 1), // purple partyhat
            entry(1048, 1, 1), // white partyhat
            entry(1053, 1, 1), // green halloween mask
            entry(1055, 1, 1), // blue halloween mask
            entry(1057, 1, 1), // red halloween mask
            entry(42408, 1, 1)  // Easter egg hat
    ),

    PVP_GEAR(
            entry(5698, 1, 4), // dragon dagger p++
            entry(4151, 1, 3), // abyssal whip
            entry(1333, 1, 6), // rune scimitar
            entry(1127, 1, 6), // rune platebody
            entry(1079, 1, 6), // rune platelegs
            entry(11732, 1, 5), // dragon boots
            entry(4587, 1, 4), // dragon scimitar
            entry(1215, 1, 4), // dragon dagger
            entry(9185, 1, 5), // rune crossbow
            entry(11235, 1, 3), // dark bow
            entry(2503, 1, 5), // black d'hide body
            entry(4675, 1, 4), // ancient staff
            entry(1249, 1, 4), // dragon spear
            entry(3204, 1, 4), // dragon halberd
            entry(7158, 1, 4), // dragon 2h sword
            entry(11212, 80, 1200), // dragon arrows
            entry(11230, 50, 800), // dragon darts
            entry(9244, 50, 700), // dragon bolts(e)
            entry(9245, 20, 300), // onyx bolts(e)
            entry(9977, 20, 350), // red chinchompa
            entry(21371, 1, 3), // abyssal vine whip
            entry(2499, 1, 5), // blue d'hide body
            entry(2501, 1, 5), // red d'hide body
            entry(12957, 1, 5)  // black dragonhide coif
    ),

    ADVANCED_GEAR(
            entry(4716, 1, 2), // dharok's helm
            entry(4718, 1, 2), // dharok's greataxe
            entry(4720, 1, 2), // dharok's platebody
            entry(4722, 1, 2), // dharok's platelegs
            entry(4708, 1, 2), // ahrim's hood
            entry(4710, 1, 2), // ahrim's staff
            entry(4712, 1, 2), // ahrim's robetop
            entry(4714, 1, 2), // ahrim's robeskirt
            entry(4732, 1, 2), // karil's coif
            entry(4734, 1, 2), // karil's crossbow
            entry(4736, 1, 2), // karil's leathertop
            entry(4738, 1, 2), // karil's leatherskirt
            entry(11694, 1, 2), // armadyl godsword
            entry(11696, 1, 2), // bandos godsword
            entry(11698, 1, 2), // saradomin godsword
            entry(11700, 1, 2), // zamorak godsword
            entry(11718, 1, 2), // armadyl helmet
            entry(11720, 1, 2), // armadyl chestplate
            entry(11722, 1, 2), // armadyl chainskirt
            entry(11724, 1, 2), // bandos chestplate
            entry(11726, 1, 2), // bandos tassets
            entry(11728, 1, 2), // bandos boots
            entry(11283, 1, 2), // dragonfire shield
            entry(14479, 1, 2), // dragon platebody
            entry(20135, 1, 2), // torva full helm
            entry(20139, 1, 2), // torva platebody
            entry(20143, 1, 2), // torva platelegs
            entry(20147, 1, 2), // pernix cowl
            entry(20151, 1, 2), // pernix body
            entry(20155, 1, 2), // pernix chaps
            entry(20159, 1, 2), // virtus mask
            entry(20163, 1, 2), // virtus robe top
            entry(20167, 1, 2), // virtus robe legs
            entry(20171, 1, 2), // zaryte bow
            entry(26579, 1, 2), // drygore longsword
            entry(26583, 1, 2), // drygore rapier
            entry(26587, 1, 2), // drygore mace
            entry(31725, 1, 2), // noxious scythe
            entry(31729, 1, 2), // noxious longbow
            entry(31733, 1, 2), // noxious staff
            entry(11702, 1, 2), // armadyl hilt
            entry(11704, 1, 2), // bandos hilt
            entry(11706, 1, 2), // saradomin hilt
            entry(11708, 1, 2), // zamorak hilt
            entry(13738, 1, 2), // arcane spirit shield
            entry(13740, 1, 2), // divine spirit shield
            entry(13742, 1, 2), // elysian spirit shield
            entry(13744, 1, 2), // spectral spirit shield
            entry(22482, 1, 2), // ganodermic visor
            entry(22486, 1, 2), // ganodermic leggings
            entry(22490, 1, 2), // ganodermic poncho
            entry(22494, 1, 2), // polypore staff
            entry(24365, 1, 2), // dragon kiteshield
            entry(24974, 1, 2), // pernix gloves
            entry(24977, 1, 2), // torva gloves
            entry(24980, 1, 2), // virtus gloves
            entry(24983, 1, 2), // torva boots
            entry(24986, 1, 2), // virtus boots
            entry(24989, 1, 2), // pernix boots
            entry(24992, 1, 2), // hood of subjugation
            entry(24995, 1, 2), // garb of subjugation
            entry(24998, 1, 2), // gown of subjugation
            entry(25001, 1, 2), // ward of subjugation
            entry(25004, 1, 2), // boots of subjugation
            entry(25007, 1, 2), // gloves of subjugation
            entry(25010, 1, 2), // armadyl boots
            entry(25016, 1, 2), // armadyl gloves
            entry(25025, 1, 2), // bandos gloves
            entry(25174, 1, 2), // kalphite greathelm
            entry(25654, 1, 2), // virtus wand
            entry(25664, 1, 2), // virtus book
            entry(25978, 1, 2), // ganodermic gloves
            entry(25980, 1, 2), // ganodermic boots
            entry(26583, 1, 2), // off-hand drygore rapier
            entry(26587, 1, 2), // drygore longsword
            entry(26591, 1, 2), // off-hand drygore longsword
            entry(26595, 1, 2), // drygore mace
            entry(26599, 1, 2), // off-hand drygore mace
            entry(28437, 1, 2), // ascension crossbow
            entry(28441, 1, 2), // off-hand ascension crossbow
            entry(28608, 1, 2), // tectonic mask
            entry(28611, 1, 2), // tectonic robe top
            entry(28614, 1, 2), // tectonic robe bottom
            entry(28617, 1, 2), // seismic wand
            entry(28621, 1, 2), // seismic singularity
            entry(29854, 1, 2), // sirenic mask
            entry(29857, 1, 2), // sirenic hauberk
            entry(29860, 1, 2), // sirenic chaps
            entry(30005, 1, 2), // malevolent helm
            entry(30008, 1, 2), // malevolent cuirass
            entry(30011, 1, 2), // malevolent greaves
            entry(30014, 1, 2), // malevolent kiteshield
            entry(30930, 1, 2), // dragon rider helm
            entry(30933, 1, 2), // dragon rider body
            entry(30936, 1, 2), // dragon rider chaps
            entry(31203, 1, 2), // ascension grips
            entry(37009, 1, 2), // dormant anima core helm
            entry(37012, 1, 2), // dormant anima core body
            entry(37015, 1, 2), // dormant anima core legs
            entry(37036, 1, 2), // anima core helm of zaros
            entry(37039, 1, 2), // anima core body of zaros
            entry(37042, 1, 2), // anima core legs of zaros
            entry(37045, 1, 2), // anima core helm of zamorak
            entry(37048, 1, 2), // anima core body of zamorak
            entry(37051, 1, 2), // anima core legs of zamorak
            entry(37054, 1, 2), // anima core helm of seren
            entry(37057, 1, 2), // anima core body of seren
            entry(37060, 1, 2), // anima core legs of seren
            entry(37063, 1, 2), // anima core helm of sliske
            entry(37066, 1, 2), // anima core body of sliske
            entry(37069, 1, 2), // anima core legs of sliske
            entry(37072, 1, 2), // dragon rider lance
            entry(37077, 1, 2), // shadow glaive
            entry(37082, 1, 2), // off-hand shadow glaive
            entry(37632, 1, 2), // seren godbow
            entry(37636, 1, 2), // staff of sliske
            entry(37640, 1, 2), // zaros godsword
            entry(39574, 1, 2), // wand of the praesul
            entry(39579, 1, 2), // imperium core
            entry(40655, 1, 2), // khopesh of tumeken
            entry(40659, 1, 2), // khopesh of elidinis
            entry(43155, 1, 2), // elite sirenic mask
            entry(43158, 1, 2), // elite sirenic hauberk
            entry(43161, 1, 2), // elite sirenic chaps
            entry(43166, 1, 2), // elite tectonic mask
            entry(43169, 1, 2), // elite tectonic robe top
            entry(43172, 1, 2), // elite tectonic robe bottom
            entry(4724, 1, 3), // Guthan's helm
            entry(4745, 1, 3), // Torag's helm
            entry(11846, 1, 3), // Barrows - Ahrim's set
            entry(12670, 1, 2), // Armadyl helmet (e)
            entry(12671, 1, 2), // Armadyl helmet (charged)
            entry(14692, 1, 2), // Bandos throne room sphere
            entry(14881, 1, 2), // Bandos statuette
            entry(14887, 1, 2), // Bandos scrimshaw
            entry(19364, 1, 2), // Bandos crozier
            entry(19370, 1, 2), // Bandos cloak
            entry(19376, 1, 2), // Bandos mitre
            entry(19380, 1, 2), // Armadyl robe top
            entry(19384, 1, 2), // Bandos robe top
            entry(19386, 1, 2), // Armadyl robe legs
            entry(19388, 1, 2), // Bandos robe legs
            entry(19394, 1, 2), // Bandos stole
            entry(19413, 1, 2), // Rune platebody (Armadyl)
            entry(19416, 1, 2), // Rune platelegs (Armadyl)
            entry(19419, 1, 2), // Rune plateskirt (Armadyl)
            entry(19422, 1, 2), // Rune full helm (Armadyl)
            entry(19425, 1, 2), // Rune kiteshield (Armadyl)
            entry(19428, 1, 2), // Rune platebody (Bandos)
            entry(19431, 1, 2), // Rune platelegs (Bandos)
            entry(19434, 1, 2), // Rune plateskirt (Bandos)
            entry(19437, 1, 2), // Rune full helm (Bandos)
            entry(19440, 1, 2), // Rune kiteshield (Bandos)
            entry(19451, 1, 2), // Bandos vambraces
            entry(19453, 1, 2), // Bandos body
            entry(19455, 1, 2), // Bandos chaps
            entry(19457, 1, 2), // Bandos coif
            entry(19459, 1, 2), // Armadyl vambraces
            entry(19461, 1, 2), // Armadyl body
            entry(19463, 1, 2), // Armadyl chaps
            entry(19465, 1, 2), // Armadyl coif
            entry(19515, 1, 2), // Armadyl platebody
            entry(19516, 1, 2), // Bandos platebody
            entry(19592, 1, 2), // Bandos armour set (lg)
            entry(19594, 1, 2), // Bandos armour set (sk)
            entry(20122, 1, 2), // Frozen key piece (bandos)
            entry(21787, 1, 3), // Steadfast boots
            entry(21790, 1, 3), // Glaiven boots
            entry(21793, 1, 3), // Ragefire boots
            entry(25019, 1, 2), // Bandos warshield
            entry(25022, 1, 2), // Bandos helmet
            entry(25037, 1, 2), // Armadyl crossbow
            entry(30245, 1, 2), // Spider hole (bandos)
            entry(30246, 1, 2), // Watchtower (bandos)
            entry(30247, 1, 2), // Divining lodestone (bandos)
            entry(30248, 1, 2), // Divining siphon (bandos)
            entry(30249, 1, 2), // Carpenter's station (Bandos)
            entry(30250, 1, 2), // Booby trap (bandos)
            entry(30251, 1, 2), // Faction banner (bandos)
            entry(30252, 1, 2), // Teleport focus (bandos)
            entry(30270, 1, 2), // Bandos's Might I
            entry(30272, 1, 2), // Bandos's Might II
            entry(30274, 1, 2), // Bandos's Might III
            entry(30901, 1, 2), // Torva armour set
            entry(30903, 1, 2), // Virtus armour set
            entry(30905, 1, 2), // Pernix armour set
            entry(31755, 1, 2), // Armadyl godsword (passive)
            entry(31756, 1, 2), // Bandos godsword (passive)
            entry(31757, 1, 3), // Saradomin godsword (passive)
            entry(31758, 1, 3), // Zamorak godsword (passive)
            entry(32014, 1, 2), // Golden Armadyl godsword
            entry(32015, 1, 2), // Golden Armadyl godsword (passive)
            entry(32016, 1, 2), // Golden Bandos godsword
            entry(32017, 1, 2), // Golden Bandos godsword (passive)
            entry(32018, 1, 3), // Golden Saradomin godsword
            entry(32019, 1, 3), // Golden Saradomin godsword (passive)
            entry(32020, 1, 3), // Golden Zamorak godsword
            entry(32021, 1, 3), // Golden Zamorak godsword (passive)
            entry(34855, 1, 2), // Off-hand Armadyl crossbow
            entry(35985, 1, 3), // Wyvern crossbow
            entry(40926, 1, 2), // Bandos armour set
            entry(43584, 1, 2)  // Bandosian bantam hen
    ),

    GENERAL_MARKET(
            entry(245, 10, 200), // Wine of Zamorak
            entry(301, 10, 200), // Lobster pot
            entry(704, 10, 200), // Ground charcoal
            entry(973, 10, 200), // Charcoal
            entry(1789, 10, 200), // Pie dish (unfired)
            entry(1853, 10, 200), // Prototype dart tip
            entry(1887, 10, 200), // Cake tin
            entry(2036, 10, 200), // Premade pineapple punch
            entry(2048, 10, 200), // Pineapple punch
            entry(2116, 10, 200), // Pineapple chunks
            entry(2283, 10, 200), // Pizza base
            entry(2313, 10, 200), // Pie dish
            entry(2315, 10, 200), // Pie shell
            entry(2864, 10, 200), // Ogre arrow shaft
            entry(2957, 10, 200), // Druid pouch
            entry(3152, 10, 200), // Karambwan paste
            entry(3157, 10, 200), // Karambwan vessel
            entry(4014, 10, 200), // Monkey bar
            entry(4205, 10, 200), // Consecration seed
            entry(4207, 10, 200), // Crystal seed
            entry(4287, 10, 200), // Raw undead beef
            entry(4486, 10, 200), // White pearl seed
            entry(4622, 10, 200), // Black mushroom ink
            entry(4700, 10, 200), // Sapphire lantern
            entry(5363, 10, 200), // Spirit seedling
            entry(5369, 10, 200), // Spirit seedling (w)
            entry(5484, 10, 200), // Pineapple seedling
            entry(5487, 5, 60), // Calquat seedling
            entry(5492, 10, 200), // Pineapple seedling (w)
            entry(5495, 5, 60), // Calquat seedling (w)
            entry(5509, 10, 200), // Small pouch
            entry(5510, 10, 200), // Medium pouch
            entry(5512, 10, 200), // Large pouch
            entry(5514, 10, 200), // Giant pouch
            entry(5583, 10, 200), // Tin ore powder
            entry(5584, 10, 200), // Cupric ore powder
            entry(5769, 5, 60), // Calquat keg
            entry(5980, 5, 60), // Calquat fruit
            entry(6053, 10, 200), // Spirit roots
            entry(6063, 10, 200), // Spirit tree
            entry(6313, 10, 200), // Opal machete
            entry(6315, 10, 200), // Jade machete
            entry(6317, 10, 200), // Red topaz machete
            entry(6681, 10, 200), // Ground guam
            entry(7068, 10, 200), // Tuna and corn
            entry(7080, 10, 200), // Sliced mushrooms
            entry(7082, 10, 200), // Fried mushrooms
            entry(8066, 10, 200), // Limestone altar
            entry(9064, 10, 200), // Emerald lantern
            entry(9066, 10, 200), // Emerald lens
            entry(9187, 10, 200), // Jade bolt tips
            entry(9188, 10, 200), // Topaz bolt tips
            entry(9650, 10, 200), // Blood tithe pouch
            entry(9728, 10, 200), // Mind bar
            entry(9767, 5, 60), // Runecrafting hood
            entry(10018, 10, 200), // Sapphire glacialis
            entry(10020, 10, 200), // Ruby harvest
            entry(10101, 10, 200), // Tatty kyatt fur
            entry(10103, 10, 200), // Kyatt fur
            entry(10149, 10, 200), // Swamp lizard
            entry(10876, 10, 200), // Metal bar
            entry(10968, 10, 200), // Mushrooms
            entry(12011, 10, 200), // Praying mantis pouch
            entry(12013, 10, 200), // Giant ent pouch
            entry(12019, 10, 200), // Thorny snail pouch
            entry(12023, 10, 200), // Karam. overlord pouch
            entry(12035, 10, 200), // Abyssal parasite pouch
            entry(12037, 10, 200), // Abyssal lurker pouch
            entry(12057, 10, 200), // Arctic bear pouch
            entry(12061, 10, 200), // Bloated leech pouch
            entry(12065, 10, 200), // Honey badger pouch
            entry(12067, 10, 200), // Albino rat pouch
            entry(12085, 10, 200), // Smoke devil pouch
            entry(12782, 10, 200), // Forge regent pouch
            entry(12794, 10, 200), // Talon beast pouch
            entry(12816, 10, 200), // Pyrelord pouch
            entry(13155, 10, 200), // Silver sickle emerald (b)
            entry(13156, 10, 200), // Enchanted sickle emerald (b)
            entry(14828, 10, 200), // Shark tooth
            entry(14878, 1, 2), // Armadyl statuette
            entry(14882, 10, 200), // Ruby chalice
            entry(14884, 1, 2), // Armadyl totem
            entry(15365, 10, 200), // Raw bird meat pack
            entry(18506, 10, 200), // Spirit guardian
            entry(18690, 10, 200), // Body bar
            entry(19362, 1, 2), // Armadyl crozier
            entry(19368, 1, 2), // Armadyl cloak
            entry(19374, 1, 2), // Armadyl mitre
            entry(19392, 1, 2), // Armadyl stole
            entry(19588, 1, 2), // Armadyl armour set (lg)
            entry(19590, 1, 2), // Armadyl armour set (sk)
            entry(19893, 10, 200), // Spirit cape
            entry(20121, 1, 2), // Frozen key piece (armadyl)
            entry(20433, 10, 200), // Cosmic bar
            entry(20434, 10, 200), // Chaos bar
            entry(21525, 10, 200), // Shark's tooth
            entry(21570, 10, 200), // Jewelled diamond statuette
            entry(21774, 1, 2), // Dust of Armadyl
            entry(21775, 1, 2), // Orb of Armadyl
            entry(21776, 1, 2), // Shards of Armadyl
            entry(24127, 10, 200), // Bakriminel bolt tips
            entry(24133, 10, 200), // Bakriminel bolt tip pack
            entry(24849, 10, 200), // Shark fist 1
            entry(24850, 10, 200), // Shark fist 2
            entry(24851, 10, 200), // Shark fist 3
            entry(24852, 10, 200), // Shark fist 4
            entry(25013, 1, 2), // Armadyl buckler
            entry(25028, 1, 2), // Saradomin's whisper
            entry(25031, 1, 2), // Saradomin's hiss
            entry(25034, 1, 2), // Saradomin's murmur
            entry(25953, 10, 200), // Off-hand shark fist 1
            entry(25954, 10, 200), // Off-hand shark fist 2
            entry(25955, 10, 200), // Off-hand shark fist 3
            entry(25956, 10, 200), // Off-hand shark fist 4
            entry(26289, 1, 12), // Tree-shaking scrimshaw
            entry(26291, 1, 12), // Superior tree-shaking scrimshaw
            entry(28251, 10, 200), // Unfermented wine of zamorak
            entry(30237, 1, 2), // Spider hole (armadyl)
            entry(30238, 1, 2), // Watchtower (armadyl)
            entry(30239, 1, 2), // Divining lodestone (armadyl)
            entry(30240, 1, 2), // Divining siphon (armadyl)
            entry(30241, 1, 2), // Carpenter's station (Armadyl)
            entry(30242, 1, 2), // Booby trap (armadyl)
            entry(30243, 1, 2), // Faction banner (armadyl) 3
            entry(30244, 1, 2), // Teleport focus (armadyl)
            entry(30264, 1, 2), // Armadyl's Glory I
            entry(30266, 1, 2), // Armadyl's Glory II
            entry(30268, 1, 2), // Armadyl's Glory III
            entry(33628, 1, 2), // Holly wreath
            entry(33733, 10, 200), // Snowboard (shark)
            entry(33896, 1, 12), // Whopper-baiting scrimshaw
            entry(33899, 1, 12), // Superior whopper-baiting scrimshaw
            entry(34698, 10, 200), // Golden shark eggs
            entry(34748, 10, 200), // Shark jaw
            entry(37404, 10, 200), // Calorie bomb (shark)
            entry(37987, 10, 200), // Shark fin
            entry(39750, 10, 200), // Shark fragments
            entry(40924, 1, 2), // Armadyl armour set
            entry(41453, 10, 200), // Ore supply box
            entry(44680, 5, 60), // Banite armour set + 1
            entry(44682, 5, 60), // Banite armour set + 2
            entry(44684, 5, 60), // Banite armour set + 3
            entry(44779, 10, 200), // Bronze ore box
            entry(44781, 10, 200), // Iron ore box
            entry(44783, 10, 200), // Steel ore box
            entry(44785, 10, 200), // Mithril ore box
            entry(44787, 10, 200), // Adamant ore box
            entry(44789, 10, 200), // Rune ore box
            entry(44791, 10, 200), // Orikalkum ore box
            entry(44793, 10, 200), // Necronium ore box
            entry(44795, 10, 200), // Bane ore box
            entry(44797, 10, 200), // Elder rune ore box
            entry(44837, 10, 200)  // Luminite injector
    );

    private final Entry[] entries;

    MarketItemCategory(Entry... entries) {
        this.entries = entries;
    }

    public Map<Integer, Integer> roll(int minEntries, int maxEntries) {
        Map<Integer, Integer> items = new HashMap<>();
        List<Entry> pool = new ArrayList<>();
        Collections.addAll(pool, entries);
        int bias = rollCountBias();
        int adjustedMin = Math.max(0, minEntries + bias);
        int adjustedMax = Math.max(adjustedMin, maxEntries + bias);
        int target = randomBetween(adjustedMin, adjustedMax);
        while (!pool.isEmpty() && items.size() < target) {
            Entry entry = removeWeighted(pool);
            if (!isMarketItem(entry.itemId)) {
                continue;
            }
            items.put(entry.itemId, randomBetween(entry.minAmount, entry.maxAmount));
        }
        return items;
    }

    static void addAll(Map<Integer, Integer> target, Map<Integer, Integer> source) {
        for (Map.Entry<Integer, Integer> entry : source.entrySet()) {
            Integer current = target.get(entry.getKey());
            long next = (long) (current == null ? 0 : current) + entry.getValue();
            target.put(entry.getKey(), (int) Math.min(Integer.MAX_VALUE, Math.max(0, next)));
        }
    }

    static boolean isMarketItem(int itemId) {
        return itemId > 0 && Utils.itemExists(itemId) && ItemConstants.isTradeable(new Item(itemId, 1));
    }

    static boolean isRareItem(int itemId) {
        for (Entry entry : RARES.entries) {
            if (entry.itemId == itemId) {
                return true;
            }
        }
        return false;
    }

    static boolean isStapleItem(int itemId) {
        return containsItem(itemId, LOGS, ORES, BARS, HERBS, RAW_FISH, FOOD, RUNES,
                SKILLING_INPUTS, GEMS, CRAFTING_INPUTS, FARMING_SEEDS,
                FARMING_PRODUCTS, DIVINATION_ENERGY, SUMMONING_REAGENTS, BONES);
    }

    static boolean isHighTierItem(int itemId) {
        return containsItem(itemId, RARES, ADVANCED_GEAR, BOSS_LOOT)
                || containsItem(itemId, PVP_GEAR, MELEE_GEAR, RANGED_GEAR, MAGE_GEAR)
                && GrandExchange.getPrice(itemId) >= 1_000_000;
    }

    static boolean containsItem(int itemId, MarketItemCategory... categories) {
        if (categories == null) {
            return false;
        }
        for (MarketItemCategory category : categories) {
            if (category == null) {
                continue;
            }
            for (Entry entry : category.entries) {
                if (entry.itemId == itemId) {
                    return true;
                }
            }
        }
        return false;
    }

    static List<Integer> allItemIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        for (MarketItemCategory category : values()) {
            for (Entry entry : category.entries) {
                ids.add(entry.itemId);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(ids));
    }

    private static Entry entry(int itemId, int minAmount, int maxAmount) {
        return new Entry(itemId, minAmount, maxAmount);
    }

    private Entry removeWeighted(List<Entry> pool) {
        int totalWeight = 0;
        for (Entry entry : pool) {
            totalWeight += entryWeight(entry);
        }
        int roll = ThreadLocalRandom.current().nextInt(Math.max(1, totalWeight));
        for (int index = 0; index < pool.size(); index++) {
            Entry entry = pool.get(index);
            roll -= entryWeight(entry);
            if (roll < 0) {
                return pool.remove(index);
            }
        }
        return pool.remove(pool.size() - 1);
    }

    private int rollCountBias() {
        switch (this) {
            case LOGS:
            case ORES:
            case BARS:
            case HERBS:
            case RAW_FISH:
            case FOOD:
            case RUNES:
            case SKILLING_INPUTS:
            case GEMS:
            case CRAFTING_INPUTS:
            case FARMING_SEEDS:
            case FARMING_PRODUCTS:
            case DIVINATION_ENERGY:
            case SUMMONING_REAGENTS:
            case BONES:
                return 4;
            case POTIONS:
            case SUPPLIES:
            case RANGED_AMMO:
            case SHORTBOWS:
            case JEWELRY:
            case TALISMANS:
                return 2;
            case RARES:
            case ADVANCED_GEAR:
                return -4;
            case BOSS_LOOT:
                return -3;
            case PVP_GEAR:
            case MELEE_GEAR:
            case RANGED_GEAR:
            case MAGE_GEAR:
                return -2;
            case GENERAL_MARKET:
            default:
                return 0;
        }
    }

    private int entryWeight(Entry entry) {
        int weight = 100;
        if (isStapleRollCategory()) {
            weight += 75;
        } else if (isHighTierRollCategory()) {
            weight -= 45;
        }
        int price = Math.max(0, GrandExchange.getPrice(entry.itemId));
        if (price <= 25_000) {
            weight += 40;
        } else if (price <= 250_000) {
            weight += 20;
        } else if (price >= 25_000_000) {
            weight -= 75;
        } else if (price >= 5_000_000) {
            weight -= 50;
        } else if (price >= 1_000_000) {
            weight -= 25;
        }
        if (entry.maxAmount >= 800) {
            weight += 25;
        } else if (entry.maxAmount <= 5) {
            weight -= 10;
        }
        return Math.max(5, Math.min(250, weight));
    }

    private boolean isStapleRollCategory() {
        switch (this) {
            case LOGS:
            case ORES:
            case BARS:
            case HERBS:
            case RAW_FISH:
            case FOOD:
            case RUNES:
            case SKILLING_INPUTS:
            case GEMS:
            case CRAFTING_INPUTS:
            case FARMING_SEEDS:
            case FARMING_PRODUCTS:
            case DIVINATION_ENERGY:
            case SUMMONING_REAGENTS:
            case BONES:
                return true;
            default:
                return false;
        }
    }

    private boolean isHighTierRollCategory() {
        switch (this) {
            case RARES:
            case ADVANCED_GEAR:
            case BOSS_LOOT:
            case PVP_GEAR:
            case MELEE_GEAR:
            case RANGED_GEAR:
            case MAGE_GEAR:
                return true;
            default:
                return false;
        }
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static final class Entry {
        private final int itemId;
        private final int minAmount;
        private final int maxAmount;

        private Entry(int itemId, int minAmount, int maxAmount) {
            this.itemId = itemId;
            this.minAmount = Math.max(1, minAmount);
            this.maxAmount = Math.max(this.minAmount, maxAmount);
        }
    }
}
