package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** Owns the management frame and mounts the paired client's native settings pages. */
public final class Native950Settings {
    private static final int ROOT = 1477;
    private static final int HOST = 715; // Management Windows, struct 21301 / param 3505.
    private static final int GAMEPLAY = 1;
    private static final int GRAPHICS = 2;
    private static final int CONTROLS = 3;
    private static final int AUDIO = 4;
    private static final int RIBBON = 5;
    private static final int ACCESSIBILITY = 6;
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private boolean cacheVerified;
    private int page;

    public Native950Settings(Player player, Channel channel) {
        this(player, channel, Native950Settings::verify);
    }

    Native950Settings(Player player, Channel channel, Runnable verifier) {
        this.player = Objects.requireNonNull(player, "player");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        player.getInterfaceManager().setNative950Settings(this);
    }

    public boolean isOpen() { return page != 0; }

    /** Allows the caller to verify before cancelling any existing bank, map or conversation. */
    void verifyBeforeOpen() {
        if (cacheVerified) return;
        verifier.run();
        cacheVerified = true;
    }

    public static boolean isOpenRequest(Native950Actions.InterfaceAction action) {
        if (action.option() != 1 || action.itemId() != -1) return false;
        return (action.interfaceId() == ROOT && action.componentId() == 8 && action.slot() == -1)
                || (action.interfaceId() == 1431 && action.componentId() == 0 && action.slot() == 7)
                || (action.interfaceId() == 1430 && action.componentId() == 256 && action.slot() == -1)
                || (action.interfaceId() == 1433 && action.slot() == -1
                    && (action.componentId() == 15 || action.componentId() == 35 || action.componentId() == 36));
    }

    public boolean handle(Native950Actions.InterfaceAction action) {
        if (isOpenRequest(action)) {
            // The action-bar cog is its combat-mode configuration entry.  Ribbon
            // controls unrelated HUD layout, which made the cog look functional
            // while putting the player on the wrong settings page.
            int destination = action.interfaceId() == 1430 ? GAMEPLAY
                    : action.interfaceId() == 1433 && action.componentId() == 35 ? RIBBON
                    : action.interfaceId() == 1433 && action.componentId() == 36 ? CONTROLS : GRAPHICS;
            if (!isOpen()) open(destination);
            else if (action.interfaceId() == 1430 || action.interfaceId() == 1431 || action.interfaceId() == 1433) {
                // Native gear hooks can close the old frame before notifying us.
                // Always focus Graphics, regardless of that notification timing.
                channel.write(Native950Packets.runClientScript(8179));
                channel.write(Native950Packets.runClientScript(8180, 1, 1));
                channel.write(Native950Packets.interfaceEvents(ROOT, 8, -1, -1, 252));
                render(destination);
            } else close();
            return true;
        }
        if (!isOpen() || action.option() != 1 || action.itemId() != -1)
            return false;
        if (action.interfaceId() == 365 && action.componentId() == 19
                && Native950PendingSettings.isPendingCheckbox(page, action.slot())) {
            if (Native950PendingSettings.isManualOrRevolutionChoice(action.slot())) {
                player.getNative950ActionBar().setRevolutionEnabled(channel,
                        Native950PendingSettings.isRevolutionChoice(action.slot()));
                // 2929 reapplies the client's local preference and can overwrite
                // the just-authoritative varbit.  setRevolutionEnabled has already
                // echoed the saved state, while the checkbox's native onOp redraws.
                return true;
            }
            // Classic remains unavailable: restore the native row rather than claiming it changed.
            channel.write(Native950Packets.runClientScript(2929));
            channel.write(Native950Packets.gameMessage(0,
                    "This combat mode is not available yet on the local server."));
            return true;
        }
        if (page == AUDIO && action.interfaceId() == 429 && action.slot() == -1) {
            int component = action.componentId();
            if (component == 7) {
                channel.write(Native950Packets.runClientScript(9287));
                return true;
            }
            // Checkbox graphics have no onOp hook; their labels and sliders
            // retain the client's own hooks. Reuse its exact mute setter.
            if (component >= 15 && component <= 66 && (component - 15) % 17 == 0) {
                channel.write(Native950Packets.runClientScript(5523, (component - 15) / 17));
                channel.write(Native950Packets.runClientScript(13818));
                return true;
            }
        }
        if (action.interfaceId() != ROOT) return false;
        if (action.componentId() == 717 && action.slot() == 1) {
            close();
            return true;
        }
        if (action.componentId() != 714) return false;
        int next = pageForActor(action.slot());
        if (next == 0) return false;
        // Even a repeat tab click locally hides the content and displays Loading.
        // Acknowledge it by refreshing visibility without recreating its controls.
        render(next);
        return true;
    }

    private void open(int destination) {
        verifyBeforeOpen();
        player.resetWalkSteps();
        player.setRouteEvent(null);
        // Gear/Escape runs the native quick-options hook before its server action.
        // Retire that overlay before opening the full management destination.
        channel.write(Native950Packets.runClientScript(8179));
        // Quick-options can fail before registering its modal context; its
        // cleanup then disables the base Options key without restoring it.
        channel.write(Native950Packets.runClientScript(8180, 1, 1));
        // While management is visible, root 8's Escape hook explicitly emits
        // close actor 717. The native router runs that hook before checking
        // this mask: suppress only the second, generic Options notification.
        // Keep native operations 2..7 unchanged (cache mask 254, minus bit 1).
        channel.write(Native950Packets.interfaceEvents(ROOT, 8, -1, -1, 252));
        channel.write(Native950Packets.openSub(ROOT, HOST, 1448, true));
        player.getInterfaceManager().registerNativeOpen(1448, ROOT, HOST);
        for (int bit : new int[] {19029, 19031, 19032, 19033, 47565, 60056, 19004})
            channel.write(Native950Packets.varbitSmall(bit, 0));
        channel.write(Native950Packets.varbitSmall(18994, 9));
        channel.write(Native950Packets.varcLarge(2911, 9));
        render(destination);
    }

    private void render(int next) {
        boolean replace = page != next;
        if (replace) removePage();
        page = next;
        channel.write(Native950Packets.varbitSmall(19001, next));
        channel.write(Native950Packets.runClientScript(8288, 9));
        channel.write(Native950Packets.runClientScript(8193));
        // 365's Accessibility selector checks the visible management wrapper
        // as well as page 19001. Make both available before its onLoad runs.
        channel.write(Native950Packets.hideInterface(ROOT, 708, false));
        if (replace && next == ACCESSIBILITY)
            channel.write(Native950Packets.runClientScript(20387));
        if (replace) {
            if (next == GRAPHICS) {
                channel.write(Native950Packets.openSub(1448, 3, 1426, true));
                channel.write(Native950Packets.openSub(1426, 0, 742, true));
                player.getInterfaceManager().registerNativeOpen(1426, 1448, 3);
                player.getInterfaceManager().registerNativeOpen(742, 1426, 0);
                // 742 onLoad 2594/2595 selects and opens its native renderer's page.
            } else if (next == AUDIO) {
                // 8784 expects slot 0 to be populated while management is open.
                // Use the empty verified frame, hidden, in place of the music catalogue.
                channel.write(Native950Packets.openSub(1448, 3, 1426, true));
                channel.write(Native950Packets.openSub(1448, 5, 429, true));
                player.getInterfaceManager().registerNativeOpen(1426, 1448, 3);
                player.getInterfaceManager().registerNativeOpen(429, 1448, 5);
            } else {
                // Gameplay and Accessibility share 365, but its onLoad reads
                // the selected page. Recreate it when changing between them.
                int interfaceId = pageInterface(next);
                channel.write(Native950Packets.openSub(1448, 3, interfaceId, true));
                player.getInterfaceManager().registerNativeOpen(interfaceId, 1448, 3);
            }
        }
        // Tab refresh 8186 -> 8282 reapplies this same native geometry later.
        // Audio must occupy its own 539px slot 1, not the 209px music column.
        channel.write(Native950Packets.runClientScript(8283, pageStruct(next), 0));
        if (next == AUDIO) channel.write(Native950Packets.runClientScript(8283, 21183, 1));
        channel.write(Native950Packets.hideInterface(1448, 3, next == AUDIO));
        if (next == AUDIO) channel.write(Native950Packets.hideInterface(1448, 5, false));
        channel.write(Native950Packets.hideInterface(1448, 1, true));
        channel.write(Native950Packets.hideInterface(ROOT, 708, false));
        if (next == GAMEPLAY) {
            pendingCheckboxEvents(2);
            player.getNative950ActionBar().refreshRevolution(channel);
        }
        for (int tab = GAMEPLAY; tab <= ACCESSIBILITY; tab++)
            channel.write(Native950Packets.interfaceEvents(ROOT, 714, tab * 4 - 1, tab * 4 - 1, 2));
        channel.write(Native950Packets.interfaceEvents(ROOT, 717, 1, 1, 2));
    }

    private void removePage() {
        if (page == GAMEPLAY) pendingCheckboxEvents(0);
        if (page == GRAPHICS) {
            channel.write(Native950Packets.closeSub(1426, 0));
            channel.write(Native950Packets.closeSub(1448, 3));
            player.getInterfaceManager().unregisterNativeOpen(742);
            player.getInterfaceManager().unregisterNativeOpen(1426);
        } else if (page == AUDIO) {
            channel.write(Native950Packets.closeSub(1448, 5));
            channel.write(Native950Packets.closeSub(1448, 3));
            player.getInterfaceManager().unregisterNativeOpen(429);
            player.getInterfaceManager().unregisterNativeOpen(1426);
        } else if (page != 0) {
            channel.write(Native950Packets.closeSub(1448, 3));
            player.getInterfaceManager().unregisterNativeOpen(pageInterface(page));
        }
    }

    private void pendingCheckboxEvents(int mask) {
        // The native onOp hook runs before this server-notification gate.
        // Local preferences retain the cache's zero mask; only waiting combat
        // choices need the server's rejection reply. Retire overrides on exit.
        channel.write(Native950Packets.interfaceEvents(365, 19, 10240, 10242, mask));
        channel.write(Native950Packets.interfaceEvents(365, 19, 15872, 15872, mask));
    }

    private static int pageForActor(int actor) {
        if (actor < 3 || actor > 23 || (actor + 1) % 4 != 0) return 0;
        return (actor + 1) / 4;
    }

    private static int pageInterface(int page) {
        switch (page) {
            case GAMEPLAY: case ACCESSIBILITY: return 365;
            case CONTROLS: return 1444;
            case RIBBON: return 567;
            default: throw new IllegalArgumentException("Not a single-interface settings page: " + page);
        }
    }

    private static int pageStruct(int page) {
        switch (page) {
            case GAMEPLAY: return 21179;
            case GRAPHICS: return 21182;
            case CONTROLS: return 21181;
            case AUDIO: return 21183;
            case RIBBON: return 44487;
            case ACCESSIBILITY: return 52418;
            default: throw new IllegalArgumentException("Unknown settings page: " + page);
        }
    }

    public void close() {
        if (!isOpen()) return;
        // Native entry hooks can leave the quick overlay visible. Retire it
        // whenever this owned management window exits as well.
        channel.write(Native950Packets.runClientScript(8179));
        channel.write(Native950Packets.runClientScript(8180, 1, 1));
        // The native close script uses the current client page for cleanup.
        channel.write(Native950Packets.varcLarge(2911, -1));
        channel.write(Native950Packets.runClientScript(8290, 1));
        removePage();
        page = 0;
        channel.write(Native950Packets.closeSub(ROOT, HOST));
        channel.write(Native950Packets.hideInterface(ROOT, 708, true));
        player.getInterfaceManager().unregisterNativeOpen(1448);
        channel.write(Native950Packets.interfaceEvents(ROOT, 8, -1, -1, 254));
    }

    public static synchronized void verify() {
        if (verified) return;
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Settings require the paired flat cache");
        // Paired 950 evidence: protocol-analysis/ui-scripts-950-evidence.json.
        // Native scripts own the changed 450px sizing, graphics and option hooks.
        Native950PendingSettings.verify();
        pinInterface(1448, 32, "2ebcf3b458292e793f89afecd25d7bb6d737e0c2ad75573a0d789952e3d94ea6");
        pinInterface(1426, 1, "867edc1e3ceb2b9cc1eb8d41f34f2cca9417b37a3f51f280fc4c48d17edc3de8");
        pinInterface(742, 7, "b99e5735457f00785c971825e94a5f408f7375c327fc00d6d1180422cddcb5f5");
        pinInterface(324, 50, "208dcb7776546ba68afb032a48f93d38d541884f9e24b549000e7fb18b079cc0");
        pinInterface(1513, 134, "789d042ca0f5f99d490d3ac236d63d2e65d558bbe5f1c3801bf26650cf09c487");
        pinInterface(429, 82, "fb2ed5aeb025b8234c4f56efa23de7404512e209c8d53957085b8140c27f99be");
        pinInterface(365, 46, "6886838ce908b8dcd947f418a82fe9f4859252354ed7d16a530f0534fbd725ea");
        pinInterface(1444, 1077, "b02584dfb4e5544564f73648becb9cd003c278d6975b0cd634f3dda2c0bc0371");
        pinInterface(567, 46, "680e13b3e7eb0cd1d14fc55d7dc09b2d77979f1d7c70eee992973553d8dd41c9");
        pin(22, 661, 27, "c5dc7946d0f7c95a03190268d439e7b8225fd4aed2f2d8e1ddba6cb7647c670d");
        pin(22, 661, 29, "5713c8d9386d0a0bf859e6e118f37508f791383132be6ebb6d0abebd3daa672f");
        pin(22, 1390, 7, "4d65bbd48d242ff26d82a82a5d41e9fb46004b410d13b66d28906da5f6f508b8");
        pin(22, 1638, 2, "e44655895c3e4cc22ce0f42838588ae0ac82e4bae334a2fa06bd6e9e1fd3bc78");
        pin(12, 2923, 0, "d2e0f5f4e99f33f8cd000f7b227388baf7bb89e18c5b04254c15c93437f84d6e");
        pin(12, 2929, 0, "93fc7a634dc6b7cd7a34cb99176f4b3f30210573455f6cd10610d5f3ed685154");
        pin(12, 2957, 0, "72715e916881eb82364e5753547b0cada9a6855a038a2141fba6286dbc8f0503");
        pin(12, 20382, 0, "aa70b5f3b5b7c2bdecfdcb607b24206b5e164ab8b4e74d9d80039155b18898d3");
        pin(12, 20387, 0, "3214cf53c9ee77450142dec1bedb0c842b66f62f6d2ea3c49fdf34ca019cfa9e");
        pin(12, 8292, 0, "57782e6088f1c134ecc34da3203377d4c23da622af9068ed0d4a72035c34059c");
        pin(12, 2930, 0, "db4ef79cbd32c575798641b304340aa7f813741c87a498d6aa2863e7275aa2d1");
        pin(12, 2514, 0, "873891f0e7305844a82f8f7acfec0f6810aab0df96f29e0627ebc2831e156c42");
        pin(17, 36, 150, "6cf2140de8a2167ad7d7197c134469d9449c8060963831dcf8bf70e584335620");
        pin(17, 56, 232, "cfe1998fe6508f648e6bc7a678e44148d683a572b55973b9f70877414b80a8d2");
        pin(17, 56, 233, "1561a5915e9ff523af4b23325e49b58170ab6d35109f81adda4c281c22b24636");
        pin(12, 8203, 0, "c35a26c44fa62c09162ee76cb8964e01d7f0cf813155d04c8851325bba842cf2");
        pin(12, 8206, 0, "2b49e05566cadb512a411033a85fb532ba12ee21b7765f5623e66e1739c71f60");
        pin(12, 8208, 0, "f90cf91efde5652bbe82bf7a279b38a3c3dda72a8d9b9d247a09fe0031347506");
        pin(12, 8209, 0, "8b5ed43bb2566a21f5f71ea5343a57252baf6865820c1d1a34604da3a1a880de");
        pin(12, 8817, 0, "29bb5ac066f28852b70d004ce43101799a3c0437f811a0b85ec89077d12766b0");
        pin(12, 8822, 0, "e3fc7cb2b7b55046b3f71d96f0e7507141d646702058413e54691afd46202599");
        pin(12, 8823, 0, "833af5cfbe6847fb93d163224007f5bf72257d80e2c1611e3d100f0b68f7dab1");
        pin(12, 8826, 0, "fa440c0cdb370359f6a070fee8b5e3191d83bbe4da0994a7d2ae16efd8062fc4");
        pin(12, 8212, 0, "e1f04f5ac9354bfdbda03483ce40ef601d4b0a16468bdde837cafc3f00426c86");
        pin(12, 2755, 0, "6bb83530169cf83a78ca40e1a8836c59f3ce84eaf0cc0180aaa412020185fe1e");
        pin(12, 13849, 0, "d1f46b29f347e5f7f051a20637f97203f30fc37093062d35b17d02d5cf0b9f88");
        pin(12, 10416, 0, "2a76e0eccc926740397aa88f2268d3b836bf8ada89132fdca19762d8428ac2ad");
        pin(12, 10419, 0, "f43c5b948aea56169ea7c3f26023b9cdf4e20e8b215fcc68257976d25211933d");
        pin(12, 14457, 0, "bb99ca871cb0f8a6faf9a3959ae07f5c2b52eadde8976be9408c3b1fd67925bf");
        pin(12, 14458, 0, "42ebdfde639f2849e2ff9732800f72674c7f3d0c0cadef4f0a4a46b8b5536bf7");
        pin(12, 10422, 0, "c76616b5e2eb6cca5427aa4586724d16d1a0df85216bfe7aafe1240c07790e39");
        pin(12, 13837, 0, "11d6462edc866caf40dddb4b780f793987dba5917629589647df921a34132e1a");
        pin(12, 13838, 0, "f76dac43e5a5915569e2b5cc4bb632027e83b261637b16006c168662884814d5");
        pin(12, 13839, 0, "b37e3c44ec878e5311b5fbf5449d8981b3caee3ba400b4419f7ed97aa24e6e4a");
        pin(12, 13848, 0, "caf8992a78af2184e1b5d0d9e35b03f5d2b96d48f7154ebc6be362b8d4938916");
        pin(12, 2396, 0, "f0c6f53a8e29598315bef0ab4e5335594e25e592add8cedad1d762853022b654");
        pin(12, 2395, 0, "7b070179aa40c7004b4262e5f09feb2b793b012dabd63c358e539f9294157f85");
        pin(12, 2393, 0, "0d69206dd1054c076eba9646ddf80b2abd3233e19a0de8896f40f93f7a39916a");
        pin(12, 2386, 0, "c1ce536c230289798e04629fc669d4733e02eac45f1af5d6abe071057a96d7f3");
        pin(12, 16021, 0, "4683dfbddfdcedd7d43de2b21cb761c26e1985b6ae9542020760a945902fa280");
        pin(12, 13860, 0, "cd8d412686fa04991043f7325b594af7174febaa101c10791fd964d784d0b459");
        pin(12, 13861, 0, "08d8485b7449726ab148ca2aabffac3b0c9199692d445cb374a2b0230fb613a1");
        pin(12, 13862, 0, "cd8f053146c5b50aa58ad98942ae3e3016d3433784a364e73733fedd32901fed");
        pin(12, 1159, 0, "ee347e0223b8c1a4bdc7a89364565f0c97b68cf36f0050b581c11b3af930561d");
        pin(3, 1477, 8, "e6c3e8f186fcb9cd9f1504368bbd8a41cd75a1e7bb28cbdd7587c5ef57aa63d2");
        pin(3, 1477, 27, "af6e31a36e133311dfedb970fe23c023efd8635d947d0541f9619c16e866c838");
        pin(3, 1477, 707, "827650aaeba23495f848bf95b6710ca7c79333949e4e0b6112221d7796259315");
        pin(3, 1477, 708, "3e454a7af0f4a8106429d0c90875df7f406610580a5a8e44df496e2d4227cf33");
        pin(3, 1477, 714, "aa13d9a89d7e8a4e145bdf1f3f69c7834e594690c49d2f60f81c3332fb8a94f7");
        pin(3, 1477, 715, "095e273fec6e74353c55cdc5af1730c80b0481c6917c57c0aeed8af51ef8c628");
        pin(3, 1477, 717, "aa13d9a89d7e8a4e145bdf1f3f69c7834e594690c49d2f60f81c3332fb8a94f7");
        pin(3, 1477, 805, "204d4eb90f76fd4663fdbc34efd6bd8ba6f3dbd0b651fc1ff8f7e3a0e1d67e59");
        pin(22, 665, 21, "04d8d253cae574d2aaea81803423d9b0b53feee736ca1651676bf09797e1cbfb");
        pin(22, 661, 26, "badfabb1ffb28788399c8f1690163236e5d3092e50bcbb8bbaf10a556c10fa21");
        pin(22, 661, 30, "be5bbea07ad1a84a30b6938cf653c9380e3390d0de08457a1794c58eaa48fc9f");
        pin(22, 661, 31, "08d4fc9451cede91582822e61510045da5c1c868393ee3785d748778e9bb0838");
        pin(17, 30, 19, "2b9f7497b9bb38a37497a468a3bfb9f6e441ce13b7821fd4993a30a7c2ccd41d");
        pin(2, 69, 18994, "a48884ff0b514869f33d3cfd5cbf8c1bdaff405696df8417859f72a089a12ae7");
        pin(2, 69, 19001, "fe5da41c9242c1245d9389dc632dc5a3bc4ee491625d4736f04dbdb31b0ea26d");
        pin(2, 69, 19004, "a3a6d30f4983979a62832618b2b6a53d1d01ee38d4788c0a35f2ef3615e588c4");
        pin(2, 69, 19029, "523f6629295ff170f20b766e373ba0fe65e29c72a70842d15fb0760798db6cd0");
        pin(2, 69, 19031, "620cd4b061bcb95578a6577fd9ec79ace8c3fb46337d2ba9a9415bc2f3000688");
        pin(2, 69, 19032, "76087972ec934a68e8c94f8a5971bcabbc583b3407f5c1f7bc51098d723aa416");
        pin(2, 69, 19033, "abcdacb60e29a11bee869d18a77b0cd30e58c9bccee2bc3b9e49132884e1272d");
        pin(2, 69, 47565, "30b98917b18436e20583f9f312a825b42a658a9cc5f692226db864b79abd1e35");
        pin(2, 69, 60056, "8890278b507db50d4cb0ba9bb9b568c7d8b5df2f14ea56037f6402ed92204db2");
        pin(12, 8179, 0, "6129f1fc028f9ba3fb259cc22ebe23d5b01c31feaa22a7a0e7b82ec880a3c888");
        pin(12, 8181, 0, "e852b15a50764ee4550b3d16ffcdb3d9fa6ed483fdae866b908619c7e611d7ae");
        pin(12, 8286, 0, "5bd6296bb761633d83eb47a86757815ee88d0369ab844439a11f13792f24d5f7");
        pin(12, 8288, 0, "bd7585328d7c15e6597038b0b19df5239fb44abd343253a17ec52355e42b0f59");
        pin(12, 8290, 0, "2e4f3cac7fbbb9799bfa079ba47898f797c9d20e36517bb0787040f04520a2b8");
        pin(12, 8192, 0, "14d87898eca9c7d5e5145cc1555d362fbc7ac5e2fcfa5944b34ae6e7599c72fe");
        pin(12, 8193, 0, "9b858e1346354d255818898aa4941900645c18d4695a0bdaf3841a83f5157a07");
        pin(12, 8283, 0, "f66af3100f2777485313489b2c09d2c666557ce817f3e01d9fd669ba082f8f1d");
        pin(12, 12343, 0, "d7d2ff738e0d536a63b902b5dc3e810bfb4bc32840375d1127c359afe879e5d3");
        pin(12, 8184, 0, "9b5d7c71076d76b1f8be3d6cd234f7f63d829d2f63debf1e5e071d4d0c7ec676");
        pin(12, 8186, 0, "d2fcafc03a569cec8ae43f46144405f592243c6f601e364f1d179566790cf889");
        pin(12, 8190, 0, "80760f2c61f21dcb47c768a312a1fc5a62ae28fa42b5444f166df72d52bcc046");
        pin(12, 8191, 0, "f5afed8d1d318160c6d8ffa8c24655e0d571039f084bf66039da07fbeb0f4174");
        pin(12, 10068, 0, "40c21b166a82aec9fa411bad082685442e56cf8cf6b385395145698691460ff5");
        pin(12, 8284, 0, "15b23d7948bc40231aed6d4020460c93b8063017085cabc6e5aad1db99a9646d");
        pin(12, 8285, 0, "60211f3f3b230a69bfb5cd87ba811714647f9d57e2d7b9fbaa1ade34c2789011");
        pin(12, 441, 0, "14de94d23b844205197a9639efe8348a9e9d55f3ae984644298568b430cdfd5e");
        pin(12, 445, 0, "5b6b61135a9a62f52f54c0f38ef1054e8f6ac3dbd93e1653263c48cac47af6b0");
        pin(12, 2594, 0, "aac66b3dfbd9287d7d053665dc59ef20270d56e60aad348533b1220545f8e491");
        pin(12, 2595, 0, "17959ca81ef94d6f27e6f6436eb9b203647f044b09d30e49f0d1c2e51c324a63");
        pin(12, 8043, 0, "245137705de7dc1331c07e3d391074f46134375b890729fff4c698caa1241c0b");
        pin(12, 13818, 0, "8c5b4afbeac0b2f832fad148b0ad79ffb68da2db1505827bb5d44d934baa6249");
        pin(12, 13819, 0, "c5e8a25fc3f7f4764083f3e65c4cfbd88f4b1a81386ad556dbd14c64cfdf519b");
        pin(12, 13820, 0, "cd22be93f7f6fba8a347da8de196128fbd73356ce267644ddca64fe22c9d04f0");
        pin(12, 13821, 0, "78af5e37748f37e71b0d23ed7358908146103556a4a1cc730c40c53d93d9c0c0");
        pin(12, 13816, 0, "020b99a4cbc9ebb7dcc293dd10277223412a23ff1182cc492002dd9b8e98d438");
        pin(12, 15087, 0, "8f50c183a1bf0d2ec1c1ea006ecaef5f1bac6a15aa1f631c6a85cb9df6702958");
        pin(12, 8784, 0, "ff19bed8f57e5de821ea8ff76a76e3c875ccb0077085f7a0df63a038facf8221");
        pin(12, 8282, 0, "40c82f6a365f2725e2a36f935cb02a3222d06a713778b9c011a68115e85b3e39");
        pin(12, 9287, 0, "c89a0925434dd5df73e152bf0cb7e33a441365f0acab2b788e412e724ab67c02");
        pin(12, 5523, 0, "b2c9ff15b10d5647d1681b2e97429ecf0c8c4a2f565ec107e66faf5c615fc87c");
        pin(12, 5633, 0, "34ba27d67e46be4b784cca60e3ff1155a404e7ee52fe7c98b8fee1602b37dd7f");
        pin(12, 5639, 0, "0bc9309ac40897cd3576856b6af38c430f481a28b4087985801c4967dfd85a50");
        pin(12, 1191, 0, "9ca89426398a6cd3aa09d63bbad14304ae6cb1e3c6af8942f1e8ecd411b1d8a6");
        pin(12, 10002, 0, "e79c182392a669222b5ccf2aeeca07ef723322db5e55700d7764d638d430fef1");
        pin(12, 10020, 0, "ce40f01d95a24572b5ad6f3c91cacb7896b5ad28608211c074f76e49c2874b6c");
        pin(12, 2798, 0, "e245c5568a5ab695a1fcdd85f16e4529504073941397955dbc2d504a0068acc0");
        pin(12, 8180, 0, "1146e4c31c6251a2246d6a3aa9cf20af860f58dd1bbf39ebec294a22f746e0ae");
        pin(12, 8844, 0, "05882a24def6f4008980c46787b5d4e32176ea84e31a2fc160e4f74dd3f60a05");

        verified = true;
    }

    private static void pinInterface(int interfaceId, int count, String expected) {
        MessageDigest digest = digest();
        for (int file = 0; file < count; file++) digest.update(file(3, interfaceId, file));
        requireDigest(digest, expected, "interface " + interfaceId);
    }

    private static void pin(int index, int group, int file, String expected) {
        MessageDigest digest = digest();
        digest.update(file(index, group, file));
        requireDigest(digest, expected, index + "/" + group + "/" + file);
    }

    private static byte[] file(int index, int group, int file) {
        byte[] data = Cache.STORE.getIndexes()[index].getFile(group, file);
        if (data == null) throw new IllegalStateException("Missing settings cache file " + index + "/" + group + "/" + file);
        return data;
    }

    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static void requireDigest(MessageDigest digest, String expected, String binding) {
        StringBuilder actual = new StringBuilder();
        for (byte value : digest.digest()) actual.append(String.format("%02x", value & 255));
        // Routed through the shared gate so a stale 947-era pin can be downgraded to a
        // report during the 950 port instead of blocking login entirely.
        NativeCacheVerification.requireBinding("Settings", binding, expected, actual.toString());
    }
}
