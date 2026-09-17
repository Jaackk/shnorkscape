package com.rs.game.player.bots.trading;

import com.rs.game.player.Player;
import com.rs.game.player.content.trade.Trade;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.InputIntegerEvent;

public final class MarketPlayerTrade extends Trade {

    private final MarketBotSession session;

    public MarketPlayerTrade(Player player, MarketBotSession session) {
        super(player);
        this.session = session;
    }

    @Override
    public void accept(boolean firstStage) {
        if (!canAccept(firstStage)) {
            return;
        }
        super.accept(firstStage);
        session.scheduleBotAccept(firstStage);
    }

    @Override
    public boolean canAccept(boolean firstStage) {
        return super.canAccept(firstStage) && session.canPlayerAccept(firstStage, true);
    }

    @Override
    public boolean canPerformTransaction() {
        return super.canPerformTransaction() && session.canPerformTransaction();
    }

    @Override
    public boolean nextStage() {
        boolean advanced = super.nextStage();
        if (advanced) {
            session.onTradeStageAdvanced();
        }
        return advanced;
    }

    @Override
    public void openTransactionAction() {
        super.openTransactionAction();
        session.refreshTradeScreenInfo();
    }

    @Override
    public void sendOptions() {
        super.sendOptions();
        // Right-click options on the bot's offer panel:
        //   slot 0 -> ACTION_BUTTON1 -> handleOtherOfferPrimary  -> session.sendOfferValue
        //   slot 1 -> ACTION_BUTTON2 -> handleOtherOfferSecondary -> ask amount
        //   slot 2 -> ACTION_BUTTON3 -> sendValue(slot, true)     -> session.removeOfferSlot (hijacked, see sendValue)
        //   slot 3 -> ACTION_BUTTON4 -> handleButtons override     -> session.selectOnlyOfferSlot (the new "Select")
        // sendIComponentSettings's mask uses (2 << slot) per
        // sendUnlockIComponentOptionSlots in PacketDispatcher; we enable
        // exactly slots 0-3 here. Bit 10 (slot 9 = the default "Examine")
        // is left OFF so the right-click menu shows our four options only.
        player.getPackets().sendInterSetItemsOptionsScript(335, OTHER_OFFER_COMPONENT_ID, 90, true, 4, 7,
                "Value", "Amount", "Remove", "Select");
        player.getPackets().sendIComponentSettings(335, OTHER_OFFER_COMPONENT_ID, 0, 27,
                (2 << 0) | (2 << 1) | (2 << 2) | (2 << 3));
    }

    @Override
    public void handleButtons(Player player, int interfaceId, int packetId, int componentId, int slotId) {
        // Route the new "Select" option (slot 3 on the bot's offer panel) to
        // session.selectOnlyOfferSlot. ItemTransaction.handleButtons doesn't
        // know about ACTION_BUTTON4 on component 17, so without this override
        // clicking Select would silently no-op.
        if (interfaceId == 335 && componentId == OTHER_OFFER_COMPONENT_ID
                && packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
            session.selectOnlyOfferSlot(slotId);
            return;
        }
        super.handleButtons(player, interfaceId, packetId, componentId, slotId);
    }

    @Override
    protected void handleOtherOfferPrimary(int slot) {
        session.sendOfferValue(slot);
    }

    @Override
    protected void handleOtherOfferSecondary(final int slot) {
        player.sendInputInteger("How many do you want?", new InputIntegerEvent() {
            @Override
            public void run(Player player) {
                int amount = getInteger();
                if (amount <= 0) {
                    return;
                }
                if (!session.requestOfferSlotAmount(slot, amount)) {
                    MarketPlayerTrade.super.handleOtherOfferSecondary(slot);
                }
            }
        });
    }

    @Override
    public void sendValue(int slot, boolean traders) {
        if (traders) {
            // sendOptions() relabels the bot's offer panel options to
            // "Value / Amount / Remove". ItemTransaction.handleButtons routes
            // the third option (component 17 / ACTION_BUTTON3_PACKET) through
            // sendValue(slot, traders=true), so we hijack that call to mean
            // "remove this entry from the bot's sell bundle" instead of
            // showing the market value. Option 1 (Value) is wired through
            // handleOtherOfferPrimary -> session.sendOfferValue. If
            // ItemTransaction.handleButtons ever changes which option calls
            // sendValue(slot, true), this hijack must be re-wired or the
            // Remove option will silently break.
            session.removeOfferSlot(slot);
            return;
        }
        super.sendValue(slot, traders);
    }

    @Override
    public void postAddItem() {
        super.postAddItem();
        session.onPlayerOfferChanged();
    }

    @Override
    public void postTradeAction(Player oldTarget, CloseTransactionStage stage) {
        super.postTradeAction(oldTarget, stage);
        session.finish(stage);
    }
}
