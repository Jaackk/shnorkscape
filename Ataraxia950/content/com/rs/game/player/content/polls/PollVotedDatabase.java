package com.rs.game.player.content.polls;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.rs.cores.CoresManager;
import com.rs.utils.Logger;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PollVotedDatabase {

    public static final class PlayerVoteList {
        public final String username;
        public final PlayerVote[] votes;

        public PlayerVoteList(String username, PlayerVote[] votes) {
            this.username = username;
            this.votes = votes;
        }
    }

    public static final class PlayerVote implements Serializable {
        private static final long serialVersionUID = 8453479600237107105L;
        public final int questionId;
        public final int answerId;

        public PlayerVote(int questionId, int answerId) {
            this.questionId = questionId;
            this.answerId = answerId;
        }
    }

    private final Path voteFile = Paths.get("data/pollbooth/votes.txt");
    private final Map<String, PlayerVoteList> votes = new HashMap<>();

    public void addVote(PlayerVoteList vote) {
        votes.put(vote.username, vote);
        saveVote(vote);
    }

    public boolean containsVote(String username) {
        return votes.containsKey(username);
    }

    public int getVotes(int questionId, int letterId) {
        int total = 0;
        for (PlayerVoteList voteList : votes.values()) {
            PlayerVote vote = voteList.votes[questionId];
            if (letterId == vote.answerId) {
                total++;
            }
        }
        return total;
    }

    public int getHighestVoteCount(int questionId) {
        Multiset<Integer> count = HashMultiset.create();
        for (PlayerVoteList voteList : votes.values()) {
            PlayerVote vote = voteList.votes[questionId];
            count.add(vote.answerId);
        }

        int votes = 0;
        for (Multiset.Entry<Integer> nextVote : count.entrySet()) {
            int nextCount = nextVote.getCount();
            if (nextCount > votes) {
                votes = nextCount;
            }
        }
        return votes;
    }


    public int getTotalVotes() {
        return votes.size();
    }

    public void clear() {
        if (votes.isEmpty())
            return;
        votes.clear();
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                PrintWriter pw = new PrintWriter(voteFile.toFile());
                pw.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void load() {
        try {
            if (Files.exists(voteFile)) {
                try (Scanner scanner = new Scanner(voteFile.toFile())) {
                    while (scanner.hasNextLine()) {
                        String username = scanner.nextLine();
                        List<PlayerVote> allVotes = new ArrayList<>();
                        while (scanner.hasNextLine()) {
                            String nextLine = scanner.nextLine();
                            if (nextLine.equals("end;")) {
                                break;
                            }
                            String[] data = nextLine.split(":");
                            int questionId = Integer.parseInt(data[0]);
                            int answerId = Integer.parseInt(data[1]);
                            allVotes.add(new PlayerVote(questionId, answerId));
                        }
                        PlayerVote[] voteArray = Iterables.toArray(allVotes, PlayerVote.class);
                        votes.put(username, new PlayerVoteList(username, voteArray));
                    }
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                }
            } else {
                Files.createFile(voteFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return votes.size();
    }

    public void saveVote(PlayerVoteList voteList) {
        try {
            CoresManager.getServiceProvider().executeNow(() -> {
                try (FileWriter fw = new FileWriter(voteFile.toFile(), true)) {
                    fw.write(voteList.username + "\n");
                    for (PlayerVote vote : voteList.votes) {
                        fw.write(vote.questionId + ":" + vote.answerId + "\n");
                    }
                    fw.write("end;\n");
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                }
            });
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}