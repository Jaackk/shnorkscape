# P0: bank keyboard context survives interface close

## Live reproduction and root cause

On 23 September the user isolated the sequence: a working ability key, open and
close `;;bank` or `;;items`, the same key produces no activation, but clicking the
same slot still works. `;;commands` alone leaves the key working. The initial
hypothesis that every chat command breaks keys was therefore corrected.

The captured library transition has IF_BUTTON events before opening and for the
later mouse click, but none for the intervening reported key attempts. The mouse
request reaches ability handling (including a cooldown rejection). This places
the observed keyboard failure before server combat dispatch. Physical key events
are user-observed; the server cannot directly record native OS key events.

Native revision-950 interface517:201 runs CS13353 on load. Instructions227-229
call `8841(24,1)`, acquiring bank keyboard context24. The native CS9299 close
routine calls `8841(24,0)`, cancels bank search through13909, and clears bank UI
state. Our bank and developer library unmounted the interface without running
that routine. Library search cleanup alone releases search context11, not24.

Context24 blocks gameplay context5. Conditional `8838(5)` cannot restore a
context while native focus rules reject it. Context5 callback8136 schedules9035,
which restores native action-bar handlers through6999 when allowed. Existing
bindings and combat state do not need replacement.

User-confirmed recovery probes:

- `;;cs 1364`: no recovery.
- `;;cs 8838 5`: no recovery.
- `;;cs 1998`: recovery, but broadly resets the keyboard context stack.
- Fresh bank open/close, then `;;cs 8841 24 0`: recovery. This is the decisive,
  bank-specific ownership test.

## Scoped correction

Both authoritative real-bank close and developer-library close now send the
original CS9299 before IF_CLOSESUB. Duplicate closes do not release unowned
context. No action-bar remount, global keyboard reset, command-wide reset,
combat scheduler change, cache patch, or saved binding change is introduced.
The existing global library search and isolated developer presets are retained.

Bug Test adds redacted public-chat/command ingress and decoded submission events,
plus a bank-release-requested marker with script9299/context24. Chat content and
command arguments are not recorded by these additions. A release-requested event
means the server queued cleanup, not that native script execution was observed.

## Verification and live gate

Regression coverage checks repeated real-bank/library close cycles through the
actual encrypted950 transport, cleanup ordering before unmount, duplicate closes,
plain commands, and separate player library sessions. Exact script hashes pin
the native lifecycle. The existing complete Java suite covers combat/action-bar
behaviour and the real-cache library acceptance covers global search, full
loadouts, atomic item displacement, custom preset/player isolation and return to
the real bank. Automated packet tests do not simulate physical keyboard input.

The automatic fix is staged, not installed by the agent. After applying through
`Apply Staged Update.cmd`, verify repeated key -> bank/items open -> close -> same
key cycles. Include X and Escape, search active/cancelled, ordinary public chat,
`;;commands`, viewport clicks, bar switching, mouse activation, yellow activation
feedback, cooldown display, Revolution, and two-player combat. The focused manual
release was live-confirmed; automatic cleanup and visual longevity still require
that client check. Do not claim all intermittent input problems solved solely
from this specific reproduction.

Recorded offline results: 1,482 JUnit tests, zero failures/errors, two skipped;
BankAcceptance142 encrypted actions/94 ticks/6,924 frames, including24 repeated
focus-close cycles; LibraryFollowupAcceptance passed; MeleeAcceptance452 ticks/
1,547 frames; CombatStylesAcceptance69 actual-cache checks. BankAcceptance also
verified all pinned scripts against the live cache read-only.

Staged candidate: `dist/p0-bank-focus-20260923`.
Engine SHA256: `0099C24E787CB0030B5D740A9B0F16871BC4006F7093AE6A05C1982C00C1A877`.
Installer check-only verified10 files. Pre-install live engine remained
`46AC9972B84CBE778E68FA58BE09B52241EEE270BE8C6C5191C24CC7773A01F3`;
cache reference remained `21AAE886E340146ED851949C0F900FAE44208BE899E305E7331C12F1D6F44E89`.
The existing library cache payloads and bootstrap classes are unchanged.
Pre-edit source backup: `d97f5a938b36309897df4c2cfb36ce9490baf680`;
local rollback snapshot: `backups/pre-edit-20260923-033246-563`.
