package com.rs.game.activites.quest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.rs.game.player.Player;

import java.util.List;

public final class QuestStagePipeline implements Iterable<QuestStage> {

    private final transient ImmutableList<QuestStage> stages;

    public QuestStagePipeline(List<QuestStage> newStages) {
        stages = ImmutableList.copyOf(newStages);
    }

    public String[] getText(Player player, int index, AbstractQuest quest) {
        return stages.get(index).description(player, quest);
    }

    public int size() {
        return stages.size();
    }

    public ImmutableList<QuestStage> list() {
        return stages;
    }

    @Override
    public UnmodifiableIterator<QuestStage> iterator() {
        return stages.iterator();
    }
}
