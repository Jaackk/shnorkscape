package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.network.protocol.modern950.Native950Packets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

/** One server-owned bank quantity request; the caller owns bank/dialogue lifecycle and mutation. */
public final class Native950QuantityInput {
    public enum Kind { WITHDRAW, DEPOSIT, DEFAULT }
    public static final int FRAME_HOST = 749, FRAME = 1418, INPUT_HOST = 2, INPUT = 1469;
    public static final int MODE = 17;
    private static volatile boolean verified;
    private final Thread owner = Thread.currentThread();
    private final Runnable verifier;
    private Pending pending;

    public Native950QuantityInput() { this(Native950QuantityInput::verify); }
    Native950QuantityInput(Runnable verifier) { this.verifier = Objects.requireNonNull(verifier, "verifier"); }

    /** Option 6 predicts quantity zero: its packet must still claim the original item. */
    public boolean beginWithdraw(long bankEpoch, int slot, int claimedItem, Native950Containers.Snapshot bank) {
        return begin(Kind.WITHDRAW,bankEpoch,slot,claimedItem,bank);
    }
    public boolean beginDeposit(long bankEpoch,int slot,int claimedItem,Native950Containers.Snapshot inventory) {
        return begin(Kind.DEPOSIT,bankEpoch,slot,claimedItem,inventory);
    }
    public boolean beginDefault(long bankEpoch,Native950Containers.Snapshot bank) {
        return begin(Kind.DEFAULT,bankEpoch,-1,-1,bank);
    }
    private boolean begin(Kind kind,long bankEpoch,int slot,int claimedItem,Native950Containers.Snapshot state) {
        checkOwner();
        if(pending!=null||state==null||state.ids.length!=state.amounts.length)return false;
        if(kind!=Kind.DEFAULT&&(slot<0||slot>=state.ids.length||claimedItem<0
                ||state.ids[slot]!=claimedItem||state.amounts[slot]<1))return false;
        verifier.run(); // A changed cache cannot acquire a request or display a prompt.
        pending=new Pending(kind,bankEpoch,slot,claimedItem,state);
        return true;
    }
    public Kind kind(){checkOwner();return pending==null?null:pending.kind;}
    public boolean active() { checkOwner(); return pending != null; }
    public void cancel() { checkOwner(); pending = null; }

    /**
     * Retire ownership before validating the untrusted signed 64-bit reply. The
     * capacity callback receives (slot, stock-capped request) and must inspect
     * current destination capacity. DEFAULT is a preference and skips capacity.
     * There is no request ID on the wire: a reply cannot identify an older
     * prompt once another prompt is active. Never replace an active request.
     */
    public Accepted consume(long count, long bankEpoch, Native950Containers.Snapshot bank,
                            IntBinaryOperator capacity) {
        checkOwner();
        Pending request = pending;
        pending = null;
        if (request == null || count < 1 || count > Integer.MAX_VALUE
                || request.bankEpoch != bankEpoch || bank == null
                || !Arrays.equals(request.ids, bank.ids) || !Arrays.equals(request.amounts, bank.amounts)) return null;
        if(request.kind==Kind.DEFAULT)return new Accepted(request.kind,-1,-1,(int)count);
        long stock=request.amounts[request.slot];
        if(request.kind==Kind.DEPOSIT) {
            stock=0;
            for(int slot=0;slot<request.ids.length;slot++)if(request.ids[slot]==request.itemId)
                stock=Math.min((long)Integer.MAX_VALUE,stock+request.amounts[slot]);
        }
        int requested=(int)Math.min(count,stock);
        int amount=Math.min(requested,Objects.requireNonNull(capacity,"capacity").applyAsInt(request.slot,requested));
        return amount<1?null:new Accepted(request.kind,request.slot,request.itemId,amount);
    }

    /** Native input mode 17 emits the verified eight-byte COUNT_DIALOGUE packet. */
    public List<Native950Packets.Packet> promptPackets() {
        checkOwner();
        if (pending == null) throw new IllegalStateException("Quantity prompt requires an owned request");
        return Arrays.asList(
                Native950Packets.openSub(1477, FRAME_HOST, FRAME, true),
                Native950Packets.openSub(FRAME, INPUT_HOST, INPUT, true),
                Native950Packets.hideInterface(1477, 747, false),
                Native950Packets.runClientScript(17396, pending.kind==Kind.DEPOSIT ? "How many would you like to deposit?"
                        : pending.kind==Kind.DEFAULT ? "Set the default bank quantity:" : "How many would you like to withdraw?"));
    }

    /** Use after cancel or consume, while this adapter still owns the visible input frame. */
    public List<Native950Packets.Packet> cancelPackets() {
        checkOwner();
        return Arrays.asList(
                Native950Packets.runClientScript(1548, MODE),
                Native950Packets.closeSub(FRAME, INPUT_HOST),
                Native950Packets.closeSub(1477, FRAME_HOST),
                Native950Packets.hideInterface(1477, 747, true),
                Native950Packets.runClientScript(1364));
    }

    private void checkOwner() {
        if (Thread.currentThread() != owner) throw new IllegalStateException("Quantity request belongs to the world thread");
    }

    private static final class Pending {
        final Kind kind;
        final long bankEpoch;
        final int slot, itemId;
        final int[] ids, amounts;
        Pending(Kind kind,long bankEpoch, int slot, int itemId, Native950Containers.Snapshot bank) {
            this.kind=kind; this.bankEpoch = bankEpoch; this.slot = slot; this.itemId = itemId;
            ids = bank.ids.clone(); amounts = bank.amounts.clone();
        }
    }

    public static final class Accepted {
        public final Kind kind;
        public final int slot, itemId, amount;
        private Accepted(Kind kind,int slot, int itemId, int amount) { this.kind=kind; this.slot = slot; this.itemId = itemId; this.amount = amount; }
    }

    public static synchronized void verify() {
        if (verified) return;
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Quantity input requires the paired flat cache");
        verifyFiles((index, group, file) -> Cache.STORE.getIndexes()[index].getFile(group, file));
        verified = true;
    }

    @FunctionalInterface interface CacheFiles { byte[] read(int index, int group, int file); }

    static void verifyFiles(CacheFiles files) {
        for (String entry : PINS) {
            String[] parts = entry.split(" ");
            String[] key = parts[0].split("/");
            byte[] data = files.read(Integer.parseInt(key[0]), Integer.parseInt(key[1]), Integer.parseInt(key[2]));
            if (data == null) throw new IllegalStateException("Missing quantity-input cache file " + parts[0]);
            try {
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
                StringBuilder actual = new StringBuilder();
                for (byte b : digest) actual.append(String.format("%02x", b & 255));
                // Routed through the shared gate rather than throwing directly, so this pin obeys
                // the same -Dataraxia.native.verifyCache switch as every other one. A fail-closed
                // set with one member that ignores the switch is worse than none: the operator
                // turns it off, gets past four verifiers, and is stopped by the fifth for the same
                // underlying reason.
                NativeCacheVerification.requireBinding(
                        "Quantity-input", parts[0], parts[1], actual.toString());
            } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        }
    }

    // Logical decompressed files, independently decoded in QUANTITY_INPUT.md and its verifier.
    private static final String[] PINS = {
        "3/1477/747 adc008cab54211505395ce179225d9e4ae3d2af375c6f4701ef82d2c7ea2c2f6",
        "3/1477/749 46bbbc3d79fe70d28b3293d8f49ccd2d532d032f00c76141a7f53418ccb64947",
        "3/1418/0 8442db47f15df87b08b8846ccf87762f3bafb1a23647b0b8fc9afb249816b3de",
        "3/1418/1 ad3df35bdbafc56d1becd2848fd2baac4e3feb433d4342e457c2754d9fe98d3a",
        "3/1418/2 4f08c684574cdba1b7033f6d1c64358e8c07a2cb86c67152c409b0bfeb76bcbd",
        "3/1418/3 189586b6f03df727cf66ef55c386519e87b2f7c51b5828b33b0fe82136b39357",
        "3/1418/4 8f8b9fdabc842d813cf3403aeb312a73f29e05faa11fb5b1d2fa9375e3cef16e",
        "3/1469/0 bd54df87065cb96d7412d337a6d16fc0805e7bf6ec5395af22376132d70646ff",
        "3/1469/1 ec3d85ed8dc97dfc7f6765700d7a5eda2931d0474d9b57c16370ce84b6ea8e1e",
        "3/1469/2 0032ffae022757dec82e6d8b875e58b8363368eff76904b1f6a96f4994b05634",
        "3/1469/3 bfe6333609c0e4d5f857b4cfff9ff53764a5de713071daf4bece0d98fcf46e91",
        "3/1469/4 675e9db10cb8fccdb24ec6b14248c4fbd5c3d78e81d20fac77812cadb357fee0",
        "3/1469/5 5a815fe09a146392ea55e666c7a8691f2521a65521b1efd702a3e027fa19c8da",
        "12/17396/0 d7bdd3cbfa358860961497d8712b4e825c943114e9ea948a687ab7f5a000ff96",
        "12/112/0 ae0802578ec2275c3db6a2ad9ec1fca9063999928339d722a8c1812aa0e394c0",
        "12/1564/0 100e717f152fd448269c873796bb77a34d7ac82cf0325c6495c4f0b5e03a841c",
        "12/1548/0 bc46029b989f4096ae321ce33cb0c1c7f9d870ae1ad10f8577e5958f7ec6c40e",
        "12/1364/0 fc33d9cd243a268438b22832706f89fc0b86e306e8eee1a85d41b859d8aac1f7",
        "12/8536/0 76ec5668e5702345d9d977d063873f6d2dd32b5b6c0f20194096dcd8546f2d4e",
        "12/17393/0 60ec8d1999a9d10a755a684ae1c236fb419017db97c0a5e9dec3b10d55e6a5eb",
        "12/17398/0 9cff3fc6524f41a66e44530b7d990e55cb010501d37e2c5649d51e0c1b32142d",
        "12/1750/0 fa9f684db356b2028ec09b78561b071a0594e1e197e6d8662e7a8c3c15053128",
        "12/11726/0 b6b5759dd69e7e227041f1505cdfe4c3defb8ebb401355abd72951c3519a2d8d",
        "12/8421/0 538f76796f8ad460ef5dc918ed036c69fba0397def38049c87a96c43fb50bf3a",
        "12/13798/0 6c82ce1618e069e0e2a5e3946e867b7fee8f454119badcd275abed001aae60ed",
        "12/6793/0 e27c0ddee4502d816c836a1c47ac5aa5806cdd0a882958f84daf3baa0eeb4f6a",
        "12/6794/0 f940a8d5dd744eadbb6e5e1fc4fe710c38a6e8d9eb08d0f460fa23fe4af2cc17",
        "12/14234/0 f3aa003b8b4ce311ffe7c78ca6024f86ed150e812a6fc9d843ff486258fb05bd"
    };
}
