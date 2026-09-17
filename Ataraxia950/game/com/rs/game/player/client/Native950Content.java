package com.rs.game.player.client;

import com.rs.network.protocol.modern950.Native950Packets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Cache-verified UI bindings passed across the frontend/gameplay boundary. */
public final class Native950Content {
    public final Native950ItemCatalog items;
    public final BankUi bank;
    public final BankerNpc banker;
    public final EquipmentUi equipment;
    public final Native950Appearance appearance;

    public Native950Content(Native950ItemCatalog items, BankUi bank) {
        this(items, bank, null);
    }

    public Native950Content(Native950ItemCatalog items, BankUi bank, BankerNpc banker) {
        this(items, bank, banker, null, null);
    }

    public Native950Content(Native950ItemCatalog items, BankUi bank, BankerNpc banker,
                            EquipmentUi equipment, Native950Appearance appearance) {
        this.items = Objects.requireNonNull(items, "items");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.banker = banker;
        if ((equipment == null) != (appearance == null))
            throw new IllegalArgumentException("Equipment UI and appearance must be supplied together");
        this.equipment = equipment;
        this.appearance = appearance;
    }

    /** Definition and placement verified against the selected modern cache. */
    public static final class BankerNpc {
        public final int definitionId, x, y, plane, size, bankOption, talkOption, collectOption;
        public final String name;

        public BankerNpc(int definitionId, String name, int x, int y, int plane, int size,
                         int bankOption, int talkOption) {
            this(definitionId, name, x, y, plane, size, bankOption, talkOption, 0);
        }

        public BankerNpc(int definitionId, String name, int x, int y, int plane, int size,
                         int bankOption, int talkOption, int collectOption) {
            if (definitionId < 0 || definitionId > 65534 || x < 0 || x > 16383 || y < 0 || y > 16383
                    || plane < 0 || plane > 3 || size != 1 || bankOption < 1 || bankOption > 5
                    || talkOption < 1 || talkOption > 5 || bankOption == talkOption
                    || collectOption < 0 || collectOption > 5 || collectOption == bankOption || collectOption == talkOption)
                throw new IllegalArgumentException("Invalid verified banker definition or placement");
            this.name = Objects.requireNonNull(name, "name");
            if (name.isEmpty()) throw new IllegalArgumentException("A banker name is required");
            this.definitionId = definitionId; this.x = x; this.y = y; this.plane = plane;
            this.size = size; this.bankOption = bankOption; this.talkOption = talkOption;
            this.collectOption = collectOption;
        }
    }

    public static final class EquipmentUi {
        public final int interfaceId, itemComponent, containerId;
        public final List<Native950Packets.Packet> bootstrap, refresh;

        public EquipmentUi(int interfaceId, int itemComponent, int containerId,
                           List<Native950Packets.Packet> bootstrap) {
            this(interfaceId, itemComponent, containerId, bootstrap, Collections.<Native950Packets.Packet>emptyList());
        }

        public EquipmentUi(int interfaceId, int itemComponent, int containerId,
                           List<Native950Packets.Packet> bootstrap, List<Native950Packets.Packet> refresh) {
            if (interfaceId < 0 || interfaceId > 65535 || itemComponent < 0 || itemComponent > 65535
                    || containerId < 0 || containerId > 65535)
                throw new IllegalArgumentException("Invalid equipment interface binding");
            this.interfaceId = interfaceId; this.itemComponent = itemComponent; this.containerId = containerId;
            this.bootstrap = Collections.unmodifiableList(new ArrayList<Native950Packets.Packet>(bootstrap));
            this.refresh = Collections.unmodifiableList(new ArrayList<Native950Packets.Packet>(refresh));
        }
    }

    public static final class BankUi {
        public final int interfaceId, itemComponent, inventoryComponent, closeComponent, depositAllComponent;
        /** Explicitly enabled native quantity prompt option; zero preserves older recipes. */
        public final int withdrawXOption;
        public final List<Native950Packets.Packet> open, close;
        private final int[] deposits, withdrawals;

        /** Option arrays use native one-based operation numbers; zero disables an operation. */
        public BankUi(int interfaceId, int itemComponent, int inventoryComponent, int closeComponent,
                      int depositAllComponent, int[] deposits, int[] withdrawals,
                      List<Native950Packets.Packet> open, List<Native950Packets.Packet> close) {
            this(interfaceId, itemComponent, inventoryComponent, closeComponent, depositAllComponent,
                    deposits, withdrawals, open, close, 0);
        }

        public BankUi(int interfaceId, int itemComponent, int inventoryComponent, int closeComponent,
                      int depositAllComponent, int[] deposits, int[] withdrawals,
                      List<Native950Packets.Packet> open, List<Native950Packets.Packet> close, int withdrawXOption) {
            this.interfaceId = interfaceId; this.itemComponent = itemComponent;
            this.inventoryComponent = inventoryComponent; this.closeComponent = closeComponent;
            this.depositAllComponent = depositAllComponent;
            if (deposits.length != 11 || withdrawals.length != 11) throw new IllegalArgumentException("Ten indexed UI operations required");
            this.deposits = deposits.clone(); this.withdrawals = withdrawals.clone();
            for (int value : this.deposits) if (value < 0) throw new IllegalArgumentException("Invalid deposit amount");
            for (int value : this.withdrawals) if (value < 0) throw new IllegalArgumentException("Invalid withdrawal amount");
            this.open = Collections.unmodifiableList(new ArrayList<Native950Packets.Packet>(open));
            this.close = Collections.unmodifiableList(new ArrayList<Native950Packets.Packet>(close));
            if (withdrawXOption != 0 && withdrawXOption != 6)
                throw new IllegalArgumentException("Unverified Withdraw-X operation");
            this.withdrawXOption = withdrawXOption;
        }

        public int depositAmount(int option) { return option < 1 || option > 10 ? 0 : deposits[option]; }
        public int withdrawAmount(int option) { return option < 1 || option > 10 ? 0 : withdrawals[option]; }
    }
}
