package com.rs.game.player.content.pet;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.Player;
import lombok.Getter;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An enum containing all the pets and their info.
 *
 * @author Emperor
 */
public enum Pets {

    /**
     * A cat/kitten pet.
     */
    CAT(1555, 1561, 1567, 761, 768, 774, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_1(1556, 1562, 1568, 762, 769, 775, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_2(1557, 1563, 1569, 763, 770, 776, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_3(1558, 1564, 1570, 764, 771, 777, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_4(1559, 1565, 1571, 765, 772, 778, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_5(1560, 1566, 1572, 766, 773, 779, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), HELLCAT(7583, 7582, 7581, 3505, 3504, 3503, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927), CAT_7(14089, 14090, 15092, 8217, 8214, 8216, 0.0154320987654321, 0, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 1927),

    /**
     * A clockwork cat.
     */
    CLOCKWORK_CAT(7771, 7772, -1, 3598, -1, -1, 0.0, 0),

    // Reaper Pet
    TINY_DEATH(39569, 39570, -1, 24001, -1, -1, 0.0, 0),
    /**
     * The firemaker's curse pets.
     */
    SEARING_FLAME(22994, -1, -1, 14769, -1, -1, 0.0, 0), GLOWING_EMBER(22993, -1, -1, 14768, -1, -1, 0.0, 0), TWISTED_FIRESTARTER(22995, -1, -1, 14770, -1, -1, 0.0, 0), WARMING_FLAME(22992, -1, -1, 14767, -1, -1, 0.0, 0),

    /**
     * Troll baby pet.
     */
    TROLL_BABY(23030, 23030, -1, 14846, -1, -1, 0.0, 0),

    /**
     * A bulldog pet.
     */
    BULLDOG(12522, 12523, -1, 6969, 6968, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), BULLDOG_1(12720, 12721, -1, 7259, 7257, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), BULLDOG_2(12722, 12723, -1, 7260, 7258, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A dalmation pet.
     */
    DALMATIAN(12518, 12519, -1, 6964, 6965, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), DALMATIAN_1(12712, 12713, -1, 7249, 7250, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), DALMATIAN_2(12714, 12715, -1, 7251, 7252, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A greyhound pet.
     */
    GREYHOUND(12514, 12515, -1, 6960, 6961, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), GREYHOUND_1(12704, 12705, -1, 7241, 7242, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), GREYHOUND_2(12706, 12707, -1, 7243, 7244, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A labrador pet.
     */
    LABRADOR(12516, 12517, -1, 6962, 6963, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), LABRADOR_1(12708, 12709, -1, 7245, 7246, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), LABRADOR_2(12710, 12711, -1, 7247, 7248, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A sheepdog pet.
     */
    SHEEPDOG(12520, 12521, -1, 6966, 6967, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), SHEEPDOG_1(12716, 12717, -1, 7253, 7254, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), SHEEPDOG_2(12718, 12719, -1, 7255, 7256, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A terrier pet.
     */
    TERRIER(12512, 12513, -1, 6958, 6859, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), TERRIER_1(12700, 12701, -1, 7237, 7238, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526), TERRIER_2(12702, 12703, -1, 7239, 7240, -1, 0.0033333333333333, 4, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 526),

    /**
     * A creeping hand pet.
     */
    CREEPING_HAND(14652, -1, -1, 8619, -1, -1, 0.0033333333333333, 4),

    /**
     * Minitrice pet.
     */
    MINITRICE(14653, -1, -1, 8620, -1, -1, 0.0033333333333333, 4),

    /**
     * Baby basilisk pet.
     */
    BABY_BASILISK(14654, -1, -1, 8621, -1, -1, 0.0033333333333333, 4),

    /**
     * Baby kurask pet.
     */
    BABY_KURASK(14655, -1, -1, 8622, -1, -1, 0.0033333333333333, 4),

    /**
     * Abyssal minion pet.
     */
    ABYSSAL_MINION(14651, -1, -1, 8624, -1, -1, 0.0033333333333333, 4),

    /**
     * Rune guardian pets.
     */
    RUNE_GUARDIAN(15626, -1, -1, 9656, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_1(15627, -1, -1, 9657, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_2(15628, -1, -1, 9658, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_3(15629, -1, -1, 9659, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_4(15630, -1, -1, 9660, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_5(15631, -1, -1, 9661, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_6(15632, -1, -1, 9662, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_7(15633, -1, -1, 9663, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_8(15634, -1, -1, 9664, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_9(15635, -1, -1, 9665, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_10(15636, -1, -1, 9666, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_11(15637, -1, -1, 9667, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_12(15638, -1, -1, 9668, -1, -1, 0.0033333333333333, 4), RUNE_GUARDIAN_13(15639, -1, -1, 9669, -1, -1, 0.0033333333333333, 4),

    /**
     * Gecko pet.
     */
    GECKO(12488, 12489, -1, 6915, 6916, -1, 0.005, 10, 12125, 12127), GECKO_1(12738, 12742, -1, 7277, 7281, -1, 0.005, 10, 12125, 12127), GECKO_2(12739, 12743, -1, 7278, 7282, -1, 0.005, 10, 12125, 12127), GECKO_3(12740, 12744, -1, 7279, 7283, -1, 0.005, 10, 12125, 12127), GECKO_4(12741, 12745, -1, 7280, 7284, -1, 0.005, 10, 12125, 12127),

    /**
     * The platypus pet.
     */
    PLATYPUS(12551, 12548, -1, 7018, 7015, -1, 0.0046296296296296, 10, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 313, 12129), PLATYPUS_1(12552, 12549, -1, 7019, 7016, -1, 0.0046296296296296, 10, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 313, 12129), PLATYPUS_2(12553, 12550, -1, 7020, 7017, -1, 0.0046296296296296, 10, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 313, 12129),

    /**
     * The broav pet.
     */
    BROAV(14533, -1, -1, 8491, -1, -1, 0.0, 23, 2970),

    /**
     * The penguin pet.
     */
    PENGUIN(12481, 12482, -1, 6908, 6909, -1, 0.0046296296296296, 30, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), PENGUIN_1(12763, 12762, -1, 7313, 7314, -1, 0.0046296296296296, 30, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), PENGUIN_2(12765, 12764, -1, 7316, 7317, -1, 0.0046296296296296, 30, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),

    /**
     * A tooth creature pet.
     */
    TOOTH_CREATURE(18671, 18669, -1, 11411, 11413, -1, 0.075757575757576, 37, 1927, 1977),

    /**
     * A giant crab pet.
     */
    GIANT_CRAB(12500, 12501, -1, 6947, 6948, -1, 0.0069444444444444, 40, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), GIANT_CRAB_1(12746, 12747, -1, 7293, 7294, -1, 0.0069444444444444, 40, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), GIANT_CRAB_2(12748, 12749, -1, 7295, 7296, -1, 0.0069444444444444, 40, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), GIANT_CRAB_3(12750, 12751, -1, 7297, 7298, -1, 0.0069444444444444, 40, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270), GIANT_CRAB_4(12752, 12753, -1, 7299, 7300, -1, 0.0069444444444444, 40, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),

    /**
     * A Raven pet.
     */
    RAVEN(12484, 12485, -1, 6911, 6912, -1, 0.00698888, 50, 313, 12129), RAVEN_1(12724, 12725, -1, 7261, 7262, -1, 0.00698888, 50, 313, 12129), RAVEN_2(12726, 12727, -1, 7263, 7264, -1, 0.00698888, 50, 313, 12129), RAVEN_3(12728, 12729, -1, 7265, 7266, -1, 0.00698888, 50, 313, 12129), RAVEN_4(12730, 12731, -1, 7267, 7268, -1, 0.00698888, 50, 313, 12129), RAVEN_5(12732, 12733, -1, 7269, 7270, -1, 0.00698888, 50, 313, 12129),

    /**
     * A squirrel pet.
     */
    SQUIRREL(12490, 12491, -1, 6919, 6920, -1, 0.0071225071225071, 60, 12130), SQUIRREL_1(12754, 12755, -1, 7301, 7302, -1, 0.0071225071225071, 60, 12130), SQUIRREL_2(12756, 12757, -1, 7303, 7304, -1, 0.0071225071225071, 60, 12130), SQUIRREL_3(12758, 12759, -1, 7305, 7306, -1, 0.0071225071225071, 60, 12130), SQUIRREL_4(12760, 12761, -1, 7307, 7308, -1, 0.0071225071225071, 60, 12130),

    /**
     * Godbirds.
     */
    SARADOMIN_OWL(12503, 12504, 12505, 6949, 6950, 6951, 0.0069444444444444, 70, 313, 12129), ZAMORAK_HAWK(12506, 12507, 12508, 6952, 6953, 6954, 0.0069444444444444, 70, 313, 12129), GUTHIX_RAPTOR(12509, 12510, 12511, 6955, 6956, 6957, 0.0069444444444444, 70, 313, 12129),

    /**
     * Ex-ex parrot
     */
    EX_EX_PARROT(13335, -1, -1, 7844, -1, -1, 0.0, 71, 13379),

    /**
     * The phoenix eggling pets.
     */
    CUTE_PHOENIX_EGGLING(14627, -1, -1, 8578, -1, -1, 0.0, 72, 592), MEAN_PHOENIX_EGGLING(14626, -1, -1, 8577, -1, -1, 0.0, 72, 592),

    /**
     * A raccoon pet.
     */
    RACCOON(12486, 12487, -1, 6913, 6914, -1, 0.0029444444444444, 80, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 2132, 2134, 2136, 2138, 10816, 9986, 9978), RACCOON_1(12734, 12735, -1, 7271, 7272, -1, 0.0029444444444444, 80, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 2132, 2134, 2136, 2138, 10816, 9986, 9978), RACCOON_2(12736, 12737, -1, 7273, 7274, -1, 0.0029444444444444, 80, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270, 2132, 2134, 2136, 2138, 10816, 9986, 9978),

    /**
     * A sneaker peeper pet.
     */
    SNEAKER_PEEPER(19894, 19895, -1, 13089, 13090, -1, 0.05, 80, 221),

    /**
     * A vulture pet.
     */
    VULTURE(12498, 12499, -1, 6945, 6946, -1, 0.0078, 85, 313, 12129), VULTURE_1(12766, 12767, -1, 7319, 7320, -1, 0.0078, 85, 313, 12129), VULTURE_2(12768, 12769, -1, 7321, 7322, -1, 0.0078, 85, 313, 12129), VULTURE_3(12770, 12771, -1, 7323, 7324, -1, 0.0078, 85, 313, 12129), VULTURE_4(12772, 12773, -1, 7325, 7326, -1, 0.0078, 85, 313, 12129), VULTURE_5(12774, 12775, -1, 7327, 7328, -1, 0.0078, 85, 313, 12129),

    /**
     * A chameleon pet.
     */
    CHAMELEON(12492, 12493, -1, 6922, 6923, -1, 0.0069444444444444, 90, 12125),

    /**
     * A monkey pet.
     */
    MONKEY(12496, 12497, -1, 6942, 6943, -1, 0.0069444444444444, 95, 1963), MONKEY_1(12682, 12683, -1, 7210, 7211, -1, 0.0069444444444444, 95, 1963), MONKEY_2(12684, 12685, -1, 7212, 7213, -1, 0.0069444444444444, 95, 1963), MONKEY_3(12686, 12687, -1, 7214, 7215, -1, 0.0069444444444444, 95, 1963), MONKEY_4(12688, 12689, -1, 7216, 7217, -1, 0.0069444444444444, 95, 1963), MONKEY_5(12690, 12691, -1, 7218, 7219, -1, 0.0069444444444444, 95, 1963), MONKEY_6(12692, 12693, -1, 7220, 7221, -1, 0.0069444444444444, 95, 1963), MONKEY_7(12694, 12695, -1, 7222, 7223, -1, 0.0069444444444444, 95, 1963), MONKEY_8(12696, 12697, -1, 7224, 7225, -1, 0.0069444444444444, 95, 1963), MONKEY_9(12698, 12699, -1, 7226, 7227, -1, 0.0069444444444444, 95, 1963),

    /**
     * A baby dragon pet.
     */
    BABY_DRAGON(12469, 12470, -1, 6900, 6901, -1, 0.0052, 99, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),
    BABY_DRAGON_1(12471, 12472, -1, 6902, 6903, -1, 0.0052, 99, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),
    BABY_DRAGON_2(12473, 12474, -1, 6904, 6905, -1, 0.0052, 99, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),
    BABY_DRAGON_3(12475, 12476, -1, 6906, 6907, -1, 0.0052, 99, 2132, 2134, 2136, 2138, 10816, 9986, 9978, 321, 363, 341, 15264, 345, 377, 353, 389, 7944, 349, 331, 327, 395, 383, 317, 371, 335, 359, 15264, 15270),

    /**
     * TzRek-Jad pet.
     */
    TZREK_JAD(21512, -1, -1, 3604, -1, -1, 0.0, 99),


    LIL_GOOP(44488, -1, -1, 26007, -1, -1, 0.0, 1),

    /**
     * Seasonal pets
     */
    SPARKLES(22973, -1, -1, 2267, -1, -1, 0.0, 1),
    SANTAR_SPAWNLING(30380, -1, -1, 18822, -1, -1, 0.0, 1),
    MACKERS(22443, -1, -1, 14686, -1, -1, 0.0, 1),

    PET_OF_SPRING(35777, -1, -1, 21795, -1, -1, 0.0, 1),
    PET_OF_SUMMER(35778, -1, -1, 21797, -1, -1, 0.0, 1),
    PET_OF_AUTUMN(35779, -1, -1, 21799, -1, -1, 0.0, 1),
    PET_OF_WINTER(35780, -1, -1, 21801, -1, -1, 0.0, 1),

    /**
     * Companion pets
     */
    GOLDEN_CHINCHOMPA(29438, 29439, 29440, 18207, 18208, 18209, 0.0069444444444444, 1),
    FIMBERZIZZ(34109, -1, -1, 20603, -1, -1, 0.0, 1),
    VORAGO_SHARD(28685, -1, -1, 17397, -1, -1, 0.0, 1),
    DAWN(30062, -1, -1, 18570, -1, -1, 0.0, 1),
    DUSK(30065, -1, -1, 18572, -1, -1, 0.0, 1),
    ELVIS_PRESSIE(30369, -1, -1, 18820, -1, -1, 0.0, 1),
    DRAKEFIRE_WARSHIP(30909, -1, -1, 19092, -1, -1, 0.0, 1),
    INARI(31304, -1, -1, 13609, -1, -1, 0.0, 1),
    BRIGHTFLAME_ANCIENT(31307, -1, -1, 13611, -1, -1, 0.0, 1),
    ZENN(31656, -1, -1, 18327, -1, -1, 0.0, 1),
    DOUGLAS(36209, -1, -1, 22059, -1, -1, 0.0, 1),
    FIRE_LYCAN(40738, -1, -1, 24791, -1, -1, 0.0, 1),
    ISHARA(40273, -1, -1, 24525, -1, -1, 0.0, 1),
    SPRINGY(40525, -1, -1, 24758, -1, -1, 0.0, 1),
    /**
     * Follower pets
     */
    PIGGLES(30233, -1, -1, 18625, -1, -1, 0.0, 1),
    TINY_LUCIEN(31025, -1, -1, 19135, -1, -1, 0.0, 1),
    KIPPLE_NANO(33566, -1, -1, 20404, -1, -1, 0.0, 1),
    TINY_HAZEEL(34196, -1, -1, 20762, -1, -1, 0.0, 1),
    SAYLN_THE_SHARK(34753, -1, -1, 21015, -1, -1, 0.0, 1),
    //	CRISPY_THE_CABBAGE(37591, -1, -1, 22503, -1, -1, 0.0, 1),
    TIM(37725, -1, -1, 2707, -1, -1, 0.0, 1),
    CRUNCHY(37727, -1, -1, 22957, -1, -1, 0.0, 1),
    //	LAGO(-1, -1, -1, 23493, -1, -1, 0.0, 1),
    IDOL_CRABLETINE(38834, -1, -1, 23388, -1, -1, 0.0, 1),
    TINY_KHARSHAI(39252, -1, -1, 23699, -1, -1, 0.0, 1),
    TINY_SLISKE(39448, -1, -1, 23874, -1, -1, 0.0, 1),
    STRINGY(39390, -1, -1, 3348, -1, -1, 0.0, 1),
    SHADOW_GORILLA(39558, -1, -1, 24000, -1, -1, 0.0, 1),
    JUNGLE_GORILLA(39557, -1, -1, 23993, -1, -1, 0.0, 1),

    /**
     * Misc pets
     */
    TWITCHING_ORB(40679, -1, -1, 24778, -1, -1, 0.0, 1),
    CRESBOT(27490, -1, -1, 17034, -1, -1, 0.0, 1),
    GOEBIE(35215, -1, -1, 21400, -1, -1, 0.0, 1),
    CORGI(24387, -1, -1, 15511, -1, -1, 0.0, 1),
    CORNELLIUS(34242, -1, -1, 20766, -1, -1, 0.0, 1),
    MEGA_DUCKLINGS(33829, -1, -1, 20555, -1, -1, 0.0, 1),
    TINY_ZEMOUREGAL(34478, -1, -1, 20860, -1, -1, 0.0, 1),
    EFFY(42459, -1, -1, 25456, -1, -1, 0.0, 1),
    /**
     * Promotional pets
     */
    RUNEFEST_DRAG(35862, -1, -1, 21817, -1, -1, 0.0, 1),
    ROWENA(32512, -1, -1, 20227, -1, -1, 0.0, 1),
    POSTIE_PETE(36138, -1, -1, 22037, -1, -1, 0.0, 1),
    JAGUAR_CUB(35255, -1, -1, 21419, -1, -1, 0.0, 1),
    TIGER_CUB(35261, -1, -1, 21422, -1, -1, 0.0, 1),
    LION_CUB(35257, -1, -1, 21425, -1, -1, 0.0, 1),
    SNOW_LEOPARD_CUB(35259, -1, -1, 21428, -1, -1, 0.0, 1),

    /**
     * GWD2, RoTs, and raids pets
     */
    TESS(37679, -1, -1, 22925, -1, -1, 0.0, 99),
    VINDIDDY(37180, -1, -1, 22507, -1, -1, 0.0, 99),
    RAWRVEK(37181, -1, -1, 22508, -1, -1, 0.0, 99),
    LILWYR(37182, -1, -1, 22509, -1, -1, 0.0, 99),
    GREG(37183, -1, -1, 22512, -1, -1, 0.0, 99),
    NYLESSA(37184, -1, -1, 22510, -1, -1, 0.0, 99),
    AVA(37185, -1, -1, 22511, -1, -1, 0.0, 99),

    AHRIM(30031, -1, -1, 18532, -1, -1, 0.0, 99),
    DHAROK(30032, -1, -1, 18533, -1, -1, 0.0, 99),
    GUTHAN(30033, -1, -1, 18534, -1, -1, 0.0, 99),
    KARIL(30034, -1, -1, 18535, -1, -1, 0.0, 99),
    TORAG(30035, -1, -1, 18536, -1, -1, 0.0, 99),
    VERAC(30036, -1, -1, 18537, -1, -1, 0.0, 99),

    TUZZY(35217, -1, -1, 21402, -1, -1, 0.0, 99),
    KRARJNR(35219, -1, -1, 21404, -1, -1, 0.0, 99),
    YAKAMINU(39671, -1, -1, 18033, -1, -1, 0.0, 99),
    REEVES(39624, -1, -1, 24021, -1, -1, 0.0, 99),

    /**
     * Treasure hunter pets and exclusives
     */
    TUMBLEWEED(28017, -1, -1, 17086, -1, -1, 0.0, 1),
    SQIRKS(28018, -1, -1, 17087, -1, -1, 0.0, 1),
    SKULLY(28019, -1, -1, 17088, -1, -1, 0.0, 1),
    HYPE_TRAIN(37132, -1, -1, 22503, -1, -1, 0.0, 1),
    PUMPKINCROW(35944, -1, -1, 21980, -1, -1, 0.0, 1),
    HELPS(36777, -1, -1, 22161, -1, -1, 0.0, 1),
    KHARIDIAN_CAT(35908, -1, -1, 21943, -1, -1, 0.0, 1),
    MIMIC(30813, -1, -1, 22164, -1, -1, 0.0, 1),
    BUDDY(33631, -1, -1, 17197, -1, -1, 0.0, 1),
    ARMADYL_BANNER(34909, -1, -1, 21126, -1, -1, 0.0, 1),
    GODLESS_BANNER(34911, -1, -1, 21128, -1, -1, 0.0, 1),
    SARADOMIN_BANNER(34905, -1, -1, 21122, -1, -1, 0.0, 1),
    ZAMORAK_BANNER(34907, -1, -1, 21124, -1, -1, 0.0, 1),
    //WINSTON(34932, -1, -1, 21132, -1, -1, 0.0, 1), Missing walk animation
    PIRATE_SHEEP(32150, -1, -1, 19724, -1, -1, 0.0, 1),
    SCURVY(39531, -1, -1, 23977, -1, -1, 0.0, 1),

    /**
     * Legendary pets
     */

    BLAZEHOUND(26567, -1, -1, 16662, -1, -1, 0.0, 1),
    SKYPOUNCER(26568, -1, -1, 16665, 16664, 16665, 0.0, 1),
    BLOODPOUNCER(26569, -1, -1, 16668, 16667, 16668, 0.0, 1),
    DRAGON_WOLF(27215, -1, -1, 4664, 16734, 16735, 0.0, 1),
    WARBORN_BEHEMOTH(28833, -1, -1, 17788, 17787, 17788, 0.0, 1),
    PROTOTYPE_COLOSSUS(28828, -1, -1, 17781, 17781, 17782, 0.0, 1),
    RORY_THE_REINDEER(30368, -1, -1, 18815, 18815, 18816, 0.0, 1),
    SHADOW_DRAKE(34786, -1, -1, 21034, 21034, 21036, 0.0, 1),
    FIRE_DRAKE(34785, -1, -1, 21033, 21035, 21037, 0.0, 1),

    /**
     * Co-op slayer pets
     */
    SQUIDGE(24511, -1, -1, 15633, -1, -1, 0.0, 1),

    FREEZY_ICE(24512, -1, -1, 15634, -1, -1, 0.0, 1), FREEZY_SAND(30746, -1, -1, 18915, -1, -1, 0.0,
            1), FREEZY_JUNGLE(30747, -1, -1, 18916, -1, -1, 0.0, 1), FREEZY_LAVA(30748, -1, -1, 18917, -1, -1, 0.0, 1),

    RUNTSTABLE(30749, -1, -1, 18921, -1, -1, 0.0, 1),

    FROSTY(31459, -1, -1, 14969, -1, -1, 0.0, 1),
    MINI_BLINK(31457, -1, -1, 14967, -1, -1, 0.0, 1),
    HOPE_NIBBLER(31458, -1, -1, 14968, -1, -1, 0.0, 1),

    /**
     * Skilling pets
     */
    AGILITY(38075, -1, -1, 23176, -1, -1, 0.0, 1),
    CONSTRUCTION(38077, -1, -1, 23178, -1, -1, 0.0, 1),
    COOKING(38079, -1, -1, 23180, -1, -1, 0.0, 1),
    CRAFTING(38081, -1, -1, 23182, -1, -1, 0.0, 1),
    DIVINATION(38083, -1, -1, 23184, -1, -1, 0.0, 1),
    DUNGEONEERING(38085, -1, -1, 23186, -1, -1, 0.0, 1),
    FARMING(38087, -1, -1, 23188, -1, -1, 0.0, 1),
    FIREMAKING(38089, -1, -1, 23190, -1, -1, 0.0, 1),
    FISHING(38091, -1, -1, 23192, -1, -1, 0.0, 1),
    FLETCHING(38093, -1, -1, 23194, -1, -1, 0.0, 1),
    HERBLORE(38095, -1, -1, 23196, -1, -1, 0.0, 1),
    HUNTER(38097, -1, -1, 23198, -1, -1, 0.0, 1),
    INVENTION(38099, -1, -1, 23200, -1, -1, 0.0, 1),
    MINING(38101, -1, -1, 23202, -1, -1, 0.0, 1),
    RUNECRAFTING(38103, -1, -1, 23204, -1, -1, 0.0, 1),
    SLAYER(38105, -1, -1, 23206, -1, -1, 0.0, 1),
    SMITHING(38107, -1, -1, 23208, -1, -1, 0.0, 1),
    THIEVING(38109, -1, -1, 23210, -1, -1, 0.0, 1),
    WOODCUTTING(38111, -1, -1, 23212, -1, -1, 0.0, 1),
    CONSTITUTION(48346, -1, -1, 25023, -1, -1, 0.0, 1),
    ATTACK(48340, -1, -1, 25017, -1, -1, 0.0, 1),
    STRENGTH(48342, -1, -1, 25019, -1, -1, 0.0, 1),
    DEFENCE(48344, -1, -1, 25021, -1, -1, 0.0, 1),
    RANGED(48348, -1, -1, 25025, -1, -1, 0.0, 1),
    PRAYER(48350, -1, -1, 25027, -1, -1, 0.0, 1),
    MAGIC(48352, -1, -1, 25029, -1, -1, 0.0, 1),
    SUMMONING(48354, -1, -1, 25031, -1, -1, 0.0, 1),

    /**
     * Boss Pets.
     */
    DAVE(31748, -1, -1, 19475, -1, -1, 0.0, 99),
    STEVE(31749, -1, -1, 19476, -1, -1, 0.0, 99),
    PETE(31750, -1, -1, 19477, -1, -1, 0.0, 99),
    GAVIN(31751, -1, -1, 19478, -1, -1, 0.0, 99),
    LANA(31752, -1, -1, 19479, -1, -1, 0.0, 99),
    BILL(31753, -1, -1, 19480, -1, -1, 0.0, 99),
    DAG_PRIME(33826, -1, -1, 20552, -1, -1, 0.0, 99),
    DAG_SUPREME(33828, -1, -1, 20554, -1, -1, 0.0, 99),
    DAG_REX(33827, -1, -1, 20553, -1, -1, 0.0, 99),
    CHAOS_ELLIE(33811, -1, -1, 20537, -1, -1, 0.0, 99),
    CORP_BEAST(33812, -1, -1, 20538, -1, -1, 0.0, 99),
    KBD(33818, -1, -1, 20544, -1, -1, 0.0, 99),
    NEX(33808, -1, -1, 20534, -1, -1, 0.0, 99),
    BANDOS(33806, -1, -1, 20532, -1, -1, 0.0, 99),
    ZAMMY(33805, -1, -1, 20531, -1, -1, 0.0, 99),
    SARA(33807, -1, -1, 20533, -1, -1, 0.0, 99),
    ARMA(33804, -1, -1, 20530, -1, -1, 0.0, 99),
    BARRY(33809, -1, -1, 20535, -1, -1, 0.0, 99),
    MALLORY(33810, -1, -1, 20536, -1, -1, 0.0, 99),
    MOLLY(33813, -1, -1, 20539, -1, -1, 0.0, 99),
    SHRIMPY(33814, -1, -1, 20540, -1, -1, 0.0, 99),
    KALPHITE_KING(33815, -1, -1, 20541, -1, -1, 0.0, 99),
    QBD(33825, -1, -1, 20551, -1, -1, 0.0, 99),
    VITALIS(28630, -1, -1, 17186, -1, -1, 0.0, 99),
    BOMBI(33717, -1, -1, 20461, -1, -1, 0.0, 99),
    LEGIO_PRIMUS(33819, -1, -1, 20545, -1, -1, 0.0, 99),
    LEGIO_SECUNDUS(33820, -1, -1, 20546, -1, -1, 0.0, 99),
    LEGIO_TERTIUS(33821, -1, -1, 20547, -1, -1, 0.0, 99),
    LEGIO_QUARTUS(33822, -1, -1, 20548, -1, -1, 0.0, 99),
    LEGIO_QUINTUS(33823, -1, -1, 20549, -1, -1, 0.0, 99),
    LEGIO_SEXTUS(33824, -1, -1, 20550, -1, -1, 0.0, 99),
    KALPHITE_GRUBLET_I(33816, -1, -1, 20542, -1, -1, 0.0, 99),
    KALPHITE_GRUBLET_II(33817, -1, -1, 20543, -1, -1, 0.0, 99),
    ENDURING_GLACYTE(41380, -1, -1, 14304, -1, -1, 0.0, 99),
    SAPPING_GLACYTE(41379, -1, -1, 14303, -1, -1, 0.0, 99),
    UNSTABLE_GLACYTE(41381, -1, -1, 14302, -1, -1, 0.0, 99),
    EDDY(32730, -1, -1, 20292, -1, -1, 0.0, 99),
    ENTLING(41078, -1, -1, 7709, -1, -1, 0.0, 1),
    CHEERLEADER(1842, -1, -1, 20390, -1, -1, 0.0, 1),
    THE_MINISTER(40669, -1, -1, 24772, -1, -1, 0.0, 99), 
    KURORYU(43051, -1, -1, 25617, -1,-1, 0.0, 1)
    ;


    // int babyItemId, int grownItemId, int overgrownItemId,
    // int babyNpcId, int grownNpcId, int overgrownNpcId,
    // double growthRate, int summoningLevel, int... food

    // Shrimpy is a miniature version of Har-aken that can be unlocked as a pet
    // from a Volcanic shard,
    // which is obtained as a reward from killing Har-aken. The odds of
    // receiving this item are 1 in 200

    /**
     * The baby pets mapping.
     */
    @Getter
    private static final Map<Integer, Pets> babyPets = new HashMap<Integer, Pets>();

    /**
     * The grown pets mapping.
     */
    @Getter
    private static final Map<Integer, Pets> grownPets = new HashMap<Integer, Pets>();

    /**
     * The overgrown pets mapping.
     */
    @Getter
    private static final Map<Integer, Pets> overgrownPets = new HashMap<Integer, Pets>();

    /**
     * A set of every single item ID.
     */
    @Getter
    private static final Set<Integer> allPetIds = new HashSet<>();

    /**
     * Populates the mappings.
     */
    static {
        for (Pets pet : Pets.values()) {
            babyPets.put(pet.babyItemId, pet);
            if (pet.grownItemId > 0) {
                grownPets.put(pet.grownItemId, pet);
                if (pet.getOvergrownItemId() > 0)
                    overgrownPets.put(pet.overgrownItemId, pet);
            }
        }
        allPetIds.addAll(babyPets.keySet());
        allPetIds.addAll(grownPets.keySet());
        allPetIds.addAll(overgrownPets.keySet());
    }

    /**
     * The baby item id.
     */
    private final int babyItemId;

    /**
     * The grown pet's item id.
     */
    private final int grownItemId;
    /**
     * The overgrown pet's item id.
     */
    private final int overgrownItemId;
    /**
     * The baby pet NPC id.
     */
    private final int babyNpcId;
    /**
     * The grown pet NPC id.
     */
    private final int grownNpcId;
    /**
     * The overgrown pet NPC id.
     */
    private final int overgrownNpcId;
    /**
     * The growth rate.
     */
    private final double growthRate;
    /**
     * The summoning level required.
     */
    private final int summoningLevel;
    /**
     * The food this pet uses.
     */
    private final int[] food;

    /**
     * Constructs a new {@code Pets} {@code Object}.
     *
     * @param babyItemId The baby pet item id.
     * @param grownItemId The grown pet item id.
     * @param overgrownItemId The overgrown item id.
     * @param babyNpcId The baby pet npc id.
     * @param grownNpcId The grown pet npc id.
     * @param overgrownNpcId The overgrown npc id.
     * @param growthRate The growth rate (amount to increase growth with every tick).
     * @param summoningLevel The summoning level required.
     * @param food The food item ids the pet uses.
     */
    Pets(int babyItemId, int grownItemId, int overgrownItemId, int babyNpcId, int grownNpcId,
         int overgrownNpcId, double growthRate, int summoningLevel, int... food) {
        this.babyItemId = babyItemId;
        this.grownItemId = grownItemId;
        this.overgrownItemId = overgrownItemId;
        this.babyNpcId = babyNpcId;
        this.grownNpcId = grownNpcId;
        this.overgrownNpcId = overgrownNpcId;
        this.growthRate = growthRate;
        this.summoningLevel = summoningLevel;
        this.food = food;
    }

    /**
     * Gets the pet object for the item id.
     *
     * @param itemId The item id.
     * @return The pet object.
     */
    public static Pets forId(int itemId) {
        Pets pet = babyPets.get(itemId);
        if (pet == null) {
            pet = grownPets.get(itemId);
            if (pet == null)
                return overgrownPets.get(itemId);
            return pet;
        }
        return pet;
    }

    /**
     * Checks if a player has a pet.
     *
     * @param player The player.
     * @return {@code True} if so.
     */
    public static boolean hasPet(Player player) {
        for (int itemId : babyPets.keySet()) {
            if (player.getInventory().containsOneItem(itemId))
                return true;
        }
        for (int itemId : grownPets.keySet()) {
            if (player.getInventory().containsOneItem(itemId))
                return true;
        }
        for (int itemId : overgrownPets.keySet()) {
            if (player.getInventory().containsOneItem(itemId))
                return true;
        }
        return false;
    }

    /**
     * Gets the babyItemId.
     *
     * @return The babyItemId.
     */
    public int getBabyItemId() {
        return babyItemId;
    }

    /**
     * Gets the babyNpcId.
     *
     * @return The babyNpcId.
     */
    public int getBabyNpcId() {
        return babyNpcId;
    }

    /**
     * Gets the food.
     *
     * @return The food.
     */
    public int[] getFood() {
        return food;
    }

    /**
     * Gets the grownItemId.
     *
     * @return The grownItemId.
     */
    public int getGrownItemId() {
        return grownItemId;
    }

    /**
     * Gets the grownNpcId.
     *
     * @return The grownNpcId.
     */
    public int getGrownNpcId() {
        return grownNpcId;
    }

    /**
     * Gets the growthRate.
     *
     * @return The growthRate.
     */
    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * Gets the item id for this pet.
     *
     * @param stage The stage of the pet.
     * @return The item id.
     */
    public int getItemId(int stage) {
        switch (stage) {
            case 0:
                return babyItemId;
            case 1:
                return grownItemId;
            case 2:
                return overgrownItemId;
        }
        return 0;
    }

    /**
     * Gets the NPC id for this pet.
     *
     * @param stage The stage of the pet.
     * @return The NPc id.
     */
    public int getNpcId(int stage) {
        switch (stage) {
            case 0:
                return babyNpcId;
            case 1:
                return grownNpcId;
            case 2:
                return overgrownNpcId;
        }
        return 0;
    }

    /**
     * Gets the overgrownItemId.
     *
     * @return The overgrownItemId.
     */
    public int getOvergrownItemId() {
        return overgrownItemId;
    }

    /**
     * Gets the overgrownNpcId.
     *
     * @return The overgrownNpcId.
     */
    public int getOvergrownNpcId() {
        return overgrownNpcId;
    }

    /**
     * Gets the summoningLevel.
     *
     * @return The summoningLevel.
     */
    public int getSummoningLevel() {
        return summoningLevel;
    }

    /**
     * returns the name from the stage
     *
     * @return
     */
    public String getName() {
        val def = ItemDefinitions.getItemDefinitions(babyItemId);
        if (def == null) {
            return "NULL";
        }
        return def.getName();
    }
}