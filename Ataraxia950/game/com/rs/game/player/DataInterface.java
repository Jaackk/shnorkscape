package com.rs.game.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Use this model as an implementation for an interface that simply holds lines of text. Its efficient because it uses a
 * dynamic runscript instead of looping to clear the interface, and can be appended to and repeatedly shown when needed.
 * {@link Iterable} is implemented so you can iterate over the lines of this interface.
 *
 * @author lare96 <http://github.com/lare96>
 */
public class DataInterface implements Iterable<String> {
    private static final int MAX_LINES = 300;
    private static final int MAX_LINE_LENGTH = 75;
    private final List<String> lines;
    private final List<String> immutableLines;
    private String title = "";
    private boolean wordWrap = false;

    public DataInterface(String title) {
        setTitle(title);
        lines = new ArrayList<>();
        immutableLines = Collections.unmodifiableList(lines);
    }


    public void addAll(Collection<?> collection) {
        for (Object obj : collection) {
            lines.add(Objects.toString(obj));
        }
    }

    /**
     * Adds a line of text to the end.
     */
    public final void add(Object obj, String wrapWith) {
        if (lines.size() >= MAX_LINES) {
            throw new IllegalStateException("The data interface has a maximum of " + MAX_LINES + " lines.");
        }
        String str = obj.toString();
        if (wordWrap && str.length() > MAX_LINE_LENGTH) {
            List<String> words = Lists.newArrayList(str.split(" "));
            StringBuilder nextLine = new StringBuilder();
            while (!words.isEmpty()) {
                // Loop until all words from the list are on the interface.
                Iterator<String> iter = words.iterator();

                // Inner loop that handles the actual "Word wrapping".
                while (iter.hasNext()) {
                    String nextWord = iter.next();
                    iter.remove(); // Remove word from the cache, possibly causing the outer loop to break.
                    if (nextWord.length() > MAX_LINE_LENGTH) {
                        // If there's no spaces, it'll go off the interface.
                        throw new IllegalStateException("A single word cannot exceed " + MAX_LINE_LENGTH + " characters with word-wrap enabled.");
                    }
                    if (nextWord.length() + nextLine.length() > MAX_LINE_LENGTH) {
                        // The buffer will exceed the interface's space if the next word is added.
                        // So "flush" the buffer, reset it, and add the next word.
                        lines.add(nextLine.toString());
                        nextLine.setLength(0);
                        nextLine.append(wrapWith != null ? wrapWith : "").append(nextWord);
                        if(!iter.hasNext()) {
                            // There are no more words to process, so add the remaining last word onto the next line.
                            lines.add(nextLine.toString());
                            nextLine.setLength(0);
                        } else {
                            // There are more words to process, add a space.
                            nextLine.append(" ");
                        }
                        break;
                    }

                    nextLine.append(nextWord);
                    if (iter.hasNext()) {
                        // Add the next word to the buffer, with a trailing space.
                        nextLine.append(" ");
                    } else {
                        // No more words to add.
                        lines.add(nextLine.toString());
                        nextLine.setLength(0);
                    }
                }
            }
        } else {
            lines.add(str);
        }
    }

    public final void add(Object obj) {
        add(obj, null);
    }

    /**
     * Adds an empty new line.
     */
    public final void blankLine() {
        add("");
    }

    /**
     * Adds {@code count} empty new lines.
     */
    public final void blankLines(int count) {
        for (int loop = 0; loop < count; loop++) {
            blankLine();
        }
    }

    /**
     * Displays the lines on the interface.
     */
    public final void show(Player player) {
        int linesNeeded = size() + 1;
        int lineId = 1;
        player.getPackets().sendIComponentText(275, lineId++, title);
        lineId += 8;
        player.getPackets().sendIComponentText(275, lineId++, "");
        for (String str : this) {
            player.getPackets().sendIComponentText(275, lineId++, str);
        }
        for (String str : onShow(player)) {
            player.getPackets().sendIComponentText(275, lineId++, str);
        }
        for (int loop = 0; loop < 25; loop++) {
            player.getPackets().sendIComponentText(275, lineId++, "");
        }
        player.getInterfaceManager().sendInterface(275);
    }

    public List<String> onShow(Player player) {
        return ImmutableList.of();
    }

    public final void setWordWrap(boolean enabled) {
        wordWrap = enabled;
    }

    /**
     * CLears all lines.
     */
    public final void clear() {
        lines.clear();
    }

    /**
     * Sets the title. {@code null} is transformed to an empty string.
     */
    public final void setTitle(String title) {
        if (title == null)
            title = "";
        this.title = title;
    }

    /**
     * The amount of lines.
     */
    public final int size() {
        return lines.size();
    }

    /**
     * The returned iterator is immutable. Calling {@code remove()} will throw an exception.
     */
    @Override
    public Iterator<String> iterator() {
        return immutableLines.iterator();
    }
}