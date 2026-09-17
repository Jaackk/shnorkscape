package com.rs.game.activites.quest;

import com.google.common.collect.ImmutableMap;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple quest abstraction model. Represents a quest, in its entirety. All variables related to the quest should be
 * declared implementations as well.
 * <p>
 * Subclasses must have a constructor that only takes one player argument. They must all also be added to the ImmutableList constant
 * below if you want them to appear
 *
 * @author lare96
 */
public abstract class AbstractQuest implements Serializable {

    public static final ImmutableMap<String, Class<? extends AbstractQuest>> QUEST_TAB = ImmutableMap.<String, Class<? extends AbstractQuest>>builder().
            // put("root of evil", RootOfEvil.class).
                   put("death's bounty", DeathsBounty.class).
                    build();
    private static final String BOLD = Colors.BLUE;
    private static final long serialVersionUID = -8725534734828283556L;


    private transient QuestStagePipeline pipeline;
    private int currentStage = -1;

    /**
     * Add the quest stages here, in order. The order should not be changed once pushed to production or else it can mess up
     * people who have a quest in progress (though this is unlikely).
     */
    public abstract void build(List<QuestStage> stages);

    /**
     * The initial quest description before the quest is started.
     */
    public abstract String[] initialDescription();

    public abstract String[] rewardDescription();

    public abstract int questPoints();

    public abstract Item completionDisplayItem();

    /**
     * The quest name.
     */
    public abstract String name();

    public final void displayInfo(Player player) {
        if (getCurrentStage() == -1) {
            DataInterface inter = new DataInterface(name());
            for (String str : initialDescription()) {
                inter.add(str);
            }
            inter.show(player);
        } else {
            DataInterface inter = new DataInterface(name());
            for (int index = 0; index < currentStage; index++) {
                String[] description = getPipeline().getText(player, index, this);
                for (String str : description) {
                    inter.add("<str>" + str + "</str>");
                }
            }
            for (String str : getPipeline().getText(player, currentStage, this)) {
                inter.add(str);
            }
            inter.show(player);
        }
    }

    protected final String bold(String msg) {
        return BOLD + msg + "</col>";
    }

    public final void advanceStage(Player player) {
        player.sendMessage(Colors.CYAN + "Your quest log has been updated.");
        currentStage++;
        onStageChanged(player, currentStage);
    }

    public final void set(Player player, int newStage) {
        int size = getPipeline().size();
        if (newStage < -1) {
            newStage = -1;
        } else if (newStage >= size) {
            newStage = size - 1;
        }
        currentStage = newStage;
        onStageChanged(player, newStage);
    }
public void onComplete(Player player) {

}
    private void onStageChanged(Player player, int newStage) {
        if (newStage >= getPipeline().size()) {
            onComplete(player);
            int questPoints = questPoints();
            String name = name();
            player.quests.addQuestPoints(questPoints);
            player.lock(2);
            player.getInterfaceManager().sendInterface(277);
            player.getPackets().sendIComponentText(277, 4, "You have completed " + name() + ".");
            player.getPackets().sendIComponentText(277, 7, player.quests.getQuestPoints());
            player.getPackets().sendIComponentText(277, 9, "You are awarded:");
            int start = 10;
            int end = 17;
            int index = 0;
            String[] rewards = rewardDescription();
            for (int i = start; i < end; i++) {
                if (index >= rewards.length) {
                    player.getPackets().sendIComponentText(277, i, "");
                } else {
                    player.getPackets().sendIComponentText(277, i, rewards[index++]);
                }
            }
            Item displayItem = completionDisplayItem();
            player.getPackets().sendItemOnIComponent(277, 5, displayItem.getId(), displayItem.getAmount());
            player.getPackets().sendGameMessage(Colors.CYAN + "Congratulations! You have completed " + name + "!");
        }
    }


    public final int getCurrentStage() {
        return currentStage;
    }

    public final boolean isCompleted() {
        return currentStage >= getPipeline().size();
    }

    public final QuestStagePipeline getPipeline() {
        if (pipeline == null || pipeline.list() == null) {
            List<QuestStage> newStages = new ArrayList<>();
            build(newStages);
            pipeline = new QuestStagePipeline(newStages);
        }
        return pipeline;
    }
}
