package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.route.Flags;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in real-cache, encrypted-input bank probe; never accesses account saves. */
public final class Native950InteractionsSmoke {
    private static final int BANK_ID = 79036, BANK_X = 3215, BANK_Y = 3257;
    private static final int BACKPACK_HASH = (1473 << 16) | 5;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Native950InteractionsSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        captureLegacyDispatchBaseline();
        // Select a real walkable approach before the owning world thread starts.
        WorldTile start = findApproachStart();
        Native950Content content = content();
        Native950World world = Native950World.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        Native950Isaac client = new Native950Isaac(new int[] {947, 3, 2026, 517});
        Native950Isaac incoming = new Native950Isaac(new int[] {947, 3, 2026, 517});
        Native950Isaac outgoing = new Native950Isaac(new int[] {997, 53, 2076, 567});
        Native950Isaac expectedOutput = new Native950Isaac(new int[] {997, 53, 2076, 567});
        for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
        for (int i = 0; i < 2; i++) { outgoing.getAsInt(); expectedOutput.getAsInt(); }
        try {
            Native950World.SceneConfig scene = new Native950World.SceneConfig(start.getX(), start.getY(),
                    0, 1, 7, 0, 0, 0);
            CompletableFuture<Native950Session> attached = world.attach(channel, "interaction-smoke",
                    incoming, outgoing, new byte[] {0}, scene, Collections.emptyList(), content);
            awaitFuture(channel, attached);
            require(World.getPlayers().size() == 1 && World.getPlayers().get(1).isNative950(),
                    "Interactions must use the real native-profile Ataraxia Player");
            require(Arrays.equals(readFirst(channel), Native950Packets.initialSinglePlayerScene(1,
                    start.getX(), start.getY(), 0, 7, 0, 0, 0).frame(expectedOutput)),
                    "Live outgoing cipher position must survive attachment");
            Native950Session.Snapshot state = snapshot(world, channel);
            assertTotals(state, 1000, 5, 5);
            require(total(state.interactions.bank, 995) == 0 && total(state.interactions.inventory, 995) == 1000,
                    "Starter supplies must begin in the actual backpack");

            long rejected = state.interactions.rejectedActions;
            button(channel, client, 2, 517, 15, 0, 995);
            state = awaitRejected(world, channel, rejected);
            require(!state.interactions.bankOpen, "A fabricated bank button cannot open the bank");
            rejected = state.interactions.rejectedActions;
            object(channel, client, BANK_ID + 1, BANK_X, BANK_Y);
            state = awaitRejected(world, channel, rejected);
            rejected = state.interactions.rejectedActions;
            object(channel, client, BANK_ID, 4000, 4000);
            state = awaitRejected(world, channel, rejected);
            assertTotals(state, 1000, 5, 5);

            object(channel, client, BANK_ID, BANK_X, BANK_Y);
            state = await(world, channel, value -> value.interactions.bankOpen, 20000, "route to actual bank chest");
            require(state.steps > 0, "Opening the bank must route the real Player from the approach tile");
            System.out.println("PASS: encrypted object option 2 routes to cache chest and opens bank at " + state.x + "," + state.y);

            // Native prediction runs before the sender reads the actor ID.
            // Exhausted inventory actors become -1; exhausted bank actors
            // clear to 48447 before the client defers its layout refresh.
            rejected = state.interactions.rejectedActions;
            button(channel, client, 1, 517, 15, 0, -1);
            state = awaitRejected(world, channel, rejected);
            rejected = state.interactions.rejectedActions;
            button(channel, client, 7, 517, 15, 0, 48447);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.inventory, 995) == 1000 && total(state.interactions.bank, 995) == 0,
                    "Partial inventory empty markers and bank markers on inventory must be rejected");

            long transactions = state.interactions.transactions;
            button(channel, client, 1, 517, 15, 0, 995);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 995) == 999 && total(state.interactions.bank, 995) == 1,
                    "Default left-click deposit must transfer one actual coin into bank storage");
            assertTotals(state, 1000, 5, 5);

            rejected = state.interactions.rejectedActions;
            button(channel, client, 2, 517, 15, 0, 1511);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.inventory, 995) == 999 && total(state.interactions.bank, 995) == 1,
                    "A stale item claim must not change either container");
            rejected = state.interactions.rejectedActions;
            button(channel, client, 2, 517, 202, 0, 995);
            state = awaitRejected(world, channel, rejected);
            assertTotals(state, 1000, 5, 5);

            rejected = state.interactions.rejectedActions;
            button(channel, client, 1, 517, 201, 0, -1);
            state = awaitRejected(world, channel, rejected);
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 1, 995);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 995) == 1000 && total(state.interactions.bank, 995) == 0,
                    "Default left-click withdrawal must restore the coin and compact the bank");
            transactions = state.interactions.transactions;
            button(channel, client, 2, 517, 15, 1, 1511);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 1511) == 4 && total(state.interactions.bank, 1511) == 1,
                    "A non-stackable log must transfer from the selected backpack slot");
            rejected = state.interactions.rejectedActions;
            button(channel, client, 2, 517, 201, 0, 995);
            state = awaitRejected(world, channel, rejected);
            assertTotals(state, 1000, 5, 5);
            System.out.println("PASS: actual backpack/bank deposits, withdrawals, stale IDs and wrong components preserve quantities");

            rejected = state.interactions.rejectedActions;
            drag(channel, client, BACKPACK_HASH, 0, -1, BACKPACK_HASH, 27, 995);
            state = awaitRejected(world, channel, rejected);
            require(state.interactions.inventory.ids[0] == 995 && state.interactions.inventory.ids[27] == -1,
                    "Backpack drag must be refused while banking");
            final long oldSteps = state.steps;
            walk(channel, client, start.getX(), start.getY());
            state = await(world, channel, value -> !value.interactions.bankOpen && value.steps > oldSteps,
                    10000, "walking must close banking before movement");
            transactions = state.interactions.transactions;
            drag(channel, client, BACKPACK_HASH, 0, -1, BACKPACK_HASH, 27, 995);
            state = awaitTransactions(world, channel, transactions);
            require(state.interactions.inventory.ids[0] == -1 && state.interactions.inventory.ids[27] == 995
                            && state.interactions.inventory.amounts[27] == 1000,
                    "Encrypted drag must move the whole stack in actual inventory storage");
            rejected = state.interactions.rejectedActions;
            // A duplicate post-swap pair cannot reverse or repeat the move.
            drag(channel, client, BACKPACK_HASH, 0, -1, BACKPACK_HASH, 27, 995);
            state = awaitRejected(world, channel, rejected);
            rejected = state.interactions.rejectedActions;
            // Legacy pre-swap item orientation is not native947 semantics.
            drag(channel, client, BACKPACK_HASH, 27, 995, BACKPACK_HASH, 26, -1);
            state = awaitRejected(world, channel, rejected);
            rejected = state.interactions.rejectedActions;
            drag(channel, client, BACKPACK_HASH + 1, 27, -1, BACKPACK_HASH, 26, 995);
            state = awaitRejected(world, channel, rejected);
            assertTotals(state, 1000, 5, 5);
            require(state.interactions.inventory.ids[27] == 995 && state.interactions.inventory.amounts[27] == 1000,
                    "Duplicate, pre-swap and wrong-component drags cannot move the new inventory state");
            System.out.println("PASS: walking closes bank; native post-swap drag moves stacks and rejects duplicate, legacy or foreign claims");

            object(channel, client, BANK_ID, BANK_X, BANK_Y);
            state = await(world, channel, value -> value.interactions.bankOpen, 15000, "reopen bank after moving");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 2, 1511);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 1511) == 5 && total(state.interactions.bank, 1511) == 0,
                    "Non-stackable withdrawal must restore the stored log");
            assertTotals(state, 1000, 5, 5);

            // Start at the final matching slot: deposit-5 must gather the other
            // four independent inventory items, including earlier slots.
            transactions = state.interactions.transactions;
            button(channel, client, 3, 517, 15, lastSlotOf(state.interactions.inventory, 1511), -1);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 1511) == 0 && total(state.interactions.bank, 1511) == 5,
                    "Deposit-5 must collect five separate logs rather than only the clicked inventory slot");
            require(total(state.interactions.inventory, 315) == 5 && total(state.interactions.inventory, 995) == 1000,
                    "Bulk log deposit must preserve unrelated inventory items");
            rejected = state.interactions.rejectedActions;
            button(channel, client, 1, 517, 201, slotOf(state.interactions.bank, 1511), 48447);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.bank, 1511) == 5,
                    "Bank empty-actor marker cannot authorize a partial withdrawal");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 7, 1511);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 1511) == 5 && total(state.interactions.bank, 1511) == 0,
                    "Withdraw-all must restore all five separately stored logs");

            transactions = state.interactions.transactions;
            button(channel, client, 7, 517, 15, lastSlotOf(state.interactions.inventory, 315), 315);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 315) == 0 && total(state.interactions.bank, 315) == 5,
                    "Item deposit-all must collect every matching shrimp across separate inventory slots");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 7, 315);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 315) == 5 && total(state.interactions.bank, 315) == 0,
                    "Withdraw-all must restore every shrimp after item deposit-all");
            assertTotals(state, 1000, 5, 5);
            System.out.println("PASS: left-click transfers and non-stackable deposit-5/deposit-all/withdraw-all conserve complete item groups");

            button(channel, client, 1, 517, 317, -1, -1);
            state = await(world, channel, value -> !value.interactions.bankOpen, 5000, "explicit bank close button");
            rejected = state.interactions.rejectedActions;
            button(channel, client, 1, 517, 39, -1, -1);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.inventory, 995) == 1000 && total(state.interactions.bank, 995) == 0,
                    "Deposit-all after explicit close must be rejected until the bank reopens");

            object(channel, client, BANK_ID, BANK_X, BANK_Y);
            state = await(world, channel, value -> value.interactions.bankOpen, 10000, "reopen after explicit close");
            transactions = state.interactions.transactions;
            button(channel, client, 1, 517, 39, -1, -1);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 995) == 0 && total(state.interactions.inventory, 1511) == 0
                            && total(state.interactions.inventory, 315) == 0,
                    "Cache-verified deposit-all button must clear all occupied backpack slots");
            require(total(state.interactions.bank, 995) == 1000 && total(state.interactions.bank, 1511) == 5
                            && total(state.interactions.bank, 315) == 5,
                    "Deposit-all must combine non-stackable bank entries and preserve every item");

            // The native All Items tab compacts actor values before sending.
            // Exercise the first valid post-compaction ID, its duplicate, and
            // another otherwise-correct claim against the now-changed slot.
            final long beforePairTick = state.ticks;
            state = await(world, channel, value -> value.ticks > beforePairTick, 2000, "fresh tick before compacted-slot batch");
            final int firstBankItem = state.interactions.bank.ids[0];
            final long firstBankAmount = total(state.interactions.bank, firstBankItem);
            final long beforePairTransactions = state.interactions.transactions;
            final long beforePairRejected = state.interactions.rejectedActions;
            final int firstPredictedItem = predictedBankItem(state.interactions.bank, 0, Integer.MAX_VALUE);
            final int secondPredictedItem = predictedBankItem(state.interactions.bank, 1, Integer.MAX_VALUE);
            bankExhaustionBatch(channel, client, 0, firstPredictedItem, secondPredictedItem);
            state = await(world, channel, value -> value.interactions.transactions > beforePairTransactions
                    && value.interactions.rejectedActions >= beforePairRejected + 2, 5000, "same-tick compacted-slot rejection");
            require(state.interactions.transactions == beforePairTransactions + 1
                            && state.interactions.rejectedActions == beforePairRejected + 2,
                    "Repeated and newly valid claims against a compacted slot must be refused in the same tick");
            for (int id : new int[] {995, 1511, 315}) {
                long expectedInventory = id == firstBankItem ? firstBankAmount : 0;
                require(total(state.interactions.inventory, id) == expectedInventory,
                        "Repeated alias must preserve every item after the original compacted slot");
            }
            assertTotals(state, 1000, 5, 5);
            // Even on a later tick, the original exhaustion's positive ID is
            // now the next item's pre-click ID, not its valid post-click ID.
            rejected = state.interactions.rejectedActions;
            button(channel, client, 7, 517, 201, 0, firstPredictedItem);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.inventory, firstPredictedItem) == 0,
                    "Delayed duplicate exhaustion must not withdraw the next compacted item");
            transactions = state.interactions.transactions;
            button(channel, client, 1, 517, 39, -1, -1);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, firstBankItem) == 0,
                    "Deposit-all must restore the alias probe's withdrawn item before bulk withdrawal checks");
            System.out.println("PASS: native post-compaction IDs, empty markers, delayed duplicates and same-tick slot barriers preserve every item");

            rejected = state.interactions.rejectedActions;
            button(channel, client, 3, 517, 201, slotOf(state.interactions.bank, 1511), 1511);
            state = awaitRejected(world, channel, rejected);
            require(total(state.interactions.bank, 1511) == 5,
                    "An exhausting withdrawal cannot use the old positive pre-click item ID");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 1, 995);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 995) == 1 && total(state.interactions.bank, 995) == 999,
                    "A partial withdrawal must retain and validate the original positive item ID");

            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 3, 1511);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 1511) == 5 && total(state.interactions.bank, 1511) == 0,
                    "Withdraw-5 must restore five separate non-stackable logs");
            for (int slot = 0; slot < state.interactions.inventory.ids.length; slot++)
                if (state.interactions.inventory.ids[slot] == 1511)
                    require(state.interactions.inventory.amounts[slot] == 1, "Non-stackable withdrawal must use separate slots");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 4, 315);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 315) == 5 && total(state.interactions.bank, 315) == 0,
                    "Withdraw-10 must move only the five available shrimps");
            transactions = state.interactions.transactions;
            withdraw(channel, client, state, 7, 995);
            state = awaitTransactions(world, channel, transactions);
            require(total(state.interactions.inventory, 995) == 1000 && total(state.interactions.bank, 995) == 0,
                    "Verified withdraw-all option must restore the entire coin stack");
            assertTotals(state, 1000, 5, 5);
            System.out.println("PASS: explicit close rejects bank actions until reopened; deposit-all and bulk withdrawals conserve all items");

            // CLOSE_MODAL is a distinct zero-length native packet, not the bank's
            // explicit close button. A queued withdrawal must not survive it.
            rejected = state.interactions.rejectedActions;
            transactions = state.interactions.transactions;
            input(channel, client, 55, new byte[0]);
            button(channel, client, 1, 517, 201, 0, 995);
            state = awaitRejected(world, channel, rejected);
            require(!state.interactions.bankOpen && state.interactions.transactions == transactions,
                    "Client modal close must retire bank state before a queued stale withdrawal");
            long afterCloseTick = state.ticks;
            state = await(world, channel, value -> value.ticks > afterCloseTick, 5000, "tick after native modal close");
            require(!state.interactions.bankOpen, "910 bookkeeping must not reopen a client-closed bank on the next tick");
            System.out.println("PASS: native CLOSE_MODAL clears bank state, rejects queued withdrawals and stays closed");

            state = verifyRunOrbPace(world, channel, client, attached.get(), state);

            require(state.unhandledFrames == 0, "All smoke input packets must use verified semantic decoders");
            // A frame that decodes and then reaches no handler used to vanish silently. It is
            // counted now, and a smoke that exercises the real interaction surface is exactly
            // where a new decoded-but-unrouted action type should be noticed.
            require(state.interactions.unhandledActions == 0,
                    "Every decoded action must reach a handler: " + state.interactions.unhandledActionReport);
            // The two fabricated components 517:202 and 1473:6 above are the only
            // unbound pairs this smoke sends; both must be counted rejections.
            reportRouting("interactions", state, 2, 3, 0, 15);
            channel.close();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (world.reservedSlots() > 0 && System.nanoTime() < deadline) { pump(channel); Thread.sleep(20); }
            require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "Disconnect must remove the temporary real character");
            System.out.println("PASS: real-cache interaction smoke, conserved coins=1000 logs=5 shrimps=5, disconnect cleanup");
        } finally { channel.finishAndReleaseAll(); }
    }

    /** Runs real encrypted orb clicks and the session's production movement phase. */
    private static Native950Session.Snapshot verifyRunOrbPace(Native950World world, EmbeddedChannel channel,
            Native950Isaac cipher, Native950Session session, Native950Session.Snapshot state) throws Exception {
        require(!state.running, "The transient smoke character must begin walking");
        int walked = movementPhaseDistance(world, session);
        require(walked == 1, "Walk mode must advance one tile per movement phase, got " + walked);
        button(channel, cipher, 1, 1465, 15, -1, -1);
        state = await(world, channel, value -> value.running, 5000, "native run orb toggles the real run state");
        int ran = movementPhaseDistance(world, session);
        require(ran == 2, "Run mode must advance two tiles per movement phase, got " + ran);
        button(channel, cipher, 1, 1465, 15, -1, -1);
        state = await(world, channel, value -> !value.running, 5000, "native run orb returns to walking");
        require(movementPhaseDistance(world, session) == 1, "Second orb click must restore walking pace");
        require(state.interactions.routerReport.contains("button:minimap.run_orb=2"),
                "Both run clicks must reach the translated 910 button handler");
        System.out.println("PASS: native 1465:15 maps to 910 1465:11; walk/run/walk advances 1/2/1 tiles per tick");
        return snapshot(world, channel);
    }

    private static int movementPhaseDistance(Native950World world, Native950Session session) throws Exception {
        return world.execute(() -> {
            com.rs.game.player.Player player = World.getPlayers().get(1);
            player.resetWalkSteps();
            int x = player.getX(), y = player.getY();
            int dx = 0, dy = 0;
            for (int direction = 0; direction < 8; direction++) {
                int nextDx = com.rs.utils.Utils.DIRECTION_DELTA_X[direction];
                int nextDy = com.rs.utils.Utils.DIRECTION_DELTA_Y[direction];
                if (World.checkWalkStep(player.getPlane(), x, y, direction, player.getSize())
                        && World.checkWalkStep(player.getPlane(), x + nextDx, y + nextDy, direction, player.getSize())) {
                    dx = nextDx; dy = nextDy; break;
                }
            }
            require(dx != 0 || dy != 0, "Pace probe needs two real, collision-checked walkable tiles");
            require(player.addWalkSteps(x + 2 * dx, y + 2 * dy, 2, true), "Pace probe must queue two steps");
            require(player.getWalkSteps().size() == 2, "Pace probe must start with exactly two queued steps");
            session.tickMove();
            int distance = Math.max(Math.abs(player.getX() - x), Math.abs(player.getY() - y));
            player.resetWalkSteps();
            return distance;
        }).get(5, TimeUnit.SECONDS);
    }

    private static WorldTile findApproachStart() {
        WorldObject bank = null;
        int region = new WorldTile(BANK_X, BANK_Y, 0).getRegionId();
        for (WorldObject object : World.getRegion(region, true).getObjects().values())
            if (object.getId() == BANK_ID && object.getX() == BANK_X && object.getY() == BANK_Y && object.getPlane() == 0)
                bank = object;
        require(bank != null && bank.getDefinitions().options != null
                        && bank.getDefinitions().options.length > 1 && "Use".equals(bank.getDefinitions().options[1]),
                "Expected the cache-verified Use option on bank chest 79036 at 3215,3257,0");
        for (int radius = 4; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
                int x = BANK_X + dx, y = BANK_Y + dy;
                World.getRegion(new WorldTile(x, y, 0).getRegionId(), true);
                if ((World.getMask(0, x, y) & (Flags.OBJ | Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK)) != 0) continue;
                if (RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, x, y, 0, 1, new ObjectStrategy(bank), false) > 0
                        && !RouteFinder.lastIsAlternative()) {
                    System.out.println("Real-cache bank approach starts at " + x + "," + y + ",0");
                    return new WorldTile(x, y, 0);
                }
            }
        }
        throw new AssertionError("No real collision-safe approach to the verified bank chest");
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] deposits = new int[11], withdrawals = new int[11];
        deposits[1] = withdrawals[1] = 1;
        deposits[2] = withdrawals[2] = 1; deposits[3] = withdrawals[3] = 5;
        deposits[4] = withdrawals[4] = 10; deposits[7] = withdrawals[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                deposits, withdrawals, Collections.emptyList(), Collections.emptyList()));
    }

    private static void object(EmbeddedChannel channel, Native950Isaac cipher, int id, int x, int y) {
        input(channel, cipher, 11, new byte[] {(byte) (x >>> 8), (byte) (x + 128), 0,
                (byte) y, (byte) (y >>> 8), (byte) (id >>> 16), (byte) (id >>> 24), (byte) id, (byte) (id >>> 8)});
    }
    private static void button(EmbeddedChannel channel, Native950Isaac cipher, int option, int iface, int component, int slot, int item) {
        int[] opcodes = {96, 77, 4, 95, 29, 51, 5, 21, 18, 36};
        int hash = (iface << 16) | component;
        input(channel, cipher, opcodes[option - 1], new byte[] {(byte) (item >>> 8), (byte) item,
                (byte) (hash >>> 8), (byte) hash, (byte) (hash >>> 24), (byte) (hash >>> 16),
                (byte) (slot >>> 8), (byte) slot});
    }
    private static void withdraw(EmbeddedChannel channel, Native950Isaac cipher,
                                 Native950Session.Snapshot state, int option, int itemId) {
        int quantity;
        switch (option) {
            case 1: case 2: quantity = 1; break;
            case 3: quantity = 5; break;
            case 4: quantity = 10; break;
            case 7: quantity = Integer.MAX_VALUE; break;
            default: throw new AssertionError("Unsupported smoke withdrawal option " + option);
        }
        int slot = slotOf(state.interactions.bank, itemId);
        button(channel, cipher, option, 517, 201, slot, predictedBankItem(state.interactions.bank, slot, quantity));
    }
    private static int predictedBankItem(Native950Containers.Snapshot bank, int slot, int quantity) {
        require(slot >= 0 && slot < bank.ids.length && bank.ids[slot] >= 0, "Prediction requires an occupied bank slot");
        if (quantity < bank.amounts[slot]) return bank.ids[slot];
        return Native950ActionRouter.EMPTY_BANK_ACTOR; // exhausted clicked actor reads as48447
    }
    private static void bankExhaustionBatch(EmbeddedChannel channel, Native950Isaac cipher, int slot,
                                             int firstPredictedItem, int secondPredictedItem) {
        int hash = (517 << 16) | 201;
        ByteBuf frames = Unpooled.buffer(27);
        for (int item : new int[] {firstPredictedItem, firstPredictedItem, secondPredictedItem}) {
            frames.writeByte(5 + cipher.getAsInt()); // option 7: Withdraw-All
            frames.writeShort(item);
            frames.writeByte(hash >>> 8).writeByte(hash).writeByte(hash >>> 24).writeByte(hash >>> 16);
            frames.writeShort(slot);
        }
        channel.writeInbound(frames);
    }
    private static void drag(EmbeddedChannel channel, Native950Isaac cipher, int sourceHash, int sourceSlot, int sourceItem,
                             int targetHash, int targetSlot, int targetItem) {
        input(channel, cipher, 40, new byte[] {(byte) (sourceItem >>> 8), (byte) (sourceItem + 128),
                (byte) sourceHash, (byte) (sourceHash >>> 8), (byte) (sourceHash >>> 16), (byte) (sourceHash >>> 24),
                (byte) (targetHash >>> 24), (byte) (targetHash >>> 16), (byte) (targetHash >>> 8), (byte) targetHash,
                (byte) (targetItem + 128), (byte) (targetItem >>> 8), (byte) targetSlot, (byte) (targetSlot >>> 8),
                (byte) (sourceSlot >>> 8), (byte) sourceSlot});
    }
    private static void walk(EmbeddedChannel channel, Native950Isaac cipher, int x, int y) {
        input(channel, cipher, 3, new byte[] {(byte) (y >>> 8), (byte) (y + 128), (byte) (x >>> 8), (byte) x, (byte) 128});
    }
    private static void input(EmbeddedChannel channel, Native950Isaac cipher, int opcode, byte[] payload) {
        require(channel.isActive(), "Cannot send input to a disconnected smoke session");
        channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + cipher.getAsInt())}));
        // Separate opcode and payload deliberately exercise native packet fragmentation.
        channel.writeInbound(Unpooled.wrappedBuffer(payload));
    }

    private static Native950Session.Snapshot awaitRejected(Native950World world, EmbeddedChannel channel, long previous) throws Exception {
        Native950Session.Snapshot state = await(world, channel, value -> value.interactions.rejectedActions > previous, 5000, "rejected action");
        assertTotals(state, 1000, 5, 5);
        return state;
    }
    private static Native950Session.Snapshot awaitTransactions(Native950World world, EmbeddedChannel channel, long previous) throws Exception {
        Native950Session.Snapshot state = await(world, channel, value -> value.interactions.transactions > previous, 5000, "container transaction");
        require(state.interactions.transactions == previous + 1, "Exactly one input must perform exactly one transaction");
        assertTotals(state, 1000, 5, 5);
        return state;
    }
    private static Native950Session.Snapshot await(Native950World world, EmbeddedChannel channel,
            Predicate<Native950Session.Snapshot> condition, long timeoutMs, String description) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        Native950Session.Snapshot state;
        do {
            state = snapshot(world, channel);
            if (condition.test(state)) return state;
            Thread.sleep(20);
        } while (System.nanoTime() < deadline);
        throw new AssertionError("Timed out: " + description + "; last state=" + state);
    }
    private static Native950Session.Snapshot snapshot(Native950World world, EmbeddedChannel channel) throws Exception {
        pump(channel);
        Native950Session.Snapshot state = world.snapshot().get(2, TimeUnit.SECONDS);
        require(state != null && state.active && state.interactions != null, "Expected a live interaction-enabled session");
        return state;
    }
    private static void awaitFuture(EmbeddedChannel channel, CompletableFuture<?> future) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!future.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
        future.get(1, TimeUnit.SECONDS);
    }
    private static void assertTotals(Native950Session.Snapshot state, long coins, long logs, long shrimps) {
        int[] ids = {995, 1511, 315}; long[] expected = {coins, logs, shrimps};
        for (int i = 0; i < ids.length; i++) require(total(state.interactions.inventory, ids[i])
                + total(state.interactions.bank, ids[i]) == expected[i], "Item conservation failed for " + ids[i]);
    }
    private static long total(Native950Containers.Snapshot state, int id) {
        long total = 0;
        for (int slot = 0; slot < state.ids.length; slot++) if (state.ids[slot] == id) total += state.amounts[slot];
        return total;
    }
    private static int slotOf(Native950Containers.Snapshot state, int id) {
        for (int slot = 0; slot < state.ids.length; slot++) if (state.ids[slot] == id) return slot;
        throw new AssertionError("Expected item " + id + " in container");
    }
    private static int lastSlotOf(Native950Containers.Snapshot state, int id) {
        for (int slot = state.ids.length - 1; slot >= 0; slot--) if (state.ids[slot] == id) return slot;
        throw new AssertionError("Expected item " + id + " in container");
    }
    private static byte[] readFirst(EmbeddedChannel channel) {
        Object output = channel.readOutbound();
        require(output instanceof ByteBuf, "Expected a native output frame");
        try { ByteBuf bytes = (ByteBuf) output; byte[] result = new byte[bytes.readableBytes()]; bytes.readBytes(result); return result; }
        finally { ReferenceCountUtil.release(output); }
    }
    private static void pump(EmbeddedChannel channel) {
        channel.runPendingTasks();
        Object output;
        while ((output = channel.readOutbound()) != null) ReferenceCountUtil.release(output);
        channel.checkException();
    }
    private static void require(boolean condition, String description) { if (!condition) throw new AssertionError(description); }

    /**
     * The 910 decoded-dispatch counters at the moment a smoke starts acting.
     * {@link #reportRouting} asserts deltas against this, not absolutes, so the
     * check survives a JVM that already routed something (the equipment smoke's
     * restart child, a future combined runner).
     */
    private static volatile long[] legacyBaseline;

    /** Called by each smoke once the cache is open and before it sends any action. */
    static void captureLegacyDispatchBaseline() {
        legacyBaseline = new long[] {
                com.rs.network.packet.impl.ObjectHandler.DISPATCHES.get(),
                com.rs.network.packet.impl.NPCHandler.DISPATCHES.get(),
                com.rs.network.packet.impl.ButtonHandler.DISPATCHES.get() };
    }

    private static long legacyDelta(int index, long current) {
        long[] baseline = legacyBaseline;
        return baseline == null ? current : current - baseline[index];
    }

    /**
     * P5 routing evidence: the three 910 decoded-dispatch counters (proof the 910
     * handler bodies ran rather than a bypass), the router's own counters, the
     * facade's strict/dropped tallies and the NO-OP tier's discarded-id inventory.
     * Fails closed when a strict-tier packet reached the wire, when a 910 handler
     * threw, when more (947 interface, component) pairs reached the router without
     * a verified binding than the fabricated ones this smoke deliberately injects,
     * or when the 910 handlers did not actually run.
     *
     * <p>The dispatch minimums are the point of M2b acceptance (b): the router's
     * own {@code routerDispatches} counter is satisfied by paths that touch no 910
     * handler at all (npcTalk / npcCollect / npcExamine run controller hooks only,
     * see notes/P5-router.md section 5), so a regression back to a hand-built
     * bypass would leave it green. Requiring the {@code ObjectHandler} /
     * {@code ButtonHandler} deltas measures the 910 dispatch instead of printing it.
     *
     * @param expectedUnmatchedPairs unbound pairs the smoke itself sent on purpose
     * @param minObjectDispatches minimum {@code ObjectHandler.dispatch} calls this smoke must cause
     * @param minNpcDispatches minimum {@code NPCHandler} decoded-option calls (0 until M7/P6, see the note)
     * @param minButtonDispatches minimum {@code ButtonHandler.handleButtons} calls this smoke must cause
     */
    static void reportRouting(String smoke, Native950Session.Snapshot state, long expectedUnmatchedPairs,
                              long minObjectDispatches, long minNpcDispatches, long minButtonDispatches) {
        long objects = legacyDelta(0, com.rs.network.packet.impl.ObjectHandler.DISPATCHES.get());
        long npcs = legacyDelta(1, com.rs.network.packet.impl.NPCHandler.DISPATCHES.get());
        long buttons = legacyDelta(2, com.rs.network.packet.impl.ButtonHandler.DISPATCHES.get());
        System.out.println("[P5] " + smoke + ": " + Native950ActionRouter.legacyDispatchCounters());
        System.out.println("[P5] " + smoke + ": 910 dispatch deltas ObjectHandler=" + objects
                + " (min " + minObjectDispatches + ") NPCHandler=" + npcs + " (min " + minNpcDispatches + ")"
                + " ButtonHandler=" + buttons + " (min " + minButtonDispatches + ")");
        System.out.println("[P5] " + smoke + ": facade sent=" + state.facadeSent + " noops=" + state.facadeNoops
                + " dropped=" + state.facadeDropped + " strictHits=" + state.facadeStrictHits);
        System.out.println("[P5] " + smoke + ": facade drops by method " + new java.util.TreeMap<String, Long>(state.droppedByMethod));
        System.out.println("[P5] " + smoke + ": facade no-ops by method " + new java.util.TreeMap<String, Long>(state.noopsByMethod));
        System.out.println("[P5] " + smoke + ": discarded no-op ids " + state.noopIdsByMethod);
        System.out.println("[P5] " + smoke + ": " + state.interactions.routerReport);
        require(state.facadeStrictHits == 0, "A strict-tier packet reached the wire during the " + smoke + " smoke");
        require(state.interactions.handlerFailures == 0, "A 910 handler threw during the " + smoke + " smoke");
        require(state.interactions.unmatchedPairs == expectedUnmatchedPairs,
                "The " + smoke + " smoke saw " + state.interactions.unmatchedPairs + " unbound 947 (interface, component)"
                        + " pairs, expected exactly the " + expectedUnmatchedPairs + " it fabricates on purpose");
        require(state.interactions.routerDispatches > 0,
                "No action reached the router during the " + smoke + " smoke");
        require(objects >= minObjectDispatches, "The " + smoke + " smoke drove ObjectHandler.dispatch " + objects
                + " times, expected at least " + minObjectDispatches + ": the 910 object handler did not run");
        require(npcs >= minNpcDispatches, "The " + smoke + " smoke drove NPCHandler " + npcs
                + " times, expected at least " + minNpcDispatches + ": the 910 NPC handler did not run");
        require(buttons >= minButtonDispatches, "The " + smoke + " smoke drove ButtonHandler.handleButtons " + buttons
                + " times, expected at least " + minButtonDispatches + ": the 910 button handler did not run");
    }

    private Native950InteractionsSmoke() { }
}
