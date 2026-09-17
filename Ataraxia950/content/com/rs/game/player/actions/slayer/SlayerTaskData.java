package com.rs.game.player.actions.slayer;

import java.util.HashMap;
import java.util.Map;

public enum SlayerTaskData {

    BANSHEES(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 30, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 20, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 15, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 5, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185)}, 15, "These creatures have a ferocious screech, I'd suggest protecting your ears.", "Banshee", "Mighty banshee"),
    BATS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Bats are found throughout the darker areas of Gielinor, best to take them down from afar with Ranged Weaponry.", "Bat", "Warped bat", "Giant bat", "Albino bat"),
    BEARS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Bears are found throughout Gielinor, they're fierce creatures but you can take them down with Water Spells.", "Bear cub", "Grizzly bear cub", "Black bear", "Grizzly bear", "Angry bear"),
    BIRDS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "Birds can be found all around Gielinor, you can take them down easily enough with Earth Spells.", "Chicken", "Seagull", "Terrorbird", "Oomlie bird", "Duck", "Vulture", "Firebird"),
    CAVE_BUGS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 7, "This nasty little critter can easily be taken down with Crush weapons.", "Cave bug"),
    CAVE_SLIME(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 30, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 17, "This foul-smelling blob can be taken down easily using Slash weapons.", "Cave slime"),
    COWS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "Cows can be found all around Gielinor, they are weak to Earth Spells.", "Cow", "Cow calf"),
    CRAWLING_HANDS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 30, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 20, 40, 70), new TaskSet(SlayerMasterData.CHAELDAR, 5, 110, 170)}, 5, "These foes won't give you a high five, best to kill them Fire spells.", "Crawling hand", "Skeletal hand", "Zombie hand"),
    DESERT_LIZARDS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 30, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 22, "You might need some Ice Coolers to finish these cold-blooded creatures. Best to take them down with Stab weaponry.", "Desert lizard"),
    DOGS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 15, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Can be found all around Gielinor, best to take them down with Slash Weapons.", "Guard dog", "Wild dog", "Jackal", "Shadow hound"),
    DWARVES(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "These short angry guys can be found in the mines around Gielinor, they can be taken down by Air Spells.", "Dwarf", "Black guard", "Dwarf gang member", "Chaos dwarf", "Chaos dwarf hand cannoneer", "Black guard crossbowdwarf", "Black guard berserker"),
    GELATINOUS_ABOMINATION(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "The core of vile energy can be taken down using Ranged weaponry.", "Gelatinous Abomination"),
    GHOSTS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 15, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Spooky. This foe can be taken down easily using Thrown weaponry.", "Ghost", "Revenant imp", "Revenant goblin", "Revenant icefiend", "Recenant hobgoblin", "Revenant pyrefiend", "Tormented wraith", "Revenant vampyre", "Tortured soul", "Revenant werewolf", "Revenant cyclops", "Ghostly warrior", "Revenant hellhound", "Revenant demon", "Revenant ork", "Revenant dark beast", "Revenant knight", "Revenant dragon"),
    GOBLINS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "This ugly green creature can be found all around Gielinor, they can be easily killed using Air Spells.", "Goblin", "Cave goblin guard", "Sergeant Grimspike", "Sergeant Steelwill", "Sergeant Strongstack", "Revenant goblin"),
    GROTWORMS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 15, 15, 30), new TaskSet(SlayerMasterData.MAZCHNA, 10, 20, 40), new TaskSet(SlayerMasterData.VANNAKA, 15, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 72, 109), new TaskSet(SlayerMasterData.SUMONA, 15, 70, 115), new TaskSet(SlayerMasterData.DURADEL, 10, 80, 120), new TaskSet(SlayerMasterData.KURADAL, 10, 80, 160), new TaskSet(SlayerMasterData.MORVRAN, 10, 100, 180)}, 1, "This huge, disgusting worm can be killed easily using Bolts.", "Grotworm", "Young grotworm", "Mature grotworm"),
    ICEFIENDS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 10, 20)}, 1, "This small ice demon can be easily overcome by using Thrown weapons.", "Icefiend", "Revenant icefiend"),
    MINOTAURS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "He wont be pleased to see you! Take him down quick using Water spells.", "Minotaur"),
    MONKEYS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "Perhaps out oldest relatives? Kill them fast using Earth spells.", "Monkey", "Monkey guard", "Monkey archer", "Zombie monkey"),
    PIGS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "This wandering bundle of porky goodness can be taken down using Earth spells.", "Pig"),
    SCORPIONS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "This vicious creature can pinch quite a pinch! Strike them down with Crush weaponry.", "Scorpion", "King scorpion", "Poison scorpion", "Pit scorpion"),
    SKELETONS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 15, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Could do with gaining a few pounds, of your damage! Kill it with Earth spells.", "Skeleton", "Skeleton mage", "Skoblin", "Skeleton fremennik", "Skeletal miner", "Skeleton mage", "Skeletal hand", "Giant skeleton", "Skeleton thug", "Skeleton brute", "Skeleton heavy", "Skeleton warlord"),
    SPIDERS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50)}, 1, "Incey wincey spider, time to kill it! Take it down with Crush weaponry.", "Spider", "Giant spider", "Shadow spider", "Crypt spider", "Giant crypt spider", "Night spider", "Deadly red spider"),
    TROLLS(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 191)}, 1, "This foe gets up close and personal, so you can strike him down! Use Air spells.", "Troll", "Troll general", "Mountain troll", "Cliff", "Ice troll", "River troll", "Sea troll"),
    WOLVES(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 15, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "This vicious mountain wolf can be taken down with some Magic spells!", "Wolf", "White wolf", "Adolescent white wolf", "Desert wolf", "Big wolf", "Dire wolf", "Fenris wolf", "Ice wolf", "Jungle wolf"),
    ZOMBIES(new TaskSet[]{new TaskSet(SlayerMasterData.TURAEL, 10, 15, 50), new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "This dead man walking, be careful of its bite! Take it down by Earth spells.", "Zombie"),
    CATABLEPONS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Green cow? No, it's actually a Bull. This foe can be taken down easily with Bolts.", "Catablepon"),
    CAVE_CRAWLERS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 20, 40, 70), new TaskSet(SlayerMasterData.CHAELDAR, 5, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185)}, 10, "Poisonous creatures but can be easily taken down with Slash Weaponry, best to take a potion to heal your poison!", "Cave crawler"),
    COCKATRICES(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 20, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 25, "This Winged reptile needs a shield to reflect it's abilities from destroying you! Best using Crush weapons to take down this Reptile.", "Cockatrice"),
    CYCLOPS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 30, 60), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This one-eyed man eater can pack quite a hit, be sure to take them down fast with Earth Spells.", "Cyclops", "Revenant cyclops"),
    FLESH_CRAWLERS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "Insect repellent wont work on these foes, you'll need to use Stab weaponry to take them down.", "Flesh Crawler"),
    GHOULS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "These save ghouls can be take down fast and efficiently using Fire spells.", "Ghoul"),
    HILL_GIANTS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This very large foe can hit quite hard, be sure to protect yourself and take them down using Air Spells.", "Hill giant"),
    HOBGOBLINS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70)}, 1, "This ugly & smelly creature can be found all around Gielinor, take them down fast with Air Spells.", "Hobgoblin", "Revenant hobgoblin"),
    ICE_WARRIORS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This cold-hearted elemental warrior can be defeated using Fire spells.", "Ice warrior"),
    KALPHITE(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 189), new TaskSet(SlayerMasterData.DURADEL, 10, 170, 250), new TaskSet(SlayerMasterData.KURADAL, 5, 170, 250), new TaskSet(SlayerMasterData.MORVRAN, 5, 205, 300)}, 1, "Insect repellent wont work on these, you'll need to use Water spells!", "Kalphite worker", "Kalphite soldier", "Kalphite guardian", "Exiled kalphite worker", "Exiled kalphite paragon", "Exiled kalphite soldier", "Exiled kalphite guardian", "Exiled kalphite marauder", "Kalphite queen", "Exiled kalphite queen", "Kalphite king"),
    MOGRES(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 32, "This angry ogre wears a funny hat, but will strike you down at first chance! Kill it fast using Air Spells.", "Mogre"),
    PYREFIENDS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 20, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 30, "This tiny fire demon can be taken down using Bolts.", "Pyrefiend", "Revenant pyrefiend"),
    ROCK_SLUGS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 15, 40, 70)}, 20, "A spoon full of salt makes the rockslug go down. Kill them with Water spells & a bag of salt to finish the job!", "Rockslug"),
    VAMPYRES(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 10, 40, 70), new TaskSet(SlayerMasterData.VANNAKA, 15, 60, 120)}, 1, "It looks really hungry, take it down with Fire spells.", "Revenant vampyre", "Feral vampyre", "Vampyre"),
    WALL_BEASTS(new TaskSet[]{new TaskSet(SlayerMasterData.MAZCHNA, 15, 10, 20)}, 35, "This big scary hand can grab you! Take it down with Water spells.", "Wall beast"),
    ABERRANT_SPECTRES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 25, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 140, 250)}, 60, "You may need to cover your nose from the potent stench.", "Aberrant spectre"),
    ANKOUS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "These foes are weak to Earth Spells, be careful with these undead creatures.", "Ankou"),
    BASILISKS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170)}, 40, "It's best to use a shield with a reflection for the draining effects from this foe, take them down with Air Spells.", "Basilisk"),
    BLOODVELD(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 20, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 180, 250)}, 50, "The Tongue of evil in Gielinor, take them down fast with Fire Spells.", "Bloodveld"),
    BRINE_RATS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170)}, 47, "Disgusting creature, BURN IT WITH FIRE!", "Brine rat"),
    CROCODILES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 30, 60)}, 1, "The Desert heat might stop you from the task, but a water source will help along with Air Spells.", "Crocodile"),
    DUST_DEVILS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 25, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 150, 250)}, 65, "This little vacuumed face of evil can hurt you, be sure to wear a face mask and take them down with Crush weaponry.", "Dust devil"),
    EARTH_WARRIORS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 15, 30, 60)}, 1, "This inhuman elemental warrior can be easily taken down using Water spells.", "Earth warrior"),
    GREEN_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 30, 60)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Bolts.", "Green dragon", "Brutal green dragon"),
    HARPIE_BUG_SWARMS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 20, 110, 170)}, 33, "A swarm of bugs that can hit quite a lot, be sure to use a lit bug lantern when fighting these foes, take them down with Crush weaponry.", "Harpie bug swarm"),
    ICE_GIANTS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "Not a big friendly giant, instead he hits you around. Take them down using Fire spells.", "Ice giant"),
    INFERNAL_MAGES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170)}, 45, "This evil magic user can be taken down using Ranged weaponry.", "Infernal mage"),
    JELLY(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170)}, 52, "Wibbly, Wobbly, not to be taken lightly. Take them down using Crush weaponry.", "Jelly"),
    JUNGLE_HORROR(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170)}, 1, "This horrible emaciated ape can be taken down with Water spells.", "Jungle horror"),
    KILLERWATTS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120)}, 37, "You'll need to wear some thick boots to fight these foes! Take them down using Crush weaponry.", "Killerwatt"),
    LESSER_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170)}, 1, "Lesser but still a powerful demon, take it down using Darklight or Bolts!", "Lesser demon", "Zakl'n Gritch", "Revenant demon"),
    MOLANISKS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 39, "This strange mole-like being can be killed using Crush weaponry.", "Molanisk"),
    MOSS_GIANTS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This bearded giant covered in moss can be easily taken down using Slash weaponry.", "Moss giant"),
    OGRES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This dim looking humanoid can look easy, but packs a punch! They're weak to Air spells.", "Ogre", "Ogress", "Ogress warrior", "Ogress champion", "Zogre", "Skogre"),
    OTHERWORDLY_BEINGS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "This being is not of this world, they can be taken down easily though using Thrown weapons.", "Otherwordly being"),
    SEA_SNAKES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 30, 60)}, 50, "This snake lives in the sea, it is vulnerable to Slash weaponry.", "Sea snake"),
    SHADES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "Dead, but not gone. You can make it gone by killing it with Earth spells.", "Shade", "Loar shade", "Phrin shade", "Riyl shade", "Asyn shade", "Fiyr shade"),
    SHADOW_WARRIORS(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170)}, 1, "This fighter from a supernatural world is weak to Air spells.", "Shadow warrior"),
    TUROTH(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 20, 60, 120), new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185)}, 55, "This foe is easily taken down with Stab weaponry.", "Turoth", "Mighty turoth"),
    WEREWOLVES(new TaskSet[]{new TaskSet(SlayerMasterData.VANNAKA, 10, 60, 120)}, 1, "Watch out it's a full moon! Take it down fast with Water spells.", "Werewolf", "Revenant werewolf"),
    BLUE_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 8, 120, 189), new TaskSet(SlayerMasterData.KURADAL, 7, 120, 200)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Bolts.", "Blue dragon", "Baby blue dragon"),
    BRONZE_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 30, 60)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Water Spells.", "Bronze dragon"),
    CAVE_HORROR(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185)}, 58, "You will need a light source to be able to find these cave dwelling foes, when you find them you should take them down with Air Spells.", "Cave horror"),
    DAGANNOTH(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 192), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 170, 240), new TaskSet(SlayerMasterData.MORVRAN, 10, 205, 290)}, 1, "These horrors from the ocean are a force to not be taken lightly, take them down with Slash weaponry.", "Dagannoth", "Dagannoth rex", "Dagannoth prime", "Dagannoth supreme", "Dagannoth mother"),
    ELVES(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 60, 150), new TaskSet(SlayerMasterData.SUMONA, 10, 60, 90), new TaskSet(SlayerMasterData.KURADAL, 10, 120, 150), new TaskSet(SlayerMasterData.MORVRAN, 12, 150, 180)}, 1, "These elven warriors are best taken down using Fire spells.", "Elf warrior", "Cadarn magus", "Cadarn ranger", "Iorwerth guard", "Iorwerth scout"),
    FEVER_SPIDERS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170)}, 42, "You'll need gloves worthy of a Slayer master in order to take down this foe, in combination with Crush weaponry to finish the kill.", "Fever spider"),
    FIRE_GIANTS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 170, 250)}, 1, "This large elemental adversary can be taken down swiftly using Slash weaponry.", "Fire giant"),
    FUNGAL_MAGI(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 83, 136), new TaskSet(SlayerMasterData.SUMONA, 10, 90, 150), new TaskSet(SlayerMasterData.DURADEL, 8, 100, 200)}, 1, "This fungal animation of earth can be taken down fast using Air Spells.", "Fungal mage"),
    GARGOYLES(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 195), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 12, 150, 250), new TaskSet(SlayerMasterData.MORVRAN, 8, 180, 300)}, 75, "This flying rock can be taken down easily using Darklight or Water spells.", "Gargoyle"),
    GRIFOLAPINES(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 8, 62, 62), new TaskSet(SlayerMasterData.SUMONA, 8, 55, 75), new TaskSet(SlayerMasterData.DURADEL, 10, 65, 80), new TaskSet(SlayerMasterData.KURADAL, 8, 65, 80)}, 88, "This little prickly ball of fungus can be taken down using Water spells.", "Grifolapine"),
    GRIFOLAROOS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 8, 62, 62), new TaskSet(SlayerMasterData.SUMONA, 8, 55, 75), new TaskSet(SlayerMasterData.DURADEL, 10, 65, 80), new TaskSet(SlayerMasterData.KURADAL, 8, 65, 80)}, 82, "This creature may be bouncy, but is far from cute. Take it down fast with Earth spells.", "Grifolaroo"),
    JUNGLE_STRYKEWYRMS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 12, 80, 110), new TaskSet(SlayerMasterData.SUMONA, 12, 90, 120), new TaskSet(SlayerMasterData.DURADEL, 10, 90, 120), new TaskSet(SlayerMasterData.KURADAL, 8, 90, 130)}, 73, "Who said wyrms were harmless? This one isn't! Take it down with Stab weaponry.", "Jungle strykewyrm"),
    KURASKS(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 15, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 15, 120, 185)}, 70, "These heavy foes are a little sharp, take them down using Air Spells.", "Kurask", "Kurask overlord"),
    MUTATED_ZYGOMITES(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 30, 60)}, 57, "You will need Fungicide Spray to finish off these little bouncy fungus. You can deal damage fast using Slash weaponry.", "Mutated zygomite"),
    VYREWATCH(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 89, 106), new TaskSet(SlayerMasterData.SUMONA, 10, 96, 105), new TaskSet(SlayerMasterData.DURADEL, 8, 98, 118), new TaskSet(SlayerMasterData.KURADAL, 8, 90, 130), new TaskSet(SlayerMasterData.MORVRAN, 7, 110, 155)}, 1, "This thirsty-looking, airborne bloodsucker can be taken down using Bolts!", "Vyrewatch"),
    WARPED_TORTOISES(new TaskSet[]{new TaskSet(SlayerMasterData.CHAELDAR, 10, 110, 170), new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.KURADAL, 8, 150, 240)}, 56, "This tortoise can be easily taken down using Bolts.", "Warped tortoise"),
    ABYSSAL_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 15, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 12, 150, 250), new TaskSet(SlayerMasterData.MORVRAN, 10, 180, 300)}, 85, "You could benefit from using Darklight to fight these foes.", "Abyssal demon"),
    AQUANITES(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 9, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 120, 240), new TaskSet(SlayerMasterData.MORVRAN, 10, 195, 240)}, 78, "Don't be fooled by the pretty shiny light on its forehead, this creature can pack a hit. I'd suggest using Ranged weaponry to defeat this foe.", "Aquanite"),
    AUTOMATONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 8, 65, 80), new TaskSet(SlayerMasterData.DURADEL, 8, 65, 80), new TaskSet(SlayerMasterData.KURADAL, 8, 65, 80), new TaskSet(SlayerMasterData.MORVRAN, 8, 80, 100)}, 67, "These foes reside in the Guthix Cave; I'd suggest Fire spells for Guardians, Ranged Weaponry for Generators & Crush weapons for Tracers. Be wary, they can pack a punch!", "Automaton guardian", "Automaton tracer", "Automaton generator"),
    BLACK_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 119, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 190, 250), new TaskSet(SlayerMasterData.MORVRAN, 10, 230, 330)}, 1, "Large creatures that inhabit dungeons across Gielinor, they can pack a punch so be sure to take them down fast with Bolts!", "Black demon", "Balfrug Kreeyath"),
    DESERT_STRYKEWYRMS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 14, 90, 110), new TaskSet(SlayerMasterData.DURADEL, 11, 90, 140), new TaskSet(SlayerMasterData.KURADAL, 9, 90, 160), new TaskSet(SlayerMasterData.MORVRAN, 7, 110, 190)}, 77, "These sand dwellers are brutal when unburrowed, be careful with them! Use Stab weaponry to take them down quickly.", "Desert strykewyrm"),
    GREATER_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 11, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 11, 150, 258), new TaskSet(SlayerMasterData.MORVRAN, 11, 180, 300)}, 1, "This big, red & evil foe can be taken down using Darklight or Bolts.", "Greater demon", "K'ril Tsutsaroth", "Tstanon Karlak"),
    HELLHOUNDS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 9, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 130, 230)}, 1, "Not to type of dog you want to pet, they can snap on you quite fast. Take them down using Slash weaponry!", "Hellhound", "Revenant hellhound"),
    IRON_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 7, 30, 85), new TaskSet(SlayerMasterData.DURADEL, 9, 40, 80), new TaskSet(SlayerMasterData.KURADAL, 9, 40, 120), new TaskSet(SlayerMasterData.MORVRAN, 7, 75, 130)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Water spells.", "Iron dragon"),
    MUTATED_JADINKO(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 80, 130), new TaskSet(SlayerMasterData.DURADEL, 8, 120, 200), new TaskSet(SlayerMasterData.KURADAL, 8, 160, 220), new TaskSet(SlayerMasterData.MORVRAN, 8, 195, 265)}, 80, "A horrible jadinko, take them down using Stab weaponry.", "Mutated jadinko baby", "Mutated jadinko guard", "Mutated jadinko male"),
    NECHRYAELS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 140, 220), new TaskSet(SlayerMasterData.MORVRAN, 10, 170, 265)}, 80, "This evil death demon can be hard hitting, be sure to use Crush weaponry to take them down.", "Nechryael"),
    RED_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 5, 30, 79)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Bolts.", "Red dragon", "Baby red dragon"),
    SCABARITES(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 5, 30, 60), new TaskSet(SlayerMasterData.DURADEL, 10, 40, 80)}, 1, "These are beetles, yet they don't half bite! Best taking them down with Water spells.", "Scarab swarm", "Small scarabs", "Locust rider", "Scarab mage", "Locust lancer", "Locust ranger", "Scabaras mage", "Scabaras ranger", "Insectiod assassin", "Scarabs", "Giant scarab", "High priest of scabaras"),
    SPIRITUAL_MAGES(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185), new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 10, 150, 240)}, 83, "This deadly servant of the gods can be taken down using Ranged weaponry.", "Spiritual mage"),
    SPIRITUAL_WARRIORS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 120, 185)}, 68, "This warrior of the gods is weak to Air spells.", "Spiritual warrior"),
    TERROR_DOGS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 10, 30, 60), new TaskSet(SlayerMasterData.KURADAL, 6, 60, 70)}, 40, "This terrifying dog beast packs a bite! Take it down with Slash weaponry.", "Terror dog"),
    AIRUT(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 16, 125, 150), new TaskSet(SlayerMasterData.KURADAL, 20, 125, 150), new TaskSet(SlayerMasterData.MORVRAN, 16, 150, 180)}, 92, "These foes are from another world, they have no weakness. I suggest using Melee and Prayer if you are to survive.", "Airut"),
    ASCENSION_MEMBERS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 8, 100, 125), new TaskSet(SlayerMasterData.KURADAL, 10, 100, 125), new TaskSet(SlayerMasterData.MORVRAN, 10, 120, 150)}, 81, "These foes reside in the Ascension Dungeon, they are weak to Ranged Weaponry, protect yourself from their magic attacks.", "Rorarius", "Gladius", "Capsarius", "Scutarius", "Legio primus", "Legio secundus", "Legio tertius", "Legio quartus", "Legio quintus", "Legion sextus"),
    AVIANSIE(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 8, 100, 125), new TaskSet(SlayerMasterData.KURADAL, 9, 100, 200), new TaskSet(SlayerMasterData.MORVRAN, 9, 150, 195)}, 1, "These followers of Armadyl can't be hit with Melee attacks, I'd suggest using Bolts to take them out of the sky!", "Kree'arra", "Aviansie", "Spiritual ranger", "Spiritual warrior", "Spiritual mage", "Flight kilisa", "Wingman skree", "Flockleader geerin"),
    BLACK_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 9, 40, 80), new TaskSet(SlayerMasterData.KURADAL, 5, 40, 90), new TaskSet(SlayerMasterData.MORVRAN, 5, 50, 125)}, 1, "Beware! Protect yourself from its Fiery Breath! Take it down with Bolts.", "Black dragon", "Baby black dragon", "King black dragon", "Queen black dragon"),
    CELESTIAL_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 16, 125, 135), new TaskSet(SlayerMasterData.KURADAL, 16, 125, 150), new TaskSet(SlayerMasterData.MORVRAN, 10, 150, 180)}, 1, "Foes not to be taken lightly, they pack a hell of a punch! Best to take down with Ranged weaponry.", "Celestial dragon"),
    DARK_BEASTS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 15, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 12, 150, 250), new TaskSet(SlayerMasterData.MORVRAN, 12, 180, 300)}, 90, "These beasts from a darker dimension can be ferocious when attacking, take them down fast using Bolts!", "Dark beast", "Revenant dark beast"),
    GANODERMIC_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 6, 55, 70), new TaskSet(SlayerMasterData.KURADAL, 7, 70, 90), new TaskSet(SlayerMasterData.MORVRAN, 7, 85, 110)}, 95, "Hideous creature covered in fungus, take it down fast with Fire spells!", "Ganodermic beast", "Ganodermic runt"),
    GORAK(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 5, 40, 80)}, 1, "Gorak by name, Gorak by nature! These foes can be taken down easily using Water spells.", "Gorak"),
    ICE_STRYKEWYRMS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 8, 100, 200), new TaskSet(SlayerMasterData.KURADAL, 12, 100, 220), new TaskSet(SlayerMasterData.MORVRAN, 8, 120, 240)}, 93, "This cold burrowed wyrm can hit quite hard on you, be sure to protect yourself and kill it fast using Fire spells.", "Ice strykewyrm"),
    MITHRIL_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 7, 4, 11), new TaskSet(SlayerMasterData.KURADAL, 8, 20, 35), new TaskSet(SlayerMasterData.MORVRAN, 8, 25, 51)}, 1, "Beware! Protect yourself from its Firey Breath! Take it down with Earth spells.", "Mithril dragon"),
    SKELETAL_WYVERNS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 5, 40, 80), new TaskSet(SlayerMasterData.KURADAL, 5, 40, 90)}, 72, "This dangerous pile of animated bones can be killed using Fire spells.", "Skeletal wyvern"),
    STEEL_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 7, 40, 80), new TaskSet(SlayerMasterData.KURADAL, 9, 40, 100), new TaskSet(SlayerMasterData.MORVRAN, 9, 50, 125)}, 1, "Steel scales for such a fierce creature, Beware! Protect yourself from its Fiery Breath! Best to take it down with Water spells.", "Steel dragon"),
    SUQAH(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 5, 40, 80), new TaskSet(SlayerMasterData.KURADAL, 5, 50, 100)}, 1, "This strange creature is taken down easily using Fire spells.", "Suqah"),
    WARPED_TERRORBIRDS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 9, 130, 200)}, 56, "A terrifying bird! Take it down with Slash weaponry.", "Warped terrorbird"),
    WATERFIENDS(new TaskSet[]{new TaskSet(SlayerMasterData.DURADEL, 10, 130, 200), new TaskSet(SlayerMasterData.KURADAL, 9, 170, 250), new TaskSet(SlayerMasterData.MORVRAN, 9, 205, 300)}, 1, "This fiendish embodiment of water can be taken down using Bolts.", "Waterfiend"),
    ADAMANT_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 8, 40, 50), new TaskSet(SlayerMasterData.MORVRAN, 8, 40, 60)}, 1, "These foes are weak to Magic, mainly Air Spells, I'd suggest using magic and protecting yourself from their Fiery Breath.", "Adamant dragon"),
    CHAOS_GIANTS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 10, 60, 100), new TaskSet(SlayerMasterData.MORVRAN, 8, 80, 120)}, 1, "Fierce looking creature, best to be taken down quickly with Stab weaponry!", "Chaos giant"),
    EDIMMUS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 10, 199, 199), new TaskSet(SlayerMasterData.MORVRAN, 10, 170, 265)}, 90, "This spiritual zombie will suck the life out of its victims, be sure to take it down fast with Slash weaponry!", "Edimmu", "Powerful edimmu"),
    GLACORS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 8, 50, 70), new TaskSet(SlayerMasterData.MORVRAN, 8, 60, 85)}, 1, "This infused ice creature powered from stone can be taken down efficiently using Fire spells.", "Glacor"),
    KALGERION_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 5, 54, 100), new TaskSet(SlayerMasterData.MORVRAN, 5, 60, 120)}, 90, "This powerful demon is a force to be reckoned with, take it down using Darklight.", "Kal'gerion demon"),
    LAVA_STRYKEWYRMS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 12, 50, 100), new TaskSet(SlayerMasterData.MORVRAN, 8, 60, 120)}, 94, "The foes are forged from pure fire and reside in the Wilderness. They have no weakness but I'd suggest Ranged weaponry!", "Lava strykewyrm", "WildyWyrm"),
    LIVING_ROCK_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 10, 110, 185)}, 1, "A being of minerals, taken down using Crush weaponry.", "Living rock protector", "Living rock striker", "Living rock patriarch"),
    MUSPAH(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 16, 120, 150), new TaskSet(SlayerMasterData.MORVRAN, 10, 150, 180)}, 76, "These little creatures pack quite a fierce hit, take them down using Ancient Magicks.", "Force muspah", "Bladed muspah", "Throwing muspah"),
    NIGHTMARE_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 10, 40, 90), new TaskSet(SlayerMasterData.MORVRAN, 10, 60, 120)}, 80, "This dark vision has been brought to life, to take yours, kill them fast using Earth spells.", "Nightmare"),
    NIHIL(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 16, 40, 90), new TaskSet(SlayerMasterData.MORVRAN, 10, 50, 110)}, 76, "These Vampyric abominations can be taken down using Magic.", "Ice nihil", "Smoke nihil", "Blood nihil", "Shadow nihil"),
    TZHAAR(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 7, 70, 110), new TaskSet(SlayerMasterData.MORVRAN, 8, 95, 130)}, 1, "You can take these all down with Water spells.", "TzHaar-Ket", "TzHaar-Hur", "TzHaar-Xil", "TzHaar-Mej"),
    TORMENTED_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 8, 40, 60), new TaskSet(SlayerMasterData.MORVRAN, 8, 50, 75)}, 1, "This demons can only be weakened with Darklight!", "Tormented demon"),
    VOLCANIC_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.KURADAL, 0, 280, 280)}, 1, "You'll need to think strategically to survive in there."),
    //ACHERON_MAMMOTHS(new TaskSet[] { new TaskSet(SlayerMasterData.MORVRAN, 8, 33, 54) }, 96, "These foes are weak to stab based weapons, I'd suggest using one.", "Acheron mammoth"),
    CAMEL_WARRIORS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 8, 75, 125)}, 96, "Beware of the mirage, they may trick you! You need to take them down with a powerful crush!", "Camel warrior"),
    CRYSTAL_SHAPESHIFTERS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 12, 128, 128)}, 80, "Dangerous foes, don't take them lightly! Your best chance at survival is using Fire Spells.", "Crystal shapeshifter"),
    GEMSTONE_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 15, 85, 110), new TaskSet(SlayerMasterData.KURADAL, 15, 85, 110)}, 95, "These majestic dragons are decorated in gems, but they aren't just shiny, they can hit hard! Be sure to use Ranged to take them down fast & efficiently!", "Dragonstone dragon", "Onyx dragon", "Hydrix dragon"),
    LIVING_WYVERNS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 8, 48, 48)}, 96, "This foe is very dangerous and can be taken down using Fire spells.", "Wyvern", "Elite wyvern"),
    RIPPER_DEMONS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 8, 68, 80)}, 96, "These Skilled demonic slashers are a force not to be taken lightly, take them down using Darklight or Earth spells.", "Ripper demon"),
    RUNE_DRAGONS(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 8, 45, 74)}, 1, "This foes scales are fused with rune metal, they are the toughest dragons around Gielinor! They have no weakness but I suggest using Ranged weaponry to defeat these mighty creatures!", "Rune dragon", "Elite rune dragon"),
    SHADOW_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 10, 100, 150)}, 1, "These shadows can be killed easily using Fire spells.", "Truthful shadow", "Blissful shadow", "Manifest shadow"),
    CORRUPTED_CREATURES(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 15, 150, 280), new TaskSet(SlayerMasterData.KURADAL, 8, 188, 223), new TaskSet(SlayerMasterData.MORVRAN, 10, 162, 246)}, 1, "", "Corrupted scorpion", "Corrupted scarab", "Corrupted lizard", "Corrupted dust devil", "Corrupted kalphite marauder", "Corrupted kalphite guardian", "Corrupted worker"),
    SOUL_DEVOURERS(new TaskSet[]{new TaskSet(SlayerMasterData.SUMONA, 15, 150, 299), new TaskSet(SlayerMasterData.KURADAL, 10, 188, 223), new TaskSet(SlayerMasterData.MORVRAN, 12, 150, 250)}, 1, "", "Salawa akh", "Feline akh", "Scarab akh", "Crocodile akh", "Gorilla akh", "The Magister", "Imperial mage akh", "Imperial warrior akh", "Imperial ranger akh"),
    TORMENTED_DEMON(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 10, 25, 50), new TaskSet(SlayerMasterData.KURADAL, 10, 20, 40)}, 80, "", "Tormented demon"),
    VINE_CRAWLER(new TaskSet[]{new TaskSet(SlayerMasterData.MORVRAN, 10, 25, 50), new TaskSet(SlayerMasterData.KURADAL, 10, 20, 40)}, 99, "", "Vinecrawler");

    private final TaskSet[] taskSet;
    private final int slayerRequirement;
    private final String[] monsters;
    private final String tip;

    private static final Map<String, SlayerTaskData> all = new HashMap<>();

    public static SlayerTaskData forName(String name) {
        return all.get(name);
    }

    static {
        for (SlayerTaskData data : SlayerTaskData.values()) {
            for (String name : data.monsters) {
                all.put(name, data);
            }
        }
    }

    SlayerTaskData(TaskSet[] taskSet, int slayerRequirement, String tip, String... monsters) {
        this.taskSet = taskSet;
        this.slayerRequirement = slayerRequirement;
        this.tip = tip;
        this.monsters = monsters;
    }

    public final TaskSet[] getTaskSet() {
        return taskSet;
    }

    public final TaskSet getCertainTaskSet(int slayerMasterId) {
        for (int i = 0; i < taskSet.length; i++)
            if (taskSet[i].getSlayerMaster().ordinal() == slayerMasterId)
                return taskSet[i];
        return null;
    }

    public final int getSlayerRequirement() {
        return slayerRequirement;
    }

    public final String getTip() {
        return tip;
    }

    public final String[] getMonsters() {
        return monsters;
    }

    public final String getSingularName() {
        if (equals(KALGERION_DEMONS))
            return "Kal'Gerion demon";
        if (equals(TZHAAR))
            return "TzHaar";
        if (equals(DWARVES))
            return "Dwarf";
        if (equals(WOLVES))
            return "Wolf";
        if (equals(WEREWOLVES))
            return "Werewolf";
        if (equals(FUNGAL_MAGI))
            return "Fungal mage";
        String name = name().toLowerCase().replace("_", " ");
        if (name.charAt(name.length() - 1) == 's')
            name = name.substring(0, name.length() - 1);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public String toString() {
        if (equals(KALGERION_DEMONS))
            return "Kal'Gerion demons";
        if (equals(TZHAAR))
            return "TzHaar";
        String name = name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
