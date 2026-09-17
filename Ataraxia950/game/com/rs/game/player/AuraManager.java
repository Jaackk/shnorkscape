package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.item.Item;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.Setter;

public class AuraManager implements Serializable {

    private static final long serialVersionUID = -8860530757819448608L;

    private transient Player player;
    private transient boolean warned;
    @Setter
    private long activation;
    @Getter
    public HashMap<Integer, Long> cooldowns;
    private boolean[] ownedAuras;
    private boolean[] favouriteAuras;
    private Item activeAura;
    private transient int selectedAura = -1;
    private boolean hideDismissAuraWaringMessage;
    @SuppressWarnings("unused")
    private boolean resetedAll, fixedAuraRecharge, fixedAuraRecharge2;
    private int[] visWaxExtenedAuras;

    /**
     * Number of auras in enum {@link #AURAS_ENUM_ID}, tolerating a JVM in which
     * cache index 17 is not loaded. With the enum present this is exactly
     * {@code ClientScriptMap.getMap(AURAS_ENUM_ID).getSize()}, so legacy 910
     * logins keep their existing array lengths; without a cache {@code getMap}
     * hands back an empty map (or, defensively, null) and the manager is
     * constructed empty instead of throwing. P4 requires the manager to exist for
     * every hydrated native player because {@code Player.processEntity}
     * dereferences it unconditionally.
     */
    static int aurasEnumSize() {
        ClientScriptMap map = ClientScriptMap.getMap(AURAS_ENUM_ID);
        return map == null ? 0 : map.getSize();
    }

    public AuraManager() {
        cooldowns = new HashMap<Integer, Long>();
        ownedAuras = new boolean[aurasEnumSize()];
        favouriteAuras = new boolean[aurasEnumSize()];
        visWaxExtenedAuras = new int[aurasEnumSize()];
    }

    protected void setPlayer(Player player) {
        this.player = player;
        if (!resetedAll) {
            cooldowns = new HashMap<Integer, Long>();
            ownedAuras = new boolean[aurasEnumSize()];
            favouriteAuras = new boolean[aurasEnumSize()];
            visWaxExtenedAuras = new int[aurasEnumSize()];
            activation = 0;
            activeAura = null;
            Item aura = player.getEquipment().getItem(Equipment.SLOT_AURA);
            if (aura != null) {
                player.addItem(aura);
                player.getEquipment().getItems().set(Equipment.SLOT_AURA, null);
                player.getPackets().sendGameMessage("<col=ff0000>Auras have been updated, to unlock your auras in new interface drop them from your inventory.");
            }
            resetedAll = true;
        }
        if (ownedAuras == null || !(ownedAuras instanceof boolean[])) {
            ownedAuras = new boolean[aurasEnumSize()];
            cooldowns = new HashMap<Integer, Long>();// resetted the cooldowns since it was incorrect
        }
        if (favouriteAuras == null)
            favouriteAuras = new boolean[aurasEnumSize()];
        if (ownedAuras.length != aurasEnumSize()) {
            boolean[] tempOwnedAuras = new boolean[aurasEnumSize()];
            for (int i = 0; i < ownedAuras.length; i++) {
                tempOwnedAuras[i] = ownedAuras[i];
            }
            ownedAuras = tempOwnedAuras;

        }
        if (favouriteAuras.length != aurasEnumSize()) {
            boolean[] tempFavouriteAuras = new boolean[aurasEnumSize()];
            for (int i = 0; i < favouriteAuras.length; i++) {
                tempFavouriteAuras[i] = favouriteAuras[i];
            }
            favouriteAuras = tempFavouriteAuras;
        }
        if (visWaxExtenedAuras == null)
            visWaxExtenedAuras = new int[aurasEnumSize()];
        if (visWaxExtenedAuras.length != aurasEnumSize()) {
            int[] tempVisExtended = new int[aurasEnumSize()];
            for (int i = 0; i < visWaxExtenedAuras.length; i++) {
                tempVisExtended[i] = visWaxExtenedAuras[i];
            }
            visWaxExtenedAuras = tempVisExtended;
        }
        if (auraRefreshers == null)
            auraRefreshers = new int[5];

        if(!fixedAuraRecharge2) {
            if(!cooldowns.isEmpty())
                cooldowns.clear();
            activeAura = null;
            activation = 0;
            warned = false;
            fixedAuraRecharge2 = true;
        }
    }

    public void init() {
        refreshOwnedAuras();
        refreshFavouriteAuras();
        refreshLoyaltyPoints();
        refreshAurasStatus();
        refreshExtendedAuras();
        refreshAuraRefreshers();
        refreshShowListView();
        refreshHideDismissAuraWaringMessage();
        refreshOpenOnFavourites();
        refreshAutoExtendUnFav();
        refreshAutoExtendFav();
        refreshAutoExtendDoubleTime();
    }

    public void openAuraManagementInterface() {
        selectedAura = -1;
        player.getInterfaceManager().sendCentralOverlayInterface(1929);
        refreshOwnedAuras();
        unlockButtons();
        refreshAurasStatus();
        refreshLoyaltyPoints();
        player.getPackets().sendConfig(7654, Utils.millisecondsToCycles(Utils.currentUTCTimeMillis()));
        player.getPackets().sendConfig(7647, 0);
        player.getPackets().sendConfig(7649, 0);
    }

    private void unlockButtons() {
        player.getPackets().sendIComponentSettings(1929, 95, 0, 130, 6);
        player.getPackets().sendIComponentSettings(1929, 92, 0, 4, 2);
        player.getPackets().sendIComponentSettings(1929, 167, 0, 1, 6);
        player.getPackets().sendIComponentSettings(1929, 107, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1929, 103, 0, 1, 2);
    }

    public void deactivateAura() {
        if (!isActivated()) {
            player.getPackets().sendGameMessage("You dont have an aura active.");
            return;
        }
        if (hideDismissAuraWaringMessage) {
            deplete(true);
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendOptionsDialogue("This aura is active. Are you sure you want to deactivate it?", "Yes.", "No.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == OPTION_1)
                    deplete(true);
                end();
            }

            @Override
            public void finish() {
            }
        });
    }

    public void refreshAurasStatus() {
        for (Aura aura : Aura.values()) {
            if (aura.getActiveAuraVarbitId() != -1)
                player.getPackets().sendConfigByFile(aura.getActiveAuraVarbitId(), isActive(aura.getItemId()) ? 1 : 0);
            if (aura.getChargingAuraVarId() != -1)
            player.getPackets().sendConfig(aura.getChargingAuraVarId(), getRechargingCycles(aura.getItemId()));
        }
    }

    public boolean isActive(int itemId) {
        if (!isActivated())
            return false;
        Aura aura = Aura.getAuraByItemId(itemId);
        boolean active = false;
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            if (activeAura.getId() == item.getId())
                active = true;
        }
        return active;
    }

    public int getRechargingCycles(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        int rechargingCycles = 0;
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            if (getAuraRechargingCycles(item.getId()) != 0)
                rechargingCycles = getAuraRechargingCycles(item.getId());
        }
        return rechargingCycles;
    }

    public int isAuraExtended(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (aura == null)
            return 0;
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            Aura a2 = Aura.getAuraByItemId(item.getId());
            if (visWaxExtenedAuras[a2.getSlotId()] != 0) {
                return visWaxExtenedAuras[a2.getSlotId()];
            }
        }
        return 0;
    }

    public void unExtend(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (aura == null)
            return;
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            Aura a2 = Aura.getAuraByItemId(item.getId());
            if (visWaxExtenedAuras[a2.getSlotId()] != 0)
                visWaxExtenedAuras[a2.getSlotId()] = 0;
        }
    }

    public void extendAura(boolean doubleTime) {
        if (selectedAura == -1)
            return;
        if (!hasAura(selectedAura))
            return;
        Aura aura = Aura.getAuraByItemId(selectedAura);
        if (aura == null)
            return;
        extendAura(selectedAura, doubleTime);
    }

    public void extendAura(int auraId, boolean doubleTime) {
        extendAura(auraId, doubleTime, true);
    }

    public void extendAura(int auraId, boolean doubleTime, boolean sendMessage) {
        Aura aura = Aura.getAuraByItemId(auraId);
        if (aura == null)
            return;
        if (isAuraExtended(auraId) != 0) {
            if (sendMessage)
                player.getPackets().sendGameMessage("This aura has already been extended with vis wax.");
            return;
        }
        if (aura.getExtendedVarbitId() == -1) {
            if (sendMessage)
                player.getPackets().sendGameMessage("This aura can't be extended with vis wax.");
            return;
        }
        int amountInInv = player.getInventory().getAmountOf(32092);
        int amountInBank = player.getBank().containsItemCurrentBank(32092, 1) ? player.getBank().getItem(32092).getAmount() : 0;
        int visWaxAmount = amountInInv + amountInBank;
        int auraType = ItemDefinitions.getItemDefinitions(auraId).getCSOpcode(4550);
        int amount = (auraType == 0 ? 10 : auraType == 1 ? 40 : 100);
        if (!doubleTime)
            amount /= 2;
        if (visWaxAmount < amount) {
            if (sendMessage)
                player.getPackets().sendGameMessage("You don't have enough vis wax to do that.");
            return;
        }
        int removed = 0;
        if (player.getInventory().containsItem(32092, 1)) {
            removed += amountInInv >= amount ? amount : player.getInventory().getAmountOf(32092);
            player.getInventory().deleteItem(32092, removed);
        }
        if (removed < amount)
            player.getBank().removeItem(player.getBank().getItemSlot(new Item(32092)), amount - removed, true, Bank.DESTROY_ITEM);
        visWaxExtenedAuras[aura.getSlotId()] = doubleTime ? 2 : 1;
        refreshExtendedAuras();
        if (sendMessage)
            player.getPackets().sendGameMessage("You extend " + aura.getAuraName() + " aura using " + amount + " vis wax.");
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                refreshComponentOptions(!isActivated());
                refreshSelectedAura(false);
            }
        });
    }
    public boolean isAura(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (itemId == 41444)
            aura = Aura.SUPREME_WISDOM_AURA;
        if (itemId == 41448)
            aura = Aura.LEGENDARY_WISDOM_AURA;
        return aura != null;
    }
    
    public boolean checkAddAura(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (!isAura(aura.getItemId()))
            return false;
        if (isActive(aura.getItemId())) {
            player.getPackets().sendGameMessage("You can't upgrade an active aura, disable this aura before you can upgrade it.");
            refreshSelectedAura(false);
            return false;
        }
        if (!ownedAuras[aura.getSlotId()]) {
            ownedAuras[aura.getSlotId()] = true;
            refreshOwnedAuras();
            player.getPackets().sendGameMessage("You have unlocked " + ItemDefinitions.getItemDefinitions(itemId).getName() + ".");
        }
        return true;
    }

    public int getAuraRechargingCycles(int auraId) {
        if (!isRecharging(auraId))
            return 0;
        long cooldown = getCoolDown(auraId);
        return Utils.millisecondsToCycles(cooldown);
    }

    public boolean isRecharging(int auraId) {
        return Utils.currentUTCTimeMillis() <= getCoolDown(auraId);
    }

    private void refreshOwnedAuras() {
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int v4 = 0;
        for (int i = 0; i < ownedAuras.length; i++) {
            Aura aura = Aura.getAuraBySlotId(i);
            int itemId = aura.getItemId();
            if (itemId == 29475) {
                player.getPackets().sendConfigByFile(20536, ownedAuras[i] ? 1 : 0);
            } else if (itemId == 29473) {
                player.getPackets().sendConfigByFile(20534, ownedAuras[i] ? 1 : 0);
            } else if (itemId == 29471) {
                player.getPackets().sendConfigByFile(20532, ownedAuras[i] ? 1 : 0);
            } else if (itemId == 29469) {
                player.getPackets().sendConfigByFile(20530, ownedAuras[i] ? 1 : 0);
            }
            int fakeSlotId = (int) ClientScriptMap.getMap(3877).getKeyForValue(itemId);
            switch (fakeSlotId / 32) {
            case 0:
                v1 |= ((ownedAuras[i] ? 1 : 0) << i);
                break;
            case 1:
                v2 |= ((ownedAuras[i] ? 1 : 0) << (i % 32));
                break;
            case 2:
                v3 |= ((ownedAuras[i] ? 1 : 0) << (i % 32));
                break;
            case 3:
                v4 |= ((ownedAuras[i] ? 1 : 0) << (i % 32));
                break;
            }
        }
        player.getPackets().sendConfig(1241, v1);
        player.getPackets().sendConfig(1242, v2);
        player.getPackets().sendConfig(1243, v3);
        player.getPackets().sendConfig(5888, v4);
    }

    private void refreshFavouriteAuras() {
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int v4 = 0;
        int v5 = 0;
        for (int i = 0; i < favouriteAuras.length; i++) {
            int fakeSlotId = getFakeSlotId(i, ClientScriptMap.getMap(3877).getSize());
            if (fakeSlotId < 0)
                continue;
            if (fakeSlotId >= 1000) {
                v5 |= ((favouriteAuras[i] ? 1 : 0) << (fakeSlotId - 1000));
            } else
                switch (fakeSlotId / 32) {
                case 0:
                    v1 |= ((favouriteAuras[i] ? 1 : 0) << fakeSlotId);
                    break;
                case 1:
                    v2 |= ((favouriteAuras[i] ? 1 : 0) << (fakeSlotId % 32));
                    break;
                case 2:
                    v3 |= ((favouriteAuras[i] ? 1 : 0) << (fakeSlotId % 32));
                    break;
                case 3:
                    v4 |= ((favouriteAuras[i] ? 1 : 0) << (fakeSlotId % 32));
                    break;
                }
        }
        player.getPackets().sendConfig(7656, v1);
        player.getPackets().sendConfig(7657, v2);
        player.getPackets().sendConfig(7658, v3);
        player.getPackets().sendConfig(7659, v4);
        player.getPackets().sendConfig(8691, v5);
    }

    public static int getFakeSlotId(int arg0, int arg1) {
        if (arg0 < arg1) {
            return arg0;
        }
        return arg0 - arg1 + 1000;
    }

    private void refreshComponentOptions(boolean refreshAuraSlot) {
        if (refreshAuraSlot) {
            player.getPackets().sendExecuteScript(3087, InterfaceManager.getComponentUId(1464, 15));
            player.getPackets().sendExecuteScript(3087, InterfaceManager.getComponentUId(1462, 31));
        }
        player.getPackets().sendExecuteScript(3087, InterfaceManager.getComponentUId(1464, 24));
    }

    private void refreshExtendedAuras() {
        for (Aura aura : Aura.values()) {
            if (aura.getExtendedVarbitId() == -1)
                continue;
            player.getPackets().sendConfigByFile(aura.getExtendedVarbitId(), isAuraExtended(aura.getItemId()) != 0 ? 1 : 0);
        }
    }

    public int getAuraActiveCycles() {
        if (!isActivated())
            return 0;
        return Utils.millisecondsToCycles(activation);
    }

    public void selectAura(int slotId) {
        Aura aura = Aura.getAuraBySlotId(slotId);
        if (aura == null) {
            player.getPackets().sendGameMessage("This aura is not added yet.");
            return;
        }
        this.selectedAura = aura.getItemId();
        refreshSelectedAura(true);
        refreshInterface();
    }

    public void refreshSelectedAura(boolean clickedOnAura) {
        if (selectedAura == -1)
            return;
        int initalCycles = isActivated() && activeAura.getId() == selectedAura ? getAuraActiveCycles() : getAuraRechargingCycles(selectedAura);
        Aura aura = Aura.getAuraByItemId(selectedAura);
        if (aura == null) {
            player.getPackets().sendGameMessage("That aura is not added yet.");
            return;
        }
        int auraIndex = aura.getSlotId();
        player.getPackets().sendConfig(7654, Utils.millisecondsToCycles(Utils.currentUTCTimeMillis()));
        player.getPackets().sendConfig(7647, auraIndex);
        player.getPackets().sendConfig(7649, initalCycles);
        player.getPackets().sendExecuteScript(2486);
        if (!clickedOnAura)
            player.getPackets().sendExecuteScript(8297, ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(selectedAura), auraIndex, hasAura(selectedAura) ? 0 : 1, 0);
        player.getPackets().sendExecuteScript(8881, Utils.millisecondsToCycles(Utils.currentUTCTimeMillis()), selectedAura);
    }

    public boolean hasAura(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (aura == null)
            return false;
        return ownedAuras[aura.getSlotId()];
    }

    private void refreshInterface() {
        player.getPackets().sendExecuteScript(8881, Utils.millisecondsToCycles(Utils.currentUTCTimeMillis()), -1);
    }

    public void process() {
        if (player == null)
            return;
        if (player.getInterfaceManager().containsCentralOverlayInterface(1929))
            refreshInterface();
        if (!isActivated())
            return;
        if (activation - Utils.currentUTCTimeMillis() <= 60000 && !warned) {
            player.getPackets().sendGameMessage("Your aura will deplete in 1 minute.");
            warned = true;
            return;
        }
        if (Utils.currentUTCTimeMillis() < activation)
            return;
        deplete();
    }

    public void removeAura() {
        if (isActivated())
            deplete();
    }

    private void deplete() {
        deplete(false);
    }

    public void buyOrActivateAuraAura() {
        if (selectedAura == -1)
            return;
        
        if (hasAura(selectedAura)) {
            activate(selectedAura);
            return;
        }
        if(player.getRights() < 2) {
            player.getPackets().sendGameMessage("You can unlock this aura by dropping the item after buying it from shop.");
            return;
        }
        if (isActive(selectedAura)) {
            player.getPackets().sendGameMessage("You can't upgrade an active aura, disable this aura before you can upgrade it.");
            refreshSelectedAura(false);
            return;
        }
        Aura aura = Aura.getAuraByItemId(selectedAura);
        if (aura == null)
            return;
        int price = aura.getPrice();
        if (player.getLoyaltyPoints() < price) {
            player.getPackets().sendGameMessage("You dont have enough loyalty points to buy this aura.");
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    refreshSelectedAura(false);
                }
            }, 1);
            return;
        }
        checkRechargeAura();
        checkUpdateFavourites();
        checkUpdateOwnedAuras();
        addAura(selectedAura);
        player.setLoyaltyPoints(player.getLoyaltyPoints() - price);
        player.getPackets().sendExecuteScript(2486);
        player.getPackets().sendExecuteScript(7806);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                refreshSelectedAura(false);
            }

        }, 1);
    }

    public void toggleFavouriteAura() {
        Aura aura = Aura.getAuraByItemId(selectedAura);
        favouriteAuras[aura.getSlotId()] = !favouriteAuras[aura.getSlotId()];
        boolean added = favouriteAuras[aura.getSlotId()];
        player.getPackets().sendGameMessage((added ? "Added" : "Removed") + " " + aura.getAuraName() + " Aura " + (added ? "to" : "from") + " favourites.");
        refreshFavouriteAuras();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                refreshComponentOptions(!isActivated());
                refreshSelectedAura(false);
            }
        });
    }

    private void checkRechargeAura() {
        if (selectedAura == -1)
            return;
        boolean removed = false;
        Aura aura = Aura.getAuraByItemId(selectedAura);
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            if (cooldowns.containsKey(item.getId())) {
                cooldowns.remove(item.getId());
                removed = true;
            }
        }
        if (removed)
            refreshAurasStatus();
    }

    private void checkUpdateFavourites() {
        if (selectedAura == -1)
            return;
        boolean updated = false;
        Aura aura = Aura.getAuraByItemId(selectedAura);
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            Aura otherVersion = Aura.getAuraByItemId(item.getId());
            int auraIndex = otherVersion.getSlotId();
            if (favouriteAuras[auraIndex]) {
                favouriteAuras[auraIndex] = false;
                updated = true;
            }
        }
        if (updated) {
            favouriteAuras[aura.getSlotId()] = true;
            refreshFavouriteAuras();
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    refreshComponentOptions(!isActivated());
                }

            });
        }
    }

    public boolean addAura(int itemId) {
        Aura aura = Aura.getAuraByItemId(itemId);
        if (aura == null)
            return false;
        ownedAuras[aura.getSlotId()] = true;
        refreshOwnedAuras();
        return true;
    }

    private void checkUpdateOwnedAuras() {
        if (selectedAura == -1)
            return;
        Aura aura = Aura.getAuraByItemId(selectedAura);
        int tier = aura.getTier();
        for (Item item : aura.getAllAuraVersions()) {
            if (item == null)
                continue;
            Aura otherVersion = Aura.getAuraByItemId(item.getId());
            int auraIndex = otherVersion.getSlotId();
            ownedAuras[auraIndex] = otherVersion.getTier() <= tier;
        }
        refreshAurasStatus();
    }

    public void activate(int auraId) {
        Aura aura = Aura.getAuraByItemId(auraId);
        if (aura == null)
            return;
        if (activeAura != null && auraId == activeAura.getId()) {
            deactivateAura();
            return;
        }
        long cooldown = getCoolDown(auraId);
        if (Utils.currentUTCTimeMillis() <= cooldown) {
            player.getPackets().sendGameMessage("Currently recharging. <col=ff0000>" + getFormatedTime((cooldown - Utils.currentUTCTimeMillis()) / 1000) + " until fully recharged.");
            return;
        }
        if (activeAura != null) {
            if (player.getInterfaceManager().containsCentralOverlayInterface(1929)) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        player.getPackets().sendHideIComponent(1929, 142, false);
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        if (componentId == 162) {
                            deplete(true);
                            int tier = aura.getTier();
                            if ((autoExtendFav && favouriteAuras[aura.getSlotId()]) || (autoExtendUnFav && !favouriteAuras[aura.getSlotId()]))
                                extendAura(auraId, autoExtendDoubleTime, false);
                            int extended = isAuraExtended(aura.getItemId());
                            activation = Utils.currentUTCTimeMillis() + (aura.getActivationTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 2 : 1)) + (int) ((double) aura.getActivationTime() * ((double) extended / 2.00));
                            cooldowns.put(aura.getItemId(), (long) (activation + (aura.getCooldownTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 0.3 : 1))));
                            player.getBuffDebuffTimersManager().addTimer(Timer.AURA_ACTIVE  , ((aura.getActivationTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 2 : 1)) + (int) ((double) aura.getActivationTime() * ((double) extended / 2.00))));
                            unExtend(aura.getItemId());
                            activeAura = new Item(auraId, 1);
                            player.getEquipment().getItems().set(Equipment.SLOT_AURA, activeAura);
                            player.getEquipment().refresh();
                            player.setNextAnimation(new Animation(2231));
                            player.setNextGraphics(new Graphics(getActiveGraphic(tier)));
                            player.getAppearence().generateAppearenceData();
                            refreshAurasStatus();
                        }
                        player.getPackets().sendHideIComponent(1929, 142, true);
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                refreshSelectedAura(false);
                            }
                        }, 1);
                        end();
                    }

                    @Override
                    public void finish() {
                    }
                });
                return;
            }
            player.getPackets().sendGameMessage("You already have an aura active. Deactivate it before you can activate this aura or open aura management interface to swap auras..");
            return;
        }
        int tier = aura.getTier();
        if ((autoExtendFav && favouriteAuras[aura.getSlotId()]) || (autoExtendUnFav && !favouriteAuras[aura.getSlotId()]))
            extendAura(auraId, autoExtendDoubleTime, false);
        int extended = isAuraExtended(aura.getItemId());
        activation = Utils.currentUTCTimeMillis() + (aura.getActivationTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 2 : 1)) + (int) ((double) aura.getActivationTime() * ((double) extended / 2.00));
        cooldowns.put(aura.getItemId(), (long) (activation + (aura.getCooldownTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 0.3 : 1))));
        unExtend(aura.getItemId());
        activeAura = new Item(auraId, 1);
        player.getBuffDebuffTimersManager().addTimer(Timer.AURA_ACTIVE, ((aura.getActivationTime() * (player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED) ? 2 : 1)) + (int) ((double) aura.getActivationTime() * ((double) extended / 2.00))));
        player.getEquipment().getItems().set(Equipment.SLOT_AURA, activeAura);
        player.getEquipment().refreshItemContainer();
        player.setNextAnimation(new Animation(2231));
        player.setNextGraphics(new Graphics(getActiveGraphic(tier)));
        player.getAppearence().generateAppearenceData();
        refreshAurasStatus();
        if (player.getInterfaceManager().containsCentralOverlayInterface(1929))
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    refreshSelectedAura(false);
                }

            }, 1);
    }

    public void handleEquipmentOptions(boolean auraSlot, int packetId) {
        switch (packetId) {
        case PacketRepository.ACTION_BUTTON1_PACKET:
            player.getAuraManager().openAuraManagementInterface();
            break;
        case PacketRepository.ACTION_BUTTON2_PACKET:
            if (auraSlot && isActivated())
                player.getAuraManager().openAuraManagementInterface();
            else
                activateDeactivateFavouriteAura(0);
            break;
        case PacketRepository.ACTION_BUTTON3_PACKET:
            if (auraSlot && isActivated())
                player.getAuraManager().sendAuraRemainingTime(activeAura.getId());
            else
                activateDeactivateFavouriteAura(1);
            break;
        case PacketRepository.ACTION_BUTTON4_PACKET:
            activateDeactivateFavouriteAura(2);
            break;
        case PacketRepository.ACTION_BUTTON5_PACKET:
            activateDeactivateFavouriteAura(3);
            break;
        case PacketRepository.ACTION_BUTTON8_PACKET:
            if (auraSlot && isActivated()) {
                Aura aura = Aura.getAuraByItemId(activeAura.getId());
                player.getPackets().sendGameMessage(aura.getAuraName() + " Aura : " + aura.getExamineMessage());
            }
            break;
        case PacketRepository.ACTION_BUTTON9_PACKET:
            if (auraSlot && isActivated())
                player.getAuraManager().deactivateAura();
            else
                activateDeactivateFavouriteAura(4);
            break;
        case PacketRepository.ACTION_BUTTON6_PACKET:
            activateDeactivateFavouriteAura(5);
            break;
        }
    }

    public void activateDeactivateFavouriteAura(int selectedIndex) {
        int auraSlotId = getFavouriteAuraByIndex(selectedIndex);
        Aura aura = Aura.getAuraBySlotId(auraSlotId);
        if (aura == null)
            return;
        if (isActivated() && activeAura.getId() == aura.getItemId())
            deactivateAura();
        else
            activate(aura.getItemId());
    }

    public int getFavouriteAuraByIndex(int selectedIndex) {
        int index = 0;
        for (int i = 0; i < favouriteAuras.length; i++) {
            if (favouriteAuras[i] && index == selectedIndex)
                return i;
            if (favouriteAuras[i])
                index++;
        }
        return -1;
    }

    private void deplete(boolean deactivate) {
        activeAura = null;
        activation = 0;
        warned = false;
        player.getBuffDebuffTimersManager().removeTimer(Timer.AURA_ACTIVE);
        player.getPackets().sendGameMessage("Your aura has " + (deactivate ? "been deactivated" : "depleted") + ".");
        player.getEquipment().getItems().set(Equipment.SLOT_AURA, null);
        player.getEquipment().refreshItemContainer();
        player.getAppearence().generateAppearenceData();
        refreshAurasStatus();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (player.getInterfaceManager().containsCentralOverlayInterface(1929))
                    refreshSelectedAura(false);
                refreshComponentOptions(true);
            }
        }, 1);
        if (!player.getPerkManager().hasPerk(DonationPerk.OVERCLOCKED))
            player.getPackets().sendGameMessage("Purchase the Overclocked perk - doubles your aura timer and lessens the cooldown times.", true);
    }

    public long getCoolDown(int aura) {
        Long coolDown = cooldowns.get(aura);
        if (coolDown == null)
            return 0;
        return coolDown;
    }

    public void sendAuraRemainingTime(int auraId) {
        if (!isActivated()) {
            long cooldown = getCoolDown(auraId);
            if (Utils.currentUTCTimeMillis() <= cooldown) {
                player.getPackets().sendGameMessage("Currently recharging. <col=ff0000>" + getFormatedTime((cooldown - Utils.currentUTCTimeMillis()) / 1000) + " remaining.");
                return;
            }
            player.getPackets().sendGameMessage("Currently not active. It is ready to use.");
            return;
        }
        player.getPackets().sendGameMessage("Currently active. <col=00ff00>" + getFormatedTime((activation - Utils.currentUTCTimeMillis()) / 1000) + " remaining");
    }

    public static String getFormatedTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes -= hours * 60;
        seconds -= (hours * 60 * 60) + (minutes * 60);
        String minutesString = (minutes < 10 ? "0" : "") + minutes;
        String secondsString = (seconds < 10 ? "0" : "") + seconds;
        return hours + ":" + minutesString + ":" + secondsString;
    }

    public void sendTimeRemaining(int aura) {
        long cooldown = getCoolDown(aura);
        if (cooldown < Utils.currentUTCTimeMillis()) {
            player.getPackets().sendGameMessage("The aura has finished recharging. It is ready to use.");
            return;
        }
        player.getPackets().sendGameMessage("Currently recharging. <col=ff0000>" + getFormatedTime((cooldown - Utils.currentUTCTimeMillis()) / 1000) + " remaining.");
    }

    public boolean isActivated() {
        if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DuelArena)
            return false;
        return activation > 0 && activeAura != null;
    }

    public boolean ignoreDuelIsActivated() {
        return activation > 0 && activeAura != null;
    }

    public int getAuraModelId2() {
        int aura = isActivated() ? activeAura.getId() : -1;
        switch (aura) {
        case 22905: // Corruption. *no eyes
            return 16449;
        case 22899: // Salvation. *no eyes
            return 16465;
        case 23848: // Harmony. *no eyes
            return 68605;
        case 22907: // Greater corruption. *no eyes
            return 16464;
        case 22901: // Greater salvation. *no eyes
            return 16524;
        case 23850: // Greater harmony. *no eyes
            return 68610;
        case 22909: // Master corruption. * eyes*
            if (player.getAppearence().isMale())
                return 16429;
            if (!player.getAppearence().isMale())
                return 16510;
        case 22903: // Master salvation. * eyes*
            if (player.getAppearence().isMale())
                return 16450;
            if (!player.getAppearence().isMale())
                return 16427;
        case 23852: // Master harmony. * eyes*
            if (player.getAppearence().isMale())
                return 68607;
            if (!player.getAppearence().isMale())
                return 68609;
        case 23874: // Supreme corruption. * eyes*
            if (player.getAppearence().isMale())
                return 68615;
            if (!player.getAppearence().isMale())
                return 68614;
        case 23876: // Supreme salvation. * eyes*
            if (player.getAppearence().isMale())
                return 68611;
            if (!player.getAppearence().isMale())
                return 68608;
        case 23854: // Supreme harmony. * eyes*
            if (player.getAppearence().isMale())
                return 68613;
            if (!player.getAppearence().isMale())
                return 68606;
        case 22889: // Aegis.
            return 83308;
        default:
            return -1;
        }
    }

    public int getAuraModelId() {
        /**
         * Aura models: Christmas wand, daggers, hatchets, pickaxes, darts, javelins,
         * knives, korasi: Default Excalibur: 8722 Scimitars: 8955, 8956, 8957, 93420
         * Longswords/darklight: 9080, 8945, 8952, 93418 Two-handed swords: 8773, 8844,
         * 8909, 93413 Abyssal whip: 8724, 8725, 46577, 93424 Staves: 8972, 9013, 9014,
         * 93423 Swords: 8961, 8962, 8963, 93422 Shortbows, longbows: 8958, 8959, 8960,
         * 93421 Crossbows: 86571, 86572, 86573, 93419 Salamander: 8910, 8911, 8912,
         * 93415 Rapier: 8961, 8962, 8963, 93422 Hand cannon: 8910, 8911, 8912, 93415
         * Two-handed crossbows: 8910, 93415
         *
         */
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon == null || player.getCombatDefinitions().isSheathe())
            return 8719;
        String name = weapon.getDefinitions().getName().toLowerCase();
        int aura = isActivated() ? activeAura.getId() : -1;
        Aura auradef = Aura.getAuraByItemId(aura);
        int tier = auradef.getTier();
        if (tier == 4)
            tier = 3;// 3 and 4 have same models.
        if (name.contains("scimitar")) {
            if (tier == 5)
                return 93420;
            if (tier == 3)
                tier = 2;
            return 8955 + tier;
        } else if (name.contains("longsword") || name.contains("darklight") || name.contains("silverlight")) {
            if (name.contains("drygore"))
                return 93411;
            switch (tier) {
            case 5:
                return 93418;
            case 3:
                return 8952;
            case 2:
                return 8945;
            default:
                return 9080;
            }
        } else if (name.contains("2h sword") || name.contains("godsword")) {
            switch (tier) {
            case 5:
                return 93413;
            case 3:
                return 8909;
            case 2:
                return 8844;
            default:
                return 8773;
            }
        } else if (name.contains("whip")) {
            switch (tier) {
            case 5:
                return 93424;
            case 3:
                return 46577;
            case 2:
                return 8725;
            default:
                return 8724;
            }
        } else if (name.contains("staff")) {
            switch (tier) {
            case 5:
                return 93423;
            case 3:
                return 9014;
            case 2:
                return 9013;
            case 1:
                return 8972;
            }
        } else if (name.contains(" sword")) {
            if (tier == 5)
                return 93422;
            if (tier == 3)
                tier = 2;
            return 8961 + tier;
        } else if (name.contains("shortbow") || name.contains("longbow") || name.contains("zaryte bow") || name.contains("shieldbow")) {
            if (tier == 5)
                return 93421;
            if (tier == 3)
                tier = 2;
            return 8958 + tier;
        } else if (name.contains("crossbow")) {
            if (tier == 5)
                return 93419;
            return 86571 + tier;
        } else if (name.contains("salamander") || name.contains("hand cannon") || name.contains("2h crossbow")) {
            if (tier == 5)
                return 93415;
            if (tier == 3)
                tier = 2;
            return 8910 + tier;
        } else if (name.contains("rapier")) {
            if (tier == 5)
                return 93422;
            if (tier == 3)
                tier = 2;
            return 8961 + tier;
        }
        return 8719;
    }

    public int getActiveGraphic(int tier) {
        if (tier == 2)
            return 370;
        if (tier == 3)
            return 1764;
        if (tier == 4)
            return 1763;
        return 370;
    }

    public boolean hasPoisonPurge() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 20958 || aura == 22268 || aura == 22917 || aura == 23862;
    }

    public double getMagicAccurayMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 20962)
            return 1.03;
        if (aura == 22270)
            return 1.05;
        if (aura == 22919)
            return 1.07;
        if (aura == 23864)
            return 1.1;
        if (aura == 35796)
            return 1.1;
        return 1;
    }

    public double getMagicDamageMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 35796)
            return 1.1;
        return 1;
    }

    public double getMeleeAccuracyMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 35786)
            return 1.03;
        if (aura == 35788)
            return 1.05;
        if (aura == 35790)
            return 1.07;
        if (aura == 35792)
            return 1.1;
        if (aura == 22897)
            return 1.1;
        if (aura == 41447)
            return 1.15;
        return 1;
    }

    public double getMeleeDamageMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22897)
            return 1.1;
        if (aura == 41447)
            return 1.15;
        return 1;
    }

    public boolean getDarkMagicBoost() {
        if (!isActivated() || World.isPvpArea(player))
            return false;
        return player.getEquipment().getAuraId() == 22891;
    }

    public boolean hasAncestorSpirits() {
        if (!isActivated())
            return false;
        return player.getEquipment().getAuraId() == 22895;
    }

    public double getRangeAccurayMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 20967)
            return 1.03;
        if (aura == 22272)
            return 1.05;
        if (aura == 22921)
            return 1.07;
        if (aura == 23866)
            return 1.1;
        if (aura == 35794)
            return 1.1;
        return 1;
    }

    public double getRangeDamageMultiplier() {
        if (!isActivated() || World.isPvpArea(player))
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 35794)
            return 1.1;
        return 1;
    }

    public double getWoodcuttingAccurayMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22280)
            return 1.025;
        if (aura == 22282)
            return 1.05;
        if (aura == 22915)
            return 1.075;
        if (aura == 23860)
            return 1.1;
        if (aura == 30796)
            return 1.25;
        return 1;
    }

    public double getMiningAccurayMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22284)
            return 1.025;
        if (aura == 22286)
            return 1.05;
        if (aura == 22913)
            return 1.075;
        if (aura == 23858)
            return 1.1;
        if (aura == 30800)
            return 1.25;
        return 1;
    }

    public boolean usingRegeneration() {
        if (!isActivated())
            return false;
        return player.getEquipment().getAuraId() == 22893;
    }

    public double getFishingAccurayMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 20966)
            return 1.025;
        if (aura == 22274)
            return 1.05;
        if (aura == 22923)
            return 1.075;
        if (aura == 23868)
            return 1.1;
        if (aura == 30794)
            return 1.25;
        return 1;
    }

    public double getPrayerPotsRestoreMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 20965)
            return 1.03;
        if (aura == 22276)
            return 1.05;
        if (aura == 22925)
            return 1.07;
        if (aura == 23870)
            return 1.1;
        return 1;
    }

    public boolean isUsingSurefooted() {
        if (!isActivated())
            return false;
        return player.getEquipment().getAuraId() == 20964;
    }

    public double getHunterMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22927)
            return 1.03;
        if (aura == 22929)
            return 1.05;
        if (aura == 22931)
            return 1.07;
        if (aura == 23872)
            return 1.1;
        if (aura == 30802)
            return 1.15;
        return 1;
    }

    public double getThievingAccurayMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22288)
            return 1.025;
        if (aura == 22290)
            return 1.05;
        if (aura == 22911)
            return 1.075;
        if (aura == 23856)
            return 1.1;
        if (aura == 30798)
            return 1.25;
        return 1;
    }

    public double getDefenceMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22889)
            return 1.05;
        return 1;
    }

    public double getChanceNotDepleteMN_WC() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22292)
            return 1.1;
        return 1;
    }

    public boolean usingEquilibrium() {
        if (!isActivated() || World.isPvpArea(player))
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 22294;
    }

    public boolean usingPenance() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 22300;
    }

    public int getDivinationEnrichment() {
        if (!isActivated())
            return 0;
        int aura = player.getEquipment().getAuraId();
        if (aura == 30784)
            return 1;
        if (aura == 30786)
            return 2;
        if (aura == 30788)
            return 3;
        if (aura == 30790)
            return 4;
        if (aura == 30792)
            return 5;
        return 0;
    }

    public double getPrayerMultiplier() {
        if (!isActivated())
            return 1;
        int aura = player.getEquipment().getAuraId();
        switch (aura) {
        case 22905: // Corruption.
        case 22899: // Salvation.
        case 23848: // Harmony.
            return 1.01;
        case 22907: // Greater corruption.
        case 22901: // Greater salvation.
        case 23850: // Greater harmony.
            return 1.015;
        case 22909: // Master corruption.
        case 22903: // Master salvation.
        case 23852: // Master harmony.
            return 1.02;
        case 23874: // Supreme corruption.
        case 23876: // Supreme salvation.
        case 23854: // Supreme harmony.
            return 1.025;
        }
        return 1.0;
    }

    public double getPrayerRestoration() {
        if (!isActivated())
            return 0;
        int aura = player.getEquipment().getAuraId();
        switch (aura) {
        case 22905: // Corruption.
        case 22899: // Salvation.
        case 23848: // Harmony.
            return 0.03;
        case 22907: // Greater corruption.
        case 22901: // Greater salvation.
        case 23850: // Greater harmony.
            return 0.05;
        case 22909: // Master corruption.
        case 22903: // Master salvation.
        case 23852: // Master harmony.
            return 0.07;
        case 23874: // Supreme corruption.
        case 23876: // Supreme salvation.
        case 23854: // Supreme harmony.
            return 0.1;
        }
        return 0;
    }

    public void checkSuccefulOutGoingHits(int damage) {
        int aura = player.getEquipment().getAuraId();
        if (aura == 22296)
            useInspiration();
        else if (aura == 22298)
            useVampyrism(damage);
        else if (aura == 41441)
            useGVampyrism(damage);
        else if (aura == 41445)
            useMVampyrism(damage);
        else if (aura == 41446)
            useSVampyrism(damage);
    }

    public int handleInGoingHit(int damage) {
        if (!isActivated() || World.isPvpArea(player))
            return damage;
        int aura = player.getEquipment().getAuraId();
        if (aura == 22897 || aura == 41447 || aura == 35794 || aura == 35796)
            return (int) ((double) damage * 1.15);
        return damage;
    }

    public void useVampyrism(int damage) {
        int heal = (int) (damage * 0.05);
        if (heal > 0)
            player.heal(heal);
    }

    public void useGVampyrism(int damage) {
        int heal = (int) (damage * 0.075);
        if (heal > 0)
            player.heal(heal);
    }

    public void useMVampyrism(int damage) {
        int heal = (int) (damage * 0.1);
        if (heal > 0)
            player.heal(heal);
    }

    public void useSVampyrism(int damage) {
        int heal = (int) (damage * 0.125);
        if (heal > 0)
            player.heal(heal);
    }

    public int getKnockoutChance() {
        if (!isActivated() || World.isPvpArea(player))
            return 0;
        int aura = player.getEquipment().getAuraId();
        if (aura == 20961)
            return 3;
        if (aura == 22933)
            return 5;
        return 0;
    }

    public void useInspiration() {
        Integer atts = (Integer) player.getTemporaryAttributtes().get("InspirationAura");
        if (atts == null)
            atts = 0;
        atts++;
        if (atts == 5) {
            atts = 0;
            player.getCombatDefinitions().restoreSpecialAttack(1);
        }
        player.getTemporaryAttributtes().put("InspirationAura", atts);
    }

    public boolean usingWisdom() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 22302;
    }

    public boolean usingGWisdom() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 41442;
    }

    public boolean usingMWisdom() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 41443;
    }

    public boolean usingSWisdom() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 35830;
    }

    public boolean usingLWisdom() {
        if (!isActivated())
            return false;
        int aura = player.getEquipment().getAuraId();
        return aura == 35832;
    }

    public boolean isWingedAura(int aura) {
        switch (aura) {
        case 22905: // Corruption.
        case 22899: // Salvation.
        case 23848: // Harmony.
        case 22907: // Greater corruption.
        case 22901: // Greater salvation.
        case 23850: // Greater harmony.
        case 22909: // Master corruption.
        case 22903: // Master salvation.
        case 23852: // Master harmony.
        case 23874: // Supreme corruption.
        case 23876: // Supreme salvation.
        case 23854: // Supreme harmony.
        case 22889: // Aegis
            return true;
        }
        return false;
    }

    public int getStatModifier(int skillId) {
        if (!isActivated())
            return 0;
        int auraId = player.getEquipment().getAuraId();
        int level = player.getSkills().getLevelForXp(skillId);
        if ((auraId == 22897 || auraId == 41447) && !World.isPvpArea(player))// berserker aura
            return (int) (skillId == Skills.DEFENCE ? -Math.round((double) level * 0.15) : skillId == Skills.ATTACK || skillId == Skills.STRENGTH ? Math.round((double) level * (auraId == 41447 ? 0.15 : 0.1)) : 0);
        if ((auraId == 35794) && !World.isPvpArea(player))// Reckless aura
            return (int) (skillId == Skills.DEFENCE ? -Math.round((double) level * 0.15) : skillId == Skills.RANGE ? Math.round((double) level * 0.1) : 0);
        if ((auraId == 35796) && !World.isPvpArea(player))// Maniacal aura
            return (int) (skillId == Skills.DEFENCE ? -Math.round((double) level * 0.15) : skillId == Skills.MAGIC ? Math.round((double) level * 0.1) : 0);

        // aura == 35794 || aura == 35796
        return 0;
    }

    public void refreshLoyaltyPoints() {
        player.getPackets().sendGlobalConfig(4660, player.getLoyaltyPoints());
        player.getPackets().sendGlobalConfig(1648, player.getLoyaltyPoints());
        player.getPackets().sendConfig(1234, player.getLoyaltyPoints());
        player.getPackets().sendGlobalConfig(1648, player.getLoyaltyPoints());
    }

    private int[] auraRefreshers;

    public void setAuraRefreshers(int index, int amount) {
        auraRefreshers[index] = amount;
        if (auraRefreshers[0] >= 250)
            auraRefreshers[0] = 250;
        refreshAuraRefreshers();
    }

    public void refreshAuraRefreshers() {
        for (int i = 0; i < auraRefreshers.length; i++)
            player.getPackets().sendConfigByFile(i == 0 ? 40396 : (40130 + (i - 1)), auraRefreshers[i]);
    }

    public void resetAura(int refreshId) {
        if (selectedAura == -1)
            return;
        if (!hasAura(selectedAura))
            return;
        if (isActive(selectedAura)) {
            player.getPackets().sendGameMessage("This aura is already activated.");
            refreshSelectedAura(false);
            return;
        }
        Aura aura = Aura.getAuraByItemId(selectedAura);
        if (aura == null)
            return;
        long cooldown = getCoolDown(aura.getItemId());
        if (Utils.currentUTCTimeMillis() > cooldown) {
            player.getPackets().sendGameMessage("Currently not recharging. It is ready to use.");
            return;
        }
        if (refreshId == 32092) {
            if (!canBeRechargedWithVisWax(aura.getItemId())) {
                player.getPackets().sendGameMessage("You can't reset this aura with vis wax.");
                return;
            }
            int amountInInv = player.getInventory().getAmountOf(32092);
            int amountInBank = player.getBank().containsItemCurrentBank(32092, 1) ? player.getBank().getItem(32092).getAmount() : 0;
            int visWaxAmount = amountInInv + amountInBank;
            int auraType = ItemDefinitions.getItemDefinitions(aura.getItemId()).getCSOpcode(4550);
            int amount = (auraType == 0 ? 10 : auraType == 1 ? 40 : 100);
            if (visWaxAmount < amount) {
                player.getPackets().sendGameMessage("You don't have enough vis wax to do reset this aura.");
                return;
            }
            int removed = 0;
            if (player.getInventory().containsItem(32092, 1)) {
                removed += amountInInv >= amount ? amount : player.getInventory().getAmountOf(32092);
                player.getInventory().deleteItem(32092, removed);
            }
            if (removed < amount)
                player.getBank().removeItem(player.getBank().getItemSlot(new Item(32092)), amount - removed, true, Bank.DESTROY_ITEM);
            player.getPackets().sendGameMessage("You successfully reset " + ItemDefinitions.getItemDefinitions(aura.getItemId()).getName() + " for " + amount + " Vis Wax.");
        } else if (refreshId >= 31847 && refreshId <= 31850) {
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(refreshId);
            if (auraRefreshers[refreshId - 31847 + 1] <= 0) {
                player.getPackets().sendGameMessage("You don't have enough " + defs.getName() + " to do reset this aura.");
                return;
            }
            int type = ItemDefinitions.getItemDefinitions(aura.getItemId()).getCSOpcode(4491);
            if (type == 0) {
                player.getPackets().sendGameMessage("This item only resets combat auras.");
                return;
            }
            int auraTier = aura.getTier();
            int refreshTier = refreshId - 31847 + 1;
            if (auraTier > refreshTier) {
                player.getPackets().sendGameMessage("The tier of reset is too low for this aura. You need tier " + auraTier);
                return;
            }
            auraRefreshers[refreshId - 31847 + 1] -= 1;
            player.getPackets().sendGameMessage("You successfully reset " + ItemDefinitions.getItemDefinitions(aura.getItemId()).getName() + " for 1 " + defs.getName() + ".");
            refreshAuraRefreshers();
        } else {
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(refreshId);
            if (auraRefreshers[0] <= 0) {
                player.getPackets().sendGameMessage("You don't have enough " + defs.getName() + " to do reset this aura.");
                return;
            }
            String auraName = ItemDefinitions.getItemDefinitions(aura.getItemId()).getName().toLowerCase();
            if (aura.getItemId() == 41451 || auraName.contains("legendary wisdom aura") || auraName.contains("jack of trades") || auraName.contains("festive aura")) {
                player.getPackets().sendGameMessage("You can't reset this aura with " + defs.getName() + ".");
                return;
            }
            auraRefreshers[0] -= 1;
            player.getPackets().sendGameMessage("You successfully reset " + ItemDefinitions.getItemDefinitions(aura.getItemId()).getName() + " for 1 Generic " + defs.getName() + ".");
            refreshAuraRefreshers();
        }
        checkRechargeAura();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                refreshComponentOptions(!isActivated());
                refreshSelectedAura(false);
            }
        });
    }

    public static boolean canBeRechargedWithVisWax(int auraId) {
        return auraId == 20959 || auraId == 30806 || auraId == 30808 || auraId == 35828 || auraId == 22302 || auraId == 35830 || auraId == 35832
                || auraId == 41442 || auraId == 41443;
    }

    public boolean showListView;

    public void toggleShowListView() {
        showListView = !showListView;
        refreshShowListView();
    }

    private void refreshShowListView() {
        player.getPackets().sendConfigByFile(39051, showListView ? 1 : 0);
    }

    public void toggleHideDismissAuraWaringMessage() {
        hideDismissAuraWaringMessage = !hideDismissAuraWaringMessage;
        refreshHideDismissAuraWaringMessage();
    }

    private void refreshHideDismissAuraWaringMessage() {
        player.getPackets().sendConfigByFile(39052, hideDismissAuraWaringMessage ? 1 : 0);
    }

    private boolean openOnFavourites;

    public void toggleOpenOnFavourites() {
        openOnFavourites = !openOnFavourites;
        refreshOpenOnFavourites();
    }

    private void refreshOpenOnFavourites() {
        player.getPackets().sendConfigByFile(39053, openOnFavourites ? 1 : 0);
    }

    private boolean autoExtendUnFav, autoExtendFav, autoExtendDoubleTime;

    public void toggleAutoExtendUnFav() {
        autoExtendUnFav = !autoExtendUnFav;
        refreshAutoExtendUnFav();
    }

    private void refreshAutoExtendUnFav() {
        player.getPackets().sendConfigByFile(40069, autoExtendUnFav ? 1 : 0);
    }

    public void toggleAutoExtendFav() {
        autoExtendFav = !autoExtendFav;
        refreshAutoExtendFav();
    }

    private void refreshAutoExtendFav() {
        player.getPackets().sendConfigByFile(40070, autoExtendFav ? 1 : 0);
    }

    public void setAutoExtendDoubleTime(boolean doubleTime) {
        autoExtendDoubleTime = !autoExtendDoubleTime;
        refreshAutoExtendDoubleTime();
    }

    private void refreshAutoExtendDoubleTime() {
        player.getPackets().sendConfigByFile(40071, autoExtendDoubleTime ? 1 : 0);
    }

    public enum Aura {
        ODDBALL_AURA(20957, 0),

        POISON_PURGE_AURA(20958, 1),

        FRIEND_IN_NEED_AURA(20963, 2),

        KNOCK_OUT_AURA(20961, 3),

        SHARPSHOOTER_AURA(20967, 4),

        RUNIC_ACCURACY_AURA(20962, 5),

        SUREFOOTED_AURA(20964, 6),

        REVERENCE_AURA(20965, 7),

        CALL_OF_THE_SEA_AURA(20966, 8),

        JACK_OF_TRADES_AURA(20959, 9),

        GREATER_POISON_PURGE_AURA(22268, 10),

        GREATER_RUNIC_ACCURACY_AURA(22270, 11),

        GREATER_SHARPSHOOTER_AURA(22272, 12),

        GREATER_CALL_OF_THE_SEA_AURA(22274, 13),

        GREATER_REVERENCE_AURA(22276, 14),

        GREATER_SUREFOOTED_AURA(22278, 15),

        LUMBERJACK_AURA(22280, 16),

        GREATER_LUMBERJACK_AURA(22282, 17),

        QUARRYMASTER_AURA(22284, 18),

        GREATER_QUARRYMASTER_AURA(22286, 19),

        FIVE_FINGER_DISCOUNT_AURA(22288, 20),

        GREATER_FIVE_FINGER_DISCOUNT_AURA(22290, 21),

        RESOURCEFUL_AURA(22292, 22),

        EQUILIBRIUM_AURA(22294, 23),

        INSPIRATION_AURA(22296, 24),

        VAMPYRISM_AURA(22298, 25),

        PENANCE_AURA(22300, 26),

        WISDOM_AURA(22302, 27),

        AEGIS_AURA(22889, 28),

        REGENERATION_AURA(22893, 29),

        DARK_MAGIC_AURA(22891, 30),

        BERSERKER_AURA(22897, 31),

        ANCESTOR_SPIRITS_AURA(22895, 32),

        GREENFINGERS_AURA(22883, 33),

        GREATER_GREENFINGERS_AURA(22885, 34),

        MASTER_GREENFINGERS_AURA(22887, 35),

        TRACKER_AURA(22927, 36),

        GREATER_TRACKER_AURA(22929, 37),

        MASTER_TRACKER_AURA(22931, 38),

        SALVATION_AURA(22899, 39),

        GREATER_SALVATION_AURA(22901, 40),

        MASTER_SALVATION_AURA(22903, 41),

        CORRUPTION_AURA(22905, 42),

        GREATER_CORRUPTION_AURA(22907, 43),

        MASTER_CORRUPTION_AURA(22909, 44),

        MASTER_FIVE_FINGER_DISCOUNT_AURA(22911, 45),

        MASTER_QUARRYMASTER_AURA(22913, 46),

        MASTER_LUMBERJACK_AURA(22915, 47),

        MASTER_POISON_PURGE_AURA(22917, 48),

        MASTER_RUNIC_ACCURACY_AURA(22919, 49),

        MASTER_SHARPSHOOTER_AURA(22921, 50),

        MASTER_CALL_OF_THE_SEA_AURA(22923, 51),

        MASTER_REVERENCE_AURA(22925, 52),

        MASTER_KNOCK_OUT_AURA(22933, 53),

        SUPREME_SALVATION_AURA(23876, 54),

        SUPREME_CORRUPTION_AURA(23874, 55),

        HARMONY_AURA(23848, 56),

        GREATER_HARMONY_AURA(23850, 57),

        MASTER_HARMONY_AURA(23852, 58),

        SUPREME_HARMONY_AURA(23854, 59),

        INVIGORATE_AURA(23840, 60),

        GREATER_INVIGORATE_AURA(23842, 61),

        MASTER_INVIGORATE_AURA(23844, 62),

        SUPREME_INVIGORATE_AURA(23846, 63),

        SUPREME_FIVE_FINGER_DISCOUNT_AURA(23856, 64),

        SUPREME_QUARRYMASTER_AURA(23858, 65),

        SUPREME_LUMBERJACK_AURA(23860, 66),

        SUPREME_POISON_PURGE_AURA(23862, 67),

        SUPREME_RUNIC_ACCURACY_AURA(23864, 68),

        SUPREME_SHARPSHOOTER_AURA(23866, 69),

        SUPREME_CALL_OF_THE_SEA_AURA(23868, 70),

        SUPREME_REVERENCE_AURA(23870, 71),

        SUPREME_TRACKER_AURA(23872, 72),

        SUPREME_GREENFINGERS_AURA(23878, 73),

        LEGENDARY_CALL_OF_THE_SEA_AURA(30794, 74),

        LEGENDARY_LUMBERJACK_AURA(30796, 75),

        LEGENDARY_FIVE_FINGER_DISCOUNT_AURA(30798, 76),

        LEGENDARY_QUARRYMASTER_AURA(30800, 77),

        LEGENDARY_TRACKER_AURA(30802, 78),

        LEGENDARY_GREENFINGERS_AURA(30804, 79),

        ENRICHMENT_AURA(30784, 80),

        GREATER_ENRICHMENT_AURA(30786, 81),

        MASTER_ENRICHMENT_AURA(30788, 82),

        SUPREME_ENRICHMENT_AURA(30790, 83),

        LEGENDARY_ENRICHMENT_AURA(30792, 84),

        MASTER_JACK_OF_TRADES_AURA(30806, 85),

        SUPREME_JACK_OF_TRADES_AURA(30808, 86),

        ENLIGHTENMENT_AURA(31434, 87),

        FESTIVE_AURA(26120, 88),

        BRAWLER_AURA(35786, 89),

        GREATER_BRAWLER_AURA(35788, 90),

        MASTER_BRAWLER_AURA(35790, 91),

        SUPREME_BRAWLER_AURA(35792, 92),

        RECKLESS_AURA(35794, 93),

        MANIACAL_AURA(35796, 94),

        DEDICATED_SLAYER_AURA(35798, 95),

        GREATER_DEDICATED_SLAYER_AURA(35800, 96),

        MASTER_DEDICATED_SLAYER_AURA(35802, 97),

        SUPREME_DEDICATED_SLAYER_AURA(35804, 98),

        LEGENDARY_DEDICATED_SLAYER_AURA(35806, 99),

        FOCUSED_SIPHONING_AURA(35808, 100),

        GREATER_FOCUSED_SIPHONING_AURA(35810, 101),

        MASTER_FOCUSED_SIPHONING_AURA(35812, 102),

        SUPREME_FOCUSED_SIPHONING_AURA(35814, 103),

        LEGENDARY_FOCUSED_SIPHONING_AURA(35816, 104),

        FLAMEPROOF_AURA(35818, 105),

        GREATER_FLAMEPROOF_AURA(35820, 106),

        MASTER_FLAMEPROOF_AURA(35822, 107),

        SUPREME_FLAMEPROOF_AURA(35824, 108),

        LEGENDARY_FLAMEPROOF_AURA(35826, 109),

        LEGENDARY_JACK_OF_TRADES_AURA(35828, 110),

        SUPREME_WISDOM_AURA(35830, 111),

        LEGENDARY_WISDOM_AURA(35832, 112),

        MAHJARRAT_AURA(36126, 113),

        ILLUMINATION_AURA(37488, 114),

        DESERT_PANTHEON_AURA(39297, 115),

        HELLION_AURA(41451, 116),

        DWARVEN_INSTINCT_AURA(33646, 117),
        
        GREATER_VAMPYRISM_AURA(41441, 118),
        
        MASTER_VAMPYRISM_AURA(41445, 119),
        
        SUPREME_VAMPYRISM_AURA(41446, 120),
        
        GREATER_WISDOM_AURA(41442, 121),
        
        MASTER_WISDOM_AURA(41443, 122),
        
        GREATER_BERSERKER_AURA(41447, 123),
        
        DAEMONHEIM_AURA_1(29469, 124),

        DAEMONHEIM_AURA_2(29471, 125),

        DAEMONHEIM_AURA_3(29473, 126),

        DAEMONHEIM_AURA_4(29475, 127),
        

        ;

        private static final Map<Integer, Aura> auras = new HashMap<Integer, Aura>();
        private static final Map<Integer, Aura> auras_slots = new HashMap<Integer, Aura>();

        static {
            for (Aura aura : Aura.values()) {
                auras.put(aura.itemId, aura);
                auras_slots.put(aura.slotId, aura);
            }
        }

        private final int itemId;
        private final int slotId;

        Aura(int itemId, int slotId) {
            this.itemId = itemId;
            this.slotId = slotId;
        }

        public int getItemId() {
            return itemId;
        }

        public int getActiveAuraVarbitId() {
            return getAuraActiveVarbitId(itemId);
        }

        public int getChargingAuraVarId() {
            return getAuraRechargingVarId(itemId);
        }

        public int getExtendedVarbitId() {
            return getVisWaxExtendedVarbitId(itemId);
        }

        public long getActivationTime() {
            int mins = ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(AURA_DURATION_ITEM_OP) / 100;
            return mins * 60 * 1000;
        }

        public long getCooldownTime() {
            int mins = ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(AURA_COOLDOWN_ITEM_OP) / 100;
            return mins * 60 * 1000;
        }

        public int getTier() {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(itemId));
            return map.getIntValue(AURA_TIER_STRUCT_OP);
        }

        public int getPrice() {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(itemId));
            return map.getIntValue(AURA_PRICE_STRUCT_OP);
        }

        public Item[] getAllAuraVersions() {
            Item[] versions = new Item[5];
            String itemn = ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase();
            String itemName = itemn.replaceFirst(itemn.startsWith("greater ") ? "greater " : "", "").replaceFirst(itemn.startsWith("master ") ? "master " : "", "").replaceFirst(itemn.startsWith("supreme ") ? "supreme " : "", "").replaceFirst(itemn.startsWith("legendary ") ? "legendary " : "", "");
            for (Aura aura : Aura.values()) {
                String checkName = ItemDefinitions.getItemDefinitions(aura.getItemId()).getName().toLowerCase();
                if (checkName.contains(itemName)) {
                    int index = checkName.startsWith("legendary ") ? 4 : checkName.startsWith("supreme ") ? 3 : checkName.startsWith("master ") ? 2 : checkName.startsWith("greater ") ? 1 : 0;
                    versions[index] = new Item(aura.getItemId(), 1);
                }
            }
            return versions;
        }

        public String getAuraName() {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(itemId));
            return map.getStringValue(AURA_NAME_STRUCT_OP);
        }

        public static Aura getAuraByItemId(int itemId) {
            return auras.get(itemId);
        }

        public static Aura getAuraBySlotId(int slotId) {
            return auras_slots.get(slotId);
        }

        public int getSlotId() {
            return slotId;
        }

        public GeneralRequirementMap getAuraMap() {
            return GeneralRequirementMap.getMap(ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(itemId));
        }

        public String getExamineMessage() {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(ClientScriptMap.getMap(AURAS_ENUM_ID).getIntValue(itemId));
            return map.getStringValue(AURA_EXAMINE_MESSAGE_STRUCT_OP);
        }
    }

    //script_5872
    public static int getAuraRechargingVarId(int aurId) {
        switch (aurId) {
        case 23862:
        case 22917:
        case 20958:
        case 22268:
            return 7660;
        case 20963:
            return 7661;
        case 22933:
        case 20961:
            return 7662;
        case 22272:
        case 23866:
        case 22921:
        case 20967:
            return 7663;
        case 23864:
        case 22919:
        case 20962:
        case 22270:
            return 7664;
        case 22278:
        case 20964:
            return 7665;
        case 22276:
        case 23870:
        case 22925:
        case 20965:
            return 7666;
        case 22274:
        case 23868:
        case 30794:
        case 22923:
        case 20966:
            return 7667;
        case 30806:
        case 30808:
        case 20959:
        case 35828:
            return 7668;
        case 22280:
        case 22282:
        case 23860:
        case 30796:
        case 22915:
            return 7670;
        case 22284:
        case 22286:
        case 23858:
        case 30800:
        case 22913:
            return 7671;
        case 22288:
        case 22290:
        case 23856:
        case 30798:
        case 22911:
            return 7669;
        case 22292:
            return 7672;
        case 22294:
            return 7673;
        case 22296:
            return 7674;
        case 22298:
        case 41441:
        case 41445:
        case 41446:
            return 7675;
        case 22300:
            return 7676;
        case 22302:
        case 35830:
        case 35832:
        case 41442:
        case 41443:
            return 7677;
        case 22889:
            return 7678;
        case 22893:
            return 7679;
        case 22891:
            return 7680;
        case 22897:
        case 41447:
            return 7681;
        case 22895:
            return 7682;
        case 23878:
        case 30804:
        case 22883:
        case 22885:
        case 22887:
            return 7683;
        case 23872:
        case 30802:
        case 22927:
        case 22929:
        case 22931:
            return 7684;
        case 23876:
        case 22899:
        case 22901:
        case 22903:
            return 7685;
        case 23874:
        case 22905:
        case 22907:
        case 22909:
            return 7686;
        case 23848:
        case 23850:
        case 23852:
        case 23854:
            return 7687;
        case 23840:
        case 23842:
        case 23844:
        case 23846:
            return 7688;
        case 30784:
        case 30786:
        case 30788:
        case 30790:
        case 30792:
            return 7689;
        case 31434:
            return 7690;
        case 35786:
        case 35788:
        case 35790:
        case 35792:
            return 7692;
        case 35794:
            return 7693;
        case 35796:
            return 7694;
        case 35798:
        case 35800:
        case 35802:
        case 35804:
        case 35806:
            return 7695;
        case 35808:
        case 35810:
        case 35812:
        case 35814:
        case 35816:
            return 7696;
        case 35818:
        case 35820:
        case 35822:
        case 35824:
        case 35826:
            return 7697;
        case 29475:
            return 7700;
        case 36126:
            return 7698;
        case 41451:
            return 7701;
        case 26120:
            return 7691;
        case 33646:
            return 7704;
        case 37488:
            return 7699;
        }
        return -1;
    }
    
    //script_5873
    public static int getAuraActiveVarbitId(int auraId) {
        switch (auraId) {
        case 20957:
            return 39211;
        case 23862:
        case 22917:
        case 20958:
        case 22268:
            return 39170;
        case 20963:
            return 39171;
        case 22933:
        case 20961:
            return 39172;
        case 22272:
        case 23866:
        case 22921:
        case 20967:
            return 39173;
        case 23864:
        case 22919:
        case 20962:
        case 22270:
            return 39174;
        case 22278:
        case 20964:
            return 39175;
        case 22276:
        case 23870:
        case 22925:
        case 20965:
            return 39176;
        case 22274:
        case 23868:
        case 30794:
        case 22923:
        case 20966:
            return 39177;
        case 30806:
        case 30808:
        case 20959:
        case 35828:
            return 39178;
        case 22280:
        case 22282:
        case 23860:
        case 30796:
        case 22915:
            return 39180;
        case 22284:
        case 22286:
        case 23858:
        case 30800:
        case 22913:
            return 39181;
        case 22288:
        case 22290:
        case 23856:
        case 30798:
        case 22911:
            return 39179;
        case 22292:
            return 39182;
        case 22294:
            return 39183;
        case 22296:
            return 39184;
        case 22298:
        case 41441:
        case 41445:
        case 41446:
            return 39185;
        case 22300:
            return 39186;
        case 22302:
        case 35830:
        case 35832:
        case 41442:
        case 41443:
            return 39187;
        case 22889:
            return 39188;
        case 22893:
            return 39189;
        case 22891:
            return 39190;
        case 22897:
        case 41447:
            return 39191;
        case 22895:
            return 39192;
        case 23878:
        case 30804:
        case 22883:
        case 22885:
        case 22887:
            return 39193;
        case 23872:
        case 30802:
        case 22927:
        case 22929:
        case 22931:
            return 39194;
        case 23876:
        case 22899:
        case 22901:
        case 22903:
            return 39195;
        case 23874:
        case 22905:
        case 22907:
        case 22909:
            return 39196;
        case 23848:
        case 23850:
        case 23852:
        case 23854:
            return 39197;
        case 23840:
        case 23842:
        case 23844:
        case 23846:
            return 39198;
        case 30784:
        case 30786:
        case 30788:
        case 30790:
        case 30792:
            return 39199;
        case 31434:
            return 39200;
        case 35786:
        case 35788:
        case 35790:
        case 35792:
            return 39202;
        case 35794:
            return 39203;
        case 35796:
            return 39204;
        case 35798:
        case 35800:
        case 35802:
        case 35804:
        case 35806:
            return 39205;
        case 35808:
        case 35810:
        case 35812:
        case 35814:
        case 35816:
            return 39206;
        case 35818:
        case 35820:
        case 35822:
        case 35824:
        case 35826:
            return 39207;
        case 29475:
            return 39209;
        case 36126:
            return 39208;
        case 41451:
            return 39210;
        case 26120:
            return 39201;
        case 33646:
            return 38988;
        case 37488:
            return 39241;
        }
        return -1;
    }

    //script_3724
    public static int getVisWaxExtendedVarbitId(int auraId) {
        switch (auraId) {
        case 22889:
            return 24880;
        case 22278:
        case 20964:
            return 24883;
        case 22895:
            return 24904;
        case 22274:
        case 23868:
        case 30794:
        case 22923:
        case 20966:
            return 24886;
        case 22897:
        case 41447:
            return 24903;
        case 35794:
            return 29594;
        case 35796:
            return 29595;
        case 35818:
        case 35820:
        case 35822:
        case 35824:
        case 35826:
            return 29598;
        case 22891:
            return 24901;
        case 23874:
        case 22905:
        case 22907:
        case 22909:
            return 24896;
        case 30784:
        case 30786:
        case 30788:
        case 30790:
        case 30792:
            return 24898;
        case 22294:
            return 24900;
        case 22288:
        case 22290:
        case 23856:
        case 30798:
        case 22911:
            return 24892;
        case 20963:
            return 24888;
        case 23878:
        case 30804:
        case 22883:
        case 22885:
        case 22887:
            return 24893;
        case 23862:
        case 22917:
        case 20958:
        case 22268:
            return 24887;
        case 23848:
        case 23850:
        case 23852:
        case 23854:
            return 24897;
        case 22296:
            return 24885;
        case 23840:
        case 23842:
        case 23844:
        case 23846:
            return 24884;
        case 22933:
        case 20961:
            return 24889;
        case 22280:
        case 22282:
        case 23860:
        case 30796:
        case 22915:
            return 24890;
        case 23864:
        case 22919:
        case 20962:
        case 22270:
            return 24879;
        case 35786:
        case 35788:
        case 35790:
        case 35792:
            return 29593;
        case 22300:
            return 24878;
        case 22284:
        case 22286:
        case 23858:
        case 30800:
        case 22913:
            return 24891;
        case 22893:
            return 24902;
        case 22292:
            return 24899;
        case 22276:
        case 23870:
        case 22925:
        case 20965:
            return 24881;
        case 35808:
        case 35810:
        case 35812:
        case 35814:
        case 35816:
            return 29597;
        case 23876:
        case 22899:
        case 22901:
        case 22903:
            return 24895;
        case 22272:
        case 23866:
        case 22921:
        case 20967:
            return 24882;
        case 35798:
        case 35800:
        case 35802:
        case 35804:
        case 35806:
            return 29596;
        case 23872:
        case 30802:
        case 22927:
        case 22929:
        case 22931:
            return 24894;
        case 22298:
        case 41441:
        case 41445:
        case 41446:
            return 24877;
        case 34759:
        case 34760:
        case 34761:
        case 34762:
            return 29902;
        case 33646:
            return 29903;
        }
        return -1;
    }

    public static final int AURAS_ENUM_ID = 13430, AURA_TIER_STRUCT_OP = 1993, AURA_PRICE_STRUCT_OP = 1932, AURA_VERSIONS_STRUCT_START_OP = 1989, AURA_LEGENDARY_VERSION_STRUCT_OP = 4084, AURA_NAME_STRUCT_OP = 1930, AURA_EXAMINE_MESSAGE_STRUCT_OP = 1931, AURA_DURATION_ITEM_OP = 1430, AURA_COOLDOWN_ITEM_OP = 1429;

}