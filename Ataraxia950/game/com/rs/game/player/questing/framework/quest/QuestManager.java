package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;
import com.rs.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David O'Neill
 */
public final class QuestManager {

    private enum QuestStatus {
        NOT_STARTED, IN_PROGRESS, FINISHED
    }

    private transient final Player player;
    private Map<QuestStatus, List<Quest>> questMap;
    private List<Quest> allQuests;

    private int questPoints;

    public static void create(Player player) {
        new QuestManager(player);
    }

    private QuestManager(Player player) {
        this.player = player;
        this.player.setQM(this);
        init(this.player);
    }

    boolean startQuest(Quest q) {
        if(!questMap.get(QuestStatus.NOT_STARTED).remove(q))
            return false;
        questMap.get(QuestStatus.IN_PROGRESS).add(q);
        return true;
    }

    boolean finishQuest(Quest q) {
        if(!questMap.get(QuestStatus.IN_PROGRESS).remove(q))
            return false;
        questMap.get(QuestStatus.FINISHED).add(q);
        QuestStage s = q.finishQuest();
        q.addToFlow(s);
        return true;
    }

    public List<Quest> getQuestsInProgress() {
        return questMap.get(QuestStatus.IN_PROGRESS);
    }

    public List<Quest> getNotStartedQuests() {
        return questMap.get(QuestStatus.NOT_STARTED);
    }

    public List<Quest> getFinishedQuests() {
        return questMap.get(QuestStatus.FINISHED);
    }


   public void addQuestPoints(int qp) {
        questPoints += qp;
    }

    void removeQuestPoints(int qp) {
        questPoints -= qp;
    }

    public int getQuestPoints() {
        return questPoints;
    }

    private void safeRemove(Quest q) {
        allQuests.remove(q);
        if(questMap.get(QuestStatus.NOT_STARTED).remove(q))
            return;
        if(questMap.get(QuestStatus.NOT_STARTED).remove(q))
            return;
        questMap.get(QuestStatus.NOT_STARTED).remove(q);
    }

    public void pull() {
        Map<Quest, Class<? extends Quest>> map = new HashMap<>();
        allQuests.forEach(q -> map.put(q, q.getClass()));
        map.forEach((q, c) -> {
           if(!QuestHandler.handledQuests.contains(c))
               safeRemove(q);
        });
        QuestHandler.handledQuests.forEach(( __class__) -> {
            if(!map.containsValue(__class__)) {
                try {
                    Quest quest = __class__.getDeclaredConstructor(Player.class).newInstance(player);
                    questMap.get(QuestStatus.NOT_STARTED).add(quest);
                    allQuests.add(quest);
                } catch(NoSuchMethodException | IllegalAccessException |
                        InstantiationException | InvocationTargetException e) {
                    Logger.getGlobal().fatal("Could not fetch: " + __class__.getSimpleName(), e);
                }
            }
        });
    }

    @SuppressWarnings("serial")
    private void init(Player player) {
        questMap = new HashMap<QuestStatus, List<Quest>>()
        {{
            put(QuestStatus.NOT_STARTED, new ArrayList<>());
            put(QuestStatus.IN_PROGRESS, new ArrayList<>());
            put(QuestStatus.FINISHED, new ArrayList<>());
        }};
        allQuests = new ArrayList<>();
        QuestHandler.handledQuests.forEach((__class__) -> {
            try {
                Quest quest = __class__.getDeclaredConstructor(Player.class).newInstance(player);
                questMap.get(QuestStatus.NOT_STARTED).add(quest);
                allQuests.add(quest);
            } catch(NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InvocationTargetException e) {
                Logger.getGlobal().fatal( "Could not instantiate: " + __class__.getSimpleName(), e);
            }
        });
    }


}
