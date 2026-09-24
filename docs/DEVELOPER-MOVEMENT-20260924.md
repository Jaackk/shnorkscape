# Almighty / DM movement cooldowns

`;;god` toggles damage immunity only, with no healing or resource/cooldown changes. `;;almighty` and `;;dm` share one command branch and one session-only Player mode/setter. Individual resource toggles no longer determine whether that mode is active. Existing permissions and logout reset remain intact.

The shared combat cooldown policy gives Surge14726, Escape14665 and the linked Dive47129/Bladed Dive1488 family zero duration in Almighty. Enabling clears existing server deadlines and native timers immediately. Each successful movement publishes zero duration rather than34 ticks; cooldown checks and redraws use the same policy. These movement paths bypass the ordinary manual queue already, so no cooldown queue is introduced. Disabling clears developer timer state and restores the existing34-tick policy for subsequent casts. Other abilities and other players retain their cooldowns.

Exact950 CS6570 branches: Surge instruction2008, Escape1589, Dive/Bladed Dive357. Argument3 updates the client start epoch and argument4 updates its end to current cycle plus `(end-start)*30`. Publishing equal endpoints with both flags1 clears the shared radial/numeric source. No preference toggles or fake overlays are used.

Normal targeting, chosen-tile selection, clipping, animation/effect and force-movement scheduling are unchanged. Existing protection against overlapping an unfinished force move remains; there is no added delay after movement completes. The command directory now states the distinction and wraps long descriptions without truncating the movement information.

Focused regressions cover alias interchange, individual resource overrides, god-only separation, clearing pre-existing timers, repeated zero-duration publication, normal cooldown restoration, native packet endpoints, unrelated abilities and two-player isolation. Existing movement collision/targeting tests remain in the regression suite.

Status: AUTOMATED VERIFIED after successful build; physical Vulkan timer/animation acceptance remains LIVE TEST PENDING. No server restart or live deployment.

## Small live check

1. Enable `;;dm` while movement is cooling down. Repeatedly Surge/Escape/Dive: no cooldown sweep, number, rejection or cooldown queue; normal movement/FX remain.
2. Disable with `;;almighty`, then activate movement: normal cooldowns and visuals return. Enable with `;;almighty` and disable with `;;dm` to confirm the shared toggle.
3. `;;god` alone gives immunity with normal movement cooldowns; another player's movement cooldowns remain independent.
