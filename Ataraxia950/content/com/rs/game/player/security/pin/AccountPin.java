package com.rs.game.player.security.pin;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AccountPin implements Serializable {

    public static final String COLOR = Colors.RCYAN;
    public static boolean DISABLED = true;
    private static final long serialVersionUID = -5341315664465458200L;

    public static boolean isValidPin(String input) {
        return input.matches("^[0-9]*$");
    }

    public static void openSettings(Player player) {
        player.getDialogueManager().startDialogue(new DAccountPinSettings());
    }

    public static void openPinInput(Player player, Consumer<String> action, Consumer<String> invalidAction) {
        player.sendInputString("Enter the PIN", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String pin = getString();
                if (isValidPin(pin)) {
                    action.accept(pin);
                } else {
                    invalidAction.accept(pin);
                }
            }
        });
    }

    public static void openPinInput(Player player, Consumer<String> action) {
        openPinInput(player, action, action);
    }

    public static void openCreatePin(Player player) {
        player.getDialogueManager().startDialogue(new DCreateAccountPin(false, false));
    }

    public static void openChangePin(Player player, boolean forgot) {
        player.getDialogueManager().startDialogue(new DCreateAccountPin(true, forgot));
    }

    public static void openEnterPin(Player player, Consumer<Dialogue> success, Consumer<Dialogue> fail) {
        player.getDialogueManager().startDialogue(new DEnterAccountPin(success, fail));
    }

    public static void openEnterPin(Player player) {
        openEnterPin(player,
                dialogue -> {
                    player.getAccountPin().resetLocked();
                    player.getAccountPin().setPinEntered();
                    player.getAccountPin().resetPinDelay();
                    player.sendMessage(AccountPin.COLOR + "You have unlocked your account.");
                    player.getAccountPin().notifyChecked();
                    player.getAccountPin().sendModStatus();
                    dialogue.end();
                },
                dialogue -> openEnterPin(player));
    }

    public static void openChangeRecoveryDelay(Player player) {
        player.getDialogueManager().startDialogue(new DChangeRecoveryDelay());
    }

    private transient Player player;

    private transient boolean enteredPin;

    private String pin;

    private int recoveryDays = DChangeRecoveryDelay.MIN_RECOVERY_DELAY;

    private Map<String, AccountPinModification> pendingMods;

    private int pinDelay;

    private boolean locked;
    private boolean checked;

    public AccountPin(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void lock() {
        if (DISABLED || player.isOwner()) {
            return;
        }
        activateMod();
        String currentIP = player.getIP();
        String lastIP = player.lastPinIP;
        if (locked || !hasPin() || !Objects.equals(currentIP, lastIP)) {
            forceLock();
        } else {
            notifyChecked();
            enteredPin = true;
            sendModStatus();
        }
    }

    public void sendModStatus() {
        Collection<AccountPinModification> modValues = mods().values();
        if (modValues.size() > 0) {
            player.sendMessage("You have pending changes to your PIN settings. You can cancel them through the banks at ;;home");
        }
    }

    public void forceLock() {
        locked = false;
        enteredPin = false;
        player.getDialogueManager().startDialogue(new DAccountPin());
    }

    public boolean hasPin() {
        return pin != null;
    }

    public boolean hasEnteredPin() {
        if (DISABLED || player.isOwner()) {
            return true;
        }
        return !hasPin() && !player.hasCompleted() || enteredPin;
    }

    public void setPinEntered() {
        enteredPin = true;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public String getFormattedPin() {
        if (pin == null || pin.isEmpty())
            return "null";
        StringBuilder sb = new StringBuilder();
        for (char c : pin.toCharArray()) {
            sb.append(c).append('-');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public int getRecoveryDays() {
        return recoveryDays;
    }

    public void changePin(String newPin) {
        LocalDateTime activationDate = Settings.TEST_SERVER_MODE ?
                LocalDateTime.now().plusMinutes(recoveryDays) :
                LocalDateTime.now().plusDays(recoveryDays);
        String key = "Change PIN";
        if (mods().putIfAbsent(key, new AccountPinModification(newPin, recoveryDays, activationDate, key)) == null) {
            player.sendMessage(COLOR + "Your PIN will be changed in " + recoveryDays + " days.");
        }
    }

    public void changeRecoveryDays(int newRecoveryDays) {
        LocalDateTime activationDate = Settings.TEST_SERVER_MODE ?
                LocalDateTime.now().plusMinutes(recoveryDays) :
                LocalDateTime.now().plusDays(recoveryDays);
        String key = "Change recovery delay";
        if (mods().putIfAbsent(key, new AccountPinModification(pin, newRecoveryDays, activationDate, key)) == null) {
            player.sendMessage(COLOR + "Your recovery delay will be changed in " + recoveryDays + " days.");
        }
    }

    public Long getModTime(String key) {
        AccountPinModification pinMod = mods().get(key);
        if (pinMod == null) {
            return null;
        }
        return LocalDateTime.now().until(pinMod.getActivationDate(), ChronoUnit.HOURS);
    }

    public void activateMod() {
        if (mods().isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Iterator<AccountPinModification> iterator = mods().values().iterator();
        while (iterator.hasNext()) {
            AccountPinModification mod = iterator.next();
            if (now.isAfter(mod.getActivationDate())) {
                pin = mod.getPin();
                recoveryDays = mod.getRecoveryDays();
                if (mod.getDescription().equals("Change PIN")) {
                    player.sendMessage(COLOR + "Your new PIN is active.");
                } else if (mod.getDescription().equals("Change recovery delay")) {
                    player.sendMessage(COLOR + "Your new recovery delay is active.");
                }
                iterator.remove();
            }
        }

    }

    public void setLocked() {
        locked = true;
    }

    public void resetLocked() {
        locked = false;
    }

    public void increasePinDelay() {
        pinDelay++;
    }

    public void resetPinDelay() {
        pinDelay = 1;
    }

    public int getPinDelay() {
        if (pinDelay < 1) {
            pinDelay = 1;
        }
        return pinDelay;
    }

    public Map<String, AccountPinModification> mods() {
        if (pendingMods == null)
            pendingMods = new HashMap<>(5);
        return pendingMods;
    }

    public void setChecked() {
        checked = true;
    }

    public void notifyChecked() {
        if(checked) {
            checked = false;
            player.sendMessage(AccountPin.COLOR + player.username + " has checked your account PIN. If you did not consent to this, please speak with a staff member.");
        }
    }
}
