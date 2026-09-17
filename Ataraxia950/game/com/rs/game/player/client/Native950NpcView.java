package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import io.netty.channel.Channel;

/**
 * One connection's NPC state: the per-session {@link Native950NpcViewport} that encodes
 * {@code NPC_INFO}, plus the world-owned banker the P5 interaction whitelist knows by name.
 *
 * <p>P6 moved two things out of this class. The NPC entities now belong to
 * {@link Native950World} - a character that logs in no longer creates its own banker, so two
 * sessions see the same entity at the same {@code EntityList} index - and every bit of the
 * frame belongs to {@code Native950NpcInfo}, so the single-NPC literal writers are gone. What
 * is left is the session-facing adapter: which NPC a click may target, and the diagnostics the
 * smokes read.
 *
 * <p>{@link #canInteract} answers from the list this viewer's client was last <em>sent</em>,
 * not from the world, because a client can only click what it has been shown. It runs in the
 * input phase, before this tick's frame, so that list is the previous tick's - exactly what the
 * player was looking at when the click was made.
 */
public final class Native950NpcView {
    /** The distance the NPC encoder publishes at; kept for callers that reason about range. */
    static final int VIEW_DISTANCE = com.rs.network.protocol.modern950.Native950NpcInfo.DEFAULT_VIEW_DISTANCE;
    private final Thread owner = Thread.currentThread();
    private final Native950Content.BankerNpc definition;
    private final NPC banker;
    private final Native950NpcViewport viewport;

    Native950NpcView(Native950Content.BankerNpc definition, NPC banker) {
        this.definition = definition;
        this.banker = banker;
        this.viewport = new Native950NpcViewport();
    }

    /** The world-owned banker, the only NPC the P5 interaction whitelist knows. */
    NPC banker() { checkThread(); return banker; }

    Native950Content.BankerNpc definition() { checkThread(); return definition; }

    /** The session's NPC encoder; the frame encoder writes through it after PLAYER_INFO. */
    Native950NpcViewport viewport() { checkThread(); return viewport; }

    /** Writes this tick's NPC_INFO. Called by the frame encoder, after the player frame. */
    void synchronize(Player viewer, Channel channel, int npcBits, boolean rebuilt) {
        checkThread();
        viewport.synchronize(viewer, channel, npcBits, rebuilt);
    }

    /** True while {@code index} is an NPC this viewer's client holds and can still reach. */
    boolean canInteract(Player player, int index) {
        checkThread();
        if (index < 1) return false;
        boolean published = false;
        for (int held : viewport.snapshot().indices) if (held == index) published = true;
        if (!published) return false;
        NPC npc = World.getNPCs().get(index);
        return inRange(player, npc);
    }

    private boolean inRange(Player player, NPC npc) {
        return npc != null && !npc.hasFinished() && !npc.isDead()
                && player.getPlane() == npc.getPlane()
                && Math.abs(player.getX() - npc.getX()) <= VIEW_DISTANCE
                && Math.abs(player.getY() - npc.getY()) <= VIEW_DISTANCE;
    }

    /** Drops this viewer's published list; the NPCs themselves stay in the world. */
    void close() {
        checkThread();
        viewport.close();
    }

    State snapshot() {
        checkThread();
        Native950NpcViewport.State state = viewport.snapshot();
        boolean bankerVisible = false;
        if (banker != null) for (int held : state.indices) if (held == banker.getIndex()) bankerVisible = true;
        return new State(banker == null ? 0 : banker.getIndex(), banker == null ? -1 : banker.getId(),
                banker == null ? 0 : banker.getX(), banker == null ? 0 : banker.getY(),
                bankerVisible, state.additions, state.removals, state.indices.length,
                state.limitDrops, state.widthDrops, state.frames);
    }

    static final class State {
        final int index, definitionId, x, y;
        final boolean visible;
        final long additions, removals;
        /** NPCs this viewer's client currently holds, and the ones the encoder refused to publish. */
        final int publishedCount;
        final long limitDrops, widthDrops, frames;
        State(int index, int definitionId, int x, int y, boolean visible, long additions, long removals,
              int publishedCount, long limitDrops, long widthDrops, long frames) {
            this.index = index; this.definitionId = definitionId; this.x = x; this.y = y;
            this.visible = visible; this.additions = additions; this.removals = removals;
            this.publishedCount = publishedCount; this.limitDrops = limitDrops;
            this.widthDrops = widthDrops; this.frames = frames;
        }
        @Override public String toString() {
            return "npc[banker=" + index + ",visible=" + visible + ",published=" + publishedCount
                    + ",adds=" + additions + ",removes=" + removals
                    + ",limitDrops=" + limitDrops + ",widthDrops=" + widthDrops + ",frames=" + frames + "]";
        }
    }

    private void checkThread() {
        if (Thread.currentThread() != owner) throw new IllegalStateException("NPC state belongs to the world thread");
    }
}
