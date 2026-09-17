package com.rs.game.player;

import com.google.common.collect.ImmutableList;
import com.rs.utils.Logger;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A model that lets you display lines and pages on the book interface.
 *
 * @author lare96
 */
public class BookInterface {

    public static final class BookPage {
        public final ImmutableList<String> lines;

        public BookPage(String... lines) {
            if (lines.length >= 11) {
                throw new IllegalStateException("Book pages can have a max of 11 lines!");
            }
            this.lines = ImmutableList.copyOf(lines);
        }

        public String getLine(int index) {
            if (index < 0 || index >= lines.size()) {
                return "";
            }
            return lines.get(index);
        }
    }

    private int currentIndex = -1;
    private String title = "title";
    private final List<Function<Player, BookPage>> indexes = new ArrayList<>();

    public BookInterface setTitle(String newTitle) {
        title = newTitle;
        return this;
    }

    public BookInterface addPage(Function<Player, BookPage> newPage) {
        indexes.add(newPage);
        return this;
    }

    public BookInterface addPage(String... lines) {
        indexes.add(player -> new BookPage(lines));
        return this;
    }

    private void displayIndex(Player player, int index) {
        if (index < 0 || index >= indexes.size()) {
            Logger.getGlobal().warn("Invalid index in BookInterface [lookup_index={}, book_size={}]", index, indexes.size());
            return;
        }
        val left = indexes.get(index);
        val right = (index + 1) >= indexes.size() ? null : indexes.get(index + 1);

        player.getPackets().sendIComponentText(959, 5, title);

        if (left != null) { // Generate left page.
            val page = left.apply(player);
            int pageIndex = 0;
            for (int cid = 30; cid <= 40; cid++) {
                player.getPackets().sendIComponentText(959, cid, page.getLine(pageIndex++));
            }
        }

        if (right != null) { // Generate right page.
            val page = right.apply(player);
            int pageIndex = 0;
            for (int cid = 41; cid <= 51; cid++) {
                player.getPackets().sendIComponentText(959, cid, page.getLine(pageIndex++));
            }
        }
        player.getPackets().sendIComponentText(959, 52, index + 1); // Left page number.
        player.getPackets().sendIComponentText(959, 53, index + 2); // Right page number.
        player.getInterfaceManager().sendInterface(959);
        currentIndex = index;
    }

    // TODO previous page & next page actionbuttons don't work?
    public void nextPage(Player player) {
        if ((currentIndex + 1) < indexes.size()) {
            displayIndex(player, currentIndex + 1);
        }
    }

    public void previousPage(Player player) {
        if (currentIndex > 0) {
            displayIndex(player, currentIndex - 1);
        }
    }

    public void show(Player player) {
        player.lastBookInterface = this;
        displayIndex(player, 0);
    }
}
