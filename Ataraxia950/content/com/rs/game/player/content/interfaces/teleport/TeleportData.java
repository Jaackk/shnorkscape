package com.rs.game.player.content.interfaces.teleport;

import com.rs.game.WorldTile;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Paolo, Discord Shnek#6969
 * 27/06/2019
 */
public enum TeleportData {
       SKILLING(new TeleportLocation[]  {
               new TeleportLocation("Dungeoneering", new WorldTile(3971,5560,0)) ,
               new TeleportLocation("Ardougne thieving", new WorldTile(2662, 3303,0)),
               new TeleportLocation("Dark Animica Rocks", new WorldTile(5613,2168,0)),
               new TeleportLocation("Seers woodcutting", new WorldTile(2725, 3491,0)),
               new TeleportLocation("Gnome Agility", new WorldTile(2468, 3437,0)),
               new TeleportLocation("Barbarian Agility", new WorldTile(2552, 3557,0)),
               new TeleportLocation("Drakolith & Orichalcite Rocks", new WorldTile(5650,2481)),
               new TeleportLocation("Light Animica Rocks", new WorldTile(5339,2259)),
               new TeleportLocation("Fishing guild", new WorldTile(2592,3420)),
               new TeleportLocation("Deep Sea Fishing", new WorldTile(2595,3411)),
               new TeleportLocation("Varock-west bank", new WorldTile(3185,3436)),
               new TeleportLocation("Living Rock Cavern", new WorldTile(3014, 9831)), //fishing teles
               new TeleportLocation("Priffidinas fishing area", new WorldTile(2265, 3403, 1)),
               new TeleportLocation("Piscatoris fishing colony", new WorldTile(2330, 3690)),
               new TeleportLocation("Al-Kharid mining", new WorldTile(3300, 3312)),
               new TeleportLocation("Lumbridge mining", new WorldTile(3229,3150)),
               new TeleportLocation("Phasmatite Rocks", new WorldTile(3692, 3399)), //mining teles
               new TeleportLocation("Red Sandstone", new WorldTile(2590, 2880)),
               new TeleportLocation("Luminite rocks", new WorldTile(5335, 2394)), //mining teles
               new TeleportLocation("Mining Guild", new WorldTile(3022, 3337)),
               new TeleportLocation("Necrite Rocks", new WorldTile(3463, 3139)),
               new TeleportLocation("Rune Essence Mine", new WorldTile(2911, 4832)),
               new TeleportLocation("Banite Rocks", new WorldTile(2718, 3872)),
               new TeleportLocation("Priffindinas Seren Stones", new WorldTile(2220, 3298, 1)),
               new TeleportLocation("Priffindinas Ore Mine", new WorldTile(2215, 3324, 1)),
               new TeleportLocation("South-east Varrock", new WorldTile(3283, 3371)), //mine
               new TeleportLocation("Wilderness Agility Course", new WorldTile(2998, 3911)),
               new TeleportLocation("Agility Pyramid", new WorldTile(3358, 2827)),
               new TeleportLocation("Prifindinas Agility Course", new WorldTile(2177, 3398, 1)),
               new TeleportLocation("Jungle", new WorldTile(2818, 3082)), //Woodcutting teles
               new TeleportLocation("Isafdar", new WorldTile(2293, 3142)), //Woodcutting teles
               new TeleportLocation("South of Varrock", new WorldTile(3258, 3369)), //Woodcutting teles
               new TeleportLocation("Priffdinas", new WorldTile(2248, 3382, 1)), //Woodcutting teles
               //new TeleportLocation("Runespan", new WorldTile(3994, 6105, 1), "RunespanController"),
               new TeleportLocation("Abyss", new WorldTile(3039, 4834)),
               new TeleportLocation("Summoning", new WorldTile(2923, 3449)),
               new TeleportLocation("Falador Herb Patch", new WorldTile(3054, 3309)),
               new TeleportLocation("Catherby Herb Patch", new WorldTile(2785, 3464)),
               new TeleportLocation("Ardougne Herb Patch", new WorldTile(2664, 3374)),
               new TeleportLocation("Port Phasmatys Herb Patch", new WorldTile(3599, 3523)),
               new TeleportLocation("Priffdinas Herb Patch", new WorldTile(2250, 3383, 1)),
               new TeleportLocation("Taverly Tree Patch", new WorldTile(2886, 3460)),
               new TeleportLocation("Varrock Tree Patch", new WorldTile(3227, 3464)),
               new TeleportLocation("Falador Tree Patch", new WorldTile(3004, 3377)),
               new TeleportLocation("Gnome Stronghold Tree Patch", new WorldTile(2432, 3418)),
               new TeleportLocation("Lumbridge Tree Patch", new WorldTile(3226, 3248)),
               new TeleportLocation("Priffdinas Tree Patch", new WorldTile(2230, 3301, 1)),
               new TeleportLocation("Catherby Fruit Tree Patch", new WorldTile(2856, 3432)),
               new TeleportLocation("Gnome Stronghold Fruit Tree Patch", new WorldTile(2491, 3177)),
               new TeleportLocation("Tree Gnome Fruit Tree Patch", new WorldTile(2475, 3447)),
               new TeleportLocation("Lletya Fruit Tree Patch", new WorldTile(2346, 3164)),
               new TeleportLocation("Karamja Fruit Tree Patch", new WorldTile(2765, 3209)),
               new TeleportLocation("Priffdinas Fruit Tree Patch", new WorldTile(2217, 3433, 1)),
               new TeleportLocation("Falconry", new WorldTile(2363, 3624)),
               new TeleportLocation("Feldip Hills", new WorldTile(2525, 2917)),
               new TeleportLocation("Puro Puro", new WorldTile(2591, 4318)),
               new TeleportLocation("Isafdar", new WorldTile(2254, 3183)), //Hunting Teles
               new TeleportLocation("Tree Gnome Stronghold Hunter Area", new WorldTile(2457, 3538)),
               new TeleportLocation("Port Phasmatys Hunter Area", new WorldTile(3660, 3429)),
               new TeleportLocation("Rellekka Hunter Area", new WorldTile(2729, 3864)),
               new TeleportLocation("Desert Quarry Hunter Area", new WorldTile(3169, 2866)),
               new TeleportLocation("Priffdinas Hunter Area", new WorldTile(2235, 3422, 1)),
               new TeleportLocation("Crafting Guild", new WorldTile(2933, 3289)),
               new TeleportLocation("Prifddinas Crafting Area", new WorldTile(2145, 3340, 1)),
               new TeleportLocation("Prifddinas Prayer Area", new WorldTile(2191, 3444, 1)),
               new TeleportLocation("Pale Wisp", new WorldTile(3128, 3216)),
               new TeleportLocation("Flickering Wisp", new WorldTile(3003, 3404)),
               new TeleportLocation("Bright Wisp", new WorldTile(3309, 3399)),
               new TeleportLocation("Glowing Wisp", new WorldTile(2731, 3419)),
               new TeleportLocation("Sparkling Wisp", new WorldTile(2777, 3598)),
               new TeleportLocation("Gleaming Wisp", new WorldTile(2887, 3041)),
               new TeleportLocation("Vibrant Wisp", new WorldTile(2418, 2860)),
               new TeleportLocation("Lustrous Wisp", new WorldTile(3460, 3539)),
               new TeleportLocation("Brilliant Wisp", new WorldTile(3403, 3300)),
               new TeleportLocation("Radiant Wisp", new WorldTile(3800, 3551)),
               new TeleportLocation("Luminous Wisp", new WorldTile(3309, 2651)),
               new TeleportLocation("Incandescant Wisp", new WorldTile(2284, 3053)),
               new TeleportLocation("Invention Guild", new WorldTile(2997, 3437, 0)),
        }),
        BOSSES(new TeleportLocation[]  {
                new TeleportLocation("Godwars", new WorldTile(2916, 3746, 0)) ,
                new TeleportLocation("Queen Black Dragon", new WorldTile(1197, 6499)) ,
                new TeleportLocation("King Black Dragon", new WorldTile(3067, 10254)) ,
                new TeleportLocation("Kalphite Queen", new WorldTile(3480, 9488, 0)) ,
                new TeleportLocation("Dagannoth Kings", new WorldTile(1914, 4368)) ,
                new TeleportLocation("Corporeal Beast", new WorldTile(2966, 4383, 2)) ,
                new TeleportLocation("Bork", new WorldTile(3143, 5545)) ,
                new TeleportLocation("Barrelchest", new WorldTile(3803, 2844)) ,
                new TeleportLocation("Chaos Elemental", new WorldTile(3251, 3915), true) ,
                new TeleportLocation("Rise of the Six", new WorldTile(3540, 3308)) ,
                new TeleportLocation("Tormented Demons", new WorldTile(2571, 5735)) ,
                new TeleportLocation("Kalphite King", new WorldTile(2974, 1654)) ,
                new TeleportLocation("Vorago", new WorldTile(2972, 3430)) ,
                new TeleportLocation("The Heart", new WorldTile(3199, 6939, 1), "HeartOfGielinorController") ,
                new TeleportLocation("Araxyte Hive", new WorldTile(4512, 6289, 1)) ,
                new TeleportLocation("Giant Mole", new WorldTile(2985, 3382)) ,
               new TeleportLocation("The Lost Grove, Solak", new WorldTile(1374, 5538)) ,
                new TeleportLocation("The Magister", new WorldTile(2464, 6729, 1), SophanemSlayerDungeon.class.getSimpleName()) ,
        }),
        MONSTERS(new TeleportLocation[]  {
                new TeleportLocation("Cows", new WorldTile(3259, 3263)) ,
                new TeleportLocation("Yaks", new WorldTile(2324, 3798)) ,
                new TeleportLocation("Rock crabs", new WorldTile(2680, 3719)) ,
                new TeleportLocation("Experiments", new WorldTile(3559,9947)) ,
                new TeleportLocation("Desert bandits", new WorldTile(3163,2984)) ,
                new TeleportLocation("Chaos Druids", new WorldTile(2928,9844)) ,
                new TeleportLocation("Frost dragons", new WorldTile(3033, 9597)) ,
                new TeleportLocation("Dwarf Battlefield", new WorldTile(1519, 4704)),
                new TeleportLocation("Glacors", new WorldTile(4181,5726)),
                new TeleportLocation("Rune dragons", new WorldTile(2367, 3357, 0))


        }),
       MINIGAMES(new TeleportLocation[]  {
                new TeleportLocation("Fight Caves", new WorldTile(4612, 5129, 0)) ,
                new TeleportLocation("Clan Wars", new WorldTile(2993, 9679, 0)) ,
               new TeleportLocation("Barrows", new WorldTile(3564, 3288)) ,
               new TeleportLocation("Clan Wars", new WorldTile(2994, 9679)) ,
               new TeleportLocation("Pest Control", new WorldTile(2652, 2655)) ,
               new TeleportLocation("Fight Kiln", new WorldTile(4742, 5169)) ,
               new TeleportLocation("Recipe for Disaster", new WorldTile(5013, 744, 1)) ,
               new TeleportLocation("Duel Arena", new WorldTile(3326, 3232)) ,
               new TeleportLocation("Warrior's Guild", new WorldTile(2878, 3543)) ,
               new TeleportLocation("Soul Wars", new WorldTile(3081, 3475)) ,
               new TeleportLocation("Dominion Tower", new WorldTile(3367, 3083)) ,
               new TeleportLocation("Artisan's Workshop", new WorldTile(3031, 3339)) ,
               new TeleportLocation("Temple of Aminishi", new WorldTile(2094, 11348, 0), "EliteDungeonsLobby") ,
                new TeleportLocation("Dragonkin Laboratory", new WorldTile(3371, 3885, 0), "EliteDungeonsLobby") ,
                new TeleportLocation("Shadow reef", new WorldTile(3511, 3693, 0), "EliteDungeonsLobby") ,
        }),
        SLAYER(new TeleportLocation[]  {
                new TeleportLocation("Kuradal's Dungeon", new WorldTile(1685, 5287, 1)) ,
                new TeleportLocation("Jadinko Lair", new WorldTile(3012, 9275)) ,
                new TeleportLocation("Polypore Dungeon", new WorldTile(4626, 5457)) ,
                new TeleportLocation("Slayer Tower", new WorldTile(3423, 3544)) ,
                new TeleportLocation("Ancient Cavern", new WorldTile(1763, 5365, 1)) ,
                new TeleportLocation("Brimhaven Dungeon", new WorldTile(2698, 9564)) ,
                new TeleportLocation("Fremennik Dungeon", new WorldTile(2807, 10003)) ,
                new TeleportLocation("Taverly Dungeon", new WorldTile(2885, 9800)) ,
                new TeleportLocation("Jungle Strykewyrms", new WorldTile(2453, 2912)) ,
                new TeleportLocation("Desert Strykewyrms", new WorldTile(3355, 3161)) ,
                new TeleportLocation("Ice Strykewyrms", new WorldTile(3436, 5648)) ,
                new TeleportLocation("Celestial Dragons", new WorldTile(2284, 5972)) ,
                new TeleportLocation("Kal'gerion Dungeon", new WorldTile(1311, 1312)) ,
                new TeleportLocation("Guthix Cave", new WorldTile(1814, 5985)) ,
                new TeleportLocation("Nihils", new WorldTile(4061, 6248)) ,
                new TeleportLocation("Muspahs", new WorldTile(4267, 6319)) ,
                new TeleportLocation("Camel Warriors", new WorldTile(3384, 2723)) ,
                new TeleportLocation("Ripper Demons", new WorldTile(5160, 7590)) ,
                new TeleportLocation("Gemstone Dragons", new WorldTile(2837, 9396)) ,
                new TeleportLocation("Crystal Shapeshifters", new WorldTile(4116, 6571)) ,
                new TeleportLocation("Dagannoths", new WorldTile(2485, 10146)) ,
                new TeleportLocation("Sophanem Slayer Dungeon", new WorldTile(3285, 2744)) ,
                new TeleportLocation("Edimmu", new WorldTile(2233, 3396, 1)) ,
                new TeleportLocation("Ascension Dungeon", new WorldTile(2501, 2886)) ,
        });
        


       @Getter @Setter TeleportLocation[] locations;
       
      TeleportData(TeleportLocation[] locations){
           this.locations = locations;
       }
}

