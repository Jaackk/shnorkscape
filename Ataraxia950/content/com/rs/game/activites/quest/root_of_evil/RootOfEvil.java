package com.rs.game.activites.quest.root_of_evil;

import com.google.common.collect.ImmutableList;
import com.rs.game.WorldTile;
import com.rs.game.activites.quest.AbstractQuest;
import com.rs.game.activites.quest.QuestStage;
import com.rs.game.item.Item;
import com.rs.game.player.BookInterface;
import com.rs.utils.Colors;

import java.util.List;

public final class RootOfEvil extends AbstractQuest {

    public static final BookInterface DIARY = new BookInterface().
            setTitle("Wizard's diary").
            addPage("Day 1",
                    "-----------",
                    "",
                    "We've finally finished setting",
                    "up the generator in this vault.",
                    "Doing this is dangerous now so",
                    "I need to develop a serum that",
                    "can neutralize the trees fast",
                    "if things get out of hand.").
            addPage("Day 2",
                    "-----------",
                    "",
                    "I've developed the serum! It",
                    "takes on the form of water",
                    "and is very simple to use.",
                    "You water the tree with the",
                    "serum like you would crops",
                    "and it seems to kill them",
                    "better than fire!",
                    "I'll lock it in my chest",
                    "so no one can find it...");
    public static final int SELF_DESTRUCT_KEY_ID = 1839;
    public static final int DIARY_ID = 2408;
    public static final int EVIL_WATERING_CAN_ID = 18682;
    public static final int EVIL_LOGS_ID = 5161;
    public static final int EVIL_ROOT_ID = 9919;
    public static final int EVIL_LOGS_REQ = 10;
    public static final int HERBLORE_REQ = 50;
    public static final int WOODCUTTING_REQ = 60;
    public static final WorldTile SOUTH_WEST_QUESTIONING = new WorldTile(5002, 704, 1);
    public static final WorldTile NORTH_EAST_QUESTIONING = new WorldTile(5041, 757, 1);

    public static final ImmutableList<String> PLAYER_QUESTIONS = ImmutableList.of(
            "What do you know about the recent Evil Tree invasions?",
            "Do you know anything about Evil Trees?",
            "You look suspicious. Where were you when the last Evil Tree attacked?",
            "Can you tell me anything about Evil Trees?",
            "Tell me what I need to know about Evil Trees, or I'll have you thrown ;;jail!"
    );

    public static final ImmutableList<String> NPC_RESPONSES = ImmutableList.of(
            "I don't know anything! I'm sorry.",
            "I don't know anything...",
            "I have no idea what you're talking about...",
            "Huh?",
            "The only thing I know is that someone with lots of spare money is behind it..."
    );

    private static final long serialVersionUID = 2166370831706498421L;

    @Override
    public void build(List<QuestStage> stages) {

        // Take artifact to Evil Tree Hunter.
        stages.add((player, quest) -> new String[]{
                "I should take the strange root to the " + bold("Evil Tree Hunter") + ". He should be",
                "able to explain what it is, and if it ties into these recent " + bold("Evil Tree"),
                "invasions."
        });

        // He tells you to take it to Mister Wiggles.
        stages.add((player, quest) -> new String[]{
                "I should go talk to " + bold("Mister Wiggles") + ".",
                "He is hiding on the " + bold("second floor") + " of " + bold("Zaff's staff shop") + " in " + bold("Varrock") + "."
        });

        // Continue talking to Mister Wiggles, post cutscene.
        stages.add((player, quest) -> new String[]{
                "I should continue talking to " + bold("Mister Wiggles") + ".",
        });

        // Mister wiggles says question everyone at ;;home.
        stages.add((player, quest) -> new String[]{
                bold("Mister Wiggles") + " says that the recent Evil Tree invasions are a direct",
                "attack on Ataraxians, and only someone with a lot of money could pull",
                "it off. I should talk to some " + bold("wealthy characters") + " at " + bold(";;home") + " and see if",
                "I can find out any suspicious info."
        });

        // The lottery coordinator is incredibly suspicious.
        stages.add((player, quest) -> new String[]{
                "The " + bold("Lottery coordinator") + " seems incredibly suspicious... I wonder if",
                "there's a way for me to get more information from him. I should go",
                "back and talk to " + bold("Mister wiggles") + " and see if he has any ideas",
                "about what to do."
        });

        // Mister wiggles says that makes sense, but we need proof. He asks you to make a truth serum
        // then go back to the lottery coordinator and make him drink it and tell you everything.
        stages.add((player, quest) -> new String[]{
                bold("Mister wiggles") + " says that I should make a truth serum potion. I'll need",
                "to use a " + bold("Ranarr herb") + " with a " + bold("vial") + ", then add some " + bold("Evil Dust") + " to the",
                "mixture to make the serum.",
                "The " + bold("Lottery coordinator") + " wants beer, so I should buy some from",
                bold("Varrock's pub") + " and mix the serum with it. Once I do that",
                "I should give him the final mixture."
        });

        // Give truth serum to Lottery coordinator.
        stages.add((player, quest) -> new String[]{
                "I've given the truth serum to the " + bold("Lottery coordinator") + ".",
                "I should talk to him and see what he really knows..."
        });

        // The Lottery coordinator talks and tells you the location of an underground vault he has where the seeds are stored and
        // the powerful wizard that's working for him is. With the help of the wizard, he's able to plant evil trees here
        // instead of being transported to their realm (like when you normally use evil seeds). using to
        // plant evil trees around ataraxia. His motive is revenge for the legislation that made the lottery tax reduced.
        // He's making less money and can no longer keep up with his lavish lifestyle
        stages.add((player, quest) -> new String[]{
                "The " + bold("Lottery coordinator") + " told me about an " + bold("underground vault") + " containing",
                "a wizard that uses machines to spawn Evil Trees in Ataraxia.",
                "",
                "It's located in the bush behind " + bold("Wydin's Food Store") + " in " + bold("Port Sarim") + ".",
                "I should go there and attempt to foil their operations.",
        });
    }

    @Override
    public String[] initialDescription() {
        return new String[]{
                "I can start this quest by killing " + Colors.DARK_RED + "Evil Trees</col> until I find an " + Colors.DARK_RED + "Evil root</col>.",
                "",
                "Length: Average",
                "",
                "Difficulty: Easy",
                "",
                "Requirements:",
                "~ " + HERBLORE_REQ + " Herblore",
                "~ " + WOODCUTTING_REQ + " Woodcutting"
        };
    }

    @Override
    public String[] rewardDescription() {
        return new String[0];
    }

    @Override
    public int questPoints() {
        return 4;
    }

    @Override
    public Item completionDisplayItem() {
        return new Item(EVIL_ROOT_ID);
    }

    @Override
    public String name() {
        return "Root of Evil";
    }
}
