package com.rs.game.activites.quest;

import com.rs.game.player.Player;
import com.rs.game.player.QuestManager;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class QuestHandler implements Serializable {

    private static final long serialVersionUID = -7215358700405407718L;
    private transient Player player;

    @Getter
    @Setter
    private int questPoints;
    private Map<Class<?>, AbstractQuest> quests = new HashMap<>();

    public void addQuestPoints(int amount) {
        questPoints += amount;
    }

    public <T extends AbstractQuest> void displayInfo(Class<T> questType) {
        val quest = get(questType);
        quest.displayInfo(player);
    }

    public <T extends AbstractQuest> void advanceStage(Class<T> questType) {
        val quest = get(questType);
        quest.advanceStage(player);
    }

    public <T extends AbstractQuest> boolean isCompleted(Class<T> questType) {
        val quest = get(questType);
        return quest.isCompleted();
    }

    public <T extends AbstractQuest> int getCurrentStage(Class<T> questType) {
        val quest = get(questType);
        return quest.getCurrentStage();
    }

    @SuppressWarnings(value = "unchecked")
    public <T extends AbstractQuest> T get(Class<T> questType) {
        if (quests == null)
            quests = new HashMap<>();
        return (T) quests.computeIfAbsent(questType, k -> {
            Constructor<T> constructor;
            try {
                constructor = questType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("All <AbstractQuest> subclasses must have a constructor with no arguments.");
            }
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void init(Player instance) {
        player = instance;
    }

    public void clear() {
        quests.clear();
    }

    public boolean isAllCompleted() {
        if (!player.getQuestManager().completedQuest(QuestManager.Quests.NOMADS_REQUIEM)) {
            return false;
        }
        for (val questType : AbstractQuest.QUEST_TAB.values()) {
            if (!isCompleted(questType)) {
                return false;
            }
        }
        return true;
    }
}
