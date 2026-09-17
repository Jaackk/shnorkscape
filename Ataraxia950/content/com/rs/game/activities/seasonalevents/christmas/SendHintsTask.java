package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.collect.ImmutableList;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.tasks.WorldTask;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author lare96
 */
public final class SendHintsTask extends WorldTask {

    @Data
    public static final class PresentLocation {
        private final int x;
        private final int y;
        private final int z;
        private final String[] hints;

        public PresentLocation(int x, int y, int z, String... hints) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hints = hints;
        }
    }

    @Data
    @AllArgsConstructor
    public static final class PresentHint {
        private final int x;
        private final int y;
        private final int z;
        @EqualsAndHashCode.Exclude
        private final String hint;

        public PresentHint(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            hint = "";
        }

        public WorldTile getHintTile() {
            return new WorldTile(x, y, z);
        }
    }

    private final Set<PresentHint> hints;
    private final Set<PresentHint> unmodifiableHints;

    private int loops;

    public SendHintsTask() {
        hints = new LinkedHashSet<>();
        unmodifiableHints = Collections.unmodifiableSet(hints);
    }

    @Override
    public void run() {
        loops++;
        if (hints.isEmpty() || loops >= 3) {
            generateNewHints();
        }
    }

    public void generateNewHints() {
        deleteAllHints();
        for (int loop = 0; loop < ChristmasSeasonalEvent.HINTS_TO_GIVE; loop++) {
            val next = Utils.randomFrom(ALL_LOCATIONS);
            String hint = Utils.randomFrom(next.getHints());
            val presentHint = new PresentHint(next.x, next.y, next.z, hint);
            hints.add(presentHint);
            World.spawnObject(new WorldObject(95001, 10, 0, presentHint.getHintTile()));
        }
        World.sendWorldMessage(Colors.DEF_SEARCH_CYAN + "Santa's reindeer have located " + hints.size() + " new presents! Go to Santa or type ;;cmashints for more info.", false);
        delayNewHints();
    }

    /**
     * The returned set is immutable.
     */
    public Set<PresentHint> getHints() {
        return unmodifiableHints;
    }

    public boolean deleteHint(WorldTile tile) {
        if (hints.remove(new PresentHint(tile.getX(), tile.getY(), tile.getPlane()))) {
            delayNewHints();
            return true;
        }
        return false;
    }

    public void deleteAllHints() {
        Iterator<PresentHint> iter = hints.iterator();
        while (iter.hasNext()) {
            World.removeObject(iter.next().getHintTile());
            iter.remove();
        }
    }

    public void delayNewHints() {
        loops = 0;
    }

    public static final ImmutableList<PresentLocation> ALL_LOCATIONS = ImmutableList.of(
            new PresentLocation(2465, 3219, 0, "This spot is where Santa lost his presents."),

            new PresentLocation(2449, 3072, 0, "Why does Guthix never involve himself in this glorious game!?",
                    "Did that player turn into a imp after entering that portal?",
                    "After all that work I earned 1 ticket..."),

            new PresentLocation(2207, 3192, 0, "This forest is a maze! I just want to say hi to the elves.",
                    "Who is going around this forest painting 'X's' on rocks?",
                    "I can see the crystal city from here!"),

            new PresentLocation(2209, 3362, 2, "The memories of Seren... simply breath taking!",
                    "Where do I put these crystals? weird!",
                    "The Tower of Voices, what an odd name for a place!"),

            new PresentLocation(2471, 3310, 0, "I'm not feeling too good... maybe they had the gates blocked for a reason.",
                    "ewwww why did it have to fall in the slums?",
                    "They're not even sick? Plus they're using them as slaves!"),

            new PresentLocation(2724, 3284, 0, "Slugs! Slugs everywhere!!!",
                    "These fishing platforms are pretty awesome!",
                    "I've been promoted to proselyte! Sweet looking armor here I come!"),

            new PresentLocation(2731, 3371, 0, "I don't feel much like a legend right now.",
                    "Nezikchened was no match for me! Now let me through.",
                    "I really need to recharge my Combat Bracelet"),

            new PresentLocation(2711, 3471, 1, "Wow it's right next to the flax fields!? That's so convenient.",
                    "Ughh why did they have to put a spinning wheel so close to a cemetery?",
                    "An iconic place in Seer's Village for F2P players at one time."),

            new PresentLocation(2263, 3553, 0, "All hail Guthix the keeper of balance! You will be missed.",
                    "An Engram? What in the world is this for?",
                    "Each butterfly is unique and named... really?"),

            new PresentLocation(2318, 3679, 0, "What was the fish that joined the church called? ...a MONK fish... HAHAHA",
                    "A village that has a hunting zone... AMAZING!",
                    "A village of the fishermen."),

            new PresentLocation(2403, 3490, 0, "Take a look at those cheerleaders! They are so out of my league.",
                    "One of the most fun gnome past times!",
                    "In these times, so tried and true, the gnomes will come, to tackle you."),

            new PresentLocation(2658, 3684, 0, "You think you can out drink me?!",
                    "My good friend always buys me a drink when I visit him, what a lad!",
                    "You are no musician, and barely a hero, but you must take this Lyre, and bluff up an ego"),

            new PresentLocation(2714, 3733, 0, "Crabs made of rock or rocks made of crab... ewww.",
                    "The ancestor of Rellekka... mhm has a nice ring to it.",
                    "How did Olaf's Boat sink?"),

            new PresentLocation(2901, 3375, 0, "Does Harry Potter live here??!!",
                    "I have to get that kids ball back for him!",
                    "How many forms can this experiment take before it dies!?"),

            new PresentLocation(3001, 3141, 0, "Finally, 99 smithing! Now where's my cape?",
                    "Why does this man like redberry pie so much?",
                    "This is the guy to have discovered Skeletal Wyverns?",
                    "Finally! a Blurite Weapon."),

            new PresentLocation(3116, 3354, 0, "It's hard to believe this weakling is a member of House Draken.",
                    "Garlic: check, Hammer: check, Stake: check.",
                    "This has to be the only vampyr outside of Morytania"),

            new PresentLocation(3102, 3142, 0, "A magic beam of light that sends you up and down a tower? How interesting!",
                    "I hear there are portals at the top of this tower that lead to another realm!",
                    "There used to be 4 factions that helped run this tower? That's crazy!"),

            new PresentLocation(3122, 3246, 0, "Wow with a few beers those guards bought the prince's disguise!",
                    "They removed this quest? Who will rescue Prince Ali now?!",
                    "What's that smell? Is that the Draynore Sewers?! Aww that's awful!"),

            new PresentLocation(3172, 3150, 0, "Quick, help the Abyssal Knights defend us against those creatures!",
                    "A portal of red, a portal of blue, these are no puzzles, the monsters will come for you.",
                    "Animasaurus rex... that can't be real. You must be joking right?"),

            new PresentLocation(3207, 3149, 0, "Time to restart the 2 years of silence and solitary... this happens every week.",
                    "This guy in the graveyard is either really excited or I'm missing something. All he says is 'woo' no matter what I ask him.",
                    "Why would there be a gold chalice in a swamp? Oh well, that's the contract the thieves guild gave me."),

            new PresentLocation(3290, 3151, 0, "This place really is a diamond in the rough!",
                    "Why do they have a water trough right outside of their palace?",
                    "4 red couches, pillows all around the throne, and cactus everywhere... what an odd place."),

            new PresentLocation(3295, 3030, 0, "Are those... slaves? They have a whole mine full of slaves??!!",
                    "So many vultures circling... I should be careful around here.",
                    "I did a little too good of a job tricking them into thinking I'm a slave...maybe I can smuggle myself out through the wooden cart!"),

            new PresentLocation(3160, 3039, 0, "I wonder why these people are called Tenti... oh well, their pineapples are to die for.",
                    "You know, for people that claim they are nomads these people don't move very frequently.",
                    "I can almost hear the Kalphites to the north of us!"),

            new PresentLocation(3283, 2917, 0, "I was told this was massive, it must be bigger on the inside.",
                    "I get you're a Doctor, but Doctor Who?",
                    "How in the world is there snow in the desert right now?"),

            new PresentLocation(3410, 2812, 0, "This used to be an ancient and prosperous place... now it's just swamp.",
                    "Is that a crocodile??!! I don't mess with that, I'm out!",
                    "Oh good, a spring I can fill my Waterskin at! Good thing this camp is on this cliff, I really needed that water!"),

            new PresentLocation(3156, 2729, 0, "A V.I.P. area with a pool and acadia trees? This place is great!",
                    "A corrupt god, a puppet leader, a shifting tomb, and a room flooded with gold.",
                    "Reputation is everything to these people! But they do reward you pretty well for your efforts I guess."),

            new PresentLocation(3176, 2650, 0, "Once a member of The Skulls, this man continues to live a life of corruption an turns a blind eye to most crimes committed in his district.",
                    "A port district full of sand with a wonderful breeze and beautiful music.",
                    "Fishing in the desert to get people to love me... never thought I'd see the day."),

            new PresentLocation(3487, 3019, 0, "This has to be the only pyramid that's rectangular and flat topped... can you even call it a pyramid at this point? More of a... Mastaba.",
                    "The first pyramid constructed in Gielinor! How... lacking... ",
                    "I think I just saw the Pharaoh Queen! What an honor! ... why is my skin burning!?!?"),

            new PresentLocation(2956, 3513, 0, "Orange... Blue... No Orange!... No Blue!!... how about just going with brown.",
                    "Is that a goblin boss? That's definitely a goblin boss... ",
                    "Do these generals ever stop fighting, it's Christmas for Saradomins sake!!"),

            new PresentLocation(3022, 3536, 0, "Bronze med helm, iron chainbody... that's all I need to get in?",
                    "'Zamorak o domine, Dona eis requiem' I feel like I've heard this somewhere else before... weird their chanting it in an evil fortress.",
                    "I'm not too sure about entering this place... part of it is in the wilderness!"),

            new PresentLocation(3547, 3563, 0, "Oh a castle in a swamp... fun?",
                    "A doctor who's mad, with creations who're glad, in a castle that's sad.",
                    "Who in the world is Charos? His ring seems so cool!"),

            new PresentLocation(3612, 3543, 0, "A model ship leads to a wrecked ship, a wrecked ship leads to a map... where's the treasure? Maybe it's here too?",
                    "I found the old crones son, it'll be smooth sailing from here.",
                    "From boy to man, from model to reality, but then a dream turns to nightmare."),

            new PresentLocation(3616, 3336, 0, "A tree of white, with wood that blisters.",
                    "I must use robes of black to hide the truth, otherwise these monsters will surly kill me.",
                    "Don't drink from the fountain... I think that stuff is... blood!"),

            new PresentLocation(3493, 3231, 0, "You know... it's in the middle of nowhere in a swamp...but with a little fixing up, this village might be pretty useful!",
                    "A secret base in a remote part of a swamp... what could go wrong?",
                    "I feel bad for revealing their location, but at least they're closer to the vampyr scum now!"),

            new PresentLocation(3262, 3454, 2, "The old and the new come together, to entertain and bewilder. Dust and sand some may see, but artifacts and history is what they be.",
                    "I want to rebuild this statue, but why did it have to break into so many different pieces!",
                    "Kudos to you, if you figure this one out!",
                    "I see a cape of true triumph here, so much to do to get it, and how cool the colors that seem to change from player to player."),

            new PresentLocation(3228, 3473, 0, "Tranquil and true, the place is peaceful in such chaotic times.",
                    "For someone that wants to create and grow something so beautiful, you really are the rudest person I've met on my journeys.",
                    "I can't believe I stole an entire statue for this garden... she better not rat me out!"),

            new PresentLocation(2975, 3345, 0, "Yeah, these knights totally drop Santa hats on Christmas! You should spend the whole day killing them... ",
                    "Death and murder where there should be peace, on the evilest day in centuries by definition. These players found a way to cause mass murder on 06/06/06... ",
                    "I can't wait to get a family crest! I'm going to look so cool!"),

            new PresentLocation(3052, 3374, 0, "This was their answer to the drop trade scams? ... Yikes.",
                    "Dancing and balloons?!?! This is my kind of party!",
                    "I wish I could be that happy all the time... or at least have that afro!"),

            new PresentLocation(3056, 3338, 0, "Before the mining and smithing update, this place was full of people! It was so much more convenient than doing a million bank runs.",
                    "Cannon upgrades and cosmetics overrides? What wonderful rewards!",
                    "They make such cool armour and weapons here. Too bad they can't be kept and used."),

            new PresentLocation(3207, 3213, 0, "This was once one of the best and most efficient places to make bowstring!",
                    "Wow! An anti-fire shield! This guy is so generous to give this to me for free!",
                    "oof I cooked a cake for this guy? I feel bad, I might have given him diabetes."),

            new PresentLocation(3206, 3254, 0, "EOC When?",
                    "...And this is the combat triangle! Melee is strong against range but weak against magic. Magic is... blah blah blah... new combat system!",
                    "Do we really need instructors to teach us what we've been doing since we started this game?"),

            new PresentLocation(3369, 3230, 0, "Customizing the rules to a fight to make it as fair as possible? This is really neat!",
                    "I just lost 1b staking... 1b!",
                    "With a ring that's green and emanating power, and can save myself from walking an hour!"),

            new PresentLocation(3368, 3296, 0, "I see a fountain that floats, shaped like an egg that water runs down. How interesting!",
                    "I love infinity robes but this dead content is making this set unobtainable!",
                    "Two mighty Saradomin mages looking down at the entrance to a magical testing grounds. Are you worth to be considered a master?"),

            new PresentLocation(3335, 3413, 0, "Wow... this was once the capital of Zaro's nation. What happened to it?",
                    "Digging for priceless treasure! count me in!! ... wait this is just old broken stuff? What do you mean priceless to history!? That doesn't pay the bills!",
                    "This site is an archaeologists dream!"),

            new PresentLocation(3366, 3475, 0, "That haunted mansion was kind of fun! Wait, I can go through it again? And get better rewards!? nice!",
                    "Completing this quest in under 37 minutes was pretty hard, but this ring is actually pretty useful!",
                    "Maria, you gotta see her!"),

            new PresentLocation(3275, 3508, 0, "I think I can hear the sound of a saw running just outside... how annoying, can't a guy enjoy his beer in peace.",
                    "Giant hairy cabbages!",
                    "Oh finally, a nice pint of ale! Is that a boars head on the wall?"),

            new PresentLocation(3243, 3609, 0, "A temple surrounded by lava and full of chaos? All hail Zamorak!",
                    "This place was most likely once beautiful... now the only remaining beauty is the altar in the ruins.",
                    "I can't believe how close I am to Zemouregal's base!"),

            new PresentLocation(3101, 3676, 0, "This is what they originally replaced the wilderness pking with? How dull.",
                    "What are these emblems for? You trade them for points?! Why would anyone want these things that look like skulls?!",
                    "The wilderness works on a point system now?"),

            new PresentLocation(2949, 3821, 0, "A temple of chaos guarded by elite knights dressed in black.",
                    "Two statues of warriors with axes and murderous look about them in-front of a place of worship... how odd!",
                    "Do I smell burning??!! I hope it's not a lava strykewyrm!"),

            new PresentLocation(2504, 3627, 0, "Thank goodness this building still stands, otherwise ships might crash!",
                    "Where in the world is Jossik? I'm sure someone told me he lived here!",
                    "By Saradomin! This book may be damaged, but it is the best reward I could ask for. I can't believe this is really a god book!"),

            new PresentLocation(2197, 3695, 0, "Creatures killed by her are reborn and used to defend her... that is insane!",
                    "Light the pyre, not me!",
                    "I must die to survive, I am both killed and born from the same object. When I extinguish, so will the flame."),

            new PresentLocation(2549, 3105, 0, "A students hand in the sand? Just the hand...in the sand... ",
                    "Someone needs a hand, someone lost a hand... I'm just excited for pink dye!",
                    "These people are insane! Why would I need 84 buckets of sand daily?!?!"),

            new PresentLocation(2676, 3086, 1, "To think the small man living on this archipelago is the last surviving creator of the Grand Tree!",
                    "I can't wait to meet this mighty mage! A leader, a battle mage, a... tree whisperer... a gnome?!?!",
                    "c*l*s why are the fairies so confusing? "),

            new PresentLocation(2586, 3162, 0, "Fight! Fight for his amusement! General Khazard demands it!",
                    "1 fight two fight three fight four, not the  Mahjarrat General is at your door!",
                    "Awww puppy! What's it's name? Bouncer? That's a weird...by Saradomin no! Down boy!"),

            new PresentLocation(2505, 3172, 0, "a maze a maze of hedge an leaf. A maze a maze that I must breach. to breach to breach that is to reach, to breach to breach for a kings speech!",
                    "A few paths there are, but only one, that leads to a city in peril.",
                    "Saradomin praise these gnomes for this tree! I can get to so many different places a lot faster now!"),

            new PresentLocation(2525, 3210, 0, "A battlefield that never stops, near a village that always hides. These gnomes may never die.",
                    "Ballistas and pikes are all that's in sight, battle and war that never ends.",
                    "Khazard and gnomes... there will never be peace."),

            new PresentLocation(2465, 3497, 0, "This one is surly in Ta Quir Priw, it looks like  a throne room!",
                    "I am near a King named endaorn nerheas... sorry I'm dyslexic.",
                    "I can't believe Glough sent a demon to kill me!"),

            new PresentLocation(2498, 2706, 2, "Agorath will rise to kill us all, bring forth the rage of the sea!",
                    "A village founded over 400 years ago, people all around come here to live a life of peace and prosperity as nothing ever happens here... until...",
                    "Ashdale is such a lovely city!"),

            new PresentLocation(2561, 2864, 0, "Ooooooohhhhh a spa! I can't wait to feel it's effects.",
                    "Mud mud glorious mud! Good thing there's a whole bath of it!",
                    "As long as this water is on me, Bandos followers should view me as one of them!"),

            new PresentLocation(2413, 2839, 0, "Come quick! I hear the armies mobilizing!",
                    "The entire fort is gone, all that's let is a note! I know they're suppose to mobilize but how do they leave with an entire fort!?",
                    "I can't wait to get a high enough rank for a quest kit!"),

            new PresentLocation(2088, 3932, 0, "Is that a giant chicken!?!? Surly not, I can clearly see a house attached to those legs...that makes it normal... ",
                    "A vessel of mortar and a pestle for a weapon she waits for you in the woods, standing upon chicken legs!",
                    "How do you sleep or sit, cook or clean in a house that bounces and shakes?"),

            new PresentLocation(2139, 3946, 0, "An island of moon near a mine of magic.",
                    "An island with almost no wood on it used enough for a massive dock to harbor one ship... these people truly are strange!",
                    "I can wait to earn some produce points to unlock these new spells!... said no one ever... "),

            new PresentLocation(2639, 3678, 0, "Fish fur fish fur fish fur docks! This place is simple, I like it!",
                    "I wouldn't want to get on the market guards bad side here. Their shields are huge and their swords aren't much smaller!",
                    "For a people that don't like outsiders, these Fremennik sure have a lot of docks!"),

            new PresentLocation(2784, 3661, 0, "a*j*r and North? What does that even mean??!!",
                    "Unlike their complacent brothers, these people are a nomadic sort. It's weird they hate each other over a rock though.",
                    "I will find you daughter for you Hamel!"),

            new PresentLocation(3078, 3300, 0, "A travelling witch! Oh... she makes goulash? Maybe she's not so bad...",
                    "Are those skeletal cows... I think they are!",
                    "A broomstick... how... thoughtful? Do I sweep with it or fly on it?"),

            new PresentLocation(3090, 3252, 1, "What did he steal from the bank...I truly don't remember.",
                    "I see this old man all over the place! I had no idea he sold a cool white cape too!",
                    "He used telegrab to get it, maybe it'll work if I use it on him!"),

            new PresentLocation(3088, 3337, 0, "Add some poison to kill the fish!",
                    "There are piranha in there!?!? I'm not sticking any part of me in that water!",
                    "So many dead trees! I hope the manor is still in one piece"),

            new PresentLocation(3156, 9906, 0, "A pipe that leads to Varrock? Ewww smells like a sewer in there!",
                    "I'm fine with Moss Giants, but spiders! That's where I draw the line! Ughhhh bu the only way out now is through the sewers!"),

            new PresentLocation(2207, 4959, 0, "48 tables on two different floors and they're all empty... this place is really dead!",
                    "Is this connect four...in Runescape??!!",
                    "I love a good game of Draughts! What do you mean you call it checkers?!?! Why?"),

            new PresentLocation(2909, 3500, 0, "Druids are such a peaceful people! such a beautiful stone circle too!",
                    "I use to have to do a quest for these people to train Herblore?! How add!",
                    "A crown of leaf and robes of white, these men keep balance through Guthix might!"),

            new PresentLocation(3014, 3445, 0, "Enemies beware, and taste the wrath of my cannon!",
                    "At the base of a mountain of ice, to the north of a city in light, this is where you'll find delight!",
                    "These men may be small, but what they build will hit you like a cannonball!"),

            new PresentLocation(2834, 3677, 0, "This Goutweedy lump is nasty... at least I have it now.",
                    "Trolls are so disgusting! I hope I don't have to be here for long...-4 quests later-",
                    "seems by the root of the name, this must be the home of the trolls!"),

            new PresentLocation(3307, 3821, 0, "The massacre was so sad... the poor Bonde family...",
                    "A farm... or what's left of one... in the wilderness?",
                    "In the wilderness I do seek, freedom from Varrock's keep. A farm I will reap, to the east we will sleep."),

            new PresentLocation(3337, 3879, 0, "A place of safety surrounded by danger. Venture further if you dare, a dragon awaits!",
                    "I shall forever be remembered as Black Stone Rudolph! Fear me!",
                    "An Inert black stone crystal! I'm so close to getting the pet, wont be long now!"),

            new PresentLocation(3136, 3840, 0, "Fight in these camps to bring balance, in the name of Guthix!",
                    "A band of war that grants experience based on my efforts... how wonderful!"),

            new PresentLocation(3072, 3913, 0, "West of a magic arena.",
                    "I am near a place of training, it's extremely dangerous here. Good thing I'm agile!",
                    "Ahhhh ghostly robes, I remember these fondly."),

            new PresentLocation(3291, 3940, 0, "I am near an element of chaos, surly this castle will protect me!",
                    "A castle near a deposit of columnar basalt.",
                    "24 degrees 26 minutes north, 26 degrees 24 minutes east"),

            new PresentLocation(2513, 3375, 0, "Caged Ogres? This is perfect for range and mage training!",
                    "So many dummies here! I'd love to hit one of them, but I'm far too skilled to learn anything from them.",
                    "King Lathas did a wonderful job making this camp! I hope it helps train his troops."),

            new PresentLocation(2523, 3575, 0, "Paint for your weapons to show true strength, as is the barbarian way!",
                    "These insignia are awesome, I can't believe I have to kill the Penance King 5 times though!",
                    "Barbarians are strong and dexterous, but agile? How is this course going to help me?!"),

            new PresentLocation(3029, 3284, 0, "This is a real milestone in my life! It feels so good, but even better with the wonderful sea air filling my lungs from close by.",
                    "My ring teleported me to... a field of cabbage?",
                    "Bacon..."),

            new PresentLocation(2995, 3234, 0, "Sir Rebrum, what are we going to do about these worm pests?",
                    "This hide looks so royal, where in the world did you get it? I'll have to send my knights to get more!",
                    "3 tents, two logs, a campfire and the wonderful sea breeze from close by!"),

            new PresentLocation(2926, 3320, 0, "I'm a girl... no a guy... no a girl... hahaha I'm going to mess with so many people with this!",
                    "Smile and say cheese! this is going to make a wonderful avatar!",
                    "The sounds of chipping gems and clinking picks is really distracting me from making this gender changer... I sure hope it works!"),

            new PresentLocation(2936, 3248, 0, "I hope I can get that map piece from the manor, otherwise that dragon will never be vanquished.",
                    "Three floors and a basement, all mazes, all need keys to proceed... this is going to take forever.",
                    "I can't focus with all these clan members running around! My house has become a maze and I'm going mad!"),

            new PresentLocation(2988, 3110, 0, "a*i*q",
                    "I was trying to make a Blurite sword... I think I ran too far!",
                    "Mudskippers and mogres what are those??"),

            new PresentLocation(2782, 3212, 0, "This cart is so useful! No dangerous jungle, and all the gems I can mine!",
                    "There must be an agility grounds near here, it sounds like a death course! I wonder if that's where people keep getting pirate hooks from?",
                    "ScarFace Pete's Mansion is near here right? We better be careful!"),

            new PresentLocation(2800, 3150, 0, "An amulet of Power, amulet of Strength, amulet of Defense, amulet of Magic... and a holy symbol?? What a weird shop.",
                    "Markets run by pirates...  yeah I don't know about this one.",
                    "The Dead Mans Chest... whatever, they have beer!"),

            new PresentLocation(2861, 9572, 0, "A dungeon under a volcano... right next to a city under the same volcano... how did they make room.",
                    "Elvarg is no match for my awesome might!",
                    "For a dungeon that used to be so dangerous, it really is pointless now. Oh well, at least the volcano keeps me warm while I'm here!"),

            new PresentLocation(2924, 3146, 0, "Karamjan rum, count me in!",
                    "This guy sells stuff that helps me make cleaning cloths! Now I can finally wipe the paint off my whip!",
                    "Wow it's only 30 coins? This drink is amazing, and brewed locally! Wait, I have to smuggle it if I leave?!?!"),

            new PresentLocation(2919, 3175, 0, "Wow there's so many people fishing swordfish here... do they think this is the best fishing exp or something -eats banana-",
                    "So many banana trees... ooohhh a dock!",
                    "I have to pick 10 of these to get off this island?!?! Smuggling this rum is proving to be a pain!"),

            new PresentLocation(2791, 3084, 0, "Trading sticks to get more sticks... that makes a lot of sense.",
                    "The name of your village means 'small clearing in the jungle' ... how odd.",
                    "All praise the Moai...err I mean tribal statue!"),

            new PresentLocation(2921, 3042, 0, "That was close, we only barely escaped Glough! Where did we crash land, it looks tropical?",
                    "d*k*p and South... What does that even mean?!?!?"),

            new PresentLocation(2970, 3038, 0, "A place to build ship! Run by a... gnome?",
                    "'Let's not go skinny dipping, eh?'",
                    "G.L.O. Caranock... what kind of a name is that?"),

            new PresentLocation(2763, 2979, 0, "Bervirius is buried in a tomb on this isle... guess I'll have to explore it.",
                    "Oooohhhhh a chameleon egg... time to... blend it into the background of this meal... hahahahha I wanna die!",
                    "How off, the tomb is not above ground or made a rock like the name suggests... this isle is weird."),

            new PresentLocation(2886, 2953, 0, "Just... one... smalll... faaavvvooouuuuuuurrrr",
                    "Oh my gloves can teleport me there? That sure beats having to make my way through the undead creatures in front of the village entrance!",
                    "This cape is so awesome! SLAAAYYYERRR!!!"),

            new PresentLocation(2494, 2881, 0, "One key per kill... no one key per run... no one key per kill... just enter the dungeon already!",
                    "I really feel as though I've ascended to a more powerful me!",
                    "A crossbow, a lot of shards and... some rings??"),

            new PresentLocation(2589, 9488, 0, "OMG zombies!!! Why would you keep zombies in the basement in a populated city?!?! What to you mean they're magic zombies that you summon... ",
                    "Uuuuggghhhh another puzzle box? Why Frumscone... why?",
                    "OR ZINC FUMES WARD"),

            new PresentLocation(2548, 3038, 0, "The ogres have a capital city??",
                    "Those poor Skavids, they don't deserve to be slaves to the ogre.",
                    "I'm so glad I found out I needed this relic to get in, those ogre really pack a punch!"),

            new PresentLocation(2225, 3050, 0, "d*l*r and South... what does that even mean?",
                    "Looks like an incandescent wisp, how pretty!",
                    "Coal-tar... does that mean coal is used in the process of modifying Anima Mundi"),

            new PresentLocation(2610, 3281, 0, "Listen Larry... I don't trust them either man... but you gotta let this go.",
                    "Power to the PBJ! Down with the penguins!",
                    "Do they have wings or flippers? Surly there's so one obsessed enough to know?"),

            new PresentLocation(2573, 3296, 0, "So close to a city plagued, but protected by these high walls, I sit here with my crown of lies.",
                    "Wait... you want this plague?!?!"),

            new PresentLocation(2601, 3357, 0, "One sheep, two sheep, blue sheep... green...sheep?",
                    "You gave me a cattleprod... hahahahaha big mistake whoever left this laying around!",
                    "This plague jacket and trousers look pretty cool! I don't even feel bad about killing those sheep now!"),

            new PresentLocation(2668, 3423, 0, "Lets do some target practice! Okay, but I'm going to have to give you a ticket...",
                    "Yes! Finally mastered archery! Does this make me Robinhood now? ...What? Just a stupid cape?",
                    "150 quests down! Zanik, what are you doing here? Set the record, with that bow? Goodluck!"),

            new PresentLocation(2702, 3397, 0, "This is the second largest amount of magic trees I've seen in one spot. Right next to a bank practically too!",
                    "I can hear it now, this is such a magic journey.",
                    "b*l*r and Northwest... what does that even mean??"),

            new PresentLocation(2768, 3396, 0, "Fort Grymwold... yeah good thing they changed the name... that's weird...",
                    "Morgan might know how to free Merlin, goodluck getting to her.",
                    "I can see the Legend's Guild to the Southwest, how glorious!"),

            new PresentLocation(2744, 3444, 0, "I can't believe I used to pick these fields for hours to save up for rune armour... those were the days!",
                    "Flax and... beehives?",
                    "I can see the back of Seer's Courthouse from here, neat!"),

            new PresentLocation(2757, 3498, 2, "A table that is round... for great nights... I think I missed something.",
                    "Do they put archery target on top of every castle anymore? Oh well, I'm sure Author likes it.",
                    "Where's Merlin??"),

            new PresentLocation(2739, 3580, 1, "d*j*r and east... what does that even mean??", "Who murdered the fair Lord?",
                    "This has so many ingredients around, right next to a beautiful mansion too! The Seer's are such lucky people."),

            new PresentLocation(3229, 3398, 0, "30... 31... 32 I am determined to find out how old Gypsy Aris is!",
                    "I can't wait to go to my first Ex-Adventures Anonymous meeting, I really need a girl! Now where was the meeting place again?",
                    "The name of this bar is when two full moons happen in the same month! I usually only happens once every three years..."),

            new PresentLocation(3203, 3483, 2, "They really keep track of how many times their guards get pickpocketed... Ugh I could throw myself from a castle top.",
                    "Look at all these dummies up here... Varrock sure is strange.",
                    "The tree looks so out of place with nothing around it, maybe I just can't see it from the top of this castle."),

            new PresentLocation(3195, 3404, 0, "I am West of a sword shop and very close to a known gang hideout... I hope nothing bad happens to me!",
                    "Why in the world would you need or want an airtight pot!??",
                    "Is that a periodic table?"),

            new PresentLocation(3229, 3202, 0, "Just visit bob! ...no not the flash mob guy... no not Sinclairs sibling... what? No not the cat... It's just bob!!",
                    "An axe salesmen right next to a cemetery? That's odd.",
                    "Just a big barrel of swords... I hope he repairs them soon."),

            new PresentLocation(3252, 3282, 0, "Getting training newbie! You may feel weak now, but those hides will at least fetch you a good amount of money!",
                    "I just need a bucket of Top-quality milk for this cake!",
                    "Goblins next to cows... which one should I kill?")
    );
}