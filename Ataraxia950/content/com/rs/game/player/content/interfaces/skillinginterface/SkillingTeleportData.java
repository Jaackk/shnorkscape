package com.rs.game.player.content.interfaces.skillinginterface;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.interfaces.teleport.TeleportLocation;

/**
 * ataraxia-server
 * paolo 23/11/2019
 * #Shnek6969
 */
public enum SkillingTeleportData {


    AGILITY(new TeleportLocation[] {
            new TeleportLocation("Gnome Course", new WorldTile(2468, 3437,0)),
            new TeleportLocation("Barbarian Course", new WorldTile(2552, 3557,0)),
            new TeleportLocation("Wilderness Course", new WorldTile(2998, 3911), true),
            new TeleportLocation("Agility Pyramid", new WorldTile(3358, 2827)),
            new TeleportLocation("Prifindinas Course", new WorldTile(2177, 3398, 1)),
    }),
    CONSTRUCTION(new TeleportLocation[] {
            new TeleportLocation("Gnome Agility", new WorldTile(2468, 3437,0)),
    }),
    COOKING(new TeleportLocation[] {
            new TeleportLocation("Gnome Agility", new WorldTile(2468, 3437,0)),

    }),
    CRAFTING(new TeleportLocation[] {
            new TeleportLocation("Crafting Guild", new WorldTile(2933, 3289)),
            new TeleportLocation("Priffdinas Crafting", new WorldTile(2145, 3340, 1)),
    }),
    DUNGEONEERING(new TeleportLocation[] {
            new TeleportLocation("Dungeoneering", new WorldTile(3971,5560,0)) ,
    }),
    FARMING(new TeleportLocation[] {
            new TeleportLocation("Falador Herb ", new WorldTile(3054, 3309)),
            new TeleportLocation("Catherby Herb ", new WorldTile(2785, 3464)),
            new TeleportLocation("Ardougne Herb ", new WorldTile(2664, 3374)),
            new TeleportLocation("Port Phasmatys Herb ", new WorldTile(3599, 3523)),
            new TeleportLocation("Priffdinas Herb ", new WorldTile(2250, 3383, 1)),
            new TeleportLocation("Taverly Tree ", new WorldTile(2886, 3460)),
            new TeleportLocation("Varrock Tree ", new WorldTile(3227, 3464)),
            new TeleportLocation("Falador Tree ", new WorldTile(3004, 3377)),
            new TeleportLocation("Gnome Stronghold Tree ", new WorldTile(2432, 3418)),
            new TeleportLocation("Lumbridge Tree ", new WorldTile(3226, 3248)),
            new TeleportLocation("Priffdinas Tree ", new WorldTile(2230, 3301, 1)),
            new TeleportLocation("Catherby Fruit Tree ", new WorldTile(2856, 3432)),
            new TeleportLocation("Gnome Fruit Tree ", new WorldTile(2491, 3177)),
            new TeleportLocation("Tree Gnome Fruit Tree ", new WorldTile(2475, 3447)),
            new TeleportLocation("Lletya Fruit Tree", new WorldTile(2346, 3164)),
            new TeleportLocation("Karamja Fruit Tree", new WorldTile(2765, 3209)),
            new TeleportLocation("Priffdinas Fruit Tree", new WorldTile(2217, 3433, 1)),
    }),
    FIREMAKING(new TeleportLocation[] {

    }),
    FISHING(new TeleportLocation[] {
            new TeleportLocation("Fishing guild", new WorldTile(2595,3414)),
            new TeleportLocation("Fishing Colony", new WorldTile(2343,3694)),
            new TeleportLocation("Living Rock Cavern", new WorldTile(3014, 9831)), //fishing teles
            new TeleportLocation("Priffidinas fishing", new WorldTile(2265, 3403, 1)),
            new TeleportLocation("Piscatoris colony", new WorldTile(2330, 3690)),
    }),
    FLETCHING(new TeleportLocation[] {

    }),
    HERBLORE(new TeleportLocation[] {

    }),
    HUNTER(new TeleportLocation[] {
            new TeleportLocation("Falconry", new WorldTile(2363, 3624)),
            new TeleportLocation("Feldip Hills", new WorldTile(2525, 2917)),
            new TeleportLocation("Puro Puro", new WorldTile(2591, 4318)),
            new TeleportLocation("Isafdar", new WorldTile(2254, 3183)), //Hunting Teles
            new TeleportLocation("Cobalt skillchompa", new WorldTile(2457, 3538)),
            new TeleportLocation("Viridian skillchompa", new WorldTile(3660, 3429)),
            new TeleportLocation("Rellekka Hunter Area", new WorldTile(2729, 3864)),
            new TeleportLocation("Desert Quarry Hunter Area", new WorldTile(3169, 2866)),
            new TeleportLocation("Priffdinas Hunter Area", new WorldTile(2235, 3422, 1)),
    }),
    MINING(new TeleportLocation[] {
            new TeleportLocation("Al-Kharid mining", new WorldTile(3300, 3312)),
            new TeleportLocation("Lumbridge mining", new WorldTile(3229,3150)),
            new TeleportLocation("Dwarf Mine", new WorldTile(3045,9785)),
            new TeleportLocation("Lavaflow Mine", new WorldTile(2179,5663)),
            new TeleportLocation("Living Rock Cavern", new WorldTile(3014, 9831)), //fishing teles
            new TeleportLocation("Karamja", new WorldTile(2849, 3033)), //mining teles
            new TeleportLocation("Red Sandstone", new WorldTile(2590, 2880)),
            new TeleportLocation("Mining Guild", new WorldTile(3022, 3337)),
            new TeleportLocation("Desert Quarry", new WorldTile(3160, 2911)),
            new TeleportLocation("Rune Essence Mine", new WorldTile(2911, 4832)),
            new TeleportLocation("Shilo Village Gem Mine", new WorldTile(2824, 2996)),
            new TeleportLocation("Priffindinas Seren Stones", new WorldTile(2220, 3298, 1)),
            new TeleportLocation("Priffindinas Ore Mine", new WorldTile(2215, 3324, 1)),
            new TeleportLocation("South-east Varrock", new WorldTile(3283, 3371)), //mine
    }),
    PRAYER(new TeleportLocation[] {
            new TeleportLocation("Corrupted seren stone", new WorldTile(2191, 3444, 1)),
    }),
    RUNECRAFTING(new TeleportLocation[] {
           // new TeleportLocation("Runespan", new WorldTile(3994, 6105, 1), "RunespanController"),
            new TeleportLocation("Abyss", new WorldTile(3039, 4834)),

    }),
    SLAYER(new TeleportLocation[] {
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
    }),
    SMITHING(new TeleportLocation[] {
            new TeleportLocation("Varock-west bank", new WorldTile(3185,3436)),
    }),
    SUMMONING(new TeleportLocation[] {
            new TeleportLocation("Summoning", new WorldTile(2923, 3449)),
    }),
    THIEVING(new TeleportLocation[] {
            new TeleportLocation("Ardougne thieving", new WorldTile(2662, 3303,0)),
            new TeleportLocation("Dwarf Traders", new WorldTile(2886,10193,1)),
    }),
    WOODCUTTING(new TeleportLocation[] {
            new TeleportLocation("Seers woodcutting", new WorldTile(2725, 3491,0)),
            new TeleportLocation("Jungle", new WorldTile(2818, 3082)), //Woodcutting teles
            new TeleportLocation("Isafdar", new WorldTile(2293, 3142)), //Woodcutting teles
            new TeleportLocation("South of Varrock", new WorldTile(3258, 3369)), //Woodcutting teles
            new TeleportLocation("Priffdinas", new WorldTile(2248, 3382, 1)), //Woodcutting teles
    }),
    DIVINATION(new TeleportLocation[] {
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
    }),
    INVENTION(new TeleportLocation[] {
            new TeleportLocation("Invention Guild", new WorldTile(2997, 3437, 0)),
    });

    public TeleportLocation[] locations;

    SkillingTeleportData(TeleportLocation[] locations){
        this.locations = locations;
    }

    public static boolean hasRequirement(Player player, String locationName){
        if ((locationName.equals("Edimmu") || locationName.toLowerCase().contains("priff")) && !player.hasAccessToPrifddinas()) {
            player.sendMessage("You do not meet the requirements to access Priffdinas.");
            return false;
        }
        return true;
    }
}
